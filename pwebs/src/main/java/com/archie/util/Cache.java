package com.archie.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.net.telnet.TelnetClient;

import net.rubyeye.xmemcached.HashAlgorithm;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

/**
 * @category memcached内存数据库操作类
 * @version 1.0
 */
public class Cache {
	// 如果没有memcached则取用本地缓存
	protected static HashMap<String, Object> localcached = new HashMap<String, Object>();
	// 是否启用MemCached内存数据库
	protected static boolean enUsed = true;

	// 创建全局唯一的可实例化对象
	protected static Cache memCached = new Cache();

	// 初始化MemCached客户端对象
	protected static MemcachedClient memClient = null;

	// 定义可用的MemCached服务器列表，用于分布式存储
	private static String serverListArr = "";

	// 定义各MemCached服务器的负载权重列表，与服务器列表按先后顺序对应
	private static int[] weightListArr = new int[1];

	// 定义MemCached服务器运行环境表，配置文件中关于参数相关数据将保存到该表
	private static Map<String, String> serverConfig;

	// 定义MemCached服务器运行状态表，用于保存各状态的中文解释
	protected static HashMap<String, String> statsItems;

	/**
	 * @category 初始化MemCached运行环境配置
	 * @category 注：该方法在整个服务器周期内仅运行一次
	 */
	protected static void initConfig() {
		// 初始化可用的MemCached服务器列表默认值（本机）
		serverListArr = "127.0.0.1:11211";
		weightListArr = new int[1];
		weightListArr[0] = 1;
		// 初始化MemCached服务器运行环境表（默认值），当某参数未在配置文件中进行定义时，将使用该默认值
		serverConfig = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put("initConn", "5"); // 设置初始连接数
				put("minConn", "5"); // 设置最小连接数
				put("maxConn", "250"); // 设置最大连接数
				put("maxIdle", "21600000"); // 设置连接最大空闲时间（6小时）
				put("maintSleep", "30"); // 设置主线程的睡眠时间（30秒）
				put("socketTO", "10000"); // 读取操作的超时限制（10秒）
				put("socketConnTO", "0"); // 连接操作的超时限制（不限制）
				put("compressEnable", "true"); // 是否启用自动压缩（启用）
				put("compressThreshold", "65536"); // 超过指定大小的数据都会被压缩（64K）
			}
		};

		// 初始化MemCached服务器运行状态表，对各状态进行中文解释
		statsItems = new HashMap<String, String>() {
			{
				put("pid", "MemCached服务进程ID");
				put("version", "MemCached服务版本");
				put("pointer_size", "MemCached服务器架构");
				put("time", "服务器当前时间");
				put("uptime", "服务器本次启动以来，总共运行时间");
				put("connection_structures", "服务器分配的连接结构数");
				put("total_connections", "服务器本次启动以来，累计响应连接总次数");
				put("curr_connections", "当前打开的连接数");
				put("limit_maxbytes", "允许服务支配的最大内存容量");
				put("bytes", "当前已使用的内存容量");
				put("bytes_written", "服务器本次启动以来，写入的数据量");
				put("bytes_read", "服务器本次启动以来，读取的数据量");
				put("total_items", "服务器本次启动以来，曾存储的Item总个数");
				put("curr_items", "当前存储的Item个数");
				put("cmd_get", "服务器本次启动以来，执行Get命令总次数");
				put("get_hits", "服务器本次启动以来，Get操作的命中次数");
				put("get_misses", "服务器本次启动以来，Get操作的未命中次数");
				put("cmd_set", "服务器本次启动以来，执行Set命令总次数");
			}
		};
	}

	/**
	 * @category 保护型构造方法，不允许实例化！
	 */
	protected Cache() {
		initConfig();
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, Object> getLocalCache() {
		return localcached;
	}

	public void shutdown() {
		try {

			memClient.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @category 操作类入口：获取唯一实例.
	 * 
	 * @return MemCached对象
	 */
	public static Cache getInstance(final String hosts) {
		try {
			TelnetClient telnet = new TelnetClient(); // 初始化Telnet对象，用来检测服务器是否可以成功连接
			telnet.setConnectTimeout(5000); // 连接超时：5秒
			String[] ht = hosts.split(" ");
			int ind = 0;
			for (String serverTmp : ht) {
				try {
					String[] hs = serverTmp.split(":");
					telnet.connect(hs[0], Integer.parseInt(hs[1])); // 连接到服务器
					telnet.disconnect(); // 断开连接
					serverListArr += serverTmp + " "; // 连接成功，将服务器添加到实际可用列表
					ind++;// 默认都为1

				} catch (Exception e) {
				}
			}
			if (serverListArr.trim().length() == 0) { // 没有发现实际可用的服务器，返回
				enUsed = false;
				return memCached;
			}

			weightListArr = new int[ind]; // 初始化服务器负载权重数组
			for (int i = 0; i < ind; i++) { // 向服务器数组进行赋值
				weightListArr[i] = 1;// 默认都为1
			}
			// 获取memcache配置
			MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(serverListArr),
					weightListArr);
			// 客户端分布 一致性hash
			builder.setSessionLocator(new KetamaMemcachedSessionLocator(HashAlgorithm.CRC32_HASH));
			// 用序列化 二进制协议 ，默认是 TextCommandFactory
			builder.setCommandFactory(new BinaryCommandFactory());
			// nio 连接池大小
			// 而且多个连接会有数据不同步的问题，提供的cas 可以解决
			builder.setConnectionPoolSize(3);
			// 宕机报警
			// builder.setFailureMode(true);
			memClient = builder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return memCached;
	}

	/**
	 * @category 返回是否已经启用memcached内存服务器
	 * 
	 * @return boolean
	 */
	public static boolean used() {
		return enUsed;
	}

	/**
	 * @category 插入新记录.
	 * @category 前提：记录的Key在缓存中不存在
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @return boolean 操作结果
	 */
	public boolean add(String key, Object value) {
		if (!enUsed) {
			return set(key, value);
		} else {
			return add(key, value, 0);
		}
	}

	/**
	 * @category 插入新记录.
	 * @category 前提：记录的Key在缓存中不存在
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @return boolean 操作结果
	 */
	public boolean add(String key, Object value, int seconds) {
		if (!enUsed) {
			return set(key, value);
		} else {
			try {
				return memClient.add(key, seconds, value);
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * @category 插入新记录并设置超时天数
	 * @category 前提：记录的Key在缓存中不存在
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @param expiryDays
	 *            超时天数
	 * @return boolean 操作结果
	 */
	public boolean addDays(String key, Object value, int expiryDays) {
		if (!enUsed) {
			return set(key, value);
		} else {
			return add(key, value, expiryDays * 24 * 3600);
		}
	}

	/**
	 * @category 插入新记录或更新已有记录
	 * @category 解释：记录的Key在缓存中不存在则插入；否则更新
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @return boolean 操作结果
	 */
	public boolean set(String key, Object value) {
		if (!enUsed) {
			localcached.put(key, value);
			return true;
		} else {
			return set(key, value, 0);
		}
	}

	/**
	 * @category 插入新记录或更新已有记录
	 * @category 解释：记录的Key在缓存中不存在则插入；否则更新
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @return boolean 操作结果
	 */
	public boolean set(String key, Object value, int seconds) {
		if (!enUsed) {
			return set(key, value);
		} else {
			try {
				return memClient.set(key, seconds, value);
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * @category 插入新记录或更新已有记录，并设置超时天数
	 * @category 解释：记录的Key在缓存中不存在则插入；否则更新
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @param expiryDate
	 *            超时天数
	 * @return boolean 操作结果
	 */
	public boolean setDays(String key, Object value, int expiryDays) {
		if (!enUsed) {
			return set(key, value);
		} else {
			return set(key, value, expiryDays * 24 * 3600);
		}
	}

	/**
	 * @category 更新已有记录
	 * @category 前提：记录的Key在缓存中已经存在
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @return boolean 操作结果
	 */
	public boolean replace(String key, Object value) {
		if (!enUsed) {
			return set(key, value);
		} else {
			return replace(key, value, 0);
		}
	}

	/**
	 * @category 更新已有记录，并设置超时日期
	 * @category 前提：该值在缓存中已经存在
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @param expiryDate
	 *            超时日期
	 * @return boolean 操作结果
	 */
	public boolean replaceDays(String key, Object value, int expiryDate) {
		if (!enUsed) {
			return set(key, value);
		} else {
			return replace(key, value, expiryDate * 24 * 3600);
		}
	}

	/**
	 * @category 更新已有记录，并设置超时秒数
	 * @category 前提：该值在缓存中已经存在
	 * @param key
	 *            记录的主键
	 * @param value
	 *            记录的内容
	 * @param expiryDays
	 *            超时天数
	 * @return boolean 操作结果
	 */
	public boolean replace(String key, Object value, int seconds) {
		if (!enUsed) {
			return set(key, value);
		} else {
			try {
				return memClient.replace(key, seconds, value);
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * @category 返回单条记录
	 * 
	 * @param key
	 *            记录的主键
	 * @return 记录的内容
	 */
	public Object get(String key) {
		if (!enUsed) {
			return localcached.get(key);
		} else {
			try {
				return memClient.get(key);
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * @category 返回多条记录
	 * 
	 * @param keys
	 *            记录的主键数组
	 * @return Map<String, Object> 多条记录的内容
	 */
	public Map<String, Object> get(List<String> keys) {
		if (!enUsed) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (String key : keys) {
				map.put(key, localcached.get(key));
			}
			return map;
		} else {
			try {
				return memClient.get(keys);
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * @category 删除记录
	 * @category 执行该方法之后，使用stats的统计结果会同步更新
	 * @param key
	 *            记录的主键
	 * @return 操作结果
	 */
	public boolean delete(String key) {
		if (!enUsed) {
			if (localcached.remove(key) == null) {
				return false;
			} else {
				return true;
			}
		} else {
			try {
				return memClient.delete(key);
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/*
	 * *************************************************************************
	 * ***************************************
	 * 下面的6个方法都是为了对memcached服务器进行监控及管理所用的，可能对服务器造成阻塞，所以除Debug以外，不推荐使用！
	 */

	/**
	 * @category 清空全部缓存数据。*慎用！！
	 * @category 执行该方法之后，使用stats的统计结果不会马上发生变化，每get一个不存在的item之后，该item的值才会被动清空
	 * @return 操作结果
	 */
	public boolean flushAll() {
		if (!enUsed) {
			localcached.clear();
			return true;
		} else {
			try {
				memClient.flushAll();
				return true;
			} catch (TimeoutException | InterruptedException | MemcachedException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * @category 返回可用的服务器列表
	 * @return 数组（服务器地址:端口）
	 */
	public String servers() {
		if (!enUsed)
			return null;
		return serverListArr;
	}

	/**
	 * @category 返回所有缓存服务器当前的运行状态
	 * 
	 * @return
	 * 
	 * 		Map |-- Key : ServerName01, Value : LinkedHashMap | |-- Key :
	 *         statName01, Value : statValue | |-- ... | |-- Key : ServerName02,
	 *         Value : LinkedHashMap | |-- Key : statName01, Value : statValue |
	 *         |-- ... | |-- ...
	 * 
	 */
	public Map<String, LinkedHashMap<String, String>> stats() {
		if (!enUsed)
			return null;
		Map<String, LinkedHashMap<String, String>> retMap = new HashMap<String, LinkedHashMap<String, String>>();
		for (String server : serverListArr.split(" ")) {
			LinkedHashMap<String, String> serverStats = this.stats(server);
			retMap.put(server, serverStats);
		}
		return retMap;
	}

	/**
	 * @category 返回指定服务器当前的运行状态
	 * @param server
	 *            服务器地址:端口
	 * 
	 *            优化： 参数名称中文显示 优化： 毫秒数转换为小时 优化： 字节数转换为MB或KB 优化： UNIX时间戳转换为标准时间
	 *            优化： 参数显示顺序更加直观
	 * 
	 * @return LinkedHashMap<String, String> 可对Map进行有序遍历
	 * 
	 */
	public LinkedHashMap<String, String> stats(String server) {
		if (!enUsed)
			return null;
		LinkedHashMap<String, String> retMap = new LinkedHashMap<String, String>();
		Map<String, String> statsList;
		try {
			statsList = memClient.stats(AddrUtil.getOneAddress(serverListArr));
		} catch (MemcachedException | InterruptedException | TimeoutException e) {
			e.printStackTrace();
			return null;
		}
		// System.out.println(memClient.stats().toString());
		DecimalFormat format = new DecimalFormat("0.0");
		for (Object serverTitle : statsList.keySet().toArray()) {
			// Map<String, String> serverStats = (Map<String, String>)
			// statsList.get(serverTitle);
			retMap.put(statsItems.get("pid"), statsList.get("pid").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("version"), statsList.get("version").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("pointer_size"), statsList.get("pointer_size").replaceAll("\\r\\n", "") + "位");
			retMap.put(statsItems.get("time"),
					new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date(Long.parseLong(statsList.get("time").replaceAll("\\r\\n", "")) * 1000))
							.toString());
			retMap.put(statsItems.get("uptime"),
					format.format(Double.parseDouble(statsList.get("uptime").replaceAll("\\r\\n", "")) / (60 * 60))
							+ "小时");
			retMap.put(statsItems.get("connection_structures"),
					statsList.get("connection_structures").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("total_connections"),
					statsList.get("total_connections").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("curr_connections"), statsList.get("curr_connections").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("limit_maxbytes"), format.format(
					Double.parseDouble(statsList.get("limit_maxbytes").replaceAll("\\r\\n", "")) / (1024 * 1024))
					+ "MB");
			retMap.put(statsItems.get("bytes"),
					format.format(Double.parseDouble(statsList.get("bytes").replaceAll("\\r\\n", "")) / (1024 * 1024))
							+ "MB");
			retMap.put(statsItems.get("bytes_written"),
					format.format(Double.parseDouble(statsList.get("bytes_written").replaceAll("\\r\\n", "")) / (1024))
							+ "KB");
			retMap.put(statsItems.get("bytes_read"),
					format.format(Double.parseDouble(statsList.get("bytes_read").replaceAll("\\r\\n", "")) / (1024))
							+ "KB");
			retMap.put(statsItems.get("total_items"), statsList.get("total_items").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("curr_items"), statsList.get("curr_items").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("cmd_get"), statsList.get("cmd_get").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("get_hits"), statsList.get("get_hits").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("get_misses"), statsList.get("get_misses").replaceAll("\\r\\n", ""));
			retMap.put(statsItems.get("cmd_set"), statsList.get("cmd_set").replaceAll("\\r\\n", ""));
		}
		return retMap;
	}

	/*
	 * 上面的6个方法都是为了对memcached服务器进行监控及管理所用的，可能对服务器造成阻塞，所以除Debug以外，不推荐使用！
	 * *************************************************************************
	 * ***************************************
	 */

	/**
	 * 使用示例
	 */
	public static void main(String[] args) {

		// 初始化memcached操作类对象
		Cache cache = Cache.getInstance("127.0.0.1:11211");

		// 验证memcached服务是否已启用
		if (!cache.used()) {
			System.out.println("memcached服务未启用！");
			return;
		}

		// 插入新记录
		System.out.println("开始插入新记录（add）：\r\n===================================");
		System.out.println("keyTest01:" + cache.add("keyTest01", "keyTest01Content"));
		System.out.println("keyTest02:" + cache.add("keyTest02", "keyTest02Content"));
		System.out.println("插入新记录操作完成\r\n===================================");

		// 读取单条记录
		System.out.println("读取单条记录（get）：\r\n===================================");
		System.out.println("keyTest01:" + cache.get("keyTest01"));
		System.out.println("keyTest02:" + cache.get("keyTest02"));
		System.out.println("读取单条记录操作完成\r\n===================================");
		List<String> list = new ArrayList<String>();
		list.add("keyTest01");
		list.add("keyTest02");
		// 读取多条记录
		System.out.println("读取多条记录（add）：\r\n===================================");
		System.out.println("keyTest01、keyTest02:" + cache.get(list));
		System.out.println("读取多条记录操作完成\r\n===================================");

		// 修改记录值
		System.out.println("修改记录值（replace）：\r\n===================================");
		System.out.println("keyTest01:" + cache.get("keyTest01"));
		System.out.println("keyTest01:" + cache.replace("keyTest01", "keyTest01ContentReplace!"));
		System.out.println("keyTest01:" + cache.get("keyTest01"));
		System.out.println("修改记录值操作完成\r\n===================================");

		// 添加或修改记录
		System.out.println("添加或修改记录（set）：\r\n===================================");
		System.out.println("keyTest03:" + cache.set("keyTest03", "keyTest03Content"));
		System.out.println("keyTest03:" + cache.get("keyTest03"));
		System.out.println("keyTest03:" + cache.set("keyTest03", "keyTest03ContentReplace!"));
		System.out.println("keyTest03:" + cache.get("keyTest03"));
		System.out.println("添加或修改记录操作完成\r\n===================================");

		// 删除记录
		System.out.println("删除记录（delete）：\r\n===================================");
		System.out.println("keyTest01:" + cache.delete("keyTest01"));
		System.out.println("keyTest02:" + cache.delete("keyTest02"));
		System.out.println("keyTest03:" + cache.get("keyTest03"));
		System.out.println("keyTest03:" + cache.delete("keyTest03"));
		System.out.println("keyTest03:" + cache.get("keyTest03"));
		System.out.println("修改记录值操作完成\r\n===================================");

		// 打印当前的服务器参数及统计信息
		System.out.println("服务器参数及统计信息（stats）：\r\n===================================");
		Map statsList = cache.stats();
		for (Object server : statsList.keySet().toArray()) {
			System.out.println("-------------------------\r\n服务器：" + server + " : \r\n-------------------------");
			LinkedHashMap serverStats = (LinkedHashMap) statsList.get(server);
			for (Object statKey : serverStats.keySet().toArray()) {
				System.out.println(statKey + " : " + serverStats.get(statKey));
			}
		}
	}
}
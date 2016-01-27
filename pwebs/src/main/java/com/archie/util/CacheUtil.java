package com.archie.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.archie.dao.BaseDao;
import com.archie.model.DynaBean;
import com.archie.service.BaseService;
import com.archie.util.sql.SqlBuilder;

/** 
* @author panlihai E-mail:panlihai@zlwh.com.cn 
* @version 创建时间：2015年12月30日 下午5:22:36 
* 类说明: 
*/
/**
 * @author Administrator
 *
 */
public class CacheUtil {
	private static Logger logger = Logger.getLogger(BaseService.class);
	// 系统参数后缀
	public final static String SYSPARAM = "SYSPARAM";
	// 数据表结构后缀
	public final static String SYSTABLEDEF = "SYSTABLEDEF";
	// 数据表SYS_MENU
	public final static String SYSMENU = "SYSMENU";
	// 数据表SYSAPP
	public final static String SYSAPP = "SYSAPP";
	// 应用程序表字段结构后缀
	public final static String SYSAPPFIELDS = "SYSAPPFIELDS";
	// 应用程序表SYS_MENU
	public final static String SYSAPPBUTTONS = "SYSAPPBUTTONS";
	// 数据字典结构后缀
	public final static String SYSDIC = "SYSDIC";
	// 数据字典明细结构后缀
	public final static String SYSDICDETAIL = "SYSDICDETAIL";
	// 数据动态字典结构后缀
	public final static String SYSDICAPP = "SYSDICAPP";
	// 数据动态字典明细结构后缀
	public final static String SYSDICAPPDETAIL = "SYSDICAPPDETAIL";
	// SYSAPPLINKS 关联功能
	public final static String SYSAPPLINKS = "SYSAPPLINKS";
	// SYSROLE 角色
	public final static String SYSROLE = "SYSROLE";
	// SYSPRODUCT 产品
	public static final String SYSPRODUCT = "SYSPRODUCT";
	// SYSPRODUCTAPP 产品
	public static final String SYSPRODUCTAPP = "SYSPRODUCTAPP";

	/**
	 * 缓存类型为type的key对应的对象内容
	 * 
	 * @param cache
	 * @param type
	 *            缓存类型
	 * @param key
	 * @param value
	 */
	public static boolean setTypeCache(Cache cache, String type, String key, Object value) {
		return cache.set(key + type, value);
	}
	/**
	 * 根据缓存类型以及key获取缓存
	 * 
	 * @param cache
	 * @param type
	 *            缓存类型
	 * @param key
	 */
	public static DynaBean getDynaBeanByCacheID(Cache cache,String key) {
		return (DynaBean)cache.get(key);		
	}
	/**
	 * 根据缓存类型以及key获取缓存
	 * 
	 * @param cache
	 * @param type
	 *            缓存类型
	 * @param key
	 */
	public static Object getTypeCache(Cache cache, String type, String key) {
		return cache.get(key + type);
	}

	/**
	 * 根据缓存类型以及key获取缓存
	 * 
	 * @param cache
	 * @param type
	 *            缓存类型
	 * @param key
	 */
	public static DynaBean getDynaBeanByTypeCache(Cache cache, String type, String key) {
		Object tempKey = cache.get(key + type);
		if (tempKey != null) {
			return (DynaBean) cache.get(tempKey.toString());
		} else {
			return null;
		}
	}

	/**
	 * 根据type及key删除
	 * 
	 * @param cache
	 * @param type
	 *            缓存类型
	 * @param key
	 */
	public static Object removeTypeCache(Cache cache, String type, String key) {
		return cache.delete(key + type);
	}

	/**
	 * 根据type及key替换
	 * 
	 * @param cache
	 * @param type
	 *            缓存类型
	 * @param key
	 */
	public static boolean replaceTypeCache(Cache cache, String type, String key, Object value) {
		return cache.replace(key + type, value);
	}

	/**
	 * 根据type接key的值获取key缓存的多个key,并把从key中获取对象
	 * 
	 * @param cache
	 * @param type
	 * @param key
	 *            单个key 此key的缓存是多个key值,再把key值从缓存中获取对象
	 */
	public static List<DynaBean> getTypeListCache(Cache cache, String type, String key) {
		List<DynaBean> beanList = new ArrayList<DynaBean>();
		List<Map> mapList = (List) cache.get(key + type);
		if (mapList == null) {
			return null;
		}
		for (Map map : mapList) {
			DynaBean dynaBean = (DynaBean) cache.get(map.get("ID").toString());
			if (dynaBean != null) {
				beanList.add(dynaBean);
			}
		}
		return beanList;
	}

	/**
	 * 根据应用程序获取
	 * 
	 * @param cache
	 * @param key
	 */
	public static DynaBean getSysapp(Cache cache, String appId, String pId) {
		DynaBean appBean = (DynaBean) getDynaBeanByTypeCache(cache, SYSAPP, appId);
		if (appBean == null) {
			return null;
		}
		List<DynaBean> dicList = getTypeListCache(cache, SYSDIC, appId);
		for (DynaBean dynaBean : dicList) {
			if (dynaBean.getStr("DICTYPE", "").equals("LISTVALUE")) {
				dynaBean.set(PageUtil.PAGE_APPDICDETAILS,
						getTypeListCache(cache, SYSDICDETAIL, appId + dynaBean.getStr("DICID")));
			} else if (dynaBean.getStr("DICTYPE", "").equals("LISTAPP")) {
				List<DynaBean> childList = getTypeListCache(cache, SYSDICAPP, appId + dynaBean.getStr("DICID"));
				for (DynaBean cBean : childList) {
					cBean.set(PageUtil.PAGE_APPDICAPPDETAILS, getTypeListCache(cache, SYSDICAPPDETAIL,
							appId + dynaBean.getStr("DICID") + cBean.getStr("APPID")));
				}
				dynaBean.set(PageUtil.PAGE_APPDICAPPS, childList);
			}
		}
		appBean.set(PageUtil.PAGE_APPDICS, dicList);
		appBean.set(PageUtil.PAGE_APPFIELDS, getTypeListCache(cache, SYSAPPFIELDS, appId));
		appBean.set(PageUtil.PAGE_APPBUTTONS, getTypeListCache(cache, SYSAPPBUTTONS, appId));
		appBean.set(PageUtil.PAGE_APPLINKS, getTypeListCache(cache, SYSAPPLINKS, appId));
		return appBean;
	}

	/**
	 * 根据应用程序获取
	 * 
	 * @param cache
	 * @param key
	 */
	public static void removeSysapp(Cache cache, String appId) {
		removeTypeCache(cache, SYSAPP, appId);
		removeTypeCache(cache, SYSAPPFIELDS, appId);
		removeTypeCache(cache, SYSAPPBUTTONS, appId);
		DynaBean dynaBean = (DynaBean) getTypeCache(cache, SYSDIC, appId);
		removeTypeCache(cache, SYSDIC, appId);
		removeTypeCache(cache, SYSDICDETAIL, appId + dynaBean.getStr("DICID"));
		removeTypeCache(cache, SYSAPPLINKS, appId);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月8日 下午3:43:51 方法说明: 初始化系统参数列表缓存
	 * @param
	 */
	public static void initSysParamList(Cache cache, List<Map> mapList) {
		for (Map map : mapList) {
			setTypeCache(cache, SYSPARAM, map.get("PARAMID").toString(), map.get("ID").toString());
		}
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月8日 下午3:43:51 方法说明: 初始化系统参数列表缓存
	 * @param
	 */
	public static void initSysMenuList(Cache cache, List<Map> mapList) {
		for (Map map : mapList) {
			setTypeCache(cache, SYSMENU, map.get("MENUID").toString(), map.get("ID").toString());
		}
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月8日 下午3:56:10 方法说明: 缓存应用程序对应的表结构
	 *          先写应用程序及结构表加入缓存,确保所有表结构都写入缓存
	 * 
	 * @param cache
	 * @param beanList
	 * @param conn
	 * @throws Exception
	 */
	public static void initSysAppList(Cache cache, List<Map> mapList, BaseDao dao, Connection conn) throws Exception {
		for (Map map : mapList) {
			DynaBean dynaBean = new DynaBean();
			dynaBean.setValues(map);
			initSysapp(cache, dynaBean, dao, conn);
		}
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月8日 下午3:56:10 方法说明: 缓存应用程序对应的表结构
	 *          先写应用程序及结构表加入缓存,确保所有表结构都写入缓存
	 * 
	 * @param cache
	 * @param beanList
	 * @param conn
	 * @throws Exception
	 */
	public static void initSysapp(Cache cache, DynaBean appBean, BaseDao dao, Connection conn) throws Exception {
		String appId = appBean.getStr("APPID");
		// 初始化数据结构
		DynaBean dynaBean = SqlBuilder.getTableDef(appBean.getStr("MAINTABLE"), conn);
		setTypeCache(cache, SYSTABLEDEF, appBean.getStr("MAINTABLE"), dynaBean);
		// 把appid作为Key存入
		setTypeCache(cache, SYSAPP, appId, appBean.getStr("ID"));
		// 字段写入缓存
		initSysAppFields(cache, dao, appId);
		// 按钮写入缓存
		initSysAppButtons(cache, dao, appId);
		// 关联应用写入app缓存
		initSysAppLinks(cache, dao, appId);
		// 字典表加入缓存,必须在表结构缓存写完后执行,把数据字典写入缓存中
		initSysAppSysDic(cache, dao, appId);

	}

	/**
	 * 根据APpid字典表写入缓存
	 * 
	 * @param cache
	 * @param dao
	 * @param appId
	 */
	public static void initSysAppSysDic(Cache cache, BaseDao dao, String appId) {
		DynaBean dynaBean = new DynaBean("SYS_DIC",
				"and DICID in(select DICCODE from SYS_APPFIELDS where APPID='" + appId + "')");
		List<Map> mapList = dao.findWithQueryNoCacheMap(dynaBean);
		// 数据字典写入缓存
		setTypeCache(cache, SYSDIC, appId, mapList);
		for (Map map : mapList) {
			if (map.get("DICTYPE") != null && map.get("DICTYPE").toString().equals("LISTVALUE")) {
				// 静态字典写入缓存
				dynaBean = new DynaBean("SYS_DICDETAIL", " and DICID='" + map.get("DICID") + "'", "SORT");
				dynaBean.setStr(BeanUtils.KEY_SELECT, "ID,DICID");
				List<Map> childMapList = dao.findWithQueryNoCacheMap(dynaBean);
				setTypeCache(cache, SYSDICDETAIL, appId + map.get("DICID").toString(), childMapList);
			} else {
				// 动态字段写入缓存
				dynaBean = new DynaBean("SYS_DICAPP", " and DICID='" + map.get("DICID") + "'");
				dynaBean.setStr(BeanUtils.KEY_SELECT, "ID,DICID,APPID");
				List<Map> childMapList = dao.findWithQueryNoCacheMap(dynaBean);
				setTypeCache(cache, SYSDICAPP, appId + map.get("DICID").toString(), childMapList);
				for (Map map1 : childMapList) {
					dynaBean = new DynaBean("SYS_DICAPPDETAIL", " and DICID='" + map.get("DICID") + "'");
					dynaBean.setStr(BeanUtils.KEY_SELECT, "ID,APPID,DICID");
					List<Map> childMapList1 = dao.findWithQueryNoCacheMap(dynaBean);
					setTypeCache(cache, SYSDICAPPDETAIL,
							appId + map.get("DICID").toString() + map1.get("APPID").toString(), childMapList1);
				}
			}
		}
	}

	/**
	 * 根据APpid字段写入缓存
	 * 
	 * @param cache
	 * @param dao
	 * @param appId
	 */
	public static void initSysAppFields(Cache cache, BaseDao dao, String appId) {
		DynaBean dynaBean = new DynaBean("SYS_APPFIELDS",
				"and ENABLE='" + SyConstant.STR_YES + "' and APPID='" + appId + "'", "SORT");
		dynaBean.setStr(BeanUtils.KEY_SELECT, "ID");
		setTypeCache(cache, SYSAPPFIELDS, appId, dao.findWithQueryNoCacheMap(dynaBean));
	}

	/**
	 * 根据APpid按钮写入缓存
	 * 
	 * @param cache
	 * @param dao
	 * @param appId
	 */
	public static void initSysAppButtons(Cache cache, BaseDao dao, String appId) {
		DynaBean dynaBean = new DynaBean("SYS_APPBUTTONS",
				"and ENABLE='" + SyConstant.STR_YES + "' and APPID='" + appId + "'", "SORT");
		dynaBean.setStr(BeanUtils.KEY_SELECT, "ID");
		// 按钮写入缓存
		setTypeCache(cache, SYSAPPBUTTONS, appId, dao.findWithQueryNoCacheMap(dynaBean));
	}

	/**
	 * 根据APpid设置关联应用
	 * 
	 * @param cache
	 * @param dao
	 * @param appId
	 */
	public static void initSysAppLinks(Cache cache, BaseDao dao, String appId) {
		DynaBean dynaBean = new DynaBean("SYS_APPLINKS", " and MAINAPP='" + appId + "'", "SORTBY");
		dynaBean.setStr(BeanUtils.KEY_SELECT, "ID");
		setTypeCache(cache, SYSAPPLINKS, appId, dao.findWithQueryNoCacheMap(dynaBean));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月8日 下午4:01:41 方法说明:
	 * @param cache
	 * @param beanList
	 * @param dao
	 * @param conn
	 * @throws Exception
	 */
	public static void initPersonalDataList(Cache cache, List<DynaBean> beanList, BaseDao dao) throws Exception {
		for (DynaBean bean : beanList) {
			initPersonalData(cache, bean, dao);
		}

	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月8日 下午4:01:41 方法说明:
	 * @param cache
	 * @param beanList
	 * @param dao
	 * @throws Exception
	 */
	public static void initPersonalData(Cache cache, DynaBean bean, BaseDao dao) throws Exception {
		DynaBean childsBean = new DynaBean(bean.getStr("MAINTABLE"), bean.getStr("APPFILTER", " and 1=1 "));
		List<DynaBean> childBeanList = dao.findWithQueryNoCache(childsBean,
				(DynaBean) getTypeCache(cache, SYSTABLEDEF, bean.getStr("MAINTABLE")));
		for (DynaBean childBean : childBeanList) {
			cache.set(childBean.getStr("ID"), childBean);
		}
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月8日 下午4:06:45 方法说明:
	 * 
	 *          获取缓存消息
	 * @param cache
	 */
	public static Map<String, Object> getCacheState(Cache cache) {
		Map<String, Object> serverStateList = new HashMap<String, Object>();
		Map statsList = cache.stats();
		for (Object server : statsList.keySet().toArray()) {
			// System.out.println("\r\n服务器缓存加载情况：" + server + " : \r\n");
			LinkedHashMap serverStats = (LinkedHashMap) statsList.get(server);
			for (Object statKey : serverStats.keySet().toArray()) {
				// System.out.println(statKey + " : " +
				// serverStats.get(statKey));
				serverStateList.put(server + statKey.toString(), serverStats.get(statKey));
			}
		}
		return serverStateList;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月8日 下午4:06:45 方法说明:
	 * 
	 *          // 初始化完成 打印缓存消息
	 * @param cache
	 */
	public static void printCacheState(Cache cache) {
		if (cache.used()) {
			Map statsList = cache.stats();
			for (Object server : statsList.keySet().toArray()) {
				System.out.println("\r\n" + (new Date()).toString() + "  服务器缓存加载情况：" + server + " : \r\n");
				LinkedHashMap serverStats = (LinkedHashMap) statsList.get(server);
				for (Object statKey : serverStats.keySet().toArray()) {
					System.out.println((new Date()).toString() + "  " + statKey + " : " + serverStats.get(statKey));
				}
			}
		}
	}

	/**
	 * 缓存清除
	 */
	public static void clearCache(BaseDao dao) {
		dao.getMemCached().flushAll();
	}

	/**
	 * 系统重新初始化
	 */
	public static void serverInitialized(BaseDao dao) {
		// 初始化数据结构
		Connection conn = null;
		// 缓存对象
		Cache cache = null;
		// 获取系统参数配置
		DynaBean dynaBean = null;
		// 查询结果
		List<DynaBean> beanList = new ArrayList<DynaBean>();
		// 缓存结构id
		List<Map> mapList = new ArrayList<Map>();
		try {
			// 获取链接
			conn = dao.getSqlMapClient().getDataSource().getConnection();
			// 获取系统参数配置
			dynaBean = new DynaBean("SYS_PARAM",
					" and ENABLE='" + SyConstant.STR_YES + "' and PARAMID='SYS_MEMCACHED_HOST'");
			// 查询系统参数配置
			beanList = dao.findWithQueryNoCache(dynaBean, SqlBuilder.getTableDef("SYS_PARAM", conn));
			String hostname = "";
			for (DynaBean dyBean : beanList) {
				if (dyBean.get("PARAMVALUE") == null) {
					continue;
				}
				hostname = dyBean.getStr("PARAMVALUE");
			}
			// 初始化memcached
			dao.setMemCached(Cache.getInstance(hostname));
			cache = dao.getMemCached();
			if (!cache.used()) {
				logger.error("Memcached is not starting,please check memcached host&port......");
			}
			beanList.clear();
			// 清空缓存
			dao.getMemCached().flushAll();
			// 初始化产品
			dynaBean = new DynaBean("SYS_PRODUCT", " and ENABLE='" + SyConstant.STR_YES + "'");
			// 查询系统参数配置
			mapList = dao.findWithQueryNoCacheMap(dynaBean, SqlBuilder.getTableDef("SYS_PRODUCT", conn));
			// 系统参数写入缓存
			initSysProductList(cache, mapList);

			dynaBean = new DynaBean("SYS_PARAM", " and ENABLE='" + SyConstant.STR_YES + "'");
			// 查询系统参数配置
			mapList = dao.findWithQueryNoCacheMap(dynaBean, SqlBuilder.getTableDef("SYS_PARAM", conn));
			// 系统参数写入缓存
			initSysParamList(cache, mapList);

			// 初始化菜单
			dynaBean = new DynaBean("SYS_MENU", "and ENABLE='" + SyConstant.STR_YES + "'");
			mapList = dao.findWithQueryNoCacheMap(dynaBean, SqlBuilder.getTableDef("SYS_MENU", conn));
			CacheUtil.initSysMenuList(cache, mapList);

			// 缓存应用涉及的配置内容
			dynaBean = new DynaBean("SYS_APP", "and ENABLE='" + SyConstant.STR_YES + "'");
			// 查询应用程序表
			mapList = dao.findWithQueryNoCacheMap(dynaBean, SqlBuilder.getTableDef("SYS_APP", conn));
			CacheUtil.initSysAppList(cache, mapList, dao, conn);
			// 初始化开放接口

			// 加载需要缓存的数据
			// 当memcache为启动成功的时候,将不启用数据缓存
			if (cache.used()) {
				dynaBean = new DynaBean("SYS_APP",
						"and ENABLE='" + SyConstant.STR_YES + "' and ENABLECACHE='" + SyConstant.STR_YES + "'");
				beanList = dao.findWithQueryNoCache(dynaBean, (DynaBean) getTypeCache(cache, SYSTABLEDEF, "SYS_APP"));

			} else {
				dynaBean = new DynaBean("SYS_APP", "and APPMODEL='SYSTEM' and ENABLE='" + SyConstant.STR_YES
						+ "' and ENABLECACHE='" + SyConstant.STR_YES + "'");
				beanList = dao.findWithQueryNoCache(dynaBean, (DynaBean) getTypeCache(cache, SYSTABLEDEF, "SYS_APP"));
			}

			initPersonalDataList(cache, beanList, dao);
			// 打印缓存消息
			printCacheState(cache);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月14日 下午12:56:56 方法说明:
	 * @param cache
	 * @param mapList
	 */
	private static void initSysProductList(Cache cache, List<Map> mapList) {
		for (Map map : mapList) {
			setTypeCache(cache, SYSPRODUCT, map.get("PID").toString(), map.get("ID").toString());
		}
	}

}
package com.archie.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.archie.dao.BaseDao;
import com.archie.model.DynaBean;
import com.archie.service.system.SysServer;

import net.sf.json.JSONObject;

public class ApiUtil {

	// 接口系统接口-产品id
	public final static String PID = "PID";
	// 接口系统接口-应用id
	public final static String AID = "APPID";
	// 接口系统接口-用户token
	public final static String USERTOKEN = "USERTOKEN";
	// 接口系统接口-时间戳
	public final static String TIMESTAMP = "TIMESTAMP";
	// 接口系统接口-版本号
	public final static String VERSION = "VERSION";
	// 接口系统接口-请求操作
	public final static String ACT = "ACT";
	// 接口系统接口-请求查询列表信息操作
	public final static String ACT_LISTINFO = "LISTINFO";
	// 接口系统接口-请求查询列表结果总数信息操作
	public final static String TOTALSIZE = "TOTALSIZE";
	// 接口系统接口-请求查询列表结果偏移数信息操作
	public final static String LISTSIZE = "LISTSIZE";
	// 接口系统接口-请求获取明细信息操作
	public final static String ACT_INFO = "INFO";
	// 接口系统接口-请求新建操作
	public final static String ACT_CREATE = "CREATE";
	// 接口系统接口-请求删除操作
	public final static String ACT_DELETE = "DELETE";
	// 接口系统接口-请求更新操作
	public final static String ACT_UPDATE = "UPDATE";
	// 接口系统接口-注册用户重置密码操作
	public final static String ACT_RESETPSW = "RESETPWD";
	// 接口系统接口-注册用户操作
	public final static String ACT_REGISTER = "REGISTER";
	// 接口系统接口-用户登录操作
	public final static String ACT_LOGIN = "LOGIN";
	// 接口系统接口-用户注销操作
	public final static String ACT_LOGOUT = "LOGOUT";
	// 接口系统接口-用户手机短信验证码操作
	public final static String ACT_TELSMS = "TELSMS";
	// 接口系统接口-请求响应消息内容
	public final static String DATA = "DATA";
	// 接口系统接口-错误编码
	public final static String CODE = "CODE";
	// 接口系统接口-消息内容
	public final static String MSG = "MSG";

	// 处理接口
	public static DynaBean doImpl(DynaBean paramBean) {
		BaseDao dao = (BaseDao) SysServer.getServer().getBean("baseDao");
		DynaBean result = new DynaBean();
		// 接口产品的合法性
		if (!checkPid(dao, paramBean, result)) {
			return result;
		}
		//// 接口应用的合法性
		if (!checkAid(dao, paramBean, result)) {
			return result;
		}
		// 接口操作的合法性
		if (!checkAct(dao, paramBean, result)) {
			return result;
		}
		// 接口版本的合法性
		if (!checkVersion(dao, paramBean, result)) {
			return result;
		}
		// 得到产品对象
		DynaBean productBean = CacheUtil.getDynaBeanByTypeCache(dao.getMemCached(), CacheUtil.SYSPRODUCT,
				paramBean.getStr(PID));
		List<DynaBean> intefaceList = dao
				.findWithQuery(new DynaBean("SYS_INTERFACE", " and REQTYPE='" + paramBean.getStr(ACT) + "' and PID='"
						+ paramBean.getStr(PID) + "' and REQURL='" + paramBean.getStr(AID) + "'"));
		if (intefaceList.size() == 0) {
			result.setStr(CODE, "40004");// 不合法的AID凭证
			result.setStr(MSG, getBackName(dao, "40004"));
			return result;
		}
		// 获取接口配置信息表
		DynaBean interfaceBean = intefaceList.get(0);

		// 得到接口配置的应用程序
		DynaBean appBean = CacheUtil.getSysapp(dao.getMemCached(), interfaceBean.getStr("APPID"),
				paramBean.getStr(PID));
		// 得到接口请求及响应的参数列表
		List<DynaBean> paramList = dao.findWithQuery(
				new DynaBean("SYS_INTERFACEPARAM", "and IMPLID='" + interfaceBean.getStr("IMPLID") + "'"));
		paramBean.setStr("APPID", appBean.getStr("APPID"));
		// 获取参数体消息
		JSONObject paramJson = JSONObject.fromObject(paramBean.getStr(DATA, "{}"));
		// 校验合法性
		if (!checkParams(dao, paramBean, paramJson, interfaceBean, paramList, result)) {
			return result;
		}
		// 执行接口核心功能
		switch (paramBean.getStr(ACT)) {
		case ACT_CREATE:
			DynaBean insertBean = createMethod(dao, paramBean, productBean, appBean);
			// 获取参数内容
			// 得到接口返回参数列表
			result.set(DATA, createBackParam(dao, insertBean, paramList));
			// 写日志,如点击率,热点等
			LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_CREATE);
			break;
		case ACT_LISTINFO:
			String sqlwhere = queryMethod(dao, paramBean, productBean, appBean, paramList);
			DynaBean queryBean = new DynaBean(appBean.getStr("MAINTABLE"), sqlwhere);
			result.set(TOTALSIZE, dao.findCountWithQuery(queryBean));
			long pageNum = paramBean.getLong("PAGENUM", 0);
			long pageSize = paramBean.getLong("PAGESIZE", appBean.getLong("PAGESIZE", 10));
			queryBean.set(BeanUtils.KEY_PAGE_COUNT, pageNum);
			queryBean.set(BeanUtils.KEY_PAGE_SIZE, pageSize);
			List<Map> resultList = dao.findWithQueryMap(queryBean);
			result.set(LISTSIZE, resultList.size());
			// 得到接口返回参数列表
			result.set(DATA, resultList);
			// 写日志,如点击率,热点等
			// 写日志,如点击率,热点等
			LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_LISTINFO);
			break;
		case ACT_INFO:
			sqlwhere = queryMethod(dao, paramBean, productBean, appBean, paramList);
			queryBean = new DynaBean(appBean.getStr("MAINTABLE"), sqlwhere);
			resultList = dao.findWithQueryMap(queryBean);
			// 得到接口返回参数列表
			if (resultList.size() > 0) {
				result.set(DATA, resultList.get(0));
				// 写日志,如点击率,热点等
				LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_INFO);
			} else {
				result.set(DATA, new HashMap());
			}
			break;
		case ACT_UPDATE:
			// 写日志,如点击率,热点等
			LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_UPDATE);
			break;
		case ACT_DELETE:
			// 写日志,如点击率,热点等
			LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_DELETE);
			break;
		case ACT_TELSMS:// 手机短信验证
			if (!checkTel(dao, paramBean, productBean, appBean, paramList, result)) {
				return result;
			} else {
				String code = (int) (Math.random() * 10) + "" + (int) (Math.random() * 10) + (int) (Math.random() * 10)
						+ (int) (Math.random() * 10);
				paramBean.set("SMSCODE", code);
				paramBean.set("CREATETIME", DateUtils.getDatetime());
				// 写入库
				createMethod(dao, paramBean, productBean, appBean);
				// 获取参数内容
				TelUtil.sendSMS(paramBean.getStr("MOBILEPHONE"), code);
				// 写日志,如点击率,热点等
				LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_TELSMS);
			}
			break;
		case ACT_RESETPSW:
			// 查看参数是否是修改密码如果是修改密码则
			// 注册校验 重复注册 用户名重复
			if (!checkUser(dao, paramBean, productBean, appBean, paramList, result, true)) {
				return result;
			} else {
				resetPsw(dao, paramBean, productBean, appBean, paramList, result);
				// 写日志,如点击率,热点等
				LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_RESETPSW);
			}
			// 注册完自动登录
		case ACT_REGISTER:
			// 如果是修改密码的操作不执行注册的代码
			if (paramBean.getStr(ACT).equals(ACT_REGISTER)) {
				// 查看参数是否是修改密码如果是修改密码则
				// 注册校验 重复注册 用户名重复
				if (!checkUser(dao, paramBean, productBean, appBean, paramList, result, false)) {
					return result;
				} else {
					register(dao, paramBean, productBean, appBean, paramList, result);
					// 写日志,如点击率,热点等
					LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_REGISTER);
				}
			}
			// 注册完自动登录
		case ACT_LOGIN:
			// 校验密码是否正确?
			if (paramBean.getStr(ACT).equals(ACT_LOGIN)) {// 注册完了直接登录.不再校验
				if (!checkUserPassword(dao, paramBean, productBean, appBean, paramList, result)) {
					return result;
				}
			}
			login(dao, paramBean.getStr(USERTOKEN));
			// 写日志,如点击率,热点等
			LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_LOGIN);
			break;
		case ACT_LOGOUT:
			// 用户登出
			logout(dao, paramBean.getStr(USERTOKEN));
			// 写日志,如点击率,热点等
			LogUtil.logAppDetail(paramBean, appBean.getStr("APPID"), ACT_LOGOUT);
			break;
		}
		result.set(ApiUtil.CODE, "0");// 请求成功
		result.set(ApiUtil.MSG, getBackName(dao, "0"));
		return result;
	}
	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月18日 下午1:38:44 方法说明:
	 * @param dao
	 * @param paramBean
	 * @param productBean
	 * @param appBean
	 * @return
	 */
	private static String queryMethod(BaseDao dao, DynaBean paramBean, DynaBean productBean, DynaBean appBean,
			List<DynaBean> paramList) {
		StringBuffer sqlwhere = new StringBuffer(appBean.getStr("APPFILTER", " and 1=1 "));
		for (DynaBean param : paramList) {
			// 只看入口参数作为查询条件
			if (param.getStr("GETPUT", "PUT").equals("PUT")) {
				continue;
			}
			// 只对条件参数进行条件拼接
			if (!param.getStr("PARAMTYPE", "").equals("PARAMWHERE")) {
				continue;
			}
			switch (param.getStr("VALUETYPE", "")) {
			case "STR":// 字符型
				sqlwhere.append(" and ").append(param.getStr("PARAMNAME")).append("='")
						.append(paramBean.get(param.getStr("PARAMNAME"))).append("'");
				break;
			case "NUM":
				sqlwhere.append(" and ").append(param.getStr("PARAMNAME")).append("=")
						.append(paramBean.get(param.getStr("PARAMNAME")));
				break;
			case "DATE":
				sqlwhere.append(" and ").append(param.getStr("PARAMNAME")).append("='")
						.append(paramBean.get(param.getStr("PARAMNAME"))).append("'");
				break;
			}

		}
		sqlwhere.append(" and PID='").append(paramBean.get(PID)).append("'");
		return sqlwhere.toString();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月21日 下午5:37:02 方法说明:
	 * @param dao
	 * @param paramBean
	 * @param productBean
	 * @param appBean
	 * @param paramList
	 * @param result
	 */
	private static void resetPsw(BaseDao dao, DynaBean paramBean, DynaBean productBean, DynaBean appBean,
			List<DynaBean> paramList, DynaBean result) {
		List<DynaBean> dy = dao
				.findWithQuery(new DynaBean("C_USER", " and USERTOKEN='" + paramBean.getStr(USERTOKEN) + "'"));
		DynaBean dynaBean = dy.get(0);
		dynaBean.setValues(paramBean.getValues());
		dynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "C_USER");
		dynaBean.setStr("PWD", MD5Util.MD5(paramBean.getStr("PASSWORD")));
		dynaBean.setStr(BeanUtils.KEY_WHERE," and USERTOKEN='" + paramBean.getStr(USERTOKEN) + "'");
		dao.updateOne(dynaBean);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月21日 下午5:37:02 方法说明:
	 * @param dao
	 * @param paramBean
	 * @param productBean
	 * @param appBean
	 * @param paramList
	 * @param result
	 */
	private static void register(BaseDao dao, DynaBean paramBean, DynaBean productBean, DynaBean appBean,
			List<DynaBean> paramList, DynaBean result) {
		DynaBean dynaBean = new DynaBean();
		dynaBean.setValues(paramBean.getValues());
		dynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "C_USER");
		dynaBean.setStr("PWD", MD5Util.MD5(paramBean.getStr("PASSWORD")));
		dynaBean.setStr("CREATETIME", DateUtils.getDatetime());
		dynaBean.setStr("TEL", paramBean.getStr("MOBILEPHONE"));
		dao.insertOne(dynaBean);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月21日 下午5:36:54 方法说明:
	 * @param str
	 */
	private static void logout(BaseDao dao, String usertoken) {
		DynaBean session = new DynaBean("C_SESSION", "and USERTOKEN='" + usertoken + "'");
		List<DynaBean> userSessionList = dao.findWithQuery(session);
		// 曾经登录过
		if (userSessionList.size() > 0) {
			session = userSessionList.get(0);
		}
		session.setStr("USERTOKEN", usertoken);
		session.setStr("STATUS", "N");// 记录在线状态
		session.setStr("LASTLOGOUTTIME", DateUtils.getDatetime());// 记录登录时间
		session.setStr(BeanUtils.KEY_TABLE_CODE, "C_SESSION");
		if (userSessionList.size() > 0) {
			dao.updateOne(session);
		} else {
			dao.insertOne(session);
		}
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月21日 下午5:36:51 方法说明:
	 * @param str
	 * @param str2
	 */
	private static void login(BaseDao dao, String usertoken) {
		DynaBean session = new DynaBean("C_SESSION", "and USERTOKEN='" + usertoken + "'");
		List<DynaBean> userSessionList = dao.findWithQuery(session);
		// 曾经登录过
		if (userSessionList.size() > 0) {
			session = userSessionList.get(0);
		}
		session.setStr("USERTOKEN", usertoken);
		session.setStr("SESSIONID", SyConstant.getUUID());
		session.setStr("STATUS", "Y");// 记录在线状态
		session.setStr("LOGTIME", DateUtils.getDatetime());// 记录登录时间
		session.setStr(BeanUtils.KEY_TABLE_CODE, "C_SESSION");		
		if (userSessionList.size() > 0) {
			dao.updateOne(session);
		} else {
			dao.insertOne(session);
		}
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月21日 下午5:36:48 方法说明:
	 * @param dao
	 * @param paramBean
	 * @param productBean
	 * @param appBean
	 * @param paramList
	 * @param result
	 * @return
	 */
	private static boolean checkUserPassword(BaseDao dao, DynaBean paramBean, DynaBean productBean, DynaBean appBean,
			List<DynaBean> paramList, DynaBean result) {
		DynaBean dynaBean = new DynaBean("C_USER",
				"and PWD='" + MD5Util.MD5(paramBean.getStr("PASSWORD"))
						+ "' and USERTOKEN='" + paramBean.getStr(USERTOKEN) + "'");
		if (dao.findCountWithQuery(dynaBean) == 0) {
			result.setStr(CODE, "61001");// 密码不正确
			result.set(MSG, getBackName(dao, "61001"));
			return false;
		}
		return true;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月21日 上午9:13:11 方法说明: 注册校验 重复注册 用户名重复
	 * @param dao
	 * @param paramBean
	 * @param productBean
	 * @param appBean
	 * @param paramList
	 * @param result
	 * @param isUpdate
	 *            是否是修改密码
	 * @return
	 */
	private static boolean checkUser(BaseDao dao, DynaBean paramBean, DynaBean productBean, DynaBean appBean,
			List<DynaBean> paramList, DynaBean result, boolean isUpdate) {
		// 校验验证码是否正确
		if (dao.findCountWithQuery(new DynaBean("C_TELSMS",
				"and USERTOKEN='" + paramBean.getStr("USERTOKEN") + "' and MOBILEPHONE='"
						+ paramBean.getStr("MOBILEPHONE") + "'" + " and SMSCODE='" + paramBean.getStr("AUTHCODE") + "'"
						+ " and RANDOM='" + paramBean.getStr("RANDOM", "") + "'")) == 0) {
			result.set(CODE, "61005");// 手机验证码错误
			result.set(MSG, getBackName(dao, "61005"));
			return false;
		}
		//找到此用户
		List<DynaBean> userList = dao.findWithQuery(
				new DynaBean("C_USER", " and USERTOKEN='" + paramBean.getStr(USERTOKEN) + "'"));
		if (!isUpdate) {
			if (dao.findCountWithQuery(
					new DynaBean("C_USER", " and TEL='" + paramBean.getStr("MOBILEPHONE") + "'")) > 0) {
				result.set(CODE, "61003");// 此用户手机号已经注册
				result.set(MSG, getBackName(dao, "61003"));
				return false;
			}
			if (dao.findCountWithQuery(
					new DynaBean("C_USER", " and USERID='" + paramBean.getStr("USERID") + "'")) > 0) {
				result.set(CODE, "61004");// 此用户号已经注册
				result.set(MSG, getBackName(dao, "61004"));
				return false;
			}
			if(userList.size()>0){
				result.set(CODE, "61002");// 存在此用户异常
				result.set(MSG, getBackName(dao, "61002"));
				return false;
			}
		}else{
			if(userList.size()==0){
				result.set(CODE, "61006");//此用户没有注册,请注册
				result.set(MSG, getBackName(dao, "61006"));
				return false;
			}			
		}
		return true;
	}


	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月22日 下午2:47:01 说明:手机号码校验
	 * @param dao
	 * @param paramBean
	 * @param productBean
	 * @param appBean
	 * @param paramList
	 * @param result
	 * @return
	 */
	private static boolean checkTel(BaseDao dao, DynaBean paramBean, DynaBean productBean, DynaBean appBean,
			List<DynaBean> paramList, DynaBean result) {
		String telnum = paramBean.getStr("MOBILEPHONE", "");
		if (telnum.length() != 11) {
			result.setStr(CODE, "40040");// 手机号必须为11位数字
			result.setStr(MSG, getBackName(dao, "40040"));
			return false;
		}
		if (TelUtil.TELNUM.indexOf(telnum.substring(0, 3)) == -1) {
			result.setStr(CODE, "40041");// 不被支持的手机号
			result.setStr(MSG, getBackName(dao, "40040"));
			return false;
		}
		return true;
	}

	/**
	 * 校验产品PID合法性
	 * 
	 * @param dao
	 * @param paramBean
	 * @param result
	 * @return
	 */
	private static boolean checkPid(BaseDao dao, DynaBean paramBean, DynaBean result) {
		DynaBean implBean = new DynaBean("SYS_INTERFACE");
		implBean.setStr(BeanUtils.KEY_WHERE, " and PID='" + paramBean.getStr(PID, "") + "'");
		if (dao.findCountWithQuery(implBean) == 0) {
			result.setStr(CODE, "40001");// 不合法的产品凭证
			result.setStr(MSG, getBackName(dao, "40001"));
			return false;
		}
		return true;
	}

	/**
	 * 校验应用AID合法性
	 * 
	 * @param dao
	 * @param paramBean
	 * @param result
	 * @return
	 */
	private static boolean checkAid(BaseDao dao, DynaBean paramBean, DynaBean result) {
		DynaBean implBean = new DynaBean("SYS_APP");
		implBean.setStr(BeanUtils.KEY_WHERE, " and APPID in (select APPID from SYS_INTERFACE where PID='"
				+ paramBean.getStr(PID, "") + "' and REQURL='" + paramBean.getStr(AID, "") + "')");
		if (dao.findCountWithQuery(implBean) == 0) {
			result.setStr(CODE, "40003");// 不合法的AID凭证
			result.setStr(MSG, getBackName(dao, "40003"));
			return false;
		}
		return true;
	}

	/**
	 * 校验产品应用版本合法性
	 * 
	 * @param dao
	 * @param paramBean
	 * @param result
	 * @return
	 */
	private static boolean checkVersion(BaseDao dao, DynaBean paramBean, DynaBean result) {
		DynaBean implBean = new DynaBean("SYS_INTERFACE");
		implBean.setStr(BeanUtils.KEY_WHERE,
				"and version='" + paramBean.getStr(VERSION) + "' and REQTYPE='" + paramBean.getStr(ACT, "")
						+ "' and REQURL='" + paramBean.getStr(AID, "") + "' and PID='" + paramBean.getStr(PID, "")
						+ "'");
		if (dao.findCountWithQuery(implBean) == 0) {
			result.setStr(CODE, "40006");// 不合法的版本号
			result.setStr(MSG, getBackName(dao, "40006"));
			return false;
		}
		return true;
	}

	/**
	 * 校验产品应用的操作ACT合法性
	 * 
	 * @param dao
	 * @param paramBean
	 * @param result
	 * @return
	 */
	private static boolean checkAct(BaseDao dao, DynaBean paramBean, DynaBean result) {
		DynaBean implBean = new DynaBean("SYS_INTERFACE");
		implBean.setStr(BeanUtils.KEY_WHERE, "and REQTYPE='" + paramBean.getStr(ACT, "") + "' and REQURL='"
				+ paramBean.getStr(AID, "") + "' and PID='" + paramBean.getStr(PID, "") + "'");
		if (dao.findCountWithQuery(implBean) == 0) {
			result.setStr(CODE, "40004");// 不合法的AID凭证
			result.setStr(MSG, getBackName(dao, "40004"));
			return false;
		}
		return true;
	}

	// 校验接口合法性
	private static boolean checkParams(BaseDao dao, DynaBean paramBean, JSONObject dataJson, DynaBean interfaceBean,
			List<DynaBean> paramList, DynaBean result) {
		if (dataJson == null) {
			result.setStr(CODE, "40005");// 不合法的消息体
			result.setStr(MSG, getBackName(dao, "40005"));
			return false;
		}
		// 缺少USERTOKEN 41001
		if (interfaceBean.getStr("NEEDUSERTOKEN", "N").equals("Y") && paramBean.getStr(USERTOKEN, "").length() == 0) {
			result.setStr(CODE, "41001");// 缺少USERTOKEN
			result.setStr(MSG, getBackName(dao, "41001"));
			return false;
		}
		// 校验USERTOKEN的合法性
		if (interfaceBean.getStr("NEEDUSERTOKEN", "N").equals("Y")) {
			List<DynaBean> userTokenList = dao.findWithQuery(
					new DynaBean("SYS_PRODUCTUSER", "and USERTOKEN='" + paramBean.getStr(USERTOKEN) + "'"));
			// 不存在此用户token
			if (userTokenList.size() == 0) {
				result.setStr(CODE, "46004");// 非法用户token
				result.setStr(MSG, getBackName(dao, "46004"));
				return false;
			}
			DynaBean userToken = (DynaBean) userTokenList.get(0);
			// 用户受限，可能是违规后接口被封禁
			if (!userToken.getStr("ENABLE", "").equals("Y")) {
				result.setStr(CODE, "50002");// 用户受限，可能是违规后接口被封禁
				result.setStr(MSG, getBackName(dao, "50002"));
				return false;
			}
			if (!userToken.getStr("PID", "").equals(paramBean.getStr(PID))) {
				result.setStr(CODE, "50001");// 未被授权的用户api
				result.setStr(MSG, getBackName(dao, "50001"));
				return false;
			}
		}
		// 缺少参数检查
		for (DynaBean inparam : paramList) {
			// 只检查入口参数
			if (inparam.getStr("GETPUT", "PUT").equals("PUT")) {
				continue;
			}
			String paramName = inparam.getStr("PARAMNAME", "");
			// 是否必须的参数
			boolean isnull = inparam.getStr("ISNULL", "N").equals("Y");
			// 必须要的参数不能为空
			if (isnull) {
				// 不能为空字符串
				if (paramBean.getStr(paramName, "").length() == 0) {
					if (!dataJson.containsKey(paramName)) {
						result.setStr(CODE, "41002");
						result.set(MSG, getBackName(dao, "41002") + ":" + paramName);
						return false;
					} else if (dataJson.get(paramName).toString().length() == 0) {
						result.setStr(CODE, "41003");
						result.set(MSG, getBackName(dao, "41003") + ":" + paramName);
						return false;
					}
				}
			}
			// 不合法的偏移量
			if (paramBean.getStr(ACT, "").equals("LISTINFO")) {
				if (paramBean.getInt("PAGESIZE", 0) > 500) {
					result.setStr(CODE, "40116");
					result.set(MSG, getBackName(dao, "41116"));
					return false;
				}
			}

		}
		return true;
	}

	/**
	 * 
	 * @param dao
	 * @param backCode
	 * @return
	 */
	private static String getBackName(BaseDao dao, String backCode) {
		List<DynaBean> backBeanList = dao
				.findWithQuery(new DynaBean("SYS_BACKCODE", " and backCode ='" + backCode + "'"));
		if (backBeanList.size() == 0) {
			return "未知错误";
		}
		return backBeanList.get(0).getStr("BACKNAME", "");
	}

	/**
	 * 
	 * @return
	 */
	private static DynaBean createMethod(BaseDao dao, DynaBean paramBean, DynaBean product, DynaBean appBean) {
		DynaBean insertBean = new DynaBean();
		insertBean.setValues(paramBean.getValues());
		insertBean.set(BeanUtils.KEY_TABLE_CODE, appBean.getStr("MAINTABLE"));
		// 设置默认值
		BeanUtils.setDefaultValueByAppFieldSetting(appBean, insertBean, true);
		insertBean.set("APPID", appBean.getStr("APPID"));
		insertBean.set("PID", paramBean.getStr(PID));
		dao.insertOne(insertBean);
		return insertBean;
	}

	/**
	 * 根据新建的内容返回值
	 * 
	 * @param dao
	 * @param insertBean
	 * @param interfaceparamList
	 * @return
	 */
	private static Map<String, String> createBackParam(BaseDao dao, DynaBean insertBean,
			List<DynaBean> interfaceparamList) {
		Map<String, String> map = new HashMap<String, String>();
		for (DynaBean param : interfaceparamList) {
			// 输出参数才需要循环
			if (param.getStr("GETPUT", "GET").equals("GET")) {
				continue;
			}
			// 首先从当前对象中获取值
			String value = insertBean.getStr(param.getStr("PARAMNAME", ""), "");
			// 当前对象中没有此值
			if (value.length() == 0) {
				// 从父级对象中获取值

			}
			map.put(param.getStr("PARAMNAME", ""), value);
		}
		return map;
	}
}

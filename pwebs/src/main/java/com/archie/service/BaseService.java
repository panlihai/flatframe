package com.archie.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.archie.dao.BaseDao;
import com.archie.model.DynaBean;
import com.archie.model.user.UserInfo;
import com.archie.util.BeanUtils;
import com.archie.util.CacheUtil;
import com.archie.util.PageUtil;
import com.archie.util.ServiceUtil;
import com.archie.util.SyConstant;

/**
 * 
 * @author panlihai E-mail:panlihai@zlwh.com.cn
 * @version 创建时间：2015年12月4日 上午11:04:01 类说明:
 */
public class BaseService {

	private static Logger logger = Logger.getLogger(BaseService.class);
	@Resource(name = "baseDao")
	private BaseDao baseDao;

	public BaseDao getBaseDao() {
		return baseDao;
	}

	public void setBaseDao(BaseDao baseDao) {
		this.baseDao = baseDao;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param dynaBean
	 * 
	 */
	public List<DynaBean> findWithQuery(DynaBean dynaBean) {
		return baseDao.findWithQuery(dynaBean);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据表及主表Id获取主表内容 0,20
	 * @param maintable
	 *            表名称
	 * @param id
	 *            表id值
	 * 
	 */
	public DynaBean findOneWithQuery(String maintable, String id) {
		return baseDao.findOneWithQuery(maintable.toUpperCase(), id);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 获取总记录数
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public long findCountWithQuery(DynaBean dynaBean) {
		return baseDao.findCountWithQuery(dynaBean);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 不同表批量插入,不推荐使用相同表的插入
	 * @param beanList
	 * @return 插入条数
	 */
	public long insertList(List<DynaBean> beanList) {
		return baseDao.insertList(beanList);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 相同表批量插入.
	 * @param beanList
	 * @return 插入条数
	 */
	public long insertBatList(List<DynaBean> beanList) {
		return baseDao.insertBatList(beanList);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 插入.
	 * @param beanList
	 * @return 插入条数
	 */
	public long insertOne(DynaBean dynaBean) {
		return baseDao.insertOne(dynaBean);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 插入.
	 * @param beanList
	 * @return 插入条数
	 */
	public long insertOne(DynaBean dynaBean, DynaBean tableDef) {
		return baseDao.insertOne(dynaBean, tableDef);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 批量对相同表的插入
	 * @param beanList
	 * @return 插入条数
	 */
	public long updateBatList(List<DynaBean> beanList, DynaBean tableDef) {
		return baseDao.updateBatList(beanList, tableDef);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 批量插入.同表
	 * @param beanList
	 * @return 插入条数
	 */
	public long updateList(List<DynaBean> beanList) {
		return baseDao.updateList(beanList);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 批量插入.
	 * @param beanList
	 * @return 插入条数
	 */
	public long updateOne(DynaBean dynaBean, DynaBean tableDef) {
		return baseDao.updateOne(dynaBean, tableDef);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 批量插入.
	 * @param beanList
	 * @return 插入条数
	 */
	public long updateOne(DynaBean dynaBean) {
		return baseDao.updateOne(dynaBean);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 删除.
	 * @param dynaBean
	 * @param tableDef
	 * @return 删除条数
	 */
	public long delete(DynaBean dynaBean) {
		return baseDao.delete(dynaBean);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 删除.
	 * @param dynaBean
	 * @param tableDef
	 * @return 删除条数
	 */
	public long delete(DynaBean dynaBean, DynaBean tableDef) {
		return baseDao.delete(dynaBean, tableDef);

	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据appCode获取app对象.
	 * @param appCode
	 * @return 当前app对象
	 */
	public DynaBean findSysAppByCode(String appCode,String pId) {
		if (appCode == null) {
			return null;
		}
		// 从缓存中获取
		DynaBean appBean = (DynaBean) CacheUtil.getSysapp(baseDao.getMemCached(), appCode,pId);
		if (appBean == null) {
			return null;
		}
		// 如果没有字段级按钮则自动生成标准字段及按钮
		List<DynaBean> fieldList = (List<DynaBean>) appBean.get(PageUtil.PAGE_APPFIELDS);
		if (fieldList == null || fieldList.size() == 0) {
			appBean.set(PageUtil.PAGE_APPFIELDS, saveSysAppFieldsFromTableDef(appBean));
		}
		List<DynaBean> btnList = (List<DynaBean>) appBean.get(PageUtil.PAGE_APPBUTTONS);
		if (btnList == null || btnList.size() == 0) {
			// 自动写入按钮内容
			appBean.set(PageUtil.PAGE_APPBUTTONS, findButtonsFromAppid(appCode,pId));
		}
		return appBean;
	}
	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据appCode获取app对象.
	 * @param appCode
	 * @return 当前app对象
	 */
	public DynaBean findSysMenuByMenuid(String menuId) {
		if (menuId == null) {
			return null;
		}
		// 从缓存中获取
		DynaBean menuBean = (DynaBean) CacheUtil.getDynaBeanByTypeCache(baseDao.getMemCached(),CacheUtil.SYSMENU,menuId);
		if (menuBean == null) {
			return null;
		}
		// 如果没有字段级按钮则自动生成标准字段及按钮		
		return menuBean;
	}
	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据appCode获取关联app对象.
	 * @param appCode
	 * @return 当前app对象
	 */
	public List<DynaBean> findSysAppLinksByMainAppId(String appCode,String pId) {
		DynaBean linkappBean = new DynaBean("SYS_APPLINKS", "and MAINAPP='" + appCode + "'");
		linkappBean.setStr(BeanUtils.KEY_SELECT, "ITEMAPP");
		List<Map> mapList = baseDao.findWithQueryNoCacheMap(linkappBean);
		List<DynaBean> applinksList = new ArrayList<DynaBean>();
		for (Map map : mapList) {
			DynaBean appBean = (DynaBean) CacheUtil.getSysapp(baseDao.getMemCached(), map.get("ITEMAPP").toString(),pId);
			applinksList.add(appBean);
		}
		return applinksList;
	}

	/**
	 * 根据表名插入应用程序默认字段内容.
	 * 
	 * @param tableName
	 * @return
	 */
	public List<DynaBean> saveSysAppFieldsFromTableDef(String appCode,String pId) {
		return saveSysAppFieldsFromTableDef(findSysAppByCode(appCode,pId));
	}

	/**
	 * 根据表名插入应用程序默认字段内容.
	 * 
	 * @param tableName
	 * @return
	 */
	public List<DynaBean> saveSysAppFieldsFromTableDef(DynaBean appBean) {
		DynaBean defBean = baseDao.getTableDef(appBean.getStr("MAINTABLE"));
		if (findCountWithQuery(new DynaBean("SYS_APPFIELDS", " and APPID='" + appBean.getStr("APPID") + "'")) != 0) {
			this.delete(new DynaBean("SYS_APPFIELDS", " and APPID='" + appBean.getStr("APPID") + "'"));
		}
		List<DynaBean> saveList = ServiceUtil.getFieldBeanByAppBean(appBean, defBean);
		insertBatList(saveList);
		return saveList;

	}

	/**
	 * 根据应用程序编码,主应用程序对应的表数据ID获取表内容
	 * 
	 * @param mainApp
	 * @param mainId
	 * @return 表数据
	 */
	public DynaBean getBeanByAppIdBeanId(final String mainApp, final String mainId,final String pId) {
		// 得到主应用
		DynaBean mainAppBean = findSysAppByCode(mainApp,pId);
		// 得到主应用对应的主表数据内容
		return findOneWithQuery(mainAppBean.getStr("MAINTABLE"), mainId);
	}

	/**
	 * 根据应用程序及表数据ID获取表内容
	 * 
	 * @param mainAppBean
	 * @param mainId
	 * @return 表数据
	 */
	public DynaBean getBeanByAppBeanMainId(final DynaBean mainAppBean, final String mainId) {
		// 得到主应用对应的主表数据内容
		return findOneWithQuery(mainAppBean.getStr("MAINTABLE"), mainId);
	}

	/**
	 * 根据主应用程序名称,主应用编码,主应用程序对应的表数据ID得到关联应用程序的查询条件
	 * 
	 * @param mainAppBean
	 *            主应用程序
	 * @param itemApp
	 *            子应用程序的编码
	 * @param mainId
	 *            主表ID
	 * @return
	 */
	public DynaBean getLinkItemBeanBy(final DynaBean mainAppBean, final String itemAppId) {
		// 得到主应用对应的关联应用
		@SuppressWarnings("unchecked")
		List<DynaBean> linkAppList = (List<DynaBean>) mainAppBean.get(PageUtil.PAGE_APPLINKS);
		// 获取sql过滤条件并把内容从对象内容中获取,支持参数获取,系统参数包括用户内容参数暂时没有加入.
		for (DynaBean linkBean : linkAppList) {
			// 取到对应的关联应用
			if (linkBean.getStr("ITEMAPP", "").equals(itemAppId)) {
				return linkBean;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param mainApp
	 * @param itemAppId
	 * @param mainId
	 * @return
	 */
	public String getSqlWhereBy(final String mainApp, final String mainId, final String itemAppId,final String pId) {
		// 得到主应用
		DynaBean mainAppBean = findSysAppByCode(mainApp,pId);
		if (mainAppBean == null) {
			return "";
		}
		StringBuffer sqlSb = new StringBuffer(" ");
		// 得到关联子应用的数据内容
		DynaBean linkBean = getLinkItemBeanBy(mainAppBean, itemAppId);
		// 得到主数据
		DynaBean parentBean = findOneWithQuery(mainAppBean.getStr("MAINTABLE"), mainId);
		// 解析条件
		String filter = linkBean.getStr("LINKFILTER", "");
		String[] where = filter.split(":\\{");
		for (String str : where) {
			if (str.indexOf("}") == -1) {
				sqlSb.append(str.replaceAll(itemAppId + "\\.", ""));
			} else {
				String[] str2 = str.split("\\}");
				sqlSb.append(parentBean.getStr(str2[0].toUpperCase(), "")).append(str2[1]);
			}
		}
		// 把子表的应用名称去除掉
		return sqlSb.toString().replaceAll(itemAppId+"\\.", "");
	}

	/**
	 * 给子应用设置初始化参数值
	 * 
	 * @param mainApp
	 * @param mainId
	 * @param itemAppId
	 * @return
	 */
	public DynaBean getDefaultFieldValueByLinkFilter(final String mainApp, final String mainId,
			final String itemAppId,final String pId) {
		// 得到主应用
		DynaBean mainAppBean = findSysAppByCode(mainApp,pId);
		// 得到主数据
		DynaBean dataBean = getBeanByAppBeanMainId(mainAppBean, mainId);
		// 得到关联子应用的数据内容
		DynaBean linkBean = getLinkItemBeanBy(mainAppBean, itemAppId);
		// 获取子应用对应的字段内容列表
		// 获取子应用程序
		DynaBean childAppBean = findSysAppByCode(linkBean.getStr("ITEMAPP"),pId);
		// 从应用程序中获取字段
		List<DynaBean> fieldsList = (List<DynaBean>) childAppBean.get(PageUtil.PAGE_APPFIELDS);
		// 设置默认值
		return ServiceUtil.getDefaultFieldValueByLinkFilter(dataBean, itemAppId, fieldsList,
				linkBean.getStr("LINKFILTER", ""));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月16日 上午11:27:44 方法说明: 获取该产品的所有的子菜单对象 嵌套获取子菜单
	 *          暂时未加入用户权限
	 * @param string
	 * @param userInfo
	 */
	public List<DynaBean> selectMenusByParentMenus(final String pId, final String parentMenuCode,
			final UserInfo userInfo) {
		String sqlwhere = "";
		// 最顶端的菜单
		if (parentMenuCode.length()==0) {
			sqlwhere = " and (PARENT is null or PARENT='')";
		} else if (parentMenuCode.length() > 0) {
			sqlwhere = " and PARENT ='" + parentMenuCode + "'";
		}
		// 加上过滤条件,获取到公告的菜单及私有的菜单
		sqlwhere += " and PID in (select PID from SYS_PRODUCT where PID='" + pId + "' or ISOPEN='" + SyConstant.STR_YES
				+ "')";
		DynaBean menuBean = new DynaBean("SYS_MENU", " and ENABLE='" + SyConstant.STR_YES + "' " + sqlwhere);
		menuBean.setStr(BeanUtils.KEY_ORDER, "SORT");
		menuBean.setStr(BeanUtils.KEY_SELECT, "MENUID");
		List<Map> idsList = baseDao.findWithQueryNoCacheMap(menuBean);
		List<DynaBean> menuList = new ArrayList<DynaBean>();
		for (Map map : idsList) {
			DynaBean dynaBean = CacheUtil.getDynaBeanByTypeCache(baseDao.getMemCached(), CacheUtil.SYSMENU,
					map.get("MENUID").toString());
			if (dynaBean != null) {
				String parentCode = dynaBean.getStr("PARENT", "");
				if (parentCode.length() > 0) {
					DynaBean pBean = new DynaBean();
					dynaBean.set(PageUtil.PAGE_CHILDMENUS,
							selectMenusByParentMenus(pId, dynaBean.getStr("MENUID", ""), userInfo));
				}
				menuList.add(dynaBean);
			}
		}
		return menuList;
	}

	/**
	 * 根据应用程序对应的数据内容 json串方式返回
	 * 
	 * @param paramBean
	 * @return
	 */
	public String listJsonFromAppid(String appId,String pId) {
		// 根据APPCODE获取APP对象
		DynaBean appBean = findSysAppByCode(appId,pId);
		return listJsonFromTable(appBean.getStr("MAINTABLE"), appBean.getStr(BeanUtils.KEY_WHERE));
	}

	/**
	 * 根据条件获取表对应的数据内容 json串方式返回
	 * 
	 * @param paramBean
	 * @return
	 */
	public String listJsonFromTable(final String tableName, final String keyWhere) {
		DynaBean dynaBean = new DynaBean(tableName, keyWhere);
		List<DynaBean> list = findWithQuery(dynaBean);
		return BeanUtils.listBeanToJson(list);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月29日 下午2:18:34 方法说明: 获取数据字典列表
	 * @param
	 */
	public String listJsonValueByDicId(String appId) {
		DynaBean dynaBean = new DynaBean("SYS_DICDETAIL", " and DICID='" + appId + "'");
		dynaBean.set(BeanUtils.KEY_ORDER, "SORT");
		List<DynaBean> list = findWithQuery(dynaBean);
		return BeanUtils.listBeanToJson(list);
	}

	/**
	 * 根据appId获取字段内容
	 * 
	 * @param appId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DynaBean> findFieldsFromAppid(String appId,String pId) {
		// 获取应用程序
		DynaBean appBean = CacheUtil.getSysapp(baseDao.getMemCached(), appId,pId);
		// 获取应用的字段
		List<DynaBean> fieldList = (List<DynaBean>) appBean.get(PageUtil.PAGE_APPFIELDS);
		// 如果没有字段则插入所有字段到库中
		if (fieldList == null || fieldList.size() == 0) {
			return saveSysAppFieldsFromTableDef(appId,pId);
		}
		return fieldList;
	}

	/**
	 * 根据appId获取按钮
	 * 
	 * @param appId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DynaBean> findButtonsFromAppid(String appId,String pId) {
		DynaBean appBean = CacheUtil.getSysapp(baseDao.getMemCached(), appId,pId);
		List<DynaBean> beanList = (List<DynaBean>) appBean.get(PageUtil.PAGE_APPBUTTONS);
		// 如果没有按钮则插入所有按钮到库中
		if (beanList == null || beanList.size() == 0) {
			beanList = ServiceUtil.getSysAppButtonsByTempletModel(appBean.getStr("APPID"));
			baseDao.insertBatList(beanList);
		}
		return beanList;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月16日 下午2:06:28 方法说明:清除缓存
	 * @param str
	 */
	public void clearCache() {
		CacheUtil.clearCache(baseDao);
		initSystem();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月16日 下午2:06:28 方法说明:自动导入默认的菜单按钮内容;未来重构内容
	 * @param str
	 */
	public void initSystem() {
		CacheUtil.serverInitialized(baseDao);
	}
}

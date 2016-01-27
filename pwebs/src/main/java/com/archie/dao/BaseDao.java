package com.archie.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.archie.model.DynaBean;
import com.archie.util.BeanUtils;
import com.archie.util.Cache;
import com.archie.util.CacheUtil;
import com.archie.util.SyConstant;
import com.archie.util.sql.SQLAdapter;
import com.archie.util.sql.SqlBuilder;
import com.ibatis.sqlmap.client.SqlMapClient;
/**
 * @author Administrator
 *
 */
public class BaseDao extends SqlMapClientDaoSupport {
	/**
	 * memClient
	 */
	private Cache memCached;

	/**
	 * @return the memClient
	 */
	public Cache getMemCached() {
		return memCached;
	}

	/**
	 * 
	 */
	public void setMemCached(Cache memCached) {
		this.memCached = memCached;
	}

	@Resource(name = "sqlMapClient")
	// 通过bean名称注入
	private SqlMapClient sqlMapClient;

	@PostConstruct
	// 完成sqlMapClient初始化工作
	public void initSqlMapClient() {
		super.setSqlMapClient(sqlMapClient);
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月11日 上午11:05:46 方法说明: 获取总记录数
	 * @param
	 */
	public long findCountWithQuery(DynaBean dynaBean) {
		String sql;
		try {
			sql = SqlBuilder.getSelectCountSql(dynaBean, getTableDef(dynaBean.getStr(BeanUtils.KEY_TABLE_CODE)));
			logger.info(sql);
			SQLAdapter sqlAda = new SQLAdapter();
			sqlAda.setSql(sql);
			return (Long) sqlMapClient.queryForObject(SyConstant.SELECTCOUNT, sqlAda);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public DynaBean findOneWithQuery(String tableName, String id) {
		try {
			List<DynaBean> dynaList = null;
			// 判断是否启用memcached,否则从数据库中去
			if (!getMemCached().used()) {
				// 默认获取key内容,通过key从缓存中获取内容 ID为KEY内容
				dynaList = findWithQueryNoCache(new DynaBean(tableName, " and ID='" + id + "'"));

			} else {
				dynaList = findWithQuery(new DynaBean(tableName, " and ID='" + id + "'"));
			}
			if (dynaList.size() > 0) {
				return dynaList.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public List<DynaBean> findWithQuery(DynaBean dynaBean) {
		return findWithQuery(dynaBean, getTableDef(dynaBean.getStr(BeanUtils.KEY_TABLE_CODE)));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public List<DynaBean> findWithQueryNoCache(DynaBean dynaBean) {
		return findWithQueryNoCache(dynaBean, getTableDef(dynaBean.getStr(BeanUtils.KEY_TABLE_CODE)));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public List<DynaBean> findWithQueryNoCache(DynaBean dynaBean, DynaBean tableDef) {
		String sql;
		try {
			sql = SqlBuilder.getSelectSql(dynaBean, tableDef);
			logger.info(sql);
			System.out.println(new Date()+sql);
			SQLAdapter sqlAda = new SQLAdapter();
			sqlAda.setSql(sql);
			List<Map> list = sqlMapClient.queryForList(SyConstant.SELECT, sqlAda);
			return BeanUtils.listMapToDynaBean(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public List<Map> findWithQueryMap(DynaBean dynaBean) {
		return findWithQueryMap(dynaBean, getTableDef(dynaBean.getStr(BeanUtils.KEY_TABLE_CODE)));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public List<Map> findWithQueryMap(DynaBean dynaBean, DynaBean tableDef) {
		String sql;
		try {
			sql = SqlBuilder.getSelectSql(dynaBean, tableDef);
			logger.info(sql);
			SQLAdapter sqlAda = new SQLAdapter();
			sqlAda.setSql(sql);
			List<Map> list = sqlMapClient.queryForList(SyConstant.SELECT, sqlAda);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public List<Map> findWithQueryNoCacheMap(DynaBean dynaBean) {
		return findWithQueryNoCacheMap(dynaBean, getTableDef(dynaBean.getStr(BeanUtils.KEY_TABLE_CODE)));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public List<Map> findWithQueryNoCacheMap(DynaBean dynaBean, DynaBean tableDef) {
		String sql;
		try {
			sql = SqlBuilder.getSelectSql(dynaBean, tableDef);
			logger.info(sql);
			SQLAdapter sqlAda = new SQLAdapter();
			sqlAda.setSql(sql);
			List<Map> list = sqlMapClient.queryForList(SyConstant.SELECT, sqlAda);
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 根据pojo类自动生成查询语句并执行查询操作.limit
	 *          0,20
	 * @param beanClass
	 *            当前类
	 * @param queryString
	 *            查询条件
	 */
	public List<DynaBean> findWithQuery(DynaBean dynaBean, DynaBean tableDef) {
		String sql;
		try {
			// 判断是否启用memcached,否则从数据库中去
			if (getMemCached().used()) {
				// 默认获取key内容,通过key从缓存中获取内容 ID为KEY内容
				if (dynaBean.getStr(BeanUtils.KEY_SELECT, "").length() == 0) {
					dynaBean.setStr(BeanUtils.KEY_SELECT, "ID");
				}
				sql = SqlBuilder.getSelectSql(dynaBean, tableDef);
				logger.info(sql);
				SQLAdapter sqlAda = new SQLAdapter();
				sqlAda.setSql(sql);
				List<Map> list = sqlMapClient.queryForList(SyConstant.SELECT, sqlAda);
				List<DynaBean> dynaList = new ArrayList<DynaBean>();
				for (Map map : list) {
					DynaBean bean = (DynaBean) memCached.get(map.get("ID").toString());
					if (bean != null) {
						dynaList.add(bean);
					}
				}
				if (list.size() != dynaList.size()) {
					// 如果缓存中没有,则重新查询
					dynaBean.setStr(BeanUtils.KEY_SELECT, "");
					return findWithQueryNoCache(dynaBean, tableDef);
				}
				return dynaList;
			} else {
				return findWithQueryNoCache(dynaBean, tableDef);
			}
			// return BeanUtils.listMapToDynaBean(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 不同表批量插入,不推荐使用相同表的插入
	 * @param beanList
	 * @return 插入条数
	 */
	public long insertList(List<DynaBean> beanList) {
		try {
			int batch = 0;
			sqlMapClient.startBatch();
			for (DynaBean dynaBean : beanList) {
				batch++;
				insertOne(dynaBean);
				if (batch == 5000) {
					sqlMapClient.executeBatch();
					batch = 0;
				}
			}
			sqlMapClient.executeBatch();
			return beanList.size();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
		return 0;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 相同表批量插入.
	 * @param beanList
	 * @return 插入条数
	 */
	public long insertBatList(List<DynaBean> beanList) {
		try {
			int batch = 0;
			sqlMapClient.startBatch();
			DynaBean tableDef = null;
			if (beanList.size() > 0) {
				tableDef = getTableDef(beanList.get(0).getStr(BeanUtils.KEY_TABLE_CODE));
			}
			for (DynaBean dynaBean : beanList) {
				batch++;
				insertOne(dynaBean, tableDef);
				if (batch == 5000) {
					sqlMapClient.executeBatch();
					batch = 0;
				}
			}
			if (batch == 0) {
				sqlMapClient.executeBatch();
			}
			return beanList.size();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());
			// try {
			// sqlMapClient.getCurrentConnection().rollback();
			// } catch (SQLException e) {
			// e.printStackTrace();
			// }
		} finally {
			// try {
			// sqlMapClient.commitTransaction();
			// } catch (SQLException e) {
			// e.printStackTrace();
			// }
		}
		return 0;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 插入.会自动为ID字段产生ID内容
	 * @param beanList
	 * @return 插入条数
	 */
	public long insertOne(DynaBean dynaBean) {
		return insertOne(dynaBean, getTableDef(dynaBean.getStr(BeanUtils.KEY_TABLE_CODE)));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 插入.
	 * @param beanList
	 * @return 插入条数
	 */
	public long insertOne(DynaBean dynaBean, DynaBean tableDef) {
		try {
			dynaBean.setStr("ID", SyConstant.getUUID());
			String sql = SqlBuilder.getInsertSql(dynaBean, false, tableDef);
			SQLAdapter sqlAdapter = new SQLAdapter();
			sqlAdapter.setSql(sql);
			sqlMapClient.insert(SyConstant.INSERT, sqlAdapter);
			cached(1, dynaBean);// 1:插入;2:修改;3:删除缓存
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return 1;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月5日 上午8:55:14 方法说明:
	 * @param string
	 * @param appBean
	 * @param dynaBean
	 */
	private void cached(final int action, final DynaBean dynaBean) {
		final BaseDao dao = this;
		new Thread() {
			public void run() {
				// 判断是否需要缓存
				List<DynaBean> appList = findWithQuery(
						new DynaBean("SYS_APP", "and MAINTABLE='" + dynaBean.getStr(BeanUtils.KEY_TABLE_CODE) + "'"));
				for (DynaBean appBean : appList) {
					// 判断是否需要缓存,如果需要则开始缓存
					if (appBean != null && appBean.getStr("ENABLECACHE", "N").equals("Y")) {
						// 如果是MEMCACHE缓存则缓存,否则只缓存系统核心数据 SYSTEM为系统模块
						if (!getMemCached().used() && !appBean.getStr("APPMODEL", "").equals("SYSTEM")) {
							return;
						} else if (appBean.getStr("APPMODEL", "").equals("SYSTEM")) {
							switch (action) {
							case 1: // 新增
							case 2:// 修改
									// 初始化数据结构
								Connection conn = null;
								try {
									// 获取链接
									conn = dao.getSqlMapClient().getDataSource().getConnection();
									switch (appBean.getStr("APPID")) {
									case "SYSAPP":// 自动处理字典及关联应用的缓存。SYSAPP会直接处理缓存
										CacheUtil.initSysapp(getMemCached(),appBean, dao, conn);
										break;
									case "SYSAPPFIELDS":
										CacheUtil.initSysAppFields(getMemCached(), dao, dynaBean.getStr("APPID"));
										break;
									case "SYSAPPBUTTONS":
										CacheUtil.initSysAppButtons(getMemCached(), dao, dynaBean.getStr("APPID"));
										break;
									case "SYSAPPLINKS":
										CacheUtil.initSysAppLinks(getMemCached(), dao, dynaBean.getStr("APPID"));
										break;
									case "SYSDICAPP":
									case "SYSDICAPPDETAIL":
									case "SYSDIC":
									case "SYSDICDETAIL":
										CacheUtil.initSysAppSysDic(getMemCached(), dao, dynaBean.getStr("APPID"));
										break;
									case "SYSPARAM":
										CacheUtil.setTypeCache(getMemCached(), CacheUtil.SYSPARAM,
												dynaBean.getStr("PARAMID"), dynaBean.getStr("ID"));
										break;
									case "SYSROLE":
										CacheUtil.setTypeCache(getMemCached(), CacheUtil.SYSROLE,
												dynaBean.getStr("ROLEID"), dynaBean.getStr("ID"));
										break;
									case "SYSMENU":
										CacheUtil.setTypeCache(getMemCached(), CacheUtil.SYSMENU,
												dynaBean.getStr("MENUID"), dynaBean.getStr("ID"));
										break;
									}
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
								break;
							case 3:
								// 删除的不一定都是有ID传递过来的.
								String[] ids = dynaBean.getStr(BeanUtils.KEY_WHERE, "").split("'");
								for (String id : ids) {
									// 系统模块的缓存
									switch (appBean.getStr("APPID")) {
									case "SYSMENU":
										DynaBean child = (DynaBean) CacheUtil.getTypeCache(getMemCached(),
												CacheUtil.SYSMENU, id);
										if (child != null) {
											CacheUtil.removeTypeCache(getMemCached(), CacheUtil.SYSMENU,
													child.getStr("MENUID"));
										}
										break;
									case "SYSPARAM":
										child = (DynaBean) CacheUtil.getTypeCache(getMemCached(), CacheUtil.SYSPARAM,
												id);
										if (child != null) {
											CacheUtil.removeTypeCache(getMemCached(), CacheUtil.SYSPARAM,
													child.getStr("PARAMID"));
										}
										break;
									case "SYSROLE":
										child = (DynaBean) CacheUtil.getTypeCache(getMemCached(), CacheUtil.SYSROLE,
												id);
										if (child != null) {
											CacheUtil.removeTypeCache(getMemCached(), CacheUtil.SYSROLE,
													child.getStr("ROLEID"));
										}
									case "SYSAPP":
										child = (DynaBean) CacheUtil.getTypeCache(getMemCached(), CacheUtil.SYSAPP, id);
										if (child != null) {
											CacheUtil.removeSysapp(getMemCached(), child.getStr("APPID"));
										}
										break;
									case "SYSDIC":
									case "SYSDICDETAIL":
									case "SYSDICAPP":
									case "SYSDICAPPDETAIL":
										child = (DynaBean) CacheUtil.getTypeCache(getMemCached(), CacheUtil.SYSAPP, id);
										if (child != null) {
											CacheUtil.initSysAppSysDic(getMemCached(), dao, child.getStr("DICID"));
										}
										break;
									case "SYSAPPLINKS":
										child = (DynaBean) CacheUtil.getTypeCache(getMemCached(), CacheUtil.SYSAPPLINKS,
												id);
										if (child != null) {
											CacheUtil.initSysAppLinks(getMemCached(), dao, child.getStr("APPID"));
										}
										break;
									case "SYSAPPFIELDS":
										child = (DynaBean) CacheUtil.getTypeCache(getMemCached(),
												CacheUtil.SYSAPPFIELDS, id);
										if (child != null) {
											CacheUtil.initSysAppFields(getMemCached(), dao, child.getStr("APPID"));
										}
										break;
									case "SYSAPPBUTTONS":
										child = (DynaBean) CacheUtil.getTypeCache(getMemCached(),
												CacheUtil.SYSAPPBUTTONS, id);
										if (child != null) {
											CacheUtil.initSysAppButtons(getMemCached(), dao, child.getStr("APPID"));
										}
										break;

									}
								}
							}
						}
						// 对数据id内容进行处理
						switch (action) {
						case 1:
						case 2:
							getMemCached().set(dynaBean.getStr("ID"), dynaBean);
							break;
						case 3:
							// 删除的不一定都是有ID传递过来的.
							String[] ids = dynaBean.getStr(BeanUtils.KEY_WHERE, "").split("'");
							for (String id : ids) {
								getMemCached().delete(id);
							}
							break;
						}
					}
				}
			}
		}.start();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 批量对相同表的插入
	 * @param beanList
	 * @return 插入条数
	 */
	public long updateBatList(List<DynaBean> beanList, DynaBean tableDef) {
		int batch = 0;
		try {
			sqlMapClient.startBatch();
			for (DynaBean dynaBean : beanList) {
				batch++;
				updateOne(dynaBean, tableDef);
				if (batch == 5000) {
					sqlMapClient.executeBatch();
					batch = 0;
				}
			}
			sqlMapClient.executeBatch();
			sqlMapClient.commitTransaction();
			return beanList.size();
		} catch (SQLException ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
		return 0;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 批量插入.同表
	 * @param beanList
	 * @return 插入条数
	 */
	public long updateList(List<DynaBean> beanList) {
		int batch = 0;
		try {
			sqlMapClient.startBatch();
			for (DynaBean dynaBean : beanList) {
				batch++;
				updateOne(dynaBean);
				if (batch == 5000) {
					sqlMapClient.executeBatch();
					batch = 0;
				}
			}
			sqlMapClient.executeBatch();
			return beanList.size();
		} catch (SQLException ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
		return 0;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 批量更新.
	 * @param beanList
	 * @return 插入条数
	 */
	public long updateOne(DynaBean dynaBean, DynaBean tableDef) {
		try {
			SQLAdapter sqlAdapter = new SQLAdapter();
			sqlAdapter.setSql(SqlBuilder.getUpdateSql(dynaBean, false, false, tableDef));
			sqlMapClient.update(SyConstant.UPDATE, sqlAdapter);
			cached(2, dynaBean);// 1:插入;2:修改;3:删除
			return 1;
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return 0;
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 批量插入.
	 * @param beanList
	 * @return 插入条数
	 */
	public long updateOne(DynaBean dynaBean) {
		return updateOne(dynaBean, getTableDef(dynaBean.getStr(BeanUtils.KEY_TABLE_CODE)));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 删除.
	 * @param dynaBean
	 * @param tableDef
	 * @return 删除条数
	 */
	public long delete(DynaBean dynaBean) {
		return delete(dynaBean, getTableDef(dynaBean.getStr(BeanUtils.KEY_TABLE_CODE)));
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月4日 上午11:35:35 方法说明: 删除.
	 * @param dynaBean
	 * @param tableDef
	 * @return 删除条数
	 */
	public long delete(DynaBean dynaBean, DynaBean tableDef) {
		try {
			SQLAdapter sqlAdapter = new SQLAdapter();
			sqlAdapter.setSql(SqlBuilder.getDeleteSql(dynaBean, false, false, tableDef));
			sqlMapClient.delete(SyConstant.DELETE, sqlAdapter);
			cached(3, dynaBean);// 1:插入;2:修改;3:删除
			return 1;
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return 0;
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 根据表名获取表字段信息
	 * 
	 * @param tableName
	 * @return
	 */
	public DynaBean getTableDef(String tableName) {
		String tableCode = tableName.toUpperCase();
		Connection conn = null;
		try {
			Object dy = CacheUtil.getTypeCache(getMemCached(), CacheUtil.SYSTABLEDEF, tableCode);
			if (null == dy) {
				conn = sqlMapClient.getDataSource().getConnection();
				dy = SqlBuilder.getTableDef(tableCode, conn);
				CacheUtil.setTypeCache(getMemCached(), CacheUtil.SYSTABLEDEF, tableCode, dy);
			}
			return (DynaBean) dy;
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
		return null;
	}
}

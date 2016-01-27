package com.archie.service;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.archie.dao.BaseDao;
import com.archie.model.DynaBean;
import com.archie.service.system.SysServer;
import com.archie.util.BeanUtils;
import com.archie.util.CacheUtil;

/**
 * 
 * @author panlihai E-mail:panlihai@zlwh.com.cn
 * @version 创建时间：2015年12月30日 下午12:43:42 类说明:
 */
public class BaseListener implements ServletContextListener {
	private static org.apache.log4j.Logger logger = Logger.getLogger(BaseListener.class);
	// 主业务处理类
	private BaseDao dao;
	// 获取spring注入的bean对象
	private WebApplicationContext springContext;

	/**
	 * 系统服务注销   
	 */
	@SuppressWarnings("deprecation")
	public void serverDestroyed() {
		dao.getMemCached().shutdown();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月30日 下午1:11:03 方法说明:
	 * @param
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
		if (springContext != null) {
			dao = (BaseDao) springContext.getBean("baseDao");
			final SysServer server = SysServer.getInstanse();
			server.setSpringContext(springContext);
			new Thread() {
				public void run() {
					while (true) {
						DynaBean obj = (DynaBean) server.getQueue().poll();
						if (obj == null) {
							try {
								sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}else{
							dao.insertOne(obj,dao.getTableDef(obj.getStr(BeanUtils.KEY_TABLE_CODE)));
						}

					}
				}
			}.start();
			// 初始化系统
			CacheUtil.serverInitialized(dao);
			logger.info("Loaded cached finished!");
			// 加载器初始化
			////// 加载业务类实体到spring容器
			Class<?> bzClass;
			DefaultListableBeanFactory acf = (DefaultListableBeanFactory) springContext.getAutowireCapableBeanFactory();
			// 获取所有的应用程序对象
			List<DynaBean> appBeanList = dao.findWithQuery(new DynaBean("SYS_APP", "and ENABLE='Y'"));
			for (DynaBean appBean : appBeanList) {
				try {
					String clsName = appBean.getStr("SERVICECLASS", "");
					if (clsName.length() == 0) {
						continue;
					}
					bzClass = Class.forName(clsName);
					if (bzClass != null) {
						AbstractBeanDefinition abstractBeanDefinition = BeanDefinitionBuilder
								.rootBeanDefinition(bzClass).getBeanDefinition();
						abstractBeanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE); // SCOPE_PROTOTYPE
						((BeanDefinitionRegistry) acf).registerBeanDefinition(appBean.getStr("APPID"),
								abstractBeanDefinition);
					}
				} catch (BeanDefinitionStoreException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			logger.info("ServiceListener loaded finished!");
		} else {
			contextDestroyed(event);
		}
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月30日 下午1:11:03 方法说明:
	 * @param
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		serverDestroyed();
	}
}
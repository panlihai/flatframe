package com.archie.service.system;

import com.archie.dao.BaseDao;
import com.archie.model.DynaBean;

/**
 * 
 * @author panlihai E-mail:panlihai@zlwh.com.cn
 * @version 创建时间：2016年1月14日 下午5:05:13 类说明:
 * 
 * @author Administrator
 *
 */
public class SysappService implements ServiceListener {
	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月14日 下午5:10:22 方法说明:
	 * @param response
	 * @param request
	 * @param paramBean
	 *            参数列表
	 */
	@Override	
	public DynaBean execute(BaseDao baseDao,DynaBean paramBean) {
		return new DynaBean();
	}
	

}

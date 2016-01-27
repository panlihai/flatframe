package com.archie.service.system;

import com.archie.dao.BaseDao;
import com.archie.model.DynaBean;

/** 
* @author panlihai E-mail:panlihai@zlwh.com.cn 
* @version 创建时间：2016年1月14日 下午5:06:32 
* 类说明: 
*/
/**
 * @author Administrator
 *
 */
public interface ServiceListener {	
	DynaBean execute(BaseDao dao,DynaBean paramBean);
}

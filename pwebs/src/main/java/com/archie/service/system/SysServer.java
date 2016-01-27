package com.archie.service.system;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.web.context.WebApplicationContext;

import com.archie.model.DynaBean;

/** 
* @author panlihai E-mail:panlihai@zlwh.com.cn 
* @version 创建时间：2016年1月14日 下午6:03:22 
* 类说明: 
*/
/**
 * @author Administrator
 *
 */
public class SysServer {
	private static SysServer server = new SysServer();
	final int FILE_QUEUE_SIZE = 1000;// 阻塞队列大小  
    // 基于ArrayBlockingQueue的阻塞队列  
	ArrayBlockingQueue<DynaBean> queue = new ArrayBlockingQueue<DynaBean>(  
            FILE_QUEUE_SIZE);  

	public static SysServer getInstanse() {
		return server;
	}

	private SysServer() {

	}

	public static SysServer getServer() {
		return server;
	}

	public Object getBean(String beanId) {
		return this.springContext.getBean(beanId);
	}

	/**
	 * @return the springContext
	 */
	public WebApplicationContext getSpringContext() {
		return springContext;
	}

	/**
	 * @param springContext
	 *            the springContext to set
	 */
	public void setSpringContext(WebApplicationContext springContext) {
		this.springContext = springContext;
	}

	/**
	 * @return the logQueue
	 */
	public Queue getQueue() {
		return queue;
	}
	/**
	 * @return the logQueue
	 */
	public void wrileLog(DynaBean logBean) {
		queue.offer(logBean);
	}

	/**
	 * @param logQueue the logQueue to set
	 */
	public void setQueue(ArrayBlockingQueue queue) {
		this.queue = queue;
	}

	// 获取spring注入的bean对象
	private WebApplicationContext springContext;

}

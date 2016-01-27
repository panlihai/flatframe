package com.archie.controllor;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.archie.model.DynaBean;
import com.archie.model.user.UserInfo;
import com.archie.service.BaseService;
import com.archie.service.system.SysServer;
import com.archie.util.ApiUtil;
import com.archie.util.BeanUtils;
import com.archie.util.LogUtil;
import com.archie.util.PageUtil;
import com.archie.util.SyConstant;

import javacommon.util.JsonUtils;
import net.sf.json.JSONObject;

/**
 * @author panlihai E-mail:panlihai@zlwh.com.cn
 * @version 创建时间：2015年12月4日 上午10:54:45 类说明: 对平台处理操作
 */
@RestController()
public class MainController {
	@Resource(name = "baseService")
	private BaseService baseService;
	private static org.apache.log4j.Logger logger = Logger.getLogger(MainController.class);

	/**
	 * 对APPID进行操作显示 get请求只作显示操作
	 * 
	 * @param appId
	 * @param action
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/view/{pId}/{appId}/{action}", method = RequestMethod.GET)
	public ModelAndView view(@PathVariable String pId, @PathVariable String appId, @PathVariable String action,
			HttpServletRequest request) throws ParseException {
		ModelAndView view = null;
		HttpSession session = request.getSession();
		UserInfo ub = (UserInfo) session.getAttribute("adloginInfo");
		try {
			if (ub == null) {
				// view = new ModelAndView("/twoad/adloginorRegister");
				// } else {
				DynaBean paramBean = BeanUtils.requestToDynaBean(request);
				paramBean.set(PageUtil.PAGE_ACTION, action);
				paramBean.set(PageUtil.PAGE_APPID, appId);
				paramBean.set(PageUtil.PAGE_PID, pId);
				switch (action) {
				/** $ACTION$：显示列表查看页面 */
				case SyConstant.ACT_LIST_VIEW:
					return new ModelAndView("/view");
				/** $ACTION$：显示添加页面 */
				case SyConstant.ACT_CARD_ADD:
					return new ModelAndView("/view");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			view = new ModelAndView("/exception");
		}
		return view;
	}

	/**
	 * 对APPID进行操作显示 get请求只作显示操作
	 * 
	 * @param appId
	 * @param action
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/{pId}/{appId}/{version}/{action}")
	public void impl(@PathVariable String pId, @PathVariable String appId, @PathVariable String action,
			@PathVariable String version, HttpServletRequest request, HttpServletResponse response)
					throws ParseException {
		// 返回数据对象
		DynaBean result = new DynaBean();
		try {
			// 获取参数列表信息
			DynaBean paramBean = BeanUtils.requestToDynaBean(request);
			// 获取操作参数
			paramBean.set(ApiUtil.ACT, action);
			// 获取产品id
			paramBean.set(ApiUtil.PID, pId);
			// 获取应用id
			paramBean.set(ApiUtil.AID, appId);
			// 获取产品ID
			paramBean.set(ApiUtil.VERSION, version);
			// 获取请求客户端IP
			paramBean.set(LogUtil.HOST, request.getRemoteAddr());
			// 记录请求日志
			DynaBean log = LogUtil.logRequest(paramBean);
			// 获取返回结果
			result = ApiUtil.doImpl(paramBean);
			// 记录APPID
			log.setStr("APPID", paramBean.getStr("APPID"));
			// 记录响应日志
			LogUtil.logResponse(log, result);
			// 操作原样返回
			result.set(ApiUtil.ACT, action);
			// 返回时间戳
			result.set(ApiUtil.TIMESTAMP, new Date().getTime());
		} catch (Exception e) {
			e.printStackTrace();
			// 错误详情
			result.set(ApiUtil.CODE, "-1");
			// 错误消息体
			result.set(ApiUtil.MSG, e.getMessage());
		}
		try {
			// UTF-8编码
			response.setCharacterEncoding("UTF-8");
			// 把结果写回响应中
			response.getWriter().write(JsonUtils.toJson(result.getValues()));
			logger.info(result.toJsonString());
			// 刷新
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对APPCODE进行操作get请求只作功能操作
	 * 
	 * @param appCode
	 * @param action
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/ajax/{pId}/{menuId}/{appId}/{action}", method = RequestMethod.GET)
	@ResponseBody
	public void getMenuAjax(@PathVariable String pId, @PathVariable String menuId, @PathVariable String appId,
			@PathVariable String action, HttpServletRequest request, HttpServletResponse response)
					throws ParseException {
		HttpSession session = request.getSession();
		UserInfo ub = (UserInfo) session.getAttribute("adloginInfo");
		String json = "";
		try {
			if (ub == null) {
				// json = "{}";
				// } else {
				DynaBean paramBean = BeanUtils.requestToDynaBean(request);
				paramBean.set(PageUtil.PAGE_ACTION, action);
				paramBean.set(PageUtil.PAGE_APPID, appId);
				paramBean.set(PageUtil.PAGE_MENUID, menuId.equals("TOP") ? "" : menuId);
				paramBean.set(PageUtil.PAGE_PID, pId);
				switch (action) {
				case SyConstant.ACT_VIEW_MENUS:
					json = JsonUtils.toJson(showMenus(paramBean, ub).getModel());
					break;
				/** ACT_DATA_MENUS 显示所有的数据 */
				case SyConstant.ACT_VIEW_ONE:
					json = JsonUtils.toJson(viewOne(paramBean, ub).getModel());
					break;
				/** $ACTION$：显示列表查看页面 */
				case SyConstant.ACT_LIST_VIEW:
					json = JsonUtils.toJson(listView(paramBean).getModel());
					break;
				/** $ACTION$：显示列表JSON数据 */
				case SyConstant.ACT_DATA_JSON:
					// 根据应用程序获得json
					json = baseService.listJsonFromAppid(appId, pId);
					break;
				/** $ACTION$：获取静态数据字典列表列表JSON数据 */
				case SyConstant.ACT_DATA_JSON_VALUE:
					json = baseService.listJsonValueByDicId(appId);
					break;
				/** $ACTION$：显示添加页面 */
				case SyConstant.ACT_CARD_ADD:
					/** $ACTION$：显示列表编辑页面 */
				case SyConstant.ACT_LIST_EDIT:
				case SyConstant.ACT_LIST_ADD:
					json = JsonUtils.toJson(cardAdd(paramBean).getModel());
					break;
				/** $ACTION$：卡片保存 */
				case SyConstant.ACT_CARD_SAVE:
					json = JsonUtils.toJson(cardSave(paramBean).getModel());
					break;
				/** $ACTION$：删除 */
				case SyConstant.ACT_DELETE:
					json = JsonUtils.toJson(listDelete(paramBean).getModel());
					break;
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			logger.info(json);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除列表数据内容 根据ID
	 * 
	 * @param paramBean
	 * @return
	 */
	private ModelAndView listDelete(DynaBean paramBean) {
		DynaBean appBean = baseService.findSysAppByCode(paramBean.getStr(PageUtil.PAGE_APPID),
				paramBean.getStr(PageUtil.PAGE_PID));
		ModelAndView view = new ModelAndView("/view");
		// 如果为空则跳转到主页
		if (appBean != null) {
			// 解析数据
			String json = paramBean.getStr(PageUtil.PAGE_JSON);
			// 获取到对象内容
			JSONObject obj = JSONObject.fromObject(json).getJSONObject(appBean.getStr("APPID"));
			// 动态sql条件
			DynaBean dynaBean = new DynaBean(appBean.getStr("MAINTABLE"),
					" and ID in (" + obj.get(PageUtil.PAGE_IDS).toString() + ")");
			view.addObject(PageUtil.PAGE_STATUS, baseService.delete(dynaBean));
		} else {
			view.addObject(PageUtil.PAGE_STATUS, "-1");
		}
		return view;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月16日 上午11:14:25 方法说明: 获取当前用户有权限的菜单
	 * @param ub2
	 * @param
	 */
	private ModelAndView showMenus(DynaBean paramBean, UserInfo userInfo) {
		Object obj = SysServer.getServer().getBean("baseDao");
		ModelAndView view = new ModelAndView("/view");
		// 根据APPCODE获取APP对象
		DynaBean appBean = baseService.findSysAppByCode(paramBean.getStr(PageUtil.PAGE_APPID),
				paramBean.getStr(PageUtil.PAGE_PID));
		if (appBean == null) {
			return view;
		}
		// 获取这个产品的此模块的所有子菜单
		view.addObject(PageUtil.PAGE_MENUS, baseService.selectMenusByParentMenus(
				paramBean.getStr(PageUtil.PAGE_PID, ""), paramBean.getStr(PageUtil.PAGE_MENUID, ""), userInfo));
		return view;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月24日 下午1:17:08 方法说明:卡片保存
	 * @param
	 */

	private ModelAndView cardSave(DynaBean paramBean) {
		DynaBean appBean = baseService.findSysAppByCode(paramBean.getStr(PageUtil.PAGE_APPID),
				paramBean.getStr(PageUtil.PAGE_PID));
		ModelAndView view = new ModelAndView("/view");
		// 解析数据
		String json = paramBean.getStr(PageUtil.PAGE_JSON);
		// 获取到对象内容
		JSONObject obj = JSONObject.fromObject(json).getJSONObject(appBean.getStr("APPID"));
		// 设置各个内容值
		DynaBean saveBean = BeanUtils.jsonToDynaBean(obj);
		// 设置表名称
		saveBean.setStr(BeanUtils.KEY_TABLE_CODE, appBean.getStr("MAINTABLE", ""));
		// 给默认的设置内容,不覆盖内容
		BeanUtils.setDefaultValueByAppFieldSetting(appBean, saveBean, false);
		// 如果没有APPID则返回
		if (saveBean.getStr("ID", "").length() == 0) {
			// 应该从传入的该菜单的PID中获取
			// 获取菜单的对象
			DynaBean menuBean = baseService.findSysMenuByMenuid(paramBean.getStr(PageUtil.PAGE_MENUID));
			saveBean.set("PID", menuBean.getStr(PageUtil.PAGE_PID));
			baseService.insertOne(saveBean);
		} else {
			saveBean.set(BeanUtils.KEY_WHERE, " and ID='" + saveBean.getStr("ID") + "'");
			baseService.updateOne(saveBean);
		}
		// APP结构写入页面
		view.addObject(PageUtil.PAGE_SYSAPP, appBean);
		// // APP字段结构写入页面
		// view.addObject(PageUtil.PAGE_APPFIELDS,
		// baseService.findFieldsFromAppid(appId));
		// // APP按钮及权限写入页面 LIST为列表按钮
		// view.addObject(PageUtil.PAGE_APPBUTTONS,baseService.findButtonsFromAppid(appId));
		// 查询结果写入页面
		view.addObject(PageUtil.PAGE_CARDVALUE, saveBean);
		return view;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月10日 下午2:30:25 方法说明:
	 * @param mMap
	 * @param
	 */
	private ModelAndView listView(DynaBean paramBean) {
		ModelAndView view = new ModelAndView("/view");
		// 根据APPCODE获取APP对象
		DynaBean appBean = baseService.findSysAppByCode(paramBean.getStr(PageUtil.PAGE_APPID),
				paramBean.getStr(PageUtil.PAGE_PID));
		if (appBean == null) {
			return view;
		}
		// 构建查询条件bean
		DynaBean queryBean = new DynaBean(appBean.getStr("MAINTABLE", ""));
		String appId = appBean.getStr("APPID");
		// APP的查询条件如不为空
		String sqlWhere = appBean.getStr("APPFILTER", "");
		// 判断是否有页面查询条件
		if (paramBean.getStr(PageUtil.PAGE_LISTFILTER, "").length() != 0) {
			sqlWhere += PageUtil.getPageFilterSql(appBean,paramBean.getStr(PageUtil.PAGE_LISTFILTER));
		}
		// 获取菜单的对象
		DynaBean menuBean = baseService.findSysMenuByMenuid(paramBean.getStr(PageUtil.PAGE_MENUID));
		// 判断是否有菜单过滤查询条件
		if (menuBean.getStr("APPFILTER", "").length() != 0) {
			sqlWhere +=" "+  menuBean.getStr("APPFILTER");
		}
		// 如果是关联应用过来的则有MAINAPP参数内容
		if (paramBean.getStr("MAINAPP", "").length() != 0 && paramBean.getStr("MAINAPPID", "").length() != 0) {
			// 根据主应用,主应用id获取子关联应用,并把主数据的参数内容写入条件
			sqlWhere += baseService.getSqlWhereBy(paramBean.getStr("MAINAPP"), paramBean.getStr("MAINAPPID"), appId,
					paramBean.getStr(PageUtil.PAGE_PID));
		}
		String filter = paramBean.getStr(PageUtil.PAGE_APPFILTER, "");
		// 页面过滤条件 需要对产品进行过滤
		if (filter.length() > 0) {
			// 转义及体会产品ID
			filter = filter.replaceAll(":‘", "'").replaceAll(":\\{PID}", paramBean.getStr(PageUtil.PAGE_PID));
			sqlWhere += filter;
		}
		// 设置查询条件
		if (sqlWhere.length() > 0) {
			queryBean.setStr(BeanUtils.KEY_WHERE, sqlWhere);
		}

		// 设置当前页数
		queryBean.setInt(BeanUtils.KEY_PAGE_COUNT, paramBean.getInt(BeanUtils.KEY_PAGE_COUNT, 0));
		// 把APP配置表中获取当前app的页记录数,如果没有则默认为每页20条记录
		queryBean.setInt(BeanUtils.KEY_PAGE_SIZE, appBean.getInt("PAGESIZE", 15));
		queryBean.setStr(BeanUtils.KEY_ORDER, appBean.getStr("SORTBY", "ID"));
		// 按条件查询结果
		List resultList = baseService.findWithQuery(queryBean);
		// 查询结果写入页面
		view.addObject(PageUtil.PAGE_LISTVALUE, resultList);
		// 总记录数
		view.addObject(PageUtil.PAGE_ALLCOUNT, baseService.findCountWithQuery(queryBean));
		// 设置当前页数
		view.addObject(PageUtil.PAGE_COUNT, paramBean.getInt(PageUtil.PAGE_COUNT, 0));
		// 把APP配置表中获取当前app的页记录数,如果没有则默认为每页20条记录
		view.addObject(PageUtil.PAGE_SIZE, appBean.getInt("PAGESIZE", 15));
		// APP结构写入页面
		view.addObject(PageUtil.PAGE_SYSAPP, appBean);
		return view;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月10日 下午2:30:25 方法说明:列表点击添加,双击列表,点击修改后进入
	 * @param mMap
	 * @param
	 */
	private ModelAndView cardAdd(DynaBean paramBean) {
		ModelAndView view = new ModelAndView("/view");
		// 根据APPCODE获取APP对象
		DynaBean appBean = baseService.findSysAppByCode(paramBean.getStr(PageUtil.PAGE_APPID),
				paramBean.getStr(PageUtil.PAGE_PID));
		// 如果为空则跳转到主页
		if (appBean != null) {
			String appId = appBean.getStr("APPID");
			// APP结构写入页面
			view.addObject(PageUtil.PAGE_SYSAPP, appBean);
			DynaBean main = new DynaBean();
			// APP的对象内容写入页面 如果是修改
			if (paramBean.getStr("ID", "").length() != 0) {
				view.addObject(PageUtil.PAGE_CARDVALUE,
						baseService.findOneWithQuery(appBean.getStr("MAINTABLE"), paramBean.getStr("ID", "")));
			} else if (paramBean.getStr("MAINAPP", "").length() != 0
					&& paramBean.getStr("MAINAPPID", "").length() != 0) {
				// 初始化值 如果是关联应用过来的则有MAINAPP参数内容
				main = baseService.getDefaultFieldValueByLinkFilter(paramBean.getStr("MAINAPP"),
						paramBean.getStr("MAINAPPID"), appId, paramBean.getStr(PageUtil.PAGE_PID));
				view.addObject(PageUtil.PAGE_CARDVALUE, main);
			}
			// 设置默认值,如果参数为false 则不覆盖内容, true则覆盖生成.
			BeanUtils.setDefaultValueByAppFieldSetting(appBean, main, true);
		}
		return view;
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2015年12月10日 下午2:30:25 方法说明:列表点击添加,双击列表,点击修改后进入
	 * @param mMap
	 * @param
	 */
	private ModelAndView viewOne(DynaBean paramBean, UserInfo userinfo) {
		ModelAndView view = new ModelAndView("/view");
		// 根据APPCODE获取APP对象
		DynaBean appBean = baseService.findSysAppByCode(paramBean.getStr(PageUtil.PAGE_APPID),
				paramBean.getStr(PageUtil.PAGE_PID));
		// 如果为空则跳转到主页
		if (appBean != null) {
			String appId = appBean.getStr("APPID");
			// APP结构写入页面
			view.addObject(PageUtil.PAGE_SYSAPP, appBean);
			if (paramBean.getStr("ID", "").length() != 0) {
				view.addObject(PageUtil.PAGE_CARDVALUE,
						baseService.findOneWithQuery(appBean.getStr("MAINTABLE"), paramBean.getStr("ID", "")));
			} else if (paramBean.getStr("MAINAPP", "").length() != 0
					&& paramBean.getStr("MAINAPPID", "").length() != 0) {
				// 初始化值 如果是关联应用过来的则有MAINAPP参数内容
				DynaBean main = baseService.getDefaultFieldValueByLinkFilter(paramBean.getStr("MAINAPP"),
						paramBean.getStr("MAINAPPID"), appId, paramBean.getStr(PageUtil.PAGE_PID));
				view.addObject(PageUtil.PAGE_CARDVALUE, main);
			}
		}
		return view;
	}

	public void clearCache() {
		baseService.clearCache();
	}

	public void initSystem() {
		baseService.initSystem();
	}
}
package com.archie.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.archie.model.DynaBean;
import com.archie.service.system.SysServer;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/** 
* @author panlihai E-mail:panlihai@zlwh.com.cn 
* @version 创建时间：2016年1月8日 上午10:43:15 
* 类说明: 
*/
/**
 * @author Administrator
 *
 */
public class LogUtil {
	public final static String PROVINCE = "PROVINCE";
	public final static String CITY = "CITY";
	public final static String HOST = "CLIENTHOST";
	public final static String LAT = "LAT";
	public final static String LNG = "LNG";
	public final static String LOGTIME = "LOGTIME";

	/**
	 * 接口详情日志
	 * 
	 * @param request
	 * @param log
	 */
	public static DynaBean logRequest(DynaBean paramBean) {
		DynaBean logBean = new DynaBean("SYS_LOG");
		logBean.setStr(HOST, paramBean.getStr("CLIENTIP"));
		logBean.setStr(LOGTIME, DateUtils.getDatetime());
		logBean.setStr("PID", paramBean.getStr(ApiUtil.PID));
		logBean.setStr("ACTTYPE", paramBean.getStr(ApiUtil.ACT));
		logBean.setStr("LOGTYPE", "REQUEST");
		logBean.setStr("LOGID", SyConstant.getUUID());
		logBean.setStr("USERTOKEN", paramBean.getStr("USERTOKEN"));
		logBean.setStr("CONTENT", paramBean.toJsonString().replaceAll("\"", ""));
		logBean.setStr(LAT, paramBean.getStr(LAT));
		logBean.setStr(LNG, paramBean.getStr(LNG));
		return logBean;
	}

	/**
	 * 接口写日志
	 * 
	 * @param request
	 * @param log
	 */
	public static void logAppDetail(final DynaBean paramBean,final String appId,final String tableName) {
		new Thread() {
			public void run() {
				DynaBean logBean = new DynaBean("SYS_LOG_"+tableName);
				if (paramBean.getStr(LAT, "").length() == 0 && paramBean.getStr(LNG, "").length() == 0) {
					Map<String, String> map = getLngLatByHostIp(paramBean.getStr(HOST));
					logBean.setStr(LAT, map.get(LAT).toString());
					logBean.setStr(LNG, map.get(LNG).toString());
					logBean.setStr(PROVINCE, map.get(PROVINCE).toString());
					logBean.setStr(CITY, map.get(CITY).toString());
				} else {
					Map<String, String> map = getProvinceCityByLNGLAT(paramBean.getStr(LNG), paramBean.getStr(LAT));
					logBean.setStr(LAT, paramBean.getStr(LAT));
					logBean.setStr(LNG, paramBean.getStr(LNG));
					logBean.setStr(PROVINCE, map.get(PROVINCE).toString());
					logBean.setStr(CITY, map.get(CITY).toString());
				}
				logBean.setStr("APPID", appId);
				logBean.setStr(HOST, paramBean.getStr(HOST));
				logBean.setStr(LOGTIME, DateUtils.getDatetime());
				logBean.setStr("PID", paramBean.getStr(ApiUtil.PID));
				logBean.setStr("ACTTYPE", paramBean.getStr(ApiUtil.ACT));
				logBean.setStr("LOGTYPE", "REQUEST");
				logBean.setStr("LOGID", SyConstant.getUUID());
				logBean.setStr("USERTOKEN", paramBean.getStr("USERTOKEN"));
				logBean.setStr("CONTENT", paramBean.toJsonString().replaceAll("\"", ""));
				writeLog(logBean);
			}
		}.start();
	}

	/**
	 * 接口写日志
	 * 
	 * @param request
	 * @param log
	 */
	public static void logResponse(final DynaBean parentLog, final DynaBean log) {
		new Thread() {
			public void run() {
				if (parentLog.getStr(LAT, "").length() == 0 && parentLog.getStr(LNG, "").length() == 0) {
					Map<String, String> map = getLngLatByHostIp(parentLog.getStr(HOST));
					parentLog.setStr(LAT, map.get(LAT).toString());
					parentLog.setStr(LNG, map.get(LNG).toString());
					parentLog.setStr(PROVINCE, map.get(PROVINCE).toString());
					parentLog.setStr(CITY, map.get(CITY).toString());
				} else {
					Map<String, String> map = getProvinceCityByLNGLAT(parentLog.getStr(LNG), parentLog.getStr(LAT));
					parentLog.setStr(LAT, parentLog.getStr(LAT));
					parentLog.setStr(LNG, parentLog.getStr(LNG));
					parentLog.setStr(PROVINCE, map.get(PROVINCE).toString());
					parentLog.setStr(CITY, map.get(CITY).toString());
				}
				// 加入队列
				writeLog(parentLog);
				DynaBean logBean = new DynaBean("SYS_LOG");
				logBean.setStr(HOST, parentLog.getStr(HOST));
				logBean.setStr(LOGTIME, DateUtils.getDatetime());
				logBean.setStr("PID", parentLog.getStr("PID"));
				logBean.setStr("ACTTYPE", parentLog.getStr("ACTTYPE"));
				logBean.setStr("APPID", parentLog.getStr("APPID"));
				logBean.setStr("LOGID", SyConstant.getUUID());
				logBean.setStr("USERTOKEN", parentLog.getStr("USERTOKEN"));
				logBean.setStr("LOGTYPE", "RESPONSE");
				logBean.setStr("CONTENT", log.toJsonString().replaceAll("\"", ""));
				logBean.setStr(LAT, parentLog.getStr(LAT));
				logBean.setStr(LNG, parentLog.getStr(LNG));
				logBean.setStr(PROVINCE, parentLog.getStr(PROVINCE));
				logBean.setStr(CITY, parentLog.getStr(CITY));
				// 加入队列
				writeLog(logBean);
			}
		}.start();
	}

	/**
	 * @author panlihai E-mail:panlihai@zlwh.com.cn
	 * @version 创建时间：2016年1月20日 上午11:05:02 方法说明:
	 * @param dao
	 * @param obj
	 */
	private static void writeLog(DynaBean obj) {
		SysServer.getServer().getQueue().offer(obj);
	}

	/**
	 * 根据ip地址获取经纬度
	 * 
	 * @param ip
	 * @return
	 */
	private static Map<String, String> getLngLatByHostIp(String ip) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			JSONObject json = readJsonFromUrl(
					"http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip=" + ip);
			if (json.containsKey("content")) {
				JSONObject contentJson = json.getJSONObject("content");
				if (contentJson.containsKey("address_detail")) {
					JSONObject addJson = contentJson.getJSONObject("address_detail");
					if (addJson.containsKey("province")) {
						map.put(PROVINCE, addJson.getString("province"));
					}
					if (addJson.containsKey("city")) {
						map.put(CITY, addJson.getString("city"));
					}
				}
				if (contentJson.containsKey("point")) {
					JSONObject point = contentJson.getJSONObject("point");
					if (point.containsKey("y")) {
						map.put(LNG, point.getString("y"));

					}
					if (point.containsKey("x")) {
						map.put(LAT, point.getString("x"));
					}
				}
			} else {
				map.put(LNG, "");
				map.put(LAT, "");
				map.put(PROVINCE, "");
				map.put(CITY, "");
			}

		} catch (JSONException | IOException e) {
			e.printStackTrace();
			map.put(LNG, "");
			map.put(LAT, "");
			map.put(PROVINCE, "");
			map.put(CITY, "");
		}
		return map;
	}

	/**
	 * 根据经纬度获取省份及城市
	 * 
	 * @param ip
	 * @return
	 */
	private static Map getProvinceCityByLNGLAT(String lng, String lat) {
		// String key = "f247cdb592eb43ebac6ccd27f796e2d2";
		// String url = String
		// .format("http://api.map.baidu.com/geocoder?address=%s&output=json&key=%s",
		// address, key);//获取经纬度
		Map<String, String> map = new HashMap<String, String>();
		String str = "http://api.map.baidu.com/geocoder/v2/?ak=F454f8a5efe5e577997931cc01de3974&location=" + lat + ","
				+ lng + "&output=json&pois=1";// 获取地址
		try {
			JSONObject json = readJsonFromUrl(str);
			if (json.containsKey("result")) {
				JSONObject contentJson = json.getJSONObject("result");
				if (contentJson.containsKey("addressComponent")) {
					JSONObject addJson = contentJson.getJSONObject("addressComponent");
					if (addJson.containsKey("province")) {
						map.put(PROVINCE, addJson.getString("province"));
					}
					if (addJson.containsKey("city")) {
						map.put(CITY, addJson.getString("city"));
					}
				}
			} else {
				map.put(PROVINCE, "");
				map.put(CITY, "");
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			map.put(PROVINCE, "");
			map.put(CITY, "");
		}
		return map;
	}

	public static void main(String args[]) {
		// 这里调用百度的ip定位api服务 详见
		// http://api.map.baidu.com/lbsapi/cloud/ip-location-api.htm
		JSONObject json;
		try {
			json = readJsonFromUrl(
					"http://api.map.baidu.com/location/ip?ak=F454f8a5efe5e577997931cc01de3974&ip=202.198.16.3");
			System.out.println(json.toString());
			json = readJsonFromUrl(
					"http://api.map.baidu.com/geocoder/v2/?ak=F454f8a5efe5e577997931cc01de3974&location=39.975369,116.250246&output=json&pois=1");
			System.out.println(json.toString());

			// {"status":0,"result":
			// {"location":
			// {"lng":104.04701,"lat":30.548397},
			// "formatted_address":"四川省成都市武侯区天府四街",
			// "business":"",
			// "addressComponent":
			// {"city":"成都市",
			// "country":"中国",
			// "direction":"",
			// "distance":"",
			// "district":"武侯区",
			// "province":"四川省",
			// "street":"天府四街",
			// "street_number":"",
			// "country_code":0
			// }
			// }
			// }
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = JSONObject.fromObject(jsonText);
			return json;
		} finally {
			is.close();
			// System.out.println("同时 从这里也能看出 即便return了，仍然会执行finally的！");
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

}

package com.archie.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class TelUtil {
	// 验证码发送地址
	private static final String URLSTR = "http://zhiliao.ezhoutongxue.com:7070/webservice/sendMessages/sendInfo.do";
	private static final String SMSURLSTR = "http://sdk.entinfo.cn:8061/webservice.asmx/mdsmssend";
	private static final String SN = "SDK-BBX-010-19809";
	private static final String SMSPWD = "7EFB4E32E0F72348087BF9144FC8585D";
	public static final String TELNUM = "139,138,137,136,135,134,147,188,187,184,183,182,178,159,158,157,152,151,150,186,185,176,145,156,155,132,131,130,189,181,180,177,153,133,189,133";
	public static int testPost(String mobilePhone, int authCode) {

		try {
			URL url = new URL(URLSTR);
			URLConnection con = url.openConnection();
			con.setDoOutput(true);
			con.setRequestProperty("Pragma:", "no-cache");
			con.setRequestProperty("Cache-Control", "no-cache");
			// con.setRequestProperty("Content-Type", "text/xml");

			OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
			String xmlInfo = getXmlInfo(mobilePhone, authCode);
//			System.out.println("urlStr=" + URLSTR);
//			System.out.println("xmlInfo=" + xmlInfo);
			out.write(new String(xmlInfo.getBytes("ISO-8859-1")));
			out.flush();
			out.close();
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = "";
			for (line = br.readLine(); line != null; line = br.readLine()) {
				System.out.println(line);
			}
			return 1;

		} catch (MalformedURLException e) {
			return 0;
		} catch (IOException e) {
			return 0;
		}
	}
	/**
	 * 
	 * @param mobilePhone
	 * @param code
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static boolean sendSMS(String mobilePhone, String code) {
		try {
			String content = ",为您的注册码，请您在一分钟内完成注册。如非本人操作，请忽略。【中联文化】";
			String contentAuthCode = String.valueOf(code);
			String contentResult = java.net.URLEncoder.encode(contentAuthCode + content, "utf-8");
			String param = "sn=" + SN + "&pwd=" + SMSPWD + "&mobile=" + mobilePhone
					+ "&content=" + contentResult + "&ext=1&stime=&rrid=&msgfmt=";
			String result = HttpRequestTool.sendGet(SMSURLSTR, param);
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
			// e.printStackTrace();
		}
	}

	private static String getXmlInfo(String mobilePhone, int authCode) {
		StringBuilder sb = new StringBuilder();
		sb.append("infor=");
		sb.append("<MEG>");
		sb.append("<UNAME>ez1216tx</UNAME>");
		sb.append("<PASSWORD>tx1612ez</PASSWORD>");
		sb.append("<MOBILE>");
		sb.append(mobilePhone);
		sb.append("</MOBILE>");
		sb.append("<CONTENT>");
		sb.append(authCode);
		sb.append("</CONTENT>");
		sb.append("</MEG>");
		return sb.toString();
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		BigDecimal a1 = new BigDecimal("8817010393021010");
		BigDecimal a2 = new BigDecimal("1"); 
//		System.out.println(TelAuthUtil.sendSMS("186118140788",1314));
//		System.out.println(TelAuthUtil.sendSMS("18611140788",1314));
		String code = (int)(Math.random()*10)+""+(int)(Math.random()*10)+(int)(Math.random()*10)+(int)(Math.random()*10);
		System.out.println(code);
	}
}

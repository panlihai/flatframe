/*
 * 版权所有（C）北京神州中联教育科技有限公司 2015
 *
 * http://www.zlwh.com.cn
 *
 * 本程序是神州中联专有产品，神州中联拥有全部产权，仅限于在公司项目或产品中使用。
 */
package com.archie.interceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.archie.service.BaseService;
import com.archie.util.BaseInterceptor;



/**
 * @ClassName: UserAuthInterceptor
 * @Description: 用户自动登录验证拦截器
 */
public class UserAuthInterceptor extends BaseInterceptor implements HandlerInterceptor {
	
	@Resource(name = "baseService")
	private BaseService baseService;
    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object hd) {
        LOG.debug("preHandle start");
        
/*        String userName;
        //此sessionId是用户保存于客户端的识别码，用于用户后续自动访问的自动登录，不是本次访问的sessionid
        String sessionId;
        int seconds=14*24*60*60;
       
        String mobilePhone=request.getParameter("mobilePhone");
		String pwd=request.getParameter("pwd");
		String savetime=request.getParameter("saveTime");
         boolean isAutoLogin;
         UserInfo userInfo=new UserInfo();
         userInfo.setMobilePhone(mobilePhone);
         userInfo.setRegisterCiphertext(mobilePhone+pwd);
         String checkUser=sysUserService.selectUserInfo(userInfo);
         if(checkUser!="none"&&checkUser!="wrong"){
        	 if(savetime!=null&&!savetime.equals("")){
        		 Cookie cookie = new Cookie("user", mobilePhone+"=="+pwd);
                 cookie.setMaxAge(seconds);                                        
                 response.addCookie(cookie);
        	 }
         }*/

        return true;
    }
    
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
            throws Exception {
        
    }
    
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
            throws Exception {
        
    }
    
}

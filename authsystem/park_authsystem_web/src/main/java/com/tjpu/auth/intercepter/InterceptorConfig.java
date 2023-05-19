package com.tjpu.auth.intercepter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
* @author: lwc  
* @date: 2018年3月14日 上午9:07:31
* @Description: 拦截器类，控制方法访问前根据指定条件进行拦截判断
* @updateUser: 
* @updateDate: 
* @updateDescription:
* @version V1.0  
*
 */
public class InterceptorConfig implements HandlerInterceptor{

	
	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
	}

	/**
	 * 
	 * @author: lwc
	 * @date: 2018年3月14日 上午9:08:32
	 * @Description: 调用方法前走拦截器进行指定条件判断 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2) throws Exception {
//		String parameter = arg0.getParameter("a");
		return true;
	}

}

package com.tjpu.sp.config;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tjpu.sp.filter.LoginFilter;



/**
 *
 * @author: lip
 * @date: 2018年4月9日 上午10:12:21
 * @Description:过滤器配置处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
@Configuration
public class FilterConfig {

	/**
	 *
	 * @author: lip
	 * @date: 2018年4月9日 上午10:17:18
	 * @Description: 配置过滤器的相关参数
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @return
	 */
	@Bean
	public FilterRegistrationBean<Filter> someFilterRegistration() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<Filter>();
		registration.setFilter(loginFilter());
		registration.addUrlPatterns("/*");
		registration.addInitParameter("paramName", "paramValue");
		registration.setName("sessionFilter");
		return registration;
	}

	/**
	 *
	 * @author: lip
	 * @date: 2018年4月9日 上午10:17:46
	 * @Description: 创建一个bean
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @return
	 */
	@Bean(name = "loginFilter")
	public Filter loginFilter() {
		return new LoginFilter();
	}
}

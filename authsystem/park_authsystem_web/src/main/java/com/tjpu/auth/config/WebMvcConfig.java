package com.tjpu.auth.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tjpu.pk.common.annotation.RequestJsonHandlerMethodArgumentResolver;
 
 
 

/**
 * 
 * @author: lip
 * @date: 2018年3月13日 下午2:08:46
 * @Description:跨域访问配置
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	/*// 设置跨域访问
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedHeaders("*").allowedOrigins("*").allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE")
		 .allowedMethods("*")
		.allowCredentials(true);

	}*/

	/**
	 * 
	 * @author: lip
	 * @date: 2018年8月2日 上午9:07:23
	 * @Description: 获取请求json中的指定参数
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param argumentResolvers
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new RequestJsonHandlerMethodArgumentResolver());

	}
	 
	
}

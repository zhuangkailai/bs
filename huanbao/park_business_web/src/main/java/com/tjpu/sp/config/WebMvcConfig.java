package com.tjpu.sp.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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
		registry.addMapping("/**").allowedHeaders("*").allowedOrigins("*").allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE").allowCredentials(true);

	}*/

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 文件磁盘url 映射
		// 配置server虚拟路径，handler为前台访问的目录，locations为files相对应的本地路径
		//此处将  /PlaybackData/  映射为右侧的路径
		registry.addResourceHandler("/static/videoPath/**").addResourceLocations("file:D:/videoFile/");
		//若是java:"file:E:\\\\wetemHeadUrlbProject\\FileWebsite\\PlaybackData\\"  (其中\\可以换为/)
		//http://localhost:13389/PlaybackData/434524.txt
		//映射为:/home/javauser/project1/logs/files/flydata/PlaybackData/434524.txt
	}

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

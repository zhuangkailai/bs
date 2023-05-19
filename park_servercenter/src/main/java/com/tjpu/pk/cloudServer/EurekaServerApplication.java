package com.tjpu.pk.cloudServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}


	 /**
	  * 
	  * @author: lip
	  * @date: 2023/1/27 0027 下午 5:21
	  * @Description: 
	  * @updateUser: 
	  * @updateDate: 
	  * @updateDescription:
	  * @param: 
	  * @return: 

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		builder.sources(this.getClass());
		return super.configure(builder);
	}
	  */
}


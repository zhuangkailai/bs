package com.tjpu.sp;

import com.tjpu.pk.common.datasource.DataSourceConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;


/**
 * @version V1.0
 * @author: lip
 * @date: 2018年3月14日 上午9:03:15
 * @Description: springboot 项目启动类 启动项目运行main方法
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@Configuration
@EnableFeignClients
// 开启服务消费者注解
//@EnableDiscoveryClient
@EnableTransactionManagement
@SpringBootApplication
@MapperScan(basePackages = "com.tjpu.sp.dao")
@EnableZuulProxy
@Import({DataSourceConfig.class})
@EnableAsync
public class businessApplication
        /*打包成war包时，操作步骤1.1：打开下面注释*/
        /* extends SpringBootServletInitializer*/ {


    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;


    }
    /**
     * @param builder
     * @return
     * @author: lip
     * @date: 2018年3月19日 下午4:18:25
     * @Description:项目部署到外部容器重写方法
     * @updateUser:
     * @updateDate:
     * @updateDescription: extends SpringBootServletInitializer
     */


    /*打包成war包时，操作步骤1.2：打开下面注释*/
    /*@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        builder.sources(this.getClass());
        return super.configure(builder);
    }*/

    /**
     * @author: lip
     * @date: 2019/3/18 0018 下午 2:03
     * @Description: 程序启动类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static void main(String[] args) {
        SpringApplication.run(businessApplication.class, args);
    }
}

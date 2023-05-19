package com.pub;

import com.tjpu.pk.common.datasource.DataSourceConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ClassUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


@EnableEurekaClient
@EnableTransactionManagement
@SpringBootApplication
@ServletComponentScan(basePackages = "com.pub")
@MapperScan("com.pub.dao")
@Import({DataSourceConfig.class})
public class PublicSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(PublicSystemApplication.class, args);
    }

}

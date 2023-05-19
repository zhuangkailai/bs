package com.tjpu.auth;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.tjpu.pk.common.datasource.DataSourceConfig;

@EnableEurekaClient
@EnableTransactionManagement
@SpringBootApplication
@ServletComponentScan(basePackages = "com.tjpu.auth")
@MapperScan("com.tjpu.auth.dao")
@Import({DataSourceConfig.class})
public class AuthSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthSystemApplication.class);
    }
}

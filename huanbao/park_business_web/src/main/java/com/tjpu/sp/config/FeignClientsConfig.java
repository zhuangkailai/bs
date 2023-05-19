package com.tjpu.sp.config;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;


import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FeignHystrixConcurrencyStrategy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;



import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年7月9日 下午6:17:53
 * @Description:feign配置类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Configuration
@EnableFeignClients(basePackages = "com.tjpu.sp.service.common.micro")
public class FeignClientsConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        try {

            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return;
            }
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    String values = request.getHeader(name);
                    template.header(name, values);
                }
            }
            template.header("encryption", "false");
            Enumeration<String> bodyNames = request.getParameterNames();
            StringBuffer body = new StringBuffer();
            if (bodyNames != null) {
                while (bodyNames.hasMoreElements()) {
                    String name = bodyNames.nextElement();
                    String values = request.getParameter(name);
                    body.append(name).append("=").append(values).append("&");
                }
            }
            if (body.length() != 0) {
                body.deleteCharAt(body.length() - 1);
                template.body(body.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Bean
    public FeignHystrixConcurrencyStrategy feignHystrixConcurrencyStrategy() {
        return new FeignHystrixConcurrencyStrategy();
    }
}

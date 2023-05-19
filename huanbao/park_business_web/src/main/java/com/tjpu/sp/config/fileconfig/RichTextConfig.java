package com.tjpu.sp.config.fileconfig;

import com.tjpu.pk.common.utils.DataFormatUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class RichTextConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String richtextpath = DataFormatUtil.parseProperties("richtextpath");//富文本存储目录
        //富文本磁盘图片url 映射
        //配置server虚拟路径，handler为前台访问的目录，locations为files相对应的本地路径
        registry.addResourceHandler("/static/richtextuploads/**")
                .addResourceLocations("file:" + richtextpath);
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }






}

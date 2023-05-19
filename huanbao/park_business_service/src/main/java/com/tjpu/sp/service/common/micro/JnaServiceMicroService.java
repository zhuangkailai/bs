package com.tjpu.sp.service.common.micro;

import feign.codec.Encoder;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import feign.form.spring.SpringFormEncoder;

import org.springframework.cloud.openfeign.support.SpringEncoder;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年3月19日 下午5:57:29
 * @Description:微服务调用接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
@FeignClient(value = "JnaService", path = "/JnaService", configuration = JnaServiceMicroService.MultipartSupportConfig.class)

public interface JnaServiceMicroService {

    /**
     * @author: lip
     * @date: 2020/3/10 0010 下午 4:38
     * @Description: 发送消息给好友
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: username：好友备注，message：消息内容
     * @return:
     */
    @RequestMapping(value = "weChart/sendUserMessage", method = RequestMethod.POST)
    Object sendUserMessage(@RequestBody JSONObject jsonObject);


    /**
     * @author: lip
     * @date: 2020/3/10 0010 下午 4:38
     * @Description: 发送消息给指定群
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: groupname：群名称，message：消息内容
     * @return:
     */
    @RequestMapping(value = "weChart/sendGroupMessage", method = RequestMethod.POST)
    Object sendGroupMessage(@RequestBody JSONObject jsonObject);

    /**
     * @author: lip
     * @date: 2020/3/10 0010 下午 4:38
     * @Description: 发送文件给指定群
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: groupname：群名称，filedata：文件数据
     * @return:
     */
    @RequestMapping(value = "weChart/sendGroupFileData",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Object sendGroupFileData(@RequestPart("attachment") MultipartFile file,
                             @RequestParam("groupname") String groupname);
    @RequestMapping(value = "tim/sendGroupFileData",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Object sendTXGroupFileData(@RequestPart("attachment") MultipartFile file,
                               @RequestParam("groupname") String groupname);

    @RequestMapping(value = "tim/sendGroupMessage", method = RequestMethod.POST)
    Object sendTXGroupMessage(JSONObject sendMessage);


    class MultipartSupportConfig {
        @Autowired
        private ObjectFactory<HttpMessageConverters> messageConverters;

        @Bean
        public Encoder feignFormEncoder() {
            return new SpringFormEncoder(new SpringEncoder(messageConverters));
        }
    }


}

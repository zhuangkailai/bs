package com.tjpu.sp.controller.common;


import com.tjpu.sp.model.common.ReturnInfo;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年4月13日 下午1:28:47
 * @Description:公共异常处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * @param req
     * @param e
     * @return
     * @throws Exception
     * @author: lip
     * @date: 2018年4月13日 下午2:02:34
     * @Description: RuntimeException异常返回
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ReturnInfo<String> runtimeExceptionHandler(HttpServletRequest req, Exception e) throws Exception {
        ReturnInfo<String> returnInfo = new ReturnInfo<>();
        returnInfo.setFlag(ReturnInfo.fail);
        returnInfo.setErrormessage(e + "");
        return returnInfo;
    }

}

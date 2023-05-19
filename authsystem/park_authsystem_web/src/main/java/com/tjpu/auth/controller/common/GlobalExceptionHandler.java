package com.tjpu.auth.controller.common;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tjpu.auth.model.common.ReturnInfo;

/**
 * 
 * @author: lip
 * @date: 2018年4月13日 下午1:28:47
 * @Description:公共异常处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * 
	 * @author: lip
	 * @date: 2018年4月13日 下午2:02:34
	 * @Description: RuntimeException异常返回
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param req
	 * @param e
	 * @return
	 * @throws Exception
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

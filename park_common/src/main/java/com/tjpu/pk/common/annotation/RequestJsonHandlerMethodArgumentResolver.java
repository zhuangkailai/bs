package com.tjpu.pk.common.annotation;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.tjpu.pk.common.utils.AESUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;


/**
 *
 * @author: lip
 * @date: 2018年8月2日 上午9:27:12
 * @Description:解析请求体中json字符串的类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */

public class RequestJsonHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {


	private static final String JSON_REQUEST_BODY = "JSON_REQUEST_BODY";

	// 判断是否支持要转换的参数类型
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestJson.class);
	}

	// 当支持后进行相应的转换
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		String body = getRequestBody(webRequest);
		Object val = null;
		try {

			body = parseBodyData(body);

			HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
			String url = 	servletRequest.getRequestURI();
			if (body!=null&&!"".equals(body)){
				val = JsonPath.read(body, parameter.getParameterAnnotation(RequestJson.class).value());
				if (parameter.getParameterAnnotation(RequestJson.class).required() && (val == null||"".equals(val))) {
					System.out.println(url+"的参数："+parameter.getParameterAnnotation(RequestJson.class).value() + "不能为空");
					throw new PathNotFoundException("parameter:"+parameter.getParameterName()+" not null or '' ");
				}
			}else if(parameter.getParameterAnnotation(RequestJson.class).required()){
				System.out.println(url+"的参数："+parameter.getParameterAnnotation(RequestJson.class).value() + "不能为空");
				throw new PathNotFoundException("parameter:"+parameter.getParameterName()+" not null or '' ");
			}
		} catch (PathNotFoundException exception) {
			System.out.println(exception.getStackTrace());
			if (parameter.getParameterAnnotation(RequestJson.class).required()) {
				throw exception;
			}
		}
		return val;
	}

	private String parseBodyData(String body) throws Exception {
		if (DataFormatUtil.parseProperties("isEncryption")!=null){
			Boolean isEncryption = Boolean.parseBoolean(DataFormatUtil.parseProperties("isEncryption"));
			if (isEncryption){
				final HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

				String userAgent = request.getHeader("user-agent");
				if (request.getHeader("encryption")!=null||userAgent.contains("Java")){
					//排除微服务调用
				}else {
					String secret = DataFormatUtil.parseProperties("secret");
					body = AESUtil.Decrypt(body,secret);
				}
			}
		}
		return body;
	}
	private String getRequestBody(NativeWebRequest webRequest) {
		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		String jsonBody = (String) servletRequest.getAttribute(JSON_REQUEST_BODY);
		if (jsonBody == null) {
			try {
				jsonBody = IOUtils.toString(servletRequest.getInputStream(),"UTF-8");
				servletRequest.setAttribute(JSON_REQUEST_BODY, jsonBody);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return jsonBody;

	}
}

package com.tjpu.pk.common.utils;

import net.sf.json.JSONObject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * @author: zhangzc
 * @date: 2018/5/25 8:47
 * @Description: session操作工具类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class SessionUtil {


    public static final String Cache_Key = "session_key";

    /**
     *
     * @author: lip
     * @date: 2018年6月8日 下午1:55:22
     * @Description: 从session中获取json对象
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param key
     * @param session
     * @return
     */
    public static JSONObject getCacheJsonInSession(String key, HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        try {
            Object object = session.getAttribute(key);
            jsonObject = JSONObject.fromObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * 获取request
     * @return
     */
    public static HttpServletRequest getRequest(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        return requestAttributes==null? null : requestAttributes.getRequest();
    }

    /**
     * 获取sessionID
     * @return
     */
    public static String getSessionID(){
        return Objects.requireNonNull(getRequest()).getSession().getId();
    }


    /**
     * 获取session
     * @return
     */
    public static HttpSession getSession(){
        return Objects.requireNonNull(getRequest()).getSession(false);
    }
    /**
     * 获取真实路径
     * @return
     */
    public static String getRealRootPath(){
        return Objects.requireNonNull(getRequest()).getServletContext().getRealPath("/");
    }
    /**
     * 获取ip
     * @return
     */
    public static String getIp() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if(servletRequestAttributes!=null){
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getRemoteAddr();
        }
        return null;
    }
    /**
     * 获取session中的Attribute
     * @param name
     * @return
     */
    public static Object getSessionAttribute(String name){
        HttpServletRequest request = getRequest();
        return request == null?null:request.getSession().getAttribute(name);
    }
    /**
     * 设置session的Attribute
     * @param name
     * @param value
     */
    public static void setSessionAttribute(String name,Object value){
        HttpServletRequest request = getRequest();
        if(request!=null){
            request.getSession().setAttribute(name, value);
        }
    }
    /**
     * 获取request中的Attribute
     * @param name
     * @return
     */
    public static Object getRequestAttribute(String name){
        HttpServletRequest request = getRequest();
        return request == null?null:request.getAttribute(name);
    }

    /**
     * 设置request的Attribute
     * @param name
     * @param value
     */
    public static void setRequestAttribute(String name,Object value){
        HttpServletRequest request = getRequest();
        if(request!=null){
            request.setAttribute(name, value);
        }
    }
    /**
     * 获取上下文path
     * @return
     */
    public static String getContextPath() {
        return Objects.requireNonNull(getRequest()).getContextPath();
    }
    /**
     * 删除session中的Attribute
     * @param name
     */
    public static void removeSessionAttribute(String name) {
        Objects.requireNonNull(getRequest()).getSession().removeAttribute(name);
    }

}

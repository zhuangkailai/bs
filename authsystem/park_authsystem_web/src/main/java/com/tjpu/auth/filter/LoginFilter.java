package com.tjpu.auth.filter;

import com.tjpu.auth.common.utils.RedisTemplateUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version V1.0
 * @author: lwc
 * @date: 2018年3月14日 上午9:04:53
 * @Description: 登录过滤器 判断用户登录信息、权限信息
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class LoginFilter implements Filter {

    /**
     * 封装，不需要过滤的list列表
     */
    protected static List<Pattern> patterns = new ArrayList<Pattern>();


    private static final String JSON_REQUEST_BODY = "JSON_REQUEST_BODY";


    /**
     * @param filterConfig
     * @throws ServletException
     * @author: lip
     * @date: 2018年4月9日 上午10:42:47
     * @Description: 初始化方法，添加不需要过滤的资源
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        //设置免过滤
        patterns.add(Pattern.compile("login/loginUser"));
        patterns.add(Pattern.compile("login/loginEntUser"));
        patterns.add(Pattern.compile("login/getOrUpdateCheckCode"));
        patterns.add(Pattern.compile("login/loginUserAndCode"));
        patterns.add(Pattern.compile("auth/getTokenJson"));
        patterns.add(Pattern.compile("socket/getAllClientInfo"));
        patterns.add(Pattern.compile("socket/sendAllClientMessage"));
        patterns.add(Pattern.compile("socket/sendAppointMessageToAppointClient"));
        patterns.add(Pattern.compile("menuController/getLoginUserAppChildrenMenuByMenuId"));
        patterns.add(Pattern.compile("jgpush/sendMessageToAppClient"));
        patterns.add(Pattern.compile("jgpush/sendMessageAndTitleToAppClient"));
        patterns.add(Pattern.compile("jgpush/sendTaskSMSToClient"));
        patterns.add(Pattern.compile("jgpush/sendPassWordSMSToClient"));
        patterns.add(Pattern.compile("jgpush/sendAlarmSMSToClient"));
        patterns.add(Pattern.compile("jgpush/sendOffLineSMSToClient"));
        patterns.add(Pattern.compile("userInfo/getLoginUserMenuByAppId"));
        patterns.add(Pattern.compile("userInfo/getSystemMenuRightByUserId"));
        patterns.add(Pattern.compile("login/getCacheUser"));

    }

    @Override
    public void destroy() {

    }

    /**
     * @param servletRequest
     * @param servletResponse
     * @param chain
     * @throws IOException
     * @throws ServletException
     * @author: lip
     * @date: 2018年4月2日 下午3:38:01
     * @Description:过滤器逻辑处理
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        httpResponse.setHeader("P3P", "CP=CAO PSA OUR");
        try {
            String url = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
            if (url.startsWith("/") && url.length() > 1) {
                url = url.substring(1);
            }
            if (isInclude(url) || validToken(httpRequest)) {
                chain.doFilter(httpRequest, httpResponse);
                return;
            } else {
               /* chain.doFilter(httpRequest, httpResponse);
                return;*/
                httpResponse.sendError(HttpServletResponse.SC_ACCEPTED, "nosession");
            }
        } catch (Exception e) {
            e.printStackTrace();
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "fail");
        }
    }

    private boolean validToken(HttpServletRequest httpRequest) {
        boolean isTrue = false;
        String token = httpRequest.getHeader("token");
        if (StringUtils.isNotBlank(token)){
            JSONObject jsonObject = RedisTemplateUtil.getCache(token, JSONObject.class);
            if (jsonObject!=null) {
                isTrue = true;
                // 1，判断刷新全局会话
                Date lastActionDate = DataFormatUtil.parseDate(jsonObject.getString("lastactiondate"));
                List<Date> timeList = new ArrayList<Date>();
                timeList.add(lastActionDate);
                if (isNeedUpdateRedis(timeList)) {
                    jsonObject.put("lastactiondate", DataFormatUtil.getDateYMDHMS(new Date()));
                    RedisTemplateUtil.putCacheWithExpireTime(token, jsonObject, RedisTemplateUtil.CAHCEDAY);
                }
            }
        }
        return isTrue;

    }

    /**
     * @param timeList
     * @return
     * @author: lip
     * @date: 2018年8月13日 上午10:26:13
     * @Description: 判断时间数组中是否有需要更新
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    private boolean isNeedUpdateRedis(List<Date> timeList) {
        boolean isNeedUpdate = false;
        for (Date date : timeList) {
            if (DataFormatUtil.isNeedUpdate(date, new Date(), 20)) {
                isNeedUpdate = true;
                break;
            }
        }
        return isNeedUpdate;
    }

    /**
     * @param url
     * @return
     * @author: lip
     * @date: 2018年4月10日 上午8:58:37
     * @Description: url是否在过滤列表中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    private boolean isInclude(String url) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }


}
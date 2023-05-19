package com.tjpu.sp.filter;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version V1.0
 * @author: lip
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
        patterns.add(Pattern.compile("file/uploadFile"));
        patterns.add(Pattern.compile("file/uploadFileToDrive"));
        patterns.add(Pattern.compile("file/deleteFileFromDrive"));
        patterns.add(Pattern.compile("appVersion/downloadAPKFileByFileId"));
        patterns.add(Pattern.compile("appVersion/getLastAppVersionInfo"));
        patterns.add(Pattern.compile("file/getFilesByFileIds"));
        patterns.add(Pattern.compile("pollution/getUserButtonAuthBySysmodel"));
        patterns.add(Pattern.compile("rabbitmq/sendTaskDirectQueue"));
        patterns.add(Pattern.compile("pubCode/getPubCodeDataByParam"));
        patterns.add(Pattern.compile("file/downloadFileGet"));
        //消息推送相关接口
        patterns.add(Pattern.compile("onlineMonitor/getOneMonitorCharDataByParamMap"));
        patterns.add(Pattern.compile("pollution/getMonitorPointHourConcentrationDataByParam"));
        patterns.add(Pattern.compile("overAlarmData/getOnePollutantChangeWarnByParams"));
        patterns.add(Pattern.compile("pollutant/getPointAlarmSetByParam"));
        patterns.add(Pattern.compile("onlineWater/getTreatmentPlantDayConcentrationDataByParam"));
        patterns.add(Pattern.compile("onlineWater/getWaterPollutantDayConcentrationDataByParam"));
        patterns.add(Pattern.compile("deviceProblemRecord/getDeviceProblemRecordsByParamMap"));
        patterns.add(Pattern.compile("rabbitmq/test"));

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
            if (isInclude(url) || validToken(httpRequest) || isStaticResource(url)) {
                chain.doFilter(httpRequest, httpResponse);
            } else {
                httpResponse.sendError(HttpServletResponse.SC_ACCEPTED, "nosession");
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
            //httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "fail");
        }
    }

    private boolean isStaticResource(String url) {
        String patternCode = "^static(.*?)";
        Pattern pattern = Pattern.compile(patternCode);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
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

    private boolean validToken(HttpServletRequest httpRequest) {
        boolean isTrue = false;
        String token = httpRequest.getHeader("token");
        if (StringUtils.isNotBlank(token)) {
            if (token.equals(DataFormatUtil.parseProperties("QR.TOKEN"))) {
                isTrue = true;
            }else {
                JSONObject jsonObject = RedisTemplateUtil.getCache(token, JSONObject.class);
                if (jsonObject != null) {
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
        }
        return isTrue;

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

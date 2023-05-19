package com.tjpu.auth.controller.common;

import com.corundumstudio.socketio.SocketIOClient;
import com.tjpu.auth.common.utils.RedisTemplateUtil;
import com.tjpu.auth.model.common.ReturnInfo;
import com.tjpu.auth.model.system.UserInfoVO;
import com.tjpu.auth.service.system.OperateLogService;
import com.tjpu.auth.service.system.UserInfoService;
import com.tjpu.auth.socketIo.MessageEventHandler;
import com.tjpu.auth.socketIo.MessageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import io.swagger.annotations.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;

@RestController
@RequestMapping("login")

public class LoginController extends BaseController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private OperateLogService operateLogService;
    /* 默认数据源 */
    @Value("${spring.datasource.primary.name}")
    private String defaultDataSourceKey;


    private final String login_prefix = "login_";

    private Map<String, String> qrAndUser = new HashMap<>();

    /**
     * @author: lip
     * @date: 2018/9/27 0027 下午 1:50
     * @Description: 用户登陆的方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "loginUser", method = RequestMethod.POST)
    public Object loginUser(HttpServletRequest request, @RequestJson(value = "useraccount") String useraccount,
                            @RequestJson(value = "userpwd") String userpwd) throws Exception {
        try {
            String state = "";
            Map<String, Object> paramData = new HashMap<>();
            paramData.put("sessionId", request.getSession().getId());
            paramData.put("clientIp", RequestUtil.getIpAddress(request));
            paramData.put("agent", RequestUtil.checkAgent(request));
            Map<String, String> dataMap = validAccountAndPwd(useraccount, userpwd, paramData, false);
            if (dataMap.get("rediskey") != null) {//验证用户名和密码
                state = "pass";
            } else {
                state = "nopass";
            }
            return AuthUtil.parseJsonKeyToLower(state, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018/9/27 0027 下午 1:50
     * @Description: 企业用户登陆的方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "loginEntUser", method = RequestMethod.POST)
    public Object loginEntUser(HttpServletRequest request, @RequestJson(value = "useraccount") String useraccount,
                               @RequestJson(value = "userpwd") String userpwd) throws Exception {
        try {
            String state = "";
            Map<String, Object> paramData = new HashMap<>();
            paramData.put("sessionId", request.getSession().getId());
            paramData.put("clientIp", RequestUtil.getIpAddress(request));

            Map<String, String> dataMap = validAccountAndPwd(useraccount, userpwd, paramData, true);
            if (dataMap.get("rediskey") != null) {//验证用户名和密码
                state = "pass";
            } else {
                state = "nopass";
            }
            return AuthUtil.parseJsonKeyToLower(state, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018年6月8日 下午3:16:52
     * @Description: 获取当前登录用户的json
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: userAccount 用户账户
     * @param: userPassword 用户密码
     * @param： request 请求
     * @param： response 响应
     * @return：
     */
    @ApiOperation(value = "获取当前登陆用户信息", notes = "获取当前登陆用户信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errormessage\":\"具体异常信息\"}")})
    @GetMapping(value = "getCacheUser")
    public Object getCacheUser(HttpServletRequest request, @RequestJson(value = "rediskey", required = false) String rediskey) {
        try {
            String sessionId;
            if (request.getHeader("token") != null) {
                sessionId = request.getHeader("token");
            } else {
                sessionId = request.getSession().getId();
            }
            JSONObject jsonObject = RedisTemplateUtil.getCache(sessionId, JSONObject.class);
            Map<String, Object> userCacheMap = new HashMap<String, Object>();
            if (jsonObject != null) {
                userCacheMap.put("userid", jsonObject.get("userid"));
                userCacheMap.put("username", jsonObject.get("username"));
                userCacheMap.put("useraccount", jsonObject.get("useraccount"));
                userCacheMap.put("usercode", jsonObject.get("usercode"));
                userCacheMap.put("regioncode", jsonObject.get("regioncode"));
                userCacheMap.put("theme", jsonObject.get("theme"));
                userCacheMap.put("userroles", jsonObject.get("userroles"));
                //如果是企业用户获取污染源ID
                if (jsonObject.get("usertype") != null && "1".equals(jsonObject.get("usertype"))) {
                    String userId = jsonObject.get("userid").toString();

                    List<Map<String, Object>> entUsers = userInfoService.getEntUserByUserId(userId);
                    if (entUsers != null) {
                        List<String> Ent_Ids = new ArrayList<>();
                        for (Map<String, Object> map : entUsers) {
                            Ent_Ids.add(map.get("Ent_Id").toString());
                        }
                        if (Ent_Ids.size() > 0) {
                            //DynamicDataSourceContextHolderUtil.setDataSourceType(datacenterDataSource);
                            Map<String, Object> paramMap = new HashMap<>();
                            paramMap.put("pollutionlist", Ent_Ids);
                            List<Map<String, Object>> dataList = userInfoService.getPollutionListByPollutionIdList(paramMap);
                            // ###切换回默认数据源
                            //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSourceKey);
                            if (dataList.size() > 0) {
                                userCacheMap.put("pollutionid", dataList.get(0).get("pollutionid"));
                                userCacheMap.put("pollutionname", dataList.get(0).get("PollutionName"));
                            }
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", userCacheMap);
        } catch (Exception e) {
            e.printStackTrace();

            //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSourceKey);
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018年6月8日 下午3:16:52
     * @Description: 根据redisKey获取用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @return：
     */
    @ApiOperation(value = "根据redisKey获取用户信息", notes = "根据redisKey获取用户信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "rediskey", value = "登陆成功后返回的值", defaultValue = "", required = true, dataType = "String")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errormessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getCacheUserByRedisKey", method = RequestMethod.POST)
    public Object getCacheUserByRedisKey(@RequestJson(value = "rediskey") String redisKey) throws Exception {
        try {
            redisKey = AESUtil.Decrypt(redisKey, AESUtil.KEY_Secret);
            JSONObject jsonObject = RedisTemplateUtil.getCache(redisKey, JSONObject.class);
            Map<String, Object> userCacheMap = new HashMap<String, Object>();
            if (jsonObject != null) {
                userCacheMap.put("userid", jsonObject.get("userid"));
                userCacheMap.put("username", jsonObject.get("username"));
                userCacheMap.put("useraccount", jsonObject.get("useraccount"));
                userCacheMap.put("theme", jsonObject.get("theme"));
                userCacheMap.put("lastactiondate", jsonObject.get("lastactiondate"));
                userCacheMap.put("sendpush", jsonObject.get("sendpush"));
                userCacheMap.put("alarmtype", jsonObject.get("alarmtype"));
                userCacheMap.put("regioncode", jsonObject.get("regioncode"));
                userCacheMap.put("alarmtime", jsonObject.get("alarmtime"));
            }
            return AuthUtil.parseJsonKeyToLower("success", userCacheMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018年7月13日 上午9:28:18
     * @Description: 换肤的方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param： request
     * @param： response
     * @param： session
     * @return:
     */
    @ApiOperation(value = "换肤的方法", notes = "换肤的方法（更新到redis,以及数据库记录）")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "theme", value = "皮肤code：black：黑，blue：蓝，purple：紫，green：绿", defaultValue = "", required = true, dataType = "String")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @GetMapping(value = "updateCacheUser")
    public Object updateCacheUser(HttpServletRequest request) {
        try {
            String redisKey = request.getHeader("token");
            String theme = request.getParameter("theme");
            JSONObject jsonObject = RedisTemplateUtil.getCache(redisKey, JSONObject.class);
            jsonObject.put("theme", theme);
            jsonObject.put("lastactiondate", DataFormatUtil.getDateYMDHMS(new Date()));
            RedisTemplateUtil.putCacheWithExpireTime(redisKey, jsonObject, RedisTemplateUtil.CAHCEDAY);

            // 更新数据库中的记录
            String userId = jsonObject.getString("userid");
            if (userId != null && !"".equals(userId)) {
                UserInfoVO userInfo = new UserInfoVO();
                userInfo.setUserId(userId);
                userInfo.setTheme(theme);
                userInfoService.updateByPrimaryKeySelective(userInfo);
                return AuthUtil.parseJsonKeyToLower("success", "");
            } else {
                return AuthUtil.parseJsonKeyToLower("fail", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AuthUtil.parseJsonKeyToLower("fail", null);
        }
    }

    /**
     * @author: zzc
     * @date: 2018/6/26 17:23
     * @Description: 退出
     * @updateUser:lip
     * @updateDate:2018/08/13
     * @updateDescription:退出系统时，清除全局会话，局部会话
     * @updateUser:xsm
     * @updateDate:2018/08/27
     * @updateDescription:退出系统时，记录退出操作的日志
     * @param:
     * @return:
     */
    @ApiOperation(value = "退出系统", notes = "退出系统")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @GetMapping(value = "loginOut")
    public Object loginOut(HttpServletRequest request) {
        try {
            String sessionId;
            if (request.getHeader("token") != null) {
                sessionId = request.getHeader("token");
            } else {
                sessionId = request.getSession().getId();
            }
            String userName = "";
            JSONObject jsonObject = RedisTemplateUtil.getCache(sessionId, JSONObject.class);
            if (jsonObject != null) {
                userName = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                Object subSessionObject = jsonObject.get("subsessionlist");
                if (subSessionObject != null) {
                    List<Map<String, Object>> subSessionList = (List<Map<String, Object>>) subSessionObject;
                    for (Map<String, Object> map : subSessionList) {
                        RedisTemplateUtil.deleteCache(map.get("subrediskey").toString());
                    }
                }
            }
            RedisTemplateUtil.deleteCache(sessionId);
            if (StringUtils.isNotBlank(userName)) {
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("username", userName);
                paramMap.put("baseOperateIp", RequestUtil.getIpAddress(request));
                //添加日志记录
                operateLogService.saveUserOperationLog("exit", paramMap, null, null, null);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018/10/11 0011 下午 6:19
     * @Description: 用户登录时的方法，（验证用户名，密码，验证码）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "用户登录时的方法", notes = "用户登录时的方法（验证用户名，密码，验证码）")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "useraccount", value = "用户账号", defaultValue = "", required = true, dataType = "String"),
            @ApiImplicitParam(name = "userpwd", value = "用户密码加密后的串", defaultValue = "", required = true, dataType = "String"),
            @ApiImplicitParam(name = "checkcode", value = "验证码", defaultValue = "", required = true, dataType = "String")
    })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"), @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "loginUserAndCode", method = RequestMethod.POST)
    public Object loginUserAndCode(HttpServletRequest request,
                                   @RequestJson(value = "useraccount") String useraccount,
                                   @RequestJson(value = "userpwd") String userpwd,
                                   @RequestJson(value = "checkcode") String checkcode) {
        try {
            String type = request.getMethod();
            String state = "";
            Map<String, String> dataMap = new HashMap<>();

            if (!"OPTIONS".equalsIgnoreCase(type)) {
                //验证验证码
                state = validCode(request.getSession(), checkcode);
                if ("rightCode".equals(state)) {
                    String prefix = "checkcode";
                    Map<String, Object> paramData = new HashMap<>();
                    paramData.put("sessionId", request.getSession().getId());
                    paramData.put("clientIp", RequestUtil.getIpAddress(request));
                    dataMap = validAccountAndPwd(useraccount, userpwd, paramData, true);
                    if (dataMap.get("rediskey") != null) {//验证用户名和密码
                        state = "pass";
                    } else {
                        state = "nopass";
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower(state, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            return AuthUtil.parseJsonKeyToLower("nopass", null);
        }
    }

    /**
     * @author: lip
     * @date: 2018/10/11 0011 下午 7:09
     * @Description: 验证用户名和密码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, String> validAccountAndPwd(String useraccount, String userpwd,
                                                   Map<String, Object> paramData,
                                                   boolean flag) throws Exception {

        Map<String, String> dataMap = new HashMap<String, String>();


        Map<String, Object> paramMap = new HashMap<>();

        String pre;
        if (flag) {//企业用户
            paramMap.put("usertype", "1");
            pre = "qy";
        } else {//环保用户
            paramMap.put("usertype", "0");
            pre = "hb";
        }
        String key = login_prefix + useraccount;

        //阻隔登录
        if (isSeparate(dataMap, key)) {
            return dataMap;
        }
        paramMap.put("useraccount", useraccount);
        String effectiveTime = DataFormatUtil.getDateYMDH(new Date());
        paramMap.put("effectivetime", effectiveTime);
        List<UserInfoVO> userInfoVOS = userInfoService.getUserInfoVOsByParam(paramMap);
        if (userInfoVOS.size() == 1) {
            UserInfoVO userInfoVO = userInfoVOS.get(0);
            if (userInfoVO.getUserPwd().equals(userpwd)) {
                //清除阻隔信息
                RedisTemplateUtil.deleteCache(key);
                // 定义map，存放登录用户的信息
                JSONObject jsonObject = new JSONObject();
                String sessionId = paramData.get("sessionId").toString()+pre;
                String clientIp = paramData.get("clientIp") != null ? paramData.get("clientIp").toString() : "";
                jsonObject.put("clientip", clientIp);
                jsonObject.put("userid", userInfoVO.getUserId());
                jsonObject.put("username", userInfoVO.getUserName());
                jsonObject.put("usercode", userInfoVO.getUserCode());
                jsonObject.put("sendpush", userInfoVO.getSendPush());
                jsonObject.put("alarmtype", userInfoVO.getAlarmType());
                jsonObject.put("alarmtime", userInfoVO.getAlarmType());
                jsonObject.put("usertype", userInfoVO.getUserType());
                jsonObject.put("regioncode", userInfoVO.getRegionCode());
                jsonObject.put("useraccount", userInfoVO.getUserAccount());
                jsonObject.put("theme", StringUtils.isNotBlank(userInfoVO.getTheme()) ? userInfoVO.getTheme() : "blue");
                jsonObject.put("logindate", DataFormatUtil.getDateYMDHMS(new Date()));
                jsonObject.put("lastactiondate", DataFormatUtil.getDateYMDHMS(new Date()));
                jsonObject.put("sessionid", sessionId);
                //数据权限
                String userId = userInfoVO.getUserId();
                List<Map<String, Object>> userRoleList = userInfoService.getUserRoleListByUserId(userId);
                jsonObject.put("userroles", userRoleList);
                List<String> list = userInfoService.getDataPermissionsByUserID(userId);
                jsonObject.put("dataauth", list);
                List<String> userDgimn = userInfoService.getUserDgimnListByUserId(userId);
                jsonObject.put("userdgimns", userDgimn);
                List<String> pollutionIdList = userInfoService.getUserPollutionIdListByUserId(userId);
                jsonObject.put("pollutionids", pollutionIdList);
                //获取用户的系统权限和菜单权限
                List<Map<String, Object>> userAuth = userInfoService.getSystemRightByUserId(userId);
                //获取用户的菜单和按钮权限
                jsonObject.put("userauth", userAuth);
                //获取用户的系统权限和菜单权限
                Map<String, List<Map<String, Object>>> userMenuButtonAuth = userInfoService.getUserMenusButtonAuth(userId);
                jsonObject.put("usermenuandbuttonauth", userMenuButtonAuth);
                JSONObject jsonObject2 = AuthUtil.parseObjectKeyToLower(jsonObject);
                // key值：sessionId，value值：登录用户的json对象
                String redisKey = sessionId;
                //将登陆信息放入redis中
                RedisTemplateUtil.putCacheWithExpireTime(redisKey, jsonObject2, RedisTemplateUtil.CAHCEDAY);

                // 用于websocket的key值
                String sessionKey = redisKey + "_" + userInfoVO.getUserAccount() + "_" + userInfoVO.getUserId() + "_" + getUUID();
                // 加密处理
                redisKey = AESUtil.Encrypt(redisKey, AESUtil.KEY_Secret);
                sessionKey = AESUtil.Encrypt(sessionKey, AESUtil.KEY_Secret);
                dataMap.put("rediskey", redisKey);
                dataMap.put("sessionkey", sessionKey);
                //添加日志记录
                paramMap.clear();
                paramMap.put("username", userInfoVO.getUserName());
                paramMap.put("baseOperateIp", clientIp);

                paramMap.put("agent", paramData.get("agent"));
                operateLogService.saveUserOperationLog("login", paramMap, null, null, null);
                return dataMap;
            } else {
                separateLogin(dataMap, key);
            }
        } else {//尝试loginNum次，登录失败阻隔loginTime
            separateLogin(dataMap, key);
        }
        return dataMap;
    }

    private boolean isSeparate(Map<String, String> dataMap, String key) {
        boolean isSeparate = false;
        String loginNum = DataFormatUtil.parseProperties("login.fail.num");
        String loginTime = DataFormatUtil.parseProperties("login.separate.time");
        if (StringUtils.isNotBlank(loginNum) && StringUtils.isNotBlank(loginTime)) {
            JSONObject userLogin = RedisTemplateUtil.getCache(key, JSONObject.class);
            if (userLogin != null) {
                isSeparate = userLogin.getBoolean("isSeparate");
                if (isSeparate) {
                    long second = RedisTemplateUtil.getExpireTime(key);
                    int minute = (int) (second / 60);
                    dataMap.put("separateTime", minute + "");
                    dataMap.put("isSeparate", isSeparate + "");
                    dataMap.put("savenum", "0");
                }
            }
        }
        return isSeparate;

    }

    /**
     * @Description: //尝试loginNum次，登录失败阻隔loginTime
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/21 9:04
     */
    private void separateLogin(Map<String, String> dataMap, String key) {
        String loginNum = DataFormatUtil.parseProperties("login.fail.num");
        String loginTime = DataFormatUtil.parseProperties("login.separate.time");
        String tryTime = DataFormatUtil.parseProperties("login.try.time");
        if (StringUtils.isNotBlank(loginNum) && StringUtils.isNotBlank(loginTime)) {
            JSONObject userLogin = RedisTemplateUtil.getCache(key, JSONObject.class);
            Integer haveNum = 0;
            long second;
            boolean isSeparate;
            int saveNum;
            int totalNum = Integer.parseInt(loginNum);
            if (userLogin != null) {
                haveNum = userLogin.getInt("havenum");
                haveNum++;
                second = RedisTemplateUtil.getExpireTime(key);
                isSeparate = userLogin.getBoolean("isSeparate");
                if (haveNum >= totalNum) {//尝试次数大于配置次数：阻断
                    if (!isSeparate) {//第一次
                        second = Integer.parseInt(loginTime) * 60;
                        isSeparate = true;
                    }
                    int minute = (int) (second / 60);
                    dataMap.put("separateTime", minute + "");
                } else {
                    dataMap.put("separateTime", "");
                }
                saveNum = (totalNum - haveNum) < 0 ? 0 : (totalNum - haveNum);
                dataMap.put("saveNum", saveNum + "");
                dataMap.put("isSeparate", isSeparate + "");
                userLogin.put("havenum", haveNum);
                userLogin.put("isSeparate", isSeparate);
            } else {
                haveNum++;
                dataMap.put("separateTime", "");
                saveNum = (totalNum - haveNum) < 0 ? 0 : (totalNum - haveNum);
                dataMap.put("saveNum", saveNum + "");
                dataMap.put("isSeparate", "false");
                userLogin = new JSONObject();
                userLogin.put("havenum", haveNum);
                userLogin.put("isSeparate", false);
                second = Integer.parseInt(tryTime) * 60;
            }
            RedisTemplateUtil.putCacheWithExpireTime(key, userLogin, second);
        }
    }

    /**
     * @author: lip
     * @date: 2018/10/11 0011 下午 6:25
     * @Description: 获取或刷新验证码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "获取或刷新验证码", notes = "获取或刷新验证码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "imagewidth", value = "验证码图片宽度", defaultValue = "", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "imagehight", value = "验证码图片高度", defaultValue = "", required = true, dataType = "Integer")
    })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "请求响应图片流数据"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getOrUpdateCheckCode", method = RequestMethod.POST)
    public Object getOrUpdateCheckCode(HttpServletResponse response, HttpSession session,
                                       @RequestJson(value = "imagewidth") Integer imagewidth,
                                       @RequestJson(value = "imagehight") Integer imagehight) throws Exception {
        try {
            Map<String, Object> map = ImageUtil.getImageCode(imagewidth, imagehight);
            String checkKey = ImageUtil.checkKey;
            session.setAttribute(checkKey, map.get("randomCode").toString().toLowerCase());
            session.setAttribute("lastTime", new Date().getTime());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();//io流
            ImageIO.write((BufferedImage) map.get("image"), "png", baos);
            byte[] bytes = baos.toByteArray();//转换成字节
            String png_base64 = Base64.getEncoder().encodeToString(bytes);
            return AuthUtil.parseJsonKeyToLower("success", "data:image/png;base64," + png_base64);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2020/9/14 0014 上午 9:07
     * @Description: 二维码信息和用户绑定，登录用户
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "bindQRCodeAndUserData", method = RequestMethod.POST)
    public Object bindQRCodeAndUserData(@RequestJson(value = "qrcode") String qrcode,
                                        @RequestJson(value = "useraccount") String useraccount,
                                        @RequestJson(value = "userpwd") String userpwd
    ) throws Exception {
        try {
            String mark = "pass";
            //绑定用户
            if (qrAndUser.containsKey(qrcode)) {//当前二维码已绑定其他用户
                mark = "ishaveuser";
            } else {
                qrAndUser.put(qrcode, useraccount);
            }
            //用户登录
            Map<String, SocketIOClient> keyAndLoinUser = MessageEventHandler.keyAndLoginUser;
            if (keyAndLoinUser != null && keyAndLoinUser.containsKey(qrcode)) {
                SocketIOClient socketIOClient = keyAndLoinUser.get(qrcode);
                Map<String, Object> paramData = new HashMap<>();
                paramData.put("sessionId", socketIOClient.getSessionId().toString());
                String clientIp = socketIOClient.getRemoteAddress().toString();
                clientIp = clientIp.startsWith("/") ? clientIp.substring(1) : clientIp;
                clientIp = clientIp.split(":")[0];
                paramData.put("sessionId", socketIOClient.getSessionId().toString());
                paramData.put("clientIp", clientIp);
                validAccountAndPwd(useraccount, userpwd, paramData, false);
                String state;
                Map<String, String> dataMap = validAccountAndPwd(useraccount, userpwd, paramData, false);
                //发生消息给PC端
                if (dataMap.get("rediskey") != null) {
                    String dateTime = new DateTime().toString("hh:mm:ss");
                    dataMap.put("flag", MessageInfo.rightSuccess);
                    socketIOClient.sendEvent("loginSever", dateTime, dataMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @Description: 解锁登录用户
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/27 8:50
     */
    @RequestMapping(value = "unlockLoginUser", method = RequestMethod.POST)
    public Object unlockLoginUser(@RequestJson(value = "useraccount") String useraccount) {
        try {
            String key = login_prefix + useraccount;
            RedisTemplateUtil.deleteCache(key);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2020/9/14 0014 上午 9:07
     * @Description: 二维码信息和用户绑定，登录用户
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "validScan", method = RequestMethod.POST)
    public Object validScan(@RequestJson(value = "qrcode", required = false) String qrcode) {
        try {
            //用户登录
            Map<String, SocketIOClient> keyAndLoinUser = MessageEventHandler.keyAndLoginUser;
            if (keyAndLoinUser != null && keyAndLoinUser.containsKey(qrcode)) {
                SocketIOClient socketIOClient = keyAndLoinUser.get(qrcode);
                //发生消息给PC端
                String dateTime = new DateTime().toString("hh:mm:ss");
                socketIOClient.sendEvent("loginSever", dateTime, MessageInfo.scanSuccess);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2020/9/14 0014 上午 9:07
     * @Description: 二维码信息过期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "overScan", method = RequestMethod.POST)
    public Object overScan(@RequestJson(value = "qrcode", required = false) String qrcode) {
        try {
            //用户登录
            Map<String, SocketIOClient> keyAndLoinUser = MessageEventHandler.keyAndLoginUser;
            if (keyAndLoinUser != null && keyAndLoinUser.containsKey(qrcode)) {
                SocketIOClient socketIOClient = keyAndLoinUser.get(qrcode);
                //发生消息给PC端
                keyAndLoinUser.remove(qrcode);
                String dateTime = new DateTime().toString("hh:mm:ss");
                socketIOClient.sendEvent("loginSever", dateTime, MessageInfo.scanOver);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2018/10/11 0011 下午 6:25
     * @Description: 验证验证码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "验证验证码", notes = "验证验证码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "checkcode", value = "验证码", defaultValue = "", required = true, dataType = "String")
    })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"), @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "validCheckCode", method = RequestMethod.POST)
    public Object validCheckCode(HttpServletResponse response, HttpSession session,
                                 @RequestJson(value = "checkcode") String checkcode
    ) throws Exception {

        String state = validCode(session, checkcode);
        return AuthUtil.parseJsonKeyToLower(state, null);
    }

    /**
     * @author: lip
     * @date: 2018/10/11 0011 下午 7:01
     * @Description: 验证session中的checkcode是否等于参数中的checkcode
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String validCode(HttpSession session, String checkcode) {
        String state = "";
        Object sessionCheckCode = session.getAttribute(ImageUtil.checkKey);
        if (sessionCheckCode == null) {
            state = "invalidCode";
        } else {
            String sessionCheckCodeString = sessionCheckCode.toString();
            Date now = new Date();
            Long codeTime = Long.valueOf(session.getAttribute("lastTime") + "");
            if (StringUtils.isEmpty(checkcode) || sessionCheckCodeString == null || !(checkcode.equalsIgnoreCase(sessionCheckCodeString))) {
                state = "errorCode";
            } else if ((now.getTime() - codeTime) / 1000 / 60 > 5) {
                state = "invalidCode";
            } else {
                session.removeAttribute(ImageUtil.checkKey);
                state = "rightCode";
            }
        }

        return state;
    }


}

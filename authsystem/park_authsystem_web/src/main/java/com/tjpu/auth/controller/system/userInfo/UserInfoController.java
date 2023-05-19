package com.tjpu.auth.controller.system.userInfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.auth.common.utils.RedisTemplateUtil;
import com.tjpu.auth.model.codeTable.CommonSelectFieldConfigVO;
import com.tjpu.auth.model.codeTable.CommonSelectTableConfigVO;
import com.tjpu.auth.model.system.SysMenuVO;
import com.tjpu.auth.model.system.UserInfoVO;
import com.tjpu.auth.service.codeTable.CommonSelectFieldConfigService;
import com.tjpu.auth.service.codeTable.CommonSelectTableConfigService;
import com.tjpu.auth.service.common.CommonServiceSupport;
import com.tjpu.auth.service.micro.AuthSystemMicroService;
import com.tjpu.auth.service.system.OperateLogService;
import com.tjpu.auth.service.system.SysMenuService;
import com.tjpu.auth.service.system.UserInfoService;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataAuthCommon;
import com.tjpu.pk.common.utils.RequestUtil;
import io.swagger.annotations.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author: zzc
 * @date: 2018年4月18日 上午10:50:29
 * @Description:用户操作控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version： V1.0
 */
@RestController
@Api(value = "用户操作Api", tags = {"用户操作接口"})
@RequestMapping("userInfo")
public class UserInfoController {

    /* 默认数据源 */
    @Value("${spring.datasource.primary.name}")
    private String defaultDataSourceKey;

    /**
     * 配置的行政区划父级编码
     */
    @Value("${com.dc.regionparentcode}")
    private String regionparentcode;


    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private UserInfoService userService;
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonSelectTableConfigService commonSelectTableConfigService;
    @Autowired
    private CommonSelectFieldConfigService commonSelectFieldConfigService;

    //企业用户污染源sysmodel
    private static final String entUserPollutionListSysModel = "entUserPollutionList";

    /**
     * @author: lip
     * @date: 2018/9/27 0027 上午 11:09
     * @Description: 获取当前登陆用户系统信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "获取当前登陆用户系统信息", notes = "获取当前登陆用户系统信息")
    @ApiImplicitParam(name = "userId", value = "用户ID", defaultValue = "", required = true, dataType = "String")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")
    })
    @RequestMapping(value = "getLoginUserSystemInfo", method = RequestMethod.POST)
    public Object getLoginUserSystemInfo() {
        try {
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

            if (objectList != null) {
                for (JSONObject jsonObject : objectList) {
                    if (jsonObject.get("parentid") != null && "root".equals(jsonObject.get("parentid"))) {
                        Map<String, Object> sysAppMap = new LinkedHashMap<String, Object>();
                        sysAppMap.put("appName", jsonObject.get("menuname") != null ? jsonObject.get("menuname").toString() : "");
                        sysAppMap.put("menuType", jsonObject.get("menutype") != null ? jsonObject.get("menutype"): "");
                        sysAppMap.put("appId", jsonObject.get("menuid") != null ? jsonObject.get("menuid").toString() : "");
                        sysAppMap.put("target", jsonObject.get("target") != null ? jsonObject.get("target").toString() : "");
                        sysAppMap.put("appImg", jsonObject.get("icon") != null ? jsonObject.get("icon").toString() : "");
                        sysAppMap.put("appUrl", jsonObject.get("navigateurl") != null ? jsonObject.get("navigateurl").toString() : "");
                        sysAppMap.put("datalistchildren", jsonObject.get("datalistchildren"));
                        dataList.add(sysAppMap);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018/9/27 0027 上午 11:32
     * @Description: 根据系统ID获取当前登陆用户的菜单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "根据系统ID获取当前登陆用户的菜单信息", notes = "根据系统ID获取当前登陆用户的菜单信息")
    @ApiImplicitParam(name = "appid", value = "系统ID", defaultValue = "", required = true, dataType = "String")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")
    })
    @RequestMapping(value = "getLoginUserMenuByAppId", method = RequestMethod.POST)
    public Object getLoginUserMenuByAppId(@RequestJson(value = "appid") String appId) {
        try {
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            Map<String, Object> sysAppMap = new LinkedHashMap<String, Object>();
            if (objectList != null) {
                for (JSONObject jsonObject : objectList) {
                    if (jsonObject.get("menuid") != null && appId.equals(jsonObject.get("menuid"))) {
                        sysAppMap.put("icon", jsonObject.get("icon") != null ? jsonObject.get("icon").toString() : "");
                        sysAppMap.put("appName", jsonObject.get("menuname") != null ? jsonObject.get("menuname").toString() : "");
                        List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonObject.get("datalistchildren");
                        sysAppMap.put("dataList", dataList);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", sysAppMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018/9/27 0027 上午 10:31
     * @Description: 根据用户ID, 获取用户系统菜单权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "根据用户ID,获取用户系统菜单权限", notes = "根据用户ID,获取用户系统菜单权限")
    @ApiImplicitParam(name = "userid", value = "用户ID", defaultValue = "", required = true, dataType = "String")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")
    })
    @RequestMapping(value = "getSystemMenuRightByUserId", method = RequestMethod.POST)
    public Object getSystemMenuRightByUserId(@RequestJson(value = "userid") String userid,HttpServletRequest request) {
        try {


            List<Map<String, Object>> userAuth = userService.getSystemRightByUserId(userid);
            return AuthUtil.parseJsonKeyToLower("success", userAuth);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2018/6/8 17:54
     * @Description: 重置用户密码
     * @updateUser:xsm
     * @updateDate:2018/8/6 15:32
     * @updateDescription:添加日志记录
     * @param: userId 用户ID
     * @return:
     */
    @ApiOperation(value = "重置用户密码", notes = "将用户密码重置为初始默认密码")
    @ApiImplicitParam(name = "userId", value = "用户ID", defaultValue = "", required = true, dataType = "String")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")
    })
    @RequestMapping(value = "resetUserPassword", method = RequestMethod.POST)
    public Object resetUserPassword(@RequestJson(value = "userid") String userId) throws Exception {
        try {
            if (StringUtils.isNotBlank(userId)) {
                userService.resetUserPassword(userId);
                //添加日志记录
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("resetuserid", userId);
                operateLogService.saveUserOperationLog("resetpwd", paramMap, null, null, null);
                return AuthUtil.parseJsonKeyToLower("success", null);
            }
            return AuthUtil.parseJsonKeyToLower("fail", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2018/6/8 17:54
     * @Description: 修改用户密码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: userId     用户ID
     * @param: userNewPwd 修改后的密码
     * @return:
     */
    @ApiOperation(value = "修改用户密码", notes = "修改用户密码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "userid", value = "用户ID", defaultValue = "", required = true, dataType = "String"),
            @ApiImplicitParam(name = "usernewpwd", value = "修改后的密码", defaultValue = "", required = true, dataType = "String")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")
    })
    @RequestMapping(value = "editUserPassword", method = RequestMethod.POST)
    public Object editUserPassword(@RequestJson(value = "userid") String userId, @RequestJson(value = "usernewpwd") String userNewPwd) {
        try {
            if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userNewPwd)) {
                userService.editUserPassword(userId, userNewPwd);
                //添加日志记录
                Map<String,Object> paramMap = new HashMap<>();
                operateLogService.saveUserOperationLog("editpwd", paramMap, null, null, null);
                return AuthUtil.parseJsonKeyToLower("success", true);
            }
            return AuthUtil.parseJsonKeyToLower("fail", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2018/6/20 18:08
     * @Description: 判断用户密码是否正确
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: userId 用户id userOldPwd 旧的密码
     * @return: true 旧密码正确 false 旧密码错误
     */
    @ApiIgnore
    @RequestMapping(value = "judgeUserPassword", method = RequestMethod.POST)
    public Object judgeUserPassword(@RequestJson(value = "userid") String userId, @RequestJson(value = "useroldpwd") String userOldPwd) {
        try {
            if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userOldPwd)) {
                Boolean flag = userService.judgeUserPassword(userId, userOldPwd);
                return AuthUtil.parseJsonKeyToLower("success", flag);
            }
            return AuthUtil.parseJsonKeyToLower("fail", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2018/8/2 10:42
     * @Description: 获取行政区划树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "获取行政区划Tree数据", notes = "获取所有行政区划Tree数据")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getRegionTreeData", method = RequestMethod.POST)
    public Object getRegionTreeData() {
        try {
            // ###自定义数据中心数据源
            //DynamicDataSourceContextHolderUtil.setDataSourceType(datacenterDataSource);
            List<Map<String, Object>> regionList = userService.getRegionTreeData(regionparentcode);
            // ###切换回默认数据源
            // DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSourceKey);
            return AuthUtil.parseJsonKeyToLower("success", regionList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2018/8/2 10:42
     * @Description: 根据用户ID和用户类型获取用户拥有的行政区划权限或污染源企业权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "根据用户ID和用户类型获取用户拥有的行政区划权限或污染源企业权限", notes = "根据用户ID和用户类型获取用户拥有的行政区划权限或污染源企业权限，当用户类型为环保用户时，只返回用户拥有的行政区划权限；当用户类型为企业用户时，则返回用户拥有的污染源企业权限")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "userid", value = "用户ID", defaultValue = "", required = true, dataType = "String"),
            @ApiImplicitParam(name = "usertype", value = "用户类型(\"0\":环保用户;\"1\":企业用户)", defaultValue = "", required = true, dataType = "String")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getRegionInfoOrPollutionInfoByUserIdAndUserType", method = RequestMethod.POST)
    public Object getRegionInfoOrPollutionInfoByUserIdAndUserType(
            @RequestJson(value = "userid", required = true) String userId,
            @RequestJson(value = "usertype", required = false) String userType) {
        try {
            String regionCode = "";
            Map<String, Object> data = new LinkedHashMap<>();
            String type = "";
            if (userId != null && !"".equals(userId)) {
                UserInfoVO user = userService.selectUserByUserId(userId);
                type = user.getUserType();
            }
            // 根据用户类型判断该用户是监管用户还是企业用户
            if (StringUtils.isNotBlank(userType) && type.equals(userType)) {
                if (StringUtils.isNotBlank(userType) && "0".equals(userType)) {// 环保用户
                    List<String> RegionCodeList = userService.getRegionListByUserId(userId);
                    data.put("regioncodelistone", RegionCodeList);
                } else if (StringUtils.isNotBlank(userType) && "1".equals(userType)) {// 企业用户
                    List<Map<String, Object>> RegionCodeList = userService.getDataPermissionsByUserId(userId);
                    List<String> pollutionList = new ArrayList<String>();
                    List<String> regionList = new ArrayList<String>();
                    if (RegionCodeList.size() > 0) {
                        for (int i = 0; i < RegionCodeList.size(); i++) {
                            Map<String, Object> map = RegionCodeList.get(i);
                            if (map.get("fk_regioncode") != null && map.get("pollutionid") != null) {
                                if (i == 0) {
                                    // 获取行政区划编码
                                    regionCode = map.get("fk_regioncode").toString();
                                    // regionList.add(map.get("FK_RegionCode").toString());
                                    pollutionList.add(map.get("pollutionid").toString());
                                } else {
                                    pollutionList.add(map.get("pollutionid").toString());

                                }
                            }
                        }
                        if (!"".equals(regionCode)) {
                            // ###自定义数据中心数据源
                            //DynamicDataSourceContextHolderUtil.setDataSourceType(datacenterDataSource);
                            // 根据行政区划编码查询其是否有子节点，有则返回一个包含父节点和子节点在内的List
                            regionList = userService.getChildRegionCodeByParentRegionCode(regionCode);
                            // ###切换回默认数据源
                            //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSourceKey);
                        }
                        data.put("regioncodelisttwo", regionList);
                        data.put("pollutionidlist", pollutionList);
                    } else {
                        data.put("regioncodelisttwo", null);
                        data.put("pollutionidlist", null);
                    }
                }
            } else {
                data.put("regioncodelistone", null);
                data.put("regioncodelisttwo", null);
                data.put("pollutionidlist", null);
            }
            return AuthUtil.parseJsonKeyToLower("success", data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private final AuthSystemMicroService authSystemMicroService;


    @Autowired
    public UserInfoController(AuthSystemMicroService authSystemMicroService,
                              CommonSelectTableConfigService commonSelectTableConfigService) {
        this.authSystemMicroService = authSystemMicroService;

        this.commonSelectTableConfigService = commonSelectTableConfigService;
    }


    /**
     * @author: xsm
     * @date: 2018/8/14 10:03
     * @Description: 根据行政区划编码获取污染源企业列表数据和按钮数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "根据行政区划编码获取污染源企业列表数据和按钮数据（带控件）", notes = "根据行政区划编码获取该行政区划和其子级行政区划关联的污染源企业列表数据和按钮数据")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "fk_region", value = "行政区划编码", defaultValue = "", required = false, dataType = "String"),
            @ApiImplicitParam(name = "pagenum", value = "当前页码数，从第一页开始，必须与pageSize一起使用，若pageNum和pagesize为null,则查询满足其它查询条件的所有数据", defaultValue = "", required = false, dataType = "Integer"),
            @ApiImplicitParam(name = "pagesize", value = "每页显示的行数，必须与pagenum一起使用，若pagenum和pagesize为null,则查询满足其它查询条件的所有数据", defaultValue = "", required = false, dataType = "Integer")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getPollutionInfosAndButtonsByRegionCode", method = RequestMethod.POST)
    public Object getPollutionInfosAndButtonsByRegionCode(
            @RequestJson(value = "fk_region", required = false) String FK_Region,
            @RequestJson(value = "pagenum", required = false) Integer pageNum,
            @RequestJson(value = "pagesize", required = false) Integer pageSize) {
        try {
//            String sysModel = "entUserPollutionList";
            Map<String, Object> data = new HashMap<String, Object>();
            //自定义查询按钮
            List<Map<String, Object>> buttonlist = new ArrayList<Map<String, Object>>();
            Map<String, Object> buttonmap = new HashMap<String, Object>();
            buttonmap.put("icon", "icon-tianjia");
            buttonmap.put("model", 2);
            buttonmap.put("compile", "查询");
            buttonmap.put("type", "success");
            buttonmap.put("clickName", "searchButton");
            buttonlist.add(buttonmap);
            data.put("topOperations", buttonlist);
            // 默认查询的字段类型
            String listFieldType = "list-region";
            String queryControlFieldType = "query-region";

            //微服务获取查询控件信息

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", entUserPollutionListSysModel);
            paramMap.put("listfieldtype", listFieldType);
            paramMap.put("queryfieldtype", queryControlFieldType);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object queryCriteriaData = authSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject = JSONObject.fromObject(queryCriteriaData);
            JSONObject queryFieldsData = JSONObject.fromObject(jsonObject.get("data"));
            //表头信息
            Object title = authSystemMicroService.getTableTitle(param);
            JSONObject jsonObject1 = JSONObject.fromObject(title);
            Object tableTitle = jsonObject1.get("data");

            // 分页
            if (pageNum != null && pageSize != null) {
                PageHelper.startPage(pageNum, pageSize);
            }
            paramMap.clear();
            // 行政区划编码
            if (FK_Region != null && !"".equals(FK_Region)) {
                String regionList[] = FK_Region.split(",");
                paramMap.put("regionlist", regionList);
            }
            List<Map<String, Object>> dataList = null;
            Map<String, Object> resultMap = new HashMap<>();
            if (!"".equals(FK_Region) && FK_Region != null) {
                // ###自定义数据中心数据源
                //DynamicDataSourceContextHolderUtil.setDataSourceType(datacenterDataSource);
                // 根据行政区划编码查询关联的污染源企业信息
                dataList = userService.getDataPermissionsListByParams(paramMap);
                // ###切换回默认数据源
                //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSourceKey);
                // 保存分页信息
                PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
                // 分页后的数据
                List<Map<String, Object>> listInfo = pageInfo.getList();
                resultMap.put("pagesize", pageInfo.getPageSize());// 每页条数
                resultMap.put("pagenum", pageInfo.getPageNum());// 当前页
                resultMap.put("total", pageInfo.getTotal());// 总条数
                resultMap.put("pages", pageInfo.getPages());// 总页数
                resultMap.put("listdata", listInfo);// 数据            
            }

            data.put("ruleform", queryFieldsData.get("ruleform"));//
            data.put("queryfields", queryFieldsData.get("queryfields"));
            data.put("tabletitle", tableTitle);// 表头
            data.put("listdata", resultMap);// 数据
            return AuthUtil.parseJsonKeyToLower("success", data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2018/8/7 10:34
     * @Description: 根据用户ID获取用户拥有的行政区划权限或污染源企业权限及污染源企业列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "根据用户ID获取用户拥有的行政区划权限或污染源企业权限及污染源企业列表数据（带控件）", notes = "根据用户ID获取用户拥有的行政区划权限或污染源企业权限及污染源企业列表数据，当用户类型为环保用户时，只返回用户拥有的行政区划权限；当用户类型为企业用户时，则返回用户拥有的污染源企业权限以及污染源的列表数据")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "userid", value = "用户主键ID", defaultValue = "", required = true, dataType = "String"),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getRegionInfoOrPollutionListDataByUserId", method = RequestMethod.POST)
    public Object getRegionInfoOrPollutionListDataByUserId(
            @RequestJson(value = "userid", required = true) String userId) {
        try {
//            String sysModel = "entUserPollutionList";
            // 根据用户ID查询用户信息
            UserInfoVO user = userService.getUserInfoByUserId(userId);
            // 获取用户类型
            String userType = user.getUserType();
            // 判断用户类型，"0":表示监管用户；"1":表示企业用户。
            String regionCode = "";
            Map<String, Object> data = new LinkedHashMap<>();
            if ("0".equals(userType)) {
                // 根据用户ID去获取用户行政区划关系表中该用户关联的行政区划Code(一对多)
                List<String> regionList = userService.getRegionListByUserId(userId);
                data.put("usertype", "0");
                data.put("regioncodelistone", regionList);
            } else if ("1".equals(userType)) {
                List<Map<String, Object>> RegionCodeList = userService.getDataPermissionsByUserId(userId);
                List<String> pollutionList = new ArrayList<String>();
                if (RegionCodeList.size() > 0) {
                    for (int i = 0; i < RegionCodeList.size(); i++) {
                        Map<String, Object> map = RegionCodeList.get(i);
                        if (i == 0) {
                            // 获取行政区划编码
                            regionCode = map.get("fk_regioncode").toString();
                            pollutionList.add(map.get("pollutionid").toString());
                        } else {
                            pollutionList.add(map.get("pollutionid").toString());
                        }
                    }
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("pollutionlist", pollutionList);
                    // ###自定义数据中心数据源
                    //DynamicDataSourceContextHolderUtil.setDataSourceType(datacenterDataSource);
                    // 根据污染源ID去数据中心库中查询ID对应的污染源信息
                    List<Map<String, Object>> pollutionlist = userService.getPollutionListByPollutionIdList(params);
                    // ###切换回默认数据源
                    //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSourceKey);
                    data.put("usertype", "1");
                    data.put("regioncodelisttwo", regionCode);
                    data.put("pollutionidlist", pollutionlist);
                } else {
                    data.put("usertype", "1");
                    data.put("regioncodelisttwo", null);
                    data.put("pollutionidlist", null);
                }
                String listFieldType = "list-region";
                // 根据表名，获取配置表中获取该表的实体信息
                CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(entUserPollutionListSysModel);
                List<CommonSelectFieldConfigVO> listFields = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableVO.getPkTableConfigId(), listFieldType);
                // 获取表头信息
                List<Map<String, Object>> tableTitle = CommonServiceSupport.getTableTitle(listFields);
                data.put("tableTitle", tableTitle);
            }
            return AuthUtil.parseJsonKeyToLower("success", data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2018/10/20 0020 上午 11:24
     * @Description: 获取当前登录企业用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "获取当前登录企业用户信息", notes = "获取当前登录企业用户信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getLoginEntInfoData", method = RequestMethod.POST)
    public Object getLoginEntInfoData() {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);

            List<Map<String, Object>> dataList = new ArrayList<>();
            if (userId != null && !"".equals(userId)) {
                List<Map<String, Object>> entUsers = userService.getEntUserByUserId(userId);
                if (entUsers != null) {
                    List<String> Ent_Ids = new ArrayList<>();
                    for (Map<String, Object> map : entUsers) {
                        Ent_Ids.add(map.get("Ent_Id").toString());
                    }
                    if (Ent_Ids.size() > 0) {
                        //DynamicDataSourceContextHolderUtil.setDataSourceType(datacenterDataSource);
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("pollutionlist", Ent_Ids);
                        dataList = userService.getPollutionListByPollutionIdList(paramMap);
                        // ###切换回默认数据源
                        //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSourceKey);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            // ###切换回默认数据源
            //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSourceKey);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zzc
     * @date: 2018/6/21 11:29
     * @Description: 获取用户在菜单上拥有的按钮权限信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getUserButtonAuthInMenu", method = RequestMethod.POST)
    public Object getUserButtonAuthInMenu(@RequestJson(value = "sysmodel", required = true) String sysModel) {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            SysMenuVO menuVO = sysMenuService.getMenuVOByMenuCode(sysModel);
            String menuId = menuVO.getMenuId();
            if (StringUtils.isNotBlank(menuId) && StringUtils.isNotBlank(userId)) {
                Map<String, Object> buttonAuthData = userService.getUserButtonAuthInMenu(menuId, userId);
                return AuthUtil.parseJsonKeyToLower("success", buttonAuthData);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private static final String USER_SYS_MODEL = "userManager";

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:21
     * @Description: 获取用户列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getUserListDataByParamMap", method = RequestMethod.POST)
    public Object getUserListDataByParamMap(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", USER_SYS_MODEL);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object listData = authSystemMicroService.getListData(param);
            listData =  AuthUtil.decryptData(listData);
            JSONObject jsonObject = JSONObject.fromObject(listData);
            Object data = jsonObject.get("data");
            if (data != null) {
                JSONObject jsonObject1 = JSONObject.fromObject(data);
                Object tablelistdata = jsonObject1.get("tablelistdata");
                if (tablelistdata != null) {
                    List<Map<String, Object>> listInfo = (List<Map<String, Object>>) tablelistdata;
                    if (listInfo.size() > 0) {
                        List<Map<String, Object>> list = new ArrayList<>();
                        list.add(DataAuthCommon.getAuthDataMap("user_account", "admin", false, false, true));
                        DataAuthCommon.formatDataAuth(listInfo, list);
                        jsonObject1.put("tablelistdata", listInfo);
                        jsonObject.put("data", jsonObject1);
                    }
                }
                return AuthUtil.parseJsonKeyToLower("success",jsonObject1);
            }
            return AuthUtil.parseJsonKeyToLower("success",data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:21
     * @Description: 获取用户列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getUserListPage", method = RequestMethod.POST)
    public Object getUserListPage(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("sysmodel", USER_SYS_MODEL);
            paramMap.put("userid", userid);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object listData = authSystemMicroService.getListByParam(param);
            listData =  AuthUtil.decryptData(listData);
            JSONObject jsonObject = JSONObject.fromObject(listData);
            Object data = jsonObject.get("data");
            if (data != null) {
                JSONObject jsonObject1 = JSONObject.fromObject(data);
                Object tabledata = jsonObject1.get("tabledata");
                if (tabledata != null) {
                    JSONObject tabledata1 = JSONObject.fromObject(tabledata);
                    Object tablelistdata = tabledata1.get("tablelistdata");
                    if (tablelistdata != null) {
                        List<Map<String, Object>> listInfo = (List<Map<String, Object>>) tablelistdata;
                        if (listInfo.size() > 0) {
                            List<Map<String, Object>> list = new ArrayList<>();
                            list.add(DataAuthCommon.getAuthDataMap("user_account", "admin", false, false, true));
                            DataAuthCommon.formatDataAuth(listInfo, list);
                            tabledata1.put("tablelistdata", listInfo);
                            jsonObject1.put("tabledata", tabledata1);
                            jsonObject.put("data", jsonObject1);
                        }
                    }
                }
                return AuthUtil.parseJsonKeyToLower("success",jsonObject1);
            }
            return AuthUtil.parseJsonKeyToLower("success",data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     *
     * @author: lip
     * @date: 2020/8/18 0018 上午 9:34
     * @Description: 获取用户id和Name
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @RequestMapping(value = "getAllUserInfo", method = RequestMethod.POST)
    public Object getAllUserInfo() {
        try {
            List<Map<String, Object>> resultList = userService.getAllUserInfo();
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    @RequestMapping(value = "getUserDepartmentTree", method = RequestMethod.POST)
    public Object getUserDepartmentTree() {
        try {
            List<Map<String, Object>> resultList = userService.getUserDepartmentTree();
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


}

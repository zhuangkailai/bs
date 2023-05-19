package com.tjpu.sp.controller.environmentalprotection.pubclassconfig;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.pubclassconfig.PubClassConfigService;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: xsm
 * @date: 2019/6/3  上午 9:30
 * @Description: 公共代码分类配置处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("pubclassconfig")
@Api(value = "公共代码分类配置处理类", tags = "公共代码分类配置处理类")
public class PubClassConfigController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private PubClassConfigService pubClassConfigService;
    /**
     * 公共代码分类配置表的sysmodel
     */
    private String sysmodel = "commoncode";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @Author: xsm
     * @Date: 2019/6/3 下午1:17
     * @Description: 公共代码-获取码表分类配置树
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getPubClassConfigTreeData", method = RequestMethod.POST)
    public Object getPubClassConfigTreeData() {
        List<Map<String, Object>> result = pubClassConfigService.getPubClassConfigTreeData();
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    /**
     * @param
     * @return
     * @throws Exception
     * @author: xsm
     * @date: 2019/6/4 上午9:14
     * @Description: 根据自定义参数获取某类型码表的记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @RequestMapping(value = "getPubCodeRecordsByParamMap", method = RequestMethod.POST)
    public Object getPubCodeRecordsByParamMap(@RequestJson(value = "paramsjson", required = false) Object paramsjson
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (null != paramsjson) {
                paramMap = JSONObject.fromObject(paramsjson);
            }
            paramMap.put("sysmodel", paramMap.get("tablecode"));
            paramMap.put("listfieldtype", "list");
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListData(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/4 16:58
     * @Description: 根据码表类型编码获取该类型码表的初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getPubCodeRecordsListPageByTableCode", method = RequestMethod.POST)
    public Object getPubCodeRecordsListPageByTableCode(HttpSession session, HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            paramMap.put("userid", userId);
            if (paramMap.get("tablecode") != null && !"".equals(paramMap.get("tablecode"))) {
                paramMap.put("sysmodel", sysmodel);
            } else {
                paramMap.put("sysmodel", paramMap.get("tablecode"));
            }
            //微服务参数
            paramMap.put("datasource", datasource);
            //按钮数据
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject4 = JSONObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject4.getString("data");
            JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
            String topoperations = jsonObject5.getString("topbuttondata");
            String listoperation = jsonObject5.getString("tablebuttondata");
            Map<String, Object> buttondatamap = new HashMap<>();//按钮
            buttondatamap.put("topbuttondata", topoperations);
            buttondatamap.put("tablebuttondata", listoperation);

            paramMap.put("listfieldtype", "list");
            paramMap.put("sysmodel", paramMap.get("tablecode"));
            paramMap.put("queryfieldtype", "query");
            String param = AuthUtil.paramDataFormat(paramMap);
            //表头数据
            Object tableTitle = publicSystemMicroService.getTableTitle(param);
            JSONObject jsonObject = JSONObject.fromObject(tableTitle);
            String titleData = jsonObject.getString("data");
            //查询条件数据
            Map<String, Object> querydata = new HashMap<>();
            Object queryCriteriaData = publicSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject2 = jsonObject.fromObject(queryCriteriaData);
            String queryData = jsonObject2.getString("data");
            JSONObject jsonObject3 = JSONObject.fromObject(queryData);
            String dualcontrolskey = jsonObject3.getString("dualcontrolskey");
            String querycontroldata = jsonObject3.getString("querycontroldata");
            String queryformdata = jsonObject3.getString("queryformdata");
            querydata.put("dualcontrolskey", dualcontrolskey);
            querydata.put("querycontroldata", querycontroldata);
            querydata.put("queryformdata", queryformdata);
            //列表数据
            Object listData = publicSystemMicroService.getListData(param);
            listData = AuthUtil.decryptData(listData);
            JSONObject jsondata = JSONObject.fromObject(listData);
            String listdata = jsondata.getString("data");
            JSONObject jsondatas = JSONObject.fromObject(listdata);
            jsondatas.put("tabletitledata", titleData);// 表头
            //返回数据
            resultMap.put("querydata", querydata);
            resultMap.put("tabledata", jsondatas);
            resultMap.put("buttondata", buttondatamap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2018/12/27 13:54
     * @Description: 获取公共代码分类配置信息添加页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getPubCodeRecordAddPageByTableCode", method = RequestMethod.POST)
    public Object getPubCodeRecordAddPageByTableCode(@RequestJson(value = "tablecode", required = false) String tablecode, HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", tablecode);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(Param);
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/10/17 0017 上午 10:09
     * @Description: 新增公共代码分类配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "addPubCodeRecordByTableCode", method = RequestMethod.POST)
    public Object addPubCodeRecordByTableCode(@RequestJson(value = "formdata", required = false) Object paramsjson, @RequestJson(value = "tablecode", required = false) String tablecode) throws Exception {
        try {
            if (paramsjson != null) {
                Map<String, Object> paramMap = new HashMap<>();
                Map<String, Object> keyAndValue = (Map<String, Object>) paramsjson;
                List<String> keys = new ArrayList<>();
                List<Object> values = new ArrayList<>();
                for (String key : keyAndValue.keySet()) {
                    if (!"".equals(keyAndValue.get(key))) {
                        keys.add(key);
                        values.add(keyAndValue.get(key));
                    }
                }
                if (keys.size() > 0) {
                    paramMap.put("fieldList", keys);
                    paramMap.put("values", values);
                    paramMap.put("sysmodel", tablecode);
                    pubClassConfigService.addPubCodeDataByParam(paramMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2018/9/7 8:36
     * @Description: 根据主键ID获取公共代码分类配置信息修改页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getPubCodeRecordUpdatePageByParamMap", method = RequestMethod.POST)
    public Object getPubCodeRecordUpdatePageByParamMap(@RequestJson(value = "tablecode", required = false) String tablecode,
                                                       @RequestJson(value = "tablekey", required = true) String tablekey,
                                                       @RequestJson(value = "keyval", required = true) Object keyval) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (tablekey != null && !"".equals(tablekey) && keyval != null && !"".equals(keyval)) {
                //设置参数
                paramMap.put("sysmodel", tablecode);
                paramMap.put(tablekey, keyval);
                paramMap.put("datasource", datasource);
                String Param = AuthUtil.paramDataFormat(paramMap);
                Object resultList = publicSystemMicroService.goUpdatePage(Param);
                return resultList;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/10/17 0017 上午 10:10
     * @Description: 修改公共代码分类配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "updatePubCodeRecordByTableCode", method = RequestMethod.POST)
    public Object updatePubCodeRecordByTableCode(@RequestJson(value = "formdata", required = false) Object paramsjson, @RequestJson(value = "tablecode", required = false) String tablecode) throws Exception {
        try {
            if (paramsjson != null) {
                Map<String, Object> paramMap = new HashMap<>();
                Map<String, Object> keyAndValue = (Map<String, Object>) paramsjson;
                paramMap.putAll(keyAndValue);
                paramMap.put("sysmodel", tablecode);
                pubClassConfigService.editPubCodeDataByParam(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2018/12/27 18:12
     * @Description: 根据公共代码分类配置信息主键ID删除单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "deletePubCodeRecordByParamMap", method = RequestMethod.POST)
    public Object deletePubCodeRecordByParamMap(@RequestJson(value = "tablecode") String tablecode,
                                                @RequestJson(value = "tablekey") String tablekey,
                                                @RequestJson(value = "keyval") Object keyval) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (tablekey != null && !"".equals(tablekey) && keyval != null && !"".equals(keyval)) {
                paramMap.put("sysmodel", tablecode);
                paramMap.put("tablekey", tablekey);
                paramMap.put("keyval", keyval);
                pubClassConfigService.deletePubCodeDataByParam(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2018/12/27 18:12
     * @Description: 根据公共代码分类配置信息主键ID获取详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getPubCodeRecordDetailByParamMap", method = RequestMethod.POST)
    public Object getPubCodeRecordDetailByParamMap(@RequestJson(value = "tablecode", required = false) String tablecode,
                                                   @RequestJson(value = "tablekey", required = true) String tablekey,
                                                   @RequestJson(value = "keyval", required = true) Object keyval) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (tablekey != null && !"".equals(tablekey) && keyval != null && !"".equals(keyval)) {
                //设置参数
                paramMap.put("sysmodel", tablecode);
                paramMap.put(tablekey, keyval);
                paramMap.put("datasource", datasource);
                String Param = AuthUtil.paramDataFormat(paramMap);
                Object resultList = publicSystemMicroService.getDetail(Param);
                return resultList;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/11/14 0014 上午 11:51
     * @Description: 验证传入数据是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [code, tablename]
     * @throws:
     */
    @RequestMapping(value = "isTableDataHaveInfo", method = RequestMethod.POST)
    public Object isTableDataHaveInfo(@RequestJson(value = "sysmodel") String sysmodel,
                                      @RequestJson(value = "andstring", required = false) String andstring,
                                      @RequestJson(value = "value") String value,
                                      @RequestJson(value = "key") String key) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("key", key);
            paramMap.put("value", value);
            paramMap.put("andstring", andstring);
            int tableDataHaveInfo = pubClassConfigService.isTableDataHaveInfo(paramMap);
            if (tableDataHaveInfo == 0) {
                return AuthUtil.parseJsonKeyToLower("success", "no");
            } else {    //已经有了不添加
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

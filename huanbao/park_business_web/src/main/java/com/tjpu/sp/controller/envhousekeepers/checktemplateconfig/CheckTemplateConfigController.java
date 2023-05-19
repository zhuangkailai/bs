package com.tjpu.sp.controller.envhousekeepers.checktemplateconfig;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.FreeMarkerWordUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.checktemplateconfig.CheckTemplateConfigVO;
import com.tjpu.sp.model.envhousekeepers.dataconnection.DataConnectionVO;
import com.tjpu.sp.service.envhousekeepers.checkitemdata.CheckItemDataService;
import com.tjpu.sp.service.envhousekeepers.checktemplateconfig.CheckTemplateConfigService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 *
 * @author: xsm
 * @date: 2021/06/29 0029 上午 9:25
 * @Description: 检查模板配置控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
*/
@RestController
@RequestMapping("checkTemplateConfig")
public class CheckTemplateConfigController {

    @Autowired
    private CheckTemplateConfigService checkTemplateConfigService;
    @Autowired
    private CheckItemDataService checkItemDataService;


    /**
     * @author: xsm
     * @date: 2021/06/29 0029 上午 9:25
     * @Description: 根据自定义参数获取检查模板配置列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @return:
     */
    @RequestMapping(value = "getCheckTemplateConfigsByParamMap", method = RequestMethod.POST)
    public Object getCheckTemplateConfigsByParamMap(@RequestJson(value = "paramsjson", required = false) Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String,Object>> listdata = checkTemplateConfigService.getCheckTemplateConfigsByParamMap(jsonObject);
            if (listdata!=null&&listdata.size()>0) {
                listdata = checkItemDataService.SetCheckTemplateConfigUrlPath(listdata,jsonObject);
            }
            resultMap.put("datalist",listdata);
            resultMap.put("total",listdata.size());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/06/29 0029 上午 9:25
     * @Description: 获取巡查类型
     * @updateUser:xsm
     * @updateDate:2021/07/13 0013 下午 14:27
     * @updateDescription:根据类型标记查政府巡查类型 或企业巡查类型
     * @param: [typeflag]
     * @return:
     */
    @RequestMapping(value = "getAllInspectTypes", method = RequestMethod.POST)
    public Object getAllInspectTypes(@RequestJson(value = "typeflag", required = false) Integer typeflag,
                                     @RequestJson(value = "checktime", required = false) String checktime,
                                     @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                     @RequestJson(value = "issubmit", required = false) Boolean issubmit) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("typeflag",typeflag);
            param.put("checktime",checktime);
            param.put("pollutionid",pollutionid);
            param.put("issubmit",issubmit);
            List<Map<String,Object>> listdata = checkTemplateConfigService.getAllInspectTypes(param);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 9:55
     *@Description: 通过主键id删除检查模板配置单条数据
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [id]
     *@throws:
     **/
    @RequestMapping(value = "deleteCheckTemplateConfigById",method = RequestMethod.POST)
    public Object deleteCheckTemplateConfigById(@RequestJson(value = "id",required = true) String id){
        try {
            checkTemplateConfigService.deleteCheckTemplateConfigById(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:56
     *@Description: 添加检查模板配置
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [addformdata]
     *@throws:
     **/
    @RequestMapping(value = "addCheckTemplateConfig",method = RequestMethod.POST)
    public Object addCheckTemplateConfig(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<JSONObject> connectiondata = jsonObject.getJSONArray("connectiondata");
            List<String> questionscommons  = (List<String>) jsonObject.get("questionscommons");
            List<String> explaincommons = (List<String>) jsonObject.get("explaincommons");
            CheckTemplateConfigVO obj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new CheckTemplateConfigVO());
            obj.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            obj.setUpdateuser(username);
            String pkid = UUID.randomUUID().toString();
            obj.setPkId(pkid);
            //问题常用
            if (questionscommons!=null&&questionscommons.size()>0){
                String questionscommon="";
                for (String str:questionscommons){
                    questionscommon =questionscommon+str+"#";
                }
                if (!"".equals(questionscommon)){
                    questionscommon = questionscommon.substring(0,questionscommon.length()-1);
                    obj.setQuestionscommon(questionscommon);
                }
            }
            //检查说明常用
            if (explaincommons!=null&&explaincommons.size()>0){
                String explaincommon="";
                for (String str:explaincommons){
                    explaincommon =explaincommon+str+"#";
                }
                if (!"".equals(explaincommon)){
                    explaincommon = explaincommon.substring(0,explaincommon.length()-1);
                    obj.setExplaincommon(explaincommon);
                }
            }

            List<DataConnectionVO> listobj = new ArrayList<>();
            //添加检查项数据
            int i=1;
            if (connectiondata!=null&&connectiondata.size()>0){
                for (JSONObject json:connectiondata){
                    DataConnectionVO objjson = JSONObjectUtil.JsonObjectToEntity(json, new DataConnectionVO());
                    String  id = UUID.randomUUID().toString();
                    objjson.setPkId(id);
                    objjson.setUpdatetime(new Date());
                    objjson.setUpdateuser(username);
                    objjson.setOrderindex(i);
                    objjson.setFkChecktemplateconfigid(pkid);
                    listobj.add(objjson);
                    i++;
                }
            }
            checkTemplateConfigService.insert(obj,listobj);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:56
     *@Description: 编辑检查模板配置
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [updateformdata]
     *@throws:
     **/
    @RequestMapping(value = "updateCheckTemplateConfig",method = RequestMethod.POST)
    public Object updateCheckTemplateConfig(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            List<JSONObject> connectiondata = jsonObject.getJSONArray("connectiondata");
            List<String> questionscommons  = (List<String>) jsonObject.get("questionscommons");
            List<String> explaincommons = (List<String>) jsonObject.get("explaincommons");
            CheckTemplateConfigVO obj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new CheckTemplateConfigVO());
            obj.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            obj.setUpdateuser(username);
            //问题常用
            if (questionscommons!=null&&questionscommons.size()>0){
                String questionscommon="";
                for (String str:questionscommons){
                    questionscommon =questionscommon+str+"#";
                }
                if (!"".equals(questionscommon)){
                    questionscommon = questionscommon.substring(0,questionscommon.length()-1);
                    obj.setQuestionscommon(questionscommon);
                }
            }
            //检查说明常用
            if (explaincommons!=null&&explaincommons.size()>0){
                String explaincommon="";
                for (String str:explaincommons){
                    explaincommon =explaincommon+str+"#";
                }
                if (!"".equals(explaincommon)){
                    explaincommon = explaincommon.substring(0,explaincommon.length()-1);
                    obj.setExplaincommon(explaincommon);
                }
            }
            List<DataConnectionVO> listobj = new ArrayList<>();
            //添加检查项数据
            int i=1;
            if (connectiondata!=null&&connectiondata.size()>0){
                for (JSONObject json:connectiondata){
                    DataConnectionVO objjson = JSONObjectUtil.JsonObjectToEntity(json, new DataConnectionVO());
                    String  id = UUID.randomUUID().toString();
                    objjson.setPkId(id);
                    objjson.setUpdatetime(new Date());
                    objjson.setUpdateuser(username);
                    objjson.setFkChecktemplateconfigid(obj.getPkId());
                    objjson.setOrderindex(i);
                    listobj.add(objjson);
                    i++;
                }
            }
            checkTemplateConfigService.updateByPrimaryKey(obj,listobj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/01 0001 下午 4:09
     * @Description: 验证检查项目是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsValidForValueByParam", method = RequestMethod.POST)
    public Object IsValidForValueByParam(
            @RequestJson(value = "checktypecode") String checktypecode,
            @RequestJson(value = "checkcategory") String checkcategory,
            @RequestJson(value = "checkcontent", required = false) String checkcontent
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("checktypecode", checktypecode);
            paramMap.put("checkcategory", checkcategory);
            paramMap.put("checkcontent", checkcontent);
            List<Map<String, Object>> datalist = checkTemplateConfigService.IsValidForValueByParam(paramMap);
            String flag = "no";
            if (datalist != null&&datalist.size()>0) {    //不等于空，表示重复不可以添加
                flag = "yes";
            }
            return AuthUtil.parseJsonKeyToLower("success", flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/01 0001 下午 4:09
     * @Description: 验证检查项目的检查情况历史记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsHasCheckTemplateConfigHistoryData", method = RequestMethod.POST)
    public Object IsHasCheckTemplateConfigHistoryData(
            @RequestJson(value = "checktemplateconfigid") String checktemplateconfigid
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("checktemplateconfigid", checktemplateconfigid);
            List<Map<String, Object>> datalist = checkTemplateConfigService.IsHasCheckTemplateConfigHistoryData(paramMap);
            String flag = "no";
            if (datalist != null&&datalist.size()>0) {    //不等于空，表示重复不可以添加
                flag = "yes";
            }
            return AuthUtil.parseJsonKeyToLower("success", flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/05 0005 上午 12:05
     * @Description: 生成检查项目模板文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "createCheckTemplateConfigReport", method = RequestMethod.POST)
    public void createCheckTemplateConfigReport(
            @RequestJson("checktypecode") String checktypecode,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> resultData = new HashMap<>();
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("checktypecode",checktypecode);
            List<Map<String,Object>> listdata = checkTemplateConfigService.getAllInspectTypes(parammap);
            List<Map<String,Object>> data = checkTemplateConfigService.getCheckTemplateConfigsByParamMap(parammap);
            String start = "<w:vmerge w:val=\"restart\"/>";
            String continuestr = "<w:vmerge w:val=\"continue\"/>";
            String end = "<w:vMerge/>";
            if (data!=null&&data.size()>0){
                String firstvalue ="";
                String firstBasisItem = "";
                int n  =0;
                int m  =0;
                for (int i =0;i<data.size();i++){
                    Map<String,Object> map = data.get(i);
                    if (i == 0){
                        map.put("start",start);
                        map.put("basisitemstart",start);
                    }else{
                        if (map.get("CheckCategoryName")!=null&&!"".equals(firstvalue)&&!firstvalue.equals(map.get("CheckCategoryName"))){
                            map.put("start",start);
                            if (n>1){
                                data.get(i-1).put("end",end);
                            }
                            n=0;
                            //当类别不同时 设置新的合并单元格初始
                            map.put("basisitemstart",start);
                            m=0;
                        }else{
                            map.put("continuestr",continuestr);
                            n+=1;
                            //判断依据
                            if (map.get("BasisItem")!=null&&!"".equals(firstBasisItem)&&!firstBasisItem.equals(map.get("BasisItem"))){
                                map.put("basisitemstart",start);
                                if (m>1){
                                    data.get(i-1).put("basisitemend",end);
                                }
                                m=0;
                            }else{
                                map.put("basisitemcontinue",continuestr);
                                m+=1;
                            }
                        }

                    }
                    firstvalue = map.get("CheckCategoryName")!=null?map.get("CheckCategoryName").toString():"";
                    firstBasisItem = map.get("BasisItem")!=null?map.get("BasisItem").toString():"";
                    if (map.get("CheckSituation")!=null&&map.get("CheckSituationType")!=null){
                        String checksituationtype = map.get("CheckSituationType").toString();
                        String checksituation = map.get("CheckSituation").toString();
                        if ("checkbox".equals(checksituationtype)){
                            checksituation = checksituation.replace("#"," □");
                            map.put("CheckSituation","□"+checksituation);
                        }
                    }
                }
            }
            String titlename ="";
            if (listdata!=null&&listdata.size()>0){
                for (Map<String,Object> map:listdata){
                    if (map.get("code")!=null&&checktypecode.equals(map.get("code").toString())) {
                        titlename = map.get("TableTitle") != null ? map.get("TableTitle").toString() : "";
                        break;
                    }
                }
            }
            resultData.put("tabletitle",titlename);
            resultData.put("list", data);
            //文件名称
            String fileName = titlename+"模板_" + new Date().getTime() + ".doc";
            byte[] fileBytes = FreeMarkerWordUtil.createWord(resultData, "templates/环保手续及管理制度.ftl");
            ExcelUtil.downLoadFile(fileName, response, request, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/30 0030 下午 4:57
     * @Description: 根据检查类型ID 获取该类型下的所有检查类别
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllCheckCategoryDataByInspectTypeID", method = RequestMethod.POST)
    public Object getAllCheckCategoryDataByInspectTypeID(@RequestJson(value = "inspecttypeid", required = false) String inspecttypeid,
                                                         @RequestJson(value = "ismanually", required = false) Integer ismanually) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("inspecttypeid",inspecttypeid);
            param.put("ismanually",ismanually);
            List<Map<String,Object>> listdata = checkTemplateConfigService.getAllCheckCategoryDataByInspectTypeID(param);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/03 0003 下午 15:53
     * @Description: 根据检查类别ID获取该检查类别下的检查内容信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getCheckContentDataByCheckCategoryID", method = RequestMethod.POST)
    public Object getCheckContentDataByCheckCategoryID(
            @RequestJson(value = "checkcategoryid") String checkcategoryid
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("checkcategoryid", checkcategoryid);
            List<Map<String, Object>> datalist = checkTemplateConfigService.getCheckContentDataByCheckCategoryID(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/30 0030 上午 9:22
     * @Description: 根据企业ID和检查类型获取企业的检查项、检查内容配置
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getEntCheckItemConfigDataByParam", method = RequestMethod.POST)
    public Object getEntCheckItemConfigDataByParam(
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "checktypecode") String checktypecode
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("checktypecode", checktypecode);
            List<Map<String, Object>> listdata = checkTemplateConfigService.getEntCheckItemConfigDataByParam(paramMap);
            if (listdata!=null&&listdata.size()>0) {
                listdata = checkItemDataService.SetCheckTemplateConfigUrlPath(listdata,paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/30 0030 上午 9:22
     * @Description: 根据检查项code 获取所有检查内容
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getCheckItemConfigDataByCheckItemCode", method = RequestMethod.POST)
    public Object getCheckItemConfigDataByCheckItemCode(
            @RequestJson(value = "checkcategory") String checkcategory
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("checkcategory", checkcategory);
            List<Map<String, Object>> listdata = checkTemplateConfigService.getCheckItemConfigDataByCheckItemCode(paramMap);
            if (listdata!=null&&listdata.size()>0) {
                listdata = checkItemDataService.SetCheckTemplateConfigUrlPath(listdata,paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/30 0030 上午 9:22
     * @Description: 根据检查项配置ID和企业ID  删除企业关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/deleteEntCheckItemConfigByParam", method = RequestMethod.POST)
    public Object deleteEntCheckItemConfigByParam(
            @RequestJson(value = "fkchecktemplateconfigid") String fkchecktemplateconfigid,
            @RequestJson(value = "pollutionid") String pollutionid
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fkchecktemplateconfigid", fkchecktemplateconfigid);
            paramMap.put("pollutionid", pollutionid);
            checkTemplateConfigService.deleteEntCheckItemConfigByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/01 0001 下午 4:09
     * @Description: 验证检查项目的检查情况历史记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsHasEntCheckItemConfigHistoryData", method = RequestMethod.POST)
    public Object IsHasEntCheckItemConfigHistoryData(
            @RequestJson(value = "fkchecktemplateconfigid") String fkchecktemplateconfigid,
            @RequestJson(value = "pollutionid") String pollutionid
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("checktemplateconfigid", fkchecktemplateconfigid);
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> datalist = checkTemplateConfigService.IsHasEntCheckItemConfigHistoryData(paramMap);
            String flag = "no";
            if (datalist != null&&datalist.size()>0) {    //不等于空，表示重复不可以添加
                flag = "yes";
            }
            return AuthUtil.parseJsonKeyToLower("success", flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     *@author: xsm
     *@date: 2021/08/30 0030 10:16
     *@Description: 添加企业检查项配置
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [addformdata]
     *@throws:
     **/
    @RequestMapping(value = "addEntCheckTemplateConfig",method = RequestMethod.POST)
    public Object addEntCheckTemplateConfig(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<String> configids  = (List<String>) jsonObject.get("configids");
            String pollutionid = (String) jsonObject.get("pollutionid");
            String checktypecode = (String) jsonObject.get("checktypecode");
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
           List<Map<String,Object>> addlist = new ArrayList<>();
            if (configids!=null&&configids.size()>0){
               for (String id:configids){
                   Map<String,Object> map = new HashMap<>();
                   map.put("pkid",UUID.randomUUID().toString());
                   map.put("fkpollutionid",pollutionid);
                   map.put("fkchecktypecode",checktypecode);
                   map.put("fkchecktemplateconfigid",id);
                   map.put("updateuser",username);
                   map.put("updatetime",new Date());
                   addlist.add(map);
               }
           }
            checkTemplateConfigService.addEntCheckTemplateConfig(addlist,pollutionid,checktypecode);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/05 0005 上午 12:05
     * @Description: 生成企业检查项目模板文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "createEntCheckTemplateConfigReport", method = RequestMethod.POST)
    public void createEntCheckTemplateConfigReport(
            @RequestJson("checktypecode") String checktypecode,
            @RequestJson("pollutionid") String pollutionid,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> resultData = new HashMap<>();
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("checktypecode",checktypecode);
            List<Map<String,Object>> listdata = checkTemplateConfigService.getAllInspectTypes(parammap);
            parammap.put("pollutionid",pollutionid);
            List<Map<String, Object>> data = checkTemplateConfigService.getEntCheckItemConfigDataByParam(parammap);
            String start = "<w:vmerge w:val=\"restart\"/>";
            String continuestr = "<w:vmerge w:val=\"continue\"/>";
            String end = "<w:vMerge/>";
            if (data!=null&&data.size()>0){
                String firstvalue ="";
                String firstBasisItem = "";
                int n  =0;
                int m  =0;
                for (int i =0;i<data.size();i++){
                    Map<String,Object> map = data.get(i);
                    map.put("OrderIndex",i+1);
                    if (i == 0){
                        map.put("start",start);
                        map.put("basisitemstart",start);
                    }else{
                        if (map.get("CheckCategoryName")!=null&&!"".equals(firstvalue)&&!firstvalue.equals(map.get("CheckCategoryName"))){
                            map.put("start",start);
                            if (n>1){
                                data.get(i-1).put("end",end);
                            }
                            n=0;
                            //当类别不同时 设置新的合并单元格初始
                            map.put("basisitemstart",start);
                            m=0;
                        }else{
                            map.put("continuestr",continuestr);
                            n+=1;
                            //判断依据
                            if (map.get("BasisItem")!=null&&!"".equals(firstBasisItem)&&!firstBasisItem.equals(map.get("BasisItem"))){
                                map.put("basisitemstart",start);
                                if (m>1){
                                    data.get(i-1).put("basisitemend",end);
                                }
                                m=0;
                            }else{
                                map.put("basisitemcontinue",continuestr);
                                m+=1;
                            }
                        }

                    }
                    firstvalue = map.get("CheckCategoryName")!=null?map.get("CheckCategoryName").toString():"";
                    firstBasisItem = map.get("BasisItem")!=null?map.get("BasisItem").toString():"";
                    if (map.get("CheckSituation")!=null&&map.get("CheckSituationType")!=null){
                        String checksituationtype = map.get("CheckSituationType").toString();
                        String checksituation = map.get("CheckSituation").toString();
                        if ("checkbox".equals(checksituationtype)){
                            checksituation = checksituation.replace("#"," □");
                            map.put("CheckSituation","□"+checksituation);
                        }
                    }
                }
            }
            String titlename ="";
            if (listdata!=null&&listdata.size()>0){
                for (Map<String,Object> map:listdata){
                    if (map.get("code")!=null&&checktypecode.equals(map.get("code").toString())) {
                        titlename = map.get("TableTitle") != null ? map.get("TableTitle").toString() : "";
                        break;
                    }
                }
            }
            resultData.put("tabletitle",titlename);
            resultData.put("list", data);
            //文件名称
            String fileName = titlename+"模板_" + new Date().getTime() + ".doc";
            byte[] fileBytes = FreeMarkerWordUtil.createWord(resultData, "templates/环保手续及管理制度.ftl");
            ExcelUtil.downLoadFile(fileName, response, request, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

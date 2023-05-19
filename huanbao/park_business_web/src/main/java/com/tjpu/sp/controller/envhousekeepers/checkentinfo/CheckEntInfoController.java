package com.tjpu.sp.controller.envhousekeepers.checkentinfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.envhousekeepers.checkcontentdescription.CheckContentDescriptionVO;
import com.tjpu.sp.model.envhousekeepers.checkentinfo.CheckEntInfoVO;
import com.tjpu.sp.model.envhousekeepers.checkentinfo.EntCheckFeedbackRecordVO;
import com.tjpu.sp.model.envhousekeepers.checkitemdata.CheckItemDataVO;
import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.CheckProblemExpoundVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.envhousekeepers.checkentinfo.CheckEntInfoService;
import com.tjpu.sp.service.envhousekeepers.problemconsult.EntProblemConsultRecordService;
import com.tjpu.sp.service.environmentalprotection.notice.NoticeService;
import com.tjpu.sp.service.environmentalprotection.pointofflinerecord.PointOffLineRecordService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xsm
 * @date: 2021/06/29 0029 下午 13:12
 * @Description: 检查企业信息控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("checkEntInfo")
public class CheckEntInfoController {

    @Autowired
    private CheckEntInfoService checkEntInfoService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private EntProblemConsultRecordService entProblemConsultRecordService;
    @Autowired
    private PointOffLineRecordService pointOffLineRecordService;
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;


    /**
     * @author: xsm
     * @date: 2021/06/29 0029 13:24
     * @Description: 添加检查企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     **/
    @RequestMapping(value = "addCheckEntInfo", method = RequestMethod.POST)
    public Object addCheckEntInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            JSONObject entcheckdata = jsonObject.getJSONObject("entcheckdata");
            List<JSONObject> checkitemdata = jsonObject.getJSONArray("checkitemdata");
            //检查企业信息
            CheckEntInfoVO obj = JSONObjectUtil.JsonObjectToEntity(entcheckdata, new CheckEntInfoVO());
            obj.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            obj.setUpdateuser(username);
            String checkentid = UUID.randomUUID().toString();
            obj.setPkId(checkentid);
            obj.setStatus(0);
            List<CheckItemDataVO> listobj = new ArrayList<>();
            List<CheckProblemExpoundVO> remarkobjs = new ArrayList<>();
            List<CheckContentDescriptionVO> contentobjs = new ArrayList<>();
            //添加检查项数据
            if (checkitemdata != null && checkitemdata.size() > 0) {
                for (JSONObject json : checkitemdata) {
                    List<JSONObject> remarkdata = json.getJSONArray("remarkdata");
                    CheckItemDataVO objjson = JSONObjectUtil.JsonObjectToEntity(json, new CheckItemDataVO());
                    String checkitemid = UUID.randomUUID().toString();
                    objjson.setFkCheckentid(checkentid);
                    objjson.setUpdatetime(new Date());
                    objjson.setUpdateuser(username);
                    objjson.setPkId(checkitemid);
                    listobj.add(objjson);
                    //添加检查问题数据
                    if (remarkdata != null && remarkdata.size() > 0) {
                        int i = 0;
                        for (JSONObject onejson : remarkdata) {
                            CheckProblemExpoundVO oneobj = JSONObjectUtil.JsonObjectToEntity(onejson, new CheckProblemExpoundVO());
                            oneobj.setFkCheckitemdataid(checkitemid);
                            oneobj.setPkId(UUID.randomUUID().toString());
                            oneobj.setStatus((short) 0);
                            oneobj.setUpdatetime(new Date());
                            oneobj.setUpdateuser(username);
                            oneobj.setOrderindex(i);
                            //新加字段
                            oneobj.setFkpollutionid(obj.getFkPollutionid());
                            //oneobj.setFkproblemsourcecode("1");
                            oneobj.setChecktime(obj.getChecktime());
                            remarkobjs.add(oneobj);
                            i++;
                        }
                    }
                    //检查项目配置
                    List<JSONObject> contentdata = json.getJSONArray("contentdata");
                    if (contentdata != null && contentdata.size() > 0) {
                        int j = 0;
                        for (JSONObject onejson : contentdata) {
                            CheckContentDescriptionVO oneobj = JSONObjectUtil.JsonObjectToEntity(onejson, new CheckContentDescriptionVO());
                            oneobj.setFkCheckitemdataid(checkitemid);
                            oneobj.setPkId(UUID.randomUUID().toString());
                            oneobj.setUpdatetime(new Date());
                            oneobj.setUpdateuser(username);
                            oneobj.setOrderindex(j);
                            contentobjs.add(oneobj);
                            j++;
                        }
                    }
                }
            }
            checkEntInfoService.insert(obj, listobj, remarkobjs, contentobjs);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/06/30 0030 11:44
     * @Description: 修改检查企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     **/
    @RequestMapping(value = "updateCheckItemData", method = RequestMethod.POST)
    public Object updateCheckItemData(@RequestJson(value = "updateformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            JSONObject entcheckdata = jsonObject.getJSONObject("entcheckdata");
            List<JSONObject> checkitemdata = jsonObject.getJSONArray("checkitemdata");
            //检查企业信息
            CheckEntInfoVO obj = JSONObjectUtil.JsonObjectToEntity(entcheckdata, new CheckEntInfoVO());
            String checkentid = "";
            if (obj.getPkId() == null) {
                checkentid = UUID.randomUUID().toString();
                obj.setPkId(checkentid);
            } else {
                checkentid = obj.getPkId();
            }
            obj.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            obj.setUpdateuser(username);
            List<CheckItemDataVO> listobj = new ArrayList<>();
            List<CheckProblemExpoundVO> remarkobjs = new ArrayList<>();
            List<CheckContentDescriptionVO> contentobjs = new ArrayList<>();
            //添加检查项数据
            if (checkitemdata != null && checkitemdata.size() > 0) {
                for (JSONObject json : checkitemdata) {
                    List<JSONObject> remarkdata = json.getJSONArray("remarkdata");
                    CheckItemDataVO objjson = JSONObjectUtil.JsonObjectToEntity(json, new CheckItemDataVO());
                    objjson.setFkCheckentid(checkentid);
                    objjson.setUpdatetime(new Date());
                    objjson.setUpdateuser(username);
                    String checkitemid = UUID.randomUUID().toString();
                    objjson.setFkCheckentid(checkentid);
                    objjson.setPkId(checkitemid);
                    listobj.add(objjson);
                    if (remarkdata != null && remarkdata.size() > 0) {
                        int i = 0;
                        for (JSONObject onejson : remarkdata) {
                            CheckProblemExpoundVO oneobj = JSONObjectUtil.JsonObjectToEntity(onejson, new CheckProblemExpoundVO());
                            oneobj.setFkCheckitemdataid(checkitemid);
                            oneobj.setPkId(UUID.randomUUID().toString());
                            oneobj.setStatus((short) 0);
                            oneobj.setUpdatetime(new Date());
                            oneobj.setUpdateuser(username);
                            oneobj.setOrderindex(i);
                            //新加字段
                            oneobj.setFkpollutionid(obj.getFkPollutionid());
                            //oneobj.setFkproblemsourcecode("1");
                            oneobj.setChecktime(obj.getChecktime());
                            remarkobjs.add(oneobj);
                            i++;
                        }
                    }
                    //检查项目配置
                    List<JSONObject> contentdata = json.getJSONArray("contentdata");
                    if (contentdata != null && contentdata.size() > 0) {
                        int j = 0;
                        for (JSONObject onejson : contentdata) {
                            CheckContentDescriptionVO oneobj = JSONObjectUtil.JsonObjectToEntity(onejson, new CheckContentDescriptionVO());
                            oneobj.setFkCheckitemdataid(checkitemid);
                            oneobj.setPkId(UUID.randomUUID().toString());
                            oneobj.setUpdatetime(new Date());
                            oneobj.setUpdateuser(username);
                            oneobj.setOrderindex(j);
                            contentobjs.add(oneobj);
                            j++;
                        }
                    }
                }
            }
            checkEntInfoService.updateCheckItemData(obj, listobj, remarkobjs, contentobjs);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/6/29 0029 下午 14:24
     * @Description: 获取企业下拉框信息和企业地址
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getEntInfoAndEntAddressData", method = RequestMethod.POST)
    public Object getEntInfoAndEntAddressData() throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> maplist = pollutionService.getPollutionInfoByParamMaps(new HashMap<>());
            if (maplist != null && maplist.size() > 0) {
                for (Map<String, Object> map : maplist) {
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("id", map.get("PK_PollutionID"));
                    onemap.put("label", map.get("PollutionName"));
                    onemap.put("address", map.get("Address"));
                    onemap.put("environmentalmanager", map.get("EnvironmentalManager"));
                    onemap.put("linkmanphone", map.get("LinkManPhone"));
                    result.add(onemap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: mmt
     * @date: 2022/08/17
     * @Description: 检查企业列表
     * @updateUser:mmt
     * @updateDate:2022/08/17
     * @return:
     */
    @RequestMapping(value = "getCheckEntInfoList", method = RequestMethod.POST)
    public Object getCheckEntInfoList(
            @RequestJson(value = "pollutionname", required = false) String pollutionname,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "checkperson", required = false) String checkPersonParam,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("checkperson", checkPersonParam);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);

            List userauth = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            boolean isShow = false;//是否有显示数据的权限
            for (Object o : userauth) {
                JSONArray childrenList;
                JSONObject jsonObject;
                jsonObject = JSONObject.fromObject(o);
                if ("hb2App".equals(jsonObject.getString("menucode"))) {
                    childrenList = jsonObject.getJSONArray("datalistchildren");
                    for (Object item : childrenList) {
                        jsonObject = JSONObject.fromObject(item);
                        if ("hb2_App_Check".equals(jsonObject.getString("menucode"))) {
                            childrenList = jsonObject.getJSONArray("datalistchildren");
                            for (Object data : childrenList) {
                                jsonObject = JSONObject.fromObject(data);
                                if ("hb2_App_ZSSY".equals(jsonObject.get("menucode"))) {//展示所有数据
                                    paramMap.remove("userid");
                                    isShow = true;
                                } else if ("hb2_App_ZSGR".equals(jsonObject.get("menucode"))) {//只展示个人数据
                                    isShow = true;
                                }
                            }
                        }
                    }
                    break;
                }
            }
            if (isShow) {
                List<Map<String, Object>> allCheckEntInfoList = checkEntInfoService.getAllCheckEntInfoList(paramMap);
                return AuthUtil.parseJsonKeyToLower("success", allCheckEntInfoList);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/06/29 0029 下午 15:37
     * @Description: 根据数据类型获取检查企业树
     * @updateUser:xsm
     * @updateDate:2021/07/15 0015 上午 08:54
     * @updateDescription:添加小红点标记表示是否有提交，各类型表有一个表未提交，该企业当天的检查状态就是未提交状态
     * @param:[datatype:["pollution":按企业名称分组，"date":按日期分组]，checktype："1":表示监督检查，"2"：表示企业自查]
     * @return:
     */
    @RequestMapping(value = "getCheckEntInfoTreeDataByDataType", method = RequestMethod.POST)
    public Object getCheckEntInfoTreeDataByDataType(@RequestJson(value = "datatype") String datatype,
                                                    @RequestJson(value = "checktype", required = false) Integer checktype,
                                                    @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                    @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                    @RequestJson(value = "checkperson", required = false) String checkPersonParam,
                                                    @RequestJson(value = "starttime", required = false) String starttime,
                                                    @RequestJson(value = "endtime", required = false) String endtime
    ) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("datatypeflag", datatype);
            param.put("pollutionname", pollutionname);
            param.put("pollutionid", pollutionid);
            param.put("checktype", checktype);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("checkperson", checkPersonParam);
            if (checktype != null && checktype == 2) {
                String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                param.put("userid", userId);
                param.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode());
            }
            List<Map<String, Object>> datalist = checkEntInfoService.getAllCheckEntInfoGroupByEntAndData(param);
            //部门用户组合下拉树
            List<Map<String, Object>> resultList = new ArrayList<>();
            String childkey = "";
            if (datalist != null && datalist.size() > 0) {
                Map<String, List<Map<String, Object>>> listmap = new HashMap<>();
                List<String> keysets = new ArrayList<>();
                for (Map<String, Object> map : datalist) {
                    String key = "";
                    if ("pollution".equals(datatype)) {
                        key = map.get("PollutionName") != null ? map.get("PollutionName").toString() : "";
                    } else if ("date".equals(datatype)) {
                        key = map.get("CheckTime") != null ? map.get("CheckTime").toString() : "";
                    }
                    if (!"".equals(key) && !keysets.contains(key)) {
                        keysets.add(key);
                    }
                }
                if ("pollution".equals(datatype)) {
                    //通过污染源ID分组数据
                    listmap = datalist.stream().collect(Collectors.groupingBy(m -> m.get("PollutionName").toString()));
                    childkey = "CheckTime";
                } else if ("date".equals(datatype)) {
                    //通过日期分组数据
                    listmap = datalist.stream().collect(Collectors.groupingBy(m -> m.get("CheckTime").toString()));
                    childkey = "PollutionName";
                }
                for (String key : keysets) {
                    List<Map<String, Object>> list = listmap.get(key);
                    Map<String, Object> parentmap = new HashMap<>();
                    parentmap.put("id", UUID.randomUUID().toString());
                    /*if ("pollution".equals(datatype)) {
                        parentmap.put("id",(list!=null&&list.size()>0)?list.get(0).get("FK_PollutionID"):key);
                    } else if ("date".equals(datatype)) {
                        parentmap.put("id",key);
                    }*/
                    parentmap.put("label", key);
                    parentmap.put("type", "parent");
                    List<Map<String, Object>> childlist = new ArrayList<>();
                    Object isfocuson = null;
                    for (Map<String, Object> onemap : list) {
                        Map<String, Object> childmap = new HashMap<>();
                        childmap.put("id", onemap.get("FK_PollutionID") + "_" + onemap.get("CheckTime"));
                        childmap.put("label", onemap.get(childkey));
                        childmap.put("status", onemap.get("status"));
                        childmap.put("pollutionid", onemap.get("FK_PollutionID"));
                        childmap.put("date", onemap.get("CheckTime"));
                        childmap.put("type", "child");
                        if (checktype != null && checktype == 2) {
                            childmap.put("feedbackid", onemap.get("feedbackid"));
                            //是否反馈
                            childmap.put("isfeedback", onemap.get("isfeedback"));
                            //已读未读
                            childmap.put("isread", onemap.get("isread"));
                        }
                        if ("date".equals(datatype)) {
                            childmap.put("isfocuson", onemap.get("isfocuson"));
                        }
                        if (isfocuson == null) {
                            isfocuson = onemap.get("isfocuson");
                        }
                        childlist.add(childmap);
                    }
                    if ("pollution".equals(datatype)) {
                        parentmap.put("isfocuson", isfocuson);
                    }
                    parentmap.put("children", childlist);
                    resultList.add(parentmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/01 0001 下午 4:09
     * @Description: 验证检查记录是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsCheckEntInfoValidByParam", method = RequestMethod.POST)
    public Object IsCheckEntInfoValidByParam(
            @RequestJson(value = "checktypecode") String checktypecode,
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "checktime", required = false) String checktime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("checktypecode", checktypecode);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("checktime", checktime);
            List<Map<String, Object>> datalist = checkEntInfoService.IsCheckEntInfoValidByParam(paramMap);
            String flag = "no";
            if (datalist != null && datalist.size() > 0) {    //不等于空，表示重复 不可以添加
                flag = "yes";
            }
            return AuthUtil.parseJsonKeyToLower("success", flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取单个企业待提交列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/8/31 14:36
     */
    @RequestMapping(value = "getSubmitListData", method = RequestMethod.POST)
    public Object getSubmitListData(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = checkEntInfoService.getSubmitListData(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/30 0030 14:47
     * @Description: 添加企业检查反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     **/
    @RequestMapping(value = "addEntCheckFeedbackRecord", method = RequestMethod.POST)
    public Object addEntCheckFeedbackRecord(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            //企业检查反馈信息
            EntCheckFeedbackRecordVO obj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntCheckFeedbackRecordVO());
            obj.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            obj.setUpdateuser(username);
            String checkentid = UUID.randomUUID().toString();
            obj.setIsupdate((short) 0);
            obj.setPkId(checkentid);
            obj.setFeedbacktime(new Date());
            obj.setFeedbackuser(username);
            int i = checkEntInfoService.insertEntCheckFeedbackRecord(obj);
            if (i > 0) {
                //推送到企业端首页
                String messageType = CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode();
                //根据企业ID获取企业关联的企业用户ID
                List<String> userids = pollutionService.getUserInfoByPollution(obj.getFkPollutionid());
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("userids", userids);
                jsonobj.put("pkid", obj.getPkId());
                jsonobj.put("pollutionname", jsonObject.get("pollutionname"));
                jsonobj.put("Pollutionid", obj.getFkPollutionid());
                jsonobj.put("fkpollutionid", obj.getFkPollutionid());
                jsonobj.put("checktime", DataFormatUtil.getDateYMD(obj.getChecktime()));
                jsonobj.put("updatetime", obj.getFeedbacktime() != null ? DataFormatUtil.getDateYMDHMS(obj.getFeedbacktime()) : DataFormatUtil.parseDateYMDHMS(new Date()));
                String str = "";
                if (obj.getChecktime() != null) {
                    str = "您有一条检查日期为【" + DataFormatUtil.getDateYMD(obj.getChecktime()) + "】的企业巡查记录反馈信息！";
                } else {
                    str = "您有一条企业巡查反馈信息！";
                }
                jsonobj.put("messagestr", str);
                jsonobj.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode());
                jsonobj.put("isread", "0");
                rabbitmqController.sendEntCheckFeedbackInfo(jsonobj, messageType);
            }

            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/30 0030 下午 5:45
     * @Description: 通过id获取企业检查反馈详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntCheckFeedbackRecordDetailByParam", method = RequestMethod.POST)
    public Object getEntCheckFeedbackRecordDetailByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                         @RequestJson(value = "checkdate") String checkdate) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid", pollutionid);
            param.put("checkdate", checkdate);
            Map<String, Object> result = checkEntInfoService.getEntCheckFeedbackRecordDetailByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/31 0031 上午 9:22
     * @Description: 获取该企业的所有未读的反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getEntCheckFeedbackRecordDataByParam", method = RequestMethod.POST)
    public Object getEntCheckFeedbackRecordDataByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                       @RequestJson(value = "messagetype") String messagetype
    ) throws Exception {
        try {
            Map<String, Object> parammap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            parammap.put("pollutionid", pollutionid);
            parammap.put("userid", userid);
            if (messagetype.equals(CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode())) {
                parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode());
                List<Map<String, Object>> datalist = checkEntInfoService.getEntCheckFeedbackRecordDataByParam(parammap);
                if (datalist.size() > 0) {
                    for (Map<String, Object> map : datalist) {
                        if (map.get("CheckTime") != null) {
                            map.put("messagestr", "您有一条检查日期为【" + map.get("CheckTime") + "】的企业巡查记录反馈信息！");
                        } else {
                            map.put("messagestr", "您有一条企业巡查反馈信息！");
                        }
                        result.add(map);
                    }
                }
            } else if (messagetype.equals(CommonTypeEnum.HomePageMessageTypeEnum.ProblemReplyMessage.getCode())) {
                parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.ProblemReplyMessage.getCode());
                result = entProblemConsultRecordService.getNoReadProblemConsultRecordByParam(parammap);
            } else if (messagetype.equals(CommonTypeEnum.HomePageMessageTypeEnum.NoticeMessage.getCode())) {
                result = noticeService.getNoReadNoticeDataByParam(parammap);
            } else if (messagetype.equals(CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode())) {
                //点位离线
                parammap.put("isread", "0");
                parammap.put("isentflag", "yes");
                parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode());
                result = pointOffLineRecordService.getEntPointOffLineRecordsByParamMap(parammap);
            }
            if (result != null && result.size() > 0) {
                Comparator<Object> comparebyisread = Comparator.comparing(m -> ((Map) m).get("isread").toString());
                Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("UpdateTime").toString()).reversed();
                Comparator<Object> finalComparator = comparebyisread.thenComparing(comparebytime);
                List<Map<String, Object>> collect = result.stream().sorted(finalComparator).collect(Collectors.toList());
                resultMap.put("datalist", collect);
            } else {
                resultMap.put("datalist", result);
            }
            resultMap.put("messagetype", messagetype);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/06/29 0029 下午 15:37
     * @Description: 根据数据类型获取检查企业树
     * @updateUser:xsm
     * @return:
     */
    @RequestMapping(value = "getEntCheckFeedbackTreeDataByParam", method = RequestMethod.POST)
    public Object getEntCheckFeedbackTreeDataByParam(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                     @RequestJson(value = "starttime", required = false) String starttime,
                                                     @RequestJson(value = "endtime", required = false) String endtime
    ) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid", pollutionid);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("userid", userId);
            param.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode());
            List<Map<String, Object>> datalist = checkEntInfoService.getEntCheckFeedbackTreeDataByParam(param);
            //部门用户组合下拉树
            List<Map<String, Object>> resultList = new ArrayList<>();
            String childkey = "";
            if (datalist != null && datalist.size() > 0) {
                Map<String, List<Map<String, Object>>> listmap = new HashMap<>();
                List<String> keysets = new ArrayList<>();
                for (Map<String, Object> map : datalist) {
                    String key = "";
                    key = map.get("PollutionName") != null ? map.get("PollutionName").toString() : "";
                    if (!"".equals(key) && !keysets.contains(key)) {
                        keysets.add(key);
                    }
                }
                //通过污染源ID分组数据
                listmap = datalist.stream().collect(Collectors.groupingBy(m -> m.get("PollutionName").toString()));
                childkey = "CheckTime";
                for (String key : keysets) {
                    List<Map<String, Object>> list = listmap.get(key);
                    Map<String, Object> parentmap = new HashMap<>();
                    parentmap.put("id", UUID.randomUUID().toString());
                    parentmap.put("label", key);
                    parentmap.put("type", "parent");
                    List<Map<String, Object>> childlist = new ArrayList<>();
                    Object isfocuson = null;
                    for (Map<String, Object> onemap : list) {
                        Map<String, Object> childmap = new HashMap<>();
                        childmap.put("id", onemap.get("FK_PollutionID") + "_" + onemap.get("CheckTime"));
                        childmap.put("label", onemap.get(childkey));
                        childmap.put("status", onemap.get("status"));
                        childmap.put("pollutionid", onemap.get("FK_PollutionID"));
                        childmap.put("date", onemap.get("CheckTime"));
                        childmap.put("type", "child");
                        childmap.put("feedbackid", onemap.get("feedbackid"));
                        //是否反馈
                        childmap.put("isfeedback", onemap.get("isfeedback"));
                        //已读未读
                        childmap.put("isread", onemap.get("isread"));
                        //是否能修改反馈信息
                        childmap.put("isupdate", onemap.get("isupdate"));
                        if (isfocuson == null) {
                            isfocuson = onemap.get("isfocuson");
                        }
                        //判断 是否有提及一个类型的表
                        if (onemap.get("status") != null && Integer.valueOf(onemap.get("status").toString()) > 0 && Integer.valueOf(onemap.get("status").toString()) < 3) {
                            childlist.add(childmap);
                        }
                    }
                    parentmap.put("isfocuson", isfocuson);
                    parentmap.put("children", childlist);
                    if (childlist.size() > 0) {
                        resultList.add(parentmap);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/30 0030 14:47
     * @Description: 修改企业检查反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     **/
    @RequestMapping(value = "updateEntCheckFeedbackRecord", method = RequestMethod.POST)
    public Object updateEntCheckFeedbackRecord(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            //企业检查反馈信息
            EntCheckFeedbackRecordVO obj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntCheckFeedbackRecordVO());
            obj.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            obj.setUpdateuser(username);
            obj.setFeedbacktime(new Date());
            obj.setFeedbackuser(username);
            obj.setIsupdate((short) 0);
            int i = checkEntInfoService.updateEntCheckFeedbackRecord(obj);
            if (i > 0) {
                //推送到企业端首页
                String messageType = CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode();
                //根据企业ID获取企业关联的企业用户ID
                List<String> userids = pollutionService.getUserInfoByPollution(obj.getFkPollutionid());
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("userids", userids);
                jsonobj.put("pkid", obj.getPkId());
                jsonobj.put("pollutionname", jsonObject.get("pollutionname"));
                jsonobj.put("Pollutionid", obj.getFkPollutionid());
                jsonobj.put("fkpollutionid", obj.getFkPollutionid());
                jsonobj.put("checktime", jsonObject.get("checktime"));
                jsonobj.put("updatetime", obj.getFeedbacktime() != null ? DataFormatUtil.getDateYMDHMS(obj.getFeedbacktime()) : DataFormatUtil.parseDateYMDHMS(new Date()));
                String str = "";
                if (obj.getChecktime() != null) {
                    str = "您有一条检查日期为【" + jsonObject.get("checktime") + "】的企业巡查记录反馈信息！";
                } else {
                    str = "您有一条企业巡查反馈信息！";
                }
                jsonobj.put("messagestr", str);
                jsonobj.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode());
                jsonobj.put("isread", "0");
                rabbitmqController.sendEntCheckFeedbackInfo(jsonobj, messageType);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取待反馈信息列表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/8 11:24
     */
    @RequestMapping(value = "getToBeCheckFeedbackDataList", method = RequestMethod.POST)
    public Object getToBeCheckFeedbackDataList(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                               @RequestJson(value = "pagenum", required = false) Integer pagenum

    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            Map<String, Object> paramMap = new HashMap<>();

            List<Map<String, Object>> datalist = checkEntInfoService.getFeedbackDataListByParam(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

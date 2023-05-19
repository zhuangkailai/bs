package com.tjpu.sp.controller.envhousekeepers.problemconsult;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.envhousekeepers.problemconsult.EntProblemConsultRecordVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.user.UserService;
import com.tjpu.sp.service.envhousekeepers.problemconsult.EntProblemConsultRecordService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.*;


/**
 * @author: xsm
 * @date: 2021/08/25 0025 上午 09:08
 * @Description: 企业问题咨询记录控制层
 */
@RestController
@RequestMapping("entProblemConsultRecord")
public class EntProblemConsultRecordController {

    @Autowired
    private EntProblemConsultRecordService entProblemConsultRecordService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private UserService userService;

    /**
     * @author: xsm
     * @date: 2021/08/25 0025 上午 09:08
     * @Description: 通过自定义参数查询企业问题咨询记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getEntProblemConsultRecordByParamMap", method = RequestMethod.POST)
    public Object getEntProblemConsultRecordByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            jsonObject.put("userid", userid);
            jsonObject.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.ProblemReplyMessage.getCode());
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = entProblemConsultRecordService.getEntProblemConsultRecordByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", dataList);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/25 0025 上午 09:08
     * @Description: 新增问题
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addEntProblemConsultRecord", method = RequestMethod.POST)
    public Object addEntProblemConsultRecord(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            EntProblemConsultRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntProblemConsultRecordVO.class);
            //String menucode = jsonObject.get("menucode")!=null?jsonObject.get("menucode").toString():"";
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setStatus((short)0);
            entity.setAskproblemuser(username);
            entity.setAskproblemtime(new Date());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            int i = entProblemConsultRecordService.insert(entity);
            //咨询问题时 向管委会端推送消息
            if (i > 0) {
                //推送到管委会端首页
                String messageType = CommonTypeEnum.HomePageMessageTypeEnum.EntProblemConsultMessage.getCode();
                //根据菜单Code 获取拥有该菜单code权限的用户
                //List<String> userids = userService.getAllUserIdsByMenuCode(menucode);
                JSONObject jsonobj = new JSONObject();
                //jsonobj.put("userids", userids);
                jsonobj.put("pkid", entity.getPkId());
                jsonobj.put("pollutionname", jsonObject.get("pollutionname"));
                jsonobj.put("Pollutionid", entity.getFkPollutionid());
                jsonobj.put("updatetime", entity.getAskproblemtime() != null ? DataFormatUtil.getDateYMDHMS(entity.getAskproblemtime()) : DataFormatUtil.parseDateYMDHMS(new Date()));
                String str = "";
                if (entity.getProblemtitle()!= null) {//标题不为空
                    str = jsonObject.get("pollutionname") +"咨询问题【"+entity.getProblemtitle()+"】。";
                } else {
                    str = "您有一条咨询问题信息！";
                }
                jsonobj.put("messagestr", str);
                jsonobj.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntProblemConsultMessage.getCode());
                jsonobj.put("isread", "0");
                rabbitmqController.sendMessageToManagementSide(jsonobj, messageType);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/25 0025 上午 09:08
     * @Description: 通过id获取企业问题咨询记录记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntProblemConsultRecordByID", method = RequestMethod.POST)
    public Object getEntProblemConsultRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> entity = entProblemConsultRecordService.getEntProblemConsultRecordDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/25 0025 上午 09:08
     * @Description: 回复问题
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateEntProblemConsultRecord", method = RequestMethod.POST)
    public Object updateEntProblemConsultRecord(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            EntProblemConsultRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntProblemConsultRecordVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            EntProblemConsultRecordVO obj = entProblemConsultRecordService.selectByPrimaryKey(entity.getPkId());
            obj.setReplycontent(entity.getReplycontent());
            obj.setStatus((short)1);
            obj.setReplyuser(username);
            obj.setReplytime(new Date());
            obj.setUpdatetime(new Date());
            obj.setUpdateuser(username);
            int i  = entProblemConsultRecordService.updateByPrimaryKey(obj);
            if (i > 0) {
                //推送到企业端首页
                String messageType = CommonTypeEnum.HomePageMessageTypeEnum.ProblemReplyMessage.getCode();
                //根据企业ID获取企业关联的企业用户ID
                List<String> userids = pollutionService.getUserInfoByPollution(obj.getFkPollutionid());
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("userids", userids);
                jsonobj.put("pkid", obj.getPkId());
                //jsonobj.put("pollutionname", jsonObject.get("pollutionname"));
                jsonobj.put("Pollutionid", obj.getFkPollutionid());
                jsonobj.put("updatetime", obj.getReplytime() != null ? DataFormatUtil.getDateYMDHMS(obj.getReplytime()) : DataFormatUtil.parseDateYMDHMS(new Date()));
                String str = "您有新的问题回复信息【"+obj.getProblemtitle()+"】。";
                jsonobj.put("messagestr", str);
                jsonobj.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.ProblemReplyMessage.getCode());
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
     * @date: 2021/08/25 0025 上午 09:08
     * @Description: 通过id删除企业问题咨询记录记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteEntProblemConsultRecordByID", method = RequestMethod.POST)
    public Object deleteEntProblemConsultRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            entProblemConsultRecordService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/25 0025 上午 09:08
     * @Description: 通过id获取企业问题咨询记录记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntProblemConsultRecordDetailByID", method = RequestMethod.POST)
    public Object getEntProblemConsultRecordDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = entProblemConsultRecordService.getEntProblemConsultRecordDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/25 0025 下午 2:15
     * @Description: 关键字搜索问题
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getAllSearchProblemDataByParamMap", method = RequestMethod.POST)
    public Object getAllSearchProblemDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = entProblemConsultRecordService.getAllSearchProblemDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", dataList);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

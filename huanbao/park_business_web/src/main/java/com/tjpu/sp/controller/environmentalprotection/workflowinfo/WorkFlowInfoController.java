package com.tjpu.sp.controller.environmentalprotection.workflowinfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.workflowinfo.WorkFlowInfoVO;
import com.tjpu.sp.service.environmentalprotection.workflowinfo.WorkFlowInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;


/**
 * @author: chengzq
 * @date: 2021/05/07 0011 下午 1:58
 * @Description: 工作流程控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("workflowinfo")
public class WorkFlowInfoController {

    @Autowired
    private WorkFlowInfoService workFlowInfoService;

    /**
     * @author: chengzq
     * @date: 2021/05/07 0011 下午 2:58
     * @Description: 通过自定义参数获取工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getWorkFlowInfoByParamMap", method = RequestMethod.POST)
    public Object getWorkFlowInfoByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List workFlowInfoByParamMap = workFlowInfoService.getWorkFlowInfoByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(workFlowInfoByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", workFlowInfoByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/5/7 0007 上午 11:22
     * @Description: 存在修改不存在新增工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addOrUpdateWorkFlowInfo", method = RequestMethod.POST)
    public Object addOrUpdateWorkFlowInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            String fkworkflowtype = jsonObject.get("fkworkflowtype") == null ? "" : jsonObject.get("fkworkflowtype").toString();
            Map<String, Object> paramMap = workFlowInfoService.selectByWorkFlowType(fkworkflowtype);
            if(paramMap!=null){
                WorkFlowInfoVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WorkFlowInfoVO());
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                entity.setcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                entity.setcreateuser(username);
                entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                entity.setupdateuser(username);
                workFlowInfoService.updateByWorkFlowType(entity);
            }else{
                WorkFlowInfoVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WorkFlowInfoVO());
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                entity.setpkid(UUID.randomUUID().toString());
                entity.setcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                entity.setcreateuser(username);
                entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                entity.setupdateuser(username);
                workFlowInfoService.insert(entity);
            }

            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/07 0011 下午 3:17
     * @Description: 新增工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addWorkFlowInfo", method = RequestMethod.POST)
    public Object addWorkFlowInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            WorkFlowInfoVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WorkFlowInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setpkid(UUID.randomUUID().toString());
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            workFlowInfoService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/07 0011 下午 3:19
     * @Description: 通过workflowtype获取工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getWorkFlowInfoByWorkFlowType", method = RequestMethod.POST)
    public Object getWorkFlowInfoByWorkFlowType(@RequestJson(value = "workflowtype") String workflowtype) throws Exception {
        try {
            Map<String,Object> result = workFlowInfoService.selectByWorkFlowType(workflowtype);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/05/07 0011 下午 3:19
     * @Description: 通过id获取工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getWorkFlowInfoByID", method = RequestMethod.POST)
    public Object getWorkFlowInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> result = workFlowInfoService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/07 0011 下午 3:19
     * @Description: 修改工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateWorkFlowInfo", method = RequestMethod.POST)
    public Object updateWorkFlowInfo(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            WorkFlowInfoVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WorkFlowInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            workFlowInfoService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/07 0011 下午 3:21
     * @Description: 通过id删除工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteWorkFlowInfoByID", method = RequestMethod.POST)
    public Object deleteWorkFlowInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            workFlowInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/07 0011 下午 3:31
     * @Description: 通过id查询工作流程信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getWorkFlowInfoDetailByID", method = RequestMethod.POST)
    public Object getWorkFlowInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> detailInfo = workFlowInfoService.getWorkFlowInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}

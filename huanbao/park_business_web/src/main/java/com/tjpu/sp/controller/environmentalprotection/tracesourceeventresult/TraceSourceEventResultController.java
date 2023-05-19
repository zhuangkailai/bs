package com.tjpu.sp.controller.environmentalprotection.tracesourceeventresult;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.tracesourceeventresult.TraceSourceEventResultVO;
import com.tjpu.sp.service.environmentalprotection.tracesourceeventresult.TraceSourceEventResultService;
import net.sf.json.JSONArray;
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
 * @date: 2021/05/10 0011 下午 1:58
 * @Description: 溯源事件结果控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("tracesourceeventresult")
public class TraceSourceEventResultController {

    @Autowired
    private TraceSourceEventResultService traceSourceEventResultService;

    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 2:58
     * @Description: 通过自定义参数获取溯源事件结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceEventResultByParamMap", method = RequestMethod.POST)
    public Object getTraceSourceEventResultByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List traceSourceEventResultByParamMap = traceSourceEventResultService.getTraceSourceEventResultByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(traceSourceEventResultByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", traceSourceEventResultByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:17
     * @Description: 新增溯源事件结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addTraceSourceEventResult", method = RequestMethod.POST)
    public Object addTraceSourceEventResult(@RequestJson(value = "addformdatalist") Object addformdatalist) throws Exception {
        try {

            List<TraceSourceEventResultVO> records=new ArrayList<>();
            JSONArray jsonArray = JSONArray.fromObject(addformdatalist);
            for (Object jsonObject : jsonArray) {
                TraceSourceEventResultVO entity = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(jsonObject), new TraceSourceEventResultVO());
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                entity.setpkid(UUID.randomUUID().toString());
                entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                entity.setupdateuser(username);
                records.add(entity);
            }

            JSONObject paramMap = JSONObject.fromObject(jsonArray.stream().findFirst().orElse(new HashMap<>()));
            List<Map<String,Object>> traceSourceEventResultByParamMap = traceSourceEventResultService.getTraceSourceEventResultByParamMap(paramMap);
            if(traceSourceEventResultByParamMap.size()>0){
                traceSourceEventResultService.update(records);
            }else{
                traceSourceEventResultService.insert(records);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:19
     * @Description: 通过id获取溯源事件结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceEventResultByID", method = RequestMethod.POST)
    public Object getTraceSourceEventResultByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> result = traceSourceEventResultService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:19
     * @Description: 修改溯源事件结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateTraceSourceEventResult", method = RequestMethod.POST)
    public Object updateTraceSourceEventResult(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            TraceSourceEventResultVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TraceSourceEventResultVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            traceSourceEventResultService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:21
     * @Description: 通过id删除溯源事件结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteTraceSourceEventResultByID", method = RequestMethod.POST)
    public Object deleteTraceSourceEventResultByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            traceSourceEventResultService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:31
     * @Description: 通过id查询溯源事件结果信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceEventResultDetailByID", method = RequestMethod.POST)
    public Object getTraceSourceEventResultDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> detailInfo = traceSourceEventResultService.getTraceSourceEventResultDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}

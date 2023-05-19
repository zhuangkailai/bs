package com.tjpu.sp.controller.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpoint.UseElectricFacilityMonitorPointVO;
import com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpointset.UseElectricFacilityMonitorPointSetVO;
import com.tjpu.sp.service.environmentalprotection.useelectricfacilitymonitorpoint.UseElectricFacilityMonitorPointService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2020/06/18 0011 下午 1:58
 * @Description: 用电设施监测点控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("useelectricfacilitymonitorpoint")
public class UseElectricFacilityMonitorPointController {

    @Autowired
    private UseElectricFacilityMonitorPointService useElectricFacilityMonitorPointService;

    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 2:58
     * @Description: 通过自定义参数获取用电设施监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getUseElectricFacilityMonitorPointByParamMap", method = RequestMethod.POST)
    public Object getUseElectricFacilityMonitorPointByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String,Object>> useElectricFacilityMonitorPointByParamMap = useElectricFacilityMonitorPointService.getUseElectricFacilityMonitorPointByParamMap(jsonObject);
            long total = useElectricFacilityMonitorPointByParamMap.size();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                Integer pagenum = Integer.valueOf(jsonObject.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(jsonObject.get("pagesize").toString());
                useElectricFacilityMonitorPointByParamMap=useElectricFacilityMonitorPointByParamMap.stream().skip((pagenum-1)*pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("datalist", useElectricFacilityMonitorPointByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:17
     * @Description: 新增用电设施监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addUseElectricFacilityMonitorPoint", method = RequestMethod.POST)
    public Object addUseElectricFacilityMonitorPoint(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("pollutants"));
            String monitorid = UUID.randomUUID().toString();
            Date date = new Date();
            UseElectricFacilityMonitorPointVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new UseElectricFacilityMonitorPointVO());

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            entity.setpkid(monitorid);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(date));
            entity.setupdateuser(username);

            List<UseElectricFacilityMonitorPointSetVO> pollutants=new ArrayList<>();
            for (Object obj : jsonArray) {
                UseElectricFacilityMonitorPointSetVO monitorPointSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(obj), new UseElectricFacilityMonitorPointSetVO());
                monitorPointSetVO.setpkid(UUID.randomUUID().toString());
                monitorPointSetVO.setfkuseelectricfacilitymonitorpointid(monitorid);
                monitorPointSetVO.setupdatetime(DataFormatUtil.getDateYMDHMS(date));
                monitorPointSetVO.setupdateuser(username);
                pollutants.add(monitorPointSetVO);
            }
            useElectricFacilityMonitorPointService.insert(entity,pollutants);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:19
     * @Description: 通过id获取用电设施监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getUseElectricFacilityMonitorPointByID", method = RequestMethod.POST)
    public Object getUseElectricFacilityMonitorPointByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> result = useElectricFacilityMonitorPointService.getUseElectricFacilityMonitorPointDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:19
     * @Description: 修改用电设施监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateUseElectricFacilityMonitorPoint", method = RequestMethod.POST)
    public Object updateUseElectricFacilityMonitorPoint(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("pollutants"));
            Date date = new Date();
            UseElectricFacilityMonitorPointVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new UseElectricFacilityMonitorPointVO());

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(date));
            entity.setupdateuser(username);

            List<UseElectricFacilityMonitorPointSetVO> pollutants=new ArrayList<>();
            for (Object obj : jsonArray) {
                UseElectricFacilityMonitorPointSetVO monitorPointSetVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(obj), new UseElectricFacilityMonitorPointSetVO());
                monitorPointSetVO.setpkid(UUID.randomUUID().toString());
                monitorPointSetVO.setfkuseelectricfacilitymonitorpointid(entity.getpkid());
                monitorPointSetVO.setupdatetime(DataFormatUtil.getDateYMDHMS(date));
                monitorPointSetVO.setupdateuser(username);
                pollutants.add(monitorPointSetVO);
            }

            useElectricFacilityMonitorPointService.updateByPrimaryKey(entity,pollutants);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:21
     * @Description: 通过id删除用电设施监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteUseElectricFacilityMonitorPointByID", method = RequestMethod.POST)
    public Object deleteUseElectricFacilityMonitorPointByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            useElectricFacilityMonitorPointService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:31
     * @Description: 通过id查询用电设施监测点信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getUseElectricFacilityMonitorPointDetailByID", method = RequestMethod.POST)
    public Object getUseElectricFacilityMonitorPointDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> detailInfo = useElectricFacilityMonitorPointService.getUseElectricFacilityMonitorPointDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}

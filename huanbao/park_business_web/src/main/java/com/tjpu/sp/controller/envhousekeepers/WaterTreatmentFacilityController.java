package com.tjpu.sp.controller.envhousekeepers;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.WaterTreatmentFacilityVO;
import com.tjpu.sp.service.envhousekeepers.WaterTreatmentFacilityService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 废水治理设施处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("waterTreatmentFacility")
public class WaterTreatmentFacilityController {


    @Autowired
    private WaterTreatmentFacilityService waterTreatmentFacilityService;


    /**
     * @Description: 添加或更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateInfo", method = RequestMethod.POST)
    public Object addOrUpdateInfo(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            WaterTreatmentFacilityVO waterTreatmentFacilityVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new WaterTreatmentFacilityVO());
            waterTreatmentFacilityVO.setUpdatedate(new Date());
            waterTreatmentFacilityVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(waterTreatmentFacilityVO.getPkId())) {//更新操作
                waterTreatmentFacilityService.updateData(waterTreatmentFacilityVO);
            } else {//添加操作
                waterTreatmentFacilityVO.setPkId(UUID.randomUUID().toString());
                waterTreatmentFacilityService.insertData(waterTreatmentFacilityVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getListDataByParamMap", method = RequestMethod.POST)
    public Object getListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            List<Map<String, Object>> datalist = waterTreatmentFacilityService.getListDataByParamMap(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 根据企业ID获取废气点位信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getWaterOutPutByPollutionId", method = RequestMethod.POST)
    public Object getWaterOutPutByPollutionId(@RequestJson(value = "pollutionid") String pollutionid,
                                              @RequestJson(value = "directorindirect",required = false) Integer directorindirect
                                              ) {
        try {

            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid",pollutionid);
            paramMap.put("directorindirect",directorindirect);
            List<Map<String, Object>> datalist=  waterTreatmentFacilityService.getWaterOutPutByPollutionId(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 根据ID治理设施删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "deleteInfoById", method = RequestMethod.POST)
    public Object deleteInfoById(@RequestJson(value = "id") String id) {
        try {
            waterTreatmentFacilityService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

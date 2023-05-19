package com.tjpu.sp.controller.envhousekeepers;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.PollutionProductFacilityVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.envhousekeepers.GasTreatmentFacilityService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 废气治理设施处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("gasTreatmentFacility")
public class GasTreatmentFacilityController {


    @Autowired
    private GasTreatmentFacilityService gasTreatmentFacilityService;
    @Autowired
    private FileInfoService fileInfoService;


    /**
     * @Description: 添加或更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateFacilityInfo", method = RequestMethod.POST)
    public Object addOrUpdateFacilityInfo(@RequestJson(value = "parentformdata") Object parentformdata,
                                          @RequestJson(value = "chlidformdata",required = false) Object chlidformdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(parentformdata);
            PollutionProductFacilityVO pollutionProductFacilityVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PollutionProductFacilityVO());
            pollutionProductFacilityVO.setUpdatedate(new Date());
            pollutionProductFacilityVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(pollutionProductFacilityVO.getPkId())){//更新操作
                gasTreatmentFacilityService.updateData(pollutionProductFacilityVO,chlidformdata);
            }else {//添加操作
                pollutionProductFacilityVO.setPkId(UUID.randomUUID().toString());
                gasTreatmentFacilityService.insertData(pollutionProductFacilityVO,chlidformdata);
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
            List<Map<String, Object>> datalist = gasTreatmentFacilityService.getListDataByParamMap(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取编辑数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getEditDataByFacilityid", method = RequestMethod.POST)
    public Object getEditDataByFacilityid(@RequestJson(value = "facilityid") String facilityid) throws Exception {
        try {
            Map<String,Object> resultMap = new HashMap<>();
            PollutionProductFacilityVO pollutionProductFacilityVO =  gasTreatmentFacilityService.getProductInfoById(facilityid);
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("facilityid",facilityid);
            List<Map<String, Object>> datalist = gasTreatmentFacilityService.getGasTreatmentListDataByParamMap(paramMap);
            resultMap.put("parentformdata",pollutionProductFacilityVO);
            resultMap.put("chlidformdata",datalist);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
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
    @RequestMapping(value = "getGasOutPutByPollutionId", method = RequestMethod.POST)
    public Object getGasOutPutByPollutionId(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            List<Map<String, Object>> datalist=  gasTreatmentFacilityService.getGasOutPutByPollutionId(pollutionid);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @Description: 根据ID产污设施及治理设施删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "deleteProductById", method = RequestMethod.POST)
    public Object deleteProductById(@RequestJson(value = "id") String id) {
        try {
            gasTreatmentFacilityService.deleteProductById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
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
            gasTreatmentFacilityService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

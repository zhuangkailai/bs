package com.tjpu.sp.controller.environmentalprotection.particularpollutants;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;

import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;

import com.tjpu.sp.model.environmentalprotection.particularpollutants.EntGasPollutantVO;

import com.tjpu.sp.service.environmentalprotection.particularpollutants.EntGasPollutantService;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 企业废气特征污染物
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/7/7 9:49
 */
@RestController
@RequestMapping("entGasPollutant")
public class EntGasPollutantController {


    @Autowired
    private EntGasPollutantService entGasPollutantService;


    /**
     * @Description: 获取列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 10:51
     */
    @RequestMapping(value = "getDataListByParam", method = RequestMethod.POST)
    public Object getDataListByParam(
            @RequestJson(value = "paramjson") Object paramjson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramjson);
            Map<String, Object> resultMap = entGasPollutantService.getDataListByParam(jsonObject);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取企业检查信息列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 10:51
     */
    @RequestMapping(value = "addOrUpdateData", method = RequestMethod.POST)
    public Object addOrUpdateData(
            @RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            String pollutionId = jsonObject.getString("pollutionid");
            Date nowDay = new Date();
            List<String> pollutants = jsonObject.getJSONArray("pollutants");
            if (pollutants.size() > 0) {
                List<EntGasPollutantVO> entGasPollutantVOS = new ArrayList<>();
                for (String code : pollutants) {
                    EntGasPollutantVO entGasPollutantVO = new EntGasPollutantVO();
                    entGasPollutantVO.setPkId(UUID.randomUUID().toString());
                    entGasPollutantVO.setUpdateuser(username);
                    entGasPollutantVO.setUpdatetime(nowDay);
                    entGasPollutantVO.setFkPollutionid(pollutionId);
                    entGasPollutantVO.setFkPollutantcode(code);
                    entGasPollutantVOS.add(entGasPollutantVO);
                }
                entGasPollutantService.updateOrAddData(pollutionId, entGasPollutantVOS);
            }

            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @Description: 信息删除
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 12:03
     */
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public Object deleteById(
            @RequestJson(value = "pollutionid") String pollutionid) {
        try {
            entGasPollutantService.deleteById(pollutionid);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

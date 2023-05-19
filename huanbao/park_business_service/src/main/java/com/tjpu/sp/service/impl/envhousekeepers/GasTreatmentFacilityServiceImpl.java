package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.dao.envhousekeepers.GasTreatmentFacilityMapper;
import com.tjpu.sp.dao.envhousekeepers.PollutionProductFacilityMapper;
import com.tjpu.sp.model.envhousekeepers.GasTreatmentFacilityVO;
import com.tjpu.sp.model.envhousekeepers.PollutionProductFacilityVO;
import com.tjpu.sp.service.envhousekeepers.GasTreatmentFacilityService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@Transactional
public class GasTreatmentFacilityServiceImpl implements GasTreatmentFacilityService {
    @Autowired
    private GasTreatmentFacilityMapper gasTreatmentFacilityMapper;

    @Autowired
    private PollutionProductFacilityMapper pollutionProductFacilityMapper;
    @Override
    public void insertData(PollutionProductFacilityVO pollutionProductFacilityVO, Object chlidformdata) throws Exception {
        pollutionProductFacilityMapper.insert(pollutionProductFacilityVO);
        if (chlidformdata!=null){
            JSONArray jsonArray = JSONArray.fromObject(chlidformdata);
            for (int i = 0; i <jsonArray.size() ; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                GasTreatmentFacilityVO gasTreatmentFacilityVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new GasTreatmentFacilityVO());
                gasTreatmentFacilityVO.setPkId(UUID.randomUUID().toString());
                gasTreatmentFacilityVO.setFkFacilityid(pollutionProductFacilityVO.getPkId());
                gasTreatmentFacilityVO.setUpdatedate(pollutionProductFacilityVO.getUpdatedate());
                gasTreatmentFacilityVO.setUpdateuser(pollutionProductFacilityVO.getUpdateuser());
                gasTreatmentFacilityMapper.insert(gasTreatmentFacilityVO);
            }
        }
    }

    @Override
    public void updateData(PollutionProductFacilityVO pollutionProductFacilityVO, Object chlidformdata) throws Exception {
        pollutionProductFacilityMapper.updateByPrimaryKey(pollutionProductFacilityVO);
        if (chlidformdata!=null){
            JSONArray jsonArray = JSONArray.fromObject(chlidformdata);
            for (int i = 0; i <jsonArray.size() ; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                GasTreatmentFacilityVO gasTreatmentFacilityVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new GasTreatmentFacilityVO());
                gasTreatmentFacilityVO.setUpdatedate(pollutionProductFacilityVO.getUpdatedate());
                gasTreatmentFacilityVO.setUpdateuser(pollutionProductFacilityVO.getUpdateuser());
                gasTreatmentFacilityVO.setFkFacilityid(pollutionProductFacilityVO.getPkId());
                if (StringUtils.isNotBlank(gasTreatmentFacilityVO.getPkId())){
                    gasTreatmentFacilityMapper.updateByPrimaryKey(gasTreatmentFacilityVO);
                }else {
                    gasTreatmentFacilityVO.setPkId(UUID.randomUUID().toString());
                    gasTreatmentFacilityMapper.insert(gasTreatmentFacilityVO);
                }
            }
        }
    }

    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return gasTreatmentFacilityMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void deleteInfoById(String id) {
        gasTreatmentFacilityMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void deleteProductById(String id) {
        gasTreatmentFacilityMapper.deleteByFacilityId(id);
        pollutionProductFacilityMapper.deleteByPrimaryKey(id);
    }

    @Override
    public PollutionProductFacilityVO getProductInfoById(String facilityid) {
        return pollutionProductFacilityMapper.selectByPrimaryKey(facilityid);
    }

    @Override
    public List<Map<String, Object>> getGasOutPutByPollutionId(String pollutionid) {
        return gasTreatmentFacilityMapper.getGasOutPutByPollutionId( pollutionid);
    }

    @Override
    public List<Map<String, Object>> getGasTreatmentListDataByParamMap(Map<String, Object> paramMap) {
        return gasTreatmentFacilityMapper.getGasTreatmentListDataByParamMap(paramMap);
    }
}

package com.tjpu.sp.dao.common.emergency;

import com.tjpu.sp.model.common.emergency.EmergencyCaseInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EmergencyCaseInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EmergencyCaseInfoVO record);

    int insertSelective(EmergencyCaseInfoVO record);

    EmergencyCaseInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EmergencyCaseInfoVO record);

    int updateByPrimaryKey(EmergencyCaseInfoVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    Map<String, Object> getEditOrDetailsDataById(String id);
}
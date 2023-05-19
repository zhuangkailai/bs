package com.tjpu.sp.dao.environmentalprotection.particularpollutants;

import com.tjpu.sp.model.environmentalprotection.particularpollutants.EntGasPollutantVO;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntGasPollutantMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntGasPollutantVO record);

    int insertSelective(EntGasPollutantVO record);

    EntGasPollutantVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntGasPollutantVO record);

    int updateByPrimaryKey(EntGasPollutantVO record);

    List<Map<String, Object>> getDataListByParam(JSONObject jsonObject);

    void deleteByFId(String pollutionId);
}
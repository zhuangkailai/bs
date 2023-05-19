package com.tjpu.sp.dao.environmentalprotection.assess;

import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentInfoVO;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntAssessmentInfoMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(EntAssessmentInfoVO record);

    int insertSelective(EntAssessmentInfoVO record);

    EntAssessmentInfoVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(EntAssessmentInfoVO record);

    int updateByPrimaryKey(EntAssessmentInfoVO record);

    List<Map<String, Object>> getEntAssessInfoListByParam(JSONObject jsonObject);

    List<Map<String, Object>> getAddItemDataList();
}
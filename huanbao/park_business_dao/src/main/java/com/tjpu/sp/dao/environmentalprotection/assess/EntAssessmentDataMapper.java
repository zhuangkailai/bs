package com.tjpu.sp.dao.environmentalprotection.assess;

import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentDataVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntAssessmentDataMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(EntAssessmentDataVO record);

    int insertSelective(EntAssessmentDataVO record);

    EntAssessmentDataVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(EntAssessmentDataVO record);

    int updateByPrimaryKey(EntAssessmentDataVO record);

    void deleteByFId(String fkId);

    List<Map<String, Object>> getCheckDataListByFId(String id);
}
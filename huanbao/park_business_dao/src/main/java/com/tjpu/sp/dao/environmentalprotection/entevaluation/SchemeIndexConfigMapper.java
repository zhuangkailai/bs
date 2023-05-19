package com.tjpu.sp.dao.environmentalprotection.entevaluation;

import com.tjpu.sp.model.environmentalprotection.entevaluation.SchemeIndexConfigVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SchemeIndexConfigMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(SchemeIndexConfigVO record);

    int insertSelective(SchemeIndexConfigVO record);

    SchemeIndexConfigVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(SchemeIndexConfigVO record);

    int updateByPrimaryKey(SchemeIndexConfigVO record);

    void batchInsert(@Param("list")List<SchemeIndexConfigVO> list);

    void deleteByEntEvaluationSchemeID(String pkSchemeid);

    List<Map<String,Object>> getSchemeIndexConfigListDataBySchemeID(String id);
}
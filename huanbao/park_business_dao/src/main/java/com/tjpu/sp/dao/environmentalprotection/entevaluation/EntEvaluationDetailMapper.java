package com.tjpu.sp.dao.environmentalprotection.entevaluation;

import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface EntEvaluationDetailMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntEvaluationDetailVO record);

    int insertSelective(EntEvaluationDetailVO record);

    EntEvaluationDetailVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntEvaluationDetailVO record);

    int updateByPrimaryKey(EntEvaluationDetailVO record);

    void batchInsert(@Param("list")List<EntEvaluationDetailVO> list);

    void deleteByEntSynthesizeEvaluationID(String pkId);

    List<Map<String,Object>> getEntEvaluationDetailInfoByParam(Map<String, Object> param);


}
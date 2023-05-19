package com.tjpu.sp.dao.envhousekeepers.checkproblemexpound;


import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.RectifiedAndReviewRecordVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface RectifiedAndReviewRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RectifiedAndReviewRecordVO record);

    int insertSelective(RectifiedAndReviewRecordVO record);

    RectifiedAndReviewRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RectifiedAndReviewRecordVO record);

    int updateByPrimaryKey(RectifiedAndReviewRecordVO record);

    Map<String,Object> getLastRectifiedAndReviewRecordByParamMap(Map<String, Object> paramtMap);

    List<Map<String,Object>> getRectifiedAndReviewRecordByParamMap(Map<String, Object> paramtMap);

    List<Map<String,Object>> getHistoryDisposalDataByParamMap(Map<String, Object> paramtMap);

    List<Map<String,Object>> countEntProblemRectifyReportNumByID(@Param("fkCheckproblemexpoundid") String fkCheckproblemexpoundid);

    void deleteByCheckProblemExpoundID(String id);
}
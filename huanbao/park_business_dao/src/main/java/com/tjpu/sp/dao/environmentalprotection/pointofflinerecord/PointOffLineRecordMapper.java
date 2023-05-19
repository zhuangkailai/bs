package com.tjpu.sp.dao.environmentalprotection.pointofflinerecord;

import com.tjpu.sp.model.environmentalprotection.pointofflinerecord.PointOffLineRecordVO;

import java.util.List;
import java.util.Map;

public interface PointOffLineRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PointOffLineRecordVO record);

    int insertSelective(PointOffLineRecordVO record);

    PointOffLineRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PointOffLineRecordVO record);

    int updateByPrimaryKey(PointOffLineRecordVO record);

    List<Map<String,Object>> getPointOffLineRecordsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getPointOffLineRecordDetailById(Map<String, Object> paramMap);

    List<Map<String,Object>> getNowPointOffLineRecordsByParamMap(Map<String, Object> parammap);

    List<Map<String,Object>> getEntPointOffLineRecordsByParamMap(Map<String, Object> parammap);
}
package com.tjpu.sp.service.environmentalprotection.pointofflinerecord;


import com.tjpu.sp.model.environmentalprotection.pointofflinerecord.PointOffLineRecordVO;

import java.util.List;
import java.util.Map;

public interface PointOffLineRecordService {

    List<Map<String,Object>> getPointOffLineRecordsByParamMap(Map<String, Object> paramMap);

    PointOffLineRecordVO getPointOffLineRecordInfoById(String id);

    int insert(PointOffLineRecordVO obj);

    void update(PointOffLineRecordVO obj);

    Map<String,Object> getPointOffLineRecordDetailById(Map<String, Object> paramMap);

    List<Map<String,Object>> getNowPointOffLineRecordsByParamMap(Map<String, Object> parammap);

    List<Map<String,Object>> getEntPointOffLineRecordsByParamMap(Map<String, Object> parammap);
}

package com.tjpu.sp.service.envhousekeepers.wastematerial;


import com.tjpu.sp.model.envhousekeepers.wastematerial.WasteMaterialHandleRecordVO;

import java.util.List;
import java.util.Map;

public interface WasteMaterialHandleRecordService {

    List<Map<String,Object>> getWasteMaterialHandleRecordByParamMap(Map<String, Object> paramMap);

    void insert(WasteMaterialHandleRecordVO entity);

    Map<String, Object> selectByPrimaryKey(String id);

    void updateByPrimaryKey(WasteMaterialHandleRecordVO entity);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getWasteMaterialHandleRecordDetailByID(String id);

   void deleteHandleRecordByEntIDAndCode(Map<String, Object> param);

    String WasteMaterialTitleNameByParam(Map<String, Object> paramMap);


    List<Map<String, Object>> getWasteMaterialTreeByParam(Map<String, Object> param);
}

package com.tjpu.sp.dao.envhousekeepers.wastematerial;

import com.tjpu.sp.model.envhousekeepers.wastematerial.WasteMaterialHandleRecordVO;

import java.util.List;
import java.util.Map;

public interface WasteMaterialHandleRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WasteMaterialHandleRecordVO record);

    int insertSelective(WasteMaterialHandleRecordVO record);

    WasteMaterialHandleRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WasteMaterialHandleRecordVO record);

    int updateByPrimaryKey(WasteMaterialHandleRecordVO record);

    List<Map<String,Object>> getWasteMaterialHandleRecordByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getWasteMaterialHandleRecordDetailByID(String id);

    void deleteHandleRecordByEntIDAndCode(Map<String, Object> param);

    Map<String,Object> WasteMaterialTitleNameByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getWasteMaterialTreeByParam(Map<String, Object> param);
}
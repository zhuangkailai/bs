package com.tjpu.sp.dao.environmentalprotection.devopsinfo;

import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevicePersonnelRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DevicePersonnelRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(DevicePersonnelRecordVO record);

    int insertSelective(DevicePersonnelRecordVO record);

    DevicePersonnelRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(DevicePersonnelRecordVO record);

    int updateByPrimaryKey(DevicePersonnelRecordVO record);

    void batchInsert(@Param("list")List<DevicePersonnelRecordVO> list);

    void deleteByUnitIDAndPersonnelID(Map<String, Object> param);

    List<Map<String,Object>> getEntDevOpsIdDataByParam(Map<String, Object> paramMap);

    void deleteByEntDevOpsID(String pkId);

    List<Map<String,Object>> getPersonnelIdDataByParam(Map<String, Object> paramMap);
}
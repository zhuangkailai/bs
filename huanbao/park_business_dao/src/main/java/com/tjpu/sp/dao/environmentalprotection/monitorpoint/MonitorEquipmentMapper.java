package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.MonitorEquipmentVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface MonitorEquipmentMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(MonitorEquipmentVO record);

    int insertSelective(MonitorEquipmentVO record);

    MonitorEquipmentVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(MonitorEquipmentVO record);

    int updateByPrimaryKey(MonitorEquipmentVO record);

    List<Map<String,Object>> getMonitorEquipmentByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> isTableDataHaveInfoByMonitorNameAndMonitorPointID(Map<String, Object> paramMap);

    Map<String,Object> getMonitorEquipmentDetailByID(Map<String, Object> paramMap);
}
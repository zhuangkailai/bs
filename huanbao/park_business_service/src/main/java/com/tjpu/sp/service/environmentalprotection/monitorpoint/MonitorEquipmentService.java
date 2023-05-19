package com.tjpu.sp.service.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.MonitorEquipmentVO;

import java.util.List;
import java.util.Map;

public interface MonitorEquipmentService {
    int insert(MonitorEquipmentVO record);
    int updateByPrimaryKey(MonitorEquipmentVO record);

    List<Map<String,Object>> isTableDataHaveInfoByMonitorNameAndMonitorPointID(Map<String, Object> paramMap);

    List<Map<String,Object>> getMonitorEquipmentsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getMonitorEquipmentDetailByID(Map<String, Object> paramMap);

    void deleteMonitorEquipmentInfoByID(String id);

    MonitorEquipmentVO getMonitorEquipmentInfoByID(String id);
}

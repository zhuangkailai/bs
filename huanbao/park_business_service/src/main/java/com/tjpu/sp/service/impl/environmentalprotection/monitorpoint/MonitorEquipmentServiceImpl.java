package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;


import com.tjpu.sp.dao.environmentalprotection.monitorpoint.MonitorEquipmentMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.MonitorEquipmentVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.MonitorEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MonitorEquipmentServiceImpl implements MonitorEquipmentService {

	@Autowired
	private MonitorEquipmentMapper monitorEquipmentMapper;
	@Autowired
	private PollutantFactorMapper pollutantFactorMapper;


	@Override
	public int insert(MonitorEquipmentVO record) {
		return monitorEquipmentMapper.insert(record);
	}

	@Override
	public int updateByPrimaryKey(MonitorEquipmentVO record) {
		return monitorEquipmentMapper.updateByPrimaryKey(record);
	}

	/**
	 * @author: xsm
	 * @date: 2019/05/29  下午 4:27
	 * @Description: 验证监测设备是否有重复数据
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param:
	 * @return:
	 */
	@Override
	public List<Map<String, Object>> isTableDataHaveInfoByMonitorNameAndMonitorPointID(Map<String, Object> paramMap) {
		return monitorEquipmentMapper.isTableDataHaveInfoByMonitorNameAndMonitorPointID(paramMap);
	}

	/**
	 * @author: xsm
	 * @date: 2019/05/27  上午 7:34
	 * @Description: 根据自定义参数获取在线监测设备列表数据
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param:
	 * @return:
	 */
	@Override
	public List<Map<String, Object>> getMonitorEquipmentsByParamMap(Map<String, Object> paramMap) {
		return monitorEquipmentMapper.getMonitorEquipmentByParams(paramMap);
	}

	/**
	 * @author: xsm
	 * @date: 2020/08/05  上午 8:55
	 * @Description: 根据主键ID获取设备详情信息
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param:
	 * @return:
	 */
	@Override
	public Map<String, Object> getMonitorEquipmentDetailByID(Map<String, Object> paramMap) {
		return monitorEquipmentMapper.getMonitorEquipmentDetailByID(paramMap);
	}

	@Override
	public void deleteMonitorEquipmentInfoByID(String id) {
		 monitorEquipmentMapper.deleteByPrimaryKey(id);
	}

	@Override
	public MonitorEquipmentVO getMonitorEquipmentInfoByID(String id) {
		return monitorEquipmentMapper.selectByPrimaryKey(id);
	}
}

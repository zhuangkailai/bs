package com.pub.impl;

import com.pub.service.CommonSelectFieldConfigService;
import com.pub.dao.CommonSelectFieldConfigMapper;
import com.pub.model.CommonSelectFieldConfigVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class CommonSelectFieldConfigServiceImpl implements CommonSelectFieldConfigService {
    @Autowired
    private CommonSelectFieldConfigMapper commonSelectFieldConfigMapper;

    @Override
    public List<CommonSelectFieldConfigVO> getFieldsByFkTableConfigIdAndConfigType(String pkTableConfigId, String value) {
        return commonSelectFieldConfigMapper.getFieldsByFkTableConfigIdAndConfigType(pkTableConfigId, value);
    }

	@Override
	public Map<String, Object> getDeleteData(Map<String, Object> paramMaps) {

		return commonSelectFieldConfigMapper.getDeleteData(paramMaps);
	}

    @Override
    public List<CommonSelectFieldConfigVO> getDefaultAddFields(String pkTableConfigId, String configType) {
        return commonSelectFieldConfigMapper.getDefaultAddFields(pkTableConfigId,configType);
    }

    @Override
    public List<CommonSelectFieldConfigVO> getFieldListByTableIdAndConfigTypeList(String tableid, List<String> fieldTypes) {
        return commonSelectFieldConfigMapper.getFieldListByTableIdAndConfigTypeList(tableid,fieldTypes);
    }

}

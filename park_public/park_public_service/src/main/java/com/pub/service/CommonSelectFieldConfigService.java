package com.pub.service;

import com.pub.model.CommonSelectFieldConfigVO;

import java.util.List;
import java.util.Map;



public interface CommonSelectFieldConfigService {

    List<CommonSelectFieldConfigVO> getFieldsByFkTableConfigIdAndConfigType(String pkTableConfigId, String value);

	Map<String, Object> getDeleteData(Map<String, Object> paramMaps);

	List<CommonSelectFieldConfigVO> getDefaultAddFields(String pkTableConfigId, String configType);

    List<CommonSelectFieldConfigVO> getFieldListByTableIdAndConfigTypeList(String tableid, List<String> fieldTypes);
}

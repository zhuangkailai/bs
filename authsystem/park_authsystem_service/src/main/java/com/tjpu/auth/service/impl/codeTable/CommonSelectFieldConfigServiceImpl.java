package com.tjpu.auth.service.impl.codeTable;

import com.tjpu.auth.dao.codeTable.CommonSelectFieldConfigMapper;
import com.tjpu.auth.model.codeTable.CommonSelectFieldConfigVO;
import com.tjpu.auth.service.codeTable.CommonSelectFieldConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: zzc
 * @date: 2018/4/2212:12
 * @Description:字段操作实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
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
    public CommonSelectFieldConfigVO getCommonSelectFieldConfigVO(Map<String, String> paramMap) {
        return commonSelectFieldConfigMapper.getCommonSelectFieldConfigVO(paramMap);
    }

    /**      
	 * @author: xsm
	 * @date: 2018年9月3日 下午1:30:51
	 * @Description:根据自定义参数获取将要删除的数据信息 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param paramMaps
	 * @return    
	 */
	@Override
	public Map<String, Object> getDeleteData(Map<String, Object> paramMaps) {
		// TODO Auto-generated method stub
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

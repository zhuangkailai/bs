package com.tjpu.auth.service.codeTable;

import com.tjpu.auth.model.codeTable.CommonSelectFieldConfigVO;

import java.util.List;
import java.util.Map;


/**
 * @author: zzc
 * @date: 2018/4/2212:10
 * @Description:字段操作接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public interface CommonSelectFieldConfigService {

    List<CommonSelectFieldConfigVO> getFieldsByFkTableConfigIdAndConfigType(String pkTableConfigId, String value);

    CommonSelectFieldConfigVO getCommonSelectFieldConfigVO(Map<String, String> paramMap);

	/**      
	 * @author: xsm
	 * @date: 2018年9月3日 下午1:25:51
	 * @Description:根据自定义参数获取将要删除的数据信息 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param paramMaps
	 * @return    
	 */
	Map<String, Object> getDeleteData(Map<String, Object> paramMaps);
	/**
	 * @Author: zhangzc
	 * @Date: 2018/9/13 17:07
	 * @Description: 获取表中默认添加字段
	 * @UpdateUser:
	 * @UpdateDate:
	 * @UpdateDescription:
	 * @Param:
	 * @Return:
	 */
	List<CommonSelectFieldConfigVO> getDefaultAddFields(String pkTableConfigId,String configType);

	/**
	 * @author: ZhangZhenChao
	 * @date: 2018/7/25 18:44
	 * @Description: 获取查询列表数据所需配置字段
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param:
	 * @return:
	 */
    List<CommonSelectFieldConfigVO> getFieldListByTableIdAndConfigTypeList(String tableid, List<String> fieldTypes);
}

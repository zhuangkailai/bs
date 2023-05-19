package com.tjpu.auth.dao.codeTable;

import com.tjpu.auth.model.codeTable.CommonSelectFieldConfigVO;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @author: zhangzc
 * @date: 2018/4/22 12:15
 * @Description: 持久层字段操作接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @return
 */
@Repository
public interface CommonSelectFieldConfigMapper {
    int deleteByPrimaryKey(String pkFieldConfigId);

    int insert(CommonSelectFieldConfigVO record);

    int insertSelective(CommonSelectFieldConfigVO record);

    CommonSelectFieldConfigVO selectByPrimaryKey(String pkFieldConfigId);

    int updateByPrimaryKeySelective(CommonSelectFieldConfigVO record);

    int updateByPrimaryKey(CommonSelectFieldConfigVO record);

    /**
     * @author: zhangzc
     * @date: 2018/5/21 16:46
     * @Description: 动态条件查询获取符合条件的字段信息集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<CommonSelectFieldConfigVO> getFieldListByParam(
            HashMap<String, String> paramMap);

    /**
     * @author: zzc
     * @date: 2018/6/25 11:08
     * @Description: 通过表id和字段类型查询字段集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<CommonSelectFieldConfigVO> getFieldsByFkTableConfigIdAndConfigType(
            @Param("pktableconfigid") String fkTableConfigId,
            @Param("configtype") String configType);

    /**
     * @author: xsm
     * @date: 2018/7/11 13:08
     * @Description: 获取日志描述要显示的带标记的字段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<CommonSelectFieldConfigVO> getOperateLogFieldFlag(
            Map<String, Object> map);

    /**
     * @author: xsm
     * @date: 2018/7/11 13:33
     * @Description: 根据map中的查询条件获取配置信息，用来判断字段是否有关联关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<CommonSelectFieldConfigVO> getCommonSelectFieldConfigByMap(Map map);

    /**
     * @author: xsm
     * @date: 2018/7/12 14:13
     * @Description: 获取关联表里一对多的字段数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, String>> getRelationTableFieldNameList(
            Map<String, Object> params);

    /**
     * @author: xsm
     * @date: 2018/7/13 15:42
     * @Description: 获取删除操作中删除的那条数据信息。
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getDeleteData(Map<String, Object> paramMap);

    /**
     * @author: zzc
     * @date: 2018/7/18 15:10
     * @Description: 根据配置表ID和字段类型集合获取字段对象
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<CommonSelectFieldConfigVO> getFieldListByTableIdAndConfigTypeList(@Param("tableconfigid") String tableconfigid,@Param("configtypelist") List<String> fieldconfigtypes);

    CommonSelectFieldConfigVO getCommonSelectFieldConfigVO(Map<String, String> paramMap);

    /**
     * 获取表的默认字段
     *
     * @param pkTableConfigId
     * @param configType
     * @return
     */
    List<CommonSelectFieldConfigVO> getDefaultAddFields(@Param("pktableconfigid") String pkTableConfigId, @Param("configtype") String configType);

	/**      
	 * @author: xsm
	 * @date: 2018年8月8日 上午9:50:06
	 * @Description: 根据查询条件去查询行政区划信息（跨库查询）
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param paramsOld:查询条件，里面包含有需要查询的行政区划的编码集合
	 * @return    
	 */
	List<Map<String, String>> getRegionListByparams(
			Map<String, Object> paramsOld);

	/**      
	 * @author: xsm
	 * @date: 2018年8月8日 上午9:54:01
	 * @Description: 根据查询条件去查询污染源企业信息（跨库查询）
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param paramsOld:查询条件，里面包含有需要查询的污染源企业ID的集合
	 * @return    
	 */
	List<Map<String, String>> getPollutionListByparams(
			Map<String, Object> paramsOld);
}
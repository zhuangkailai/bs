package com.pub.dao;

import com.pub.model.CommonSelectFieldConfigVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface CommonSelectFieldConfigMapper {

    List<CommonSelectFieldConfigVO> getFieldsByFkTableConfigIdAndConfigType(
            @Param("pktableconfigid") String fkTableConfigId,
            @Param("configtype") String configType);

    List<CommonSelectFieldConfigVO> getOperateLogFieldFlag(
            Map<String, Object> map);


    List<CommonSelectFieldConfigVO> getCommonSelectFieldConfigByMap(Map map);

    List<Map<String, String>> getRelationTableFieldNameList(
            Map<String, Object> params);

    Map<String, Object> getDeleteData(Map<String, Object> paramMap);

    List<CommonSelectFieldConfigVO> getFieldListByTableIdAndConfigTypeList(@Param("tableconfigid") String tableconfigid, @Param("configtypelist") List<String> fieldconfigtypes);



    List<CommonSelectFieldConfigVO> getDefaultAddFields(@Param("pktableconfigid") String pkTableConfigId, @Param("configtype") String configType);


    List<Map<String, String>> getRegionListByparams(
            Map<String, Object> paramsOld);


    List<Map<String, String>> getPollutionListByparams(
            Map<String, Object> paramsOld);
}
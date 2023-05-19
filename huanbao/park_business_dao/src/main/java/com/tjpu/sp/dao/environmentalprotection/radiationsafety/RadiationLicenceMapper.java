package com.tjpu.sp.dao.environmentalprotection.radiationsafety;

import com.tjpu.sp.model.environmentalprotection.radiationsafety.RadiationLicenceVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface RadiationLicenceMapper {
    int deleteByPrimaryKey(String pkLicenceid);

    int insert(RadiationLicenceVO record);

    int insertSelective(RadiationLicenceVO record);

    RadiationLicenceVO selectByPrimaryKey(String pkLicenceid);

    int updateByPrimaryKeySelective(RadiationLicenceVO record);

    int updateByPrimaryKey(RadiationLicenceVO record);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 17:00
    *@Description: 通过自定义参数获取辐射安全许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getRadiationSafetyByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 19:09
    *@Description: 获取辐射安全许证详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getRadiationDetailById(String id);
}
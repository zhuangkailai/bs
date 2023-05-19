package com.tjpu.sp.dao.environmentalprotection.dischargepermit;

import com.tjpu.sp.model.environmentalprotection.dischargepermit.LicenceVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface LicenceMapper {
    int deleteByPrimaryKey(String pkLicenceid);

    int insert(LicenceVO record);

    int insertSelective(LicenceVO record);

    LicenceVO selectByPrimaryKey(String pkLicenceid);

    int updateByPrimaryKeySelective(LicenceVO record);

    int updateByPrimaryKey(LicenceVO record);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 13:43
    *@Description: 通过自定义参数获取排污许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getPermitListByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 15:10
    *@Description: 获取排污许证详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getPermitDetailById(String id);
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 14:37
    *@Description: 通过企业id获取排污许可证统计信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<String,Object>> getPWXKZLicenseByPollutionId(String pollutionid);



    /**
     *@author: xsm
     *@date: 2020/03/24 0024 14:09
     *@Description: 获取最新排污许可证信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [pollutionid]
     *@throws:
     **/
    Map<String,Object>  getNewPWXKZLicenseByPollutionId(Map<String, Object> paramMap);

    Long countPermitNumDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getPermitListDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getPermitDetailInfoById(String id);
}
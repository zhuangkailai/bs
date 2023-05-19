package com.tjpu.sp.service.environmentalprotection.dischargepermit;

import com.tjpu.sp.model.environmentalprotection.dischargepermit.LicenceVO;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/21 0021 11:52
 * @Description: 排污许可证实现层接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface DischargePermitService {

    /**
    *@author: liyc
    *@date: 2019/10/21 0021 13:34
    *@Description: 通过自定义参数获取排污许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getPermitListByParamMap(Map<String,Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 14:32
    *@Description: 通过主键id删除排污许可证列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    void deletePermitById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 14:43
    *@Description: 添加排污许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [licenceVO]
    *@throws:
    **/
    void addPermitInfo(LicenceVO licenceVO);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 14:48
    *@Description: 排污许可证列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    LicenceVO getPermitInfoById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 15:04
    *@Description: 编辑保存排污许可证列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [licenceVO]
    *@throws:
    **/
    void updatePermitInfo(LicenceVO licenceVO);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 15:09
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
    *@date: 2019/11/5 0005 14:36
    *@Description: 通过企业id获取排污许可证统计信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<String,Object>> getPWXKZLicenseByPollutionId(String pollutionid);

    Long countPermitNumDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getPermitListDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getPermitDetailInfoById(String id);
}

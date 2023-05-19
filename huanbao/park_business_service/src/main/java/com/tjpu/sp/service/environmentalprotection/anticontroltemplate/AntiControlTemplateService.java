package com.tjpu.sp.service.environmentalprotection.anticontroltemplate;

import com.tjpu.sp.model.environmentalprotection.anticontroltemplate.AntiControlTemplateConfigVO;

import java.util.List;
import java.util.Map;


public interface AntiControlTemplateService {
    /**
     *@author: xsm
     *@date: 2021/12/28 0028 16:37
     *@Description: 通过自定义参数获取设备反控信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getAntiControlTemplateDataByParamMap(Map<String, Object> paramMap);

    /**
     *@author: xsm
     *@date: 2021/12/29 0029 08:41
     *@Description: 通过模板ID获取模板信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [jsonObject]
     *@throws:
     **/
    AntiControlTemplateConfigVO selectByPrimaryKey(String templateid);

    /**
     *@author: xsm
     *@date: 2021/12/29 0029 08:41
     *@Description: 通过模板ID获取配置字段信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [jsonObject]
     *@throws:
     **/
    List<Map<String,Object>> getAntiControlFieldDataByTemplateid(String templateid);

    /**
     *@author: xsm
     *@date: 2021/12/29 0029 08:41
     *@Description: 通过自定义参数获取配置字段信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [jsonObject]
     *@throws:
     **/
    List<Map<String,Object>> getAntiControlFieldDataByParam(Map<String, Object> param);

    /**
     *@author: xsm
     *@date: 2022/01/05 0005 09:41
     *@Description: 通过自定义参数获取点位监测的污染物信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    List<Map<String,Object>> getPointPollutantDataByParamMap(Map<String, Object> param);

    /**
     *@author: xsm
     *@date: 2022/01/05 0005 09:41
     *@Description: 通过自定义参数获取点位的访问密码
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    Map<String,Object> getOnePointAccessPasswordByParamMap(Map<String, Object> param);

    /**
     *@author: xsm
     *@date: 2022/01/10 0010 16:43
     *@Description: 获取现场端信息编码
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    List<Map<String,Object>> getInformationEncoding();
}

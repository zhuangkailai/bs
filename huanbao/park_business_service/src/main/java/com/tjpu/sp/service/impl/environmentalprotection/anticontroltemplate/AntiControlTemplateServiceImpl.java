package com.tjpu.sp.service.impl.environmentalprotection.anticontroltemplate;

import com.tjpu.sp.dao.environmentalprotection.anticontroltemplate.AntiControlTemplateConfigMapper;
import com.tjpu.sp.model.environmentalprotection.anticontroltemplate.AntiControlTemplateConfigVO;
import com.tjpu.sp.service.environmentalprotection.anticontroltemplate.AntiControlTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AntiControlTemplateServiceImpl implements AntiControlTemplateService {
    @Autowired
    private AntiControlTemplateConfigMapper antiControlTemplateConfigMapper;
    /**
    *@author: xsm
    *@date: 2021/12/28 16:46
    *@Description: 通过自定义参数获取清洁生产信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getAntiControlTemplateDataByParamMap(Map<String, Object> paramMap) {
        return antiControlTemplateConfigMapper.getAntiControlTemplateDataByParamMap(paramMap);
    }

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
    @Override
    public AntiControlTemplateConfigVO selectByPrimaryKey(String templateid) {
        return antiControlTemplateConfigMapper.selectByPrimaryKey(templateid);
    }

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
    @Override
    public List<Map<String, Object>> getAntiControlFieldDataByTemplateid(String templateid) {
        return antiControlTemplateConfigMapper.getAntiControlFieldDataByTemplateid(templateid);
    }

    @Override
    public List<Map<String, Object>> getAntiControlFieldDataByParam(Map<String, Object> param) {
        return antiControlTemplateConfigMapper.getAntiControlFieldDataByParam(param);
    }

    /**
     *@author: xsm
     *@date: 2022/01/05 0005 09:41
     *@Description: 通过自定义参数获取点位监测的污染物信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [jsonObject]
     *@throws:
     **/
    @Override
    public List<Map<String, Object>> getPointPollutantDataByParamMap(Map<String, Object> param) {
        return antiControlTemplateConfigMapper.getPointPollutantDataByParamMap(param);
    }

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
    @Override
    public Map<String, Object> getOnePointAccessPasswordByParamMap(Map<String, Object> param) {
        return antiControlTemplateConfigMapper.getOnePointAccessPasswordByParamMap(param);
    }

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
    @Override
    public List<Map<String, Object>> getInformationEncoding() {
        return antiControlTemplateConfigMapper.getInformationEncoding();
    }

}

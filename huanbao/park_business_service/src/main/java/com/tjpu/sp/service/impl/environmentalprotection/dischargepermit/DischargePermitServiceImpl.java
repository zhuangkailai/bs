package com.tjpu.sp.service.impl.environmentalprotection.dischargepermit;

import com.tjpu.sp.dao.environmentalprotection.dischargepermit.LicenceMapper;
import com.tjpu.sp.model.environmentalprotection.dischargepermit.LicenceVO;
import com.tjpu.sp.service.environmentalprotection.dischargepermit.DischargePermitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/21 0021 11:53
 * @Description: 排污许可证实现层实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class DischargePermitServiceImpl implements DischargePermitService {
    @Autowired
    private LicenceMapper licenceMapper;
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 13:42
    *@Description: 通过自定义参数获取排污许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getPermitListByParamMap(Map<String, Object> paramMap) {
        return licenceMapper.getPermitListByParamMap(paramMap);
    }
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
    @Override
    public void deletePermitById(String id) {
        licenceMapper.deleteByPrimaryKey(id);
    }
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
    @Override
    public void addPermitInfo(LicenceVO licenceVO) {
        licenceMapper.insert(licenceVO);
    }
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
    @Override
    public LicenceVO getPermitInfoById(String id) {
        return licenceMapper.selectByPrimaryKey(id);
    }
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
    @Override
    public void updatePermitInfo(LicenceVO licenceVO) {
        licenceMapper.updateByPrimaryKey(licenceVO);
    }
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
    @Override
    public Map<String, Object> getPermitDetailById(String id) {
        return licenceMapper.getPermitDetailById(id);
    }
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
    @Override
    public List<Map<String, Object>> getPWXKZLicenseByPollutionId(String pollutionid) {
        return licenceMapper.getPWXKZLicenseByPollutionId(pollutionid);
    }

    @Override
    public Long countPermitNumDataByParamMap(Map<String, Object> paramMap) {
        return licenceMapper.countPermitNumDataByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPermitListDataByParamMap(Map<String, Object> paramMap) {
        return licenceMapper.getPermitListDataByParamMap(paramMap);
    }

    @Override
    public Map<String, Object> getPermitDetailInfoById(String id) {
        return  licenceMapper.getPermitDetailInfoById(id);
    }
}

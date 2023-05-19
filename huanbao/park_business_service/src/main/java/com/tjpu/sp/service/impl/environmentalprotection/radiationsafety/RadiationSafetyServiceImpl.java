package com.tjpu.sp.service.impl.environmentalprotection.radiationsafety;

import com.tjpu.sp.dao.environmentalprotection.radiationsafety.RadiationLicenceMapper;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.RadiationLicenceVO;
import com.tjpu.sp.service.environmentalprotection.radiationsafety.RadiationSafetyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/21 0021 16:44
 * @Description: 辐射安全许可证业务层实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class RadiationSafetyServiceImpl implements RadiationSafetyService {

    @Autowired
    private RadiationLicenceMapper radiationLicenceMapper;
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 16:58
    *@Description: 通过自定义参数获取辐射安全许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getRadiationSafetyByParamMap(Map<String, Object> paramMap) {
        return radiationLicenceMapper.getRadiationSafetyByParamMap(paramMap);
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 18:20
    *@Description: 通过主键id删除辐射安全许可证列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public void deleteRadiationById(String id) {
        radiationLicenceMapper.deleteByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 18:35
    *@Description: 添加辐射安全许可证信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [radiationLicenceVO]
    *@throws:
    **/
    @Override
    public void addRadiationInfo(RadiationLicenceVO radiationLicenceVO) {
        radiationLicenceMapper.insert(radiationLicenceVO);
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 18:45
    *@Description: 辐射安全许可证列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public RadiationLicenceVO getRadiationInfoById(String id) {
        return radiationLicenceMapper.selectByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 19:02
    *@Description: 编辑保存辐射安全许可证列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [radiationLicenceVO]
    *@throws:
    **/
    @Override
    public void updateRadiationInfo(RadiationLicenceVO radiationLicenceVO) {
        radiationLicenceMapper.updateByPrimaryKey(radiationLicenceVO);
    }
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
    @Override
    public Map<String, Object> getRadiationDetailById(String id) {
        return radiationLicenceMapper.getRadiationDetailById(id);
    }
}

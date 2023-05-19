package com.tjpu.sp.service.environmentalprotection.radiationsafety;

import com.tjpu.sp.model.environmentalprotection.radiationsafety.RadiationLicenceVO;

import java.util.List;
import java.util.Map; /**
 * @author: liyc
 * @date:2019/10/21 0021 16:43
 * @Description: 辐射安全许可证业务层接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface RadiationSafetyService {
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
    List<Map<String,Object>> getRadiationSafetyByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 18:19
    *@Description: 通过主键id删除辐射安全许可证列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    void deleteRadiationById(String id);
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
    void addRadiationInfo(RadiationLicenceVO radiationLicenceVO);
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
    RadiationLicenceVO getRadiationInfoById(String id);
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
    void updateRadiationInfo(RadiationLicenceVO radiationLicenceVO);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 19:08
    *@Description: 获取辐射安全许证详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getRadiationDetailById(String id);
}

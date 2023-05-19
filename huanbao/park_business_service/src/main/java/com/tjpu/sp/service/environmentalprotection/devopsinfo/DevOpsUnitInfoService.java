package com.tjpu.sp.service.environmentalprotection.devopsinfo;


import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevOpsUnitInfoVO;

import java.util.List;
import java.util.Map;

public interface DevOpsUnitInfoService {

    /**
     * @Author: xsm
     * @Date: 2022/04/01 0001 08:56
     * @Description: 自定义查询条件查询运维单位列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> getDevOpsUnitInfoListDataByParamMap(Map<String,Object> paramMap);

    void addDevOpsUnitInfoInfo(DevOpsUnitInfoVO entity);

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 修改运维单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updateDevOpsUnitInfoInfo(DevOpsUnitInfoVO entity);

    Map<String,Object> getDevOpsUnitInfoDetailById(String id);

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 删除运维单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void deleteDevOpsUnitInfoById(String id);

    List<Map<String,Object>> IsHaveDevOpsUnitInfo(Map<String, Object> param);

    List<Map<String,Object>> countDevOpsUnitDevOpsPointNumDataByParamMap();
}

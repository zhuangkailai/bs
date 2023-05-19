package com.tjpu.sp.service.environmentalprotection.devopsinfo;


import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevOpsPersonnelVO;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevicePersonnelRecordVO;

import java.util.List;
import java.util.Map;

public interface DevOpsPersonnelService {

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
    List<Map<String,Object>> getDevOpsPersonnelListDataByParamMap(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 新增运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void addDevOpsPersonnelInfo(DevOpsPersonnelVO entity, List<DevicePersonnelRecordVO> list);

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 修改运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updateDevOpsPersonnelInfo(DevOpsPersonnelVO entity, List<DevicePersonnelRecordVO> list);

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维人员回显信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getDevOpsPersonnelInfoByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维人员详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getDevOpsPersonnelDetailByID(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 删除运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void deleteDevOpsPersonnelByID(String id);

}

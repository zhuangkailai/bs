package com.tjpu.sp.service.environmentalprotection.devopsinfo;


import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevicePersonnelRecordVO;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;

import java.util.List;
import java.util.Map;

public interface EntDevOpsInfoService {

    /**
     * @author: xsm
     * @date: 2019/12/03 0003 下午 2:08
     * @Description: 根据自定义参数获取企业运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getEntDevOpsInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/03 0003 下午 4:20
     * @Description: 新增企业运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void addEntDevOpsInfos(List<EntDevOpsInfoVO> objlist, String monitorpointtype,List<String>pollutionids);

    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 1:17
     * @Description: 通过自定义参数获取企业运维信息和排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutionAndOutputInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getEntDevOpsDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 下午 14:49
     * @Description: 通过自定义参数获取运维点位列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getEntDevOpsInfoListDataByParamMap(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 新增运维点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void addDevOpsMonitorPointInfo(EntDevOpsInfoVO entity, List<DevicePersonnelRecordVO> list);

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 修改运维监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updateDevOpsMonitorPointInfo(EntDevOpsInfoVO entity, List<DevicePersonnelRecordVO> list);

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 获取运维监测点详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getDevOpsMonitorPointDetailByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 获取运维监测点详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void deleteDevOpsMonitorPointByID(String id);

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 下午 15:25
     * @Description: 根据监测类型和监测点ID获取该监测点的历史运维记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    List<Map<String,Object>> getEntDevOpsHistoryDataByParamMap(Map<String, Object> paramMap);


    void updateDevOpsPlanInfo(List<EntDevOpsInfoVO> onelist, List<DevicePersonnelRecordVO> twolist, List<String> oldpkids);
}

package com.tjpu.sp.service.environmentalprotection.devopsinfo;


import java.util.List;
import java.util.Map;

public interface DevOpsAnalysisService {

    /**
     * @Author: xsm
     * @Date: 2022/03/09 0009 13:09
     * @Description: 统计所有点位各状态点位数量
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    Map<String,Object> countAllPonitStatusDataByParam(Map<String, Object> paramMap);

    /**
     * @Author: xsm
     * @Date: 2022/03/09 0009 14:37
     * @Description: 统计所有正在运维中的设备数量
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    Map<String,Object> countAllDeviceDevOpsNumDataByParam(Map<String, Object> paramMap);

    /**
     * @Author: xsm
     * @Date: 2022/03/09 0009 16:50
     * @Description: 统计有数据缺失的设备数
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    Map<String,Object> countAllDataMissingDeviceNumByParam(Map<String, Object> param);

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 运维工单统计(某月)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> countDeviceDevOpsWorkOrderData(Map<String, Object> param);

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 设备运维分类统计(某月)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> countDeviceDevOpsDataGroupByMonitorType(Map<String, Object> param);

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 运维单位工单统计(企业分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> countDeviceDevOpsDataGroupByPollution(Map<String, Object> param);

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 历史运维单位工单统计分析(月份分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> countDeviceDevOpsDataGroupByMonth(Map<String, Object> param);

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 统计设备传输率(按类型分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> countDeviceTransmissionRateDataGroupByType(Map<String, Object> param);

    /**
     * @Author: xsm
     * @Date: 2022/03/11 0011 09:12
     * @Description: 异常排名统计(按点位分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> countDeviceExceptionRateDataByParamMap(Map<String, Object> param);

    /**
     * @Author: xsm
     * @Date: 2022/03/11 0011 09:12
     * @Description: 获取运维点位分布
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> getAllDeviceDevOpsPointDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countDeviceDevOpsDataGroupByPoint(Map<String, Object> param);
}

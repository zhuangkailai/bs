package com.tjpu.sp.dao.environmentalprotection.online;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OnlineMapper {

    /**
     * @author: zhangzc
     * @date: 2019/8/12 15:39
     * @Description: 根据监测点类型获取监测点信息以及污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOutPutsAndPollutantsByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getGasMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getAirMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getRainMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getStinkMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getVOCMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getFactorySmallStationMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getFactoryStinkMNSByParam(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/8/27 16:43
     * @Description: 获取气象相关监测点mn号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getMnForWeatherByParam(@Param("monitorPointType") Integer monitorPointType);

    /**
     *
     * @author: lip
     * @date: 2019/10/30 0030 下午 5:01
     * @Description: 自定义查询条件获取设备状态表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getDeviceStatusDataByParam(Map<String, Object> paramTemp);
    /**
     *
     * @author: lip
     * @date: 2019/11/15 0015 上午 9:11
     * @Description: 自定义查询条件获取水质站点数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getWaterStationMNSByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutionOutputMnAndStinkMonitorPointMn(Map<String,Object> ParamMap);

    /**
     *
     * @author: lip
     * @date: 2020/1/6 0006 下午 6:22
     * @Description: 自定义查询条件获取rtsp数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getMonitorPointRTSPDataByParam(Map<String, Object> rtspMap);

    List<Map<String,Object>> getOutPutsAndPollutantAlarmSetByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getStinkPointStateAndPollutantsByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getSecurityRiskAreaMonitorPointMNSByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getStorageTankAreaMNSByParam(Map<String, Object> paramMap);
}

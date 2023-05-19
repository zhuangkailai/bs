package com.tjpu.sp.service.environmentalprotection.monitorpoint;

import java.util.List;
import java.util.Map;

public interface MonitorPointService {

    /**
     * @author: xsm
     * @date: 2019/8/22 0022 上午 11:07
     * @Description: 根据监测点类型返回按点位状态分组的点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getGroupByStatusMonitorPointInfoByTypes(List<Integer> monitortypes);


    Object getHourOverDataIsContinueOverJudge(Object mn, String daytime);

    Object getHourDataIsContinueExceptionJudge(Object mn, String daytime);

    Object getHourDataIsContinueChangeJudge(Object mn, String daytime);

    List<Map<String, Object>> getMonitorPointDataTimeSetListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getTimeDataSetByParam(Map<String, Object> paramMap);

    void updateListData(List<Map<String, Object>> updateDataList, Integer monitorpointtype);

    /**
     * @author: xsm
     * @date: 2022/01/11 0011 下午 13:21
     * @Description:统计各类型点位的浓度及排放情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Map<String, Object>> countAllPointFlowAndConcentrationData(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2022/01/12 0012 上午 11:08
     * @Description:统计各类型点位近七天总超标时长排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getEntLastSevenDaysOverTimeRankData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/01/13 0013 上午 08:41
     * @Description:获取当月报警任务处置情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getLastMonthAlarmTaskDisposalByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/01/17 0017 上午 09:46
     * @Description:获取报警统计清单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime,endtime [yyyy-mm-dd]
     * @return:
     */
    Map<String, Object> getAlarmStatisticsInventoryByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getTableTitleForAlarmStatisticsInventory();
}

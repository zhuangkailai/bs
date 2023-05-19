package com.tjpu.sp.service.envhousekeepers.managementportal;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ManagementPortalService {

    /**
     * @author: xsm
     * @date: 2021/09/07 0007 下午 2:23
     * @Description: 获取管委会监督检查巡查任务提醒(管委会端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countManagementCommitteePatrolDataNum();

    /**
     * @author: xsm
     * @date: 2021/09/08 0008 上午 09:56
     * @Description: 获取年度问题企业排行(管委会端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getProblemDataGroupByEntForYearRank(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/09/08 0008 下午 16:34
     * @Description: 统计近一个月企业自查问题情况(管委会端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countLastMonthEntProblemDataSituation(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/09/07 0007 下午 17:09
     * @Description: 获取自查企业整改信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getEntSelfExaminationSituationByParam(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/09/14 0014 上午 9:52
     * @Description:统计所有未完成检查问题和本月新增问题个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> countNotCompleteCheckProblemNum(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/09/16 0016 下午 15:57
     * @Description:整合超标时段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getOverOrExceptionDataByParam(Integer remindCode, Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/09/16 0016 下午 15:57
     * @Description:统计报警model表中当天报警点位
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<String> getOverOrExceptionAlarmMnsByParams(Set<String> mns, String daytime, Integer code);
}

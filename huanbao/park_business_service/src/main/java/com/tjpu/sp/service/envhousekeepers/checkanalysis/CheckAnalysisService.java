package com.tjpu.sp.service.envhousekeepers.checkanalysis;

import java.util.List;
import java.util.Map;

public interface CheckAnalysisService {

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 上午 09:09
     * @Description: 根据检查日期获取企业问题数量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countCheckProblemNumForEntRank(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 上午 09:36
     * @Description: 根据检查日期分组统计各问题类型数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countCheckProblemNumGroupByProblemClass(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 14:52
     * @Description: 根据检查日期分组统计各企业检查次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countCheckNumGroupByEnt(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/08/11 0011 上午 09:01
     * @Description: 根据检查年份和检查表类型分组统计企业巡查情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countCheckNumGroupByMonthDate(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/08/11 0011 下午 13:18
     * @Description: 根据检查年份和检查表类型分组统计企业问题整改情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countProblemRectificationRateDataGroupByEnt(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/08/11 0011 下午 17:45
     * @Description: 根据检查时间段、行业类型和检查表类型分组统计企业问题整改情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countProblemRateDataGroupByIndustryTypeAndEnt(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 上午 8:57
     * @Description: 根据企业ID获取企业巡查任务提醒
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countPollutionPatrolDataByEntID(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 上午 9:59
     * @Description: 根据企业ID获取企业问题类别分组统计(企业端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countPollutionProblemDataGroupByCategory(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 上午 10:52
     * @Description: 根据企业ID获取企业自查次数统计(企业端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countEntSelfCheckNumGroupByMonthByEntID(Map<String, Object> param);
}

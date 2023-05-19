package com.tjpu.sp.service.environmentalprotection.report;


import com.tjpu.sp.model.environmentalprotection.report.ReportInfoVO;

import java.util.List;
import java.util.Map;

public interface ReportManagementService {

    /**
     * @author: xsm
     * @date: 2019/7/24 0024 上午 10:06
     * @Description: 保存报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [reportInfoVO]
     * @throws:
     */
    void insertSelective(ReportInfoVO reportInfoVO);

    /**
     * @author: xsm
     * @date: 2019/7/24 0024 上午 10:26
     * @Description: 根据自定义参数获取相关报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getReportInfosByParamMap(Map<String, Object> paramMap, List<String> reporttypes);

    /**
     * @author: xsm
     * @date: 2019/7/27 0027 下午 3:38
     * @Description: 根据主键ID删除报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    void deleteReportInfo(String pkid);

    /**
     *
     * @author: lip
     * @date: 2019/8/26 0026 上午 11:14
     * @Description: 自定义查询条件获取分析报告属性数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getReportAttributeDataByParam(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2019/8/26 0026 下午 2:34
     * @Description: 暂存分析报告属性数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    void updateReportAttributeDataByParam(Map<String, Object> paramMap);

    void addControlData(Map<String, Object> paramMap);

    List<Map<String, Object>> countReportDataByParam(Map<String, Object> paramMap);
}

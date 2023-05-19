package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetVO;

import java.util.List;
import java.util.Map;

public interface OtherMonitorPointPollutantSetService {


    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 上午 10:18
     * @Description: 通过其他监测点id查询其他监测点相关污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getOtherPollutantsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 上午 10:18
     * @Description: 通过监测点id查询空气站相关污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getOtherPollutantByParamMap(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 6:13
     * @Description: 新增污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, list]
     * @throws:
     */
    int insertPollutants(OtherMonitorPointPollutantSetVO record, List<EarlyWarningSetVO> list);

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:15
     * @Description: 删除污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, list]
     */
    int deletePollutants(String id,  Map<String,Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:17
     * @Description: 修改污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, list]
     * @throws:
     */
    int updatePollutants(OtherMonitorPointPollutantSetVO record, List<EarlyWarningSetVO> list);

    /**
     *
     * @author: xsm
     * @date: 2019/7/8 0008 下午 2:41
     * @Description: 批量新增其它监测点染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void insertOtherMonitorPointPollutantSets(List<OtherMonitorPointPollutantSetVO> otherlist);

    List<Map<String, Object>> getOtherPollutantSetByParam(Map<String, Object> paramMap);
}

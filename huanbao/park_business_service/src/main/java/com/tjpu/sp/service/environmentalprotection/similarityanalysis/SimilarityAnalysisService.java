package com.tjpu.sp.service.environmentalprotection.similarityanalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SimilarityAnalysisService {

    /**
     * @author: xsm
     * @date: 2019/7/19 0019 下午 2:59
     * @Description:获取所有恶臭监测点信息（包括厂界恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getAllStenchMonitorPointInfo();

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 8:43
     * @Description:根据监测点类型获取对应类型的重要污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getAllKeyPollutantsByTypes(List<String> monitorpointtypes);

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 11:09
     * @Description:根据自定义参数获取单个监测点的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getOnePointMonitorDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 下午 1:43
     * @Description:根据自定义参数获取多个监测点的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getMorePointMonitorDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 下午 5:58
     * @Description:根据自定义参数获取监测点相似度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getMonitorPointSimilarityByParamMap(List<Map<String, Object>> listdatas, List<Map<String, Object>> comparelistdatas, Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2019/7/23 0023 上午 8:49
     * @Description:获取所有企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getSelectPollutionInfo();

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 上午 9:48
     * @Description:获取监测点类型为废气的重点污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getGsaOutPutKeyPollutants();

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 上午 10:39
     * @Description:根据自定义参数获取多个排口的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getMoreOutputMonitorDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 下午 1:06
     * @Description:根据自定义参数获取排口相似度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getOutputSimilarityByParamMap(List<Map<String, Object>> listdatas, List<Map<String, Object>> comparelistdatas, Map<String, Object> paramMap);


    List<Map<String, Object>> getMorePointMonitorSimilarityDataByParamMap(Map<String, Object> paramMap, List<Map<String, Object>> otherMonitorPoint);

    /**
     * @author: mmt
     * @date: 2022/9/20 0023 下午 1:06
     * @Description:根据自定义参数获取废水排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getWaterOutputPointInfo(HashMap<String, Object> paramMap);
}

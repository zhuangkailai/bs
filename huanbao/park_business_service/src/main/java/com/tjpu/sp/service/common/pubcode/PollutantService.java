package com.tjpu.sp.service.common.pubcode;

import java.util.List;
import java.util.Map;

public interface PollutantService {
    /**
     * @author: chengzq
     * @date: 2019/5/20 0020 下午 2:34
     * @Description: 通过污染物code集合和类型查询污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getPollutantsByCodesAndType(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/5/30 9:31
     * @Description: 根据监测点类型获取重点监测污染物信息
     * @param: pollutantType  9 是恶臭 、10 voc
     * @return:
     */
    List<Map<String,Object>> getKeyPollutantsByMonitorPointType(Integer pollutantType);

    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 3:56
     * @Description: 根据污染物类型获取该类型下的所有污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutantsByPollutantType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/11 0011 下午 4:24
     * @Description: 通过监测点ID，监测点类型和污染物编码获取污染物的标准值和预警值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getEarlyAndStandardValueByParams(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 4:37
     * @Description: 通过自定义参数获取污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutantinfoByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2020/5/7 0007 上午 9:56
     * @Description: 自定义参数获取污染物预警值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getEarlyValueByParams(Map<String, Object> paramMapTemp);

    void orderPollutantDataByParamMap(List<Map<String, Object>> listdata, String key, Integer code);

    /**
     *
     * @author: xsm
     * @date: 2020/6/16 0016 上午 8:29
     * @Description: 根据MN号获取该MN监测类型的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getPollutantsByDgimn(String mn);

    /**
     *
     * @author: lip
     * @date: 2020/6/28 0028 上午 9:17
     * @Description:  获取全部监测点类型集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getAllMonitorPointTypeList();

    /**
     * @author: xsm
     * @date: 2020/09/07 0007 下午 6:18
     * @Description: 根据监测类型和监测点MN号获取该点位监测污染物的标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getPollutantStandardValueDataByParam(Map<String, Object> param);

    Map<String, Map<String, Map<String, Object>>> getMnAndCodeAndStandardData(Integer monitorpointtype, List<String> outputids);

    /**
     * @author: xsm
     * @date: 2021/01/13 0013 上午 10:02
     * @Description: 通过监测点ID、监测点类型获取该点位监测的污染物信息（安全点位）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getSecurityPointPollutantDataByParams(Map<String, Object> paramMap);


    List<Map<String,Object>> getWaterEarlyAndStandardValueById(Map<String, Object> paramMap);

    List<Map<String,Object>> getSecurityPollutantStandardDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllPollutionPollutants(Map<String,Object> paramMap);



    List<Map<String, Object>> getFlagListByParam(Map<String, Object> f_map);
}

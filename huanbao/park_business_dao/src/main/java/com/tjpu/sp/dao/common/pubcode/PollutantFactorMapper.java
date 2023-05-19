package com.tjpu.sp.dao.common.pubcode;


import com.tjpu.sp.model.common.pubcode.PollutantFactorVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PollutantFactorMapper {
    int deleteByPrimaryKey(Integer pkId);

    int insert(PollutantFactorVO record);

    int insertSelective(PollutantFactorVO record);

    PollutantFactorVO selectByPrimaryKey(Integer pkId);

    int updateByPrimaryKeySelective(PollutantFactorVO record);

    int updateByPrimaryKey(PollutantFactorVO record);

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
     * @date: 2019/5/29 14:54
     * @Description: 获取空气监测站下的污染物信息根据点位IDs
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAirPollutantsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/5/29 15:02
     * @Description: 获取其他监测点位监测污染物根据点位IDs
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOtherPollutantsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 上午 9:47
     * @Description: 根据监测点ID获取水质监测点set表中监测污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getWaterStationPollutantsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/5/30 9:31
     * @Description: 根据监测点类型获取重点监测污染物信息
     * @param: pollutantType  9 是恶臭 、10 voc
     * @return:
     */
    List<Map<String, Object>> getKeyPollutantsByMonitorPointType(@Param("pollutantType") Integer pollutantType);

    /**
     * @author: lip
     * @date: 2019/6/21 0021 上午 11:37
     * @Description: 自定义查询条件获取污染物set信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutantSetInfoByParamMap(Map<String, Object> paramMap);

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
    List<Map<String, Object>> getPollutantsByPollutantType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/11 0011 下午 2:44
     * @Description: 根据监测点ID和污染物编码获取废气该污染物的预警限值和标准限值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getGasEarlyAndStandardValueById(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/11 0011 下午 2:44
     * @Description: 根据监测点ID和污染物编码获取废水该污染物的预警限值和标准限值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getWaterEarlyAndStandardValueById(Map<String, Object> paramMap);

    List<Map<String, Object>> getUnorganizedEarlyAndStandardValueByOutPutId(Map<String, Object> paramMap);

    List<Map<String, Object>> getAirEarlyAndStandardValueByOutPutId(Map<String, Object> paramMap);

    List<Map<String, Object>> getOtherEarlyAndStandardValueByOutPutId(Map<String, Object> paramMap);

    List<Map<String, Object>> getAllKeyPollutantsByMonitorPointTypes(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/01 0001 下午 3:26
     * @Description:根据类型获取关联该类型在线排口的企业信息（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getNotKeyPollutantsByMonitorPointType(Map<String, Object> paramMap);

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
    List<Map<String, Object>> getPollutantinfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/9/3 0003 下午 1:47
     * @Description: 通过污染类型和污染物编码获取相关排放量污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getPollutantInfoByPollutanTypeAndCodes(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/9/19 13:24
     * @Description: 根据水质监测点ID获取监测点set表中监测污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getWaterQualityPollutantsByParamMap(Map<String, Object> tempMap);

    /**
     * @author: xsm
     * @date: 2019/11/07 0007 上午 10:50
     * @Description: 根据监测类型获取重点排放量污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getKeyFlowPollutantsByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2019/12/09 0009 下午 2:15
     * @Description: 根据自定义参数获取因子标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getPollutantStandarddataByParam(Map<String, Object> param);

    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 4:31
     * @Description: 通过自定义参数获取所有企业下的废水，废气，雨水，厂界恶臭，厂界小型站因子
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    List<Map<String,Object>> getAllPollutionPollutantInfoByParamMap(Map<String, Object> param);
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

    List<Map<String,Object>> getWaterStationEarlyAndStandardValueById(Map<String, Object> paramMap);

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
    List<Map<String,Object>> getPollutantsByDgimn(@Param("dgimn") String mn);


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

    List<Map<String,Object>> getPollutantStandardByParams(Map<String, Object> param);

    List<Map<String,Object>> getAllPollutionSecurityPollutants(Map<String, Object> paramMap);

    List<Map<String,Object>> getSecurityPointPollutantDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getSecurityPollutantStandardDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getEnvPollutantStandardDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getPollutantStandardsByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getOutPutPropertyByParam(Map<String,Object> paramMap);

    List<Map<String, Object>> getMonitorPollutantSetDataByParam(Map<String,Object> paramMap);

    List<Map<String, Object>> getAirControlLevelByParam(Map<String, Object> paramMap);



    List<Map<String, Object>> getFlagListByParam(Map<String, Object> f_map);

    Integer getPollutantAccuracyByParamMap(Map<String, Object> param);

    List<Map<String, Object>> getVocPollutantFactorGroupCountData(Map<String, Object> paramMap);
}
package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.GasOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface GasOutPutPollutantSetMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(GasOutPutPollutantSetVO record);

    int insertSelective(GasOutPutPollutantSetVO record);

    GasOutPutPollutantSetVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(GasOutPutPollutantSetVO record);

    int updateByPrimaryKey(GasOutPutPollutantSetVO record);


    /**
     * @author: lip
     * @date: 2019/5/22 0022 下午 1:29
     * @Description: 根据排口ID获取废气污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<GasOutPutPollutantSetVO> getGasOutPutPollutantSetsByOutputId(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/5/29 14:17
     * @Description: 根据排口ID获取废气排口下污染物信息
     * @param:
     * @return:
     */
    List<GasOutPutPollutantSetVO> getGasPollutantsByParamMap(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2019/5/22 0022 下午 1:29
     * @Description: 根据排口ID获取废气污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getGasOutPutPollutantSetByOutputId(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/6/22 0022 下午 7:00
     * @Description: 通过排口id集合查询废气，或废气无组织污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getGasOutPutPollutantSetsByOutputIds(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/9 0009 下午 7:07
     * @Description: 获取set表中突变百分比及污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getOutPutPollutantSetsByParams(Map<String,Object> paramMap);

    List<Map<String,Object>> getGasOutPutPollutantSetInfoByParam(Map<String, Object> paramMap);
    /**
     * @author: chengzq
     * @date: 2020/2/18 0018 下午 8:09
     * @Description: 通过自定义条件获取废水，废气，雨水，水质的污染物报警类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutantSetInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAlarmLevelDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutantStandardDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllGasPollutantInfo(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getPollutantSetDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetListByParam(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getUNGasPollutantSetDataListByParam(Map<String, Object> paramMap);

    void deleteGasOutPutPollutantByID(String id);

    void deleteGasOutPutEarlyWarningSetByID(@Param("fkOutputid")String id);

    List<Map<String,Object>> getOtherPointPollutantSetsByParams(Map<String, Object> paramMap);
}
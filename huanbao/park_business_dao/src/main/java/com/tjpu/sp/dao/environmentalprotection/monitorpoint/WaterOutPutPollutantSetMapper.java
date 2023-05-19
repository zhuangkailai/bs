package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutPutPollutantSetVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface WaterOutPutPollutantSetMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(WaterOutPutPollutantSetVO record);

    int insertSelective(WaterOutPutPollutantSetVO record);

    WaterOutPutPollutantSetVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(WaterOutPutPollutantSetVO record);

    int updateByPrimaryKey(WaterOutPutPollutantSetVO record);

    /**
     *
     * @author: lip
     * @date: 2019/5/22 0022 下午 1:29
     * @Description: 根据自定义查询集合获取废水/雨水污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<WaterOutPutPollutantSetVO> getWaterOrRainPollutantsByParamMap( Map<String,Object> paramMap);
    /**
     *
     * @author: chengzq
     * @date: 2019/5/22 0022 下午 1:29
     * @Description: 根据自定义查询集合获取废水/雨水污染物报警设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getWaterOrRainPollutantByParamMap( Map<String,Object> paramMap);

    /**
    *@author:liyc
    *@date:2019/9/26 0026 16:21
    *@Description: 通过排口id删除报警关联数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    int deleteByOutPutID(Map<String, Object> paramMap);

    List<Map<String,Object>> getWaterOrRainPollutantSetInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutantStandardDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllWaterPollutantInfo(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getPollutantSetDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetListByParam(Map<String, Object> paramMap);
}
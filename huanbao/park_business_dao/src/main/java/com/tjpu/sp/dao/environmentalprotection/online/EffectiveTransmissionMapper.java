package com.tjpu.sp.dao.environmentalprotection.online;

import com.tjpu.sp.model.environmentalprotection.online.EffectiveTransmissionVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EffectiveTransmissionMapper {
    int deleteByPrimaryKey(String pkId);
    int deleteByParamMap(Map<String, Object> paramMap);

    int insert(EffectiveTransmissionVO record);
    int insertBatch(@Param("effectivetransmissionvos") List<EffectiveTransmissionVO> effectivetransmissionvos);

    int insertSelective(EffectiveTransmissionVO record);

    EffectiveTransmissionVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EffectiveTransmissionVO record);

    int updateByPrimaryKey(EffectiveTransmissionVO record);
    /**
     *
     * @author: lip
     * @date: 2019/7/31 0031 下午 1:43
     * @Description: 自定义查询条件获取有效传输数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getEffectiveTransmissionByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/7/31 0031 下午 2:09
     * @Description: 自定义查询条件获取最新时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    String getLastDateByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2020/1/16 0016 上午 11:50
     * @Description: 通过自定义参数获取企业，监测点，传输有效信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutionEffectiveTransmissionInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/1/16 0016 下午 5:21
     * @Description: 通过自定义参数获取监测点传输有效率信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getEffectiveTransmissionInfoByParamMap(Map<String, Object> paramMap);
    List<Map<String,Object>> getMonitorEffectiveTransmissionInfoByParamMap(Map<String, Object> paramMap);
    List<Map<String,Object>> getOutPutEffectiveTransmissionInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getStinkEffectiveTransmissionByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getGasEffectiveTransmissionByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getAllEffectiveTransmissionDataByParam(Map<String, Object> param);

    List<Map<String,Object>> countDeviceTransmissionRateDataGroupByType(Map<String, Object> param);
}
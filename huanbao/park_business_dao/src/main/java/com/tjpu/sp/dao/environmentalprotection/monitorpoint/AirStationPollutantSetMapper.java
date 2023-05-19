package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirStationPollutantSetVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface AirStationPollutantSetMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(AirStationPollutantSetVO record);

    int insertSelective(AirStationPollutantSetVO record);

    AirStationPollutantSetVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(AirStationPollutantSetVO record);

    int updateByPrimaryKey(AirStationPollutantSetVO record);

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
    List<AirStationPollutantSetVO> getAirStationPollutantSetByOutputId(Map<String,Object> paramMap);
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
    List<Map<String,Object>> getAirStationPollutantSetsByMonitorId(Map<String,Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2019/6/5 0005 下午 3:41
     * @Description: 获取城市空气质量污染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getCityAirPollutantSetInfo();

    /**
     *
     * @author: xsm
     * @date: 2019/7/8 0008 下午 1:41
     * @Description: 批量新增空气质量污染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void batchInsert(Map<String, Object> paramMap);

    List<Map<String,Object>> getAirStationPollutantSetInfoByParam(Map<String, Object> paramMap);
}
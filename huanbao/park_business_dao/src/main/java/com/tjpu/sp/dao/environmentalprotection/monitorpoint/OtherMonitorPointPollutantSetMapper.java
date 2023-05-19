package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface OtherMonitorPointPollutantSetMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(OtherMonitorPointPollutantSetVO record);

    int insertSelective(OtherMonitorPointPollutantSetVO record);

    OtherMonitorPointPollutantSetVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(OtherMonitorPointPollutantSetVO record);

    int updateByPrimaryKey(OtherMonitorPointPollutantSetVO record);

    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 上午 10:18
     * @Description: 通过监测点id查询其他监测点相关污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<OtherMonitorPointPollutantSetVO> getOtherMonitorPollutantSetByOutputId(Map<String,Object> paramMap);
    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 上午 10:18
     * @Description: 通过监测点id查询其他监测点相关污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getOtherMonitorPollutantSetsByMonitorId(Map<String,Object> paramMap);

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
    void batchInsert(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllStenchPollutantsByDgimns(Map<String, Object> parammap);

    List<Map<String,Object>> getOtherMonitorPollutantSetInfoByMonitorId(Map<String, Object> paramMap);

    List<Map<String, Object>> getOtherPollutantSetByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getPointMonitorPollutantDataParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantDataParamMap(Map<String, Object> paramMap);

    void deleteByFid(String id);
}
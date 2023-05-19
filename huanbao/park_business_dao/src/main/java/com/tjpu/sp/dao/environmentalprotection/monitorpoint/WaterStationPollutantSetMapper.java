package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationPollutantSetVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface WaterStationPollutantSetMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(WaterStationPollutantSetVO record);

    int insertSelective(WaterStationPollutantSetVO record);

    WaterStationPollutantSetVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(WaterStationPollutantSetVO record);

    int updateByPrimaryKey(WaterStationPollutantSetVO record);


    /**
     * @author: chengzq
     * @date: 2019/9/18 0018 下午 6:10
     * @Description: 根据排口ID获取水质监测点污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<WaterStationPollutantSetVO> getWaterStationPollutantSetByOutputId(Map<String,Object> paramMap);
    /**
    *@author: liyc
    *@date:2019/9/26 0026 9:50
    *@Description: 根据水质监测点ID获取该监测点下所以污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param:  paramMap
    *@throws:
    */
    List<Map<String,Object>> getWaterStationAllPollutantsByIDAndType(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date:2019/9/26 0026 13:58
    *@Description: 通过水质监测站点id获取该监测点的污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param:No [paramMap]
    *@throws:
    */
    List<Map<String,Object>> getWaterStationPollutantSetsByMonitorId(Map<String, Object> paramMap);
    /**
    *@author:liyc
    *@date:2019/10/10 0010 15:01
    *@Description: 通过监测点id查询水质站相关污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    List<WaterStationPollutantSetVO> getWaterStationPollutantSetsByOutputId(Map<String, Object> paramMap);
    /**
    *@author:liyc
    *@date:2019/10/12 0012 11:00
    *@Description: 验证传入数据是否重复
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    int isTableDataHaveInfo(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 13:43
    *@Description: 批量新增水质质量污染物设置信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    void batchInsert(Map<String, Object> paramMap);
}
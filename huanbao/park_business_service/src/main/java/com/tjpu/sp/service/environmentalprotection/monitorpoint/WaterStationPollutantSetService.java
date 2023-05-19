package com.tjpu.sp.service.environmentalprotection.monitorpoint;



import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationPollutantSetVO;

import java.util.List;
import java.util.Map;

public interface WaterStationPollutantSetService {


    /**
     * @author: chengzq
     * @date: 2019/9/18 0018 下午 5:27
     * @Description: 根据排口ID获取水质监测点污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getWaterStationPollutantByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date:2019/9/26 0026 9:45
    *@Description: 根据水质监测点ID获取该监测点下所以污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param:No [paramMap]
    *@throws:
    */
    List<Map<String,Object>> getWaterStationAllPollutantsByIDAndType(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date:2019/9/26 0026 13:48
    *@Description: 通过水质监测站点id获取该监测点的污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    List<Map<String,Object>> getWaterPollutantsByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date:2019/9/26 0026 15:57
    *@Description: 通过主键id，污染物code和水质站点id删除水质监测站污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id,paramMap]
    *@throws:
    */
    int deletePollutants(String id, Map<String, Object> paramMap);
    /**
    *@author:liyc
    *@date:2019/10/9 0009 9:57
    *@Description: 修改污染物set数据以及报警set数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [record,list]
    *@throws:
    */
    int updatePollutants(WaterStationPollutantSetVO record, List<EarlyWarningSetVO> list);
    /**
    *@author:liyc
    *@date:2019/10/9 0009 11:16
    *@Description: 新增污染物set数据以及报警set数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [record,list]
    *@throws:
    */
    int insertPollutants(WaterStationPollutantSetVO record, List<EarlyWarningSetVO> list);
    /**
    *@author:liyc
    *@date:2019/10/10 0010 14:47
    *@Description: 通过监测点id查询空气站相关污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    List<Map<String,Object>> getWaterPollutantByParamMap(Map<String, Object> paramMap);
    /**
    *@author:liyc
    *@date:2019/10/12 0012 10:56
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
    *@date: 2019/11/4 0004 13:37
    *@Description: 批量新增水质质量污染物设置信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [waterlist]
    *@throws:
    **/
    void insertWaterStationPollutantSets(List<WaterStationPollutantSetVO> waterlist);
}

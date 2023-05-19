package com.tjpu.sp.service.environmentalprotection.useelectricfacilitymonitorpoint;


import com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpoint.UseElectricFacilityMonitorPointVO;
import com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpointset.UseElectricFacilityMonitorPointSetVO;

import java.util.List;
import java.util.Map;

public interface UseElectricFacilityMonitorPointService {

    int deleteByPrimaryKey(String pkId);

    int insert(UseElectricFacilityMonitorPointVO record,List<UseElectricFacilityMonitorPointSetVO> pollutants);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(UseElectricFacilityMonitorPointVO record,List<UseElectricFacilityMonitorPointSetVO> pollutants);

    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:37
     * @Description:  通过自定义参数获取用电设施监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getUseElectricFacilityMonitorPointByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:37
     * @Description:  通过id获取用电设施监测点详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> getUseElectricFacilityMonitorPointDetailByID(String pkid);


    /**
     *
     * @author: lip
     * @date: 2020/6/22 0022 下午 4:38
     * @Description: 自定义查询条件获取用电设施点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getOnlineMonitorPointListByParam(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2020/6/23 0023 下午 4:25
     * @Description: 获取产污治污点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getCWAndZWMonitorPointListByParam(Map<String, Object> paramMap);
}

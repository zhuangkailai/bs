package com.tjpu.sp.dao.environmentalprotection.useelectricfacilitymonitorpointset;

import com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpointset.UseElectricFacilityMonitorPointSetVO;

import java.util.List;
import java.util.Map;

public interface UseElectricFacilityMonitorPointSetMapper {
    int deleteByPrimaryKey(String pkId);

    int deleteByfkuseelectricfacilitymonitorpointid(String fkuseelectricfacilitymonitorpointid);

    int insert(UseElectricFacilityMonitorPointSetVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(UseElectricFacilityMonitorPointSetVO record);


    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:37
     * @Description:  通过自定义参数获取用电设施监测点设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getUseElectricFacilityMonitorPointSetByParamMap(Map<String, Object> paramMap);

}
package com.tjpu.sp.dao.environmentalprotection.radiationsafety;

import com.tjpu.sp.model.environmentalprotection.radiationsafety.RayDeviceVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RayDeviceMapper {
    int deleteByPrimaryKey(String pkRaydeviceid);

    int insert(RayDeviceVO record);

    int insertSelective(RayDeviceVO record);

    RayDeviceVO selectByPrimaryKey(String pkRaydeviceid);

    int updateByPrimaryKeySelective(RayDeviceVO record);

    int updateByPrimaryKey(RayDeviceVO record);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:46
    *@Description: 通过自定义参数获取射线装置信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getRayDeviceByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 20:01
    *@Description: 获取射线装置详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getRayDeviceDetailById(String id);
}
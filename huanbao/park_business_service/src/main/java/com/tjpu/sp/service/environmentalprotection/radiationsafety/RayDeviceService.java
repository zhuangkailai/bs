package com.tjpu.sp.service.environmentalprotection.radiationsafety;

import com.tjpu.sp.model.environmentalprotection.radiationsafety.RayDeviceVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/22 0022 19:32
 * @Description: 射线装置信息实现层接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface RayDeviceService {
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:44
    *@Description: 通过自定义参数获取射线装置信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getRayDeviceByParamMap(Map<String,Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:49
    *@Description: 通过主键id删除射线装置单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    void deleteRayDeviceById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:52
    *@Description: 添加射线装置信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [rayDeviceVO]
    *@throws:
    **/
    void addRayDeviceInfo(RayDeviceVO rayDeviceVO);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:55
    *@Description: 射线装置列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    RayDeviceVO getRayDeviceById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:58
    *@Description: 编辑保射线装置列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [rayDeviceVO]
    *@throws:
    **/
    void updateRayDeviceInfo(RayDeviceVO rayDeviceVO);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 20:00
    *@Description: 获取射线装置详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getRayDeviceDetailById(String id);
}

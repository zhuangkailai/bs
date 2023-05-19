package com.tjpu.sp.service.impl.environmentalprotection.radiationsafety;

import com.tjpu.sp.dao.environmentalprotection.radiationsafety.RayDeviceMapper;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.RayDeviceVO;
import com.tjpu.sp.service.environmentalprotection.radiationsafety.RayDeviceService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/22 0022 19:33
 * @Description: 射线装置信息实现层实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class RayDeviceServiceImpl implements RayDeviceService {
    @Autowired
    private RayDeviceMapper rayDeviceMapper;

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
    @Override
    public List<Map<String, Object>> getRayDeviceByParamMap(Map<String, Object> paramMap) {
        return rayDeviceMapper.getRayDeviceByParamMap(paramMap);
    }
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
    @Override
    public void deleteRayDeviceById(String id) {
        rayDeviceMapper.deleteByPrimaryKey(id);
    }
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
    @Override
    public void addRayDeviceInfo(RayDeviceVO rayDeviceVO) {
        rayDeviceMapper.insert(rayDeviceVO);
    }
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
    @Override
    public RayDeviceVO getRayDeviceById(String id) {
        return rayDeviceMapper.selectByPrimaryKey(id);
    }
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
    @Override
    public void updateRayDeviceInfo(RayDeviceVO rayDeviceVO) {
        rayDeviceMapper.updateByPrimaryKey(rayDeviceVO);
    }
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
    @Override
    public Map<String, Object> getRayDeviceDetailById(String id) {
        return rayDeviceMapper.getRayDeviceDetailById(id);
    }
}

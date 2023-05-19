package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.dao.environmentalprotection.monitorpoint.GroundWaterMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GroundWaterVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GroundWaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/12/14 0014 12:55
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class GroundWaterServiceImpl implements GroundWaterService {
    @Autowired
    private GroundWaterMapper groundWaterMapper;
    /**
    *@author: liyc
    *@date: 2019/12/14 0014 13:09
    *@Description: 通过自定义参数获取地下水监测点信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getGroundWaterInfoByParamMap(Map<String, Object> paramMap) {
        return groundWaterMapper.getGroundWaterInfoByParamMap(paramMap);
    }
    /**
    *@author: liyc
    *@date: 2019/12/14 0014 14:33
    *@Description: 添加地下水监测点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [list]
    *@throws:
    **/
    @Override
    public void addGroundWater(List<GroundWaterVO> list) {
        for (GroundWaterVO groundWaterVO : list) {
            groundWaterMapper.insert(groundWaterVO);
        }
    }
    /**
    *@author: liyc
    *@date: 2019/12/14 0014 14:44
    *@Description: 根据主键id删除地下水监测点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public void deleteGroundWaterByID(String id) {
        groundWaterMapper.deleteByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/12/14 0014 14:52
    *@Description: 获取地下水监测点详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public Map<String, Object> getGroundWaterDetailById(String id) {
        return groundWaterMapper.getGroundWaterDetailById(id);
    }
    /**
    *@author: liyc
    *@date: 2019/12/14 0014 16:40
    *@Description: 获取地下水监测点信息(回显)
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public GroundWaterVO getGroundWaterByID(String id) {
        return groundWaterMapper.selectByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/12/17 0017 9:03
    *@Description: 导出地下水监测点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: []
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getTableTitleForSafety() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"监测点名称", "数采仪MN号", "测点控制级别", "中心经度", "中心纬度", "功能区类别"};
        String[] titlefiled = new String[]{"MonitorPointName","DGIMN","controllevename","Longitude","Latitude","waterqualityclassname"};
        for (int i = 0; i < titlefiled.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", titlefiled[i]);
            map.put("label", titlename[i]);
            map.put("align", "center");
            tableTitleData.add(map);
        }
        return tableTitleData;
    }


    /**
     * @author: chengzq
     * @date: 2021/4/13 0013 上午 10:41
     * @Description: 动态条件获取地下水监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOnlineGroundWaterInfoByParamMap(Map<String, Object> paramMap) {
        return groundWaterMapper.getOnlineGroundWaterInfoByParamMap(paramMap);
    }

    @Override
    public String getTargetLevelByDgimn(String dgimn) {
        return groundWaterMapper.getTargetLevelByDgimn(dgimn);
    }
}

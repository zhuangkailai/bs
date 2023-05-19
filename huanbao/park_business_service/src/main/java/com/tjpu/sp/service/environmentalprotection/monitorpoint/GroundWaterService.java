package com.tjpu.sp.service.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.GroundWaterVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/12/14 0014 12:53
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface GroundWaterService {
    /**
    *@author: liyc
    *@date: 2019/12/14 0014 13:08
    *@Description: 通过自定义参数获取地下水监测点信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getGroundWaterInfoByParamMap(Map<String,Object> paramMap);
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
    void addGroundWater(List<GroundWaterVO> list);
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
    void deleteGroundWaterByID(String id);
    /**
    *@author: liyc
    *@date: 2019/12/14 0014 14:50
    *@Description: 获取地下水监测点详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getGroundWaterDetailById(String id);
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
    GroundWaterVO getGroundWaterByID(String id);
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
    List<Map<String,Object>> getTableTitleForSafety();


    /**
     * @author: chengzq
     * @date: 2021/4/13 0013 上午 10:40
     * @Description: 动态条件获取地下水监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    List<Map<String,Object>> getOnlineGroundWaterInfoByParamMap(Map<String,Object> paramMap);

    String getTargetLevelByDgimn(String dataGatherCode);
}

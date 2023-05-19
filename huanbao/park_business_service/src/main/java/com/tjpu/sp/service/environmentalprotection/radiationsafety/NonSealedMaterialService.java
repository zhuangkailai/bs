package com.tjpu.sp.service.environmentalprotection.radiationsafety;

import com.tjpu.sp.model.environmentalprotection.radiationsafety.NonSealedVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/23 0023 9:31
 * @Description: 非密封放射性物质实现层接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface NonSealedMaterialService {
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 9:49
    *@Description: 通过自定义参数获取非密封放射性物质信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getNonSealedByParamMap(Map<String,Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:14
    *@Description: 通过主键id删除非密封放射性物质数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    void deleteNonSealedById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:27
    *@Description: 添加非密封放射性物质列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [nonSealedVO]
    *@throws:
    **/
    void addNonSealedInfo(NonSealedVO nonSealedVO);

    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:33
    *@Description: 非密封放射性物质编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    NonSealedVO getNonSealedById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:46
    *@Description: 编辑保存非密封放射性物质信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [nonSealedVO]
    *@throws:
    **/
    void updateNonSealedInfo(NonSealedVO nonSealedVO);
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:51
    *@Description: 获取非密封放射性物质详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getNonSealedDetailById(String id);
}

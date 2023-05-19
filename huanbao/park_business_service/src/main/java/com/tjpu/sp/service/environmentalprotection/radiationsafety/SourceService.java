package com.tjpu.sp.service.environmentalprotection.radiationsafety;

import com.tjpu.sp.model.environmentalprotection.radiationsafety.SourcesVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/22 0022 16:12
 * @Description: 放射源实现层接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface SourceService {
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 16:34
    *@Description: 通过自定义参数获取放射源信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getSourceListByParamMap(Map<String,Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 17:22
    *@Description: 通过主键id删除放射源单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    void deleteSourceById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:02
    *@Description: 添加放射源信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [sourcesVO]
    *@throws:
    **/
    void addSourceInfo(SourcesVO sourcesVO);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:06
    *@Description: 放射源列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    SourcesVO getSourceInfoById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:12
    *@Description: 编辑保存放射源列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [sourcesVO]
    *@throws:
    **/
    void updateSourceInfo(SourcesVO sourcesVO);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:15
    *@Description: 获取放射源详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getSourceDetailById(String id);
}

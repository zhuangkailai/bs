package com.tjpu.sp.service.environmentalprotection.cleanerproduction;

import com.tjpu.sp.model.environmentalprotection.cleanerproduction.CleanerProductionVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/18 0018 15:15
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface CleanerProductionService {
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 16:13
    *@Description: 通过自定义参数获取清洁生产信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getCleanerInfoByParamMap(Map<String,Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 8:58
    *@Description: 通过主键id删除清洁生产列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    void deleteCleanerInfoById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:20
    *@Description: 清洁生产列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [cleanerProductionVO]
    *@throws:
    **/
    void addCleanerInfo(CleanerProductionVO cleanerProductionVO);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:29
    *@Description: 清洁生产列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    CleanerProductionVO getCleanerInfoById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:46
    *@Description: 编辑保存清洁生产列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [cleanerProductionVO]
    *@throws:
    **/
    void updateCleanerInfo(CleanerProductionVO cleanerProductionVO);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:54
    *@Description: 获取清洁生产的详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getCleanerDetailById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 11:08
    *@Description: 导出清洁生产信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: []
    *@throws:
    **/
    List<Map<String,Object>> getTableTitleForCleaner();
}

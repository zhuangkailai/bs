package com.tjpu.sp.service.environmentalprotection.punishment;

import com.tjpu.sp.model.environmentalprotection.punishment.PunishmentVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/18 0018 9:55
 * @Description: 行政处罚模块接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface AdministrativePunishmentService {

    /**
    *@author: liyc
    *@date: 2019/10/18 0018 10:51
    *@Description: 获取行政处罚信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getPunishmentListPage(Map<String,Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 13:30
    *@Description: 通过主键id删除行政处罚列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    void deletePunishmentById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 13:47
    *@Description: 往行政处罚列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [punishmentVO]
    *@throws:
    **/
    void addPunishmentInfo(PunishmentVO punishmentVO);
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 13:57
    *@Description: 行政处罚列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    PunishmentVO getPunishmentInfoById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 14:09
    *@Description: 编辑保存行政处罚列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [punishmentVO]
    *@throws:
    **/
    void updatePunishmentInfo(PunishmentVO punishmentVO);
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 14:17
    *@Description: 通过主键id获取行政处罚详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getPunishmentDetailById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/19 0019 16:53
    *@Description: 导出行政处罚的信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: []
    *@throws:
    **/
    List<Map<String,Object>> getTableTitleForPunishment();
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 19:06
    *@Description: 根据企业id统计行政处罚信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<String,Object>> countPunishmentByPollutionId(String pollutionid);
}

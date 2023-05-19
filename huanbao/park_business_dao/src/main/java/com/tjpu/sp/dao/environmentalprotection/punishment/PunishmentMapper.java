package com.tjpu.sp.dao.environmentalprotection.punishment;

import com.tjpu.sp.model.environmentalprotection.punishment.PunishmentVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface PunishmentMapper {
    int deleteByPrimaryKey(String pkCaseid);

    int insert(PunishmentVO record);

    int insertSelective(PunishmentVO record);

    PunishmentVO selectByPrimaryKey(String pkCaseid);

    int updateByPrimaryKeySelective(PunishmentVO record);

    int updateByPrimaryKey(PunishmentVO record);
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 10:53
    *@Description:  获取行政处罚信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getPunishmentListPage(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 14:19
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
    *@date: 2019/11/5 0005 19:07
    *@Description: 根据企业id统计行政处罚信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<String,Object>> countPunishmentByPollutionId(String pollutionid);
}
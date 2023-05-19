package com.tjpu.sp.service.impl.environmentalprotection.punishment;

import com.tjpu.sp.dao.environmentalprotection.punishment.PunishmentMapper;
import com.tjpu.sp.model.environmentalprotection.punishment.PunishmentVO;
import com.tjpu.sp.service.environmentalprotection.punishment.AdministrativePunishmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/18 0018 9:56
 * @Description: 行政处罚模块实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class AdministrativePunishmentServiceImpl implements AdministrativePunishmentService {
    @Autowired
    private PunishmentMapper punishmentMapper;
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 10:52
    *@Description: 获取行政处罚信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getPunishmentListPage(Map<String, Object> paramMap) {
        return punishmentMapper.getPunishmentListPage(paramMap);
    }
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
    @Override
    public void deletePunishmentById(String id) {
        punishmentMapper.deleteByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 13:48
    *@Description: 往行政处罚列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [punishmentVO]
    *@throws:
    **/
    @Override
    public void addPunishmentInfo(PunishmentVO punishmentVO) {
        punishmentMapper.insert(punishmentVO);
    }
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
    @Override
    public PunishmentVO getPunishmentInfoById(String id) {
        return punishmentMapper.selectByPrimaryKey(id);
    }
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
    @Override
    public void updatePunishmentInfo(PunishmentVO punishmentVO) {
        punishmentMapper.updateByPrimaryKey(punishmentVO);
    }
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 14:18
    *@Description: 通过主键id获取行政处罚详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public Map<String, Object> getPunishmentDetailById(String id) {
        return punishmentMapper.getPunishmentDetailById(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/19 0019 16:55
    *@Description: 导出行政处罚的信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: []
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getTableTitleForPunishment() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"立案号", "案件名称", "立案时间", "处罚机构", "违法类型", "案件类型"};
        String[] titlefiled = new String[]{"RegisterCode","CaseName","FilingTime","FKPunishUnitName","FKIllegalTypeName","FK_CaseTypeName"};
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
    *@author: liyc
    *@date: 2019/11/5 0005 19:06
    *@Description: 根据企业id统计行政处罚信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> countPunishmentByPollutionId(String pollutionid) {
        return punishmentMapper.countPunishmentByPollutionId(pollutionid);
    }
}

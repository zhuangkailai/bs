package com.tjpu.sp.service.impl.environmentalprotection.cleanerproduction;

import com.tjpu.sp.dao.environmentalprotection.cleanerproduction.CleanerProductionMapper;
import com.tjpu.sp.model.environmentalprotection.cleanerproduction.CleanerProductionVO;
import com.tjpu.sp.service.environmentalprotection.cleanerproduction.CleanerProductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/18 0018 15:16
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class CleanerProductionServiceImpl implements CleanerProductionService {
    @Autowired
    private CleanerProductionMapper cleanerProductionMapper;
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 16:15
    *@Description: 通过自定义参数获取清洁生产信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getCleanerInfoByParamMap(Map<String, Object> paramMap) {
        return cleanerProductionMapper.getCleanerInfoByParamMap(paramMap);
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 8:59
    *@Description: 通过主键id删除清洁生产列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public void deleteCleanerInfoById(String id) {
        cleanerProductionMapper.deleteByPrimaryKey(id);
    }
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
    @Override
    public void addCleanerInfo(CleanerProductionVO cleanerProductionVO) {
        cleanerProductionMapper.insert(cleanerProductionVO);
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:30
    *@Description: 清洁生产列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public CleanerProductionVO getCleanerInfoById(String id) {
        return cleanerProductionMapper.selectByPrimaryKey(id);
    }
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
    @Override
    public void updateCleanerInfo(CleanerProductionVO cleanerProductionVO) {
        cleanerProductionMapper.updateByPrimaryKey(cleanerProductionVO);
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:55
    *@Description: 获取清洁生产的详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public Map<String, Object> getCleanerDetailById(String id) {
        return cleanerProductionMapper.getCleanerDetailById(id);
    }
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
    @Override
    public List<Map<String, Object>> getTableTitleForCleaner() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"咨询机构", "评估时间", "评估机构", "评估结论", "验收时间", "验收机构","验收结论"};
        String[] titlefiled = new String[]{"ConsultOrganizition","AssessDate","AssessOrganizition","AssessRsult","CheckDate","CheckOrganizition","CheckRsult"};
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
}

package com.tjpu.sp.service.impl.environmentalprotection.creditevaluation;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.sp.dao.environmentalprotection.creditevaluation.EnvCreditEvaluationMapper;
import com.tjpu.sp.model.environmentalprotection.creditevaluation.EnvCreditEvaluationVO;
import com.tjpu.sp.service.environmentalprotection.creditevaluation.EnvCreditEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnvCreditEvaluationServiceImpl implements EnvCreditEvaluationService {
    @Autowired
    private EnvCreditEvaluationMapper envCreditEvaluationMapper;

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:根据自定义参数获取环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getEnvCreditEvaluationsByParamMap(Map<String, Object> paramMap) {
        return envCreditEvaluationMapper.getEnvCreditEvaluationsByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:新增环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void insert(EnvCreditEvaluationVO obj) {
        envCreditEvaluationMapper.insert(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:修改环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateByPrimaryKey(EnvCreditEvaluationVO obj) {
        envCreditEvaluationMapper.updateByPrimaryKey(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:根据主键ID删除环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        envCreditEvaluationMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:根据主键ID获取环境信用评价详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getEnvCreditEvaluationDetailByID(String pkid) {
        return envCreditEvaluationMapper.getEnvCreditEvaluationDetailByID(pkid);
    }

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:获取环境信用评价表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getTableTitleForEnforceLawTaskInfo() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"污染源", "评价年份", "评价结论", "复核结论"};
        String[] titlefiled = new String[]{"pollutionname", "evaluationyear", "evaluationrsult", "reviewrsult"};
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
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:根据id获取环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public EnvCreditEvaluationVO selectByPrimaryKey(String id) {
        return envCreditEvaluationMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getEntRegionEvaDataList() {
        return envCreditEvaluationMapper.getEntRegionEvaDataList();
    }

    @Override
    public List<Map<String, Object>> countEntEvaDataList() {
        return envCreditEvaluationMapper.countEntEvaDataList();
    }

    @Override
    public String getLastEntEnvCreditByPid(String pollutionid) {
        return envCreditEvaluationMapper.getLastEntEnvCreditByPid(pollutionid);
    }

    @Override
    public PageInfo<Map<String, Object>> getLastEntEvaDataListByParam(Map<String, Object> paramMap) {
        if (paramMap.get("pagesize")!=null&&paramMap.get("pagenum")!=null) {
            Integer pageSize =Integer.parseInt( paramMap.get("pagesize").toString());
            Integer pageNum =Integer.parseInt( paramMap.get("pagenum").toString());
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Map<String, Object>> listData = envCreditEvaluationMapper.getLastEntEvaDataListByParam(paramMap);
        return new PageInfo<>(listData);
    }


}

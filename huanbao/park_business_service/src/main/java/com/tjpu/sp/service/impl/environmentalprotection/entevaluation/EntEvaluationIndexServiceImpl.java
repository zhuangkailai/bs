package com.tjpu.sp.service.impl.environmentalprotection.entevaluation;

import com.tjpu.sp.dao.environmentalprotection.entevaluation.EntEvaluationIndexMapper;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DeviceDevOpsInfoVO;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationIndexVO;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntEvaluationIndexService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EntEvaluationIndexServiceImpl implements EntEvaluationIndexService {
    @Autowired
    private EntEvaluationIndexMapper entEvaluationIndexMapper;

    /**
     * @Author: xsm
     * @Date: 2022/03/04 0004 09:18
     * @Description: 自定义查询条件查询企业评价指标控列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getEntEvaluationIndexListDataByParamMap(Map<String,Object> parammap) {
        return entEvaluationIndexMapper.getEntEvaluationIndexListDataByParamMap(parammap);
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 上午 9:22
     * @Description: 新增企业评价指标信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addEntEvaluationIndexInfo(EntEvaluationIndexVO entity) {
        entEvaluationIndexMapper.insert(entity);
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 上午 09:21
     * @Description: 修改企业评价指标信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updateEntEvaluationIndexInfo(EntEvaluationIndexVO entity) {
        entEvaluationIndexMapper.updateByPrimaryKey(entity);
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 获取企业评价指标控详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getEntEvaluationIndexDetailById(String id) {
        return entEvaluationIndexMapper.getEntEvaluationIndexDetailById(id);
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 删除企业评价指标信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deleteEntEvaluationIndexById(String id) {
        entEvaluationIndexMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getAllEntEvaluationIndexType() {
        return entEvaluationIndexMapper.getAllEntEvaluationIndexType();
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/04 0004 09:18
     * @Description: 获取企业评价指标评分页面
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getEntEvaluationIndexPageData(Map<String,Object> paramMap) {
        return entEvaluationIndexMapper.getEntEvaluationIndexPageData(paramMap);
    }
}

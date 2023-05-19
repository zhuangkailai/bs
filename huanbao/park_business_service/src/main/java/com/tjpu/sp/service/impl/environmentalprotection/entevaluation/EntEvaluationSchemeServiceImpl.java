package com.tjpu.sp.service.impl.environmentalprotection.entevaluation;

import com.tjpu.sp.dao.environmentalprotection.entevaluation.EntEvaluationSchemeMapper;
import com.tjpu.sp.dao.environmentalprotection.entevaluation.SchemeIndexConfigMapper;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationSchemeVO;
import com.tjpu.sp.model.environmentalprotection.entevaluation.SchemeIndexConfigVO;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntEvaluationSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class EntEvaluationSchemeServiceImpl implements EntEvaluationSchemeService {
    @Autowired
    private EntEvaluationSchemeMapper entEvaluationSchemeMapper;
    @Autowired
    private SchemeIndexConfigMapper schemeIndexConfigMapper;

    /**
     * @Author: xsm
     * @Date: 2022/03/14 0014 13:18
     * @Description: 自定义查询条件查询企业评价方案控列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getEntEvaluationSchemeListDataByParamMap(Map<String, Object> paramMap) {
        return entEvaluationSchemeMapper.getEntEvaluationSchemeListDataByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/03/14 0014 下午 13:24
     * @Description: 新增企业评价方案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addEntEvaluationSchemeInfo(EntEvaluationSchemeVO entity, List<SchemeIndexConfigVO> list) {
        //添加方案信息
        entEvaluationSchemeMapper.insert(entity);
        //批量新增方案指标信息
        if (list.size()>0){
            schemeIndexConfigMapper.batchInsert(list);
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/14 0014 下午 13:32
     * @Description: 修改企业评价方案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updateEntEvaluationSchemeInfo(EntEvaluationSchemeVO entity, List<SchemeIndexConfigVO> list) {
        entEvaluationSchemeMapper.updateByPrimaryKey(entity);
        //先删除 后新增指标详情
        schemeIndexConfigMapper.deleteByEntEvaluationSchemeID(entity.getPkSchemeid());
        if (list.size()>0){
            schemeIndexConfigMapper.batchInsert(list);
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 获取企业评价方案详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getEntEvaluationSchemeDetailById(String id) {
        Map<String, Object> map  = entEvaluationSchemeMapper.getEntEvaluationSchemeDetailById(id);
        List<String> ids = new ArrayList<>();
        List<Map<String, Object>> list = schemeIndexConfigMapper.getSchemeIndexConfigListDataBySchemeID(id);
        if (list!=null&&list.size()>0){
            ids = list.stream().filter(m -> m.get("fkevaluationindexid") != null).map(m -> m.get("fkevaluationindexid").toString()).collect(Collectors.toList());
        }
        map.put("indexlist",ids);
        return map;
    }

    /**
     * @author: xsm
     * @date: 2022/03/14 0014 13:52
     * @Description: 删除企业评价方案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deleteEntEvaluationSchemeById(String id) {
        entEvaluationSchemeMapper.deleteByPrimaryKey(id);
        //删除配置的指标信息
        schemeIndexConfigMapper.deleteByEntEvaluationSchemeID(id);
    }

}

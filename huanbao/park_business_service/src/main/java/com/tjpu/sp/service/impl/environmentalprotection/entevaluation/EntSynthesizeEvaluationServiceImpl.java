package com.tjpu.sp.service.impl.environmentalprotection.entevaluation;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.sp.dao.environmentalprotection.entevaluation.EntEvaluationDetailMapper;
import com.tjpu.sp.dao.environmentalprotection.entevaluation.EntEvaluationIndexMapper;
import com.tjpu.sp.dao.environmentalprotection.entevaluation.EntSynthesizeEvaluationMapper;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationDetailVO;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntSynthesizeEvaluationVO;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntSynthesizeEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class EntSynthesizeEvaluationServiceImpl implements EntSynthesizeEvaluationService {
    @Autowired
    private EntSynthesizeEvaluationMapper entSynthesizeEvaluationMapper;
    @Autowired
    private EntEvaluationIndexMapper entEvaluationIndexMapper;
    @Autowired
    private EntEvaluationDetailMapper entEvaluationDetailMapper;

    //满分100分
    private Double fullMarks=100d;

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
    public List<Map<String, Object>> getEntSynthesizeEvaluationListDataByParamMap(Map<String,Object> parammap) {
        return entSynthesizeEvaluationMapper.getEntSynthesizeEvaluationListDataByParamMap(parammap);
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/07 0007 16:32
     * @Description: 获取企业评价级别
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getEvaluationLevelByParamMap(Map<Object, Object> objectObjectHashMap) {
        return entSynthesizeEvaluationMapper.getEvaluationLevelByParamMap(objectObjectHashMap);
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/07 0007 16:32
     * @Description: 计算企业综合评价分数
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public Double countEvaluationIndex(List<EntEvaluationDetailVO> list) {
        List<Map<String, Object>> evaluationIndexInfo = entEvaluationIndexMapper.getEntEvaluationIndexInfos();
        Map<String, List<Map<String, Object>>> collect = evaluationIndexInfo.stream().filter(m -> m.get("indextype") != null).collect(Collectors.groupingBy(m -> m.get("indextype").toString()));

        Double total=0d;

        for (String IndexType : collect.keySet()) {
            List<Map<String, Object>> maps = collect.get(IndexType);
            List<String> Codes = maps.stream().filter(m -> m.get("pkid") != null).map(m -> m.get("pkid").toString()).collect(Collectors.toList());
            //权重求平均
            Double Weight = maps.stream().filter(m -> m.get("weight") != null).map(m -> m.get("weight")).collect(Collectors.averagingDouble(m->Double.valueOf(m.toString())));


            Double score = list.stream().filter(m -> Codes.contains(m.getFkEntevaluationindexid())).map(m -> m.getIndexevaluationscore()).collect(Collectors.summingDouble(m -> m));

            //如果打分分数*权重>目标分数*权重则设置打分分数为目标分数
            total += score*Weight > fullMarks*Weight ? fullMarks*Weight/100 : score*Weight/100;

        }

        return total;
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/07 0007 16:32
     * @Description: 添加企业评价信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public void addEntSynthesizeEvaluationInfo(EntSynthesizeEvaluationVO entity, List<EntEvaluationDetailVO> list) {
        entSynthesizeEvaluationMapper.insert(entity);
        if (list.size()>0){
            entEvaluationDetailMapper.batchInsert(list);
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/07 0007 16:32
     * @Description: 修改企业评价信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public void updateEntSynthesizeEvaluationInfo(EntSynthesizeEvaluationVO entity, List<EntEvaluationDetailVO> list) {
        entSynthesizeEvaluationMapper.updateByPrimaryKey(entity);
        //先删除 后新增指标详情
        entEvaluationDetailMapper.deleteByEntSynthesizeEvaluationID(entity.getPkId());
        if (list.size()>0){
            entEvaluationDetailMapper.batchInsert(list);
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/07 0007 17:35
     * @Description: 删除企业评价信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public void deleteEntSynthesizeEvaluationById(String id) {
        entSynthesizeEvaluationMapper.deleteByPrimaryKey(id);
        entEvaluationDetailMapper.deleteByEntSynthesizeEvaluationID(id);
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 获取企业综合评价详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getEntSynthesizeEvaluationDetailById(String id) {
        //获取企业综合评价信息
        Map<String, Object> onemap = entSynthesizeEvaluationMapper.getEntSynthesizeEvaluationDetailById(id);
        //获取企业评价各指标的详情信息
        Map<String,Object> param = new HashMap<>();
        param.put("entevaluationid",id);
        List<Map<String,Object>> onelist =  entEvaluationDetailMapper.getEntEvaluationDetailInfoByParam(param);
        onemap.put("indexdata",onelist);
        return onemap;
    }


    /**
     * @Description: 最新评价企业信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/8 11:45
     * @param paramMap
     */
    @Override
    public  PageInfo<Map<String, Object>> getLastEntEvaDataListByParam(Map<String, Object> paramMap) {
        if (paramMap.get("pagesize")!=null&&paramMap.get("pagenum")!=null) {
            Integer pageSize =Integer.parseInt( paramMap.get("pagesize").toString());
            Integer pageNum =Integer.parseInt( paramMap.get("pagenum").toString());
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Map<String, Object>> listData = entSynthesizeEvaluationMapper.getLastEntEvaDataListByParam(paramMap);
        return new PageInfo<>(listData);


    }

    @Override
    public List<Map<String, Object>> countEntEvaDataList() {
        return entSynthesizeEvaluationMapper.countEntEvaDataList();
    }

    @Override
    public List<Map<String, Object>> getEntRegionEvaDataList() {
        return entSynthesizeEvaluationMapper.getEntRegionEvaDataList();
    }

}

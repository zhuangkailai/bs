package com.tjpu.sp.service.environmentalprotection.entevaluation;


import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationIndexVO;

import java.util.List;
import java.util.Map;

public interface EntEvaluationIndexService {

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
    List<Map<String,Object>> getEntEvaluationIndexListDataByParamMap(Map<String,Object> parammap);

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
    void addEntEvaluationIndexInfo(EntEvaluationIndexVO entity);

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
    void updateEntEvaluationIndexInfo(EntEvaluationIndexVO entity);

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
    Map<String,Object> getEntEvaluationIndexDetailById(String id);

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
    void deleteEntEvaluationIndexById(String id);

    List<Map<String,Object>> getAllEntEvaluationIndexType();

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
    List<Map<String,Object>> getEntEvaluationIndexPageData(Map<String,Object> paramMap);
}

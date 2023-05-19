package com.tjpu.sp.service.environmentalprotection.entevaluation;


import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationSchemeVO;
import com.tjpu.sp.model.environmentalprotection.entevaluation.SchemeIndexConfigVO;

import java.util.List;
import java.util.Map;

public interface EntEvaluationSchemeService {

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
    List<Map<String,Object>> getEntEvaluationSchemeListDataByParamMap(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 上午 9:22
     * @Description: 新增企业评价方案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void addEntEvaluationSchemeInfo(EntEvaluationSchemeVO entity, List<SchemeIndexConfigVO> list);

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
    void updateEntEvaluationSchemeInfo(EntEvaluationSchemeVO entity, List<SchemeIndexConfigVO> list);

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
    Map<String,Object> getEntEvaluationSchemeDetailById(String id);

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
    void deleteEntEvaluationSchemeById(String id);
}

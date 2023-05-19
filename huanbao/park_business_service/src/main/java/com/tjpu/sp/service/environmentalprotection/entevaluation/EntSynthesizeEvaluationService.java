package com.tjpu.sp.service.environmentalprotection.entevaluation;


import com.github.pagehelper.PageInfo;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationDetailVO;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntSynthesizeEvaluationVO;

import java.util.List;
import java.util.Map;

public interface EntSynthesizeEvaluationService {

    /**
     * @Author: xsm
     * @Date: 2022/03/04 0004 09:18
     * @Description: 自定义查询条件查询企业综合评价控列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> getEntSynthesizeEvaluationListDataByParamMap(Map<String, Object> parammap);

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
    List<Map<String,Object>> getEvaluationLevelByParamMap(Map<Object, Object> objectObjectHashMap);

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
    Double countEvaluationIndex(List<EntEvaluationDetailVO> list);

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
    void addEntSynthesizeEvaluationInfo(EntSynthesizeEvaluationVO entity, List<EntEvaluationDetailVO> list);

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
    void updateEntSynthesizeEvaluationInfo(EntSynthesizeEvaluationVO entity, List<EntEvaluationDetailVO> list);

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
    void deleteEntSynthesizeEvaluationById(String id);

    Map<String,Object> getEntSynthesizeEvaluationDetailById(String id);

    PageInfo<Map<String, Object>> getLastEntEvaDataListByParam(Map<String, Object> paramMap);



    List<Map<String, Object>> countEntEvaDataList();

    List<Map<String, Object>> getEntRegionEvaDataList();
}

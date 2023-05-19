package com.tjpu.sp.service.environmentalprotection.creditevaluation;


import com.github.pagehelper.PageInfo;
import com.tjpu.sp.model.environmentalprotection.creditevaluation.EnvCreditEvaluationVO;

import java.util.List;
import java.util.Map;

public interface EnvCreditEvaluationService {

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:根据自定义参数获取环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getEnvCreditEvaluationsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:新增环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void insert(EnvCreditEvaluationVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:修改环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateByPrimaryKey(EnvCreditEvaluationVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:根据主键ID删除环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:根据主键ID获取环境信用评价详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getEnvCreditEvaluationDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:获取环境信用评价表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getTableTitleForEnforceLawTaskInfo();

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description:根据id获取环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    EnvCreditEvaluationVO selectByPrimaryKey(String id);

    List<Map<String, Object>> getEntRegionEvaDataList();

    List<Map<String, Object>> countEntEvaDataList();

    String getLastEntEnvCreditByPid(String pollutionid);



    PageInfo<Map<String, Object>> getLastEntEvaDataListByParam(Map<String, Object> paramMap);
}

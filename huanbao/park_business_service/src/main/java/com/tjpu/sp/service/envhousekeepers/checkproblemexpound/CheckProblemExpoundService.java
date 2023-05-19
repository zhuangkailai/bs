package com.tjpu.sp.service.envhousekeepers.checkproblemexpound;

import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.CheckProblemExpoundVO;
import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.RectifiedAndReviewRecordVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;

import java.util.List;
import java.util.Map;

public interface CheckProblemExpoundService {

    /**
     * @author: xsm
     * @date: 2021/06/29 0029 下午 15:37
     * @Description: 根据污染源ID、检查日期、检查类型获取检查项目数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getCheckProblemExpoundsByParamMap(Map<String, Object> param);


    /**
     * @author: xsm
     * @date: 2021/07/07 0007 下午 3:55
     * @Description: 通过id获取问题记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    Map<String,Object> getCheckProblemExpoundDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2021/07/07 0008 上午 9:15
     * @Description: 通过附件id获取相关附件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    Map<String,List<Map<String, Object>>> getFileDataByFileFlags(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/07/07 0008 上午 9:15
     * @Description: 通过问题记录id获取相关流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    List<Map<String,Object>> getCheckProblemExpoundProcedureByID(String id);

    /**
     * @author: xsm
     * @date: 2021/07/15 0015 下午 13:24
     * @Description: 通过问题记录id获取相关流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    List<Map<String,Object>> getOneCheckProbleReportDataByParamMap(Map<String, Object> parammap);

    List<Map<String, Object>> getCheckProblemFileDataByFileFlags(Map<String, Object> parammap);

    /**
     *@author: xsm
     *@date: 2021/07/31 0031 13:30
     *@Description: 添加检查问题信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [addformdata]
     *@throws:
     **/
    void addCheckProblemExpound(CheckProblemExpoundVO oneobj);

    /**
     *@author: xsm
     *@date: 2021/07/31 0031 13:30
     *@Description: 修改检查问题信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [addformdata]
     *@throws:
     **/
    void updateCheckProblemExpound(CheckProblemExpoundVO oneobj);

    /**
     *@author: xsm
     *@date: 2021/07/31 0031 13:43
     *@Description: 通过主键id删除问题单条数据
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [id]
     *@throws:
     **/
    void deleteCheckProblemExpoundById(String id);

    /**
     *@author: xsm
     *@date: 2021/08/02 0002 08:26
     *@Description: 获取超时配置
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    List<Map<String,Object>> getOverdueTimeConfigData();

    /**
     * @author: xsm
     * @date: 2021/08/03 0003 下午 4:12
     * @Description: 通过id获取问题记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    Map<String,Object> getCheckProblemExpoundDataByID(String id);

    CheckProblemExpoundVO selectCheckProblemExpoundByID(String id);

    /**
     * @author: xsm
     * @date: 2021/08/04 0004 上午 9:49
     * @Description: 通过问题记录ID获取最新整改或复查记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramtMap]
     * @throws:
     */
    Map<String,Object> getLastRectifiedAndReviewRecordByParamMap(Map<String, Object> paramtMap);

    /**
     * @author: xsm
     * @date: 2021/08/04 0004 下午 14:12
     * @Description: 自定义参数获取多个问题记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    List<Map<String,Object>> getManyCheckProblemExpoundDataByParamMap(Map<String, Object> paramtMap);

    List<Map<String,Object>> getHistoryDisposalDataByParamMap(Map<String, Object> paramtMap);

    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 17:13
     * @Description: 复查并更新问题状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void updateProblemExpoundDataForReview(CheckProblemExpoundVO obj, RectifiedAndReviewRecordVO objone, List<TaskFlowRecordInfoVO> objlist);

    /**
     * @author: xsm
     * @date: 2021/08/06 0006 上午 08:34
     * @Description: 统计问题历史处置信息数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countEntProblemRectifyReportNumByID(String id);

    /**
     * @author: xsm
     * @date: 2021/09/01 0001 下午 16:35
     * @Description: 根据企业ID和检查类别获取问题信息(当前年)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getCurrentYearEntCheckProblemDataByParamMap(Map<String, Object> param);

    List<Map<String, Object>> getHBProblemDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> countProblemForType(Map<String, Object> paramMap);

    List<Map<String, Object>> getMonthProblemByParam(Map<String, Object> paramMap);

    long getTotalTaskNumByParam(Map<String, Object> paramMap);

    List<String> getUserModuleByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getDataListByParamMap(Map<String, Object> paramMap);
}

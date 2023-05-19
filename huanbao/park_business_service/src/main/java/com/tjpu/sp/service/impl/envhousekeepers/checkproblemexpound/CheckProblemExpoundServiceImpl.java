package com.tjpu.sp.service.impl.envhousekeepers.checkproblemexpound;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.FileInfoMapper;
import com.tjpu.sp.dao.envhousekeepers.checkproblemexpound.CheckProblemExpoundMapper;
import com.tjpu.sp.dao.envhousekeepers.checkproblemexpound.RectifiedAndReviewRecordMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TaskFlowRecordInfoMapper;
import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.CheckProblemExpoundVO;
import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.RectifiedAndReviewRecordVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CheckProblemExpoundServiceImpl implements CheckProblemExpoundService {
    @Autowired
    private CheckProblemExpoundMapper checkProblemExpoundMapper;
    @Autowired
    private FileInfoMapper fileInfoMapper;
    @Autowired
    private RectifiedAndReviewRecordMapper rectifiedAndReviewRecordMapper;
    @Autowired
    private TaskFlowRecordInfoMapper taskFlowRecordInfoMapper;

    /**
     * @author: xsm
     * @date: 2021/06/29 0029 下午 15:37
     * @Description: 根据自定义参数获取检查问题信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getCheckProblemExpoundsByParamMap(Map<String, Object> param) {
        return checkProblemExpoundMapper.getCheckProblemExpoundsByParamMap(param);
    }


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
    @Override
    public Map<String, Object> getCheckProblemExpoundDetailByID(String id) {
        Map<String, Object> result = checkProblemExpoundMapper.getCheckProblemExpoundDetailByID(id);
        if (result!=null) {
            if (result.get("FK_FileID") != null) {
                Map<String, Object> param = new HashMap<>();
                List<Map<String, Object>> datalist = new ArrayList<>();
                if (result.get("FK_FileID")!=null&&!"".equals(result.get("FK_FileID").toString())){
                    param.put("fileflag", result.get("FK_FileID"));
                    datalist = fileInfoMapper.getFileDataByFileflags(param);
                }
                if (datalist != null) {
                    result.put("filedata", datalist);
                } else {
                    result.put("filedata", new ArrayList<>());
                }
            } else {
                result.put("filedata", new ArrayList<>());
            }
        }
        return result;
    }

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
    @Override
    public Map<String,List<Map<String, Object>>> getFileDataByFileFlags(Map<String, Object> param) {
        Map<String,List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> datalist = fileInfoMapper.getFileDataByFileflags(param);
        if (datalist!=null){
            result = datalist.stream().collect(Collectors.groupingBy(m -> m.get("FileFlag").toString()));
        }
        return result;
    }

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
    @Override
    public List<Map<String, Object>> getCheckProblemExpoundProcedureByID(String id) {
        return checkProblemExpoundMapper.getCheckProblemExpoundProcedureByID(id);
    }

    @Override
    public  List<Map<String,Object>> getOneCheckProbleReportDataByParamMap(Map<String, Object> parammap) {
        return checkProblemExpoundMapper.getOneCheckProbleReportDataByParamMap(parammap);
    }

    @Override
    public List<Map<String, Object>> getCheckProblemFileDataByFileFlags(Map<String, Object> parammap) {
        return fileInfoMapper.getFileDataByFileflags(parammap);
    }

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
    @Override
    public void addCheckProblemExpound(CheckProblemExpoundVO oneobj) {
        //oneobj.setRectifycontent("整改结果内容");
        if (StringUtils.isNotBlank(oneobj.getRectifycontent())){
            //完成整改
            addReCheckData(oneobj);

            oneobj.setStatus((short) 3);

        }
        checkProblemExpoundMapper.insert(oneobj);
    }

    private void addReCheckData(CheckProblemExpoundVO oneobj) {
        RectifiedAndReviewRecordVO rectifiedAndReviewRecordVO = new RectifiedAndReviewRecordVO();
        rectifiedAndReviewRecordVO.setPkId(UUID.randomUUID().toString());//主键ID
        rectifiedAndReviewRecordVO.setFkCheckproblemexpoundid(oneobj.getPkId());//任务ID
        rectifiedAndReviewRecordVO.setFkFileid(oneobj.getFkRefileid());//附件ID
        rectifiedAndReviewRecordVO.setManagementtype((short) 1);
        rectifiedAndReviewRecordVO.setManagementtime(oneobj.getManagementtime());
        rectifiedAndReviewRecordVO.setManagementuser(oneobj.getUpdateuser());
        rectifiedAndReviewRecordVO.setRemark(oneobj.getRectifycontent());
        rectifiedAndReviewRecordVO.setUpdatetime(new Date());
        rectifiedAndReviewRecordVO.setUpdateuser(oneobj.getUpdateuser());
        rectifiedAndReviewRecordMapper.insert(rectifiedAndReviewRecordVO);

    }

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
    @Override
    public void updateCheckProblemExpound(CheckProblemExpoundVO oneobj) {
        if (StringUtils.isNotBlank(oneobj.getRectifycontent())){
            //完成整改
            oneobj.setStatus((short) 3);
            addReCheckData(oneobj);
        }
        checkProblemExpoundMapper.updateByPrimaryKey(oneobj);
        TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
        obj.setPkId(UUID.randomUUID().toString());//主键ID
        obj.setFkTaskid(oneobj.getPkId());//任务ID
        obj.setCurrenttaskstatus(CommonTypeEnum.ProblemProcedureRecordStatusEnum.RectifiedEnum.getName().toString());//任务状态
        obj.setFkTasktype(CommonTypeEnum.TaskTypeEnum.CheckProblemExpoundEnum.getCode().toString());//任务类型
        obj.setTaskhandletime(new Date());//被分派该任务的时间
        taskFlowRecordInfoMapper.insert(obj);
    }

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
    @Override
    public void deleteCheckProblemExpoundById(String id) {
        //删除问题记录
        checkProblemExpoundMapper.deleteByPrimaryKey(id);

        //删除问题处置记录（整改、复查）
        rectifiedAndReviewRecordMapper.deleteByCheckProblemExpoundID(id);
        //删除相关流程
        taskFlowRecordInfoMapper.deleteByTaskid(id);
    }

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
    @Override
    public List<Map<String, Object>> getOverdueTimeConfigData() {
        return checkProblemExpoundMapper.getOverdueTimeConfigData();
    }

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
    @Override
    public Map<String, Object> getCheckProblemExpoundDataByID(String id) {
        return checkProblemExpoundMapper.getCheckProblemExpoundDataByID(id);
    }

    @Override
    public CheckProblemExpoundVO selectCheckProblemExpoundByID(String id) {
        return checkProblemExpoundMapper.selectByPrimaryKey(id);
    }

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
    @Override
    public Map<String, Object> getLastRectifiedAndReviewRecordByParamMap(Map<String, Object> paramtMap) {
        return rectifiedAndReviewRecordMapper.getLastRectifiedAndReviewRecordByParamMap(paramtMap);
    }

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
    @Override
    public List<Map<String, Object>> getManyCheckProblemExpoundDataByParamMap(Map<String, Object> paramtMap) {
        List<Map<String, Object>> problemlist  = checkProblemExpoundMapper.getManyCheckProblemExpoundDataByParamMap(paramtMap);
        List<Map<String, Object>> listdata  = rectifiedAndReviewRecordMapper.getRectifiedAndReviewRecordByParamMap(paramtMap);
        Map<String, List<Map<String, Object>>> problemmap = new HashMap<>();
        if (listdata!=null&&listdata.size()>0){
            problemmap = listdata.stream().collect(Collectors.groupingBy(m -> m.get("FK_CheckProblemExpoundID").toString()));
        }
        if (problemlist!=null&&problemlist.size()>0){
            for (Map<String, Object> map:problemlist){
                if (problemmap.get(map.get("PK_ID").toString())!=null){
                    map.put("disposaldata",problemmap.get(map.get("PK_ID").toString()));
                }else{
                    map.put("disposaldata",new ArrayList<>());
                }
            }
        }
        return problemlist;
    }

    @Override
    public List<Map<String, Object>> getHistoryDisposalDataByParamMap(Map<String, Object> paramtMap) {
        return rectifiedAndReviewRecordMapper.getHistoryDisposalDataByParamMap(paramtMap);
    }

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
    @Override
    public void updateProblemExpoundDataForReview(CheckProblemExpoundVO obj, RectifiedAndReviewRecordVO objone, List<TaskFlowRecordInfoVO> objlist) {
        //更新问题状态
        checkProblemExpoundMapper.updateByPrimaryKey(obj);
        //新增问题处置情况记录
        rectifiedAndReviewRecordMapper.insert(objone);
        //新增问题处置流程记录
        if (objlist.size()>0) {
            for (TaskFlowRecordInfoVO threeobj:objlist) {
                taskFlowRecordInfoMapper.insert(threeobj);
            }
        }
    }

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
    @Override
    public List<Map<String, Object>> countEntProblemRectifyReportNumByID(String id) {
        return rectifiedAndReviewRecordMapper.countEntProblemRectifyReportNumByID(id);
    }

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
    @Override
    public List<Map<String, Object>> getCurrentYearEntCheckProblemDataByParamMap(Map<String, Object> param) {
        return checkProblemExpoundMapper.getCurrentYearEntCheckProblemDataByParamMap(param);
    }

    @Override
    public List<Map<String, Object>> getHBProblemDataListByParam(Map<String, Object> paramMap) {
        return checkProblemExpoundMapper.getHBProblemDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> countProblemForType(Map<String, Object> paramMap) {
        return checkProblemExpoundMapper.countProblemForType(paramMap);
    }

    @Override
    public List<Map<String, Object>> getMonthProblemByParam(Map<String, Object> paramMap) {
        return checkProblemExpoundMapper.getMonthProblemByParam(paramMap);
    }

    @Override
    public long getTotalTaskNumByParam(Map<String, Object> paramMap) {
        return checkProblemExpoundMapper.getTotalTaskNumByParam(paramMap);
    }

    @Override
    public List<String> getUserModuleByParam(Map<String, Object> paramMap) {
        return checkProblemExpoundMapper.getUserModuleByParam(paramMap);
    }

    /**
     * @Description: 自定义查询参数获取问题检查列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/11/3 9:26
     */
    @Override
    public List<Map<String, Object>> getDataListByParamMap(Map<String, Object> paramMap) {
        return checkProblemExpoundMapper.getDataListByParamMap(paramMap);
    }
}

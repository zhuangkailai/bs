package com.tjpu.sp.dao.environmentalprotection.petition;

import com.tjpu.sp.model.environmentalprotection.petition.PetitionInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface PetitionInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PetitionInfoVO record);

    int insertSelective(PetitionInfoVO record);

    PetitionInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PetitionInfoVO record);

    int updateByPrimaryKey(PetitionInfoVO record);

    /**
     * @author: xsm
     * @date: 2019/7/25 0025 下午 4:57
     * @Description: 根据监测时间获取该时间段投诉信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getPetitionInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/6 0006 下午 2:05
     * @Description: 根据自定义参数获取投诉信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getComplaintTaskDisposeDataByParams(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2019/8/12 0012 下午 3:42
     * @Description: 自定义查询条件获取任务处置数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getTaskDisposeNumDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getStinkPetitionInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/24 0024 下午 1:17
     * @Description: 自定义查询条件获取某个状态的投诉任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getComplaintTaskDisposeNumDataByParams(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2019/9/3 0003 上午 10:22
     * @Description: 自定义查询条件获取投诉数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getPetitionDataByParam(Map<String, Object> paramMap);
    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 下午 5:06
     * @Description: 获取投诉任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPetitionDetailById(Map<String, Object> paramMap);

    List<Map<String,Object>> countTaskDisposeNumGroupByStatusByParams(Map<String, Object> paramMap);
}
package com.tjpu.sp.dao.common.knowledge;

import com.tjpu.sp.model.common.knowledge.TrainInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TrainInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TrainInfoVO record);

    int insertSelective(TrainInfoVO record);

    TrainInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TrainInfoVO record);

    int updateByPrimaryKey(TrainInfoVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    Map<String, Object> getEditOrDetailsDataById(String id);

    Map<String, Object> getEditDataById(String id);

    List<Map<String, Object>> getStudyUserListById(String id);
}
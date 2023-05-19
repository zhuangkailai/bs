package com.tjpu.sp.dao.common.knowledge;

import com.tjpu.sp.model.common.knowledge.TrainUserInfoVO;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainUserInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TrainUserInfoVO record);

    int insertSelective(TrainUserInfoVO record);

    TrainUserInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TrainUserInfoVO record);

    int updateByPrimaryKey(TrainUserInfoVO record);
}
package com.tjpu.sp.dao.base.pollution;


import com.tjpu.sp.model.base.pollution.PollutionLabelDataVO;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PollutionLabelDataMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PollutionLabelDataVO record);

    int insertSelective(PollutionLabelDataVO record);

    PollutionLabelDataVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PollutionLabelDataVO record);

    int updateByPrimaryKey(PollutionLabelDataVO record);

    int insertLabels(List<PollutionLabelDataVO> records);

    int deleteByPolltionid(String pollutionid);
}
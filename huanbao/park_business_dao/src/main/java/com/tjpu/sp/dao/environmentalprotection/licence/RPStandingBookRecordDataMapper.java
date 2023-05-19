package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPStandingBookRecordDataVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPStandingBookRecordDataMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPStandingBookRecordDataVO record);

    int insertSelective(RPStandingBookRecordDataVO record);

    RPStandingBookRecordDataVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPStandingBookRecordDataVO record);

    int updateByPrimaryKey(RPStandingBookRecordDataVO record);

    List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap);
}
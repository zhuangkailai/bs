package com.tjpu.sp.dao.environmentalprotection.dangerwaste;

import com.tjpu.sp.model.environmentalprotection.dangerwaste.DangerWasteLicenceInfoVO;

import java.util.List;
import java.util.Map;

public interface DangerWasteLicenceInfoMapper {
    int deleteByPrimaryKey(String pkLicenceid);

    int insert(DangerWasteLicenceInfoVO record);

    int insertSelective(DangerWasteLicenceInfoVO record);

    DangerWasteLicenceInfoVO selectByPrimaryKey(String pkLicenceid);

    int updateByPrimaryKeySelective(DangerWasteLicenceInfoVO record);

    int updateByPrimaryKey(DangerWasteLicenceInfoVO record);

    List<Map<String,Object>> getDangerWasteLicenceInfosByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getDangerWasteLicenceInfoDetailByID(String pkid);
}
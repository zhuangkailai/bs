package com.tjpu.sp.dao.envhousekeepers.checkentinfo;

import com.tjpu.sp.model.envhousekeepers.checkentinfo.CheckEntInfoVO;

import java.util.List;
import java.util.Map;

public interface CheckEntInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(CheckEntInfoVO record);

    int insertSelective(CheckEntInfoVO record);

    CheckEntInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(CheckEntInfoVO record);

    int updateByPrimaryKey(CheckEntInfoVO record);

    List<Map<String,Object>> getAllCheckEntInfoGroupByEntAndData(Map<String, Object> param);

    List<Map<String,Object>> IsCheckEntInfoValidByParam(Map<String, Object> paramMap);

    Map<String,Object> getOneCheckEntInfoByParam(Map<String, Object> param);

    List<Map<String,Object>> getCheckEntInfoStatusByParam(Map<String, Object> param);

    void updatecheckEntInfoStatusByParam(Map<String, Object> param);

    List<Map<String,Object>> countPollutionPatrolDataByEntID(Map<String, Object> param);

    List<Map<String,Object>> countEntSelfCheckNumGroupByMonthByEntID(Map<String, Object> param);

    List<Map<String, Object>> getSubmitListData(Map<String, Object> jsonObject);

    List<Map<String,Object>> getEntCheckFeedbackTreeDataByParam(Map<String, Object> param);

    List<Map<String, Object>> getFeedbackDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countManagementCommitteePatrolDataNum();

    List<Map<String,Object>> getEntCheckSubmitDataByParam(Map<String, Object> parammap);

    Map<String,Object> getOneCheckEntDataByParam(Map<String, Object> param);

    List<Map<String, Object>> getAllCheckEntInfoList(Map<String, Object> param);
}
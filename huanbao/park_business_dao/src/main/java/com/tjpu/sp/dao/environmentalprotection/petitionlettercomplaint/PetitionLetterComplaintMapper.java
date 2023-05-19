package com.tjpu.sp.dao.environmentalprotection.petitionlettercomplaint;

import com.tjpu.sp.model.environmentalprotection.petitionlettercomplaint.PetitionLetterComplaintVO;

import java.util.List;
import java.util.Map;

public interface PetitionLetterComplaintMapper {
    int deleteByPrimaryKey(String id);

    int insert(PetitionLetterComplaintVO record);

    int insertSelective(PetitionLetterComplaintVO record);

    PetitionLetterComplaintVO selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PetitionLetterComplaintVO record);

    int updateByPrimaryKey(PetitionLetterComplaintVO record);

    List<Map<String, Object>> getPetitionLetterComplaintsByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getPetitionLetterComplaintDetailByID(String pkid);
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 17:21
    *@Description: 通过企业id统计信访投诉信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<String,Object>> countLetterComplaintByPollutionId(String pollutionid);
}
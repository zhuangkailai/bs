package com.tjpu.sp.dao.environmentalprotection.dangerwaste;

import com.tjpu.sp.model.environmentalprotection.dangerwaste.TransferListVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface TransferListMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TransferListVO record);

    int insertSelective(TransferListVO record);

    TransferListVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TransferListVO record);

    int updateByPrimaryKey(TransferListVO record);

    List<Map<String,Object>> getTransferListsByParamMap(Map<String, Object> paramMap);

    List<String> getTransferlistnumByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getTransferListInfoByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getTransferListDetailByID(String pkid);

    List<Map<String,Object>> countTransferNumDataGroupByYear(Map<String, Object> paramMap);

    List<Map<String,Object>> countTransferNumDataGroupByParentCode(Map<String, Object> paramMap);
}
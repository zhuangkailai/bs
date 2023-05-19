package com.tjpu.sp.dao.environmentalprotection.cjpz;

import com.tjpu.sp.model.environmentalprotection.cjpz.PointAddressSetVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PointAddressSetMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PointAddressSetVO record);

    int insertSelective(PointAddressSetVO record);

    PointAddressSetVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PointAddressSetVO record);

    int updateByPrimaryKey(PointAddressSetVO record);

    void batchInsert(@Param("list") List<PointAddressSetVO> paramList);

    void deleteByEntConnectSetID(String id);

    List<Map<String,Object>> getPointAddressSetsByEntConnectSetID(Map<String, Object> paramMap);
}
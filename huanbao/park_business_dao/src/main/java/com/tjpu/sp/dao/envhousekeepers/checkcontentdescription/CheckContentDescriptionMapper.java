package com.tjpu.sp.dao.envhousekeepers.checkcontentdescription;

import com.tjpu.sp.model.envhousekeepers.checkcontentdescription.CheckContentDescriptionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CheckContentDescriptionMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(CheckContentDescriptionVO record);

    int insertSelective(CheckContentDescriptionVO record);

    CheckContentDescriptionVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(CheckContentDescriptionVO record);

    int updateByPrimaryKey(CheckContentDescriptionVO record);

    void deleteCheckContentDescriptionByCheckEntID(String pkId);

    void batchInsert(@Param("list") List<CheckContentDescriptionVO> paramList);

    List<Map<String,Object>> getCheckContentDescriptionFileDataByParam(Map<String, Object> param);
}
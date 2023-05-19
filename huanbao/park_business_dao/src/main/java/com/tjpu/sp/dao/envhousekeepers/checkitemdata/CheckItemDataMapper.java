package com.tjpu.sp.dao.envhousekeepers.checkitemdata;

import com.tjpu.sp.model.envhousekeepers.checkitemdata.CheckItemDataVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CheckItemDataMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(CheckItemDataVO record);

    int insertSelective(CheckItemDataVO record);

    CheckItemDataVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(CheckItemDataVO record);

    int updateByPrimaryKey(CheckItemDataVO record);

    void batchInsert(@Param("list") List<CheckItemDataVO> paramList);

    List<Map<String,Object>> getAllCheckItemDataByParam(Map<String, Object> param);

    void deleteByCheckEntInfoID(String checkentid);

    List<Map<String,Object>> getRemarkAndFileDataByParam(Map<String, Object> param);

    List<Map<String, Object>> getManyCheckProblemExpoundDataByParamMap(Map<String, Object> param);
}
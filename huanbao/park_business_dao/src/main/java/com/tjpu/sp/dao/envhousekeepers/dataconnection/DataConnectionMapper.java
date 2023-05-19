package com.tjpu.sp.dao.envhousekeepers.dataconnection;

import com.tjpu.sp.model.envhousekeepers.checkitemdata.CheckItemDataVO;
import com.tjpu.sp.model.envhousekeepers.dataconnection.DataConnectionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DataConnectionMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(DataConnectionVO record);

    int insertSelective(DataConnectionVO record);

    DataConnectionVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(DataConnectionVO record);

    int updateByPrimaryKey(DataConnectionVO record);

    void batchInsert(@Param("list") List<DataConnectionVO> paramList);

    void deleteByTemplateConfigID(String pkId);

    List<Map<String,Object>> getCheckContentConnectionDataByParam(Map<String, Object> param);
}
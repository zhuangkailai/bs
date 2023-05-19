package com.pub.dao;


import com.pub.model.CommonSelectTableConfigVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;


@Repository
public interface CommonSelectTableConfigMapper {


	CommonSelectTableConfigVO getTableConfigByName(@Param("tablename") String tableName);



    CommonSelectTableConfigVO getTableConfigVOBySysModel(@Param("sysmodel") String sysModel);

    int getTableHasIdentity(@Param("tableName") String tableName);

    int getMaxNumByTableName(Map<String, Object> paramMap);
}
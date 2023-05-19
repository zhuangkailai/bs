package com.pub.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface GeneralMethodMapper {

    List<Map<String, Object>> getQueryFieldData(Map<String, Object> paramMap);


    List<Map<String, Object>> getListData(Map<String, Object> resultMap);


    void doAddMethod(Map<String, Object> addMap);

    void deleteMethod(Map<String, Object> paramMap);

    Map<String, Object> getUpdateDataById(Map<String, Object> sqlMap);


    List<Map<String, Object>> getRelationTableData(Map<String, Object> sqlMap);


    void batchAddData(Map<String, Object> addMap);


    List<Map<String, Object>> getManyRelationTableData(@Param("sql") String sql);

    void doEditMethod(Map<String, Object> addMap);


    List<Map<String, Object>> isTableDataHaveInfo(Map<String, Object> paramMap);

    List<Map<String, Object>> getTreeData(Map<String, Object> paramMap);


    void batchDelete(Map<String, Object> sqlMap);

    void deleteMiddleTableData(@Param("sql") String deleteMiddleSql);

    String getPKFieldNameByTableName(@Param("tablename") String middleTableName);

    List<Map<String, Object>> getRelationTableForList(@Param("sql") StringBuilder sql, @Param("values") List<String> keyFieldValues);


    List<Map<String, Object>> getDetailRelationFieldData(Map<String, Object> relationSqlMap);

    List<Map<String, Object>> getDeleteIDs(Map<String, Object> map);


    List<String> getDeleteData(@Param("menuid") String menuID);
}
package com.pub.service;

import com.pub.model.CommonSelectTableConfigVO;
import com.pub.model.CommonSelectFieldConfigVO;

import java.util.List;
import java.util.Map;


public interface GeneralMethodService {


    Map<String, Object> getAddPageInfo(List<CommonSelectFieldConfigVO> fieldVOs, CommonSelectTableConfigVO tableVO, Map<String, Object> paramMap);


    void deleteMethod(CommonSelectTableConfigVO tableConfigVO, List<Object> deIDs, List<CommonSelectFieldConfigVO> relationFields, Map<String, Object> params);


    String isTableDataHaveInfo(Map<String, Object> paramMap, CommonSelectTableConfigVO tableConfigVO);


    Map<String, Object> getUserButtonAuthInMenu(String menuID, String userID);

    Map<String, Object> getQueryFields(List<CommonSelectFieldConfigVO> queryFields);




    Map<String, Object> getDetailData(CommonSelectTableConfigVO tableVO, String pkValue, List<CommonSelectFieldConfigVO> detailFieldVOs);




    Map<String, Object> getListData(CommonSelectTableConfigVO tableVO, List<CommonSelectFieldConfigVO> queryFields, List<CommonSelectFieldConfigVO> listInfoFields, List<CommonSelectFieldConfigVO> fields, Map<String, Object> paramMap);


    Map<String, Object> getEditPageData(List<CommonSelectFieldConfigVO> fieldVOs, CommonSelectTableConfigVO tableVO, Map<String, Object> paramMap);

    void doAddMethod(Map<String, Object> addMainTableMap, List<Map<String, Object>> addRelationAddListMapSql, String tableName, String datasource, Map<String, Object> formData, String pkValue);


    String doEditMethod(CommonSelectTableConfigVO tableVO, Map<String, Object> newDataMap, Map<String, Object> oldDataMap, Map<String, Object> mainMap, Map<String, Object> compareResultMap, List<CommonSelectFieldConfigVO> middleFields, Map<String, Object> relationTableDefaultFields, Object dataSource);

    String getPKFieldNameByTableName(String middleTableName);

    List<Map<String,Object>> getDeleteIDs(Map<String, Object> map);
}

package com.pub.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pub.model.CommonSelectTableConfigVO;
import com.pub.service.OperateLogService;
import com.tjpu.pk.common.utils.ConstantsUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.pub.dao.ButtonMapper;
import com.pub.dao.GeneralMethodMapper;
import com.pub.dao.RolesMapper;
import com.pub.dao.UserInfoMapper;
import com.pub.model.ButtonVO;
import com.pub.model.CommonSelectFieldConfigVO;
import com.pub.model.RolesVO;
import com.pub.service.GeneralMethodService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Transactional
public class GeneralMethodServiceImpl implements GeneralMethodService {
    @Autowired
    private ButtonMapper buttonMapper;
    @Autowired
    private GeneralMethodMapper generalMethodMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private RolesMapper rolesMapper;


    @Override
    public Map<String, Object> getListData(CommonSelectTableConfigVO tableVO,
                                           List<CommonSelectFieldConfigVO> queryFieldVOs,
                                           List<CommonSelectFieldConfigVO> listFieldVOs,
                                           List<CommonSelectFieldConfigVO> fields,
                                           Map<String, Object> paramMap) {

        List<CommonSelectFieldConfigVO> defaultOrderByFields = new ArrayList<>();

        List<CommonSelectFieldConfigVO> relationTableVO = new ArrayList<>();

        List<CommonSelectFieldConfigVO> fieldValuesCustom = new ArrayList<>();

        List<CommonSelectFieldConfigVO> timeFormatField = new ArrayList<>();

        List<CommonSelectFieldConfigVO> customOrderByField = new ArrayList<>();


        for (CommonSelectFieldConfigVO field : listFieldVOs) {



            if (StringUtils.isNotBlank(field.getRelationalTable())) {
                relationTableVO.add(field);
            }

            if (StringUtils.isNotBlank(field.getControlValueFormat())) {
                timeFormatField.add(field);
            }
            if (StringUtils.isNotBlank(field.getDefaultOrderType())) {
                if ("asc".equals(field.getDefaultOrderType()) || "desc".equals(field.getDefaultOrderType())) {

                    defaultOrderByFields.add(field);
                }
            }
            if (StringUtils.isNotBlank(field.getCustomOptions())) {
                fieldValuesCustom.add(field);
            }

            if (field.getCustomOrderBy() != null && field.getCustomOrderBy() == 1) {
                customOrderByField.add(field);
            }
        }
        Map<String, Object> sqlMap = new HashMap<>(2);

        String tableName = tableVO.getTableName();
        Map<String, Object> whereSqlMap = getWhereSqlMap(paramMap, queryFieldVOs, tableName);
        sqlMap.put("whereSqlMap", whereSqlMap);

        StringBuilder viewFields = new StringBuilder(getMainViewFields(listFieldVOs, tableVO));
        List<String> removeOrder = new ArrayList<>();
        for (CommonSelectFieldConfigVO orderByField : defaultOrderByFields) {
            String replace = UUID.randomUUID().toString().replace("-", "");
            if (StringUtils.isNotBlank(orderByField.getRelationalTable()) &&
                    StringUtils.isNotBlank(orderByField.getFkNameField())) {
                String s1 = orderByField.getRelationalTable() + "." + orderByField.getFkNameField() + " as " + "A" + replace;
                viewFields.append(",").append(s1);
                removeOrder.add("A" + replace);
            }
        }

        String joinSql = getJoinSql(fields, tableVO);
        String sqlInfo = viewFields + joinSql;
        sqlMap.put("sqlInfo", sqlInfo);

        Boolean isPaging = true;

        String orderSql = CommonServiceSupport.doPageHelpOrderByField(paramMap, customOrderByField, defaultOrderByFields, tableName);
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            int pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
            int pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
            PageHelper.startPage(pageNum, pageSize, orderSql);
        } else {
            PageHelper.orderBy(orderSql);
            isPaging = false;
        }
        List<Map<String, Object>> listData = generalMethodMapper.getListData(sqlMap);
        PageInfo<Map<String, Object>> page = new PageInfo<>(listData);

        List<Map<String, Object>> listInfo = page.getList();
        if (removeOrder.size() > 0) {
            for (Map<String, Object> map : listInfo) {
                removeOrder.forEach(map::remove);
            }
        }

        if (relationTableVO.size() > 0 && listInfo.size() > 0) {
            doRelationListData(listInfo, relationTableVO, tableVO, isPaging);
        }
        for (Map<String, Object> map : listInfo) {
            doResultListData(fieldValuesCustom, timeFormatField, map);
        }
















        Map<String, Object> resultMap = new HashMap<>(6);

        resultMap.put("primarykey", tableVO.getKeyFieldName().toLowerCase());
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            resultMap.put("pagesize", page.getPageSize());
            resultMap.put("pagenum", page.getPageNum());
            resultMap.put("pages", page.getPages());
        }

        resultMap.put("total", page.getTotal());
        resultMap.put("tablelistdata", listInfo);
        return resultMap;
    }


    private void doRelationListData(List<Map<String, Object>> listInfo, List<CommonSelectFieldConfigVO> relationTableFields, CommonSelectTableConfigVO tableVO, Boolean isPaging) {
        for (CommonSelectFieldConfigVO field : relationTableFields) {
            List<String> keyFieldValues = new ArrayList<>();
            String key1 = "field9527";
            String key2 = "field9528";
            String listInfoFieldKey = "";
            String flag = "";
            StringBuilder sql = new StringBuilder();

            if (StringUtils.isNotBlank(field.getRelationalTable()) && StringUtils.isNotBlank(field.getMiddleTable())) {
                flag = "mtm";
                listInfoFieldKey = tableVO.getKeyFieldName();
                sql.append("select ")
                        .append(tableVO.getTableName()).append(".").append(tableVO.getKeyFieldName()).append(" as ").append(key1).append(",")
                        .append(field.getRelationalTable()).append(".").append(field.getFkNameField()).append(" as ").append(key2)
                        .append(" from ")
                        .append(tableVO.getTableName())
                        .append(" join ")
                        .append(field.getMiddleTable())
                        .append(" on ")
                        .append(field.getMiddleTable()).append(".").append(field.getLeftId())
                        .append(" = ")
                        .append(tableVO.getTableName()).append(".").append(tableVO.getKeyFieldName())
                        .append(" join ")
                        .append(field.getRelationalTable())
                        .append(" on ")
                        .append(field.getRelationalTable()).append(".").append(field.getFkKeyField())
                        .append(" = ")
                        .append(field.getMiddleTable()).append(".").append(field.getRightId());
                if (StringUtils.isNotBlank(field.getQueryFieldSql()) || isPaging) {
                    sql.append(" where ");
                    if (StringUtils.isNotBlank(field.getQueryFieldSql())) {
                        sql.append(field.getQueryFieldSql());
                        if (isPaging) {
                            sql.append(" and ");
                        }
                    }
                    if (isPaging) {
                        getIsPagingDataByKeyInListInfo(listInfo, keyFieldValues, listInfoFieldKey);
                        if (keyFieldValues.size() > 0) {
                            sql.append(tableVO.getTableName()).append(".").append(tableVO.getKeyFieldName()).append(" in ");
                        }
                    }
                }
            } else if (StringUtils.isNotBlank(field.getRelationalTable()) && StringUtils.isBlank(field.getMiddleTable())) {
                listInfoFieldKey = StringUtils.isNotBlank(field.getOtherFieldName()) ? field.getOtherFieldName() : field.getFieldName();
                if (StringUtils.isNotBlank(field.getControlType()) && field.getControlType().equals(ConstantsUtil.ControlTypeEnum.array.getValue())) {
                    flag = ",";
                    sql.append("select DISTINCT ")
                            .append(field.getFkKeyField()).append(" as ").append(key1).append(",")
                            .append(field.getFkNameField()).append(" as ").append(key2)
                            .append(" from ")
                            .append(field.getRelationalTable());
                } else {
                    flag = "otm";
                    sql.append("select DISTINCT ").append(tableVO.getTableName()).append(".").append(field.getFieldName()).append(" as ").append(key1).append(",")
                            .append(field.getRelationalTable()).append(".").append(field.getFkNameField()).append(" as ").append(key2)
                            .append(" from ")
                            .append(tableVO.getTableName())
                            .append(" join ")
                            .append(field.getRelationalTable())
                            .append(" on ")
                            .append(tableVO.getTableName()).append(".").append(field.getFieldName())
                            .append(" = ")
                            .append(field.getRelationalTable()).append(".").append(field.getFkKeyField());
                    if (isPaging) {
                        getIsPagingDataByKeyInListInfo(listInfo, keyFieldValues, listInfoFieldKey);
                        if (keyFieldValues.size() > 0) {
                            sql.append(" where ")
                                    .append(tableVO.getTableName()).append(".").append(field.getFieldName()).append(" in ");
                        }
                    }
                }
            }
            List<Map<String, Object>> relationTableList = generalMethodMapper.getRelationTableForList(sql, keyFieldValues);
            if (!flag.equals(",")) {
                for (Map<String, Object> map : listInfo) {
                    StringBuilder values = new StringBuilder();
                    Object codeValue = map.get(listInfoFieldKey);
                    if (codeValue != null) {
                        for (Map<String, Object> mapInfo : relationTableList) {
                            Object codeValueInfo = mapInfo.get(key1);
                            if (codeValue.equals(codeValueInfo)) {
                                Object value = mapInfo.get(key2);
                                if (value != null) {
                                    values.append(value).append("、");
                                }
                            }
                        }
                        if (values.length() > 0) {
                            values.deleteCharAt(values.lastIndexOf("、"));
                        }
                        map.put(StringUtils.isNotBlank(field.getOtherFieldName()) ? field.getOtherFieldName() : field.getFieldName(), StringUtils.isBlank(values.toString()) ? null : values.toString());
                    }
                }
            } else {
                for (Map<String, Object> map : listInfo) {
                    StringBuilder values = new StringBuilder();
                    Object codeValue = map.get(listInfoFieldKey);
                    if (codeValue != null) {
                        String[] codeValues = codeValue.toString().split(",");
                        for (String code : codeValues) {
                            for (Map<String, Object> datum : relationTableList) {
                                if (datum.get(key1) != null) {
                                    Object codeInfo = datum.get(key1);
                                    if (codeInfo.equals(code)) {
                                        Object value = datum.get(key2);
                                        if (value != null && StringUtils.isNotBlank(value.toString())) {
                                            values.append(value).append("、");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (values.length() > 0) {
                        values.deleteCharAt(values.lastIndexOf("、"));
                    }
                    map.put(StringUtils.isNotBlank(field.getOtherFieldName()) ? field.getOtherFieldName() : field.getFieldName(), StringUtils.isBlank(values.toString()) ? null : values.toString());
                }
            }
        }

    }

    private void getIsPagingDataByKeyInListInfo(List<Map<String, Object>> listInfo, List<String> keyFieldValues, String fieldName) {
        for (Map<String, Object> map : listInfo) {
            if (map.get(fieldName) != null && StringUtils.isNotBlank(map.get(fieldName).toString())) {
                String s = map.get(fieldName).toString();
                if (!keyFieldValues.contains(s)) {
                    keyFieldValues.add(s);
                }
            }
        }
    }


    private String getMainViewFields(List<CommonSelectFieldConfigVO> listFieldVOs, CommonSelectTableConfigVO tableVO) {
        String tableName = tableVO.getTableName();

        StringBuilder mainTableViewFields = new StringBuilder();
        mainTableViewFields.append("SELECT DISTINCT ");
        for (CommonSelectFieldConfigVO fieldVO : listFieldVOs) {
            if (StringUtils.isBlank(fieldVO.getMiddleTable())) {
                if (StringUtils.isNotBlank(fieldVO.getOtherFieldName())) {
                    mainTableViewFields.append(tableName).append(".").append(fieldVO.getFieldName()).append(" as ").append(fieldVO.getOtherFieldName()).append(",");
                } else {
                    mainTableViewFields.append(tableName).append(".").append(fieldVO.getFieldName()).append(",");
                }
            }
        }
        String pkFieldNameCol = tableName + "." + tableVO.getKeyFieldName() + ",";
        if (!mainTableViewFields.toString().contains(pkFieldNameCol)) {
            mainTableViewFields.append(pkFieldNameCol);
        }
        return mainTableViewFields.deleteCharAt(mainTableViewFields.lastIndexOf(",")).toString();
    }

    private String getJoinSql(List<CommonSelectFieldConfigVO> listFields, CommonSelectTableConfigVO tableVO) {
        Map<String, CommonSelectFieldConfigVO> fieldsMap = new LinkedHashMap<>();
        Map<String, CommonSelectFieldConfigVO> manyToMany = new HashMap<>();
        List<CommonSelectFieldConfigVO> oneToManyList = new ArrayList<>();
        for (CommonSelectFieldConfigVO field : listFields) {
            if (StringUtils.isNotBlank(field.getRelationalTable()) && !fieldsMap.containsKey(field.getFieldName())) {
                if (StringUtils.isNotBlank(field.getRelationalTable()) && StringUtils.isNotBlank(field.getMiddleTable()) && !manyToMany.containsKey(field.getMiddleTable())) {
                    manyToMany.put(field.getMiddleTable(), field);

                } else if (StringUtils.isNotBlank(field.getRelationalTable()) && StringUtils.isBlank(field.getMiddleTable())) {
                    oneToManyList.add(field);
                }
                fieldsMap.put(field.getFieldName(), field);
            }
        }


        Map<String, List<CommonSelectFieldConfigVO>> map = new LinkedHashMap<>();
        for (CommonSelectFieldConfigVO vo : oneToManyList) {
            String relationalTable = vo.getRelationalTable();
            if (map.containsKey(relationalTable)) {
                List<CommonSelectFieldConfigVO> listVos = map.get(relationalTable);
                listVos.add(vo);
                map.replace(relationalTable, listVos);
            } else {
                List<CommonSelectFieldConfigVO> listVos = new ArrayList<>();
                listVos.add(vo);
                map.put(relationalTable, listVos);
            }
        }

        String tableName = tableVO.getTableName();
        String pkName = tableVO.getKeyFieldName();
        StringBuilder joinSql = new StringBuilder();
        joinSql.append(" from ").append(tableName);

        String joinType = " left join ";
        String key;
        for (Map.Entry<String, CommonSelectFieldConfigVO> entry : manyToMany.entrySet()) {
            CommonSelectFieldConfigVO fieldVO = entry.getValue();
            if (StringUtils.isNotBlank(fieldVO.getTableJoinType())) {
                joinType = " " + fieldVO.getTableJoinType() + " ";
            } else {
                joinType = " left join ";
            }

            String middleTable = entry.getKey();
            String rightId = fieldVO.getRightId();
            String leftId = fieldVO.getLeftId();
            String relationalTable = fieldVO.getRelationalTable();
            String fkKeyField = fieldVO.getFkKeyField();
            key = middleTable + " on " + middleTable + "." + leftId + " = " + tableName + "." + pkName;
            if (!joinSql.toString().contains(key)) {
                joinSql.append(joinType).append(key);
            }
            key = relationalTable + " on " + relationalTable + "." + fkKeyField + " = " + middleTable + "." + rightId;
            if (!joinSql.toString().contains(key)) {
                joinSql.append(joinType).append(key);
            }
        }

        for (Map.Entry<String, List<CommonSelectFieldConfigVO>> entry : map.entrySet()) {
            List<CommonSelectFieldConfigVO> listVo = entry.getValue();
            String relationTableName = entry.getKey();
            if (listVo.size() == 1) {
                CommonSelectFieldConfigVO vo = listVo.get(0);
                String fkKeyField = vo.getFkKeyField();
                if (StringUtils.isNotBlank(vo.getTableJoinType())) {
                    joinType = " " + vo.getTableJoinType() + " ";
                } else {
                    joinType = " left join ";
                }
                key = relationTableName + " on " + relationTableName + "." + fkKeyField + " = " + tableName + "." + vo.getFieldName();
                if (!joinSql.toString().contains(key)) {
                    joinSql.append(joinType).append(key);
                }
            } else if (listVo.size() > 1) {
                StringBuilder info = new StringBuilder();
                for (CommonSelectFieldConfigVO vo : listVo) {
                    String fieldName = vo.getFieldName();
                    String fkKeyField = vo.getFkKeyField();
                    info.append(relationTableName).append(".").append(fkKeyField).append(" = ").append(tableName).append(".").append(fieldName).append(" and ");
                }

                key = relationTableName + " on ( " + info.subSequence(0, info.length() - 5) + " ) ";
                joinSql.append(joinType).append(key);
            }
        }
        return joinSql.toString();
    }


    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDetailData(CommonSelectTableConfigVO tableVo, String pkValue, List<CommonSelectFieldConfigVO> detailFieldVOs) {
        Map<String, Object> resultInfo = new HashMap<>(4);

        if (detailFieldVOs.size() > 0) {

            List<CommonSelectFieldConfigVO> middleFields = new ArrayList<>();

            List<CommonSelectFieldConfigVO> relationFields = new ArrayList<>();

            List<CommonSelectFieldConfigVO> authFields = new ArrayList<>();

            List<CommonSelectFieldConfigVO> mainFields = new ArrayList<>();

            List<String> queryField = new ArrayList<>();
            for (CommonSelectFieldConfigVO vo : detailFieldVOs) {
                if (StringUtils.isBlank(vo.getRelationalTable()) && StringUtils.isBlank(vo.getMiddleTable())) {
                    mainFields.add(vo);
                    if (!queryField.contains(vo.getFieldName())) {
                        queryField.add(vo.getFieldName());
                    }
                } else if (StringUtils.isNotBlank(vo.getRelationalTable()) && StringUtils.isBlank(vo.getMiddleTable())) {
                    relationFields.add(vo);
                    if (!queryField.contains(vo.getFieldName())) {
                        queryField.add(vo.getFieldName());
                    }
                } else if (StringUtils.isNotBlank(vo.getRelationalTable()) && StringUtils.isNotBlank(vo.getMiddleTable())) {
                    middleFields.add(vo);
                } else if (StringUtils.isBlank(vo.getRelationalTable()) && StringUtils.isNotBlank(vo.getMiddleTable())) {
                    authFields.add(vo);
                }
            }

            if (!queryField.contains(tableVo.getKeyFieldName())) {
                queryField.add(tableVo.getKeyFieldName());
            }
            Map<String, Object> sqlMap = new HashMap<>(3);
            sqlMap.put("queryField", queryField);
            sqlMap.put("tableName", tableVo.getTableName());
            sqlMap.put("key", tableVo.getKeyFieldName());
            sqlMap.put("value", pkValue);

            Map<String, Object> mainResultInfo = generalMethodMapper.getUpdateDataById(sqlMap);
            if (mainResultInfo != null && mainFields.size() > 0) {
                List<Map<String, Object>> detailData = new ArrayList<>();
                String arrayType = ConstantsUtil.ControlTypeEnum.array.getValue();
                for (CommonSelectFieldConfigVO fieldVO : mainFields) {
                    Map<String, Object> detailMapInfo = new HashMap<>(3);
                    String stringOrArray = "string";
                    Object valueInfo = mainResultInfo.get(fieldVO.getFieldName());

                    if (valueInfo != null && valueInfo instanceof Date) {
                        if (StringUtils.isNotBlank(fieldVO.getControlValueFormat())) {
                            valueInfo = DataFormatUtil.formatDateToOtherFormat((Date) valueInfo, fieldVO.getControlValueFormat());
                        }
                    }

                    if (StringUtils.isNotBlank(fieldVO.getControlType()) && fieldVO.getControlType().equals(arrayType)) {
                        stringOrArray = arrayType;
                        List<Object> values = new ArrayList<>();
                        if (null != valueInfo && valueInfo != "") {

                            if (StringUtils.isNotBlank(fieldVO.getCustomOptions())) {
                                String customOptions = fieldVO.getCustomOptions();
                                String substring = customOptions.substring(1, customOptions.length());
                                JSONObject jsonObject = JSONObject.fromObject(substring);
                                String[] valueInfos = valueInfo.toString().split(",");
                                for (String s : valueInfos) {
                                    if (jsonObject.get(s) != null) {
                                        values.add(jsonObject.get(s));
                                    }
                                }
                            }
                        }
                        detailMapInfo.put("value", values);
                    } else {
                        if (StringUtils.isNotBlank(fieldVO.getCustomOptions())) {
                            String customOptions = fieldVO.getCustomOptions();
                            String substring = customOptions.substring(1, customOptions.length());
                            JSONObject jsonObject = JSONObject.fromObject(substring);
                            if (null != valueInfo && valueInfo != "") {
                                if (jsonObject.get(valueInfo.toString()) != null) {
                                    valueInfo = jsonObject.get(valueInfo.toString());
                                }
                            } else {
                                if (StringUtils.isNotBlank(fieldVO.getControlDefault())) {
                                    String controlDefaultCode = fieldVO.getControlDefault();
                                    if (jsonObject.get(controlDefaultCode) != null) {
                                        valueInfo = jsonObject.get(controlDefaultCode);
                                    }
                                }
                            }
                        }
                        detailMapInfo.put("value", valueInfo);
                    }
                    detailMapInfo.put("type", stringOrArray);
                    detailMapInfo.put("controlType", fieldVO.getControlType());
                    detailMapInfo.put("label", fieldVO.getFieldComments());
                    detailMapInfo.put("fieldName", fieldVO.getFieldName());
                    if (fieldVO.getShowPage() != null) {
                        detailMapInfo.put("showhide", fieldVO.getShowPage() != 0);
                    } else {
                        detailMapInfo.put("showhide", true);
                    }
                    detailMapInfo.put("width", fieldVO.getControlWidth());
                    detailMapInfo.put("orderNum", fieldVO.getOrderIndex() == null ? 0 : fieldVO.getOrderIndex());
                    detailData.add(detailMapInfo);
                }

                if (relationFields.size() > 0) {

                    for (CommonSelectFieldConfigVO fieldVO : relationFields) {
                        String fieldName = fieldVO.getFieldName();
                        Map<String, Object> detailDataMap = new LinkedHashMap<>();
                        String controlype = "string";
                        if (mainResultInfo.get(fieldName) != null && mainResultInfo.get(fieldName) != "") {
                            Map<String, Object> relationSqlMap = new HashMap<>();

                            Object fkValue = mainResultInfo.get(fieldName);
                            if (StringUtils.isNotBlank(fieldVO.getControlType()) && fieldVO.getControlType().equals(arrayType)) {
                                controlype = arrayType;
                                String[] split = fkValue.toString().split(",");
                                relationSqlMap.put("queryFieldValues", split);
                            } else {
                                relationSqlMap.put("queryFieldValue", fkValue);
                            }
                            if (StringUtils.isNotBlank(fieldVO.getQueryFieldSql())) {
                                relationSqlMap.put("queryFieldSql", fieldVO.getQueryFieldSql());
                            }
                            relationSqlMap.put("filedName", fieldVO.getFkNameField());
                            relationSqlMap.put("tableName", fieldVO.getRelationalTable());
                            relationSqlMap.put("queryField", fieldVO.getFkKeyField());
                            List<Map<String, Object>> relationFieldData = generalMethodMapper.getDetailRelationFieldData(relationSqlMap);
                            if (controlype.equals(arrayType)) {
                                List<Object> viewValue = new ArrayList<>();
                                for (Map<String, Object> datum : relationFieldData) {
                                    viewValue.add(datum.get(fieldVO.getFkNameField()));
                                }
                                detailDataMap.put("value", viewValue);
                            } else {
                                if (relationFieldData.size() > 0) {
                                    detailDataMap.put("value", relationFieldData.get(0).get(fieldVO.getFkNameField()));
                                } else {
                                    detailDataMap.put("value", "");
                                }
                            }
                        }

                        detailDataMap.put("type", controlype);
                        detailDataMap.put("label", fieldVO.getFieldComments());
                        if (fieldVO.getShowPage() != null) {
                            detailDataMap.put("showhide", fieldVO.getShowPage() != 0);
                        } else {
                            detailDataMap.put("showhide", true);
                        }
                        detailDataMap.put("width", StringUtils.isNotBlank(fieldVO.getControlWidth()) ? fieldVO.getControlWidth() : "");
                        detailDataMap.put("fieldName", fieldVO.getFieldName());
                        detailDataMap.put("orderNum", fieldVO.getOrderIndex() == null ? 0 : fieldVO.getOrderIndex());
                        detailDataMap.put("controlType", fieldVO.getControlType());
                        detailData.add(detailDataMap);
                    }
                }

                if (middleFields.size() > 0) {
                    for (CommonSelectFieldConfigVO fieldVO : middleFields) {
                        Map<String, Object> detailDataMap = new LinkedHashMap<>();

                        List<Object> codeValue = new ArrayList<>();

                        List<Object> viewValue = new ArrayList<>();
                        String sql = "select " +
                                fieldVO.getRelationalTable() + "." + fieldVO.getFkNameField() + "," +
                                fieldVO.getRelationalTable() + "." + fieldVO.getFkKeyField() +
                                " from " +
                                fieldVO.getRelationalTable() +
                                " join " +
                                fieldVO.getMiddleTable() +
                                " on " +
                                fieldVO.getRelationalTable() + "." + fieldVO.getFkKeyField() +
                                " = " +
                                fieldVO.getMiddleTable() + "." + fieldVO.getRightId() +
                                " where " +
                                fieldVO.getMiddleTable() + "." + fieldVO.getLeftId() +
                                " = " + "'" + pkValue + "'";
                        List<Map<String, Object>> manyRelationTableData = generalMethodMapper.getManyRelationTableData(sql);
                        for (Map<String, Object> datum : manyRelationTableData) {
                            viewValue.add(datum.get(fieldVO.getFkNameField()));
                            codeValue.add(datum.get(fieldVO.getFkKeyField()));
                        }
                        detailDataMap.put("type", arrayType);
                        detailDataMap.put("label", fieldVO.getFieldComments());
                        detailDataMap.put("value", viewValue);
                        detailDataMap.put("orderNum", fieldVO.getOrderIndex() == null ? 0 : fieldVO.getOrderIndex());
                        detailDataMap.put("fieldName", fieldVO.getFieldName());
                        detailDataMap.put("width", StringUtils.isNotBlank(fieldVO.getControlWidth()) ? fieldVO.getControlWidth() : "");
                        resultInfo.put(fieldVO.getFieldName(), codeValue);
                        if (fieldVO.getShowPage() != null) {
                            detailDataMap.put("showhide", fieldVO.getShowPage() != 0);
                        } else {
                            detailDataMap.put("showhide", true);
                        }
                        detailDataMap.put("controlType", fieldVO.getControlType());
                        detailData.add(detailDataMap);
                    }
                }

                if (authFields.size() > 0) {
                    for (CommonSelectFieldConfigVO fieldVO : authFields) {

                        List<Object> codeValue = new ArrayList<>();
                        final String[] split = fieldVO.getRightId().split(",");
                        List<String> relationTableFields = new ArrayList<>(Arrays.asList(split));
                        sqlMap.put("tableName", fieldVO.getMiddleTable());
                        sqlMap.put("key", fieldVO.getLeftId());
                        sqlMap.put("value", pkValue);
                        sqlMap.put("queryField", relationTableFields);
                        List<Map<String, Object>> infoData = generalMethodMapper.getRelationTableData(sqlMap);
                        for (Map<String, Object> datum : infoData) {
                            if (datum.get(split[0]) != null && datum.get(split[1]) != null) {
                                codeValue.add(datum.get(split[0]) + "_" + datum.get(split[1]));
                            } else if (datum.get(split[0]) != null && datum.get(split[1]) == null) {
                                codeValue.add(datum.get(split[0]));
                            }
                        }
                        resultInfo.put("checkedData", codeValue);
                    }
                }

                detailData.sort(Comparator.comparingInt(o -> (int) o.get("orderNum")));
                resultInfo.put("detailData", detailData);
            }
        }
        return resultInfo;
    }

    @Override
    public Map<String, Object> getEditPageData(List<CommonSelectFieldConfigVO> fieldVOs, CommonSelectTableConfigVO tableVO, Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>(3);
        String tableName = tableVO.getTableName();
        paramMap.put("tableName", tableName);

        Map<String, Object> ruleForm = new LinkedHashMap<>();

        List<Map<String, Object>> fieldData = new ArrayList<>();

        List<String> queryFields = new ArrayList<>();

        List<String> twoControlRange = new ArrayList<>();

        List<CommonSelectFieldConfigVO> relationTableFieldsVos = new ArrayList<>();
        for (CommonSelectFieldConfigVO fieldVO : fieldVOs) {
            if (StringUtils.isNotBlank(fieldVO.getControlType())) {
                if (StringUtils.isNotBlank(fieldVO.getMiddleTable())) {

                    relationTableFieldsVos.add(fieldVO);
                } else {
                    queryFields.add(fieldVO.getFieldName());
                }
                doFieldByControlType(fieldVO, ruleForm, fieldData, paramMap, twoControlRange);
            }
        }
        if (queryFields.size() > 0) {

            final String pkFieldName = tableVO.getKeyFieldName().toLowerCase();

            String pkValue = paramMap.get(pkFieldName).toString();

            Map<String, Object> sqlMap = new HashMap<>(4);

            sqlMap.put("queryField", queryFields);

            sqlMap.put("tableName", tableName);

            sqlMap.put("key", pkFieldName);

            sqlMap.put("value", pkValue);

            Map<String, Object> info = generalMethodMapper.getUpdateDataById(sqlMap);
            if (!info.isEmpty()) {

                if (relationTableFieldsVos.size() > 0) {
                    for (CommonSelectFieldConfigVO fieldVo : relationTableFieldsVos) {
                        final String[] split = fieldVo.getRightId().split(",");

                        List<String> relationTableFields = new ArrayList<>(Arrays.asList(split));
                        Map<String, Object> sqlMapInfo = new HashMap<>(4);
                        sqlMapInfo.put("tableName", fieldVo.getMiddleTable());
                        sqlMapInfo.put("key", fieldVo.getLeftId());
                        sqlMapInfo.put("value", pkValue);
                        sqlMapInfo.put("queryField", relationTableFields);

                        List<Map<String, Object>> infoData = generalMethodMapper.getRelationTableData(sqlMapInfo);
                        if (infoData.size() > 0) {
                            Object ruleFormType = ruleForm.get(fieldVo.getFieldName());

                            if (split.length == 1) {

                                if (ruleFormType instanceof ArrayList) {
                                    List<Object> values = new ArrayList<>();
                                    for (Map<String, Object> map : infoData) {
                                        values.add(map.get(split[0]));
                                    }
                                    info.put(fieldVo.getFieldName(), values);
                                } else {
                                    StringBuilder rightIds = new StringBuilder();
                                    for (Map<String, Object> map : infoData) {
                                        rightIds.append(map.get(split[0])).append(",");
                                    }
                                    info.put(fieldVo.getFieldName(), rightIds.deleteCharAt(rightIds.lastIndexOf(",")));
                                }

                            } else if (split.length == 2) {
                                StringBuilder rightIds = new StringBuilder();
                                for (Map<String, Object> datum : infoData) {
                                    if (datum.get(split[0]) != null && datum.get(split[1]) != null) {
                                        rightIds.append(datum.get(split[0])).append("_").append(datum.get(split[1])).append(",");
                                    } else if (datum.get(split[0]) != null && datum.get(split[1]) == null) {
                                        rightIds.append(datum.get(split[0])).append(",");
                                    }
                                }
                                info.put(fieldVo.getFieldName(), rightIds.deleteCharAt(rightIds.lastIndexOf(",")));
                            }
                        }
                    }
                }

                ruleFormTypeToData(info, ruleForm);
            }
        }
        resultMap.put("editformdata", ruleForm);
        resultMap.put("editcontroldata", fieldData);
        resultMap.put("dualcontrolskey", twoControlRange);
        return resultMap;
    }


    private void ruleFormTypeToData(Map<String, Object> data, Map<String, Object> roleForm) {
        for (Map.Entry<String, Object> objectEntry : data.entrySet()) {
            for (Map.Entry<String, Object> stringObjectEntry : roleForm.entrySet()) {
                if (objectEntry.getKey().equalsIgnoreCase(stringObjectEntry.getKey())) {

                    Object roleFormType = stringObjectEntry.getValue();

                    Object dataType = objectEntry.getValue();
                    if (dataType != null && roleFormType != null) {
                        if (!dataType.getClass().getName().equals(roleFormType.getClass().getName())) {
                            if (roleFormType.getClass().isArray() || roleFormType instanceof ArrayList) {
                                String[] split = dataType.toString().split(",");
                                stringObjectEntry.setValue(split);
                            } else {
                                stringObjectEntry.setValue(dataType.toString());
                            }
                        } else {
                            stringObjectEntry.setValue(dataType);
                        }
                    } else if (roleFormType == null && dataType != null && dataType.equals("")) {
                        stringObjectEntry.setValue(null);
                    } else if (roleFormType == null && dataType != null && !dataType.equals("")) {
                        stringObjectEntry.setValue(dataType);
                    }
                }
            }
        }

    }


    @Override
    public Map<String, Object> getAddPageInfo(List<CommonSelectFieldConfigVO> fieldVOs, CommonSelectTableConfigVO tableVO, Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>(4);

        Map<String, Object> ruleForm = new HashMap<>(3);

        List<Map<String, Object>> queryFieldsInfo = new ArrayList<>();

        List<String> twoControlRange = new ArrayList<>();
        paramMap.put("tableName", tableVO.getTableName());
        for (CommonSelectFieldConfigVO fieldVO : fieldVOs) {
            if (StringUtils.isNotBlank(fieldVO.getControlType())) {
                doFieldByControlType(fieldVO, ruleForm, queryFieldsInfo, paramMap, twoControlRange);
            }
        }
        resultMap.put("dualcontrolskey", twoControlRange);
        resultMap.put("addcontroldata", queryFieldsInfo);
        resultMap.put("addformdata", ruleForm);
        return resultMap;
    }

    @Override
    public void doAddMethod(Map<String, Object> addMainTableMap, List<Map<String, Object>> addRelationAddListMapSql, String tableName, String datasource, Map<String, Object> formData, String pkValue) {

        generalMethodMapper.doAddMethod(addMainTableMap);
        if (addRelationAddListMapSql.size() > 0) {
            for (Map<String, Object> map : addRelationAddListMapSql) {
                List<Object> values = (List<Object>) map.get("values");
                if (values.size() > 200) {
                    List<List<Object>> lists = averageAssign(values, 200);
                    for (List<Object> list : lists) {
                        map.replace("values", list);
                        generalMethodMapper.batchAddData(map);
                    }
                } else {
                    generalMethodMapper.batchAddData(map);
                }
            }
        }

        if (datasource == null) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tablename", tableName);
            operateLogService.saveUserOperationLog("add", paramMap, null, null, formData);
        }

        if ("Base_SysMenu".equals(tableName)) {

            String parentMenuID = formData.get("parentid").toString();

            List<String> menuids = generalMethodMapper.getDeleteData(parentMenuID);
            if (menuids.size() > 0) {

                rolesMapper.deleteRoleAuthForAddMenu(menuids);
                userInfoMapper.deleteUserAuthForAddMenu(menuids);
            }

            RolesVO topRoleVO = rolesMapper.getTopRoleVO();

            rolesMapper.deleteTopRoleAuth(topRoleVO.getRolesId());

            rolesMapper.resetTopRoleAuth(topRoleVO.getRolesId());
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public String doEditMethod(CommonSelectTableConfigVO tableVO, Map<String, Object> newDataMap, Map<String, Object> oldDataMap, Map<String, Object> mainMap, Map<String, Object> compareResultMap, List<CommonSelectFieldConfigVO> middleFields, Map<String, Object> relationTableDefaultFields, Object dataSource) {
        try {
            String pkValue = newDataMap.get(tableVO.getKeyFieldName().toLowerCase()).toString();

            if (mainMap.size() > 0) {
                Map<String, Object> mainSqlMap = new HashMap<>(3);
                mainSqlMap.put("editMap", mainMap);
                mainSqlMap.put("key", tableVO.getKeyFieldName());
                mainSqlMap.put("value", pkValue);
                mainSqlMap.put("tableName", tableVO.getTableName());
                generalMethodMapper.doEditMethod(mainSqlMap);
            }

            for (CommonSelectFieldConfigVO fieldVO : middleFields) {
                String middleTableName = fieldVO.getMiddleTable();
                String editFieldName = fieldVO.getRightId();
                String leftId = fieldVO.getLeftId();
                String fieldName = fieldVO.getFieldName().toLowerCase();

                Object oldValue = oldDataMap.get(fieldName);

                Object newValue = newDataMap.get(fieldName);

                List<Object> newList;
                List<Object> oldList;
                if (newValue instanceof ArrayList) {
                    newList = (List<Object>) newValue;
                    oldList = (List<Object>) oldValue;
                } else {
                    newList = Arrays.asList(newValue);
                    oldList = Arrays.asList(oldValue);
                }
                List<Object> addList = new ArrayList<>();
                List<Object> deleteList = new ArrayList<>();

                for (Object oldValueInfo : oldList) {
                    if (!newList.contains(oldValueInfo)) {
                        deleteList.add(oldValueInfo);
                    }
                }
                for (Object newValueInfo : newList) {
                    if (!oldList.contains(newValueInfo)) {
                        addList.add(newValueInfo);
                    }
                }
                String[] split = editFieldName.split(",");
                if (deleteList.size() > 0) {
                    StringBuilder deleteSql = new StringBuilder();
                    deleteSql.append("delete").append(" from ").append(middleTableName).append(" where ").append(leftId).append("=").append("'").append(pkValue).append("'");
                    getDeleteSqlForEdit(deleteList, split, deleteSql);

                    generalMethodMapper.deleteMiddleTableData(deleteSql.toString());

                    if (middleTableName.equals("Base_RoleRight") && tableVO.getTableName().equals("Base_Roles")) {

                        List<String> sonRoleIDs = rolesMapper.getSonRoleIDsForChangeParentRoleAuth(pkValue);
                        sonRoleIDs.remove(pkValue);
                        if (sonRoleIDs.size() > 0) {

                            StringBuilder sql = new StringBuilder();
                            for (String id : sonRoleIDs) {
                                sql.delete(0, sql.length());
                                sql.append("delete").append(" from ").append(middleTableName).append(" where ").append(leftId).append("=").append("'").append(id).append("'");
                                getDeleteSqlForEdit(deleteList, split, sql);

                                generalMethodMapper.deleteMiddleTableData(sql.toString());
                            }
                        }
                    }

                    if (middleTableName.equals("Base_MenuButton") && tableVO.getTableName().equals("Base_SysMenu")) {

                        String[] table = {"Base_UserRight", "Base_RoleRight"};
                        Map<String, Object> map = new HashMap<>();
                        for (String tableName : table) {
                            map.put("tableName", tableName);
                            map.put("menuFieldName", leftId);
                            map.put("menuValue", pkValue);
                            map.put("buttonFieldName", editFieldName);
                            map.put("buttonValues", deleteList);
                            generalMethodMapper.batchDelete(map);
                            map.clear();
                        }
                    }
                }
                if (addList.size() > 0) {
                    if (relationTableDefaultFields.get("$" + middleTableName) != null) {
                        String pkFieldName = (String) relationTableDefaultFields.get("$" + middleTableName);
                        String[] splitField = fieldVO.getRightId().split(",");
                        List<String> addFields = new ArrayList<>();
                        addFields.add(pkFieldName);
                        addFields.add(leftId);
                        addFields.addAll(Arrays.asList(splitField));

                        List<CommonSelectFieldConfigVO> defaultFields = (List<CommonSelectFieldConfigVO>) relationTableDefaultFields.get(middleTableName);
                        List<Object> valuesdef = new ArrayList<>();
                        if (defaultFields.size() > 0) {
                            for (CommonSelectFieldConfigVO field : defaultFields) {
                                addFields.add(field.getFieldName());
                                String controlDefault = field.getControlDefault();
                                if (controlDefault.contains("()")) {
                                    String defaultFieldMethodValue = CommonServiceSupport.getDefaultFieldMethodValue(controlDefault);
                                    valuesdef.add(defaultFieldMethodValue);
                                } else {
                                    valuesdef.add(controlDefault);
                                }
                            }
                        }

                        List<Object> values = new ArrayList<>();
                        for (Object valueInfo : addList) {
                            List<Object> fieldValues = new ArrayList<>();
                            fieldValues.add((UUID.randomUUID().toString()));
                            fieldValues.add(pkValue);
                            if (splitField.length == 1) {
                                fieldValues.add(valueInfo);
                            } else if (splitField.length == 2) {
                                final String[] split2 = valueInfo.toString().split("_");
                                if (split2.length == 2) {
                                    fieldValues.add(split2[0]);
                                    fieldValues.add(split2[1]);
                                } else if (split2.length == 1) {
                                    fieldValues.add(split2[0]);
                                    fieldValues.add(null);
                                }
                            }

                            if (valuesdef.size() > 0) {
                                fieldValues.addAll(valuesdef);
                            }
                            if (addFields.size() == fieldValues.size()) {
                                values.add(fieldValues);
                            }
                        }
                        if (addList.size() == values.size()) {
                            Map<String, Object> sqlMap = new HashMap<>();
                            sqlMap.put("fieldList", addFields);
                            sqlMap.put("tableName", middleTableName);
                            sqlMap.put("values", values);
                            if (values.size() > 200) {
                                List<List<Object>> lists = averageAssign(values, 200);
                                for (List<Object> list : lists) {
                                    sqlMap.put("values", list);
                                    generalMethodMapper.batchAddData(sqlMap);
                                }
                            } else {
                                generalMethodMapper.batchAddData(sqlMap);
                            }

                            if (middleTableName.equals("Base_MenuButton") && tableVO.getTableName().equals("Base_SysMenu")) {

                                List<String> menuids = generalMethodMapper.getDeleteData(pkValue);
                                if (menuids.size() > 0) {

                                    rolesMapper.deleteRoleAuthForAddMenu(menuids);
                                    userInfoMapper.deleteUserAuthForAddMenu(menuids);
                                }

                                RolesVO topRoleVO = rolesMapper.getTopRoleVO();

                                rolesMapper.deleteTopRoleAuth(topRoleVO.getRolesId());

                                rolesMapper.resetTopRoleAuth(topRoleVO.getRolesId());
                            }
                        }
                    }
                }

            }


            if (dataSource == null) {
                if (!"Base_userInfo".equals(tableVO.getTableName())) {
                    Map<String, Object> paramMap = new HashMap<>(2);
                    paramMap.put("tablename", tableVO.getTableName());
                    operateLogService.saveUserOperationLog("edit", paramMap, compareResultMap, oldDataMap, newDataMap);
                }
            }
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }


    private static <T> List<List<T>> averageAssign(List<T> list, int n) {
        List<List<T>> result = new ArrayList<>();
        int remainder = list.size() % n;
        int number = list.size() / n;
        for (int i = 0; i < number; i++) {
            List<T> ts = list.subList(i * n, (i + 1) * n);
            result.add(ts);
        }
        if (0 != remainder) {
            List<T> ts = list.subList(n * number, n * number + remainder);
            result.add(ts);
        }
        return result;
    }


    private void getDeleteSqlForEdit(List<Object> deleteList, String[] split, StringBuilder sql) {
        if (split.length == 1) {
            sql.append(" and ").append(split[0]).append(" in ").append("( ");
            for (Object s : deleteList) {
                sql.append("'").append(s).append("'").append(",");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(" )");
        } else if (split.length == 2) {
            sql.append(" and (");
            for (Object s : deleteList) {
                String s1 = "";
                String[] val = s.toString().split("_");
                if (val.length == 1) {
                    s1 = "( " + split[0] + " = " + "'" + val[0] + "'" + " and " + split[1] + " is " + "null" + " or " + split[1] + " = " + "'" + "'" + " )";
                } else if (val.length == 2) {
                    s1 = "( " + split[0] + " = " + "'" + val[0] + "'" + " and " + split[1] + " = " + "'" + val[1] + "'" + " )";
                }
                sql.append(s1).append(" or ");
            }
            sql.delete(sql.length() - 3, sql.length()).append(" )");
        }
    }


    @Override
    public String getPKFieldNameByTableName(String middleTableName) {
        return generalMethodMapper.getPKFieldNameByTableName(middleTableName);
    }

    @Override
    public List<Map<String, Object>> getDeleteIDs(Map<String, Object> map) {
        return generalMethodMapper.getDeleteIDs(map);
    }


    @Override
    @SuppressWarnings("unchecked")
    public void deleteMethod(CommonSelectTableConfigVO tableConfigVO, List<Object> deIDs, List<CommonSelectFieldConfigVO> relationFields, Map<String, Object> params) {
        for (CommonSelectFieldConfigVO vo : relationFields) {

            doDeleteRelationTableData(deIDs, vo.getRelationalTable(), vo.getFkKeyField());
        }

        doDeleteRelationTableData(deIDs, tableConfigVO.getTableName(), tableConfigVO.getKeyFieldName());

        Map<String, Object> OldMaps = (Map<String, Object>) params.get("oldmaps");
        Object datasource = params.get("datasource");
        if (datasource == null) {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("tablename", tableConfigVO.getTableName());
            operateLogService.saveUserOperationLog("delete", paramsMap, null, OldMaps, null);
        }
    }


    private Map<String, Object> getWhereSqlMap(Map<String, Object> paramMap, List<CommonSelectFieldConfigVO> queryFieldInfo, String tableNameInfo) {
        Map<String, Object> resultMap = new HashMap<>(4);

        Map<String, Object> whereEqual = new HashMap<>(3);

        Map<String, Object> whereNoEqual = new HashMap<>(3);

        Map<String, String> whereMap = new HashMap<>(3);

        Map<String, Object> whereIn = new HashMap<>(3);
        Map<String, Object> whereCharIndex = new HashMap<>(3);

        Map<String, Object> whereRange = new HashMap<>(3);

        Map<String, Object> whereMonth = new HashMap<>(3);

        Map<String, Object> customMap = new HashMap<>(3);
        if (queryFieldInfo.size() > 0) {
            for (CommonSelectFieldConfigVO fieldConfigVO : queryFieldInfo) {
                String keyFieldName = (StringUtils.isNotBlank(fieldConfigVO.getOtherFieldName()) ? fieldConfigVO.getOtherFieldName() : fieldConfigVO.getFieldName()).toLowerCase();

                Object queryValue = paramMap.get(keyFieldName);
                if (queryValue == null && StringUtils.isNotBlank(fieldConfigVO.getControlDefault())) {
                    if (fieldConfigVO.getControlDefault().contains("()")) {
                        queryValue = CommonServiceSupport.getDefaultFieldMethodValue(fieldConfigVO.getControlDefault());
                    } else {
                        queryValue = fieldConfigVO.getControlDefault();
                    }
                } else {
                    if (fieldConfigVO.getQueryFieldSql() != null && !fieldConfigVO.getQueryFieldSql().equals("")) {
                        if (StringUtils.isNotBlank(fieldConfigVO.getControlType())) {
                            if (fieldConfigVO.getControlType().equals("text")) {
                                resultMap.put("customsql", fieldConfigVO.getQueryFieldSql());
                            }
                        } else {
                            resultMap.put("customsql", fieldConfigVO.getQueryFieldSql());
                        }
                    }
                }
                String tableName = tableNameInfo;
                if (queryValue != null && StringUtils.isNotBlank(queryValue.toString())) {

                    Object[] split;
                    if (queryValue instanceof JSONArray) {
                        split = ((JSONArray) queryValue).toArray();
                    } else {
                        split = queryValue.toString().split(",");
                    }

                    if (StringUtils.isNotBlank(fieldConfigVO.getRelationalTable())) {
                        keyFieldName = fieldConfigVO.getFkKeyField();
                        tableName = fieldConfigVO.getRelationalTable();
                        if (fieldConfigVO.getControlType() != null) {
                            if (fieldConfigVO.getControlType().equals(ConstantsUtil.ControlTypeEnum.text.getValue())) {
                                keyFieldName = fieldConfigVO.getFkNameField();
                            }
                            if (StringUtils.isNotBlank(fieldConfigVO.getFieldQueryType()) && fieldConfigVO.getFieldQueryType().equals("charindex")
                                    && fieldConfigVO.getControlType().equals(ConstantsUtil.ControlTypeEnum.selectChecked.getValue())) {
                                keyFieldName = fieldConfigVO.getFieldName();
                                tableName = tableNameInfo;
                            }
                        }
                    } else if (StringUtils.isBlank(fieldConfigVO.getRelationalTable()) && StringUtils.isNotBlank(fieldConfigVO.getOtherFieldName())) {
                        keyFieldName = fieldConfigVO.getFieldName();
                    }

                    String queryType = "like";
                    if (StringUtils.isNotBlank(fieldConfigVO.getFieldQueryType())) {
                        queryType = fieldConfigVO.getFieldQueryType();
                    }
                    if (fieldConfigVO.getIsRangeQuery() != null && fieldConfigVO.getIsRangeQuery() == 1) {
                        queryType = "between and";
                    }
                    if (StringUtils.isNotBlank(fieldConfigVO.getControlType()) && fieldConfigVO.getControlType().equals("yearQuarter")) {
                        queryType = "quarter";
                    }
                    switch (queryType) {
                        case "in":
                            if (split.length > 0) {
                                List<Object> fieldValue = new ArrayList<>(Arrays.asList(split));
                                whereIn.put(tableName + "." + keyFieldName, fieldValue);
                            }
                            break;

                        case "quarter":
                            if (split.length == 2) {
                                String year = (String) split[0];
                                String season = (String) split[1];
                                Map quarter = getQuarter(year, season);
                                whereMonth.put(tableName + "." + keyFieldName, quarter);
                            }
                            break;
                        case "charindex":
                            if (split.length > 0) {
                                StringBuffer sqlInfo = new StringBuffer();
                                sqlInfo.append("( ");
                                for (Object o : split) {
                                    sqlInfo.append("CHARINDEX('").append(o).append("',").append(tableName).append(".").append(keyFieldName).append(")").append(" > 0 ").append(" OR ");
                                }
                                sqlInfo.replace(sqlInfo.length() - 3, sqlInfo.length(), " )");
                                whereCharIndex.put(tableName + "." + keyFieldName, sqlInfo);
                            }
                            break;
                        case "=":
                            whereEqual.put(tableName + "." + keyFieldName, queryValue);
                            break;
                        case "nullornull":
                            if (queryValue.equals("1")) {
                                customMap.put(tableName + "." + keyFieldName, "notnull");
                            } else if (queryValue.equals("0")) {
                                customMap.put(tableName + "." + keyFieldName, "null");
                            }
                            break;
                        case "!=":
                            whereNoEqual.put(tableName + "." + keyFieldName, queryValue);
                            break;
                        case "like":
                            whereMap.put(tableName + "." + keyFieldName, queryValue.toString());
                            break;
                        case "between and":
                            if (split.length > 0) {
                                if (split.length == 1) {
                                    whereEqual.put(tableName + "." + keyFieldName, queryValue);

                                } else if (split.length == 2) {
                                    Map<String, Object> map = new HashMap<>(4);
                                    Object startValue = split[0];
                                    Object endValue = split[1];
                                    if (startValue != null && !startValue.toString().contains("null")) {
                                        if (endValue != null && !endValue.toString().contains("null")) {
                                            map.put("start", startValue);
                                            map.put("end", endValue);
                                        } else {
                                            map.put("start", startValue);
                                            map.put("end", null);
                                        }
                                    } else if (endValue != null && !endValue.toString().contains("null")) {
                                        map.put("start", null);
                                        map.put("end", endValue);
                                    }
                                    if (map.size() == 2) {
                                        whereRange.put(tableName + "." + keyFieldName, map);
                                    }
                                }
                            }
                            break;
                        default:
                            whereMap.put(tableName + "." + keyFieldName, queryValue.toString());
                    }
                }
            }
            resultMap.put("whereMap", whereMap);
            resultMap.put("whereIn", whereIn);
            resultMap.put("whereEqual", whereEqual);
            resultMap.put("whereNoEqual", whereNoEqual);
            resultMap.put("customMap", customMap);
            resultMap.put("whereRange", whereRange);
            resultMap.put("whereMonth", whereMonth);
            resultMap.put("whereCharIndex", whereCharIndex);
        }
        return resultMap;
    }


    private Map getQuarter(String year, String quarter) {
        Map<String, String> map = new HashMap<>();
        Integer flag = Integer.parseInt(quarter);
        String em = String.valueOf(flag * 3);
        String sm = String.valueOf(flag * 3 - 2);
        if (em.length() < 2 && sm.length() < 2) {
            em = year + "-" + "0" + em;
            sm = year + "-" + "0" + sm;
        } else {
            em = year + "-" + em;
            sm = year + "-" + sm;
        }
        map.put("start", sm);
        map.put("end", em);
        return map;
    }

    private void doResultListData(List<CommonSelectFieldConfigVO> fieldValuesCustom, List<CommonSelectFieldConfigVO> timeFormatField, Map<String, Object> map) {
        String fieldName;
        for (CommonSelectFieldConfigVO configVO : timeFormatField) {
            fieldName = StringUtils.isNotBlank(configVO.getOtherFieldName()) ? configVO.getOtherFieldName() : configVO.getFieldName();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().equals(fieldName) && entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())) {
                    String a = "regex:";
                    Object value = entry.getValue();
                    String formatStyle = configVO.getControlValueFormat();
                    if (formatStyle.substring(0, a.length()).equals(a)) {
                        String s = value.toString();
                        String regex = formatStyle.substring(a.length(), formatStyle.length());
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(s);
                        if (matcher.find()) {
                            s = matcher.group(0);
                        }
                        entry.setValue(s);
                    } else {
                        entry.setValue(DataFormatUtil.formatDateToOtherFormat((Date) value, configVO.getControlValueFormat()));
                    }
                }
            }
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Date) {
                entry.setValue(entry.getValue().toString());
            }
        }
        for (CommonSelectFieldConfigVO configVO : fieldValuesCustom) {
            fieldName = StringUtils.isNotBlank(configVO.getOtherFieldName()) ? configVO.getOtherFieldName() : configVO.getFieldName();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (fieldName.equals(entry.getKey())) {
                    String customOptions = configVO.getCustomOptions();
                    String substring = customOptions.substring(1, customOptions.length());
                    JSONObject jsonObject = JSONObject.fromObject(substring);
                    if (entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())) {
                        String s = entry.getValue().toString();
                        if (StringUtils.isNotBlank(configVO.getControlType()) && configVO.getControlType().equals(ConstantsUtil.ControlTypeEnum.array.getValue())) {
                            String[] split = s.split(",");
                            StringBuilder value = new StringBuilder();
                            for (String s1 : split) {
                                if (jsonObject.get(s1) != null && StringUtils.isNotBlank(jsonObject.get(s1).toString())) {
                                    String s2 = jsonObject.get(s1).toString();
                                    value.append(s2).append("、");
                                }
                            }
                            if (value.length() > 0) {
                                value.deleteCharAt(value.lastIndexOf("、"));
                                entry.setValue(value.toString());
                            }

                        } else {
                            if (jsonObject.get(s) != null && StringUtils.isNotBlank(jsonObject.get(s).toString())) {
                                String s1 = jsonObject.get(s).toString();
                                entry.setValue(s1);
                            }
                        }
                    }
                }

            }
        }
    }


    private void doFieldByControlType(CommonSelectFieldConfigVO fieldVO, Map<String, Object> ruleForm, List<Map<String, Object>> fieldData, Map<String, Object> paramMap, List<String> twoControlRange) {
        Map<String, Object> resultMap = new HashMap<>(10);

        String controlType = fieldVO.getControlType();

        String configType = fieldVO.getConfigType();
        String fieldNameInfo = fieldVO.getFieldName().toLowerCase();

        Object ruleFormType = null;
        ConstantsUtil.ControlTypeEnum controlTypeEnum = ConstantsUtil.getControlTypeEnumByControlType(controlType);
        if (controlTypeEnum != null) {

            resultMap.put("disabled", fieldVO.getUpdateReadOnly() != null && fieldVO.getUpdateReadOnly() == 1);

            resultMap.put("showHide", fieldVO.getShowPage() == null || fieldVO.getShowPage() == 1);

            if (StringUtils.isNotBlank(fieldVO.getValidMessage()) && StringUtils.isNotBlank(fieldVO.getValidRules()) && StringUtils.isNotBlank(fieldVO.getValidTriggers())) {
                resultMap.put("validMessage", fieldVO.getValidMessage());
                resultMap.put("validRules", fieldVO.getValidRules());
                resultMap.put("validTriggers", fieldVO.getValidTriggers());
            }
            String message = "";

            Boolean isClearAble = false;

            boolean isRange = false;
            if (fieldVO.getIsRangeQuery() != null && fieldVO.getIsRangeQuery() == 1) {
                isRange = true;
            }
            switch (controlTypeEnum) {

                case text:
                    message = "请输入";
                    isClearAble = true;
                    if (StringUtils.isNotBlank(fieldVO.getFkNameField()) && paramMap != null && paramMap.get(fieldVO.getFkNameField().toLowerCase()) != null) {
                        if ("add".equals(configType) || "edit".equals(configType)) {
                            String s = paramMap.get(fieldVO.getFkNameField().toLowerCase()).toString();
                            if (StringUtils.isNotBlank(s)) {
                                ruleFormType = s;
                            }
                        }
                    }
                    if (ruleFormType == null) {
                        ruleFormType = "";
                    }
                    break;

                case textarea:
                    message = "请填写";
                    isClearAble = true;
                    ruleFormType = "";
                    break;

                case number:
                    message = "请输入";
                    isClearAble = true;

                    resultMap.put("min", 1);
                    ruleFormType = "";
                    break;

                case radio:

                    resultMap.put("radioChildren", doFieldCustomAndRelationData(fieldVO));
                    ruleFormType = "";
                    break;


                case TimePicker:
                    isClearAble = true;
                    if (isRange) {
                        resultMap.put("isRange", true);
                        ruleFormType = new ArrayList<>();
                    } else {
                        ruleFormType = "";
                    }
                    doRangeTimeControl(isRange, resultMap);

                    doDateControl(fieldVO, resultMap, "oneControl", "hms");
                    break;

                case DatePicker:
                    isClearAble = true;
                    if (isRange) {
                        resultMap.put("dateType", "daterange");
                        ruleFormType = new ArrayList<>();
                    } else {
                        resultMap.put("dateType", "date");
                        ruleFormType = "";
                    }
                    doRangeTimeControl(isRange, resultMap);

                    doDateControl(fieldVO, resultMap, "oneControl", "date");
                    break;

                case DatePicker_oneInputTwoValue:
                    isClearAble = true;
                    resultMap.put("dateType", "date");
                    resultMap.put("isDiadic", true);
                    ruleFormType = new ArrayList<>();
                    controlType = ConstantsUtil.ControlTypeEnum.DatePicker.getValue();
                    resultMap.put("placeholder", "点击选择");
                    if (isRange) {
                        if (StringUtils.isNotBlank(fieldVO.getControlValueFormat())) {
                            String controlValueFormat = fieldVO.getControlValueFormat();
                            String substring = controlValueFormat.substring(1, controlValueFormat.length());
                            JSONObject jsonObject = JSONObject.fromObject(substring);
                            resultMap.put("format", jsonObject.get("viewFormat") != null ? jsonObject.get("viewFormat") : "yyyy-MM-dd");
                            resultMap.put("valueFormat", jsonObject.get("startValueFormat") != null ? jsonObject.get("startValueFormat") : "yyyy-MM-dd 00:00:00");
                            resultMap.put("valueFormatEnd", jsonObject.get("endValueFormat") != null ? jsonObject.get("endValueFormat") : "yyyy-MM-dd 23:59:59");
                        } else {
                            resultMap.put("format", "yyyy-MM-dd");
                            resultMap.put("valueFormat", "yyyy-MM-dd 00:00:00");
                            resultMap.put("valueFormatEnd", "yyyy-MM-dd 23:59:59");
                        }
                    }
                    break;

                case DatePickerMonth_oneInputTwoValue:
                    isClearAble = true;
                    controlType = ConstantsUtil.ControlTypeEnum.DatePicker.getValue();
                    ruleFormType = new ArrayList<>();
                    resultMap.put("placeholder", "点击选择");
                    resultMap.put("dateType", "month");
                    resultMap.put("isDiadic", true);
                    if (isRange) {
                        if (StringUtils.isNotBlank(fieldVO.getControlValueFormat())) {
                            String controlValueFormat = fieldVO.getControlValueFormat();
                            String substring = controlValueFormat.substring(1, controlValueFormat.length());
                            JSONObject jsonObject = JSONObject.fromObject(substring);
                            resultMap.put("format", jsonObject.get("viewFormat") != null ? jsonObject.get("viewFormat") : "yyyy-MM");
                            resultMap.put("valueFormat", jsonObject.get("startValueFormat") != null ? jsonObject.get("startValueFormat") : "yyyy-MM-dd");
                            resultMap.put("valueFormatEnd", jsonObject.get("endValueFormat") != null ? jsonObject.get("endValueFormat") : "yyyy-MM-dd");
                        }
                    } else {
                        resultMap.put("format", "yyyy-MM-dd");
                        resultMap.put("valueFormat", "yyyy-MM-dd");
                        resultMap.put("valueFormatEnd", "yyyy-MM-dd");
                    }
                    break;
                case DatePicker_month:
                    isClearAble = true;
                    ruleFormType = "";
                    controlType = ConstantsUtil.ControlTypeEnum.DatePicker.getValue();
                    resultMap.put("dateType", "month");
                    resultMap.put("placeholder", "点击选择");
                    doDateControl(fieldVO, resultMap, "oneControl", "month");
                    break;
                case DatePicker_year:
                    isClearAble = true;
                    ruleFormType = "";
                    controlType = ConstantsUtil.ControlTypeEnum.DatePicker.getValue();
                    resultMap.put("dateType", "year");
                    resultMap.put("placeholder", "点击选择");
                    doDateControl(fieldVO, resultMap, "oneControl", "year");
                    break;

                case DatePicker_dates:
                    isClearAble = true;
                    controlType = ConstantsUtil.ControlTypeEnum.DatePicker.getValue();
                    resultMap.put("dateType", "dates");
                    resultMap.put("placeholder", "请选择");

                    ruleFormType = new ArrayList<>();

                    doDateControl(fieldVO, resultMap, "oneControl", "dates");
                    break;

                case DateTimePicker:
                    controlType = ConstantsUtil.ControlTypeEnum.DatePicker.getValue();
                    isClearAble = true;
                    if (isRange) {
                        resultMap.put("dateType", "datetimerange");
                        ruleFormType = new ArrayList<>();
                    } else {
                        ruleFormType = "";
                        resultMap.put("dateType", "datetime");
                    }
                    doRangeTimeControl(isRange, resultMap);
                    doDateControl(fieldVO, resultMap, "oneControl", "dateTime");
                    break;

                case textScope:
                    if (isRange) {
                        resultMap.put("startPlaceholder", "请选择起始值");
                        String end = StringUtils.isNotBlank(fieldVO.getOtherFieldName()) ? fieldVO.getOtherFieldName() : fieldVO.getFieldName();
                        resultMap.put("endName", "end" + end);
                        resultMap.put("startName", "start" + end);
                        resultMap.put("endPlaceholder", "请选择结束值");
                        ruleFormType = new ArrayList<>();
                    }
                    twoControlRange.add(fieldVO.getFieldName().toLowerCase());
                    break;

                case datePickerScope_Month:
                    controlType = ConstantsUtil.ControlTypeEnum.DatePickerScope.getValue();
                    twoControlRange.add(fieldVO.getFieldName().toLowerCase());
                    if (isRange) {
                        resultMap.put("dateType", "month");
                        String end = StringUtils.isNotBlank(fieldVO.getOtherFieldName()) ? fieldVO.getOtherFieldName() : fieldVO.getFieldName();
                        resultMap.put("endName", "end" + end);
                        resultMap.put("startName", "start" + end);
                        resultMap.put("characteristic", fieldVO.getPkFieldConfigId());
                        resultMap.put("pickerOptionsEnd", "pickerOptionsEnd" + fieldVO.getPkFieldConfigId());
                        resultMap.put("startPlaceholder", "请选择起始值");
                        resultMap.put("endPlaceholder", "请选择结束值");

                        doDateControl(fieldVO, resultMap, "twoControl", "month");
                        ruleFormType = new ArrayList<>();
                    }
                    break;
                case datePickerScope_Year:
                    controlType = ConstantsUtil.ControlTypeEnum.DatePickerScope.getValue();
                    twoControlRange.add(fieldVO.getFieldName().toLowerCase());
                    if (isRange) {
                        resultMap.put("dateType", "year");
                        String end = StringUtils.isNotBlank(fieldVO.getOtherFieldName()) ? fieldVO.getOtherFieldName() : fieldVO.getFieldName();
                        resultMap.put("endName", "end" + end);
                        resultMap.put("startName", "start" + end);
                        resultMap.put("characteristic", fieldVO.getPkFieldConfigId());
                        resultMap.put("pickerOptionsEnd", "pickerOptionsEnd" + fieldVO.getPkFieldConfigId());
                        resultMap.put("startPlaceholder", "请选择起始值");
                        resultMap.put("endPlaceholder", "请选择结束值");

                        doDateControl(fieldVO, resultMap, "twoControl", "month");
                        ruleFormType = new ArrayList<>();
                    }
                    break;

                case datePickerScope_Date:
                    controlType = ConstantsUtil.ControlTypeEnum.DatePickerScope.getValue();
                    twoControlRange.add(fieldVO.getFieldName().toLowerCase());
                    if (isRange) {
                        resultMap.put("dateType", "date");
                        resultMap.put("characteristic", fieldVO.getPkFieldConfigId());
                        resultMap.put("pickerOptionsEnd", "pickerOptionsEnd" + fieldVO.getPkFieldConfigId());
                        resultMap.put("startPlaceholder", "请选择起始值");
                        resultMap.put("endPlaceholder", "请选择结束值");
                        doDateControl(fieldVO, resultMap, "twoControl", "date");
                        ruleFormType = new ArrayList<>();
                    }
                    break;

                case yearQuarter:
                    twoControlRange.add(fieldVO.getFieldName().toLowerCase());
                    resultMap.put("characteristic", fieldVO.getPkFieldConfigId());
                    resultMap.put("placeholder", "请选择");
                    ruleFormType = new ArrayList<>();
                    break;

                case checkbox:
                    List<Map<String, Object>> checkedData = doFieldCustomAndRelationData(fieldVO);

                    resultMap.put("checkboxChildren", checkedData);
                    ruleFormType = new ArrayList<>();
                    break;

                case select:
                    message = "请选择";
                    isClearAble = true;
                    List<Map<String, Object>> option = doFieldCustomAndRelationData(fieldVO);

                    if (paramMap != null && paramMap.get("tableName") != null) {
                        String tableName = paramMap.get("tableName").toString();
                        if (StringUtils.isNotBlank(fieldVO.getRelationalTable()) && StringUtils.isNotBlank(fieldVO.getFkKeyField())) {
                            if (paramMap.get(fieldVO.getFkKeyField().toLowerCase()) != null && fieldVO.getRelationalTable().equals(tableName)) {
                                ruleFormType = paramMap.get(fieldVO.getFkKeyField().toLowerCase()).toString();
                            }
                        }
                    }
                    if (ruleFormType == null) {
                        ruleFormType = "";
                    }

                    resultMap.put("option", option);
                    break;

                case selectInput:
                    message = "请选择";
                    isClearAble = true;
                    ruleFormType = "";
                    controlType = ConstantsUtil.ControlTypeEnum.select.getValue();

                    resultMap.put("option", doFieldCustomAndRelationData(fieldVO));

                    resultMap.put("allowCreate", true);

                    resultMap.put("filterable", true);
                    break;

                case selectChecked:
                    message = "请选择";
                    isClearAble = true;
                    controlType = ConstantsUtil.ControlTypeEnum.select.getValue();

                    resultMap.put("option", doFieldCustomAndRelationData(fieldVO));

                    resultMap.put("multiple", true);

                    resultMap.put("collapseTags", true);
                    ruleFormType = new ArrayList<>();
                    break;

                case treeSelect:
                    message = "请选择";
                    isClearAble = true;
                    resultMap.put("treeData", doFieldControlTypeForTree(fieldVO));
                    resultMap.put("closeOnSelect", true);
                    resultMap.put("searchable", false);
                    ruleFormType = null;
                    break;

                case treeSelectChecked:
                    message = "请选择";
                    isClearAble = true;
                    controlType = ConstantsUtil.ControlTypeEnum.treeSelect.getValue();
                    resultMap.put("treeData", doFieldControlTypeForTree(fieldVO));

                    resultMap.put("multiple", true);
                    resultMap.put("closeOnSelect", true);
                    resultMap.put("flat", true);
                    resultMap.put("searchable", false);

                    ruleFormType = new ArrayList<>();
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotBlank(message)) {

                resultMap.put("placeholder", message + fieldVO.getFieldComments());
            }
            if (isClearAble) {

                resultMap.put("clearable", true);
            }

            resultMap.put("label", fieldVO.getFieldComments());

            resultMap.put("type", controlType);

            if (fieldVO.getConfigType().contains("query")) {
                if (StringUtils.isNotBlank(fieldVO.getRelationalTable()) && StringUtils.isNotBlank(fieldVO.getOtherFieldName())) {
                    fieldNameInfo = fieldVO.getOtherFieldName().toLowerCase();
                }
            }


            resultMap.put("name", fieldNameInfo);

            resultMap.put("height", fieldVO.getControlHeight());

            resultMap.put("width", fieldVO.getControlWidth());
        }

        if (StringUtils.isNotBlank(fieldVO.getControlDefault())) {

            String controlDefault = fieldVO.getControlDefault();
            if (controlDefault.contains("()")) {
                if (StringUtils.isNotBlank(CommonServiceSupport.getDefaultFieldMethodValue(fieldVO.getControlDefault()))) {
                    controlDefault = CommonServiceSupport.getDefaultFieldMethodValue(fieldVO.getControlDefault());
                } else {
                    controlDefault = "";
                }
            }
            if (ruleFormType instanceof ArrayList) {
                String[] split = controlDefault.split(",");
                ruleForm.put(fieldVO.getFieldName(), split);
            } else {
                if (StringUtils.isNotBlank(controlDefault)) {
                    ruleForm.put(fieldVO.getFieldName(), controlDefault);
                } else {
                    ruleForm.put(fieldVO.getFieldName(), "");
                }
            }
        } else {
            ruleForm.put(fieldVO.getFieldName(), ruleFormType);
        }
        fieldData.add(resultMap);
    }


    private void doRangeTimeControl(boolean isRange, Map<String, Object> resultMap) {
        if (isRange) {
            resultMap.put("startPlaceholder", "请选择开始值");
            resultMap.put("endPlaceholder", "请选择结束值");
        } else {
            resultMap.put("placeholder", "点击选择");
        }

    }


    private void doDateControl(CommonSelectFieldConfigVO fieldVO, Map<String, Object> resultMap, String type, String timeType) {
        String defaultFormat;
        switch (timeType) {
            case "hms":
                defaultFormat = "HH:mm:ss";
                break;
            case "dateTime":
                defaultFormat = "yyyy-MM-dd HH:mm:ss";
                break;
            case "month":
                defaultFormat = "yyyy-MM";
                break;
            case "year":
                defaultFormat = "yyyy";
                break;
            default:

                defaultFormat = "yyyy-MM-dd";
        }
        switch (type) {
            case "twoControl":
                if (StringUtils.isNotBlank(fieldVO.getControlValueFormat())) {
                    String controlValueFormat = fieldVO.getControlValueFormat();
                    String substring = controlValueFormat.substring(1, controlValueFormat.length());
                    JSONObject jsonObject = JSONObject.fromObject(substring);
                    Map start = (Map) jsonObject.get("start");
                    Map end = (Map) jsonObject.get("end");

                    resultMap.put("format", start.get("format") != null ? start.get("format") : defaultFormat);
                    resultMap.put("valueFormat", start.get("valueFormat") != null ? start.get("valueFormat") : defaultFormat);
                    resultMap.put("format1", end.get("format") != null ? end.get("format") : defaultFormat);
                    resultMap.put("valueFormat1", end.get("valueFormat") != null ? end.get("valueFormat") : defaultFormat);
                } else {

                    resultMap.put("format", defaultFormat);
                    resultMap.put("format1", defaultFormat);

                    resultMap.put("valueFormat", defaultFormat);
                    resultMap.put("valueFormat1", defaultFormat);
                }
                break;
            default:
                if (StringUtils.isNotBlank(fieldVO.getControlValueFormat())) {
                    String controlValueFormat = fieldVO.getControlValueFormat();
                    String substring = controlValueFormat.substring(1, controlValueFormat.length());
                    JSONObject jsonObject = JSONObject.fromObject(substring);

                    resultMap.put("format", jsonObject.get("format") != null ? jsonObject.get("format") : defaultFormat);
                    resultMap.put("valueFormat", jsonObject.get("valueFormat") != null ? jsonObject.get("valueFormat") : defaultFormat);
                } else {

                    resultMap.put("format", defaultFormat);

                    resultMap.put("valueFormat", defaultFormat);
                }
        }
    }

    private List<Map<String, Object>> doFieldCustomAndRelationData(CommonSelectFieldConfigVO fieldVO) {
        List<Map<String, Object>> childrenData = new ArrayList<>();
        if (StringUtils.isNotBlank(fieldVO.getCustomOptions())) {
            String customOptions = fieldVO.getCustomOptions();
            String substring = customOptions.substring(1, customOptions.length());
            doCustomFieldData(substring, childrenData);
        } else if (StringUtils.isNotBlank(fieldVO.getRelationalTable()) && StringUtils.isBlank(fieldVO.getCustomOptions())
                && StringUtils.isNotBlank(fieldVO.getFkNameField()) && StringUtils.isNotBlank(fieldVO.getFkKeyField())) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tableName", fieldVO.getRelationalTable());
            paramMap.put("fkFieldName", fieldVO.getFkNameField());
            paramMap.put("FkKeyField", fieldVO.getFkKeyField());
            if (StringUtils.isNotBlank(fieldVO.getQueryFieldSql())) {
                paramMap.put("customsql", fieldVO.getQueryFieldSql());
            }

            List<Map<String, Object>> fileData = generalMethodMapper.getQueryFieldData(paramMap);
            doRelationFieldData(fileData, childrenData, fieldVO.getFkKeyField(), fieldVO.getFkNameField());
        }
        return childrenData;
    }


    private void doRelationFieldData(List<Map<String, Object>> data, List<Map<String, Object>> resultList, String key, String value) {
        for (Map<String, Object> fileDatum : data) {
            Map<String, Object> map = new HashMap<>();
            map.put("value", fileDatum.get(key));
            map.put("labelName", fileDatum.get(value));
            resultList.add(map);
        }
    }


    private void doCustomFieldData(String jsonCustomData, List<Map<String, Object>> resultList) {
        Map jsonObject = JSONObject.fromObject(jsonCustomData);
        for (Object key : jsonObject.keySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("value", key);
            map.put("labelName", jsonObject.get(key));
            resultList.add(map);
        }
    }


    private List<Map<String, Object>> doFieldControlTypeForTree(CommonSelectFieldConfigVO fieldVO) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (StringUtils.isNotBlank(fieldVO.getRelationalTable()) &&
                StringUtils.isNotBlank(fieldVO.getFkNameField()) &&
                StringUtils.isNotBlank(fieldVO.getFkKeyField())) {
            final String parentFieldName = fieldVO.getParentKeyField();
            final String keyFieldName = fieldVO.getFkKeyField();
            final String labelNameFieldName = fieldVO.getFkNameField();
            List<String> queryFields = new ArrayList<>();
            queryFields.add(parentFieldName);
            queryFields.add(keyFieldName);
            queryFields.add(labelNameFieldName);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("queryField", queryFields);
            paramMap.put("tableName", fieldVO.getRelationalTable());
            if (StringUtils.isNotBlank(fieldVO.getQueryFieldSql())) {
                paramMap.put("sql", fieldVO.getQueryFieldSql());
            }

            List<Map<String, Object>> fileData = generalMethodMapper.getTreeData(paramMap);
            for (Map<String, Object> map : fileData) {

                boolean top;

                top = map.get(parentFieldName) == null
                        || map.get(parentFieldName).toString().equals("")
                        || map.get(parentFieldName).toString().equals("0")
                        || map.get(keyFieldName).toString().equals(map.get(parentFieldName).toString())
                        || fieldVO.getRelationalTable().equals("Base_SysMenu") && map.get(parentFieldName).toString().equals("root")
                        || istop(fileData, map.get(parentFieldName), keyFieldName);
                if (top) {
                    String keyValue = map.get(keyFieldName).toString();
                    String labelName = map.get(labelNameFieldName).toString();
                    Map<String, Object> topMap = new LinkedHashMap<>();
                    topMap.put("id", keyValue);
                    topMap.put("label", labelName);
                    List<Map<String, Object>> listMapInfo = doTreeData(fileData, keyValue, parentFieldName, keyFieldName, labelNameFieldName);
                    if (listMapInfo.size() > 0) {
                        topMap.put("children", listMapInfo);
                    }
                    listMap.add(topMap);
                }
            }
        }
        return listMap;
    }

    private boolean istop(List<Map<String, Object>> data, Object pid, String keyFieldName) {
        for (Map<String, Object> datum : data) {
            Object o = datum.get(keyFieldName);
            if (o.equals(pid)) {
                return false;
            }
        }
        return true;
    }


    private List<Map<String, Object>> doTreeData(List<Map<String, Object>> fileData, String parentID, String parentFieldName, String keyFieldName, String labelNameFieldName) {
        List<Map<String, Object>> listMapInfo = new ArrayList<>();
        for (Map<String, Object> datum : fileData) {
            if (datum.get(parentFieldName) != null && !datum.get(keyFieldName).equals(datum.get(parentFieldName)) && datum.get(parentFieldName).toString().equals(parentID)) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", datum.get(keyFieldName));
                map.put("label", datum.get(labelNameFieldName) == null ? "" : datum.get(labelNameFieldName));
                List<Map<String, Object>> maps = doTreeData(fileData, datum.get(keyFieldName).toString(), parentFieldName, keyFieldName, labelNameFieldName);
                if (maps.size() > 0) {
                    map.put("children", maps);
                }
                listMapInfo.add(map);
            }
        }
        return listMapInfo;
    }


    private void doDeleteRelationTableData(List<Object> ids, String tableName, String fieldName) {
        Map<String, Object> deleteParamMap = new HashMap<>();
        deleteParamMap.put("tableName", tableName);
        deleteParamMap.put("fieldName", fieldName);
        deleteParamMap.put("fieldValues", ids);
        generalMethodMapper.deleteMethod(deleteParamMap);
        deleteParamMap.clear();
    }


    @Override
    public String isTableDataHaveInfo(Map<String, Object> paramMap, CommonSelectTableConfigVO tableConfigVO) {

        paramMap.put("tableName", tableConfigVO.getTableName());
        List<Map<String, Object>> value = generalMethodMapper.isTableDataHaveInfo(paramMap);
        if (value.size() == 0) {
            return "no";
        } else {
            return "yes";
        }
    }


    @Override
    public Map<String, Object> getUserButtonAuthInMenu(String menuID, String userID) {
        Map<String, Object> resultMap = new HashMap<>(3);

        List<ButtonVO> buttons = buttonMapper.getButtonsByMenuIdAndUserId(menuID, userID);
        if (buttons.size() > 0) {

            List<Map<String, Object>> topOperations = new ArrayList<>();

            List<Map<String, Object>> listOperation = new ArrayList<>();
            for (ButtonVO button : buttons) {
                Map<String, Object> buttonMap = new HashMap<>(6);

                buttonMap.put("name", button.getButtonCode());

                buttonMap.put("label", button.getButtonName());

                buttonMap.put("icon", button.getButtonImg());

                buttonMap.put("type", button.getButtonStyle());

                if ("2".equals(button.getButtonType())) {
                    topOperations.add(buttonMap);
                }

                if ("1".equals(button.getButtonType())) {
                    listOperation.add(buttonMap);
                }
            }
            resultMap.put("topbuttondata", topOperations);
            resultMap.put("tablebuttondata", listOperation);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getQueryFields(List<CommonSelectFieldConfigVO> queryFields) {
        Map<String, Object> resultMap = new HashMap<>(3);

        List<Map<String, Object>> queryFieldsInfo = new ArrayList<>();

        Map<String, Object> ruleForm = new HashMap<>(3);

        List<String> twoControlRange = new ArrayList<>();
        if (queryFields.size() > 0) {
            for (CommonSelectFieldConfigVO field : queryFields) {
                if (StringUtils.isNotBlank(field.getControlType())) {
                    doFieldByControlType(field, ruleForm, queryFieldsInfo, null, twoControlRange);
                }
            }
            resultMap.put("querycontroldata", queryFieldsInfo);
            resultMap.put("dualcontrolskey", twoControlRange);
            resultMap.put("queryformdata", ruleForm);
        }
        return resultMap;
    }


}
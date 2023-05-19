package com.pub.controller;


import com.alibaba.fastjson.JSONObject;
import com.tjpu.pk.common.datasource.DynamicDataSourceContextHolderUtil;
import com.tjpu.pk.common.utils.*;
import com.pub.common.utils.RedisTemplateUtil;
import com.pub.impl.CommonServiceSupport;
import com.pub.model.CommonSelectFieldConfigVO;
import com.pub.model.CommonSelectTableConfigVO;
import com.pub.model.OperateLogVO;
import com.pub.model.SysMenuVO;
import com.pub.service.*;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("generalMethod")
public class GeneralMethodController {
    private final GeneralMethodService generalMethodService;
    private final CommonSelectTableConfigService commonSelectTableConfigService;
    private final CommonSelectFieldConfigService commonSelectFieldConfigService;
    private final SysMenuService sysMenuService;
    private final OperateLogService operateLogService;


    @Value("${spring.datasource.primary.name}")
    private String defaultDataSource;

    @Value("${spring.datasource.slave.smartpark.name}")
    private String slaveDataSource;

    @Autowired
    public GeneralMethodController(GeneralMethodService generalMethodService, CommonSelectTableConfigService commonSelectTableConfigService, CommonSelectFieldConfigService commonSelectFieldConfigService, SysMenuService sysMenuService, OperateLogService operateLogService) {
        this.generalMethodService = generalMethodService;
        this.commonSelectTableConfigService = commonSelectTableConfigService;
        this.commonSelectFieldConfigService = commonSelectFieldConfigService;
        this.sysMenuService = sysMenuService;
        this.operateLogService = operateLogService;
    }


    @RequestMapping(value = "getListByParam", method = RequestMethod.POST)
    public Object getListByParam(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            String sysModel = paramMap.get("sysmodel").toString();

            CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(sysModel);
            if (tableVO != null) {

                Map<String, String> customFieldType = getListFieldConfigType(paramMap);
                List<CommonSelectFieldConfigVO> fields = getFieldsForSql(tableVO.getPkTableConfigId(), customFieldType);
                List<CommonSelectFieldConfigVO> queryFields = new ArrayList<>();
                List<CommonSelectFieldConfigVO> listFields = new ArrayList<>();
                doListAndQueryFields(customFieldType, fields, queryFields, listFields);
                List<Map<String, Object>> tableTitle = CommonServiceSupport.getTableTitle(listFields);
                if (listFields.size() > 0) {
                    Object dataSource = paramMap.get("datasource");
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                    }
                    Map<String, Object> resultMap = new HashMap<>();
                    Map<String, Object> querydata = generalMethodService.getQueryFields(queryFields);
                    Map<String, Object> tabledata = generalMethodService.getListData(tableVO, queryFields, listFields, fields, paramMap);
                    tabledata.put("tabletitledata", tableTitle);
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                    }
                    if (paramMap.get("userid") != null) {
                        String userId = paramMap.get("userid").toString();
                        SysMenuVO menuVO = sysMenuService.getMenuVOByMenuCode(sysModel);
                        if (menuVO != null) {
                            String menuId = paramMap.get("menuid") != null ? paramMap.get("menuid").toString() : menuVO.getMenuId();
                            Map<String, Object> buttondata = generalMethodService.getUserButtonAuthInMenu(menuId, userId);
                            resultMap.put("buttondata", buttondata);
                        }
                    }
                    resultMap.put("querydata", querydata);
                    resultMap.put("tabledata", tabledata);
                    return AuthUtil.parseJsonKeyToLower("success", resultMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }



    private void doListAndQueryFields(Map<String, String> customFieldType, List<CommonSelectFieldConfigVO> fields, List<CommonSelectFieldConfigVO> queryFields, List<CommonSelectFieldConfigVO> listFields) {
        for (CommonSelectFieldConfigVO field : fields) {
            if (field.getConfigType().equals(customFieldType.get("query"))) {
                queryFields.add(field);
            } else if (field.getConfigType().equals(customFieldType.get("list"))) {
                listFields.add(field);
            }
        }
    }


    private List<CommonSelectFieldConfigVO> getFieldsForSql(String configTableId, Map<String, String> customFieldType) {
        Collection<String> values = customFieldType.values();
        List<String> fieldTypes = new ArrayList<>(values);
        return commonSelectFieldConfigService.getFieldListByTableIdAndConfigTypeList(configTableId, fieldTypes);
    }


    @RequestMapping(value = "getTableTitle", method = RequestMethod.POST)
    public Object getTableTitle(HttpServletRequest request) {
        Map<String, Object> paramMap = doMicroServiceParam(request);
        String sysModel = paramMap.get("sysmodel").toString();
        CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(sysModel);
        if (tableVO != null) {
            Map<String, String> customFieldType = getListFieldConfigType(paramMap);
            List<CommonSelectFieldConfigVO> listFields = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableVO.getPkTableConfigId(), customFieldType.get("list"));
            List<Map<String, Object>> tableTitle = CommonServiceSupport.getTableTitle(listFields);
            return AuthUtil.parseJsonKeyToLowerNoEncrypt("success", tableTitle);
        }
        return AuthUtil.parseJsonKeyToLowerNoEncrypt("success", null);
    }


    @SuppressWarnings("unchecked")
    @RequestMapping(value = "getHSSFWorkbook", method = RequestMethod.POST)
    public byte[] getHSSFWorkbook(HttpServletRequest request) throws IOException {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            String sysModel = paramMap.get("sysmodel").toString();
            CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(sysModel);
            Map<String, String> customFieldType = getListFieldConfigType(paramMap);
            List<CommonSelectFieldConfigVO> fields = getFieldsForSql(tableVO.getPkTableConfigId(), customFieldType);
            List<CommonSelectFieldConfigVO> queryFields = new ArrayList<>();
            List<CommonSelectFieldConfigVO> listFields = new ArrayList<>();
            doListAndQueryFields(customFieldType, fields, queryFields, listFields);
            List<String> excelFieldList = new ArrayList<>();
            List<String> excelTitleList = new ArrayList<>();
            if (paramMap.get("excelfields") != null && paramMap.get("exceltitles") != null) {
                JSONArray excelfields = JSONArray.fromObject(paramMap.get("excelfields"));
                JSONArray exceltitles = JSONArray.fromObject(paramMap.get("exceltitles"));
                if (excelfields.size() > 0 && exceltitles.size() > 0 && excelfields.size() == exceltitles.size()) {
                    for (Object o : excelfields) {
                        excelFieldList.add(o.toString());
                    }
                    for (Object o : exceltitles) {
                        excelTitleList.add(o.toString());
                    }
                }
            } else {
                List<Map<String, Object>> tableTitle = CommonServiceSupport.getTableTitle(listFields);
                for (Map<String, Object> map : tableTitle) {
                    if ((boolean) map.get("showhide") && map.get("prop") != null && map.get("label") != null) {
                        String fieldName = (String) map.get("prop");
                        String titlename = (String) map.get("label");
                        excelFieldList.add(fieldName);
                        excelTitleList.add(titlename);
                    }

                }
            }
            if (listFields.size() > 0 && excelFieldList.size() > 0 && excelFieldList.size() == excelTitleList.size()) {
                Object dataSource = paramMap.get("datasource");
                if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                    DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                }
                Map<String, Object> resultMap = generalMethodService.getListData(tableVO, queryFields, listFields, fields, paramMap);
                if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                    DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                }
                List<Map<String, Object>> listInfo = (List<Map<String, Object>>) resultMap.get("tablelistdata");
                String fileName = paramMap.get("excelfilename") != null ? paramMap.get("excelfilename").toString() : tableVO.getTableComments().replace("-", "");
                HSSFWorkbook workbook = ExcelUtil.exportExcel(fileName, excelTitleList, excelFieldList, listInfo, null);
                return ExcelUtil.getBytesForWorkBook(workbook);
            }
            return null;
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "getListData", method = RequestMethod.POST)
    public Object getListData(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            String sysModel = paramMap.get("sysmodel").toString();

            CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(sysModel);
            if (StringUtils.isNotBlank(sysModel) && tableVO != null) {
                Map<String, String> customFieldType = getListFieldConfigType(paramMap);
                List<CommonSelectFieldConfigVO> fields = getFieldsForSql(tableVO.getPkTableConfigId(), customFieldType);
                List<CommonSelectFieldConfigVO> queryFields = new ArrayList<>();
                List<CommonSelectFieldConfigVO> listFields = new ArrayList<>();
                doListAndQueryFields(customFieldType, fields, queryFields, listFields);
                if (listFields.size() > 0) {
                    Object dataSource = paramMap.get("datasource");
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                    }
                    Map<String, Object> resultMap = generalMethodService.getListData(tableVO, queryFields, listFields, fields, paramMap);
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                    }
                    return AuthUtil.parseJsonKeyToLower("success", resultMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "getAddPageInfo", method = RequestMethod.POST)
    public Object getAddPageInfo(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(paramMap.get("sysmodel").toString());
            if (tableVO != null) {
                String fieldType = paramMap.get("addfieldtype") != null ? paramMap.get("addfieldtype").toString() : ConstantsUtil.FieldConfigType.ADD.getValue();
                List<CommonSelectFieldConfigVO> fieldVOs = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableVO.getPkTableConfigId(), fieldType);
                deleteFilterFields(paramMap, fieldVOs);
                if (fieldVOs.size() > 0) {
                    Object dataSource = paramMap.get("datasource");
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                    }
                    Map<String, Object> resultMap = generalMethodService.getAddPageInfo(fieldVOs, tableVO, paramMap);
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                    }
                    return AuthUtil.parseJsonKeyToLower("success", resultMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "doAddMethod", method = RequestMethod.POST)
    public Object doAddMethod(HttpServletRequest request) {
        try {
            final Map<String, Object> paramMap = doMicroServiceParam(request);
            String sysModel = paramMap.get("sysmodel").toString();

            CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(sysModel);
            final Map<String, Object> formData = JSONObject.parseObject(paramMap.get("formdata").toString());
            if (tableVO != null && StringUtils.isNotBlank(tableVO.getKeyFieldName()) && formData != null && formData.size() > 0) {
                String fieldType = paramMap.get("addfieldtype") != null ? paramMap.get("addfieldtype").toString() : ConstantsUtil.FieldConfigType.ADD.getValue();
                List<CommonSelectFieldConfigVO> fieldVOs = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableVO.getPkTableConfigId(), fieldType);
                deleteFilterFields(paramMap, fieldVOs);
                if (fieldVOs.size() > 0) {
                    int isHave = 0;
                    String tableNameString = "'" + tableVO.getTableName() + "'";
                    Object dataSource = paramMap.get("datasource");
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        String datasource = dataSource.toString();
                        if (!datasource.equals(defaultDataSource)) {
                            DynamicDataSourceContextHolderUtil.setDataSourceType(datasource);

                            isHave = commonSelectTableConfigService.getTableHasIdentity(tableNameString);
                            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                        } else {
                            isHave = commonSelectTableConfigService.getTableHasIdentity(tableNameString);
                        }
                    } else {
                        isHave = commonSelectTableConfigService.getTableHasIdentity(tableNameString);
                    }
                    String pkValue = "";
                    if (isHave == 0) {

                        if (tableVO.getKeyFieldIsNumber() != null && tableVO.getKeyFieldIsNumber() == 1) {
                            Map<String, Object> paramMapTemp = new HashMap<>();
                            paramMapTemp.put("tablename", tableVO.getTableName());
                            paramMapTemp.put("pkid", tableVO.getKeyFieldName());
                            int pkId = 0;
                            if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                                String datasource = dataSource.toString();
                                if (!datasource.equals(defaultDataSource)) {
                                    DynamicDataSourceContextHolderUtil.setDataSourceType(datasource);
                                    pkId = commonSelectTableConfigService.getMaxNumByTableName(paramMapTemp);
                                    DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                                } else {
                                    pkId = commonSelectTableConfigService.getMaxNumByTableName(paramMapTemp);
                                }
                            } else {
                                pkId = commonSelectTableConfigService.getMaxNumByTableName(paramMapTemp);
                            }
                            pkValue = (pkId + 1) + "";
                        } else {
                            pkValue = UUID.randomUUID().toString();
                        }
                    }
                    List<String> addFieldsName = new ArrayList<>();
                    List<CommonSelectFieldConfigVO> mainDefaultFields = new ArrayList<>();
                    List<CommonSelectFieldConfigVO> relationFieldVo = new ArrayList<>();
                    for (CommonSelectFieldConfigVO fieldConfigVO : fieldVOs) {
                        if (StringUtils.isBlank(fieldConfigVO.getMiddleTable())) {
                            if (StringUtils.isBlank(fieldConfigVO.getControlType()) && StringUtils.isNotBlank(fieldConfigVO.getControlDefault())) {
                                mainDefaultFields.add(fieldConfigVO);
                            }
                            addFieldsName.add(fieldConfigVO.getFieldName());
                        } else {
                            relationFieldVo.add(fieldConfigVO);
                        }
                    }
                    Map<String, Object> addMainTableMap = new HashMap<>();
                    String tableName = tableVO.getTableName();
                    if (addFieldsName.size() > 0) {
                        List<String> fieldList = new ArrayList<>();
                        List<Object> values = new ArrayList<>();

                        if (isHave == 0) {

                            fieldList.add(tableVO.getKeyFieldName());
                            values.add(pkValue);
                        }
                        for (CommonSelectFieldConfigVO field : mainDefaultFields) {
                            final String value = CommonServiceSupport.getDefaultFieldMethodValue(field.getControlDefault());
                            if (StringUtils.isNotBlank(value)) {
                                fieldList.add(field.getFieldName());
                                values.add(value);
                            }
                        }
                        CommonServiceSupport.doStoredProcedureFieldsForAddMethod(mainDefaultFields, fieldList, values, formData);
                        for (String s : addFieldsName) {
                            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                                if (s.equalsIgnoreCase(entry.getKey())) {
                                    if (entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())) {
                                        fieldList.add(s);
                                        values.add(entry.getValue());
                                    }
                                }
                            }
                        }
                        addMainTableMap.put("fieldList", fieldList);
                        addMainTableMap.put("values", values);
                        addMainTableMap.put("tableName", tableName);
                    }

                    List<Map<String, Object>> addRelationAddListMapSql = getAddRelationAddListMapSql(pkValue, relationFieldVo, formData, dataSource);
                    if (addMainTableMap.size() > 0) {
                        if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                            String datasource = dataSource.toString();
                            if (!datasource.equals(defaultDataSource)) {
                                DynamicDataSourceContextHolderUtil.setDataSourceType(datasource);
                                generalMethodService.doAddMethod(addMainTableMap, addRelationAddListMapSql, tableName, datasource, formData, pkValue);
                                DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                                Map<String, Object> paramMaps = new HashMap<>();
                                paramMaps.put("tablename", tableName);
                                operateLogService.saveUserOperationLog("add", paramMaps, null, null, formData);
                            } else {
                                generalMethodService.doAddMethod(addMainTableMap, addRelationAddListMapSql, tableName, null, formData, pkValue);
                            }
                        } else {
                            generalMethodService.doAddMethod(addMainTableMap, addRelationAddListMapSql, tableName, null, formData, pkValue);
                        }
                        return AuthUtil.parseJsonKeyToLower("success", pkValue);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }


    private List<Map<String, Object>> getAddRelationAddListMapSql(String pkValue, List<CommonSelectFieldConfigVO> relationFieldVo, Map<String, Object> formData, Object dataSource) {
        List<Map<String, Object>> sqlMapList = new ArrayList<>();
        if (relationFieldVo.size() > 0) {
            for (CommonSelectFieldConfigVO fieldVo : relationFieldVo) {
                final String key = fieldVo.getFieldName();
                for (Map.Entry<String, Object> entry : formData.entrySet()) {
                    if (entry.getValue() != null && entry.getKey().equalsIgnoreCase(key) && StringUtils.isNotBlank(entry.getValue().toString())) {
                        String middleTableName = fieldVo.getMiddleTable();
                        String pkFieldName;
                        if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                            String datasource = dataSource.toString();
                            DynamicDataSourceContextHolderUtil.setDataSourceType(datasource);
                            pkFieldName = generalMethodService.getPKFieldNameByTableName(middleTableName);
                            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                        } else {
                            pkFieldName = generalMethodService.getPKFieldNameByTableName(middleTableName);
                        }
                        if (StringUtils.isNotBlank(pkFieldName)) {
                            Map<String, Object> sqlMap = new HashMap<>(3);
                            List<String> fieldList = new ArrayList<>();
                            fieldList.add(pkFieldName);
                            fieldList.add(fieldVo.getLeftId());
                            String[] split = fieldVo.getRightId().split(",");
                            fieldList.addAll(Arrays.asList(split));
                            List<Object> defaultValues = new ArrayList<>();
                            final CommonSelectTableConfigVO middleTableVO = commonSelectTableConfigService.getTableConfigByName(fieldVo.getMiddleTable());
                            if (middleTableVO != null) {
                                List<CommonSelectFieldConfigVO> defaultFields = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(middleTableVO.getPkTableConfigId(), ConstantsUtil.FieldConfigType.ADD.getValue());
                                if (defaultFields.size() > 0) {
                                    for (CommonSelectFieldConfigVO field : defaultFields) {
                                        final String value = CommonServiceSupport.getDefaultFieldMethodValue(field.getControlDefault());
                                        if (StringUtils.isNotBlank(value)) {
                                            defaultValues.add(value);
                                            fieldList.add(field.getFieldName());
                                        }
                                    }
                                }
                            }

                            List<Object> values = new ArrayList<>();
                            final String addValues = entry.getValue().toString();
                            final String[] split1 = addValues.split(",");
                            for (String s : split1) {
                                List<Object> value2 = new ArrayList<>();
                                value2.add(UUID.randomUUID().toString());
                                value2.add(pkValue);
                                if (split.length == 1) {
                                    value2.add(s);
                                } else if (split.length == 2) {
                                    final String[] split2 = s.split("_");
                                    if (split2.length == 2) {
                                        value2.add(split2[0]);
                                        value2.add(split2[1]);
                                    } else if (split2.length == 1) {
                                        value2.add(split2[0]);
                                        value2.add(null);
                                    }
                                }
                                value2.addAll(defaultValues);
                                values.add(value2);
                            }
                            sqlMap.put("fieldList", fieldList);
                            sqlMap.put("values", values);
                            sqlMap.put("tableName", fieldVo.getMiddleTable());
                            sqlMapList.add(sqlMap);
                        }
                    }
                }
            }
        }
        return sqlMapList;
    }

    @RequestMapping(value = "goUpdatePage", method = RequestMethod.POST)
    public Object goUpdateMethod(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            CommonSelectTableConfigVO tableVO = getTableConfigVO(paramMap);
            if (tableVO != null) {
                String fieldType = paramMap.get("editfieldtype") != null ? paramMap.get("editfieldtype").toString() : ConstantsUtil.FieldConfigType.EDIT.getValue();
                List<CommonSelectFieldConfigVO> fieldVOs = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableVO.getPkTableConfigId(), fieldType);

                deleteFilterFields(paramMap, fieldVOs);
                if (fieldVOs.size() > 0) {

                    Object dataSource = paramMap.get("datasource");
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                    }

                    Map<String, Object> resultMap = generalMethodService.getEditPageData(fieldVOs, tableVO, paramMap);
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                    }
                    return AuthUtil.parseJsonKeyToLower("success", resultMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "doEditMethod", method = RequestMethod.POST)
    @SuppressWarnings("unchecked")
    public Object doEditMethod(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            CommonSelectTableConfigVO tableVO = getTableConfigVO(paramMap);
            Map<String, Object> newDataMap = (Map<String, Object>) paramMap.get("formdata");
            if (null != tableVO && newDataMap != null && newDataMap.size() > 0) {
                String fieldType = paramMap.get("editfieldtype") != null ? paramMap.get("editfieldtype").toString() : ConstantsUtil.FieldConfigType.EDIT.getValue();
                List<CommonSelectFieldConfigVO> fieldVOs = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableVO.getPkTableConfigId(), fieldType);
                deleteFilterFields(paramMap, fieldVOs);
                if (fieldVOs.size() > 0) {
                    Object dataSource = paramMap.get("datasource");
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("tableName", tableVO.getTableName());
                    String pkFieldName = tableVO.getKeyFieldName().toLowerCase();
                    map.put(pkFieldName, newDataMap.get(pkFieldName));
                    Map<String, Object> resultMap = generalMethodService.getEditPageData(fieldVOs, tableVO, map);
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                    }
                    Map<String, Object> oldDataMapInfo = (Map<String, Object>) resultMap.get("editformdata");
                    Map<String, Object> oldDataMap = DataFormatUtil.toLowerCaseMap(oldDataMapInfo);
                    formatEditData(oldDataMap, newDataMap);
                    Map<String, Object> compareResultMap = getCompareResultEditData(oldDataMap, newDataMap);
                    if (compareResultMap.size() > 0) {
                        List<CommonSelectFieldConfigVO> middleFields = new ArrayList<>();
                        List<CommonSelectFieldConfigVO> defaultFields = new ArrayList<>();
                        Map<String, Object> mainMap = new HashMap<>();
                        for (CommonSelectFieldConfigVO fieldVO : fieldVOs) {
                            for (Map.Entry<String, Object> entry : compareResultMap.entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(fieldVO.getFieldName())) {
                                    if (StringUtils.isNotBlank(fieldVO.getControlType())) {
                                        if (StringUtils.isBlank(fieldVO.getMiddleTable())) {
                                            if (entry.getValue() instanceof ArrayList) {
                                                List<Object> value = (List<Object>) entry.getValue();
                                                String info = "";
                                                for (Object o : value) {
                                                    info += o.toString() + ",";
                                                }
                                                if (StringUtils.isNotBlank(info)) {
                                                    mainMap.put(entry.getKey(), info.substring(0, info.length() - 1));
                                                } else {
                                                    mainMap.put(entry.getKey(), null);
                                                }
                                            } else {
                                                mainMap.put(entry.getKey(), entry.getValue().equals("") ? null : entry.getValue());
                                            }
                                        } else {
                                            middleFields.add(fieldVO);
                                        }
                                    }
                                }
                            }
                            if (StringUtils.isBlank(fieldVO.getControlType()) && StringUtils.isNotBlank(fieldVO.getControlDefault())) {
                                defaultFields.add(fieldVO);
                            }
                        }
                        if (defaultFields.size() > 0) {
                            for (CommonSelectFieldConfigVO fieldConfigVO : defaultFields) {
                                final String value = CommonServiceSupport.getDefaultFieldMethodValue(fieldConfigVO.getControlDefault());
                                if (StringUtils.isNotBlank(value)) {
                                    mainMap.put(fieldConfigVO.getFieldName(), value);
                                }
                            }
                        }
                        Map<String, Object> relationTableDefaultFields = getRelationTableDefaultFields(middleFields, dataSource);
                        String flag;
                        if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                            DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                            flag = generalMethodService.doEditMethod(tableVO, newDataMap, oldDataMap, mainMap, compareResultMap, middleFields, relationTableDefaultFields, dataSource);
                            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                            Map<String, Object> paramMaps = new HashMap<>();
                            paramMaps.put("tablename", tableVO.getTableName());
                            operateLogService.saveUserOperationLog("edit", paramMaps, compareResultMap, oldDataMap, newDataMap);
                        } else {
                            if (tableVO.getTableName().equals("Base_userInfo")) {
                                flag = generalMethodService.doEditMethod(tableVO, newDataMap, oldDataMap, mainMap, compareResultMap, middleFields, relationTableDefaultFields, null);


                            } else {
                                flag = generalMethodService.doEditMethod(tableVO, newDataMap, oldDataMap, mainMap, compareResultMap, middleFields, relationTableDefaultFields, null);
                            }
                        }
                        return AuthUtil.parseJsonKeyToLower(flag, null);
                    } else {
                        return AuthUtil.parseJsonKeyToLower("success", null);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }

    private void deleteFilterFields(Map<String, Object> paramMap, List<CommonSelectFieldConfigVO> fieldVOs) {
        if (paramMap.get("deletefields") != null) {
            JSONArray deletefields = JSONArray.fromObject(paramMap.get("deletefields"));
            Iterator<CommonSelectFieldConfigVO> iterator = fieldVOs.iterator();
            while (iterator.hasNext()) {
                CommonSelectFieldConfigVO fieldVO = iterator.next();
                for (Object o : deletefields) {
                    if (o.toString().equals(fieldVO.getFilterFieldFlag())) {
                        iterator.remove();
                    }
                }
            }

        }
    }


    private Map<String, Object> getRelationTableDefaultFields(List<CommonSelectFieldConfigVO> middleFields, Object dataSource) {
        Map<String, Object> map = new HashMap<>();
        for (CommonSelectFieldConfigVO field : middleFields) {
            String middleTableName = field.getMiddleTable();
            if (!map.containsKey(middleTableName)) {
                String pkFieldName = "";
                if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                    String datasource = dataSource.toString();
                    if (!datasource.equals(defaultDataSource)) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(datasource);
                        pkFieldName = generalMethodService.getPKFieldNameByTableName(middleTableName);
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                    } else {
                        pkFieldName = generalMethodService.getPKFieldNameByTableName(middleTableName);
                    }
                } else {
                    pkFieldName = generalMethodService.getPKFieldNameByTableName(middleTableName);
                }
                if (StringUtils.isNotBlank(pkFieldName)) {
                    map.put("$" + middleTableName, pkFieldName);
                    CommonSelectTableConfigVO middleTableVO = commonSelectTableConfigService.getTableConfigByName(middleTableName);
                    if (middleTableVO != null) {
                        String middleTablePkId = middleTableVO.getPkTableConfigId();
                        List<CommonSelectFieldConfigVO> defaultFieldVOs = commonSelectFieldConfigService.getDefaultAddFields(middleTablePkId, ConstantsUtil.FieldConfigType.ADD.getValue());
                        map.put(middleTableName, defaultFieldVOs);
                    } else {
                        map.put(middleTableName, new ArrayList<CommonSelectFieldConfigVO>());
                    }
                }
            }
        }
        return map;
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> getCompareResultEditData(Map<String, Object> oldDataMap, Map<String, Object> newDataMap) {
        Map<String, Object> compareResultMap = new HashMap<>();
        for (Map.Entry<String, Object> oldEntry : oldDataMap.entrySet()) {
            for (Map.Entry<String, Object> newEntry : newDataMap.entrySet()) {
                if (oldEntry.getKey().equalsIgnoreCase(newEntry.getKey())) {
                    Object oldValue = oldEntry.getValue();
                    Object newValue = newEntry.getValue();
                    if (oldValue.getClass().equals(newValue.getClass())) {
                        if (oldValue instanceof ArrayList) {
                            List<Object> oldList = (List<Object>) oldValue;
                            List<Object> newList = (List<Object>) newValue;
                            if (oldList.size() != newList.size()) {
                                compareResultMap.put(newEntry.getKey(), newList);
                            } else {
                                for (Object newValueInfo : newList) {
                                    if (!oldList.contains(newValueInfo)) {
                                        compareResultMap.put(newEntry.getKey(), newList);
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (!oldValue.equals(newValue)) {
                                compareResultMap.put(newEntry.getKey(), newValue);
                            }
                        }
                    } else {
                        if (!oldValue.toString().equals(newValue.toString())) {
                            compareResultMap.put(newEntry.getKey(), newValue);
                        }
                    }
                }
            }
        }
        return compareResultMap;
    }



    private void formatEditData(Map<String, Object> oldDataMap, Map<String, Object> newDataMap) {
        formatEditMap(oldDataMap);
        formatEditMap(newDataMap);
        for (Map.Entry<String, Object> oldEntry : oldDataMap.entrySet()) {
            for (Map.Entry<String, Object> newEntry : newDataMap.entrySet()) {
                if (oldEntry.getKey().equalsIgnoreCase(newEntry.getKey())) {
                    Object oldValue = oldEntry.getValue();
                    Object newValue = newEntry.getValue();
                    if (oldValue instanceof ArrayList && !(newValue instanceof ArrayList)) {
                        if (StringUtils.isNotBlank(newValue.toString())) {
                            String[] split = newValue.toString().split(",");
                            newEntry.setValue(new ArrayList<>(Arrays.asList(split)));
                        } else {
                            newEntry.setValue(new ArrayList<>());
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void formatEditMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                if (value.getClass().isArray() || value instanceof JSONArray) {
                    List<Object> valueList = value.getClass().isArray() ? new ArrayList<>(Arrays.asList((Object[]) value)) : new ArrayList<>((JSONArray) value);
                    entry.setValue(valueList);
                }
            } else {
                entry.setValue("");
            }
        }
    }


    @RequestMapping("getDetail")
    public Object getDetail(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            CommonSelectTableConfigVO tableVO = getTableConfigVO(paramMap);
            if (tableVO != null) {

                String pkValue = paramMap.get(tableVO.getKeyFieldName().toLowerCase()).toString();

                String fieldType = paramMap.get("detailfieldtype") != null ? paramMap.get("detailfieldtype").toString() : ConstantsUtil.FieldConfigType.DETAIL.getValue();

                List<CommonSelectFieldConfigVO> detailFields = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableVO.getPkTableConfigId(), fieldType);
                deleteFilterFields(paramMap, detailFields);
                if (detailFields.size() > 0 && StringUtils.isNotBlank(pkValue)) {
                    Object dataSource = paramMap.get("datasource");
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                    }
                    Map<String, Object> detailData = generalMethodService.getDetailData(tableVO, pkValue, detailFields);
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                    }
                    return AuthUtil.parseJsonKeyToLower("success", detailData);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {

            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "deleteMethod", method = RequestMethod.POST)
    @SuppressWarnings("unchecked")
    public Object deleteMethod(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            CommonSelectTableConfigVO tableConfigVO = getTableConfigVO(paramMap);
            if (tableConfigVO != null) {
                List<Object> ids = new ArrayList<>();
                String pkFieldName = tableConfigVO.getKeyFieldName().toLowerCase();
                Object pkValue = paramMap.get(pkFieldName).toString();
                ids.add(pkValue);
                List<CommonSelectFieldConfigVO> fieldVOs = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableConfigVO.getPkTableConfigId(), ConstantsUtil.FieldConfigType.DELETE.getValue());
                List<CommonSelectFieldConfigVO> relationFiledVO = new ArrayList<>();
                if (fieldVOs.size() > 0) {
                    List<CommonSelectFieldConfigVO> treeFieldVO = new ArrayList<>();
                    for (CommonSelectFieldConfigVO fieldVO : fieldVOs) {
                        if (StringUtils.isNotBlank(fieldVO.getParentKeyField()) && StringUtils.isNotBlank(fieldVO.getControlType()) && fieldVO.getControlType().equals("tree")) {
                            treeFieldVO.add(fieldVO);
                        } else if (StringUtils.isNotBlank(fieldVO.getRelationalTable()) && !fieldVO.getRelationalTable().equals(tableConfigVO.getTableName())) {
                            relationFiledVO.add(fieldVO);
                        }
                    }
                    if (treeFieldVO.size() == 1) {

                        CommonSelectFieldConfigVO field = treeFieldVO.get(0);
                        Map<String, Object> map = new HashMap<>();
                        map.put("tableName", tableConfigVO.getTableName());
                        map.put("pkFieldName", pkFieldName);
                        map.put("pkValue", pkValue);
                        map.put("parentFieldName", field.getParentKeyField());
                        if (StringUtils.isNotBlank(field.getOtherFieldName())) {
                            map.put("codeFieldName", field.getOtherFieldName());
                        }
                        Object dataSource = paramMap.get("datasource");
                        if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                            DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                        }
                        List<Map<String, Object>> list = generalMethodService.getDeleteIDs(map);
                        if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                        }
                        if (list.size() > 0) {
                            ids.clear();
                            for (Map<String, Object> map1 : list) {
                                Object o = map1.get(pkFieldName);
                                ids.add(o);
                            }
                        }
                    }
                }
                Map<String, Object> paramMaps = new HashMap<>();
                paramMaps.put("tablename", tableConfigVO.getTableName());
                paramMaps.put("pkfieldname", tableConfigVO.getKeyFieldName());
                paramMaps.put("pkvalue", pkValue);

                Object dataSource = paramMap.get("datasource");
                if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                    DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                }

                Map<String, Object> OldMapsInfo = commonSelectFieldConfigService.getDeleteData(paramMaps);
                if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                    DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                }
                Map<String, Object> OldMaps = DataFormatUtil.toLowerCaseMap(OldMapsInfo);
                Map<String, Object> params = new HashMap<>();
                params.put("oldmaps", OldMaps);
                if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                    String datasource = dataSource.toString();
                    if (!datasource.equals(defaultDataSource)) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                        params.put("datasource", dataSource);

                        generalMethodService.deleteMethod(tableConfigVO, ids, relationFiledVO, params);
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);

                        Map<String, Object> paramsmap = new HashMap<>();
                        paramsmap.put("tablename", tableConfigVO.getTableName());
                        operateLogService.saveUserOperationLog("delete", paramsmap, null, OldMaps, null);
                    } else {
                        params.put("datasource", null);
                        generalMethodService.deleteMethod(tableConfigVO, ids, relationFiledVO, params);
                    }
                } else {
                    params.put("datasource", null);
                    generalMethodService.deleteMethod(tableConfigVO, ids, relationFiledVO, params);
                }
                return AuthUtil.parseJsonKeyToLower("success", null);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {

            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }


    private Map<String, String> getListFieldConfigType(Map<String, Object> paramMap) {
        Map<String, String> customFieldType = new HashMap<>(3);

        String listFieldType = ConstantsUtil.FieldConfigType.LIST.getValue();

        String queryControlFieldType = ConstantsUtil.FieldConfigType.QUERY.getValue();

        Object listTypeInfo = paramMap.get("listfieldtype");
        if (listTypeInfo != null && StringUtils.isNotBlank(listTypeInfo.toString())) {
            listFieldType = listTypeInfo.toString();
            String substring = listFieldType.substring(0, ConstantsUtil.FieldConfigType.LIST.getValue().length());
            if (ConstantsUtil.FieldConfigType.LIST.getValue().equals(substring)) {
                String info = listFieldType.substring(ConstantsUtil.FieldConfigType.LIST.getValue().length(), listFieldType.length());
                queryControlFieldType += info;
            }
        }
        customFieldType.put("list", listFieldType);
        customFieldType.put("query", queryControlFieldType);
        return customFieldType;
    }


    private CommonSelectTableConfigVO getTableConfigVO(Map<String, Object> paramMap) {
        Object sysModel = paramMap.get("sysmodel");
        if (sysModel != null && StringUtils.isNotBlank(sysModel.toString())) {
            CommonSelectTableConfigVO tableConfigVO = commonSelectTableConfigService.getTableConfigVOBySysModel(sysModel.toString());

            if (tableConfigVO != null && StringUtils.isNotBlank(tableConfigVO.getKeyFieldName())) {

                String keyFieldName = tableConfigVO.getKeyFieldName().toLowerCase();
                Object formData = paramMap.get("formdata");
                if (paramMap.get(keyFieldName) != null && StringUtils.isNotBlank(paramMap.get(keyFieldName).toString())) {
                    return tableConfigVO;
                } else if (formData != null && StringUtils.isNotBlank(formData.toString())) {

                    final Map<String, Object> newData = JSONObject.parseObject(formData.toString());
                    if (newData.get(keyFieldName) != null && StringUtils.isNotBlank(newData.get(keyFieldName).toString())) {
                        return tableConfigVO;
                    }
                }
            }
        }
        return null;
    }

    @RequestMapping(value = "isTableDataHaveInfo", method = RequestMethod.POST)
    public Object isTableDataHaveInfo(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            CommonSelectTableConfigVO tableConfigVO = commonSelectTableConfigService.getTableConfigVOBySysModel(paramMap.get("sysmodel").toString());
            if (tableConfigVO != null) {
                if (paramMap.get("datasource") != null) {
                    DynamicDataSourceContextHolderUtil.setDataSourceType(paramMap.get("datasource").toString());
                }

                String flag = generalMethodService.isTableDataHaveInfo(paramMap, tableConfigVO);
                return AuthUtil.parseJsonKeyToLower("success", flag);
            } else {
                return AuthUtil.parseJsonKeyToLower("fail", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
        }
    }


    private Map<String, Object> doMicroServiceParam(HttpServletRequest request) {
        String microParam = request.getParameter("microparam");
        Map<String, Object> paramMap;

        if (StringUtils.isNotBlank(microParam)) {
            JSONObject microParamJson = JSONObject.parseObject(microParam);
            String microParamData = microParamJson.getString("microparamdata");
            paramMap = RequestUtil.parseStringToMap(microParamData);
        } else {
            paramMap = RequestUtil.parseRequest(request);
        }
        return paramMap;
    }

    private void saveUpdateUserDataPermissionsLog(String tableName,
                                                  Map<String, Object> compareMap, Map<String, Object> oldMapData,
                                                  Map<String, Object> newMapData) {
        String logDescription = "";
        String logType = "handlelog";

        String firstDescription = operateLogService.doOperateLogVO("edit", tableName, oldMapData, newMapData);

        String lastDescription = "";

        String DataPermissions = "";

        Map<String, Object> oldMap = new HashMap<String, Object>();

        Map<String, Object> newMap = new HashMap<String, Object>();

        Map<String, Object> oldMaps = new HashMap<String, Object>();

        Map<String, Object> newMaps = new HashMap<String, Object>();

        if (!"".equals(firstDescription)) {

            for (Map.Entry<String, Object> entry : compareMap.entrySet()) {
                String key = entry.getKey();
                if (!"fk_region".equals(key) && !"fk_pollutionid".equals(key)) {
                    oldMaps.put(key, oldMapData.get(key));
                    newMaps.put(key, newMapData.get(key));
                }
            }
            oldMap.put("fk_region", oldMapData.get("fk_region"));
            newMap.put("fk_region", newMapData.get("fk_region"));
            oldMap.put("fk_pollutionid", oldMapData.get("fk_pollutionid"));
            newMap.put("fk_pollutionid", newMapData.get("fk_pollutionid"));

            if (oldMap.size() > 0 && newMap.size() > 0) {

                DynamicDataSourceContextHolderUtil.setDataSourceType(slaveDataSource);
                DataPermissions = (String) operateLogService.getUserDataPermissionsEditLog(oldMap, newMap);

                DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            }

            lastDescription = operateLogService.fomateEditLog(tableName, oldMaps, newMaps);
            if (!"".equals(DataPermissions) || !"".equals(lastDescription)) {
                logDescription = firstDescription + " " + lastDescription + " " + DataPermissions;
            }
            OperateLogVO operateLogVO = new OperateLogVO();

            String uuid = CommonServiceSupport.getUUID();

            String userName = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);

            operateLogVO.setBaseOperateId(uuid);
            operateLogVO.setBaseOperateType("edit");
            operateLogVO.setBaseOperateContent(logDescription);
            operateLogVO.setBaseOperatePerson(userName);
            operateLogVO.setBaseOperateDatetime(DataFormatUtil.getDate());
            operateLogVO.setBaseLogType(logType);
            if (!"".equals(logDescription)) {
                operateLogService.insert(operateLogVO);
            }
        }
    }


    @RequestMapping(value = "getUserButtonAuthInMenu", method = RequestMethod.POST)
    public Object getUserButtonAuthInMenu(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            String menuId = "";
            String userId = paramMap.get("userid").toString();
            if (paramMap.get("menuid") != null) {
                menuId = paramMap.get("menuid").toString();
            } else if (paramMap.get("sysmodel") != null) {
                SysMenuVO menuVO = sysMenuService.getMenuVOByMenuCode(paramMap.get("sysmodel").toString());
                menuId = menuVO == null ? "" : menuVO.getMenuId();
            }
            if (StringUtils.isNotBlank(menuId) && StringUtils.isNotBlank(userId)) {
                Map<String, Object> buttonAuthData = generalMethodService.getUserButtonAuthInMenu(menuId, userId);
                return AuthUtil.parseJsonKeyToLowerNoEncrypt("success", buttonAuthData);
            }
            return AuthUtil.parseJsonKeyToLowerNoEncrypt("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "getQueryCriteriaData", method = RequestMethod.POST)
    public Object getQueryCriteriaData(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = doMicroServiceParam(request);
            if (paramMap.get("sysmodel") != null && paramMap.get("queryfieldtype") != null) {
                String sysModel = paramMap.get("sysmodel").toString();
                String queryFieldType = paramMap.get("queryfieldtype").toString();
                CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(sysModel);
                Map<String, Object> queryFieldsData = new HashMap<>();
                if (tableVO!=null){
                    List<CommonSelectFieldConfigVO> queryFields = commonSelectFieldConfigService.getFieldsByFkTableConfigIdAndConfigType(tableVO.getPkTableConfigId(), queryFieldType);
                    Object dataSource = paramMap.get("datasource");
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(dataSource.toString());
                    }
                    queryFieldsData = generalMethodService.getQueryFields(queryFields);
                    if (dataSource != null && StringUtils.isNotBlank(dataSource.toString())) {
                        DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
                    }
                }
                return AuthUtil.parseJsonKeyToLowerNoEncrypt("success", queryFieldsData);
            }
            return AuthUtil.parseJsonKeyToLowerNoEncrypt("success", "");
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }
}
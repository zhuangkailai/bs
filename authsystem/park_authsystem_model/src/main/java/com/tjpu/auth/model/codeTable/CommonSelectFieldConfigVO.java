package com.tjpu.auth.model.codeTable;

import java.io.Serializable;

public class CommonSelectFieldConfigVO implements Serializable {

    private static final long serialVersionUID = 6346784295876771807L;

    private String pkFieldConfigId;

    private String fkTableConfigId;

    private String fieldName;

    private String fieldComments;

    private String fieldDataType;

    private Integer fieldLength;

    private String relationalTable;

    private String fkKeyField;

    private String fkNameField;

    private String configType;

    private Integer updateReadOnly;

    private Integer orderIndex;

    private String customOptions;

    private String parentKeyField;

    private String controlType;

    private String controlWidth;
    private String controlHeight;
    private String middleTable;
    private String leftId;
    private String rightId;
    private String controlTextAlign;
    private String controlDefault;
    private Integer showPage;
    private Integer logFieldFlag;
    private String defaultOrderType;
    private String validRules;
    private String validTriggers;
    private String validMessage;
    private String tableJoinType;
    private String fieldQueryType;
    private String controlValueFormat;  //控件值格式化类型
    private Integer isRangeQuery;  //查询条件是否按范围查询
    private String listFixed;  //列表滚动条固定规则字段
    private String otherFieldName;  //别名
    private Integer customOrderBy;  //用户排序字段
    private String filterFieldFlag;  //添加、修改、详情时，对同一表操作需要排除字段的标记

    public String getQueryFieldSql() {
        return queryFieldSql;
    }

    public void setQueryFieldSql(String queryFieldSql) {
        this.queryFieldSql = queryFieldSql;
    }

    private String queryFieldSql;   //自定义sql

    public String getFilterFieldFlag() {
        return filterFieldFlag;
    }

    public void setFilterFieldFlag(String filterFieldFlag) {
        this.filterFieldFlag = filterFieldFlag;
    }
    public Integer getCustomOrderBy() {
        return customOrderBy;
    }

    public void setCustomOrderBy(Integer customOrderBy) {
        this.customOrderBy = customOrderBy;
    }
    public String getControlValueFormat() {
        return controlValueFormat;
    }

    public void setControlValueFormat(String controlValueFormat) {
        this.controlValueFormat = controlValueFormat;
    }

    public Integer getIsRangeQuery() {
        return isRangeQuery;
    }

    public void setIsRangeQuery(Integer isRangeQuery) {
        this.isRangeQuery = isRangeQuery;
    }

    public String getDefaultOrderType() {
        return defaultOrderType;
    }

    public void setDefaultOrderType(String defaultOrderType) {
        this.defaultOrderType = defaultOrderType;
    }

    public String getValidRules() {
        return validRules;
    }

    public void setValidRules(String validRules) {
        this.validRules = validRules;
    }

    public String getValidTriggers() {
        return validTriggers;
    }

    public void setValidTriggers(String validTriggers) {
        this.validTriggers = validTriggers;
    }

    public String getValidMessage() {
        return validMessage;
    }

    public void setValidMessage(String validMessage) {
        this.validMessage = validMessage;
    }

    public Integer getLogFieldFlag() {
        return logFieldFlag;
    }

    public void setLogFieldFlag(Integer logFieldFlag) {
        this.logFieldFlag = logFieldFlag;
    }
    public String getFieldQueryType() {
        return fieldQueryType;
    }

    public void setFieldQueryType(String fieldQueryType) {
        this.fieldQueryType = fieldQueryType;
    }
    public Integer getShowPage() {
        return showPage;
    }

    public String getOtherFieldName() {
        return otherFieldName;
    }

    public void setOtherFieldName(String otherFieldName) {
        this.otherFieldName = otherFieldName;
    }

    public void setShowPage(Integer showPage) {
        this.showPage = showPage;
    }

    public String getControlDefault() {
        return controlDefault;
    }

    public void setControlDefault(String controlDefault) {
        this.controlDefault = controlDefault;
    }

    public String getMiddleTable() {
        return middleTable;
    }

    public void setMiddleTable(String middleTable) {
        this.middleTable = middleTable;
    }

    public String getLeftId() {
        return leftId;
    }

    public void setLeftId(String leftId) {
        this.leftId = leftId;
    }

    public String getRightId() {
        return rightId;
    }

    public void setRightId(String rightId) {
        this.rightId = rightId;
    }

    public String getPkFieldConfigId() {
        return pkFieldConfigId;
    }

    public void setPkFieldConfigId(String pkFieldConfigId) {
        this.pkFieldConfigId = pkFieldConfigId == null ? null : pkFieldConfigId.trim();
    }

    public String getFkTableConfigId() {
        return fkTableConfigId;
    }

    public void setFkTableConfigId(String fkTableConfigId) {
        this.fkTableConfigId = fkTableConfigId == null ? null : fkTableConfigId.trim();
    }

    public String getTableJoinType() {
        return tableJoinType;
    }

    public void setTableJoinType(String tableJoinType) {
        this.tableJoinType = tableJoinType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName == null ? null : fieldName.trim();
    }

    public String getFieldComments() {
        return fieldComments;
    }

    public void setFieldComments(String fieldComments) {
        this.fieldComments = fieldComments == null ? null : fieldComments.trim();
    }

    public String getFieldDataType() {
        return fieldDataType;
    }

    public void setFieldDataType(String fieldDataType) {
        this.fieldDataType = fieldDataType == null ? null : fieldDataType.trim();
    }

    public Integer getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(Integer fieldLength) {
        this.fieldLength = fieldLength;
    }

    public String getRelationalTable() {
        return relationalTable;
    }

    public void setRelationalTable(String relationalTable) {
        this.relationalTable = relationalTable == null ? null : relationalTable.trim();
    }

    public String getFkKeyField() {
        return fkKeyField;
    }

    public void setFkKeyField(String fkKeyField) {
        this.fkKeyField = fkKeyField == null ? null : fkKeyField.trim();
    }

    public String getFkNameField() {
        return fkNameField;
    }

    public void setFkNameField(String fkNameField) {
        this.fkNameField = fkNameField == null ? null : fkNameField.trim();
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType == null ? null : configType.trim();
    }

    public Integer getUpdateReadOnly() {
        return updateReadOnly;
    }

    public void setUpdateReadOnly(Integer updateReadOnly) {
        this.updateReadOnly = updateReadOnly;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getCustomOptions() {
        return customOptions;
    }

    public void setCustomOptions(String customOptions) {
        this.customOptions = customOptions == null ? null : customOptions.trim();
    }

    public String getParentKeyField() {
        return parentKeyField;
    }

    public void setParentKeyField(String parentKeyField) {
        this.parentKeyField = parentKeyField == null ? null : parentKeyField.trim();
    }

    public String getControlType() {
        return controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType == null ? null : controlType.trim();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getControlWidth() {
        return controlWidth;
    }

    public void setControlWidth(String controlWidth) {
        this.controlWidth = controlWidth;
    }

    public String getControlHeight() {
        return controlHeight;
    }

    public void setControlHeight(String controlHeight) {
        this.controlHeight = controlHeight;
    }

    public String getControlTextAlign() {
        return controlTextAlign;
    }

    public void setControlTextAlign(String controlTextAlign) {
        this.controlTextAlign = controlTextAlign;
    }


    public String getListFixed() {
        return listFixed;
    }

    public void setListFixed(String listFixed) {
        this.listFixed = listFixed;
    }
}
package com.tjpu.auth.model.codeTable;

import java.io.Serializable;

public class CommonSelectTableConfigVO implements Serializable {
    private static final long serialVersionUID = 1498120920709557243L;

    private String pkTableConfigId;

    private String tableName;

    private String tableComments;

    private String keyFieldName;

    private Integer keyFieldIsNumber;

    private Integer orderIndex;

    public String getPkTableConfigId() {
        return pkTableConfigId;
    }

    public void setPkTableConfigId(String pkTableConfigId) {
        this.pkTableConfigId = pkTableConfigId == null ? null : pkTableConfigId.trim();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName == null ? null : tableName.trim();
    }

    public String getTableComments() {
        return tableComments;
    }

    public void setTableComments(String tableComments) {
        this.tableComments = tableComments == null ? null : tableComments.trim();
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public void setKeyFieldName(String keyFieldName) {
        this.keyFieldName = keyFieldName == null ? null : keyFieldName.trim();
    }

    public Integer getKeyFieldIsNumber() {
        return keyFieldIsNumber;
    }

    public void setKeyFieldIsNumber(Integer keyFieldIsNumber) {
        this.keyFieldIsNumber = keyFieldIsNumber;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
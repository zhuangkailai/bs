package com.tjpu.sp.model.common.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author: chengzq
 * @date: 2019/5/17 0017 13:26
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Component
@Document(collection = "ExceptionData")
public class ExceptionDataVO {

    @Id
    private String id;
    @Field("PollutantCode")
    private String pollutantCode;
    @Field("ExceptionTime")
    private Date exceptionTime;
    @Field("DataType")
    private String dataType;
    @Field("ExceptionType")
    private String exceptionType;
    @Field("MonitorValue")
    private String monitorValue;
    @Field("DataGatherCode")
    private String dataGatherCode;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPollutantCode() {
        return pollutantCode;
    }

    public void setPollutantCode(String pollutantCode) {
        this.pollutantCode = pollutantCode;
    }

    public Date getExceptionTime() {
        return exceptionTime;
    }

    public void setExceptionTime(Date exceptionTime) {
        this.exceptionTime = exceptionTime;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getMonitorValue() {
        return monitorValue;
    }

    public void setMonitorValue(String monitorValue) {
        this.monitorValue = monitorValue;
    }

    public String getDataGatherCode() {
        return dataGatherCode;
    }

    public void setDataGatherCode(String dataGatherCode) {
        this.dataGatherCode = dataGatherCode;
    }
}

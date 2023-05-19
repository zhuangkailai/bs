package com.tjpu.sp.model.common.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlowDataVO {

    @Id
    private String id;
    @Field("DayFlowDataList")
    private List dayFlowDataList;
    @Field("MonthFlowDataList")
    private List monthFlowDataList;
    @Field("YearFlowDataList")
    private List yearFlowDataList;
    @Field("HourFlowDataList")
    private List hourFlowDataList;
    @Field("FlowUnit")
    private String flowUnit;
    @Field("TotalFlow")
    private String TotalFlow;
    @Field("MonitorTime")
    private String monitorTime;
    @Field("DataGatherCode")
    private String dataGatherCode;

    public List getHourFlowDataList() {
        return hourFlowDataList;
    }

    public void setHourFlowDataList(List hourFlowDataList) {
        this.hourFlowDataList = hourFlowDataList;
    }

    public List getMonthFlowDataList() {
        return monthFlowDataList;
    }

    public void setMonthFlowDataList(List monthFlowDataList) {
        this.monthFlowDataList = monthFlowDataList;
    }

    public List getYearFlowDataList() {
        return yearFlowDataList;
    }

    public void setYearFlowDataList(List yearFlowDataList) {
        this.yearFlowDataList = yearFlowDataList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List getDayFlowDataList() {
        return dayFlowDataList;
    }

    public void setDayFlowDataList(List dayFlowDataList) {
        this.dayFlowDataList = dayFlowDataList;
    }

    public String getFlowUnit() {
        return flowUnit;
    }

    public void setFlowUnit(String flowUnit) {
        this.flowUnit = flowUnit;
    }

    public String getTotalFlow() {
        return TotalFlow;
    }

    public void setTotalFlow(String totalFlow) {
        TotalFlow = totalFlow;
    }

    public String getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(String monitorTime) {
        this.monitorTime = monitorTime;
    }

    public String getDataGatherCode() {
        return dataGatherCode;
    }

    public void setDataGatherCode(String dataGatherCode) {
        this.dataGatherCode = dataGatherCode;
    }
}

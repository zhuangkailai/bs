package com.tjpu.sp.model.common.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
@Document(collection = "GroundWaterData")
public class GroundWaterDataVO {

    @Id
    private String id;
    @Field("DataGatherCode")
    private String dataGatherCode;

    @Field("WaterQualityClass")
    private String waterqualityclass;

    @Field("MonitorTime")
    private Date monitorTime;

    @Field("DataList")
    private List<Map<String,Object>> dataList;

    public String getWaterqualityclass() {
        return waterqualityclass;
    }

    public void setWaterqualityclass(String waterqualityclass) {
        this.waterqualityclass = waterqualityclass;
    }

    public List<Map<String, Object>> getDataList() {
        return dataList;
    }

    public void setDataList(List<Map<String, Object>> dataList) {
        this.dataList = dataList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataGatherCode() {
        return dataGatherCode;
    }

    public void setDataGatherCode(String dataGatherCode) {
        this.dataGatherCode = dataGatherCode;
    }

    public Date getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(Date monitorTime) {
        this.monitorTime = monitorTime;
    }
}

package com.tjpu.sp.model.common.mongodb;

import java.util.Date;
import java.util.Set;

/**
 * @author: zhangzc
 * @date: 2019/8/15 14:39
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
public class OnlineAlarmCountQueryVO {
    private String userId;
    private Date startTime;
    private Date endTime;
    private Set<String> mns;
    private Set<String> pollutantCodes;
    private String pollutantCodeFieldName;
    private String collection;
    private String timeFieldName;
    private String unwindFieldName;
    private int monitorPointType;
    private String pollutionID;

    private String monitorpointid;

    private String exceptionType;

    public String getMonitorpointid() {
        return monitorpointid;
    }

    public void setMonitorpointid(String monitorpointid) {
        this.monitorpointid = monitorpointid;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public int getMonitorPointType() {
        return monitorPointType;
    }

    public void setMonitorPointType(int monitorPointType) {
        this.monitorPointType = monitorPointType;
    }

    public String getPollutionID() {
        return pollutionID;
    }

    public void setPollutionID(String pollutionID) {
        this.pollutionID = pollutionID;
    }

    public String getPollutantCodeFieldName() {
        return pollutantCodeFieldName;
    }

    public void setPollutantCodeFieldName(String pollutantCodeFieldName) {
        this.pollutantCodeFieldName = pollutantCodeFieldName;
    }

    public String getUnwindFieldName() {
        return unwindFieldName;
    }

    public void setUnwindFieldName(String unwindFieldName) {
        this.unwindFieldName = unwindFieldName;
    }

    public String getTimeFieldName() {
        return timeFieldName;
    }

    public void setTimeFieldName(String timeFieldName) {
        this.timeFieldName = timeFieldName;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public Set<String> getPollutantCodes() {
        return pollutantCodes;
    }

    public void setPollutantCodes(Set<String> pollutantCodes) {
        this.pollutantCodes = pollutantCodes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Set<String> getMns() {
        return mns;
    }

    public void setMns(Set<String> mns) {
        this.mns = mns;
    }
}

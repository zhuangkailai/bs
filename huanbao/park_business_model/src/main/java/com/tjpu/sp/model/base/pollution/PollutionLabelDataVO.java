package com.tjpu.sp.model.base.pollution;

public class PollutionLabelDataVO {
    private String pkId;

    private Integer fkPollutionlabelid;

    private String fkPollutionid;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public Integer getFkPollutionlabelid() {
        return fkPollutionlabelid;
    }

    public void setFkPollutionlabelid(Integer fkPollutionlabelid) {
        this.fkPollutionlabelid = fkPollutionlabelid;
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid;
    }
}
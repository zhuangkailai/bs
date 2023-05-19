package com.tjpu.sp.model.environmentalprotection.monitorpoint;

public class AlarmLevelDataVO {
    private String pkId;
    private String levelname;
    private Integer levelcode;
    private Double standardminvalue;
    private Double standardmaxvalue;

    public String getLevelname() {
        return levelname;
    }

    public void setLevelname(String levelname) {
        this.levelname = levelname;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public Integer getLevelcode() {
        return levelcode;
    }

    public void setLevelcode(Integer levelcode) {
        this.levelcode = levelcode;
    }

    public Double getStandardminvalue() {
        return standardminvalue;
    }

    public void setStandardminvalue(Double standardminvalue) {
        this.standardminvalue = standardminvalue;
    }

    public Double getStandardmaxvalue() {
        return standardmaxvalue;
    }

    public void setStandardmaxvalue(Double standardmaxvalue) {
        this.standardmaxvalue = standardmaxvalue;
    }
}

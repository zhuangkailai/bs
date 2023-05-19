package com.tjpu.sp.model.environmentalprotection.watercorrelation;

public class WaterCorrelationPollutantSetVO {
    private String pkId;

    private String fkWatercorrelationid;

    private String fkPollutantcode;

    private String value;

    private Double a;

    private Double b;

    private Double r;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkWatercorrelationid() {
        return fkWatercorrelationid;
    }

    public void setFkWatercorrelationid(String fkWatercorrelationid) {
        this.fkWatercorrelationid = fkWatercorrelationid == null ? null : fkWatercorrelationid.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Double getA() {
        return a;
    }

    public void setA(Double a) {
        this.a = a;
    }

    public Double getB() {
        return b;
    }

    public void setB(Double b) {
        this.b = b;
    }

    public Double getR() {
        return r;
    }

    public void setR(Double r) {
        this.r = r;
    }

}
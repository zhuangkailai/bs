package com.tjpu.sp.model.envhousekeepers.fuelconsumption;

import java.util.Date;

public class FuelConsumptionRecordVO {
    private String pkId;

    private String fkPollutionid;

    private String fuelname;

    private String consumption;

    private Double calorificvalue;

    private String unit;

    private Double coalsulfurcontent;

    private Double ashcontent;

    private Double volatilizationcontent;

    private String othercoal;

    private Double fuelsulfurcontent;

    private String otherfuel;

    private Double hydrogensulfide;

    private String othergas;

    private String otherfuels;

    private String recorduser;

    private Date recordtime;

    private String revieweruser;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getFuelname() {
        return fuelname;
    }

    public void setFuelname(String fuelname) {
        this.fuelname = fuelname == null ? null : fuelname.trim();
    }

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption == null ? null : consumption.trim();
    }

    public Double getCalorificvalue() {
        return calorificvalue;
    }

    public void setCalorificvalue(Double calorificvalue) {
        this.calorificvalue = calorificvalue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit == null ? null : unit.trim();
    }

    public Double getCoalsulfurcontent() {
        return coalsulfurcontent;
    }

    public void setCoalsulfurcontent(Double coalsulfurcontent) {
        this.coalsulfurcontent = coalsulfurcontent;
    }

    public Double getAshcontent() {
        return ashcontent;
    }

    public void setAshcontent(Double ashcontent) {
        this.ashcontent = ashcontent;
    }

    public Double getVolatilizationcontent() {
        return volatilizationcontent;
    }

    public void setVolatilizationcontent(Double volatilizationcontent) {
        this.volatilizationcontent = volatilizationcontent;
    }

    public String getOthercoal() {
        return othercoal;
    }

    public void setOthercoal(String othercoal) {
        this.othercoal = othercoal == null ? null : othercoal.trim();
    }

    public Double getFuelsulfurcontent() {
        return fuelsulfurcontent;
    }

    public void setFuelsulfurcontent(Double fuelsulfurcontent) {
        this.fuelsulfurcontent = fuelsulfurcontent;
    }

    public String getOtherfuel() {
        return otherfuel;
    }

    public void setOtherfuel(String otherfuel) {
        this.otherfuel = otherfuel == null ? null : otherfuel.trim();
    }

    public Double getHydrogensulfide() {
        return hydrogensulfide;
    }

    public void setHydrogensulfide(Double hydrogensulfide) {
        this.hydrogensulfide = hydrogensulfide;
    }

    public String getOthergas() {
        return othergas;
    }

    public void setOthergas(String othergas) {
        this.othergas = othergas == null ? null : othergas.trim();
    }


    public String getRecorduser() {
        return recorduser;
    }

    public void setRecorduser(String recorduser) {
        this.recorduser = recorduser == null ? null : recorduser.trim();
    }

    public Date getRecordtime() {
        return recordtime;
    }

    public void setRecordtime(Date recordtime) {
        this.recordtime = recordtime;
    }

    public String getRevieweruser() {
        return revieweruser;
    }

    public void setRevieweruser(String revieweruser) {
        this.revieweruser = revieweruser == null ? null : revieweruser.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public String getOtherfuels() {
        return otherfuels;
    }

    public void setOtherfuels(String otherfuels) {
        this.otherfuels = otherfuels;
    }
}
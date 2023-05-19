package com.tjpu.sp.model.environmentalprotection.productionmaterials;

import java.util.Date;

public class FuelInfoVO {
    private String pkFuelinfoid;

    private String fkPollutionid;

    private String fuelname;

    private Double ashcontent;

    private Double volatilizationcontent;

    private Double sulfur;

    private Double sulfurpercent;

    private Double sulfurcontent;

    private Double calorific;

    private Double calorificvalue;

    private Double annualmaximumuse;

    private Double mercury;

    private String yearmaxunit;

    private String remark;

    private String updateuser;

    private Date updatetime;

    public String getPkFuelinfoid() {
        return pkFuelinfoid;
    }

    public void setPkFuelinfoid(String pkFuelinfoid) {
        this.pkFuelinfoid = pkFuelinfoid == null ? null : pkFuelinfoid.trim();
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

    public Double getSulfur() {
        return sulfur;
    }

    public void setSulfur(Double sulfur) {
        this.sulfur = sulfur;
    }

    public Double getSulfurpercent() {
        return sulfurpercent;
    }

    public void setSulfurpercent(Double sulfurpercent) {
        this.sulfurpercent = sulfurpercent;
    }

    public Double getSulfurcontent() {
        return sulfurcontent;
    }

    public void setSulfurcontent(Double sulfurcontent) {
        this.sulfurcontent = sulfurcontent;
    }

    public Double getCalorific() {
        return calorific;
    }

    public void setCalorific(Double calorific) {
        this.calorific = calorific;
    }

    public Double getCalorificvalue() {
        return calorificvalue;
    }

    public void setCalorificvalue(Double calorificvalue) {
        this.calorificvalue = calorificvalue;
    }

    public Double getAnnualmaximumuse() {
        return annualmaximumuse;
    }

    public void setAnnualmaximumuse(Double annualmaximumuse) {
        this.annualmaximumuse = annualmaximumuse;
    }

    public Double getMercury() {
        return mercury;
    }

    public void setMercury(Double mercury) {
        this.mercury = mercury;
    }

    public String getYearmaxunit() {
        return yearmaxunit;
    }

    public void setYearmaxunit(String yearmaxunit) {
        this.yearmaxunit = yearmaxunit == null ? null : yearmaxunit.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}
package com.tjpu.sp.model.environmentalprotection.navigation;

import java.util.Date;

public class NavigationStandardVO {
    private String pkId;

    private Integer standardlevel;

    private Double standardminvalue;

    private Double standardmaxvalue;

    private String colourvalue;

    private Date updatetime;

    private String updateuser;
    private String fkpollutantcode;
    private String fkmonitorpointtypecode;

    private Short pollutantcategory;

    public String getFkpollutantcode() {
        return fkpollutantcode;
    }

    public void setFkpollutantcode(String fkpollutantcode) {
        this.fkpollutantcode = fkpollutantcode;
    }

    public String getFkmonitorpointtypecode() {
        return fkmonitorpointtypecode;
    }

    public void setFkmonitorpointtypecode(String fkmonitorpointtypecode) {
        this.fkmonitorpointtypecode = fkmonitorpointtypecode;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public Integer getStandardlevel() {
        return standardlevel;
    }

    public void setStandardlevel(Integer standardlevel) {
        this.standardlevel = standardlevel;
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

    public String getColourvalue() {
        return colourvalue;
    }

    public void setColourvalue(String colourvalue) {
        this.colourvalue = colourvalue == null ? null : colourvalue.trim();
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

    public Short getPollutantcategory() {
        return pollutantcategory;
    }

    public void setPollutantcategory(Short pollutantcategory) {
        this.pollutantcategory = pollutantcategory;
    }
}
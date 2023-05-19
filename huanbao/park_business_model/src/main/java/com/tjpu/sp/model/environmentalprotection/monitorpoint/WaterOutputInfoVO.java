package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import java.util.Date;

public class WaterOutputInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String outputcode;

    private String outputname;

    private Double outputlongitude;

    private Double outputlatitude;

    private String outputposition;

    private String intervaldischargettime;

    private String fkOutputrule;

    private String fkDraindirection;

    private String fkWaterqualityclass;

    private String fkBasin;

    private String fkSewageplant;

    private String fkOutputproperty;

    private Integer outputtype;

    private Integer isonlinedevice;

    private Double intotlongitude;

    private Double intolatitude;

    private Integer inputoroutput;

    private String outputyear;

    private String dgimn;

    private Integer status;

    private String remark;

    private String belongdatasource;

    private Date updatetime;

    private String updateuser;

    private String fkImgid;

    public String getFkImgid() {
        return fkImgid;
    }

    public void setFkImgid(String fkImgid) {
        this.fkImgid = fkImgid;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid;
    }

    public String getOutputcode() {
        return outputcode;
    }

    public void setOutputcode(String outputcode) {
        this.outputcode = outputcode;
    }

    public String getOutputname() {
        return outputname;
    }

    public void setOutputname(String outputname) {
        this.outputname = outputname;
    }

    public Double getOutputlongitude() {
        return outputlongitude;
    }

    public void setOutputlongitude(Double outputlongitude) {
        this.outputlongitude = outputlongitude;
    }

    public Double getOutputlatitude() {
        return outputlatitude;
    }

    public void setOutputlatitude(Double outputlatitude) {
        this.outputlatitude = outputlatitude;
    }

    public String getOutputposition() {
        return outputposition;
    }

    public void setOutputposition(String outputposition) {
        this.outputposition = outputposition;
    }

    public String getIntervaldischargettime() {
        return intervaldischargettime;
    }

    public void setIntervaldischargettime(String intervaldischargettime) {
        this.intervaldischargettime = intervaldischargettime;
    }

    public String getFkOutputrule() {
        return fkOutputrule;
    }

    public void setFkOutputrule(String fkOutputrule) {
        this.fkOutputrule = fkOutputrule;
    }

    public String getFkDraindirection() {
        return fkDraindirection;
    }

    public void setFkDraindirection(String fkDraindirection) {
        this.fkDraindirection = fkDraindirection;
    }

    public String getFkWaterqualityclass() {
        return fkWaterqualityclass;
    }

    public void setFkWaterqualityclass(String fkWaterqualityclass) {
        this.fkWaterqualityclass = fkWaterqualityclass;
    }

    public String getFkBasin() {
        return fkBasin;
    }

    public void setFkBasin(String fkBasin) {
        this.fkBasin = fkBasin;
    }

    public String getFkSewageplant() {
        return fkSewageplant;
    }

    public void setFkSewageplant(String fkSewageplant) {
        this.fkSewageplant = fkSewageplant;
    }

    public String getFkOutputproperty() {
        return fkOutputproperty;
    }

    public void setFkOutputproperty(String fkOutputproperty) {
        this.fkOutputproperty = fkOutputproperty;
    }

    public Integer getOutputtype() {
        return outputtype;
    }

    public void setOutputtype(Integer outputtype) {
        this.outputtype = outputtype;
    }

    public Integer getIsonlinedevice() {
        return isonlinedevice;
    }

    public void setIsonlinedevice(Integer isonlinedevice) {
        this.isonlinedevice = isonlinedevice;
    }

    public Double getIntotlongitude() {
        return intotlongitude;
    }

    public void setIntotlongitude(Double intotlongitude) {
        this.intotlongitude = intotlongitude;
    }

    public Double getIntolatitude() {
        return intolatitude;
    }

    public void setIntolatitude(Double intolatitude) {
        this.intolatitude = intolatitude;
    }

    public Integer getInputoroutput() {
        return inputoroutput;
    }

    public void setInputoroutput(Integer inputoroutput) {
        this.inputoroutput = inputoroutput;
    }

    public String getOutputyear() {
        return outputyear;
    }

    public void setOutputyear(String outputyear) {
        this.outputyear = outputyear;
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBelongdatasource() {
        return belongdatasource;
    }

    public void setBelongdatasource(String belongdatasource) {
        this.belongdatasource = belongdatasource;
    }

    public Date getUpdatetime() {
        return updatetime==null?new Date():updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }
}
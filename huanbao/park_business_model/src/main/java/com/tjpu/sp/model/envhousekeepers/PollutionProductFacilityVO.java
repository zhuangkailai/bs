package com.tjpu.sp.model.envhousekeepers;

import java.util.Date;
import java.util.List;

public class PollutionProductFacilityVO {
    private String pkId;

    private String fkPollutionid;

    private String facilitynum;

    private String facilityname;

    private String productionlinename;

    private String productionlinenum;

    private String productionunitnum;

    private String productionunitname;

    private String updateuser;

    private Date updatedate;


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

    public String getFacilitynum() {
        return facilitynum;
    }

    public void setFacilitynum(String facilitynum) {
        this.facilitynum = facilitynum == null ? null : facilitynum.trim();
    }

    public String getFacilityname() {
        return facilityname;
    }

    public void setFacilityname(String facilityname) {
        this.facilityname = facilityname == null ? null : facilityname.trim();
    }

    public String getProductionlinename() {
        return productionlinename;
    }

    public void setProductionlinename(String productionlinename) {
        this.productionlinename = productionlinename == null ? null : productionlinename.trim();
    }

    public String getProductionlinenum() {
        return productionlinenum;
    }

    public void setProductionlinenum(String productionlinenum) {
        this.productionlinenum = productionlinenum == null ? null : productionlinenum.trim();
    }

    public String getProductionunitnum() {
        return productionunitnum;
    }

    public void setProductionunitnum(String productionunitnum) {
        this.productionunitnum = productionunitnum == null ? null : productionunitnum.trim();
    }

    public String getProductionunitname() {
        return productionunitname;
    }

    public void setProductionunitname(String productionunitname) {
        this.productionunitname = productionunitname == null ? null : productionunitname.trim();
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public Date getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(Date updatedate) {
        this.updatedate = updatedate;
    }
}
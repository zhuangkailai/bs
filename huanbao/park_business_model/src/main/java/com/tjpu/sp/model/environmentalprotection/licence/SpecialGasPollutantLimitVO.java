package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class SpecialGasPollutantLimitVO {
    private String pkId;

    private String fkLicenceid;

    private String situationtype;

    private String outlettype;

    private String fkPollutantcode;

    private String permittimeinterval;

    private Double permitlimit;

    private Double permitdaylimit;

    private Double permitmonthlimit;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkLicenceid() {
        return fkLicenceid;
    }

    public void setFkLicenceid(String fkLicenceid) {
        this.fkLicenceid = fkLicenceid == null ? null : fkLicenceid.trim();
    }

    public String getSituationtype() {
        return situationtype;
    }

    public void setSituationtype(String situationtype) {
        this.situationtype = situationtype == null ? null : situationtype.trim();
    }

    public String getOutlettype() {
        return outlettype;
    }

    public void setOutlettype(String outlettype) {
        this.outlettype = outlettype == null ? null : outlettype.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public String getPermittimeinterval() {
        return permittimeinterval;
    }

    public void setPermittimeinterval(String permittimeinterval) {
        this.permittimeinterval = permittimeinterval == null ? null : permittimeinterval.trim();
    }

    public Double getPermitlimit() {
        return permitlimit;
    }

    public void setPermitlimit(Double permitlimit) {
        this.permitlimit = permitlimit;
    }

    public Double getPermitdaylimit() {
        return permitdaylimit;
    }

    public void setPermitdaylimit(Double permitdaylimit) {
        this.permitdaylimit = permitdaylimit;
    }

    public Double getPermitmonthlimit() {
        return permitmonthlimit;
    }

    public void setPermitmonthlimit(Double permitmonthlimit) {
        this.permitmonthlimit = permitmonthlimit;
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
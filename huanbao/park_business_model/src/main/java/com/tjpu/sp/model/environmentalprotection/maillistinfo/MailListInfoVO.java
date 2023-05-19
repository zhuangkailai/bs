package com.tjpu.sp.model.environmentalprotection.maillistinfo;

import java.util.Date;

public class MailListInfoVO {
    private String pkId;

    private String peoplename;

    private String contactunit;

    private String telephone;

    private Date updatetime;

    private String updateuser;

    private String positions;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getPeoplename() {
        return peoplename;
    }

    public void setPeoplename(String peoplename) {
        this.peoplename = peoplename == null ? null : peoplename.trim();
    }

    public String getContactunit() {
        return contactunit;
    }

    public void setContactunit(String contactunit) {
        this.contactunit = contactunit == null ? null : contactunit.trim();
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone == null ? null : telephone.trim();
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

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = positions == null ? null : positions.trim();
    }
}
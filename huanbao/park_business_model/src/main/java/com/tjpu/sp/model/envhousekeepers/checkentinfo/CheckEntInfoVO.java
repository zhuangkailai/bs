package com.tjpu.sp.model.envhousekeepers.checkentinfo;

import java.util.Date;

public class CheckEntInfoVO {
    private String pkId;

    private String fkChecktypecode;

    private String fkPollutionid;

    private String linkman;

    private String telephone;

    private String checkpeople;

    private Date checktime;

    private String updateuser;

    private Date updatetime;

    private String entaddress;

    private Integer status;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkChecktypecode() {
        return fkChecktypecode;
    }

    public void setFkChecktypecode(String fkChecktypecode) {
        this.fkChecktypecode = fkChecktypecode == null ? null : fkChecktypecode.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getLinkman() {
        return linkman;
    }

    public void setLinkman(String linkman) {
        this.linkman = linkman == null ? null : linkman.trim();
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone == null ? null : telephone.trim();
    }

    public String getCheckpeople() {
        return checkpeople;
    }

    public void setCheckpeople(String checkpeople) {
        this.checkpeople = checkpeople == null ? null : checkpeople.trim();
    }

    public Date getChecktime() {
        return checktime;
    }

    public void setChecktime(Date checktime) {
        this.checktime = checktime;
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

    public String getEntaddress() {
        return entaddress;
    }

    public void setEntaddress(String entaddress) {
        this.entaddress = entaddress;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
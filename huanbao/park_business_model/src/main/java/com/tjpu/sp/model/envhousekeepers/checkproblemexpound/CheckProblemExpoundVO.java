package com.tjpu.sp.model.envhousekeepers.checkproblemexpound;

import java.util.Date;

public class CheckProblemExpoundVO {
    private String pkId;

    private String fkCheckitemdataid;

    private String remark;

    private String fkFileid;

    private String updateuser;

    private String enteredby;

    private Date updatetime;

    private Integer orderindex;

    private Short status;

    private String fkproblemsourcecode;
    private String fkpollutionid;
    private Date checktime;
    private String fkcheckcategorydataid;
    private String checkcontent;
    private Date rectifynoticetime;
    private Date rectificationtermtime;


    private String fkRefileid;
    private String rectifycontent;
    private Date managementtime;

    public Date getManagementtime() {
        return managementtime;
    }

    public void setManagementtime(Date managementtime) {
        this.managementtime = managementtime;
    }

    public String getEnteredby() {
        return enteredby;
    }

    public void setEnteredby(String enteredby) {
        this.enteredby = enteredby;
    }

    public String getFkRefileid() {
        return fkRefileid;
    }

    public void setFkRefileid(String fkRefileid) {
        this.fkRefileid = fkRefileid;
    }

    public String getRectifycontent() {
        return rectifycontent;
    }

    public void setRectifycontent(String rectifycontent) {
        this.rectifycontent = rectifycontent;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkCheckitemdataid() {
        return fkCheckitemdataid;
    }

    public void setFkCheckitemdataid(String fkCheckitemdataid) {
        this.fkCheckitemdataid = fkCheckitemdataid == null ? null : fkCheckitemdataid.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
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

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getFkproblemsourcecode() {
        return fkproblemsourcecode;
    }

    public void setFkproblemsourcecode(String fkproblemsourcecode) {
        this.fkproblemsourcecode = fkproblemsourcecode;
    }

    public String getFkpollutionid() {
        return fkpollutionid;
    }

    public void setFkpollutionid(String fkpollutionid) {
        this.fkpollutionid = fkpollutionid;
    }

    public Date getChecktime() {
        return checktime;
    }

    public void setChecktime(Date checktime) {
        this.checktime = checktime;
    }

    public String getFkcheckcategorydataid() {
        return fkcheckcategorydataid;
    }

    public void setFkcheckcategorydataid(String fkcheckcategorydataid) {
        this.fkcheckcategorydataid = fkcheckcategorydataid;
    }

    public String getCheckcontent() {
        return checkcontent;
    }

    public void setCheckcontent(String checkcontent) {
        this.checkcontent = checkcontent;
    }

    public Date getRectifynoticetime() {
        return rectifynoticetime;
    }

    public void setRectifynoticetime(Date rectifynoticetime) {
        this.rectifynoticetime = rectifynoticetime;
    }

    public Date getRectificationtermtime() {
        return rectificationtermtime;
    }

    public void setRectificationtermtime(Date rectificationtermtime) {
        this.rectificationtermtime = rectificationtermtime;
    }
}
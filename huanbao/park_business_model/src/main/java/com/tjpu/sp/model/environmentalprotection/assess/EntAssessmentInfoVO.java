package com.tjpu.sp.model.environmentalprotection.assess;

import java.util.Date;
import java.util.List;

public class EntAssessmentInfoVO {
    private String pkDataid;

    private Date checktime;

    private String checkpeople;

    private String fkPollutionid;

    private Double totalreducescore;

    private String remark;

    private String updateuser;

    private Date updatetime;


    private List<EntAssessmentDataVO> entAssessmentDataVOS;

    public List<EntAssessmentDataVO> getEntAssessmentDataVOS() {
        return entAssessmentDataVOS;
    }

    public void setEntAssessmentDataVOS(List<EntAssessmentDataVO> entAssessmentDataVOS) {
        this.entAssessmentDataVOS = entAssessmentDataVOS;
    }

    public String getPkDataid() {
        return pkDataid;
    }

    public void setPkDataid(String pkDataid) {
        this.pkDataid = pkDataid == null ? null : pkDataid.trim();
    }

    public Date getChecktime() {
        return checktime;
    }

    public void setChecktime(Date checktime) {
        this.checktime = checktime;
    }

    public String getCheckpeople() {
        return checkpeople;
    }

    public void setCheckpeople(String checkpeople) {
        this.checkpeople = checkpeople == null ? null : checkpeople.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public Double getTotalreducescore() {
        return totalreducescore;
    }

    public void setTotalreducescore(Double totalreducescore) {
        this.totalreducescore = totalreducescore;
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
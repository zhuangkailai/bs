package com.tjpu.sp.model.envhousekeepers.checktemplateconfig;

import java.util.Date;

public class CheckTemplateConfigVO {
    private String pkId;

    private String fkChecktypecode;

    private Integer orderindex;

    private String checkcategory;

    private String checkcontent;

    private String checksituation;

    private String basisitem;

    private String remark;

    private String updateuser;

    private Date updatetime;

    private String checksituationtype;

    private String fkfileid;

    private String textcontent;
    private String explaincommon;
    private String questionscommon;

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

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }

    public String getCheckcategory() {
        return checkcategory;
    }

    public void setCheckcategory(String checkcategory) {
        this.checkcategory = checkcategory == null ? null : checkcategory.trim();
    }

    public String getCheckcontent() {
        return checkcontent;
    }

    public void setCheckcontent(String checkcontent) {
        this.checkcontent = checkcontent == null ? null : checkcontent.trim();
    }

    public String getChecksituation() {
        return checksituation;
    }

    public void setChecksituation(String checksituation) {
        this.checksituation = checksituation == null ? null : checksituation.trim();
    }

    public String getBasisitem() {
        return basisitem;
    }

    public void setBasisitem(String basisitem) {
        this.basisitem = basisitem == null ? null : basisitem.trim();
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

    public String getChecksituationtype() {
        return checksituationtype;
    }

    public void setChecksituationtype(String checksituationtype) {
        this.checksituationtype = checksituationtype;
    }

    public String getFkfileid() {
        return fkfileid;
    }

    public void setFkfileid(String fkfileid) {
        this.fkfileid = fkfileid;
    }

    public String getTextcontent() {
        return textcontent;
    }

    public void setTextcontent(String textcontent) {
        this.textcontent = textcontent;
    }

    public String getExplaincommon() {
        return explaincommon;
    }

    public void setExplaincommon(String explaincommon) {
        this.explaincommon = explaincommon;
    }

    public String getQuestionscommon() {
        return questionscommon;
    }

    public void setQuestionscommon(String questionscommon) {
        this.questionscommon = questionscommon;
    }
}
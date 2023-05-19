package com.pub.model;

import java.util.Date;

public class ButtonVO {
    private String buttonId;

    private String buttonName;

    private String buttonTitle;

    private String buttonImg;

    private String buttonCode;

    private Integer sortcode;

    private String buttonType;

    private String buttonRemak;

    private Integer deletemark;

    private Date createdate;

    private String createuserid;

    private String createusername;

    private Date modifydate;

    private String modifyuserid;

    private String modifyusername;

    public String getButtonStyle() {
        return buttonStyle;
    }

    public void setButtonStyle(String buttonStyle) {
        this.buttonStyle = buttonStyle;
    }

    private String buttonStyle;

    public String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId == null ? null : buttonId.trim();
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName == null ? null : buttonName.trim();
    }

    public String getButtonTitle() {
        return buttonTitle;
    }

    public void setButtonTitle(String buttonTitle) {
        this.buttonTitle = buttonTitle == null ? null : buttonTitle.trim();
    }

    public String getButtonImg() {
        return buttonImg;
    }

    public void setButtonImg(String buttonImg) {
        this.buttonImg = buttonImg == null ? null : buttonImg.trim();
    }

    public String getButtonCode() {
        return buttonCode;
    }

    public void setButtonCode(String buttonCode) {
        this.buttonCode = buttonCode == null ? null : buttonCode.trim();
    }

    public Integer getSortcode() {
        return sortcode;
    }

    public void setSortcode(Integer sortcode) {
        this.sortcode = sortcode;
    }

    public String getButtonType() {
        return buttonType;
    }

    public void setButtonType(String buttonType) {
        this.buttonType = buttonType == null ? null : buttonType.trim();
    }

    public String getButtonRemak() {
        return buttonRemak;
    }

    public void setButtonRemak(String buttonRemak) {
        this.buttonRemak = buttonRemak == null ? null : buttonRemak.trim();
    }

    public Integer getDeletemark() {
        return deletemark;
    }

    public void setDeletemark(Integer deletemark) {
        this.deletemark = deletemark;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }

    public String getCreateuserid() {
        return createuserid;
    }

    public void setCreateuserid(String createuserid) {
        this.createuserid = createuserid == null ? null : createuserid.trim();
    }

    public String getCreateusername() {
        return createusername;
    }

    public void setCreateusername(String createusername) {
        this.createusername = createusername == null ? null : createusername.trim();
    }

    public Date getModifydate() {
        return modifydate;
    }

    public void setModifydate(Date modifydate) {
        this.modifydate = modifydate;
    }

    public String getModifyuserid() {
        return modifyuserid;
    }

    public void setModifyuserid(String modifyuserid) {
        this.modifyuserid = modifyuserid == null ? null : modifyuserid.trim();
    }

    public String getModifyusername() {
        return modifyusername;
    }

    public void setModifyusername(String modifyusername) {
        this.modifyusername = modifyusername == null ? null : modifyusername.trim();
    }
}
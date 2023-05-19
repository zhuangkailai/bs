package com.tjpu.auth.model.system;

import java.util.Date;
import java.util.List;

public class SysAppVO {
	private String appId;

	private String appName;

	private String appDescription;

	private String appVersion;

	private String appUrl;

	private String appLicensed;

	private String appType;

	private String regsionIp;

	private String regsionUrl;

	private String updateUrl;

	private Integer sequence;

	private String remark;

	private String parentId;

	private Boolean deleteMark;

	private Date createDate;

	private String createUserId;

	private String appImg;

	/**
	 * 一对多关联系统菜单信息表 2018-04-03 lip 添加
	 */
	private List<SysMenuVO> sysMenuVOs;

	public List<SysMenuVO> getSysMenuVOs() {
		return sysMenuVOs;
	}

	public void setSysMenuVOs(List<SysMenuVO> sysMenuVOs) {
		this.sysMenuVOs = sysMenuVOs;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppDescription() {
		return appDescription;
	}

	public void setAppDescription(String appDescription) {
		this.appDescription = appDescription;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public String getAppLicensed() {
		return appLicensed;
	}

	public void setAppLicensed(String appLicensed) {
		this.appLicensed = appLicensed;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getRegsionIp() {
		return regsionIp;
	}

	public void setRegsionIp(String regsionIp) {
		this.regsionIp = regsionIp;
	}

	public String getRegsionUrl() {
		return regsionUrl;
	}

	public void setRegsionUrl(String regsionUrl) {
		this.regsionUrl = regsionUrl;
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Boolean getDeleteMark() {
		return deleteMark;
	}

	public void setDeleteMark(Boolean deleteMark) {
		this.deleteMark = deleteMark;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getAppImg() {
		return appImg;
	}

	public void setAppImg(String appImg) {
		this.appImg = appImg;
	}
}
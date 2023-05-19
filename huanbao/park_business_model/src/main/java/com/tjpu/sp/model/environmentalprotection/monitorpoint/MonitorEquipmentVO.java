package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import java.util.Date;

public class MonitorEquipmentVO {
    private String pkId;

    private String monitorcode;

    private String monitorname;

    private String montiortype;

    private String monitorfrequency;

    private String fkMonitorpointoroutputid;

    private String fkMonitorpointtypecode;

    private String monitorpollutant;

    private Date firstrundate;

    private String manufacturer;

    private String contact;

    private String contactphone;

    private String operatemaintenanceunit;

    private Short status;

    private String operatemaintenancecontact;

    private String operatemaintenancecontactphone;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getMonitorcode() {
        return monitorcode;
    }

    public void setMonitorcode(String monitorcode) {
        this.monitorcode = monitorcode == null ? null : monitorcode.trim();
    }

    public String getMonitorname() {
        return monitorname;
    }

    public void setMonitorname(String monitorname) {
        this.monitorname = monitorname == null ? null : monitorname.trim();
    }

    public String getMontiortype() {
        return montiortype;
    }

    public void setMontiortype(String montiortype) {
        this.montiortype = montiortype == null ? null : montiortype.trim();
    }

    public String getMonitorfrequency() {
        return monitorfrequency;
    }

    public void setMonitorfrequency(String monitorfrequency) {
        this.monitorfrequency = monitorfrequency == null ? null : monitorfrequency.trim();
    }

    public String getFkMonitorpointoroutputid() {
        return fkMonitorpointoroutputid;
    }

    public void setFkMonitorpointoroutputid(String fkMonitorpointoroutputid) {
        this.fkMonitorpointoroutputid = fkMonitorpointoroutputid;
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode;
    }

    public String getMonitorpollutant() {
        return monitorpollutant;
    }

    public void setMonitorpollutant(String monitorpollutant) {
        this.monitorpollutant = monitorpollutant == null ? null : monitorpollutant.trim();
    }

    public Date getFirstrundate() {
        return firstrundate;
    }

    public void setFirstrundate(Date firstrundate) {
        this.firstrundate = firstrundate;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer == null ? null : manufacturer.trim();
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact == null ? null : contact.trim();
    }

    public String getContactphone() {
        return contactphone;
    }

    public void setContactphone(String contactphone) {
        this.contactphone = contactphone == null ? null : contactphone.trim();
    }

    public String getOperatemaintenanceunit() {
        return operatemaintenanceunit;
    }

    public void setOperatemaintenanceunit(String operatemaintenanceunit) {
        this.operatemaintenanceunit = operatemaintenanceunit == null ? null : operatemaintenanceunit.trim();
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getOperatemaintenancecontact() {
        return operatemaintenancecontact;
    }

    public void setOperatemaintenancecontact(String operatemaintenancecontact) {
        this.operatemaintenancecontact = operatemaintenancecontact == null ? null : operatemaintenancecontact.trim();
    }

    public String getOperatemaintenancecontactphone() {
        return operatemaintenancecontactphone;
    }

    public void setOperatemaintenancecontactphone(String operatemaintenancecontactphone) {
        this.operatemaintenancecontactphone = operatemaintenancecontactphone == null ? null : operatemaintenancecontactphone.trim();
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
}
package com.tjpu.sp.model.environmentalprotection.productiondevice;

public class ProductionDeviceVO {
    private String pkId;

    private String parentid;

    private String mainfacilities;

    private String devicename;

    private String specifications;

    private String params;

    private String material;

    private Integer num;

    private String manufacturer;

    private String manufacturercontacts;

    private String phone;

    private String remark;

    private String updateuser;

    private String updatetime;

    private String fkPollutionid;

    private Short devicetype;

    public Short getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(Short devicetype) {
        this.devicetype = devicetype;
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public String getMainfacilities() {
        return mainfacilities;
    }

    public void setMainfacilities(String mainfacilities) {
        this.mainfacilities = mainfacilities;
    }

    public String getDevicename() {
        return devicename;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturercontacts() {
        return manufacturercontacts;
    }

    public void setManufacturercontacts(String manufacturercontacts) {
        this.manufacturercontacts = manufacturercontacts;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }
}
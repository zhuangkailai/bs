package com.tjpu.sp.model.environmentalprotection.productionmaterials;

import java.util.Date;

public class ProductInfoVO {
    private String pkFuelinfoid;

    private String fkPollutionid;

    private String productname;

    private Double productioncapacity;

    private String meaunit;

    private Double designproducthour;

    private String otherproductioninfo;

    private String updateuser;

    private Date updatetime;

    private String packagingform;

    private String fkPhysicalstatecode;

    public String getPackagingform() {
        return packagingform;
    }

    public void setPackagingform(String packagingform) {
        this.packagingform = packagingform == null ? null : packagingform.trim();
    }

    public String getFkPhysicalstatecode() {
        return fkPhysicalstatecode;
    }

    public void setFkPhysicalstatecode(String fkPhysicalstatecode) {
        this.fkPhysicalstatecode = fkPhysicalstatecode == null ? null : fkPhysicalstatecode.trim();
    }

    public String getPkFuelinfoid() {
        return pkFuelinfoid;
    }

    public void setPkFuelinfoid(String pkFuelinfoid) {
        this.pkFuelinfoid = pkFuelinfoid == null ? null : pkFuelinfoid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname == null ? null : productname.trim();
    }

    public Double getProductioncapacity() {
        return productioncapacity;
    }

    public void setProductioncapacity(Double productioncapacity) {
        this.productioncapacity = productioncapacity;
    }

    public String getMeaunit() {
        return meaunit;
    }

    public void setMeaunit(String meaunit) {
        this.meaunit = meaunit == null ? null : meaunit.trim();
    }

    public Double getDesignproducthour() {
        return designproducthour;
    }

    public void setDesignproducthour(Double designproducthour) {
        this.designproducthour = designproducthour;
    }

    public String getOtherproductioninfo() {
        return otherproductioninfo;
    }

    public void setOtherproductioninfo(String otherproductioninfo) {
        this.otherproductioninfo = otherproductioninfo == null ? null : otherproductioninfo.trim();
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
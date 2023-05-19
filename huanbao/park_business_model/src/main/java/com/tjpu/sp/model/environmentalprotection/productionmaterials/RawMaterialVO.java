package com.tjpu.sp.model.environmentalprotection.productionmaterials;

import java.util.Date;

public class RawMaterialVO {
    private String pkRawmaterialid;

    private String fkPollutionid;

    private String materialname;

    private String fkMaterialtype;

    private Double consume;

    private String meaunit;

    private Double sulphurproportion;

    private String harmfulproportion;

    private Double utilization;

    private Double materialpurity;

    private String fkFirerisktypecode;

    private String fkHazardlevelcode;

    private String fkHazardtypecode;

    private Double explosionlimit;

    private String materialsources;

    private String remark;

    private String updateuser;

    private Date updatetime;

    public String getPkRawmaterialid() {
        return pkRawmaterialid;
    }

    public void setPkRawmaterialid(String pkRawmaterialid) {
        this.pkRawmaterialid = pkRawmaterialid == null ? null : pkRawmaterialid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getMaterialname() {
        return materialname;
    }

    public void setMaterialname(String materialname) {
        this.materialname = materialname == null ? null : materialname.trim();
    }

    public String getFkMaterialtype() {
        return fkMaterialtype;
    }

    public void setFkMaterialtype(String fkMaterialtype) {
        this.fkMaterialtype = fkMaterialtype == null ? null : fkMaterialtype.trim();
    }

    public Double getConsume() {
        return consume;
    }

    public void setConsume(Double consume) {
        this.consume = consume;
    }

    public String getMeaunit() {
        return meaunit;
    }

    public void setMeaunit(String meaunit) {
        this.meaunit = meaunit == null ? null : meaunit.trim();
    }

    public Double getSulphurproportion() {
        return sulphurproportion;
    }

    public void setSulphurproportion(Double sulphurproportion) {
        this.sulphurproportion = sulphurproportion;
    }

    public String getHarmfulproportion() {
        return harmfulproportion;
    }

    public void setHarmfulproportion(String harmfulproportion) {
        this.harmfulproportion = harmfulproportion == null ? null : harmfulproportion.trim();
    }

    public Double getUtilization() {
        return utilization;
    }

    public void setUtilization(Double utilization) {
        this.utilization = utilization;
    }

    public Double getMaterialpurity() {
        return materialpurity;
    }

    public void setMaterialpurity(Double materialpurity) {
        this.materialpurity = materialpurity;
    }

    public String getFkFirerisktypecode() {
        return fkFirerisktypecode;
    }

    public void setFkFirerisktypecode(String fkFirerisktypecode) {
        this.fkFirerisktypecode = fkFirerisktypecode == null ? null : fkFirerisktypecode.trim();
    }

    public String getFkHazardlevelcode() {
        return fkHazardlevelcode;
    }

    public void setFkHazardlevelcode(String fkHazardlevelcode) {
        this.fkHazardlevelcode = fkHazardlevelcode == null ? null : fkHazardlevelcode.trim();
    }

    public String getFkHazardtypecode() {
        return fkHazardtypecode;
    }

    public void setFkHazardtypecode(String fkHazardtypecode) {
        this.fkHazardtypecode = fkHazardtypecode == null ? null : fkHazardtypecode.trim();
    }

    public Double getExplosionlimit() {
        return explosionlimit;
    }

    public void setExplosionlimit(Double explosionlimit) {
        this.explosionlimit = explosionlimit;
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

    public String getMaterialsources() {
        return materialsources;
    }

    public void setMaterialsources(String materialsources) {
        this.materialsources = materialsources;
    }
}
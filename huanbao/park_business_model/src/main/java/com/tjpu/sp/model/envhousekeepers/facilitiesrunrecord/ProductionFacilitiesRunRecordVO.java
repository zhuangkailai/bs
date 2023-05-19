package com.tjpu.sp.model.envhousekeepers.facilitiesrunrecord;

import java.util.Date;

public class ProductionFacilitiesRunRecordVO {
    private String pkId;

    private String fkPollutionid;

    private String facilitiename;

    private String facilitiecode;

    private String facilitiemodel;

    private String parametername;

    private Double designvalue;

    private Double actuallyvalue;

    private String parameterunit;

    private String throughput;

    private String throughputunit;

    private Date runstarttime;

    private Date runendtime;

    private String productionload;

    private String semiproduct;

    private String semiproductunit;

    private String finalproduct;

    private String finalproductunit;

    private String rawmaterialname;

    private String fkMaterialtype;

    private Double consume;

    private String meaunit;

    private String harmfulproportion;

    private String harmfulcomposition;

    private String materialsources;

    private String recorduser;

    private Date recordtime;

    private String revieweruser;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getFacilitiename() {
        return facilitiename;
    }

    public void setFacilitiename(String facilitiename) {
        this.facilitiename = facilitiename == null ? null : facilitiename.trim();
    }

    public String getFacilitiecode() {
        return facilitiecode;
    }

    public void setFacilitiecode(String facilitiecode) {
        this.facilitiecode = facilitiecode == null ? null : facilitiecode.trim();
    }

    public String getFacilitiemodel() {
        return facilitiemodel;
    }

    public void setFacilitiemodel(String facilitiemodel) {
        this.facilitiemodel = facilitiemodel == null ? null : facilitiemodel.trim();
    }

    public String getParametername() {
        return parametername;
    }

    public void setParametername(String parametername) {
        this.parametername = parametername == null ? null : parametername.trim();
    }

    public Double getDesignvalue() {
        return designvalue;
    }

    public void setDesignvalue(Double designvalue) {
        this.designvalue = designvalue;
    }

    public Double getActuallyvalue() {
        return actuallyvalue;
    }

    public void setActuallyvalue(Double actuallyvalue) {
        this.actuallyvalue = actuallyvalue;
    }

    public String getParameterunit() {
        return parameterunit;
    }

    public void setParameterunit(String parameterunit) {
        this.parameterunit = parameterunit == null ? null : parameterunit.trim();
    }

    public String getThroughput() {
        return throughput;
    }

    public void setThroughput(String throughput) {
        this.throughput = throughput == null ? null : throughput.trim();
    }

    public String getThroughputunit() {
        return throughputunit;
    }

    public void setThroughputunit(String throughputunit) {
        this.throughputunit = throughputunit == null ? null : throughputunit.trim();
    }

    public Date getRunstarttime() {
        return runstarttime;
    }

    public void setRunstarttime(Date runstarttime) {
        this.runstarttime = runstarttime;
    }

    public Date getRunendtime() {
        return runendtime;
    }

    public void setRunendtime(Date runendtime) {
        this.runendtime = runendtime;
    }

    public String getProductionload() {
        return productionload;
    }

    public void setProductionload(String productionload) {
        this.productionload = productionload == null ? null : productionload.trim();
    }

    public String getSemiproduct() {
        return semiproduct;
    }

    public void setSemiproduct(String semiproduct) {
        this.semiproduct = semiproduct == null ? null : semiproduct.trim();
    }

    public String getSemiproductunit() {
        return semiproductunit;
    }

    public void setSemiproductunit(String semiproductunit) {
        this.semiproductunit = semiproductunit == null ? null : semiproductunit.trim();
    }

    public String getFinalproduct() {
        return finalproduct;
    }

    public void setFinalproduct(String finalproduct) {
        this.finalproduct = finalproduct == null ? null : finalproduct.trim();
    }

    public String getFinalproductunit() {
        return finalproductunit;
    }

    public void setFinalproductunit(String finalproductunit) {
        this.finalproductunit = finalproductunit == null ? null : finalproductunit.trim();
    }

    public String getRawmaterialname() {
        return rawmaterialname;
    }

    public void setRawmaterialname(String rawmaterialname) {
        this.rawmaterialname = rawmaterialname == null ? null : rawmaterialname.trim();
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

    public String getHarmfulproportion() {
        return harmfulproportion;
    }

    public void setHarmfulproportion(String harmfulproportion) {
        this.harmfulproportion = harmfulproportion == null ? null : harmfulproportion.trim();
    }

    public String getHarmfulcomposition() {
        return harmfulcomposition;
    }

    public void setHarmfulcomposition(String harmfulcomposition) {
        this.harmfulcomposition = harmfulcomposition == null ? null : harmfulcomposition.trim();
    }

    public String getMaterialsources() {
        return materialsources;
    }

    public void setMaterialsources(String materialsources) {
        this.materialsources = materialsources == null ? null : materialsources.trim();
    }

    public String getRecorduser() {
        return recorduser;
    }

    public void setRecorduser(String recorduser) {
        this.recorduser = recorduser == null ? null : recorduser.trim();
    }

    public Date getRecordtime() {
        return recordtime;
    }

    public void setRecordtime(Date recordtime) {
        this.recordtime = recordtime;
    }

    public String getRevieweruser() {
        return revieweruser;
    }

    public void setRevieweruser(String revieweruser) {
        this.revieweruser = revieweruser == null ? null : revieweruser.trim();
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
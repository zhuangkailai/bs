
package com.tjpu.sp.model.environmentalprotection.hazardouswasteinfo;


public class HazardousWasteInfoVO {

    private String pkid;

    private String fkpollutionid;

    private String fkwastematerialtype;

    private String monthdate;

    private Double lastmonthlegacyquantity;

    private Double productionquantity;

    private Double plannedproductionquantity;

    private Double stockadjustquantity;

    private Double delegateutilizationquantity;

    private Double outprovincetransferlist;

    private Double provincetransferlist;

    private Double selfusequantity;

    private Double amongusequantity;

    private Double amongmanagequantity;

    private Double secondaryquantity;

    private Double endingstocks;

    private Double overyearstocks;

    private String remark;

    private String updatetime;

    private String updateuser;


    public void setpkid(String pkid) {
        this.pkid = pkid;
    }


    public String getpkid() {
        return "".equals(pkid)?null:pkid;
        
    }
    public void setfkpollutionid(String fkpollutionid) {
        this.fkpollutionid = fkpollutionid;
    }


    public String getfkpollutionid() {
        return "".equals(fkpollutionid)?null:fkpollutionid;
        
    }
    public void setfkwastematerialtype(String fkwastematerialtype) {
        this.fkwastematerialtype = fkwastematerialtype;
    }


    public String getfkwastematerialtype() {
        return "".equals(fkwastematerialtype)?null:fkwastematerialtype;
        
    }
    public void setmonthdate(String monthdate) {
        this.monthdate = monthdate;
    }


    public String getmonthdate() {
        return "".equals(monthdate)?null:monthdate;
        
    }
    public void setlastmonthlegacyquantity(Double lastmonthlegacyquantity) {
        this.lastmonthlegacyquantity = lastmonthlegacyquantity;
    }


    public Double getlastmonthlegacyquantity() {
        return lastmonthlegacyquantity;
    }
    public void setproductionquantity(Double productionquantity) {
        this.productionquantity = productionquantity;
    }


    public Double getproductionquantity() {
        return productionquantity;
    }
    public void setplannedproductionquantity(Double plannedproductionquantity) {
        this.plannedproductionquantity = plannedproductionquantity;
    }


    public Double getplannedproductionquantity() {
        return plannedproductionquantity;
    }
    public void setstockadjustquantity(Double stockadjustquantity) {
        this.stockadjustquantity = stockadjustquantity;
    }


    public Double getstockadjustquantity() {
        return stockadjustquantity;
    }
    public void setdelegateutilizationquantity(Double delegateutilizationquantity) {
        this.delegateutilizationquantity = delegateutilizationquantity;
    }


    public Double getdelegateutilizationquantity() {
        return delegateutilizationquantity;
    }
    public void setoutprovincetransferlist(Double outprovincetransferlist) {
        this.outprovincetransferlist = outprovincetransferlist;
    }


    public Double getoutprovincetransferlist() {
        return outprovincetransferlist;
    }
    public void setprovincetransferlist(Double provincetransferlist) {
        this.provincetransferlist = provincetransferlist;
    }


    public Double getprovincetransferlist() {
        return provincetransferlist;
    }
    public void setselfusequantity(Double selfusequantity) {
        this.selfusequantity = selfusequantity;
    }


    public Double getselfusequantity() {
        return selfusequantity;
    }
    public void setamongusequantity(Double amongusequantity) {
        this.amongusequantity = amongusequantity;
    }


    public Double getamongusequantity() {
        return amongusequantity;
    }
    public void setamongmanagequantity(Double amongmanagequantity) {
        this.amongmanagequantity = amongmanagequantity;
    }


    public Double getamongmanagequantity() {
        return amongmanagequantity;
    }
    public void setsecondaryquantity(Double secondaryquantity) {
        this.secondaryquantity = secondaryquantity;
    }


    public Double getsecondaryquantity() {
        return secondaryquantity;
    }
    public void setendingstocks(Double endingstocks) {
        this.endingstocks = endingstocks;
    }


    public Double getendingstocks() {
        return endingstocks;
    }
    public void setoveryearstocks(Double overyearstocks) {
        this.overyearstocks = overyearstocks;
    }


    public Double getoveryearstocks() {
        return overyearstocks;
    }
    public void setremark(String remark) {
        this.remark = remark;
    }


    public String getremark() {
        return "".equals(remark)?null:remark;
        
    }
    public void setupdatetime(String updatetime) {
        this.updatetime = updatetime;
    }


    public String getupdatetime() {
        return "".equals(updatetime)?null:updatetime;
        
    }
    public void setupdateuser(String updateuser) {
        this.updateuser = updateuser;
    }


    public String getupdateuser() {
        return "".equals(updateuser)?null:updateuser;
        
    }

}

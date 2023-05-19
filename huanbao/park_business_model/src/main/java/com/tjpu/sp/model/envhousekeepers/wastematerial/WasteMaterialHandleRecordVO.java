package com.tjpu.sp.model.envhousekeepers.wastematerial;

import java.util.Date;

public class WasteMaterialHandleRecordVO {
    private String pkId;

    private String fkPollutionid;

    private String fkWastematerialcode;

    private Date recorddate;

    private Double generatednum;

    private String scpersoncharge;

    private String receiptnum;

    private String inventorybalance;

    private String zcpersoncharge;

    private String receivingunit;

    private Double outsourcehandlenum;

    private String transfernumber;

    private Date transfertime;

    private String wwpersoncharge;

    private Double selfdisposalnum;

    private String zxpersoncharge;

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

    public String getFkWastematerialcode() {
        return fkWastematerialcode;
    }

    public void setFkWastematerialcode(String fkWastematerialcode) {
        this.fkWastematerialcode = fkWastematerialcode == null ? null : fkWastematerialcode.trim();
    }

    public Date getRecorddate() {
        return recorddate;
    }

    public void setRecorddate(Date recorddate) {
        this.recorddate = recorddate;
    }

    public Double getGeneratednum() {
        return generatednum;
    }

    public void setGeneratednum(Double generatednum) {
        this.generatednum = generatednum;
    }

    public String getScpersoncharge() {
        return scpersoncharge;
    }

    public void setScpersoncharge(String scpersoncharge) {
        this.scpersoncharge = scpersoncharge == null ? null : scpersoncharge.trim();
    }

    public String getReceiptnum() {
        return receiptnum;
    }

    public void setReceiptnum(String receiptnum) {
        this.receiptnum = receiptnum == null ? null : receiptnum.trim();
    }

    public String getInventorybalance() {
        return inventorybalance;
    }

    public void setInventorybalance(String inventorybalance) {
        this.inventorybalance = inventorybalance == null ? null : inventorybalance.trim();
    }

    public String getZcpersoncharge() {
        return zcpersoncharge;
    }

    public void setZcpersoncharge(String zcpersoncharge) {
        this.zcpersoncharge = zcpersoncharge == null ? null : zcpersoncharge.trim();
    }

    public String getReceivingunit() {
        return receivingunit;
    }

    public void setReceivingunit(String receivingunit) {
        this.receivingunit = receivingunit == null ? null : receivingunit.trim();
    }

    public Double getOutsourcehandlenum() {
        return outsourcehandlenum;
    }

    public void setOutsourcehandlenum(Double outsourcehandlenum) {
        this.outsourcehandlenum = outsourcehandlenum;
    }

    public String getTransfernumber() {
        return transfernumber;
    }

    public void setTransfernumber(String transfernumber) {
        this.transfernumber = transfernumber == null ? null : transfernumber.trim();
    }

    public Date getTransfertime() {
        return transfertime;
    }

    public void setTransfertime(Date transfertime) {
        this.transfertime = transfertime;
    }

    public String getWwpersoncharge() {
        return wwpersoncharge;
    }

    public void setWwpersoncharge(String wwpersoncharge) {
        this.wwpersoncharge = wwpersoncharge == null ? null : wwpersoncharge.trim();
    }

    public Double getSelfdisposalnum() {
        return selfdisposalnum;
    }

    public void setSelfdisposalnum(Double selfdisposalnum) {
        this.selfdisposalnum = selfdisposalnum;
    }

    public String getZxpersoncharge() {
        return zxpersoncharge;
    }

    public void setZxpersoncharge(String zxpersoncharge) {
        this.zxpersoncharge = zxpersoncharge == null ? null : zxpersoncharge.trim();
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
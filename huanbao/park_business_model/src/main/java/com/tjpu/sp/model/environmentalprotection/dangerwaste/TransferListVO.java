package com.tjpu.sp.model.environmentalprotection.dangerwaste;

public class TransferListVO {
    private String pkid;

    private String transferlistnum;

    private String fkProductentid;

    private String transferentname;

    private String receiveentname;

    private String fkWastematerialcode;

    private String materialdetailname;

    private Double transferquantity;
    private Double receivevolume;

    private String state;
    private String quantityunit;

    private String materialproperty;

    private String materialstate;

    private String packingway;

    private String dangerouscomponent;

    private String emergencymeasure;

    private String outtransportpurpose;

    private String shipper;

    private String transportaddress;

    private String transfertime;

    private String firsttransporter;

    private String firsttransportdate;

    private String firsttransporttoolmodel;

    private String firsttransporttoolnum;

    private String firsttransportlicensenum;

    private String firsttransportstartaddr;

    private String firsttransportmidaddr;

    private String firsttransportendaddr;

    private String secondtransporter;

    private String secondtransportdate;

    private String secondtransporttoolmodel;

    private String secondtransporttoolnum;

    private String secondtransportlicensenum;

    private String secondtransportstartaddr;

    private String secondtransportmidaddr;

    private String secondtransportendaddr;

    private String transportstartdate;

    private String transportenddate;

    private Double receivequantity;

    private String receiver;

    private String receivedate;

    private String fkDisposalmethod;

    private String receiveunitlicensenum;

    private String barcode;

    private String fkTransferareatypecode;

    private String moveyear;

    private String remark;

    private String updateuser;

    private String updatetime;

    public Double getReceivevolume() {
        return receivevolume;
    }

    public void setReceivevolume(Double receivevolume) {
        this.receivevolume = receivevolume;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTransferlistnum() {
        return transferlistnum;
    }

    public void setTransferlistnum(String transferlistnum) {
        this.transferlistnum = transferlistnum == null ? null : transferlistnum.trim();
    }

    public String getFkProductentid() {
        return fkProductentid;
    }

    public void setFkProductentid(String fkProductentid) {
        this.fkProductentid = fkProductentid == null ? null : fkProductentid.trim();
    }


    public String getTransferentname() {
        return transferentname;
    }

    public void setTransferentname(String transferentname) {
        this.transferentname = transferentname == null ? null : transferentname.trim();
    }


    public String getReceiveentname() {
        return receiveentname;
    }

    public void setReceiveentname(String receiveentname) {
        this.receiveentname = receiveentname == null ? null : receiveentname.trim();
    }

    public String getFkWastematerialcode() {
        return fkWastematerialcode;
    }

    public void setFkWastematerialcode(String fkWastematerialcode) {
        this.fkWastematerialcode = fkWastematerialcode == null ? null : fkWastematerialcode.trim();
    }

    public String getMaterialdetailname() {
        return materialdetailname;
    }

    public void setMaterialdetailname(String materialdetailname) {
        this.materialdetailname = materialdetailname == null ? null : materialdetailname.trim();
    }

    public Double getTransferquantity() {
        return transferquantity;
    }

    public void setTransferquantity(Double transferquantity) {
        this.transferquantity = transferquantity;
    }

    public String getQuantityunit() {
        return quantityunit;
    }

    public void setQuantityunit(String quantityunit) {
        this.quantityunit = quantityunit == null ? null : quantityunit.trim();
    }

    public String getMaterialproperty() {
        return materialproperty;
    }

    public void setMaterialproperty(String materialproperty) {
        this.materialproperty = materialproperty == null ? null : materialproperty.trim();
    }

    public String getMaterialstate() {
        return materialstate;
    }

    public void setMaterialstate(String materialstate) {
        this.materialstate = materialstate == null ? null : materialstate.trim();
    }

    public String getPackingway() {
        return packingway;
    }

    public void setPackingway(String packingway) {
        this.packingway = packingway == null ? null : packingway.trim();
    }

    public String getDangerouscomponent() {
        return dangerouscomponent;
    }

    public void setDangerouscomponent(String dangerouscomponent) {
        this.dangerouscomponent = dangerouscomponent == null ? null : dangerouscomponent.trim();
    }

    public String getEmergencymeasure() {
        return emergencymeasure;
    }

    public void setEmergencymeasure(String emergencymeasure) {
        this.emergencymeasure = emergencymeasure == null ? null : emergencymeasure.trim();
    }

    public String getOuttransportpurpose() {
        return outtransportpurpose;
    }

    public void setOuttransportpurpose(String outtransportpurpose) {
        this.outtransportpurpose = outtransportpurpose == null ? null : outtransportpurpose.trim();
    }

    public String getShipper() {
        return shipper;
    }

    public void setShipper(String shipper) {
        this.shipper = shipper == null ? null : shipper.trim();
    }

    public String getTransportaddress() {
        return transportaddress;
    }

    public void setTransportaddress(String transportaddress) {
        this.transportaddress = transportaddress == null ? null : transportaddress.trim();
    }

    public String getTransfertime() {
        return "".equals(transfertime)?null:transfertime;
    }

    public void setTransfertime(String transfertime) {
        this.transfertime = transfertime;
    }

    public String getFirsttransporter() {
        return firsttransporter;
    }

    public void setFirsttransporter(String firsttransporter) {
        this.firsttransporter = firsttransporter == null ? null : firsttransporter.trim();
    }

    public String getFirsttransportdate() {
        return "".equals(firsttransportdate)?null:firsttransportdate;
    }

    public void setFirsttransportdate(String firsttransportdate) {
        this.firsttransportdate = firsttransportdate;
    }

    public String getFirsttransporttoolmodel() {
        return firsttransporttoolmodel;
    }

    public void setFirsttransporttoolmodel(String firsttransporttoolmodel) {
        this.firsttransporttoolmodel = firsttransporttoolmodel == null ? null : firsttransporttoolmodel.trim();
    }

    public String getFirsttransporttoolnum() {
        return firsttransporttoolnum;
    }

    public void setFirsttransporttoolnum(String firsttransporttoolnum) {
        this.firsttransporttoolnum = firsttransporttoolnum == null ? null : firsttransporttoolnum.trim();
    }

    public String getFirsttransportlicensenum() {
        return firsttransportlicensenum;
    }

    public void setFirsttransportlicensenum(String firsttransportlicensenum) {
        this.firsttransportlicensenum = firsttransportlicensenum == null ? null : firsttransportlicensenum.trim();
    }

    public String getFirsttransportstartaddr() {
        return firsttransportstartaddr;
    }

    public void setFirsttransportstartaddr(String firsttransportstartaddr) {
        this.firsttransportstartaddr = firsttransportstartaddr == null ? null : firsttransportstartaddr.trim();
    }

    public String getFirsttransportmidaddr() {
        return firsttransportmidaddr;
    }

    public void setFirsttransportmidaddr(String firsttransportmidaddr) {
        this.firsttransportmidaddr = firsttransportmidaddr == null ? null : firsttransportmidaddr.trim();
    }

    public String getFirsttransportendaddr() {
        return firsttransportendaddr;
    }

    public void setFirsttransportendaddr(String firsttransportendaddr) {
        this.firsttransportendaddr = firsttransportendaddr == null ? null : firsttransportendaddr.trim();
    }

    public String getSecondtransporter() {
        return secondtransporter;
    }

    public void setSecondtransporter(String secondtransporter) {
        this.secondtransporter = secondtransporter == null ? null : secondtransporter.trim();
    }

    public String getSecondtransportdate() {
        return "".equals(secondtransportdate)?null:secondtransportdate;
    }

    public void setSecondtransportdate(String secondtransportdate) {
        this.secondtransportdate = secondtransportdate;
    }

    public String getSecondtransporttoolmodel() {
        return secondtransporttoolmodel;
    }

    public void setSecondtransporttoolmodel(String secondtransporttoolmodel) {
        this.secondtransporttoolmodel = secondtransporttoolmodel == null ? null : secondtransporttoolmodel.trim();
    }

    public String getSecondtransporttoolnum() {
        return secondtransporttoolnum;
    }

    public void setSecondtransporttoolnum(String secondtransporttoolnum) {
        this.secondtransporttoolnum = secondtransporttoolnum == null ? null : secondtransporttoolnum.trim();
    }

    public String getSecondtransportlicensenum() {
        return secondtransportlicensenum;
    }

    public void setSecondtransportlicensenum(String secondtransportlicensenum) {
        this.secondtransportlicensenum = secondtransportlicensenum == null ? null : secondtransportlicensenum.trim();
    }

    public String getSecondtransportstartaddr() {
        return secondtransportstartaddr;
    }

    public void setSecondtransportstartaddr(String secondtransportstartaddr) {
        this.secondtransportstartaddr = secondtransportstartaddr == null ? null : secondtransportstartaddr.trim();
    }

    public String getSecondtransportmidaddr() {
        return secondtransportmidaddr;
    }

    public void setSecondtransportmidaddr(String secondtransportmidaddr) {
        this.secondtransportmidaddr = secondtransportmidaddr == null ? null : secondtransportmidaddr.trim();
    }

    public String getSecondtransportendaddr() {
        return secondtransportendaddr;
    }

    public void setSecondtransportendaddr(String secondtransportendaddr) {
        this.secondtransportendaddr = secondtransportendaddr == null ? null : secondtransportendaddr.trim();
    }

    public String getTransportstartdate() {
        return "".equals(transportstartdate)?null:transportstartdate;
    }

    public void setTransportstartdate(String transportstartdate) {
        this.transportstartdate = transportstartdate;
    }

    public String getTransportenddate() {
        return "".equals(transportenddate)?null:transportenddate;
    }

    public void setTransportenddate(String transportenddate) {
        this.transportenddate = transportenddate;
    }

    public Double getReceivequantity() {
        return receivequantity;
    }

    public void setReceivequantity(Double receivequantity) {
        this.receivequantity = receivequantity;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver == null ? null : receiver.trim();
    }

    public String getReceivedate() {
        return "".equals(receivedate)?null:receivedate;
    }

    public void setReceivedate(String receivedate) {
        this.receivedate = receivedate;
    }

    public String getFkDisposalmethod() {
        return fkDisposalmethod;
    }

    public void setFkDisposalmethod(String fkDisposalmethod) {
        this.fkDisposalmethod = fkDisposalmethod == null ? null : fkDisposalmethod.trim();
    }

    public String getReceiveunitlicensenum() {
        return receiveunitlicensenum;
    }

    public void setReceiveunitlicensenum(String receiveunitlicensenum) {
        this.receiveunitlicensenum = receiveunitlicensenum == null ? null : receiveunitlicensenum.trim();
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode == null ? null : barcode.trim();
    }

    public String getFkTransferareatypecode() {
        return fkTransferareatypecode;
    }

    public void setFkTransferareatypecode(String fkTransferareatypecode) {
        this.fkTransferareatypecode = fkTransferareatypecode == null ? null : fkTransferareatypecode.trim();
    }

    public String getMoveyear() {
        return moveyear;
    }

    public void setMoveyear(String moveyear) {
        this.moveyear = moveyear == null ? null : moveyear.trim();
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

    public String getUpdatetime() {
        return "".equals(updatetime)?null:updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getPkid() {
        return pkid;
    }

    public void setPkid(String pkid) {
        this.pkid = pkid;
    }
}
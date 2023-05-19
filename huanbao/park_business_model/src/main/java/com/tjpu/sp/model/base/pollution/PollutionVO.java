package com.tjpu.sp.model.base.pollution;


import java.util.Date;
import java.util.List;

public class PollutionVO {
    private String pkpollutionid;

    private String pollutioncode;

    private String pollutionname;

    private String address;

    private Double longitude;

    private Double latitude;

    private String fkregion;

    private String fkentstate;

    private String fkentscale;

    private String fkenttype;

    private String fkregistrationtype;

    private String fkbasin;

    private String fkdraindirection;

    private String fkentcontrolleve;

    private String fkcontrollevelwater;

    private String fkcontrollevelgas;

    private String fkcentralentrelation;

    private String fkgroupcompany;

    private String fkindustrytype;

    private String fkkeyindustrytype;

    private String fkpollutionclass;

    private String organizationcode;

    private String environmentalmanager;

    private String linkmanphone;

    private String linkmanemail;

    private String fax;

    private String postalcode;

    private Double totalinvestment;

    private Double totalfloorarea;

    private String establishmentdate;

    private String pollutionintroduction;
    private String closedate;

    private String oldpkid;

    private Short isneedaudit;

    private String fkparkid;
    private String putintodate;
    private String newextensiondate;

    private String department;

    private Integer personnumber;

    private String openbank;

    private String bankaccount;

    private String pollutionurl;

    private String businessllicense;

    private String entsocialcreditcode;

    private String entregistercode;

    private String operatingperiod;

    private String businessscope;

    private String corporationname;

    private String certificateno;

    private String fkcertificate;

    private String fknationality;

    private Double registercapital;

    private String fkcurrency;
    private String revokedate;

    private String revokereason;

    private String revokeorganization;

    private Short isspecialname;

    private String maintaindocumentnumber;

    private String ismaintainname;

    private String fkunittype;

    private String fksubjectionrelation;

    private String fileid;

    private String headofsafety;

    private String safetyfixedphone;

    private String safetymobilephone;

    private String safetyemail;

    private Integer employeesnum;

    private Integer safeengineernum;

    private Integer safemanagernum;

    private Integer specialoperatornum;

    private Integer safepromanagernum;

    private Integer emergenmanagernum;

    private Short ishavesecuritagen;

    private String fkstandardlevel;

    private Short ishavesecuritperson;

    private String dangergovernplan;

    private Short ishavemajorhazards;

    private String dangergovernsystem;

    private String updatetime;

    private String updateuser;

    private List labelcode;

    private String shortername;

    private String ishavemajorhazardsname;
    private String ishavesecuritagenname;
    private String ishavesecuritpersonname;
    private String certificatename;
    private String entscalename;
    private String pollutionclassname;
    private String controllevename;
    private String standardlevelname;
    private String draindirectionname;
    private String registrationtypename;
    private String industrytypename;
    private String basinname;
    private String keyindustrytypename;
    private String entstatename;
    private String regionjson;

    public String getPkpollutionid() {
        return pkpollutionid;
    }

    public void setPkpollutionid(String pkpollutionid) {
        this.pkpollutionid = pkpollutionid;
    }

    public String getPollutioncode() {
        return pollutioncode;
    }

    public void setPollutioncode(String pollutioncode) {
        this.pollutioncode = pollutioncode;
    }

    public String getPollutionname() {
        return pollutionname;
    }

    public void setPollutionname(String pollutionname) {
        this.pollutionname = pollutionname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getFkregion() {
        return fkregion;
    }

    public void setFkregion(String fkregion) {
        this.fkregion = fkregion;
    }

    public String getFkentstate() {
        return fkentstate;
    }

    public void setFkentstate(String fkentstate) {
        this.fkentstate = fkentstate;
    }

    public String getFkentscale() {
        return fkentscale;
    }

    public void setFkentscale(String fkentscale) {
        this.fkentscale = fkentscale;
    }

    public String getFkenttype() {
        return fkenttype;
    }

    public void setFkenttype(String fkenttype) {
        this.fkenttype = fkenttype;
    }

    public String getFkregistrationtype() {
        return fkregistrationtype;
    }

    public void setFkregistrationtype(String fkregistrationtype) {
        this.fkregistrationtype = fkregistrationtype;
    }

    public String getFkbasin() {
        return fkbasin;
    }

    public void setFkbasin(String fkbasin) {
        this.fkbasin = fkbasin;
    }

    public String getFkdraindirection() {
        return fkdraindirection;
    }

    public void setFkdraindirection(String fkdraindirection) {
        this.fkdraindirection = fkdraindirection;
    }

    public String getFkentcontrolleve() {
        return fkentcontrolleve;
    }

    public void setFkentcontrolleve(String fkentcontrolleve) {
        this.fkentcontrolleve = fkentcontrolleve;
    }

    public String getFkcontrollevelwater() {
        return fkcontrollevelwater;
    }

    public void setFkcontrollevelwater(String fkcontrollevelwater) {
        this.fkcontrollevelwater = fkcontrollevelwater;
    }

    public String getFkcontrollevelgas() {
        return fkcontrollevelgas;
    }

    public void setFkcontrollevelgas(String fkcontrollevelgas) {
        this.fkcontrollevelgas = fkcontrollevelgas;
    }

    public String getFkcentralentrelation() {
        return fkcentralentrelation;
    }

    public void setFkcentralentrelation(String fkcentralentrelation) {
        this.fkcentralentrelation = fkcentralentrelation;
    }

    public String getFkgroupcompany() {
        return fkgroupcompany;
    }

    public void setFkgroupcompany(String fkgroupcompany) {
        this.fkgroupcompany = fkgroupcompany;
    }

    public String getFkindustrytype() {
        return fkindustrytype;
    }

    public void setFkindustrytype(String fkindustrytype) {
        this.fkindustrytype = fkindustrytype;
    }

    public String getFkkeyindustrytype() {
        return fkkeyindustrytype;
    }

    public void setFkkeyindustrytype(String fkkeyindustrytype) {
        this.fkkeyindustrytype = fkkeyindustrytype;
    }

    public String getFkpollutionclass() {
        return fkpollutionclass;
    }

    public void setFkpollutionclass(String fkpollutionclass) {
        this.fkpollutionclass = fkpollutionclass;
    }

    public String getOrganizationcode() {
        return organizationcode;
    }

    public void setOrganizationcode(String organizationcode) {
        this.organizationcode = organizationcode;
    }

    public String getEnvironmentalmanager() {
        return environmentalmanager;
    }

    public void setEnvironmentalmanager(String environmentalmanager) {
        this.environmentalmanager = environmentalmanager;
    }

    public String getLinkmanphone() {
        return linkmanphone;
    }

    public void setLinkmanphone(String linkmanphone) {
        this.linkmanphone = linkmanphone;
    }

    public String getLinkmanemail() {
        return linkmanemail;
    }

    public void setLinkmanemail(String linkmanemail) {
        this.linkmanemail = linkmanemail;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public Double getTotalinvestment() {
        return totalinvestment;
    }

    public void setTotalinvestment(Double totalinvestment) {
        this.totalinvestment = totalinvestment;
    }

    public Double getTotalfloorarea() {
        return totalfloorarea;
    }

    public void setTotalfloorarea(Double totalfloorarea) {
        this.totalfloorarea = totalfloorarea;
    }


    public String getPollutionintroduction() {
        return pollutionintroduction;
    }

    public void setPollutionintroduction(String pollutionintroduction) {
        this.pollutionintroduction = pollutionintroduction;
    }



    public String getOldpkid() {
        return oldpkid;
    }

    public void setOldpkid(String oldpkid) {
        this.oldpkid = oldpkid;
    }

    public Short getIsneedaudit() {
        return isneedaudit;
    }

    public void setIsneedaudit(Short isneedaudit) {
        this.isneedaudit = isneedaudit;
    }

    public String getFkparkid() {
        return fkparkid;
    }

    public void setFkparkid(String fkparkid) {
        this.fkparkid = fkparkid;
    }



    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getPersonnumber() {
        return personnumber;
    }

    public void setPersonnumber(Integer personnumber) {
        this.personnumber = personnumber;
    }

    public String getOpenbank() {
        return openbank;
    }

    public void setOpenbank(String openbank) {
        this.openbank = openbank;
    }

    public String getBankaccount() {
        return bankaccount;
    }

    public void setBankaccount(String bankaccount) {
        this.bankaccount = bankaccount;
    }

    public String getPollutionurl() {
        return pollutionurl;
    }

    public void setPollutionurl(String pollutionurl) {
        this.pollutionurl = pollutionurl;
    }

    public String getBusinessllicense() {
        return businessllicense;
    }

    public void setBusinessllicense(String businessllicense) {
        this.businessllicense = businessllicense;
    }

    public String getEntsocialcreditcode() {
        return entsocialcreditcode;
    }

    public void setEntsocialcreditcode(String entsocialcreditcode) {
        this.entsocialcreditcode = entsocialcreditcode;
    }

    public String getEntregistercode() {
        return entregistercode;
    }

    public void setEntregistercode(String entregistercode) {
        this.entregistercode = entregistercode;
    }

    public String getOperatingperiod() {
        return operatingperiod;
    }

    public void setOperatingperiod(String operatingperiod) {
        this.operatingperiod = operatingperiod;
    }

    public String getBusinessscope() {
        return businessscope;
    }

    public void setBusinessscope(String businessscope) {
        this.businessscope = businessscope;
    }

    public String getCorporationname() {
        return corporationname;
    }

    public void setCorporationname(String corporationname) {
        this.corporationname = corporationname;
    }

    public String getCertificateno() {
        return certificateno;
    }

    public void setCertificateno(String certificateno) {
        this.certificateno = certificateno;
    }

    public String getFkcertificate() {
        return fkcertificate;
    }

    public void setFkcertificate(String fkcertificate) {
        this.fkcertificate = fkcertificate;
    }

    public String getFknationality() {
        return fknationality;
    }

    public void setFknationality(String fknationality) {
        this.fknationality = fknationality;
    }

    public Double getRegistercapital() {
        return registercapital;
    }

    public void setRegistercapital(Double registercapital) {
        this.registercapital = registercapital;
    }

    public String getFkcurrency() {
        return fkcurrency;
    }

    public void setFkcurrency(String fkcurrency) {
        this.fkcurrency = fkcurrency;
    }



    public String getRevokereason() {
        return revokereason;
    }

    public void setRevokereason(String revokereason) {
        this.revokereason = revokereason;
    }

    public String getRevokeorganization() {
        return revokeorganization;
    }

    public void setRevokeorganization(String revokeorganization) {
        this.revokeorganization = revokeorganization;
    }

    public Short getIsspecialname() {
        return isspecialname;
    }

    public void setIsspecialname(Short isspecialname) {
        this.isspecialname = isspecialname;
    }

    public String getMaintaindocumentnumber() {
        return maintaindocumentnumber;
    }

    public void setMaintaindocumentnumber(String maintaindocumentnumber) {
        this.maintaindocumentnumber = maintaindocumentnumber;
    }

    public String getIsmaintainname() {
        return ismaintainname;
    }

    public void setIsmaintainname(String ismaintainname) {
        this.ismaintainname = ismaintainname;
    }

    public String getFkunittype() {
        return fkunittype;
    }

    public void setFkunittype(String fkunittype) {
        this.fkunittype = fkunittype;
    }

    public String getFksubjectionrelation() {
        return fksubjectionrelation;
    }

    public void setFksubjectionrelation(String fksubjectionrelation) {
        this.fksubjectionrelation = fksubjectionrelation;
    }

    public String getFileid() {
        return fileid;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid;
    }

    public String getHeadofsafety() {
        return headofsafety;
    }

    public void setHeadofsafety(String headofsafety) {
        this.headofsafety = headofsafety;
    }

    public String getSafetyfixedphone() {
        return safetyfixedphone;
    }

    public void setSafetyfixedphone(String safetyfixedphone) {
        this.safetyfixedphone = safetyfixedphone;
    }

    public String getSafetymobilephone() {
        return safetymobilephone;
    }

    public void setSafetymobilephone(String safetymobilephone) {
        this.safetymobilephone = safetymobilephone;
    }

    public String getSafetyemail() {
        return safetyemail;
    }

    public void setSafetyemail(String safetyemail) {
        this.safetyemail = safetyemail;
    }

    public Integer getEmployeesnum() {
        return employeesnum;
    }

    public void setEmployeesnum(Integer employeesnum) {
        this.employeesnum = employeesnum;
    }

    public Integer getSafeengineernum() {
        return safeengineernum;
    }

    public void setSafeengineernum(Integer safeengineernum) {
        this.safeengineernum = safeengineernum;
    }

    public Integer getSafemanagernum() {
        return safemanagernum;
    }

    public void setSafemanagernum(Integer safemanagernum) {
        this.safemanagernum = safemanagernum;
    }

    public Integer getSpecialoperatornum() {
        return specialoperatornum;
    }

    public void setSpecialoperatornum(Integer specialoperatornum) {
        this.specialoperatornum = specialoperatornum;
    }

    public Integer getSafepromanagernum() {
        return safepromanagernum;
    }

    public void setSafepromanagernum(Integer safepromanagernum) {
        this.safepromanagernum = safepromanagernum;
    }

    public Integer getEmergenmanagernum() {
        return emergenmanagernum;
    }

    public void setEmergenmanagernum(Integer emergenmanagernum) {
        this.emergenmanagernum = emergenmanagernum;
    }

    public Short getIshavesecuritagen() {
        return ishavesecuritagen;
    }

    public void setIshavesecuritagen(Short ishavesecuritagen) {
        this.ishavesecuritagen = ishavesecuritagen;
    }

    public String getFkstandardlevel() {
        return fkstandardlevel;
    }

    public void setFkstandardlevel(String fkstandardlevel) {
        this.fkstandardlevel = fkstandardlevel;
    }

    public Short getIshavesecuritperson() {
        return ishavesecuritperson;
    }

    public void setIshavesecuritperson(Short ishavesecuritperson) {
        this.ishavesecuritperson = ishavesecuritperson;
    }

    public String getDangergovernplan() {
        return dangergovernplan;
    }

    public void setDangergovernplan(String dangergovernplan) {
        this.dangergovernplan = dangergovernplan;
    }

    public Short getIshavemajorhazards() {
        return ishavemajorhazards;
    }

    public void setIshavemajorhazards(Short ishavemajorhazards) {
        this.ishavemajorhazards = ishavemajorhazards;
    }

    public String getDangergovernsystem() {
        return dangergovernsystem;
    }

    public void setDangergovernsystem(String dangergovernsystem) {
        this.dangergovernsystem = dangergovernsystem;
    }



    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }

    public List getLabelcode() {
        return labelcode;
    }

    public void setLabelcode(List labelcode) {
        this.labelcode = labelcode;
    }

    public String getShortername() {
        return shortername;
    }

    public void setShortername(String shortername) {
        this.shortername = shortername;
    }

    public String getIshavemajorhazardsname() {
        return ishavemajorhazardsname;
    }

    public void setIshavemajorhazardsname(String ishavemajorhazardsname) {
        this.ishavemajorhazardsname = ishavemajorhazardsname;
    }

    public String getIshavesecuritagenname() {
        return ishavesecuritagenname;
    }

    public void setIshavesecuritagenname(String ishavesecuritagenname) {
        this.ishavesecuritagenname = ishavesecuritagenname;
    }

    public String getIshavesecuritpersonname() {
        return ishavesecuritpersonname;
    }

    public void setIshavesecuritpersonname(String ishavesecuritpersonname) {
        this.ishavesecuritpersonname = ishavesecuritpersonname;
    }

    public String getCertificatename() {
        return certificatename;
    }

    public void setCertificatename(String certificatename) {
        this.certificatename = certificatename;
    }

    public String getEntscalename() {
        return entscalename;
    }

    public void setEntscalename(String entscalename) {
        this.entscalename = entscalename;
    }

    public String getPollutionclassname() {
        return pollutionclassname;
    }

    public void setPollutionclassname(String pollutionclassname) {
        this.pollutionclassname = pollutionclassname;
    }

    public String getControllevename() {
        return controllevename;
    }

    public void setControllevename(String controllevename) {
        this.controllevename = controllevename;
    }

    public String getStandardlevelname() {
        return standardlevelname;
    }

    public void setStandardlevelname(String standardlevelname) {
        this.standardlevelname = standardlevelname;
    }

    public String getDraindirectionname() {
        return draindirectionname;
    }

    public void setDraindirectionname(String draindirectionname) {
        this.draindirectionname = draindirectionname;
    }

    public String getRegistrationtypename() {
        return registrationtypename;
    }

    public void setRegistrationtypename(String registrationtypename) {
        this.registrationtypename = registrationtypename;
    }

    public String getEstablishmentdate() {
        return establishmentdate;
    }

    public void setEstablishmentdate(String establishmentdate) {
        this.establishmentdate = establishmentdate;
    }

    public String getClosedate() {
        return closedate;
    }

    public void setClosedate(String closedate) {
        this.closedate = closedate;
    }

    public String getPutintodate() {
        return putintodate;
    }

    public void setPutintodate(String putintodate) {
        this.putintodate = putintodate;
    }

    public String getNewextensiondate() {
        return newextensiondate;
    }

    public void setNewextensiondate(String newextensiondate) {
        this.newextensiondate = newextensiondate;
    }

    public String getRevokedate() {
        return revokedate;
    }

    public void setRevokedate(String revokedate) {
        this.revokedate = revokedate;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getIndustrytypename() {
        return industrytypename;
    }

    public void setIndustrytypename(String industrytypename) {
        this.industrytypename = industrytypename;
    }

    public String getBasinname() {
        return basinname;
    }

    public void setBasinname(String basinname) {
        this.basinname = basinname;
    }

    public String getKeyindustrytypename() {
        return keyindustrytypename;
    }

    public void setKeyindustrytypename(String keyindustrytypename) {
        this.keyindustrytypename = keyindustrytypename;
    }

    public String getEntstatename() {
        return entstatename;
    }

    public void setEntstatename(String entstatename) {
        this.entstatename = entstatename;
    }

    public String getRegionjson() {
        return regionjson;
    }

    public void setRegionjson(String regionjson) {
        this.regionjson = regionjson;
    }
}

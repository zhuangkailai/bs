package com.tjpu.sp.model.environmentalprotection.petitionlettercomplaint;

public class PetitionLetterComplaintVO {
    private String pkPetitionid;

    private String fkPollutionid;

    private String petitioncode;

    private String petitiontime;

    private String personname;

    private String persontel;

    private String eventtitle;

    private String fkPetitionkindcode;

    private String fkEnerlvlcode;

    private String petitionobject;

    private String address;

    private String description;

    private String notetime;

    private String notepersionname;

    private String noteunit;

    private String transactunit;

    private String transactpersion;

    private String fkPetitiontype;

    private String fkRegioncode;

    private Double latitude;

    private Double longitude;

    private String status;

    private String replytime;

    private String replyresult;

    private String remark;

    private String updateuser;

    private String updatetime;

    public String getPkPetitionid() {
        return pkPetitionid;
    }

    public void setPkPetitionid(String pkPetitionid) {
        this.pkPetitionid = pkPetitionid == null ? null : pkPetitionid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getPetitioncode() {
        return petitioncode;
    }

    public void setPetitioncode(String petitioncode) {
        this.petitioncode = petitioncode == null ? null : petitioncode.trim();
    }

    public String getPetitiontime() {
        return "".equals(petitiontime)?null:petitiontime;
    }

    public void setPetitiontime(String petitiontime) {
        this.petitiontime = petitiontime;
    }

    public String getPersonname() {
        return personname;
    }

    public void setPersonname(String personname) {
        this.personname = personname == null ? null : personname.trim();
    }

    public String getPersontel() {
        return persontel;
    }

    public void setPersontel(String persontel) {
        this.persontel = persontel == null ? null : persontel.trim();
    }

    public String getEventtitle() {
        return eventtitle;
    }

    public void setEventtitle(String eventtitle) {
        this.eventtitle = eventtitle == null ? null : eventtitle.trim();
    }

    public String getFkPetitionkindcode() {
        return fkPetitionkindcode;
    }

    public void setFkPetitionkindcode(String fkPetitionkindcode) {
        this.fkPetitionkindcode = fkPetitionkindcode == null ? null : fkPetitionkindcode.trim();
    }

    public String getFkEnerlvlcode() {
        return fkEnerlvlcode;
    }

    public void setFkEnerlvlcode(String fkEnerlvlcode) {
        this.fkEnerlvlcode = fkEnerlvlcode == null ? null : fkEnerlvlcode.trim();
    }

    public String getPetitionobject() {
        return petitionobject;
    }

    public void setPetitionobject(String petitionobject) {
        this.petitionobject = petitionobject == null ? null : petitionobject.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getNotetime() {
        return "".equals(notetime)?null:notetime;
    }

    public void setNotetime(String notetime) {
        this.notetime = notetime;
    }

    public String getNotepersionname() {
        return notepersionname;
    }

    public void setNotepersionname(String notepersionname) {
        this.notepersionname = notepersionname == null ? null : notepersionname.trim();
    }

    public String getNoteunit() {
        return noteunit;
    }

    public void setNoteunit(String noteunit) {
        this.noteunit = noteunit == null ? null : noteunit.trim();
    }

    public String getTransactunit() {
        return transactunit;
    }

    public void setTransactunit(String transactunit) {
        this.transactunit = transactunit == null ? null : transactunit.trim();
    }

    public String getTransactpersion() {
        return transactpersion;
    }

    public void setTransactpersion(String transactpersion) {
        this.transactpersion = transactpersion == null ? null : transactpersion.trim();
    }

    public String getFkPetitiontype() {
        return fkPetitiontype;
    }

    public void setFkPetitiontype(String fkPetitiontype) {
        this.fkPetitiontype = fkPetitiontype == null ? null : fkPetitiontype.trim();
    }

    public String getFkRegioncode() {
        return fkRegioncode;
    }

    public void setFkRegioncode(String fkRegioncode) {
        this.fkRegioncode = fkRegioncode == null ? null : fkRegioncode.trim();
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getReplytime() {
        return "".equals(replytime)?null:replytime;
    }

    public void setReplytime(String replytime) {
        this.replytime = replytime;
    }

    public String getReplyresult() {
        return replyresult;
    }

    public void setReplyresult(String replyresult) {
        this.replyresult = replyresult == null ? null : replyresult.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getUpdatetime() {
        return "".equals(updatetime)?null:updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }
}
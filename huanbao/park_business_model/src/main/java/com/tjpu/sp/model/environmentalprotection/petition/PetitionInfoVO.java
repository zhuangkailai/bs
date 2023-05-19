package com.tjpu.sp.model.environmentalprotection.petition;


public class PetitionInfoVO {
    private String pkId;

    private String petitiontitle;

    private String submittime;

    private String petitioncontent;

    private String undertakedepartment;

    private String completetime;

    private String completereply;

    private Double longitude;

    private Double latitude;

    private String petitionadress;

    private String petitionpeople;

    private String petitionpeopletelephone;

    private String fkFileid;

    private String updatetime;

    private String updateuser;

    private String pollutestarttime;

    private String polluteendtime;

    private String smell;

    private Integer Duration;

    private Short status;

    public String getSmell() {
        return smell;
    }

    public void setSmell(String smell) {
        this.smell = smell;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getPetitiontitle() {
        return petitiontitle;
    }

    public void setPetitiontitle(String petitiontitle) {
        this.petitiontitle = petitiontitle == null ? null : petitiontitle.trim();
    }

    public String getSubmittime() {
        return submittime;
    }

    public void setSubmittime(String submittime) {
        this.submittime = submittime;
    }

    public String getPetitioncontent() {
        return petitioncontent;
    }

    public void setPetitioncontent(String petitioncontent) {
        this.petitioncontent = petitioncontent == null ? null : petitioncontent.trim();
    }

    public String getUndertakedepartment() {
        return undertakedepartment;
    }

    public void setUndertakedepartment(String undertakedepartment) {
        this.undertakedepartment = undertakedepartment == null ? null : undertakedepartment.trim();
    }

    public String getCompletetime() {
        return completetime;
    }

    public void setCompletetime(String completetime) {
        this.completetime = completetime;
    }

    public String getCompletereply() {
        return completereply;
    }

    public void setCompletereply(String completereply) {
        this.completereply = completereply == null ? null : completereply.trim();
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

    public String getPetitionadress() {
        return petitionadress;
    }

    public void setPetitionadress(String petitionadress) {
        this.petitionadress = petitionadress == null ? null : petitionadress.trim();
    }

    public String getPetitionpeople() {
        return petitionpeople;
    }

    public void setPetitionpeople(String petitionpeople) {
        this.petitionpeople = petitionpeople == null ? null : petitionpeople.trim();
    }

    public String getPetitionpeopletelephone() {
        return petitionpeopletelephone;
    }

    public void setPetitionpeopletelephone(String petitionpeopletelephone) {
        this.petitionpeopletelephone = petitionpeopletelephone == null ? null : petitionpeopletelephone.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public String getPollutestarttime() {
        return pollutestarttime;
    }

    public void setPollutestarttime(String pollutestarttime) {
        this.pollutestarttime = pollutestarttime;
    }

    public String getPolluteendtime() {
        return polluteendtime;
    }

    public void setPolluteendtime(String polluteendtime) {
        this.polluteendtime = polluteendtime;
    }

    public Integer getDuration() {
        return Duration;
    }

    public void setDuration(Integer duration) {
        Duration = duration;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }
}
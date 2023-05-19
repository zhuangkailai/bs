
package com.tjpu.sp.model.environmentalprotection.parkprofile;


public class ParkProfileVO {

    private String pkid;

    private Integer pollutionnum;

    private Integer pollutionsettlednum;

    private Integer personnum;

    private Double area;

    private Double planingbuilland;

    private Double planingbuilingland;

    private Double buildproject;

    private String updatetime;

    private String updateuser;



    public void setpkid(String pkid) {
        this.pkid = pkid;
    }


    public String getpkid() {
        return "".equals(pkid)?null:pkid;
        
    }
    public void setpollutionnum(Integer pollutionnum) {
        this.pollutionnum = pollutionnum;
    }


    public Integer getpollutionnum() {
        return pollutionnum;
    }
    public void setpollutionsettlednum(Integer pollutionsettlednum) {
        this.pollutionsettlednum = pollutionsettlednum;
    }


    public Integer getpollutionsettlednum() {
        return pollutionsettlednum;
    }
    public void setpersonnum(Integer personnum) {
        this.personnum = personnum;
    }


    public Integer getpersonnum() {
        return personnum;
    }
    public void setarea(Double area) {
        this.area = area;
    }


    public Double getarea() {
        return area;
    }
    public void setplaningbuilland(Double planingbuilland) {
        this.planingbuilland = planingbuilland;
    }


    public Double getplaningbuilland() {
        return planingbuilland;
    }
    public void setplaningbuilingland(Double planingbuilingland) {
        this.planingbuilingland = planingbuilingland;
    }


    public Double getplaningbuilingland() {
        return planingbuilingland;
    }
    public void setbuildproject(Double buildproject) {
        this.buildproject = buildproject;
    }


    public Double getbuildproject() {
        return buildproject;
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

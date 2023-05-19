
package com.tjpu.sp.model.environmentalprotection.patroluserent;


public class PatrolUserEntVO {

    private String pkid;

    private String fkpollutionid;

    private String patrolteam;

    private String patroltime;

    private String fkpatrolpersonnelid;

    private String description;

    private String fkgroupleaderid;

    private String updatetime;

    private String updateuser;
    private String fkmonitorpointid;
    private String fkmonitorpointtypecode;

    public String getFkmonitorpointid() {
        return fkmonitorpointid;
    }

    public void setFkmonitorpointid(String fkmonitorpointid) {
        this.fkmonitorpointid = fkmonitorpointid;
    }

    public String getFkmonitorpointtypecode() {
        return fkmonitorpointtypecode;
    }

    public void setFkmonitorpointtypecode(String fkmonitorpointtypecode) {
        this.fkmonitorpointtypecode = fkmonitorpointtypecode;
    }

    public String getFkgroupleaderid() {
        return "".equals(fkgroupleaderid)?null:fkgroupleaderid;
    }

    public void setFkgroupleaderid(String fkgroupleaderid) {
        this.fkgroupleaderid = fkgroupleaderid;
    }

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
    public void setpatrolteam(String patrolteam) {
        this.patrolteam = patrolteam;
    }


    public String getpatrolteam() {
        return "".equals(patrolteam)?null:patrolteam;
        
    }
    public void setpatroltime(String patroltime) {
        this.patroltime = patroltime;
    }


    public String getpatroltime() {
        return "".equals(patroltime)?null:patroltime;
        
    }
    public void setfkpatrolpersonnelid(String fkpatrolpersonnelid) {
        this.fkpatrolpersonnelid = fkpatrolpersonnelid;
    }


    public String getfkpatrolpersonnelid() {
        return "".equals(fkpatrolpersonnelid)?null:fkpatrolpersonnelid;
        
    }
    public void setdescription(String description) {
        this.description = description;
    }


    public String getdescription() {
        return "".equals(description)?null:description;
        
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

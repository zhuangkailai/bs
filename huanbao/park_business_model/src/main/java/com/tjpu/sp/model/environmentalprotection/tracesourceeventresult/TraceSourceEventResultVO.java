
package com.tjpu.sp.model.environmentalprotection.tracesourceeventresult;


public class TraceSourceEventResultVO {

    private String pkid;

    private String fkpollutionid;

    private String fktracesourceeventid;

    private Double contributionratio;

    private String updateuser;

    private String updatetime;



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
    public void setfktracesourceeventid(String fktracesourceeventid) {
        this.fktracesourceeventid = fktracesourceeventid;
    }


    public String getfktracesourceeventid() {
        return "".equals(fktracesourceeventid)?null:fktracesourceeventid;
        
    }
    public void setcontributionratio(Double contributionratio) {
        this.contributionratio = contributionratio;
    }


    public Double getcontributionratio() {
        return contributionratio;
    }
    public void setupdateuser(String updateuser) {
        this.updateuser = updateuser;
    }


    public String getupdateuser() {
        return "".equals(updateuser)?null:updateuser;
        
    }
    public void setupdatetime(String updatetime) {
        this.updatetime = updatetime;
    }


    public String getupdatetime() {
        return "".equals(updatetime)?null:updatetime;
        
    }

}


package com.tjpu.sp.model.environmentalprotection.entemissioncontribution;


public class EntEmissionContributionVO {

    private String pkid;

    private String fkpollutionid;

    private Double contributionratio;

    private String fkpollutantcodes;

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
    public void setcontributionratio(Double contributionratio) {
        this.contributionratio = contributionratio;
    }


    public Double getcontributionratio() {
        return contributionratio;
    }
    public void setfkpollutantcodes(String fkpollutantcodes) {
        this.fkpollutantcodes = fkpollutantcodes;
    }


    public String getfkpollutantcodes() {
        return "".equals(fkpollutantcodes)?null:fkpollutantcodes;
        
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


package com.tjpu.sp.model.environmentalprotection.tracesamplesimilarity;


public class TraceSampleSimilarityVO {

    private String pkid;

    private String fktracesampleid;

    private String fkfingerprintid;

    private Double similarity;

    private String updateuser;

    private String updatetime;
    private String calculattype;
    private Double proportionsimilarity;

    public Double getProportionsimilarity() {
        return proportionsimilarity;
    }

    public void setProportionsimilarity(Double proportionsimilarity) {
        this.proportionsimilarity = proportionsimilarity;
    }

    public String getCalculattype() {
        return calculattype;
    }

    public void setCalculattype(String calculattype) {
        this.calculattype = calculattype;
    }

    public void setpkid(String pkid) {
        this.pkid = pkid;
    }


    public String getpkid() {
        return "".equals(pkid)?null:pkid;
        
    }
    public void setfktracesampleid(String fktracesampleid) {
        this.fktracesampleid = fktracesampleid;
    }


    public String getfktracesampleid() {
        return "".equals(fktracesampleid)?null:fktracesampleid;
        
    }
    public void setfkfingerprintid(String fkfingerprintid) {
        this.fkfingerprintid = fkfingerprintid;
    }


    public String getfkfingerprintid() {
        return "".equals(fkfingerprintid)?null:fkfingerprintid;
        
    }
    public void setsimilarity(Double similarity) {
        this.similarity = similarity;
    }


    public Double getsimilarity() {
        return similarity;
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


package com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpointset;


public class UseElectricFacilityMonitorPointSetVO {

    private String pkid;

    private String fkuseelectricfacilitymonitorpointid;

    private String fkpollutantcode;

    private Double minthreshold;

    private Double maxthreshold;

    private Integer alarmtype;

    private String updatetime;

    private String updateuser;



    public void setpkid(String pkid) {
        this.pkid = pkid;
    }


    public String getpkid() {
        return "".equals(pkid)?null:pkid;
        
    }
    public void setfkuseelectricfacilitymonitorpointid(String fkuseelectricfacilitymonitorpointid) {
        this.fkuseelectricfacilitymonitorpointid = fkuseelectricfacilitymonitorpointid;
    }


    public String getfkuseelectricfacilitymonitorpointid() {
        return "".equals(fkuseelectricfacilitymonitorpointid)?null:fkuseelectricfacilitymonitorpointid;
        
    }
    public void setfkpollutantcode(String fkpollutantcode) {
        this.fkpollutantcode = fkpollutantcode;
    }


    public String getfkpollutantcode() {
        return "".equals(fkpollutantcode)?null:fkpollutantcode;
        
    }
    public void setminthreshold(Double minthreshold) {
        this.minthreshold = minthreshold;
    }


    public Double getminthreshold() {
        return minthreshold;
    }
    public void setmaxthreshold(Double maxthreshold) {
        this.maxthreshold = maxthreshold;
    }


    public Double getmaxthreshold() {
        return maxthreshold;
    }
    public void setalarmtype(Integer alarmtype) {
        this.alarmtype = alarmtype;
    }


    public Integer getalarmtype() {
        return alarmtype;
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

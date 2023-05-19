
package com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpoint;


public class UseElectricFacilityMonitorPointVO {

    private String pkid;

    private String fkuseelectricfacilityid;

    private String monitorpointname;

    private Double poweronoffthreshold;

    private Double loadonoffthreshold;

    private Integer monitorstatus;

    private String dgimn;

    private String installposition;

    private String updatetime;

    private String updateuser;


    public Double getPoweronoffthreshold() {
        return poweronoffthreshold;
    }

    public void setPoweronoffthreshold(Double poweronoffthreshold) {
        this.poweronoffthreshold = poweronoffthreshold;
    }

    public Double getLoadonoffthreshold() {
        return loadonoffthreshold;
    }

    public void setLoadonoffthreshold(Double loadonoffthreshold) {
        this.loadonoffthreshold = loadonoffthreshold;
    }

    public void setpkid(String pkid) {
        this.pkid = pkid;
    }


    public String getpkid() {
        return "".equals(pkid)?null:pkid;
        
    }
    public void setfkuseelectricfacilityid(String fkuseelectricfacilityid) {
        this.fkuseelectricfacilityid = fkuseelectricfacilityid;
    }


    public String getfkuseelectricfacilityid() {
        return "".equals(fkuseelectricfacilityid)?null:fkuseelectricfacilityid;
        
    }
    public void setmonitorpointname(String monitorpointname) {
        this.monitorpointname = monitorpointname;
    }


    public String getmonitorpointname() {
        return "".equals(monitorpointname)?null:monitorpointname;
        
    }
    public void setmonitorstatus(Integer monitorstatus) {
        this.monitorstatus = monitorstatus;
    }


    public Integer getmonitorstatus() {
        return monitorstatus;
    }
    public void setdgimn(String dgimn) {
        this.dgimn = dgimn;
    }


    public String getdgimn() {
        return "".equals(dgimn)?null:dgimn;
        
    }
    public void setinstallposition(String installposition) {
        this.installposition = installposition;
    }


    public String getinstallposition() {
        return "".equals(installposition)?null:installposition;
        
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

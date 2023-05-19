
package com.tjpu.sp.model.environmentalprotection.useelectricfacility;


public class UseElectricFacilityVO {

    private String pkid;

    private String fkpollutionid;

    private String fkcontrolpollutionfacilityid;

    private String equipmentname;

    private String production;

    private Double equipmentcapacity;

    private String putintodate;

    private Double poweronoffthreshold;

    private Double loadonoffthreshold;

    private Integer facilitytype;

    private String installposition;

    private String updatetime;

    private String updateuser;



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
    public void setfkcontrolpollutionfacilityid(String fkcontrolpollutionfacilityid) {
        this.fkcontrolpollutionfacilityid = fkcontrolpollutionfacilityid;
    }


    public String getfkcontrolpollutionfacilityid() {
        return "".equals(fkcontrolpollutionfacilityid)?null:fkcontrolpollutionfacilityid;
        
    }
    public void setequipmentname(String equipmentname) {
        this.equipmentname = equipmentname;
    }


    public String getequipmentname() {
        return "".equals(equipmentname)?null:equipmentname;
        
    }
    public void setproduction(String production) {
        this.production = production;
    }


    public String getproduction() {
        return "".equals(production)?null:production;
        
    }
    public void setequipmentcapacity(Double equipmentcapacity) {
        this.equipmentcapacity = equipmentcapacity;
    }


    public Double getequipmentcapacity() {
        return equipmentcapacity;
    }
    public void setputintodate(String putintodate) {
        this.putintodate = putintodate;
    }


    public String getputintodate() {
        return "".equals(putintodate)?null:putintodate;
        
    }
    public void setpoweronoffthreshold(Double poweronoffthreshold) {
        this.poweronoffthreshold = poweronoffthreshold;
    }


    public Double getpoweronoffthreshold() {
        return poweronoffthreshold;
    }
    public void setloadonoffthreshold(Double loadonoffthreshold) {
        this.loadonoffthreshold = loadonoffthreshold;
    }


    public Double getloadonoffthreshold() {
        return loadonoffthreshold;
    }
    public void setfacilitytype(Integer facilitytype) {
        this.facilitytype = facilitytype;
    }


    public Integer getfacilitytype() {
        return facilitytype;
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

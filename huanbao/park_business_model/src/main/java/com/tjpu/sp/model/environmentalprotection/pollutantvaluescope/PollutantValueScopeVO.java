
package com.tjpu.sp.model.environmentalprotection.pollutantvaluescope;


public class PollutantValueScopeVO {

    private String pkid;

    private String fkpollutantcode;

    private String valuescope;

    private Integer orderindex;

    private String remark;

    private String updateuser;

    private String updatetime;



    public void setpkid(String pkid) {
        this.pkid = pkid;
    }


    public String getpkid() {
        return "".equals(pkid)?null:pkid;
        
    }
    public void setfkpollutantcode(String fkpollutantcode) {
        this.fkpollutantcode = fkpollutantcode;
    }


    public String getfkpollutantcode() {
        return "".equals(fkpollutantcode)?null:fkpollutantcode;
        
    }
    public void setvaluescope(String valuescope) {
        this.valuescope = valuescope;
    }


    public String getvaluescope() {
        return "".equals(valuescope)?null:valuescope;
        
    }
    public void setorderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }


    public Integer getorderindex() {
        return orderindex;
    }
    public void setremark(String remark) {
        this.remark = remark;
    }


    public String getremark() {
        return "".equals(remark)?null:remark;
        
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

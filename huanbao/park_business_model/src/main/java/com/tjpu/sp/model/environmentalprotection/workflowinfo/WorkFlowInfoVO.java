
package com.tjpu.sp.model.environmentalprotection.workflowinfo;


public class WorkFlowInfoVO {

    private String pkid;

    private String fkworkflowtype;

    private String workflowxml;

    private String createuser;

    private String createtime;

    private String updateuser;

    private String updatetime;



    public void setpkid(String pkid) {
        this.pkid = pkid;
    }


    public String getpkid() {
        return "".equals(pkid)?null:pkid;
        
    }

    public void setfkworkflowtype(String fkworkflowtype) {
        this.fkworkflowtype = fkworkflowtype;
    }


    public String getfkworkflowtype() {
        return "".equals(fkworkflowtype)?null:fkworkflowtype;
        
    }

    public String getWorkflowxml() {
        return workflowxml;
    }

    public void setWorkflowxml(String workflowxml) {
        this.workflowxml = workflowxml;
    }

    public void setcreateuser(String createuser) {
        this.createuser = createuser;
    }


    public String getcreateuser() {
        return "".equals(createuser)?null:createuser;
        
    }
    public void setcreatetime(String createtime) {
        this.createtime = createtime;
    }


    public String getcreatetime() {
        return "".equals(createtime)?null:createtime;
        
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

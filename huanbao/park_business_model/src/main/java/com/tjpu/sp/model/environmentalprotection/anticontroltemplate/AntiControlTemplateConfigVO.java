package com.tjpu.sp.model.environmentalprotection.anticontroltemplate;

public class AntiControlTemplateConfigVO {
    private String pkTemplateid;

    private String antcontrolcommand;

    private String templateformat;

    private Integer orderindex;

    private String commandexplain;

    public String getPkTemplateid() {
        return pkTemplateid;
    }

    public void setPkTemplateid(String pkTemplateid) {
        this.pkTemplateid = pkTemplateid == null ? null : pkTemplateid.trim();
    }

    public String getAntcontrolcommand() {
        return antcontrolcommand;
    }

    public void setAntcontrolcommand(String antcontrolcommand) {
        this.antcontrolcommand = antcontrolcommand == null ? null : antcontrolcommand.trim();
    }

    public String getTemplateformat() {
        return templateformat;
    }

    public void setTemplateformat(String templateformat) {
        this.templateformat = templateformat == null ? null : templateformat.trim();
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }

    public String getCommandexplain() {
        return commandexplain;
    }

    public void setCommandexplain(String commandexplain) {
        this.commandexplain = commandexplain;
    }
}
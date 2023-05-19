package com.tjpu.sp.model.common.knowledge;

import java.util.Date;

public class TrainInfoVO {
    private String pkId;

    private String traintitle;

    private String fkTraintypecode;

    private String trainpeople;

    private String traindes;

    private String fkFileid;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getTraintitle() {
        return traintitle;
    }

    public void setTraintitle(String traintitle) {
        this.traintitle = traintitle == null ? null : traintitle.trim();
    }

    public String getFkTraintypecode() {
        return fkTraintypecode;
    }

    public void setFkTraintypecode(String fkTraintypecode) {
        this.fkTraintypecode = fkTraintypecode == null ? null : fkTraintypecode.trim();
    }

    public String getTrainpeople() {
        return trainpeople;
    }

    public void setTrainpeople(String trainpeople) {
        this.trainpeople = trainpeople == null ? null : trainpeople.trim();
    }

    public String getTraindes() {
        return traindes;
    }

    public void setTraindes(String traindes) {
        this.traindes = traindes == null ? null : traindes.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }
}
package com.tjpu.sp.model.environmentalprotection.patrol;

import java.util.Date;
import java.util.List;

public class PatrolTeamVO {
    private String pkId;

    private String teamname;

    private String fkUserid;

    private String remark;

    private Date updatetime;

    private String updateuser;

    private String fkReviewerid;

    private List<PatrolTeamUserVO> patrolTeamUserVOS;
    private List<PatrolTeamEntOrPointVO> patrolTeamEntOrPointVOS;


    public List<PatrolTeamUserVO> getPatrolTeamUserVOS() {
        return patrolTeamUserVOS;
    }

    public void setPatrolTeamUserVOS(List<PatrolTeamUserVO> patrolTeamUserVOS) {
        this.patrolTeamUserVOS = patrolTeamUserVOS;
    }

    public List<PatrolTeamEntOrPointVO> getPatrolTeamEntOrPointVOS() {
        return patrolTeamEntOrPointVOS;
    }

    public void setPatrolTeamEntOrPointVOS(List<PatrolTeamEntOrPointVO> patrolTeamEntOrPointVOS) {
        this.patrolTeamEntOrPointVOS = patrolTeamEntOrPointVOS;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getTeamname() {
        return teamname;
    }

    public void setTeamname(String teamname) {
        this.teamname = teamname == null ? null : teamname.trim();
    }

    public String getFkUserid() {
        return fkUserid;
    }

    public void setFkUserid(String fkUserid) {
        this.fkUserid = fkUserid == null ? null : fkUserid.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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

    public String getFkReviewerid() {
        return fkReviewerid;
    }

    public void setFkReviewerid(String fkReviewerid) {
        this.fkReviewerid = fkReviewerid;
    }
}
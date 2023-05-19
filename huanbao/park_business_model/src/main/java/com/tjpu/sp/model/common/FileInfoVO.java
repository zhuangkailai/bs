package com.tjpu.sp.model.common;

import java.util.Date;

public class FileInfoVO {
    private String pkFileid;

    private String originalfilename;

    private String filename;

    private long filesize;

    private String fileextname;

    private String uploaduser;

    private Date uploadtime;

    private Integer businesstype;

    private String fileflag;

    private String filepath;

    private String businessfiletype;


    public String getPkFileid() {
        return pkFileid;
    }

    public void setPkFileid(String pkFileid) {
        this.pkFileid = pkFileid == null ? null : pkFileid.trim();
    }

    public String getOriginalfilename() {
        return originalfilename;
    }

    public void setOriginalfilename(String originalfilename) {
        this.originalfilename = originalfilename == null ? null : originalfilename.trim();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename == null ? null : filename.trim();
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public String getFileextname() {
        return fileextname;
    }

    public void setFileextname(String fileextname) {
        this.fileextname = fileextname == null ? null : fileextname.trim();
    }

    public String getUploaduser() {
        return uploaduser;
    }

    public void setUploaduser(String uploaduser) {
        this.uploaduser = uploaduser == null ? null : uploaduser.trim();
    }

    public Date getUploadtime() {
        return uploadtime;
    }

    public void setUploadtime(Date uploadtime) {
        this.uploadtime = uploadtime;
    }

    public Integer getBusinesstype() {
        return businesstype;
    }

    public void setBusinesstype(Integer businesstype) {
        this.businesstype = businesstype;
    }

    public String getFileflag() {
        return fileflag;
    }

    public void setFileflag(String fileflag) {
        this.fileflag = fileflag == null ? null : fileflag.trim();
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath == null ? null : filepath.trim();
    }

    public String getBusinessfiletype() {
        return businessfiletype;
    }

    public void setBusinessfiletype(String businessfiletype) {
        this.businessfiletype = businessfiletype == null ? null : businessfiletype.trim();
    }
}
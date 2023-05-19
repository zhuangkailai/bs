package com.tjpu.sp.model.common;


import java.util.ArrayList;
import java.util.List;

public class PageEntity<T> {
    //当前页码
    private int pageNum;
    //单页记录数
    private int pageSize;
    //总页数
    private int pageCount;
    //总记录数
    private long totalCount;
    //分页数据
    private List<T> listItems = new ArrayList<>();

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getListItems() {
        return listItems;
    }

    public void setListItems(List<T> listItems) {
        this.listItems = listItems;
    }
}

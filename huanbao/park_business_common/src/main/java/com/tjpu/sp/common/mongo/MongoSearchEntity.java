package com.tjpu.sp.common.mongo;

import java.io.Serializable;
import java.util.List;

/**
 * @author: chengzq
 * @date: 2018/8/27 0027 上午 11:54
 * @Description:  分页查询类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
public class MongoSearchEntity implements Serializable {

    /** 第一页 */
    private int page = 1;
    private int size = Integer.MAX_VALUE;
    private List<String> sortname;
    private Integer sortorder;


    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String> getSortname() {
        return sortname;
    }

    public void setSortname(List<String> sortname) {
        this.sortname = sortname;
    }

    public Integer getSortorder() {
        return sortorder;
    }

    public void setSortorder(Integer sortorder) {
        this.sortorder = sortorder;
    }
}
package com.pub.dao;

import com.pub.model.OperateLogVO;
import org.springframework.stereotype.Repository;

@Repository
public interface OperateLogMapper {

    int insert(OperateLogVO record);

}
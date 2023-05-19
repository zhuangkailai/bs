package com.tjpu.sp.dao.environmentalprotection.knowledgestore;


import com.tjpu.sp.model.base.knowledgestore.StoreType;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreTypeMapper {
    int deleteByPrimaryKey(Integer pkId);

    int insert(StoreType record);

    int insertSelective(StoreType record);

    StoreType selectByPrimaryKey(Integer pkId);

    int updateByPrimaryKeySelective(StoreType record);

    int updateByPrimaryKey(StoreType record);
}
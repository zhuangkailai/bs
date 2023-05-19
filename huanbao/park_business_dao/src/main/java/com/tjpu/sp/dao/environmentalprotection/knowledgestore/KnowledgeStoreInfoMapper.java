package com.tjpu.sp.dao.environmentalprotection.knowledgestore;


import com.tjpu.sp.model.base.knowledgestore.KnowledgeStoreInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface KnowledgeStoreInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(KnowledgeStoreInfo record);

    int insertSelective(KnowledgeStoreInfo record);

    KnowledgeStoreInfo selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(KnowledgeStoreInfo record);

    int updateByPrimaryKey(KnowledgeStoreInfo record);

    List<Map<String,Object>> getKnowledgeStoresByParam(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/9/3 16:48
     * @Description: 获取知识库类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getKnowledgeStoresType();

    List<Map<String,Object>> countKnowledgeStoreGroupByStoreType();
}
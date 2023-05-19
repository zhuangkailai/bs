package com.tjpu.sp.service.environmentalprotection.knowledgestore;

import com.tjpu.sp.model.base.knowledgestore.KnowledgeStoreInfo;

import java.util.List;
import java.util.Map;

public interface KnowledgeStoreService {
    int deleteByPrimaryKey(String pkId);

    int insertSelective(KnowledgeStoreInfo record);

    int updateByPrimaryKeySelective(KnowledgeStoreInfo record);

    Map<String, Object> getKnowledgeStoresByParam(Map<String, Object> paramMap, Integer pageSize, Integer pageNum);
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

    List<Map<String,Object>> getKnowledgeStoresDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countKnowledgeStoreGroupByStoreType();
}

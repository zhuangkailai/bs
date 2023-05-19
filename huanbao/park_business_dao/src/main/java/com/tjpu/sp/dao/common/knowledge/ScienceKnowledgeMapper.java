package com.tjpu.sp.dao.common.knowledge;

import com.tjpu.sp.model.common.knowledge.ScienceKnowledgeVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ScienceKnowledgeMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(ScienceKnowledgeVO record);

    int insertSelective(ScienceKnowledgeVO record);

    ScienceKnowledgeVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(ScienceKnowledgeVO record);

    int updateByPrimaryKey(ScienceKnowledgeVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    Map<String, Object> getEditOrDetailsDataById(String id);
}
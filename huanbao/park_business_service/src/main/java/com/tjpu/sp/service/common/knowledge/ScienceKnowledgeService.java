package com.tjpu.sp.service.common.knowledge;


import com.tjpu.sp.model.common.knowledge.ScienceKnowledgeVO;
import java.util.List;
import java.util.Map;

public interface ScienceKnowledgeService {


    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void updateInfo(ScienceKnowledgeVO scienceKnowledgeVO);

    void insertInfo(ScienceKnowledgeVO scienceKnowledgeVO);

    void deleteInfoById(String id);

    Map<String, Object> getEditOrDetailsDataById(String id);
}

package com.tjpu.sp.service.common.standard;


import com.tjpu.sp.model.common.standard.StandardInfoVO;

import java.util.List;
import java.util.Map;

public interface StandardInfoService {


    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void updateInfo(StandardInfoVO standardInfoVO);

    void insertInfo(StandardInfoVO standardInfoVO);

    void deleteInfoById(String id);

    List<Map<String, Object>> countKnowledgeData();

    Map<String, Object> getEditOrDetailsDataById(String id);
}

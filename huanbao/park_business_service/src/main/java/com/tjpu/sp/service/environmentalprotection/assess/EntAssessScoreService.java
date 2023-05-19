package com.tjpu.sp.service.environmentalprotection.assess;

import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentInfoVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface EntAssessScoreService {


    List<Map<String, Object>> getEntAssessInfoListByParam(JSONObject jsonObject);

    void updateInfo(EntAssessmentInfoVO entAssessmentInfoVO);

    void insertInfo(EntAssessmentInfoVO entAssessmentInfoVO);

    void deleteById(String id);

    List<Map<String, Object>>  getAddItemDataList();

    Map<String, Object> getEditOrViewDataById(String id);
}

package com.tjpu.sp.service.common.emergency;


import com.tjpu.sp.model.common.emergency.EmergencyCaseInfoVO;
import com.tjpu.sp.model.common.standard.StandardInfoVO;

import java.util.List;
import java.util.Map;

public interface EmergencyCaseInfoService {


    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void updateInfo(EmergencyCaseInfoVO emergencyCaseInfoVO);

    void insertInfo(EmergencyCaseInfoVO emergencyCaseInfoVO);

    void deleteInfoById(String id);
    Map<String, Object> getEditOrDetailsDataById(String id);
}

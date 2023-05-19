package com.tjpu.sp.service.common.knowledge;
import com.tjpu.sp.model.common.knowledge.TrainInfoVO;
import com.tjpu.sp.model.common.knowledge.TrainUserInfoVO;

import java.util.List;
import java.util.Map;

public interface TrainInfoService {


    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void updateInfo(TrainInfoVO trainInfoVO);

    void insertInfo(TrainInfoVO trainInfoVO);

    void deleteInfoById(String id);

    Map<String, Object> getEditDataById(String id);

    List<Map<String, Object>> getStudyUserListById(String id);

    void insertUserInfo(TrainUserInfoVO trainUserInfoVO);
}

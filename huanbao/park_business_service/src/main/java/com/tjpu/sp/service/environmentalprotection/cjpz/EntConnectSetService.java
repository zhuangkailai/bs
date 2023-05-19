package com.tjpu.sp.service.environmentalprotection.cjpz;


import com.tjpu.sp.model.environmentalprotection.cjpz.EntConnectSetVO;
import com.tjpu.sp.model.environmentalprotection.cjpz.PointAddressSetVO;

import java.util.List;
import java.util.Map;

public interface EntConnectSetService {


    List<Map<String,Object>> getEntConnectSetsByParamMap(Map<String, Object> paramMap);

    void insert(EntConnectSetVO entConnectSetVO);

    EntConnectSetVO selectByPrimaryKey(String id);

    void updateByPrimaryKey(EntConnectSetVO entConnectSetVO);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getEntConnectSetDetailByID(String id);

    void insertEntityAndSetData(EntConnectSetVO entConnectSetVO, List<PointAddressSetVO> paramList);

    long getEntConnectSetNumByParamMap(Map<String, Object> paramMap);

    void updateEntityAndSetData(EntConnectSetVO entConnectSetVO, List<PointAddressSetVO> paramList);

    List<Map<String,Object>> getSecurityPointTreeData(Map<String, Object> param);

    List<Map<String,Object>> getPointAddressSetsByEntConnectSetID(Map<String, Object> paramMap);
}

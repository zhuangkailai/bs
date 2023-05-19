package com.tjpu.sp.service.environmentalprotection.devopsinfo;


import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsExplainVO;

import java.util.List;
import java.util.Map;

public interface EntDevOpsExplainService {

    /**
     * @author: xsm
     * @date: 2021/05/26 0026 上午 11:44
     * @Description: 根据自定义参数获取企业运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getEntDevOpsExplainsByParamMap(Map<String, Object> paramMap);


    void addEntDevOpsExplain(EntDevOpsExplainVO entity);

    Map<String,Object> getEntDevOpsExplainDetailByID(String id);

    List<Map<String,Object>> getDeviceAndEntDevOpsExplainsByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getOnePointEntDevOpsExplainsByParamMap(Map<String, Object> paramMap);

   void deleteEntDevOpsExplainByID(String id);

    void editDevOpsExplain(EntDevOpsExplainVO obj);

    Map<String,Object> getEntDevOpsExplainUpdateDataByID(String id);
}

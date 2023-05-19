package com.tjpu.sp.service.common.pubcode;

import java.util.List;
import java.util.Map;

/**
 * @author: chengzq
 * @date: 2021/3/10 0010 13:21
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public interface MonitorPointCommonService {

    /**
     * @author: chengzq
     * @date: 2021/3/30 0030 上午 8:59
     * @Description: 通过自定义条件获取所有环保监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getOutPutInfosByParamMap(Map<String, Object> paramMap);

}

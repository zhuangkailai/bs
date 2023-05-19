package com.tjpu.sp.service.base.output;


import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface UserMonitorPointRelationDataService {


    /**
     * @author: chengzq
     * @date: 2020/4/20 0020 下午 12:08
     * @Description: 从查询出的监测点里筛选拥有权限的监测点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void ExcludeNoAuthDGIMNByParamMap(Collection<String> mns);

    /**
     * @author: chengzq
     * @date: 2020/4/21 0021 下午 12:18
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [ParamMap]
     * @throws:
     */
    List<Map<String,Object>> getDGIMNByParamMap(Map<String,Object> ParamMap);

    /**
     * @author: xsm
     * @date: 2020/9/03 0003 下午 15:20
     * @Description:根据MN和类型批量修改数据权限MN
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updataUserMonitorPointRelationDataByMnAndType(String oldmn, String newmn, String monitorpointtype);

    /**
     * @author: xsm
     * @date: 2020/9/03 0003 下午 15:20
     * @Description:根据MN和类型批量删除该MN权限数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void deleteUserMonitorPointRelationDataByMnAndType(String dgimn, String monitorpointtype);

    List<String> getUserIdByParamMap(Map<String, Object> parammap);

    void addUserMonitorPointRelation(Map<String, Object> deletemap, List<UserMonitorPointRelationDataVO> list);

    void deleteByParamMap(Map<String, Object> deletemap);

    List<String> getMonitorPointIDsByUserid(String userid);
}

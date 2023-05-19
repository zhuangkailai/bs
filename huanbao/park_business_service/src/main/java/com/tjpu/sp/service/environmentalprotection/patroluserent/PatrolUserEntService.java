package com.tjpu.sp.service.environmentalprotection.patroluserent;


import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;
import com.tjpu.sp.model.environmentalprotection.patroluserent.PatrolUserEntVO;

import java.util.List;
import java.util.Map;

public interface PatrolUserEntService {

    int deleteByPrimaryKey(String pkId);

    int deleteBydeletMap(Map<String,Object> deleteMap,Map<String,Object> monitorpointdeletMap);

    int update(Map<String,Object> deletMap,Map<String,Object> monitordeletMap, List<PatrolUserEntVO> patroluserent, List<UserMonitorPointRelationDataVO> monitorpointrelation);

    int insert(Map<String, Object> deletMap,Map<String, Object> monitordeletMap, List<PatrolUserEntVO> patroluserent, List<UserMonitorPointRelationDataVO> monitorpointrelation);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(PatrolUserEntVO record);

    /**
     * @author: chengzq
     * @date: 2020/04/29 0016 下午 2:37
     * @Description:  通过自定义参数获取巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPatrolUserEntByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/04/29 0016 下午 2:37
     * @Description:  通过id获取巡查人员分配详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> getPatrolUserEntDetailByID(String pkid);

    /**
     * @author: chengzq
     * @date: 2020/4/30 0030 上午 10:23
     * @Description: 通过自定义参数获取企业下巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPatrolUserEntPatroTeamByParamMap(Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2020/04/30 0030 上午 11:47
     * @Description:  通过污染源id获取巡查人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<String> getPatrolPersonnelIdsByPolluionid(Map<String,Object> param);

    /**
     * @author: xsm
     * @date: 2020/04/30 0030 上午 11:47
     * @Description:  修改巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updatePollutionPatrolUserEnt(List<String> newuserids,String username,String pollutionid,List<PatrolUserEntVO> objlist, List<String> patrolpersonnelids);

    void updateMonitorpointPatrolUserEnt(List<String> newuserids,String username,PatrolUserEntVO entity,List<PatrolUserEntVO> objlist, List<String> patrolpersonnelids,String dgimn );

    /**
     * @author: xsm
     * @date: 2020/04/30 0030 上午 11:47
     * @Description:  删除巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void deletePollutionPatrolUserEntByID(String id, List<String> patrolpersonnelids,String patroltime);

    void deleteMonitiorpointPatrolUserEntByID(String id, List<String> patrolpersonnelids, String patroltime,String patrolteam,String monitorpointtype);

    void deleteByPollutionIDAndPatrolTime(Map<String, Object> param);


    /**
     * @author: chengzq
     * @date: 2020/9/14 0014 下午 3:36
     * @Description: 通过自定义条件统计巡查人员个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    Integer countPatrolUserEntByParams(Map<String,Object> param);

    List<Map<String,Object>> getMonitorPatrolUserEntByParamMap(Map<String, Object> paramMap);


    List<Map<String, Object>> getPatrolUserEntByParams(Map<String, Object> paramMap);


    List<Map<String,Object>> getPatrolPersonnelIdsByMonitorPointID(Map<String, Object> param);


    List<String> getPatrolPersonnelIdsByPointid(Map<String, Object> param);
}

package com.tjpu.sp.dao.environmentalprotection.patroluserent;

import com.tjpu.sp.model.environmentalprotection.patroluserent.PatrolUserEntVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface PatrolUserEntMapper {
    int deleteByPrimaryKey(String pkId);

    int deleteByPatrolTeam(Map<String,Object> paramMap);

    int insert(PatrolUserEntVO record);

    int insertBatch(List<PatrolUserEntVO> record);

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

    List<Map<String,Object>> getMonitorPatrolUserEntByParamMap(Map<String, Object> paramMap);

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
    List<Map<String,Object>> getPatrolPersonnelIdsByPolluionid(Map<String,Object> param);

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

    void deleteByPollutionID(String fkpollutionid);

    void deleteByPollutionIDAndPatrolTime(Map<String, Object> paramMap);

    List<Map<String,Object>> getPatrolPersonnelIdsByMonitorPointID(Map<String, Object> param);

    void deleteByMonitorpointInfo(Map<String, Object> paramMap);

    List<Map<String, Object>> getPatrolUserEntByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getPatrolPersonnelIdsByPointid(Map<String, Object> param);
}
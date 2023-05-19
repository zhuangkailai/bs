package com.tjpu.sp.service.impl.environmentalprotection.patroluserent;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.output.UserMonitorPointRelationDataMapper;
import com.tjpu.sp.dao.environmentalprotection.patroluserent.PatrolUserEntMapper;
import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;
import com.tjpu.sp.model.environmentalprotection.patroluserent.PatrolUserEntVO;
import com.tjpu.sp.service.environmentalprotection.patroluserent.PatrolUserEntService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class PatrolUserEntServiceImpl implements PatrolUserEntService {

    @Autowired
    private PatrolUserEntMapper patrolUserEntMapper;
    @Autowired
    private UserMonitorPointRelationDataMapper userMonitorPointRelationDataMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return patrolUserEntMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int deleteBydeletMap(Map<String, Object> deletMap, Map<String, Object> monitorpointdeletMap) {
        List<Map<String, Object>> patrolUserEntByParams = patrolUserEntMapper.getPatrolUserEntByParams(deletMap);
        List<String> FK_PollutionId = patrolUserEntByParams.stream().filter(m -> m.get("FK_PollutionId") != null).map(m -> m.get("FK_PollutionId").toString()).distinct().collect(Collectors.toList());
        List<Map<String, Object>> collect = patrolUserEntByParams.stream().filter(m -> m.get("Fk_MonitorPointID") != null && m.get("FK_MonitorPointTypeCode") != null).peek(m -> {
            m.put("monitorpointid", m.get("Fk_MonitorPointID"));
            m.put("monitorpointtype", m.get("FK_MonitorPointTypeCode"));
            m.put("patrolteam", deletMap.get("patrolteam"));
            m.put("patroltime", deletMap.get("patroltime"));
        }).collect(Collectors.toList());
        List<String> FK_PatrolPersonnelId = patrolUserEntByParams.stream().filter(m -> m.get("FK_PatrolPersonnelId") != null).map(m -> m.get("FK_PatrolPersonnelId").toString()).distinct().collect(Collectors.toList());
        List<String> FK_GroupLeaderId = patrolUserEntByParams.stream().filter(m -> m.get("FK_GroupLeaderId") != null).map(m -> m.get("FK_GroupLeaderId").toString()).distinct().collect(Collectors.toList());
        FK_PatrolPersonnelId.addAll(FK_GroupLeaderId);
        deletMap.put("userids", FK_PatrolPersonnelId);
        deletMap.put("pollutionids", FK_PollutionId);
        monitorpointdeletMap.put("monitorpoints", collect);
        monitorpointdeletMap.put("userids", FK_PatrolPersonnelId);
        userMonitorPointRelationDataMapper.deleteByParamMap(deletMap);
        userMonitorPointRelationDataMapper.deleteByParamMap(monitorpointdeletMap);
        patrolUserEntMapper.deleteByMonitorpointInfo(monitorpointdeletMap);
        return patrolUserEntMapper.deleteByPatrolTeam(deletMap);
    }

    @Override
    public int update(Map<String, Object> deletMap, Map<String, Object> monitordeletMap, List<PatrolUserEntVO> patroluserent, List<UserMonitorPointRelationDataVO> monitorpointrelation) {
        List<Map<String, Object>> patrolUserEntByParams = patrolUserEntMapper.getPatrolUserEntByParams(deletMap);
        List<String> FK_PollutionId = patrolUserEntByParams.stream().filter(m -> m.get("FK_PollutionId") != null).map(m -> m.get("FK_PollutionId").toString()).distinct().collect(Collectors.toList());
        List<Map<String, Object>> collect = patrolUserEntByParams.stream().filter(m -> m.get("Fk_MonitorPointID") != null && m.get("FK_MonitorPointTypeCode") != null).peek(m -> {
            m.put("monitorpointid", m.get("Fk_MonitorPointID"));
            m.put("monitorpointtype", m.get("FK_MonitorPointTypeCode"));
            m.put("patrolteam", deletMap.get("patrolteam"));
            m.put("patroltime", deletMap.get("patroltime"));
        }).collect(Collectors.toList());
        List<String> FK_PatrolPersonnelId = patrolUserEntByParams.stream().filter(m -> m.get("FK_PatrolPersonnelId") != null).map(m -> m.get("FK_PatrolPersonnelId").toString()).distinct().collect(Collectors.toList());
        List<String> FK_GroupLeaderId = patrolUserEntByParams.stream().filter(m -> m.get("FK_GroupLeaderId") != null).map(m -> m.get("FK_GroupLeaderId").toString()).distinct().collect(Collectors.toList());
        FK_PatrolPersonnelId.addAll(FK_GroupLeaderId);
        deletMap.put("userids", FK_PatrolPersonnelId);
        deletMap.put("pollutionids", FK_PollutionId);
        monitordeletMap.put("monitorpoints", collect);
        monitordeletMap.put("userids", FK_PatrolPersonnelId);
        userMonitorPointRelationDataMapper.deleteByParamMap(deletMap);
        userMonitorPointRelationDataMapper.deleteByParamMap(monitordeletMap);
        patrolUserEntMapper.deleteByMonitorpointInfo(deletMap);
        patrolUserEntMapper.deleteByPatrolTeam(deletMap);


        userMonitorPointRelationDataMapper.batchAdd(monitorpointrelation);
        return patrolUserEntMapper.insertBatch(patroluserent);
    }

    @Override
    public int insert(Map<String, Object> deletMap, Map<String, Object> monitordeletMap, List<PatrolUserEntVO> patroluserent, List<UserMonitorPointRelationDataVO> monitorpointrelation) {
        //如果有不添加
        List<UserMonitorPointRelationDataVO> userMonitorPointRelationData = userMonitorPointRelationDataMapper.getUserMonitorPointRelationData(new HashMap<>());
        Iterator<UserMonitorPointRelationDataVO> iterator = monitorpointrelation.iterator();
        while (iterator.hasNext()) {
            UserMonitorPointRelationDataVO next = iterator.next();
            String dgimn = next.getDgimn();
            String fkUserid = next.getFkUserid();
            for (UserMonitorPointRelationDataVO userMonitorPointRelationDatum : userMonitorPointRelationData) {
                if (dgimn.equals(userMonitorPointRelationDatum.getDgimn()) && fkUserid.equals(userMonitorPointRelationDatum.getFkUserid())) {
                    iterator.remove();
                }
            }
        }

        List<Map<String, Object>> patrolUserEntByParams = patrolUserEntMapper.getPatrolUserEntByParams(deletMap);
        List<String> FK_PollutionId = patrolUserEntByParams.stream().filter(m -> m.get("FK_PollutionId") != null).map(m -> m.get("FK_PollutionId").toString()).distinct().collect(Collectors.toList());
        List<Map<String, Object>> collect = patrolUserEntByParams.stream().filter(m -> m.get("Fk_MonitorPointID") != null && m.get("FK_MonitorPointTypeCode") != null).collect(Collectors.toList());
        List<String> FK_PatrolPersonnelId = patrolUserEntByParams.stream().filter(m -> m.get("FK_PatrolPersonnelId") != null).map(m -> m.get("FK_PatrolPersonnelId").toString()).distinct().collect(Collectors.toList());
        List<String> FK_GroupLeaderId = patrolUserEntByParams.stream().filter(m -> m.get("FK_GroupLeaderId") != null).map(m -> m.get("FK_GroupLeaderId").toString()).distinct().collect(Collectors.toList());
        FK_PatrolPersonnelId.addAll(FK_GroupLeaderId);
        deletMap.put("userids", FK_PatrolPersonnelId);
        deletMap.put("pollutionids", FK_PollutionId);
        monitordeletMap.put("monitorpoints", collect);
        monitordeletMap.put("userids", FK_PatrolPersonnelId);
        userMonitorPointRelationDataMapper.deleteByParamMap(deletMap);//删除企业相关
        userMonitorPointRelationDataMapper.deleteByParamMap(monitordeletMap);//删除监测点相关
        userMonitorPointRelationDataMapper.batchAdd(monitorpointrelation);
        return patrolUserEntMapper.insertBatch(patroluserent);
    }

    @Override
    public Map<String, Object> selectByPrimaryKey(String pkId) {
        return patrolUserEntMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(PatrolUserEntVO record) {
        return patrolUserEntMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2020/04/29 0016 下午 2:38
     * @Description: 通过自定义参数获取巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPatrolUserEntByParamMap(Map<String, Object> paramMap) {
        return patrolUserEntMapper.getPatrolUserEntByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/04/29 0016 下午 2:38
     * @Description: 通过id获取巡查人员分配详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String, Object> getPatrolUserEntDetailByID(String pkid) {
        return patrolUserEntMapper.selectByPrimaryKey(pkid);
    }


    /**
     * @author: chengzq
     * @date: 2020/4/30 0030 上午 10:24
     * @Description: 通过自定义参数获取企业下巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPatrolUserEntPatroTeamByParamMap(Map<String, Object> paramMap) {
        return patrolUserEntMapper.getPatrolUserEntPatroTeamByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/04/30 0030 上午 11:47
     * @Description: 通过污染源id获取巡查人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<String> getPatrolPersonnelIdsByPolluionid(Map<String, Object> param) {
        List<String> userids = new ArrayList<>();
        List<Map<String, Object>> list = patrolUserEntMapper.getPatrolPersonnelIdsByPolluionid(param);
        if (list != null && list.size() > 0) {
            for (Map<String, Object> map : list) {
                if (map.get("userid") != null) {
                    userids.add(map.get("userid").toString());
                }
            }
        }
        return userids;
    }

    /**
     * @author: xsm
     * @date: 2020/04/30 0030 上午 11:47
     * @Description: 修改巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updatePollutionPatrolUserEnt(List<String> newuserids, String username, String pollutionid, List<PatrolUserEntVO> objlist, List<String> patrolpersonnelids) {
        //批量新增巡查人员信息

        //patrolUserEntMapper.deleteByPollutionID(pollutionid);
        patrolUserEntMapper.insertBatch(objlist);
        //判断修改前该企业是否有巡查人员分配信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("userids", patrolpersonnelids);
        //根据污染源ID获取污染源关联的点位信息
        List<Map<String, Object>> pointdata = userMonitorPointRelationDataMapper.getAllMonitorPointDataByParamMap(paramMap);
        if (patrolpersonnelids != null && patrolpersonnelids.size() > 0) {
            //有则先根据污染源ID和用户ID删除权限信息
            userMonitorPointRelationDataMapper.deleteByParamMap(paramMap);
        }
        if (newuserids != null && newuserids.size() > 0) {//判断修改信息中是否有分配人员
            //保存数据权限时  先根据企业ID和用户ID清除存在的数据权限
            paramMap.put("userids", newuserids);
            userMonitorPointRelationDataMapper.deleteByParamMap(paramMap);
            //有则添加数据权限
            List<UserMonitorPointRelationDataVO> list1 = new ArrayList<>();
            if (pointdata != null && pointdata.size() > 0) {
                for (String userid : newuserids) {
                    for (Map<String, Object> map : pointdata) {
                        UserMonitorPointRelationDataVO obj = new UserMonitorPointRelationDataVO();
                        obj.setPkId(UUID.randomUUID().toString());
                        obj.setDgimn(map.get("DGIMN") != null ? map.get("DGIMN").toString() : "");
                        obj.setFkMonitorpointid(map.get("outputid") != null ? map.get("outputid").toString() : "");
                        obj.setFkMonitorpointtype(map.get("FK_MonitorPointType") != null ? map.get("FK_MonitorPointType").toString() : null);
                        obj.setFkUserid(userid);
                        obj.setFkPollutionid(map.get("Pollutionid") != null ? map.get("Pollutionid").toString() : "");
                        obj.setUpdateuser(username);
                        obj.setUpdatetime(new Date());
                        list1.add(obj);
                    }
                }
            }
            if (list1.size() > 0) {
                //批量添加
                userMonitorPointRelationDataMapper.batchAdd(list1);
            }
        }
    }


    @Override
    public void updateMonitorpointPatrolUserEnt(List<String> newuserids, String username, PatrolUserEntVO entity, List<PatrolUserEntVO> objlist, List<String> patrolpersonnelids, String dgimn) {
        Map<String, Object> paramMap = new HashMap<>();

        List<Map<String,Object>> monitorpoints=new ArrayList<>();
        Map<String,Object> monitorinfo=new HashMap<>();
        monitorinfo.put("Fk_MonitorPointID",entity.getFkmonitorpointid());
        monitorinfo.put("FK_MonitorPointTypeCode",entity.getFkmonitorpointtypecode());
        monitorinfo.put("monitorpointid", entity.getFkmonitorpointid());
        monitorinfo.put("monitorpointtype",entity.getFkmonitorpointid());
        monitorinfo.put("patrolteam",entity.getpatrolteam());
        monitorinfo.put("patroltime",entity.getpatroltime());
        monitorpoints.add(monitorinfo);

        paramMap.put("monitorpoints",monitorpoints);

        patrolUserEntMapper.deleteByMonitorpointInfo(paramMap);
        //批量新增巡查人员信息
        //patrolUserEntMapper.deleteByPollutionID(pollutionid);
        patrolUserEntMapper.insertBatch(objlist);
        //判断修改前该企业是否有巡查人员分配信息
        paramMap.put("userids", patrolpersonnelids);

        patrolpersonnelids.addAll(newuserids);

        if (patrolpersonnelids != null && patrolpersonnelids.size() > 0) {
            //有则先根据污染源ID和用户ID删除权限信息
            userMonitorPointRelationDataMapper.deleteByParamMap(paramMap);
        }
        if (newuserids != null && newuserids.size() > 0) {//判断修改信息中是否有分配人员
            //保存数据权限时  先根据企业ID和用户ID清除存在的数据权限
            //有则添加数据权限
            List<UserMonitorPointRelationDataVO> list1 = new ArrayList<>();
            for (String userid : newuserids) {
                UserMonitorPointRelationDataVO obj = new UserMonitorPointRelationDataVO();
                obj.setPkId(UUID.randomUUID().toString());
                obj.setDgimn(dgimn);
                obj.setFkMonitorpointid(entity.getFkmonitorpointid());
                obj.setFkMonitorpointtype(entity.getFkmonitorpointtypecode());
                obj.setFkUserid(userid);
                obj.setFkPollutionid(entity.getfkpollutionid());
                obj.setUpdateuser(username);
                obj.setUpdatetime(new Date());
                list1.add(obj);
            }
            if (list1.size() > 0) {
                //批量添加
                userMonitorPointRelationDataMapper.batchAdd(list1);
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2020/04/30 0030 上午 11:47
     * @Description: 删除巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deletePollutionPatrolUserEntByID(String id, List<String> patrolpersonnelids, String patroltime) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutionid", id);
        paramMap.put("patroltime", patroltime);
        paramMap.put("userids", patrolpersonnelids);
        if (patrolpersonnelids != null && patrolpersonnelids.size() > 0) {
            //有则先根据污染源ID和用户ID删除权限信息
            userMonitorPointRelationDataMapper.deleteByParamMap(paramMap);
        }
        //删除巡查信息
        patrolUserEntMapper.deleteByPollutionIDAndPatrolTime(paramMap);
    }

    @Override
    public void deleteMonitiorpointPatrolUserEntByID(String id, List<String> patrolpersonnelids, String patroltime,String patrolteam, String monitorpointtype) {
        List<Map<String,Object>> monitorpoints=new ArrayList<>();
        Map<String, Object> monitorpoint = new HashMap<>();
        monitorpoint.put("Fk_MonitorPointID", id);
        monitorpoint.put("monitorpointtype", monitorpointtype);
        monitorpoint.put("monitorpointid", id);
        monitorpoint.put("FK_MonitorPointTypeCode", monitorpointtype);
        monitorpoints.add(monitorpoint);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitorpoints",monitorpoints);
        paramMap.put("userids", patrolpersonnelids);
        if (patrolpersonnelids != null && patrolpersonnelids.size() > 0) {
            //有则先根据污染源ID和用户ID删除权限信息
            userMonitorPointRelationDataMapper.deleteByParamMap(paramMap);
        }
        monitorpoint.put("patroltime", patroltime);
        monitorpoint.put("patrolteam", patrolteam);

        //删除巡查信息
        patrolUserEntMapper.deleteByMonitorpointInfo(paramMap);
    }

    @Override
    public void deleteByPollutionIDAndPatrolTime(Map<String, Object> param) {
        patrolUserEntMapper.deleteByPollutionIDAndPatrolTime(param);
    }

    @Override
    public Integer countPatrolUserEntByParams(Map<String, Object> param) {
        return patrolUserEntMapper.countPatrolUserEntByParams(param);
    }

    @Override
    public List<Map<String, Object>> getMonitorPatrolUserEntByParamMap(Map<String, Object> paramMap) {
        return patrolUserEntMapper.getMonitorPatrolUserEntByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPatrolUserEntByParams(Map<String, Object> paramMap) {
        return patrolUserEntMapper.getPatrolUserEntByParams(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPatrolPersonnelIdsByMonitorPointID(Map<String, Object> param) {
        return patrolUserEntMapper.getPatrolPersonnelIdsByMonitorPointID(param);
    }

    @Override
    public List<String> getPatrolPersonnelIdsByPointid(Map<String, Object> param) {
        List<String> userids = new ArrayList<>();
        List<Map<String, Object>> list = patrolUserEntMapper.getPatrolPersonnelIdsByPointid(param);
        if (list != null && list.size() > 0) {
            for (Map<String, Object> map : list) {
                if (map.get("userid") != null) {
                    userids.add(map.get("userid").toString());
                }
            }
        }
        return userids;
    }

}

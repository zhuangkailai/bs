package com.tjpu.sp.controller.environmentalprotection.patroluserent;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;
import com.tjpu.sp.model.environmentalprotection.patroluserent.PatrolUserEntVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.patroluserent.PatrolUserEntService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2020/04/29 0011 下午 1:58
 * @Description: 巡查人员分配控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("patroluserent")
public class PatrolUserEntController {

    @Autowired
    private PatrolUserEntService patrolUserEntService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;


    /**
     * @author: chengzq
     * @date: 2020/04/29 0011 下午 2:58
     * @Description: 通过自定义参数获取企业巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getPollutionPatrolUserEntByParamMap", method = RequestMethod.POST)
    public Object getPollutionPatrolUserEntByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            jsonObject.put("flag", true);

            if (jsonObject.get("patroltime") != null) {
                String patroltime = jsonObject.get("patroltime").toString();
                String dateYM1 = DataFormatUtil.getDateYM(new Date());
                Date dateYM = DataFormatUtil.getDateYM(patroltime);
                Date dateYM2 = DataFormatUtil.getDateYM(dateYM1);
                if (dateYM.getTime() >= dateYM2.getTime()) {
                    jsonObject.put("flag", false);
                }
            }

            List<Map<String, Object>> patrolUserEntByParamMap = patrolUserEntService.getPatrolUserEntByParamMap(jsonObject);
            resultMap.put("total", patrolUserEntByParamMap.size());
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                Integer pagenum = Integer.valueOf(jsonObject.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(jsonObject.get("pagesize").toString());
                patrolUserEntByParamMap = patrolUserEntByParamMap.stream().skip((pagenum - 1) & pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("datalist", patrolUserEntByParamMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/04/29 0011 下午 2:58
     * @Description: 通过自定义参数获取巡查组巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getPatrolTeamPatrolUserEntByParamMap", method = RequestMethod.POST)
    public Object getPatrolTeamPatrolUserEntByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> patrolUserEntByParamMap = patrolUserEntService.getPatrolUserEntPatroTeamByParamMap(jsonObject);
            resultMap.put("total", patrolUserEntByParamMap.size());
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                Integer pagenum = Integer.valueOf(jsonObject.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(jsonObject.get("pagesize").toString());
                patrolUserEntByParamMap = patrolUserEntByParamMap.stream().skip((pagenum - 1) & pagesize).limit(pagesize).collect(Collectors.toList());
            }
            patrolUserEntByParamMap.stream().forEach(m -> {
                Set<Map<String, Object>> pollutions = (Set<Map<String, Object>>) m.get("types");
                Set<Map<String, Object>> data = new HashSet<>();
                pollutions.stream().forEach(n -> {
                    Set<Map<String, Object>> users = (Set<Map<String, Object>>) n.get("users");
                    n.remove("users");
                    data.addAll(users);
                });
                m.put("users", data);
            });
            resultMap.put("datalist", patrolUserEntByParamMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/04/29 0011 下午 3:17
     * @Description: 新增巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addPatrolUserEnt", method = RequestMethod.POST)
    public Object addPatrolUserEnt(@RequestJson(value = "patrolteam") String patrolteam,
                                   @RequestJson(value = "patroltime") String patroltime,
                                   @RequestJson(value = "fkgroupleaderid") String fkgroupleaderid,
                                   @RequestJson(value = "userids") Object userids,
                                   @RequestJson(value = "pollutionids") Object pollutionids,
                                   @RequestJson(value = "monitorpoints") Object monitorpoints,
                                   HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> deletMap = new HashMap<>();
            Map<String, Object> monitordeletMap = new HashMap<>();
            List<String> pkuserids = (List<String>) userids;
            pkuserids.add(fkgroupleaderid);
            List<String> pkpollutionids = (List<String>) pollutionids;
            List<Map<String, Object>> monitorpointinfos = (List<Map<String, Object>>) monitorpoints;
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);

            pkuserids = pkuserids.stream().distinct().collect(Collectors.toList());

            Date now = new Date();
            paramMap.put("patrolteam", patrolteam);
            paramMap.put("patroltime", patroltime);
            paramMap.put("pkuserids", pkuserids);
            paramMap.put("pkpollutionids", pkpollutionids);

            //删除集合
            deletMap.put("patrolteam", patrolteam);
            deletMap.put("patroltime", patroltime);
            deletMap.put("userids", pkuserids);
            deletMap.put("pollutionids", pkpollutionids);

            monitordeletMap.put("monitorpoints", monitorpoints);
            monitordeletMap.put("userids", userids);

            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            List<PatrolUserEntVO> patroluserent = new ArrayList<>();
            List<UserMonitorPointRelationDataVO> monitorpointrelation = new ArrayList<>();
            for (String pkpollutionid : pkpollutionids) {
                for (String pkuserid : pkuserids) {
                    PatrolUserEntVO entity = new PatrolUserEntVO();
                    entity.setpkid(UUID.randomUUID().toString());
                    entity.setfkpollutionid(pkpollutionid);
                    entity.setfkpatrolpersonnelid(pkuserid);
                    entity.setpatrolteam(patrolteam);
                    entity.setpatroltime(patroltime);
                    entity.setupdatetime(DataFormatUtil.getDateYMDHMS(now));
                    entity.setupdateuser(username);
                    entity.setFkgroupleaderid(fkgroupleaderid);
                    patroluserent.add(entity);


                    for (Map<String, Object> map : outPutInfosByParamMap) {
                        String PK_PollutionID = map.get("PK_PollutionID") == null ? "" : map.get("PK_PollutionID").toString();
                        if (PK_PollutionID.equals(pkpollutionid)) {
                            String outputid = map.get("outputid") == null ? "" : map.get("outputid").toString();
                            String FK_MonitorPointTypeCode = map.get("FK_MonitorPointTypeCode") == null ? "" : map.get("FK_MonitorPointTypeCode").toString();
                            String DGIMN = map.get("DGIMN") == null ? "" : map.get("DGIMN").toString();
                            UserMonitorPointRelationDataVO userMonitorPointRelationDataVO = new UserMonitorPointRelationDataVO();
                            userMonitorPointRelationDataVO.setPkId(UUID.randomUUID().toString());
                            userMonitorPointRelationDataVO.setFkPollutionid(pkpollutionid);
                            userMonitorPointRelationDataVO.setDgimn(DGIMN);
                            userMonitorPointRelationDataVO.setFkMonitorpointid(outputid);
                            userMonitorPointRelationDataVO.setFkMonitorpointtype(FK_MonitorPointTypeCode);
                            userMonitorPointRelationDataVO.setFkUserid(pkuserid);
                            userMonitorPointRelationDataVO.setUpdatetime(now);
                            userMonitorPointRelationDataVO.setUpdateuser(username);
                            monitorpointrelation.add(userMonitorPointRelationDataVO);
                        }
                    }
                }
            }
            for (Map<String, Object> monitorpointinfo : monitorpointinfos) {
                String monitorpointid = monitorpointinfo.get("monitorpointid") == null ? "" : monitorpointinfo.get("monitorpointid").toString();
                String monitorpointtype = monitorpointinfo.get("monitorpointtype") == null ? "" : monitorpointinfo.get("monitorpointtype").toString();
                String DGIMN = monitorpointinfo.get("dgimn") == null ? "" : monitorpointinfo.get("dgimn").toString();
                for (String pkuserid : pkuserids) {
                    PatrolUserEntVO entity = new PatrolUserEntVO();
                    entity.setpkid(UUID.randomUUID().toString());
                    entity.setFkmonitorpointid(monitorpointid);
                    entity.setFkmonitorpointtypecode(monitorpointtype);
                    entity.setfkpatrolpersonnelid(pkuserid);
                    entity.setpatrolteam(patrolteam);
                    entity.setpatroltime(patroltime);
                    entity.setupdatetime(DataFormatUtil.getDateYMDHMS(now));
                    entity.setupdateuser(username);
                    entity.setFkgroupleaderid(fkgroupleaderid);
                    patroluserent.add(entity);


                    UserMonitorPointRelationDataVO userMonitorPointRelationDataVO = new UserMonitorPointRelationDataVO();
                    userMonitorPointRelationDataVO.setPkId(UUID.randomUUID().toString());
                    userMonitorPointRelationDataVO.setDgimn(DGIMN);
                    userMonitorPointRelationDataVO.setFkMonitorpointid(monitorpointid);
                    userMonitorPointRelationDataVO.setFkMonitorpointtype(monitorpointtype);
                    userMonitorPointRelationDataVO.setFkUserid(pkuserid);
                    userMonitorPointRelationDataVO.setUpdatetime(now);
                    userMonitorPointRelationDataVO.setUpdateuser(username);
                    monitorpointrelation.add(userMonitorPointRelationDataVO);
                }
            }

            patrolUserEntService.insert(deletMap, monitordeletMap, patroluserent, monitorpointrelation);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * getPollutionPatrolUserEntByParamMap
     *
     * @author: chengzq
     * @date: 2020/04/29 0011 下午 3:19
     * @Description: 通过id获取巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getPatrolUserEntByID", method = RequestMethod.POST)
    public Object getPatrolUserEntByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = patrolUserEntService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/04/29 0011 下午 3:19
     * @Description: 修改巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updatePatrolUserEnt", method = RequestMethod.POST)
    public Object updatePatrolUserEnt(@RequestJson(value = "patrolteam") String patrolteam, @RequestJson(value = "patroltime") String patroltime, @RequestJson(value = "fkgroupleaderid") String fkgroupleaderid,
                                      @RequestJson(value = "userids") Object userids, @RequestJson(value = "pollutionids") Object pollutionids, @RequestJson(value = "monitorpoints") Object monitorpoints, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> deletMap = new HashMap<>();
            Map<String, Object> monitordeletMap = new HashMap<>();
            List<String> pkuserids = (List<String>) userids;
            pkuserids.add(fkgroupleaderid);
            List<String> pkpollutionids = (List<String>) pollutionids;
            List<Map<String, Object>> monitorpointinfos = (List<Map<String, Object>>) monitorpoints;
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Date now = new Date();


            pkuserids = pkuserids.stream().distinct().collect(Collectors.toList());
            paramMap.put("patrolteam", patrolteam);
            paramMap.put("patroltime", patroltime);
            paramMap.put("pkuserids", pkuserids);
            paramMap.put("pkpollutionids", pkpollutionids);

            //删除集合
            deletMap.put("patrolteam", patrolteam);
            deletMap.put("patroltime", patroltime);
            deletMap.put("userids", pkuserids);
            deletMap.put("pollutionids", pkpollutionids);

            monitordeletMap.put("monitorpoints", monitorpoints);
            monitordeletMap.put("userids", userids);

            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);


            List<PatrolUserEntVO> patroluserent = new ArrayList<>();
            List<UserMonitorPointRelationDataVO> monitorpointrelation = new ArrayList<>();
            for (String pkpollutionid : pkpollutionids) {
                for (String pkuserid : pkuserids) {
                    PatrolUserEntVO entity = new PatrolUserEntVO();
                    entity.setpkid(UUID.randomUUID().toString());
                    entity.setfkpollutionid(pkpollutionid);
                    entity.setfkpatrolpersonnelid(pkuserid);
                    entity.setpatrolteam(patrolteam);
                    entity.setpatroltime(patroltime);
                    entity.setupdatetime(DataFormatUtil.getDateYMDHMS(now));
                    entity.setupdateuser(username);
                    entity.setFkgroupleaderid(fkgroupleaderid);
                    patroluserent.add(entity);

                    for (Map<String, Object> map : outPutInfosByParamMap) {
                        String PK_PollutionID = map.get("PK_PollutionID") == null ? "" : map.get("PK_PollutionID").toString();
                        if (PK_PollutionID.equals(pkpollutionid)) {
                            String outputid = map.get("outputid") == null ? "" : map.get("outputid").toString();
                            String FK_MonitorPointTypeCode = map.get("FK_MonitorPointTypeCode") == null ? "" : map.get("FK_MonitorPointTypeCode").toString();
                            String DGIMN = map.get("DGIMN") == null ? "" : map.get("DGIMN").toString();
                            UserMonitorPointRelationDataVO userMonitorPointRelationDataVO = new UserMonitorPointRelationDataVO();
                            userMonitorPointRelationDataVO.setPkId(UUID.randomUUID().toString());
                            userMonitorPointRelationDataVO.setFkPollutionid(pkpollutionid);
                            userMonitorPointRelationDataVO.setDgimn(DGIMN);
                            userMonitorPointRelationDataVO.setFkMonitorpointid(outputid);
                            userMonitorPointRelationDataVO.setFkMonitorpointtype(FK_MonitorPointTypeCode);
                            userMonitorPointRelationDataVO.setFkUserid(pkuserid);
                            userMonitorPointRelationDataVO.setUpdatetime(now);
                            userMonitorPointRelationDataVO.setUpdateuser(username);
                            monitorpointrelation.add(userMonitorPointRelationDataVO);
                        }
                    }
                }
            }

            pkuserids.remove(fkgroupleaderid);
            for (Map<String, Object> monitorpointinfo : monitorpointinfos) {
                String monitorpointid = monitorpointinfo.get("monitorpointid") == null ? "" : monitorpointinfo.get("monitorpointid").toString();
                String monitorpointtype = monitorpointinfo.get("monitorpointtype") == null ? "" : monitorpointinfo.get("monitorpointtype").toString();
                String DGIMN = monitorpointinfo.get("dgimn") == null ? "" : monitorpointinfo.get("dgimn").toString();
                for (String pkuserid : pkuserids) {
                    PatrolUserEntVO entity = new PatrolUserEntVO();
                    entity.setpkid(UUID.randomUUID().toString());
                    entity.setFkmonitorpointid(monitorpointid);
                    entity.setFkmonitorpointtypecode(monitorpointtype);
                    entity.setfkpatrolpersonnelid(pkuserid);
                    entity.setpatrolteam(patrolteam);
                    entity.setpatroltime(patroltime);
                    entity.setupdatetime(DataFormatUtil.getDateYMDHMS(now));
                    entity.setupdateuser(username);
                    entity.setFkgroupleaderid(fkgroupleaderid);
                    patroluserent.add(entity);


                    UserMonitorPointRelationDataVO userMonitorPointRelationDataVO = new UserMonitorPointRelationDataVO();
                    userMonitorPointRelationDataVO.setPkId(UUID.randomUUID().toString());
                    userMonitorPointRelationDataVO.setDgimn(DGIMN);
                    userMonitorPointRelationDataVO.setFkMonitorpointid(monitorpointid);
                    userMonitorPointRelationDataVO.setFkMonitorpointtype(monitorpointtype);
                    userMonitorPointRelationDataVO.setFkUserid(pkuserid);
                    userMonitorPointRelationDataVO.setUpdatetime(now);
                    userMonitorPointRelationDataVO.setUpdateuser(username);
                    monitorpointrelation.add(userMonitorPointRelationDataVO);
                }
            }
            patrolUserEntService.update(deletMap, monitordeletMap, patroluserent, monitorpointrelation);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/04/29 0011 下午 3:21
     * @Description: 通过id删除巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deletePatrolUserByParams", method = RequestMethod.POST)
    public Object deletePatrolUserByParams(@RequestJson(value = "patrolteam") String patrolteam, @RequestJson(value = "patroltime") String patroltime,
                                           @RequestJson(value = "userids") Object userids, @RequestJson(value = "pollutionids") Object pollutionids,
                                           @RequestJson(value = "monitorpoints") Object monitorpoints) throws Exception {
        try {
            Map<String, Object> deletMap = new HashMap<>();
            Map<String, Object> monitorpointdeletMap = new HashMap<>();

            monitorpointdeletMap.put("monitorpoints", monitorpoints);
            monitorpointdeletMap.put("userids", userids);

            //删除集合
            deletMap.put("patrolteam", patrolteam);
            deletMap.put("patroltime", patroltime);
            deletMap.put("userids", userids);
            deletMap.put("pollutionids", pollutionids);
            patrolUserEntService.deleteBydeletMap(deletMap, monitorpointdeletMap);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/04/29 0011 下午 3:31
     * @Description: 通过id查询巡查人员分配信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getPatrolUserEntDetailByID", method = RequestMethod.POST)
    public Object getPatrolUserEntDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> detailInfo = patrolUserEntService.getPatrolUserEntDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/04/30 0030 上午 10:35
     * @Description: 修改按企业分配下巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updatePollutionPatrolUserEnt", method = RequestMethod.POST)
    public Object updatePollutionPatrolUserEnt(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            List<String> newuserids = jsonObject.getJSONArray("patrolpersonnelids");
            PatrolUserEntVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PatrolUserEntVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String patroltime = "";
            if (entity.getpatroltime() != null && !"".equals(entity.getpatroltime())) {
                patroltime = entity.getpatroltime();
                patroltime = patroltime.substring(0, 7);
            }
            List<PatrolUserEntVO> objlist = new ArrayList<>();
            //根据污染源ID获取该污染源下的巡查人员信息
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid", entity.getfkpollutionid());
            param.put("patroltime", patroltime);
            List<String> patrolpersonnelids = patrolUserEntService.getPatrolPersonnelIdsByPolluionid(param);
            if (newuserids.size() > 0) {
                for (String userid : newuserids) {
                    PatrolUserEntVO obj = new PatrolUserEntVO();
                    obj.setpkid(UUID.randomUUID().toString());
                    obj.setfkpollutionid(entity.getfkpollutionid());
                    obj.setFkgroupleaderid(entity.getFkgroupleaderid());
                    obj.setpatroltime(entity.getpatroltime());
                    obj.setpatrolteam(entity.getpatrolteam());
                    obj.setfkpatrolpersonnelid(userid);
                    obj.setdescription(entity.getdescription());
                    obj.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    obj.setupdateuser(username);
                    objlist.add(obj);
                }
            } else {
                PatrolUserEntVO obj = new PatrolUserEntVO();
                obj.setpkid(UUID.randomUUID().toString());
                obj.setfkpollutionid(entity.getfkpollutionid());
                obj.setFkgroupleaderid(entity.getFkgroupleaderid());
                obj.setpatroltime(entity.getpatroltime());
                obj.setpatrolteam(entity.getpatrolteam());
                obj.setdescription(entity.getdescription());
                obj.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                obj.setupdateuser(username);
                objlist.add(obj);
            }
            patrolUserEntService.deleteByPollutionIDAndPatrolTime(param);
            patrolUserEntService.updatePollutionPatrolUserEnt(newuserids, username, entity.getfkpollutionid(), objlist, patrolpersonnelids);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "/updateMonitorPointPatrolUserEnt", method = RequestMethod.POST)
    public Object updateMonitorPointPatrolUserEnt(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            String dgimn = jsonObject.get("dgimn") == null ? "" : jsonObject.get("dgimn").toString();
            List<String> newuserids = jsonObject.getJSONArray("patrolpersonnelids");
            PatrolUserEntVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PatrolUserEntVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String patroltime = "";
            if (entity.getpatroltime() != null && !"".equals(entity.getpatroltime())) {
                patroltime = entity.getpatroltime();
                patroltime = patroltime.substring(0, 7);
            }
            List<PatrolUserEntVO> objlist = new ArrayList<>();
            //根据污染源ID获取该污染源下的巡查人员信息
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointid", entity.getFkmonitorpointid());
            param.put("monitorpointtype", entity.getFkmonitorpointtypecode());
            param.put("patrolteam", entity.getpatrolteam());
            param.put("patroltime", patroltime);
            List<String> patrolpersonnelids = patrolUserEntService.getPatrolPersonnelIdsByPolluionid(param);


            List<Map<String, Object>> patrolUserEntByParams = patrolUserEntService.getPatrolUserEntByParams(param);
            String FK_GroupLeaderId = patrolUserEntByParams.stream().filter(m -> m.get("FK_GroupLeaderId") != null).map(m -> m.get("FK_GroupLeaderId").toString()).findFirst().orElse("");
            for (String userid : newuserids) {
                PatrolUserEntVO obj = new PatrolUserEntVO();
                obj.setpkid(UUID.randomUUID().toString());
//                obj.setfkpollutionid(entity.getfkpollutionid());
                obj.setFkgroupleaderid(FK_GroupLeaderId);
                obj.setpatroltime(entity.getpatroltime());
                obj.setpatrolteam(entity.getpatrolteam());
                obj.setfkpatrolpersonnelid(userid);
                obj.setdescription(entity.getdescription());
                obj.setFkmonitorpointtypecode(entity.getFkmonitorpointtypecode());
                obj.setFkmonitorpointid(entity.getFkmonitorpointid());
                obj.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                obj.setupdateuser(username);
                objlist.add(obj);
            }
            patrolUserEntService.updateMonitorpointPatrolUserEnt(newuserids, username, entity, objlist, patrolpersonnelids, dgimn);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/04/30 0030 下午 1:21
     * @Description: 通过污染源id删除巡查人员分配信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deletePollutionPatrolUserEntByID", method = RequestMethod.POST)
    public Object deletePollutionPatrolUserEntByID(@RequestJson(value = "id") String id,
                                                   @RequestJson(value = "patroltime") String patroltime) throws Exception {
        try {
            //根据污染源ID获取该污染源下的巡查人员信息
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid", id);
            param.put("patroltime", patroltime);
            List<String> patrolpersonnelids = patrolUserEntService.getPatrolPersonnelIdsByPolluionid(param);
            patrolUserEntService.deletePollutionPatrolUserEntByID(id, patrolpersonnelids, patroltime);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "/deleteMonitorPatrolUserEntByID", method = RequestMethod.POST)
    public Object deleteMonitorPatrolUserEntByID(@RequestJson(value = "monitorpointid") String monitorpointid, @RequestJson(value = "monitorpointtype") String monitorpointtype,
                                                 @RequestJson(value = "patroltime") String patroltime,
                                                 @RequestJson(value = "patrolteam") String patrolteam) throws Exception {
        try {
            //根据污染源ID获取该污染源下的巡查人员信息
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointid", monitorpointid);
            param.put("patroltime", patroltime);
            param.put("patrolteam", patrolteam);
            param.put("monitorpointtype", monitorpointtype);
            List<String> patrolpersonnelids = patrolUserEntService.getPatrolPersonnelIdsByMonitorPointID(param).stream().filter(m -> m.get("FK_PatrolPersonnelId") != null).map(map -> map.get("FK_PatrolPersonnelId").toString()).distinct().collect(Collectors.toList());
            patrolUserEntService.deleteMonitiorpointPatrolUserEntByID(monitorpointid, patrolpersonnelids, patroltime, patrolteam, monitorpointtype);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/9/14 0014 下午 3:38
     * @Description: 通过巡查队伍，巡查时间验证是否包含巡查人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [patrolteam, patroltime]
     * @throws:
     */
    @RequestMapping(value = "/isHavePatrolUserEntByParams", method = RequestMethod.POST)
    public Object isHavePatrolUserEntByParams(@RequestJson(value = "patrolteam") String patrolteam,
                                              @RequestJson(value = "patroltime") String patroltime) throws Exception {
        try {
            //根据污染源ID获取该污染源下的巡查人员信息
            Map<String, Object> param = new HashMap<>();
            param.put("patrolteam", patrolteam);
            param.put("patroltime", patroltime);
            Integer count = patrolUserEntService.countPatrolUserEntByParams(param);

            return AuthUtil.parseJsonKeyToLower("success", count > 0 ? false : true);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/1/26 0026 下午 2:22
     * @Description: 获取所有企业信息和voc，恶臭，扬尘，微站信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getPollutionByParamsMap", method = RequestMethod.POST)
    public Object getPollutionByParamsMap(@RequestJson(value = "paramsjson",required = false) Object paramsjson) {
        try {
            Map<String, Object> paramMap   = new HashMap<>();
            if (paramsjson!=null){
                paramMap = (Map<String, Object>) paramsjson;
            }
            paramMap.put("monitortypes", Arrays.asList(9, 10, 12, 33));
            List<Map<String, Object>> allMonitorInfoByParams = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", allMonitorInfoByParams);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2021/1/26 0026 下午 4:29
     * @Description: 通过自定义参数获取其他监测点巡查信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPatrolUserEntByParamMap", method = RequestMethod.POST)
    public Object getOtherMonitorPatrolUserEntByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            jsonObject.put("flag", true);

            if (jsonObject.get("patroltime") != null) {
                String patroltime = jsonObject.get("patroltime").toString();
                String dateYM1 = DataFormatUtil.getDateYM(new Date());
                Date dateYM = DataFormatUtil.getDateYM(patroltime);
                Date dateYM2 = DataFormatUtil.getDateYM(dateYM1);
                if (dateYM.getTime() >= dateYM2.getTime()) {
                    jsonObject.put("flag", false);
                }
            }

            List<Map<String, Object>> patrolUserEntByParamMap = patrolUserEntService.getMonitorPatrolUserEntByParamMap(jsonObject);
            resultMap.put("total", patrolUserEntByParamMap.size());
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                Integer pagenum = Integer.valueOf(jsonObject.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(jsonObject.get("pagesize").toString());
                patrolUserEntByParamMap = patrolUserEntByParamMap.stream().skip((pagenum - 1) & pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("datalist", patrolUserEntByParamMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

}

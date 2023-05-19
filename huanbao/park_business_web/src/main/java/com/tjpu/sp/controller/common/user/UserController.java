package com.tjpu.sp.controller.common.user;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.common.user.UserService;
import com.tjpu.sp.service.environmentalprotection.patroluserent.PatrolUserEntService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.DevOpsTaskDisposeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private DevOpsTaskDisposeService devOpsTaskDisposeService;
    @Autowired
    private PatrolUserEntService patrolUserEntService;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;


    /**
     * @author: zhangzhenchao
     * @date: 2020/1/10 15:27
     * @Description: 获取部门用户树形结构
     * @param:
     * @return:
     */
    @RequestMapping(value = "getUserDepartmentTree", method = RequestMethod.POST)
    public Object getUserDepartmentTree() {
        try {
            List<Map<String, Object>> resultList = userService.getUserDepartmentTree();
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2020/04/29 11:13
     * @Description: 根据部门ID获取该部门向下的用户信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getUserInfosByDepartmentId", method = RequestMethod.POST)
    public Object getUserInfosByDepartmentId(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("departmentid", id);
            List<Map<String, Object>> resultList = userService.getUserInfosByDepartmentId(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: chengzq
     * @date: 2020/5/7 0007 下午 3:15
     * @Description: 获取所有用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getAllUserInfo", method = RequestMethod.POST)
    public Object getAllUserInfo() {
        try {
            List<Map<String, Object>> resultList = userService.getAllUserInfo();
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2020/06/03 0003 下午 4:22
     * @Description: 根据任务类型获取首页一键派单用户下拉信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneClickAssignUserTreeData", method = RequestMethod.POST)
    public Object getOneClickAssignUserTreeData(@RequestJson(value = "id", required = false) String id,
                                                @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype,
                                                @RequestJson(value = "tasktype") Integer tasktype) throws Exception {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            //部门用户组合下拉树
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (tasktype == 1) {
                resultList = userService.getUserDepartmentTree();
            } else if (tasktype == 5) {
                resultList = userService.getDevOpsUserDepartmentTree();
            }
            String userids = "";
            Date nowTime = new Date();
            String ym = DataFormatUtil.getDateYM(nowTime);//当前时间
            if (tasktype == CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode()) {
                Map<String, Object> param = new HashMap<>();
                /* param.put("monitorpointid", id);
                param.put("patroltime", ym);
                List<String> patrolpersonnelids = patrolUserEntService.getPatrolPersonnelIdsByPointid(param);*/
                param.put("pointorentid",id);
                // param.put("monitorpointtype",monitorpointtype);
                List<Map<String, Object>> patrolpersonnelids = userService.getTaskAssignUserData(param);
                if (patrolpersonnelids != null && patrolpersonnelids.size() > 0) {
                    for (Map<String, Object> onemap : patrolpersonnelids) {
                        userids = userids + onemap.get("User_ID") + ",";
                    }
                }
                if (!"".equals(userids)) {
                    userids = userids.substring(0, userids.length() - 1);
                }
            } else if (tasktype == CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("id", id);
                paramMap.put("monitorpointtype", monitorpointtype);
                EntDevOpsInfoVO obj = devOpsTaskDisposeService.getEntDevOpsInfoVOByParam(paramMap);
                userids = obj != null ? obj.getDevopspeople() : "";
            }
            resultmap.put("usertreedata", resultList);
            resultmap.put("devopspeoples", userids);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/12/14 0014 下午 3:41
     * @Description: 返回企业相关的人员通讯录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getPollutionMailListInfo", method = RequestMethod.POST)
    public Object getPollutionMailListInfo(@RequestJson(value = "field", required = false) String field) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("field", field);
            List<Map<String, Object>> resultList = userService.getMailListInfo(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: chengzq
     * @date: 2020/12/14 0014 下午 3:41
     * @Description: 获取用户通讯录按字母分组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getPinYinUserMailListInfo", method = RequestMethod.POST)
    public Object getPinYinUserMailListInfo(@RequestJson(value = "field", required = false) String field) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("field", field);
            List<Map<String, Object>> resultList = userService.getPinYinUserMailListInfo(paramMap);
            for (Map<String, Object> stringObjectMap : resultList) {
                Set<Map<String, Object>> users = stringObjectMap.get("users") == null ? new HashSet<>() : (Set<Map<String, Object>>) stringObjectMap.get("users");
                for (Map<String, Object> user : users) {
                    Set<String> list = user.get("departments") == null ? new HashSet<>() : (Set<String>) user.get("departments");
                    String collect = list.stream().collect(Collectors.joining("、"));
                    user.put("departments", collect);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/12/15 0015 下午 3:58
     * @Description: 获取用户通讯录按部门分组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [departmentname]
     * @throws:
     */
    @RequestMapping(value = "getDepartmentUserMailListInfo", method = RequestMethod.POST)
    public Object getDepartmentUserMailListInfo(@RequestJson(value = "field", required = false) String field) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("field", field);
            List<Map<String, Object>> resultList = userService.getDepartmentUserMailListInfo(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2021/3/4 0004 下午 4:42
     * @Description: 获取所有用户按部门分组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [field]
     * @throws:
     */
    @RequestMapping(value = "getAllDepartmentUserMailListInfo", method = RequestMethod.POST)
    public Object getAllDepartmentUserMailListInfo(@RequestJson(value = "field", required = false) String field) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("field", field);
            List<Map<String, Object>> resultList = userService.getAllDepartmentUserMailListInfo(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2021/1/25 16:59
     * @Description: 获取运维人员信息树
     * @param:
     * @return:
     */
    @RequestMapping(value = "getDevOpsUserDepartmentTree", method = RequestMethod.POST)
    public Object getDevOpsUserDepartmentTree() {
        try {
            List<Map<String, Object>> resultList = userService.getDevOpsUserDepartmentTree();
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/1/25 16:59
     * @Description: 获取企业用户信息树
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEntUserDepartmentTree", method = RequestMethod.POST)
    public Object getEntUserDepartmentTree() {
        try {
            List<Map<String, Object>> resultList = userService.getDevOpsUserDepartmentTree();
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2021/3/2 0002 下午 3:07
     * @Description: 批量新增多个用户数据权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [userids, monitiorpints]
     * @throws:
     */
    @RequestMapping(value = "addUserMonitorPointRelation", method = RequestMethod.POST)
    public Object addUserMonitorPointRelation(@RequestJson(value = "userids") List<String> userids, @RequestJson(value = "monitiorpints") List<Map<String, Object>> monitiorpints) {
        try {
            monitiorpints = getMonitorPoints(monitiorpints);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpoints", monitiorpints);
            paramMap.put("userids", userids);
            List<UserMonitorPointRelationDataVO> list = new ArrayList<>();
            for (String userid : userids) {
                for (Map<String, Object> monitiorpint : monitiorpints) {
                    UserMonitorPointRelationDataVO userMonitorPointRelationDataVO = new UserMonitorPointRelationDataVO();
                    userMonitorPointRelationDataVO.setPkId(UUID.randomUUID().toString());
                    userMonitorPointRelationDataVO.setFkUserid(userid);
                    userMonitorPointRelationDataVO.setFkPollutionid(monitiorpint.get("FK_Pollutionid") == null ? "" : monitiorpint.get("FK_Pollutionid").toString());
                    userMonitorPointRelationDataVO.setFkMonitorpointtype(monitiorpint.get("FK_MonitorPointTypeCode") == null ? "" : monitiorpint.get("FK_MonitorPointTypeCode").toString());
                    userMonitorPointRelationDataVO.setFkMonitorpointid(monitiorpint.get("Fk_MonitorPointID") == null ? "" : monitiorpint.get("Fk_MonitorPointID").toString());
                    userMonitorPointRelationDataVO.setDgimn(monitiorpint.get("DGIMN") == null ? "" : monitiorpint.get("DGIMN").toString());
                    list.add(userMonitorPointRelationDataVO);
                }
            }
            userMonitorPointRelationDataService.addUserMonitorPointRelation(paramMap, list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: chengzq
     * @date: 2021/3/30 0030 上午 9:54
     * @Description: 批量修改多个用户数据权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [userids, monitiorpints]
     * @throws:
     */
    @RequestMapping(value = "updateUserMonitorPointRelation", method = RequestMethod.POST)
    public Object updateUserMonitorPointRelation(@RequestJson(value = "userids") List<String> userids, @RequestJson(value = "monitiorpints") List<Map<String, Object>> monitiorpints) {
        try {
            monitiorpints = getMonitorPoints(monitiorpints);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userids", userids);
            paramMap.put("monitorpoints", new ArrayList<>());
            List<UserMonitorPointRelationDataVO> list = new ArrayList<>();
            for (String userid : userids) {
                for (Map<String, Object> monitiorpint : monitiorpints) {
                    UserMonitorPointRelationDataVO userMonitorPointRelationDataVO = new UserMonitorPointRelationDataVO();
                    userMonitorPointRelationDataVO.setPkId(UUID.randomUUID().toString());
                    userMonitorPointRelationDataVO.setFkUserid(userid);
                    userMonitorPointRelationDataVO.setFkPollutionid(monitiorpint.get("FK_Pollutionid") == null ? "" : monitiorpint.get("FK_Pollutionid").toString());
                    userMonitorPointRelationDataVO.setFkMonitorpointtype(monitiorpint.get("FK_MonitorPointTypeCode") == null ? "" : monitiorpint.get("FK_MonitorPointTypeCode").toString());
                    userMonitorPointRelationDataVO.setFkMonitorpointid(monitiorpint.get("Fk_MonitorPointID") == null ? "" : monitiorpint.get("Fk_MonitorPointID").toString());
                    userMonitorPointRelationDataVO.setDgimn(monitiorpint.get("DGIMN") == null ? "" : monitiorpint.get("DGIMN").toString());
                    list.add(userMonitorPointRelationDataVO);
                }
            }
            userMonitorPointRelationDataService.addUserMonitorPointRelation(paramMap, list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2021/3/2 0002 下午 3:07
     * @Description: 批量删除多个用户数据权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [userids, monitiorpints]
     * @throws:
     */
    @RequestMapping(value = "deleteUserMonitorPointRelation", method = RequestMethod.POST)
    public Object deleteUserMonitorPointRelation(@RequestJson(value = "userids") List<String> userids, @RequestJson(value = "monitiorpints") List<Map<String, Object>> monitiorpints) {
        try {
            monitiorpints = getMonitorPoints(monitiorpints);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpoints", monitiorpints);
            paramMap.put("userids", userids);
            userMonitorPointRelationDataService.deleteByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    private List<Map<String, Object>> getMonitorPoints(List<Map<String, Object>> monitiorpints) {
        List<Map<String, Object>> listdata = new ArrayList<>();

        List<Map<String, Object>> output = monitiorpints.stream().filter(m -> m.get("selected") != null && ((List<String>) m.get("selected")).stream().filter(n -> n.contains("_output_")).count() > 0).collect(Collectors.toList());
        List<Map<String, Object>> monitorpoint = monitiorpints.stream().filter(m -> m.get("selected") != null && ((List<String>) m.get("selected")).stream().filter(n -> n.contains("_monitorpointname,")).count() > 0).collect(Collectors.toList());
        for (Map<String, Object> stringObjectMap : monitorpoint) {
            List<String> selected = (List<String>) stringObjectMap.get("selected");
            for (String datastr : selected) {
                String[] monitorpointname_s = datastr.split("_monitorpointname,");
                if(monitorpointname_s.length==2){
                    Map<String, Object> data = new HashMap<>();
                    data.put("FK_MonitorPointTypeCode",stringObjectMap.get("monitorpointtype"));
                    data.put("Fk_MonitorPointID",monitorpointname_s[0]);
                    data.put("DGIMN",monitorpointname_s[1]);
                    listdata.add(data);
                }
            }
        }

        for (Map<String, Object> stringObjectMap : output) {
            List<String> selected = (List<String>) stringObjectMap.get("selected");
            for (String datastr : selected) {
                String[] monitorpointname_s = datastr.split("_output_");
                if(monitorpointname_s.length==2 ){
                    String[] split = monitorpointname_s[1].split(",");
                    if(split.length==2){
                        Map<String, Object> data = new HashMap<>();
                        data.put("FK_MonitorPointTypeCode",stringObjectMap.get("monitorpointtype"));
                        data.put("Fk_MonitorPointID",monitorpointname_s[0]);
                        data.put("FK_Pollutionid",split[0]);
                        data.put("DGIMN",split[1]);
                        listdata.add(data);
                    }
                }
            }
        }
        return listdata;
    }

}

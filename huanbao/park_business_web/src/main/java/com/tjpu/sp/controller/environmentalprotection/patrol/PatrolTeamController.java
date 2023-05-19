package com.tjpu.sp.controller.environmentalprotection.patrol;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamEntOrPointVO;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamUserVO;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamVO;
import com.tjpu.sp.service.common.user.UserService;
import com.tjpu.sp.service.environmentalprotection.patrol.PatrolTeamService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Description: 巡查组
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/12/6 10:16
 */
@RestController
@RequestMapping("patrolTeam")
public class PatrolTeamController {
    @Autowired
    private PatrolTeamService patrolTeamService;
    @Autowired
    private UserService userService;


    /**
     * @Description: 获取列表信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:14
     */
    @RequestMapping(value = "getDataListByParamMap", method = RequestMethod.POST)
    public Object getDataListByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = patrolTeamService.getDataListByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取点位列表信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:14
     */
    @RequestMapping(value = "getPointDataListByParamMap", method = RequestMethod.POST)
    public Object getPointDataListByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = patrolTeamService.getPointDataListByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @Description: 获取点位列表信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:14
     */
    @RequestMapping(value = "getEntDataListByParamMap", method = RequestMethod.POST)
    public Object getEntDataListByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = patrolTeamService.getEntDataListByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 添加或修改信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:14
     */
    @RequestMapping(value = "/addOrUpdateData", method = RequestMethod.POST)
    public Object addOrUpdateData(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            PatrolTeamVO obj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PatrolTeamVO());
            Date nowDay = new Date();
            obj.setUpdatetime(nowDay);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            obj.setUpdateuser(username);
            List<String> userids = jsonObject.getJSONArray("userids");

            if (userids.size()>0){
                List<PatrolTeamUserVO> patrolTeamUserVOS = new ArrayList<>();
                for (String id : userids) {
                    PatrolTeamUserVO patrolTeamUserVO =  new PatrolTeamUserVO();
                    patrolTeamUserVO.setPkId(UUID.randomUUID().toString());
                    patrolTeamUserVO.setFkUserid(id);
                    patrolTeamUserVOS.add(patrolTeamUserVO);
                }
                obj.setPatrolTeamUserVOS(patrolTeamUserVOS);
            }
            JSONArray entOrPointDataList = jsonObject.getJSONArray("entorpointdatalist");
            if (entOrPointDataList.size()>0){
                List<PatrolTeamEntOrPointVO> patrolTeamEntOrPointVOS = new ArrayList<>();
                for (Object o : entOrPointDataList) {
                    JSONObject object = JSONObject.fromObject(o);
                    PatrolTeamEntOrPointVO patrolTeamEntOrPointVO =  JSONObjectUtil.JsonObjectToEntity(object, new PatrolTeamEntOrPointVO());
                    patrolTeamEntOrPointVO.setPkId(UUID.randomUUID().toString());
                    patrolTeamEntOrPointVOS.add(patrolTeamEntOrPointVO);
                }
                obj.setPatrolTeamEntOrPointVOS(patrolTeamEntOrPointVOS);
            }
            if (StringUtils.isNotBlank(obj.getPkId())){//更新操作
                patrolTeamService.updateData(obj);
            }else {//添加操作
                obj.setPkId(UUID.randomUUID().toString());
                patrolTeamService.insertData(obj);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 添加或修改信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:14
     */
    @RequestMapping(value = "/addOrUpdatePointData", method = RequestMethod.POST)
    public Object addOrUpdatePointData(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            PatrolTeamEntOrPointVO obj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PatrolTeamEntOrPointVO());
            Date nowDay = new Date();
            obj.setUpdatetime(nowDay);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            obj.setUpdateuser(username);
            if (StringUtils.isNotBlank(obj.getPkId())){//更新操作
                patrolTeamService.updatePointData(obj);
            }else {//添加操作
                obj.setPkId(UUID.randomUUID().toString());
                patrolTeamService.insertPointData(obj);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }






    /**
     * @Description: 根据主键ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:18
     */
    @RequestMapping(value = "/deleteById", method = RequestMethod.POST)
    public Object deleteById(@RequestJson(value = "id") String id) throws Exception {
        try {
            patrolTeamService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 根据主键ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:18
     */
    @RequestMapping(value = "/deletePointDataById", method = RequestMethod.POST)
    public Object deletePointDataById(@RequestJson(value = "id") String id) throws Exception {
        try {
            patrolTeamService.deletePointDataById(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取详情或编辑数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:18
     */
    @RequestMapping(value = "/getDetailOrEditByID", method = RequestMethod.POST)
    public Object getDetailOrEditByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> result = patrolTeamService.getDetailOrEditById(id);
            List<Map<String,Object>> userids = patrolTeamService.getUserDataListById(id);
            List<Map<String,Object>> entorpointdatalist = patrolTeamService.getEntOrPointDataListById(id);
            result.put("userids",userids);
            result.put("entorpointdatalist",entorpointdatalist);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取详情或编辑数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:18
     */
    @RequestMapping(value = "/isHaveTeamName", method = RequestMethod.POST)
    public Object isHaveTeamName(@RequestJson(value = "teamname") String teamname,
                                 @RequestJson(value = "id",required = false) String id) {
        try {
            boolean isHave = false;
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("teamname",teamname);
            paramMap.put("id",id);
            Map<String, Object> result = patrolTeamService.getDataMapByParam(paramMap);
            if (result!=null&&result.size()>0){
                isHave = true;
            }
            return AuthUtil.parseJsonKeyToLower("success", isHave);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取详情或编辑数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:18
     */
    @RequestMapping(value = "/getTeamDataList", method = RequestMethod.POST)
    public Object getTeamDataList() {
        try {
            List<Map<String,Object>> resultList =  patrolTeamService.getTeamDataList();
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取详情或编辑数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/28 11:18
     */
    @RequestMapping(value = "/getTeamUserIds", method = RequestMethod.POST)
    public Object getTeamUserIds(@RequestJson(value = "id") String id) {
        try {
            List<Map<String,Object>> userids = patrolTeamService.getUserDataListById(id);
            return AuthUtil.parseJsonKeyToLower("success", userids);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/27 0027 上午 10:48
     * @Description: 根剧企业ID或点位ID 获取审核人信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOverReviewerUserTreeData", method = RequestMethod.POST)
    public Object getOverReviewerUserTreeData(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                              @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                              @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype
                                             ) throws Exception {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            //部门用户组合下拉树
            List<Map<String, Object>> resultList = userService.getUserDepartmentTree();
            String userids = "";
            Map<String, Object> param = new HashMap<>();
            if (pollutionid!=null) {
                param.put("pollutionid", pollutionid);
            }else {
                param.put("monitorpointid", monitorpointid);
                param.put("monitorpointtype", monitorpointtype);
            }
            List<Map<String, Object>> patrolpersonnelids = patrolTeamService.getOverReviewerUserTreeData(param);
            if (patrolpersonnelids != null && patrolpersonnelids.size() > 0) {
                for (Map<String, Object> onemap : patrolpersonnelids) {
                    userids = userids + onemap.get("User_ID") + ",";
                }
            }
            if (!"".equals(userids)) {
                userids = userids.substring(0, userids.length() - 1);
            }
            resultmap.put("usertreedata", resultList);
            resultmap.put("devopspeoples", userids);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

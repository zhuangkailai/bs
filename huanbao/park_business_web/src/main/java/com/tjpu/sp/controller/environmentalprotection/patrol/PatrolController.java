package com.tjpu.sp.controller.environmentalprotection.patrol;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolVO;
import com.tjpu.sp.service.environmentalprotection.patrol.PatrolService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("patrol")
public class PatrolController {
    @Autowired
    private PatrolService patrolService;

    /**
     * @Author: xsm
     * @Date: 2020/08/31 0031 下午 1:52
     * @Description: 自定义查询条件查询巡查列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getPatrolsByParamMap", method = RequestMethod.POST)
    public Object getPatrolsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = patrolService.getPatrolsByParamMap(jsonObject);
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
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 新增巡查信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addPatrol", method = RequestMethod.POST)
    public Object addPatrol(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            PatrolVO PatrolVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PatrolVO());
            PatrolVO.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            PatrolVO.setUpdateuser(username);
            PatrolVO.setPkId(UUID.randomUUID().toString());
            patrolService.insert(PatrolVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 通过id获取巡查信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getPatrolByID", method = RequestMethod.POST)
    public Object getPatrolByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            PatrolVO PatrolVO = patrolService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", PatrolVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 修改巡查信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updatePatrol", method = RequestMethod.POST)
    public Object updatePatrol(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            PatrolVO PatrolVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PatrolVO());
            PatrolVO.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            PatrolVO.setUpdateuser(username);
            patrolService.updateByPrimaryKey(PatrolVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 通过id删除巡查信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deletePatrolByID", method = RequestMethod.POST)
    public Object deletePatrolByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            patrolService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 通过id获取巡查详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getPatrolDetailByID", method = RequestMethod.POST)
    public Object getPatrolDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = patrolService.getPatrolDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

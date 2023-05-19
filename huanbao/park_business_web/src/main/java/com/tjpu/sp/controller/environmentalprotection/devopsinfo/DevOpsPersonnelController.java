package com.tjpu.sp.controller.environmentalprotection.devopsinfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevOpsPersonnelVO;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevicePersonnelRecordVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DevOpsPersonnelService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("devOpsPersonnel")
public class DevOpsPersonnelController {
    @Autowired
    private DevOpsPersonnelService devOpsPersonnelService;

    /**
     * @Author: xsm
     * @Date: 2022/04/01 0001 08:56
     * @Description: 自定义查询条件查询运维单位列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getDevOpsPersonnelListDataByParamMap", method = RequestMethod.POST)
    public Object getDevOpsPersonnelListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = devOpsPersonnelService.getDevOpsPersonnelListDataByParamMap(jsonObject);
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
     * @date: 2022/04/01 0001 08:56
     * @Description: 新增运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addDevOpsPersonnelInfo", method = RequestMethod.POST)
    public Object addDevOpsPersonnelInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<String> entdevopsids = jsonObject.getJSONArray("entdevopsids");
            DevOpsPersonnelVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), DevOpsPersonnelVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            entity.setPkPersonnelid(pkid);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            List<DevicePersonnelRecordVO> list = new ArrayList<>();
            for (String entdevopsid:entdevopsids){
                DevicePersonnelRecordVO oneobj = new DevicePersonnelRecordVO();
                oneobj.setPkId(UUID.randomUUID().toString());
                oneobj.setUpdatetime(new Date());
                oneobj.setUpdateuser(username);
                oneobj.setFkEntdevopsid(entdevopsid);
                oneobj.setFkPersonnelid(pkid);
                list.add(oneobj);
            }
            //添加运维人员信息
            devOpsPersonnelService.addDevOpsPersonnelInfo(entity,list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 修改运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateDevOpsPersonnelInfo", method = RequestMethod.POST)
    public Object updateDevOpsPersonnelInfo(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            List<String> entdevopsids = jsonObject.getJSONArray("entdevopsids");
            DevOpsPersonnelVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), DevOpsPersonnelVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            List<DevicePersonnelRecordVO> list = new ArrayList<>();
            for (String entdevopsid : entdevopsids) {
                DevicePersonnelRecordVO oneobj = new DevicePersonnelRecordVO();
                oneobj.setPkId(UUID.randomUUID().toString());
                oneobj.setUpdatetime(new Date());
                oneobj.setUpdateuser(username);
                oneobj.setFkEntdevopsid(entdevopsid);
                oneobj.setFkPersonnelid(entity.getPkPersonnelid());
                list.add(oneobj);
            }
            //添加运维人员信息
            devOpsPersonnelService.updateDevOpsPersonnelInfo(entity, list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维人员回显信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getDevOpsPersonnelInfoByParam", method = RequestMethod.POST)
    public Object getDevOpsPersonnelInfoByParam(@RequestJson(value = "unitid") String unitid,
                                                @RequestJson(value = "personnelid") String personnelid) throws Exception {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("unitid",unitid);
            paramMap.put("personnelid",personnelid);
            Map<String,Object> objmap = devOpsPersonnelService.getDevOpsPersonnelInfoByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维人员详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getDevOpsPersonnelDetailByID", method = RequestMethod.POST)
    public Object getDevOpsPersonnelDetailByID(@RequestJson(value = "unitid") String unitid,
                                               @RequestJson(value = "personnelid") String personnelid) throws Exception {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("unitid",unitid);
            paramMap.put("personnelid",personnelid);
            Map<String,Object> objmap = devOpsPersonnelService.getDevOpsPersonnelDetailByID(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 删除运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "deleteDevOpsPersonnelByID", method = RequestMethod.POST)
    public Object deleteDevOpsPersonnelByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            devOpsPersonnelService.deleteDevOpsPersonnelByID(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

package com.tjpu.sp.controller.environmentalprotection.devopsinfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevOpsUnitInfoVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DevOpsUnitInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("devOpsUnitInfo")
public class DevOpsUnitInfoController {
    @Autowired
    private DevOpsUnitInfoService devOpsUnitInfoService;

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
    @RequestMapping(value = "getDevOpsUnitInfoListDataByParamMap", method = RequestMethod.POST)
    public Object getDevOpsUnitInfoListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = devOpsUnitInfoService.getDevOpsUnitInfoListDataByParamMap(jsonObject);
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
     * @Description: 新增运维单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addDevOpsUnitInfoInfo", method = RequestMethod.POST)
    public Object addDevOpsUnitInfoInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            DevOpsUnitInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), DevOpsUnitInfoVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            entity.setPkDevopsunitid(pkid);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            devOpsUnitInfoService.addDevOpsUnitInfoInfo(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 修改运维单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateDevOpsUnitInfoInfo", method = RequestMethod.POST)
    public Object updateDevOpsUnitInfoInfo(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            DevOpsUnitInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), DevOpsUnitInfoVO.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            devOpsUnitInfoService.updateDevOpsUnitInfoInfo(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维单位详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getDevOpsUnitInfoDetailById", method = RequestMethod.POST)
    public Object getDevOpsUnitInfoDetailById(@RequestJson(value = "id") String id ) throws Exception {
        try {
            Map<String,Object> objmap = devOpsUnitInfoService.getDevOpsUnitInfoDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 删除运维单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/deleteDevOpsUnitInfoById", method = RequestMethod.POST)
    public Object deleteDevOpsUnitInfoById(@RequestJson(value = "id") String id ) throws Exception {
        try {
            devOpsUnitInfoService.deleteDevOpsUnitInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 验证运维单位名称是否数据重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsHaveDevOpsUnitInfo", method = RequestMethod.POST)
    public Object IsHaveDevOpsUnitInfo(@RequestJson(value = "unitname") String unitname) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("hasunitname",unitname);
            List<Map<String, Object>> datalist = devOpsUnitInfoService.IsHaveDevOpsUnitInfo(param);
            if(datalist.size()>0){
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            }else{
                return AuthUtil.parseJsonKeyToLower("success", "no");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/06/10 0010 10:28
     * @Description: 运维单位正在运维点位个数排名
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countDevOpsUnitDevOpsPointNumDataByParamMap", method = RequestMethod.POST)
    public Object countDevOpsUnitDevOpsPointNumDataByParamMap() {
        try {
            List<Map<String, Object>> datalist = devOpsUnitInfoService.countDevOpsUnitDevOpsPointNumDataByParamMap();
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

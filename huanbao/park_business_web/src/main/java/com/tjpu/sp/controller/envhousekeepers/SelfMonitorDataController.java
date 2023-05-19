package com.tjpu.sp.controller.envhousekeepers;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.envhousekeepers.SelfMonitorDataInfoVO;
import com.tjpu.sp.model.envhousekeepers.SelfMonitorInfoVO;
import com.tjpu.sp.service.envhousekeepers.SelfMonitorDataService;
import com.tjpu.sp.service.envhousekeepers.SelfMonitorInfoService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 自行监测数据记录信息
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("selfMonitorData")
public class SelfMonitorDataController {


    @Autowired
    private SelfMonitorDataService selfMonitorDataService;


    /**
     * @Description: 添加或更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateInfo", method = RequestMethod.POST)
    public Object addOrUpdateInfo(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            SelfMonitorDataInfoVO selfMonitorDataInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new SelfMonitorDataInfoVO());
            selfMonitorDataInfoVO.setUpdatetime(new Date());
            selfMonitorDataInfoVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(selfMonitorDataInfoVO.getPkId())) {//更新操作
                selfMonitorDataService.updateData(selfMonitorDataInfoVO);
            } else {//添加操作
                selfMonitorDataInfoVO.setPkId(UUID.randomUUID().toString());
                selfMonitorDataService.insertData(selfMonitorDataInfoVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getListDataByParamMap", method = RequestMethod.POST)
    public Object getListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson)  {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = selfMonitorDataService.getListDataByParamMap(jsonObject);
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
     * @Description: 根据ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "deleteInfoById", method = RequestMethod.POST)
    public Object deleteInfoById(@RequestJson(value = "id") String id) {
        try {
            selfMonitorDataService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

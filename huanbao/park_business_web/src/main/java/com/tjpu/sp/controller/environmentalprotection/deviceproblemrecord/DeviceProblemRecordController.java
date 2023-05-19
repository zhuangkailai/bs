package com.tjpu.sp.controller.environmentalprotection.deviceproblemrecord;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.service.environmentalprotection.deviceproblemrecord.DeviceProblemRecordService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: xsm
 * @description: 设备问题记录
 * @create: 2021-05-24 10:37
 * @version: V1.0
 */
@RestController
@RequestMapping("deviceProblemRecord")
public class DeviceProblemRecordController {
    @Autowired
    private DeviceProblemRecordService deviceProblemRecordService;

    /**
     * @Author: xsm
     * @Date: 2021/05/13 0013 10:16
     * @Description: 自定义查询条件查询设备问题记录列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getDeviceProblemRecordsByParamMap", method = RequestMethod.POST)
    public Object getDeviceProblemRecordsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = deviceProblemRecordService.getDeviceProblemRecordsByParamMap(jsonObject);
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
     * @date: 2021/05/13 0013 上午 11:50
     * @Description: 获取设备问题记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getDeviceProblemRecordDetailById", method = RequestMethod.POST)
    public Object getDeviceProblemRecordDetailById(@RequestJson(value = "id") String id ) throws Exception {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("id",id);
            Map<String,Object> objmap = deviceProblemRecordService.getDeviceProblemRecordDetailById(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

package com.tjpu.sp.controller.environmentalprotection.cjpz;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.cjpz.ProgramExecutionLogService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: xsm
 * @description: 程序执行日志表
 * @create: 2021-01-13 11:02
 * @version: V1.0
 */
@RestController
@RequestMapping("programExecutionLog")
public class ProgramExecutionLogController {
    @Autowired
    private ProgramExecutionLogService programExecutionLogService;


    /**
     * @Author: xsm
     * @Date: 2021/01/13 0013 上午 11:07
     * @Description: 自定义查询条件查询程序执行日志列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getProgramExecutionLogsByParamMap", method = RequestMethod.POST)
    public Object getProgramExecutionLogsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = programExecutionLogService.getProgramExecutionLogsByParamMap(jsonObject);
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
     * @date: 2021/01/13 0013 上午 11:07
     * @Description: 清空程序执行日志信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/clearProgramExecutionLogs", method = RequestMethod.POST)
    public Object clearProgramExecutionLogs() throws Exception {
        try {
            programExecutionLogService.clearProgramExecutionLogs();
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/01/13 0013 上午 11:07
     * @Description: 通过id获取程序执行日志详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getProgramExecutionLogDetailByID", method = RequestMethod.POST)
    public Object getProgramExecutionLogDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = programExecutionLogService.getProgramExecutionLogDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/01/13 0013 上午 11:23
     * @Description: 统计当天的程序执行日志数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/countProgramExecutionLogNumByTimes", method = RequestMethod.POST)
    public Object countProgramExecutionLogNumByTimes(@RequestJson(value = "nowday", required = false) String nowday) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String,Object> data=new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String,Object>> datalist=new ArrayList<>();
            String nowTime = DataFormatUtil.getDateYMD(new Date());
            if (StringUtils.isNotBlank(nowday)) {
                nowTime = nowday;
            }
            paramMap.put("starttime",nowTime);
            paramMap.put("endtime",nowTime);
            //获取总条数
            Long countall = programExecutionLogService.countProgramExecutionLogNumByTimes(paramMap);
            data.put("count", countall);
            data.put("name", "采集错误日志");
            data.put("sysmodel", "CollectErrorLog");
            datalist.add(data);
            resultMap.put("datalist",datalist);
            resultMap.put("sum",countall);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

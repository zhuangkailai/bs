package com.tjpu.sp.controller.environmentalprotection.superviseenforcelaw;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.superviseenforcelaw.TaskInfoVO;
import com.tjpu.sp.service.environmentalprotection.superviseenforcelaw.EnforceLawTaskInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: xsm
 * @description: 监察执法-执法任务信息表
 * @create: 2019-10-16 11:18
 * @version: V1.0
 */
@RestController
@RequestMapping("enforceLawTaskInfo")
public class EnforceLawTaskInfoController {
    @Autowired
    private EnforceLawTaskInfoService enforceLawTaskInfoService;

    /**
     * @Author: xsm
     * @Date: 2019/10/10 9:14
     * @Description: 自定义查询条件查询执法任务列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getEnforceLawTaskInfosByParamMap", method = RequestMethod.POST)
    public Object getEnforceLawTaskInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = enforceLawTaskInfoService.getEnforceLawTaskInfosByParamMap(jsonObject);
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
     * @date: 2019/10/16 0016 下午 2:04
     * @Description: 新增执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addEnforceLawTaskInfo", method = RequestMethod.POST)
    public Object addEnforceLawTaskInfo(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            TaskInfoVO taskInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TaskInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            taskInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            taskInfoVO.setUpdateuser(username);
            taskInfoVO.setPkTaskid(UUID.randomUUID().toString());
            enforceLawTaskInfoService.insert(taskInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 3:19
     * @Description: 通过id获取执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEnforceLawTaskInfoByID", method = RequestMethod.POST)
    public Object getEnforceLawTaskInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            TaskInfoVO taskinfovo = enforceLawTaskInfoService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", taskinfovo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:23
     * @Description: 修改执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateEnforceLawTaskInfo", method = RequestMethod.POST)
    public Object updateEnforceLawTaskInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            TaskInfoVO taskInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TaskInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            taskInfoVO.setUpdateuser(username);
            taskInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            enforceLawTaskInfoService.updateByPrimaryKey(taskInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:27
     * @Description: 通过id删除执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteEnforceLawTaskInfoByID", method = RequestMethod.POST)
    public Object deleteEnforceLawTaskInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            enforceLawTaskInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:28
     * @Description: 通过id获取执法任务详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEnforceLawTaskInfoDetailByID", method = RequestMethod.POST)
    public Object getEnforceLawTaskInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = enforceLawTaskInfoService.getEnforceLawTaskInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 3:28
     * @Description: 根据自定义参数导出执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/ExportEnforceLawTaskInfosByParams", method = RequestMethod.POST)
    public void ExportEnforceLawTaskInfosByParams(@RequestJson(value = "paramsjson") Object paramsJson, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            //获取表头数据
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            List<Map<String, Object>> tabletitledata = enforceLawTaskInfoService.getTableTitleForEnforceLawTaskInfo();
            //获取数据
            List<Map<String, Object>> tableListData = enforceLawTaskInfoService.getEnforceLawTaskInfosByParamMap(jsonObject);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "执法任务数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 18:22
    *@Description: 根据企业id统计监察执法信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @RequestMapping(value = "countEnforceLawTaskByPollutionId",method = RequestMethod.POST)
    public Object countEnforceLawTaskByPollutionId(@RequestJson(value = "pollutionid",required = true) String pollutionid){
        try {
            Map<Object, Object> requestMap = new HashMap<>();
            List<Map<Object, Object>> datas=enforceLawTaskInfoService.countEnforceLawTaskByPollutionId(pollutionid);
            requestMap.put("datajczf",datas);
            int num=0;
            for (Map<Object, Object> map:datas) {
                num+=(int)map.get("VALUE");
            }
            requestMap.put("totalnum",num);
            return AuthUtil.parseJsonKeyToLower("success",requestMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

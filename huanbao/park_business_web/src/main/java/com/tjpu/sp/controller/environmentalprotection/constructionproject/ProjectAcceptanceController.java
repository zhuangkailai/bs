package com.tjpu.sp.controller.environmentalprotection.constructionproject;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.constructionproject.CheckVO;
import com.tjpu.sp.service.environmentalprotection.constructionproject.ProjectAcceptanceService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: liyc
 * @date:2019/10/16 0016 9:01
 * @Description: 建设项目--项目验收信息模块控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("projectAcceptance")
public class ProjectAcceptanceController {
    @Autowired
    private ProjectAcceptanceService projectAcceptanceService;

    /**
     * @author: liyc
     * @date: 2019/10/17 0017 8:54
     * @Description: 获取项目验收信息列表+分页+条件查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid, pagenum, pagesize, projectname]
     * @throws:
     **/
    @RequestMapping(value = "getProjectAcceptanceListPage", method = RequestMethod.POST)
    public Object getProjectAcceptanceListPage(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                               @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                               @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                               @RequestJson(value = "projectname", required = false) String projectname,
                                               @RequestJson(value = "starttime", required = false) String starttime,
                                               @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("projectname", projectname);
            paramMap.put("starttime",starttime);
            paramMap.put("endtime",endtime);
            //分页
            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            Map<String, Object> tabledata = new HashMap<>();
            List<Map<String, Object>> dataList = projectAcceptanceService.getProjectAcceptanceListPage(paramMap);
            // 保存分页信息
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            // 分页后的数据
            tabledata.put("pagesize", pageInfo.getPageSize());// 每页条数
            tabledata.put("pagenum", pageInfo.getPageNum());// 当前页
            tabledata.put("total", pageInfo.getTotal());// 总条数
            tabledata.put("pages", pageInfo.getPages());// 总页数
            tabledata.put("total", dataList.size());// 总条数
            tabledata.put("tablelistdata", dataList);// 数据
            //返回数据
            requestMap.put("tabledata", tabledata);
            return AuthUtil.parseJsonKeyToLower("success", requestMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/10/17 0017 9:25
     * @Description: 往验收信息列表添加一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     **/
    @RequestMapping(value = "addProjectAcceptanceInfo", method = RequestMethod.POST)
    public Object addProjectAcceptanceInfo(HttpServletRequest request ) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            CheckVO checkVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new CheckVO());
            checkVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            checkVO.setPkCheckid(UUID.randomUUID().toString());
            checkVO.setUpdateuser(username);
            projectAcceptanceService.addProjectAcceptanceInfo(checkVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/10/17 0017 10:02
     * @Description: 通过主键id删除验收信息列表的单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "deleteProjectAcceptanceById", method = RequestMethod.POST)
    public Object deleteProjectAcceptanceById(@RequestJson(value = "pkCheckid", required = true) String pkCheckid) {
        try {
            projectAcceptanceService.deleteProjectAcceptanceById(pkCheckid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/10/17 0017 10:33
     * @Description: 通过主键id获取验收详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "getProjectAcceptanceDetailById", method = RequestMethod.POST)
    public Object getProjectAcceptanceDetailById(@RequestJson(value = "id", required = true) String id) {
        try {
            Map<String, Object> dataList = projectAcceptanceService.getProjectAcceptanceDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/10/17 0017 11:51
     * @Description: 项目验收编辑回显
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "getProjectAcceptanceById", method = RequestMethod.POST)
    public Object getProjectAcceptanceById(@RequestJson(value = "id", required = true) String id) {
        try {
            CheckVO checkVO = projectAcceptanceService.getProjectAcceptanceById(id);
            return AuthUtil.parseJsonKeyToLower("success", checkVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/10/17 0017 13:17
     * @Description: 编辑保存验收信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     **/
    @RequestMapping(value = "updateProjectAcceptance", method = RequestMethod.POST)
    public Object updateProjectAcceptance(HttpServletRequest request ) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            CheckVO checkVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new CheckVO());
            checkVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            checkVO.setUpdateuser(username);
            projectAcceptanceService.updateProjectAcceptance(checkVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 16:06
    *@Description: 根据企业id统计环评验收的信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @RequestMapping(value = "countCheckNatureByPollutionId",method = RequestMethod.POST)
    public Object countCheckNatureByPollutionId(@RequestJson(value = "pollutionid",required = true) String pollutionid){
        try {
            Map<String, Object> requestMap = new HashMap<>();
            List<Map<String,Object>> datas=projectAcceptanceService.countCheckNatureByPollutionId(pollutionid);
            requestMap.put("typedata",datas);
            int num=0;
            for (Map<String,Object> map:datas) {
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

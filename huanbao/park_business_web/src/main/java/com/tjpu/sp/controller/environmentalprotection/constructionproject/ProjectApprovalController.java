package com.tjpu.sp.controller.environmentalprotection.constructionproject;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.constructionproject.ApprovalVO;
import com.tjpu.sp.service.environmentalprotection.constructionproject.ProjectApprovalService;
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
 * @date:2019/10/14 0014 13:52
 * @Description: 建设项目--项目审批信息模块控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("projectApproval")
public class ProjectApprovalController {
    @Autowired
    private ProjectApprovalService projectApprovalService;

    /**
     * @author:liyc
     * @date:2019/10/14 0014 19:37
     * @Description: 获取项目审批信息列表+分页+条件查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pagenum, pagesize, projectname, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getProjectApprovalInfoListPage", method = RequestMethod.POST)
    public Object getProjectApprovalInfoListPage(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "projectname", required = false) String projectname,
                                                 @RequestJson(value = "starttime", required = false) String starttime,
                                                 @RequestJson(value = "endtime", required = false) String endtime,
                                                 @RequestJson(value = "eiacategory", required = false) List<String> eiacategory
    ) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("projectname", projectname);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("eiacategory", eiacategory);
            //分页
            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            Map<String, Object> tabledata = new HashMap<>();
            List<Map<String, Object>> dataList = projectApprovalService.getProjectApprovalInfoListPage(paramMap);
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
     * @author:liyc
     * @date:2019/10/15 0015 10:49
     * @Description: 通过主键id删除审批信息列表的单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteProjectApprovalById", method = RequestMethod.POST)
    public Object deleteProjectApprovalById(@RequestJson(value = "id", required = true) String pkApprovalid) {
        try {
            projectApprovalService.deleteProjectApprovalById(pkApprovalid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/10/15 0015 11:47
     * @Description: 往审批信息列表添加一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addProjectApprovalInfo", method = RequestMethod.POST)
    public Object addProjectApprovalInfo(HttpServletRequest request ) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            ApprovalVO approvalVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ApprovalVO());
            approvalVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            approvalVO.setPkApprovalid(UUID.randomUUID().toString());
            approvalVO.setUpdateuser(username);
            projectApprovalService.addProjectApprovalInfo(approvalVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/10/15 0015 16:47
     * @Description: 编辑审批信息列表一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateProjectApprovalInfo", method = RequestMethod.POST)
    public Object updateProjectApprovalInfo(HttpServletRequest request ) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            ApprovalVO approvalVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ApprovalVO());
            approvalVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            approvalVO.setUpdateuser(username);
            projectApprovalService.updateProjectApprovalInfo(approvalVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author:liyc
     * @date:2019/10/15 0015 15:21
     * @Description: 通过主键id获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getProjectApprovalDetailById", method = RequestMethod.POST)
    public Object getProjectApprovalDetailById(@RequestJson(value = "id", required = true) String id) {
        try {
            Map<String, Object> dataList = projectApprovalService.getProjectApprovalDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/10/16 0016 20:03
     * @Description: 编辑回显
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "getProjectApprovalById", method = RequestMethod.POST)
    public Object getProjectApprovalById(@RequestJson(value = "id", required = true) String id) {
        try {
            ApprovalVO approvalVO = projectApprovalService.getProjectApprovalById(id);
            return AuthUtil.parseJsonKeyToLower("success", approvalVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 11:28
    *@Description: 根据企业id统计环评审批的信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "countApprovalNatureByPollutionId",method = RequestMethod.POST)
    public Object countApprovalNatureByPollutionId(@RequestJson(value = "pollutionid",required = true) String pollutionid){
        try {
            Map<String, Object> requestMap = new HashMap<>();
            List<Map<String, Object>> datas = projectApprovalService.countApprovalNatureByPollutionId(pollutionid);
            requestMap.put("typedata",datas);
            if (datas != null && datas.size() > 0){
                int num=0;
                for (Map<String, Object> map : datas) {
                    num+=(int)map.get("VALUE");
                }
                requestMap.put("totalnum",num);
            }
            return AuthUtil.parseJsonKeyToLower("success", requestMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

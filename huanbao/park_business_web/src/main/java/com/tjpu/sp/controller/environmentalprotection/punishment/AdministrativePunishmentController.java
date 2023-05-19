package com.tjpu.sp.controller.environmentalprotection.punishment;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.punishment.PunishmentVO;
import com.tjpu.sp.service.common.UserAuthSupportService;
import com.tjpu.sp.service.environmentalprotection.punishment.AdministrativePunishmentService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * @author: liyc
 * @date:2019/10/18 0018 9:51
 * @Description: 行政处罚模块控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("administrativePunishment")
public class AdministrativePunishmentController {
    @Autowired
    private AdministrativePunishmentService punishmentService;

    /**
    *@author: liyc
    *@date: 2019/10/18 0018 10:37
    *@Description: 获取行政处罚信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid,pagenum,pagesize,fkpunishunitcodes,fkillegaltypecodes,fkcasetypecodes]
    *@throws:
    **/
    @RequestMapping(value = "getPunishmentListPage",method = RequestMethod.POST)
    public Object getPunishmentListPage(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                        @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                        @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                        @RequestJson(value = "fkpunishunitcodes", required = false) List<String> fkpunishunitcodes,
                                        @RequestJson(value = "fkillegaltypecodes", required = false) List<String> fkillegaltypecodes,
                                        @RequestJson(value = "fkcasetypecodes", required = false) List<String> fkcasetypecodes) throws Exception{
        try {
            Map<String, Object> requestMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pkpollutionid",pollutionid);
            paramMap.put("fkpunishunitcodes",fkpunishunitcodes);
            paramMap.put("fkillegaltypecodes",fkillegaltypecodes);
            paramMap.put("fkcasetypecodes",fkcasetypecodes);
            //分页
            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            Map<String, Object> tabledata = new HashMap<>();
            List<Map<String, Object>> dataList = punishmentService.getPunishmentListPage(paramMap);
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
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 13:27
    *@Description: 通过主键id删除行政处罚列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "deletePunishmentById",method = RequestMethod.POST)
    public Object deletePunishmentById(@RequestJson(value = "id",required = true) String id){
        try {
            punishmentService.deletePunishmentById(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 13:37
    *@Description: 往行政处罚列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value ="addPunishmentInfo",method = RequestMethod.POST)
    public Object addPunishmentInfo(HttpServletRequest request ) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            PunishmentVO punishmentVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PunishmentVO());
            punishmentVO.setPkCaseid(UUID.randomUUID().toString());
            punishmentVO.setUpdateuser(username);
            punishmentVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            punishmentService.addPunishmentInfo(punishmentVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 13:53
    *@Description: 行政处罚列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getPunishmentInfoById",method = RequestMethod.POST)
    public Object getPunishmentInfoById(@RequestJson(value = "id",required = true) String id){
        try {
            PunishmentVO punishmentVO=punishmentService.getPunishmentInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success",punishmentVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 14:01
    *@Description: 编辑保存行政处罚列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "updatePunishmentInfo",method = RequestMethod.POST)
    public Object updatePunishmentInfo(HttpServletRequest request ) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            PunishmentVO punishmentVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PunishmentVO());
            punishmentVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            punishmentVO.setUpdateuser(username);
            punishmentService.updatePunishmentInfo(punishmentVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 14:15
    *@Description: 通过主键id获取行政处罚详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getPunishmentDetailById",method = RequestMethod.POST)
    public Object getPunishmentDetailById(@RequestJson(value = "id",required = true) String id){
        try {
            Map<String,Object> dataList=punishmentService.getPunishmentDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success",dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/19 0019 16:44
    *@Description: 导出行政处罚的信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid, fkpunishunitcodes, fkillegaltypecodes, fkcasetypecodes, session, request, response]
    *@throws:
    **/
    @RequestMapping(value = "exportPunishmentInfo",method = RequestMethod.POST)
    public void exportPunishmentInfo(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                     @RequestJson(value = "fkpunishunitcodes", required = false) List<String> fkpunishunitcodes,
                                     @RequestJson(value = "fkillegaltypecodes", required = false) List<String> fkillegaltypecodes,
                                     @RequestJson(value = "fkcasetypecodes", required = false) List<String> fkcasetypecodes,
                                      HttpServletRequest request, HttpServletResponse response)throws Exception{

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pkpollutionid",pollutionid);
            paramMap.put("fkpunishunitcodes",fkpunishunitcodes);
            paramMap.put("fkillegaltypecodes",fkillegaltypecodes);
            paramMap.put("fkcasetypecodes",fkcasetypecodes);
            //获取表头数据
            List<Map<String, Object>> tabletitledata = punishmentService.getTableTitleForPunishment();
            List<Map<String, Object>> dataList = punishmentService.getPunishmentListPage(paramMap);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "行政处罚导出文件";
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, dataList, "");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 19:00
    *@Description: 根据企业id统计行政处罚信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @RequestMapping(value = "countPunishmentByPollutionId",method = RequestMethod.POST)
    public Object countPunishmentByPollutionId(@RequestJson(value = "pollutionid",required = true) String pollutionid){
        try {
            Map<String, Object> requestMap = new HashMap<>();
            List<Map<String, Object>> datas=punishmentService.countPunishmentByPollutionId(pollutionid);
            requestMap.put("datahjwf",datas);
            int num=0;
            for (Map<String, Object> map : datas) {
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

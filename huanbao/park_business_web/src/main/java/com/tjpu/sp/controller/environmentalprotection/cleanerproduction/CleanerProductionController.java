package com.tjpu.sp.controller.environmentalprotection.cleanerproduction;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.cleanerproduction.CleanerProductionVO;
import com.tjpu.sp.service.environmentalprotection.cleanerproduction.CleanerProductionService;
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
 * @author: liyc
 * @date:2019/10/18 0018 15:02
 * @Description: 清洁生产控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("cleanerProduction")
public class CleanerProductionController {
    @Autowired
    private CleanerProductionService cleanerProductionService;
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 15:57
    *@Description: 通过自定义参数获取清洁生产信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson]
    *@throws:
    **/
    @RequestMapping(value = "/getCleanerInfoByParamMap",method = RequestMethod.POST)
    public Object getCleanerInfoByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception{
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null){
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List cleanerInfoByParamMap = cleanerProductionService.getCleanerInfoByParamMap(jsonObject);
            PageInfo<Map<String,Object>> pageInfo = new PageInfo<>(cleanerInfoByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist",cleanerInfoByParamMap);
            resultMap.put("total",total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 8:55
    *@Description: 通过主键id删除清洁生产列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "deleteCleanerInfoById",method = RequestMethod.POST)
    public Object deleteCleanerInfoById(@RequestJson(value = "id",required = true) String id){
        try {
            cleanerProductionService.deleteCleanerInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:05
    *@Description: 清洁生产列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "addCleanerInfo",method = RequestMethod.POST)
    public Object addCleanerInfo(HttpServletRequest request ) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            CleanerProductionVO cleanerProductionVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new CleanerProductionVO());
            cleanerProductionVO.setPkCleanerproductid(UUID.randomUUID().toString());
            cleanerProductionVO.setUpdateuser(username);
            cleanerProductionVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            cleanerProductionService.addCleanerInfo(cleanerProductionVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:26
    *@Description: 清洁生产列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getCleanerInfoById",method = RequestMethod.POST)
    public Object getCleanerInfoById(@RequestJson(value = "id",required = true) String id){
        try {
            CleanerProductionVO cleanerProductionVO=cleanerProductionService.getCleanerInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success",cleanerProductionVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:37
    *@Description: 编辑保存清洁生产列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [request, session]
    *@throws:
    **/
    @RequestMapping(value = "updateCleanerInfo",method = RequestMethod.POST)
    public Object updateCleanerInfo(HttpServletRequest request) throws Exception{
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("updateformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            CleanerProductionVO cleanerProductionVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new CleanerProductionVO());
            cleanerProductionVO.setUpdateuser(username);
            cleanerProductionVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            cleanerProductionService.updateCleanerInfo(cleanerProductionVO);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:50
    *@Description: 获取清洁生产的详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @RequestMapping(value = "getCleanerDetailById",method = RequestMethod.POST)
    public Object getCleanerDetailById(@RequestJson(value = "id",required = true) String id){
        try {
            Map<String,Object> dataList=cleanerProductionService.getCleanerDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success",dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 10:57
    *@Description: 导出清洁生产信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramsJson]
    *@throws:
    **/
    @RequestMapping(value = "exportCleanerInfo",method = RequestMethod.POST)
    public void exportCleanerInfo(@RequestJson(value = "paramsjson") Object paramsJson,
                                    HttpServletRequest request, HttpServletResponse response)throws Exception{
        JSONObject jsonObject = JSONObject.fromObject(paramsJson);
        //获取表头数据
        List<Map<String, Object>> tabletitledata = cleanerProductionService.getTableTitleForCleaner();
        //获取查询列表数据
        List cleanerInfoByParamMap = cleanerProductionService.getCleanerInfoByParamMap(jsonObject);
        //设置导出文件数据格式
        List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
        List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
        //设置文件名称
        String fileName = "清洁生产导出文件_" + new Date().getTime();
        ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, cleanerInfoByParamMap, "");
    }
}

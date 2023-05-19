package com.tjpu.sp.controller.environmentalprotection.petitionlettercomplaint;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.petitionlettercomplaint.PetitionLetterComplaintVO;
import com.tjpu.sp.service.environmentalprotection.petitionlettercomplaint.PetitionLetterComplaintService;
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
 * @description: 信访投诉-投诉案件信息表
 * @create: 2019-10-16 17:11
 * @version: V1.0
 */
@RestController
@RequestMapping("petitionLetterComplaint")
public class PetitionLetterComplaintController {
    @Autowired
    private PetitionLetterComplaintService petitionLetterComplaintService;

    /**
     * @Author: xsm
     * @Date: 2019/10/10 9:14
     * @Description: 自定义查询条件查询投诉案件列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getPetitionLetterComplaintsByParamMap", method = RequestMethod.POST)
    public Object getPetitionLetterComplaintsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = petitionLetterComplaintService.getPetitionLetterComplaintsByParamMap(jsonObject);
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
     * @Description: 新增投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addPetitionLetterComplaint", method = RequestMethod.POST)
    public Object addPetitionLetterComplaint(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            PetitionLetterComplaintVO petitionLetterComplaintVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PetitionLetterComplaintVO());
            petitionLetterComplaintVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            petitionLetterComplaintVO.setUpdateuser(username);
            petitionLetterComplaintVO.setPkPetitionid(UUID.randomUUID().toString());
            petitionLetterComplaintService.insert(petitionLetterComplaintVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 3:19
     * @Description: 通过id获取投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getPetitionLetterComplaintByID", method = RequestMethod.POST)
    public Object getPetitionLetterComplaintByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            PetitionLetterComplaintVO petitionLetterComplaintVO = petitionLetterComplaintService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", petitionLetterComplaintVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:23
     * @Description: 修改投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updatePetitionLetterComplaint", method = RequestMethod.POST)
    public Object updatePetitionLetterComplaint(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            PetitionLetterComplaintVO petitionLetterComplaintVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PetitionLetterComplaintVO());
            petitionLetterComplaintVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            petitionLetterComplaintVO.setUpdateuser(username);
            petitionLetterComplaintService.updateByPrimaryKey(petitionLetterComplaintVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:27
     * @Description: 通过id删除投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deletePetitionLetterComplaintByID", method = RequestMethod.POST)
    public Object deletePetitionLetterComplaintByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            petitionLetterComplaintService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:28
     * @Description: 通过id获取投诉案件详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getPetitionLetterComplaintDetailByID", method = RequestMethod.POST)
    public Object getPetitionLetterComplaintDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = petitionLetterComplaintService.getPetitionLetterComplaintDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 3:28
     * @Description: 根据自定义参数导出投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/ExportPetitionLetterComplaintsByParams", method = RequestMethod.POST)
    public void ExportPetitionLetterComplaintsByParams(@RequestJson(value = "paramsjson") Object paramsJson, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            //获取表头数据
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            List<Map<String, Object>> tabletitledata = petitionLetterComplaintService.getTableTitleForPetitionLetterComplaint();
            //获取数据
            List<Map<String, Object>> tableListData = petitionLetterComplaintService.getPetitionLetterComplaintsByParamMap(jsonObject);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "投诉案件数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 17:07
    *@Description: 通过企业id统计信访投诉信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @RequestMapping(value = "countLetterComplaintByPollutionId",method = RequestMethod.POST)
    public Object countLetterComplaintByPollutionId(@RequestJson(value = "pollutionid",required = true) String pollutionid){
        try {
            Map<String, Object> requestMap = new HashMap<>();
            List<Map<String, Object>> datas=petitionLetterComplaintService.countLetterComplaintByPollutionId(pollutionid);
            requestMap.put("dataxfts",datas);
            int num=0;
            for (Map<String, Object> map:datas) {
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

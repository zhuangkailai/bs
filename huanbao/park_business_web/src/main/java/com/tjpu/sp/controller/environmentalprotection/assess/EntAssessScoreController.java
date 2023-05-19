package com.tjpu.sp.controller.environmentalprotection.assess;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.common.emergency.EmergencyCaseInfoVO;
import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentDataVO;
import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentInfoVO;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamEntOrPointVO;
import com.tjpu.sp.service.environmentalprotection.assess.EntAssessScoreService;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


/**
 * @Description: 企业考核评分
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/7/7 9:49
 */
@RestController
@RequestMapping("entAssessScore")
public class EntAssessScoreController {


    @Autowired
    private EntAssessScoreService entAssessScoreService;


    /**
     * @Description: 获取企业检查信息列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 10:51
     */
    @RequestMapping(value = "getEntAssessInfoListByParam", method = RequestMethod.POST)
    public Object getEntAssessInfoListByParam(
            @RequestJson(value = "paramjson") Object paramjson) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            JSONObject jsonObject = JSONObject.fromObject(paramjson);
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entAssessScoreService.getEntAssessInfoListByParam(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo(datalist);
            resultMap.put("datalist", pageInfo.getList());
            resultMap.put("total", pageInfo.getTotal());
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 导出企业检查信息列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 10:51
     */
    @RequestMapping(value = "exportEntAssessInfoListByParam", method = RequestMethod.POST)
    public void exportEntAssessInfoListByParam(
            @RequestJson(value = "paramjson") Object paramjson,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramjson);
            List<Map<String, Object>> dataList = entAssessScoreService.getEntAssessInfoListByParam(jsonObject);
            int orderIndex = 0;
            for (Map<String, Object> dataMap : dataList) {
                orderIndex++;
                dataMap.put("orderindex", orderIndex);
                if (dataMap.get("totalreducescore") != null) {
                    dataMap.put("totalreducescore", DataFormatUtil.subZeroAndDot(dataMap.get("totalreducescore").toString()));
                } else {
                    dataMap.put("totalreducescore", "");
                }
            }
            //设置导出文件数据格式
            List<String> headers = Arrays.asList("序号", "检查企业", "检查时间", "巡查人员", "扣分值");
            List<String> headersField = Arrays.asList("orderindex", "pollutionname", "checktime", "checkpeople", "totalreducescore");
            //设置文件名称
            String fileName = "企业检查信息_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, dataList, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取企业检查信息列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 10:51
     */
    @RequestMapping(value = "addOrUpdateData", method = RequestMethod.POST)
    public Object addOrUpdateData(
            @RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            EntAssessmentInfoVO entAssessmentInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntAssessmentInfoVO());
            Date nowDay = new Date();
            entAssessmentInfoVO.setUpdatetime(nowDay);
            entAssessmentInfoVO.setUpdateuser(username);
            JSONArray itemDataList = jsonObject.getJSONArray("itemdatalist");
            if (itemDataList.size() > 0) {
                List<EntAssessmentDataVO> entAssessmentDataVOS = new ArrayList<>();
                for (Object o : itemDataList) {
                    JSONObject object = JSONObject.fromObject(o);
                    EntAssessmentDataVO entAssessmentDataVO = JSONObjectUtil.JsonObjectToEntity(object, new EntAssessmentDataVO());
                    entAssessmentDataVO.setPkDataid(UUID.randomUUID().toString());
                    entAssessmentDataVO.setUpdateuser(username);
                    entAssessmentDataVO.setUpdatetime(nowDay);
                    entAssessmentDataVOS.add(entAssessmentDataVO);
                }
                entAssessmentInfoVO.setEntAssessmentDataVOS(entAssessmentDataVOS);
            }
            if (StringUtils.isNotBlank(entAssessmentInfoVO.getPkDataid())) {//更新操作
                entAssessScoreService.updateInfo(entAssessmentInfoVO);
            } else {//添加操作
                entAssessmentInfoVO.setPkDataid(UUID.randomUUID().toString());
                entAssessScoreService.insertInfo(entAssessmentInfoVO);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 信息删除
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 12:03
     */
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public Object deleteById(
            @RequestJson(value = "id") String id) throws Exception {
        try {
            entAssessScoreService.deleteById(id);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 编辑或详情数据回显
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 12:03
     */
    @RequestMapping(value = "getEditOrViewDataById", method = RequestMethod.POST)
    public Object getEditOrViewDataById(
            @RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> resultMap = entAssessScoreService.getEditOrViewDataById(id);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取添加项数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 12:03
     */
    @RequestMapping(value = "getAddItemDataList", method = RequestMethod.POST)
    public Object getAddItemDataList() throws Exception {
        try {
            List<Map<String, Object>> resultList = entAssessScoreService.getAddItemDataList();
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

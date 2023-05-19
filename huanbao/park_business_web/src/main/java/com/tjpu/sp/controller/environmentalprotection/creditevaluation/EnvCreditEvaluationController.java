package com.tjpu.sp.controller.environmentalprotection.creditevaluation;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.anticontrol.NotResubmit;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.environmentalprotection.creditevaluation.EnvCreditEvaluationVO;
import com.tjpu.sp.service.environmentalprotection.creditevaluation.EnvCreditEvaluationService;
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
 * @description: 信用评价-环境信用评价信息表
 * @create: 2019-10-16 19:28
 * @version: V1.0
 */
@RestController
@RequestMapping("envCreditEvaluation")
public class EnvCreditEvaluationController {
    @Autowired
    private EnvCreditEvaluationService envCreditEvaluationService;


    /**
     * @Author: xsm
     * @Date: 2019/10/17 0017 上午 8:51
     * @Description: 自定义查询条件查询环境信用评价列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getEnvCreditEvaluationsByParamMap", method = RequestMethod.POST)
    public Object getEnvCreditEvaluationsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = envCreditEvaluationService.getEnvCreditEvaluationsByParamMap(jsonObject);
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
     * @date: 2019/10/17 0017 上午 8:51
     * @Description: 新增环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addEnvCreditEvaluation", method = RequestMethod.POST)
    public Object addEnvCreditEvaluation(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            EnvCreditEvaluationVO envCreditEvaluationVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EnvCreditEvaluationVO());
            envCreditEvaluationVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            envCreditEvaluationVO.setUpdateuser(username);
            envCreditEvaluationVO.setPkCreditEvaluationid(UUID.randomUUID().toString());
            envCreditEvaluationService.insert(envCreditEvaluationVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description: 通过id获取环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEnvCreditEvaluationByID", method = RequestMethod.POST)
    public Object getEnvCreditEvaluationByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            EnvCreditEvaluationVO envCreditEvaluationVO = envCreditEvaluationService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", envCreditEvaluationVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description: 修改环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateEnvCreditEvaluation", method = RequestMethod.POST)
    public Object updateEnvCreditEvaluation(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            EnvCreditEvaluationVO envCreditEvaluationVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EnvCreditEvaluationVO());
            envCreditEvaluationVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            envCreditEvaluationVO.setUpdateuser(username);
            envCreditEvaluationService.updateByPrimaryKey(envCreditEvaluationVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description: 通过id删除环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteEnvCreditEvaluationByID", method = RequestMethod.POST)
    public Object deleteEnvCreditEvaluationByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            envCreditEvaluationService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description: 通过id获取环境信用评价详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEnvCreditEvaluationDetailByID", method = RequestMethod.POST)
    public Object getEnvCreditEvaluationDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = envCreditEvaluationService.getEnvCreditEvaluationDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/17 0017 上午 8:51
     * @Description: 根据自定义参数导出环境信用评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/ExportEnvCreditEvaluationsByParams", method = RequestMethod.POST)
    public void ExportEnvCreditEvaluationsByParams(@RequestJson(value = "paramsjson") Object paramsJson, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            //获取表头数据
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            List<Map<String, Object>> tabletitledata = envCreditEvaluationService.getTableTitleForEnforceLawTaskInfo();
            //获取数据
            List<Map<String, Object>> tableListData = envCreditEvaluationService.getEnvCreditEvaluationsByParamMap(jsonObject);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "环境信用评价数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计企业各个行政区划环境信用评价数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/8 11:43
     */
    @RequestMapping(value = "/countEntRegionEvaDataList", method = RequestMethod.POST)
    public Object countEntRegionEvaDataList() throws Exception {
        try {
            Map<String, List<Map<String, Object>>> regionAndList = new HashMap<>();
            List<Map<String, Object>> list;
            String regioncode;
            Map<String, Object> codeAndName = new HashMap<>();
            List<Map<String, Object>> dataList = envCreditEvaluationService.getEntRegionEvaDataList();
            for (Map<String, Object> dataMap : dataList) {
                regioncode = dataMap.get("regioncode").toString();
                if (regionAndList.get(regioncode) != null) {
                    list = regionAndList.get(regioncode);
                } else {
                    list = new ArrayList<>();
                }
                codeAndName.put(regioncode, dataMap.get("regionname"));
                list.add(dataMap);
                regionAndList.put(regioncode, list);
            }
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (String codeIndex : regionAndList.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("regioncode", codeIndex);
                resultMap.put("regionname", codeAndName.get(codeIndex));
                dataList = regionAndList.get(codeIndex);
                for (Map<String, Object> dataMap : dataList) {
                    dataMap.remove("regioncode");
                    dataMap.remove("regionname");
                }
                resultMap.put("datalist", dataList);
                resultList.add(resultMap);
            }


            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 统计企业环境信用评价最新数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/8 11:43
     */
    @RequestMapping(value = "/countEntEvaDataList", method = RequestMethod.POST)
    public Object countEntEvaDataList() throws Exception {
        try {
            List<Map<String, Object>> resultList = envCreditEvaluationService.countEntEvaDataList();
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计企业环境信用评价最新数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/8 11:43
     */
    //@NotResubmit
    @RequestMapping(value = "/getLastEntEvaDataListByParam", method = RequestMethod.POST)
    public Object getLastEntEvaDataListByParam(@RequestJson(value = "paramjson") Object paramjson) throws Exception {
        try {
            Map<String, Object> paramMap = (Map<String, Object>) paramjson;
            PageInfo<Map<String, Object>> pageInfos = envCreditEvaluationService.getLastEntEvaDataListByParam(paramMap);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("datalist", pageInfos.getList());
            resultMap.put("total", pageInfos.getTotal());
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

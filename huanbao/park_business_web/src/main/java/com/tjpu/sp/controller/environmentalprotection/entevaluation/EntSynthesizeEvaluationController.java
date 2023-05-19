package com.tjpu.sp.controller.environmentalprotection.entevaluation;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationDetailVO;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntSynthesizeEvaluationVO;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntSynthesizeEvaluationService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: xsm
 * @description: 企业综合评价控制层
 * @create: 2022-03-04 13:24
 * @version: V1.0
 */
@RestController
@RequestMapping("entSynthesizeEvaluation")
public class EntSynthesizeEvaluationController {
    @Autowired
    private EntSynthesizeEvaluationService entSynthesizeEvaluationService;

    /**
     * @Author: xsm
     * @Date: 2022/03/04 0004 09:18
     * @Description: 自定义查询条件查询企业综合评价控列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getEntSynthesizeEvaluationListDataByParamMap", method = RequestMethod.POST)
    public Object getEntSynthesizeEvaluationListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entSynthesizeEvaluationService.getEntSynthesizeEvaluationListDataByParamMap(jsonObject);
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
     * @date: 2022/03/04 0004 上午 9:22
     * @Description: 新增企业综合评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addEntSynthesizeEvaluationInfo", method = RequestMethod.POST)
    public Object addEntSynthesizeEvaluationInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<JSONObject> indexdata = jsonObject.getJSONArray("indexdata");
            EntSynthesizeEvaluationVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntSynthesizeEvaluationVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            entity.setPkId(pkid);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            List<EntEvaluationDetailVO> list = new ArrayList<>();
            for (JSONObject onejson : indexdata) {
                EntEvaluationDetailVO oneobj = JSONObjectUtil.parseStringToJavaObject(onejson.toString(), EntEvaluationDetailVO.class);
                oneobj.setPkId(UUID.randomUUID().toString());
                oneobj.setUpdatetime(new Date());
                oneobj.setUpdateuser(username);
                oneobj.setFkEntevaluationid(pkid);
                list.add(oneobj);
            }
            Double total = entSynthesizeEvaluationService.countEvaluationIndex(list);
            entity.setEvaluationscore(total);
            entity.setFkEvaluationlevelcode(getEvaluationLevelCode(total));
            //添加企业评价信息
            entSynthesizeEvaluationService.addEntSynthesizeEvaluationInfo(entity, list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/07 0007 下午 16:31
     * @Description: 获取评分等级代码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [value]
     * @throws:
     */
    private String getEvaluationLevelCode(Double value) {
        List<Map<String, Object>> evaluationLevels = entSynthesizeEvaluationService.getEvaluationLevelByParamMap(new HashMap<>());
        Map<String, Object> stringObjectMap = evaluationLevels.stream().filter(m -> m.get("maxvalue") != null && m.get("minvalue") != null
                && Float.valueOf(m.get("minvalue").toString()) <= value && Float.valueOf(m.get("maxvalue").toString()) > value).findFirst().orElse(new HashMap<>());
        return stringObjectMap.get("code") == null ? "" : stringObjectMap.get("code").toString();
    }


    /**
     * @author: xsm
     * @date: 2022/03/04 0004 上午 09:21
     * @Description: 修改企业综合评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateEntSynthesizeEvaluationInfo", method = RequestMethod.POST)
    public Object updateEntSynthesizeEvaluationInfo(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            List<JSONObject> indexdata = jsonObject.getJSONArray("indexdata");
            EntSynthesizeEvaluationVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntSynthesizeEvaluationVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            List<EntEvaluationDetailVO> list = new ArrayList<>();
            for (JSONObject onejson : indexdata) {
                EntEvaluationDetailVO oneobj = JSONObjectUtil.parseStringToJavaObject(onejson.toString(), EntEvaluationDetailVO.class);
                oneobj.setPkId(UUID.randomUUID().toString());
                oneobj.setUpdatetime(new Date());
                oneobj.setUpdateuser(username);
                oneobj.setFkEntevaluationid(entity.getPkId());
                list.add(oneobj);
            }
            Double total = entSynthesizeEvaluationService.countEvaluationIndex(list);
            entity.setEvaluationscore(total);
            entity.setFkEvaluationlevelcode(getEvaluationLevelCode(total));
            //添加企业评价信息
            entSynthesizeEvaluationService.updateEntSynthesizeEvaluationInfo(entity, list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 获取企业综合评价详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getEntSynthesizeEvaluationDetailById", method = RequestMethod.POST)
    public Object getEntSynthesizeEvaluationDetailById(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> objmap = entSynthesizeEvaluationService.getEntSynthesizeEvaluationDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/08 0008 11:57
     * @Description: 验证是否数据重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsHaveEntSynthesizeEvaluationData", method = RequestMethod.POST)
    public Object IsHaveEntSynthesizeEvaluationData(@RequestJson(value = "pollutionid") String pollutionid,
                                                    @RequestJson(value = "evaluationdate") String evaluationdate
    ) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid", pollutionid);
            param.put("evaluationdate", evaluationdate);
            List<Map<String, Object>> datalist = entSynthesizeEvaluationService.getEntSynthesizeEvaluationListDataByParamMap(param);
            if (datalist.size() > 0) {
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            } else {
                return AuthUtil.parseJsonKeyToLower("success", "no");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 删除企业综合评价信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/deleteEntSynthesizeEvaluationById", method = RequestMethod.POST)
    public Object deleteEntSynthesizeEvaluationById(@RequestJson(value = "id") String id) throws Exception {
        try {
            entSynthesizeEvaluationService.deleteEntSynthesizeEvaluationById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取最新评价企业信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/8 11:43
     */
    @RequestMapping(value = "/getLastEntEvaDataList", method = RequestMethod.POST)
    public Object getLastEntEvaDataList(@RequestJson(value="paramjson",required = false)Object paramjson) throws Exception {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            if (paramjson!=null){
                paramMap = (Map<String, Object>) paramjson;
            }
            PageInfo<Map<String, Object>> pageInfos = entSynthesizeEvaluationService.getLastEntEvaDataListByParam(paramMap);
            if (paramjson!=null){
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("datalist", pageInfos.getList());
                resultMap.put("total", pageInfos.getTotal());
                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
            }else {
                List<Map<String,Object>> dataList = pageInfos.getList();
                for (Map<String, Object> dataMap : dataList) {
                    dataMap.put("evaluationscore", DataFormatUtil.subZeroAndDot(dataMap.get("evaluationscore").toString()));
                }
                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, dataList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计企业评价数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/8 11:43
     */
    @RequestMapping(value = "/countEntEvaDataList", method = RequestMethod.POST)
    public Object countEntEvaDataList() throws Exception {
        try {

            List<Map<String, Object>> resultList = entSynthesizeEvaluationService.countEntEvaDataList();

            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计企业行政区划评价数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/8 11:43
     */
    @RequestMapping(value = "/countEntRegionEvaDataList", method = RequestMethod.POST)
    public Object countEntRegionEvaDataList() throws Exception {
        try {
            Map<String,List<Map<String,Object>>> regionAndList = new HashMap<>();
            List<Map<String,Object>> list;
            String regioncode;
            Map<String,Object> codeAndName = new HashMap<>();
            List<Map<String, Object>> dataList = entSynthesizeEvaluationService.getEntRegionEvaDataList();
            for (Map<String,Object> dataMap:dataList){
                regioncode = dataMap.get("regioncode").toString();
                if (regionAndList.get(regioncode)!=null){
                    list = regionAndList.get(regioncode);
                }else {
                    list = new ArrayList<>();
                }
                codeAndName.put(regioncode,dataMap.get("regionname"));
                list.add(dataMap);
                regionAndList.put(regioncode,list);
            }
            List<Map<String,Object>> resultList = new ArrayList<>();
            for (String codeIndex:regionAndList.keySet()){
                Map<String,Object>  resultMap = new HashMap<>();
                resultMap.put("regioncode",codeIndex);
                resultMap.put("regionname",codeAndName.get(codeIndex));
                dataList = regionAndList.get(codeIndex);
                for (Map<String,Object> dataMap:dataList){
                    dataMap.remove("regioncode");
                    dataMap.remove("regionname");
                }
                resultMap.put("datalist",dataList);
                resultList.add(resultMap);
            }


            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    private void setNum(Map<String, Object> resultMap, List<Map<String, Object>> dataList) {
        if (resultMap.get("MinValue") != null && resultMap.get("MaxValue") != null) {
            Double MinValue = Double.parseDouble(resultMap.get("MinValue").toString());
            Double MaxValue = Double.parseDouble(resultMap.get("MaxValue").toString());
            int countnum = 0;
            Double evaluationscore;
            for (Map<String, Object> dataMap : dataList) {
                if (dataMap.get("evaluationscore") != null) {
                    evaluationscore = Double.parseDouble(dataMap.get("evaluationscore").toString());
                    if (MinValue <= evaluationscore && evaluationscore < MaxValue) {
                        countnum++;
                    }
                }
            }
            resultMap.put("countnum",countnum);
        }


    }

}

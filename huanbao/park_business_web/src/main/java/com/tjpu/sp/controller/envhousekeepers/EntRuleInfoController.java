package com.tjpu.sp.controller.envhousekeepers;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.envhousekeepers.EntRuleInfoVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.envhousekeepers.EntRuleInfoService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description: 企业规章制度处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("entRuleInfo")
public class EntRuleInfoController {


    @Autowired
    private EntRuleInfoService entRuleInfoService;
    @Autowired
    private FileInfoService fileInfoService;


    /**
     * @Description: 添加或更新规章制度信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateEntRuleInfo", method = RequestMethod.POST)
    public Object addOrUpdateEntRuleInfo(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            EntRuleInfoVO entRuleInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntRuleInfoVO());
            entRuleInfoVO.setUpdatetime(new Date());
            entRuleInfoVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(entRuleInfoVO.getPkId())) {//更新操作
                entRuleInfoService.updateInfo(entRuleInfoVO);
            } else {//添加操作
                entRuleInfoVO.setPkId(UUID.randomUUID().toString());
                entRuleInfoService.insertInfo(entRuleInfoVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取规章制度列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getEntRuleListDataByParamMap", method = RequestMethod.POST)
    public Object getEntRuleListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entRuleInfoService.getEntRuleListDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            //获取相关附件信息（待处理）
            if (datalist.size() > 0) {
                List<String> fileIds = new ArrayList<>();
                String fileId;
                for (Map<String, Object> dataMap : datalist) {
                    if (dataMap.get("fk_fileid") != null) {
                        fileIds.add(dataMap.get("fk_fileid").toString());
                    }
                }
                if (fileIds.size() > 0) {
                    jsonObject.clear();
                    jsonObject.put("fileflags", fileIds);
                    jsonObject.put("businesstype", "37");
                    List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfosByParam(jsonObject);
                    if (fileInfoVOS.size() > 0) {
                        Map<String, List<Map<String, Object>>> idAndFileList = new HashMap<>();
                        List<Map<String, Object>> fileList;
                        for (FileInfoVO fileInfoVO : fileInfoVOS) {
                            fileId = fileInfoVO.getFileflag();
                            if (idAndFileList.containsKey(fileId)) {
                                fileList = idAndFileList.get(fileId);
                            } else {
                                fileList = new ArrayList<>();
                            }
                            Map<String, Object> fileMap = new HashMap<>();
                            fileMap.put("fileid", fileInfoVO.getFilepath());
                            fileMap.put("filename", fileInfoVO.getOriginalfilename());
                            fileList.add(fileMap);
                            idAndFileList.put(fileId, fileList);
                        }
                        for (Map<String, Object> dataMap : datalist) {
                            if (dataMap.get("fk_fileid") != null) {
                                fileId = dataMap.get("fk_fileid").toString();
                                dataMap.put("fileDataList", idAndFileList.get(fileId));
                            }
                        }
                    }


                }
            }
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
     * @Description: 根据ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public Object deleteAppSuggestionById(
            @RequestJson(value = "id") String id
    ) {
        try {
            entRuleInfoService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取制度树形结构数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getRuleTypeTree", method = RequestMethod.POST)
    public Object getRuleTypeTree(@RequestJson(value = "pollutionid")String pollutionid) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> ruleTypeList = entRuleInfoService.getAllRuleTypeList();
            if (ruleTypeList.size() > 0) {
                Map<String,Object> paramMap = new HashMap<>();
                paramMap.put("pollutionid",pollutionid);
                List<Map<String, Object>> ruleNumList = entRuleInfoService.getRuleTypeNum(paramMap);
                Map<String, Object> codeAndNum = new HashMap<>();
                for (Map<String, Object> ruleNum : ruleNumList) {
                    codeAndNum.put(ruleNum.get("code").toString(), ruleNum.get("num"));
                }
                for (Map<String, Object> ruleType : ruleTypeList) {
                    if (ruleType.get("parentcode").equals("0")) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("ruletypecode", ruleType.get("code"));
                        resultMap.put("ruletypename", ruleType.get("name"));
                        List<Map<String, Object>> chlidren = new ArrayList<>();
                        for (Map<String, Object> rule : ruleTypeList) {
                            if (rule.get("parentcode").equals(ruleType.get("code"))) {
                                Map<String, Object> chlidMap = new HashMap<>();
                                chlidMap.put("ruletypecode", rule.get("code"));
                                chlidMap.put("ruletypename", rule.get("name"));
                                chlidMap.put("num", codeAndNum.get(rule.get("code")));
                                chlidMap.put("orderindex", rule.get("orderindex")!=null?rule.get("orderindex"):9999);
                                chlidren.add(chlidMap);
                            }
                        }
                        //排序
                        if (chlidren.size()>0){
                            chlidren = chlidren.stream().sorted(
                                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString())
                                    )).collect(Collectors.toList());
                        }
                        resultMap.put("chlilren", chlidren);
                        resultMap.put("orderindex", ruleType.get("orderindex")!=null?ruleType.get("orderindex"):9999);
                        resultList.add(resultMap);

                    }
                }

            }
            if (resultList.size()>0){
                resultList = resultList.stream().sorted(
                        Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString())
                        )).collect(Collectors.toList());

            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

package com.tjpu.sp.controller.envhousekeepers;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.envhousekeepers.EntExecuteReportVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.envhousekeepers.EntExecuteReportService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 企业执行报告处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("entExecuteReport")
public class EntExecuteReportController {


    @Autowired
    private EntExecuteReportService entExecuteReportService;
    @Autowired
    private FileInfoService fileInfoService;


    /**
     * @Description: 添加或更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateData", method = RequestMethod.POST)
    public Object addOrUpdateData(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            EntExecuteReportVO entExecuteReportVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntExecuteReportVO());
            entExecuteReportVO.setUpdatetime(new Date());
            entExecuteReportVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(entExecuteReportVO.getPkId())) {//更新操作
                entExecuteReportService.updateInfo(entExecuteReportVO);
            } else {//添加操作
                entExecuteReportVO.setPkId(UUID.randomUUID().toString());
                entExecuteReportService.insertInfo(entExecuteReportVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getEntExecuteReportListDataByParamMap", method = RequestMethod.POST)
    public Object getEntExecuteReportListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entExecuteReportService.getListDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            //获取相关附件信息
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
                    jsonObject.put("businesstype", "38");
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
            entExecuteReportService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取企业最新执行报告信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/10/18 9:02
     */
    @RequestMapping(value = "getEntLastExecuteDataByParamMap", method = RequestMethod.POST)
    public Object getEntLastExecuteDataByParamMap(@RequestJson(value = "paramjson") Object paramjson) {
        try {
            Map<String, Object> paramMap = (Map<String, Object>) paramjson;
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(paramMap.get("pagenum").toString()), Integer.valueOf(paramMap.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = entExecuteReportService.getEntLastExecuteDataByParamMap(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            if (dataList.size() > 0) {
                for (Map<String, Object> dataMap : dataList) {
                    if (dataMap.get("reporttype") != null) {
                        dataMap.put("reporttypename", CommonTypeEnum.EntStandingBookTypeEnum.getNameByCode(dataMap.get("reporttype").toString()));
                    }

                }
            }
            Map<String,Object> resultMap = new HashMap<>();
            long total = pageInfo.getTotal();
            resultMap.put("datalist", dataList);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

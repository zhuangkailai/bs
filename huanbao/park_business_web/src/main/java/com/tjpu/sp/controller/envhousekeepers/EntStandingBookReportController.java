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
import com.tjpu.sp.model.envhousekeepers.EntStandingBookReportVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.envhousekeepers.EntStandingBookReportService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 企业台账报告控制层
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("entStandingBookReport")
public class EntStandingBookReportController {


    @Autowired
    private EntStandingBookReportService entStandingBookReportService;
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
            EntStandingBookReportVO entStandingBookReportVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntStandingBookReportVO());
            entStandingBookReportVO.setUpdatetime(new Date());
            entStandingBookReportVO.setUpdateuser(username);
            entStandingBookReportVO.setUploadtime(new Date());
            entStandingBookReportVO.setUploaduser(username);
            if (StringUtils.isNotBlank(entStandingBookReportVO.getPkId())) {//更新操作
                entStandingBookReportService.updateInfo(entStandingBookReportVO);
            } else {//添加操作
                entStandingBookReportVO.setPkId(UUID.randomUUID().toString());
                entStandingBookReportService.insertInfo(entStandingBookReportVO);
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
    @RequestMapping(value = "getListDataByParamMap", method = RequestMethod.POST)
    public Object getListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entStandingBookReportService.getListDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            //获取相关附件信息
            if (datalist.size() > 0) {
                List<String> fileIds = new ArrayList<>();
                String fileId;
                for (Map<String, Object> dataMap : datalist) {
                    if (dataMap.get("reporttype") != null) {
                        dataMap.put("reporttypename", CommonTypeEnum.EntStandingBookTypeEnum.getNameByCode(dataMap.get("reporttype").toString()));
                    }
                    if (dataMap.get("fk_fileid") != null) {
                        fileIds.add(dataMap.get("fk_fileid").toString());
                    }
                }
                if (fileIds.size() > 0) {
                    jsonObject.clear();
                    jsonObject.put("fileflags", fileIds);
                    jsonObject.put("businesstype", "56");
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
     * @Description: 获取企业台账（手工报告、执行报告、其他台账）列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getAllEntStandingByParamMap", method = RequestMethod.POST)
    public Object getAllEntStandingByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entStandingBookReportService.getAllEntStandingByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            //获取相关附件信息
            if (datalist.size() > 0) {
                List<String> fileIds = new ArrayList<>();
                String fileId;
                for (Map<String, Object> dataMap : datalist) {
                    if (dataMap.get("reporttype") != null) {
                        dataMap.put("reporttypename", CommonTypeEnum.EntStandingBookTypeEnum.getNameByCode(dataMap.get("reporttype").toString()));
                    }
                    if (dataMap.get("fk_fileid") != null) {
                        fileIds.add(dataMap.get("fk_fileid").toString());
                    }
                }
                if (fileIds.size() > 0) {
                    jsonObject.clear();
                    jsonObject.put("fileflags", fileIds);
                    jsonObject.put("businesstypes", Arrays.asList("56", "38", "39"));
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
            entStandingBookReportService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/02 0002 下午 14:18
     * @Description: 通过id获取回显信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntStandingBookDetailByID", method = RequestMethod.POST)
    public Object getEntStandingBookDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = entStandingBookReportService.getEntStandingBookDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计企业台账数据（执行报告、手工监测报告、生产设施、废气治理设施、废水治理设施、燃料使用记录）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "countEntStandingBookData", method = RequestMethod.POST)
    public Object countEntStandingBookData(@RequestJson(value = "pollutionid") String pollutionid) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> dataList = entStandingBookReportService.countEntStandingBookData(pollutionid);
            Map<String, Integer> codeAndNum = new HashMap<>();
            if (dataList.size() > 0) {
                for (Map<String, Object> dataMap : dataList) {
                    codeAndNum.put(dataMap.get("reporttype") + "", Integer.parseInt(dataMap.get("countnum").toString()));
                }
            }
            for (CommonTypeEnum.EntStandingBookTypeEnum transactType : CommonTypeEnum.EntStandingBookTypeEnum.values()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("countnum", codeAndNum.get(transactType.getCode()) != null ? codeAndNum.get(transactType.getCode()) : 0);
                dataMap.put("countcode", transactType.getCode());
                dataMap.put("countname", transactType.getName());
                resultList.add(dataMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取台账下拉列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "getEntStandingType", method = RequestMethod.POST)
    public Object getEntStandingType() {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (CommonTypeEnum.EntStandingBookTypeEnum transactType : CommonTypeEnum.EntStandingBookTypeEnum.values()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("code", transactType.getCode());
                dataMap.put("name", transactType.getName());
                resultList.add(dataMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取所有企业台账更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "getUpdateDataByParamMap", method = RequestMethod.POST)
    public Object getUpdateDataByParamMap(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "enttime") String enttime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("enttime", enttime);
            List<Map<String, Object>> dataList = entStandingBookReportService.getUpdateDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取所有企业台账未更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "getNoUpdateDataByParamMap", method = RequestMethod.POST)
    public Object getNoUpdateDataByParamMap(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "enttime") String enttime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("enttime", enttime);
            List<Map<String, Object>> dataList = entStandingBookReportService.getNoUpdateDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取企业最新台账信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/10/18 9:02
     */
    @RequestMapping(value = "getEntLastStandingDataByParamMap", method = RequestMethod.POST)
    public Object getEntLastStandingDataByParamMap(@RequestJson(value = "paramjson") Object paramjson) {
        try {
            Map<String, Object> paramMap = (Map<String, Object>) paramjson;
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(paramMap.get("pagenum").toString()), Integer.valueOf(paramMap.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = entStandingBookReportService.getEntLastStandingDataByParamMap(paramMap);
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

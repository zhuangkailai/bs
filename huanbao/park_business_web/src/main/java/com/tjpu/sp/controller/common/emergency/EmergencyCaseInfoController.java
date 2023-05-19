package com.tjpu.sp.controller.common.emergency;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.common.emergency.EmergencyCaseInfoVO;
import com.tjpu.sp.model.common.standard.StandardInfoVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.emergency.EmergencyCaseInfoService;
import com.tjpu.sp.service.common.standard.StandardInfoService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 应急案例库处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("emergencyCaseInfo")
public class EmergencyCaseInfoController {


    @Autowired
    private EmergencyCaseInfoService emergencyCaseInfoService;

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
            EmergencyCaseInfoVO emergencyCaseInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EmergencyCaseInfoVO());
            emergencyCaseInfoVO.setUpdatetime(new Date());
            emergencyCaseInfoVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(emergencyCaseInfoVO.getPkId())) {//更新操作
                emergencyCaseInfoService.updateInfo(emergencyCaseInfoVO);
            } else {//添加操作
                emergencyCaseInfoVO.setPkId(UUID.randomUUID().toString());
                emergencyCaseInfoService.insertInfo(emergencyCaseInfoVO);
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
    public Object getListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson){
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = emergencyCaseInfoService.getListDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            //附件信息
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
                    jsonObject.put("businesstype","13");
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
    public Object deleteById(
            @RequestJson(value = "id") String id
    ) {
        try {
            emergencyCaseInfoService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取编辑回显或详情数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "getEditOrDetailsDataById", method = RequestMethod.POST)
    public Object getEditOrDetailsDataById(@RequestJson(value = "id") String id
                                   ) {
        try {
            Map<String,Object> resultMap = emergencyCaseInfoService.getEditOrDetailsDataById(id);
            if (resultMap.get("fkfileid")!=null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.clear();
                jsonObject.put("fileflags", Arrays.asList(resultMap.get("fkfileid")));
                jsonObject.put("businesstype", "13");
                List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfosByParam(jsonObject);
                if (fileInfoVOS.size() > 0) {
                    List<Map<String, Object>> fileList = new ArrayList<>();
                    for (FileInfoVO fileInfoVO : fileInfoVOS) {
                        Map<String, Object> fileMap = new HashMap<>();
                        fileMap.put("fileid", fileInfoVO.getFilepath());
                        fileMap.put("filename", fileInfoVO.getOriginalfilename());
                        fileList.add(fileMap);
                    }
                    resultMap.put("fileDataList",fileList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




}

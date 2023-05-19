package com.tjpu.sp.controller.common.standard;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.common.standard.StandardInfoVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.standard.StandardInfoService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 标准信息表
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("standardInfo")
public class StandardInfoController {


    @Autowired
    private StandardInfoService standardInfoService;

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
            StandardInfoVO standardInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new StandardInfoVO());
            standardInfoVO.setUpdatetime(new Date());
            standardInfoVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(standardInfoVO.getPkStandardid())) {//更新操作
                standardInfoService.updateInfo(standardInfoVO);
            } else {//添加操作
                standardInfoVO.setPkStandardid(UUID.randomUUID().toString());
                standardInfoService.insertInfo(standardInfoVO);
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
    public Object getListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = standardInfoService.getListDataByParamMap(jsonObject);
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
                    jsonObject.put("businesstype", "13");
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
            standardInfoService.deleteInfoById(id);
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
    public Object getEditOrDetailsDataById(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> resultMap = standardInfoService.getEditOrDetailsDataById(id);
            if (resultMap.get("fkfileid") != null) {
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
                    resultMap.put("fileDataList", fileList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计知识库数据（标准、科普知识、应急案例）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "countKnowledgeData", method = RequestMethod.POST)
    public Object countKnowledgeData() {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> dataList = standardInfoService.countKnowledgeData();
            if (dataList.size() > 0) {
                List<Map<String, Object>> treeList = DataFormatUtil.buildTree(dataList, "root", "parentcode", "countcode");
                List<Map<String, Object>> children;
                for (Map<String, Object> treeMap : treeList) {
                    if (treeMap.get("children") != null) {
                        children = (List<Map<String, Object>>) treeMap.get("children");
                        setCountNum(treeMap,children);
                    }
                }
                resultList = treeList;
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    private void setCountNum(Map<String, Object> parentMap,List<Map<String, Object>> children) {
        int countnum = 0;
        for (Map<String, Object> treeMap : children) {
             countnum += Integer.parseInt(treeMap.get("countnum").toString());
            if (treeMap.get("children") != null) {
                children = (List<Map<String, Object>>) treeMap.get("children");
                setCountNum(treeMap,children);
            }
        }
        parentMap.put("countnum",countnum);
    }




}

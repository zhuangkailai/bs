package com.tjpu.sp.controller.environmentalprotection.knowledgestore;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.FileController;
import com.tjpu.sp.model.base.knowledgestore.KnowledgeStoreInfo;
import com.tjpu.sp.service.environmentalprotection.knowledgestore.KnowledgeStoreService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping(value = "knowledgeStore")
public class KnowledgeStoreController {

    private final KnowledgeStoreService knowledgeStoreService;
    private final PublicSystemMicroService publicSystemMicroService;
    private final FileController fileController;


    private static final String sysModel = "knowledgeManagement";

    public KnowledgeStoreController(KnowledgeStoreService knowledgeStoreService, PublicSystemMicroService publicSystemMicroService, FileController fileController) {
        this.knowledgeStoreService = knowledgeStoreService;
        this.publicSystemMicroService = publicSystemMicroService;
        this.fileController = fileController;
    }


    @RequestMapping(value = "deleteByPrimaryKey", method = RequestMethod.POST)
    public Object deleteByPrimaryKey(@RequestJson(value = "id") String pkId) {
        try {
            int i = knowledgeStoreService.deleteByPrimaryKey(pkId);
            return AuthUtil.parseJsonKeyToLower("success", i);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "insertSelective", method = RequestMethod.POST)
    public Object insertSelective(@RequestJson(value = "knowledgestoreinfo") KnowledgeStoreInfo record) {
        try {
            Assert.notNull(record.getStorename(), "StoreName must not be null!");
            String pkid = UUID.randomUUID().toString();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            record.setUpdateuser(username);
            record.setPkId(pkid);
            record.setUpdatetime(new Date());
            int i = knowledgeStoreService.insertSelective(record);
            return AuthUtil.parseJsonKeyToLower("success", i);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "getKnowledgeStoresByParam", method = RequestMethod.POST)
    public Object getKnowledgeStoresByParam(@RequestJson(value = "storetypecode", required = false) String storetypecode,
                                            @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                            @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                            @RequestJson(value = "keywords", required = false) String keywords,
                                            @RequestJson(value = "publishunit", required = false) String publishunit,
                                            @RequestJson(value = "storename", required = false) String storename) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("storetypecode", storetypecode);
            paramMap.put("publishunit", publishunit);
            paramMap.put("storename", storename);
            paramMap.put("keywords", keywords);
            Map<String, Object> result = knowledgeStoreService.getKnowledgeStoresByParam(paramMap, pagesize, pagenum);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/9/9 0009 下午 2:34
     * @Description: 自定义查询条件获取知识库/法律法规列表数据（文件）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getKnowledgeStoresDataFileByParam", method = RequestMethod.POST)
    public Object getKnowledgeStoresDataFileByParam(
            @RequestJson(value = "storetmark",required = false) String storetmark,
            @RequestJson(value = "storetypecode",required = false) String storetypecode,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "storename", required = false) String storename) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNotBlank(storetmark)){
                paramMap.put("storetmark", storetmark);
            }
            if (StringUtils.isNotBlank(storetypecode)){
                paramMap.put("storetypecode", storetypecode);
            }
            paramMap.put("storename", storename);
            if (pagesize != null && pagenum != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> dataList = knowledgeStoreService.getKnowledgeStoresDataListByParam(paramMap);
            if (dataList.size() > 0) {
                List<String> fileIds = new ArrayList<>();
                String fileId;
                for (Map<String, Object> dataMap : dataList) {
                    if (dataMap.get("FileID") != null) {
                        fileIds.add(dataMap.get("FileID").toString());
                    }
                }
                if (fileIds.size() > 0) {
                    Map<String, List<Map<String, Object>>> idAndData = fileController.getFileIdAndData(fileIds, "13");
                    for (Map<String, Object> dataMap : dataList) {
                        if (dataMap.get("FileID") != null) {
                            fileId = dataMap.get("FileID").toString();
                            dataMap.put("fileDataList", idAndData.get(fileId));
                        }
                    }
                }
            }
            //获取分页信息
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            resultMap.put("total", pageInfo.getTotal());
            resultMap.put("datalist", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/9/3 16:48
     * @Description: 获取知识库类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getKnowledgeStoresType", method = RequestMethod.POST)
    public Object getKnowledgeStoresType() {
        try {
            List<Map<String, Object>> result = knowledgeStoreService.getKnowledgeStoresType();
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "updateByPrimaryKeySelective", method = RequestMethod.POST)
    public Object updateByPrimaryKeySelective(@RequestJson(value = "knowledgestoreinfo") KnowledgeStoreInfo record) {
        try {
            Assert.notNull(record.getStorename(), "StoreName must not be null!");
            int i = knowledgeStoreService.updateByPrimaryKeySelective(record);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            record.setUpdateuser(username);
            record.setUpdatetime(new Date());
            return AuthUtil.parseJsonKeyToLower("success", i);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:26
     * @Description: 获取知识库添加页面
     * @param:
     * @return:
     */
    @RequestMapping(value = "getKnowledgeStoreAddPage", method = RequestMethod.POST)
    public Object getKnowledgeStoreAddPage() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.getAddPageInfo(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "addKnowledgeStore", method = RequestMethod.POST)
    public Object addKnowledgeStore(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysModel);
            String Param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.doAddMethod(Param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "getKnowledgeStoreUpdatePageByID", method = RequestMethod.POST)
    public Object getKnowledgeStoreUpdatePageByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            paramMap.put("pk_id", id);
            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.goUpdatePage(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "updateKnowledgeStore", method = RequestMethod.POST)
    public Object updateKnowledgeStore(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysModel);
            String Param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.doEditMethod(Param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "getKnowledgeStoreDetailByID", method = RequestMethod.POST)
    public Object getKnowledgeStoreDetailByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            paramMap.put("pk_id", id);
            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.getDetail(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/24 0024 下午 4:36
     * @Description:分组统计各类别知识库信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "countKnowledgeStoreGroupByStoreType", method = RequestMethod.POST)
    public Object countKnowledgeStoreGroupByStoreType() throws Exception {
        try {
            List<Map<String, Object>> maps = knowledgeStoreService.countKnowledgeStoreGroupByStoreType();
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

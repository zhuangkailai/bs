package com.tjpu.sp.controller.extand;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.fileconfig.BusinessTypeConfig;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.extand.AppVersionService;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/9/25 0025 11:35
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("appVersion")
public class AppVersionController {


    // 使用的数据库
    private MongoDatabase useDatabase;
    // bucket
    private GridFSBucket gridFSBucket;

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;

    @Autowired
    private AppVersionService appVersionService;

    private String sysmodel = "appVersionManagement";
    private String pk_id = "pk_id";
    private String listfieldtype = "list-version";

    @Autowired
    @Qualifier("secondMongoTemplate")
    private MongoTemplate mongoTemplate;

    private final String businesstype = "14";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: liyc
     * @date:2019/9/25 0025 11:42
     * @Description: 获取app初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "getAppVersionListPage", method = RequestMethod.POST)
    public Object getAppVersionListPage(HttpServletRequest request, HttpSession session) {
        try {
            Map<String, Object> datas = new HashMap<>();
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("queryfieldtype", "query-version");
            String param = AuthUtil.paramDataFormat(paramMap);
            //表头数据
            Object tableTitle = publicSystemMicroService.getTableTitle(param);
            JSONObject jsonObject = JSONObject.fromObject(tableTitle);
            String titleData = jsonObject.getString("data");
            //按钮数据
            paramMap.put("sysmodel", "appVersionManagement");//获取父级
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject4 = jsonObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject4.getString("data");
            JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
            String topoperations = jsonObject5.getString("topbuttondata");
            String listoperation = jsonObject5.getString("tablebuttondata");
            Map<String, Object> buttondatamap = new HashMap<>();//按钮
            buttondatamap.put("topbuttondata", topoperations);
            buttondatamap.put("tablebuttondata", listoperation);
            //查询条件数据
            Map<String, Object> querydata = new HashMap<>();
            Object queryCriteriaData = publicSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject2 = jsonObject.fromObject(queryCriteriaData);
            String queryData = jsonObject2.getString("data");
            JSONObject jsonObject3 = JSONObject.fromObject(queryData);
            String dualcontrolskey = jsonObject3.getString("dualcontrolskey");
            String querycontroldata = jsonObject3.getString("querycontroldata");
            String queryformdata = jsonObject3.getString("queryformdata");
            querydata.put("dualcontrolskey", dualcontrolskey);
            querydata.put("querycontroldata", querycontroldata);
            querydata.put("queryformdata", queryformdata);
            // 分页
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
            }
            Map<String, Object> tabledata = new HashMap<>();
            List<Map<String, Object>> dataList = appVersionService.getVersionListByParam(paramMap);
            // 保存分页信息
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            // 分页后的数据
            List<Map<String, Object>> listInfo = pageInfo.getList();
            tabledata.put("pagesize", pageInfo.getPageSize());// 每页条数
            tabledata.put("pagenum", pageInfo.getPageNum());// 当前页
            tabledata.put("total", pageInfo.getTotal());// 总条数
            tabledata.put("pages", pageInfo.getPages());// 总页数
            tabledata.put("total", dataList.size());// 总条数
            tabledata.put("primarykey", pk_id);// 主键
            tabledata.put("tablelistdata", dataList);// 数据
            tabledata.put("tabletitledata", titleData);// 表头
            //返回数据
            datas.put("querydata", querydata);
            datas.put("tabledata", tabledata);
            datas.put("buttondata", buttondatamap);
            return AuthUtil.parseJsonKeyToLower("success", datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author:liyc
     * @date:2019/10/11 0011 13:51
     * @Description: 获取最大的版本号+1
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAppVersionMaxVersion", method = RequestMethod.POST)
    public Object getAppVersionMaxVersion(@RequestJson(value = "apptype", required = false) String appType) {
        try {

            Map<String, Object> paramMap = new HashMap<>();

            if (StringUtils.isNotBlank(appType)) {
                paramMap.put("apptype", appType);
            }
            String maxVersion = appVersionService.getAppVersionMaxVersion(paramMap);
            if (maxVersion != null && maxVersion != "") {
                String[] strs = maxVersion.split("[.]");
                String numStr = strs[strs.length - 1];
                if (numStr != null && numStr.length() > 0) {
                    int num = numStr.length();
                    int x = Integer.parseInt(numStr) + 1;
                    String added = String.valueOf(x);
                    num = Math.min(num, added.length());
                    String datas = maxVersion.subSequence(0, maxVersion.length() - num) + added;
                    return AuthUtil.parseJsonKeyToLower("success", datas);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", "1.0");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/12/15 0015 上午 9:40
     * @Description: 验证版本号重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "isTableDataHaveInfo", method = RequestMethod.POST)
    public Object isTableDataHaveInfo(
            @RequestJson(value = "versioncode") String versioncode,
            @RequestJson(value = "apptype") String appType,
            @RequestJson(value = "appid", required = false) String appid) {
        try {

            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("apptype", appType);
            paramMap.put("versioncode", versioncode);

            List<Map<String, Object>> dataList = appVersionService.getVersionListByParam(paramMap);
            String flag = "no";
            if (dataList.size() > 0) {
                flag = "yes";
                Map<String, Object> dataMap = dataList.get(0);
                if (StringUtils.isNotBlank(appid)) {//编辑操作
                    if (appid.equals(dataMap.get("pk_id"))) {
                        flag = "no";
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/29 0029 9:38
     * @Description: 根据自定义参数获取APP管理列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No [map]
     * @throws:
     */
    @RequestMapping(value = "getVersionByParamMap", method = RequestMethod.POST)
    public Object getVersionListByParam(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                int pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
                int pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> listData = appVersionService.getVersionListByParam(paramMap);
            PageInfo<Map<String, Object>> page = new PageInfo<>(listData);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultMap.put("pageSize", page.getPageSize());
                resultMap.put("pageNum", page.getPageNum());
                resultMap.put("pages", page.getPages());
            }
            //总条数
            resultMap.put("total", page.getTotal());
            resultMap.put("tablelistdata", listData);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/29 0029 10:23
     * @Description: 获取app版本管理新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAppVersionAddPage", method = RequestMethod.POST)
    public Object getAppVersionAddPage() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: liyc
     * @date:2019/9/29 0029 10:36
     * @Description: 新增APP版本管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No [request]
     * @throws:
     */
    @RequestMapping(value = "addAppVersion", method = RequestMethod.POST)
    public Object addAppVersion(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/30 0030 11:30
     * @Description: 通过主键id删除App版本管理单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteVersionByID", method = RequestMethod.POST)
    public Object deleteVersionByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<Object, Object> parammap = new HashMap<>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            parammap.put("pk_id", id);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/30 0030 11:40
     * @Description: 根据主键ID获取App版本管理修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVersionUpdatePageByID", method = RequestMethod.POST)
    public Object getVersionUpdatePageByID(@RequestJson(value = "pk_id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: liyc
     * @date:2019/9/30 0030 11:47
     * @Description: 根据id修改App管理的一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateVersionByID", method = RequestMethod.POST)
    public Object updateVersionByID(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: liyc
     * @date:2019/9/30 0030 13:29
     * @Description: 根据App版本管理主键ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVersionDetailByID", method = RequestMethod.POST)
    public Object getVersionDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018/9/3 0003 下午 3:07
     * @Description: 通过文件ID下载apk文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: file_id：
     * @return:
     */
    @GetMapping(value = "/downloadAPKFileByFileId")
    public void downloadAPKFileByFileId(
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        String file_id = request.getParameter("file_id");
        //1，初始化gridFSBucket
        gridFSBucket = initGridFSBucket(businesstype);
        GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(file_id)));
        GridFSFile gridFSFile = gridFSFindIterable.first();
        if (gridFSFile == null) {
            return;
        } else {
            GridFsResource gridFsResource = convertGridFSFile2Resource(gridFSFile);
            String fileName = gridFsResource.getFilename().replace(",", "");
            //处理中文文件名乱码
            String agent = request.getHeader("USER-AGENT").toLowerCase();
            response.setContentType("application/octet- stream");
            response.setCharacterEncoding("utf-8");
            String codedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            codedFileName = codedFileName.replaceAll("\\+", "%20");
            if (agent.contains("firefox")) {
                response.setCharacterEncoding("utf-8");
                response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1") + ".xls");
            } else if (agent.contains("IE")) {
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "No-cache");
                response.setDateHeader("Expires", 0);
                response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName);
            } else {
                response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName);
            }
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("isStream", "true");
            IOUtils.copy(gridFsResource.getInputStream(), response.getOutputStream());
        }
    }


    /**
     * @author: lip
     * @date: 2018/9/3 0003 下午 3:06
     * @Description: GridFSFile 转换成 GridFsResource
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private GridFsResource convertGridFSFile2Resource(GridFSFile gridFsFile) {
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFsFile.getObjectId());
        return new GridFsResource(gridFsFile, gridFSDownloadStream);
    }

    /**
     * @author: lip
     * @date: 2018/8/31 0031 下午 4:51
     * @Description:GridFSBucket初始化方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:fileId:文件类型标记
     * @return:
     */
    private GridFSBucket initGridFSBucket(String businesstype) {
        if (useDatabase == null) {
            useDatabase = mongoTemplate.getDb();
        }
        String collectionType = BusinessTypeConfig.businessTypeMap.get(businesstype);
        return GridFSBuckets.create(useDatabase, collectionType);
    }

    /**
     * @author: lip
     * @date: 2019/9/25 0025 下午 1:17
     * @Description: 获取app最新版本信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLastAppVersionInfo", method = RequestMethod.POST)
    public Object getLastVersionInfo(@RequestJson(value = "apptype", required = false) String apptype) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isBlank(apptype)) {
                apptype = "1";
            }
            paramMap.put("apptype", apptype);
            List<Map<String, Object>> lastAppVersionList = appVersionService.getLastAppVersionInfo(paramMap);
            if (lastAppVersionList.size() > 0) {
                resultMap = lastAppVersionList.get(0);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/9/25 0025 下午 1:17
     * @Description: 获取app最新版本信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAppVersionInfoByParam", method = RequestMethod.POST)
    public Object getAppVersionInfoByParam(
            @RequestJson(value = "apptype") String apptype,
            @RequestJson(value = "versionnum") String versionnum) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("versioncode", versionnum);
            paramMap.put("apptype", apptype);
            List<Map<String, Object>> appDataList = appVersionService.getVersionListByParam(paramMap);
            if (appDataList.size() > 0) {
                resultMap = appDataList.get(0);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

package com.tjpu.sp.controller.common;


import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AsposeUtil;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.extand.AppVersionService;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

import static com.tjpu.sp.config.fileconfig.BusinessTypeConfig.businessTypeMap;

@RestController
@RequestMapping("file")
@Api(value = "文件处理类", tags = "文件处理类：包含文件上传，文件下载等")
public class FileController {


    //图片类型数据集合
    public static List<String> imgList = new ArrayList<>();
    public static List<String> videoList = new ArrayList<>();
    // 使用的数据库
    private MongoDatabase useDatabase;
    // bucket
    private GridFSBucket gridFSBucket;

    @Autowired
    @Qualifier("secondMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Value("${spring.datasource.primary.name}")
    private String dataSource;

    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private AppVersionService appVersionService;


    private final PublicSystemMicroService publicSystemMicroService;


    @Autowired
    public FileController(PublicSystemMicroService publicSystemMicroService) {
        this.publicSystemMicroService = publicSystemMicroService;

    }


    static {
        imgList.add("BMP");
        imgList.add("JPG");
        imgList.add("JPEG");
        imgList.add("PNG");
        imgList.add("GIF");
    }

    static {
        videoList.add("wmv");
        videoList.add("rm");
        videoList.add("rmvb");
        videoList.add("mp4");
        videoList.add("3gp");
        videoList.add("mov");
        videoList.add("m4v");
        videoList.add("avi");
        videoList.add("dat");
        videoList.add("mkv");
        videoList.add("flv");
        videoList.add("vob");
    }

    /**
     * @author: lip
     * @date: 2018/8/31 0031 下午 4:48
     * @Description:文件上传处理方法，支持单个文件，多个文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @return:
     */
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public Object uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("business_type") String businesstype,
                             @RequestParam("fkfileid") String fkfileid,
                             @RequestParam(value = "filename", required = false) String filename,
                             @RequestParam(value = "businessfiletype", required = false) String businessfiletype,
                             HttpSession session) throws Exception {


        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> fileParam = new HashMap<>();
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String userName = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            fileParam.put("user_id", userId);
            fileParam.put("user_name", userName);

            fileParam.put("business_type", businesstype);
            fileParam.put("fk_fileid", fkfileid);
            //判断是否为视频文件
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            //1，初始化gridFSBucket
            gridFSBucket = initGridFSBucket(businesstype);
            //2，初始化GridFSUploadOptions
            GridFSUploadOptions options = null;

            fileParam.put("file", file);
            options = initGridFSUploadOptions(fileParam);

            ObjectId objectId = gridFSBucket.uploadFromStream(file.getOriginalFilename(), file.getInputStream(), options);

            if (objectId != null) {//上传成功后，添加文件关联关系
                FileInfoVO fileInfoVO = new FileInfoVO();
                fileInfoVO.setPkFileid(UUID.randomUUID().toString());
                fileInfoVO.setFilepath(objectId.toString());
                fileInfoVO.setBusinessfiletype(businessfiletype);
                fileInfoVO.setBusinesstype(Integer.parseInt(businesstype));
                fileInfoVO.setFileflag(fkfileid);
                fileInfoVO.setFilesize(file.getSize());
                fileInfoVO.setBusinessfiletype(businessfiletype);
                fileInfoVO.setFilename(filename);
                fileInfoVO.setOriginalfilename(originalFilename);
                fileInfoVO.setFileextname(ext);
                Date nowDay = new Date();
                fileInfoVO.setUploadtime(nowDay);
                fileInfoVO.setUploaduser(userName);
                fileInfoService.insert(fileInfoVO);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("originalfilename", file.getOriginalFilename());
            map.put("objectid", objectId.toString());
            dataList.add(map);
            return AuthUtil.returnObject("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }


    }


    /**
     * @Description: 上传文件到磁盘
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/10/11 9:39
     */
    @RequestMapping(value = "/uploadFileToDrive", method = RequestMethod.POST)
    public Object uploadFileToDrive(@RequestParam("file") MultipartFile file,
                                    @RequestParam("business_type") String businesstype,
                                    @RequestParam("fkfileid") String fkfileid,
                                    @RequestParam(value = "filename", required = false) String filename,
                                    @RequestParam(value = "businessfiletype", required = false) String businessfiletype
    ) throws Exception {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            String userName = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            //判断是否为视频文件
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            String rootPath = DataFormatUtil.parseProperties("richtextpath");
            String fileName =originalFilename.replace("."+ext,"")+"_"+System.currentTimeMillis() + "." + ext;
            String filePath = businessTypeMap.get(businesstype) + "/" + fileName;
            File dest = new File(rootPath + filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdir();
            }
            file.transferTo(dest);
            FileInfoVO fileInfoVO = new FileInfoVO();
            fileInfoVO.setPkFileid(UUID.randomUUID().toString());
            fileInfoVO.setFilepath(filePath);
            fileInfoVO.setBusinessfiletype(businessfiletype);
            fileInfoVO.setBusinesstype(Integer.parseInt(businesstype));
            fileInfoVO.setFileflag(fkfileid);
            fileInfoVO.setFilesize(file.getSize());
            fileInfoVO.setBusinessfiletype(businessfiletype);
            fileInfoVO.setFilename(filename);
            fileInfoVO.setOriginalfilename(originalFilename);
            fileInfoVO.setFileextname(ext);
            Date nowDay = new Date();
            fileInfoVO.setUploadtime(nowDay);
            fileInfoVO.setUploaduser(userName);
            fileInfoService.insert(fileInfoVO);
            Map<String, Object> map = new HashMap<>();
            map.put("originalfilename", file.getOriginalFilename());
            map.put("filePath",  filePath);
            map.put("pkid", fileInfoVO.getPkFileid());
            dataList.add(map);
            return AuthUtil.returnObject("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 上传文件到磁盘
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/10/11 9:39
     */
    @RequestMapping(value = "/deleteFileFromDrive", method = RequestMethod.POST)
    public Object deleteFileFromDrive(@RequestJson("business_type") String businesstype,
                                      @RequestJson("filepath") String filepath,
                                      @RequestJson("pkid") String pkid
    ) {

        try {
            fileInfoService.deleteById(pkid);

            String rootPath = DataFormatUtil.parseProperties("richtextpath");
            String filePath = businessTypeMap.get(businesstype) + "/" + filepath;
            File file = new File(rootPath + filePath);
            file.delete();
            return AuthUtil.returnObject("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2018/9/3 0003 下午 3:07
     * @Description:通过objectId和文件定义的类型，从mongodb下载文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "/downloadFile", method = RequestMethod.POST)
    public void downloadFile(@RequestJson("file_id") String fileId,
                             @RequestJson("business_type") String fileType,
                             HttpServletRequest request, HttpServletResponse response) throws Exception {
        //1，初始化gridFSBucket
        gridFSBucket = initGridFSBucket(fileType);
        GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileId)));
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
     * @date: 2018/9/3 0003 下午 3:07
     * @Description:通过objectId和文件定义的类型，从mongodb下载文件 get请求
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/downloadFileGet", method = RequestMethod.GET)
    public void downloadFileGet(
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        String fileType = request.getParameter("business_type");
        String fileId = request.getParameter("file_id");

        //1，初始化gridFSBucket
        gridFSBucket = initGridFSBucket(fileType);
        GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileId)));
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
     * @Description: 生成临时文件
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/8 13:47
     */
    @RequestMapping(value = "/downloadTempFile", method = RequestMethod.POST)
    public Object downloadTempFile(
            @RequestJson("file_id") String fileId,
            @RequestJson("business_type") String fileType) throws Exception {

        try {
            //1，初始化gridFSBucket
            gridFSBucket = initGridFSBucket(fileType);
            GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileId)));
            GridFSFile gridFSFile = gridFSFindIterable.first();
            Map<String, Object> resultMap = new HashMap<>();
            if (gridFSFile == null) {
                resultMap.put("filepath", "");
                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
            } else {
                String rootTemp = DataFormatUtil.parseProperties("richtextpath");

                String tempRoot = "/downloadTempFile/";
                rootTemp = rootTemp + tempRoot;
                File file = new File(rootTemp);
                if (!file.exists()) {
                    file.mkdirs();
                } else {
                    //清理临时文件
                    DataFormatUtil.deleteFile(file);
                    //生成临时文件夹
                }
                GridFsResource gridFsResource = convertGridFSFile2Resource(gridFSFile);
                //获取流中的数据
                InputStream inputStream = gridFsResource.getInputStream();
                String fileName = gridFsResource.getFilename().replace(",", "");
                String time = new Date().getTime() + "";
                String filePath = time + "/" + fileName;
                rootTemp = rootTemp + filePath;
                File f1 = new File(rootTemp);
                if (!f1.exists()) {
                    f1.getParentFile().mkdirs();
                }
                byte[] bytes = new byte[1024];
                // 创建基于文件的输出流
                FileOutputStream fos = new FileOutputStream(f1);
                int len = 0;
                while ((len = inputStream.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                }
                inputStream.close();
                fos.close();
                filePath = tempRoot+filePath;
                resultMap.put("filepath", filePath);

                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }


    }


    /**
     * @author: lip
     * @date: 2018/9/3 0003 下午 3:07
     * @Description:通过objectId和文件定义的类型，通过objectId和文件定义的类型，从mongodb获取文件，转换图片预览
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */


    @RequestMapping(value = "/viewFile", method = RequestMethod.POST)
    public Object viewFile(@RequestJson("file_id") String fileId,
                           @RequestJson("business_type") String fileType
    ) throws Exception {

        try {
            //1，初始化gridFSBucket
            gridFSBucket = initGridFSBucket(fileType);
            GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileId)));
            GridFSFile gridFSFile = gridFSFindIterable.first();
            if (gridFSFile == null) {
                return AuthUtil.parseJsonKeyToLower("success", null);
            }
            GridFsResource gridFsResource = convertGridFSFile2Resource(gridFSFile);
            //判断文件类型:txt,doc,docx,pdf,excel,ppt

            String filename = gridFsResource.getFilename();
            String ext = filename.substring(filename.lastIndexOf(".") + 1);

            String base64_PNG = AsposeUtil.parseFileToBase64(gridFsResource.getInputStream(), 5, ext);
            return AuthUtil.parseJsonKeyToLower("success", base64_PNG);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    @RequestMapping(value = "/getFileInputStream", method = RequestMethod.POST)
    public InputStream getFileInputStream(@RequestJson("file_id") String fileId,
                                          @RequestJson("business_type") String fileType
    ) throws Exception {

        try {
            //1，初始化gridFSBucket
            gridFSBucket = initGridFSBucket(fileType);
            GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileId)));
            GridFSFile gridFSFile = gridFSFindIterable.first();
            GridFsResource gridFsResource = convertGridFSFile2Resource(gridFSFile);
            if (gridFsResource != null) {
                return gridFsResource.getInputStream();
            } else {
                return null;
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2018/9/3 0003 下午 3:07
     * @Description:通过objectId和文件定义的类型，从mongodb删除文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */


    @RequestMapping(value = "/deleteFile", method = RequestMethod.POST)
    public Object deleteFile(@RequestJson("file_id") String fileId,
                             @RequestJson("business_type") String businessType
    ) throws Exception {

        try {
            //1，初始化gridFSBucket
            gridFSBucket = initGridFSBucket(businessType);
            GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileId)));
            GridFSFile gridFSFile = gridFSFindIterable.first();
            if (gridFSFile != null) {
                gridFSBucket.delete(new ObjectId(fileId));
            }
            //2，删除文件关联
            fileInfoService.deleteByFilePath(fileId);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018/9/3 0003 下午 3:07
     * @Description: 通过objectId和文件定义的类型，从mongodb批量删除文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @ApiOperation(value = "文件批量删除", notes = "通过objectId和文件定义的类型，从mongodb删除文件")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "file_ids", value = "文件主键ID数组（必传）", defaultValue = "", required = true, dataType = "List<String>"),
            @ApiImplicitParam(name = "business_type", value = "文件业务相关类型（必传）", defaultValue = "", required = true, dataType = "String")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "/deleteFiles", method = RequestMethod.POST)
    public Object deleteFiles(@RequestJson("file_ids") List<String> fileIds,
                              @RequestJson("business_type") String businessType
    ) throws Exception {
        try {
            gridFSBucket = initGridFSBucket(businessType);
            //1，初始化gridFSBucket

            for (String fileId : fileIds) {

                GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileId)));
                GridFSFile gridFSFile = gridFSFindIterable.first();
                if (gridFSFile != null) {
                    gridFSBucket.delete(new ObjectId(fileId));
                }
                //2，删除文件关联
                fileInfoService.deleteByFilePath(fileId);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
     * @date: 2018/8/31 0031 下午 5:02
     * @Description: 初始化上传配置
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private GridFSUploadOptions initGridFSUploadOptions(Map<String, Object> fileParam) {

        GridFSUploadOptions options = new GridFSUploadOptions();
        //设置分片大小 350kb
        options.chunkSizeBytes(358400);
        MultipartFile file = (MultipartFile) fileParam.get("file");
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        if (ext != null) {//转换小写
            ext = ext.toLowerCase();
        }

        String fileType = getFileType(ext);

        //设置自定义数据文档
        Document document = new Document();
        document.append("content_type", file.getContentType());
        document.append("user_id", fileParam.get("user_id"));
        document.append("user_name", fileParam.get("user_name"));
        document.append("file_ext", ext);
        document.append("file_type", fileType);
        document.append("fk_fileid", fileParam.get("fk_fileid"));
        options.metadata(document);
        return options;
    }

    /**
     * @author: lip
     * @date: 2018/9/4 0004 下午 4:29
     * @Description: 根据文件拓展名获取文件类型：file，img
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getFileType(String ext) {
        List<String> imgList = new ArrayList<>();
        if (imgList.contains(ext.toUpperCase())) {
            return "img";
        } else {
            return "file";
        }
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
        String collectionType = businessTypeMap.get(businesstype);
        return GridFSBuckets.create(useDatabase, collectionType);
    }


    /**
     * @Author: zhangzc
     * @Date: 2018/11/10 16:15
     * @Description: 根据文件标识和文件业务相关类型以及业务类型下的文件类型获取文件信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param: fileflag         文件标识（对应业务表中附件字段所存的ID）（必传）
     * @Param: businesstype     文件业务相关类型（必传）
     * @Param: businessfiletype 业务类型下的文件类型（非必传）
     * @Return:
     */

    @RequestMapping(value = "getFilesInfoByParam", method = RequestMethod.POST)
    public Object getFilesInfoByParam(
            @RequestJson(value = "fileflag", required = true) String fileflag,
            @RequestJson(value = "businesstype", required = false) String businesstype,
            @RequestJson(value = "businessfiletype", required = false) String businessfiletype
    ) {
        try {
            List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfoByParam(fileflag, businesstype, businessfiletype);
            List<Map<String, Object>> result = new ArrayList<>();
            for (FileInfoVO fileInfoVO : fileInfoVOS) {
                Map<String, Object> map = new HashMap<>();
                if (fileInfoVO.getUploadtime() != null) {
                    map.put("uploadtime", DataFormatUtil.getDateYMDHMS(fileInfoVO.getUploadtime()));
                } else {
                    map.put("uploadtime", "");
                }
                map.put("originalfilename", fileInfoVO.getOriginalfilename());
                map.put("filename", fileInfoVO.getFilename());
                map.put("filesize", fileInfoVO.getFilesize());
                map.put("fileextname", fileInfoVO.getFileextname());
                map.put("uploaduser", fileInfoVO.getUploaduser());
                map.put("businesstype", fileInfoVO.getBusinesstype());
                map.put("fileflag", fileInfoVO.getFileflag());
                map.put("businessfiletype", fileInfoVO.getBusinessfiletype());
                map.put("filepath", fileInfoVO.getFilepath());
                result.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Author: lip
     * @Date: 2018/11/10 16:15
     * @Description: 根据文件标识和文件业务类型，获取文件信息/base64图片数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param: fileflag         文件标识（对应业务表中附件字段所存的ID）（必传）
     * @Param: businesstype     文件业务相关类型（必传）
     * @Param: businessfiletype 业务类型下的文件类型（非必传）
     * @Return:
     */

    @RequestMapping(value = "getFilesInfoAndImgByParam", method = RequestMethod.POST)
    public Object getFilesInfoAndImgByParam(
            @RequestJson(value = "fileflag", required = true) String fileflag,
            @RequestJson(value = "businesstype", required = false) String businesstype,
            @RequestJson(value = "businessfiletype", required = false) String businessfiletype
    ) throws Exception {
        try {
            List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfoByParam(fileflag, businesstype, businessfiletype);
            List<Map<String, Object>> result = getUploadFileInfos(fileInfoVOS);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: lip
     * @Date: 2018/11/10 16:15
     * @Description: 根据文件标识和文件业务类型，获取文件信息/base64图片数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param: fileids 文件ID数组
     * @Return:
     */

    @RequestMapping(value = "getFilesByFileIds", method = RequestMethod.POST)
    public Object getFilesInfosByParams(
            @RequestJson(value = "fileids", required = true) List<String> fileids

    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("filepaths", fileids);
            List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesByParamMap(paramMap);
            List<Map<String, Object>> result = getUploadFileInfos(fileInfoVOS);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getUploadFileInfos(List<FileInfoVO> fileInfoVOS) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        for (FileInfoVO fileInfoVO : fileInfoVOS) {
            Map<String, Object> map = new HashMap<>();
            if (fileInfoVO.getUploadtime() != null) {
                map.put("uploadtime", DataFormatUtil.getDateYMDHMS(fileInfoVO.getUploadtime()));
            } else {
                map.put("uploadtime", "");
            }
            map.put("originalfilename", fileInfoVO.getOriginalfilename());
            map.put("filename", fileInfoVO.getFilename());
            map.put("filesize", fileInfoVO.getFilesize());
            map.put("fileextname", fileInfoVO.getFileextname());
            map.put("uploaduser", fileInfoVO.getUploaduser());
            map.put("businesstype", fileInfoVO.getBusinesstype());
            map.put("fileflag", fileInfoVO.getFileflag());
            map.put("businessfiletype", fileInfoVO.getBusinessfiletype());
            map.put("filepath", fileInfoVO.getFilepath());
            if (fileInfoVO.getBusinesstype() != null && fileInfoVO.getFileextname() != null && imgList.contains(fileInfoVO.getFileextname().toUpperCase())) {
                //1，初始化gridFSBucket
                gridFSBucket = initGridFSBucket(fileInfoVO.getBusinesstype().toString());
                GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileInfoVO.getFilepath())));
                GridFSFile gridFSFile = gridFSFindIterable.first();
                if (gridFSFile != null) {
                    GridFsResource gridFsResource = convertGridFSFile2Resource(gridFSFile);
                    String filename = gridFsResource.getFilename();
                    String ext = filename.substring(filename.lastIndexOf(".") + 1);
                    String base64 = AsposeUtil.parseFileToBase64(gridFsResource.getInputStream(), 5, ext);
                    map.put("imgsrc", "data:image/" + ext + ";base64," + base64);
                    map.put("base64", base64);
                }
            }
            result.add(map);
        }
        return result;
    }


    public Map<String, Object> getImgIdAndData(List<ObjectId> filePathList, String businessType) throws Exception {
        Map<String, Object> idAndData = new HashMap<>();
        //1，初始化gridFSBucket
        gridFSBucket = initGridFSBucket(businessType);
        GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.in("_id", filePathList));
        MongoCursor<GridFSFile> gridFSFileList = gridFSFindIterable.iterator();

        GridFSFile gridFSFile;
        while (gridFSFileList.hasNext()) {
            gridFSFile = gridFSFileList.next();
            GridFsResource gridFsResource = convertGridFSFile2Resource(gridFSFile);
            String filename = gridFsResource.getFilename();
            String ext = filename.substring(filename.lastIndexOf(".") + 1);
            String base64 = AsposeUtil.parseFileToBase64(gridFsResource.getInputStream(), 5, ext);
            idAndData.put(gridFSFile.getObjectId().toString(), "data:image/" + ext + ";base64," + base64);
        }
        return idAndData;
    }

    public Map<String, List<Map<String, Object>>> getFileIdAndData(List<String> fileIds, String businessType) throws Exception {
        Map<String, List<Map<String, Object>>> idAndData = new HashMap<>();
        List<Map<String, Object>> dataList;

        //1，初始化gridFSBucket
        gridFSBucket = initGridFSBucket(businessType);
        GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.in("metadata.fk_fileid", fileIds));
        MongoCursor<GridFSFile> gridFSFileList = gridFSFindIterable.iterator();
        GridFSFile gridFSFile;
        String fileId;
        String filePath;
        Document metadata;
        while (gridFSFileList.hasNext()) {
            gridFSFile = gridFSFileList.next();
            Map<String, Object> dataMap = new HashMap<>();
            GridFsResource gridFsResource = convertGridFSFile2Resource(gridFSFile);
            String filename = gridFsResource.getFilename();
            String ext = filename.substring(filename.lastIndexOf(".") + 1);
            filePath = gridFSFile.getObjectId().toString();
            metadata = gridFSFile.getMetadata();
            fileId = metadata.getString("fk_fileid");

            if (idAndData.containsKey(fileId)) {
                dataList = idAndData.get(fileId);
            } else {
                dataList = new ArrayList<>();
            }

            dataMap.put("filepath", filePath);
            dataMap.put("filename", filename);
            dataMap.put("businesstype", businessType);

            if (imgList.contains(ext.toUpperCase())) {
                String base64 = AsposeUtil.parseFileToBase64(gridFsResource.getInputStream(), 5, ext);
                dataMap.put("imgdata", "data:image/" + ext + ";base64," + base64);
            }
            dataList.add(dataMap);

            idAndData.put(fileId, dataList);
        }
        return idAndData;
    }


    @RequestMapping(value = "exportExcel", method = RequestMethod.GET)
    public void exportExcel(HttpServletResponse response,
                            @RequestJson(value = "sysmodel", required = false) String sysmodel

    ) throws Exception {
        OutputStream outputStream = null;
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", dataSource);

            String param = AuthUtil.paramDataFormat(paramMap);
            ResponseEntity<byte[]> entity = publicSystemMicroService.exportExcel(param);
            outputStream = response.getOutputStream();
            outputStream.write(entity.getBody());
            HttpHeaders httpHeaders = entity.getHeaders();
            response.setContentType(httpHeaders.getContentType().toString());
            response.setHeader("Content-Disposition", httpHeaders.getContentDisposition().toString());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }

        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/13 0013 上午 10:24
     * @Description: 下载服务器端视频到本地
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "downloadVideo", method = RequestMethod.POST)
    public boolean downloadVideo(@RequestJson(value = "httpurl") String httpurl, @RequestJson(value = "filename") String filename, HttpServletResponse response,
                                 HttpServletRequest request) {
        String rootpath = DataFormatUtil.parseProperties("rootpath");//获取配置的根目录
        byte[] bytes = file2byte(rootpath + httpurl + "\\" + filename);
        response.setContentType("application/octet-stream");
        response.setContentLength(bytes.length);

        try {
            response.getOutputStream().write(bytes);
            return true;
        } catch (IOException e) {
            System.out.println("IO异常----");
            return false;
        }
    }

    public byte[] file2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * @author: xsm
     * @date: 2020/2/14 0014 下午 20:18
     * @Description: 播放服务器端视频
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "playVideo", method = RequestMethod.POST)
    public boolean playVideo(@RequestJson(value = "httpurl") String httpurl, @RequestJson(value = "filename") String filename, HttpServletResponse response,
                             HttpServletRequest request) {
        String rootpath = DataFormatUtil.parseProperties("rootpath");//获取配置的根目录
        byte[] bytes = file2byte(rootpath + httpurl + "\\" + filename);
        response.setContentType("application/octet-stream");
        response.setContentLength(bytes.length);
        try {
            response.getOutputStream().write(bytes);
            return true;
        } catch (IOException e) {
            System.out.println("IO异常----");
            return false;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/9/29 0029 下午 3:15
     * @Description: 下载最新版本app
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, response]
     * @throws:
     */
    @RequestMapping(value = "downloadApp", method = RequestMethod.GET)
    public void downloadApp(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String businesstype = "14";
        String fileId = "";
        String collect = businessTypeMap.get(businesstype) + ".files";

        String appType = request.getParameter("apptype");
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtils.isBlank(appType)) {
            appType = "1";
        }
        paramMap.put("apptype", appType);
        MongoIterable<String> strings = mongoTemplate.getDb().listCollectionNames();
        for (String string : strings) {
            if (string.equals(collect)) {
                List<Map<String, Object>> lastAppVersionList = appVersionService.getLastAppVersionInfo(paramMap);
                if (lastAppVersionList.size() > 0) {
                    fileId = lastAppVersionList.get(0).get("file_id") == null ? "" : lastAppVersionList.get(0).get("file_id").toString();
                }
                downloadFile(fileId, businesstype, request, response);
                return;
            }
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/5/14 0014 下午 3:35
     * @Description: 通过文件标记，业务类型下的文件类型集合获取企业图片
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fileflag, imgtype]
     * @throws:
     */
    @RequestMapping(value = "getPollutionImgByParam", method = RequestMethod.POST)
    public Object getPollutionImgByParam(@RequestJson(value = "fileflag") String fileflag, @RequestJson(value = "imgtype") Object imgtype) throws Exception {
        String businesstype = "1";
        List<String> businessfiletypes = (List<String>) imgtype;
        List<Map<String, Object>> result = new ArrayList<>();

        for (String businessfiletype : businessfiletypes) {
            List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfoByParam(fileflag, businesstype, businessfiletype);
            result.addAll(getUploadFileInfos(fileInfoVOS));
        }
        return AuthUtil.parseJsonKeyToLower("success", result);
    }

    /**
     * @author: xsm
     * @date: 2021/8/25 0025 下午 2:45
     * @Description:富文本上传
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @return:
     */
    @RequestMapping(value = "/uploadRichTextFile", method = RequestMethod.POST)
    public Object uploadRichTextFile(@RequestParam("file") MultipartFile file, HttpSession session) throws Exception {
        //本地使用,上传位置 取配置地址
        String rootPath = DataFormatUtil.parseProperties("richtextpath");//富文本存储目录
        //文件的完整名称,如spring.jpeg
        String filename = file.getOriginalFilename();
        //文件名,如spring
        String name = filename.substring(0, filename.indexOf("."));
        //文件后缀,如.jpeg
        String suffix = filename.substring(filename.lastIndexOf("."));
        //加时间戳 解决重复问题
        filename = name + "_" + new Date().getTime() + suffix;
        //目标文件
        File descFile = new File(rootPath + File.separator + File.separator + filename);
        //int i = 1;
        String newFilename = filename;
        //若文件存在重命名
            /*while(descFile.exists()) {
                newFilename = name+"("+i+")"+suffix;
                String parentPath = descFile.getParent();
                descFile = new File(parentPath+File.separator+newFilename);
                i++;
            }*/
        //判断目标文件所在的目录是否存在
        if (!descFile.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            descFile.getParentFile().mkdirs();
        }
        try {
            //将内存中的数据写入磁盘
            file.transferTo(descFile);
        } catch (Exception e) {
            e.printStackTrace();

        }
        //完整的url
        String fileUrl = newFilename;
        return AuthUtil.parseJsonKeyToLower("success", fileUrl);
    }

}

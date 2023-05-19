package com.tjpu.sp.controller.environmentalprotection.tracesource;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceResultVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.environmentalprotection.tracesource.TraceSourceResultService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: lip
 * @date: 2021/10/11 0013 下午 3:58
 * @Description: 溯源结果处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("traceSourceResult")
public class TraceSourceResultController {
    @Autowired
    private TraceSourceResultService traceSourceResultService;

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * @Description: 获取溯源结果列表信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/10/11 9:26
     */
    @RequestMapping(value = "/getDataListByParam", method = RequestMethod.POST)
    public Object getDataListByParam(@RequestJson(value = "paramjson") Object paramjson) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            JSONObject jsonObject = JSONObject.fromObject(paramjson);
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()),
                        Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = traceSourceResultService.getDataListByParam(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo(datalist);
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
                    jsonObject.put("businesstype", "57");
                    List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfosByParam(jsonObject);
                    if (fileInfoVOS.size() > 0) {
                        String rootPath = DataFormatUtil.parseProperties("richtextpath");

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
                            fileMap.put("pkid", fileInfoVO.getPkFileid());
                            fileMap.put("filepath", fileInfoVO.getFilepath());
                            fileMap.put("originalfilename", fileInfoVO.getOriginalfilename());
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
            resultMap.put("data", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 添加或更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateDatass", method = RequestMethod.POST)
    public Object addOrUpdateDatass(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            TraceSourceResultVO traceSourceResultVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TraceSourceResultVO());
            traceSourceResultVO.setUpdatetime(new Date());
            traceSourceResultVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(traceSourceResultVO.getPkId())) {//更新操作
                traceSourceResultService.updateInfo(traceSourceResultVO);
            } else {//添加操作
                traceSourceResultVO.setPkId(UUID.randomUUID().toString());
                traceSourceResultService.insertInfo(traceSourceResultVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
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
            traceSourceResultService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 根据时间导出对应时间的TXT文件
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/5/11 9:23
     */
    @RequestMapping(value="exportTraceSourceResultTxtFile", method = RequestMethod.POST)
    public void exportTraceSourceResultTxtFile(@RequestJson(value = "monitortime", required = false) String monitortime, HttpServletRequest request,
                          HttpServletResponse response){
        //获取数据
        //判断是否有该时刻的数据
        //获取网格版本
        monitortime = "2022-04-21 07:30:00";
        Document one= traceSourceResultService.getOneSourceResultData(monitortime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long timeStemp = 0;
        Date date = null;
        try {
            date = simpleDateFormat.parse(monitortime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        timeStemp = date.getTime();
        if (one!=null&&one.get("GridVersion")!=null) {//为空 则证明无该时刻的网格结果数据
            Integer GridVersion = one.getInteger("GridVersion");
            //根据时间获取网格坐标数据
            List<Document> zb_result = traceSourceResultService.getGridCoordinateByGridVersion(GridVersion);
            //根据时间获取网格溯源结果
            List<Document> value_result = traceSourceResultService.getGridTraceSourceResultByTime(monitortime);
            if (zb_result!=null&&zb_result.size()>0&&value_result!=null&&value_result.size()>0&&zb_result.size()==value_result.size()){
                //拼接字符串
                StringBuffer text = new StringBuffer();
                text.append("regioncode=100000");
                text.append("\r\n");//换行字符
                //经度
                setTxtFileCoordinateData(text,zb_result,"longitude");
                text.append("\r\n");//换行字符
                //纬度
                setTxtFileCoordinateData(text,zb_result,"latitude");
                text.append("\r\n");//换行字符
                //值
                setTxtFileValueData(text,value_result);
                text.append("\r\n");//换行字符
                text.append("legend=0");
                text.append("\r\n");//换行字符
                text.append("transparent=1");
                text.append("\r\n");//换行字符
                text.append("innerboundary=0");
                text.append("\r\n");//换行字符
                text.append("title=");
                text.append("\r\n");//换行字符
                text.append("time=");
                text.append("\r\n");//换行字符
                text.append("filename=wg_"+timeStemp+".png");
                text.append("\r\n");//换行字符
                exportTxt(response,"wg_"+timeStemp, text.toString());
            }

        }
    }

    /**
     * 处理经纬度
     * */
    private void setTxtFileCoordinateData(StringBuffer text, List<Document> zb_result, String keystr) {
        if ("longitude".equals(keystr)){
            text.append("lons=");
        }else if("latitude".equals(keystr)){
            text.append("lats=");
        }
        //排序 按分组数排序
        //zb_result = zb_result.stream().sorted(Comparator.comparingDouble((Document m) -> Integer.parseInt(m.get("GridGroup").toString()))).collect(Collectors.toList());
        List<Document> groupdoc;
        int GridGroup = 0;
        int groupsize = 0;
        for (Document document:zb_result){
            GridGroup = document.getInteger("GridGroup");
            groupdoc = (List<Document>) document.get("DataList");
            groupsize = ((GridGroup-1) * groupdoc.size());
            for (int i = 1;i<=groupdoc.size();i++){
                for (Document doc :groupdoc){
                    if ((""+(i+groupsize)).equals(doc.getString("GridNumber"))){
                        text.append(doc.getString(keystr));//添加经纬度
                        if (i+groupsize<(document.getInteger("GridSize"))) {
                            text.append(",");//逗号隔开
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 处理值
     * */
    private void setTxtFileValueData(StringBuffer text, List<Document> value_result) {
        text.append("values=");
        //排序 按分组数排序
        //value_result = value_result.stream().sorted(Comparator.comparingDouble((Document m) -> Integer.parseInt(m.get("GridGroup").toString()))).collect(Collectors.toList());
        int i = 1;
        for (Document document:value_result){
            List<String> values = (List<String>) document.get("ValueList");
            for(String str:values) {
                text.append(str);//添加值
                if (i < (value_result.size() * values.size())){
                    text.append(",");//逗号隔开
                }
                i+=1;
            }
        }
    }

    /* 导出txt文件
     * @author
     * @param    response
     * @param    text 导出的字符串
     * @return
     */
    public void exportTxt(HttpServletResponse response,String filename,String text){
        response.setCharacterEncoding("utf-8");
        //设置响应的内容类型
        response.setContentType("text/plain");
        //设置文件的名称和格式
        response.addHeader("Content-Disposition","attachment;filename="
                //+ genAttachmentFileName( "网格", "JSON_FOR_UCC_")//设置名称格式，没有这个中文名称无法显示
                + filename+".txt");
        BufferedOutputStream buff = null;
        ServletOutputStream outStr = null;
        try {
            outStr = response.getOutputStream();
            buff = new BufferedOutputStream(outStr);
            buff.write(text.getBytes("UTF-8"));
            buff.flush();
            buff.close();
        } catch (Exception e) {
            //LOGGER.error("导出文件文件出错:{}",e);
        } finally {try {
            buff.close();
            outStr.close();
        } catch (Exception e) {
            //LOGGER.error("关闭流对象出错 e:{}",e);
        }
        }
    }

//防止中文文件名显示出错

    public  String genAttachmentFileName(String cnName, String defaultName) {
        try {
            cnName = new String(cnName.getBytes("gb2312"), "ISO8859-1");
        } catch (Exception e) {
            cnName = defaultName;
        }
        return cnName;
    }

}

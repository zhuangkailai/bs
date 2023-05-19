package com.tjpu.sp.controller.environmentalprotection.tracesourcesample;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.AlarmRemindUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.mongodb.RealTimeDataVO;
import com.tjpu.sp.model.environmentalprotection.tracesourcesample.TraceSourceSampleVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.tracesourcesample.TraceSourceSampleService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.FingerPrintDatabaseEnum;


/**
 * @author: chengzq
 * @date: 2020/10/21 0011 下午 1:58
 * @Description: 溯源样品控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("tracesourcesample")
public class TraceSourceSampleController {

    @Autowired
    private TraceSourceSampleService traceSourceSampleService;
    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private PubCodeService pubCodeService;

    /**
     * @author: chengzq
     * @date: 2020/10/21 0011 下午 2:58
     * @Description: 通过自定义参数获取溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceSampleByParamMap", method = RequestMethod.POST)
    public Object getTraceSourceSampleByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();

            List<Map<String,Object>> traceSourceSampleByParamMap = traceSourceSampleService.getTraceSourceSampleByParamMap(jsonObject);
            long total = traceSourceSampleByParamMap.size();

            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                traceSourceSampleByParamMap=traceSourceSampleByParamMap.stream().skip((Integer.valueOf(jsonObject.get("pagenum").toString())-1)*Integer.valueOf(jsonObject.get("pagesize").toString()))
                        .limit(Integer.valueOf(jsonObject.get("pagesize").toString())).collect(Collectors.toList());
            }


            //查询实时数据
            String  pkids = traceSourceSampleByParamMap.stream().filter(m -> m.get("pkid") != null).map(m -> m.get("pkid").toString()).collect(Collectors.joining(","));
            RealTimeDataVO realTimeDataVO=new RealTimeDataVO();
            realTimeDataVO.setDataGatherCode(pkids);
            List<RealTimeDataVO> realTimeData = mongoBaseService.getListByParam(realTimeDataVO, "RealTimeData", null);


            //将样品数据和实时数据组装起来
            for (Map<String, Object> stringObjectMap : traceSourceSampleByParamMap) {
                String pkid = stringObjectMap.get("pkid") == null ? "" : stringObjectMap.get("pkid").toString();
                RealTimeDataVO realTimeDataVO1 = realTimeData.stream().filter(m -> m.getDataGatherCode() != null && pkid.equals(m.getDataGatherCode())).findFirst().orElse(new RealTimeDataVO());
                List<Map<String, Object>> realDataList = realTimeDataVO1.getRealDataList()==null?new ArrayList<>():realTimeDataVO1.getRealDataList();
                for (Map<String, Object> map : realDataList) {
                    String PollutantCode = map.get("PollutantCode") == null ? "" : map.get("PollutantCode").toString();
                    String MonitorValue = map.get("MonitorValue") == null ? "" : map.get("MonitorValue").toString();
                    String OverMultiple = map.get("OverMultiple") == null ? "" : map.get("OverMultiple").toString();
                    stringObjectMap.put(PollutantCode+"_similarity",MonitorValue);
                    stringObjectMap.put(PollutantCode+"_proportionsimilarity",OverMultiple);
                }
                stringObjectMap.put("onlineid",realTimeDataVO1.getId());
            }

            resultMap.put("tablelist", traceSourceSampleByParamMap);
            resultMap.put("tabletitle", getTitle());
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/10/21 0011 下午 3:17
     * @Description: 新增溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addTraceSourceSample", method = RequestMethod.POST)
    public Object addTraceSourceSample(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            Date now = new Date();
            Object tracesourcesample = jsonObject.get("tracesourcesample");
            TraceSourceSampleVO entity = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(tracesourcesample), new TraceSourceSampleVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setpkid(UUID.randomUUID().toString());
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(now));
            entity.setupdateuser(username);

            //新增污染物数据到mongodb
            Object pollutants = jsonObject.get("pollutants");
            JSONArray jsonArray = JSONArray.fromObject(pollutants);

            RealTimeDataVO realTimeDataVO = new RealTimeDataVO();
            realTimeDataVO.setDataGatherCode(entity.getpkid());
            realTimeDataVO.setMonitorTime(now);
            realTimeDataVO.setDataType("Manual");
            List<Map<String,Object>> pollutantlist = new ArrayList<>();
            setData(realTimeDataVO,jsonArray,pollutantlist);

            if(StringUtils.isBlank(entity.getCharacterpollutants())){
                LinkedHashMap<String, Float> collect = pollutantlist.stream().filter(m -> m.get("PollutantCode") != null && m.get("MonitorValue") != null).collect(Collectors.toMap(m -> m.get("PollutantCode").toString()
                        , m -> Float.valueOf(m.get("MonitorValue").toString()), (a, b) -> a, LinkedHashMap::new));
                if(AlarmRemindUtil.findOutLiers(collect)!=null){
                    String collect1 = AlarmRemindUtil.findOutLiers(collect).keySet().stream().distinct().collect(Collectors.joining(","));
                    entity.setCharacterpollutants(collect1);
                }
            }

            traceSourceSampleService.insert(entity);
            mongoBaseService.save(realTimeDataVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/10/21 0011 下午 3:19
     * @Description: 通过id获取溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceSampleByID", method = RequestMethod.POST)
    public Object getTraceSourceSampleByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> result = traceSourceSampleService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/10/21 0011 下午 3:19
     * @Description: 修改溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateTraceSourceSample", method = RequestMethod.POST)
    public Object updateTraceSourceSample(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            Date now = new Date();
            Object tracesourcesample = jsonObject.get("tracesourcesample");
            TraceSourceSampleVO entity = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(tracesourcesample), new TraceSourceSampleVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(now));
            entity.setupdateuser(username);
            traceSourceSampleService.updateByPrimaryKey(entity);


            //新增/修改污染物数据到mongodb
            Object pollutants = jsonObject.get("pollutants");
            JSONArray jsonArray = JSONArray.fromObject(pollutants);
            RealTimeDataVO realTimeDataVO = new RealTimeDataVO();
            realTimeDataVO.setDataGatherCode(entity.getpkid());

            List<RealTimeDataVO> realTimeData = mongoBaseService.getListByParam(realTimeDataVO, "RealTimeData", null);
            realTimeDataVO.setDataGatherCode(entity.getpkid());
            realTimeDataVO.setMonitorTime(entity.getsampletime()==null?new Date():DataFormatUtil.getDateYMDHMS(JSONObjectUtil.getStartTime(entity.getsampletime())));
            List<Map<String,Object>> pollutantlist = new ArrayList<>();

            if(realTimeData.size()>0){
                RealTimeDataVO realTimeDatum = realTimeData.get(0);
                setData(realTimeDatum,jsonArray,pollutantlist);
                mongoBaseService.update(realTimeDatum);
            }else{
                realTimeDataVO.setId(null);
                setData(realTimeDataVO,jsonArray,pollutantlist);
                mongoBaseService.save(realTimeDataVO);
            }


            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    /**
     * @author: chengzq
     * @date: 2020/10/21 0011 下午 3:21
     * @Description: 通过id删除溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteTraceSourceSampleByID", method = RequestMethod.POST)
    public Object deleteTraceSourceSampleByID(@RequestJson(value = "id") String id,@RequestJson(value = "onlineid",required = false) String onlineid) throws Exception {
        try {
            traceSourceSampleService.deleteByPrimaryKey(id);
            if(StringUtils.isNotBlank(onlineid)){
                RealTimeDataVO realTimeDataVO = new RealTimeDataVO();
                realTimeDataVO.setId(onlineid);
                mongoBaseService.delete(realTimeDataVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/10/21 0011 下午 3:31
     * @Description: 通过id查询溯源样品信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceSampleDetailByID", method = RequestMethod.POST)
    public Object getTraceSourceSampleDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {

            Map<String,Object> resultMap=new HashMap<>();
            Map<String,Object> detailInfo = traceSourceSampleService.selectByPrimaryKey(id);

            //获取所有voc污染物
            List<Map<String, Object>> title = getTitle();


            //查询实时数据
            RealTimeDataVO realTimeDataVO=new RealTimeDataVO();

            realTimeDataVO.setDataGatherCode(detailInfo.get("pkid")==null?"":detailInfo.get("pkid").toString());
            List<RealTimeDataVO> realTimeData = mongoBaseService.getListByParam(realTimeDataVO, "RealTimeData", null);


            //将样品数据和实时数据组装起来
            String pkid = detailInfo.get("pkid")==null?"":detailInfo.get("pkid").toString();
            RealTimeDataVO realTimeDataVO1 = realTimeData.stream().filter(m -> m.getDataGatherCode() != null && pkid.equals(m.getDataGatherCode())).findFirst().orElse(new RealTimeDataVO());
            List<Map<String, Object>> realDataList = realTimeDataVO1.getRealDataList()==null?new ArrayList<>():realTimeDataVO1.getRealDataList();

            for (Map<String, Object> stringObjectMap : title) {
                String code = stringObjectMap.get("code")==null?"":stringObjectMap.get("code").toString();

                Object MonitorValue = realDataList.stream().filter(m -> m.get("PollutantCode") != null && code.equals(m.get("PollutantCode").toString())).findFirst().orElse(new HashMap<>()).get("MonitorValue");
                Object OverMultiple = realDataList.stream().filter(m -> m.get("PollutantCode") != null && code.equals(m.get("PollutantCode").toString())).findFirst().orElse(new HashMap<>()).get("OverMultiple");
                stringObjectMap.put("MonitorValue",MonitorValue);
                stringObjectMap.put("OverMultiple",OverMultiple);
            }


            resultMap.put("sampledata",detailInfo);
            resultMap.put("pollutants",title);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/10/23 0023 下午 4:40
     * @Description: 导出溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson, request, response]
     * @throws:
     */
    @RequestMapping(value = "/ExportTraceSourceSampleByParamMap", method = RequestMethod.POST)
    public void ExportTraceSourceSampleByParamMap(@RequestJson(value = "paramsjson") Object paramsJson, HttpServletRequest request, HttpServletResponse response)  {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;

            List<Map<String,Object>> traceSourceSampleByParamMap = traceSourceSampleService.getTraceSourceSampleByParamMap(jsonObject);


            //查询实时数据
            String  pkids = traceSourceSampleByParamMap.stream().filter(m -> m.get("pkid") != null).map(m -> m.get("pkid").toString()).collect(Collectors.joining(","));
            RealTimeDataVO realTimeDataVO=new RealTimeDataVO();
            realTimeDataVO.setDataGatherCode(pkids);
            List<RealTimeDataVO> realTimeData = mongoBaseService.getListByParam(realTimeDataVO, "RealTimeData", null);


            List<Map<String,Object>> headers = new ArrayList<>();
            List<Map<String,Object>> headersField = new ArrayList<>();

            Map<String,Object> samplehead=new HashMap<>();
            samplehead.put("headername","监测点名称");
            samplehead.put("headercode","samplename");
            samplehead.put("columnnum",1);
            samplehead.put("rownum",2);
            Map<String,Object> sampletimehead=new HashMap<>();
            sampletimehead.put("headername","采样时间");
            sampletimehead.put("headercode","sampletime");
            sampletimehead.put("columnnum",1);
            sampletimehead.put("rownum",2);
            headers.add(samplehead);
            headers.add(sampletimehead);


            for (Map<String, Object> stringObjectMap : getTitle()) {
                String Name = stringObjectMap.get("name")==null?"":stringObjectMap.get("name").toString();
                String Code = stringObjectMap.get("code")==null?"":stringObjectMap.get("code").toString();
                String pollutantunit = stringObjectMap.get("pollutantunit") == null ? "" : stringObjectMap.get("pollutantunit").toString();

                Map<String,Object> pollutantheaders=new HashMap<>();
                pollutantheaders.put("headername",Name);
                pollutantheaders.put("headercode",Code);
                pollutantheaders.put("columnnum",2);
                pollutantheaders.put("rownum",1);

                List<Map<String,Object>> childlist=new ArrayList<>();
                Map<String,Object> child1=new HashMap<>();
                child1.put("headername","浓度"+(StringUtils.isBlank(pollutantunit)?"":"("+pollutantunit+")"));
                child1.put("headercode",Code+"_similarity");
                child1.put("columnnum",1);
                child1.put("rownum",1);
                childlist.add(child1);

                Map<String,Object> child2=new HashMap<>();
                child2.put("headername","占比(%)");
                child2.put("headercode",Code+"_proportionsimilarity");
                child2.put("columnnum",1);
                child2.put("rownum",1);
                childlist.add(child2);



                pollutantheaders.put("chlidheader",childlist);

                headers.add(pollutantheaders);

            }

            //将样品数据和实时数据组装起来
            for (Map<String, Object> stringObjectMap : traceSourceSampleByParamMap) {
                String pkid = stringObjectMap.get("pkid") == null ? "" : stringObjectMap.get("pkid").toString();
                String samplename = stringObjectMap.get("samplename") == null ? "" : stringObjectMap.get("samplename").toString();
                String outputname = stringObjectMap.get("outputname") == null ? "" : stringObjectMap.get("outputname").toString();
                String sampletime = stringObjectMap.get("sampletime") == null ? "" : stringObjectMap.get("sampletime").toString();
                RealTimeDataVO realTimeDataVO1 = realTimeData.stream().filter(m -> m.getDataGatherCode() != null && pkid.equals(m.getDataGatherCode())).findFirst().orElse(new RealTimeDataVO());
                List<Map<String, Object>> realDataList = realTimeDataVO1.getRealDataList()==null?new ArrayList<>():realTimeDataVO1.getRealDataList();
                Map<String,Object> data=new HashMap<>();
                data.put("samplename",samplename);
                data.put("outputname",outputname);
                data.put("sampletime",sampletime);

                for (Map<String, Object> map : realDataList) {
                    String PollutantCode = map.get("PollutantCode") == null ? "" : map.get("PollutantCode").toString();
                    String MonitorValue = map.get("MonitorValue") == null ? "" : map.get("MonitorValue").toString();
                    String OverMultiple = map.get("OverMultiple") == null ? "" : map.get("OverMultiple").toString();
                    data.put(PollutantCode+"_similarity",MonitorValue);
                    data.put(PollutantCode+"_proportionsimilarity",OverMultiple);
                }
                headersField.add(data);
            }


            HSSFWorkbook sheet1 = ExcelUtil.exportManyHeaderExcel("sheet1", headers, headersField, "yyyy-MM-dd HH:mm");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("指纹溯源", response, request, bytesForWorkBook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * @author: chengzq
     * @date: 2020/10/23 0023 下午 5:17
     * @Description: 获取所有voc污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getVocPollutants", method = RequestMethod.POST)
    public Object getVocPollutants(){
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("tablename","PUB_CODE_PollutantFactor");
        paramMap.put("fields",Arrays.asList("concat('_',code) code","name","pollutantunit"));
        paramMap.put("wherestring","PollutantType="+EnvironmentalVocEnum.getCode()+" and isused=1");
        paramMap.put("orderfield","OrderIndex");
        return AuthUtil.parseJsonKeyToLower("success",pubCodeService.getPubCodesDataByParam(paramMap));
    }



    /**
     * @author: chengzq
     * @date: 2021/4/27 0027 下午 2:33
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [file]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceSamplePollutantData", method = RequestMethod.POST)
    public Object getTraceSourceSamplePollutantData(@RequestParam(value = "file") MultipartFile file) throws IOException {
        Map<String,Object> resultMap= new HashMap<>();
        String contentType = file.getContentType();//
        if( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)){
            String filename = file.getOriginalFilename().replaceAll("图谱分析-", "").replaceAll(".xlsx", "");
            InputStream inputStream = file.getInputStream();
            Workbook book = new XSSFWorkbook(inputStream);
            CellStyle cellStyle = book.createCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            List<Map<String,Object>> list= new ArrayList<>();
            Sheet sheetAt = book.getSheetAt(0);
            //获取最后一行，当数据没有问题时将该条数据删除并往上移动一行
            int lastRowNum = sheetAt.getLastRowNum();
            if(lastRowNum>1){
                Row row1 = sheetAt.getRow(0);
                String codekey = getRowStringValue(row1, 0);
                String valuekey = getRowStringValue(row1, 5);
                String propkey = getRowStringValue(row1, 6);
                if(!("code".equals(codekey) && "体积浓度均值ppb".equals(valuekey) && "体积浓度均值占比%".equals(propkey))){
                    return AuthUtil.parseJsonKeyToLower("success",resultMap);
                }
                for (int i = 1; i <= lastRowNum; i++) {
                    Map<String,Object> data=new HashMap<>();
                    //第i行
                    Row row = sheetAt.getRow(i);
                    String code = getRowStringValue(row, 0);
                    Double value = getRowDoubleValue(row, 5);
                    Double prop = getRowDoubleValue(row, 6);
                    data.put("code",code);
                    data.put("MonitorValue",value);
                    data.put("OverMultiple",prop);
                    list.add(data);
                }
                resultMap.put("filename",filename);
                resultMap.put("pollutants",list);
                return AuthUtil.parseJsonKeyToLower("success",resultMap);
            }
        }
        return AuthUtil.parseJsonKeyToLower("success",resultMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/10/22 0022 下午 4:19
     * @Description: 获取voc污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private List<Map<String, Object>> getTitle(){
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("tablename","PUB_CODE_PollutantFactor");
        paramMap.put("fields",Arrays.asList("code","name","pollutantunit"));
        paramMap.put("wherestring","PollutantType="+FingerPrintDatabaseEnum.getCode()+" and isused=1");
        paramMap.put("orderfield","OrderIndex");
        return pubCodeService.getPubCodesDataByParam(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/10/22 0022 下午 4:19
     * @Description: 设置mongodb污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [realTimeDataVO, jsonArray, pollutantlist]
     * @throws:
     */
    private void setData(RealTimeDataVO realTimeDataVO,JSONArray jsonArray,List<Map<String,Object>> pollutantlist){
        for (Object o : jsonArray) {
            Map<String,Object> data=(Map)o;
            data.put("Flag","n");
            data.put("IsOver",-1);
            data.put("IsException",-1);
            data.put("RepairTypeId",null);
            data.put("RepairVal",null);
            data.put("ConvertConcentration",null);
            data.put("IsOverStandard",false);
            data.put("IsSuddenChange",false);
            data.put("ChangeMultiple",0f);
            pollutantlist.add(data);
        }
        realTimeDataVO.setRealDataList(pollutantlist);
    }

    public static String getRowStringValue(Row row, Integer index){
        short lastCellNum = row.getLastCellNum();
        if(lastCellNum>=index && row.getCell(index)!=null){
            int cellTypeBlank = Cell.CELL_TYPE_BLANK;
            int cellType = row.getCell(index).getCellType();
            if(cellType!=cellTypeBlank){
                return row.getCell(index).getStringCellValue();
            }
        }
        return null;
    }

    public static Double getRowDoubleValue(Row row, Integer index){
        short lastCellNum = row.getLastCellNum();
        if(lastCellNum>=index && row.getCell(index)!=null){
            int cellTypeBlank = Cell.CELL_TYPE_BLANK;
            int cellTypeNumeric = Cell.CELL_TYPE_NUMERIC;
            int cellType = row.getCell(index).getCellType();
            if(cellType!=cellTypeBlank){//不是空类型
                if(cellType==cellTypeNumeric){
                    return row.getCell(index).getNumericCellValue();
                }
            }
        }
        return null;
    }
}

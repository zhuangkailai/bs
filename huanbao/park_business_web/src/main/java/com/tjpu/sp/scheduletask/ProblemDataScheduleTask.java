package com.tjpu.sp.scheduletask;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.FreeMarkerWordUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DeviceDevOpsInfoService;
import com.tjpu.sp.service.environmentalprotection.online.EffectiveTransmissionService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.stopproductioninfo.StopProductionInfoService;
import com.tjpu.sp.service.impl.environmentalprotection.online.OnlineServiceImpl;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import freemarker.template.TemplateException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.RainEnum;
import static com.tjpu.sp.config.fileconfig.BusinessTypeConfig.businessTypeMap;

/**
 * @author: chengzq
 * @date: 2020/2/21 0021 08:39
 * @Description: 问题数据定时任务
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
//@Configuration
//@EnableScheduling
//@RestController
//@RequestMapping("ProblemDataScheduleTask")
public class ProblemDataScheduleTask {

    // bucket
    private GridFSBucket gridFSBucket;
    // 使用的数据库
    private MongoDatabase useDatabase;
    @Autowired
    @Qualifier("secondMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private PubCodeService pubCodeService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    private EffectiveTransmissionService effectiveTransmissionService;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private StopProductionInfoService stopProductionInfoService;
    @Autowired
    private DeviceDevOpsInfoService deviceDevOpsInfoService;

    private List<Map<String,Object>> outPutInfosByParamMap;
    private List<Map<String,Object>> pollutants;

    private String reportingDate="";
    private final String id=UUID.randomUUID().toString();



    public void init() {
        Map<String, Object> paramMap = new HashMap<>();
        //获取企业和监测点信息
        this.outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
        paramMap.put("tablename", "PUB_CODE_PollutantFactor");
        paramMap.put("wherestring", "isused=1");
        this.pollutants = pubCodeService.getPubCodeDataByParam(paramMap);
    }

    @Scheduled(cron = "0 0 3 * * ?")
//    @Scheduled(cron = "10 * * * * ?")
//    @RequestMapping(value = "uploadFile",method = RequestMethod.POST)
    public Object uploadFile() throws Exception {


        List<Map<String, Object>> dataList = new ArrayList<>();
        //rediskey
        String problemDataRediskey = DataFormatUtil.parseProperties("problemDataRediskey");
        Long unixTimeInMillis = getUnixTimeInMillis();

        try {
            boolean isPut = RedisTemplateUtil.putCacheNXWithExpireAtTime(problemDataRediskey, id, unixTimeInMillis);

            if(!isPut){
                return null;
            }
            byte[] bytes = getProbleData();
            ByteInputStream byteInputStream = new ByteInputStream();
            byteInputStream.setBuf(bytes);

            Map<String, Object> fileParam = new HashMap<>();
            fileParam.put("business_type", 30);
            //1，初始化gridFSBucket
            gridFSBucket = initGridFSBucket("30");
            //2，初始化GridFSUploadOptions
            GridFSUploadOptions options = null;
            options = initGridFSUploadOptions(fileParam);
            ObjectId objectId = gridFSBucket.uploadFromStream(reportingDate+"监测设备问题报告.doc", byteInputStream, options);
            if (objectId != null) {//上传成功后，添加文件关联关系
                FileInfoVO fileInfoVO = new FileInfoVO();
                fileInfoVO.setPkFileid(UUID.randomUUID().toString());
                fileInfoVO.setFilepath(objectId.toString());
                fileInfoVO.setBusinessfiletype("30");
                fileInfoVO.setBusinesstype(Integer.parseInt("30"));
                fileInfoVO.setFileflag("");
                fileInfoVO.setFilesize(bytes.length);
                fileInfoVO.setBusinessfiletype("30");
                fileInfoVO.setFilename(reportingDate+"监测设备问题报告.doc");
                String originalFilename = reportingDate+"监测设备问题报告.doc";
                fileInfoVO.setOriginalfilename(originalFilename);
                String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                fileInfoVO.setFileextname(ext);
                Date nowDay = new Date();
                fileInfoVO.setUploadtime(nowDay);
                fileInfoVO.setUploaduser(null);
                fileInfoService.insert(fileInfoVO);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("originalFilename", reportingDate+"监测设备问题报告.doc");
            map.put("objectId", objectId.toString());
            dataList.add(map);


            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            //设置key有效期到凌晨12点
            RedisTemplateUtil.setExpireAtTime(problemDataRediskey,unixTimeInMillis);
        }


    }



//    @RequestMapping(value = "getProbleData", method = RequestMethod.POST)
    private byte[] getProbleData(/*HttpServletRequest request, HttpServletResponse response*/) throws IOException, TemplateException {
        init();
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        Calendar instance = Calendar.getInstance();
        instance.setTime(now);
        instance.add(Calendar.DAY_OF_MONTH, -1);
        Date time = instance.getTime();
        String starttime = DataFormatUtil.getDateYMD(time);
        String endtime = starttime;

        this.reportingDate=format.format(time);

        //一、	出现数据异常的设备集合
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();

        List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());


        //二、获取传输有效率未达标的点位数据
        List<Map<String, Object>> transmissionEffectiveRateList = getTransmissionEffectiveRateList(endtime, dgimns);


        List<String> collect3 = pollutants.stream().filter(m -> m.get("Code") != null).map(m -> m.get("Code").toString()).distinct().collect(Collectors.toList());
        List<String> ExcludeCode = Arrays.asList(new String[]{"b01", "b02", "b11"});
        collect3.removeAll(ExcludeCode);
        paramMap.put("mns", dgimns);
        paramMap.put("pollutantcodes", collect3);


        paramMap.put("datatypes", Arrays.asList(new String[]{"RealTimeData"}));
        paramMap.put("starttime", starttime + " 00:00:00");
        paramMap.put("endtime", endtime + " 23:59:59");
        paramMap.put("monitortimekey", "ExceptionTime");
        paramMap.put("collection", "ExceptionData");
        //查询异常数据
        List<Document> earlyOrOverOrExceptionDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
        Map<String, List<Document>> collect = earlyOrOverOrExceptionDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("PollutantCode") != null && m.get("ExceptionTime") != null)
                .collect(Collectors.groupingBy(m -> {
                            try {
                                return (m.get("DataGatherCode").toString() + "_" + m.get("PollutantCode").toString() + "_" + formatCSTString(m.get("ExceptionTime").toString(), "yyyy-MM-dd"));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return "";
                        }
                ));

        //组装数据
        for (String key : collect.keySet()) {
            String[] split = key.split("_");
            Map<String, Object> data = new HashMap<>();
            if (split.length > 2) {
                String dgimn = split[0];
                String pollutantcode = split[1];
                String exceptiontime = split[2];
                //排口下污染物同一天数据集合
                List<Document> documents = collect.get(key);
                //获取不同报警类型集合
                Map<String, List<Document>> collect2 = documents.stream().filter(m -> m.get("ExceptionType") != null && !m.get("ExceptionType").toString().equals("")).collect(Collectors.groupingBy(m -> m.get("ExceptionType").toString()));

                Optional<Map<String, Object>> outputinfo = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && dgimn.equals(m.get("DGIMN").toString())).findFirst();
                Optional<Map<String, Object>> pollutantinfo = pollutants.stream().filter(m -> m.get("Code") != null && pollutantcode.equals(m.get("Code").toString())).findFirst();

                if (outputinfo.isPresent() && pollutantinfo.isPresent()) {
                    data.put("outputname", outputinfo.get().get("OutputName"));
                    data.put("PollutionName", outputinfo.get().get("PollutionName"));
                    data.put("pollutantname", pollutantinfo.get().get("Name"));
                    for (String ExceptionType : collect2.keySet()) {
                        List<Document> documents1 = collect2.get(ExceptionType);
                        List<String> collect1 = documents1.stream().filter(m -> m.get("ExceptionTime") != null).map(m -> {
                            try {
                                String exceptionTime = formatCSTString(m.get("ExceptionTime").toString(), "yyyy-MM-dd HH:mm");
                                return exceptionTime;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return "";
                        }).filter(m -> !"".equals(m)).collect(Collectors.toList());
                        String line = DataFormatUtil.mergeContinueDate(collect1, 30, "yyyy-MM-dd HH:mm", "、", "HH:mm");

                        /*List<List<Integer>> lists = OnlineServiceImpl.groupIntegerList(collect1);
                        String line = OnlineServiceImpl.getLine(lists);
                        if (line.length() > 0) {
                            line = line.substring(0, line.length() - 1);
                        }*/

                        if (ZeroExceptionEnum.getCode().equals(ExceptionType)) {//零值异常
                            data.put("zeroexception", line);
                        } else if (ContinuousExceptionEnum.getCode().equals(ExceptionType)) {//连续值
                            data.put("continuousexception", line);
                        } else if (OverExceptionEnum.getCode().equals(ExceptionType)) {//超限
                            data.put("overexception", line);
                        } else if (NoFlowExceptionEnum.getCode().equals(ExceptionType)) {//无流量异常
                            data.put("noflow", line);
                        }
                        data.put("exceptiontime", exceptiontime);
                    }
                    resultList.add(data);
                }
            }
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        List<String> collects = pollutants.stream().filter(m -> m.get("Code") != null).map(m -> m.get("Code").toString()).distinct().collect(Collectors.toList());
        collect3.removeAll(ExcludeCode);
        paramMap.put("mns", dgimns);
        paramMap.put("pollutantcodes", collects);
        //计算时间差
        Map<String, List<Map>> lasttdata = onlineService.getLatestTwoRealTimeDataByParamMap(paramMap).stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        setTimeDifference(lasttdata, outPutInfosByParamMap, decimalFormat, now, dgimns);


        //组装时间差表格模板数据
        Map<Object, List<Map<String, Object>>> timedifferencinfo = outPutInfosByParamMap.stream().filter(m -> m.get("TimeDifference") != null).collect(Collectors.groupingBy(m -> m.get("PollutionName")));
        List<Map<String,Object>> timedifferenclist=new ArrayList<>();
        int index=1;
        for (Object pollutionname : timedifferencinfo.keySet()) {
            List<Map<String, Object>> list = timedifferencinfo.get(pollutionname);
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                map.put("index",index);
                if(i==0){
                    map.put("merge","<w:vmerge w:val=\"restart\"/>");
                    index++;//序号++
                }else{
                    map.put("merge","<w:vmerge/>");
                }
                timedifferenclist.add(map);
            }
        }

        //组装异常表格模板数据
        index=1;
        List<Map<String,Object>> exceptionlist=new ArrayList<>();
        Map<Object, List<Map<String, Object>>> collect1 = resultList.stream().filter(m -> m.get("PollutionName") != null).collect(Collectors.groupingBy(m -> m.get("PollutionName")));
        for (Object pollutionname : collect1.keySet()) {
            List<Map<String, Object>> list = collect1.get(pollutionname);
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                map.put("index",index);
                if(i==0){
                    map.put("merge","<w:vmerge w:val=\"restart\"/>");
                    index++;//序号++
                }else{
                    map.put("merge","<w:vmerge/>");
                }
                map.put("zeroexception",map.get("zeroexception")==null?"":map.get("zeroexception"));
                map.put("continuousexception",map.get("continuousexception")==null?"":map.get("continuousexception"));
                map.put("overexception",map.get("overexception")==null?"":map.get("overexception"));
                map.put("noflow",map.get("noflow")==null?"":map.get("noflow"));

                exceptionlist.add(map);
            }

        }


        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("timedifference",timedifferenclist);
        resultMap.put("exceptiondata",exceptionlist);
        resultMap.put("transmissionEffective",transmissionEffectiveRateList);
        resultMap.put("date",format.format(time));



        byte[] fileBytes = FreeMarkerWordUtil.createWord(resultMap, "templates/监测设备问题报告.ftl");
//        ExcelUtil.downLoadFile("echou", response, request, fileBytes);

        return fileBytes;
    }


    public List<Map<String,Object>> getTransmissionEffectiveRateList(String countdate,List<String> dgimns){
        Map<String,Object> paramMap = new HashMap<>();
        List<Map<String,Object>> resultList=new ArrayList<>();
        paramMap.put("countdate",countdate);
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        //传输有效率
        List<Map<String, Object>> effectiveTransmissionInfoByParamMap = effectiveTransmissionService.getEffectiveTransmissionInfoByParamMap(paramMap);

        //小时数据
        paramMap.put("starttime",countdate+" 00");
        paramMap.put("endtime",countdate+" 23");
        paramMap.put("dgimns",dgimns);
        paramMap.put("datetype","hour");
        List<Map> hourdata = onlineService.getWaterStationOnlineDataByParamMap(paramMap);

        //排除小型站，雨水站点
        List<String> monitorpointtypss=Arrays.asList(new String[]{FactoryBoundarySmallStationEnum.getCode()+"", RainEnum.getCode()+""});

        //停产数据（要排除的排口）
        List<Map<String, Object>> stopOutput = getExcludeOutput(countdate);

        /*//日数据
        paramMap.put("starttime",countdate);
        paramMap.put("endtime",countdate);
        paramMap.put("datetype","day");
        List<Map> daydata = onlineService.getWaterStationOnlineDataByParamMap(paramMap);*/

        Map<String, List<Map<String, Object>>> collect = effectiveTransmissionInfoByParamMap.stream().filter(m->m.get("EffectiveRate") != null && m.get("FK_MonitorPointID")!=null && m.get("FK_MonitorPointTypeCode")!=null &&
                //排除停产排口数据
                stopOutput.stream().filter(n->n.get("FK_Outputid")!=null && n.get("FK_MonitorPointType")!=null && m.get("FK_MonitorPointID").toString().equals(n.get("FK_Outputid").toString())
                && m.get("FK_MonitorPointTypeCode").toString().equals(n.get("FK_MonitorPointType").toString())).count()==0
                //排除小型站，雨水站点数据
                && !monitorpointtypss.contains(m.get("FK_MonitorPointTypeCode").toString()) &&
                m.get("TransmissionRate") != null && (Double.valueOf(m.get("EffectiveRate").toString())<1 || Double.valueOf(m.get("TransmissionRate").toString())<1))
                .filter(m -> m.get("CountDate") != null && m.get("outputname")!=null && m.get("outputname").toString().length()>0).collect(Collectors.groupingBy(m -> m.get("pollutionname") == null ? "null" : m.get("pollutionname").toString() +"_"+ m.get("CountDate").toString()));


        int index=0;//序号
        for (String pollutionnameAndTime : collect.keySet()) {
            Map<String, List<Map<String, Object>>> collect1 = collect.get(pollutionnameAndTime).stream().filter(m -> m.get("DGIMN") != null).collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
            int size=0;
            for (String dgimn : collect1.keySet()) {
                List<Map<String, Object>> list = collect1.get(dgimn);
                List<String> monitorTimes = hourdata.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null && dgimn.equals(m.get("DataGatherCode").toString())).map(m -> {
                    try {
                        return formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd HH");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return "";
                }).collect(Collectors.toList());

                //如果小于24条证明站点缺少数据
                if(list.size()>0 && monitorTimes.size()<24){
                    Map<String, Object> map = list.get(0);
                    //有效率 平均值
                    Double eff = list.stream().filter(m -> m.get("EffectiveRate") != null).map(m -> Double.valueOf(m.get("EffectiveRate").toString())).collect(Collectors.averagingDouble(m -> m));
                    //传输率 平均值
                    Double tran = list.stream().filter(m -> m.get("TransmissionRate") != null).map(m -> Double.valueOf(m.get("TransmissionRate").toString())).collect(Collectors.averagingDouble(m -> m));

                    List<Integer> noDataTimepoint = getNoDataTimepoint(monitorTimes);

                    List<List<Integer>> lists = OnlineServiceImpl.groupIntegerList(noDataTimepoint);
                    String line = OnlineServiceImpl.getLine(lists);
                    if (line.length() > 0) {
                        line = line.substring(0, line.length() - 1);
                        String describe="该站点缺少"+line+"的小时数据";
                        map.put("describe",describe);
                    }

                    map.put("EffectiveRate",decimalFormat.format(eff*100)+"%");
                    map.put("TransmissionRate",decimalFormat.format(tran*100)+"%");

                    String pollutionname = map.get("pollutionname") == null ? "" : map.get("pollutionname").toString();
                    map.put("pollutionname",pollutionname);
                    if(size==0){
                        map.put("merge","<w:vmerge w:val=\"restart\"/>");
                        index++;//序号++
                    }else{
                        map.put("merge","<w:vmerge/>");
                    }
                    map.put("index",index);
                    resultList.add(map);
                    size++;
                    continue;
                }
                List<Map<String,Object>> trasmdata=new ArrayList<>();
                List<Map<String,Object>> effecdata=new ArrayList<>();//有效率
                for (Map<String, Object> map : list) {
                    Map<String,Object> trasminfo=new HashMap<>();
                    Map<String,Object> effecinfo=new HashMap<>();
                    List<Integer> effechourpoint=new ArrayList<>();//传输率时间集合
                    List<Integer> transmhourpoint=new ArrayList<>();//有效时间集合
                    String pollutantcode = map.get("FK_PollutantCode") == null ? "" : map.get("FK_PollutantCode").toString();
                    Optional<Map<String, Object>> first = pollutants.stream().filter(n -> n.get("Code") != null && pollutantcode.equals(n.get("Code"))).findFirst();
                    //有效率
                    double EffectiveRate = map.get("EffectiveRate") == null ? 0d : Double.valueOf(map.get("EffectiveRate").toString());
                    //传输率
                    double TransmissionRate = map.get("TransmissionRate") == null ? 0d : Double.valueOf(map.get("TransmissionRate").toString());

                    //当传输率小于1说明  该点位此时间没有数据或者点位有数据污染物没数
                    if(TransmissionRate<1){
                        hourdata.stream().filter(m->m.get("DataGatherCode") !=null && dgimn.equals(m.get("DataGatherCode").toString())).peek(m->{
                            try {
                                List<Map<String, Object>> hourDataList = (List<Map<String, Object>>) m.get("HourDataList");
                                String monitortime = m.get("MonitorTime") == null ? "" : formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd HH");
                                long count = hourDataList.stream().filter(n -> n.get("PollutantCode") != null && pollutantcode.equals(n.get("PollutantCode").toString())).count();
                                List<Map<String, Object>> collect2 = hourDataList.stream().filter(n -> n.get("PollutantCode") != null && pollutantcode.equals(n.get("PollutantCode").toString())).collect(Collectors.toList());
                                //该时间点没有该污染物数据，证明该站点此时缺少数据，添加到传输率时间集合中
                                if(count==0){
                                    transmhourpoint.add(Integer.valueOf(monitortime.substring(11)));
                                    trasminfo.put("transmhourpoint",transmhourpoint);
                                    trasminfo.put("pollutantname",first.get().get("Name"));
                                    trasminfo.put("pollutantcode",pollutantcode);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }).collect(Collectors.toList());
                    }

                    //当有效率小于1说明  此点位此时间有报警数据
                    if(EffectiveRate<1 && TransmissionRate!=EffectiveRate){
                        hourdata.stream().filter(m->m.get("DataGatherCode") !=null && dgimn.equals(m.get("DataGatherCode").toString())).peek(m->{
                            try {
                                List<Map<String, Object>> hourDataList = (List<Map<String, Object>>) m.get("HourDataList");
                                String monitortime = m.get("MonitorTime") == null ? "" : formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd HH");
                                long count = hourDataList.stream().filter(n -> n.get("PollutantCode") != null && pollutantcode.equals(n.get("PollutantCode").toString())).count();
                                if(count>0){
                                    //有污染物 证明是污染物在该时间点有异常
                                    long IsException = hourDataList.stream().filter(n -> n.get("PollutantCode") != null && pollutantcode.equals(n.get("PollutantCode").toString()) && n.get("IsException") != null &&
                                            Integer.valueOf(n.get("IsException").toString()) > 0).count();
                                    if(IsException>0){
                                        if(first.isPresent()){
                                            effechourpoint.add(Integer.valueOf(monitortime.substring(11)));
                                            effecinfo.put("effechourpoint",effechourpoint);
                                            effecinfo.put("pollutantname",first.get().get("Name"));
                                            effecinfo.put("pollutantcode",pollutantcode);
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }).collect(Collectors.toList());
                    }
                    if(!effecinfo.isEmpty()){
                        effecdata.add(effecinfo);
                    }
                    if(!trasminfo.isEmpty()){
                        trasmdata.add(trasminfo);
                    }
                }

                if(effecdata.size()>0 || trasmdata.size()>0){
                    for (Map<String, Object> map : list) {
                        //有效率
                        String eff = map.get("EffectiveRate") == null ? "" : decimalFormat.format(Double.valueOf(map.get("EffectiveRate").toString()) * 100) + "%";
                        //传输率
                        String tran = map.get("TransmissionRate") == null ? "" : decimalFormat.format(Double.valueOf(map.get("TransmissionRate").toString()) * 100) + "%";
                        String describe="";
                        for (Map<String, Object> effecdatum : effecdata) {
                            String code = effecdatum.get("pollutantcode") == null ? "" : effecdatum.get("pollutantcode").toString();
                            if(code.equals(map.get("FK_PollutantCode").toString())){
                                List<Integer> effechourpoint = (List<Integer>) effecdatum.get("effechourpoint");
                                List<List<Integer>> lists = OnlineServiceImpl.groupIntegerList(effechourpoint);
                                String line = OnlineServiceImpl.getLine(lists);
                                if (line.length() > 0) {
                                    line = line.substring(0, line.length() - 1);
                                    describe+=effecdatum.get("pollutantname").toString()+"有效率为"+eff+","+line+"小时数据异常;";
                                }
                            }
                        }
                        for (Map<String, Object> effecdatum : trasmdata) {
                            String code = effecdatum.get("pollutantcode") == null ? "" : effecdatum.get("pollutantcode").toString();
                            if(code.equals(map.get("FK_PollutantCode").toString())){
                                List<Integer> transmhourpoint = (List<Integer>) effecdatum.get("transmhourpoint");
                                List<List<Integer>> lists = OnlineServiceImpl.groupIntegerList(transmhourpoint);
                                String line = OnlineServiceImpl.getLine(lists);
                                if (line.length() > 0) {
                                    line = line.substring(0, line.length() - 1);
                                    describe+=effecdatum.get("pollutantname").toString()+"传输率为"+tran+","+"缺少"+line+"小时数据;";
                                }
                            }
                        }
                        map.put("describe",describe);
                        map.put("EffectiveRate",eff);
                        map.put("TransmissionRate",tran);
                        String pollutionname = map.get("pollutionname") == null ? "" : map.get("pollutionname").toString();
                        map.put("pollutionname",pollutionname);
                        if(size==0){
                            map.put("merge","<w:vmerge w:val=\"restart\"/>");
                            index++;
                        }else{
                            map.put("merge","<w:vmerge/>");
                        }
                        map.put("index",index);
                        resultList.add(map);
                        size++;
                    }
                }
            }
        }


        return resultList;
    }



    /**
     * @author: chengzq
     * @date: 2020/2/20 0020 上午 11:30
     * @Description: 设置时间差
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private void setTimeDifference(Map<String, List<Map>> lasttdata, List<Map<String, Object>> outPutInfosByParamMap, DecimalFormat decimalFormat, Date now, List<String> collect) {
        for (String dgimn : lasttdata.keySet()) {
            outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && dgimn.equals(m.get("DGIMN").toString()) && collect.contains(dgimn)).forEach(m -> {
                List<Map> list = lasttdata.get(dgimn);
                if (list.size() > 1) {
                    Map first = list.get(0);
                    Date MonitorTime1 = first.get("MonitorTime") == null ? null : (Date) first.get("MonitorTime");
                    //设置数据包时间差 单位分钟
                    Float time = (Float.valueOf(now.getTime() - MonitorTime1.getTime())) / 1000 / 60;
                    //如果时间差大于1分钟
                    if (time >= 1) {
                        m.put("TimeDifference", "慢" + decimalFormat.format(time) + "分钟");
                        m.put("isshow", true);
                    } else if (time <= -1) {
                        m.put("TimeDifference", "快" + decimalFormat.format(Math.abs(time)) + "分钟");
                        m.put("isshow", true);
                    }
                }
            });
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/3/31 0031 下午 3:18
     * @Description: 排除的排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [time]
     * @throws:
     */
    private List<Map<String,Object>> getExcludeOutput(String time){
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("starttime",time+" 00:00:00");
        paramMap.put("endtime",time+" 23:59:59");
        //停产排口
        List<Map<String, Object>> lastStopProductionInfoByParamMap = stopProductionInfoService.getLastStopProductionInfoByParamMap(paramMap);
        //运维排口
        List<Map<String, Object>> lastDeviceDevOpsInfoByParamMap = deviceDevOpsInfoService.getLastDeviceDevOpsInfoByParamMap(paramMap);

        lastStopProductionInfoByParamMap.addAll(lastDeviceDevOpsInfoByParamMap);

        return lastStopProductionInfoByParamMap;
    }

    /**
     * @author: chengzq
     * @date: 2020/2/20 0020 上午 10:17
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [cst, pattern]
     * @throws:
     */
    public static String formatCSTString(String cst, String pattern) throws ParseException {
        //获取监测时间
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date d = sdf.parse(cst);
        String formatDate = new SimpleDateFormat(pattern).format(d);
        return formatDate;
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
//        MultipartFile file = (MultipartFile) fileParam.get("file");
        String originalFilename = "问题类型.xls";
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        if (ext != null) {//转换小写
            ext = ext.toLowerCase();
        }

        String fileType = getFileType(ext);

        //设置自定义数据文档
        Document document = new Document();
        document.append("content_type", "30");
        document.append("user_id", null);
        document.append("user_name", null);
        document.append("file_ext", ext);
        document.append("file_type", fileType);
        document.append("fk_fileid", null);
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
     * @author: chengzq
     * @date: 2020/3/5 0005 下午 4:54
     * @Description: 获取一天没有数据得时间点 时间格式yyyy-MM-dd HH
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [times]
     * @throws:
     */
    private List<Integer> getNoDataTimepoint(List<String> times){
        List<Integer> allpoints =new ArrayList<>();
        List<Integer> points =new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            allpoints.add(i);
        }

        for (String time : times) {
            Integer integer = Integer.valueOf(time.substring(11));
            points.add(integer);
        }

        allpoints.removeAll(points);
        return allpoints;
    }


    /**
     * @author: chengzq
     * @date: 2019/9/3 0003 下午 7:58
     * @Description: 获取当日23:59:59时间戳
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private Long getUnixTimeInMillis() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Calendar instance = Calendar.getInstance();
        String time = instance.get(Calendar.YEAR) + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + instance.get(Calendar.DATE) + " 23:59:59";
        long unixTimeInMillis = format.parse(time).getTime();
        return unixTimeInMillis;
    }


}

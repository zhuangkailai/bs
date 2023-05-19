package com.tjpu.sp.controller.common;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.FtpUtil;
import com.tjpu.sp.model.common.mongodb.LatestDataVO;
import com.tjpu.sp.model.common.mongodb.RealTimeDataVO;
import com.tjpu.sp.model.extand.MeteDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author: lip
 * @date: 2020/9/17 0017 上午 9:10
 * @Description: 气象任务程序
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("scheduleMeteTask")
public class ScheduleMeteTaskController {
    private static final String latestData = "LatestData";
    private static final String dataType = "MeteData";
    private static final String nullString = "NA";
    private static final int minute = 30;

    private static List<String> nullList = new ArrayList<>();


    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private MongoBaseService mongoBaseService;
    private Map<String, Date> mnAndTime = new HashMap<>();
    private static Map<String, Object> realDataMap = new LinkedHashMap<>();
    private static Map<String, Object> lastDataMap = new LinkedHashMap<>();


    static {
        nullList.add(nullString);
        nullList.add("999999");
        nullList.add("999998");
        nullList.add("999990");
        nullList.add("999017");
        nullList.add("999997");
        realDataMap.put("Flag", "n");
        realDataMap.put("IsOver", -1);
        realDataMap.put("IsException", -1);
        realDataMap.put("RepairTypeId", null);
        realDataMap.put("RepairVal", null);
        realDataMap.put("ConvertConcentration", 0);
        realDataMap.put("IsOverStandard", false);
        realDataMap.put("OverMultiple", 0.0);
        realDataMap.put("IsSuddenChange", false);
        realDataMap.put("ChangeMultiple", 0.0);

        lastDataMap.put("MinStrength", null);
        lastDataMap.put("MaxStrength", null);
        lastDataMap.put("CouStrength", null);
        lastDataMap.put("Flag", "n");
        lastDataMap.put("IsOver", -1);
        lastDataMap.put("IsException", -1);
        lastDataMap.put("IsOverStandard", false);
        lastDataMap.put("IsSuddenChange", false);
    }

    private Map<String, Date> getMnAndTime() {
        Query query = new Query();
        query.addCriteria(Criteria.where("DataType").is(dataType));
        List<Document> documents = mongoTemplate.find(query, Document.class, latestData);
        if (documents.size() > 0) {
            for (Document document : documents) {
                mnAndTime.put(document.getString("DataGatherCode"), document.getDate("MonitorTime"));
            }
        }
        return mnAndTime;
    }

    /**
     * @author: lip
     * @date: 2020/9/17 0017 上午 8:50
     * @Description: 定时解析气象数据入库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @GetMapping("/fixedTimeParseMeteData")
    public void fixedTimeParseMeteData() {
        try {
            String remoteDir = DataFormatUtil.parseProperties("ftp.remoteDir");
            if (StringUtils.isNotBlank(remoteDir)) {
                parseMeteData(remoteDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void initMnAndTime() {
        try {
            String remoteDir = DataFormatUtil.parseProperties("ftp.remoteDir");
            if (StringUtils.isNotBlank(remoteDir)) {
                //初始化Map
                mnAndTime = getMnAndTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: lip
     * @date: 2020/9/17 0017 下午 5:29
     * @Description: 循环检查状态表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void intervalTimeSetStatusData() {

        Map<String, Object> paramMap = new HashMap<>();
        Date nowDay = new Date();
        for (String mnIndex : mnAndTime.keySet()) {
            if (DataFormatUtil.isNeedUpdate(mnAndTime.get(mnIndex), nowDay, minute)) {
                paramMap.put("dgimn", mnIndex);
                paramMap.put("updateuser", "ftpuser");
                paramMap.put("updatetime", new Date());
                paramMap.put("status", 0);
                paramMap.put("fkMonitorpointtypecode", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                deviceStatusService.updateStatusByParam(paramMap);
            }
        }
    }

    private void parseMeteData(String remoteDir) {
        InputStream inputStream = null;
        FTPClient ftpClient = null;
        try {
            //1，获取ftp连接
            ftpClient = FtpUtil.connectFtpServer();
            if (ftpClient != null) {
                List<MeteDataVO> meteDataVOS;
                ftpClient.changeWorkingDirectory(remoteDir);
                ftpClient.enterLocalPassiveMode();
                FTPFile[] ftpFiles = ftpClient.listFiles();
                for (FTPFile ftpFile : ftpFiles) {
                    if (ftpFile.getName().endsWith(".csv")) {
                        //获取待读文件输入流
                        System.out.println(ftpFile.getName());
                        inputStream = ftpClient.retrieveFileStream(ftpFile.getName());
                        if (inputStream != null) {
                            //2，解析数据
                            meteDataVOS = parseFileToJson(inputStream);
                            //3，判断数据时间，更新sql状态表
                            updateAndInsertData(meteDataVOS);
                            //4，删除文件，更新文件名称

                            ftpClient.deleteFile(ftpFile.getName());
                            inputStream.close();
                        }
                        ftpClient.completePendingCommand(); // 每当读完一个文件时，要执行该语句
                    }
                }
                ftpClient.logout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateAndInsertData(List<MeteDataVO> meteDataVOS) {
        MeteDataVO meteDataVO;
        String timeString;
        Date timeData;
        Date nowDay = new Date();
        boolean isAdd;
        String mnCommon;
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 1; i < meteDataVOS.size(); i++) {
            meteDataVO = meteDataVOS.get(i);
            if (!nullString.equals(meteDataVO.getRain()) ||
                    !nullString.equals(meteDataVO.getTemperature()) ||
                    !nullString.equals(meteDataVO.getWindDirection()) ||
                    !nullString.equals(meteDataVO.getWindSpeed())) {
                timeString = meteDataVO.getTime();
                timeData = DataFormatUtil.getDateYMDHM(timeString);
                if (timeData != null) {
                    mnCommon = meteDataVO.getStaID();
                    //插入实时数据表
                    insertRealData(meteDataVO);
                    isAdd = false;
                    //插入最新数据表
                    if (mnAndTime.containsKey(mnCommon)) {
                        if (mnAndTime.get(mnCommon).getTime() < timeData.getTime()) {
                            isAdd = true;
                            mnAndTime.put(mnCommon, timeData);
                        }
                    } else {
                        isAdd = true;
                        mnAndTime.put(mnCommon, timeData);
                    }
                    if (isAdd) {
                        insertLastData(meteDataVO);
                    }
                    //更新状态表
                    if (DataFormatUtil.getDateYMD(nowDay).equals(DataFormatUtil.getDateYMD(timeData))) {//当天数据
                        paramMap.put("dgimn", meteDataVO.getStaID());
                        paramMap.put("updateuser", "ftpuser");
                        paramMap.put("updatetime", nowDay);
                        paramMap.put("status", 1);
                        paramMap.put("fkMonitorpointtypecode", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                        deviceStatusService.updateStatusByParam(paramMap);
                    }
                }
            }
        }
    }

    private void insertLastData(MeteDataVO meteDataVO) {
        LatestDataVO latestDataVO = new LatestDataVO();
        latestDataVO.setDataGatherCode(meteDataVO.getStaID());
        latestDataVO.setMonitorTime(DataFormatUtil.getDateYMDHM(meteDataVO.getTime()));
        latestDataVO.setDataType(dataType);
        latestDataVO.setType("RealTimeData");
        List<Map<String, Object>> lastDataList = new ArrayList<>();
        Map<String, Object> dataMap = new LinkedHashMap<>();
        //降雨量
        String MonitorValue = null;
        if (!nullList.contains(meteDataVO.getRain())) {
            MonitorValue = meteDataVO.getRain();
        }
        dataMap.put("PollutantCode", CommonTypeEnum.WeatherPollutionEnum.RainfallEnum.getCode());
        dataMap.put("AvgStrength", MonitorValue);
        dataMap.putAll(lastDataMap);
        lastDataList.add(dataMap);
        //温度
        dataMap.clear();
        if (!nullList.contains(meteDataVO.getTemperature())) {
            MonitorValue = meteDataVO.getTemperature();
        }
        dataMap.put("PollutantCode", CommonTypeEnum.WeatherPollutionEnum.TemperatureEnum.getCode());
        dataMap.put("AvgStrength", MonitorValue);
        dataMap.putAll(lastDataMap);
        lastDataList.add(dataMap);
        //风向
        dataMap.clear();
        if (!nullList.contains(meteDataVO.getWindDirection())) {
            MonitorValue = meteDataVO.getWindDirection();
        }
        dataMap.put("PollutantCode", CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode());
        dataMap.put("AvgStrength", MonitorValue);
        dataMap.putAll(lastDataMap);
        lastDataList.add(dataMap);
        //风速
        dataMap.clear();
        if (!nullList.contains(meteDataVO.getWindSpeed())) {
            if ( MonitorValue.startsWith("998")) {
                MonitorValue = MonitorValue.replaceFirst("998","");
            }
        }
        dataMap.put("PollutantCode", CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        dataMap.put("AvgStrength", MonitorValue);
        dataMap.putAll(lastDataMap);
        lastDataList.add(dataMap);
        latestDataVO.setDataList(lastDataList);
        mongoBaseService.deleteAndInsertByKey(latestDataVO, "DataGatherCode");
    }

    private void insertRealData(MeteDataVO meteDataVO) {
        RealTimeDataVO realTimeDataVO = new RealTimeDataVO();
        realTimeDataVO.setDataGatherCode(meteDataVO.getStaID());
        realTimeDataVO.setMonitorTime(DataFormatUtil.getDateYMDHM(meteDataVO.getTime()));
        realTimeDataVO.setDataType(dataType);
        List<Map<String, Object>> realDataList = new ArrayList<>();
        String MonitorValue = null;
        Map<String, Object> dataMap = new LinkedHashMap<>();
        //降雨量
        if (!nullList.contains(meteDataVO.getRain())) {
            MonitorValue = meteDataVO.getRain();
        }
        dataMap.put("PollutantCode", CommonTypeEnum.WeatherPollutionEnum.RainfallEnum.getCode());
        dataMap.put("MonitorValue", MonitorValue);
        dataMap.putAll(realDataMap);
        realDataList.add(dataMap);
        //温度
        dataMap.clear();
        if (!nullList.contains(meteDataVO.getTemperature())) {
            MonitorValue = meteDataVO.getTemperature();
        }
        dataMap.put("PollutantCode", CommonTypeEnum.WeatherPollutionEnum.TemperatureEnum.getCode());
        dataMap.put("MonitorValue", MonitorValue);
        dataMap.putAll(realDataMap);
        realDataList.add(dataMap);
        //风向
        dataMap.clear();
        if (!nullList.contains(meteDataVO.getWindDirection())) {
            MonitorValue = meteDataVO.getWindDirection();
        }
        dataMap.put("PollutantCode", CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode());
        dataMap.put("MonitorValue", MonitorValue);
        dataMap.putAll(realDataMap);
        realDataList.add(dataMap);
        //风速
        dataMap.clear();
        if (!nullList.contains(meteDataVO.getWindSpeed())) {
            if ( MonitorValue.startsWith("998")) {
                MonitorValue = MonitorValue.replaceFirst("998","");
            }
        }
        dataMap.put("PollutantCode", CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        dataMap.put("MonitorValue", MonitorValue);
        dataMap.putAll(realDataMap);
        realDataList.add(dataMap);
        realTimeDataVO.setRealDataList(realDataList);
        mongoBaseService.save(realTimeDataVO);
    }

    private List<MeteDataVO> parseFileToJson(InputStream inputStream) throws Exception {
        String charset = "GBK";
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
        // 设置解析策略，csv的头和POJO属性的名称对应，也可以使用@CsvBindByName注解来指定名称
        HeaderColumnNameMappingStrategy strategy = new HeaderColumnNameMappingStrategy();
        strategy.setType(MeteDataVO.class);
        CsvToBean csvToBean = new CsvToBeanBuilder(inputStreamReader).withMappingStrategy(strategy).build();
        List<MeteDataVO> meteDataVOS = csvToBean.parse();
        return meteDataVOS;
    }


    /**
     * @author: lip
     * @date: 2020/9/17 0017 上午 8:50
     * @Description: 间隔时间解析气象数据入库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @GetMapping("/intervalTimeParseMeteData")
    public void intervalTimeParseMeteData() {
        try {
            String remoteDir = DataFormatUtil.parseProperties("ftp.remoteDir");
            if (StringUtils.isNotBlank(remoteDir)) {
                parseMeteData(remoteDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

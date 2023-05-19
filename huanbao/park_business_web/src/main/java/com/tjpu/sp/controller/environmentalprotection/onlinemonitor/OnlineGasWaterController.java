package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


/**
 * @Description: 废水废气在线监测
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/6/27 9:44
 */
@RestController
@RequestMapping("onlineGasWater")
public class OnlineGasWaterController {

    private final PollutantService pollutantService;

    private final OnlineMonitorService onlineMonitorService;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    public OnlineGasWaterController(PollutantService pollutantService, OnlineMonitorService onlineMonitorService) {
        this.pollutantService = pollutantService;
        this.onlineMonitorService = onlineMonitorService;
    }


    /**
     * @Description: 废水、废气小时因子数据（废水：化学需氧量，氨氮，废气：烟尘，SO2，NOx）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/24 8:50
     */
    @RequestMapping(value = "getHourFixedDataListByParam", method = RequestMethod.POST)
    public Object getHourFixedDataListByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {
        try {

            Map<String, Object> resultMap = new HashMap<>();
            Map<String,Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String,Map<String,Object>> mnAndPointMap = new HashMap<>();
            paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
            List<Map<String,Object>> waterPointList = onlineMonitorService.getOnlineOutPutListByParamMap(paramMap);
            int onlineNumber = 0;
            int offlineNumber = 0;
            int alarmNumber = 0;
            int status;

            for (Map<String,Object> point:waterPointList){
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                Map<String,Object> pointMap = new HashMap<>();
                pointMap.put("psname",point.get("pollutionname"));
                pointMap.put("pscode",point.get("PollutionCode"));
                pointMap.put("Industryname",point.get("IndustryTypeName"));
                pointMap.put("outputname",point.get("outputname"));
                pointMap.put("outpoutcode",point.get("outputcode"));
                pointMap.put("lon",point.get("Longitude"));
                pointMap.put("lat",point.get("Latitude"));
                pointMap.put("outtype","废水");
                if (point.get("status")!=null){
                    status = Integer.parseInt(point.get("status").toString());
                    if (status ==1){
                        status = 0;//正常
                        onlineNumber++;
                    }else if (status ==0){
                        status = 1;//离线
                        offlineNumber++;
                    }else if (status==2){
                        status = 2;//超标
                        alarmNumber++;
                    }
                    pointMap.put("state",status);
                }else {
                    pointMap.put("state","");
                }


                mnAndPointMap.put(mnCommon,pointMap);
            }
            paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
            List<Map<String,Object>> gasPointList = onlineMonitorService.getOnlineOutPutListByParamMap(paramMap);
            for (Map<String,Object> point:gasPointList){
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                Map<String,Object> pointMap = new HashMap<>();
                pointMap.put("psname",point.get("pollutionname"));
                pointMap.put("pscode",point.get("PollutionCode"));
                pointMap.put("Industryname",point.get("IndustryTypeName"));
                pointMap.put("outputname",point.get("outputname"));
                pointMap.put("outpoutcode",point.get("outputcode"));
                pointMap.put("lon",point.get("Longitude"));
                pointMap.put("lat",point.get("Latitude"));
                pointMap.put("outtype","废气");
                if (point.get("status")!=null){
                    status = Integer.parseInt(point.get("status").toString());
                    if (status ==1){
                        status = 0;//正常
                        onlineNumber++;
                    }else if (status ==0){
                        status = 1;//离线
                        offlineNumber++;
                    }else if (status==2){
                        status = 2;//超标
                        alarmNumber++;
                    }
                    pointMap.put("state",status);
                }else {
                    pointMap.put("state","");
                }
                mnAndPointMap.put(mnCommon,pointMap);
            }
            resultMap.put("onlineNumber",onlineNumber);
            resultMap.put("offlineNumber",offlineNumber);
            resultMap.put("alarmNumber",alarmNumber);
            List<Map<String,Object>> outputs = new ArrayList<>();
            if (mns.size()>0){
                paramMap.clear();
                List<String> pollutantCodes = Arrays.asList(
                        CommonTypeEnum.GasWaterPollutionEnum.ADEnum.getCode(),
                        CommonTypeEnum.GasWaterPollutionEnum.CODEnum.getCode(),
                        CommonTypeEnum.GasWaterPollutionEnum.YCEnum.getCode(),
                        CommonTypeEnum.GasWaterPollutionEnum.SO2Enum.getCode(),
                        CommonTypeEnum.GasWaterPollutionEnum.NOXEnum.getCode()
                );
                paramMap.put("mns",mns);
                paramMap.put("pollutantcodes",pollutantCodes);
                paramMap.put("starttime",starttime+":00:00");
                paramMap.put("endtime",endtime+":59:59");
                paramMap.put("collection","HourData");
                paramMap.put("MonitorTime","asc");
                List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
                List<Document> pollutantList;
                String monitorTime;
                String pollutantCode;

                for(Document document:documents){
                    mnCommon = document.getString("DataGatherCode");

                    Map<String,Object> pointMap = mnAndPointMap.get(mnCommon);
                    if (pointMap!=null){
                        Map<String,Object> dataMap = new HashMap<>();
                        dataMap.putAll(pointMap);
                        monitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                        pollutantList = document.get("HourDataList",List.class);
                        dataMap.put("time",monitorTime);
                        List<Map<String,Object>> factors = new ArrayList<>();
                        for (Document pollutant:pollutantList){
                            pollutantCode = pollutant.getString("PollutantCode");
                            if (pollutantCodes.contains(pollutantCode)){
                                Map<String,Object> map = new HashMap<>();
                                map.put("label",CommonTypeEnum.GasWaterPollutionEnum.getNameByCode(pollutantCode));
                                map.put("code",pollutantCode);
                                map.put("value",pollutant.get("AvgStrength"));
                                factors.add(map);
                            }
                        }
                        dataMap.put("factors",factors);
                        outputs.add(dataMap);
                    }

                }
            }
            resultMap.put("outputs",outputs);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

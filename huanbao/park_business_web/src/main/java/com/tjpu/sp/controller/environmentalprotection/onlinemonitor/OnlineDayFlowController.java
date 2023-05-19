package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;


/**
 * @author: lip
 * @date: 2019/8/9 0009 上午 9:32
 * @Description: 在线监测日排放量处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineDayFlow")
public class OnlineDayFlowController {
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;

    @Autowired
    private OnlineService onlineService;

    private final static String collection = "DayFlowData";
    /**
     * @author: lip
     * @date: 2019/8/9 0009 上午 9:11
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: 根据污染物和监测类型统计企业日排放量
     * @return:
     */
    @RequestMapping(value = "countPollutionDayFlowDataByParams", method = RequestMethod.POST)
    public Object countPollutionDayFlowDataByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> outputData = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();

            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("flag", "water");
                    outputData = waterOutPutInfoService.getAllOutPutInfoByType(paramMap);
                    break;
                case WasteGasEnum:
                    outputData = gasOutPutInfoService.getAllOutPutInfo(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("flag", "rain");
                    outputData = waterOutPutInfoService.getAllOutPutInfoByType(paramMap);
                    break;
            }
            if (outputData.size() > 0) {
                List<String> mns = new ArrayList<>();
                Map<String,Set<String>> pollutionIdAndMN = new HashMap<>();
                Map<String,Object> pollutionIdAndName = new HashMap<>();
                Map<String,Object> pollutionIdAndLongitude = new HashMap<>();
                Map<String,Object> pollutionIdAndLatitude = new HashMap<>();
                String mn;
                String pollutionId;
                Set<String> mnSet;
                for (Map<String, Object> output : outputData) {
                    if (output.get("DGIMN")!=null&&!mns.contains(output.get("DGIMN"))){
                        mn = output.get("DGIMN").toString();
                        mns.add(mn);
                        pollutionId = output.get("PK_PollutionID").toString();
                        if (pollutionIdAndMN.containsKey(pollutionId)){
                            mnSet =  pollutionIdAndMN.get(pollutionId);
                        }else {
                            mnSet = new HashSet<>();
                        }
                        mnSet.add(mn);
                        pollutionIdAndMN.put(pollutionId,mnSet);
                        pollutionIdAndName.put(pollutionId,output.get("pollution"));
                        pollutionIdAndLongitude.put(pollutionId,output.get("Longitude"));
                        pollutionIdAndLatitude.put(pollutionId,output.get("Latitude"));
                    }
                }
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", mns);
                paramMap.put("collection", collection);
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                List<Document> documents = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
                if (documents.size()>0){
                    resultList = MongoDataUtils.sumPollutionDayFlowData(documents,collection,pollutantcode,pollutionIdAndMN);
                    for (Map<String,Object> map:resultList){
                        pollutionId = map.get("pollutionid").toString();
                        map.put("pollutionname",pollutionIdAndName.get(pollutionId));
                        map.put("longitude",pollutionIdAndLongitude.get(pollutionId));
                        map.put("latitude",pollutionIdAndLatitude.get(pollutionId));
                    }
                    //根据排放量排序

                    Collections.sort(resultList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            Double one = Double.parseDouble(o1.get("totalflow").toString());
                            Double other = Double.parseDouble(o2.get("totalflow").toString());
                            return other.compareTo(one);
                        }
                    });
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }
}

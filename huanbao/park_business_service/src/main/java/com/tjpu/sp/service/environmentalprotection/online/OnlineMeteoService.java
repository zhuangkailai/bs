package com.tjpu.sp.service.environmentalprotection.online;

import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OnlineMeteoService {

    Map<String,Object> getMeteoMonitorPointLastHourData(Set<String> mns);

    List<Map<String,Object>> getParkWindData(String pollutantDataKey, List<Document> documents);

    Map<String,Object> countWeatherAndMeteoMonitorPointDataByParamMap(List<String> mns, List<String> pollutantcodes, String starttime, String endtime);
}

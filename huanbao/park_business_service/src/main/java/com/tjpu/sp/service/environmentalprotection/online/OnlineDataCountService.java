package com.tjpu.sp.service.environmentalprotection.online;


import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface OnlineDataCountService {

    List<Document> getMonUnWindDataByParam(Map<String, Object> paramMap);

    List<Document> getMongodbDataByParam(Map<String, Object> paramMap);

    List<Document> getOverModelDataByParam(Map<String, Object> paramMap);

    List<Document> getWaterDetectDataByParam(Map<String, Object> paramMap);

    List<Document> getEntPollutantHourOnlineDataByParams(String starttime, String endtime, List<String> mns, String pollutantcode);

    List<Document> getWaterDetectOnePollutantDataByParam(Map<String, Object> parammap);

    Document getOneRiverSectionLastDataByParam(Map<String, Object> paramMap);

    List<Document> getPollutantFlowDataByParam(Map<String, Object> paramMap);

    List<Document> getMonUnWindOrAirDataByParam(Map<String, Object> paramMap);
}

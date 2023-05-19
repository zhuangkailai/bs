package com.tjpu.sp.service.environmentalprotection.tracesource;


import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceResultVO;
import net.sf.json.JSONObject;

import org.bson.Document;
import java.util.List;
import java.util.Map;

public interface TraceSourceResultService {


    List<Map<String,Object>> getDataListByParam(JSONObject jsonObject);

    void updateInfo(TraceSourceResultVO traceSourceResultVO);

    void insertInfo(TraceSourceResultVO traceSourceResultVO);

    void deleteInfoById(String id);

    List<Document> getGridTraceSourceResultByTime(String monitortime);

    Document getOneSourceResultData(String monitortime);

    List<Document> getGridCoordinateByGridVersion(Integer gridVersion);
}

package com.tjpu.sp.service.impl.environmentalprotection.tracesource;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TraceSourceResultMapper;
import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceResultVO;
import com.tjpu.sp.service.environmentalprotection.tracesource.TraceSourceResultService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.bson.Document;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TraceSourceResultServiceImpl implements TraceSourceResultService {
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    //网格坐标
    private final String wgzbCollection = "GridCoordinateData";
    //网格结果
    private final String wgjgCollection = "GridTraceSourceData";

    @Autowired
    private TraceSourceResultMapper traceSourceResultMapper;

    @Override
    public List<Map<String, Object>> getDataListByParam(JSONObject jsonObject) {
        return traceSourceResultMapper.getDataListByParam(jsonObject);
    }

    @Override
    public void updateInfo(TraceSourceResultVO traceSourceResultVO) {
        traceSourceResultMapper.updateByPrimaryKey(traceSourceResultVO);
    }

    @Override
    public void insertInfo(TraceSourceResultVO traceSourceResultVO) {
        traceSourceResultMapper.insert(traceSourceResultVO);
    }

    @Override
    public void deleteInfoById(String id) {
        traceSourceResultMapper.deleteByPrimaryKey(id);
    }


    @Override
    public Document getOneSourceResultData(String monitortime) {
        Query query = new Query();
        query.addCriteria(Criteria.where("MonitorTime").gte(DataFormatUtil.parseDate(monitortime)).lte(DataFormatUtil.parseDate(monitortime)));
        Document document = mongoTemplate.findOne(query, Document.class, wgjgCollection);
        return document;
    }

    /**
     * 获取网格坐标
     * */
    @Override
    public List<Document> getGridCoordinateByGridVersion(Integer gridVersion) {
        Query query = new Query();
        query.addCriteria(Criteria.where("GridVersion").is(gridVersion));
        query.with(new Sort(Sort.Direction.ASC, "GridGroup"));
        List<Document> documents = mongoTemplate.find(query, Document.class, wgzbCollection);
        return documents;
    }

    /**
     * 获取网格溯源结果
     * */
    @Override
    public List<Document> getGridTraceSourceResultByTime(String monitortime) {
        Query query = new Query();
        query.addCriteria(Criteria.where("MonitorTime").gte(DataFormatUtil.parseDate(monitortime)).lte(DataFormatUtil.parseDate(monitortime)));
        query.with(new Sort(Sort.Direction.ASC, "GridGroup"));
        List<Document> documents = mongoTemplate.find(query, Document.class, wgjgCollection);
        return documents;
    }




}

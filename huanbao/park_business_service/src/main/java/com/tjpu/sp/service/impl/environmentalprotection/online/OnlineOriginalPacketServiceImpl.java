package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.service.environmentalprotection.online.OnlineOriginalPacketService;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;

@Service
public class OnlineOriginalPacketServiceImpl implements OnlineOriginalPacketService {

    @Autowired
    @Qualifier("originalPacketMongoTemplate")
    private MongoTemplate mongoTemplate;
    private final String DB_OriginalData = "OriginalData";
    /**
     * @author: xsm
     * @date: 2020/1/16 0016 下午 3:33
     * @Description: 根据监测点类型获取原始数据包表头
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForOriginalPackageList(Integer pollutanttype) {
        List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(),
                WasteGasEnum.getCode(),
                SmokeEnum.getCode(),
                RainEnum.getCode(),
                unOrganizationWasteGasEnum.getCode(),
                FactoryBoundarySmallStationEnum.getCode(),
                FactoryBoundaryStinkEnum.getCode()
        );
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        if (monitortypes.contains(pollutanttype)) {
            String[] titlename = new String[]{"企业名称", "监测点名称", "接收时间", "原始数据包值"};
            String[] titlefiled = new String[]{"pollutionname", "monitorpointname", "packettime", "packet"};
            for (int i = 0; i < titlefiled.length; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("minwidth", "180px");
                map.put("headeralign", "center");
                map.put("fixed", "left");
                map.put("showhide", true);
                map.put("prop", titlefiled[i]);
                map.put("label", titlename[i]);
                map.put("align", "center");
                tableTitleData.add(map);
            }
        } else {
            String[] titlename = new String[]{"监测点名称", "接收时间", "原始数据包值"};
            String[] titlefiled = new String[]{"monitorpointname", "packettime", "packet"};
            for (int i = 0; i < titlefiled.length; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("minwidth", "180px");
                map.put("headeralign", "center");
                map.put("fixed", "left");
                map.put("showhide", true);
                map.put("prop", titlefiled[i]);
                map.put("label", titlename[i]);
                map.put("align", "center");
                tableTitleData.add(map);
            }
        }
        return tableTitleData;
    }

    /**
     * @author: xsm
     * @date: 2020/1/16 0016 下午 3:33
     * @Description: 根据自定义参数获取原始数据表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getOriginalDataPackageListDataByParam(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resultlist = new ArrayList<>();
        List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(),
                WasteGasEnum.getCode(),
                SmokeEnum.getCode(),
                RainEnum.getCode(),
                unOrganizationWasteGasEnum.getCode(),
                FactoryBoundarySmallStationEnum.getCode(),
                FactoryBoundaryStinkEnum.getCode()
        );
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        int monitorpointtype = Integer.parseInt(paramMap.get("monitorpointtype").toString());
        List<String> datetypes = paramMap.get("datetypes")==null?new ArrayList<>():(List<String>)paramMap.get("datetypes");
        Map<String, Object> mnAndPollution = (Map<String, Object>) paramMap.get("mnAndPollution");
        Map<String, Object> mnAndMonitorPoint = (Map<String, Object>) paramMap.get("mnAndMonitorPoint");
        List<String> mns = (List<String>) paramMap.get("mns");
        long totalCount = 0;
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        if (mns.size() > 0) {
            //构建Mongdb查询条件
            Query query = new Query();
            query.addCriteria(Criteria.where("MN").in(mns));
            query.addCriteria(Criteria.where("PacketTime").gte(startDate).lte(endDate));
            Criteria packet = new Criteria();
            List<Criteria> criteriaList=new ArrayList<>();
            for (String datetype : datetypes) {
                Pattern pat = Pattern.compile("^.*"+datetype+".*$", Pattern.CASE_INSENSITIVE);
                criteriaList.add(Criteria.where("Packet").regex(pat));
            }
            if(criteriaList.size()>0){
                query.addCriteria(packet.orOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
            }

            totalCount = mongoTemplate.count(query, paramMap.get("collection").toString());
            //总条数
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(Criteria.where("MN").in(mns)));
            operations.add(Aggregation.match(Criteria.where("PacketTime").gte(startDate).lte(endDate)));
            if(criteriaList.size()>0) {
                operations.add(Aggregation.match(packet));
            }
            //排序条件
            String orderBy = "PacketTime";
            Sort.Direction direction = Sort.Direction.DESC;
            operations.add(Aggregation.sort(direction, orderBy.split(",")));
            operations.add(Aggregation.project("MN", "PacketTime", "Packet"));
            //插入分页、排序条件
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations.add(Aggregation.limit(pageEntity.getPageSize()));
            }
            Aggregation aggregationquery = Aggregation.newAggregation(operations);
            AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, paramMap.get("collection").toString(), Document.class);
            List<Document> documents = resultdocument.getMappedResults();
            if (documents.size() > 0) {//判断查询数据是否为空
                for (Document document : documents) {
                    String mn = document.getString("MN");//MN号
                    Map<String, Object> map = new HashMap<>();
                    if (monitortypes.contains(monitorpointtype)) {
                        map.put("pollutionname", "");
                        map.put("monitorpointname", "");
                        if (mnAndPollution != null && mnAndPollution.size() > 0) {//企业简称
                            map.put("pollutionname", mnAndPollution.get(mn));
                        }
                        if (mnAndMonitorPoint != null && mnAndMonitorPoint.size() > 0) {//监测点名称
                            map.put("monitorpointname", mnAndMonitorPoint.get(mn));
                        }
                    } else {
                        map.put("monitorpointname", "");
                        if (mnAndMonitorPoint != null && mnAndMonitorPoint.size() > 0) {//监测点名称
                            map.put("monitorpointname", mnAndMonitorPoint.get(mn));
                        }
                    }
                    map.put("packettime", DataFormatUtil.getDateYMDHMS(document.getDate("PacketTime")));
                    map.put("packet", document.getString("Packet"));
                    resultlist.add(map);
                }
            }
        }
        result.put("total", totalCount);
        result.put("datalist", resultlist);
        return result;
    }

    @Override
    public PageEntity<Document> getOriginalDataPackageDataByParam(Map<String, Object> paramMap) {


        boolean isPage = true;
        int pageNum = 1;
        int pageSize = 100000;
        if (paramMap.get("pagenum") != null) {
            pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
        } else {
            isPage = false;
        }
        if (paramMap.get("pagesize") != null) {
            pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
        } else {
            isPage = false;
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        pageEntity.setPageNum(pageNum);
        pageEntity.setPageSize(pageSize);
        List<AggregationOperation> operations = new ArrayList<>();
        //查询条件
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
            operations.add(Aggregation.match(Criteria.where("PacketTime").gte(startDate).lte(endDate)));
        } else {
            if (paramMap.get("starttime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                operations.add(Aggregation.match(Criteria.where("PacketTime").gte(startDate)));
            }
            if (paramMap.get("endtime") != null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                operations.add(Aggregation.match(Criteria.where("PacketTime").lte(endDate)));
            }
        }
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            operations.add(Aggregation.match(Criteria.where("MN").in(mns)));
        }
        if (paramMap.get("mnlike") != null) {
            String likeName = paramMap.get("mnlike").toString();
            Pattern pattern = Pattern.compile("^.*"+likeName+".*$", Pattern.CASE_INSENSITIVE);
            operations.add(Aggregation.match(Criteria.where("MN").regex(pattern)));
        }
        //排序条件
        String orderBy = "PacketTime,MN";
        if (paramMap.get("orderBy") != null) {
            orderBy = paramMap.get("orderBy").toString();
        }
        Sort.Direction direction = Sort.Direction.DESC;
        if (paramMap.get("direction") != null && "asc".equals(paramMap.get("direction"))) {
            direction = Sort.Direction.ASC;
        }
        if (isPage) {
            long totalCount = 0;
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, DB_OriginalData, Document.class);

            totalCount = resultsCount.getMappedResults().size();
            pageEntity.setTotalCount(totalCount);
            int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
            pageEntity.setPageCount(pageCount);
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        String project = "_id,MN,PacketTime,Packet";
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project(project.split(",")));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, DB_OriginalData, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        pageEntity.setListItems(listItems);
        return pageEntity;


    }

}

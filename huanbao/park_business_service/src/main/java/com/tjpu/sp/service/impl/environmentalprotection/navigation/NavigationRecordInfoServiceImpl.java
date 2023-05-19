package com.tjpu.sp.service.impl.environmentalprotection.navigation;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.navigation.NavigationRecordInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.navigation.NavigationStandardMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.model.environmentalprotection.navigation.NavigationRecordInfoVO;
import com.tjpu.sp.service.environmentalprotection.navigation.NavigationRecordInfoService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.NavigationEnum;


@Service
@Transactional
public class NavigationRecordInfoServiceImpl implements NavigationRecordInfoService {
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private NavigationRecordInfoMapper navigationRecordInfoMapper;
    @Autowired
    private NavigationStandardMapper navigationStandadMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;


    @Override
    public List<Map<String, Object>> getNavigationRecordInfosByParamMap(Map<String, Object> paramMap) {
        return navigationRecordInfoMapper.getNavigationRecordInfosByParamMap(paramMap);
    }

    @Override
    public void insert(NavigationRecordInfoVO navigationRecordInfoVO) {
        navigationRecordInfoMapper.insert(navigationRecordInfoVO);
    }

    @Override
    public NavigationRecordInfoVO selectByPrimaryKey(String id) {
        return navigationRecordInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateByPrimaryKey(NavigationRecordInfoVO navigationRecordInfoVO) {
        navigationRecordInfoMapper.updateByPrimaryKey(navigationRecordInfoVO);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        navigationRecordInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getNavigationRecordInfoDetailByID(String id) {
        return navigationRecordInfoMapper.getNavigationRecordInfoDetailByID(id);
    }

    @Override
    public List<Map<String, Object>> getNavigationDataGroupByNavigationDateByMonth(Map<String, Object> paramMap) {
        return navigationRecordInfoMapper.getNavigationDataGroupByNavigationDateByMonth(paramMap);
    }

    @Override
    public List<Map<String, Object>> getNavigationDataByNavigationDate(Map<String, Object> paramMap) {
        return navigationRecordInfoMapper.getNavigationDataByNavigationDate(paramMap);
    }

    @Override
    public List<Map<String,Object>> countNavigationPollutantDataByMonitorTimes(String dgimn, Date startdate, Date enddate) {
        List<Map<String,Object>> result = new ArrayList<>();
        //查询污染物平均浓度值
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("MonitorValue", "$value");
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startdate).lte(enddate);
        operations.add(Aggregation.unwind("NavigationDataList"));
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode")
                .and("NavigationDataList.PollutantCode").as("PollutantCode")
                .and("NavigationDataList.MonitorValue").as("value"));
        operations.add(Aggregation.group("DataGatherCode","PollutantCode")
                .push(pollutantList).as("avglist")

        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Map> pageResults = mongoTemplate.aggregate(aggregationList, "NavigationData", Map.class);
        List<Map> listItems = pageResults.getMappedResults();
        if (listItems.size()>0){
            Map<String, Object> param = new HashMap<>();
            param.put("pollutanttype", NavigationEnum.getCode());
            List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(param);
            param.clear();
            for (Map<String, Object> map:pollutants){
                param.put(map.get("code").toString(),map.get("name"));
            }
            for (Map obj:listItems){
                Map<String, Object> onemap = new HashMap<>();
                List<Map<String,Object>> onelist = (List<Map<String, Object>>) obj.get("avglist");
                Object codevalue = countPollutantAvgValue(onelist);
                onemap.put("pollutantcode",obj.get("PollutantCode"));
                onemap.put("pollutantname",param.get(obj.get("PollutantCode").toString()));
                onemap.put("value",codevalue);
                result.add(onemap);
            }
        }

        return result;
    }


    private Object countPollutantAvgValue(List<Map<String, Object>> onelist) {
        int num = 0;
        double totalnum = 0d;
        Object value = null;
        for (Map<String, Object> map:onelist){
            if (map.get("MonitorValue")!=null&&!"".equals(map.get("MonitorValue").toString())&&!"0".equals(map.get("MonitorValue").toString())){
                num+=1;
                totalnum+= Double.valueOf(map.get("MonitorValue").toString());
            }
        }
        if (num>0){
            value = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(totalnum/num));
        }
        return value;
    }

    /**
     * @author: xsm
     * @date: 2020/09/04 0004 上午 9:03
     * @Description: 根据MN号和时间范围查询出所有的走航实时数据(每条实时数据 所有污染物总和)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getNavigationRealTimeSumDataByParam(String dgimn, Date startdate, Date enddate, Integer pagenum) {
        Map<String, Object> result = new HashMap<>();
        int pagesize = 4;//查询条数
        PageEntity<Document> pageEntity = new PageEntity<>();
        pageEntity.setPageNum(pagenum);
        pageEntity.setPageSize(pagesize);
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.addCriteria(Criteria.where("MonitorTime").gte(startdate).lte(enddate));
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").is(dgimn)));
        operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startdate).lte(enddate)));
        //总条数
        long totalCount = mongoTemplate.count(query, "NavigationData");
        pageEntity.setTotalCount(totalCount);
        String orderBy = "DataGatherCode,MonitorTime";
        Sort.Direction direction = Sort.Direction.ASC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime", "NavigationDataList","Longitude","Latitude"));
        //插入分页、排序条件
        if (pagenum>1){
            operations.add(Aggregation.skip((long) ((pageEntity.getPageNum() - 1) * pageEntity.getPageSize())-1));
            operations.add(Aggregation.limit(pageEntity.getPageSize()+1));
        }else {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, "NavigationData", Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        //获取走航标准数据
        List<Map<String,Object>> standards =  navigationStandadMapper.getAllLevelNavigationStandardData();
        List<Map<String,Object>> datalist = new ArrayList<>();
        Map<String,Object> lastdata = new HashMap<>();
        if (documents.size()>0){
             countPollutantConcentrationSum(pagenum,documents,standards,datalist,lastdata);
        }
        result.put("total", totalCount);
        result.put("pagesize", pagesize);
        result.put("datalist", datalist);
        result.put("lastdata", lastdata);
        return result;
    }


    private void countPollutantConcentrationSum(Integer pagenum, List<Document> documents, List<Map<String, Object>> standards, List<Map<String, Object>> datalist, Map<String, Object> lastdata) {
        if (pagenum>1){//当获取其它页数据  取上一页最后一条数据
            Document document = documents.get(0);
            List<Map<String,Object>> pollutants = (List<Map<String, Object>>) document.get("NavigationDataList");
            double onesum = 0d;
            for (Map<String,Object> map:pollutants){
                if (map.get("MonitorValue")!=null&&!"".equals(map.get("MonitorValue").toString())){
                    onesum += Double.valueOf(map.get("MonitorValue").toString());
                }
            }
            lastdata.put("monitortime",DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            lastdata.put("sumvalue",DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(onesum)));
            lastdata.put("Longitude",document.getString("Longitude"));
            lastdata.put("Latitude",document.getString("Latitude"));
            lastdata.put("colorvalue",getColorValueByStandard(standards,onesum));

            Document documenttwo ;
            List<Map<String,Object>> pollutantstwo;
            for (int i = 1;i<documents.size();i++){
                documenttwo = documents.get(i);
                pollutantstwo = (List<Map<String, Object>>) documenttwo.get("NavigationDataList");
                double twosum = 0d;
                for (Map<String,Object> map:pollutantstwo){
                    if (map.get("MonitorValue")!=null&&!"".equals(map.get("MonitorValue").toString())){
                        twosum += Double.valueOf(map.get("MonitorValue").toString());
                    }
                }
                Map<String,Object> twomap = new HashMap<>();
                twomap.put("monitortime",DataFormatUtil.getDateYMDHMS(documenttwo.getDate("MonitorTime")));
                twomap.put("sumvalue",DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(twosum)));
                twomap.put("Longitude",documenttwo.getString("Longitude"));
                twomap.put("Latitude",documenttwo.getString("Latitude"));
                twomap.put("colorvalue",getColorValueByStandard(standards,twosum));
                datalist.add(twomap);
            }
        }else{
            Document documenttwo = null;
            List<Map<String,Object>> pollutantstwo = null;
            for (int i = 0;i<documents.size();i++){
                documenttwo = documents.get(i);
                pollutantstwo = (List<Map<String, Object>>) documenttwo.get("NavigationDataList");
                double twosum = 0d;
                for (Map<String,Object> map:pollutantstwo){
                    if (map.get("MonitorValue")!=null&&!"".equals(map.get("MonitorValue").toString())){
                        twosum += Double.valueOf(map.get("MonitorValue").toString());
                    }
                }
                Map<String,Object> twomap = new HashMap<>();
                twomap.put("monitortime",DataFormatUtil.getDateYMDHMS(documenttwo.getDate("MonitorTime")));
                twomap.put("sumvalue",DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(twosum)));
                twomap.put("colorvalue",getColorValueByStandard(standards,twosum));
                twomap.put("Longitude",documenttwo.getString("Longitude"));
                twomap.put("Latitude",documenttwo.getString("Latitude"));
                datalist.add(twomap);
            }
        }
    }


    @Override
    public Map<String, Object> getNavigationRealTimeDataByParam(String dgimn, Date startdate, Date enddate, Integer pagenum,List<String> pollutantcodes) {
        Map<String, Object> result = new HashMap<>();
        int pagesize = 4;//查询条数
        PageEntity<Document> pageEntity = new PageEntity<>();
        pageEntity.setPageNum(pagenum);
        pageEntity.setPageSize(pagesize);
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.addCriteria(Criteria.where("MonitorTime").gte(startdate).lte(enddate));
        query.addCriteria(Criteria.where("NavigationDataList.PollutantCode").in(pollutantcodes));
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").is(dgimn)));
        operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startdate).lte(enddate)));
        operations.add(Aggregation.match(Criteria.where("NavigationDataList.PollutantCode").in(pollutantcodes)));
        //总条数
        long totalCount = mongoTemplate.count(query, "NavigationData");
        pageEntity.setTotalCount(totalCount);
        String orderBy = "DataGatherCode,MonitorTime";
        Sort.Direction direction = Sort.Direction.ASC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime", "NavigationDataList","Longitude","Latitude"));
        //插入分页、排序条件
        if (pagenum>1){
            operations.add(Aggregation.skip((long) ((pageEntity.getPageNum() - 1) * pageEntity.getPageSize())-1));
            operations.add(Aggregation.limit(pageEntity.getPageSize()+1));
        }else {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, "NavigationData", Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        //获取走航标准数据
        List<Map<String,Object>> standards =  navigationStandadMapper.getAllLevelNavigationStandardData();
        List<Map<String,Object>> datalist = new ArrayList<>();
        Map<String,Object> lastdata = new HashMap<>();
        if (documents.size()>0){
            countPollutantConcentrationData(pagenum,documents,pollutantcodes,standards,datalist,lastdata);
        }
        result.put("total", totalCount);
        result.put("pagesize", pagesize);
        result.put("datalist", datalist);
        result.put("lastdata", lastdata);
        return result;
    }

    private void countPollutantConcentrationData(Integer pagenum, List<Document> documents, List<String> pollutantcodes, List<Map<String, Object>> standards, List<Map<String, Object>> datalist, Map<String, Object> lastdata) {
        //根据污染物codes获取相关污染物信息
        Map<String,Object> param = new HashMap<>();
        param.put("monitorpointtype",NavigationEnum.getCode());
        param.put("codes",pollutantcodes);
        List<Map<String, Object>> pollutantlist = pollutantFactorMapper.getPollutantsByPollutantType(param);
        Map<String,Object> codename = new HashMap<>();
        for (Map<String, Object> map:pollutantlist){
            codename.put(map.get("code").toString(),map.get("name"));
        }
        if (pagenum>1){//当获取其它页数据  取上一页最后一条数据
            Document document = documents.get(0);
            List<Map<String,Object>> pollutants = (List<Map<String, Object>>) document.get("NavigationDataList");
            List<Map<String,Object>> ptwo = new ArrayList<>();
            for (Map<String,Object> map:pollutants){
                if (codename.get(map.get("PollutantCode").toString())!=null) {
                    if (map.get("MonitorValue")!=null&&!"".equals(map.get("MonitorValue").toString())){
                        map.put("colorvalue",getColorValueByStandard(standards,Double.valueOf(map.get("MonitorValue").toString())));
                    }else{
                        map.put("colorvalue",null);
                    }
                    ptwo.add(map);
                    }
                }
            lastdata.put("monitortime",DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
            lastdata.put("pollutantdata",ptwo);
            lastdata.put("Longitude",document.getString("Longitude"));
            lastdata.put("Latitude",document.getString("Latitude"));

            Document documenttwo ;
            List<Map<String,Object>> pollutantstwo;
            for (int i = 1;i<documents.size();i++){
                documenttwo = documents.get(i);
                pollutantstwo = (List<Map<String, Object>>) documenttwo.get("NavigationDataList");
                List<Map<String,Object>> pollutantarry = new ArrayList<>();
                for (Map<String,Object> maptwo:pollutantstwo){
                    if (codename.get(maptwo.get("PollutantCode").toString())!=null) {
                        if (maptwo.get("MonitorValue")!=null&&!"".equals(maptwo.get("MonitorValue").toString())){
                            maptwo.put("colorvalue",getColorValueByStandard(standards,Double.valueOf(maptwo.get("MonitorValue").toString())));
                        }else{
                            maptwo.put("colorvalue",null);
                        }
                        pollutantarry.add(maptwo);
                    }
                }
                Map<String,Object> twomap = new HashMap<>();
                twomap.put("monitortime",DataFormatUtil.getDateYMDHMS(documenttwo.getDate("MonitorTime")));
                twomap.put("pollutantdata",pollutantarry);
                twomap.put("Longitude",documenttwo.getString("Longitude"));
                twomap.put("Latitude",documenttwo.getString("Latitude"));
                datalist.add(twomap);
            }
        }else{
            Document documenttwo = null;
            List<Map<String,Object>> pollutantstwo = null;
            for (int i = 0;i<documents.size();i++){
                documenttwo = documents.get(i);
                pollutantstwo = (List<Map<String, Object>>) documenttwo.get("NavigationDataList");
                List<Map<String,Object>> pollutantarry = new ArrayList<>();
                for (Map<String,Object> maptwo:pollutantstwo){
                    if (codename.get(maptwo.get("PollutantCode").toString())!=null) {
                        if (maptwo.get("MonitorValue")!=null&&!"".equals(maptwo.get("MonitorValue").toString())){
                            maptwo.put("colorvalue",getColorValueByStandard(standards,Double.valueOf(maptwo.get("MonitorValue").toString())));
                        }else{
                            maptwo.put("colorvalue",null);
                        }
                        pollutantarry.add(maptwo);
                    }
                }
                Map<String,Object> twomap = new HashMap<>();
                twomap.put("monitortime",DataFormatUtil.getDateYMDHMS(documenttwo.getDate("MonitorTime")));
                twomap.put("pollutantdata",pollutantarry);
                twomap.put("Longitude",documenttwo.getString("Longitude"));
                twomap.put("Latitude",documenttwo.getString("Latitude"));
                datalist.add(twomap);
            }
        }
    }

    private Object getColorValueByStandard(List<Map<String, Object>> standards, double onesum) {
        Object colorvalue = null;
        if (standards!=null&&standards.size()>0) {
            for (int i = 0;i<standards.size();i++){
                Map<String, Object> map=standards.get(i);
                if (i<standards.size()-1){
                    if (map.get("StandardMaxValue")!=null&&!"".equals(map.get("StandardMaxValue").toString())&&map.get("StandardMinValue")!=null&&!"".equals(map.get("StandardMinValue").toString())) {
                        double min = (double) map.get("StandardMinValue");
                        double max = (double) map.get("StandardMaxValue");
                        if (onesum>min&&onesum<=max){
                            colorvalue = map.get("ColourValue");
                            break;
                        }
                    }
                }else{
                    if (map.get("StandardMinValue")!=null&&!"".equals(map.get("StandardMinValue").toString())) {
                        double min =(double) map.get("StandardMinValue");
                        Object max = map.get("StandardMaxValue");
                        if (max!=null&&!"".equals(max.toString())){
                            if (onesum>min&&onesum<=(double)max){
                                colorvalue = map.get("ColourValue");
                                break;
                            }
                        }else {
                            if (onesum>min){
                                colorvalue = map.get("ColourValue");
                                break;
                            }
                        }

                    }
                }
            }
        }
        return colorvalue;
    }
}

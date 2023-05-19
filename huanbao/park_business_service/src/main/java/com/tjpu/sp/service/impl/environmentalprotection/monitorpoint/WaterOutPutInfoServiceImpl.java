package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterOutputInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.particularpollutants.ParticularPollutantsMapper;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutInfoService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

@Service
@Transactional
public class WaterOutPutInfoServiceImpl implements WaterOutPutInfoService {
    @Autowired
    private WaterOutputInfoMapper waterOutputInfoMapper;
    @Autowired
    private PollutionMapper pollutionMapper;

    @Autowired
    private ParticularPollutantsMapper particularPollutantsMapper;

    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private final String DB_DayFlowData = "DayFlowData";
    private final String DB_MonthFlowData = "MonthFlowData";
    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.countTotalByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description:获取废水、雨水污染源下排放口及状态信息，并组合成污染源排口树形结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datamark：数据标记1-废水、3-雨水
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutionWaterOuputsAndStatus(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
        if (dataList.size() > 0) {
            List<Map<String, Object>> dataListTemp = new ArrayList<>();
            Set<String> tempSet = new HashSet<>();
            for (Map<String, Object> map : dataList) {
                if (!tempSet.contains(map.get("pk_pollutionid").toString())) {
                    tempSet.add(map.get("pk_pollutionid").toString());
                    Map<String, Object> mapTemp = new HashMap<>();
                    mapTemp.put("shortername", map.get("shortername"));
                    mapTemp.put("pk_pollutionid", map.get("pk_pollutionid"));
                    List<Map<String, Object>> outputdata = new ArrayList<>();
                    Map<String, Object> outputMap = new HashMap<>();
                    outputMap.put("pk_id", map.get("pk_id"));
                    outputMap.put("outputname", map.get("outputname"));
                    outputMap.put("dgimn", map.get("dgimn"));
                    outputMap.put("monitorpointtype", map.get("monitorpointtype"));
                    outputMap.put("onlinestatus", map.get("onlinestatus"));
                    outputMap.put("longitude", map.get("Longitude"));
                    outputMap.put("latitude", map.get("Latitude"));
                    outputMap.put("status", map.get("status"));
                    outputMap.put("draindirectionname", map.get("DrainDirectionName"));
                    outputdata.add(outputMap);
                    mapTemp.put("outputdata", outputdata);
                    dataListTemp.add(mapTemp);
                } else {
                    for (Map<String, Object> mapTemp : dataListTemp) {
                        if (mapTemp.get("pk_pollutionid").equals(map.get("pk_pollutionid"))) {
                            List<Map<String, Object>> outputdata = (List<Map<String, Object>>) mapTemp.get("outputdata");
                            Map<String, Object> outputMap = new HashMap<>();
                            outputMap.put("pk_id", map.get("pk_id"));
                            outputMap.put("outputname", map.get("outputname"));
                            outputMap.put("dgimn", map.get("dgimn"));
                            outputMap.put("monitorpointtype", map.get("monitorpointtype"));
                            outputMap.put("onlinestatus", map.get("onlinestatus"));
                            outputMap.put("longitude", map.get("Longitude"));
                            outputMap.put("latitude", map.get("Latitude"));
                            outputMap.put("draindirectionname", map.get("DrainDirectionName"));
                            outputMap.put("status", map.get("status"));
                            outputdata.add(outputMap);
                            mapTemp.put("outputdata", outputdata);
                            break;
                        }
                    }
                }

            }
            dataList = dataListTemp;
        }
        return dataList;
    }
//
//    /**
//     * @author: chengzq
//     * @date: 2019/5/23 0023 下午 4:31
//     * @Description: 通过自定义条件获取废水列表
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [jsonObject]
//     * @throws:
//     */
//    @Override
//    public List<Map<String, Object>> getWatreOutPutByParamMap(JSONObject jsonObject) {
//        return waterOutputInfoMapper.getWatreOutPutByParamMap(jsonObject);
//    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 5:15
     * @Description: 通过id获取废水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public WaterOutputInfoVO selectByPrimaryKey(String pkId) {
        return waterOutputInfoMapper.selectByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 5:42
     * @Description: 通过实体新增排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int insertSelective(WaterOutputInfoVO record) {
        return waterOutputInfoMapper.insertSelective(record);
    }


    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 5:42
     * @Description: 通过实体修改排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int updateByPrimaryKey(WaterOutputInfoVO record) {
        return waterOutputInfoMapper.updateByPrimaryKey(record);
    }

    /**
     * @author: chengzq
     * @date: 2019/5/24 0024 上午 8:43
     * @Description: 通过id删除废水排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkId) {
        waterOutputInfoMapper.deleteAssociateInfoByOutPutID(pkId);
        return waterOutputInfoMapper.deleteByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 下午 6:05
     * @Description: 通过排口类型获取废水排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllOutPutInfoByType(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getAllOutPutInfoByType(paramMap);
    }

//    /**
//     * @author: zhangzc
//     * @date: 2019/5/27 16:01
//     * @Description: 获取在线废水排口信息(分页)
//     * @param:
//     * @return:
//     */
//    @Override
//    public PageInfo<Map<String, Object>> getOnlineWaterOutPutInfoByParamMapForPaging(Integer pageSize, Integer pageNum, Map<String, Object> paramMap) {
//        if (pageSize != null && pageNum != null) {
//            PageHelper.startPage(pageNum, pageSize);
//        }
//        List<Map<String, Object>> listData = waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
//        return new PageInfo<>(listData);
//    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 3:22
     * @Description: 组装污染源、排口、监测污染物、特征污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> setWaterOutPutAndPollutantDetail(List<Map<String, Object>> detailDataTemp, String id, Integer monitorPointType) {
        List<Map<String, Object>> detailData = new ArrayList<>();
        String pollutionid = "";
        for (Map<String, Object> map : detailDataTemp) {
            if ("fk_pollutionid".equals(map.get("fieldname").toString().toLowerCase())) {
                pollutionid = (String) map.get("value");
            }
        }
        //添加污染源信息
        detailData.add(pollutionMap(pollutionid));
        int num = 1;
        List<String> noInList = Arrays.asList("updatetime", "updateuser", "remark");
        //添加排口信息
        for (Map<String, Object> map : detailDataTemp) {
            if (!noInList.contains(map.get("fieldname").toString().toLowerCase())) {
                num = num + 1;
                if (num == 4) {
                    map.put("width", "50%");
                }
                map.put("ordernum", num);
                detailData.add(map);
            }
        }
        //添加特征污染物信息
        num = num + 1;
        detailData.add(getParticularPollutant(pollutionid, id, num, monitorPointType));
        //添加监测污染物信息
        num = num + 1;
        detailData.add(getMonitorPollutant(pollutionid, id, num, monitorPointType));
        return detailData;
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 2:52
     * @Description: 获取监测污染物信息详情格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getMonitorPollutant(String pollutionid, String id, int num, Integer monitorPointType) {
        Map<String, Object> paramMap = new HashMap<>();
        String monitorpollutants = "";
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("outputid", id);
        paramMap.put("monitorpointtype", monitorPointType);
        List<Map<String, Object>> monitorPollutant = pollutantFactorMapper.getPollutantSetInfoByParamMap(paramMap);
        if (monitorPollutant.size() > 0) {
            for (Map<String, Object> pollutant : monitorPollutant) {
                monitorpollutants += pollutant.get("pollutantname") + "、";
            }
            if (StringUtils.isNotBlank(monitorpollutants)) {
                monitorpollutants = monitorpollutants.substring(0, monitorpollutants.length() - 1);
            }
        } else {
            monitorpollutants = "";
        }
        paramMap.clear();
        paramMap.put("fieldname", "monitorpollutants");
        paramMap.put("controltype", "");
        paramMap.put("width", "50%");
        paramMap.put("showhide", true);
        paramMap.put("ordernum", num);
        paramMap.put("label", "监测污染物");
        paramMap.put("type", "string");
        paramMap.put("value", monitorpollutants);
        return paramMap;

    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 2:48
     * @Description: 获取特征污染物信息详情格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getParticularPollutant(String pollutionid, String id, int num, Integer monitorPointType) {
        Map<String, Object> paramMap = new HashMap<>();
        String particularpollutants = "";
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("outputid", id);
        paramMap.put("monitorpointtype", monitorPointType);
        List<Map<String, Object>> particularPollutant = particularPollutantsMapper.getLastVersionPollutantInfoByParamMap(paramMap);
        if (particularPollutant.size() > 0) {
            for (Map<String, Object> pollutant : particularPollutant) {
                particularpollutants += pollutant.get("pollutantname") + "、";
            }
            if (StringUtils.isNotBlank(particularpollutants)) {
                particularpollutants = particularpollutants.substring(0, particularpollutants.length() - 1);
            }
        } else {
            particularpollutants = "";
        }
        paramMap.clear();
        paramMap.put("fieldname", "particularpollutants");
        paramMap.put("controltype", "");
        paramMap.put("width", "50%");
        paramMap.put("showhide", true);
        paramMap.put("ordernum", num);
        paramMap.put("label", "特征污染物");
        paramMap.put("type", "string");
        paramMap.put("value", particularpollutants);
        return paramMap;
    }

    private Map<String, Object> pollutionMap(String pollutionid) {
        PollutionVO pollutionVO = pollutionMapper.selectByPrimaryKey(pollutionid);
        Map<String, Object> map = new HashMap<>();
        map.put("fieldname", "pollutionname");
        map.put("controltype", "");
        map.put("width", "100%");
        map.put("showhide", true);
        map.put("ordernum", 1);
        map.put("label", "企业名称");
        map.put("type", "string");
        map.put("value", pollutionVO.getPollutionname());
        return map;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:07
     * @Description: 获取所有已监测废水排口和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorWaterOutPutAndStatusInfo() {
        return waterOutputInfoMapper.getAllMonitorWaterOutPutAndStatusInfo();
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:07
     * @Description: 获取所有已监测雨水排口和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorRainOutPutAndStatusInfo() {
        return waterOutputInfoMapper.getAllMonitorRainOutPutAndStatusInfo();
    }

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 2:16
     * @Description: 通过排口名称，污染源id查询排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.selectByPollutionidAndOutputName(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 4:17
     * @Description: 通过污染源id查询排口名称和主键
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> selectByPollutionid(String pollutionid) {
        return waterOutputInfoMapper.selectByPollutionid(pollutionid);
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/26 10:55
     * @Description: 动态条件查询污染源排口污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutionWaterOutPutPollutants(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getPollutionWaterOutPutPollutants(paramMap);
    }

    @Override
    public List<Map<String, Object>> getWaterOuPutAndStatusByParamMap(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/27 16:01
     * @Description: 获取在线废水排口信息
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnlineWaterOutPutInfoByParamMap(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 11:11
     * @Description: gis-获取所有废水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllMonitorWaterOutPutInfo(Map<String, Object> pollutionMap) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        paramMap.put("outputtype", "water");
        paramMap.put("orderfield", "status");
        List<Map<String, Object>> waterdata = waterOutputInfoMapper.getAllWaterOrRainOutPutInfoByOutputType(paramMap);
        int onlinenum = 0;
        int offlinenum = 0;
        if (waterdata != null && waterdata.size() > 0) {
            for (Map<String, Object> map : waterdata) {
                int status = 0;
                if (map.get("OnlineStatus") != null && !"".equals(map.get("OnlineStatus").toString())) {//当状态不为空
                    if ("1".equals(map.get("OnlineStatus").toString())) {//有在线排口，有一个在线排口，则该企业为在线企业
                        status = 1;
                    } else {
                        if (status < Integer.parseInt(map.get("OnlineStatus").toString())) {
                            status = Integer.parseInt(map.get("OnlineStatus").toString());
                        }
                    }
                }
                if (status == 0) {//离线
                    offlinenum += 1;
                } else if (status == 1) {//在线
                    onlinenum += 1;
                }
                map.put("OnlineStatus", status);
            }
        }
        result.put("total", (waterdata != null && waterdata.size() > 0) ? waterdata.size() : 0);
        result.put("pollutiontotal", pollutionMap.get("total"));
        result.put("waterpollution", pollutionMap.get("waterpollution"));
        result.put("onlinepollution", pollutionMap.get("onlinepollution"));
        result.put("onlinenum", onlinenum);
        result.put("offlinenum", offlinenum);
        result.put("listdata", waterdata);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 11:22
     * @Description: gis-获取所有雨水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllMonitorRainOutPutInfo(Map<String, Object> pollutionMap) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        paramMap.put("outputtype", "rain");
        paramMap.put("orderfield", "status");
        List<Map<String, Object>> raindata = waterOutputInfoMapper.getAllWaterOrRainOutPutInfoByOutputType(paramMap);
        int onlinenum = 0;
        int offlinenum = 0;
        if (raindata != null && raindata.size() > 0) {
            for (Map<String, Object> map : raindata) {
                int status = 0;
                if (map.get("OnlineStatus") != null && !"".equals(map.get("OnlineStatus").toString())) {//当状态不为空
                    if ("1".equals(map.get("OnlineStatus").toString())) {//有在线排口，有一个在线排口，则该企业为在线企业
                        status = 1;
                    } else {
                        if (status < Integer.parseInt(map.get("OnlineStatus").toString())) {
                            status = Integer.parseInt(map.get("OnlineStatus").toString());
                        }
                    }
                }
                if (status == 0) {//离线
                    offlinenum += 1;
                } else if (status == 1) {//在线
                    onlinenum += 1;
                }
                map.put("OnlineStatus", status);
            }
        }
        result.put("total", (raindata != null && raindata.size() > 0) ? raindata.size() : 0);
        result.put("pollutiontotal", pollutionMap.get("total"));
        result.put("waterpollution", pollutionMap.get("waterpollution"));
        result.put("onlinepollution", pollutionMap.get("onlinepollution"));
        result.put("onlinenum", onlinenum);
        result.put("offlinenum", offlinenum);
        result.put("listdata", raindata);
        return result;
    }

    /**
     * @author: chengzq
     * @date: 2019/11/4 0004 下午 1:28
     * @Description: 删除状态表垃圾数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public int deleteGarbageData() {
        return waterOutputInfoMapper.deleteGarbageData();
    }

    /**
     * @author: xsm
     * @date: 2020/06/17 0017 下午 2:27
     * @Description: 根据自定义参数获取废气废水排口信息及状态（包括停产状态）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getGasAndWaterOutPutAndStatusByParamMap(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getGasAndWaterOutPutAndStatusByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/06/17 0017 下午 2:27
     * @Description: 根据自定义参数获取雨水排口信息及状态（包括排放中状态）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getRainOutPutAndStatusByParamMap(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getRainOutPutAndStatusByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getRainAndWaterOutPutPollutantOnlineDataByParam(Map<String, Object> paramMap) {
        List<String> mns = (List<String>) paramMap.get("mns");
        List<Map<String, Object>> onlineOutPuts = (List<Map<String, Object>>) paramMap.get("onlineOutPuts");
        String code = paramMap.get("pollutantcode").toString();
        String monitortime ="";
        int datamark = Integer.parseInt(paramMap.get("datamark").toString());
        String collection = MongoDataUtils.getCollectionByDataMark(datamark);
        String pollutantDataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);
        Date startDate = null;
        Date endDate = null;
        List<Document> documents = new ArrayList<>();
        if (datamark == 1) {//实时数据
            Bson bson = Filters.and(eq("Type", "RealTimeData"), in("DataGatherCode", mns));
            FindIterable<Document> list = mongoTemplate.getCollection("LatestData").find(bson);
            for (Document document : list) {
                documents.add(document);
            }
        }else{
            if (paramMap.get("monitortime")!=null) {
                monitortime = paramMap.get("monitortime").toString();
                if (datamark == 2) {//分钟数据
                    startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00");
                    endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59");
                } else if (datamark == 3) {//小时数据
                    startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
                    endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
                } else if (datamark == 4) {//日数据
                    startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
                    endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
                } else if (datamark == 5) {//月数据
                    startDate = DataFormatUtil.getDateYMDHMS(monitortime + "-01 00:00:00");
                    endDate = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getLastDayOfMonth(monitortime) + " 23:59:59");
                } else if (datamark == 6) {//年月数据
                    startDate = DataFormatUtil.getDateYMDHMS(monitortime + "-01-01 00:00:00");
                    endDate = DataFormatUtil.getDateYMDHMS(monitortime + "12-31 23:59:59");
                }
                Criteria criteria = new Criteria();
                criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
                List<AggregationOperation> operations = new ArrayList<>();
                operations.add(Aggregation.match(criteria));
                operations.add(unwind(pollutantDataKey));
                operations.add(match(Criteria.where(pollutantDataKey + ".PollutantCode").is(code)));
                operations.add(Aggregation.project("DataGatherCode").and(pollutantDataKey + ".AvgStrength").as("AvgStrength"));
                Aggregation aggregationList = Aggregation.newAggregation(operations)
                        .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
                AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
                documents = pageResults.getMappedResults();
            }
        }
        for (Map<String, Object> map:onlineOutPuts){
            String value = "-";
            if (documents.size()>0){
                String mn = map.get("dgimn")!=null?map.get("dgimn").toString():"";
                for (Document document:documents){
                    if (datamark == 1) {
                        if (!"".equals(mn)&&mn.equals(document.getString("DataGatherCode"))){
                         List <Document> pollutants = (List<Document>) document.get("DataList");
                         if (pollutants.size()>0){
                             for (Document objdocument:pollutants){
                                 if (code.equals(objdocument.getString("PollutantCode"))){
                                     value = objdocument.get("AvgStrength")!=null?objdocument.getString("AvgStrength"):"";
                                 }
                             }
                         }
                        }
                    }else{
                        if (!"".equals(mn)&&mn.equals(document.getString("DataGatherCode"))){
                            value = document.get("AvgStrength")!=null?document.getString("AvgStrength"):"";
                        }
                    }
                }
            }
            map.remove("onlinestatus");
            map.remove("onlinestatusname");
            map.put("monitorvalue",value);
        }
        return onlineOutPuts;
    }

    @Override
    public List<Map<String, Object>> countWGPointData() {
        return waterOutputInfoMapper.countWGPointData();
    }

    @Override
    public List<String> getInOrOutPutMnListByParam(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getInOrOutPutMnListByParam(paramMap);
    }

    @Override
    public List<Document> getDayFlowDataByParam(Map<String, Object> paramMap) {
        List<String> mns = (List<String>) paramMap.get("mns");
        Date startDate = DataFormatUtil.getDateYMD(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMD(paramMap.get("endtime").toString());
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("MonitorTime","TotalFlow"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, DB_DayFlowData, Document.class);
        return pageResults.getMappedResults();
    }

    @Override
    public List<Map<String, Object>> getWSPollutionList() {
        return pollutionMapper.getWSPollutionList();
    }

    @Override
    public List<Map<String,Object>>  getEntWaterSupplyDataByParam(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getEntWaterSupplyDataByParam(paramMap);
    }

    @Override
    public List<Document> getMonthFlowDataByParam(Map<String, Object> paramMap) {
        List<String> mns = (List<String>) paramMap.get("mns");
        Date startDate = DataFormatUtil.getDateYM(paramMap.get("monitortime").toString());
        Date endDate = DataFormatUtil.getDateYM(paramMap.get("monitortime").toString());
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode","MonitorTime","TotalFlow"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, DB_MonthFlowData, Document.class);
        return pageResults.getMappedResults();
    }

    @Override
    public List<Map<String, Object>> getPWOutPutSelectData(Map<String, Object> paramMap) {
        return waterOutputInfoMapper.getPWOutPutSelectData(paramMap);
    }


}

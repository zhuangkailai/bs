package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.navigation.NavigationStandardMapper;
import com.tjpu.sp.dao.environmentalprotection.output.UserMonitorPointRelationDataMapper;
import com.tjpu.sp.dao.environmentalprotection.pollutantvaluescope.PollutantValueScopeMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class OtherMonitorPointServiceImpl implements OtherMonitorPointService {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;

    @Autowired
    private OtherMonitorPointPollutantSetMapper otherMonitorPointPollutantSetMapper;

    @Autowired
    private KeyMonitorPollutantMapper keyMonitorPollutantMapper;
    @Autowired
    private AirMonitorStationMapper airMonitorStationMapper;
    @Autowired
    private PollutantValueScopeMapper pollutantValueScopeMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    @Autowired
    private NavigationStandardMapper navigationStandardMapper;
    @Autowired
    private DeviceStatusMapper deviceStatusMapper;

    @Autowired
    private UserMonitorPointRelationDataMapper userMonitorPointRelationDataMapper;


    /**
     * @author: zhangzc
     * @date: 2019/5/29 15:31
     * @Description: 动态条件获取其他监测点信息
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnlineOtherPointInfoByParamMap(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> isTableDataHaveInfoByParamMap(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.isTableDataHaveInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 下午8:35
     * @Description: 通过监测点名称和监测点类型获取该类型监测点的基础信息及点位状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOtherMonitorPointInfoAndStateByparamMap(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 下午20:59
     * @Description: 根据其它监测点id和监测点类型获取该监测点下监测的所有污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOtherMonitorPointAllPollutantsByIDAndType(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getOtherMonitorPointAllPollutantsByIDAndType(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2019/6/12 0012 下午 4:54
     * @Description: 通过id获取其它监测点的监测设备状态基础信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getOtherMonitorPointDeviceStatusByID(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getOtherMonitorPointDeviceStatusByID(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/6/16 0016 下午 5:27
     * @Description: 根据监测点ID获取附件表对应关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<String> getfileIdsByID(Map<String, Object> parammap) {
        List<Map<String, Object>> oldobj = otherMonitorPointMapper.getfileIdsByID(parammap);
        List<String> list = new ArrayList<>();
        if (oldobj != null && oldobj.size() > 0) {
            for (Map<String, Object> obj : oldobj) {
                list.add(obj.get("FilePath").toString());
            }
        }
        return list;
    }

    /**
     * @author: xsm
     * @date: 2019/6/21 0021 下午 5:33
     * @Description: 获取所有VOC点位信息及状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorEnvironmentalVocAndStatusInfo() {
        return otherMonitorPointMapper.getAllMonitorEnvironmentalVocAndStatusInfo();
    }

    /**
     * @author: xsm
     * @date: 2019/6/21 0021 下午 5:40
     * @Description: 获取所有恶臭点位信息及状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorEnvironmentalStinkAndStatusInfo() {
        return otherMonitorPointMapper.getAllMonitorEnvironmentalStinkAndStatusInfo();
    }

    /**
     * @author: xsm
     * @date: 2019/6/26 0026 下午 6:31
     * @Description: 根据监测点ID和监测点类型获取该监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOtherMonitorPointInfoByIDAndType(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 上午 10:58
     * @Description: 查询所有恶臭及厂界恶臭监测点信息包含状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStenchMonitorPointInfo(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getStenchMonitorPointInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getMicroStationInfo(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getMicroStationInfo(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 下午 2:19
     * @Description: 通过监测点集合查询恶臭及厂界恶臭污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStenchPollutantMonitorPointids(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getStenchPollutantMonitorPointids(paramMap);
    }

    @Override
    public List<Map<String, Object>> getMicroStationPollutantMonitorPointids(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getMicroStationPollutantMonitorPointids(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 上午 10:21
     * @Description: 通过监测点Dgimn号查询空气Dgimn号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn]
     * @throws:
     */
    @Override
    public List<String> getAirDgimnByMonitorDgimn(String dgimn) {
        return otherMonitorPointMapper.getAirDgimnByMonitorDgimn(dgimn);
    }

    /**
     * @author: xsm
     * @date: 2019/7/8 0008 下午 2:26
     * @Description: 根据监测点名称和监测点类型以及MN号获取其它监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    @Override
    public Map<String, Object> selectOtherMonitorPointInfoByParams(Map<String, Object> params) {
        return otherMonitorPointMapper.selectOtherMonitorPointInfoByParams(params);
    }

//    /**
//     * @author: zhangzc
//     * @date: 2019/7/30 15:37
//     * @Description: 条件查询其他监测点企业、排口、污染物信息
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    @Override
//    public List<Map<String, Object>> getOtherMonitorPollutionOutPutPollutants(Map<String, Object> paramMap) {
//        return otherMonitorPointMapper.getOtherMonitorPollutionOutPutPollutants(paramMap);
//    }

    /**
     * @author: xsm
     * @date: 2019/8/10 16:25
     * @Description: gis-根据类型获取所有恶臭或VOC站点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllOtherMonitorPointInfoByType(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> airdata = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
        int onlinenum = 0;
        int offlinenum = 0;
        if (airdata != null && airdata.size() > 0) {
            for (Map<String, Object> map : airdata) {
                int status = 0;
                if (map.get("OnlineStatus") != null && !"".equals(map.get("OnlineStatus").toString())) {//当状态不为空
                    if ("1".equals(map.get("OnlineStatus").toString())) {//1为在线
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
        result.put("total", (airdata != null && airdata.size() > 0) ? airdata.size() : 0);
        result.put("onlinenum", onlinenum);
        result.put("offlinenum", offlinenum);
        result.put("listdata", airdata);
        return result;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 7:54
     * @Description: 通过自定义参数获取监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorInfoByParams(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getAllMonitorInfoByParams(paramMap);
    }

    /**
     * @author: zhangzc
     * @date: 2019/9/4 15:24
     * @Description: 获取恶臭监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getStinkMonitorPoint(int code) {
        return otherMonitorPointMapper.getStinkMonitorPoint(code);
    }

    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 3:59
     * @Description: 通过味道code和mn集合获取恶臭监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> selectStinkInfoBySmellcodeAndMns(Map<String, Object> param) {
        return otherMonitorPointMapper.selectStinkInfoBySmellcodeAndMns(param);
    }

    /**
     * @author: xsm
     * @date: 2019/11/02 0002 上午 11:24
     * @Description: 获取所有传输通道点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTransportChannelMonitorPointInfos() {
        return otherMonitorPointMapper.getTransportChannelMonitorPointInfos();
    }

    @Override
    public OtherMonitorPointVO getOtherMonitorPointByID(String pkid) {
        return otherMonitorPointMapper.selectByPrimaryKey(pkid);
    }

    /**
     * @author: xsm
     * @date: 2019/11/23 0023 上午 11:51
     * @Description: 根据恶臭MN号获取关联的空气Mn号（无关联则取自身，包括厂界恶臭和环境恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<String> getAirDgimnByStinkMonitorDgimn(String dgimn) {
        return otherMonitorPointMapper.getAirDgimnByStinkMonitorDgimn(dgimn);
    }

    /**
     * @author: xsm
     * @date: 2020/04/09 0009 上午 10:47
     * @Description: 获取所有气象点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorEnvironmentalMeteoAndStatusInfo() {
        return otherMonitorPointMapper.getAllMonitorEnvironmentalMeteoAndStatusInfo();
    }

    /**
     * @author: xsm
     * @date: 2020/6/10 0010 下午 6:03
     * @Description: 获取气象站点分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public PageInfo<Map<String, Object>> getOnlineMeteoInfoByParamMapForPage(Integer pagesize, Integer pagenum, HashMap<Object, Object> objectObjectHashMap) {
        if (pagesize != null && pagenum != null) {
            PageHelper.startPage(pagenum, pagesize);
        }
        List<Map<String, Object>> listData = otherMonitorPointMapper.getAllMonitorEnvironmentalMeteoAndStatusInfo();
        return new PageInfo<>(listData);
    }

    /**
     * @author: xsm
     * @date: 2020/6/11 0011 下午 2:11
     * @Description: 获取所有微站点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorMicroStationAndStatusInfo() {
        return otherMonitorPointMapper.getAllMonitorMicroStationAndStatusInfo();
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 根据监测时间和恶臭点位MN号及污染物获取恶臭污染物超标数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getStenchOverDataAndWeatherDataByParamMap(Map<String, Object> paramMap) {
        String dgimn = paramMap.get("dgimn").toString();
        List<String> mnlist = new ArrayList<>();
        Map<String, Object> standmap = new HashMap<>();
        standmap.put("standardmaxvalue", null);
        standmap.put("standardminvalue", null);
        mnlist.add(dgimn);
        paramMap.put("dgimns", mnlist);
        //通过MN号获取对应空气站MN号
        List<Map<String, Object>> airlist = airMonitorStationMapper.getAirStationsByMN(paramMap);
        Map<String, Object> result = new HashMap<>();
        String airmn = "";
        if (airlist != null && airlist.size() > 0 && (airlist.get(0).get("airmn") != null && !"".equals(airlist.get(0).get("airmn").toString()))) {//判断该点位是否关联空气站
            airmn = airlist.get(0).get("airmn").toString();//获取空气点位MN号
        }
        //判断是自身还是关联的空气点MN
        String pollutantcode = paramMap.get("pollutantcode").toString();
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        paramMap.put("fkpollutantcode", pollutantcode);
        List<Map<String, Object>> pollutantValueScopeByParamMap = pollutantValueScopeMapper.getPollutantValueScopeByParamMap(paramMap);
        List<Map<String, Object>> listmap = otherMonitorPointMapper.getOtherMonitorPointAllPollutantsByIDAndType(paramMap);//恶臭
        if (listmap != null && listmap.size() > 0) {
            standmap.put("standardmaxvalue", listmap.get(0).get("standardmaxvalue"));
            standmap.put("standardminvalue", listmap.get(0).get("standardminvalue"));
        }
        if (airmn.equals(dgimn)) {//是同一MN  则 污染物监测值和风向风速  同时查
            result = getOverPollutantAndWeatherData(dgimn, pollutantcode, startDate, endDate, pollutantValueScopeByParamMap, standmap);
        } else {//不是同一MN  则分开查
            result = getStinkOverAndAirWeatherData(dgimn, airmn, pollutantcode, startDate, endDate, pollutantValueScopeByParamMap, standmap);
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 获取关联空气监测点的恶臭超标污染物浓度及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getStinkOverAndAirWeatherData(String dgimn, String airmn, String pollutantcode, Date startDate, Date endDate, List<Map<String, Object>> pollutantValueScopeByParamMap, Map<String, Object> standmap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        aggregations.add(match(criteria));
        criteria1.and("RealDataList.PollutantCode").is(pollutantcode).and("RealDataList.IsOverStandard").is(true);
        aggregations.add(unwind("RealDataList"));
        aggregations.add(match(criteria1));
        aggregations.add(project("DataGatherCode", "MonitorTime").and("RealDataList.MonitorValue").as("MonitorValue")
                .and("RealDataList.PollutantCode").as("PollutantCode"));
        aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "RealTimeData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> overlist = new ArrayList<>();
        List<Map<String, Object>> weatherlist = new ArrayList<>();
        List<Map<String, Object>> weathernumlist = new ArrayList<>();
        if (resultDocument.size() > 0) {
            Date start = resultDocument.get(0).getDate("MonitorTime");
            Date end = resultDocument.get(resultDocument.size() - 1).getDate("MonitorTime");
            //从枚举类中获取风速风向的编码
            List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                    , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
            List<AggregationOperation> aggregationstwo = new ArrayList<>();
            Criteria criteria2 = new Criteria();
            criteria2.and("DataGatherCode").is(airmn).and("MonitorTime").gte(start).lte(end).and("RealDataList.PollutantCode").in(pollutantcodes);
            aggregationstwo.add(match(criteria2));
            aggregationstwo.add(project("DataGatherCode", "MonitorTime", "RealDataList"));
            aggregationstwo.add(sort(Sort.Direction.ASC, "MonitorTime"));
            Aggregation aggregation2 = newAggregation(aggregationstwo);
            AggregationResults<Document> resulttwo = mongoTemplate.aggregate(aggregation2, "RealTimeData", Document.class);
            List<Document> resultDocumenttwo = resulttwo.getMappedResults();
            for (Document document : resultDocument) {
                String monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                String value = "";
                String windspeed = "";
                String winddirection = "";
                if (pollutantcode.equals(document.getString("PollutantCode"))) {
                    value = document.getString("MonitorValue");
                }
                if (resultDocumenttwo.size() > 0) {
                    for (Document documenttwo : resultDocumenttwo) {
                        String monitortimetwo = DataFormatUtil.getDateYMDHMS(documenttwo.getDate("MonitorTime"));
                        if (!(documenttwo.getDate("MonitorTime")).after(document.getDate("MonitorTime"))) {
                            if (monitortime.equals(monitortimetwo)) {
                                List<Document> pollutants = (List<Document>) documenttwo.get("RealDataList");
                                if (pollutants.size() > 0) {
                                    for (Document podocument : pollutants) {
                                        if ((CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()).equals(podocument.getString("PollutantCode"))) {
                                            winddirection = podocument.getString("MonitorValue");
                                        }
                                        if ((CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode()).equals(podocument.getString("PollutantCode"))) {
                                            windspeed = podocument.getString("MonitorValue");
                                        }
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (!"".equals(value)) {
                    Map<String, Object> valuemap = new HashMap<>();
                    valuemap.put("monitortime", monitortime);
                    valuemap.put("monitorvalue", value);
                    valuemap.put("standardmaxvalue", standmap.get("standardmaxvalue"));
                    valuemap.put("standardminvalue", standmap.get("standardminvalue"));
                    overlist.add(valuemap);
                    Map<String, Object> valuemap2 = new HashMap<>();
                    valuemap2.put("monitortime", monitortime);
                    if (!"".equals(winddirection)) {
                        valuemap2.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "code"));
                        valuemap2.put("winddirectionname", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "name"));
                        valuemap2.put("winddirectionvalue", winddirection);
                        valuemap2.put("windspeed", !"".equals(windspeed) ? windspeed : null);
                    } else {
                        valuemap2.put("winddirectioncode", null);
                        valuemap2.put("winddirectionvalue", null);
                        valuemap2.put("winddirectionname", null);
                        valuemap2.put("windspeed", !"".equals(windspeed) ? windspeed : null);
                    }
                    weatherlist.add(valuemap2);
                }
            }

        }
        if (weatherlist != null && weatherlist.size() > 0) {
            for (Map<String, Object> map : pollutantValueScopeByParamMap) {
                String valuescope = map.get("valuescope") == null ? "" : map.get("valuescope").toString();
                String[] scopeArr = valuescope.split("-");
                //单个数字类型
                if (scopeArr.length == 1) {
                    String standard = scopeArr[0];
                    weatherlist.stream().filter(m -> m.get("winddirectionvalue") != null && standard.equals(m.get("winddirectionvalue").toString())).forEach(m -> {
                        m.put("valuescope", valuescope);
                    });
                }
                //范围类型
                else if (scopeArr.length == 2) {
                    Double standardfir = Double.valueOf(scopeArr[0]);
                    Double standardsec = Double.valueOf(scopeArr[1]);
                    weatherlist.stream().filter(m -> m.get("winddirectionvalue") != null && standardfir <= Double.valueOf(m.get("winddirectionvalue").toString()) && standardsec >= Double.valueOf(m.get("winddirectionvalue").toString())).forEach(m -> {
                        m.put("valuescope", valuescope);
                    });
                }
            }
            Map<String, List<Map<String, Object>>> collect = weatherlist.stream().filter(m -> m.get("valuescope") != null).collect(Collectors.groupingBy(m -> m.get("valuescope").toString()));
            String[] names = DataFormatUtil.directName;
            String[] codes = DataFormatUtil.directCode;
            for (String valuescope : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(valuescope);
                Map<String, List<Map<String, Object>>> collect2 = list.stream().filter(m -> m.get("winddirectionname") != null).collect(Collectors.groupingBy(m -> m.get("winddirectionname").toString()));
                List<Map<String, Object>> winddatalist = new ArrayList<>();
                Map<String, Object> onemap = new HashMap<>();
                for (String name : collect2.keySet()) {
                    String code = "";
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < names.length; i++) {
                        if (name.equals(names[i])) {
                            code = codes[i];
                            break;
                        }
                    }
                    map.put("winddirectioncode", code);
                    map.put("winddirectionname", name);
                    map.put("num", collect2.get(name).size());
                    winddatalist.add(map);
                }
                onemap.put("valuescope", valuescope);
                onemap.put("winddatalist", winddatalist);
                weathernumlist.add(onemap);
            }
        }
        result.put("overdata", overlist);
        result.put("weatherdata", weatherlist);
        if (weatherlist != null && weatherlist.size() > 0) {
            //排序 监测点类型 污染源名称 监测点名称 升序
            List<Map<String, Object>> col = weathernumlist.stream().sorted(Comparator.comparing((Map m) -> m.get("valuescope").toString())).collect(Collectors.toList());
            result.put("weathernumdata", col);
        } else {
            result.put("weathernumdata", weathernumlist);
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 获取恶臭超标污染物浓度及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getOverPollutantAndWeatherData(String dgimn, String pollutantcode, Date startDate, Date endDate, List<Map<String, Object>> pollutantValueScopeByParamMap, Map<String, Object> standmap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate).and("RealDataList.PollutantCode").is(pollutantcode).and("RealDataList.IsOverStandard").is(true);
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "MonitorTime", "RealDataList"));
        aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "RealTimeData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        String directioncode = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
        String windspeedcode = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> overlist = new ArrayList<>();
        List<Map<String, Object>> weatherlist = new ArrayList<>();
        List<Map<String, Object>> weathernumlist = new ArrayList<>();
        if (resultDocument.size() > 0) {
            for (Document document : resultDocument) {
                String monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                List<Document> pollutants = (List<Document>) document.get("RealDataList");
                if (pollutants.size() > 0) {
                    boolean flag = false;
                    String value = "";
                    String windspeed = "";
                    String winddirection = "";
                    for (Document podocument : pollutants) {
                        if (pollutantcode.equals(podocument.getString("PollutantCode")) && podocument.getBoolean("IsOverStandard") == true) {
                            flag = true;
                            value = podocument.getString("MonitorValue");
                        }
                        if (directioncode.equals(podocument.getString("PollutantCode"))) {
                            winddirection = podocument.getString("MonitorValue");
                        }
                        if (windspeedcode.equals(podocument.getString("PollutantCode"))) {
                            windspeed = podocument.getString("MonitorValue");
                        }
                    }
                    if (flag == true && !"".equals(value)) {
                        Map<String, Object> valuemap = new HashMap<>();
                        Map<String, Object> valuemap2 = new HashMap<>();
                        valuemap.put("monitortime", monitortime);
                        valuemap.put("standardmaxvalue", standmap.get("standardmaxvalue"));
                        valuemap.put("standardminvalue", standmap.get("standardminvalue"));
                        valuemap.put("monitorvalue", value);
                        valuemap2.put("monitortime", monitortime);
                        if (!"".equals(winddirection)) {
                            valuemap2.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "code"));
                            valuemap2.put("winddirectionname", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "name"));
                            valuemap2.put("winddirectionvalue", winddirection);
                            valuemap2.put("windspeed", !"".equals(windspeed) ? windspeed : null);
                        } else {
                            valuemap2.put("winddirectioncode", null);
                            valuemap2.put("winddirectionvalue", null);
                            valuemap2.put("winddirectionname", null);
                            valuemap2.put("windspeed", !"".equals(windspeed) ? windspeed : null);
                        }
                        overlist.add(valuemap);
                        weatherlist.add(valuemap2);
                    }
                }

            }
        }
        for (Map<String, Object> map : pollutantValueScopeByParamMap) {
            String valuescope = map.get("valuescope") == null ? "" : map.get("valuescope").toString();
            String[] scopeArr = valuescope.split("-");
            //单个数字类型
            if (scopeArr.length == 1) {
                String standard = scopeArr[0];
                weatherlist.stream().filter(m -> m.get("winddirectionvalue") != null && standard.equals(m.get("winddirectionvalue").toString())).forEach(m -> {
                    m.put("valuescope", valuescope);
                });
            }
            //范围类型
            else if (scopeArr.length == 2) {
                Double standardfir = Double.valueOf(scopeArr[0]);
                Double standardsec = Double.valueOf(scopeArr[1]);
                weatherlist.stream().filter(m -> m.get("winddirectionvalue") != null && standardfir <= Double.valueOf(m.get("winddirectionvalue").toString()) && standardsec >= Double.valueOf(m.get("winddirectionvalue").toString())).forEach(m -> {
                    m.put("valuescope", valuescope);
                });
            }
        }
        String ee = "";
        Map<String, List<Map<String, Object>>> collect = weatherlist.stream().filter(m -> m.get("valuescope") != null).collect(Collectors.groupingBy(m -> m.get("valuescope").toString()));
        String[] names = DataFormatUtil.directName;
        String[] codes = DataFormatUtil.directCode;
        for (String valuescope : collect.keySet()) {
            List<Map<String, Object>> list = collect.get(valuescope);
            Map<String, List<Map<String, Object>>> collect2 = list.stream().filter(m -> m.get("winddirectionname") != null).collect(Collectors.groupingBy(m -> m.get("winddirectionname").toString()));
            List<Map<String, Object>> winddatalist = new ArrayList<>();
            Map<String, Object> onemap = new HashMap<>();
            for (String name : collect2.keySet()) {
                String code = "";
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < names.length; i++) {
                    if (name.equals(names[i])) {
                        code = codes[i];
                        break;
                    }
                }
                map.put("winddirectioncode", code);
                map.put("winddirectionname", name);
                map.put("num", collect2.get(name).size());
                winddatalist.add(map);
            }
            onemap.put("valuescope", valuescope);
            onemap.put("winddatalist", winddatalist);
            weathernumlist.add(onemap);
        }
        result.put("overdata", overlist);
        result.put("weatherdata", weatherlist);
        if (weatherlist != null && weatherlist.size() > 0) {
            //排序 监测点类型 污染源名称 监测点名称 升序
            List<Map<String, Object>> col = weathernumlist.stream().sorted(Comparator.comparing((Map m) -> m.get("valuescope").toString())).collect(Collectors.toList());
            result.put("weathernumdata", col);
        } else {
            result.put("weathernumdata", weathernumlist);
        }
        return result;

    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和MN号获取该时间段内超标污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStenchOverPollutantByParamMap(String dgimn, String starttime, String endtime) {
        List<Map<String, Object>> result = new ArrayList<>();
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> codeandunit = new HashMap<>();
        paramMap.put("dgimn", dgimn);
        List<Map<String, Object>> stenchPollutantMonitorPointids = otherMonitorPointMapper.getStenchPollutantMonitorPointids(paramMap);
        if (stenchPollutantMonitorPointids != null && stenchPollutantMonitorPointids.size() > 0) {
            List<String> pollutants = new ArrayList<>();
            paramMap.clear();
            for (Map<String, Object> map : stenchPollutantMonitorPointids) {
                paramMap.put(map.get("code").toString(), map.get("name"));
                pollutants.add(map.get("code").toString());
                codeandunit.put(map.get("code").toString(), map.get("pollutantunit"));

            }
            List<AggregationOperation> aggregations = new ArrayList<>();
            Criteria criteria = new Criteria();
            Criteria criteria1 = new Criteria();
            criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
            aggregations.add(match(criteria));
            criteria1.and("RealDataList.PollutantCode").in(pollutants).and("RealDataList.IsOverStandard").is(true);
            aggregations.add(unwind("RealDataList"));
            aggregations.add(match(criteria1));
            aggregations.add(project("DataGatherCode", "MonitorTime").and("RealDataList.MonitorValue").as("MonitorValue")
                    .and("RealDataList.PollutantCode").as("PollutantCode"));
            GroupOperation groupOperation = group("PollutantCode");
            aggregations.add(groupOperation);
            Aggregation aggregation = newAggregation(aggregations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "RealTimeData", Document.class);
            List<Document> resultDocument = results.getMappedResults();
            if (resultDocument.size() > 0) {
                for (Document document : resultDocument) {
                    Map<String, Object> objmap = new HashMap<>();
                    objmap.put("pollutantcode", document.getString("_id"));
                    objmap.put("pollutantname", paramMap.get(document.getString("_id")));
                    objmap.put("pollutantunit", codeandunit.get(document.getString("_id")));
                    result.add(objmap);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和恶臭点位MN号及污染物获取恶臭污染物超标列表数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime，pollutantcode]
     * @throws:
     */
    @Override
    public Map<String, Object> getStenchOverAndWeatherListDataByParamMap(Map<String, Object> paramMap) {
        String dgimn = paramMap.get("dgimn").toString();
        List<String> mnlist = new ArrayList<>();
        mnlist.add(dgimn);
        paramMap.put("dgimns", mnlist);
        paramMap.put("pointtypes", Arrays.asList(FactoryBoundaryStinkEnum.getCode(), EnvironmentalStinkEnum.getCode()));
        String monitorpointname = "";
        //通过MN号获取对应空气站MN号
        List<Map<String, Object>> airlist = airMonitorStationMapper.getAirStationsByMN(paramMap);
        List<Map<String, Object>> thelist = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
        if (thelist != null && thelist.size() > 0) {
            monitorpointname = thelist.get(0).get("MonitorPointName").toString();
        }
        Map<String, Object> result = new HashMap<>();
        String airmn = "";
        if (airlist != null && airlist.size() > 0 && (airlist.get(0).get("airmn") != null && !"".equals(airlist.get(0).get("airmn").toString()))) {//判断该点位是否关联空气站
            airmn = airlist.get(0).get("airmn").toString();//获取空气点位MN号
        }
        //判断是自身还是关联的空气点MN
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        if (airmn.equals(dgimn)) {//是同一MN  则 污染物监测值和风向风速  同时查
            result = getOverPollutantAndWeatherListData(dgimn, monitorpointname, pollutantcodes, startDate, endDate, paramMap);
        } else {//不是同一MN  则分开查
            result = getStinkOverAndAirWeatherListData(dgimn, monitorpointname, airmn, pollutantcodes, startDate, endDate, paramMap);
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 获取恶臭超标污染物浓度及风向列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getOverPollutantAndWeatherListData(String dgimn, String monitorpointname, List<String> pollutantcodes, Date startDate, Date endDate, Map<String, Object> parammap) {
        Map<String, Object> datamap = new HashMap<>();
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (parammap.get("pagenum") != null && parammap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(parammap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(parammap.get("pagesize").toString()));
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.addCriteria(Criteria.where("RealDataList.PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("RealDataList.IsOverStandard").is(true));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        //总条数
        long totalCount = mongoTemplate.count(query, "RealTimeData");
        datamap.put("total", totalCount);
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate).and("RealDataList.PollutantCode").in(pollutantcodes).and("RealDataList.IsOverStandard").is(true);
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "MonitorTime", "RealDataList"));
        aggregations.add(sort(Sort.Direction.DESC, "MonitorTime"));
        if (parammap.get("pagenum") != null && parammap.get("pagesize") != null) {
            aggregations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            aggregations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "RealTimeData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        String directioncode = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
        String windspeedcode = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
        pollutantcodes.add(directioncode);
        pollutantcodes.add(windspeedcode);
        List<Map<String, Object>> result = new ArrayList<>();
        if (resultDocument.size() > 0) {
            for (Document document : resultDocument) {
                String monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                List<Document> pollutants = (List<Document>) document.get("RealDataList");
                Map<String, Object> valuemap = new HashMap<>();
                valuemap.put("monitorpointname", monitorpointname);
                valuemap.put("monitortime", monitortime);
                if (pollutants.size() > 0) {
                    for (String code : pollutantcodes) {
                        String value = "";
                        for (Document podocument : pollutants) {
                            if (code.equals(podocument.getString("PollutantCode"))) {
                                value = podocument.getString("MonitorValue");
                                break;
                            }
                        }
                        if (code.equals(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode())) {
                            if (!"".equals(value)) {
                                value = DataFormatUtil.windDirectionSwitch(Double.valueOf(value), "name");
                            }
                        }
                        valuemap.put("monitorvalue_" + code, value);
                    }
                    result.add(valuemap);
                }
            }
        }
        datamap.put("datalist", result);
        return datamap;
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 获取关联空气点位的恶臭超标污染物浓度及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getStinkOverAndAirWeatherListData(String dgimn, String monitorpointname, String airmn, List<String> pollutantcodes, Date startDate, Date endDate, Map<String, Object> parammap) {
        Map<String, Object> datamap = new HashMap<>();
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (parammap.get("pagenum") != null && parammap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(parammap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(parammap.get("pagesize").toString()));
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.addCriteria(Criteria.where("RealDataList.PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("RealDataList.IsOverStandard").is(true));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        //总条数
        long totalCount = mongoTemplate.count(query, "RealTimeData");
        datamap.put("total", totalCount);
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate).and("RealDataList.PollutantCode").in(pollutantcodes).and("RealDataList.IsOverStandard").is(true);
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "MonitorTime", "RealDataList"));
        aggregations.add(sort(Sort.Direction.DESC, "MonitorTime"));
        if (parammap.get("pagenum") != null && parammap.get("pagesize") != null) {
            aggregations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            aggregations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "RealTimeData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        List<Map<String, Object>> result = new ArrayList<>();
        if (resultDocument.size() > 0) {
            Date end = resultDocument.get(0).getDate("MonitorTime");
            Date start = resultDocument.get(resultDocument.size() - 1).getDate("MonitorTime");
            //从枚举类中获取风速风向的编码
            List<String> weathercodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                    , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
            List<AggregationOperation> aggregationstwo = new ArrayList<>();
            Criteria criteria2 = new Criteria();
            criteria2.and("DataGatherCode").is(airmn).and("MonitorTime").gte(start).lte(end).and("RealDataList.PollutantCode").in(weathercodes);
            aggregationstwo.add(match(criteria2));
            aggregationstwo.add(project("DataGatherCode", "MonitorTime", "RealDataList"));
            aggregationstwo.add(sort(Sort.Direction.DESC, "MonitorTime"));
            Aggregation aggregation2 = newAggregation(aggregationstwo);
            AggregationResults<Document> resulttwo = mongoTemplate.aggregate(aggregation2, "RealTimeData", Document.class);
            List<Document> resultDocumenttwo = resulttwo.getMappedResults();
            for (Document document : resultDocument) {
                String monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                List<Document> pollutants = (List<Document>) document.get("RealDataList");
                Map<String, Object> valuemap = new HashMap<>();
                valuemap.put("monitorpointname", monitorpointname);
                valuemap.put("monitortime", monitortime);
                if (pollutants.size() > 0) {
                    for (String code : pollutantcodes) {
                        String value = "";
                        for (Document podocument : pollutants) {
                            if (code.equals(podocument.getString("PollutantCode"))) {
                                value = podocument.getString("MonitorValue");
                                break;
                            }
                        }
                        valuemap.put("monitorvalue_" + code, value);
                    }
                }
                valuemap.put("monitorvalue_" + CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode(), "");
                valuemap.put("monitorvalue_" + CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode(), "");
                if (resultDocumenttwo.size() > 0) {
                    for (Document documenttwo : resultDocumenttwo) {
                        String monitortimetwo = DataFormatUtil.getDateYMDHMS(documenttwo.getDate("MonitorTime"));
                        if (!(documenttwo.getDate("MonitorTime")).after(document.getDate("MonitorTime"))) {
                            if (monitortime.equals(monitortimetwo)) {
                                List<Document> pollutantstwo = (List<Document>) documenttwo.get("RealDataList");
                                if (pollutantstwo.size() > 0) {
                                    for (String codetwo : weathercodes) {
                                        String valuetwo = "";
                                        for (Document podocument : pollutantstwo) {
                                            if (codetwo.equals(podocument.getString("PollutantCode"))) {
                                                valuetwo = podocument.getString("MonitorValue");
                                                break;
                                            }
                                        }
                                        if (codetwo.equals(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode())) {
                                            if (!"".equals(valuetwo)) {
                                                valuetwo = DataFormatUtil.windDirectionSwitch(Double.valueOf(valuetwo), "name");
                                            }
                                        }
                                        valuemap.put("monitorvalue_" + codetwo, valuetwo);
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
                result.add(valuemap);
            }
        }
        datamap.put("datalist", result);
        return datamap;
    }

    @Override
    public List<Map<String, Object>> countManyWindOverPollutantValueData(Map<String, Object> paramMap) {
        String dgimn = paramMap.get("dgimn").toString();
        List<String> mnlist = new ArrayList<>();
        mnlist.add(dgimn);
        paramMap.put("dgimns", mnlist);
        //通过MN号获取对应空气站MN号
        List<Map<String, Object>> airlist = airMonitorStationMapper.getAirStationsByMN(paramMap);
        List<Map<String, Object>> result = new ArrayList<>();
        String airmn = "";
        if (airlist != null && airlist.size() > 0 && (airlist.get(0).get("airmn") != null && !"".equals(airlist.get(0).get("airmn").toString()))) {//判断该点位是否关联空气站
            airmn = airlist.get(0).get("airmn").toString();//获取空气点位MN号
        }
        //判断是自身还是关联的空气点MN
        String pollutantcode = paramMap.get("pollutantcode").toString();
        paramMap.put("fkpollutantcode", pollutantcode);
//        List<Map<String, Object>> pollutantValueScopeByParamMap = pollutantValueScopeMapper.getPollutantValueScopeByParamMap(paramMap);

        paramMap.put("monitorpointtype", EnvironmentalStinkEnum.getCode());
        paramMap.put("pollutantcode", pollutantcode);
        List<Map<String, Object>> pollutantValueScopeByParamMap = navigationStandardMapper.getStandardColorDataByParamMap(paramMap);

        if (airmn.equals(dgimn)) {//是同一MN  则 污染物监测值和风向风速  同时查
            result = getOverPollutantAndWeatherHourData(dgimn, pollutantcode, paramMap.get("starttime").toString(), paramMap.get("endtime").toString(), pollutantValueScopeByParamMap);
        } else {//不是同一MN  则分开查
            result = getStinkOverAndAirWeatherHourData(dgimn, airmn, pollutantcode, paramMap.get("starttime").toString(), paramMap.get("endtime").toString(), pollutantValueScopeByParamMap);
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 获取关联空气监测点的恶臭超标污染物浓度及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkOverAndAirWeatherHourData(String dgimn, String airmn, String pollutantcode, String starttime, String endtime, List<Map<String, Object>> pollutantValueScopeByParamMap) {
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        List<String> daylist = DataFormatUtil.getYMDBetween(starttime, endtime);
        daylist.add(endtime);
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        aggregations.add(match(criteria));
        criteria1.and("HourDataList.PollutantCode").is(pollutantcode).and("HourDataList.IsOverStandard").is(true);
        aggregations.add(unwind("HourDataList"));
        aggregations.add(match(criteria1));
        aggregations.add(project("DataGatherCode", "MonitorTime").and("HourDataList.AvgStrength").as("AvgStrength")
                .and("HourDataList.PollutantCode").as("PollutantCode"));
        aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        List<Map<String, Object>> result = new ArrayList<>();
        String[] names = DataFormatUtil.directName;
        String[] codes = DataFormatUtil.directCode;
        List<Document> resultDocumenttwo = new ArrayList<>();
        if (resultDocument.size() > 0) {
            Date start = resultDocument.get(0).getDate("MonitorTime");
            Date end = resultDocument.get(resultDocument.size() - 1).getDate("MonitorTime");
            //从枚举类中获取风速风向的编码
            List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                    , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
            List<AggregationOperation> aggregationstwo = new ArrayList<>();
            Criteria criteria2 = new Criteria();
            criteria2.and("DataGatherCode").is(airmn).and("MonitorTime").gte(start).lte(end).and("HourDataList.PollutantCode").in(pollutantcodes);
            aggregationstwo.add(match(criteria2));
            aggregationstwo.add(project("DataGatherCode", "MonitorTime", "HourDataList"));
            aggregationstwo.add(sort(Sort.Direction.ASC, "MonitorTime"));
            Aggregation aggregation2 = newAggregation(aggregationstwo);
            AggregationResults<Document> resulttwo = mongoTemplate.aggregate(aggregation2, "HourData", Document.class);
            resultDocumenttwo = resulttwo.getMappedResults();
        }
        for (String ymd : daylist) {
            Map<String, Object> resultmap = new HashMap<>();
            List<Map<String, Object>> weatherlist = new ArrayList<>();
            List<Map<String, Object>> weathernumlist = new ArrayList<>();
            if (resultDocument.size() > 0) {
                for (Document document : resultDocument) {
                    String theymd = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    if (ymd.equals(theymd)) {
                        String ymdh = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                        String value = "";
                        String windspeed = "";
                        String winddirection = "";
                        if (pollutantcode.equals(document.getString("PollutantCode"))) {
                            value = document.getString("AvgStrength");
                        }
                        if (resultDocumenttwo.size() > 0) {
                            for (Document documenttwo : resultDocumenttwo) {
                                String monitortimetwo = DataFormatUtil.getDateYMDH(documenttwo.getDate("MonitorTime"));
                                if (!(documenttwo.getDate("MonitorTime")).after(document.getDate("MonitorTime"))) {
                                    if (ymdh.equals(monitortimetwo)) {
                                        List<Document> pollutants = (List<Document>) documenttwo.get("HourDataList");
                                        if (pollutants.size() > 0) {
                                            for (Document podocument : pollutants) {
                                                if ((CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()).equals(podocument.getString("PollutantCode"))) {
                                                    winddirection = podocument.getString("AvgStrength");
                                                }
                                                if ((CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode()).equals(podocument.getString("PollutantCode"))) {
                                                    windspeed = podocument.getString("AvgStrength");
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        if (!"".equals(value)) {
                            Map<String, Object> valuemap2 = new HashMap<>();
                            valuemap2.put("monitortime", theymd);
                            valuemap2.put("value", value);
                            if (!"".equals(winddirection)) {
                                valuemap2.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "code"));
                                valuemap2.put("winddirectionname", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "name"));
                                valuemap2.put("winddirectionvalue", winddirection);
                                valuemap2.put("windspeed", !"".equals(windspeed) ? windspeed : null);
                                weatherlist.add(valuemap2);
                            } /*else {
                                    valuemap2.put("winddirectioncode", null);
                                    valuemap2.put("winddirectionvalue", null);
                                    valuemap2.put("winddirectionname", null);
                                    valuemap2.put("windspeed", !"".equals(windspeed) ? windspeed : null);
                                }*/

                        }
                    }
                }
                if (weatherlist != null && weatherlist.size() > 0) {
                    for (Map<String, Object> map : pollutantValueScopeByParamMap) {
                        String valuescope = map.get("valuescope") == null ? "" : map.get("valuescope").toString();
                        String[] scopeArr = valuescope.split("-");
                        //单个数字类型
                        if (scopeArr.length == 1) {
                            String standard = scopeArr[0];
                            weatherlist.stream().filter(m -> m.get("value") != null && standard.equals(m.get("value").toString())).forEach(m -> {
                                m.put("valuescope", valuescope);
                            });
                        }
                        //范围类型
                        else if (scopeArr.length == 2) {
                            Double standardfir = Double.valueOf(scopeArr[0]);
                            Double standardsec = Double.valueOf(scopeArr[1]);
                            weatherlist.stream().filter(m -> m.get("value") != null && standardfir <= Double.valueOf(m.get("value").toString()) && standardsec >= Double.valueOf(m.get("value").toString())).forEach(m -> {
                                m.put("valuescope", valuescope);
                            });
                        }
                    }
                    Map<String, List<Map<String, Object>>> collect = weatherlist.stream().filter(m -> m.get("valuescope") != null).collect(Collectors.groupingBy(m -> m.get("valuescope").toString()));
                    for (String valuescope : collect.keySet()) {
                        List<Map<String, Object>> list = collect.get(valuescope);
                        Map<String, List<Map<String, Object>>> collect2 = list.stream().filter(m -> m.get("winddirectionname") != null).collect(Collectors.groupingBy(m -> m.get("winddirectionname").toString()));
                        List<Map<String, Object>> winddatalist = new ArrayList<>();
                        Map<String, Object> onemap = new HashMap<>();
                        for (String name : collect2.keySet()) {
                            String code = "";
                            Map<String, Object> map = new HashMap<>();
                            for (int i = 0; i < names.length; i++) {
                                if (name.equals(names[i])) {
                                    code = codes[i];
                                    break;
                                }
                            }
                            map.put("winddirectioncode", code);
                            map.put("winddirectionname", name);
                            map.put("num", collect2.get(name).size());
                            winddatalist.add(map);
                        }
                        onemap.put("valuescope", valuescope);
                        onemap.put("winddatalist", winddatalist);
                        weathernumlist.add(onemap);
                    }
                }
            }
            resultmap.put("monitortime", ymd);
            resultmap.put("weathernumdata", weathernumlist);
            if (weathernumlist.size() > 0) {
                result.add(resultmap);
            }
        }

        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 获取恶臭超标污染物浓度及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getOverPollutantAndWeatherHourData(String dgimn, String pollutantcode, String starttime, String endtime, List<Map<String, Object>> pollutantValueScopeByParamMap) {
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        List<Map<String, Object>> result = new ArrayList<>();
        String directioncode = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
        String windspeedcode = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
        String[] names = DataFormatUtil.directName;
        String[] codes = DataFormatUtil.directCode;
        //验证是否包含风向风速
        Boolean ishave = isHaveWeatherData(dgimn, startDate, endDate);
        List<String> daylist = DataFormatUtil.getYMDBetween(starttime, endtime);
        daylist.add(endtime);
        if (ishave == true) {
            List<AggregationOperation> aggregations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate).and("HourDataList.PollutantCode").is(pollutantcode).and("HourDataList.IsOverStandard").is(true);
            aggregations.add(match(criteria));
            aggregations.add(project("DataGatherCode", "MonitorTime", "HourDataList"));
            aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
            Aggregation aggregation = newAggregation(aggregations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
            List<Document> resultDocument = results.getMappedResults();
            for (String ymd : daylist) {
                Map<String, Object> resultmap = new HashMap<>();
                List<Map<String, Object>> weatherlist = new ArrayList<>();
                List<Map<String, Object>> weathernumlist = new ArrayList<>();
                if (resultDocument.size() > 0) {
                    for (Document document : resultDocument) {
                        String theymd = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                        if (ymd.equals(theymd)) {
                            String ymdh = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                            List<Document> pollutants = (List<Document>) document.get("HourDataList");
                            if (pollutants.size() > 0) {
                                boolean flag = false;
                                String value = "";
                                String windspeed = "";
                                String winddirection = "";
                                for (Document podocument : pollutants) {
                                    if (pollutantcode.equals(podocument.getString("PollutantCode")) && podocument.getBoolean("IsOverStandard") == true) {
                                        flag = true;
                                        value = podocument.getString("AvgStrength");
                                    }
                                    if (directioncode.equals(podocument.getString("PollutantCode"))) {
                                        winddirection = podocument.getString("AvgStrength");
                                    }
                                    if (windspeedcode.equals(podocument.getString("PollutantCode"))) {
                                        windspeed = podocument.getString("AvgStrength");
                                    }
                                }
                                if (flag == true && !"".equals(value)) {
                                    Map<String, Object> valuemap2 = new HashMap<>();
                                    valuemap2.put("monitortime", ymdh);
                                    valuemap2.put("value", value);
                                    if (!"".equals(winddirection)) {
                                        valuemap2.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "code"));
                                        valuemap2.put("winddirectionname", DataFormatUtil.windDirectionSwitch(Double.valueOf(winddirection), "name"));
                                        valuemap2.put("winddirectionvalue", winddirection);
                                        valuemap2.put("windspeed", !"".equals(windspeed) ? windspeed : null);
                                        weatherlist.add(valuemap2);
                                    } /*else {
                                        valuemap2.put("winddirectioncode", null);
                                        valuemap2.put("winddirectionvalue", null);
                                        valuemap2.put("winddirectionname", null);
                                        valuemap2.put("windspeed", !"".equals(windspeed) ? windspeed : null);
                                    }*/
                                }
                            }
                        }
                    }
                    for (Map<String, Object> map : pollutantValueScopeByParamMap) {
                        if (map.get("StandardMinValue") != null && map.get("StandardMaxValue") != null) {
                            String StandardMinValue = map.get("StandardMinValue").toString();
                            String StandardMaxValue = map.get("StandardMaxValue").toString();
                            String ColourValue = map.get("ColourValue").toString();
                            weatherlist.stream().filter(m -> m.get("value") != null && Double.valueOf(StandardMinValue) <= Double.valueOf(m.get("value").toString()) && Double.valueOf(StandardMaxValue) >= Double.valueOf(m.get("value").toString())).forEach(m -> {
                                m.put("valuescope", StandardMinValue + "-" + StandardMaxValue);
                                m.put("colourvalue", ColourValue);
                            });
                        }
                    }
                    Map<String, List<Map<String, Object>>> collect = weatherlist.stream().filter(m -> m.get("valuescope") != null).collect(Collectors.groupingBy(m -> m.get("valuescope").toString()));
                    for (String valuescope : collect.keySet()) {
                        List<Map<String, Object>> list = collect.get(valuescope);
                        Map<String, List<Map<String, Object>>> collect2 = list.stream().filter(m -> m.get("winddirectionname") != null).collect(Collectors.groupingBy(m -> m.get("winddirectionname").toString()));
                        List<Map<String, Object>> winddatalist = new ArrayList<>();
                        Map<String, Object> onemap = new HashMap<>();
                        for (String name : collect2.keySet()) {
                            String code = "";
                            Map<String, Object> map = new HashMap<>();
                            for (int i = 0; i < names.length; i++) {
                                if (name.equals(names[i])) {
                                    code = codes[i];
                                    break;
                                }
                            }
                            map.put("winddirectioncode", code);
                            map.put("winddirectionname", name);
                            map.put("num", collect2.get(name).size());
                            winddatalist.add(map);
                        }
                        onemap.put("valuescope", valuescope);
                        onemap.put("colourvalue", list.stream().filter(m -> m.get("colourvalue") != null).findFirst().orElse(new HashMap<>()).get("colourvalue"));
                        onemap.put("winddatalist", winddatalist);
                        weathernumlist.add(onemap);
                    }
                }
                resultmap.put("monitortime", ymd);
                resultmap.put("weathernumdata", weathernumlist);
                if (weathernumlist.size() > 0) {
                    result.add(resultmap);
                }
            }
        }
        return result;

    }


    private Boolean isHaveWeatherData(String dgimn, Date startDate, Date endDate) {
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode(), CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        aggregations.add(match(criteria));
        criteria1.and("HourDataList.PollutantCode").in(pollutantcodes);
        aggregations.add(unwind("HourDataList"));
        aggregations.add(match(criteria1));
        aggregations.add(project("DataGatherCode", "MonitorTime")
                .and("HourDataList.PollutantCode").as("PollutantCode"));
        aggregations.add(group("PollutantCode"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        if (resultDocument != null && resultDocument.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 根据监测点id 监测类型 (厂界、通道、敏感点) 监测时间获取监测点污染物日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getOneStenchDayMonitorDataByParamsForApp(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        String dgimn = paramMap.get("dgimn").toString();
        String monitortime = paramMap.get("monitortime").toString();
        if (!"".equals(dgimn)) {
            List<Map<String, Object>> pollutantlist = otherMonitorPointMapper.getStenchPollutantMonitorPointids(paramMap);
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> codeandunit = new HashMap<>();
            if (pollutantlist != null && pollutantlist.size() > 0) {
                for (Map<String, Object> map : pollutantlist) {
                    codeandname.put(map.get("code").toString(), map.get("name"));
                    codeandunit.put(map.get("code").toString(), map.get("pollutantunit"));
                }
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
            List<AggregationOperation> aggregations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
            aggregations.add(match(criteria));
            aggregations.add(project("DataGatherCode", "MonitorTime", "DayDataList"));
            Aggregation aggregation = newAggregation(aggregations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "DayData", Document.class);
            List<Document> resultDocument = results.getMappedResults();
            if (resultDocument.size() > 0) {
                Document document = resultDocument.get(0);
                resultmap.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                List<Document> pollutantList = document.get("DayDataList", List.class);
                List<Map<String, Object>> pollutantdata = new ArrayList<>();
                for (Document pollutant : pollutantList) {
                    Map<String, Object> map = new HashMap<>();
                    String code = pollutant.getString("PollutantCode");
                    String value = pollutant.getString("AvgStrength");
                    map.put("code", code);
                    map.put("value", value);
                    map.put("name", codeandname.get(code));
                    map.put("unit", codeandunit.get(code));
                    if (codeandname.get(code) != null) {
                        pollutantdata.add(map);
                    }
                }
                resultmap.put("pollutantdata", pollutantdata);
            }
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 返回所有恶臭点位其中最近一条监测点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getStenchLastOneMonitorTimeByParamsForApp(List<String> mns, String datamark) {
        Map<String, Object> resultmap = new HashMap<>();
        String connection = "";
        if ("hour".equals(datamark)) {
            connection = "HourData";
        } else if ("day".equals(datamark)) {
            connection = "DayData";
        } else if ("month".equals(datamark)) {
            connection = "MonthData";
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        Document document = mongoTemplate.findOne(query, Document.class, connection);
        if (document != null) {//判断查询数据是否为空
            if ("hour".equals(datamark)) {
                resultmap.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
            } else if ("day".equals(datamark)) {
                resultmap.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
            } else if ("month".equals(datamark)) {
                resultmap.put("monitortime", DataFormatUtil.getDateYM(document.getDate("MonitorTime")));
            }
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 根据时间标记、监测时间 监测类型 (厂界、通道、敏感点) 获取恶臭监测点该时间的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStenchMonitorListDataByParamsForApp(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        String monitortime = paramMap.get("monitortime").toString();
        String datamark = paramMap.get("datamark").toString();
        String stinkflag = paramMap.get("stinkflag") != null ? paramMap.get("stinkflag").toString() : "";
        Map<String, Object> mnandent = (Map<String, Object>) paramMap.get("mnandent");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        Date startDate = null;
        Date endDate = null;
        Map<String, Object> codeandname = new HashMap<>();
        Map<String, Object> codeandunit = new HashMap<>();
        List<Map<String, Object>> pollutantlist = new ArrayList<>();
        String dataliststr = "";
        String collection = "";
        if (mns.size() > 0) {
            //污染物
            if ("3".equals(stinkflag)) {
                pollutantlist = pollutantFactorMapper.getKeyPollutantsByMonitorPointType(FactoryBoundaryStinkEnum.getCode());
            } else {
                pollutantlist = pollutantFactorMapper.getKeyPollutantsByMonitorPointType(EnvironmentalStinkEnum.getCode());
            }
            if (pollutantlist != null && pollutantlist.size() > 0) {
                for (Map<String, Object> map : pollutantlist) {
                    codeandname.put(map.get("Code").toString(), map.get("Name"));
                    codeandunit.put(map.get("Code").toString(), map.get("PollutantUnit"));
                }
            }
            //时间
            if ("hour".equals(datamark)) {
                startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
                endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
                dataliststr = "HourDataList";
                collection = "HourData";
            } else if ("day".equals(datamark)) {
                startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
                dataliststr = "DayDataList";
                collection = "DayData";
            } else if ("month".equals(datamark)) {
                startDate = DataFormatUtil.getDateYMDHMS(monitortime + "-01 00:00:00");
                endDate = DataFormatUtil.getDateYMDHMS(monitortime + "-31 23:59:59");
                dataliststr = "MonthDataList";
                collection = "MonthData";
            }
            List<AggregationOperation> aggregations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
            aggregations.add(match(criteria));
            aggregations.add(project("DataGatherCode", "MonitorTime", dataliststr));
            Aggregation aggregation = newAggregation(aggregations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> resultDocument = results.getMappedResults();
            if (resultDocument.size() > 0) {
                for (String mn : mns) {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("pollutionname", mnandent.get(mn) != null ? mnandent.get(mn) : "");
                    resultmap.put("monitorpointname", mnandname.get(mn) != null ? mnandname.get(mn) : "");
                    resultmap.put("monitortime", monitortime);
                    List<Document> pollutantList = new ArrayList<>();
                    for (Document document : resultDocument) {
                        if (mn.equals(document.getString("DataGatherCode"))) {
                            pollutantList = document.get(dataliststr, List.class);
                        }
                    }
                    if (pollutantList != null && pollutantList.size() > 0) {
                        List<Map<String, Object>> pollutantdata = new ArrayList<>();
                        for (Document pollutant : pollutantList) {
                            Map<String, Object> map = new HashMap<>();
                            String code = pollutant.getString("PollutantCode");
                            String value = pollutant.getString("AvgStrength");
                            map.put("code", code);
                            map.put("value", value);
                            map.put("name", codeandname.get(code));
                            map.put("unit", codeandunit.get(code));
                            if (codeandname.get(code) != null) {
                                pollutantdata.add(map);
                            }
                        }
                        resultmap.put("pollutantdata", pollutantdata);
                    } else {
                        resultmap.put("pollutantdata", new ArrayList<>());
                    }
                    result.add(resultmap);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/12/02 0002 下午 3:20
     * @Description: 根据自定义参数获取异常报警各异常类型数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStinkExceptionAlarmDetailDataByParam(Map<String, Object> paramMap) {
        try {
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Date startTime = (Date) paramMap.get("starttime");
            Date endTime = (Date) paramMap.get("endtime");
            Set mns = (Set) paramMap.get("mns");
            Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
            Map<String, Object> codeandunit = (Map<String, Object>) paramMap.get("codeandunit");
            Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
            Map<String, Object> mnandid = (Map<String, Object>) paramMap.get("mnandid");
            Criteria criteria = new Criteria();
            List<AggregationOperation> operations = new ArrayList<>();
            criteria.and("DataGatherCode").in(mns).and("ExceptionTime").gte(startTime).lte(endTime);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "ExceptionTime", "PollutantCode", "DataType", "MonitorValue"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "ExceptionTime"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "ExceptionData", Document.class);
            List<Document> documents = pageResults.getMappedResults();
            if (documents.size() > 0) {
                for (Document document : documents) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("dgimn", document.getString("DataGatherCode"));
                    map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")));
                    map.put("pollutantcode", document.getString("PollutantCode"));
                    map.put("pollutantname", codeandname.get(document.getString("PollutantCode")));
                    map.put("pollutantunit", codeandunit.get(document.getString("PollutantCode")) != null ? codeandunit.get(document.getString("PollutantCode")) : "");
                    map.put("monitorpointname", mnandname.get(document.getString("DataGatherCode")));
                    map.put("monitorvalue", document.getString("MonitorValue"));
                    map.put("monitorpointid", mnandid.get(document.getString("DataGatherCode")));
                    map.put("datetype", document.getString("DataType"));
                    map.put("alarmtype", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode());
                    map.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getName());
                    resultlist.add(map);
                }
            }
            return resultlist;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getVocPollutantFactorGroupData(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getVocPollutantFactorGroupData(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/11/18 0018 上午 8:51
     * @Description: 根据因子组获取该因子组下所有因子信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllVocPollutantByParam(Map<String, Object> param) {
        return otherMonitorPointMapper.getAllVocPollutantByParam(param);
    }


    /**
     * @author: xsm
     * @date: 2019/12/11 0011 上午 8:47
     * @Description: 根据自定义参数获取超阈值或超限报警的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStinkEarlyOrOverAlarmDetailDataByParam(Map<String, Object> paramMap) {
        try {
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Date startTime = (Date) paramMap.get("starttime");
            Date endTime = (Date) paramMap.get("endtime");
            Integer remindtype = (Integer) paramMap.get("remindtype");
            Set mns = (Set) paramMap.get("mns");
            Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
            Map<String, Object> codeandunit = (Map<String, Object>) paramMap.get("codeandunit");
            Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
            Map<String, Object> mnandid = (Map<String, Object>) paramMap.get("mnandid");
            String timekey = "";
            String collection = "";
            if (remindtype == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {//超阈值
                timekey = "EarlyWarnTime";
                collection = "EarlyWarnData";
            } else if (remindtype == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {//超限
                timekey = "OverTime";
                collection = "OverData";
            }
            Criteria criteria = new Criteria();
            List<AggregationOperation> operations = new ArrayList<>();
            criteria.and("DataGatherCode").in(mns).and(timekey).gte(startTime).lte(endTime);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project(timekey, "DataGatherCode", "PollutantCode", "DataType", "MonitorValue"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, timekey));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> documents = pageResults.getMappedResults();
            if (documents.size() > 0) {
                for (Document document : documents) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("dgimn", document.getString("DataGatherCode"));
                    map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate(timekey)));
                    map.put("pollutantcode", document.getString("PollutantCode"));
                    map.put("pollutantname", codeandname.get(document.getString("PollutantCode")));
                    map.put("pollutantunit", codeandunit.get(document.getString("PollutantCode")) != null ? codeandunit.get(document.getString("PollutantCode")) : "");
                    map.put("monitorpointname", mnandname.get(document.getString("DataGatherCode")));
                    map.put("monitorvalue", document.getString("MonitorValue"));
                    map.put("monitorpointid", mnandid.get(document.getString("DataGatherCode")));
                    map.put("datetype", document.getString("DataType"));
                    map.put("alarmtype", remindtype);
                    if (remindtype == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {//超阈值
                        map.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getName());
                    } else if (remindtype == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {//超限
                        map.put("alarmtypename", "超限报警");
                    }
                    resultlist.add(map);
                }
            }
            return resultlist;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 上午 9:09
     * @Description: 根据自定义参数获取恶臭浓度突变报警的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStinkConcentrationDetailDataByParam(Map<String, Object> paramMap) {
        try {
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Date startTime = (Date) paramMap.get("starttime");
            Date endTime = (Date) paramMap.get("endtime");
            Set mns = (Set) paramMap.get("mns");
            Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
            Map<String, Object> codeandunit = (Map<String, Object>) paramMap.get("codeandunit");
            Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
            Map<String, Object> mnandid = (Map<String, Object>) paramMap.get("mnandid");
            Criteria criteria = new Criteria();
            Criteria criteria1 = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startTime).lte(endTime);
            criteria1.and("HourDataList.IsSuddenChange").is(true);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.unwind("HourDataList"));
            operations.add(Aggregation.match(criteria1));
            operations.add(Aggregation.project("DataGatherCode", "HourDataList.PollutantCode", "MonitorTime", "HourDataList.AvgStrength"));
            //插入分页、排序条件
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "HourData", Document.class);
            List<Document> documents = pageResults.getMappedResults();
            if (documents.size() > 0) {
                for (Document document : documents) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("dgimn", document.getString("DataGatherCode"));
                    map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                    map.put("pollutantcode", document.getString("PollutantCode"));
                    map.put("pollutantname", codeandname.get(document.getString("PollutantCode")));
                    map.put("pollutantunit", codeandunit.get(document.getString("PollutantCode")) != null ? codeandunit.get(document.getString("PollutantCode")) : "");
                    map.put("monitorpointname", mnandname.get(document.getString("DataGatherCode")));
                    map.put("monitorpointid", mnandid.get(document.getString("DataGatherCode")));
                    map.put("monitorvalue", document.getString("AvgStrength"));
                    map.put("datetype", "HourData");
                    map.put("alarmtype", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
                    map.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getName());
                    resultlist.add(map);
                }
            }
            return resultlist;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/17 4:51
     * @Description: 获取VOC某因子组某段时间各因子小时浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVocPollutantConcentrationDataByParam(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> codeandname = (Map<String, Object>) param.get("codeandname");
        Map<String, Object> codeandunit = (Map<String, Object>) param.get("codeandunit");
        List<String> codes = (List<String>) param.get("codes");
        String dgimn = param.get("dgimn").toString();
        String starttime = param.get("starttime").toString();
        String endtime = param.get("endtime").toString();
        List<String> hours = DataFormatUtil.getYMDHBetween(starttime, endtime);
        hours.add(endtime);
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("time", "$MonitorTime");
        pollutantList.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("HourDataList"));
        operations.add(match(Criteria.where("HourDataList.PollutantCode").in(codes).and("HourDataList.AvgStrength").ne("0.00")));
        operations.add(Aggregation.project("MonitorTime").and("HourDataList.AvgStrength").as("AvgStrength")
                .and("HourDataList.PollutantCode").as("PollutantCode"));
        operations.add(group("PollutantCode").push(pollutantList).as("pollutantList"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "HourData", Document.class);
        List<Document> documents = pageResults.getMappedResults();
        for (String code : codes) {
            Map<String, Object> objmap = new HashMap<>();
            objmap.put("code", code);
            objmap.put("name", codeandname.get(code));
            objmap.put("unit", codeandunit.get(code));
            objmap.put("timelist", hours);
            List<String> values = new ArrayList<>();
            boolean flag = false;
            for (String ymdh : hours) {
                String value = "";
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        if (code.equals(document.getString("_id"))) {
                            List<Document> pollutantdata = (List<Document>) document.get("pollutantList");
                            if (pollutantdata != null && pollutantdata.size() > 0) {
                                for (Document podocument : pollutantdata) {
                                    if (ymdh.equals(DataFormatUtil.getDateYMDH(podocument.getDate("time")))) {
                                        value = podocument.getString("value");
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else {
                    break;
                }
                values.add(value);
            }
            if (flag == true) {
                objmap.put("valuelist", values);
            } else {
                objmap.put("valuelist", new ArrayList<>());
            }
            result.add(objmap);
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/11/18 10:31
     * @Description: 根据自定义参数统计VOC因子组占比数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> countVocFactorGroupProportionDataByParam(Map<String, Object> param) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> codeandcategory = (Map<String, Object>) param.get("codeandcategory");
        String datemark = param.get("datemark").toString();
        String dgimn = param.get("dgimn").toString();
        String starttime = param.get("starttime").toString();
        String endtime = param.get("endtime").toString();
        List<String> codes = (List<String>) param.get("codes");
        String secondstarttime = param.get("secondstarttime").toString();
        String secondendtime = param.get("secondendtime").toString();
        Map<String, Object> firstmap = getVocFactorGroupProportionData(dgimn, codeandcategory, datemark, starttime, endtime, codes);
        Map<String, Object> secondmap = getVocFactorGroupProportionData(dgimn, codeandcategory, datemark, secondstarttime, secondendtime, codes);
        result.put("firstdata", firstmap);
        result.put("seconddate", secondmap);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/11/18 10:31
     * @Description: 根统计Voc因子组占比数据
     */
    private Map<String, Object> getVocFactorGroupProportionData(String dgimn, Map<String, Object> codeandcategory, String datemark, String starttime, String endtime, List<String> codes) {
        Map<String, Object> resultmap = new HashMap<>();
        List<Document> documents = getVocHourOrDayOnlineData(dgimn, starttime, endtime, codes, datemark);
        Map<String, Double> codemap = new HashMap<>();
        Map<String, Integer> codeandnum = new HashMap<>();
        Map<Integer, Double> categorymap = new HashMap<>();
        if (documents.size() > 0) {
            for (Document document : documents) {
                String pocode = document.getString("_id");
                List<Document> pollutantdata = (List<Document>) document.get("pollutantList");
                double totalvalue = 0d;
                int i = 0;
                for (Document obj : pollutantdata) {
                    if (obj.get("value") != null && !"".equals(obj.getString("value")))
                        totalvalue += Double.parseDouble(obj.getString("value"));
                    i += 1;
                }
                codemap.put(pocode, totalvalue);
                codeandnum.put(pocode, i);
            }
        }
        double totalnum = 0d;
        double avgtotalnum = 0d;
        if (codemap.size() > 0) {
            for (Map.Entry<String, Double> entry : codemap.entrySet()) {
                double value = entry.getValue();
                if (codeandcategory.get(entry.getKey()) != null) {
                    int key = Integer.parseInt(codeandcategory.get(entry.getKey()).toString());
                    if (categorymap.get(key) != null) {
                        categorymap.put(key, value + categorymap.get(key));
                    } else {
                        categorymap.put(key, value);
                    }
                    totalnum += value;
                }
                if (codeandnum.get(entry.getKey()) != null) {
                    int num = codeandnum.get(entry.getKey());
                    String avgvalue = DataFormatUtil.SaveTwoAndSubZero((value) / num);
                    avgtotalnum += Double.parseDouble(avgvalue);
                }
            }
        }
        resultmap.put("Vocs", "-");
        if (avgtotalnum > 0) {
            resultmap.put("Vocs", DataFormatUtil.subZeroAndDot(DataFormatUtil.SaveTwoAndSubZero((avgtotalnum) / codemap.size())));
        }
        if (categorymap.size() > 0) {
            for (Map.Entry<Integer, Double> entry : categorymap.entrySet()) {
                double onevalue = entry.getValue();
                String proportion = "-";
                if (totalnum > 0) {
                    proportion = DataFormatUtil.subZeroAndDot(DataFormatUtil.SaveTwoAndSubZero(100 * (onevalue) / totalnum)) + "%";
                }
                resultmap.put(entry.getKey().toString(), proportion);
            }
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/11/18 10:31
     * @Description: 据自定义参数统计VOC因子OFP排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countVocPollutantOFPRankDataByParam(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        String dgimn = param.get("dgimn").toString();
        String starttime = param.get("starttime").toString();
        String endtime = param.get("endtime").toString();
        List<String> codes = (List<String>) param.get("codes");
        String secondstarttime = param.get("secondstarttime").toString();
        String secondendtime = param.get("secondendtime").toString();
        Map<String, Object> codeandname = (Map<String, Object>) param.get("codeandname");
        List<Document> onedocuments = getVocHourOrDayOnlineData(dgimn, starttime, endtime, codes, "hour");
        List<Document> twodocuments = getVocHourOrDayOnlineData(dgimn, secondstarttime, secondendtime, codes, "hour");
        for (String code : codes) {
            Map<String, Object> resultmap = new HashMap<>();
            double one = countVocPollutantAvgValue(code, onedocuments);
            double two = countVocPollutantAvgValue(code, twodocuments);
            resultmap.put("code", code);
            resultmap.put("name", codeandname.get(code));
            resultmap.put("firstvalue", one);
            resultmap.put("secondvalue", two);
            result.add(resultmap);
        }
        //排序 按监测值 降序
        List<Map<String, Object>> collect = result.stream().sorted(Comparator.comparingDouble((Map m) -> Double.parseDouble(m.get("firstvalue").toString())).reversed().thenComparing(Comparator.comparingDouble((Map m) -> Double.parseDouble(m.get("secondvalue").toString())).reversed())).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<Map<String, Object>> getStinkPointDataByParamMap(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getStinkPointDataByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/11/18 10:31
     * @Description: 根统计Voc因子平均值
     */
    private double countVocPollutantAvgValue(String code, List<Document> documents) {
        DecimalFormat df = new DecimalFormat("#.00");
        double value = 0d;
        int num = 0;
        double total = 0d;
        if (documents.size() > 0) {
            for (Document one : documents) {
                if (code.equals(one.getString("_id"))) {
                    List<Document> onepollutant = (List<Document>) one.get("pollutantList");
                    for (Document obj : onepollutant) {
                        if (!"".equals(obj.getString("value")) && Double.parseDouble(obj.getString("value")) > 0) {
                            num += 1;
                            total += Double.parseDouble(obj.getString("value"));
                        }
                    }
                }
            }
        }
        if (num > 0) {
            value = Double.parseDouble(df.format(total / num));
        }
        return value;
    }

    /**
     * @author: xsm
     * @date: 2020/11/18 10:31
     * @Description: 获取单个点位小时或日监测数据
     */
    private List<Document> getVocHourOrDayOnlineData(String dgimn, String starttime, String endtime, List<String> codes, String datemark) {
        Date startDate = null;
        Date endDate = null;
        String collection = "";
        String dataliststr = "";
        if ("hour".equals(datemark)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            collection = "HourData";
            dataliststr = "HourDataList";
        } else if ("day".equals(datemark)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            collection = "DayData";
            dataliststr = "DayDataList";
        }
        startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
        endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(dataliststr));
        operations.add(match(Criteria.where(dataliststr + ".PollutantCode").in(codes).and(dataliststr + ".AvgStrength").ne("0.00")));
        operations.add(Aggregation.project().and(dataliststr + ".AvgStrength").as("AvgStrength")
                .and(dataliststr + ".PollutantCode").as("PollutantCode"));
        operations.add(group("PollutantCode").push(pollutantList).as("pollutantList"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        return documents;
    }

    /**
     * @author: xsm
     * @date: 2020/11/26 16:53
     * @Description: 获取某VOC点位单个或多个污染物的小时浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getVocPollutantHourConcentrationDataByParam(Map<String, Object> param) {
        String dgimn = param.get("dgimn").toString();
        String starttime = param.get("starttime").toString();
        String endtime = param.get("endtime").toString();
        List<String> codes = (List<String>) param.get("codes");
        param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
        List<Map<String, Object>> mapList = pollutantFactorMapper.getPollutantsByCodesAndType(param);
        Map<String, String> codeAndName = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            codeAndName.put(map.get("code").toString(), map.get("name").toString());
        }
        List<String> hours = DataFormatUtil.getYMDHBetween(starttime + " 00", endtime + " 23");
        hours.add(endtime + " 23");
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("time", "$MonitorTime");
        pollutantList.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("HourDataList"));
        operations.add(match(Criteria.where("HourDataList.PollutantCode").in(codes)));
        operations.add(Aggregation.project("MonitorTime").and("HourDataList.AvgStrength").as("AvgStrength")
                .and("HourDataList.PollutantCode").as("PollutantCode"));
        operations.add(group("PollutantCode").push(pollutantList).as("pollutantList"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "HourData", Document.class);
        List<Document> documents = pageResults.getMappedResults();
        Map<String, Object> resultmap = new HashMap<>();
        List<Map<String, Object>> valuelist = new ArrayList<>();
        resultmap.put("timelist", hours);
        for (String code : codes) {
            Map<String, Object> objmap = new HashMap<>();
            objmap.put("pollutantcode", code);
            objmap.put("pollutantname", codeAndName.get(code));
            List<String> values = new ArrayList<>();
            List<Document> pollutantlist = new ArrayList<>();
            if (documents.size() > 0) {
                for (Document document : documents) {
                    if (code.equals(document.getString("_id"))) {
                        pollutantlist = (List<Document>) document.get("pollutantList");
                    }
                }
            }
            for (String strtime : hours) {
                String value = "";
                if (pollutantlist != null && pollutantlist.size() > 0) {
                    for (Document documentobj : pollutantlist) {
                        if (documentobj.getDate("time") != null && strtime.equals(DataFormatUtil.getDateYMDH(documentobj.getDate("time")))) {
                            value = documentobj.get("value") != null ? documentobj.getString("value") : "";
                        }
                    }
                }
                values.add(value);
            }
            objmap.put("valuelist", values);
            valuelist.add(objmap);
        }
        resultmap.put("valuedata", valuelist);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/11/27 10:28
     * @Description: 根据自定义参数统计VOC因子组各小时浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> countVocPointFactorGroupHourDataByParam(Map<String, Object> param) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resultlist = new ArrayList<>();
        Map<String, Object> codeandcategory = (Map<String, Object>) param.get("codeandcategory");
        String dgimn = param.get("dgimn").toString();
        String starttime = param.get("starttime").toString();
        String endtime = param.get("endtime").toString();
        String datamark = param.get("datamark").toString();
        Set<String> factorgroup = (Set<String>) param.get("factorgroup");//该点位 关联的因子所属于的因子组
        List<String> codes = (List<String>) param.get("codes");
        List<String> hours = DataFormatUtil.getYMDHBetween(starttime, endtime);//时间组
        hours.add(endtime);
        List<String> hourlist = new ArrayList<>();//昼夜分析时  用来存储时间值  0时，1时...
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("code", "$PollutantCode");
        pollutantList.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("HourDataList"));
        operations.add(match(Criteria.where("HourDataList.PollutantCode").in(codes)));
        operations.add(Aggregation.project("MonitorTime").and("HourDataList.AvgStrength").as("AvgStrength")
                .and("HourDataList.PollutantCode").as("PollutantCode"));
        operations.add(group("MonitorTime").push(pollutantList).as("pollutantList"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "HourData", Document.class);
        List<Document> documents = pageResults.getMappedResults();
        Map<String, List<String>> resultmap = new HashMap<>();
        if (factorgroup.size() > 0) {
            for (String i : factorgroup) {
                resultmap.put(i, new ArrayList<>());
            }
        }
        for (String strtime : hours) {
            hourlist.add(Integer.valueOf(strtime.substring(11, strtime.length())) + "时");
            Map<String, Double> categorymap = new HashMap<>();
            if (documents.size() > 0) {
                for (Document document : documents) {
                    String monitortime = DataFormatUtil.getDateYMDH(document.getDate("_id"));
                    if (strtime.equals(monitortime)) {
                        List<Document> pollutantdata = (List<Document>) document.get("pollutantList");
                        for (Document obj : pollutantdata) {
                            String pocode = obj.getString("code");
                            double totalvalue = 0d;
                            if (obj.get("value") != null && !"".equals(obj.getString("value"))) {
                                totalvalue += Double.parseDouble(obj.getString("value"));
                            }
                            if (codeandcategory.get(pocode) != null) {
                                String key = codeandcategory.get(pocode).toString();
                                if (categorymap.get(key) != null) {
                                    categorymap.put(key, totalvalue + categorymap.get(key));
                                } else {
                                    categorymap.put(key, totalvalue);
                                }
                            }
                        }
                    }
                }
            }
            for (String key : resultmap.keySet()) {
                if (categorymap.get(key) != null) {
                    resultmap.get(key).add(!"".equals(categorymap.get(key).toString()) ? DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(categorymap.get(key).toString()))) : "");
                } else {
                    resultmap.get(key).add("");
                }
            }
        }
        if ("1".equals(datamark)) {
            result.put("timelist", hours);
        } else {
            result.put("timelist", hourlist);
        }

        if (resultmap != null && resultmap.size() > 0) {
            for (String key : resultmap.keySet()) {
                Map<String, Object> objmap = new HashMap<>();
                objmap.put("factorgroupcode", key);
                objmap.put("factorgroupname", CommonTypeEnum.VocPollutantFactorGroupEnum.getNameByCode(Integer.valueOf(key)));
                objmap.put("valuelist", resultmap.get(key));
                resultlist.add(objmap);
            }
        }
        result.put("valuedata", resultlist);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/11/30 8:37
     * @Description: 根据自定义参数统计VOC各时间组的总浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> counVocPollutantSumDataByParam(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> pollutants = (List<Map<String, Object>>) param.get("pollutants");
        Map<String, Object> mnandname = (Map<String, Object>) param.get("mnandname");
        List<String> vocmns = (List<String>) param.get("vocmns");
        String monitortime = param.get("monitortime").toString();
        String datemark = param.get("datemark").toString();
        List<String> hours = (List<String>) param.get("timelist");
        ;//时间组
        Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
        String collection = "";
        String dataliststr = "";
        Map<String, Set<String>> mnandcodelist = new HashMap<>();
        Set<String> allcodes = new HashSet<>();
        if (pollutants != null && pollutants.size() > 0) {
            for (Map<String, Object> map : pollutants) {
                if (map.get("pollutantcode") != null) {
                    String vocmn = map.get("Dgimn").toString();
                    allcodes.add(map.get("pollutantcode").toString());
                    if (mnandcodelist.get(vocmn) != null) {
                        mnandcodelist.get(vocmn).add(map.get("pollutantcode").toString());
                    } else {
                        Set<String> pocodes = new HashSet<>();
                        pocodes.add(map.get("pollutantcode").toString());
                        mnandcodelist.put(vocmn, pocodes);
                    }
                }
            }
        }
        if ("minute".equals(datemark)) {
            collection = "MinuteData";
            dataliststr = "MinuteDataList";
        } else if ("hour".equals(datemark)) {
            collection = "HourData";
            dataliststr = "HourDataList";
        }
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("time", "$MonitorTime");
        pollutantList.put("code", "$PollutantCode");
        pollutantList.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(vocmns).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(dataliststr));
        operations.add(match(Criteria.where(dataliststr + ".PollutantCode").in(allcodes)));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and(dataliststr + ".AvgStrength").as("AvgStrength")
                .and(dataliststr + ".PollutantCode").as("PollutantCode"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        operations.add(group("DataGatherCode").push(pollutantList).as("pollutantList"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        /*Map<String, List<Document>> mapDocuments = new HashMap<>();
        if (mappedResults.size()>0){
            mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        }*/
        for (String mn : vocmns) {
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("dgimn", mn);
            resultmap.put("monitorpointname", mnandname.get(mn));
            List<String> values = new ArrayList<>();
            List<Document> podatalist = new ArrayList<>();
            Map<String, List<Document>> mapDocuments = new HashMap<>();
            if (mappedResults.size() > 0) {
                for (Document document : mappedResults) {
                    if (mn.equals(document.getString("_id"))) {
                        podatalist = (List<Document>) document.get("pollutantList");
                        if ("minute".equals(datemark)) {
                            mapDocuments = podatalist.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDHM(m.getDate("time"))));
                        } else if ("hour".equals(datemark)) {
                            mapDocuments = podatalist.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDH(m.getDate("time"))));
                        }

                    }
                }
            }
            Set<String> themnpocodes = mnandcodelist.get(mn);
            for (String strtime : hours) {
                String totalvalue = "";
                if (themnpocodes != null) {
                    if (mapDocuments.get(strtime) != null) {
                        List<Document> onetimedata = mapDocuments.get(strtime);
                        if (onetimedata.size() > 0) {
                            for (Document document1 : onetimedata) {
                                if (themnpocodes.contains(document1.getString("code"))) {
                                    if (!"".equals(totalvalue)) {
                                        totalvalue = (Float.valueOf(totalvalue) + ((document1.get("value") != null && !"".equals(document1.get("value").toString())) ? Float.valueOf(document1.get("value").toString()) : 0d)) + "";
                                    } else {
                                        totalvalue = ((document1.get("value") != null && !"".equals(document1.get("value").toString())) ? Float.valueOf(document1.get("value").toString()) : 0d) + "";
                                    }
                                }
                            }
                        }
                    }
                }
                if (!"".equals(totalvalue)) {
                    values.add(DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(totalvalue))));
                } else {
                    values.add(totalvalue);
                }
            }
            resultmap.put("valuelist", values);
            result.add(resultmap);
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/11/30 8:37
     * @Description: 根据自定义参数获取各点位污染物VOC浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getStinkAndGasOutPutPollutantDataByParam(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        String voccode = DataFormatUtil.parseProperties("pollutant.voccode");
        Map<String, Object> mnandname = (Map<String, Object>) param.get("mnandname");
        List<String> mns = (List<String>) param.get("mns");
        String monitortime = param.get("monitortime").toString();
        String datemark = param.get("datemark").toString();
        List<String> hours = (List<String>) param.get("timelist");
        ;//时间组
        Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
        String collection = "";
        String dataliststr = "";
        if ("minute".equals(datemark)) {
            collection = "MinuteData";
            dataliststr = "MinuteDataList";
        } else if ("hour".equals(datemark)) {
            collection = "HourData";
            dataliststr = "HourDataList";
        }
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("time", "$MonitorTime");
        pollutantList.put("code", "$PollutantCode");
        pollutantList.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(dataliststr));
        operations.add(match(Criteria.where("HourDataList.PollutantCode").is(voccode)));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and(dataliststr + ".AvgStrength").as("AvgStrength")
                .and(dataliststr + ".PollutantCode").as("PollutantCode"));
        operations.add(group("DataGatherCode").push(pollutantList).as("pollutantList"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        for (String mn : mns) {
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("dgimn", mn);
            resultmap.put("monitorpointname", mnandname.get(mn));
            List<String> values = new ArrayList<>();
            List<Document> podatalist = new ArrayList<>();
            if (documents.size() > 0) {
                for (Document document : documents) {
                    if (mn.equals(document.getString("_id"))) {
                        podatalist = (List<Document>) document.get("pollutantList");
                    }
                }
            }
            for (String strtime : hours) {
                String thevalue = "";
                if (podatalist.size() > 0) {
                    for (Document document1 : podatalist) {
                        String thetime = "";
                        if ("minute".equals(datemark)) {
                            thetime = DataFormatUtil.getDateYMDHM(document1.getDate("time"));
                        } else if ("hour".equals(datemark)) {
                            thetime = DataFormatUtil.getDateYMDH(document1.getDate("time"));
                        }
                        if (strtime.equals(thetime)) {
                            thevalue = document1.get("value") != null ? document1.getString("value") : "";
                        }
                    }
                }
                values.add(thevalue);
            }
            resultmap.put("valuelist", values);
            result.add(resultmap);
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/11/30 0030 下午 1:07
     * @Description: 根据监测类型获取点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointDataByTypeForVocAnalysis(Integer monitorpointtype, Map<String, Object> paramMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case WasteGasEnum:
                paramMap.put("monitorpointtype", monitorpointtype);
                dataList = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                break;
            case EnvironmentalStinkEnum:
                paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                dataList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case EnvironmentalVocEnum:
                paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                dataList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                dataList = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                break;
        }
        if (dataList.size() > 0) {
            for (Map<String, Object> map : dataList) {
                Map<String, Object> resultMap = new HashMap<>();
                if (CommonTypeEnum.getOutPutTypeList().contains(monitorpointtype)) {
                    resultMap.put("monitorpointname", map.get("shortername") + "_" + map.get("outputname"));
                    resultMap.put("dgimn", map.get("dgimn"));
                } else {
                    if (map.get("shortername") != null) {
                        resultMap.put("monitorpointname", map.get("shortername") + "_" + map.get("monitorpointname"));
                    } else {
                        resultMap.put("monitorpointname", map.get("monitorpointname"));
                    }
                    resultMap.put("dgimn", map.get("dgimn"));
                }
                resultMap.put("monitorpointid", map.get("pk_id"));
                resultList.add(resultMap);
            }
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getVocPollutantDataByFactorGroups(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getVocPollutantDataByFactorGroups(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTraceSourceMeteoMonitorPointMN(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getTraceSourceMeteoMonitorPointMN(paramMap);
    }

    @Override
    public List<Document> getStinkHourOrDayDataByParam(List<String> dgimns, String monitortime, String dateType) {
        Criteria criteria = new Criteria();
        String liststr = "";
        if ("hour".equals(dateType)) {
            liststr = "HourDataList";
            Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startDate).lte(endDate);//.and("HourDataList.PollutantCode").is(pollutantcode);
        } else if ("day".equals(dateType)) {
            liststr = "DayDataList";
            Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startDate).lte(endDate);//and("DayDataList.PollutantCode").is(pollutantcode);
        }
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime", liststr));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        List<Document> documents = new ArrayList<>();
        if ("hour".equals(dateType)) {
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregationquery, "HourData", Document.class);
            documents = results.getMappedResults();
        } else if ("day".equals(dateType)) {
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregationquery, "DayData", Document.class);
            documents = results.getMappedResults();
        }
        return documents;
    }

    @Override
    public List<Map<String, Object>> getAllStinkPointDataList() {
        return otherMonitorPointMapper.getAllStinkPointDataList();
    }

    @Override
    public List<Map<String, Object>> getAllOnlineOtherPointInfoByParamMap(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getAllOnlineOtherPointInfoByParamMap(paramMap);
    }


    @Override
    public List<Map<String, Object>> getAllMonitorPointAndStatusInfo(Map<String, Object> param) {
        return otherMonitorPointMapper.getAllMonitorPointAndStatusInfo(param);
    }

    @Override
    public List<Map<String, Object>> getOtherPointInfoAndAirMNByParamMap(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getOtherPointInfoAndAirMNByParamMap(paramMap);
    }

    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.countTotalByParam(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/05/23 13:30
     * @Description: 根据自定义参数统计站点VOCs因子时段均值排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countVocPollutantVocsAvgValueRankDataByParam(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        String dgimn = param.get("dgimn").toString();
        String starttime = param.get("starttime").toString();
        String endtime = param.get("endtime").toString();
        List<String> codes = (List<String>) param.get("codes");
        String datatype = param.get("datatype").toString();
        Map<String, Object> codeandname = (Map<String, Object>) param.get("codeandname");
        List<Document> onedocuments = getVocHourOrDayOnlineData(dgimn, starttime, endtime, codes, datatype);
        for (String code : codes) {
            Map<String, Object> resultmap = new HashMap<>();
            double one = countVocPollutantAvgValue(code, onedocuments);
            resultmap.put("code", code);
            resultmap.put("name", codeandname.get(code));
            resultmap.put("avgvalue", one);
            result.add(resultmap);
        }
        //排序 按监测值 降序
        List<Map<String, Object>> collect = result.stream().sorted(Comparator.comparingDouble((Map m) -> Double.parseDouble(m.get("avgvalue").toString())).reversed()).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<Map<String, Object>> countVocPollutantLohRankDataByParam(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        String dgimn = param.get("dgimn").toString();
        String starttime = param.get("starttime").toString();
        String endtime = param.get("endtime").toString();
        List<String> codes = (List<String>) param.get("codes");
        String datatype = param.get("datatype").toString();
        Map<String, Object> codeandname = (Map<String, Object>) param.get("codeandname");
        Map<String, Object> codeandkohi = (Map<String, Object>) param.get("codeandkohi");
        List<Document> onedocuments = getVocHourOrDayOnlineData(dgimn, starttime, endtime, codes, datatype);
        for (String code : codes) {
            Map<String, Object> resultmap = new HashMap<>();
            double one = countVocPollutantTotalValue(code, onedocuments);
            if (codeandkohi.get(code) != null) {
                resultmap.put("code", code);
                resultmap.put("name", codeandname.get(code));
                resultmap.put("value", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(one * Double.valueOf(codeandkohi.get(code).toString()))));
                result.add(resultmap);
            }
        }
        //排序 按监测值 降序
        List<Map<String, Object>> collect = result.stream().sorted(Comparator.comparingDouble((Map m) -> Double.parseDouble(m.get("value").toString())).reversed()).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<Map<String, Object>> getVocRelationDgimnByParam(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getVocRelationDgimnByParam(paramMap);
    }


    /**
     * @Description: 其他监测点列表数据查询
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/1 9:21
     */
    @Override
    public Map<String, Object> getDataListMapByParam(JSONObject jsonObject) {

        Map<String, Object> resultMap = new HashMap<>();
        //点位数据

        if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
            PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()),
                    Integer.valueOf(jsonObject.get("pagesize").toString()));
        }
        List<Map<String, Object>> datalist = otherMonitorPointMapper.getDataListMapByParam(jsonObject);

        PageInfo<Map<String, Object>> pageInfo = new PageInfo(datalist);

        resultMap.put("total", pageInfo.getTotal());
        List<String> ids = datalist.stream().map(m -> m.get("pk_monitorpointid").toString()).collect(Collectors.toList());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pkidlist", ids);
        List<Map<String, Object>> pollutants = otherMonitorPointMapper.getOtherMonitorPointAllPollutantsByIDAndType(paramMap);
        //因子数据
        if (pollutants.size() > 0) {
            Map<String, List<Map<String, Object>>> idAndPollutant = pollutants.stream().collect(Collectors.groupingBy(m -> m.get("PK_MonitorPointID").toString()));
            List<Map<String, Object>> tempList;
            List<String> nameList;
            for (Map<String, Object> dataMap : datalist) {
                tempList = idAndPollutant.get(dataMap.get("pk_monitorpointid"));
                if (tempList != null) {
                    nameList = tempList.stream().map(m -> m.get("name").toString()).collect(Collectors.toList());
                    dataMap.put("pollutants", DataFormatUtil.FormatListToString(nameList, "、"));
                } else {
                    dataMap.put("pollutants", "");
                }
            }
        }
        resultMap.put("datalist", datalist);
        return resultMap;
    }

    @Override
    public void updateInfo(OtherMonitorPointVO otherMonitorPointVO) {
        //更新mn号
        updateDeviceStatus(otherMonitorPointVO);
        OtherMonitorPointVO oldObj = otherMonitorPointMapper.selectByPrimaryKey(otherMonitorPointVO.getPkMonitorpointid());
        String mn = oldObj.getDgimn();
        //更新数据权限表
        if (otherMonitorPointVO.getDgimn()!=null&&!otherMonitorPointVO.getDgimn().equals(mn)){
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("newmn",otherMonitorPointVO.getDgimn());
            paramMap.put("oldmn",mn);
            paramMap.put("monitorpointtype",otherMonitorPointVO.getFkMonitorpointtypecode());
            userMonitorPointRelationDataMapper.updataUserMonitorPointRelationDataByMnAndType(paramMap);
        }
        otherMonitorPointMapper.updateByPrimaryKey(otherMonitorPointVO);

    }

    private void updateDeviceStatus(OtherMonitorPointVO otherMonitorPointVO) {

        OtherMonitorPointVO oldObj = otherMonitorPointMapper.selectByPrimaryKey(otherMonitorPointVO.getPkMonitorpointid());

        String mn = oldObj.getDgimn();
        DeviceStatusVO deviceStatusVO = deviceStatusMapper.selectByMnKey(mn);
        if (StringUtils.isNotBlank(mn) && !mn.equals(otherMonitorPointVO.getDgimn())&&deviceStatusVO!=null) {
            deviceStatusVO.setDgimn(otherMonitorPointVO.getDgimn());
            deviceStatusVO.setFkMonitorpointtypecode(otherMonitorPointVO.getFkMonitorpointtypecode());
            deviceStatusVO.setUpdateuser(otherMonitorPointVO.getUpdateuser());
            deviceStatusVO.setUpdatetime(otherMonitorPointVO.getUpdatetime());
            deviceStatusMapper.updateByPrimaryKeySelective(deviceStatusVO);
        } else if (StringUtils.isNotBlank(mn)&& !mn.equals(otherMonitorPointVO.getDgimn())) {
            //添加
            addDeviceStatus(otherMonitorPointVO);
        }
    }

    private void addDeviceStatus(OtherMonitorPointVO otherMonitorPointVO) {
        DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
        deviceStatusVO.setPkId(UUID.randomUUID().toString());
        deviceStatusVO.setStatus((short) 0);
        deviceStatusVO.setDgimn(otherMonitorPointVO.getDgimn());
        deviceStatusVO.setFkMonitorpointtypecode(otherMonitorPointVO.getFkMonitorpointtypecode());
        deviceStatusVO.setUpdateuser(otherMonitorPointVO.getUpdateuser());
        deviceStatusVO.setUpdatetime(otherMonitorPointVO.getUpdatetime());
        deviceStatusMapper.insert(deviceStatusVO);
    }

    @Override
    public void insertInfo(OtherMonitorPointVO otherMonitorPointVO) {
        otherMonitorPointMapper.insert(otherMonitorPointVO);
        //更新mn
        addDeviceStatus(otherMonitorPointVO);
        //添加首要污染物
        List<Map<String, Object>> pollutants = keyMonitorPollutantMapper.selectByPollutanttype(otherMonitorPointVO.getFkMonitorpointtypecode());
        List<OtherMonitorPointPollutantSetVO> otherlist = new ArrayList<>();
        for (Map<String, Object> pollutant : pollutants) {
            OtherMonitorPointPollutantSetVO otherobj = new OtherMonitorPointPollutantSetVO();
            otherobj.setUpdatetime(new Date());
            otherobj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
            otherobj.setPkDataid(UUID.randomUUID().toString());
            otherobj.setFkOthermonintpointid(otherMonitorPointVO.getPkMonitorpointid());
            otherobj.setFkPollutantcode(pollutant.get("FK_PollutantCode").toString());
            otherlist.add(otherobj);
        }
        if (otherlist != null && otherlist.size() > 0) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutants", otherlist);
            otherMonitorPointPollutantSetMapper.batchInsert(paramMap);
        }
    }

    @Override
    public void deleteById(String id) {
        String mn = otherMonitorPointMapper.selectByPrimaryKey(id).getDgimn();
        otherMonitorPointMapper.deleteByPrimaryKey(id);
        //删除set表
        otherMonitorPointPollutantSetMapper.deleteByFid(id);
        //删除状态表
        deviceStatusMapper.deleteDeviceStatusByMN(mn);
    }

    @Override
    public Map<String, Object> getEditOrViewDataById(String id) {
        return otherMonitorPointMapper.getEditOrViewDataById(id);
    }

    /**
     * @author: xsm
     * @date: 2022/05/23 14:25
     * @Description: 统计Voc因子时间段内累计浓度
     */
    private double countVocPollutantTotalValue(String code, List<Document> documents) {
        double total = 0d;
        if (documents.size() > 0) {
            for (Document one : documents) {
                if (code.equals(one.getString("_id"))) {
                    List<Document> onepollutant = (List<Document>) one.get("pollutantList");
                    for (Document obj : onepollutant) {
                        if (!"".equals(obj.getString("value")) && Double.parseDouble(obj.getString("value")) > 0) {
                            total += Double.parseDouble(obj.getString("value"));
                        }
                    }
                    break;
                }
            }
        }
        return total;
    }
}

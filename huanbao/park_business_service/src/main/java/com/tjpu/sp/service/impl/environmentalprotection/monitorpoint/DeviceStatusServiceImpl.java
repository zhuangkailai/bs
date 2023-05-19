package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.SessionUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.DeviceStatusMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointMapper;
import com.tjpu.sp.dao.environmentalprotection.online.OnlineMapper;
import com.tjpu.sp.dao.environmentalprotection.video.VideoCameraMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.meteoEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.OnlineStatusEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class DeviceStatusServiceImpl implements DeviceStatusService {

    @Autowired
    private DeviceStatusMapper deviceStatusMapper;
    @Autowired
    private VideoCameraMapper videoCameraMapper;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    private final String hourCollection = "StationHourAQIData";
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
    @Autowired
    private  OnlineMapper onlineMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;

    /**
     * @author: chengzq
     * @date: 2019/6/12 0012 下午 1:26
     * @Description: 通过mn号查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [Dgimn]
     * @throws:
     */
    @Override
    public List<DeviceStatusVO> selectByDgimn(String Dgimn) {
        return deviceStatusMapper.selectByDgimn(Dgimn);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/12 0012 下午 1:26
     * @Description: 新增记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int insert(DeviceStatusVO record) {
        return deviceStatusMapper.insert(record);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/12 0012 下午 1:26
     * @Description: 通过id修改
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int updateByPrimaryKey(DeviceStatusVO record) {
        return deviceStatusMapper.updateByPrimaryKey(record);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/12 0012 下午 2:18
     * @Description: 通过id删除
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkId) {
        return deviceStatusMapper.deleteByPrimaryKey(pkId);
    }


    @Override
    public List<DeviceStatusVO> getDeviceStatusInfosByDgimn(String Dgimn) {
        return deviceStatusMapper.selectByDgimn(Dgimn);
    }

    /**
     * @author: xsm
     * @date: 2019/11/05 0005 下午 3:11
     * @Description: 通过mn删除信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgmin]
     * @throws:
     */
    @Override
    public void deleteDeviceStatusByMN(String dgimn) {
        deviceStatusMapper.deleteDeviceStatusByMN(dgimn);
    }

    /**
     * @author: xsm
     * @date: 2020/2/17 0017 上午 10:35
     * @Description:根据监测类型统计各类型监测点的点位状态情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countMonitorPointStatusNumByMonitorPointTypes(Map<String, Object> paramMap, Boolean userAuth) {
        List<Map<String, Object>> listdata = new ArrayList<>();
        if (userAuth != null && userAuth) {
            String sessionID = SessionUtil.getSessionID();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataService.getDGIMNByParamMap(paramMap);
            Set<String> authMNs = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());
            paramMap.put("dgimns", authMNs);
        }


        if (paramMap.get("monitorpointtypes") != null) {
            listdata = deviceStatusMapper.countMonitorPointStatusNumByMonitorPointTypes(paramMap);
            List<Integer> typelist = (List<Integer>) paramMap.get("monitorpointtypes");
            boolean flag = false;
            for (Integer type : typelist) {
                if (type == meteoEnum.getCode()) {
                    flag = true;
                }
            }
            if (flag == true) {
                listdata.addAll(deviceStatusMapper.countMeteoMonitorPointStatusNumByMonitorPointTypes(paramMap));
            }
        } else {
            listdata = deviceStatusMapper.countMonitorPointStatusNumByMonitorPointTypes(paramMap);
            listdata.addAll(deviceStatusMapper.countMeteoMonitorPointStatusNumByMonitorPointTypes(paramMap));
        }
        List<Integer> monitorpointtypes = null;
        if (paramMap.get("monitorpointtypes") != null) {
            monitorpointtypes = (List<Integer>) paramMap.get("monitorpointtypes");
            if (monitorpointtypes.size() == 0) {
                monitorpointtypes = new ArrayList<>(CommonTypeEnum.getAllMonitorPointTypeList());
                monitorpointtypes.add(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            }
        } else {
            monitorpointtypes = new ArrayList<>(CommonTypeEnum.getAllMonitorPointTypeList());
            monitorpointtypes.add(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Integer type : monitorpointtypes) {
            Map<String, Object> datamap = new HashMap<>();
            datamap.put("monitorpointtype", type);
            if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气单独处理
                Map<String, Object> statusmap = new HashMap<>();
                statusmap.put("offlinestatus", 0);
                statusmap.put("overstatus", 0);
                statusmap.put("exceptionstatus", 0);
                if (listdata != null && listdata.size() > 0) {
                    String typecode = type.toString();
                    for (Map<String, Object> map : listdata) {
                        if (map.get("FK_MonitorPointTypeCode") != null && typecode.equals(map.get("FK_MonitorPointTypeCode").toString())) {
                            if (map.get("Status") != null && (map.get("Status").toString()).equals(OfflineStatusEnum.getCode())) {
                                statusmap.put("offlinestatus", map.get("num"));
                            } else if (map.get("Status") != null && (map.get("Status").toString()).equals(NormalStatusEnum.getCode())) {
                                Map<String, Object> obj = new HashMap<>();
                                //判断有无在线空气监测点
                                if (map.get("num") != null) {
                                    paramMap.clear();
                                    paramMap.put("status", NormalStatusEnum.getCode());
                                    List<Map<String, Object>> airdata = deviceStatusMapper.getNormalStatusAirMonitorPoints(paramMap);
                                    if (airdata != null && airdata.size() > 0) {
                                        List<String> mns = new ArrayList<>();
                                        for (Map<String, Object> airmap : airdata) {
                                            if (airmap.get("DGIMN") != null) {
                                                mns.add(airmap.get("DGIMN").toString());
                                            }
                                        }
                                        if (mns.size() > 0) {
                                            for (String mn : mns) {
                                                Query query = new Query();
                                                query.addCriteria(Criteria.where("StationCode").is(mn));
                                                query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
                                                Document document = mongoTemplate.findOne(query, Document.class, hourCollection);
                                                if (document != null) {
                                                    String quality = DataFormatUtil.getQualityByAQI(document.getInteger("AQI"));
                                                    if (obj.get(quality) != null) {
                                                        obj.put(quality, Integer.parseInt(obj.get(quality).toString()) + 1);
                                                    } else {
                                                        obj.put(quality, 1);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                statusmap.put("normalstatusdata", obj);
                            } else if (map.get("Status") != null && (map.get("Status").toString()).equals(OverStatusEnum.getCode())) {
                                statusmap.put("overstatus", map.get("num"));
                            } else if (map.get("Status") != null && (map.get("Status").toString()).equals(ExceptionStatusEnum.getCode())) {
                                statusmap.put("exceptionstatus", map.get("num"));
                            }
                        }
                    }
                }
                datamap.put("statusdata", statusmap);
                result.add(datamap);
            } else {
                Map<String, Object> statusmap = new HashMap<>();
                statusmap.put("offlinestatus", 0);
                statusmap.put("normalstatus", 0);
                statusmap.put("overstatus", 0);
                statusmap.put("exceptionstatus", 0);
                if (listdata != null && listdata.size() > 0) {
                    String typecode = type.toString();
                    for (Map<String, Object> map : listdata) {
                        if (map.get("FK_MonitorPointTypeCode") != null && typecode.equals(map.get("FK_MonitorPointTypeCode").toString())) {
                            if (map.get("Status") != null && (map.get("Status").toString()).equals(OfflineStatusEnum.getCode())) {
                                statusmap.put("offlinestatus", map.get("num"));
                            } else if (map.get("Status") != null && (map.get("Status").toString()).equals(NormalStatusEnum.getCode())) {
                                statusmap.put("normalstatus", map.get("num"));
                            } else if (map.get("Status") != null && (map.get("Status").toString()).equals(OverStatusEnum.getCode())) {
                                statusmap.put("overstatus", map.get("num"));
                            } else if (map.get("Status") != null && (map.get("Status").toString()).equals(ExceptionStatusEnum.getCode())) {
                                statusmap.put("exceptionstatus", map.get("num"));
                            }
                        }
                    }
                }
                datamap.put("statusdata", statusmap);
                result.add(datamap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 9:45
     * @Description:统计按点位状态分组的各类型点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countMonitorPointNumGroupByStatusByTypes(Map<String, Object> paramMap) {
        return deviceStatusMapper.countMonitorPointNumGroupByStatusByTypes(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 下午 14:36
     * @Description:根据监测类型和点位状态获取点位信息(包含经纬度)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointInfoByMonitorTypeAndPointStatus(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist = deviceStatusMapper.getMonitorPointInfoByMonitorTypeAndPointStatus(paramMap);
        //获取所有视频信息
        List<Map<String,Object>> videolist =  videoCameraMapper.getVideoInfoByMonitorpointType(new HashMap<>());
        Map<String,List<Map<String,Object>>> pointidAndrtsp = new HashMap<>();
        if (videolist!=null&&videolist.size()>0){
            Set<String> idset = new HashSet<>();
            for (Map<String, Object> map : videolist) {
                if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                    if (!idset.contains(map.get("monitorpointid").toString())) {
                        idset.add(map.get("monitorpointid").toString());
                        List<Map<String, Object>> rtsplist = new ArrayList<>();
                        for (Map<String, Object> map2 : videolist) {
                            if (map2.get("monitorpointid") != null&&(map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))){
                                Map<String, Object> objmap = new HashMap<>();
                                objmap.put("rtsp",map2.get("rtsp"));
                                objmap.put("name",map2.get("name"));
                                objmap.put("id",map2.get("pkid"));
                                rtsplist.add(objmap);
                            }
                        }
                        pointidAndrtsp.put(map.get("monitorpointid").toString(),rtsplist);
                    }
                    //
                }
            }
        }
        for (Map<String, Object> map:datalist){
            map.put("rtsplist",map.get("monitorpointid")!=null?pointidAndrtsp.get(map.get("monitorpointid").toString()):null);
        }
        return datalist;
    }


    /**
     * @author: xsm
     * @date: 2020/3/04 0004 上午 10:43
     * @Description:根据监测类型和点位ID获取点位信息(包含经纬度)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getMonitorPointInfoByDgimn(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist = deviceStatusMapper.getMonitorPointInfoByMonitorTypeAndPointStatus(paramMap);
        if (datalist != null && datalist.size() > 0) {
            //获取所有视频信息
            List<Map<String,Object>> videolist =  videoCameraMapper.getVideoInfoByMonitorpointType(new HashMap<>());
            Map<String,List<Map<String,Object>>> pointidAndrtsp = new HashMap<>();
            if (videolist!=null&&videolist.size()>0){
                Set<String> idset = new HashSet<>();
                for (Map<String, Object> map : videolist) {
                    if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                        if (!idset.contains(map.get("monitorpointid").toString())) {
                            idset.add(map.get("monitorpointid").toString());
                            List<Map<String, Object>> rtsplist = new ArrayList<>();
                            for (Map<String, Object> map2 : videolist) {
                                if (map2.get("monitorpointid") != null&&(map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))){
                                    Map<String, Object> objmap = new HashMap<>();
                                    objmap.put("rtsp",map2.get("rtsp"));
                                    objmap.put("name",map2.get("name"));
                                    objmap.put("id",map2.get("pkid"));
                                    rtsplist.add(objmap);
                                }
                            }
                            pointidAndrtsp.put(map.get("monitorpointid").toString(),rtsplist);
                        }
                        //
                    }
                }
            }
            Map<String, Object> map = datalist.get(0);
            map.put("rtsplist",map.get("monitorpointid")!=null?pointidAndrtsp.get(map.get("monitorpointid").toString()):null);
            return map;
        } else {
            return null;
        }

    }

    /**
     * @author: xsm
     * @date: 2020/3/31 0031 上午 10:39
     * @Description:根据监测类型获取相关监测类型信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getALLMonitorPointTypeInfoByTypes(Map<String, Object> paramMap) {
        return deviceStatusMapper.getALLMonitorPointTypeInfoByTypes(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/4/20 0020 下午 1:22
     * @Description:根据用户数据权限获取在线监控首页的所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorPointTypeDataForOnlineMonitorHomeMap(Map<String, Object> paramMap) {
        return deviceStatusMapper.getAllMonitorPointTypeDataForOnlineMonitorHomeMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/4/20 0020 下午 3:12
     * @Description:根据用户数据权限获取环境监管首页的所有监测类型
     * @updateUser:xsm
     * @updateDate:2022/01/17
     * @updateDescription:只查环保
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorPointTypeDataForEnvSupervisionHomeMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> data = new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> monitortypes = new ArrayList<>();
        List<String> envtypes = new ArrayList<>();
        List<Map<String, Object>> types = deviceStatusMapper.getAllMonitorPointTypeDataForEnvSupervisionHomeMap(paramMap);
        List<String> categorys = new ArrayList<>();
        if (paramMap.get("categorys")!=null){
            categorys = (List<String>) paramMap.get("categorys");
        }else{//没有传改参数 则根据配置文件的 展示环保or安全点位类型
            categorys = Arrays.asList("1");
        }
        if (paramMap.get("ishavecategory")!=null&&"no".equals(paramMap.get("ishavecategory").toString())){
            for (Map<String, Object> map : types) {
                    if (map.get("code") != null) {
                        monitortypes.add(map.get("code").toString());
                        map.put("onlinenum", 0);
                        map.put("totalnum", 0);
                        map.put("alarmflag", 1);
                        result.add(map);
                    }
            }
            Map<String, Object> param = new HashMap<>();
            param.put("monitortypes", monitortypes);
            param.put("userid", paramMap.get("userid"));
            if (paramMap.get("ishavepropertys")!=null){
                param.put("ishavepropertys", paramMap.get("ishavepropertys"));
            }
            List<Map<String, Object>>  datalist = new ArrayList<>();
            datalist = deviceStatusMapper.countAllMonitorTypePointOnlineStatusNum(param);
            if (datalist != null && datalist.size() > 0) {
                for (Map<String, Object> obj : result) {
                    for (Map<String, Object> objmap : datalist) {
                        if (objmap.get("FK_MonitorPointTypeCode") != null && (obj.get("code").toString().equals(objmap.get("FK_MonitorPointTypeCode").toString()))) {
                            obj.put("onlinenum", objmap.get("onlinenum"));
                            obj.put("totalnum", objmap.get("totalnum"));
                            obj.put("monitorpointtypecode", (obj.get("code").toString().equals("yqygoutput"))?"1":obj.get("code"));
                            obj.put("monitorpointtypename", obj.get("name"));
                            obj.put("mainname", obj.get("mainname"));
                            int overnum = objmap.get("overnum") != null ? Integer.parseInt(objmap.get("overnum").toString()) : 0;
                            int exceptionnum = objmap.get("exceptionnum") != null ? Integer.parseInt(objmap.get("exceptionnum").toString()) : 0;
                            if (exceptionnum > 0) {
                                obj.put("alarmflag", 3);
                            } else {
                                if (overnum > 0) {
                                    obj.put("alarmflag", 2);
                                }
                            }
                            data.add(obj);
                        }
                    }
                }
            }
        }else  if (paramMap.get("ishavecategory")!=null&&"stink".equals(paramMap.get("ishavecategory").toString())){
           //合并厂界  环境恶臭
            boolean stinkflag = false;
            for (Map<String, Object> map : types) {
                if (map.get("code") != null) {
                    monitortypes.add(map.get("code").toString());
                    if (Integer.parseInt(map.get("code").toString()) !=CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()&&
                            Integer.parseInt(map.get("code").toString()) !=CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()) {
                        map.put("onlinenum", 0);
                        map.put("totalnum", 0);
                        map.put("alarmflag", 1);
                        result.add(map);
                    }else{
                        stinkflag = true;
                    }
                }
            }
            if (stinkflag == true){
                Map<String, Object> stinkmap = new HashMap<>();
                stinkmap.put("code", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                stinkmap.put("name", "恶臭");
                stinkmap.put("onlinenum", 0);
                stinkmap.put("totalnum", 0);
                stinkmap.put("alarmflag", 1);
                result.add(stinkmap);
            }
            Map<String, Object> param = new HashMap<>();
            param.put("monitortypes", monitortypes);
            param.put("userid", paramMap.get("userid"));
            if (paramMap.get("ishavepropertys")!=null){
                param.put("ishavepropertys", paramMap.get("ishavepropertys"));
            }
            List<Map<String, Object>>  datalist = new ArrayList<>();
            datalist = deviceStatusMapper.countAllMonitorTypePointOnlineStatusNum(param);
            if (datalist != null && datalist.size() > 0) {
                for (Map<String, Object> obj : result) {
                    if (Integer.parseInt(obj.get("code").toString())==CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()){
                        int onlinenum = 0;
                        int totalnum = 0;
                        int overnum = 0;
                        int exceptionnum = 0;
                        for (Map<String, Object> objmap : datalist) {
                            if (objmap.get("FK_MonitorPointTypeCode") != null ) {
                                int fktype = Integer.parseInt(objmap.get("FK_MonitorPointTypeCode").toString());
                                if (fktype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() ||
                                        fktype == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()   ) {
                                    onlinenum += Integer.parseInt(objmap.get("onlinenum").toString());
                                    totalnum += Integer.parseInt(objmap.get("totalnum").toString());
                                    overnum += Integer.parseInt(objmap.get("overnum").toString());
                                    exceptionnum += Integer.parseInt(objmap.get("exceptionnum").toString());
                                }
                            }
                        }
                        obj.put("onlinenum", onlinenum);
                        obj.put("totalnum", totalnum);
                        obj.put("monitorpointtypecode", obj.get("code"));
                        obj.put("monitorpointtypename", obj.get("name"));
                        obj.put("mainname", obj.get("mainname"));
                        if (exceptionnum > 0) {
                            obj.put("alarmflag", 3);
                        } else {
                            if (overnum > 0) {
                                obj.put("alarmflag", 2);
                            }
                        }
                        data.add(obj);
                    }else{
                        for (Map<String, Object> objmap : datalist) {
                            if (objmap.get("FK_MonitorPointTypeCode") != null && (obj.get("code").toString().equals(objmap.get("FK_MonitorPointTypeCode").toString()))) {
                                obj.put("onlinenum", objmap.get("onlinenum"));
                                obj.put("totalnum", objmap.get("totalnum"));
                                obj.put("monitorpointtypecode", obj.get("code"));
                                obj.put("monitorpointtypename", obj.get("name"));
                                int overnum = objmap.get("overnum") != null ? Integer.parseInt(objmap.get("overnum").toString()) : 0;
                                int exceptionnum = objmap.get("exceptionnum") != null ? Integer.parseInt(objmap.get("exceptionnum").toString()) : 0;
                                if (exceptionnum > 0) {
                                    obj.put("alarmflag", 3);
                                } else {
                                    if (overnum > 0) {
                                        obj.put("alarmflag", 2);
                                    }
                                }
                                data.add(obj);
                            }
                        }
                    }

                }
            }
        }else {
            //按传输通道点类型分
            if (types.size() > 0) {//数据权限
                for (Map<String, Object> map : types) {
                    if (map.get("code") != null && (CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode() == Integer.parseInt(map.get("code").toString()))) {
                        envtypes.add(map.get("code").toString());
                    } else if (map.get("code") != null && (CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode() == Integer.parseInt(map.get("code").toString()))) {
                        envtypes.add(map.get("code").toString());
                    } else {
                        if (map.get("code") != null) {
                            monitortypes.add(map.get("code").toString());
                            map.put("onlinenum", 0);
                            map.put("totalnum", 0);
                            map.put("alarmflag", 1);
                            result.add(map);
                        }
                    }
                }
                if (envtypes.size() > 0) {
                    Map<String, Object> cstd_map = new HashMap<>();
                    cstd_map.put("code", "cstdd");
                    cstd_map.put("name", "传输途径");
                    cstd_map.put("onlinenum", 0);
                    cstd_map.put("totalnum", 0);
                    cstd_map.put("alarmflag", 1);
                    result.add(cstd_map);
                    Map<String, Object> mgd_map = new HashMap<>();
                    mgd_map.put("code", "mgd");
                    mgd_map.put("name", "敏感点");
                    mgd_map.put("onlinenum", 0);
                    mgd_map.put("totalnum", 0);
                    mgd_map.put("alarmflag", 1);
                    result.add(mgd_map);

                }
                Map<String, Object> param = new HashMap<>();
                param.put("monitortypes", monitortypes);
                param.put("envtypes", envtypes);
                param.put("userid", paramMap.get("userid"));
                List<Map<String, Object>> datalist = new ArrayList<>();
                datalist = deviceStatusMapper.countAllMonitorPointTypeDataOnlineNum(param);
                if (datalist != null && datalist.size() > 0) {
                    for (Map<String, Object> obj : result) {
                        for (Map<String, Object> objmap : datalist) {
                            if (objmap.get("FK_MonitorPointTypeCode") != null && (obj.get("code").toString().equals(objmap.get("FK_MonitorPointTypeCode").toString()))) {
                                obj.put("onlinenum", objmap.get("onlinenum"));
                                obj.put("totalnum", objmap.get("totalnum"));
                                if ("cstdd".equals(obj.get("code").toString()) || "mgd".equals(obj.get("code").toString())) {
                                    if ("cstdd".equals(obj.get("code").toString())) {
                                        //获取传输通道或敏感点中所有类型
                                        paramMap.put("monitorpointcategory", 2);
                                    } else if ("mgd".equals(obj.get("code").toString())) {
                                        paramMap.put("monitorpointcategory", 1);
                                    }
                                    List<Map<String, Object>> typelist = deviceStatusMapper.getMonitorPointCategoryTypeByParamMap(paramMap);
                                    if (typelist != null && typelist.size() > 0) {
                                        String monitorPointTypeCode = "";
                                        String monitorPointTypeName = "";
                                        for (Map<String, Object> typemap : typelist) {
                                            monitorPointTypeCode = monitorPointTypeCode + typemap.get("Code") + ",";
                                            monitorPointTypeName = monitorPointTypeName + typemap.get("Name") + ",";
                                        }
                                        if (!"".equals(monitorPointTypeCode)) {
                                            monitorPointTypeCode = monitorPointTypeCode.substring(0, monitorPointTypeCode.length() - 1);
                                        }
                                        if (!"".equals(monitorPointTypeName)) {
                                            monitorPointTypeName = monitorPointTypeName.substring(0, monitorPointTypeName.length() - 1);
                                        }
                                        obj.put("monitorpointtypecode", monitorPointTypeCode);
                                        obj.put("monitorpointtypename", monitorPointTypeName);
                                    } else {

                                    }
                                } else {
                                    obj.put("monitorpointtypecode", obj.get("code"));
                                    obj.put("monitorpointtypename", obj.get("name"));
                                }
                                int overnum = objmap.get("overnum") != null ? Integer.parseInt(objmap.get("overnum").toString()) : 0;
                                int exceptionnum = objmap.get("exceptionnum") != null ? Integer.parseInt(objmap.get("exceptionnum").toString()) : 0;
                                if (exceptionnum > 0) {
                                    obj.put("alarmflag", 3);
                                } else {
                                    if (overnum > 0) {
                                        obj.put("alarmflag", 2);
                                    }
                                }
                                data.add(obj);
                            }
                        }
                    }
                }
            }
        }
        return data;
    }


    /**
     * @author: xsm
     * @date: 2020/4/20 0020 下午 3:12
     * @Description:根据用户数据权限获取环境监管首页的所有监测类型点位的状态（在线，离线，超标，异常，停产）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countMonitorPointStatusNumForEnvSupervisionHomeMap(Map<String, Object> paramMap) {
            return deviceStatusMapper.countMonitorPointStatusNumForEnvSupervisionHomeMap(paramMap);
    }


    @Override
    public List<Map<String, Object>> getMonitorPointInfoForEnvSupervisionHomeMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist = deviceStatusMapper.getMonitorPointInfoForEnvSupervisionHomeMap(paramMap);
        //获取敏感点、传输点
        List<Map<String, Object>> otherlist = otherMonitorPointMapper.getStinkAndVocMonitorPointInfos();
        Map<String,Object> mn_category = new HashMap<>();
        if (otherlist!=null&&otherlist.size()>0){
            for (Map<String, Object> other:otherlist){
                if (other.get("DGIMN")!=null&&other.get("MonitorPointCategory")!=null){
                    mn_category.put(other.get("DGIMN").toString(),other.get("MonitorPointCategory"));
                }
            }
        }

        //获取所有视频信息
        List<Map<String,Object>> videolist =  videoCameraMapper.getVideoInfoByMonitorpointType(new HashMap<>());
        Map<String,List<Map<String,Object>>> pointidAndrtsp = new HashMap<>();
        if (videolist!=null&&videolist.size()>0){
            Set<String> idset = new HashSet<>();
            for (Map<String, Object> map : videolist) {
                if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                    if (!idset.contains(map.get("monitorpointid").toString())) {
                        idset.add(map.get("monitorpointid").toString());
                        List<Map<String, Object>> rtsplist = new ArrayList<>();
                        for (Map<String, Object> map2 : videolist) {
                            if (map2.get("monitorpointid") != null&&(map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))){
                                Map<String, Object> objmap = new HashMap<>();
                                objmap.put("rtsp",map2.get("rtsp"));
                                objmap.put("name",map2.get("name"));
                                objmap.put("id",map2.get("pkid"));
                                rtsplist.add(objmap);
                            }
                        }
                        pointidAndrtsp.put(map.get("monitorpointid").toString(),rtsplist);
                    }
                    //
                }
            }
        }
        for (Map<String, Object> map:datalist){
            if (mn_category!=null&&mn_category.get(map.get("DGIMN").toString())!=null){
                map.put("category",mn_category.get(map.get("DGIMN").toString()));
            }
            map.put("rtsplist",map.get("monitorpointid")!=null?pointidAndrtsp.get(map.get("monitorpointid").toString()):null);
        }
        return datalist;
    }


    @Override
    public void updateMonitorDgimn(String befordgimn,String afterdgimn,String monitortype) {
        List<DeviceStatusVO> deviceStatusVOS = deviceStatusMapper.selectByDgimn(befordgimn);
        //之前没有mn号，现在有mn号新增mn表记录
         if(deviceStatusVOS.size()==0 && StringUtils.isNotBlank(afterdgimn)){
             DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
             //获取username
             String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
             deviceStatusVO.setDgimn(afterdgimn);
             deviceStatusVO.setPkId(UUID.randomUUID().toString());
             deviceStatusVO.setFkMonitorpointtypecode(monitortype);
             deviceStatusVO.setUpdatetime(new Date());
             deviceStatusVO.setUpdateuser(username);
             deviceStatusMapper.insert(deviceStatusVO);
         }
         if(deviceStatusVOS.size()>0){
             DeviceStatusVO deviceStatusVO = deviceStatusVOS.get(0);
             //之前有mn号，现在没有删除之前mn表记录
             if(StringUtils.isNotBlank(befordgimn) && StringUtils.isBlank(afterdgimn)){
                 deviceStatusMapper.deleteByPrimaryKey(deviceStatusVO.getPkId());
             }
             //之前没有，现在没有

             //之前有，现在有 修改mn表记录
             if(StringUtils.isNotBlank(befordgimn) && StringUtils.isNotBlank(afterdgimn)){
                 //获取username
                 String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                 deviceStatusVO.setDgimn(afterdgimn);
                 deviceStatusVO.setUpdatetime(new Date());
                 deviceStatusVO.setUpdateuser(username);
                 deviceStatusMapper.updateByPrimaryKey(deviceStatusVO);
             }
         }
    }

    @Override
    public void updateStatusByParam(Map<String, Object> paramMap) {
        deviceStatusMapper.updateStatusByParam(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/11/05 0005 上午 9:45
     * @Description:统计按点位状态分组的各类型点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countSecurityMonitorPointAllStatusNumByTypes(Map<String, Object> paramMap) {
        return deviceStatusMapper.countSecurityMonitorPointAllStatusNumByTypes(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/11/06 0006 上午 9:16
     * @Description:根据监测类型和点位状态获取多个点位信息(包含经纬度,有数据权限)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllSecurityMonitorPointInfoByParamForHomePage(Map<String, Object> paramMap) {
        Map<String, List<Map<String, Object>>> idAndRTSP = new HashMap<>();
        if (paramMap.get("monitorpointtypes")!=null){
            idAndRTSP = getRTSPData((List<Integer>) paramMap.get("monitorpointtypes"));
        }
        List<Map<String, Object>> datalist = deviceStatusMapper.getAllSecurityMonitorPointInfoByParamForHomePage(paramMap);
        if (datalist!=null&&datalist.size()>0){
            for (Map<String, Object> map:datalist){
                if (map.get("parentid")!=null) {
                    map.put("rtsplist", idAndRTSP.get(map.get("parentid").toString()));
                }else{
                    map.put("rtsplist", null);
                }
            }
        }
        return datalist;
    }

    /**
     * @author: lip
     * @date: 2020/1/6 0006 下午 6:29
     * @Description: 获取视频RTSP数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, List<Map<String, Object>>> getRTSPData(List<Integer> monitorpointtypes) {

        Map<String, Object> paramMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> idAndList = new HashMap<>();
        List<Map<String, Object>> rtspList;
        paramMap.put("monitorpointtypes", monitorpointtypes);
        List<Map<String, Object>> RTSPDataList = onlineMapper.getMonitorPointRTSPDataByParam(paramMap);
        if (RTSPDataList.size() > 0) {
            String monitorPointId;
            for (Map<String, Object> RTSPData : RTSPDataList) {
                if (RTSPData.get("rtsp") != null) {
                    monitorPointId = RTSPData.get("monitorpointid").toString();
                    if (idAndList.containsKey(monitorPointId)) {
                        rtspList = idAndList.get(monitorPointId);
                    } else {
                        rtspList = new ArrayList<>();
                    }
                    Map<String, Object> rtspMap = new HashMap<>();
                    rtspMap.put("rtsp", RTSPData.get("rtsp"));
                    rtspMap.put("id", RTSPData.get("id"));
                    rtspMap.put("name", RTSPData.get("name"));
                    rtspList.add(rtspMap);
                    idAndList.put(monitorPointId, rtspList);
                }
            }
        }
        return idAndList;
    }


    @Override
    public List<Map<String, Object>> getDeviceStatusDataByParam(Map<String, Object> paramMap) {
        return deviceStatusMapper.getDeviceStatusDataByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAllHBMonitorPointDataList(Map<String, Object> paramMap) {
        return deviceStatusMapper.getAllHBMonitorPointDataList(paramMap);
    }


    @Override
    public List<Map<String, Object>> countMonitorPointDataSendStatusByParam(List<String> mns, Map<String, Map<String, Object>> mnAndPointData) {
        List<Map<String, Object>> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date nowDay = new Date();
        Date startDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(nowDay)+ " 00:00:00");
        Date endDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(nowDay)+ " 23:59:59");
        Date dayendDate = DataFormatUtil.parseDate(DataFormatUtil.getDateYMD(nowDay)+ " 23:59:59");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(nowDay);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String oldtime = sdf.format(calendar.getTime()) + " 00:00:00";
        Date daystartDate = DataFormatUtil.parseDate(oldtime);
        Criteria criteria = new Criteria();
        Map<String, Object> map2 = new HashMap<>();
        map2.put("time", "$MonitorTime");
        criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
        //实时
        List<Document> realtime_documents = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode","MonitorTime"),
                sort(Sort.Direction.DESC, "DataGatherCode","MonitorTime")
                , group("DataGatherCode").push(map2).as("timelist"))
                , "RealTimeData", Document.class).getMappedResults();
        //分钟
        List<Document> minute_documents = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode","MonitorTime"),
                sort(Sort.Direction.DESC, "DataGatherCode","MonitorTime")
                , group("DataGatherCode").push(map2).as("timelist"))
                , "MinuteData", Document.class).getMappedResults();
        //小时
        List<Document> hour_documents = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode","MonitorTime"),
                sort(Sort.Direction.DESC, "DataGatherCode","MonitorTime")
                , group("DataGatherCode").push(map2).as("timelist"))
                , "HourData", Document.class).getMappedResults();
        //日
        Criteria calendar_two = new Criteria();
        calendar_two.and("DataGatherCode").in(mns).and("MonitorTime").gte(daystartDate).lte(dayendDate);
        List<Document> day_documents = mongoTemplate.aggregate(newAggregation(
                match(calendar_two), project("DataGatherCode","MonitorTime"),
                sort(Sort.Direction.DESC, "DataGatherCode","MonitorTime")
                , group("DataGatherCode").push(map2).as("timelist"))
                , "DayData", Document.class).getMappedResults();
        Map<String, List<Document>> real_mapDocuments = new HashMap<>();
        Map<String, List<Document>> minute_mapDocuments = new HashMap<>();
        Map<String, List<Document>> hour_mapDocuments = new HashMap<>();
        Map<String, List<Document>> day_mapDocuments = new HashMap<>();
        if (realtime_documents!=null&&realtime_documents.size()>0){
            real_mapDocuments = realtime_documents.stream().collect(Collectors.groupingBy(m -> m.get("_id").toString()));
        }
        if (minute_documents!=null&&minute_documents.size()>0){
            minute_mapDocuments = minute_documents.stream().collect(Collectors.groupingBy(m -> m.get("_id").toString()));
        }
        if (hour_documents!=null&&hour_documents.size()>0){
            hour_mapDocuments = hour_documents.stream().collect(Collectors.groupingBy(m -> m.get("_id").toString()));
        }
        if (day_documents!=null&&day_documents.size()>0){
            day_mapDocuments = day_documents.stream().collect(Collectors.groupingBy(m -> m.get("_id").toString()));
        }
        for (String mn :mns){
            Map<String, Object> obj = new HashMap<>();
            if (mnAndPointData.get(mn)!=null){
                Map<String, Object> point = mnAndPointData.get(mn);
                //obj.put("onlinestatus",point.get("onlinestatus"));
                obj.put("monitorpointtypename",point.get("monitorpointtypename"));
                obj.put("monitorpointname",point.get("customname"));
                obj.put("monitorpointtypecode",point.get("monitorpointtype"));
                obj.put("dgimn",mn);
                obj.put("onlinestatusname",point.get("onlinestatusname"));
                getMonitorPointDataSendStatus(obj,mn,real_mapDocuments,1);
                getMonitorPointDataSendStatus(obj,mn,minute_mapDocuments,2);
                getMonitorPointDataSendStatus(obj,mn,hour_mapDocuments,3);
                getMonitorPointDataSendStatus(obj,mn,day_mapDocuments,4);
                result.add(obj);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllHBMonitorPointDataListByParam(Map<String, Object> paramMap) {
        return deviceStatusMapper.getAllHBMonitorPointDataListByParam(paramMap);
    }

    @Override
    public long getHBMonitorPointInfoNumByParamMap(Map<String, Object> paramMap) {
        return deviceStatusMapper.getHBMonitorPointInfoNumByParamMap(paramMap);
    }

    @Override
    public List<String> getOnLinePoints(String code) {
        return deviceStatusMapper.getOnLinePoints(code );
    }



    /**
     * @author: xsm
     * @date: 2020/4/20 0020 下午 3:12
     * @Description:根据用户数据权限获取环境监管首页的所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorTypesForManagementHomeMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> data = new ArrayList<>();
        List<Integer> types =  Arrays.asList(
                CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
        paramMap.put("monitorpointtypes",types);
        //List<Map<String, Object>> types = deviceStatusMapper.getAllMonitorPointTypeDataForEnvSupervisionHomeMap(paramMap);
        List<Map<String, Object>> datalist = deviceStatusMapper.getAllMonitorTypesForManagementHomeMap(paramMap);
        return datalist;
    }

    private void getMonitorPointDataSendStatus(Map<String, Object> obj,String mn, Map<String, List<Document>> mapDocuments, int i) {
        String ishavedata = "0";
        String iscf = "0";
        String str ="";
        if (mapDocuments.get(mn)!=null){
            List<Document> documents = mapDocuments.get(mn);
            if (documents!=null&&documents.size()>0) {
                Document document =documents.get(0);
                ishavedata = "0";
                iscf = "0";
                List<Document> timedocuments = document.get("timelist", new ArrayList<>().getClass());
                    if (timedocuments != null) {
                        ishavedata = "1";
                        if (timedocuments.size() > 1) {//当数据条数大于等于2条时 判断前两条数据时间是否相等（重复）
                            Object onetime = DataFormatUtil.getDateYMDHMS(timedocuments.get(0).getDate("time"));
                            Object twotime = DataFormatUtil.getDateYMDHMS(timedocuments.get(1).getDate("time"));
                            if (onetime != null && twotime != null) {
                                if (onetime.toString().equals(twotime.toString())) {
                                    iscf = "1";
                                }
                            }
                        }
                    }
            }
        }
        if (1 ==i){
            str ="realtime";
        }else if(2 ==i){
            str ="minute";
        }else if(3 ==i){
            str ="hour";
        }else if(4 ==i){
            str ="day";
        }
        if (ishavedata.equals("0")) {
            obj.put("ishasdata_"+str,"否");
        } else if (ishavedata.equals("1")) {
            obj.put("ishasdata_"+str,"是");
        }
        if (iscf.equals("0")) {
            obj.put("iscf_"+str,"否");
        } else if (iscf.equals("1")) {
            obj.put("iscf_"+str,"是");
        }

    }

    /**
     * @author: xsm
     * @date: 2022/01/11 0011 下午 13:21
     * @Description:统计各类型点位在线离线数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countAllPointStatusNumForMonitorType(Map<String, Object> paramMap) {
        return deviceStatusMapper.countAllPointStatusNumForMonitorType(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/02/14 15:40
     * @Description: 获取挂图作战所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllWallChartOperationMonitorTypes(Map<String,Object> param) {
        return deviceStatusMapper.getAllWallChartOperationMonitorTypes(param);
    }

    @Override
    public List<Map<String,Object>> getEnvPointMnDataByParam(Map<String, Object> parammap) {
        return deviceStatusMapper.getEnvPointMnDataByParam(parammap);
    }

    @Override
    public List<Map<String, Object>> getEnvPointInfoDataByParam(Map<String, Object> parammap) {
        return deviceStatusMapper.getEnvPointInfoDataByParam(parammap);
    }

    @Override
    public List<Map<String, Object>> getEnvAirPointInfoDataByParam(Map<String, Object> parammap) {
        return deviceStatusMapper.getEnvAirPointInfoDataByParam(parammap);
    }

    @Override
    public List<Map<String, Object>> getEnvWaterQualityPointInfoDataByParam(Map<String, Object> parammap) {
        return deviceStatusMapper.getEnvWaterQualityPointInfoDataByParam(parammap);
    }

    @Override
    public List<Map<String, Object>> getAllMonitorPointTypeData() {
        return deviceStatusMapper.getAllMonitorPointTypeData();
    }

    @Override
    public List<Map<String, Object>> getMonitorTypeListByParam(Map<String, Object> paramMap) {
        return deviceStatusMapper.getMonitorTypeListByParam(paramMap);
    }

    @Override
    public List<String> getRealTimeAlarmStatusDgimns() {
        List<String> mns = new ArrayList<>();
        List<Map<String, Object>> datalist =  deviceStatusMapper.getRealTimeAlarmStatusDgimns();
        if (datalist!=null&&datalist.size()>0){
            for (Map<String,Object> map:datalist){
                if (map.get("dgimn")!=null) {
                    mns.add(map.get("dgimn").toString());
                }
            }
        }
        return mns;
    }

}

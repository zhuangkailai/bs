package com.tjpu.sp.service.impl.environmentalprotection.video;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.video.VideoCameraMapper;

import com.tjpu.sp.model.environmentalprotection.video.VideoCameraVO;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class VideoCameraServiceImpl implements VideoCameraService {
    @Autowired
    private VideoCameraMapper videoCameraMapper;
    @Autowired
    private WaterOutputInfoMapper waterOutputInfoMapper;
    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private AirMonitorStationMapper airMonitorStationMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;


    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return videoCameraMapper.countTotalByParam(paramMap);
    }

    @Override
    public int saveVideoCamera(VideoCameraVO obj) {
        return videoCameraMapper.insert(obj);
    }

    @Override
    public int updateVideoCamera(VideoCameraVO obj) {
        return videoCameraMapper.updateByPrimaryKey(obj);
    }

    @Override
    public List<Map<String, Object>> isTableDataHaveInfoByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.isTableDataHaveInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/6/21 0021 下午 5:42
     * @Description: 获取所有视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorVideoInfos() {
        return videoCameraMapper.getAllMonitorVideoInfos();
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 下午 5:40
     * @Description: gis-获取所有视频摄像头信息和在线状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllMonitorVideoInfo() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resultlist = new ArrayList<>();
        List<Map<String, Object>> videodata = videoCameraMapper.getAllMonitorVideoInfos();
        Map<String, Object> paramMap = new HashMap<>();
        if (videodata != null && videodata.size() > 0) {
            List<String> waterids = new ArrayList<>();
            List<String> gasids = new ArrayList<>();
            List<String> rainids = new ArrayList<>();
            List<String> airids = new ArrayList<>();
            List<String> stinkids = new ArrayList<>();
            List<String> vocids = new ArrayList<>();
            List<String> entstationids = new ArrayList<>();
            List<String> entstinkids = new ArrayList<>();
            List<String> storagetankareas = new ArrayList<>();
            List<String> storageroomareas = new ArrayList<>();
            for (Map<String, Object> map : videodata) {
                if (map.get("FK_MonitorPointTypeCode") != null) {
                    int type = Integer.parseInt(map.get("FK_MonitorPointTypeCode").toString());
                    if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {
                        waterids.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {
                        gasids.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
                        rainids.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {
                        airids.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
                        vocids.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {
                        stinkids.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {
                        entstationids.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()) {
                        entstinkids.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode()) {//企业贮罐区类型
                        storagetankareas.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else if (type == CommonTypeEnum.MonitorPointTypeEnum.StorageRoomAreaEnum.getCode()) {//企业仓库类型
                        storageroomareas.add(map.get("FK_MonitorPointOrOutPutID").toString());
                    } else {
                        map.put("pollutionname", "");
                        map.put("outputname", "");
                        resultlist.add(map);
                    }
                } else {
                    map.put("pollutionname", "");
                    map.put("outputname", "");
                    resultlist.add(map);
                }
            }
            if (waterids.size() > 0) {
                paramMap.put("outputtype", "water");
                paramMap.put("outputids", waterids);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode(), videodata);
            }
            if (gasids.size() > 0) {
                paramMap.clear();
                paramMap.put("outputids", gasids);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode(), videodata);
            }
            if (rainids.size() > 0) {
                paramMap.clear();
                paramMap.put("outputtype", "rain");
                paramMap.put("outputids", rainids);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode(), videodata);
            }
            if (airids.size() > 0) {
                paramMap.clear();
                paramMap.put("pkidlist", airids);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode(), videodata);

            }
            if (stinkids.size() > 0) {
                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), videodata);

            }
            if (vocids.size() > 0) {
                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                paramMap.put("pkidlist", vocids);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), videodata);

            }
            if (entstationids.size() > 0) {
                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                paramMap.put("pkidlist", entstationids);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), videodata);

            }
            if (entstinkids.size() > 0) {
                paramMap.clear();
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                paramMap.put("pkidlist", entstinkids);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), videodata);
            }
            if (storagetankareas.size() > 0) {
                paramMap.clear();
                paramMap.put("pkidlist", storagetankareas);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), videodata);

            }
            if (storageroomareas.size() > 0) {
                paramMap.clear();
                paramMap.put("pkidlist", storageroomareas);
                assembleDataByParamMap(resultlist, paramMap, CommonTypeEnum.MonitorPointTypeEnum.StorageRoomAreaEnum.getCode(), videodata);
            }
        }
        result.put("total", (videodata != null && videodata.size() > 0) ? videodata.size() : 0);
        result.put("listdata", resultlist);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/11/4  下午 1:16
     * @Description: 组装各类型视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void assembleDataByParamMap(List<Map<String, Object>> resultlist, Map<String, Object> paramMap, int type, List<Map<String, Object>> videodata) {
        List<Map<String, Object>> thelist = new ArrayList<>();
        String pkidkey = "";
        String outputname = "";
        if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {
            pkidkey = "pkid";
            outputname = "OutputName";
            thelist = waterOutputInfoMapper.getAllWaterOrRainOutPutInfoByOutputType(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {
            pkidkey = "PK_ID";
            outputname = "OutputName";
            thelist = gasOutPutInfoMapper.getAllMonitorGasOutPutInfo(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
            pkidkey = "pkid";
            outputname = "OutputName";
            thelist = waterOutputInfoMapper.getAllWaterOrRainOutPutInfoByOutputType(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {
            pkidkey = "PK_AirID";
            outputname = "OutputName";
            thelist = airMonitorStationMapper.getAllAirMonitorStation(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
            pkidkey = "pkid";
            outputname = "OutputName";
            thelist = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {
            pkidkey = "pkid";
            outputname = "OutputName";
            thelist = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {
            pkidkey = "pkid";
            outputname = "OutputName";
            thelist = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {
            pkidkey = "pkid";
            outputname = "OutputName";
            thelist = unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap);
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()) {
            pkidkey = "pkid";
            outputname = "OutputName";
            thelist = unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap);
        }
        if (thelist != null && thelist.size() > 0) {
            for (Map<String, Object> objmap : videodata) {
                if (objmap.get("FK_MonitorPointTypeCode") != null && objmap.get("FK_MonitorPointOrOutPutID") != null) {
                    int thetype = Integer.parseInt(objmap.get("FK_MonitorPointTypeCode").toString());
                    String id = objmap.get("FK_MonitorPointOrOutPutID").toString();
                    if (thetype == type) {//当类型相同时
                        String OutputName = "";
                        String PollutionName = "";
                        for (Map<String, Object> obj : thelist) {
                            if (id.equals(obj.get(pkidkey).toString())) {//当ID相等时
                                OutputName = obj.get(outputname).toString();
                                PollutionName = obj.get("PollutionName") != null ? obj.get("PollutionName").toString() : "";
                            }
                        }
                        objmap.put("pollutionname", PollutionName);
                        objmap.put("outputname", OutputName);
                        resultlist.add(objmap);
                    }
                }
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/29  下午 2:48
     * @Description: 获取所有高空瞭望
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllHighAltitudeVideos() {
        return videoCameraMapper.getAllHighAltitudeVideos();
    }

    /**
     * @author: xsm
     * @date: 2019/11/18  下午 1:06
     * @Description: 根据自定义参数删除点位下的视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void deleteVideoCameraByParamMap(Map<String, Object> parammap) {
        videoCameraMapper.deleteVideoCameraByParamMap(parammap);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        videoCameraMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/11/18  下午 6:25
     * @Description: 根据监测点ID和类型获取视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVideoCameraInfosByMonitorPointIDAndType(Map<String, Object> paramMap) {
        return videoCameraMapper.getVideoCameraInfosByMonitorPointIDAndType(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/24  下午 3:51
     * @Description: 根据企业id和排口id获取视频摄像头信息(app)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVideoCameraInfosByPollutionIdAndOutPutId(Map<String, Object> paramMap) {
        return videoCameraMapper.getVideoCameraInfosByPollutionIdAndOutPutId(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/27   下午 1:54
     * @Description: 根据企业id获取和企业相关的视频信息（安全）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getPollutionVideoTreeByPollutionid(Map<String, Object> paramMap) {
        //获取企业下所有视频信息
        List<Map<String, Object>> pollutionvideo = videoCameraMapper.getPollutionVideoTreeByPollutionid(paramMap);
        if (pollutionvideo.size() > 0) {
            for (int i = 0; i < pollutionvideo.size(); i++) {
                if (i == 0) {
                    pollutionvideo.get(i).put("isalarm", true);
                } else {
                    pollutionvideo.get(i).put("isalarm", false);
                }
            }
        }
        //获取企业下所有风险点信息
        // List<Map<String, Object>> riskinfos = riskInfoMapper.getRiskInfosByPollutionid(paramMap);
        Map<String, Object> resultmap = new HashMap<>();
        resultmap.put("storagetankareas", null);
        resultmap.put("riskinfos", null);
        resultmap.put("videos", pollutionvideo);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/1/7  0007 上午 11:23
     * @Description: 根据监测类型获取该类型所有视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVideoInfoByMonitorpointType(Integer monitorpointtype) {
        Map<String, Object> param = new HashMap<>();
        param.put("monitorpointtype", monitorpointtype);
        return videoCameraMapper.getVideoInfoByMonitorpointType(param);
    }

    /**
     * @author: xsm
     * @date: 2020/1/7  0007 上午 11:23
     * @Description: 根据监测类型获取该类型所有视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVideoInfoByMonitorpointTypes(List<Integer> monitorpointtypes) {
        Map<String, Object> param = new HashMap<>();
        param.put("monitorpointtypes", monitorpointtypes);
        return videoCameraMapper.getVideoInfoByMonitorpointType(param);
    }

    @Override
    public List<String> getWaterHandleEntIds() {
        return waterOutputInfoMapper.getWaterHandleEntIds();
    }

    @Override
    public List<Map<String, Object>> getVideoListByParam(Map<String, Object> paramMap) {
        return videoCameraMapper.getVideoListByParam(paramMap);
    }

    @Override
    public Map<String, Object> info(String id) {
        return videoCameraMapper.selectById(id);
    }

    /**
     * @author: chengzq
     * @date: 2020/1/17 0017 上午 11:52
     * @Description: 通过自定义参数获取视频
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getVideoCameraInfoByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.getVideoInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/1/17  0017 下午 2:55
     * @Description: 根据自定义参数获取相关视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVideoMonitoringAlarmDataByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.getVideoListDataByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/1/19  0019 上午 10:20
     * @Description: 根据自定义参数统计监控报警条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Long countVideoListDataNumByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.countVideoListDataNumByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/1/19  0019 上午 10:44
     * @Description: 根据自定义参数获取监控详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVedioMonitoringAlarmDetailDataByParams(Map<String, Object> paramMap) {
        return null;
    }

    /**
     * @author: xsm
     * @date: 2020/2/12  0012 下午 15:05
     * @Description: 根据自定义参数获取视频摄像头列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVideoCameraInfosByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.getVideoCameraInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/02/13  上午 8:29
     * @Description: 根据自定义参数获取某一个摄像头历史视频列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getVideoHistoricalRecordInfosByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.getVideoHistoricalRecordInfosByParamMap(paramMap);
    }

    @Override
    public void deleteVideoHistoricalRecordInfoByID(String pkid) {
        videoCameraMapper.deleteVideoHistoricalRecordInfoByID(pkid);
    }


    /**
     * @author: chengzq
     * @date: 2020/3/1 0001 下午 10:09
     * @Description: 通过自定义参数获取视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getvideocamerinfo(Map<String, Object> paramMap) {
        return videoCameraMapper.getvideocamerinfo(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/05/08  下午 1:46
     * @Description: 获取所有在大屏展示的摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getIsShowVideoCameraInfos() {
        return videoCameraMapper.getIsShowVideoCameraInfos();
    }

    /**
     * @author: xsm
     * @date: 2020/06/23  下午 2:15
     * @Description: 获取所有视频信息并按监测点ID分组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllVideoInfoGroupByPointID() {
        //获取所有视频信息
        List<Map<String,Object>> videolist =  videoCameraMapper.getVideoInfoByMonitorpointType(new HashMap<>());
        Map<String,Object> pointidAndrtsp = new HashMap<>();
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
                                objmap.put("vediomanufactor",map2.get("VedioManufactor"));
                                rtsplist.add(objmap);
                            }
                        }
                        pointidAndrtsp.put(map.get("monitorpointid").toString(),rtsplist);
                    }
                    //
                }
            }
        }
        return pointidAndrtsp;
    }

    /**
     * @author: xsm
     * @date: 2020/08/20  下午 3:51
     * @Description: 根据企业id获取企业下安全视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getSecurityVideoCameraInfosByPollutionId(Map<String, Object> paramMap) {
        return videoCameraMapper.getSecurityVideoCameraInfosByPollutionId(paramMap);
    }

    @Override
    public List<Map<String, Object>> getMajorHazardSourceVideoByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.getMajorHazardSourceVideoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutionVideoCameraInfosByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.getPollutionVideoCameraInfosByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getVideoCameraCategoryInfoByParamMap(Map<String, Object> paramMap) {
        return videoCameraMapper.getVideoCameraCategoryInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/04/27 0027 下午 1:50
     * @Description: 统计按企业分组的视频报警数据条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> countVideoAlarmDataNumGroupByPollution(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> datalist = new ArrayList<>();
                //monitoringAlarmRecordMapper.countVideoAlarmDataNumGroupByPollution(paramMap);
        if (datalist!=null&&datalist.size()>0){
        for (Map<String, Object> map:datalist){
            if (map.get("pollutionid")!=null){
                result.put(map.get("pollutionid").toString(),map.get("num"));
            }
        }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2021/04/27 0027 下午 1:50
     * @Description: 统计按日期分组的视频报警数据条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> countVideoAlarmDataNumGroupByDate(Map<String, Object> paramMap) {
        String timetype = paramMap.get("timetype")!=null?paramMap.get("timetype").toString():"";
        List<Map<String, Object>> datalist = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        if ("month".equals(timetype)){
            //datalist =monitoringAlarmRecordMapper.countVideoAlarmDataNumGroupByMonth(paramMap);
        }else{
            //datalist =monitoringAlarmRecordMapper.countVideoAlarmDataNumGroupByDate(paramMap);
        }
        if (datalist!=null&&datalist.size()>0){
            for (Map<String, Object> map:datalist){
                if (map.get("AlarmTime")!=null){
                    result.put(map.get("AlarmTime").toString(),map.get("num"));
                }
            }
        }
        return result;
    }
}

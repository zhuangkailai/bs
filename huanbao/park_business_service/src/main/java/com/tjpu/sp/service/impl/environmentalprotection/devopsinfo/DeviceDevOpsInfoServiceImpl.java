package com.tjpu.sp.service.impl.environmentalprotection.devopsinfo;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.UserMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DeviceDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.EntDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.output.UserMonitorPointRelationDataMapper;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DeviceDevOpsInfoVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DeviceDevOpsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeviceDevOpsInfoServiceImpl implements DeviceDevOpsInfoService {
    @Autowired
    private DeviceDevOpsInfoMapper deviceDevOpsInfoMapper;
    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private WaterOutputInfoMapper waterOutPutInfoMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    @Autowired
    private  GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper;
    @Autowired
    private  WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper;
    @Autowired
    private  AirStationPollutantSetMapper airStationPollutantSetMapper;
    @Autowired
    private  OtherMonitorPointPollutantSetMapper otherMonitorPointPollutantSetMapper;
    @Autowired
    private  WaterStationPollutantSetMapper waterStationPollutantSetMapper;
    @Autowired
    private AirMonitorStationMapper airMonitorStationMapper;
    @Autowired
    private WaterStationMapper waterStationMapper;
    @Autowired
    private UserMonitorPointRelationDataMapper userMonitorPointRelationDataMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EntDevOpsInfoMapper entDevOpsInfoMapper;


    /**
     * @author: xsm
     * @date: 2019/12/04 0004 下午 3:01
     * @Description: 根据自定义参数获取设备运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDeviceDevOpsInfosByParamMap(Map<String, Object> paramMap) {
        return deviceDevOpsInfoMapper.getDeviceDevOpsInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:20
     * @Description: 新增设备运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addDeviceDevOpsInfo(DeviceDevOpsInfoVO entity) {
        deviceDevOpsInfoMapper.insert(entity);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:20
     * @Description: 根据主键ID获取运维设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public DeviceDevOpsInfoVO getDeviceDevOpsInfoByPkid(String id) {
        return deviceDevOpsInfoMapper.selectByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:20
     * @Description: 修改运维设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void editDeviceDevOpsInfo(DeviceDevOpsInfoVO entity) {
        deviceDevOpsInfoMapper.updateByPrimaryKey(entity);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:30
     * @Description: 根据主键ID获取运维设备详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getDeviceDevOpsInfoDetailByID(String id) {
        return deviceDevOpsInfoMapper.getDeviceDevOpsInfoDetailByID(id);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 2:26
     * @Description: 根据污染源ID和监测类型获取排口或监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointDataByPollutionIDAndType(List<String> pollutionids, Integer monitorpointtype) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutionids",pollutionids);
        //获取所有非其它监测类型表的监测点类型
        List<Integer> notothertypes = CommonTypeEnum.getNotOtherPointTypeList();
        if (notothertypes.contains(monitorpointtype)){
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    dataList = waterOutPutInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case WasteGasEnum:
                    paramMap.put("monitorpointtype",monitorpointtype);
                    dataList = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case SmokeEnum:
                    paramMap.put("monitorpointtype",monitorpointtype);
                    dataList = gasOutPutInfoMapper.getPollutionGasOuputsAndStatusByParamMap(paramMap);
                    break;
                case RainEnum:
                    paramMap.put("outputtype", "rain");
                    dataList = waterOutPutInfoMapper.getPollutionWaterOuputsAndStatus(paramMap);
                    break;
                case FactoryBoundaryStinkEnum:
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    dataList = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum:
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                    dataList = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    break;
                case WaterQualityEnum:
                    dataList = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
                    break;
                case AirEnum:
                    dataList = airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
                    break;
            }
        }else{
            paramMap.put("monitorPointType", monitorpointtype);
            dataList = otherMonitorPointMapper.getOnlineOtherPointInfoByParamMap(paramMap);
        }
        if (dataList.size() > 0) {
            for (Map<String, Object> map : dataList) {
                Map<String, Object> resultMap = new HashMap<>();
                if (CommonTypeEnum.getOutPutTypeList().contains(monitorpointtype)) {
                    resultMap.put("shortername", map.get("shortername"));
                    resultMap.put("monitorpointname", map.get("outputname"));
                    resultMap.put("dgimn", map.get("dgimn"));
                }  else{
                    resultMap.put("shortername", "");
                    if (map.get("shortername")!=null){
                        resultMap.put("shortername", map.get("shortername"));
                    }
                    resultMap.put("monitorpointname", map.get("monitorpointname"));
                    resultMap.put("dgimn", map.get("dgimn"));
                }
                resultMap.put("monitorpointid", map.get("pk_id"));
                resultMap.put("monitorpointtype", monitorpointtype);
                resultList.add(resultMap);
            }
        }
        return resultList;
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 2:41
     * @Description: 根据监测点ID和监测类型获取排口或监测点设置污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointPollutantDataByIDAndType(String monitorpointid, Integer monitorpointtype) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("outputid", monitorpointid);
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case WasteWaterEnum:
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("datamark", 1);
                dataList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantSetInfoByParamMap(paramMap);
                break;
            case WasteGasEnum:
                paramMap.put("pollutanttype", monitorpointtype);
                paramMap.put("unorgflag", false);
                dataList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetInfoByParam(paramMap);
                break;
            case SmokeEnum:
                paramMap.put("pollutanttype", monitorpointtype);
                paramMap.put("unorgflag", false);
                dataList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetInfoByParam(paramMap);
                break;
            case RainEnum:
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("datamark", 3);
                dataList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantSetInfoByParamMap(paramMap);
                break;
            case AirEnum:
                dataList = airStationPollutantSetMapper.getAirStationPollutantSetInfoByParam(paramMap);
                break;
            case EnvironmentalStinkEnum://恶臭
            case MicroStationEnum://微站
            case EnvironmentalVocEnum://VOC
            case EnvironmentalDustEnum://
                paramMap.put("pollutanttype",monitorpointtype);
                dataList = otherMonitorPointPollutantSetMapper.getOtherMonitorPollutantSetInfoByMonitorId(paramMap);
                break;
            case FactoryBoundaryStinkEnum:
                paramMap.put("unorgflag", true);
                paramMap.put("pollutanttype", monitorpointtype);
                dataList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetInfoByParam(paramMap);
                break;
            case FactoryBoundarySmallStationEnum:
                paramMap.put("unorgflag", true);
                paramMap.put("pollutanttype", monitorpointtype);
                dataList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetInfoByParam(paramMap);
                break;

            case WaterQualityEnum:
                paramMap.put("monitorpointtype", monitorpointtype);
                dataList =  waterStationPollutantSetMapper.getWaterStationAllPollutantsByIDAndType(paramMap);
                break;
        }
        if (dataList!=null&&dataList.size()>0){
            for (Map<String, Object> map:dataList){
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("pollutantcode",map.get("pollutantcode"));
                resultmap.put("pollutantname",map.get("pollutantname"));
                resultmap.put("monitorstatus",map.get("MonitorStatus"));
                resultmap.put("pollutantUnit",map.get("PollutantUnit"));
                resultList.add(resultmap);
            }
        }
        return resultList;
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 3:21
     * @Description: 根据自定义参数获取运维设备信息总条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Long getAllDeviceDevOpsInfoCountByParams(Map<String, Object> paramMap) {
        return deviceDevOpsInfoMapper.getAllDeviceDevOpsInfoCountByParams(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 3:33
     * @Description: 修改污染物因子监测状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updatePollutantStatusByParam(Map<String, Object> param) {
        deviceDevOpsInfoMapper.updatePollutantStatusByParam(param);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 3:33
     * @Description: 根据自定义参数获取运维设备历史信息总条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Long getAllDeviceDevOpsHistoryInfoCountByParams(Map<String, Object> paramMap) {
        return deviceDevOpsInfoMapper.getAllDeviceDevOpsHistoryInfoCountByParams(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 3:52
     * @Description: 根据自定义参数获取运维设备历史信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDeviceDevOpsHistoryInfosByParamMap(Map<String, Object> paramMap) {
        return deviceDevOpsInfoMapper.getDeviceDevOpsHistoryInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 4:41
     * @Description: 根据自定义参数修改监测点状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updateMonitorPointStatusByParam(Map<String, Object> param) {
        deviceDevOpsInfoMapper.updateMonitorPointStatusByParam(param);
    }

    /**
     * @author: xsm
     * @date: 2020/1/9 0009 下午 16:04
     * @Description: 根据自定义参数获取相关运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getDeviceDevOpsHistoryListDataByParamMap(Map<String, Object> parammap) {
        return deviceDevOpsInfoMapper.getDeviceDevOpsHistoryListDataByParamMap(parammap);
    }

    /**
     *
     * @author: xsm
     * @date: 2020/1/10 0010 下午 13:32
     * @Description: 根据自定义参数获取相关运维信息总条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Long CountDeviceDevOpsHistoryListDataNumByParams(Map<String, Object> parammap) {
        return deviceDevOpsInfoMapper.CountDeviceDevOpsHistoryListDataNumByParams(parammap);
    }


    /**
     * @author: chengzq
     * @date: 2020/3/31 0031 下午 3:01
     * @Description: 通过自定义参数获取最新的排口运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getLastDeviceDevOpsInfoByParamMap(Map<String, Object> parammap) {
        return deviceDevOpsInfoMapper.getLastDeviceDevOpsInfoByParamMap(parammap);
    }

    /**
     * @author: chengzq
     * @date: 2020/4/9 0009 下午 1:55
     * @Description: 通过自定义参数获取设备运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDeviceDevOpsInfoByParamMap(Map<String, Object> parammap) {
        return deviceDevOpsInfoMapper.getDeviceDevOpsInfoByParamMap(parammap);
    }

    @Override
    public List<Map<String, Object>> getEntMonitorPointDataByPollutionID(Map<String, Object> paramMap) {
        return userMonitorPointRelationDataMapper.getAllMonitorPointDataByParamMap(paramMap);

    }

    /**
     * @author: xsm
     * @date: 2022/02/24 0024 下午 2:25
     * @Description: 通过运维记录ID获取运维详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getDeviceDevOpsDetailByID(String id) {
        Map<String, Object> resultmap = deviceDevOpsInfoMapper.getDeviceDevOpsDetailByID(id);
        //获取所有用户信息
        List<Map<String, Object>> listdata = userMapper.getAllUserInfo();
        Map<String,Object> idandname = new HashMap<>();
        if (listdata!=null&&listdata.size()>0){
            for (Map<String,Object> usermap:listdata){
                if (usermap.get("id")!=null&&usermap.get("name")!=null){
                    idandname.put(usermap.get("id").toString(),usermap.get("name"));
                }
            }
        }
        if (resultmap.get("devopspeople")!=null){
            String ids = resultmap.get("devopspeople").toString();
            String[] idstrs = ids.split(",");
            String names ="";
            for (String idstr:idstrs){
                if (idandname.get(idstr)!=null){
                    names = names+idandname.get(idstr)+"、";
                }
            }
            if (!"".equals(names)){
                names = names.substring(0,names.length()-1);
            }
            resultmap.put("devopspeople",names);
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2022/03/01 0001 下午 2:34
     * @Description: 统计某段时间例行运维完成情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countDeviceDevOpsCompletionDataByParam(Map<String, Object> parammap) {
        return deviceDevOpsInfoMapper.countDeviceDevOpsCompletionDataByParam(parammap);
    }


    /**
     * @author: xsm
     * @date: 2022/03/03 0003 上午 9:09
     * @Description: 通过运维记录ID删除运维详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deleteDeviceDevOpsInfoByID(String id) {
        deviceDevOpsInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getDevOpsPointTreeData(Map<String, Object> paramMap) {
        return deviceDevOpsInfoMapper.getDevOpsPointTreeData(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 根据自定义参数获取例行运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getRoutineDevOpsInfosByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist = deviceDevOpsInfoMapper.getRoutineDevOpsInfosByParamMap(paramMap);
        //获取每个运维点 关联的运维人员个数
        List<String> entdevopsids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            entdevopsids = datalist.stream().filter(m -> m.get("fkentdevopsid") != null).map(m -> m.get("fkentdevopsid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  personneldata = new ArrayList<>();
        if (entdevopsids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("entdevopsids",entdevopsids);
            //关联的运维人员信息
            personneldata = entDevOpsInfoMapper.getDevOpsPersonnelDataByParam(param);
            String entdevopsid;
            for (Map<String, Object> map:datalist) {
                if (map.get("fkentdevopsid") != null) {
                    entdevopsid = map.get("fkentdevopsid").toString();
                    String personnelnfo = "";
                    for (Map<String, Object> twomap : personneldata) {
                        if (twomap.get("pkid") != null && entdevopsid.equals(twomap.get("pkid").toString())) {
                            personnelnfo = personnelnfo + twomap.get("PersonnelName") + "、";
                        }
                    }
                    if (!"".equals(personnelnfo)) {
                        personnelnfo = personnelnfo.substring(0, personnelnfo.length() - 1);
                    }
                    map.put("personnelnfo", personnelnfo);
                }else{
                    map.put("personnelnfo", "");
                }
            }
        }
        return datalist;
    }

    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 例行运维详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public Map<String, Object> getRoutineDevOpsInfoDetailByID(String id) {
        Map<String, Object> obj =  deviceDevOpsInfoMapper.getRoutineDevOpsInfoDetailByID(id);
        //获取每个运维点 关联的运维人员个数
        List<String> entdevopsids = new ArrayList<>();
        if (obj!=null){
            entdevopsids.add(obj.get("fkentdevopsid").toString());
        }
        List<Map<String, Object>>  personneldata = new ArrayList<>();
        if (entdevopsids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("entdevopsids",entdevopsids);
            //关联的运维人员信息
            personneldata = entDevOpsInfoMapper.getDevOpsPersonnelDataByParam(param);
            String personnelnfo = "";
            for (Map<String, Object> twomap : personneldata) {
                if (twomap.get("pkid") != null) {
                    personnelnfo = personnelnfo + twomap.get("PersonnelName") + "、";
                }
            }
            if (!"".equals(personnelnfo)) {
                personnelnfo = personnelnfo.substring(0, personnelnfo.length() - 1);
            }
            obj.put("personnelnfo", personnelnfo);
        }
        return obj;
    }

    /**
     * @author: xsm
     * @date: 2022/04/13 0013 上午 8:32
     * @Description: 根据自定义参数获取企业说明列表信息（企业报备）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntExplainInfosByParamMap(Map<String,Object> paramMap) {
        return deviceDevOpsInfoMapper.getEntExplainInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/04/13 0013 上午 8:52
     * @Description: 获取企业说明详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public Map<String, Object> getEntExplainInfoDetailByID(String id) {
        return deviceDevOpsInfoMapper.getEntExplainInfoDetailByID(id);
    }

    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 根据自定义参数获取运维记录统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDevOpsRecordStatisticsDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist  = entDevOpsInfoMapper.getDevOpsRecordStatisticsDataByParamMap(paramMap);
        //获取每个运维点 关联的运维人员个数
        List<String> entdevopsids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            entdevopsids = datalist.stream().filter(m -> m.get("pkid") != null).map(m -> m.get("pkid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  patrolnumdata = new ArrayList<>();
        List<Map<String, Object>>  personneldata = new ArrayList<>();
        if (entdevopsids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("entdevopsids",entdevopsids);
            //关联的运维人员信息
            personneldata = entDevOpsInfoMapper.getDevOpsPersonnelDataByParam(param);
            param.put("starttime",paramMap.get("starttime"));
            param.put("endtime",paramMap.get("endtime"));
            //统计运维巡警次数
            patrolnumdata = entDevOpsInfoMapper.countDevOpsPatrolNumData(param);

            String entdevopsid;
            int shouldpatrol;
            int alreadypatrol;
            for (Map<String, Object> map:datalist){
                entdevopsid = map.get("pkid").toString();
                for (Map<String,Object> twomap:patrolnumdata){
                    if (twomap.get("fkentdevopsid")!=null&&entdevopsid.equals(twomap.get("fkentdevopsid").toString())){
                        shouldpatrol = Integer.valueOf(twomap.get("total").toString());
                        alreadypatrol = Integer.valueOf(twomap.get("completednum").toString());
                        map.put(twomap.get("devopspatroltype")+"_shouldpatrol",shouldpatrol);
                        map.put(twomap.get("devopspatroltype")+"_alreadypatrol",alreadypatrol);
                        if(shouldpatrol>0){
                            map.put(twomap.get("devopspatroltype")+"_proportion", (alreadypatrol * 100 / shouldpatrol) + "%");
                        }else{
                            map.put(twomap.get("devopspatroltype")+"_proportion","-");
                        }
                    }
                }
                String personnelnfo = "";
                for (Map<String, Object> twomap : personneldata) {
                    if (twomap.get("pkid") != null && entdevopsid.equals(twomap.get("pkid").toString())) {
                        personnelnfo = personnelnfo + twomap.get("PersonnelName") + "、";
                    }
                }
                if (!"".equals(personnelnfo)) {
                    personnelnfo = personnelnfo.substring(0, personnelnfo.length() - 1);
                }
                map.put("personnelnfo", personnelnfo);
            }
        }
        return datalist;
    }

    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 获取运维记录表头列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDevOpsRecordStatisticsTableTitleData() {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        stationname.put("headername", "站点名称");
        stationname.put("headercode", "monitorpointname");
        stationname.put("rownum", "2");
        stationname.put("columnnum", "1");
        stationname.put("chlidheader", new ArrayList<>());
        dataList.add(stationname);

        Map<String, Object> devopsuser = new HashMap<>();
        devopsuser.put("headername", "运维人员");
        devopsuser.put("headercode", "personnelnfo");
        devopsuser.put("rownum", "2");
        devopsuser.put("columnnum", "1");
        devopsuser.put("chlidheader", new ArrayList<>());
        dataList.add(devopsuser);
        String titlename = "";
        for (int i=1;i<=6;i++) {
            if (i==1){
                titlename ="周巡检";
            }else if(i==2){
                titlename ="月巡检";
            }else if(i==3){
                titlename ="两月巡检";
            }else if(i==4){
                titlename ="季巡检";
            }else if(i==5){
                titlename ="半年巡检";
            }else if(i==6){
                titlename ="年巡检";
            }
            Map<String, Object> map = new HashMap<>();
            map.put("headername",titlename+"巡检");
            map.put("headercode", ""+i);
            map.put("rownum", "1");
            map.put("columnnum", "3");

            List<Map<String, Object>> chlidheader = new ArrayList<>();
            Map<String, Object> map1 = new HashMap<>();
            map1.put("headername", "应巡检");
            map1.put("headercode", i+ "_shouldpatrol");
            map1.put("rownum", "1");
            map1.put("columnnum", "1");
            map1.put("chlidheader", new ArrayList<>());
            chlidheader.add(map1);

            Map<String, Object> map2 = new HashMap<>();
            map2.put("headername", "已巡检");
            map2.put("headercode", i+"_alreadypatrol");
            map2.put("rownum", "1");
            map2.put("columnnum", "1");
            map2.put("chlidheader", new ArrayList<>());
            chlidheader.add(map2);

            Map<String, Object> map3 = new HashMap<>();
            map3.put("headername", "完成率");
            map3.put("headercode", i + "_proportion");
            map3.put("rownum", "1");
            map3.put("columnnum", "1");
            map3.put("chlidheader", new ArrayList<>());
            chlidheader.add(map3);

            map.put("chlidheader", chlidheader);
            dataList.add(map);
        }
        return dataList;

    }

}

package com.tjpu.sp.service.impl.common.pubcode;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PollutantServiceImpl implements PollutantService {

    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;

    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper;

    @Autowired
    private GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper;

    @Autowired
    private AirMonitorStationMapper airMonitorStationMapper;

    @Autowired
    private WaterStationMapper waterStationMapper;


    /**
     * @author: chengzq
     * @date: 2019/5/20 0020 下午 2:34
     * @Description: 通过污染物code集合和类型查询污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutantsByCodesAndType(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
    }
    /**
     * @author: zhangzc
     * @date: 2019/5/30 9:31
     * @Description: 根据监测点类型获取重点监测污染物信息
     * @param: pollutantType  9 是恶臭 、10 voc
     * @return:
     */
    @Override
    public List<Map<String, Object>> getKeyPollutantsByMonitorPointType(Integer pollutantType) {
        return pollutantFactorMapper.getKeyPollutantsByMonitorPointType(pollutantType);
    }

    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 3:56
     * @Description: 根据污染物类型获取该类型下的所有污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutantsByPollutantType(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
    }



    /**
     * @author: xsm
     * @date: 2019/7/11 0011 下午 4:24
     * @Description: 通过排口ID和污染物编码获取污染物的标准值和预警值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEarlyAndStandardValueByParams(Map<String, Object> paramMap) {

        List<Map<String, Object>> listdata = new ArrayList<>();
        int type = Integer.valueOf(paramMap.get("monitorpointtype").toString());
        if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//废气
            listdata =  pollutantFactorMapper.getGasEarlyAndStandardValueById(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {//废水
            paramMap.put("outputtype", 1);
            listdata = pollutantFactorMapper.getWaterEarlyAndStandardValueById(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//雨水
            paramMap.put("outputtype", 3);
            listdata = pollutantFactorMapper.getWaterEarlyAndStandardValueById(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {//厂界小型站
            listdata = pollutantFactorMapper.getUnorganizedEarlyAndStandardValueByOutPutId(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()) {//厂界恶臭
            listdata = pollutantFactorMapper.getUnorganizedEarlyAndStandardValueByOutPutId(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站
            listdata = pollutantFactorMapper.getAirEarlyAndStandardValueByOutPutId(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {//微站
            listdata = pollutantFactorMapper.getOtherEarlyAndStandardValueByOutPutId(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//恶臭
            listdata = pollutantFactorMapper.getOtherEarlyAndStandardValueByOutPutId(paramMap);
        } else if (type==CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {//voc
            listdata = pollutantFactorMapper.getOtherEarlyAndStandardValueByOutPutId(paramMap);
        }else if (type==CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()){//水质
            listdata =  pollutantFactorMapper.getWaterStationEarlyAndStandardValueById(paramMap);
        }else if (type==CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//烟气
            listdata = pollutantFactorMapper.getGasEarlyAndStandardValueById(paramMap);
        }else if (type==CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode()||
                type==CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode()||
                type==CommonTypeEnum.MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode()||
                type==CommonTypeEnum.MonitorPointTypeEnum.SecurityToxicMonitor.getCode()||
                type==CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode()){//安全
            listdata =  pollutantFactorMapper.getSecurityPollutantStandardDataByParam(paramMap);

        }

        return listdata;
    }

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 4:39
     * @Description:通过自定义参数获取污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutantinfoByParamMap(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getPollutantinfoByParamMap(paramMap);
    }
    /**
     *
     * @author: lip
     * @date: 2020/5/7 0007 上午 9:56
     * @Description: 自定义参数获取污染物预警值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEarlyValueByParams(Map<String, Object> paramMapTemp) {
        return pollutantFactorMapper.getEarlyValueByParams(paramMapTemp);
    }


    /**
     *
     * @author: xsm
     * @date: 2020/6/15 0015 上午 10:15
     * @Description: 自定义参数排序污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void orderPollutantDataByParamMap(List<Map<String, Object>> tablelistdata,String key,Integer type) {
        Map<String, Object> param = new HashMap<>();
        param.put("pollutanttype",type);
        List<Map<String, Object>> pollutantlist = pollutantFactorMapper.getPollutantsByPollutantType(param);
        if (tablelistdata !=null&&tablelistdata.size()>0){
            for (Map<String, Object> map:tablelistdata){
                orderPollutantData(map,pollutantlist,key);
            }
        }
    }

    private void orderPollutantData(Map<String, Object> map, List<Map<String, Object>> pollutantlist, String key) {
        String pollutants = map.get(key)!=null?map.get(key).toString():"";
        if (!"".equals(pollutants)){
            String pollutantstrs = "";
            String[] strs = pollutants.split("、");
            if (pollutantlist!=null&&pollutantlist.size()>0) {
                for (Map<String, Object> pollutantmap : pollutantlist) {
                    for (String name:strs){
                      if (pollutantmap.get("name")!=null&&name.equals(pollutantmap.get("name").toString())){
                          pollutantstrs = pollutantstrs+ name+"、";
                      }
                    }
                }
            }
            if (!"".equals(pollutantstrs)){
                pollutantstrs = pollutantstrs.substring(0, pollutantstrs.length() - 1);
                map.put(key,pollutantstrs);
            }
        }
    }

    /**
     *
     * @author: xsm
     * @date: 2020/6/16 0016 上午 8:29
     * @Description: 根据MN号获取该MN监测类型的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutantsByDgimn(String mn) {
        return pollutantFactorMapper.getPollutantsByDgimn(mn);
    }

    /**
     *
     * @author: lip
     * @date: 2020/6/28 0028 上午 9:17
     * @Description:  获取全部监测点类型集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorPointTypeList() {
        return pollutantFactorMapper.getAllMonitorPointTypeList();
    }

    /**
     * @author: xsm
     * @date: 2020/09/07 0007 下午 6:18
     * @Description: 根据监测类型和监测点MN号获取该点位监测污染物的标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutantStandardValueDataByParam(Map<String, Object> param) {
        return pollutantFactorMapper.getPollutantStandardValueDataByParam(param);
    }

    @Override
    public Map<String, Map<String, Map<String, Object>>> getMnAndCodeAndStandardData(Integer monitorpointtype, List<String> outputids) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("outputids",outputids);

        List<Map<String, Object>> standardDataList;
        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData = new HashMap<>();

        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case StinkEnum: //恶臭监测点（环境恶臭+厂界恶臭）
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
            case WasteWaterEnum: //废水
                standardDataList = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
            case WasteGasEnum: //废气
            case SmokeEnum: //烟气
                standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
            case RainEnum: //雨水
                standardDataList = waterOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
            case AirEnum: //空气
                standardDataList = airMonitorStationMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
            case EnvironmentalVocEnum: //voc
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);

                break;
            case WaterQualityEnum: //水质
                standardDataList = waterStationMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);

                break;
            case EnvironmentalStinkEnum: //恶臭
            case MicroStationEnum://微站
            case EnvironmentalDustEnum://扬尘
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
            case meteoEnum: //气象
                standardDataList = otherMonitorPointMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
            case FactoryBoundaryStinkEnum:
                standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
            case FactoryBoundarySmallStationEnum:
                standardDataList = gasOutPutPollutantSetMapper.getPollutantStandardDataListByParam(paramMap);
                mnAndCodeAndStandardData = setMnAndCodeAndStandardData(standardDataList);
                break;
        }
        return mnAndCodeAndStandardData;
    }

    private Map<String, Map<String, Map<String, Object>>> setMnAndCodeAndStandardData(List<Map<String, Object>> standardDataList) {
        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndLevelAndStandardData = new HashMap<>();
        Map<String, Map<String, Object>> codeAndLevelAndStandardData;
        Map<String, Object> levelAndStandardData;
        if (standardDataList.size() > 0) {
            String mnCommon;
            String pollutantCode;
            String standardminvalue;
            String standardmaxvalue;
            String alarmleveltype;
            String standardDataString;
            String alarmtype;
            for (Map<String, Object> standardData : standardDataList) {
                if (standardData.get("dgimn") != null && standardData.get("alarmleveltype") != null) {
                    mnCommon = standardData.get("dgimn").toString();
                    if (mnAndCodeAndLevelAndStandardData.containsKey(mnCommon)) {
                        codeAndLevelAndStandardData = mnAndCodeAndLevelAndStandardData.get(mnCommon);
                    } else {
                        codeAndLevelAndStandardData = new HashMap<>();
                    }
                    pollutantCode = standardData.get("pollutantcode").toString();
                    standardminvalue = standardData.get("standardminvalue") != null ? standardData.get("standardminvalue").toString() : "-";
                    alarmleveltype = standardData.get("alarmleveltype").toString();
                    standardmaxvalue = standardData.get("standardmaxvalue") != null ? standardData.get("standardmaxvalue").toString() : "-";

                    alarmtype = standardData.get("alarmtype") != null ? standardData.get("alarmtype").toString() : null;
                    if (alarmtype!=null){
                        if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(alarmtype)){//上限报警
                            standardDataString = standardmaxvalue;
                        }else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(alarmtype)){//下限报警
                            standardDataString = standardminvalue;
                        }else {
                            standardDataString = standardminvalue + "," + standardmaxvalue;
                        }
                        if (codeAndLevelAndStandardData.containsKey(pollutantCode)) {
                            levelAndStandardData = codeAndLevelAndStandardData.get(pollutantCode);
                        } else {
                            levelAndStandardData = new HashMap<>();
                        }
                        levelAndStandardData.put(alarmleveltype, standardDataString);
                        codeAndLevelAndStandardData.put(pollutantCode, levelAndStandardData);
                        mnAndCodeAndLevelAndStandardData.put(mnCommon, codeAndLevelAndStandardData);
                    }
                }
            }
        }
        return mnAndCodeAndLevelAndStandardData;
    }

    /**
     * @author: xsm
     * @date: 2021/01/13 0013 上午 10:02
     * @Description: 通过监测点ID、监测点类型获取该点位监测的污染物信息（安全点位）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getSecurityPointPollutantDataByParams(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getSecurityPointPollutantDataByParams(paramMap);
    }


    @Override
    public List<Map<String, Object>> getWaterEarlyAndStandardValueById(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getWaterEarlyAndStandardValueById(paramMap);
    }

    @Override
    public List<Map<String, Object>> getSecurityPollutantStandardDataByParam(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getSecurityPollutantStandardDataByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAllPollutionPollutants(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getAllPollutionSecurityPollutants(paramMap);
    }

    @Override
    public List<Map<String, Object>> getFlagListByParam(Map<String, Object> f_map) {
        return pollutantFactorMapper.getFlagListByParam(f_map);
    }

}

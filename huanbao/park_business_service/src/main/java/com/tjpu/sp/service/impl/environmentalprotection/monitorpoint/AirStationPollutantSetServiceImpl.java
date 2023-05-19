package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.dao.environmentalprotection.monitorpoint.AirStationPollutantSetMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.EarlyWarningSetMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirStationPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirStationPollutantSetService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AirStationPollutantSetServiceImpl implements AirStationPollutantSetService {
    @Autowired
    private AirStationPollutantSetMapper airStationPollutantSetMapper;
    @Autowired
    private EarlyWarningSetMapper earlyWarningSetMapper;




    /**
     * @author: lip
     * @date: 2019/5/22 0022 下午 1:29
     * @Description: 根据排口ID获取废水污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirPollutantByParamMap(Map<String,Object> paramMap) {
        List<AirStationPollutantSetVO> airStationPollutantSetByOutputId = airStationPollutantSetMapper.getAirStationPollutantSetByOutputId(paramMap);

        List<Map<String, Object>> dataList = new ArrayList<>();

        for (AirStationPollutantSetVO airStationPollutantSetVO : airStationPollutantSetByOutputId) {
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", airStationPollutantSetVO.getPollutantFactorVO().getCode());
            map.put("pollutantname", airStationPollutantSetVO.getPollutantFactorVO().getName());
            map.put("pollutantunit", airStationPollutantSetVO.getPollutantFactorVO().getPollutantunit());
            map.put("isdefaultselect", airStationPollutantSetVO.getPollutantFactorVO().getIsdefaultselect());
            if (airStationPollutantSetVO.getMonitorway() != null) {
                if (airStationPollutantSetVO.getMonitorway() == 1) {
                    map.put("monitorway", "在线");
                } else if (airStationPollutantSetVO.getMonitorway() == 2) {
                    map.put("monitorway", "手工");
                } else {
                    map.put("monitorway", "");
                }
            } else {
                map.put("monitorway", "");
            }
            if (airStationPollutantSetVO.getStandardVO() != null) {
                map.put("fk_standardid", airStationPollutantSetVO.getStandardVO().getPkStandardid());
                map.put("standardname", airStationPollutantSetVO.getStandardVO().getStandardname());
            } else {
                map.put("fk_standardid", "");
                map.put("standardname", "");
            }
            map.put("standardmaxvalue", airStationPollutantSetVO.getStandardmaxvalue());
            map.put("standardminvalue", airStationPollutantSetVO.getStandardminvalue());
            map.put("exceptionmaxvalue", airStationPollutantSetVO.getExceptionmaxvalue());
            map.put("exceptionminvalue", airStationPollutantSetVO.getExceptionminvalue());
            map.put("fk_pollutantcode", airStationPollutantSetVO.getFkPollutantcode());
            map.put("alarmcontroltimes", airStationPollutantSetVO.getAlarmcontroltimes());
            map.put("pollutantratio", airStationPollutantSetVO.getPollutantratio());
            map.put("pk_dataid", airStationPollutantSetVO.getPkDataid());
            map.put("alarmtype", airStationPollutantSetVO.getAlarmtype());
            map.put("zerovaluetimes", airStationPollutantSetVO.getZerovaluetimes());
            map.put("continuityvaluetimes", airStationPollutantSetVO.getContinuityvaluetimes());
            map.put("iseffectivetransmission", airStationPollutantSetVO.getIseffectivetransmission());
            map.put("concentrationchangewarnpercent", airStationPollutantSetVO.getConcentrationchangewarnpercent());
            map.put("ChangeBaseValue", airStationPollutantSetVO.getChangebasevalue());
            map.put("earlyWarningSetVOS", airStationPollutantSetVO.getEarlyWarningSetVOS());
            List<Map<String, Object>> alarmList = new ArrayList<>();

            if (airStationPollutantSetVO.getEarlyWarningSetVOS().size() > 0) {
                for (EarlyWarningSetVO earlyWarningSetVO : airStationPollutantSetVO.getEarlyWarningSetVOS()) {
                    Map<String, Object> tempMap = new HashMap<>();
                    if (earlyWarningSetVO.getAlarmLevelVO() != null) {
                        tempMap.put("alarmlevelcode", earlyWarningSetVO.getAlarmLevelVO().getCode());
                        tempMap.put("alarmlevelname", earlyWarningSetVO.getAlarmLevelVO().getName());
                    }
                    tempMap.put("concenalarmminvalue", earlyWarningSetVO.getConcenalarmminvalue());
                    tempMap.put("concenalarmmaxvalue", earlyWarningSetVO.getConcenalarmmaxvalue());
                    alarmList.add(tempMap);
                }

            }
            map.put("alarmlist", alarmList);
            dataList.add(map);
        }
        return dataList;
    }


    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 上午 10:25
     * @Description: 通过监测点id查询空气站相关污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAirPollutantsByParamMap(Map<String,Object> paramMap) {
        List<Map<String, Object>> airStationPollutantSetsByMonitorId = airStationPollutantSetMapper.getAirStationPollutantSetsByMonitorId(paramMap);

        return airStationPollutantSetsByMonitorId;
    }

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 6:13
     * @Description: 新增污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, list]
     * @throws:
     */
    @Override
    public int insertPollutants(AirStationPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(list.size()>0){
            earlyWarningSetMapper.insertSelectives(list);
        }
        if(record!=null){
            return airStationPollutantSetMapper.insertSelective(record);
        }
        return -1;
    }


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:41
     * @Description: 删除污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, outputid]
     * @throws:
     */
    @Override
    public int deletePollutants(String id, Map<String,Object> paramMap) {
        if(StringUtils.isNotBlank(id)){
            earlyWarningSetMapper.deleteByOutPutID(paramMap);
            return airStationPollutantSetMapper.deleteByPrimaryKey(id);
        }
        return -1;
    }


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:20
     * @Description:修改污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, list]
     * @throws:
     */
    @Override
    public int updatePollutants(AirStationPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(record!=null){
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("outputid",record.getFkAirmonintpointid());
            paramMap.put("pollutantcode",record.getFkPollutantcode());
            earlyWarningSetMapper.deleteByOutPutID(paramMap);
            if(list.size()>0){
                earlyWarningSetMapper.insertSelectives(list);
            }
            return airStationPollutantSetMapper.updateByPrimaryKey(record);
        }
        return -1;
    }
    /**
     *
     * @author: lip
     * @date: 2019/6/5 0005 下午 3:41
     * @Description: 获取城市空气质量污染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getCityAirPollutantSetInfo() {
        return airStationPollutantSetMapper.getCityAirPollutantSetInfo();
    }

    /**
     *
     * @author: xsm
     * @date: 2019/7/8 0008 下午 1:41
     * @Description: 批量新增空气质量污染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void insertAirStationPollutantSets(List<AirStationPollutantSetVO> list) {
        if (list!=null && list.size()>0){
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutants",list);
            airStationPollutantSetMapper.batchInsert(paramMap);
        }

    }
}

package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.dao.environmentalprotection.monitorpoint.EarlyWarningSetMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterOutPutPollutantSetMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterStationPollutantSetMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationPollutantSetVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterStationPollutantSetService;
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
public class WaterStationPollutantSetServiceImpl implements WaterStationPollutantSetService {


    @Autowired
    private WaterStationPollutantSetMapper waterStationPollutantSetMapper;

    @Autowired
    private WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper;

    @Autowired
    private EarlyWarningSetMapper earlyWarningSetMapper;

    /**
     * @author: chengzq
     * @date: 2019/9/18 0018 下午 5:27
     * @Description: 根据排口ID获取水质监测点污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWaterStationPollutantByParamMap(Map<String,Object> paramMap) {

        List<WaterStationPollutantSetVO> waterStationPollutantSetByOutputId = waterStationPollutantSetMapper.getWaterStationPollutantSetByOutputId(paramMap);

        List<Map<String, Object>> dataList = new ArrayList<>();

        for (WaterStationPollutantSetVO waterStationPollutantSetVO : waterStationPollutantSetByOutputId) {
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", waterStationPollutantSetVO.getPollutantFactorVO().getCode());
            map.put("pollutantname", waterStationPollutantSetVO.getPollutantFactorVO().getName());
            map.put("pollutantunit", waterStationPollutantSetVO.getPollutantFactorVO().getPollutantunit());
            map.put("isshowflow", waterStationPollutantSetVO.getPollutantFactorVO().getIsshowflow());
            map.put("isdefaultselect", waterStationPollutantSetVO.getPollutantFactorVO().getIsdefaultselect());
            if (waterStationPollutantSetVO.getMonitorway() != null) {
                if (waterStationPollutantSetVO.getMonitorway() == 1) {
                    map.put("monitorway", "在线");
                } else if (waterStationPollutantSetVO.getMonitorway() == 2) {
                    map.put("monitorway", "手工");
                } else {
                    map.put("monitorway", "");
                }
            } else {
                map.put("monitorway", "");
            }
            if (waterStationPollutantSetVO.getStandardVO() != null) {
                map.put("fk_standardid", waterStationPollutantSetVO.getStandardVO().getPkStandardid());
                map.put("standardname", waterStationPollutantSetVO.getStandardVO().getStandardname());
            } else {
                map.put("fk_standardid", "");
                map.put("standardname", "");
            }
            map.put("ishasconvertdata", 0);
            map.put("standardmaxvalue", waterStationPollutantSetVO.getStandardmaxvalue());
            map.put("standardminvalue", waterStationPollutantSetVO.getStandardminvalue());
            map.put("exceptionmaxvalue", waterStationPollutantSetVO.getExceptionmaxvalue());
            map.put("exceptionminvalue", waterStationPollutantSetVO.getExceptionminvalue());
            map.put("fk_pollutantcode", waterStationPollutantSetVO.getFkPollutantcode());
            map.put("alarmcontroltimes", waterStationPollutantSetVO.getAlarmcontroltimes());
            map.put("pk_dataid", waterStationPollutantSetVO.getPkDataid());
            map.put("alarmtype", waterStationPollutantSetVO.getAlarmtype());
            map.put("zerovaluetimes", waterStationPollutantSetVO.getZerovaluetimes());
            map.put("continuityvaluetimes", waterStationPollutantSetVO.getContinuityvaluetimes());
            map.put("concentrationchangewarnpercent", waterStationPollutantSetVO.getConcentrationchangewarnpercent());
            map.put("earlyWarningSetVOS", waterStationPollutantSetVO.getEarlyWarningSetVOS());

            List<Map<String, Object>> alarmList = new ArrayList<>();

            if (waterStationPollutantSetVO.getEarlyWarningSetVOS().size() > 0) {
                for (EarlyWarningSetVO earlyWarningSetVO : waterStationPollutantSetVO.getEarlyWarningSetVOS()) {
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
    *@author: liyc
    *@date:2019/9/26 0026 9:49
    *@Description: 根据水质监测点ID获取该监测点下所以污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param:No [paramMap]
    *@throws:
    */
    @Override
    public List<Map<String, Object>> getWaterStationAllPollutantsByIDAndType(Map<String, Object> paramMap) {
        return waterStationPollutantSetMapper.getWaterStationAllPollutantsByIDAndType(paramMap);
    }
    /**
    *@author: liyc
    *@date:2019/9/26 0026 13:55
    *@Description: 通过水质监测站点id获取该监测点的污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    @Override
    public List<Map<String, Object>> getWaterPollutantsByParamMap(Map<String, Object> paramMap) {
        return waterStationPollutantSetMapper.getWaterStationPollutantSetsByMonitorId(paramMap);
    }
    /**
    *@author: liyc
    *@date:2019/9/26 0026 15:59
    *@Description: 通过主键id，污染物code和水质站点id删除水质监测站污染物
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id,paramMap]
    *@throws:
    */
    @Override
    public int deletePollutants(String id, Map<String, Object> paramMap) {
        if (StringUtils.isNotBlank(id)){
            waterOutPutPollutantSetMapper.deleteByOutPutID(paramMap);
            return waterStationPollutantSetMapper.deleteByPrimaryKey(id);
        }
        return -1;
    }
    /**
    *@author:liyc
    *@date:2019/10/9 0009 10:02
    *@Description: 修改污染物set数据以及报警set数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [record,list]
    *@throws:
    */
    @Override
    public int updatePollutants(WaterStationPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(record!=null){
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("outputid",record.getFkWaterpointid());
            paramMap.put("pollutantcode",record.getFkPollutantcode());
            earlyWarningSetMapper.deleteByOutPutID(paramMap);
            if(list.size()>0){
                earlyWarningSetMapper.insertSelectives(list);
            }
            return waterStationPollutantSetMapper.updateByPrimaryKey(record);
        }
        return -1;
    }
    /**
    *@author:liyc
    *@date:2019/10/9 0009 11:19
    *@Description: 新增污染物set数据以及报警set数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [record,list]
    *@throws:
    */
    @Override
    public int insertPollutants(WaterStationPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(list.size()>0){
            earlyWarningSetMapper.insertSelectives(list);
        }
        if(record!=null){
            return waterStationPollutantSetMapper.insertSelective(record);
        }
        return -1;
    }

    @Override
    public List<Map<String, Object>> getWaterPollutantByParamMap(Map<String, Object> paramMap) {
        List<WaterStationPollutantSetVO> waterStationPollutantSetByOutputId = waterStationPollutantSetMapper.getWaterStationPollutantSetsByOutputId(paramMap);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (WaterStationPollutantSetVO waterStationPollutantSetVO : waterStationPollutantSetByOutputId){
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", waterStationPollutantSetVO.getPollutantFactorVO().getCode());
            map.put("pollutantname", waterStationPollutantSetVO.getPollutantFactorVO().getName());
            map.put("pollutantunit", waterStationPollutantSetVO.getPollutantFactorVO().getPollutantunit());
            if (waterStationPollutantSetVO.getMonitorway() != null) {
                if (waterStationPollutantSetVO.getMonitorway() == 1) {
                    map.put("monitorway", "在线");
                } else if (waterStationPollutantSetVO.getMonitorway() == 2) {
                    map.put("monitorway", "手工");
                } else {
                    map.put("monitorway", "");
                }
            } else {
                map.put("monitorway", "");
            }
            if (waterStationPollutantSetVO.getStandardVO() != null) {
                map.put("fk_standardid", waterStationPollutantSetVO.getStandardVO().getPkStandardid());
                map.put("standardname", waterStationPollutantSetVO.getStandardVO().getStandardname());
            } else {
                map.put("fk_standardid", "");
                map.put("standardname", "");
            }
            map.put("standardmaxvalue", waterStationPollutantSetVO.getStandardmaxvalue());
            map.put("standardminvalue", waterStationPollutantSetVO.getStandardminvalue());
            map.put("exceptionmaxvalue", waterStationPollutantSetVO.getExceptionmaxvalue());
            map.put("exceptionminvalue", waterStationPollutantSetVO.getExceptionminvalue());
            map.put("fk_pollutantcode", waterStationPollutantSetVO.getFkPollutantcode());
            map.put("alarmcontroltimes", waterStationPollutantSetVO.getAlarmcontroltimes());
            map.put("pollutantratio", waterStationPollutantSetVO.getPollutantratio());
            map.put("pk_dataid", waterStationPollutantSetVO.getPkDataid());
            map.put("alarmtype", waterStationPollutantSetVO.getAlarmtype());
            map.put("zerovaluetimes", waterStationPollutantSetVO.getZerovaluetimes());
            map.put("continuityvaluetimes", waterStationPollutantSetVO.getContinuityvaluetimes());
            map.put("iseffectivetransmission", waterStationPollutantSetVO.getIseffectivetransmission());
            map.put("concentrationchangewarnpercent", waterStationPollutantSetVO.getConcentrationchangewarnpercent());
            map.put("ChangeBaseValue", waterStationPollutantSetVO.getChangebasevalue());
            List<Map<String, Object>> alarmList = new ArrayList<>();
            if (waterStationPollutantSetVO.getEarlyWarningSetVOS().size() > 0) {
                for (EarlyWarningSetVO earlyWarningSetVO : waterStationPollutantSetVO.getEarlyWarningSetVOS()) {
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
    *@author:liyc
    *@date:2019/10/12 0012 10:59
    *@Description: 验证传入数据是否重复
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    @Override
    public int isTableDataHaveInfo(Map<String, Object> paramMap) {
        return waterStationPollutantSetMapper.isTableDataHaveInfo(paramMap);
    }
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 13:38
    *@Description: 批量新增水质质量污染物设置信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [waterlist]
    *@throws:
    **/
    @Override
    public void insertWaterStationPollutantSets(List<WaterStationPollutantSetVO> waterlist) {
        if (waterlist != null && waterlist.size() > 0){
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutants",waterlist);
            waterStationPollutantSetMapper.batchInsert(paramMap);
        }

    }


}

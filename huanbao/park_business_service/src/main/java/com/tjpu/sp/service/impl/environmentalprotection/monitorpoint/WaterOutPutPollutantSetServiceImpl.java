package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.dao.environmentalprotection.monitorpoint.EarlyWarningSetMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterOutPutPollutantSetMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutPutPollutantSetVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutPollutantSetService;
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
public class WaterOutPutPollutantSetServiceImpl implements WaterOutPutPollutantSetService {
    @Autowired
    private WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper;
    @Autowired
    private EarlyWarningSetMapper earlyWarningSetMapper;


    /**
     * @author: lip
     * @date: 2019/5/22 0022 下午 1:29
     * @Description: 根据自定义查询集合获取废水/雨水污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getWaterOrRainPollutantsByParamMap( Map<String,Object> paramMap) {
        List<WaterOutPutPollutantSetVO> waterOutPutPollutantSetVOS = waterOutPutPollutantSetMapper.getWaterOrRainPollutantsByParamMap(paramMap);

        List<Map<String, Object>> dataList = new ArrayList<>();

        for (WaterOutPutPollutantSetVO waterOutPutPollutantSetVO : waterOutPutPollutantSetVOS) {
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", waterOutPutPollutantSetVO.getPollutantFactorVO().getCode());
            map.put("pollutantname", waterOutPutPollutantSetVO.getPollutantFactorVO().getName());
            map.put("pollutantunit", waterOutPutPollutantSetVO.getPollutantFactorVO().getPollutantunit());
            map.put("Isshowflow", waterOutPutPollutantSetVO.getPollutantFactorVO().getIsshowflow());
            map.put("isdefaultselect", waterOutPutPollutantSetVO.getPollutantFactorVO().getIsdefaultselect());
            if (waterOutPutPollutantSetVO.getMonitorway() != null) {
                if (waterOutPutPollutantSetVO.getMonitorway() == 1) {
                    map.put("monitorway", "在线");
                } else if (waterOutPutPollutantSetVO.getMonitorway() == 2) {
                    map.put("monitorway", "手工");
                } else {
                    map.put("monitorway", "");
                }
            } else {
                map.put("monitorway", "");
            }
            if (waterOutPutPollutantSetVO.getStandardVO() != null) {
                map.put("fk_standardid", waterOutPutPollutantSetVO.getStandardVO().getPkStandardid());
                map.put("standardname", waterOutPutPollutantSetVO.getStandardVO().getStandardname());
            } else {
                map.put("fk_standardid", "");
                map.put("standardname", "");
            }
            map.put("standardmaxvalue", waterOutPutPollutantSetVO.getStandardmaxvalue());
            map.put("ishasconvertdata", 0);
            map.put("standardminvalue", waterOutPutPollutantSetVO.getStandardminvalue());
            map.put("exceptionmaxvalue", waterOutPutPollutantSetVO.getExceptionmaxvalue());
            map.put("exceptionminvalue", waterOutPutPollutantSetVO.getExceptionminvalue());
            map.put("pk_dataid", waterOutPutPollutantSetVO.getPkDataid());
            map.put("fk_pollutantcode", waterOutPutPollutantSetVO.getFkPollutantcode());
            map.put("alarmtype", waterOutPutPollutantSetVO.getAlarmtype());
            map.put("alarmcontroltimes", waterOutPutPollutantSetVO.getAlarmcontroltimes());
            map.put("zerovaluetimes", waterOutPutPollutantSetVO.getZerovaluetimes());
            map.put("continuityvaluetimes", waterOutPutPollutantSetVO.getContinuityvaluetimes());
            map.put("concentrationchangewarnpercent", waterOutPutPollutantSetVO.getConcentrationchangewarnpercent());
            map.put("flowchangewarnpercent", waterOutPutPollutantSetVO.getFlowchangewarnpercent());
            map.put("ChangeBaseValue", waterOutPutPollutantSetVO.getChangebasevalue());
            map.put("pollutantratio", waterOutPutPollutantSetVO.getPollutantratio());
            map.put("iseffectivetransmission", waterOutPutPollutantSetVO.getIseffectivetransmission());
            map.put("earlyWarningSetVOS", waterOutPutPollutantSetVO.getEarlyWarningSetVOS());
            List<Map<String, Object>> alarmList = new ArrayList<>();

            if (waterOutPutPollutantSetVO.getEarlyWarningSetVOS().size() > 0) {
                for (EarlyWarningSetVO earlyWarningSetVO : waterOutPutPollutantSetVO.getEarlyWarningSetVOS()) {
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
     * @date: 2019/5/28 0028 上午 11:57
     * @Description: 通过自定义参数获取废水排口污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String,Object>> getPollutantByParamMap(Map<String, Object> paramMap) {
        return waterOutPutPollutantSetMapper.getWaterOrRainPollutantByParamMap(paramMap);
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
    public int insertPollutants(WaterOutPutPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(list.size()>0){
            earlyWarningSetMapper.insertSelectives(list);
        }
        if(record!=null){
           return waterOutPutPollutantSetMapper.insertSelective(record);
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
    public int deletePollutants(String id,Map<String,Object> paramMap) {
        if(StringUtils.isNotBlank(id)){
            earlyWarningSetMapper.deleteByOutPutID(paramMap);
            return waterOutPutPollutantSetMapper.deleteByPrimaryKey(id);
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
    public int updatePollutants(WaterOutPutPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(record!=null){
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("outputid",record.getFkWateroutputid());
            paramMap.put("pollutantcode",record.getFkPollutantcode());
            earlyWarningSetMapper.deleteByOutPutID(paramMap);
            if(list.size()>0){
                earlyWarningSetMapper.insertSelectives(list);
            }
            return waterOutPutPollutantSetMapper.updateByPrimaryKey(record);
        }
        return -1;
    }

    @Override
    public List<Map<String, Object>> getAllWaterPollutantInfo(Map<String, Object> paramMap) {
        return waterOutPutPollutantSetMapper.getAllWaterPollutantInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutantSetByParam(Map<String, Object> paramMap) {
        return waterOutPutPollutantSetMapper.getPollutantSetByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutantSetListByParam(Map<String, Object> paramMap) {
        return waterOutPutPollutantSetMapper.getPollutantSetListByParam(paramMap);
    }


}

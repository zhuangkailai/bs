package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.dao.environmentalprotection.monitorpoint.EarlyWarningSetMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.GasOutPutPollutantSetMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GasOutPutPollutantSetVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;

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
public class GasOutPutPollutantSetServiceImpl implements GasOutPutPollutantSetService {
    @Autowired
    private GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper;
    @Autowired
    private EarlyWarningSetMapper earlyWarningSetMapper;


    /**
     * @author: chengzq
     * @date: 2019/5/22 0022 下午 1:29
     * @Description: 根据排口ID获取废气污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getGasPollutantsByOutputId(Map<String,Object> paramMap) {
        List<GasOutPutPollutantSetVO> gasOutPutPollutantSetVOS = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputId(paramMap);

        List<Map<String, Object>> dataList = new ArrayList<>();

        for (GasOutPutPollutantSetVO gasOutPutPollutantSetVO : gasOutPutPollutantSetVOS) {
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", gasOutPutPollutantSetVO.getPollutantFactorVO().getCode());
            map.put("pollutantname", gasOutPutPollutantSetVO.getPollutantFactorVO().getName());
            map.put("pollutantunit", gasOutPutPollutantSetVO.getPollutantFactorVO().getPollutantunit());
            map.put("Isshowflow", gasOutPutPollutantSetVO.getPollutantFactorVO().getIsshowflow());
            map.put("isdefaultselect", gasOutPutPollutantSetVO.getPollutantFactorVO().getIsdefaultselect());
            if (gasOutPutPollutantSetVO.getMonitorway() != null) {
                if (gasOutPutPollutantSetVO.getMonitorway() == 1) {
                    map.put("monitorway", "在线");
                } else if (gasOutPutPollutantSetVO.getMonitorway() == 2) {
                    map.put("monitorway", "手工");
                } else {
                    map.put("monitorway", "");
                }
            } else {
                map.put("monitorway", "");
            }
            if (gasOutPutPollutantSetVO.getStandardVO() != null) {
                map.put("fk_standardid", gasOutPutPollutantSetVO.getStandardVO().getPkStandardid());
                map.put("standardname", gasOutPutPollutantSetVO.getStandardVO().getStandardname());
            } else {
                map.put("fk_standardid", "");
                map.put("standardname", "");
            }
            map.put("ishasconvertdata", 0);
            map.put("standardmaxvalue", gasOutPutPollutantSetVO.getStandardmaxvalue());
            map.put("ishasconvertdata", gasOutPutPollutantSetVO.getIshasconvertdata());
            map.put("standardminvalue", gasOutPutPollutantSetVO.getStandardminvalue());
            map.put("exceptionmaxvalue", gasOutPutPollutantSetVO.getExceptionmaxvalue());
            map.put("exceptionminvalue", gasOutPutPollutantSetVO.getExceptionminvalue());
            map.put("fk_pollutantcode", gasOutPutPollutantSetVO.getFkPollutantcode());
            map.put("alarmcontroltimes", gasOutPutPollutantSetVO.getAlarmcontroltimes());
            map.put("pk_dataid", gasOutPutPollutantSetVO.getPkDataid());
            map.put("alarmtype", gasOutPutPollutantSetVO.getAlarmtype());
            map.put("zerovaluetimes", gasOutPutPollutantSetVO.getZerovaluetimes());
            map.put("continuityvaluetimes", gasOutPutPollutantSetVO.getContinuityvaluetimes());
            map.put("iseffectivetransmission", gasOutPutPollutantSetVO.getIseffectivetransmission());
            map.put("concentrationchangewarnpercent", gasOutPutPollutantSetVO.getConcentrationchangewarnpercent());
            map.put("flowchangewarnpercent", gasOutPutPollutantSetVO.getFlowchangewarnpercent());
            map.put("ChangeBaseValue", gasOutPutPollutantSetVO.getChangebasevalue());
            map.put("PollutantRatio", gasOutPutPollutantSetVO.getPollutantratio());
            map.put("earlyWarningSetVOS", gasOutPutPollutantSetVO.getEarlyWarningSetVOS());
            List<Map<String, Object>> alarmList = new ArrayList<>();

            if (gasOutPutPollutantSetVO.getEarlyWarningSetVOS().size() > 0) {
                for (EarlyWarningSetVO earlyWarningSetVO : gasOutPutPollutantSetVO.getEarlyWarningSetVOS()) {
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


    public List<Map<String, Object>> getGasPollutantByOutputId(Map<String,Object> paramMap) {
        List<Map<String, Object>> gasOutPutPollutantSetByOutputId = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetByOutputId(paramMap);

        return gasOutPutPollutantSetByOutputId;
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
    public int insertPollutants(GasOutPutPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(list.size()>0){
            earlyWarningSetMapper.insertSelectives(list);
        }
        if(record!=null){
            return gasOutPutPollutantSetMapper.insertSelective(record);
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
            return gasOutPutPollutantSetMapper.deleteByPrimaryKey(id);
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
    public int updatePollutants(GasOutPutPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(record!=null){
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("outputid",record.getFkGasoutputid());
            paramMap.put("pollutantcode",record.getFkPollutantcode());
            earlyWarningSetMapper.deleteByOutPutID(paramMap);
            if(list.size()>0){
                earlyWarningSetMapper.insertSelectives(list);
            }
            return gasOutPutPollutantSetMapper.updateByPrimaryKey(record);
        }
        return -1;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/22 0022 下午 7:01
     * @Description: 通过排口id集合查询废气，或废气无组织污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getGasOutPutPollutantSetsByOutputIds(Map<String, Object> paramMap) {
        return gasOutPutPollutantSetMapper.getGasOutPutPollutantSetsByOutputIds(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/2/18 0018 下午 8:10
     * @Description:  通过自定义条件获取废水，废气，雨水，水质的污染物报警类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutantSetInfoByParamMap(Map<String, Object> paramMap) {
        return gasOutPutPollutantSetMapper.getPollutantSetInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAlarmLevelDataByParam(Map<String, Object> paramMap) {
        return gasOutPutPollutantSetMapper.getAlarmLevelDataByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAllGasPollutantInfo(Map<String, Object> paramMap) {
        return gasOutPutPollutantSetMapper.getAllGasPollutantInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutantSetByParam(Map<String, Object> paramMap) {
        return gasOutPutPollutantSetMapper.getPollutantSetByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutantSetListByParam(Map<String, Object> paramMap) {
        return gasOutPutPollutantSetMapper.getPollutantSetListByParam(paramMap);
    }
}

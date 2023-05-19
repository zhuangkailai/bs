package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.EarlyWarningSetMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetService;
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
public class OtherMonitorPointPollutantSetServiceImpl implements OtherMonitorPointPollutantSetService {
    @Autowired
    private OtherMonitorPointPollutantSetMapper otherMonitorPointPollutantSetMapper;

    @Autowired
    private EarlyWarningSetMapper earlyWarningSetMapper;




    /**
     * @author: chengzq
     * @date: 2019/5/22 0022 下午 1:29
     * @Description: 根据排口ID获取其他监测点污染物设置表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOtherPollutantByParamMap(Map<String,Object> paramMap) {
        List<OtherMonitorPointPollutantSetVO> otherMonitorPollutantSetByOutputId = otherMonitorPointPollutantSetMapper.getOtherMonitorPollutantSetByOutputId(paramMap);

        List<Map<String, Object>> dataList = new ArrayList<>();

        for (OtherMonitorPointPollutantSetVO otherMonitorPointPollutantSetVO : otherMonitorPollutantSetByOutputId) {
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", otherMonitorPointPollutantSetVO.getPollutantFactorVO().getCode());
            map.put("pollutantname", otherMonitorPointPollutantSetVO.getPollutantFactorVO().getName());
            map.put("pollutantunit", otherMonitorPointPollutantSetVO.getPollutantFactorVO().getPollutantunit());
            map.put("isshowflow", otherMonitorPointPollutantSetVO.getPollutantFactorVO().getIsshowflow());
            map.put("isdefaultselect", otherMonitorPointPollutantSetVO.getPollutantFactorVO().getIsdefaultselect());
            if (otherMonitorPointPollutantSetVO.getMonitorway() != null) {
                if (otherMonitorPointPollutantSetVO.getMonitorway() == 1) {
                    map.put("monitorway", "在线");
                } else if (otherMonitorPointPollutantSetVO.getMonitorway() == 2) {
                    map.put("monitorway", "手工");
                } else {
                    map.put("monitorway", "");
                }
            } else {
                map.put("monitorway", "");
            }
            if (otherMonitorPointPollutantSetVO.getStandardVO() != null) {
                map.put("fk_standardid", otherMonitorPointPollutantSetVO.getStandardVO().getPkStandardid());
                map.put("standardname", otherMonitorPointPollutantSetVO.getStandardVO().getStandardname());
            } else {
                map.put("fk_standardid", "");
                map.put("standardname", "");
            }
            map.put("ishasconvertdata", 0);
            map.put("standardmaxvalue", otherMonitorPointPollutantSetVO.getStandardmaxvalue());
            map.put("standardminvalue", otherMonitorPointPollutantSetVO.getStandardminvalue());
            map.put("exceptionmaxvalue", otherMonitorPointPollutantSetVO.getExceptionmaxvalue());
            map.put("exceptionminvalue", otherMonitorPointPollutantSetVO.getExceptionminvalue());
            map.put("fk_pollutantcode", otherMonitorPointPollutantSetVO.getFkPollutantcode());
            map.put("alarmcontroltimes", otherMonitorPointPollutantSetVO.getAlarmcontroltimes());
            map.put("pollutantratio", otherMonitorPointPollutantSetVO.getPollutantratio());
            map.put("pk_dataid", otherMonitorPointPollutantSetVO.getPkDataid());
            map.put("alarmtype", otherMonitorPointPollutantSetVO.getAlarmtype());
            map.put("alarmtypename", CommonTypeEnum.AlarmTypeEnum.getNameByCode(otherMonitorPointPollutantSetVO.getAlarmtype()+"") );
            map.put("zerovaluetimes", otherMonitorPointPollutantSetVO.getZerovaluetimes());
            map.put("continuityvaluetimes", otherMonitorPointPollutantSetVO.getContinuityvaluetimes());
            map.put("iseffectivetransmission", otherMonitorPointPollutantSetVO.getIseffectivetransmission());
            map.put("concentrationchangewarnpercent", otherMonitorPointPollutantSetVO.getConcentrationchangewarnpercent());
            map.put("ChangeBaseValue", otherMonitorPointPollutantSetVO.getChangebasevalue());
            map.put("earlyWarningSetVOS", otherMonitorPointPollutantSetVO.getEarlyWarningSetVOS());
            List<Map<String, Object>> alarmList = new ArrayList<>();

            if (otherMonitorPointPollutantSetVO.getEarlyWarningSetVOS().size() > 0) {
                for (EarlyWarningSetVO earlyWarningSetVO : otherMonitorPointPollutantSetVO.getEarlyWarningSetVOS()) {
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
     * @Description: 通过监测点id查询其他监测点相关污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOtherPollutantsByParamMap(Map<String,Object> paramMap) {
        List<Map<String, Object>> otherMonitorPollutantSetsByMonitorId = otherMonitorPointPollutantSetMapper.getOtherMonitorPollutantSetsByMonitorId(paramMap);

        return otherMonitorPollutantSetsByMonitorId;
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
    public int insertPollutants(OtherMonitorPointPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(list.size()>0){
            earlyWarningSetMapper.insertSelectives(list);
        }
        if(record!=null){
            return otherMonitorPointPollutantSetMapper.insertSelective(record);
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
            return otherMonitorPointPollutantSetMapper.deleteByPrimaryKey(id);
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
    public int updatePollutants(OtherMonitorPointPollutantSetVO record, List<EarlyWarningSetVO> list) {
        if(record!=null){
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("outputid",record.getFkOthermonintpointid());
            paramMap.put("pollutantcode",record.getFkPollutantcode());
            earlyWarningSetMapper.deleteByOutPutID(paramMap);
            if(list.size()>0){
                earlyWarningSetMapper.insertSelectives(list);
            }
            return otherMonitorPointPollutantSetMapper.updateByPrimaryKey(record);
        }
        return -1;
    }

    /**
     *
     * @author: xsm
     * @date: 2019/7/8 0008 下午 2:41
     * @Description: 批量新增其它监测点染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void insertOtherMonitorPointPollutantSets(List<OtherMonitorPointPollutantSetVO> otherlist) {
        if (otherlist!=null && otherlist.size()>0){
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutants",otherlist);
            otherMonitorPointPollutantSetMapper.batchInsert(paramMap);
        }

    }

    @Override
    public List<Map<String, Object>> getOtherPollutantSetByParam(Map<String, Object> paramMap) {
        return otherMonitorPointPollutantSetMapper.getOtherPollutantSetByParam(paramMap);
    }
}

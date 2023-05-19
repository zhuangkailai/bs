package com.tjpu.sp.service.impl.environmentalprotection.devopsinfo;

import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DeviceDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.EntDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.online.EffectiveTransmissionMapper;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DevOpsAnalysisService;
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
public class DevOpsAnalysisServiceImpl implements DevOpsAnalysisService {
    @Autowired
    private DeviceDevOpsInfoMapper deviceDevOpsInfoMapper;
    @Autowired
    private EffectiveTransmissionMapper effectiveTransmissionMapper;
    @Autowired
    private EntDevOpsInfoMapper entDevOpsInfoMapper;


    /**
     * @Author: xsm
     * @Date: 2022/03/09 0009 13:09
     * @Description: 统计所有点位各状态点位数量
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public Map<String, Object> countAllPonitStatusDataByParam(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = deviceDevOpsInfoMapper.countAllPonitStatusDataByParam(paramMap);
        paramMap.clear();
        paramMap.put("totalnum",0);
        paramMap.put("offlinenum",0);
        paramMap.put("onlinenum",0);
        paramMap.put("overnum",0);
        paramMap.put("exceptionnum",0);
        for(String key:resultmap.keySet()){
            if (resultmap.get(key)!=null&&!"".equals(resultmap.get(key).toString())){
                paramMap.put(key,resultmap.get(key));
            }
        }
        return paramMap;
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/09 0009 14:37
     * @Description: 统计所有正在运维中的设备数量
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public Map<String, Object> countAllDeviceDevOpsNumDataByParam(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = deviceDevOpsInfoMapper.countAllDeviceDevOpsNumDataByParam(paramMap);
        paramMap.clear();
        paramMap.put("devopsnum",0);
        if (resultmap!=null){
            paramMap.put("devopsnum",resultmap.get("devopsnum"));
        }
        return paramMap;
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/09 0009 16:50
     * @Description: 统计有数据缺失的设备数
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public Map<String, Object> countAllDataMissingDeviceNumByParam(Map<String, Object> param) {
        Map<String, Object> resultmap = effectiveTransmissionMapper.getAllEffectiveTransmissionDataByParam(param);
        param.clear();
        param.put("missingnum",0);
        if (resultmap!=null){
            param.put("missingnum",resultmap.get("missingnum"));
        }
        return param;
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 运维工单统计(某月)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> countDeviceDevOpsWorkOrderData(Map<String, Object> param) {
        return deviceDevOpsInfoMapper.countDeviceDevOpsWorkOrderData(param);
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 设备运维分类统计(某月)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> countDeviceDevOpsDataGroupByMonitorType(Map<String, Object> param) {
        return deviceDevOpsInfoMapper.countDeviceDevOpsDataGroupByMonitorType(param);
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 运维单位工单统计(企业分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> countDeviceDevOpsDataGroupByPollution(Map<String, Object> param) {
        return deviceDevOpsInfoMapper.countDeviceDevOpsDataGroupByPollution(param);
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 历史运维单位工单统计分析(月份分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> countDeviceDevOpsDataGroupByMonth(Map<String, Object> param) {
        return deviceDevOpsInfoMapper.countDeviceDevOpsDataGroupByMonth(param);
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/10 0010 10:01
     * @Description: 统计设备传输率(按类型分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> countDeviceTransmissionRateDataGroupByType(Map<String, Object> param) {
        return effectiveTransmissionMapper.countDeviceTransmissionRateDataGroupByType(param);
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/11 0011 09:12
     * @Description: 异常排名统计(按点位分组)
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> countDeviceExceptionRateDataByParamMap(Map<String, Object> param) {
        return deviceDevOpsInfoMapper.countDeviceExceptionRateDataByParamMap(param);
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/11 0011 09:12
     * @Description: 获取运维点位分布
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getAllDeviceDevOpsPointDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist =  deviceDevOpsInfoMapper.getAllDeviceDevOpsPointDataByParamMap(paramMap);
        //获取每个运维点 关联的运维人员个数
        List<String> entdevopsids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            entdevopsids = datalist.stream().filter(m -> m.get("entdevopsid") != null).map(m -> m.get("entdevopsid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  personneldata = new ArrayList<>();
        if (entdevopsids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("entdevopsids",entdevopsids);
            //关联的运维人员信息
            personneldata = entDevOpsInfoMapper.getDevOpsPersonnelDataByParam(param);
            String entdevopsid;
            for (Map<String, Object> map:datalist) {
                if (map.get("entdevopsid") != null) {
                    entdevopsid = map.get("entdevopsid").toString();
                    String personnelnfo = "";
                    for (Map<String, Object> twomap : personneldata) {
                        if (twomap.get("pkid") != null && entdevopsid.equals(twomap.get("pkid").toString())) {
                            personnelnfo = personnelnfo + twomap.get("PersonnelName") + "(" + twomap.get("PersonnelPhone") + ")" + "、";
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

    @Override
    public List<Map<String, Object>> countDeviceDevOpsDataGroupByPoint(Map<String, Object> param) {
        return deviceDevOpsInfoMapper.countDeviceDevOpsDataGroupByPoint(param);
    }
}

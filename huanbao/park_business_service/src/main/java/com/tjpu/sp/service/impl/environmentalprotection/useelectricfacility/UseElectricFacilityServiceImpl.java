package com.tjpu.sp.service.impl.environmentalprotection.useelectricfacility;

import com.tjpu.sp.dao.environmentalprotection.useelectricfacility.UseElectricFacilityMapper;
import com.tjpu.sp.model.environmentalprotection.useelectricfacility.UseElectricFacilityVO;
import com.tjpu.sp.service.environmentalprotection.useelectricfacility.UseElectricFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class UseElectricFacilityServiceImpl implements UseElectricFacilityService {

    @Autowired
    private UseElectricFacilityMapper useElectricFacilityMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return useElectricFacilityMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insert(UseElectricFacilityVO record) {
        return useElectricFacilityMapper.insert(record);
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return useElectricFacilityMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(UseElectricFacilityVO record) {
        return useElectricFacilityMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:38
     * @Description:  通过自定义参数获取用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getUseElectricFacilityByParamMap(Map<String, Object> paramMap) {
        return useElectricFacilityMapper.getUseElectricFacilityByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:38
     * @Description: 通过id获取用电设施详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getUseElectricFacilityDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = useElectricFacilityMapper.getUseElectricFacilityByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }


    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 上午 9:06
     * @Description:  通过自定义参数获取用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countUseElectricFacilityInfo(Map<String, Object> paramMap) {
        return useElectricFacilityMapper.countUseElectricFacilityInfo(paramMap);
    }



    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 上午 10:32
     * @Description: 通过自定义条件查询用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getUseElectricFacilityAndDGIMNByParamMap(Map<String, Object> paramMap) {
        return useElectricFacilityMapper.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
    }

    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:44
     * @Description: 获取企业用电设施统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEntFacilityCountData(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = useElectricFacilityMapper.getEntFacilityCountData(paramMap);
        if (dataList.size() > 0) {
            for (Map<String, Object> dataMap : dataList) {
                if (dataMap.get("totalcwnum") == null) {
                    dataMap.put("totalcwnum", 0);
                }
                if (dataMap.get("totalzwnum") == null) {
                    dataMap.put("totalzwnum", 0);
                }
                if (dataMap.get("exceptioncwnum") == null) {
                    dataMap.put("exceptioncwnum", 0);
                }
                if (dataMap.get("exceptionzwnum") == null) {
                    dataMap.put("exceptionzwnum", 0);
                }
            }
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 2:51
     * @Description: 获取用电设施树形结构数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getFacilityTreeDataByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = useElectricFacilityMapper.getFacilityTreeDataByParam(paramMap);
        List<Map<String,Object>> resultList = new ArrayList<>();
        if (dataList.size() > 0) {
            Map<String, List<Map<String, Object>>> poAndEquipment = new HashMap<>();
            Map<String, List<Map<String, Object>>> idAndMonitorPoint = new HashMap<>();
            List<Map<String, Object>> equipmentList;
            List<Map<String, Object>> monitorPointList;
            String installPosition;
            String equipmentid;
            String monitorpointid;
            Set<String> ids = new HashSet<>();


            for (Map<String, Object> dataMap : dataList) {
                if (dataMap.get("installposition") != null) {
                    installPosition = dataMap.get("installposition").toString();
                    if (poAndEquipment.containsKey(installPosition)) {
                        equipmentList = poAndEquipment.get(installPosition);
                    } else {
                        equipmentList = new ArrayList<>();
                    }
                    equipmentid = dataMap.get("equipmentid").toString();
                    if (idAndMonitorPoint.containsKey(equipmentid)) {
                        monitorPointList = idAndMonitorPoint.get(equipmentid);
                    } else {
                        monitorPointList = new ArrayList<>();
                    }
                    if (dataMap.get("monitorpointid") != null) {
                        Map<String, Object> monitorPointMap = new HashMap<>();
                        monitorPointMap.put("monitorpointid", dataMap.get("monitorpointid"));
                        monitorPointMap.put("monitorpointname", dataMap.get("monitorpointname"));
                        monitorPointList.add(monitorPointMap);
                        idAndMonitorPoint.put(equipmentid,monitorPointList);
                    }
                    if (!ids.contains(equipmentid)){
                        Map<String, Object> equipmentMap = new HashMap<>();
                        equipmentMap.put("equipmentid", equipmentid);
                        equipmentMap.put("equipmentname", dataMap.get("equipmentname"));
                        equipmentList.add(equipmentMap);
                        equipmentMap.put("monitorpointlist", monitorPointList);
                        ids.add(equipmentid);
                    }
                    poAndEquipment.put(installPosition,equipmentList);
                }
            }

            if (poAndEquipment.size()>0){
                for (String poKey:poAndEquipment.keySet()){
                    Map<String,Object> poMap = new HashMap<>();
                    poMap.put("positionid",UUID.randomUUID().toString());
                    poMap.put("installposition",poKey);
                    poMap.put("equipmentlist",poAndEquipment.get(poKey));
                    resultList.add(poMap);
                }
            }
        }
        return resultList;
    }


    /**
     * @author: chengzq
     * @date: 2020/7/2 0002 下午 5:26
     * @Description: 通过自定义条件统计企业和设施及监测点数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutionAndFacilityInfoParams(Map<String, Object> paramMap) {
        return useElectricFacilityMapper.getPollutionAndFacilityInfoParams(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/7/8 0008 上午 11:53
     * @Description: 获取所有关联用电设施的企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllPollutionInfo(Map<String, Object> paramMap) {
        return useElectricFacilityMapper.getAllPollutionInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutionAndFacilityInfoParamMap(Map<String, Object> paramMap) {
        return useElectricFacilityMapper.getPollutionAndFacilityInfoParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/9/10 0010 下午 6:33
     * @Description: 查询废气废水等排口下用电监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getUseElectricAndOutputByParamMap(Map<String, Object> paramMap) {
        return useElectricFacilityMapper.getUseElectricAndOutputByParamMap(paramMap);
    }


}

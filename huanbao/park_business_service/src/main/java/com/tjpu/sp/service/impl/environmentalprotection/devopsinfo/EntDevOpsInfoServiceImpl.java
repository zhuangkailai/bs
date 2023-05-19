package com.tjpu.sp.service.impl.environmentalprotection.devopsinfo;

import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DevicePersonnelRecordMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.EntDevOpsInfoMapper;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevicePersonnelRecordVO;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.EntDevOpsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: liyc
 * @date:2019/10/21 0021 11:53
 * @Description: 排污许可证实现层实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class EntDevOpsInfoServiceImpl implements EntDevOpsInfoService {
    @Autowired
    private EntDevOpsInfoMapper entDevOpsInfoMapper;
    @Autowired
    private DevicePersonnelRecordMapper devicePersonnelRecordMapper;

    /**
     * @author: xsm
     * @date: 2019/12/03 0003 下午 2:08
     * @Description: 根据自定义参数获取企业运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntDevOpsInfosByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist = entDevOpsInfoMapper.getEntDevOpsInfosByParamMap(paramMap);
        //获取每个运维点 关联的运维人员个数
        List<String> entdevopsids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            entdevopsids = datalist.stream().filter(m -> m.get("pkid") != null).map(m -> m.get("pkid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  personneldata = new ArrayList<>();
        if (entdevopsids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("entdevopsids",entdevopsids);
            //关联的运维人员信息
            personneldata = entDevOpsInfoMapper.getDevOpsPersonnelDataByParam(param);
            String entdevopsid;
            for (Map<String, Object> map:datalist) {
                if (map.get("pkid") != null) {
                    entdevopsid = map.get("pkid").toString();
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

    /**
     * @author: xsm
     * @date: 2019/12/03 0003 下午 4:20
     * @Description: 新增企业运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addEntDevOpsInfos(List<EntDevOpsInfoVO> objlist, String monitorpointtype,List<String> pollutionids) {
        Map<String,Object> param = new HashMap<>();
        param.put("monitorpointtype",monitorpointtype);
        param.put("pollutions",pollutionids);
        entDevOpsInfoMapper.deleteEntDevOpsInfoByTypeAndPollutionids(param);
        if (objlist.size()>0) {
            entDevOpsInfoMapper.batchInsert(objlist);
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 1:17
     * @Description: 通过自定义参数获取企业运维信息和排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutionAndOutputInfoByParamMap(Map<String, Object> paramMap) {
        return entDevOpsInfoMapper.getPollutionAndOutputInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getEntDevOpsDataByParamMap(Map<String, Object> paramMap) {
        return entDevOpsInfoMapper.getEntDevOpsDataByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 下午 14:49
     * @Description: 通过自定义参数获取运维点位列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntDevOpsInfoListDataByParamMap(Map<String,Object> paramMap) {
        List<Map<String, Object>> datalist  = entDevOpsInfoMapper.getEntDevOpsInfoListDataByParamMap(paramMap);
        //获取每个运维点 关联的运维人员个数
        List<String> entdevopsids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            entdevopsids = datalist.stream().filter(m -> m.get("pkid") != null).map(m -> m.get("pkid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  personnelnumdata = new ArrayList<>();
        if (entdevopsids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("entdevopsids",entdevopsids);
            //关联的运维人数
            personnelnumdata = entDevOpsInfoMapper.countDevOpsPersonnelNumDataByParam(param);
            String entdevopsid;
            for (Map<String, Object> map:datalist){
                entdevopsid = map.get("pkid").toString();
                map.put("personnelnum",0);
                for (Map<String,Object> twomap:personnelnumdata){
                    if (twomap.get("pkid")!=null&&entdevopsid.equals(twomap.get("pkid").toString())){
                        map.put("personnelnum",(twomap.get("num")!=null&&!"".equals(twomap.get("num").toString()))?Integer.valueOf(twomap.get("num").toString()):0);
                        break;
                    }
                }
            }
        }
        return datalist;
    }

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 新增运维点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addDevOpsMonitorPointInfo(EntDevOpsInfoVO entity, List<DevicePersonnelRecordVO> list) {
        entDevOpsInfoMapper.insert(entity);
        //批量新增运维点位、人员关系记录
        if (list.size()>0){
            devicePersonnelRecordMapper.batchInsert(list);
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 修改运维监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updateDevOpsMonitorPointInfo(EntDevOpsInfoVO entity, List<DevicePersonnelRecordVO> list) {
        entDevOpsInfoMapper.updateByPrimaryKey(entity);
        //先删除正在运维点位 已配置的运维人员关系
        devicePersonnelRecordMapper.deleteByEntDevOpsID(entity.getPkId());
        //再添加新配置的关系
        //批量新增运维点位、人员关系记录
        if (list.size()>0){
            devicePersonnelRecordMapper.batchInsert(list);
        }
    }

    public void updateDevOpsPlanInfo(List<EntDevOpsInfoVO> onelist, List<DevicePersonnelRecordVO> twolist, List<String> oldpkids){
        //删除运维点位信息  及关联关系
        for(String oldid:oldpkids){
            //先删除正在运维点位 已配置的运维人员关系
            devicePersonnelRecordMapper.deleteByEntDevOpsID(oldid);
            entDevOpsInfoMapper.deleteByPrimaryKey(oldid);
        }
        //再批量添加运维点信息
        if (onelist.size()>0){
            entDevOpsInfoMapper.batchInsert(onelist);
        }
        //再添加新配置的关系
        //批量新增运维点位、人员关系记录
        if (twolist.size()>0){
            devicePersonnelRecordMapper.batchInsert(twolist);
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 获取运维监测点详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getDevOpsMonitorPointDetailByParam(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = entDevOpsInfoMapper.getDevOpsMonitorPointDetailByParam(paramMap);
        //获取该点位关联的用户
        List<Map<String, Object>> datalist = devicePersonnelRecordMapper.getPersonnelIdDataByParam(paramMap);
        List<String> personnelids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            personnelids = datalist.stream().filter(m -> m.get("fkpersonnelid") != null).map(m -> m.get("fkpersonnelid").toString()).distinct().collect(Collectors.toList());
        }
        if (resultMap!=null){
            resultMap.put("personnelids",personnelids);
        }
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 获取运维监测点详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deleteDevOpsMonitorPointByID(String id) {
        entDevOpsInfoMapper.deleteByPrimaryKey(id);
        //先删除正在运维点位 已配置的运维人员关系
        devicePersonnelRecordMapper.deleteByEntDevOpsID(id);
    }

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 下午 15:25
     * @Description: 根据监测类型和监测点ID获取该监测点的历史运维记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntDevOpsHistoryDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist = entDevOpsInfoMapper.getEntDevOpsHistoryDataByParamMap(paramMap);
        //获取每个运维点 关联的运维人员个数
        List<String> entdevopsids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            entdevopsids = datalist.stream().filter(m -> m.get("pkid") != null).map(m -> m.get("pkid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  personneldata = new ArrayList<>();
        if (entdevopsids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("entdevopsids",entdevopsids);
            //关联的运维人员信息
            personneldata = entDevOpsInfoMapper.getDevOpsPersonnelDataByParam(param);
            String entdevopsid;
            for (Map<String, Object> map:datalist) {
                if (map.get("pkid") != null) {
                    entdevopsid = map.get("pkid").toString();
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


}

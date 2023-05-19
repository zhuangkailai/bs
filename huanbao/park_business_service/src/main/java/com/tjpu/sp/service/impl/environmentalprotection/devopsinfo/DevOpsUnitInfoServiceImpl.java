package com.tjpu.sp.service.impl.environmentalprotection.devopsinfo;

import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DevOpsPersonnelMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DevOpsUnitInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.EntDevOpsInfoMapper;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevOpsUnitInfoVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DevOpsUnitInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DevOpsUnitInfoServiceImpl implements DevOpsUnitInfoService {
    @Autowired
    private DevOpsUnitInfoMapper devOpsUnitInfoMapper;
    @Autowired
    private DevOpsPersonnelMapper devOpsPersonnelMapper;
    @Autowired
    private EntDevOpsInfoMapper entDevOpsInfoMapper;

    /**
     * @Author: xsm
     * @Date: 2022/04/01 0001 08:56
     * @Description: 自定义查询条件查询运维单位列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getDevOpsUnitInfoListDataByParamMap(Map<String,Object> paramMap) {
       List<Map<String, Object>> datalist = devOpsUnitInfoMapper.getDevOpsUnitInfoListDataByParamMap(paramMap);
       //统计每个单位正在运维的点位个数和运维人数
        List<String> unitids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            unitids = datalist.stream().filter(m -> m.get("pkdevopsunitid") != null).map(m -> m.get("pkdevopsunitid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  peopledata = new ArrayList<>();
        List<Map<String, Object>>  pointnumdata = new ArrayList<>();
        if (unitids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("unitids",unitids);
            //运维人员
            peopledata = devOpsPersonnelMapper.countDevOpsPersonneNumGropuByUnitByParam(param);
            //正在运维的点位个数
            pointnumdata = entDevOpsInfoMapper.countDevOpsPointNumGropuByUnitByParam(param);
            String unitid;
            for (Map<String, Object> map:datalist){
                unitid = map.get("pkdevopsunitid").toString();
                map.put("personnenum",0);
                map.put("pointnum",0);
                for (Map<String,Object> onemap:peopledata){
                    if (onemap.get("fkdevopsunitid")!=null&&unitid.equals(onemap.get("fkdevopsunitid").toString())){
                        map.put("personnenum",(onemap.get("num")!=null&&!"".equals(onemap.get("num").toString()))?Integer.valueOf(onemap.get("num").toString()):0);
                        break;
                    }
                }
                for (Map<String,Object> twomap:pointnumdata){
                    if (twomap.get("fkdevopsunitid")!=null&&unitid.equals(twomap.get("fkdevopsunitid").toString())){
                        map.put("pointnum",(twomap.get("num")!=null&&!"".equals(twomap.get("num").toString()))?Integer.valueOf(twomap.get("num").toString()):0);
                        break;
                    }
                }
            }
        }
       return datalist;
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 新增运维单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addDevOpsUnitInfoInfo(DevOpsUnitInfoVO entity) {
        devOpsUnitInfoMapper.insert(entity);
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 修改运维单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updateDevOpsUnitInfoInfo(DevOpsUnitInfoVO entity) {
        devOpsUnitInfoMapper.updateByPrimaryKey(entity);
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维单位详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getDevOpsUnitInfoDetailById(String id) {
        return devOpsUnitInfoMapper.getDevOpsUnitInfoDetailById(id);
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 删除运维单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deleteDevOpsUnitInfoById(String id) {
        devOpsUnitInfoMapper.deleteByPrimaryKey(id);
        //删除单位关联的运维人员信息
        devOpsPersonnelMapper.deleteByDevOpsUnitID(id);
        //删除点位、运维人员中间表信息
        //删除单位关联的监测点位记录信息
        entDevOpsInfoMapper.deleteEntDevOpsInfoByUnitID(id);

    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 验证运维单位名称是否数据重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> IsHaveDevOpsUnitInfo(Map<String, Object> param) {
        return devOpsUnitInfoMapper.getDevOpsUnitInfoListDataByParamMap(param);
    }

    /**
     * @Author: xsm
     * @Date: 2022/06/10 0010 10:28
     * @Description: 运维单位正在运维点位个数排名
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> countDevOpsUnitDevOpsPointNumDataByParamMap() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> datalist = devOpsUnitInfoMapper.getDevOpsUnitInfoListDataByParamMap(new HashMap<>());
        //统计每个单位正在运维的点位个数和运维人数
        List<String> unitids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            unitids = datalist.stream().filter(m -> m.get("pkdevopsunitid") != null).map(m -> m.get("pkdevopsunitid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  pointnumdata = new ArrayList<>();
        if (unitids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("unitids",unitids);
            //正在运维的点位个数
            pointnumdata = entDevOpsInfoMapper.countDevOpsPointNumGropuByUnitByParam(param);
            String unitid;
            for (Map<String, Object> map:datalist){
                unitid = map.get("pkdevopsunitid").toString();
                for (Map<String,Object> twomap:pointnumdata){
                    if (twomap.get("fkdevopsunitid")!=null&&unitid.equals(twomap.get("fkdevopsunitid").toString())){
                        map.put("pointnum",(twomap.get("num")!=null&&!"".equals(twomap.get("num").toString()))?Integer.valueOf(twomap.get("num").toString()):0);
                        break;
                    }
                }
                if (map.get("pointnum")!=null) {
                    result.add(map);
                }
            }
        }
        if (result.size()>0) {
            result = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("pointnum").toString())).reversed()
            ).collect(Collectors.toList());
        }
        return result;
    }
}

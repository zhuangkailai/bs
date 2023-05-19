package com.tjpu.sp.service.impl.environmentalprotection.devopsinfo;

import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DevOpsPersonnelMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DevOpsUnitInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DevicePersonnelRecordMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.EntDevOpsInfoMapper;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevOpsPersonnelVO;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevicePersonnelRecordVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DevOpsPersonnelService;
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
public class DevOpsPersonnelServiceImpl implements DevOpsPersonnelService {
    @Autowired
    private DevOpsPersonnelMapper devOpsPersonnelMapper;
    @Autowired
    private EntDevOpsInfoMapper entDevOpsInfoMapper;
    @Autowired
    private DevicePersonnelRecordMapper devicePersonnelRecordMapper;


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
    public List<Map<String, Object>> getDevOpsPersonnelListDataByParamMap(Map<String,Object> paramMap) {
        List<Map<String, Object>> datalist  = devOpsPersonnelMapper.getDevOpsPersonnelListDataByParamMap(paramMap);
        //获取每个运维人员 关联的运维点位个数
        List<String> personnelids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            personnelids = datalist.stream().filter(m -> m.get("pkpersonnelid") != null).map(m -> m.get("pkpersonnelid").toString()).distinct().collect(Collectors.toList());
        }
        List<Map<String, Object>>  pointnumdata = new ArrayList<>();
        if (personnelids.size()>0){
            Map<String,Object> param = new HashMap<>();
            param.put("personnelids",personnelids);
            //正在运维的点位个数
            pointnumdata = entDevOpsInfoMapper.countDevOpsPointNumGropuByPersonnelByParam(param);
            String personnelid;
            for (Map<String, Object> map:datalist){
                personnelid = map.get("pkpersonnelid").toString();
                map.put("pointnum",0);
                for (Map<String,Object> twomap:pointnumdata){
                    if (twomap.get("fkpersonnelid")!=null&&personnelid.equals(twomap.get("fkpersonnelid").toString())){
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
     * @Description: 新增运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addDevOpsPersonnelInfo(DevOpsPersonnelVO entity, List<DevicePersonnelRecordVO> list) {
        devOpsPersonnelMapper.insert(entity);
        //批量新增运维点位、人员关系记录
        if (list.size()>0){
            devicePersonnelRecordMapper.batchInsert(list);
        }

    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 修改运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updateDevOpsPersonnelInfo(DevOpsPersonnelVO entity, List<DevicePersonnelRecordVO> list) {
        Map<String,Object> param = new HashMap<>();
        param.put("unitid",entity.getFkdevopsunitid());
        param.put("personnelid",entity.getPkPersonnelid());
        devOpsPersonnelMapper.updateByPrimaryKey(entity);
        //先删除修改用户 已配置的正在运维中的点位关系
        devicePersonnelRecordMapper.deleteByUnitIDAndPersonnelID(param);
        //再添加新配置的关系
        //批量新增运维点位、人员关系记录
        if (list.size()>0){
            devicePersonnelRecordMapper.batchInsert(list);
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维人员回显信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getDevOpsPersonnelInfoByParam(Map<String, Object> paramMap) {
        Map<String, Object> datamap = devOpsPersonnelMapper.getDevOpsPersonnelByID(paramMap.get("personnelid").toString());
        //获取该用户运维的点位
        List<Map<String, Object>> datalist = devicePersonnelRecordMapper.getEntDevOpsIdDataByParam(paramMap);
        List<String> entdevopsids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            entdevopsids = datalist.stream().filter(m -> m.get("fkentdevopsid") != null).map(m -> m.get("fkentdevopsid").toString()).distinct().collect(Collectors.toList());
        }
        if (datamap!=null){
            datamap.put("entdevopsids",entdevopsids);
        }
        return datamap;
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维人员详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getDevOpsPersonnelDetailByID(Map<String,Object> paramMap) {
        Map<String, Object> datamap = devOpsPersonnelMapper.getDevOpsPersonnelDetailByID(paramMap.get("personnelid").toString());
        //获取该用户运维的点位
        List<Map<String, Object>> datalist = devicePersonnelRecordMapper.getEntDevOpsIdDataByParam(paramMap);
        List<String> entdevopsids = new ArrayList<>();
        if (datalist!=null&&datalist.size()>0){
            entdevopsids = datalist.stream().filter(m -> m.get("fkentdevopsid") != null).map(m -> m.get("fkentdevopsid").toString()).distinct().collect(Collectors.toList());
        }
        if (datamap!=null){
            datamap.put("entdevopsids",entdevopsids);
        }
        return datamap;
    }



    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 删除运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deleteDevOpsPersonnelByID(String id) {
        DevOpsPersonnelVO obj = devOpsPersonnelMapper.selectByPrimaryKey(id);
        if (obj!=null) {
            Map<String, Object> param = new HashMap<>();
            param.put("unitid", obj.getFkdevopsunitid());
            param.put("personnelid", id);
            //删除已配置的正在运维中的点位关系
            devicePersonnelRecordMapper.deleteByUnitIDAndPersonnelID(param);
            //删除人员信息
            devOpsPersonnelMapper.deleteByPrimaryKey(id);
        }
    }


}

package com.tjpu.sp.service.impl.environmentalprotection.useelectricfacilitymonitorpoint;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.DeviceStatusMapper;
import com.tjpu.sp.dao.environmentalprotection.useelectricfacilitymonitorpoint.UseElectricFacilityMonitorPointMapper;
import com.tjpu.sp.dao.environmentalprotection.useelectricfacilitymonitorpointset.UseElectricFacilityMonitorPointSetMapper;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpoint.UseElectricFacilityMonitorPointVO;
import com.tjpu.sp.model.environmentalprotection.useelectricfacilitymonitorpointset.UseElectricFacilityMonitorPointSetVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import com.tjpu.sp.service.environmentalprotection.useelectricfacilitymonitorpoint.UseElectricFacilityMonitorPointService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.ElectricFacilityEnum;



@Service
@Transactional
public class UseElectricFacilityMonitorPointServiceImpl implements UseElectricFacilityMonitorPointService {

    @Autowired
    private UseElectricFacilityMonitorPointMapper useElectricFacilityMonitorPointMapper;
    @Autowired
    private UseElectricFacilityMonitorPointSetMapper useElectricFacilityMonitorPointSetMapper;
    @Autowired
    private DeviceStatusMapper deviceStatusMapper;
    @Autowired
    private DeviceStatusService deviceStatusService;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        Map<String, Object> stringObjectMap = useElectricFacilityMonitorPointMapper.selectByPrimaryKey(pkId);
        String befordgimn = stringObjectMap.get("dgimn")==null?"":stringObjectMap.get("dgimn").toString();

        //删除状态信息
        if(stringObjectMap !=null && StringUtils.isNotBlank(befordgimn)){
            List<DeviceStatusVO> deviceStatusVOS = deviceStatusService.selectByDgimn(befordgimn);
            if(deviceStatusVOS.size()>0){
                DeviceStatusVO deviceStatusVO = deviceStatusVOS.get(0);
                if(deviceStatusVO!=null && StringUtils.isNotBlank(deviceStatusVO.getPkId())){
                    deviceStatusService.deleteByPrimaryKey(deviceStatusVO.getPkId());
                }
            }
        }

        useElectricFacilityMonitorPointSetMapper.deleteByfkuseelectricfacilitymonitorpointid(pkId);
        return useElectricFacilityMonitorPointMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insert(UseElectricFacilityMonitorPointVO record,List<UseElectricFacilityMonitorPointSetVO> pollutants) {
        if(record.getdgimn() != null && !"".equals(record.getdgimn())){
            DeviceStatusVO deviceStatusVO = new DeviceStatusVO();
            deviceStatusVO.setDgimn(record.getdgimn());
            deviceStatusVO.setPkId(UUID.randomUUID().toString());
            deviceStatusVO.setFkMonitorpointtypecode("2");
            deviceStatusVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(record.getupdatetime()));
            deviceStatusVO.setUpdateuser(record.getupdateuser());
            deviceStatusMapper.insert(deviceStatusVO);
        }
        for (UseElectricFacilityMonitorPointSetVO pollutant : pollutants) {
            useElectricFacilityMonitorPointSetMapper.insert(pollutant);
        }
        return useElectricFacilityMonitorPointMapper.insert(record);
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return useElectricFacilityMonitorPointMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(UseElectricFacilityMonitorPointVO record,List<UseElectricFacilityMonitorPointSetVO> pollutants) {

        Map<String,Object> useElectricFacility = useElectricFacilityMonitorPointMapper.selectByPrimaryKey(record.getpkid());

        String befordgimn = useElectricFacility.get("dgimn")==null?"":useElectricFacility.get("dgimn").toString();

        //修改状态表dgimn
        deviceStatusService.updateMonitorDgimn(befordgimn,record.getdgimn(),ElectricFacilityEnum.getCode()+"");

        useElectricFacilityMonitorPointSetMapper.deleteByfkuseelectricfacilitymonitorpointid(record.getpkid());
        for (UseElectricFacilityMonitorPointSetVO pollutant : pollutants) {
            useElectricFacilityMonitorPointSetMapper.insert(pollutant);
        }
        return useElectricFacilityMonitorPointMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:38
     * @Description:  通过自定义参数获取用电设施监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getUseElectricFacilityMonitorPointByParamMap(Map<String, Object> paramMap) {
        return useElectricFacilityMonitorPointMapper.getUseElectricFacilityMonitorPointByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:38
     * @Description: 通过id获取用电设施监测点详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getUseElectricFacilityMonitorPointDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = useElectricFacilityMonitorPointMapper.getUseElectricFacilityMonitorPointByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }
    /**
     *
     * @author: lip
     * @date: 2020/6/22 0022 下午 4:38
     * @Description: 自定义查询条件获取用电设施点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnlineMonitorPointListByParam(Map<String, Object> paramMap) {
        return useElectricFacilityMonitorPointMapper.getOnlineMonitorPointListByParam(paramMap);
    }
    /**
     *
     * @author: lip
     * @date: 2020/6/23 0023 下午 4:25
     * @Description: 获取产污治污点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getCWAndZWMonitorPointListByParam(Map<String, Object> paramMap) {
        return useElectricFacilityMonitorPointMapper.getCWAndZWMonitorPointListByParam(paramMap);
    }

}

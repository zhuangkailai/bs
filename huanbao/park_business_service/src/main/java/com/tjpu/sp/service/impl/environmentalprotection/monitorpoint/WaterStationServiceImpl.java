package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterStationMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class WaterStationServiceImpl implements WaterStationService {

    @Autowired
    private WaterStationMapper waterStationMapper;

    /**
     * @author: chengzq
     * @date: 2019/9/18 0018 下午 5:01
     * @Description: 动态条件获取水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOnlineWaterStationInfoByParamMap(Map<String, Object> paramMap) {
        return waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 上午 11:51
     * @Description: 获取所有水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWaterStationByParamMap(Map<String, Object> paramMap) {
        return waterStationMapper.getWaterStationByParamMap(paramMap);
    }
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 13:25
    *@Description: 根据监测点名称和MN号获取新增的那条水质站点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [params]
    *@throws:
    **/
    @Override
    public Map<String, Object> selectWaterStationInfoByPointNameAndDgimn(Map<String, Object> params) {
        return waterStationMapper.selectWaterStationInfoByPointNameAndDgimn(params);
    }
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 14:15
    *@Description: 根据监测点ID获取该监测点在线监测设备基础信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [parammap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getWaterStationDeviceStatusByID(Map<String, Object> parammap) {
        return waterStationMapper.getWaterStationDeviceStatusByID(parammap);
    }
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 14:42
    *@Description: 根据监测点ID获取附件表对应关系
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [parammap]
    *@throws:
    **/
    @Override
    public List<String> getfileIdsByID(Map<String, Object> parammap) {
        List<Map<String, Object>> oldobj = waterStationMapper.getfileIdsByID(parammap);
        List<String> list = new ArrayList<>();
        if (oldobj != null && oldobj.size() > 0) {
            for (Map<String, Object> obj : oldobj) {
                list.add(obj.get("FilePath").toString());
            }
        }
        return list;
    }

    /**
     *@author: xsm
     *@date: 2019/11/14 0014 14:56
     *@Description: 获取所有水质监测点信息和状态
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    @Override
    public List<Map<String, Object>> getAllWaterStationAndStatusInfo() {
        return waterStationMapper.getAllWaterStationAndStatusInfo();
    }
    /**
    *@author: liyc
    *@date: 2019/11/18 0018 10:18
    *@Description: 通过主键ID获取水质站点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pkid]
    *@throws:
    **/
    @Override
    public WaterStationVO getWaterStationByID(String pkid) {
        return waterStationMapper.selectByPrimaryKey(pkid);
    }

    @Override
    public Map<String, Object> getAllWaterStationByType(Map<String, Object> paramMap) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> airdata = waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
        int onlinenum = 0;
        int offlinenum = 0;
        if (airdata != null && airdata.size() > 0) {
            for (Map<String, Object> map : airdata) {
                int status = 0;
                if (map.get("onlinestatus") != null && !"".equals(map.get("onlinestatus").toString())) {//当状态不为空
                    if ("1".equals(map.get("onlinestatus").toString())) {//1为在线
                        status = 1;
                    } else {
                        if (status < Integer.parseInt(map.get("onlinestatus").toString())) {
                            status = Integer.parseInt(map.get("onlinestatus").toString());
                        }
                    }
                }
                if (status == 0) {//离线
                    offlinenum += 1;
                } else if (status == 1) {//在线
                    onlinenum += 1;
                }
                map.put("onlinestatus", status);
            }
        }
        result.put("total", (airdata != null && airdata.size() > 0) ? airdata.size() : 0);
        result.put("onlinenum", onlinenum);
        result.put("offlinenum", offlinenum);
        result.put("listdata", airdata);
        return result;
    }

    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return waterStationMapper.countTotalByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAllWaterStationInfoByParamMap(Map<String, Object> paramMap) {
        return waterStationMapper.getAllWaterStationInfoByParamMap(paramMap);
    }
}

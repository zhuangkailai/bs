package com.tjpu.sp.service.impl.environmentalprotection.devopsinfo;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.DeviceDevOpsInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.devopsinfo.EntDevOpsExplainMapper;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsExplainVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.EntDevOpsExplainService;
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
public class EntDevOpsExplainServiceImpl implements EntDevOpsExplainService {
    @Autowired
    private EntDevOpsExplainMapper entDevOpsExplainMapper;
    @Autowired
    private DeviceDevOpsInfoMapper deviceDevOpsInfoMapper;


    /**
     * @author: xsm
     * @date: 2019/12/04 0004 下午 3:01
     * @Description: 根据自定义参数获取设备运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntDevOpsExplainsByParamMap(Map<String, Object> paramMap) {
        return entDevOpsExplainMapper.getEntDevOpsExplainsByParamMap(paramMap);
    }

    @Override
    public void addEntDevOpsExplain(EntDevOpsExplainVO entity) {
        entDevOpsExplainMapper.insert(entity);
    }


    @Override
    public Map<String, Object> getEntDevOpsExplainDetailByID(String id) {
        return entDevOpsExplainMapper.getEntDevOpsExplainDetailByID(id);
    }

    @Override
    public List<Map<String, Object>> getDeviceAndEntDevOpsExplainsByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> pointlist = (List<Map<String, Object>>) paramMap.get("pointlist");
        //设备报备
        List<Map<String, Object>> devlist = deviceDevOpsInfoMapper.getDevicesDevOpsInfoByTimesAndType(paramMap);
        //企业报备
        List<Map<String, Object>> entlist = deviceDevOpsInfoMapper.getEntDevOpsExplainsByTimesAndType(paramMap);

        Map<String, List<Map<String, Object>>> devMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> entMap = new HashMap<>();
        if (devlist!=null&&devlist.size()>0){
            devMap= devlist.stream().collect(Collectors.groupingBy(m -> m.get("monitorpointid").toString()));
        }
        if (entlist!=null&&entlist.size()>0){
            entMap= entlist.stream().collect(Collectors.groupingBy(m -> m.get("monitorpointid").toString()));
        }
        String id = "";
        for (Map<String, Object> map:pointlist){
            Map<String, Object> objmap = new HashMap<>();
            id = map.get("monitorpointid")!=null?map.get("monitorpointid").toString():"";
            if (entMap.get(id)!=null){
                objmap.put("entdevops",entMap.get(id));
            }else{
                objmap.put("entdevops",new ArrayList<>());
            }
            if (devMap.get(id)!=null){
                objmap.put("devicedevops",devMap.get(id));
            }else{
                objmap.put("devicedevops",new ArrayList<>());
            }
            objmap.put("monitorpointid",id);
            if (entMap.get(id)!=null||devMap.get(id)!=null) {
                result.add(objmap);
            }
        }
        return result;
    }


    @Override
    public List<Map<String, Object>> getOnePointEntDevOpsExplainsByParamMap(Map<String, Object> paramMap) {
        return entDevOpsExplainMapper.getOnePointEntDevOpsExplainsByParamMap(paramMap);
    }

    @Override
    public void deleteEntDevOpsExplainByID(String id) {
         entDevOpsExplainMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void editDevOpsExplain(EntDevOpsExplainVO obj) {
        entDevOpsExplainMapper.updateByPrimaryKey(obj);
    }

    @Override
    public Map<String, Object> getEntDevOpsExplainUpdateDataByID(String id) {
        Map<String, Object> objmap = new HashMap<>();
        EntDevOpsExplainVO obj = entDevOpsExplainMapper.selectByPrimaryKey(id);
        if (obj!=null) {
            objmap.put("fk_monitorpointid", obj.getFkMonitorpointid());
            objmap.put("fk_monitorpointtypecode", obj.getFkMonitorpointtypecode());
            objmap.put("starttime", obj.getStarttime() != null ? DataFormatUtil.getDateYMDHMS(obj.getStarttime()) : null);
            objmap.put("endtime", obj.getEndtime() != null ? DataFormatUtil.getDateYMDHMS(obj.getEndtime()) : null);
            objmap.put("remark", obj.getRemark());
        }
        return objmap;
    }


}

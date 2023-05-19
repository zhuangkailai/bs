package com.tjpu.sp.service.impl.environmentalprotection.tracesource;


import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TraceSourceConfigInfoMapper;
import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceConfigInfoVO;
import com.tjpu.sp.service.environmentalprotection.tracesource.TraceSourceConfigInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class TraceSourceConfigInfoServiceImpl implements TraceSourceConfigInfoService {

    @Autowired
    private TraceSourceConfigInfoMapper traceSourceConfigInfoMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;


    /**
     * @author: lip
     * @date: 2019/8/13 0013 下午 4:51
     * @Description: 自定义查询条件获取溯源配置属性信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceConfigDataByParamMap(Map<String, Object> paramMap) {
        return traceSourceConfigInfoMapper.getTraceSourceConfigDataByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/8/28 0028 下午 5:01
     * @Description: 修改溯源配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateTraceSourceConfigInfo(List<TraceSourceConfigInfoVO> adddata, List<String> delete) {
        try {
            if (delete != null && delete.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("attributecodes", delete);
                traceSourceConfigInfoMapper.detleteTraceSourceConfigDataByAttributeCode(paramMap);
            }
            if (adddata != null && adddata.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("datalist", adddata);
                traceSourceConfigInfoMapper.batchInsert(paramMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: xsm
     * @date: 2019/8/30 0030 上午 9:38
     * @Description: 获取溯源污染物下拉框信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTraceSourcePollutantSelectData() {
        Map<String, Object> paramMap = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        list.add(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
        list.add(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        paramMap.put("monitorpointtypes", list);
        return pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/8/30 0030 上午 9:38
     * @Description: 获取按属性Code分组的溯源配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceConfigInfoGroupByCode(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> datalist = traceSourceConfigInfoMapper.getAllTraceSourceConfigInfo(paramMap);
        if (datalist != null && datalist.size() > 0) {
            Set<Object> codes = new HashSet<>();
            for (Map<String, Object> mapTemp : datalist) {
                List<Object> list = new ArrayList<>();
                if (!codes.contains(mapTemp.get("AttributeCode"))) {
                    for (Map<String, Object> map : datalist) {
                        if ((mapTemp.get("AttributeCode").toString()).equals(map.get("AttributeCode").toString())) {//找出同一属性的数据
                            if (map.get("AttributeValue") != null) {
                                list.add(map.get("AttributeValue"));
                            }
                        }
                    }
                    if (list != null && list.size() > 0) {
                        if (list.size() == 1) {
                            mapTemp.put("AttributeValue", list.get(0));
                        } else {
                            mapTemp.put("AttributeValue", list);
                        }
                    } else {
                        mapTemp.put("AttributeValue", "");
                    }
                    result.add(mapTemp);
                    codes.add(mapTemp.get("AttributeCode"));
                }

            }
        }
        return result;
    }
}

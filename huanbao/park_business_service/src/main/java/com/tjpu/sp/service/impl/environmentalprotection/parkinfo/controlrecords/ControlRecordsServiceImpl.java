package com.tjpu.sp.service.impl.environmentalprotection.parkinfo.controlrecords;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointMapper;
import com.tjpu.sp.dao.environmentalprotection.parkinfo.controlrecords.ControlRecordsMapper;
import com.tjpu.sp.model.environmentalprotection.controlrecords.ControlRecordsVO;
import com.tjpu.sp.service.environmentalprotection.parkinfo.controlrecords.ControlRecordsService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ControlRecordsServiceImpl implements ControlRecordsService {
    private final ControlRecordsMapper controlRecordsMapper;
    private final PollutantFactorMapper pollutantFactorMapper;
    private final OtherMonitorPointMapper otherMonitorPointMapper;

    public ControlRecordsServiceImpl(ControlRecordsMapper controlRecordsMapper, PollutantFactorMapper pollutantFactorMapper, OtherMonitorPointMapper otherMonitorPointMapper) {
        this.controlRecordsMapper = controlRecordsMapper;
        this.pollutantFactorMapper = pollutantFactorMapper;
        this.otherMonitorPointMapper = otherMonitorPointMapper;
    }


    /**
     * @author: lip
     * @date: 2020/5/9 0009 下午 4:58
     * @Description: 获取最新一条管控建议记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getLastData() {
        return controlRecordsMapper.getLastData();
    }

    /**
     * @author: lip
     * @date: 2020/5/11 0011 下午 3:44
     * @Description: 自定义查询条件获取管控建议记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getControlRecordsDataByParam(Map<String, Object> paramMap) {
        return controlRecordsMapper.getControlRecordsDataByParam(paramMap);
    }

    @Override
    public PageInfo<Map<String, Object>> getPageDataByParam(Map<String, Object> paramMap) {
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {//分页数据
            PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        List<Map<String, Object>> dataList = controlRecordsMapper.getControlRecordsDataByParam(paramMap);
        if (dataList.size() > 0) {
            Map<String, String> codeAndName = new HashMap<>();
            paramMap.put("pollutanttypes", Arrays.asList(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                    CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()));
            List<Map<String, Object>> pollutantDataList = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
            for (Map<String, Object> pollutantData : pollutantDataList) {
                codeAndName.put(pollutantData.get("code").toString(), pollutantData.get("name").toString());
            }
            String pollutantCodes;
            List<String> names;
            String pollutantNames;
            for (Map<String, Object> mapIndex : dataList) {
                if (mapIndex.get("pollutantcodes") != null) {
                    pollutantCodes = mapIndex.get("pollutantcodes").toString();
                    if (pollutantCodes.indexOf(",") >= 0) {
                        names = new ArrayList<>();
                        String[] codes = pollutantCodes.split(",");
                        for (int i = 0; i < codes.length; i++) {
                            if (codeAndName.containsKey(codes[i])) {
                                names.add(codeAndName.get(codes[i]));
                            }
                        }
                        names = names.stream().distinct().collect(Collectors.toList());
                        pollutantNames = DataFormatUtil.FormatListToString(names, "、");
                    } else {
                        pollutantNames = codeAndName.get(pollutantCodes);
                    }
                    mapIndex.put("pollutantNames", pollutantNames);
                }
            }
        }
        return new PageInfo<>(dataList);
    }

    @Override
    public void addData(ControlRecordsVO controlRecordsVO) {
        controlRecordsMapper.insert(controlRecordsVO);
    }

    @Override
    public void updateData(ControlRecordsVO controlRecordsVO) {
        controlRecordsMapper.updateByPrimaryKey(controlRecordsVO);
    }

    @Override
    public void deleteById(String id) {
        controlRecordsMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getEditOrDetailById(String id) {
        Map<String, Object> dataMap = controlRecordsMapper.getEditOrDetailById(id);
        Map<String, String> codeAndName = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttypes", Arrays.asList(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()));
        List<Map<String, Object>> pollutantDataList = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> pollutantData : pollutantDataList) {
            codeAndName.put(pollutantData.get("code").toString(), pollutantData.get("name").toString());
        }
        String pollutantCodes;
        List<String> names;
        String pollutantNames;
        if (dataMap.get("pollutantcodes") != null) {
            pollutantCodes = dataMap.get("pollutantcodes").toString();
            if (pollutantCodes.indexOf(",") >= 0) {
                names = new ArrayList<>();
                String[] codes = pollutantCodes.split(",");
                for (int i = 0; i < codes.length; i++) {
                    if (codeAndName.containsKey(codes[i])) {
                        names.add(codeAndName.get(codes[i]));
                    }
                }
                names = names.stream().distinct().collect(Collectors.toList());
                pollutantNames = DataFormatUtil.FormatListToString(names, "、");
            } else {
                pollutantNames = codeAndName.get(pollutantCodes);
            }
            dataMap.put("pollutantNames", pollutantNames);
        }
        return dataMap;
    }

    @Override
    public List<Map<String, Object>> getAllStinkPoint() {
        return controlRecordsMapper.getAllStinkPoint();
    }
}

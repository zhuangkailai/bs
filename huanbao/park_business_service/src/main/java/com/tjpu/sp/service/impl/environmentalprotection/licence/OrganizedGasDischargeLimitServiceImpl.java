package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.licence.OrganizedGasDischargeLimitMapper;
import com.tjpu.sp.service.environmentalprotection.licence.OrganizedGasDischargeLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Transactional
public class OrganizedGasDischargeLimitServiceImpl implements OrganizedGasDischargeLimitService {

    private final OrganizedGasDischargeLimitMapper organizedGasDischargeLimitMapper;

    public OrganizedGasDischargeLimitServiceImpl(OrganizedGasDischargeLimitMapper organizedGasDischargeLimitMapper) {
        this.organizedGasDischargeLimitMapper = organizedGasDischargeLimitMapper;
    }


    @Override
    public List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = organizedGasDischargeLimitMapper.getDataListByParam(paramMap);
        if (dataList.size() > 0) {//设置总计数据
            String outlettype = "1".equals(paramMap.get("outlettype")) ? "主要排放口合计" : "一般排放口合计";
            Map<String, List<Map<String, Object>>> nameAndDataList = dataList.stream().filter(m -> m.get("pollutantname") != null).collect(Collectors.groupingBy(m -> m.get("pollutantname").toString()));
            List<Map<String, Object>> tempList;
            for (String nameIndex : nameAndDataList.keySet()) {
                Map<String, Object> totalMap = getTotalMap(outlettype, nameIndex);
                Double value1 = 0d;
                Double value2 = 0d;
                Double value3 = 0d;
                Double value4 = 0d;
                Double value5 = 0d;
                Double promiseconcentrationlimit = 0d;
                tempList = nameAndDataList.get(nameIndex);
                for (Map<String, Object> tempMap : tempList) {

                    value1 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue1") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue1").toString()) : 0d);


                    value2 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue2") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue2").toString()) : 0d);

                    value3 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue3") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue3").toString()) : 0d);

                    value4 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue4") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue4").toString()) : 0d);


                    value5 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue5") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue5").toString()) : 0d);

                    promiseconcentrationlimit = DataFormatUtil.addDouble(value1, tempMap.get("promiseconcentrationlimit") != null ?
                            Double.parseDouble(tempMap.get("promiseconcentrationlimit").toString()) : 0d);
                }
                totalMap.put("dischargelimitvalue1", value1);
                totalMap.put("dischargelimitvalue2", value2);
                totalMap.put("dischargelimitvalue3", value3);
                totalMap.put("dischargelimitvalue4", value4);
                totalMap.put("dischargelimitvalue5", value5);
                totalMap.put("promiseconcentrationlimit", promiseconcentrationlimit);
                dataList.add(totalMap);
            }
        }
        return dataList;
    }

    @Override
    public List<Map<String, Object>> getTotalDataListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = organizedGasDischargeLimitMapper.getDataListByParam(paramMap);
        if (dataList.size() > 0) {//设置总计数据
            Map<String, List<Map<String, Object>>> nameAndDataList = dataList.stream().filter(m -> m.get("pollutantname") != null).collect(Collectors.groupingBy(m -> m.get("pollutantname").toString()));
            List<Map<String, Object>> tempList;
            dataList.clear();
            for (String nameIndex : nameAndDataList.keySet()) {
                Map<String, Object> totalMap = new HashMap<>();
                Double value1 = 0d;
                Double value2 = 0d;
                Double value3 = 0d;
                Double value4 = 0d;
                Double value5 = 0d;
                Double promiseconcentrationlimit = 0d;
                tempList = nameAndDataList.get(nameIndex);
                for (Map<String, Object> tempMap : tempList) {
                    value1 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue1") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue1").toString()) : 0d);


                    value2 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue2") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue2").toString()) : 0d);

                    value3 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue3") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue3").toString()) : 0d);

                    value4 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue4") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue4").toString()) : 0d);


                    value5 = DataFormatUtil.addDouble(value1, tempMap.get("dischargelimitvalue5") != null ?
                            Double.parseDouble(tempMap.get("dischargelimitvalue5").toString()) : 0d);

                    promiseconcentrationlimit = DataFormatUtil.addDouble(value1, tempMap.get("promiseconcentrationlimit") != null ?
                            Double.parseDouble(tempMap.get("promiseconcentrationlimit").toString()) : 0d);
                }
                totalMap.put("pollutantname", nameIndex);
                totalMap.put("dischargelimitvalue1", value1);
                totalMap.put("dischargelimitvalue2", value2);
                totalMap.put("dischargelimitvalue3", value3);
                totalMap.put("dischargelimitvalue4", value4);
                totalMap.put("dischargelimitvalue5", value5);
                totalMap.put("promiseconcentrationlimit", promiseconcentrationlimit);
                dataList.add(totalMap);
            }
        }
        return dataList;
    }

    private Map<String, Object> getTotalMap(String outlettype, String pollutantname) {
        Map<String, Object> totalMap = new HashMap<>();
        totalMap.put("pk_id", UUID.randomUUID().toString());
        totalMap.put("outputcode", outlettype);
        totalMap.put("outputname", outlettype);
        totalMap.put("pollutantname", outlettype);
        totalMap.put("permithourconcentration", outlettype);
        totalMap.put("permitdayconcentration", outlettype);
        totalMap.put("permitrate", pollutantname);
        return totalMap;

    }


}

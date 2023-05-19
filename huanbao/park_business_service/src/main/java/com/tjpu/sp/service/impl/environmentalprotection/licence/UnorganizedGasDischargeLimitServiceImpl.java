package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.licence.UnorganizedGasDischargeLimitMapper;
import com.tjpu.sp.service.environmentalprotection.licence.UnorganizedGasDischargeLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class UnorganizedGasDischargeLimitServiceImpl implements UnorganizedGasDischargeLimitService {

    private final UnorganizedGasDischargeLimitMapper unorganizedGasDischargeLimitMapper;

    public UnorganizedGasDischargeLimitServiceImpl(UnorganizedGasDischargeLimitMapper unorganizedGasDischargeLimitMapper) {
        this.unorganizedGasDischargeLimitMapper = unorganizedGasDischargeLimitMapper;
    }


    @Override
    public List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = unorganizedGasDischargeLimitMapper.getDataListByParam(paramMap);
        if (dataList.size() > 0) {//设置总计数据

            Map<String, List<Map<String, Object>>> nameAndDataList = dataList.stream().filter(m -> m.get("pollutantname") != null).collect(Collectors.groupingBy(m -> m.get("pollutantname").toString()));
            List<Map<String, Object>> tempList;
            String outlettype = "全厂无组织排放总计";
            for (String nameIndex : nameAndDataList.keySet()) {
                Map<String, Object> totalMap = getTotalMap(outlettype, nameIndex);
                Double permitoneyear = 0d;
                Double permittwoyear = 0d;
                Double permitthreeyear = 0d;
                Double permitfouryear = 0d;
                Double permitfiveyear = 0d;
                Double specialtimelimitflow = 0d;
                tempList = nameAndDataList.get(nameIndex);
                for (Map<String, Object> tempMap : tempList) {

                    permitoneyear = DataFormatUtil.addDouble(tempMap.get("permitoneyear") != null ?
                            Double.parseDouble(tempMap.get("permitoneyear").toString()) : 0d, permitoneyear);


                    permittwoyear = DataFormatUtil.addDouble(tempMap.get("permittwoyear") != null ?
                            Double.parseDouble(tempMap.get("permittwoyear").toString()) : 0d, permittwoyear);

                    permitthreeyear = DataFormatUtil.addDouble(tempMap.get("permitthreeyear") != null ?
                            Double.parseDouble(tempMap.get("permitthreeyear").toString()) : 0d, permitthreeyear);


                    permitfouryear = DataFormatUtil.addDouble(tempMap.get("permitfouryear") != null ?
                            Double.parseDouble(tempMap.get("permitfouryear").toString()) : 0d, permitfouryear);

                    permitfiveyear = DataFormatUtil.addDouble(tempMap.get("permitfiveyear") != null ?
                            Double.parseDouble(tempMap.get("permitfiveyear").toString()) : 0d, permitfiveyear);


                    specialtimelimitflow = DataFormatUtil.addDouble(tempMap.get("specialtimelimitflow") != null ?
                            Double.parseDouble(tempMap.get("specialtimelimitflow").toString()) : 0d, specialtimelimitflow);
                }
                totalMap.put("permitoneyear", permitoneyear);
                totalMap.put("permittwoyear", permittwoyear);
                totalMap.put("permitthreeyear", permitthreeyear);
                totalMap.put("permitfouryear", permitfouryear);
                totalMap.put("permitfiveyear", permitfiveyear);
                totalMap.put("specialtimelimitflow", specialtimelimitflow);
                dataList.add(totalMap);
            }
        }
        return dataList;
    }

    /**
     * @Description: 挥发性有机物无组织排放量分类统计表
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/9 9:35
     */
    @Override
    public List<Map<String, Object>> getVolatilityDataListByParam(Map<String, Object> paramMap) {
        return unorganizedGasDischargeLimitMapper.getVolatilityDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTotalDataListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = unorganizedGasDischargeLimitMapper.getTotalDataListByParam(paramMap);
        if (dataList.size() > 0) {
            double totall;
            double total2;
            double total3;
            double total4;
            double total5;
            for (Map<String, Object> dataMap : dataList) {


                totall = DataFormatUtil.addDouble(
                        (dataMap.get("nooneyear") != null ? Double.parseDouble(dataMap.get("nooneyear").toString()) : 0D),
                        (dataMap.get("organizedoneyear") != null ? Double.parseDouble(dataMap.get("organizedoneyear").toString()) : 0D)
                );

                total2 = DataFormatUtil.addDouble(
                        (dataMap.get("notwoyear") != null ? Double.parseDouble(dataMap.get("notwoyear").toString()) : 0D),
                        (dataMap.get("organizedtwoyear") != null ? Double.parseDouble(dataMap.get("organizedtwoyear").toString()) : 0D)
                );
                total3 = DataFormatUtil.addDouble(
                        (dataMap.get("nothreeyear") != null ? Double.parseDouble(dataMap.get("nothreeyear").toString()) : 0D),
                        (dataMap.get("organizedthreeyear") != null ? Double.parseDouble(dataMap.get("organizedthreeyear").toString()) : 0D)
                );
                total4 = DataFormatUtil.addDouble(
                        (dataMap.get("nofouryear") != null ? Double.parseDouble(dataMap.get("nofouryear").toString()) : 0D),
                        (dataMap.get("organizedfouryear") != null ? Double.parseDouble(dataMap.get("organizedfouryear").toString()) : 0D)
                );
                total5 = DataFormatUtil.addDouble(
                        (dataMap.get("nofiveyear") != null ? Double.parseDouble(dataMap.get("nofiveyear").toString()) : 0D),
                        (dataMap.get("organizedfiveyear") != null ? Double.parseDouble(dataMap.get("organizedfiveyear").toString()) : 0D)
                );
                dataMap.put("total1", totall);
                dataMap.put("total2", total2);
                dataMap.put("total3", total3);
                dataMap.put("total4", total4);
                dataMap.put("total5", total5);
            }
        }
        return dataList;
    }


    private Map<String, Object> getTotalMap(String outlettype, String pollutantname) {
        Map<String, Object> totalMap = new HashMap<>();
        totalMap.put("pk_id", UUID.randomUUID().toString());
        totalMap.put("monitorpointcode", outlettype);
        totalMap.put("pollutionproductionname", outlettype);
        totalMap.put("pollutantname", outlettype);
        totalMap.put("pollutantpreventionmeasure", outlettype);
        totalMap.put("standardname", outlettype);
        totalMap.put("permithourconcentration", outlettype);
        totalMap.put("permitdayconcentration", pollutantname);
        totalMap.put("remark", pollutantname);
        return totalMap;

    }


}

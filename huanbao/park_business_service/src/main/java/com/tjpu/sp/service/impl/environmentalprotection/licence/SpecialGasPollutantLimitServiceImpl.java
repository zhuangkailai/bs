package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.licence.SpecialGasPollutantLimitMapper;
import com.tjpu.sp.service.environmentalprotection.licence.SpecialGasPollutantLimitService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class SpecialGasPollutantLimitServiceImpl implements SpecialGasPollutantLimitService {

    private final SpecialGasPollutantLimitMapper specialGasPollutantLimitMapper;

    public SpecialGasPollutantLimitServiceImpl(SpecialGasPollutantLimitMapper specialGasPollutantLimitMapper) {
        this.specialGasPollutantLimitMapper = specialGasPollutantLimitMapper;
    }


    @Override
    public List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = specialGasPollutantLimitMapper.getDataListByParam(paramMap);
        if (dataList.size() > 0) {//设置总计数据

            Map<String, List<Map<String, Object>>> nameAndDataList = dataList.stream().filter(m -> m.get("pollutantname") != null).collect(Collectors.groupingBy(m -> m.get("pollutantname").toString()));
            List<Map<String, Object>> tempList;
            for (String nameIndex : nameAndDataList.keySet()) {
                Map<String, Object> totalMap = new HashMap<>();
                Double permitlimit = 0d;
                Double permitdaylimit = 0d;
                Double permitmonthlimit = 0d;
                String permittimeinterval;
                List<String> temps = new ArrayList<>();
                tempList = nameAndDataList.get(nameIndex);
                for (Map<String, Object> tempMap : tempList) {

                    permitlimit = DataFormatUtil.addDouble(permitlimit, tempMap.get("permitlimit") != null ?
                            Double.parseDouble(tempMap.get("permitlimit").toString()) : 0d);

                    permitdaylimit = DataFormatUtil.addDouble(permitdaylimit, tempMap.get("permitdaylimit") != null ?
                            Double.parseDouble(tempMap.get("permitdaylimit").toString()) : 0d);

                    permitmonthlimit = DataFormatUtil.addDouble(permitmonthlimit, tempMap.get("permitmonthlimit") != null ?
                            Double.parseDouble(tempMap.get("permitmonthlimit").toString()) : 0d);

                    if (tempMap.get("permittimeinterval") != null && !"".equals(tempMap.get("permittimeinterval"))) {
                        permittimeinterval = tempMap.get("permittimeinterval").toString();
                        if (!temps.contains(permittimeinterval)) {
                            temps.add(permittimeinterval);
                        }
                    }
                }
                totalMap.put("permitlimit", permitlimit);
                totalMap.put("permitdaylimit", permitdaylimit);
                totalMap.put("permitmonthlimit", permitmonthlimit);
                totalMap.put("permittimeinterval", DataFormatUtil.FormatListToString(temps, "、"));
                totalMap.put("pollutantname", nameIndex);
                totalMap.put("outlettype", "全厂合计");
                dataList.add(totalMap);
            }
        }
        return dataList;
    }


}

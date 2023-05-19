package com.tjpu.sp.service.impl.environmentalprotection.particularpollutants;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.dao.common.FileInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.assess.EntAssessmentDataMapper;
import com.tjpu.sp.dao.environmentalprotection.assess.EntAssessmentInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.particularpollutants.EntGasPollutantMapper;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentDataVO;
import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentInfoVO;
import com.tjpu.sp.model.environmentalprotection.particularpollutants.EntGasPollutantVO;
import com.tjpu.sp.service.environmentalprotection.assess.EntAssessScoreService;
import com.tjpu.sp.service.environmentalprotection.particularpollutants.EntGasPollutantService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class EntGasPollutantServiceImpl implements EntGasPollutantService {
    @Autowired
    private EntGasPollutantMapper entGasPollutantMapper;


    /**
     * @Description: 列表查询
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 11:34
     */
    @Override
    public  Map<String, Object> getDataListByParam(JSONObject jsonObject) {
        Map<String, Object> resultMap = new HashMap<>();
        //取数据


        List<Map<String, Object>> dataList = entGasPollutantMapper.getDataListByParam(jsonObject);
        //合并数据
        if (dataList.size() > 0) {
            Map<String, List<Map<String, Object>>> idAndDataList = dataList.stream().collect(Collectors.groupingBy(m -> m.get("pk_pollutionid").toString()));
            dataList.clear();
            List<Map<String, Object>> tempList;
            List<String> pollutantnames;
            List<String> pollutantcodes;
            for (String idIndex : idAndDataList.keySet()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("pollutionid", idIndex);
                tempList = idAndDataList.get(idIndex);
                pollutantnames = new ArrayList<>();
                pollutantcodes = new ArrayList<>();
                for (Map<String, Object> map : tempList) {
                    dataMap.putIfAbsent("pollutionname", map.get("pollutionname"));
                    dataMap.putIfAbsent("updatetime", map.get("updatetime"));
                    dataMap.putIfAbsent("updateuser", map.get("updateuser"));
                    pollutantnames.add(map.get("name") + "");
                    pollutantcodes.add(map.get("code") + "");
                }
                pollutantnames = pollutantnames.stream().distinct().collect(Collectors.toList());
                pollutantcodes = pollutantcodes.stream().distinct().collect(Collectors.toList());
                dataMap.put("pollutantnames", pollutantnames);
                dataMap.put("pollutantcodes", pollutantcodes);
                dataList.add(dataMap);
            }
            //排序+分页
            dataList = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("pollutionname").toString())).collect(Collectors.toList());
            if (jsonObject.get("pagesize") != null && jsonObject.get("pagenum") != null) {
                resultMap.put("total", dataList.size());
                int pagenum = Integer.parseInt(jsonObject.get("pagenum").toString());
                int pagesize = Integer.parseInt(jsonObject.get("pagesize").toString());
                dataList = MongoDataUtils.getPageData(dataList, pagenum, pagesize);
                resultMap.put("datalist", dataList);
            } else {
                resultMap.put("datalist", dataList);
            }
        }
        return resultMap;
    }

    @Override
    public void updateOrAddData(String pollutionId, List<EntGasPollutantVO> entGasPollutantVOS) {
        //删除
        entGasPollutantMapper.deleteByFId(pollutionId);
        for (EntGasPollutantVO entGasPollutantVO:entGasPollutantVOS){
            entGasPollutantMapper.insert(entGasPollutantVO);
        }
    }





    /**
     * @Description: 删除信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 12:04
     */
    @Override
    public void deleteById(String pollutionId) {
        entGasPollutantMapper.deleteByFId(pollutionId);
    }


    @Override
    public Map<String, Object> getEditOrViewDataById(String id) {
        Map<String, Object> resultMap = new HashMap<>();

        return resultMap;
    }
}

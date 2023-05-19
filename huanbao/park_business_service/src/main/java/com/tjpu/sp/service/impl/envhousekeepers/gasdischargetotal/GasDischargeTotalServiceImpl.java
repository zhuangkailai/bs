package com.tjpu.sp.service.impl.envhousekeepers.gasdischargetotal;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.envhousekeepers.gasdischargetotal.GasDischargeTotalMapper;
import com.tjpu.sp.dao.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueMapper;
import com.tjpu.sp.model.envhousekeepers.gasdischargetotal.GasDischargeTotalVO;
import com.tjpu.sp.service.envhousekeepers.gasdischargetotal.GasDischargeTotalService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class GasDischargeTotalServiceImpl implements GasDischargeTotalService {
    @Autowired
    private GasDischargeTotalMapper gasDischargeTotalMapper;
    @Autowired
    private EntPermittedFlowLimitValueMapper entPermittedFlowLimitValueMapper;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private String yzztype = "1";
    private String wzztype = "2";

    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 通过自定义参数获取企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getGasDischargeTotalByParamMap(Map<String, Object> paramMap) {
        List<Map<String,Object>> listdata = new ArrayList<>();
        //获取企业排放许可信息
        List<Map<String,Object>> result = gasDischargeTotalMapper.getGasDischargeTotalByParamMap(paramMap);
        if (result!=null&&result.size()>0) {
            //按污染物分组
            Map<String, List<Map<String, Object>>> listMap = result.stream().collect(Collectors.groupingBy(m -> m.get("fkpollutantcode").toString()));
            //按年份的排序
            List<Map<String, Object>> chlidren = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("Year").toString())
                    )).collect(Collectors.toList());
            //按年份分组
            Map<String, List<Map<String, Object>>> yearMap = chlidren.stream().collect(Collectors.groupingBy(m -> m.get("Year").toString()));
            for(Map.Entry<String, List<Map<String, Object>>> entry:listMap.entrySet()){
                Map<String,Object> map = new HashMap<>();
                map.put("fkpollutantcode",entry.getKey());
                List<Map<String, Object>> listone = entry.getValue();
                if (listone!=null&&listone.get(0)!=null&&listone.get(0).get("pollutantname")!=null) {
                    map.put("pollutantname", listone.get(0).get("pollutantname"));
                }else{
                    map.put("pollutantname","-");
                }
                for(Map.Entry<String, List<Map<String, Object>>> obj:yearMap.entrySet()){
                    map.put("yzzyear_"+obj.getKey(),"/");
                    map.put("wzzyear_"+obj.getKey(),"/");
                    map.put("sumyear_"+obj.getKey(),"/");
                    List<Map<String, Object>> objlist = obj.getValue();
                    Double total = 0d;
                    for (Map<String, Object> onemap:objlist){
                        if (onemap.get("fkpollutantcode")!=null&&entry.getKey().equals(onemap.get("fkpollutantcode").toString())) {
                            //有组织
                            if (onemap.get("CountType") != null && yzztype.equals(onemap.get("CountType").toString())) {
                                if (onemap.get("DischargeValue")!=null&&!"".equals(onemap.get("DischargeValue").toString())){
                                    map.put("yzzyear_"+obj.getKey(),Double.valueOf(onemap.get("DischargeValue").toString()));
                                    total +=  Double.valueOf(onemap.get("DischargeValue").toString());
                                }
                            }
                            //无组织
                            if (onemap.get("CountType") != null && wzztype.equals(onemap.get("CountType").toString())) {
                                if (onemap.get("DischargeValue")!=null&&!"".equals(onemap.get("DischargeValue").toString())){
                                    map.put("wzzyear_"+obj.getKey(),Double.valueOf(onemap.get("DischargeValue").toString()));
                                    total +=  Double.valueOf(onemap.get("DischargeValue").toString());
                                }
                            }
                        }
                    }
                    if (total>0) {
                        map.put("sumyear_" + obj.getKey(), total);
                    }

                }
                listdata.add(map);
            }
        }
        return listdata;
    }


    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 根据主键ID删除企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void deleteByPollutionIDAndPollutantCode(Map<String, Object> param) {
        gasDischargeTotalMapper.deleteByPollutionIDAndPollutantCode(param);
    }

    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 添加企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void insert(List<GasDischargeTotalVO> listobj) {
        if (listobj!=null&&listobj.size()>0) {
            gasDischargeTotalMapper.batchInsert(listobj);
        }
    }

    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 更新企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateByPrimaryKey(String pollutionid, String pollutantcode, List<GasDischargeTotalVO> listobj) {
        Map<String,Object> param = new HashMap<>();
        param.put("pollutionid",pollutionid);
        param.put("pollutantcode",pollutantcode);
        gasDischargeTotalMapper.deleteByPollutionIDAndPollutantCode(param);
        if (listobj!=null&&listobj.size()>0) {
            gasDischargeTotalMapper.batchInsert(listobj);
        }
    }

    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 根据污染源ID和污染物编码获取企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getGasDischargeTotalsByParam(Map<String, Object> param) {
        List<Map<String,Object>> listdata = new ArrayList<>();
        //获取企业排放许可信息
        List<Map<String,Object>> result = gasDischargeTotalMapper.getGasDischargeTotalByParamMap(param);
        if (result!=null&&result.size()>0) {
            //按年份的排序
            List<Map<String, Object>> chlidren = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("Year").toString())
                    )).collect(Collectors.toList());
            //按年份分组
            Map<String, List<Map<String, Object>>> yearMap = chlidren.stream().collect(Collectors.groupingBy(m -> m.get("Year").toString()));
                for(Map.Entry<String, List<Map<String, Object>>> obj:yearMap.entrySet()){
                    Map<String,Object> map = new HashMap<>();
                    map.put("year",obj.getKey());
                    List<Map<String, Object>> objlist = obj.getValue();
                    map.put("yzz_dischargevalue",null);
                    map.put("wzz_dischargevalue",null);
                    for (Map<String, Object> onemap:objlist){
                            //有组织
                            if (onemap.get("CountType") != null && yzztype.equals(onemap.get("CountType").toString())) {
                                if (onemap.get("DischargeValue")!=null&&!"".equals(onemap.get("DischargeValue").toString())){
                                    map.put("yzz_dischargevalue",Double.valueOf(onemap.get("DischargeValue").toString()));

                                }
                            }
                            //无组织
                            if (onemap.get("CountType") != null && wzztype.equals(onemap.get("CountType").toString())) {
                                if (onemap.get("DischargeValue")!=null&&!"".equals(onemap.get("DischargeValue").toString())){
                                    map.put("wzz_dischargevalue",Double.valueOf(onemap.get("DischargeValue").toString()));

                                }
                            }
                        }
                    listdata.add(map);
                }
        }
        return listdata;
    }

    /**
     *
     * @author: xsm
     * @date: 2021/08/17 0017 上午 10:03
     * @Description: 获取企业大气污染总排放许可量初始化信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getGasDischargeTotalListPage(Map<String, Object> param) {
        Map<String, Object> resultmap = new HashMap<>();
        List<Map<String,Object>> listdata = new ArrayList<>();
        //获取企业排放许可信息
        List<Map<String,Object>> result = gasDischargeTotalMapper.getGasDischargeTotalByParamMap(param);
        List<Map<String, Object>> titlelist = new ArrayList<>();
        titlelist = getFlowTitlelist(result);
        if (result!=null&&result.size()>0) {
            //按污染物分组
            Map<String, List<Map<String, Object>>> listMap = result.stream().collect(Collectors.groupingBy(m -> m.get("fkpollutantcode").toString()));
            //按年份的排序
            List<Map<String, Object>> chlidren = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("Year").toString())
                    )).collect(Collectors.toList());
            //按年份分组
            Map<String, List<Map<String, Object>>> yearMap = chlidren.stream().collect(Collectors.groupingBy(m -> m.get("Year").toString()));
            for(Map.Entry<String, List<Map<String, Object>>> entry:listMap.entrySet()){
                Map<String,Object> map = new HashMap<>();
                map.put("fkpollutantcode",entry.getKey());
                List<Map<String, Object>> listone = entry.getValue();
                if (listone!=null&&listone.get(0)!=null&&listone.get(0).get("pollutantname")!=null) {
                    map.put("pollutantname", listone.get(0).get("pollutantname"));
                }else{
                    map.put("pollutantname","-");
                }
                for(Map.Entry<String, List<Map<String, Object>>> obj:yearMap.entrySet()){
                    map.put("yzzyear_"+obj.getKey(),"/");
                    map.put("wzzyear_"+obj.getKey(),"/");
                    map.put("sumyear_"+obj.getKey(),"/");
                    List<Map<String, Object>> objlist = obj.getValue();
                    Double total = 0d;
                    for (Map<String, Object> onemap:objlist){
                        if (onemap.get("fkpollutantcode")!=null&&entry.getKey().equals(onemap.get("fkpollutantcode").toString())) {
                            //有组织
                            if (onemap.get("CountType") != null && yzztype.equals(onemap.get("CountType").toString())) {
                                if (onemap.get("DischargeValue")!=null&&!"".equals(onemap.get("DischargeValue").toString())){
                                    map.put("yzzyear_"+obj.getKey(),Double.valueOf(onemap.get("DischargeValue").toString()));
                                    total +=  Double.valueOf(onemap.get("DischargeValue").toString());
                                }
                            }
                            //无组织
                            if (onemap.get("CountType") != null && wzztype.equals(onemap.get("CountType").toString())) {
                                if (onemap.get("DischargeValue")!=null&&!"".equals(onemap.get("DischargeValue").toString())){
                                    map.put("wzzyear_"+obj.getKey(),Double.valueOf(onemap.get("DischargeValue").toString()));
                                    total +=  Double.valueOf(onemap.get("DischargeValue").toString());
                                }
                            }
                        }
                    }
                    if (total>0) {
                        map.put("sumyear_" + obj.getKey(), total);
                    }

                }
                listdata.add(map);
            }
        }
        resultmap.put("datalist",listdata);
        resultmap.put("titlelist",titlelist);
        return resultmap;
    }

    @Override
    public List<Map<String, Object>> IsHaveGasPollutantFlowYearValidByParam(Map<String, Object> paramMap) {
        return gasDischargeTotalMapper.getGasDischargeTotalByParamMap(paramMap);
    }

    private List<Map<String,Object>> getFlowTitlelist(List<Map<String, Object>> result) {
        List<Map<String, Object>> titlelist = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("label", "污染物种类");
        map1.put("prop", "pollutantname");
        map1.put("minwidth", "150px");
        map1.put("headeralign", "center");
        map1.put("fixed", "left");
        map1.put("align", "center");
        map1.put("showhide", true);
        titlelist.add(map1);
        Map<String, Object> map2 = new HashMap<>();//有组织
        map2.put("label", "全厂有组织排放总计(t/a)");
        map2.put("prop", "yzz");
        map2.put("minwidth", "150px");
        map2.put("headeralign", "center");
        map2.put("align", "center");
        map2.put("showhide", true);
        Map<String, Object> map3 = new HashMap<>();//无组织
        map3.put("label", "全厂无组织排放总计(t/a)");
        map3.put("prop", "wzz");
        map3.put("minwidth", "150px");
        map3.put("headeralign", "center");
        map3.put("align", "center");
        map3.put("showhide", true);
        Map<String, Object> map4 = new HashMap<>();//全厂
        map4.put("label", "全厂合计(t/a)");
        map4.put("prop", "sum");
        map4.put("minwidth", "150px");
        map4.put("headeralign", "center");
        map4.put("align", "center");
        map4.put("showhide", true);
        List<Map<String, Object>> chlidheader1 = new ArrayList<>();
        List<Map<String, Object>> chlidheader2 = new ArrayList<>();
        List<Map<String, Object>> chlidheader3 = new ArrayList<>();
        if (result!=null&&result.size()>0){
            //按年份的排序
            List<Map<String, Object>> chlidren = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("Year").toString())
                    )).collect(Collectors.toList());
            //按年份分组
            List<String> years = new ArrayList<>();
            for(Map<String, Object> onemap:chlidren){
                if (onemap.get("Year")!=null&&!years.contains(onemap.get("Year").toString())){
                    years.add(onemap.get("Year").toString());
                }else{
                    continue;
                }
            }
            for(String year:years) {
                Map<String, Object> map5 = new HashMap<>();//有组织
                map5.put("label", year+"年");
                map5.put("prop", "yzzyear_"+ year);
                map5.put("minwidth", "100px");
                map5.put("headeralign", "center");
                map5.put("align", "center");
                map5.put("showhide", true);
                chlidheader1.add(map5);
                Map<String, Object> map6 = new HashMap<>();//无组织
                map6.put("label",  year+"年");
                map6.put("prop", "wzzyear_"+ year);
                map6.put("minwidth", "100px");
                map6.put("headeralign", "center");
                map6.put("align", "center");
                map6.put("showhide", true);
                chlidheader2.add(map6);
                Map<String, Object> map7 = new HashMap<>();//全厂
                map7.put("label",  year+"年");
                map7.put("prop", "sumyear_"+ year);
                map7.put("minwidth", "100px");
                map7.put("headeralign", "center");
                map7.put("align", "center");
                map7.put("showhide", true);
                chlidheader3.add(map7);
            }
        }/*else{//无数据 默认显示当前年 及后四年数据
            //获取当前年份
            Calendar cale = Calendar.getInstance();
            int newyear = cale.get(Calendar.YEAR);
            for (int i=0;i<5;i++){
                newyear = i+newyear;
                Map<String, Object> map5 = new HashMap<>();//有组织
                map5.put("label", newyear+"年");
                map5.put("prop", "yzzyear_"+ newyear);
                map5.put("minwidth", "100px");
                map5.put("headeralign", "center");
                map5.put("align", "center");
                map5.put("showhide", true);
                chlidheader1.add(map5);
                Map<String, Object> map6 = new HashMap<>();//无组织
                map6.put("label",  newyear+"年");
                map6.put("prop", "wzzyear_"+ newyear);
                map6.put("minwidth", "100px");
                map6.put("headeralign", "center");
                map6.put("align", "center");
                map6.put("showhide", true);
                chlidheader2.add(map6);
                Map<String, Object> map7 = new HashMap<>();//全厂
                map7.put("label",  newyear+"年");
                map7.put("prop", "sumyear_"+ newyear);
                map7.put("minwidth", "100px");
                map7.put("headeralign", "center");
                map7.put("align", "center");
                map7.put("showhide", true);
                chlidheader3.add(map7);
            }
        }*/
        map2.put("children", chlidheader1);
        titlelist.add(map2);
        map3.put("children", chlidheader2);
        titlelist.add(map3);
        map4.put("children", chlidheader3);
        titlelist.add(map4);
        return titlelist;
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 通过自定义参数查询企业近五年各排放污染物污染总排放许可量信息（废气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @Override
    public Map<String, Object> getEntGasDischargeTotalListData(Map<String, Object> param) {
        Map<String,Object> resultmap = new HashMap<>();
        List<Map<String,Object>> listdata = new ArrayList<>();
        int startyear = Integer.valueOf(param.get("startyear").toString());
        int endyear = Integer.valueOf(param.get("endyear").toString());
        //获取企业废气排放许可信息
        List<Map<String,Object>> result = gasDischargeTotalMapper.getGasDischargeTotalByParamMap(param);
        List<Map<String, Object>> titlelist = new ArrayList<>();
        titlelist = getFiveYearGasFlowTitlelist(startyear,endyear);
        if (result!=null&&result.size()>0) {
            //按污染物分组
            Map<String, List<Map<String, Object>>> listMap = result.stream().collect(Collectors.groupingBy(m -> m.get("fkpollutantcode").toString()));
            for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("fkpollutantcode", entry.getKey());
                List<Map<String, Object>> listone = entry.getValue();
                if (listone != null && listone.get(0) != null && listone.get(0).get("pollutantname") != null) {
                    map.put("pollutantname", listone.get(0).get("pollutantname"));
                } else {
                    map.put("pollutantname", "-");
                }
                for (int i = startyear; i <= endyear; i++) {
                    Double total = 0d;
                    for (Map<String, Object> onemap : listone) {
                        //年份相同
                        if (onemap.get("Year") != null && (i + "").equals(onemap.get("Year").toString())) {
                            //有组织
                            if (onemap.get("CountType") != null && !wzztype.equals(onemap.get("CountType").toString())) {
                                if (onemap.get("DischargeValue") != null && !"".equals(onemap.get("DischargeValue").toString())) {
                                    total += Double.valueOf(onemap.get("DischargeValue").toString());
                                }
                            }
                        }
                    }
                    map.put("total_" + i, total>0?DataFormatUtil.SaveTwoAndSubZero(total)+"":"-");
                }
                listdata.add(map);
            }
        }
        resultmap.put("datalist",listdata);
        resultmap.put("titlelist",titlelist);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 近五年各排放污染物污染总排放许可量信息表头（废气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    private List<Map<String,Object>> getFiveYearGasFlowTitlelist(Integer startyear,Integer endyear) {
        List<Map<String, Object>> titlelist = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("label", "污染物种类");
        map1.put("prop", "pollutantname");
        map1.put("minwidth", "150px");
        map1.put("headeralign", "center");
        map1.put("fixed", "left");
        map1.put("align", "center");
        map1.put("showhide", true);
        titlelist.add(map1);
        Map<String, Object> map2 = new HashMap<>();//有组织
        map2.put("label", "有组织排放总计(t/a)");
        map2.put("prop", "yzz");
        map2.put("minwidth", "150px");
        map2.put("headeralign", "center");
        map2.put("align", "center");
        map2.put("showhide", true);
        List<Map<String, Object>> chlidheader1 = new ArrayList<>();
        for(int year = startyear;year<=endyear;year++) {
                Map<String, Object> map5 = new HashMap<>();//有组织
                map5.put("label", year+"年");
                map5.put("prop", "total_"+ year);
                map5.put("minwidth", "100px");
                map5.put("headeralign", "center");
                map5.put("align", "center");
                map5.put("showhide", true);
                chlidheader1.add(map5);
            }
        map2.put("children", chlidheader1);
        titlelist.add(map2);

        return titlelist;
    }

    @Override
    public Map<String, Object> getEntGasDischargeTotalAnalysisData(Map<String, Object> param) {
        Map<String,Object> resultmap = new HashMap<>();
        List<Map<String,Object>> listdata = new ArrayList<>();
        int startyear = Integer.valueOf(param.get("startyear").toString());
        int endyear = Integer.valueOf(param.get("endyear").toString());
        List<Integer> types = Arrays.asList(WasteGasEnum.getCode(), SmokeEnum.getCode());
        param.put("monitorpointtypes",types);
        //获取企业废气排放许可信息
        List<Map<String,Object>> result = gasDischargeTotalMapper.getGasDischargeTotalByParamMap(param);
        List<Map<String, Object>> titlelist = new ArrayList<>();
        titlelist = getFiveYearGasFlowAnalysisTitlelist(startyear,endyear);
        List<String> mns = new ArrayList<>();
        List<String> pollutants = new ArrayList<>();
        Map<String,Object> codeandname = new HashMap<>();
        //获取该企业所有废气烟气点位
        List<Map<String, Object>> allmns = entPermittedFlowLimitValueMapper.getEntAllOutputData(param);
        mns = allmns.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
        //获取点位监测的污染物信息
        List<Map<String, Object>> allpos = entPermittedFlowLimitValueMapper.getEntOutPutPollutantData(param);
        for (Map<String, Object> map:allpos){
            if (map.get("pollutantcode")!=null) {
                pollutants.add(map.get("pollutantcode").toString());
                codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
            }
        }
        pollutants = pollutants.stream().distinct().collect(Collectors.toList());
        Map<String, List<Map<String, Object>>> listMap = new HashMap<>();
        if (result!=null&&result.size()>0) {
            //按污染物分组
            listMap = result.stream().collect(Collectors.groupingBy(m -> m.get("fkpollutantcode").toString()));
        }
        Criteria criteria = Criteria.where("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startyear + "-01-01 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endyear + "-12-31 23:59:59")).and("DataGatherCode").in(mns).and("PollutantCode").in(pollutants);
        Aggregation aggregation = newAggregation(
                unwind("YearFlowDataList"),
                project("DataGatherCode", "MonitorTime", "YearFlowDataList.PollutantCode", "YearFlowDataList.PollutantFlow").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("YearTime"),
                match(criteria),
                project("DataGatherCode", "YearTime", "PollutantCode", "PollutantFlow").andExclude("_id")
        );
        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, "YearFlowData", Document.class).getMappedResults();
            for (String str : pollutants) {
                Map<String, Object> map = new HashMap<>();
                map.put("pollutantcode", str);
                map.put("pollutantname", codeandname.get(str));
                for (int year =startyear;year<=endyear;year++){
                    Double sj_num = 0d;//实际排放量
                    Double xk_num = 0d;//许可排放量
                    Double change_num = 0d;//变化排放量
                    List<Map<String, Object>> listone = new ArrayList<>();
                    if (mappedResults != null && mappedResults.size() > 0) {
                        for (Document document : mappedResults) {
                            if (str.equals(document.getString("PollutantCode")) && (year + "").equals(document.getString("YearTime"))) {//两个污染物相等 年份相等
                                if (document.get("PollutantFlow") != null) {
                                    sj_num += Double.parseDouble(document.getString("PollutantFlow"));
                                }
                            }
                        }
                    }
                    if (listMap.get(str)!=null){
                       listone = listMap.get(str);
                        for (Map<String, Object> onemap : listone) {
                            //年份相同
                            if (onemap.get("Year") != null && (year + "").equals(onemap.get("Year").toString())) {
                                //有组织
                                if (onemap.get("CountType") != null && !wzztype.equals(onemap.get("CountType").toString())) {
                                    if (onemap.get("DischargeValue") != null && !"".equals(onemap.get("DischargeValue").toString())) {
                                        xk_num += Double.valueOf(onemap.get("DischargeValue").toString());
                                    }
                                }
                            }
                        }
                    }
                    map.put("sj_"+year, sj_num>0?DataFormatUtil.SaveTwoAndSubZero(sj_num)+"":"-");
                    map.put("xk_"+year, xk_num>0?DataFormatUtil.SaveTwoAndSubZero(xk_num)+"":"-");
                    map.put("change_"+year, "-");
                    if (sj_num>0&&xk_num>0){
                        change_num = sj_num - xk_num;
                        if (change_num>0){
                            map.put("change_"+year, DataFormatUtil.SaveTwoAndSubZero(change_num)+"");
                        }else if (change_num<0){
                            map.put("change_"+year, DataFormatUtil.SaveTwoAndSubZero(change_num)+"");
                        }
                    }
                }
                    listdata.add(map);
                }
        resultmap.put("datalist",listdata);
        resultmap.put("titlelist",titlelist);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 近五年各排放污染物污染总排放许可量信息表头（废气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    private List<Map<String,Object>> getFiveYearGasFlowAnalysisTitlelist(Integer startyear,Integer endyear) {
        List<Map<String, Object>> titlelist = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("label", "污染物种类");
        map1.put("prop", "pollutantname");
        map1.put("minwidth", "150px");
        map1.put("headeralign", "center");
        map1.put("fixed", "left");
        map1.put("align", "center");
        map1.put("showhide", true);
        titlelist.add(map1);
        Map<String, Object> map2 = new HashMap<>();//有组织
        map2.put("label", "有组织年排放情况(t/a)");
        map2.put("prop", "yzz");
        map2.put("minwidth", "150px");
        map2.put("headeralign", "center");
        map2.put("align", "center");
        map2.put("showhide", true);
        List<Map<String, Object>> chlidheader1 = new ArrayList<>();
        for(int year = startyear;year<=endyear;year++) {
            Map<String, Object> map5 = new HashMap<>();//有组织
            map5.put("label", year+"年");
            map5.put("minwidth", "100px");
            map5.put("headeralign", "center");
            map5.put("align", "center");
            map5.put("showhide", true);
            List<Map<String, Object>> chlidheader2 = new ArrayList<>();
            Map<String, Object> map6 = new HashMap<>();//有组织
            map6.put("label", "实际排放量(t)");
            map6.put("prop", "sj_"+ year);
            map6.put("minwidth", "100px");
            map6.put("headeralign", "center");
            map6.put("align", "center");
            map6.put("showhide", true);
            chlidheader2.add(map6);
            Map<String, Object> map7 = new HashMap<>();//有组织
            map7.put("label", "许可排放量(t)");
            map7.put("prop", "xk_"+ year);
            map7.put("minwidth", "100px");
            map7.put("headeralign", "center");
            map7.put("align", "center");
            map7.put("showhide", true);
            chlidheader2.add(map7);
            Map<String, Object> map8 = new HashMap<>();//有组织
            map8.put("label", "变化幅度(t)");
            map8.put("prop", "change_"+ year);
            map8.put("type", "percentage");
            map8.put("minwidth", "100px");
            map8.put("headeralign", "center");
            map8.put("align", "center");
            map8.put("showhide", true);
            chlidheader2.add(map8);
            map5.put("children", chlidheader2);
            chlidheader1.add(map5);
        }
        map2.put("children", chlidheader1);
        titlelist.add(map2);
        return titlelist;
    }
}

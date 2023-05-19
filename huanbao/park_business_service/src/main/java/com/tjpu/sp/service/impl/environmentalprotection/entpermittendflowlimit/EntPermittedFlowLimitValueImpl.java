package com.tjpu.sp.service.impl.environmentalprotection.entpermittendflowlimit;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueMapper;
import com.tjpu.sp.model.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueVO;
import com.tjpu.sp.service.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueService;
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

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class EntPermittedFlowLimitValueImpl implements EntPermittedFlowLimitValueService {

    @Autowired
    private EntPermittedFlowLimitValueMapper entPermittedFlowLimitValueMapper;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 上午 8:39
     * @Description:  通过污染源，监测点类型，排放年限查询企业许可排放限值（回显）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> selectByParams(Map<String, Object> paramMap) {
        return entPermittedFlowLimitValueMapper.selectByParams(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 2:17
     * @Description: 通过自定义参数获取企业许可排放限值信息（列表）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<EntPermittedFlowLimitValueVO> getEntPermittedFlowLimitInfoByParamMap(Map<String, Object> paramMap) {
        return entPermittedFlowLimitValueMapper.getEntPermittedFlowLimitInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 2:58
     * @Description: 新增数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int insert(EntPermittedFlowLimitValueVO record) {
        return entPermittedFlowLimitValueMapper.insert(record);
    }


    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 3:28
     * @Description: 通过主键查询企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public EntPermittedFlowLimitValueVO selectByPrimaryKey(String pkId) {
        return entPermittedFlowLimitValueMapper.selectByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 3:34
     * @Description: 修改企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int updateByPrimaryKey(EntPermittedFlowLimitValueVO record) {
        return entPermittedFlowLimitValueMapper.updateByPrimaryKey(record);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 3:35
     * @Description: 通过主键id删除企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkId) {
        return entPermittedFlowLimitValueMapper.deleteByPrimaryKey(pkId);
    }

    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 3:59
     * @Description: 根据年份和监测点类型获取企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     */
    @Override
    public List<Map<String, Object>> getEntPermittedFlowLimitInfoByYearAndType(Map<String, Object> paramMap) {
        return entPermittedFlowLimitValueMapper.getEntPermittedFlowLimitInfoByYearAndType(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 4:19
     * @Description: 根据年份和类型获取配置有排放量许可预警值的所有企业下排口的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     */
    @Override
    public List<Map<String, Object>> getAllDgimnsByYearAndType(Map<String, Object> paramMap) {
        List<Map<String, Object>> mnlist = new ArrayList<>();
        Object type = paramMap.get("monitorpointtype");
        //获取各污染类型下各排口信息和污染物信息
        if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) || type.equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode())) {//废气
            mnlist = entPermittedFlowLimitValueMapper.getGasDgimnsByYearAndType(paramMap);
        } else if (type.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode())) {//废水
            mnlist = entPermittedFlowLimitValueMapper.getWaterDgimnsByYearAndType(paramMap);
        }
        return mnlist;
    }

    /**
     * @author: xsm
     * @date: 2021/08/13 0013 上午 9:24
     * @Description: 获取某企业下单个污染物某一年的许可排放情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     */
    @Override
    public Map<String, Object> getOnePollutantPermitFlowDataByParam(Map<String, Object> param) {
        return entPermittedFlowLimitValueMapper.getOnePollutantPermitFlowDataByParam(param);
    }

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 8:41
     * @Description: 获取企业废水排放许可信息（新、一厂一档）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     */
    @Override
    public List<Map<String, Object>> getEntWaterPermittedFlowLimitInfoByParamMap(Map<String, Object> param) {
        List<Map<String,Object>> listdata = new ArrayList<>();
        //获取企业排放许可信息
        List<Map<String,Object>> result = entPermittedFlowLimitValueMapper.getEntWaterPermittedFlowLimitInfoByParamMap(param);
        if (result!=null&&result.size()>0) {
            //按污染物分组
            Map<String, List<Map<String, Object>>> listMap = result.stream().collect(Collectors.groupingBy(m -> m.get("fkpollutantcode").toString()));
            //按年份的排序
            List<Map<String, Object>> chlidren = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("FlowYear").toString())
                    )).collect(Collectors.toList());
            //按年份分组
            Map<String, List<Map<String, Object>>> yearMap = chlidren.stream().collect(Collectors.groupingBy(m -> m.get("FlowYear").toString()));
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
                    map.put("sqpfyear_"+obj.getKey(),"/");
                    List<Map<String, Object>> objlist = obj.getValue();
                    for (Map<String, Object> onemap:objlist){
                        if (onemap.get("fkpollutantcode")!=null&&entry.getKey().equals(onemap.get("fkpollutantcode").toString())) {
                                if (onemap.get("TotalFlow")!=null&&!"".equals(onemap.get("TotalFlow").toString())){
                                    map.put("sqpfyear_"+obj.getKey(),Double.valueOf(onemap.get("TotalFlow").toString()));
                                }
                        }
                    }
                }
                listdata.add(map);
            }
        }
        return listdata;

    }

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 8:41
     * @Description: 新增企业废水排放许可信息（新、一厂一档）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     */
    @Override
    public void addEntWaterPermittedFlowLimitInfo(List<EntPermittedFlowLimitValueVO> listobj) {
        if (listobj!=null&&listobj.size()>0) {
            entPermittedFlowLimitValueMapper.batchInsert(listobj);
        }
    }

    @Override
    public void deleteEntWaterFlowInfoByIDAndCode(Map<String, Object> param) {
        entPermittedFlowLimitValueMapper.deleteEntWaterFlowInfoByIDAndCode(param);
    }

    @Override
    public void updateEntWaterPermittedFlowLimitInfo(String pollutionid, String pollutantcode, List<EntPermittedFlowLimitValueVO> listobj) {
        Map<String,Object> param = new HashMap<>();
        param.put("pollutionid",pollutionid);
        param.put("pollutantcode",pollutantcode);
        entPermittedFlowLimitValueMapper.deleteEntWaterFlowInfoByIDAndCode(param);
        if (listobj!=null&&listobj.size()>0) {
            entPermittedFlowLimitValueMapper.batchInsert(listobj);
        }
    }

    @Override
    public List<Map<String, Object>> getEntWaterPermittedFlowInfoByIDAndCode(Map<String, Object> param) {
        List<Map<String,Object>> listdata = new ArrayList<>();
        //获取企业排放许可信息
        List<Map<String,Object>> result = entPermittedFlowLimitValueMapper.getEntWaterPermittedFlowLimitInfoByParamMap(param);
        if (result!=null&&result.size()>0) {
            //按年份的排序
            List<Map<String, Object>> chlidren = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("FlowYear").toString())
                    )).collect(Collectors.toList());
            //按年份分组
            Map<String, List<Map<String, Object>>> yearMap = chlidren.stream().collect(Collectors.groupingBy(m -> m.get("FlowYear").toString()));
                for(Map.Entry<String, List<Map<String, Object>>> obj:yearMap.entrySet()){
                    Map<String,Object> map = new HashMap<>();
                    map.put("flowyear",obj.getKey());
                    map.put("totalflow","/");
                    List<Map<String, Object>> objlist = obj.getValue();
                    for (Map<String, Object> onemap:objlist){
                      if (onemap.get("TotalFlow")!=null&&!"".equals(onemap.get("TotalFlow").toString())){
                           map.put("totalflow",Double.valueOf(onemap.get("TotalFlow").toString()));
                     }
                    }
                    listdata.add(map);
                }
        }
        return listdata;
    }

    @Override
    public Map<String, Object> getEntWaterPermittedFlowInfoListPage(Map<String, Object> param) {
        Map<String, Object> resultmap = new HashMap<>();
        List<Map<String,Object>> listdata = new ArrayList<>();
        //获取企业排放许可信息
        List<Map<String,Object>> result = entPermittedFlowLimitValueMapper.getEntWaterPermittedFlowLimitInfoByParamMap(param);
        List<Map<String, Object>> titlelist = new ArrayList<>();
        titlelist = getFlowTitlelist(result);
        if (result!=null&&result.size()>0) {
            //按污染物分组
            Map<String, List<Map<String, Object>>> listMap = result.stream().collect(Collectors.groupingBy(m -> m.get("fkpollutantcode").toString()));
            //按年份的排序
            List<Map<String, Object>> chlidren = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("FlowYear").toString())
                    )).collect(Collectors.toList());
            //按年份分组
            Map<String, List<Map<String, Object>>> yearMap = chlidren.stream().collect(Collectors.groupingBy(m -> m.get("FlowYear").toString()));
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
                    map.put("sqpfyear_"+obj.getKey(),"/");
                    List<Map<String, Object>> objlist = obj.getValue();
                    for (Map<String, Object> onemap:objlist){
                        if (onemap.get("fkpollutantcode")!=null&&entry.getKey().equals(onemap.get("fkpollutantcode").toString())) {
                            if (onemap.get("TotalFlow")!=null&&!"".equals(onemap.get("TotalFlow").toString())){
                                map.put("sqpfyear_"+obj.getKey(),Double.valueOf(onemap.get("TotalFlow").toString()));
                            }
                        }
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
        return entPermittedFlowLimitValueMapper.getEntWaterPermittedFlowLimitInfoByParamMap(paramMap);
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
        map2.put("label", "申请年排放量限值(t/a)");
        map2.put("prop", "npf");
        map2.put("minwidth", "150px");
        map2.put("headeralign", "center");
        map2.put("align", "center");
        map2.put("showhide", true);
        List<Map<String, Object>> chlidheader1 = new ArrayList<>();
        if (result!=null&&result.size()>0){
            //按年份的排序
            List<Map<String, Object>> chlidren = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("FlowYear").toString())
                    )).collect(Collectors.toList());
            //按年份分组
            List<String> years = new ArrayList<>();
            for(Map<String, Object> onemap:chlidren){
                if (onemap.get("FlowYear")!=null&&!years.contains(onemap.get("FlowYear").toString())){
                    years.add(onemap.get("FlowYear").toString());
                }else{
                    continue;
                }
            }
            for(String year:years) {
                Map<String, Object> map5 = new HashMap<>();//有组织
                map5.put("label", year+"年");
                map5.put("prop", "sqpfyear_"+ year);
                map5.put("minwidth", "100px");
                map5.put("headeralign", "center");
                map5.put("align", "center");
                map5.put("showhide", true);
                chlidheader1.add(map5);
            }
        }
        map2.put("children", chlidheader1);
        titlelist.add(map2);
        return titlelist;
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 14:45
     * @Description: 查询单个企业（废气、废水）排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntAllOutputData(Map<String, Object> param) {
        return entPermittedFlowLimitValueMapper.getEntAllOutputData(param);
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 通过自定义参数查询企业近五年各排放污染物污染总排放许可量信息（废水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @Override
    public Map<String, Object> getEntWaterDischargeTotalListData(Map<String, Object> param) {
        Map<String,Object> resultmap = new HashMap<>();
        List<Map<String,Object>> listdata = new ArrayList<>();
        int startyear = Integer.valueOf(param.get("startyear").toString());
        int endyear = Integer.valueOf(param.get("endyear").toString());
        //获取企业废气排放许可信息
        List<Map<String,Object>> result = entPermittedFlowLimitValueMapper.getEntWaterPermittedFlowLimitInfoByParamMap(param);
        List<Map<String, Object>> titlelist = new ArrayList<>();
        titlelist = getFiveYearWatreFlowTitlelist(startyear,endyear);
        if (result!=null&&result.size()>0) {
            //按污染物分组
            Map<String, List<Map<String, Object>>> listMap = result.stream().collect(Collectors.groupingBy(m -> m.get("fkpollutantcode").toString()));
            for(Map.Entry<String, List<Map<String, Object>>> entry:listMap.entrySet()){
                Map<String,Object> map = new HashMap<>();
                map.put("fkpollutantcode",entry.getKey());
                List<Map<String, Object>> listone = entry.getValue();
                if (listone!=null&&listone.get(0)!=null&&listone.get(0).get("pollutantname")!=null) {
                    map.put("pollutantname", listone.get(0).get("pollutantname"));
                }else{
                    map.put("pollutantname","-");
                }
                for (int i = startyear; i <= endyear; i++) {
                    Double total = 0d;
                    for (Map<String, Object> onemap : listone) {
                        //年份相同
                        if (onemap.get("FlowYear") != null && (i + "").equals(onemap.get("FlowYear").toString())) {
                            //有组织
                            if (onemap.get("TotalFlow") != null && !"".equals(onemap.get("TotalFlow").toString())) {
                                total += Double.valueOf(onemap.get("TotalFlow").toString());
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

    private List<Map<String,Object>> getFiveYearWatreFlowTitlelist(Integer startyear,Integer endyear) {
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
        map2.put("label", "申请年排放量限值(t/a)");
        map2.put("prop", "npf");
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
    public Map<String, Object> getEntWaterDischargeTotalAnalysisData(Map<String, Object> param) {
        Map<String,Object> resultmap = new HashMap<>();
        List<Map<String,Object>> listdata = new ArrayList<>();
        int startyear = Integer.valueOf(param.get("startyear").toString());
        int endyear = Integer.valueOf(param.get("endyear").toString());
        List<Integer> types = Arrays.asList(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
        param.put("monitorpointtypes",types);
        //获取企业废气排放许可信息
        List<Map<String,Object>> result = entPermittedFlowLimitValueMapper.getEntWaterPermittedFlowLimitInfoByParamMap(param);
        List<Map<String, Object>> titlelist = new ArrayList<>();
        titlelist = getFiveYearWaterFlowAnalysisTitlelist(startyear,endyear);
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
                        if (onemap.get("FlowYear") != null && (year + "").equals(onemap.get("FlowYear").toString())) {
                            //有组织
                            if (onemap.get("TotalFlow") != null && !"".equals(onemap.get("TotalFlow").toString())) {
                                xk_num += Double.valueOf(onemap.get("TotalFlow").toString());
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
     * @Description: 近五年各排放污染物污染总排放许可量信息表头（废水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    private List<Map<String,Object>> getFiveYearWaterFlowAnalysisTitlelist(Integer startyear,Integer endyear) {
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
        map2.put("label", "废水年排放情况(t/a)");
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
            map8.put("minwidth", "100px");
            map8.put("type", "percentage");
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

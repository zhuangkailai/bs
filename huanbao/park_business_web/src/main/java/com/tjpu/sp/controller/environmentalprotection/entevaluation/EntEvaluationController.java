package com.tjpu.sp.controller.environmentalprotection.entevaluation;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntEvaluationService;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntSynthesizeEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xsm
 * @description: 企业评价控制层
 * @create: 2022-03-04 09:16
 * @version: V1.0
 */
@RestController
@RequestMapping("entEvaluation")
public class EntEvaluationController {
    @Autowired
    private EntEvaluationService entEvaluationService;
    @Autowired
    private EntSynthesizeEvaluationService entSynthesizeEvaluationService;

    /**
     * @Author: xsm
     * @Date: 2022/03/08 0008 11:04
     * @Description: 统计某企业最近评价排名和环比情况
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "countEntEvaluationRankByParamMap", method = RequestMethod.POST)
    public Object countEntEvaluationRankByParamMap(@RequestJson(value = "pollutionid") String pollutionid) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            //获取所以企业最近两次的评价数据
            List<Map<String, Object>> datalist = entEvaluationService.getEntLastTwoEvaluationData();
            if (datalist!=null && datalist.size()>0){
                List<Map<String, Object>> this_list = new ArrayList<>();
                List<Map<String, Object>> last_list = new ArrayList<>();
                resultMap.put("lasttime","");
                for (Map<String, Object> map:datalist){
                    if (map.get("rn")!=null){
                        if ("1".equals(map.get("rn").toString())){
                            this_list.add(map);
                            if (pollutionid.equals(map.get("fkpollutionid").toString())){
                                resultMap.put("lasttime",map.get("evaluationdate"));
                            }
                        }
                        if ("2".equals(map.get("rn").toString())){
                            last_list.add(map);
                        }
                    }
                }

                resultMap.put("total",this_list.size());
                resultMap.put("change","");
                resultMap.put("rank","");
                int this_rank = getPollutionEvaluationRank(this_list,pollutionid);
                int last_rank = getPollutionEvaluationRank(last_list,pollutionid);
                if (this_rank!=0) {
                    resultMap.put("rank", this_rank);
                    if (last_rank!=0){
                        if (this_rank<last_rank){
                            resultMap.put("change", "up");
                        }else if(this_rank==last_rank){
                            resultMap.put("change", "");
                        }else if(this_rank>last_rank){
                            resultMap.put("change", "down");
                        }
                    }
                }
            }else{
                resultMap.put("lasttime","");
                resultMap.put("rank","");
                resultMap.put("change","");
                resultMap.put("total",0);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 计算企业评价排名
     * */
    private int getPollutionEvaluationRank(List<Map<String, Object>> maps,String pollutionid) {
        int rank = 0;
        if (maps!=null) {
            //按评分排序
            maps = maps.stream().filter(m -> m.get("evaluationscore") != null).sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("evaluationscore").toString())).reversed()).collect(Collectors.toList());
            Map<String, Object> onemap;
            for (int i = 0; i < maps.size(); i++) {
                onemap = maps.get(i);
                if (onemap.get("fkpollutionid") != null && pollutionid.equals(onemap.get("fkpollutionid").toString())) {
                    rank = i + 1;
                }
            }
        }
        return rank;
    }


}

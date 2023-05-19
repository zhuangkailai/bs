package com.tjpu.sp.service.impl.envhousekeepers.checkanalysis;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.envhousekeepers.checkentinfo.CheckEntInfoMapper;
import com.tjpu.sp.dao.envhousekeepers.checkitemdata.CheckItemDataMapper;
import com.tjpu.sp.dao.envhousekeepers.checkproblemexpound.CheckProblemExpoundMapper;
import com.tjpu.sp.service.envhousekeepers.checkanalysis.CheckAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CheckAnalysisServiceImpl implements CheckAnalysisService {
    @Autowired
    private CheckEntInfoMapper checkEntInfoMapper;
    @Autowired
    private CheckItemDataMapper checkItemDataMapper;
    @Autowired
    private CheckProblemExpoundMapper checkProblemExpoundMapper;

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 上午 09:09
     * @Description: 根据检查日期获取企业问题数量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countCheckProblemNumForEntRank(Map<String, Object> param) {
        List<Map<String, Object>> datalist = checkProblemExpoundMapper.countCheckProblemNumForEntRank(param);
        Double total=0d;
        if (datalist!=null&&datalist.size()>0){
            for (Map<String,Object> map:datalist){
                total+=Double.valueOf(map.get("num").toString());
            }
            if (total>0){
                for (Map<String,Object> map:datalist){
                    String Proportion = DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(map.get("num").toString())*100/total)+"%";
                    map.put("proportion",Proportion);
                }
            }
        }
        return datalist;
    }
    /**
     * @author: xsm
     * @date: 2021/07/09 0009 上午 09:36
     * @Description: 根据检查日期分组统计各问题类型数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countCheckProblemNumGroupByProblemClass(Map<String, Object> param) {
        List<Map<String, Object>> datalist = checkProblemExpoundMapper.countCheckProblemNumGroupByProblemClass(param);
        Double total=0d;
        if (datalist!=null&&datalist.size()>0){
            for (Map<String,Object> map:datalist){
                total+=Double.valueOf(map.get("num").toString());
            }
            if (total>0){
                for (Map<String,Object> map:datalist){
                    String Proportion = DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(map.get("num").toString())*100/total)+"%";
                    map.put("proportion",Proportion);
                }
            }
        }
        return datalist;
    }

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 14:52
     * @Description: 根据检查日期分组统计各企业检查次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countCheckNumGroupByEnt(Map<String, Object> param) {
        return checkProblemExpoundMapper.countCheckNumGroupByEnt(param);
    }

    /**
     * @author: xsm
     * @date: 2021/08/11 0011 上午 09:01
     * @Description: 根据检查年份和检查表类型分组统计企业巡查情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countCheckNumGroupByMonthDate(Map<String, Object> param) {
        //检查企业数
        List<Map<String, Object>> entlist =  checkProblemExpoundMapper.countCheckNumGroupByMonthDate(param);
        //检查问题数
        List<Map<String, Object>> problemlist =  checkProblemExpoundMapper.countCheckProblemNumGroupByMonthDate(param);
        if (entlist!=null&&entlist.size()>0){
            for (Map<String, Object> map:entlist){
                String CheckTime = map.get("CheckTime")!=null?map.get("CheckTime").toString():"";
                if (!"".equals(CheckTime)){
                    map.put("CheckTime",DataFormatUtil.FormatDateOneToOther(CheckTime, "yyyy-MM", "yyyy年M月"));
                }
                map.put("totalmnum",0);
                map.put("yzgnum",0);
                map.put("wzgnum",0);
                map.put("proportion","-");
                if (problemlist!=null&&problemlist.size()>0){
                    for (Map<String, Object> map2:problemlist){
                        if (map2.get("CheckTime")!=null&&CheckTime.equals(map2.get("CheckTime").toString())){
                            map.put("totalmnum",map2.get("totalmnum"));
                            map.put("yzgnum",map2.get("yzgnum"));
                            map.put("wzgnum",map2.get("wzgnum"));
                            int total = map2.get("totalmnum")!=null?Integer.valueOf(map2.get("totalmnum").toString()):0;
                            int num1 = map2.get("yzgnum")!=null?Integer.valueOf(map2.get("yzgnum").toString()):0;
                            if (total>0){
                                map.put("proportion",(num1 * 100/total)+"");
                            }
                        }
                    }
                }
            }
        }
        return entlist;
    }

    /**
     * @author: xsm
     * @date: 2021/08/11 0011 下午 13:18
     * @Description: 根据检查年份和检查表类型分组统计企业问题整改情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countProblemRectificationRateDataGroupByEnt(Map<String, Object> param) {
        List<Map<String, Object>> entlist =  checkProblemExpoundMapper.countProblemRectificationRateDataGroupByEnt(param);
        if (entlist!=null&&entlist.size()>0){
            for (Map<String, Object> map:entlist){
                int total = map.get("totalmnum")!=null?Integer.valueOf(map.get("totalmnum").toString()):0;
                int num1 = map.get("yzgnum")!=null?Integer.valueOf(map.get("yzgnum").toString()):0;
                if (total>0){
                    map.put("proportion",(num1 * 100/total)+"");
                }else{
                    map.put("proportion","-");
                }
            }
        }
        return entlist;

    }

    /**
     * @author: xsm
     * @date: 2021/08/11 0011 下午 17:45
     * @Description: 根据检查时间段、行业类型和检查表类型分组统计企业问题整改情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countProblemRateDataGroupByIndustryTypeAndEnt(Map<String, Object> param) {
        List<Map<String, Object>> entlist =  checkProblemExpoundMapper.countProblemRateDataGroupByIndustryTypeAndEnt(param);
        if (entlist!=null&&entlist.size()>0){
            for (Map<String, Object> map:entlist){
                int total = map.get("totalmnum")!=null?Integer.valueOf(map.get("totalmnum").toString()):0;
                int num1 = map.get("yzgnum")!=null?Integer.valueOf(map.get("yzgnum").toString()):0;
                if (total>0){
                    map.put("proportion",(num1 * 100/total)+"");
                }else{
                    map.put("proportion","-");
                }
            }
        }
        return entlist;
    }

    @Override
    public List<Map<String, Object>> countPollutionPatrolDataByEntID(Map<String, Object> param) {
        return checkEntInfoMapper.countPollutionPatrolDataByEntID(param);
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 上午 9:59
     * @Description: 根据企业ID获取企业问题类别分组统计(企业端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countPollutionProblemDataGroupByCategory(Map<String, Object> param) {
        List<Map<String, Object>> entlist = checkProblemExpoundMapper.countPollutionProblemDataGroupByCategory(param);
        if (entlist!=null&&entlist.size()>0){
            int alltotal = 0;
            for (Map<String, Object> map:entlist){
                int total = map.get("totalmnum")!=null?Integer.valueOf(map.get("totalmnum").toString()):0;
                alltotal +=total;
            }
            for (Map<String, Object> map:entlist){
                int total = map.get("totalmnum")!=null?Integer.valueOf(map.get("totalmnum").toString()):0;
                int num1 = map.get("yzgnum")!=null?Integer.valueOf(map.get("yzgnum").toString()):0;
                if (total>0){
                    map.put("zg_proportion",(num1 * 100/total)+"");
                }else{
                    map.put("zg_proportion","-");
                }
                if (alltotal>0){
                    map.put("proportion",(total * 100/alltotal)+"");
                }else{
                    map.put("proportion","-");
                }
            }
        }
        return entlist;
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 上午 10:52
     * @Description: 根据企业ID获取企业自查次数统计(企业端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countEntSelfCheckNumGroupByMonthByEntID(Map<String, Object> param) {
        return checkEntInfoMapper.countEntSelfCheckNumGroupByMonthByEntID(param);
    }

}

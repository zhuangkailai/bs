package com.tjpu.sp.controller.envhousekeepers.checkanalysis;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.envhousekeepers.checkanalysis.CheckAnalysisService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author: xsm
 * @date: 2021/07/09 0009 上午 9:08
 * @Description: 巡查分析控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
*/
@RestController
@RequestMapping("checkAnalysis")
public class CheckAnalysisController {

    @Autowired
    private CheckAnalysisService checkAnalysisService;

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
    @RequestMapping(value = "countCheckProblemNumForEntRank", method = RequestMethod.POST)
    public Object countCheckProblemNumForEntRank(@RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("starttime",starttime);
            param.put("endtime",endtime);
            List<Map<String, Object>> maplist = checkAnalysisService.countCheckProblemNumForEntRank(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "countCheckProblemNumGroupByProblemClass", method = RequestMethod.POST)
    public Object countCheckProblemNumGroupByProblemClass(@RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("starttime",starttime);
            param.put("endtime",endtime);
            List<Map<String, Object>> maplist = checkAnalysisService.countCheckProblemNumGroupByProblemClass(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 14:36
     * @Description: 根据检查日期分组统计各企业巡查次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countCheckNumGroupByEnt", method = RequestMethod.POST)
    public Object countCheckNumGroupByEnt(@RequestJson(value = "starttime") String starttime,
                                           @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("starttime",starttime);
            param.put("endtime",endtime);
            List<Map<String, Object>> maplist = checkAnalysisService.countCheckNumGroupByEnt(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "countCheckNumGroupByMonthDate", method = RequestMethod.POST)
    public Object countCheckNumGroupByMonthDate(@RequestJson(value = "yeardate") String yeardate,
                                          @RequestJson(value = "titletype") Integer titletype) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("yeardate",yeardate);
            param.put("titletype",titletype);
            List<Map<String, Object>> maplist = checkAnalysisService.countCheckNumGroupByMonthDate(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "countProblemRectificationRateDataGroupByEnt", method = RequestMethod.POST)
    public Object countProblemRectificationRateDataGroupByEnt(@RequestJson(value = "yeardate") String yeardate,
                                                             @RequestJson(value = "titletype") Integer titletype) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("yeardate",yeardate);
            param.put("titletype",titletype);
            List<Map<String, Object>> maplist = checkAnalysisService.countProblemRectificationRateDataGroupByEnt(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/11 0011 下午 13:18
     * @Description: 根据检查时间段、行业类型和检查表类型分组统计企业问题整改情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countProblemRateDataGroupByIndustryTypeAndEnt", method = RequestMethod.POST)
    public Object countProblemRateDataGroupByIndustryTypeAndEnt(@RequestJson(value = "starttime") String starttime,
                                                                @RequestJson(value = "endtime") String endtime,
                                                                @RequestJson(value = "industrytypecode") String industrytypecode,
                                                                @RequestJson(value = "titletype") Integer titletype) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("starttime",starttime);
            param.put("endtime",endtime);
            param.put("industrytypecode",industrytypecode);
            param.put("titletype",titletype);
            List<Map<String, Object>> maplist = checkAnalysisService.countProblemRateDataGroupByIndustryTypeAndEnt(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 上午 8:57
     * @Description: 根据企业ID获取企业巡查任务提醒(企业端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countPollutionPatrolDataByEntID", method = RequestMethod.POST)
    public Object countPollutionPatrolDataByEntID(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            List<Map<String, Object>> maplist = checkAnalysisService.countPollutionPatrolDataByEntID(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "countPollutionProblemDataGroupByCategory", method = RequestMethod.POST)
    public Object countPollutionProblemDataGroupByCategory(@RequestJson(value = "pollutionid") String pollutionid,
                                                           @RequestJson(value = "titletype") Integer titletype,
                                                           @RequestJson(value = "starttime", required = false) String starttime,
                                                           @RequestJson(value = "endtime", required = false) String endtime) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("titletype",titletype);
            if (StringUtils.isNotBlank(starttime)&&StringUtils.isNotBlank(endtime)) {
                Date nowTime = new Date();
                String year = DataFormatUtil.getDateY(nowTime);//当前年
                param.put("startyear", year);
                param.put("endyear", year);
            }else{
                param.put("starttime", starttime);
                param.put("endtime", endtime);
            }
            List<Map<String, Object>> maplist = checkAnalysisService.countPollutionProblemDataGroupByCategory(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "countEntSelfCheckNumGroupByMonthByEntID", method = RequestMethod.POST)
    public Object countEntSelfCheckNumGroupByMonthByEntID(@RequestJson(value = "pollutionid") String pollutionid,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("starttime",starttime);
            param.put("endtime",endtime);
            List<Map<String, Object>> maplist = checkAnalysisService.countEntSelfCheckNumGroupByMonthByEntID(param);
            return AuthUtil.parseJsonKeyToLower("success", maplist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

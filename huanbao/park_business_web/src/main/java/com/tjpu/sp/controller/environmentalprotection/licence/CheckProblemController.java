package com.tjpu.sp.controller.environmentalprotection.licence;

import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.CheckProblemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 检查问题处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/5/11 13:34
 */
@RestController
@RequestMapping("checkproblem")
public class CheckProblemController {

    private final CheckProblemService checkProblemService;

    public CheckProblemController(CheckProblemService checkProblemService) {
        this.checkProblemService = checkProblemService;
    }


    /**
     * @Description: 统计检查问题数据（问题来源分组，工单数、完成率）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/11 13:38
     */
    @RequestMapping(value = "countProblemSourceData", method = RequestMethod.POST)
    public Object countProblemSourceData(@RequestJson(value = "starttime", required = false) String starttime,
                                         @RequestJson(value = "endtime", required = false) String endtime) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> dataList = checkProblemService.getProblemSourceDataByParam(paramMap);
            if (dataList.size() > 0) {
                Map<String, Object> codeAndName = new HashMap<>();
                Map<String, Integer> codeAndNum = new HashMap<>();
                Map<String, Integer> codeAndCNum = new HashMap<>();

                String code;
                Integer countnum;
                Integer status;
                for (Map<String, Object> dataMap : dataList) {
                    code = dataMap.get("countcode").toString();
                    countnum = Integer.parseInt(dataMap.get("countnum").toString());
                    codeAndName.put(code, dataMap.get("countname"));
                    codeAndNum.put(code, codeAndNum.get(code) != null ? codeAndNum.get(code) + countnum : countnum);
                    if (dataMap.get("status") != null) {
                        status = Integer.parseInt(dataMap.get("status").toString());
                        if (status == 3) {//已完成
                            codeAndCNum.put(code, codeAndCNum.get(code) != null ? codeAndCNum.get(code) + countnum : countnum);
                        }
                    }
                }
                Double rate;
                Integer CNum;
                for (String codeIndex : codeAndName.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("countcode", codeIndex);
                    resultMap.put("countname", codeAndName.get(codeIndex));
                    countnum = codeAndNum.get(codeIndex) != null ? codeAndNum.get(codeIndex) : 0;
                    resultMap.put("countnum", countnum);
                    CNum = codeAndCNum.get(codeIndex) != null ? codeAndCNum.get(codeIndex) : 0;
                    if (countnum > 0 && CNum > 0) {
                        rate = 100d * CNum / countnum;
                    } else {
                        rate = 0d;
                    }
                    resultMap.put("countrate", DataFormatUtil.SaveOneAndSubZero(rate));
                    resultList.add(resultMap);
                }

            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @Description: 统计执行报告（应提交、已提交、未提交）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/11 13:38
     */
    @RequestMapping(value = "countExecuteReportData", method = RequestMethod.POST)
    public Object countExecuteReportData(@RequestJson(value = "starttime", required = false) String starttime,
                                         @RequestJson(value = "endtime", required = false) String endtime) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> dataList = checkProblemService.countExecuteReportData(paramMap);
            if (dataList.size() > 0) {
                Integer tjnum;
                Integer totalnum;
                for (Map<String, Object> dataMap : dataList) {
                    tjnum = Integer.parseInt(dataMap.get("tjnum").toString());
                    totalnum = Integer.parseInt(dataMap.get("totalnum").toString());
                    dataMap.put("wnum", totalnum - tjnum);
                    resultList.add(dataMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @Description: 统计台账记录（应提交、已提交、未提交，提交率）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/11 13:38
     */
    @RequestMapping(value = "countStandingBookReport", method = RequestMethod.POST)
    public Object countStandingBookReport(@RequestJson(value = "starttime", required = false) String starttime,
                                          @RequestJson(value = "endtime", required = false) String endtime) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> dataList = checkProblemService.countStandingBookReport(paramMap);
            if (dataList.size() > 0) {
                Integer tjnum;
                Integer totalnum;
                Integer wnum;
                for (Map<String, Object> dataMap : dataList) {
                    tjnum = Integer.parseInt(dataMap.get("tjnum").toString());
                    totalnum = Integer.parseInt(dataMap.get("totalnum").toString());
                    wnum = totalnum - tjnum;
                    dataMap.put("wnum", wnum);
                    if (tjnum > 0 && totalnum > 0) {
                        dataMap.put("tjl", DataFormatUtil.SaveOneAndSubZero(100d*tjnum/totalnum));
                    }else {
                        dataMap.put("tjl",0);
                    }
                    resultList.add(dataMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }
}

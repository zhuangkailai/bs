package com.tjpu.sp.controller.environmentalprotection.problemcount;

import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.model.environmentalprotection.pollutantsmell.PollutantSmellVO;
import com.tjpu.sp.service.common.UserAuthSupportService;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import com.tjpu.sp.service.environmentalprotection.pollutantsmell.PollutantSmellService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("problemCount")
public class ProblemCountController {

    @Autowired
    private CheckProblemExpoundService checkProblemExpoundService;


    /**
     * @Description: 统计问题信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/14 13:35
     */
    @RequestMapping(value = "/countProblemData", method = RequestMethod.POST)
    public Object countProblemData(
            @RequestJson(value = "year") String year,
            @RequestJson(value = "checkcategoryids",required = false) List<String> checkcategoryids ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("year", year);
            paramMap.put("checkcategoryids", checkcategoryids);
            //paramMap.put("problemsource", 4);
            List<Map<String, Object>> dataList = checkProblemExpoundService.getHBProblemDataListByParam(paramMap);
            int totalnum = dataList.size();
            int zgnum = 0;
            int fcnum = 0;
            int status;
            for (Map<String, Object> dataMap : dataList) {
                if (dataMap.get("status") != null && !"".equals(dataMap.get("status"))) {
                    status = Integer.parseInt(dataMap.get("status").toString());
                    if (status==1) {//待整改
                        zgnum++;
                    }else if(status==2){//待复查
                        fcnum++;
                    }
                }
            }
            resultMap.put("totalnum",totalnum);
            resultMap.put("zgnum",zgnum);
            resultMap.put("fcnum",fcnum);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

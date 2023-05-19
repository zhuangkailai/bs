package com.tjpu.sp.controller.environmentalprotection.problemdata;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.service.common.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: chengzq
 * @date: 2020/2/21 0021 09:51
 * @Description: 数据问题控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("problemdata")
public class ProblemDataController {

    @Autowired
    private FileInfoService fileInfoService;



    @RequestMapping(value = "getProblemDataFileInfosByUploadTime", method = RequestMethod.POST)
    public Object getProblemDataFileInfosByUploadTime(@RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("Businesstype", 30);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> result = fileInfoService.getProbleDataInfo(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

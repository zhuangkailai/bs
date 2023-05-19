package com.tjpu.sp.controller.envhousekeepers.entrectifyreport;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.entproblemrectifyreport.EntProblemRectifyReportVO;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import com.tjpu.sp.service.envhousekeepers.entproblemrectifyreport.EntProblemRectifyReportService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xsm
 * @date: 2021/07/09 0009 上午 11:08
 * @Description: 企业问题整改报告
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("entRectifyReport")
public class EntProblemRectifyReportController {

    @Autowired
    private EntProblemRectifyReportService entProblemRectifyReportService;
    @Autowired
    private CheckProblemExpoundService checkProblemExpoundService;

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 上午 11:47
     * @Description: 通过自定义参数查询企业问题整改报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getEntProblemRectifyReportsByParamMap", method = RequestMethod.POST)
    public Object getEntProblemRectifyReportsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = entProblemRectifyReportService.getEntProblemRectifyReportByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            long total = pageInfo.getTotal();
            List<String> ids = dataList.stream().filter(m -> m.get("fkfileid") != null).map(m -> m.get("fkfileid").toString()).distinct().collect(Collectors.toList());
            Map<String,Object> param = new HashMap<>();
            param.put("fileflags",ids);
            Map<String,List<Map<String, Object>>> filedatas = checkProblemExpoundService.getFileDataByFileFlags(param);
            for (Map<String,Object> map:dataList){
                if (map.get("fkfileid")!=null&&filedatas!=null&&filedatas.get(map.get("fkfileid").toString())!=null){
                    map.put("filedata",filedatas.get(map.get("fkfileid").toString()));
                }else{
                    map.put("filedata",new ArrayList<>());
                }
            }
            resultMap.put("datalist", dataList);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 13:08
     * @Description: 新增企业问题整改报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addEntProblemRectifyReport", method = RequestMethod.POST)
    public Object addEntProblemRectifyReport(@RequestJson(value = "addformdata") Object addformdata
                                  ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            EntProblemRectifyReportVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntProblemRectifyReportVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setUpdatedate(new Date());
            entity.setUpdateuser(username);
            entProblemRectifyReportService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 13:08
     * @Description: 通过id获取企业问题整改报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntProblemRectifyReportByID", method = RequestMethod.POST)
    public Object getStorageTankByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> obj = entProblemRectifyReportService.getEntProblemRectifyReportByID(id);
            return AuthUtil.parseJsonKeyToLower("success", obj);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/07/09 0009 上午 11:08
     * @Description: 修改企业问题整改报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateEntProblemRectifyReport", method = RequestMethod.POST)
    public Object updateEntProblemRectifyReport(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            EntProblemRectifyReportVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntProblemRectifyReportVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatedate(new Date());
            entity.setUpdateuser(username);
            entProblemRectifyReportService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/07/09 0009 上午 11:08
     * @Description: 通过id删除企业问题整改报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteEntProblemRectifyReportByID", method = RequestMethod.POST)
    public Object deleteEntProblemRectifyReportByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            entProblemRectifyReportService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 上午 11:08
     * @Description: 通过id获取企业问题整改报告详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntProblemRectifyReportDetailByID", method = RequestMethod.POST)
    public Object getEntProblemRectifyReportDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = entProblemRectifyReportService.getEntProblemRectifyReportDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 4:09
     * @Description: 验证企业报告是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsEntCheckReportValidByParam", method = RequestMethod.POST)
    public Object IsEntCheckReportValidByParam(
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "checktime") String checktime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("checktime", checktime);
            List<Map<String, Object>> datalist = entProblemRectifyReportService.IsEntCheckReportValidByParam(paramMap);
            String flag = "no";
            if (datalist != null&&datalist.size()>0) {    //不等于空，表示重复 不可以添加
                flag = "yes";
            }
            return AuthUtil.parseJsonKeyToLower("success", flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

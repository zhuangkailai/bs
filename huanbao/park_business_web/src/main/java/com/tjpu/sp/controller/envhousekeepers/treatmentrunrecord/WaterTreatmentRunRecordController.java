package com.tjpu.sp.controller.envhousekeepers.treatmentrunrecord;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.envhousekeepers.treatmentrunrecord.WaterTreatmentRunRecordVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.envhousekeepers.treatmentrunrecord.WaterTreatmentRunRecordService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;


/**
 * @author: xsm
 * @date: 2021/08/17 0017 下午 16:37
 * @Description: 废水治理设施运行记录控制层
 */
@RestController
@RequestMapping("waterTreatmentRunRecord")
public class WaterTreatmentRunRecordController {

    @Autowired
    private WaterTreatmentRunRecordService waterTreatmentRunRecordService;
    @Autowired
    private PollutionService pollutionService;

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 通过自定义参数查询废水治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getWaterTreatmentRunRecordByParamMap", method = RequestMethod.POST)
    public Object getWaterTreatmentRunRecordByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = waterTreatmentRunRecordService.getWaterTreatmentRunRecordByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            long total = pageInfo.getTotal();
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
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 新增废水治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addWaterTreatmentRunRecord", method = RequestMethod.POST)
    public Object addWaterTreatmentRunRecord(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            WaterTreatmentRunRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), WaterTreatmentRunRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            waterTreatmentRunRecordService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 通过id获取废水治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getWaterTreatmentRunRecordByID", method = RequestMethod.POST)
    public Object getWaterTreatmentRunRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> entity = waterTreatmentRunRecordService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 修改废水治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateWaterTreatmentRunRecord", method = RequestMethod.POST)
    public Object updateWaterTreatmentRunRecord(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            WaterTreatmentRunRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), WaterTreatmentRunRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            waterTreatmentRunRecordService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 通过id删除废水治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteWaterTreatmentRunRecordByID", method = RequestMethod.POST)
    public Object deleteWaterTreatmentRunRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            waterTreatmentRunRecordService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 通过id获取废水治理设施运行记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getWaterTreatmentRunRecordDetailByID", method = RequestMethod.POST)
    public Object getWaterTreatmentRunRecordDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = waterTreatmentRunRecordService.getWaterTreatmentRunRecordDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/23 0021 下午 6:47
     * @Description:导出-危废处置记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportWaterTreatmentRunRecord", method = RequestMethod.POST)
    public void exportWaterTreatmentRunRecord(
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "treatmentname", required = false) String treatmentname,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("treatmentname", treatmentname);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("id", pollutionid);
            PollutionVO detailById = pollutionService.getDetailById(paramMap);
            String titlename = detailById.getPollutionname()+"废水治理设施运行记录";
            List<Map<String, Object>> tablelistdata =  waterTreatmentRunRecordService.getWaterTreatmentRunRecordByParamMap(paramMap);
            //设置文件名称
            String fileName = "企业台账_废水治理设施运行记录_" + new Date().getTime();
            List<String> keys = Arrays.asList("TreatmentName","TreatmentNum","ExceptionStartTime","ExceptionEndTime","pollutantname",
                    "FlowQuantity","DrainDirectionName","EventCause","IsReport","Solutions","RecordUser","RecordTime","ReviewerUser");//
            String[][] excelHeader = {{"防治设施名称", "编号", "异常情况起始时刻","异常情况终止时刻","污染排放情况","","","事件原因","是否报告","应对措施","记录时间","记录人","审核人"},
                    { "", "", "","","污染物种类","排放浓度","排放去向","","","","","",""}};
            String[][] headnum = {{"1,2,0,0", "1,2,1,1", "1,2,2,2", "1,2,3,3","1,1,4,6","1,2,7,7","1,2,8,8","1,2,9,9","1,2,10,10","1,2,11,11","1,2,12,12"},
                    {"2,2,4,4", "2,2,5,5","2,2,6,6"} };
            ExcelUtil.exportManyTitleHeaderExcelData(fileName, response, request, "",excelHeader,headnum,keys,tablelistdata, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
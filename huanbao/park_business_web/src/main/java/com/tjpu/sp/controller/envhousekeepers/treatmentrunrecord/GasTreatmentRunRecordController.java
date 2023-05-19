package com.tjpu.sp.controller.envhousekeepers.treatmentrunrecord;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.envhousekeepers.treatmentrunrecord.GasTreatmentRunRecordVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.envhousekeepers.treatmentrunrecord.GasTreatmentRunRecordService;
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
 * @Description: 废气治理设施运行记录控制层
 */
@RestController
@RequestMapping("gasTreatmentRunRecord")
public class GasTreatmentRunRecordController {

    @Autowired
    private GasTreatmentRunRecordService gasTreatmentRunRecordService;
    @Autowired
    private PollutionService pollutionService;

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 通过自定义参数查询废气治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getGasTreatmentRunRecordByParamMap", method = RequestMethod.POST)
    public Object getGasTreatmentRunRecordByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = gasTreatmentRunRecordService.getGasTreatmentRunRecordByParamMap(jsonObject);
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
     * @Description: 新增废气治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addGasTreatmentRunRecord", method = RequestMethod.POST)
    public Object addGasTreatmentRunRecord(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            GasTreatmentRunRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), GasTreatmentRunRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            gasTreatmentRunRecordService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 通过id获取废气治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getGasTreatmentRunRecordByID", method = RequestMethod.POST)
    public Object getGasTreatmentRunRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> entity = gasTreatmentRunRecordService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 修改废气治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateGasTreatmentRunRecord", method = RequestMethod.POST)
    public Object updateGasTreatmentRunRecord(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            GasTreatmentRunRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), GasTreatmentRunRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            gasTreatmentRunRecordService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 通过id删除废气治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteGasTreatmentRunRecordByID", method = RequestMethod.POST)
    public Object deleteGasTreatmentRunRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            gasTreatmentRunRecordService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 16:37
     * @Description: 通过id获取废气治理设施运行记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getGasTreatmentRunRecordDetailByID", method = RequestMethod.POST)
    public Object getGasTreatmentRunRecordDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = gasTreatmentRunRecordService.getGasTreatmentRunRecordDetailByID(id);
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
    @RequestMapping(value = "exportGasTreatmentRunRecord", method = RequestMethod.POST)
    public void exportGasTreatmentRunRecord(
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
            String titlename = detailById.getPollutionname()+"废气治理设施运行记录";
            List<Map<String, Object>> tablelistdata =  gasTreatmentRunRecordService.getGasTreatmentRunRecordByParamMap(paramMap);
            //设置文件名称
            String fileName = "企业台账_废气治理设施运行记录_" + new Date().getTime();
            List<String> keys = Arrays.asList("TreatmentName","TreatmentNum","TreatmentModel","ParameterName","DesignValue",
                    "ParameterUnit","RunStartTime","RunEndTime","IsNormal","SmokeVolume","pollutantname","HandleEfficiency","DataSources","EmissionPipeHeight","OutPutTemperature","Pressure","FlowTime","PowerConsumption","Afterproduct","Production","DrugName","DrugAddTime","Dosage","RecordUser","RecordTime","ReviewerUser");//
            String[][] excelHeader = {{"防治设施名称", "编号","防治设施型号", "主要防治设施规格参数","","","运行状态","","","污染排放情况","","","","排气筒高度(m)","排口温度(℃)","压力(kpa)","排放时间(h)","耗电量(kwh)","副产物","","药剂情况","","","记录时间","记录人","审核人"},
                    { "", "","", "参数名称","设计值","单位","开始时间","结束时间","是否正常","烟气量(m³/h)","污染因子","治理效率%","数据来源","","","","","","名称","产生量","名称","添加时间","添加量(t)","","",""}};
            String[][] headnum = {{"1,2,0,0", "1,2,1,1", "1,2,2,2", "1,1,3,5","1,1,6,8","1,1,9,12","1,2,13,13","1,2,14,14","1,2,15,15","1,2,16,16","1,2,17,17","1,1,18,19","1,1,20,22","1,2,23,23","1,2,24,24","1,2,25,25"},
                    {"2,2,3,3", "2,2,4,4","2,2,5,5","2,2,6,6","2,2,7,7","2,2,8,8","2,2,9,9","2,2,10,10","2,2,11,11","2,2,12,12","2,2,18,18","2,2,19,19","2,2,20,20","2,2,21,21","2,2,22,22"} };
            ExcelUtil.exportManyTitleHeaderExcelData(fileName, response, request, "",excelHeader,headnum,keys,tablelistdata, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
}
package com.tjpu.sp.controller.envhousekeepers.fuelconsumption;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.envhousekeepers.fuelconsumption.FuelConsumptionRecordVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.envhousekeepers.fuelconsumption.FuelConsumptionRecordService;
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
 * @date: 2021/08/18 0018 下午 13:01
 * @Description: 燃料用量记录控制层
 */
@RestController
@RequestMapping("fuelConsumptionRecord")
public class FuelConsumptionRecordController {

    @Autowired
    private FuelConsumptionRecordService fuelConsumptionRecordService;
    @Autowired
    private PollutionService pollutionService;


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 13:01
     * @Description: 通过自定义参数查询燃料用量记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getFuelConsumptionRecordByParamMap", method = RequestMethod.POST)
    public Object getFuelConsumptionRecordByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = fuelConsumptionRecordService.getFuelConsumptionRecordByParamMap(jsonObject);
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
     * @date: 2021/08/18 0018 下午 13:01
     * @Description: 新增燃料用量记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addFuelConsumptionRecord", method = RequestMethod.POST)
    public Object addFuelConsumptionRecord(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            FuelConsumptionRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), FuelConsumptionRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            fuelConsumptionRecordService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 13:01
     * @Description: 通过id获取燃料用量记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getFuelConsumptionRecordByID", method = RequestMethod.POST)
    public Object getFuelConsumptionRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> entity = fuelConsumptionRecordService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 13:01
     * @Description: 修改燃料用量记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateFuelConsumptionRecord", method = RequestMethod.POST)
    public Object updateFuelConsumptionRecord(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            FuelConsumptionRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), FuelConsumptionRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            fuelConsumptionRecordService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 13:01
     * @Description: 通过id删除燃料用量记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteFuelConsumptionRecordByID", method = RequestMethod.POST)
    public Object deleteFuelConsumptionRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            fuelConsumptionRecordService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 13:01
     * @Description: 通过id获取燃料用量记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getFuelConsumptionRecordDetailByID", method = RequestMethod.POST)
    public Object getFuelConsumptionRecordDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = fuelConsumptionRecordService.getFuelConsumptionRecordDetailByID(id);
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
    @RequestMapping(value = "exportFuelConsumptionRecordRecord", method = RequestMethod.POST)
    public void exportFuelConsumptionRecordRecord(
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "fuelname", required = false) String fuelname,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("fuelname", fuelname);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("id", pollutionid);
            PollutionVO detailById = pollutionService.getDetailById(paramMap);
            String titlename = detailById.getPollutionname()+"燃料用量记录";
            List<Map<String, Object>> tablelistdata =  fuelConsumptionRecordService.getFuelConsumptionRecordByParamMap(paramMap);
            //设置文件名称
            String fileName = "企业台账_燃料用量_" + new Date().getTime();
            List<String> keys = Arrays.asList("FuelName","Consumption","Calorificvalue","Unit","CoalSulfurContent",
                    "AshContent","VolatilizationContent","OtherCoal","FuelSulfurContent","OtherFuel","HydrogenSulfide",
                    "OtherGas","OtherFuels","RecordUser","RecordTime","ReviewerUser");//
            String[][] excelHeader = {{"燃料名称", "用量", "地位热值","单位","品质","","","","","","","","", "记录人","记录时间","审核人"},
                    { "","","","","燃煤","","","", "燃油","","燃气","", "其他燃料","","",""},
                    {"","","","","含硫量(%)","灰分(%)","挥发分(%)","其他","含硫量(%)","其他","硫化氢含量(%)","其他","相关物质含量","","",""}};
            String[][] headnum = {{"1,3,0,0", "1,3,1,1", "1,3,2,2", "1,3,3,3","1,1,4,12","1,3,13,13","1,3,14,14","1,3,15,15"},
                    {"2,2,4,7", "2,2,8,9","2,2,10,11", "2,2,12,12"},
                    {"3,3,4,4","3,3,5,5","3,3,6,6","3,3,7,7","3,3,8,8","3,3,9,9","3,3,10,10","3,3,11,11","3,3,12,12"}};
            ExcelUtil.exportManyTitleHeaderExcelData(fileName, response, request, "",excelHeader,headnum,keys,tablelistdata, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
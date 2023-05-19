package com.tjpu.sp.controller.envhousekeepers.wastematerial;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.wastematerial.WasteMaterialHandleRecordVO;
import com.tjpu.sp.service.envhousekeepers.wastematerial.WasteMaterialHandleRecordService;
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
 * @date: 2021/08/18 0018 下午 1:20
 * @Description: 危废处置记录控制层
 */
@RestController
@RequestMapping("wasteMaterialHandleRecord")
public class WasteMaterialHandleRecordController {

    @Autowired
    private WasteMaterialHandleRecordService wasteMaterialHandleRecordService;

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:20
     * @Description: 通过自定义参数查询危废处置记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getWasteMaterialHandleRecordByParamMap", method = RequestMethod.POST)
    public Object getWasteMaterialHandleRecordByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }

            List<Map<String, Object>> dataList = wasteMaterialHandleRecordService.getWasteMaterialHandleRecordByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", dataList);
            String titlename = wasteMaterialHandleRecordService.WasteMaterialTitleNameByParam(jsonObject);
            resultMap.put("titlename", titlename);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:20
     * @Description: 新增危废处置记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addWasteMaterialHandleRecord", method = RequestMethod.POST)
    public Object addWasteMaterialHandleRecord(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            WasteMaterialHandleRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), WasteMaterialHandleRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            wasteMaterialHandleRecordService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:20
     * @Description: 通过id获取危废处置记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getWasteMaterialHandleRecordByID", method = RequestMethod.POST)
    public Object getWasteMaterialHandleRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> entity = wasteMaterialHandleRecordService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:20
     * @Description: 修改危废处置记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateWasteMaterialHandleRecord", method = RequestMethod.POST)
    public Object updateWasteMaterialHandleRecord(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            WasteMaterialHandleRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), WasteMaterialHandleRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            wasteMaterialHandleRecordService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:20
     * @Description: 通过id删除危废处置记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteWasteMaterialHandleRecordByID", method = RequestMethod.POST)
    public Object deleteWasteMaterialHandleRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            wasteMaterialHandleRecordService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:20
     * @Description: 通过id获取危废处置记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getWasteMaterialHandleRecordDetailByID", method = RequestMethod.POST)
    public Object getWasteMaterialHandleRecordDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = wasteMaterialHandleRecordService.getWasteMaterialHandleRecordDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:37
     * @Description: 获取企业废物处置树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "/getWasteMaterialTreeByPollutionID", method = RequestMethod.POST)
    public Object getWasteMaterialTreeByPollutionID(@RequestJson(value = "pollutionid") String pollutionid,
                                                    @RequestJson(value = "fkwastematerialname", required = false) String fkwastematerialname) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("fkwastematerialname",fkwastematerialname);
            List<Map<String, Object>> result = wasteMaterialHandleRecordService.getWasteMaterialTreeByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:37
     * @Description: 根据企业ID和危险废物code删除记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid，fkwastematerialcode]
     * @throws:
     */
    @RequestMapping(value = "/deleteHandleRecordByEntIDAndCode", method = RequestMethod.POST)
    public Object deleteHandleRecordByEntIDAndCode(@RequestJson(value = "pollutionid") String pollutionid,
                                                   @RequestJson(value = "fkwastematerialcode") String fkwastematerialcode
                                                   ) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("fkwastematerialcode",fkwastematerialcode);
           wasteMaterialHandleRecordService.deleteHandleRecordByEntIDAndCode(param);
            return AuthUtil.parseJsonKeyToLower("success", null);
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
    @RequestMapping(value = "exportWasteMaterialHandleRecord", method = RequestMethod.POST)
    public void exportWasteMaterialHandleRecord(
                                    @RequestJson(value = "pollutionid") String pollutionid,
                                    @RequestJson(value = "titlename") String titlename,
                                    @RequestJson(value = "fkwastematerialcode", required = false) String fkwastematerialcode,
                                    @RequestJson(value = "starttime", required = false) String starttime,
                                    @RequestJson(value = "endtime", required = false) String endtime,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("fkwastematerialcode", fkwastematerialcode);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> tablelistdata = wasteMaterialHandleRecordService.getWasteMaterialHandleRecordByParamMap(paramMap);
            //设置文件名称
            String fileName = "企业台账_" + new Date().getTime();
            List<String> keys = Arrays.asList("RecordDate","GeneratedNum","SCPersonCharge","ReceiptNum","InventoryBalance",
                    "ZCPersonCharge","ReceivingUnit","OutsourceHandleNum","TransferNumber","TransferTime","WWPersonCharge",
                    "SelfDisposalNum","ZXPersonCharge");
            String[][] excelHeader = {{"日  期", "产生情况","", "贮存情况","","", "处置利用情况","","","","","",""},
                    { "","数量(吨)","责任人签名", "入库情况", "库存余量","责任人签名","委外处置/利用情况","","","","","自行处置情况",""},
                    {"","","","数量(吨)","数量(吨)","","接收单位","数量(吨)","联单编号","转移时间","责任人签名","数量(吨)","责任人签名"}};
            String[][] headnum = {{"1,3,0,0", "1,1,1,2", "1,1,3,5", "1,1,6,12"},
                    {"2,3,1,1", "2,3,2,2","2,2,3,3", "2,2,4,4","2,3,5,5",  "2,2,6,10", "2,2,11,12"},
                    {"3,3,3,3","3,3,4,4","3,3,6,6","3,3,7,7","3,3,8,8","3,3,9,9","3,3,10,10","3,3,11,11","3,3,12,12"}};
            ExcelUtil.exportManyTitleHeaderExcelData(fileName, response, request, "",excelHeader,headnum,keys,tablelistdata, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
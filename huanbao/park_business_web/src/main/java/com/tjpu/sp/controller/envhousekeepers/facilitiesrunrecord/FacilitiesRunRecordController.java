package com.tjpu.sp.controller.envhousekeepers.facilitiesrunrecord;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.envhousekeepers.facilitiesrunrecord.ProductionFacilitiesRunRecordVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.envhousekeepers.facilitiesrunrecord.FacilitiesRunRecordService;
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
 * @date: 2021/08/17 0017 上午 11:58
 * @Description: 生产设施运行记录控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("facilitiesRunRecord")
public class FacilitiesRunRecordController {

    @Autowired
    private FacilitiesRunRecordService facilitiesRunRecordService;
    @Autowired
    private PollutionService pollutionService;

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 通过自定义参数查询生产设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getFacilitiesRunRecordByParamMap", method = RequestMethod.POST)
    public Object getFacilitiesRunRecordByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = facilitiesRunRecordService.getFacilitiesRunRecordByParamMap(jsonObject);
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
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 新增生产设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addFacilitiesRunRecord", method = RequestMethod.POST)
    public Object addFacilitiesRunRecord(@RequestJson(value = "addformdata") Object addformdata
                                  ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            ProductionFacilitiesRunRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), ProductionFacilitiesRunRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            facilitiesRunRecordService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 通过id获取生产设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getFacilitiesRunRecordByID", method = RequestMethod.POST)
    public Object getFacilitiesRunRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> entity = facilitiesRunRecordService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 修改生产设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateFacilitiesRunRecord", method = RequestMethod.POST)
    public Object updateFacilitiesRunRecord(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            ProductionFacilitiesRunRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(),ProductionFacilitiesRunRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            facilitiesRunRecordService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 通过id删除生产设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteFacilitiesRunRecordByID", method = RequestMethod.POST)
    public Object deleteFacilitiesRunRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            facilitiesRunRecordService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 通过id获取生产设施运行记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getFacilitiesRunRecordDetailByID", method = RequestMethod.POST)
    public Object getFacilitiesRunRecordDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = facilitiesRunRecordService.getFacilitiesRunRecordDetailByID(id);
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
    @RequestMapping(value = "exportFacilitiesRunRecord", method = RequestMethod.POST)
    public void exportFacilitiesRunRecord(
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
            String titlename = detailById.getPollutionname()+"生产设施运行记录";
            List<Map<String, Object>> tablelistdata =  facilitiesRunRecordService.getFacilitiesRunRecordByParamMap(paramMap);
            //设置文件名称
            String fileName = "企业台账_生产设施运行记录_" + new Date().getTime();
            List<String> keys = Arrays.asList("FacilitieName","FacilitieCode","FacilitieModel","ParameterName","DesignValue",
                    "ActuallyValue","ParameterUnit","Throughput","ThroughputUnit","RunStartTime","RunEndTime","ProductionLoad","Semiproduct","SemiproductUnit","FinalProduct","FinalProductUnit","RawMaterialName","MaterialTypeName","Consume","MeaUnit","HarmfulComposition","HarmfulProportion","MaterialSources","RecordUser","RecordTime","ReviewerUser");//
            String[][] excelHeader = {{"生产设施(设备)名称", "编码","生产设施型号", "主要生产设施(设备)规格参数","","","","设计生产能力","","运行状态","","生产负荷","产品产量","","","","原辅料","","","","","","","记录时间","记录人","审核人"},
                    { "", "","", "参数名称","设计值","实际值","单位","生产能力","单位","开始时间","结束时间","","中间产品","单位","最终产品","单位","名称","种类","用量","单位","有毒有害元素","","来源地","记录时间","记录人","审核人"},
                    { "", "","", "","","","","","","","","","","","","","","","","","成分","占比","","","",""}};
            String[][] headnum = {{"1,3,0,0", "1,3,1,1", "1,3,2,2", "1,1,3,6","1,1,7,8","1,1,9,10","1,3,11,11","1,1,12,15","1,1,16,22","1,3,23,23","1,3,24,24","1,3,25,25"},
                    {"2,3,3,3", "2,3,4,4","2,3,5,5","2,3,6,6","2,3,7,7","2,3,8,8","2,3,9,9","2,3,10,10","2,3,12,12","2,3,13,13","2,3,14,14","2,3,15,15","2,3,16,16","2,3,17,17","2,3,18,18","2,3,19,19","2,2,20,21","2,3,22,22"},
                    {"3,3,20,20","3,3,21,21"}};
            ExcelUtil.exportManyTitleHeaderExcelData(fileName, response, request, "",excelHeader,headnum,keys,tablelistdata, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

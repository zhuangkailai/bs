package com.tjpu.sp.controller.environmentalprotection.entpermittendflowlimit;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

/**
 * @author: chengzq
 * @date: 2019/6/27 0027 下午 2:28
 * @Description: 企业许可排放限值控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("EntPermittedFlowLimit")
public class EntPermittedFlowLimitController {

    @Autowired
    private EntPermittedFlowLimitValueService entPermittedFlowLimitValueService;
    @Autowired
    private OnlineCountAlarmService onlineCountAlarmService;
    @Autowired
    private PollutionService pollutionService;


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 9:07
     * @Description: 通过自定义参数获取企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEntPermittedFlowLimitInfoByParamMap",method = RequestMethod.POST)
    public Object getEntPermittedFlowLimitInfoByParamMap(@RequestJson(value="paramsjson",required = true)Object paramsjson) throws Exception {
        try {
            Map<String,Object> resultMap=new HashMap<>();
            Map<String,Object> paramMap =(Map<String,Object>) JSONObject.fromObject(paramsjson);
            Object pagenum = paramMap.get("pagenum");
            Object pagesize = paramMap.get("pagesize");

            if(pagenum!=null && pagesize!=null ){
                PageHelper.startPage(Integer.valueOf(pagenum.toString()),Integer.valueOf(pagesize.toString()));
            }
            List<EntPermittedFlowLimitValueVO> entPermittedFlowLimitInfoByParamMap = entPermittedFlowLimitValueService.getEntPermittedFlowLimitInfoByParamMap(paramMap);
            PageInfo<EntPermittedFlowLimitValueVO> pageInfo = new PageInfo<>(entPermittedFlowLimitInfoByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("total",total);
            resultMap.put("primarykey","pk_id");
            resultMap.put("datalist",entPermittedFlowLimitInfoByParamMap);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 9:44
     * @Description: 新增企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [adddatalist, session]
     * @throws:
     */
    @RequestMapping(value = "addEntPermittedFlowLimitInfo",method = RequestMethod.POST)
    public Object addEntPermittedFlowLimitInfo(@RequestJson(value="adddataform",required = true)Object adddataform ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(adddataform);
            EntPermittedFlowLimitValueVO entPermittedFlowLimitValueVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntPermittedFlowLimitValueVO());
            Date now = new Date();

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);

            String pkid = UUID.randomUUID().toString();
            entPermittedFlowLimitValueVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(now));
            entPermittedFlowLimitValueVO.setUpdateuser(username);
            entPermittedFlowLimitValueVO.setPkId(pkid);

            entPermittedFlowLimitValueService.insert(entPermittedFlowLimitValueVO);

            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 9:57
     * @Description: 通过id获取企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, version]
     * @throws:
     */
    @RequestMapping(value = "getEntPermittedFlowLimitInfoByID",method = RequestMethod.POST)
    public Object getEntPermittedFlowLimitInfoByID(@RequestJson(value="id",required = false)String id) throws Exception {
        try {
            EntPermittedFlowLimitValueVO entPermittedFlowLimitValueVO = entPermittedFlowLimitValueService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success",entPermittedFlowLimitValueVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 10:13
     * @Description: 修改企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updatedataform, session]
     * @throws:
     */
    @RequestMapping(value = "updateEntPermittedFlowLimitInfo",method = RequestMethod.POST)
    public Object updateEntPermittedFlowLimitInfo(@RequestJson(value="updatedataform",required = true)Object updatedataform ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updatedataform);
            EntPermittedFlowLimitValueVO entPermittedFlowLimitValueVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntPermittedFlowLimitValueVO());
            Date now = new Date();

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            entPermittedFlowLimitValueVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(now));
            entPermittedFlowLimitValueVO.setUpdateuser(username);

            entPermittedFlowLimitValueService.updateByPrimaryKey(entPermittedFlowLimitValueVO);

            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 10:14
     * @Description: 通过id删除企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteEntPermittedFlowLimitInfoByID",method = RequestMethod.POST)
    public Object deleteEntPermittedFlowLimitInfoByID(@RequestJson(value="id",required = true)String id) throws Exception {
        try {

            entPermittedFlowLimitValueService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 10:14
     * @Description: 通过id查询企业许可排放限值详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getEntPermittedFlowLimitInfoDetailByID",method = RequestMethod.POST)
    public Object getEntPermittedFlowLimitInfoDetailByID(@RequestJson(value="id",required = false)String id) throws Exception {
        try {

            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("pkid",id);

            List<EntPermittedFlowLimitValueVO> data = entPermittedFlowLimitValueService.getEntPermittedFlowLimitInfoByParamMap(paramMap);
            if(data.size()>0){
                return AuthUtil.parseJsonKeyToLower("success",data.get(0));
            }else{
                return AuthUtil.parseJsonKeyToLower("success",null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    /**
     * @author: chengzq
     * @date: 2019/6/19 0019 下午 2:13
     * @Description: 通过自定义参数导出企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sysmodel, session]
     * @throws:
     */
    @RequestMapping(value = "exportEntPermittedFlowLimitInfoByParamMap",method = RequestMethod.POST)
    public Object exportEntPermittedFlowLimitInfoByParamMap(@RequestJson(value="paramsjson",required = true)Object paramsjson, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            List headers=new ArrayList<>();
            headers.add("污染源名称");
            headers.add("年份");
            headers.add("污染物名称");
            headers.add("排放限值");
            headers.add("排放口类型");

            List headersField=new ArrayList<>();
            headersField.add("pollutionname");
            headersField.add("flowyear");
            headersField.add("pollutantname");
            headersField.add("totalflow");
            headersField.add("fkmonitorpointtype");

            JSONObject jsonObject = JSONObject.fromObject(getEntPermittedFlowLimitInfoByParamMap(paramsjson));
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 =JSONObject.fromObject(data);
            Object data1 =jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(data1);


            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("企业许可排放限值信息",response,request,bytesForWorkBook);

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/28 0028 上午 11:06
     * @Description: 通过自定义参数验证重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "isHaveEntPermittedFlowLimitData",method = RequestMethod.POST)
    public Object isHaveEntPermittedFlowLimitData(@RequestJson(value="paramsjson",required = true)Object paramsjson) throws Exception {
        try {
            List<Map<String, Object>> list = entPermittedFlowLimitValueService.selectByParams(JSONObject.fromObject(paramsjson));
            if(list.size()>0){
                return AuthUtil.parseJsonKeyToLower("success","yes");
            }else{
                return AuthUtil.parseJsonKeyToLower("success","no");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/13 0013 上午 08:47
     * @Description: 统计企业污染物排放情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countEntPollutantFlowDataByParam", method = RequestMethod.POST)
    public Object countEntPollutantFlowDataByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                    @RequestJson(value = "monitorpointtype")Integer monitorpointtype,
                                                    @RequestJson(value = "pollutantcode")String pollutantcode,
                                                    @RequestJson(value = "flowyear") String flowyear) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("pollutionid",pollutionid);
            param.put("flowyear",flowyear);
            param.put("pollutantcode",pollutantcode);
            param.put("monitorpointtype",monitorpointtype);
            Map<String,Object> onemap = entPermittedFlowLimitValueService.getOnePollutantPermitFlowDataByParam(param);
            param.put("userid",userId);
            List<Map<String, Object>> pointlist = pollutionService.getEntPointMNDataByParam(param);
            List<String> mns = new ArrayList<>();
            if (pointlist!=null&&pointlist.size()>0){
                for (Map<String, Object> map:pointlist){
                    if (map.get("DGIMN")!=null) {
                        mns.add(map.get("DGIMN").toString());
                    }
                }
            }
            param.put("mns", mns);
            //获取企业该污染物的许可排放
            Double xkpfl_num = 0d;
            if (onemap!=null&&onemap.get("TotalFlow")!=null&&!"".equals(onemap.get("TotalFlow").toString())){
                xkpfl_num = Double.valueOf(onemap.get("TotalFlow").toString());
            }
            //获取该年份该企业该污染物 所有月排放数据
            Map<String, Object> result = onlineCountAlarmService.getEntPointPollutantMonthFlowDataByParam(param);
            Double total = 0d;
            //总排放量
            if (result.get("total")!=null){
                total = Double.valueOf(result.get("total").toString());
            }
            //许可排放 和剩余排放
            result.put("xkpfl_num",DataFormatUtil.SaveTwoAndSubZero(xkpfl_num));
            Double sypfl_num = 0d;
            sypfl_num = xkpfl_num - total;
            result.put("sypfl_num",DataFormatUtil.SaveTwoAndSubZero(sypfl_num));
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 5:33
     * @Description: 通过自定义参数获取企业废水许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEntWaterPermittedFlowLimitInfoByParamMap",method = RequestMethod.POST)
    public Object getEntWaterPermittedFlowLimitInfoByParamMap(@RequestJson(value="paramsjson",required = true)Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = entPermittedFlowLimitValueService.getEntWaterPermittedFlowLimitInfoByParamMap(jsonObject);
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
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 新增企业废水许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addEntWaterPermittedFlowLimitInfo", method = RequestMethod.POST)
    public Object addEntWaterPermittedFlowLimitInfo(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            String pollutantcode = jsonObject.get("pollutantcode").toString();
            String pollutionid = jsonObject.get("pollutionid").toString();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            List<Map<String,Object>> pfl_data = (List<Map<String, Object>>) jsonObject.get("flowdata");
            List<EntPermittedFlowLimitValueVO> listobj = new ArrayList<>();
            if (pfl_data.size()>0){
                for (Map<String,Object> map:pfl_data){
                    EntPermittedFlowLimitValueVO obj1 = new EntPermittedFlowLimitValueVO();
                    obj1.setPkId(UUID.randomUUID().toString());
                    obj1.setFkPollutantcode(pollutantcode);
                    obj1.setFkPollutionid(pollutionid);
                    obj1.setFkMonitorpointtype("1");
                    obj1.setTotalflow((map.get("totalflow")!=null&&!"".equals(map.get("totalflow").toString()))?Double.valueOf(map.get("totalflow").toString()):null);
                    obj1.setFlowyear(map.get("flowyear")!=null?Integer.valueOf(map.get("flowyear").toString()):null);
                    obj1.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    obj1.setUpdateuser(username);
                    listobj.add(obj1);
                }
            }
            entPermittedFlowLimitValueService.addEntWaterPermittedFlowLimitInfo(listobj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 修改企业废水许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateEntWaterPermittedFlowLimitInfo", method = RequestMethod.POST)
    public Object updateEntWaterPermittedFlowLimitInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            String pollutantcode = jsonObject.get("pollutantcode").toString();
            String pollutionid = jsonObject.get("pollutionid").toString();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            List<Map<String,Object>> pfl_data = (List<Map<String, Object>>) jsonObject.get("flowdata");
            List<EntPermittedFlowLimitValueVO> listobj = new ArrayList<>();
            if (pfl_data.size()>0){
                for (Map<String,Object> map:pfl_data){
                    EntPermittedFlowLimitValueVO obj1 = new EntPermittedFlowLimitValueVO();
                    obj1.setPkId(UUID.randomUUID().toString());
                    obj1.setFkPollutantcode(pollutantcode);
                    obj1.setFkPollutionid(pollutionid);
                    obj1.setFkMonitorpointtype("1");
                    obj1.setTotalflow((map.get("totalflow")!=null&&!"".equals(map.get("totalflow").toString()))?Double.valueOf(map.get("totalflow").toString()):null);
                    obj1.setFlowyear(map.get("flowyear")!=null?Integer.valueOf(map.get("flowyear").toString()):null);
                    obj1.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    obj1.setUpdateuser(username);
                    listobj.add(obj1);
                }
            }
            entPermittedFlowLimitValueService.updateEntWaterPermittedFlowLimitInfo(pollutionid,pollutantcode,listobj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 通过污染源ID和污染物code删除企业废水许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteEntWaterFlowInfoByIDAndCode", method = RequestMethod.POST)
    public Object deleteEntWaterFlowInfoByIDAndCode(@RequestJson(value = "pollutionid") String pollutionid,
                                                     @RequestJson(value = "pollutantcode") String pollutantcode) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("pollutantcode",pollutantcode);
            entPermittedFlowLimitValueService.deleteEntWaterFlowInfoByIDAndCode(param);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 通过污染源id和污染物获取企业废水许可排放限值信息(回显)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntWaterPermittedFlowInfoByIDAndCode", method = RequestMethod.POST)
    public Object getEntWaterPermittedFlowInfoByIDAndCode(@RequestJson(value = "pollutionid") String pollutionid,
                                     @RequestJson(value = "pollutantcode") String pollutantcode) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("pollutantcode",pollutantcode);
            List<Map<String,Object>> listdata = entPermittedFlowLimitValueService.getEntWaterPermittedFlowInfoByIDAndCode(param);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 1:24
     * @Description: 通过自定义参数查询企业企业废水许可排放限值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getEntWaterPermittedFlowInfoListPage", method = RequestMethod.POST)
    public Object getEntWaterPermittedFlowInfoListPage(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap = entPermittedFlowLimitValueService.getEntWaterPermittedFlowInfoListPage(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 下午 1:15
     * @Description: 验证排放年份是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsHaveWaterPollutantFlowYearValidByParam", method = RequestMethod.POST)
    public Object IsHaveWaterPollutantFlowYearValidByParam(
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "flowyear", required = false) Integer flowyear
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("flowyear", flowyear);
            List<Map<String, Object>> datalist = entPermittedFlowLimitValueService.IsHaveGasPollutantFlowYearValidByParam(paramMap);
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

    /**
     * @author: xsm
     * @date: 2021/09/06 0006 下午 3:00
     * @Description: 统计某个企业某一年某个污染物每月排放情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countEntFlowDataGroupByMonthByParam", method = RequestMethod.POST)
    public Object countEntFlowDataGroupByMonthByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                      @RequestJson(value = "monitorpointtype")Integer monitorpointtype,
                                                      @RequestJson(value = "pollutantcode")String pollutantcode,
                                                      @RequestJson(value = "flowyear") String flowyear) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("pollutionid",pollutionid);
            param.put("flowyear",flowyear);
            param.put("pollutantcode",pollutantcode);
            param.put("monitorpointtype",monitorpointtype);
            param.put("userid",userId);
            List<Map<String, Object>> pointlist = pollutionService.getEntPointMNDataByParam(param);
            List<String> mns = new ArrayList<>();
            if (pointlist!=null&&pointlist.size()>0){
                for (Map<String, Object> map:pointlist){
                    if (map.get("DGIMN")!=null) {
                        mns.add(map.get("DGIMN").toString());
                    }
                }
            }
            param.put("mns", mns);
            //获取该年份该企业该污染物 所有月排放数据
            Map<String, Object> result = onlineCountAlarmService.getEntPointPollutantMonthFlowDataByParam(param);
            result.remove("total");
            result.remove("lastdata");
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/06 0006 下午 3:00
     * @Description: 统计某个企业近五年某类型下单个污染物年排放
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countEntFlowDataGroupByYearByParam", method = RequestMethod.POST)
    public Object countEntFlowDataGroupByYearByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                      @RequestJson(value = "monitorpointtype")Integer monitorpointtype,
                                                      @RequestJson(value = "pollutantcode")String pollutantcode,
                                                      @RequestJson(value = "startyear") String startyear,
                                                      @RequestJson(value = "endyear") String endyear) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("pollutionid",pollutionid);
            param.put("startyear",startyear);
            param.put("endyear",endyear);
            param.put("pollutantcode",pollutantcode);
            param.put("monitorpointtype",monitorpointtype);
            param.put("userid",userId);
            List<Map<String, Object>> pointlist = pollutionService.getEntPointMNDataByParam(param);
            List<String> mns = new ArrayList<>();
            if (pointlist!=null&&pointlist.size()>0){
                for (Map<String, Object> map:pointlist){
                    if (map.get("DGIMN")!=null) {
                        mns.add(map.get("DGIMN").toString());
                    }
                }
            }
            param.put("mns", mns);
            //获取该年份该企业该污染物 所有年排放数据
            Map<String, Object> result = onlineCountAlarmService.countEntFlowDataGroupByYearByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 通过自定义参数查询企业近五年各排放污染物污染总排放许可量信息（废水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "/getEntWaterDischargeTotalListData", method = RequestMethod.POST)
    public Object getEntWaterDischargeTotalListData(@RequestJson(value = "pollutionid") String pollutionid) throws ParseException {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            //获取企业下 废水废气各排放污染物近五年的 许可排放限值
            String endtime = DataFormatUtil.getDateY(new Date());//当前年
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.YEAR, -4);
            String starttime = DataFormatUtil.getDateY(calendar.getTime()); //四年前
            param.put("startyear",starttime);
            param.put("endyear",endtime);
            Map<String, Object> resultMap = entPermittedFlowLimitValueService.getEntWaterDischargeTotalListData(param);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 通过自定义参数查询企业近五年各排放污染物污染总排放许可量信息（废水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "/getEntWaterDischargeTotalAnalysisData", method = RequestMethod.POST)
    public Object getEntWaterDischargeTotalAnalysisData(@RequestJson(value = "pollutionid") String pollutionid) throws ParseException {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            //获取企业下 废水废气各排放污染物近五年的 许可排放限值
            String endtime = DataFormatUtil.getDateY(new Date());//当前年
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.YEAR, -4);
            String starttime = DataFormatUtil.getDateY(calendar.getTime()); //四年前
            param.put("startyear",starttime);
            param.put("endyear",endtime);
            Map<String, Object> resultMap = entPermittedFlowLimitValueService.getEntWaterDischargeTotalAnalysisData(param);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

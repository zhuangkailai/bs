package com.tjpu.sp.controller.envhousekeepers.gasdischargetotal;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.gasdischargetotal.GasDischargeTotalVO;
import com.tjpu.sp.service.envhousekeepers.gasdischargetotal.GasDischargeTotalService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.*;


/**
 * @author: xsm
 * @date: 2021/08/14 0014 下午 14:46
 * @Description: 企业大气污染总排放许可量控制层
 */
@RestController
@RequestMapping("gasDischargeTotal")
public class GasDischargeTotalController {

    @Autowired
    private GasDischargeTotalService gasDischargeTotalService;

    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 通过自定义参数查询企业大气污染总排放许可量初始化信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getGasDischargeTotalListPage", method = RequestMethod.POST)
    public Object getGasDischargeTotalListPage(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap = gasDischargeTotalService.getGasDischargeTotalListPage(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 通过自定义参数查询企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getGasDischargeTotalByParamMap", method = RequestMethod.POST)
    public Object getGasDischargeTotalByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = gasDischargeTotalService.getGasDischargeTotalByParamMap(jsonObject);
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
     * @Description: 新增企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addGasDischargeTotal", method = RequestMethod.POST)
    public Object addGasDischargeTotal(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            String pollutantcode = jsonObject.get("pollutantcode").toString();
            String pollutionid = jsonObject.get("pollutionid").toString();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            List<Map<String,Object>> pfl_data = (List<Map<String, Object>>) jsonObject.get("flowdata");
            List<GasDischargeTotalVO> listobj = new ArrayList<>();
            if (pfl_data.size()>0){
                for (Map<String,Object> map:pfl_data){
                    GasDischargeTotalVO obj1 = new GasDischargeTotalVO();
                    obj1.setPkId(UUID.randomUUID().toString());
                    obj1.setFkPollutantcode(pollutantcode);
                    obj1.setFkPollutionid(pollutionid);
                    obj1.setCounttype((short)1);
                    obj1.setDischargevalue((map.get("yzz_dischargevalue")!=null&&!"".equals(map.get("yzz_dischargevalue").toString()))?Double.valueOf(map.get("yzz_dischargevalue").toString()):null);
                    obj1.setPollutanttype("2");
                    obj1.setYear(map.get("year")!=null?Integer.valueOf(map.get("year").toString()):null);
                    obj1.setUpdatetime(new Date());
                    obj1.setUpdateuser(username);
                    listobj.add(obj1);
                    GasDischargeTotalVO obj2 = new GasDischargeTotalVO();
                    obj2.setPkId(UUID.randomUUID().toString());
                    obj2.setFkPollutantcode(pollutantcode);
                    obj2.setFkPollutionid(pollutionid);
                    obj2.setCounttype((short)2);
                    obj2.setDischargevalue((map.get("wzz_dischargevalue")!=null&&!"".equals(map.get("wzz_dischargevalue").toString()))?Double.valueOf(map.get("wzz_dischargevalue").toString()):null);
                    obj2.setPollutanttype("2");
                    obj2.setYear(map.get("year")!=null?Integer.valueOf(map.get("year").toString()):null);
                    obj2.setUpdatetime(new Date());
                    obj2.setUpdateuser(username);
                    listobj.add(obj2);
                }
            }
            gasDischargeTotalService.insert(listobj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 通过id获取企业大气污染总排放许可量信息(回显)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getGasDischargeTotalByID", method = RequestMethod.POST)
    public Object getStorageTankByID(@RequestJson(value = "pollutionid") String pollutionid,
                                     @RequestJson(value = "pollutantcode") String pollutantcode) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("pollutantcode",pollutantcode);
            List<Map<String,Object>> listdata = gasDischargeTotalService.getGasDischargeTotalsByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 修改企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateGasDischargeTotal", method = RequestMethod.POST)
    public Object updateGasDischargeTotal(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            String pollutantcode = jsonObject.get("pollutantcode").toString();
            String pollutionid = jsonObject.get("pollutionid").toString();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            List<Map<String,Object>> pfl_data = (List<Map<String, Object>>) jsonObject.get("flowdata");
            List<GasDischargeTotalVO> listobj = new ArrayList<>();
            if (pfl_data.size()>0){
                for (Map<String,Object> map:pfl_data){
                    GasDischargeTotalVO obj1 = new GasDischargeTotalVO();
                    obj1.setPkId(UUID.randomUUID().toString());
                    obj1.setFkPollutantcode(pollutantcode);
                    obj1.setFkPollutionid(pollutionid);
                    obj1.setCounttype((short)1);
                    obj1.setDischargevalue((map.get("yzz_dischargevalue")!=null&&!"".equals(map.get("yzz_dischargevalue").toString()))?Double.valueOf(map.get("yzz_dischargevalue").toString()):null);
                    obj1.setPollutanttype("2");
                    obj1.setYear(map.get("year")!=null?Integer.valueOf(map.get("year").toString()):null);
                    obj1.setUpdatetime(new Date());
                    obj1.setUpdateuser(username);
                    listobj.add(obj1);
                    GasDischargeTotalVO obj2 = new GasDischargeTotalVO();
                    obj2.setPkId(UUID.randomUUID().toString());
                    obj2.setFkPollutantcode(pollutantcode);
                    obj2.setFkPollutionid(pollutionid);
                    obj2.setCounttype((short)2);
                    obj2.setDischargevalue((map.get("wzz_dischargevalue")!=null&&!"".equals(map.get("wzz_dischargevalue").toString()))?Double.valueOf(map.get("wzz_dischargevalue").toString()):null);
                    obj2.setPollutanttype("2");
                    obj2.setYear(map.get("year")!=null?Integer.valueOf(map.get("year").toString()):null);
                    obj2.setUpdatetime(new Date());
                    obj2.setUpdateuser(username);
                    listobj.add(obj2);
                }
            }
            gasDischargeTotalService.updateByPrimaryKey(pollutionid,pollutantcode,listobj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:12
     * @Description: 通过污染源ID和污染物code删除企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteGasDischargeTotalByIDAndCode", method = RequestMethod.POST)
    public Object deleteGasDischargeTotalByIDAndCode(@RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "pollutantcode") String pollutantcode) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("pollutantcode",pollutantcode);
            gasDischargeTotalService.deleteByPollutionIDAndPollutantCode(param);
            return AuthUtil.parseJsonKeyToLower("success", null);
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
    @RequestMapping(value = "/IsHaveGasPollutantFlowYearValidByParam", method = RequestMethod.POST)
    public Object IsHaveGasPollutantFlowYearValidByParam(
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "flowyear", required = false) Integer flowyear
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("flowyear", flowyear);
            List<Map<String, Object>> datalist = gasDischargeTotalService.IsHaveGasPollutantFlowYearValidByParam(paramMap);
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
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 通过自定义参数查询企业近五年各排放污染物污染总排放许可量信息（废气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "/getEntGasDischargeTotalListData", method = RequestMethod.POST)
    public Object getEntGasDischargeTotalListData(@RequestJson(value = "pollutionid") String pollutionid) throws ParseException {
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
            Map<String, Object> resultMap = gasDischargeTotalService.getEntGasDischargeTotalListData(param);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 通过自定义参数查询企业近五年各排放污染物污染总排放许可量信息（废气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "/getEntGasDischargeTotalAnalysisData", method = RequestMethod.POST)
    public Object getEntGasDischargeTotalAnalysisData(@RequestJson(value = "pollutionid") String pollutionid) throws ParseException {
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
            Map<String, Object> resultMap = gasDischargeTotalService.getEntGasDischargeTotalAnalysisData(param);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

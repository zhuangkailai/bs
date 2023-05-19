package com.tjpu.sp.controller.environmentalprotection.limitproduction;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.limitproduction.LimitProductionDetailInfoVO;
import com.tjpu.sp.model.environmentalprotection.limitproduction.LimitProductionInfoVO;
import com.tjpu.sp.service.environmentalprotection.limitproduction.LimitProductionInfoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: chengzq
 * @date: 2019/6/21 0021 上午 9:02
 * @Description: 排口限产信息控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("LimitProductionInfo")
public class LimitProductionInfoController {

    @Autowired
    private LimitProductionInfoService limitProductionInfoService;

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 9:07
     * @Description: 通过自定义参数获取排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getLimitProductionInfoByParamMap",method = RequestMethod.POST)
    public Object getLimitProductionInfoByParamMap(@RequestJson(value="paramsjson",required = true)Object paramsjson) throws Exception {
        try {
            Map<String,Object> resultMap=new HashMap<>();
            Map<String,Object> paramMap =(Map<String,Object>) JSONObject.fromObject(paramsjson);
            Object pagenum = paramMap.get("pagenum");
            Object pagesize = paramMap.get("pagesize");

            if(pagenum!=null && pagesize!=null ){
                PageHelper.startPage(Integer.valueOf(pagenum.toString()),Integer.valueOf(pagesize.toString()));
            }
            List<LimitProductionInfoVO> limitProductionInfoByParamMap = limitProductionInfoService.getLimitProductionInfoByParamMap(paramMap);
            PageInfo<LimitProductionInfoVO> pageInfo = new PageInfo<>(limitProductionInfoByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("total",total);
            resultMap.put("primarykey","pk_id");
            resultMap.put("datalist",limitProductionInfoByParamMap);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 9:44
     * @Description: 新增排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [adddatalist, session]
     * @throws:
     */
    @RequestMapping(value = "addLimitProductionInfo",method = RequestMethod.POST)
    public Object addLimitProductionInfo(@RequestJson(value="adddataform",required = true)Object adddataform ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(adddataform);
            LimitProductionInfoVO limitProductionInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new LimitProductionInfoVO());
            Date now = new Date();

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);

            String pkid = UUID.randomUUID().toString();
            limitProductionInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(now));
            limitProductionInfoVO.setUpdateuser(username);
            limitProductionInfoVO.setPkId(pkid);

            Set<Object> limitDetail = limitProductionInfoVO.getLimitDetail();
            List<LimitProductionDetailInfoVO> data=new ArrayList<>();
            for (Object o : limitDetail) {
                LimitProductionDetailInfoVO detail=new LimitProductionDetailInfoVO();
                detail.setFkOutputid(o.toString());
                detail.setFkLimitproductionid(pkid);
                detail.setPkId(UUID.randomUUID().toString());
                detail.setUpdatetime(now);
                detail.setUpdateuser(username);
                data.add(detail);
            }
            limitProductionInfoService.insert(limitProductionInfoVO,data);

            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 9:57
     * @Description: 通过id获取排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, version]
     * @throws:
     */
    @RequestMapping(value = "getLimitProductionInfoByID",method = RequestMethod.POST)
    public Object getLimitProductionInfoByID(@RequestJson(value="id",required = false)String id) throws Exception {
        try {
            LimitProductionInfoVO limitProductionInfoByID = limitProductionInfoService.getLimitProductionInfoByID(id);
            return AuthUtil.parseJsonKeyToLower("success",limitProductionInfoByID);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 10:13
     * @Description: 修改排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updatedataform, session]
     * @throws:
     */
    @RequestMapping(value = "updateLimitProductionInfo",method = RequestMethod.POST)
    public Object updateLimitProductionInfo(@RequestJson(value="updatedataform",required = true)Object updatedataform ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updatedataform);
            LimitProductionInfoVO limitProductionInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new LimitProductionInfoVO());
            Date now = new Date();

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            limitProductionInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(now));
            limitProductionInfoVO.setUpdateuser(username);

            Set<Object> limitDetail = limitProductionInfoVO.getLimitDetail();
            List<LimitProductionDetailInfoVO> data=new ArrayList<>();
            for (Object o : limitDetail) {
                LimitProductionDetailInfoVO detail=new LimitProductionDetailInfoVO();
                detail.setFkOutputid(o.toString());
                detail.setFkLimitproductionid(limitProductionInfoVO.getPkId());
                detail.setPkId(UUID.randomUUID().toString());
                detail.setUpdatetime(now);
                detail.setUpdateuser(username);
                data.add(detail);
            }
            limitProductionInfoService.updateByPrimaryKey(limitProductionInfoVO,data);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 10:14
     * @Description: 通过id删除排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteLimitProductionInfoByID",method = RequestMethod.POST)
    public Object deleteLimitProductionInfoByID(@RequestJson(value="id",required = true)String id) throws Exception {
        try {

            limitProductionInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 10:14
     * @Description: 通过id查询排口限产详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getLimitProductionInfoDetailByID",method = RequestMethod.POST)
    public Object getLimitProductionInfoDetailByID(@RequestJson(value="id",required = false)String id) throws Exception {
        try {
            return AuthUtil.parseJsonKeyToLower("success",limitProductionInfoService.getLimitProductionInfoDetailByID(id));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    /**
     * @author: chengzq
     * @date: 2019/6/19 0019 下午 2:13
     * @Description: 通过自定义参数导出排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sysmodel, session]
     * @throws:
     */
    @RequestMapping(value = "exportLimitProductionInfoByParamMap",method = RequestMethod.POST)
    public Object exportLimitProductionInfoByParamMap(@RequestJson(value="paramsjson",required = true)Object paramsjson, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            List headers=new ArrayList<>();
            headers.add("企业名称");
            headers.add("开始时间");
            headers.add("结束时间");
            headers.add("排放口类型");
            headers.add("停产排放口");
            headers.add("限产百分比(%)");
            headers.add("错峰生产时间段");

            List headersField=new ArrayList<>();
            headersField.add("pollutionname");
            headersField.add("executestarttime");
            headersField.add("executeendtime");
            headersField.add("fkmonitorpointtype");
            headersField.add("limitdetail");
            headersField.add("limitproductionpercent");
            headersField.add("staggeringpeaktime");

            JSONObject jsonObject = JSONObject.fromObject(getLimitProductionInfoByParamMap(paramsjson));
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 =JSONObject.fromObject(data);
            Object data1 =jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(data1);


            List<Map<String,Object>> result=new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                Map map = (Map) jsonArray.get(i);
                Object limitdetail = map.get("limitdetail");
                String staggeringPeakStartTimePoint ="" ;
                String staggeringPeakEndTimePoint ="" ;
                if(limitdetail!=null){
                    JSONArray jsonArray1 = JSONArray.fromObject(limitdetail);
                    String join = jsonArray1.join(",");
                    map.put("limitdetail",join.replaceAll("\"",""));
                }else{
                    map.put("limitdetail","");
                }
                if(map.get("staggeringpeakstarttimepoint")!=null){
                    staggeringPeakStartTimePoint=map.get("staggeringpeakstarttimepoint").toString()+"点";
                }
                if(map.get("staggeringpeakendtimepoint")!=null){
                    staggeringPeakEndTimePoint=map.get("staggeringpeakendtimepoint").toString()+"点";
                }
                map.put("staggeringpeaktime",staggeringPeakStartTimePoint+"-"+staggeringPeakEndTimePoint);
                result.add(map);
            }



            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, result, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("排口限产信息",response,request,bytesForWorkBook);

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 5:07
     * @Description: 验证同一污染源，同一类型监测点在表内有没有时间重叠的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "isHaveData",method = RequestMethod.POST)
    public Object isHaveData(@RequestJson(value="paramsjson",required = true)Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            LimitProductionInfoVO limitProductionInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new LimitProductionInfoVO());

            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("pollutionid",limitProductionInfoVO.getFkPollutionid());
            paramMap.put("monitortype",limitProductionInfoVO.getFkMonitorpointtype());
            paramMap.put("executestarttime",limitProductionInfoVO.getExecutestarttime());
            paramMap.put("executeendtime",limitProductionInfoVO.getExecuteendtime());
            int haveData = limitProductionInfoService.isHaveData(paramMap);

            if(haveData==0){
                return AuthUtil.parseJsonKeyToLower("success", "no");
            } else {    //已经有了不添加
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}

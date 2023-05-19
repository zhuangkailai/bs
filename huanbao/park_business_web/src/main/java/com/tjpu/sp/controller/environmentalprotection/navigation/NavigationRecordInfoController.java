package com.tjpu.sp.controller.environmentalprotection.navigation;


import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.navigation.NavigationRecordInfoVO;
import com.tjpu.sp.service.environmentalprotection.navigation.NavigationRecordInfoService;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xsm
 * @date: 2020年8月31日 下午13:47
 * @Description:走航记录信息处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("navigationRecordInfo")
@Api(value = "走航记录信息处理类", tags = "走航记录信息处理类")
public class NavigationRecordInfoController {
    @Autowired
    private NavigationRecordInfoService navigationRecordInfoService;

    @Autowired
    private RestTemplate restTemplate;
    private static final String SERVICE_URL = "http://py-sidecar/getNavigationDataByName";

    /**
     * @Author: xsm
     * @Date: 2020/08/31 0031 下午 1:52
     * @Description: 自定义查询条件查询走航记录信息列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getNavigationRecordInfosByParamMap", method = RequestMethod.POST)
    public Object getNavigationRecordInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = navigationRecordInfoService.getNavigationRecordInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 下午 1:52
     * @Description: 新增走航记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addNavigationRecordInfo", method = RequestMethod.POST)
    public Object addNavigationRecordInfo(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            NavigationRecordInfoVO NavigationRecordInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new NavigationRecordInfoVO());
            NavigationRecordInfoVO.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            NavigationRecordInfoVO.setUpdateuser(username);
            NavigationRecordInfoVO.setPkNavigationid(UUID.randomUUID().toString());
            navigationRecordInfoService.insert(NavigationRecordInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 下午 1:52
     * @Description: 通过id获取走航记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getNavigationRecordInfoByID", method = RequestMethod.POST)
    public Object getNavigationRecordInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            NavigationRecordInfoVO NavigationRecordInfoVO = navigationRecordInfoService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", NavigationRecordInfoVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 下午 1:52
     * @Description: 修改走航记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateNavigationRecordInfo", method = RequestMethod.POST)
    public Object updateNavigationRecordInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            NavigationRecordInfoVO NavigationRecordInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new NavigationRecordInfoVO());
            NavigationRecordInfoVO.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            NavigationRecordInfoVO.setUpdateuser(username);
            navigationRecordInfoService.updateByPrimaryKey(NavigationRecordInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/08/31 0031 下午 1:52
     * @Description: 通过id删除走航记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteNavigationRecordInfoByID", method = RequestMethod.POST)
    public Object deleteNavigationRecordInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            navigationRecordInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/08/31 0031 下午 1:52
     * @Description: 通过id获取走航记录信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getNavigationRecordInfoDetailByID", method = RequestMethod.POST)
    public Object getNavigationRecordInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = navigationRecordInfoService.getNavigationRecordInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 下午 5:05
     * @Description: 根据月份获取该月每日走航信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getNavigationDataGroupByNavigationDateByMonth", method = RequestMethod.POST)
    public Object getNavigationDataGroupByNavigationDateByMonth(@RequestJson(value = "starttime") String starttime,
                                                                @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime",starttime);
            paramMap.put("endtime",endtime);
            List<Map<String, Object>> resultlist = navigationRecordInfoService.getNavigationDataGroupByNavigationDateByMonth(paramMap);
            List<String> ymds = DataFormatUtil.getYMDBetween(starttime, endtime);
            ymds.add(endtime);
            List<Map<String, Object>> result = new ArrayList<>();
            for (String str : ymds) {
                Map<String, Object> map = new HashMap<>();
                map.put("monitortime", str);
                map.put("flag", 0);
                if (resultlist != null && resultlist.size() > 0) {
                    for (Map<String, Object> obj : resultlist) {
                        if (str.equals(obj.get("NavigationDate").toString())) {
                            map.put("flag", obj.get("flag"));
                        }
                    }
                }
                result.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 下午 5:05
     * @Description: 根据日期获取该日期所有走航信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getNavigationTimesByNavigationDate", method = RequestMethod.POST)
    public Object getNavigationTimesByNavigationDate(@RequestJson(value = "navigationdate") String navigationdate) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime",navigationdate);
            paramMap.put("endtime",navigationdate);
            List<Map<String, Object>> result = navigationRecordInfoService.getNavigationDataByNavigationDate(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/01 0001 下午 1:28
     * @Description: 获取时间范围内的走航污染物平均浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/countNavigationPollutantDataByParamMap", method = RequestMethod.POST)
    public Object countNavigationPollutantDataByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                            @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Date startdate = DataFormatUtil.getDateYMDHMS(starttime+":00");
            Date enddate = DataFormatUtil.getDateYMDHMS(endtime+":59");
            List<Map<String,Object>> result = navigationRecordInfoService.countNavigationPollutantDataByMonitorTimes(dgimn,startdate,enddate);
            List<Map<String, Object>> collect = result.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("value").toString())).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/01 0001 下午 6:11
     * @Description: 获取时间范围内的走航污染物浓度排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countNavigationPollutantRankByParamMap", method = RequestMethod.POST)
    public Object countNavigationPollutantRankByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Date startdate = DataFormatUtil.getDateYMDHMS(starttime+":00");
            Date enddate = DataFormatUtil.getDateYMDHMS(endtime+":59");
            List<Map<String,Object>> result = navigationRecordInfoService.countNavigationPollutantDataByMonitorTimes(dgimn,startdate,enddate);
            List<Map<String, Object>> collect = result.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("value").toString())).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/02 0002 上午 8:45
     * @Description: 根据MN号和时间范围查询出所有的走航实时数据(算污染物之和)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getNavigationRealTimeSumDataByParam", method = RequestMethod.POST)
    public Object getNavigationRealTimeSumDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                      @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Date startdate = DataFormatUtil.getDateYMDHMS(starttime+":00");
            Date enddate = DataFormatUtil.getDateYMDHMS(endtime+":59");
            if (pagenum==null||pagenum==0){
                pagenum = 1;
            }
            Map<String,Object> result = navigationRecordInfoService.getNavigationRealTimeSumDataByParam(dgimn,startdate,enddate,pagenum);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/04 0004 下午 2:51
     * @Description: 根据MN号和时间范围查询出所有的走航实时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getNavigationRealTimeDataByParam", method = RequestMethod.POST)
    public Object getNavigationRealTimeDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                   @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                   @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                                   @RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Date startdate = DataFormatUtil.getDateYMDHMS(starttime+":00");
            Date enddate = DataFormatUtil.getDateYMDHMS(endtime+":59");
            if (pagenum==null||pagenum==0){
                pagenum = 1;
            }
            Map<String,Object> result = navigationRecordInfoService.getNavigationRealTimeDataByParam(dgimn,startdate,enddate,pagenum,pollutantcodes);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2021/05/10 0010 下午 1:11
     * @Description: 自定义查询条件查询走航数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getNavigationRecordDatasByParamMap", method = RequestMethod.POST)
    public Object getNavigationRecordDatasByParamMap(@RequestJson(value = "titlename", required = false) String titlename) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> listdata = new ArrayList<>();
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> result0 = new HashMap<>();
            result0.put("observationtime","2021-05-09T09:10:00");
            result0.put("longitude",117.133213);
            result0.put("latitude",28.917146);
            result.put("pointdata",result0);

            Map<String, Object> result00 = new HashMap<>();
            result00.put("elevationVariance",8.19057690214798);
            result00.put("latitudinalVariance",0.0018770929483514316);
            result00.put("longitudinalVariance",0.00041826888894624372);
            result00.put("time","2021-05-09T05:10:00");
            result00.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_00 = new HashMap<>();
            result_00.put("latitude",28.914269);
            result_00.put("longitude",117.133892);
            result_00.put("elevation",40.6001);
            result_00.put("typename","GisPoint");
            result00.put("point",result_00);
            listdata.add(result00);

          /*  Map<String, Object> result1 = new HashMap<>();
            result1.put("elevationVariance",8.19057690214798);
            result1.put("latitudinalVariance",0.0018770929483514316);
            result1.put("longitudinalVariance",0.00041826888894624372);
            result1.put("time","2021-05-09T05:10:00");
            result1.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_1 = new HashMap<>();
            result_1.put("latitude",28.917077);
            result_1.put("longitude",117.133114);
            result_1.put("elevation",41.6001);
            result_1.put("typename","GisPoint");
            result1.put("point",result_1);
            listdata.add(result1);*/

            Map<String, Object> result2 = new HashMap<>();
            result2.put("elevationVariance",8.5162827100501985);
            result2.put("latitudinalVariance",0.0018966145517445891);
            result2.put("longitudinalVariance",0.00042037018773518329);
            result2.put("time","2021-05-09T05:11:00");
            result2.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_2 = new HashMap<>();
            result_2.put("latitude",28.915602);
            result_2.put("longitude",117.133613);
            result_2.put("elevation",41.75945);
            result_2.put("typename","GisPoint");
            result2.put("point",result_2);

            listdata.add(result2);

            Map<String, Object> result3 = new HashMap<>();
            result3.put("elevationVariance",7.7888605527381216);
            result3.put("latitudinalVariance",0.001931357662861369);
            result3.put("longitudinalVariance",0.00043278824516484814);
            result3.put("time","2021-05-09T05:12:00");
            result3.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_3 = new HashMap<>();
            result_3.put("latitude",28.917105);
            result_3.put("longitude",117.133162);
            result_3.put("elevation",41.9777);
            result_3.put("typename","GisPoint");
            result3.put("point",result_3);

            listdata.add(result3);

            Map<String, Object> result4 = new HashMap<>();
            result4.put("elevationVariance",8.3558501648545622);
            result4.put("latitudinalVariance",0.0019703585898687586);
            result4.put("longitudinalVariance",0.0004430315264706514);
            result4.put("time","2021-05-09T05:13:00");
            result4.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_4 = new HashMap<>();
            result_4.put("latitude",28.917612);
            result_4.put("longitude",117.134578);
            result_4.put("elevation",41.80235);
            result_4.put("typename","GisPoint");
            result4.put("point",result_4);

            listdata.add(result4);

            Map<String, Object> result5 = new HashMap<>();
            result5.put("elevationVariance",8.4951117449978231);
            result5.put("latitudinalVariance",0.0020084314835167008);
            result5.put("longitudinalVariance",0.00045475230286473264);
            result5.put("time","2021-05-09T05:14:00");
            result5.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_5 = new HashMap<>();
            result_5.put("latitude",28.91919);
            result_5.put("longitude",117.133183);
            result_5.put("elevation",41.6288);
            result_5.put("typename","GisPoint");
            result5.put("point",result_5);

            listdata.add(result5);

            Map<String, Object> result6 = new HashMap<>();
            result6.put("elevationVariance",8.9458480859838);
            result6.put("latitudinalVariance",0.0020379904067279165);
            result6.put("longitudinalVariance",0.00046797012827235341);
            result6.put("time","2021-05-09T05:15:00");
            result6.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_6 = new HashMap<>();
            result_6.put("latitude",28.91765);
            result_6.put("longitude",117.131381);
            result_6.put("elevation",41.38235);
            result_6.put("typename","GisPoint");
            result6.put("point",result_6);

            listdata.add(result6);

            Map<String, Object> result7 = new HashMap<>();
            result7.put("elevationVariance",9.4215341605016771);
            result7.put("latitudinalVariance",0.0020640300127348498);
            result7.put("longitudinalVariance",0.00048449866322643285);
            result7.put("time","2021-05-09T05:16:00");
            result7.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_7 = new HashMap<>();
            result_7.put("latitude",28.918363);
            result_7.put("longitude",117.12945);
            result_7.put("elevation",41.78075);
            result_7.put("typename","GisPoint");
            result7.put("point",result_7);

            listdata.add(result7);

           /* Map<String, Object> result8 = new HashMap<>();
            result8.put("elevationVariance",9.23277786800917);
            result8.put("latitudinalVariance",0.00209004953531648);
            result8.put("longitudinalVariance",0.00050147424944336886);
            result8.put("time","2021-05-09T05:17:00");
            result8.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_8 = new HashMap<>();
            result_8.put("latitude",28.918396);
            result_8.put("longitude",117.132652);
            result_8.put("elevation",41.7878);
            result_8.put("typename","GisPoint");
            result8.put("point",result_8);

            listdata.add(result8);

            Map<String, Object> result9 = new HashMap<>();
            result9.put("elevationVariance",8.9882739271786747);
            result9.put("latitudinalVariance",0.0021306139102916367);
            result9.put("longitudinalVariance",0.00051588168556382385);
            result9.put("time","2021-05-09T05:18:00");
            result9.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_9 = new HashMap<>();
            result_9.put("latitude",28.918601);
            result_9.put("longitude",117.131563);
            result_9.put("elevation",42.1459);
            result_9.put("typename","GisPoint");
            result9.put("point",result_9);

            listdata.add(result9);*/

            /*Map<String, Object> result10 = new HashMap<>();
            result10.put("elevationVariance",9.0563244518678729);
            result10.put("latitudinalVariance",0.0021756720370005134);
            result10.put("longitudinalVariance",0.00052773903049215844);
            result10.put("time","2021-05-09T05:19:00");
            result10.put("typename","SummaryGisTrackDataStep");
            Map<String, Object> result_10 = new HashMap<>();
            result_10.put("latitude",27.9072334685929);
            result_10.put("longitude",116.767759315869);
            result_10.put("elevation",41.78315);
            result_10.put("typename","GisPoint");
            result10.put("point",result_10);

            listdata.add(result10);*/
            result.put("navigationdata",listdata);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "pythonnavigationdata", method = RequestMethod.POST)
    public Object pythonnavigationdata(@RequestJson(value = "titlename", required = false) String titlename) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("titlename",titlename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(paramMap, headers);
        ResponseEntity<String> response = null;
        int i =0;
        while (response==null){
            if (i<5) {
                try {
                    response = restTemplate.postForEntity(SERVICE_URL, request, String.class);
                } catch (Exception e) {
                    //Object obj= e.getCause().getMessage();
                    response = null;
                }
            }else{
                break;
            }
            i++;
        }
        Object result;
        if (response!=null){
            result = response.getBody();
        }else{
            result = "null";
        }
        if (result!=null&&!"null".equals(result.toString())){
            JSON jsonObject = JSON.parseObject(response.getBody());
            //字符串转json 会出现精度缺失
            //JSONObject jsonObject = JSONObject.fromObject(response.getBody().toString());
            //return restTemplate.getForEntity("http://pysidecar/login?gradeID=001", String.class).getBody();
            return AuthUtil.parseJsonKeyToLower("success", jsonObject);
        }else{
            return AuthUtil.parseJsonKeyToLower("success", new HashMap<>());
        }
    }

    /*HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        Map<String, Object> map = new HashMap<>();
        map.put("titlename",titlename);
        HttpEntity<String> request = new HttpEntity<>(com.alibaba.fastjson.JSONObject.toJSONString(map), headers);
        ResponseEntity<String> response = restTemplate.postForEntity( url, request , String.class );*/
}

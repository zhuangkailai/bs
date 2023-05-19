package com.tjpu.sp.controller.environmentalprotection.petition;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.environmentalprotection.petition.PetitionInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2019/6/20 0020 下午 7:23
 * @Description: 投诉举报信息控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("petitioninfo")
public class PetitionController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private PetitionInfoService petitionInfoService;


    private String sysmodel = "complaintManagement";
    private String pk_id = "pk_id";
    private String listfieldtype = "list-petitioninfo";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;
    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:28
     * @Description: 获取投诉举报初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @throws:
     */
    @RequestMapping(value = "/getPetitionInfoListPage", method = RequestMethod.POST)
    public Object getPetitionInfoListPage(HttpServletRequest request ) {
        try {
            //获取userid

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("datasource", datasource);

            // 获取token
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListByParam(param);
            return resultList;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:29
     * @Description: 通过自定义参数获取投诉举报信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [map]
     * @throws:
     */
    @RequestMapping(value = "/getPetitionInfoByParamMap", method = RequestMethod.POST)
    public Object getPetitionInfoByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = new JSONObject();
            if (map != null) {
                paramMap = JSONObject.fromObject(map);
            }
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);

            // 获取token

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListData(param);
            return resultList;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:29
     * @Description: 获取投诉举报新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getPetitionInfoAddPage", method = RequestMethod.POST)
    public Object getPetitionInfoAddPage() {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            List<Map<String, Object>> tousu = new ArrayList<>();
            List<Map<String, Object>> huifu = new ArrayList<>();
            Map<String, Object> result = new HashMap<>();
            result.put("投诉信息", tousu);
            result.put("回复信息", huifu);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data = jsonObject.get("data");
            if (data != null) {
                JSONObject jsonObject1 = JSONObject.fromObject(data);
                Object addcontroldata = jsonObject1.get("addcontroldata");
                JSONArray jsonArray = JSONArray.fromObject(addcontroldata);
                editData(jsonArray, tousu, huifu);
                jsonObject1.put("addcontroldata", result);
                jsonObject.put("data", jsonObject1);
            }
            return jsonObject;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:29
     * @Description: 新增投诉举报信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addPetitionInfo", method = RequestMethod.POST)
    public Object addPetitionInfo(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            formdata.put("status", CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getCode());
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:30
     * @Description: 通过id获取投诉举报修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getPetitionInfoUpdatePageByID", method = RequestMethod.POST)
    public Object getPetitionInfoUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            List<Map<String, Object>> tousu = new ArrayList<>();
            List<Map<String, Object>> huifu = new ArrayList<>();
            Map<String, Object> result = new HashMap<>();
            result.put("投诉信息", tousu);
            result.put("回复信息", huifu);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data = jsonObject.get("data");
            if (data != null) {
                JSONObject jsonObject1 = JSONObject.fromObject(data);
                Object addcontroldata = jsonObject1.get("editcontroldata");
                JSONArray jsonArray = JSONArray.fromObject(addcontroldata);
                editData(jsonArray, tousu, huifu);
                jsonObject1.put("editcontroldata", result);
                jsonObject.put("data", jsonObject1);
            }
            return jsonObject;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:30
     * @Description: 修改投诉举报信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updatePetitionInfo", method = RequestMethod.POST)
    public Object updatePetitionInfo(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            // 获取token
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:31
     * @Description: 通过id删除投诉举报信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deletePetitionInfoByID", method = RequestMethod.POST)
    public Object deletePetitionInfoByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {

            petitionInfoService.deleteByPrimaryKey(id);

            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/20 0020 下午 7:31
     * @Description: 通过id查询投诉举报详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getPetitionInfoDetailByID", method = RequestMethod.POST)
    public Object getPetitionInfoDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            List<Map<String, Object>> tousu = new ArrayList<>();
            List<Map<String, Object>> huifu = new ArrayList<>();
            Map<String, Object> result = new HashMap<>();
            result.put("投诉信息", tousu);
            result.put("回复信息", huifu);

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);

            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object detaildata = jsonObject1.get("detaildata");

            JSONArray jsonArray = JSONArray.fromObject(detaildata);
            DetailData(jsonArray, tousu, huifu);
            jsonObject1.put("detaildata", result);
            jsonObject.put("data", jsonObject1);

            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/22 0022 下午 1:19
     * @Description: 新增，修改返回页面数据修改
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [jsonArray, tousu, huifu, files]
     * @throws:
     */
    private void editData(JSONArray jsonArray, List<Map<String, Object>> tousu, List<Map<String, Object>> huifu) {
        for (Object o : jsonArray) {
            Map map = (Map) o;
            Object label = map.get("label");
            Object placeholder = map.get("placeholder");

            if (label != null) {
                String s = label.toString();
                if (s.contains("投诉信息_")) {
                    String label1 = s.replaceAll("投诉信息_", "");
                    map.put("label", label1);
                    if (placeholder != null) {
                        String s1 = placeholder.toString();
                        String placeholder1 = s1.replaceAll("投诉信息_", "");
                        map.put("placeholder", placeholder1);
                    }
                    tousu.add(map);
                } else if (s.contains("回复信息_")) {
                    String label1 = s.replaceAll("回复信息_", "");
                    if (placeholder != null) {
                        String s1 = placeholder.toString();
                        String placeholder1 = s1.replaceAll("投诉信息_", "");
                        map.put("placeholder", placeholder1);
                    }
                    map.put("label", label1);
                    huifu.add(map);
                }
            }
            //污染主要来源
            if(map.get("name")!=null && "pollution".equals(map.get("name").toString())){
                map.put("collapsetags",false);
            }
            //湿度
            if(map.get("name")!=null && "humidity".equals(map.get("name").toString())){
                map.put("appendtxt","%");
                map.put("type","texttxtappend");
            }
            //气温
            if(map.get("name")!=null && "airtemperature".equals(map.get("name").toString())){
                map.put("appendtxt","℃");
                map.put("type","texttxtappend");
            }
            //风速
            if(map.get("name")!=null && "windspeed".equals(map.get("name").toString())){
                map.put("appendtxt","m/s");
                map.put("type","texttxtappend");
            }//气压
            if(map.get("name")!=null && "atmosphericpressure".equals(map.get("name").toString())){
                map.put("appendtxt","pa");
                map.put("type","texttxtappend");
            }
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/22 0022 下午 1:19
     * @Description: 详情返回数据修改
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [jsonArray, tousu, huifu, files]
     * @throws:
     */
    private void DetailData(JSONArray jsonArray, List<Map<String, Object>> tousu, List<Map<String, Object>> huifu) {
        for (Object o : jsonArray) {
            Map map = (Map) o;
            Object label = map.get("label");
            if (label != null) {
                String s = label.toString();
                if (s.contains("投诉信息_")) {
                    String label1 = s.replaceAll("投诉信息_", "");
                    map.put("label", label1);
                    tousu.add(map);
                } else if (s.contains("回复信息_")) {
                    String label1 = s.replaceAll("回复信息_", "");
                    map.put("label", label1);
                    huifu.add(map);
                }
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/24 0024 下午 6:48
     * @Description: 根据监测时间和恶臭点位MN号获取投诉信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getPetitionAndStenchOnlineListDataByParamMap", method = RequestMethod.POST)
    public Object getPetitionAndStenchOnlineListDataByParamMap(@RequestJson(value = "dgimns", required = false) List<String> dgimns,
                                                               @RequestJson(value = "starttime") String starttime,
                                                               @RequestJson(value = "endtime") String endtime,
                                                               @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                               @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws Exception {
        //根据监测时间和恶臭点位MN号获取投诉信息
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        paramMap.put("dgimns", dgimns);
        paramMap.put("starttime", starttime);
        paramMap.put("endtime", endtime);
        paramMap.put("pagenum", pagenum);
        paramMap.put("pagesize", pagesize);
        resultMap  = petitionInfoService.getPetitionAndStenchOnlineListDataByParamMap(paramMap);
        return AuthUtil.parseJsonKeyToLower("success", resultMap);
    }

    /**
     * @author: xsm
     * @date: 2019/7/26 0026 下午 4:24
     * @Description: 根据恶臭监测点MN号获取污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getAllStenchPollutantsByDgimns", method = RequestMethod.POST)
    public Object getAllStenchPollutantsByDgimns(@RequestJson(value = "dgimns", required = false) List<String> dgimns) throws Exception {
        try {
            List<Map<String, Object>> listdata = petitionInfoService.getAllStenchPollutantsByDgimns(dgimns);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 7:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }


    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 下午 5:08
     * @Description: 获取投诉任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pk_id]
     * @throws:
     */
    @RequestMapping(value = "getPetitionDetailById", method = RequestMethod.POST)
    public Object getPetitionDetailById(@RequestJson(value = "id") String pk_id) throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            Map<String,Object> result=new HashMap<>();
            paramMap.put("pkid",pk_id);
            List<Map<String, Object>> listdata = petitionInfoService.getPetitionDetailById(paramMap);
            if(listdata.size()>0){
                Map<String, Object> map = listdata.get(0);
                result.put("FeedbackResults",map.get("completereply"));
                result.put("fileid",map.get("fkFileid"));
                result.put("status",map.get("status"));
                result.put("CompleteTime",map.get("CompleteTime"));
                result.put("UndertakeDepartment",map.get("UndertakeDepartment"));

                List<Map<String,Object>> list = (List) map.get("DetailData");

                if(list!=null && list.size()>0){
                    String fenpairen = list.stream().filter(m -> m.get("currenttaskstatus") != null && m.get("username") != null && "分派任务".equals(m.get("currenttaskstatus").toString()))
                            .map(m -> m.get("username").toString()).collect(Collectors.joining("、"));
                    String taskhandletime = list.stream().filter(m -> m.get("currenttaskstatus") != null && m.get("taskhandletime") != null && "分派任务".equals(m.get("currenttaskstatus").toString()))
                            .map(m -> m.get("taskhandletime").toString()).collect(Collectors.joining("、"));
                    String zhixingren = list.stream().filter(m -> m.get("currenttaskstatus") != null && m.get("username") != null && "待处理".equals(m.get("currenttaskstatus").toString()))
                            .map(m -> m.get("username").toString()).collect(Collectors.joining("、"));
                    result.put("fenpairen",fenpairen);
                    result.put("taskhandletime",taskhandletime);
                    result.put("zhixingren",zhixingren);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

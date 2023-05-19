package com.tjpu.sp.controller.environmentalprotection.keymonitorpollutant;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
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


/**
 * @author: xsm
 * @date: 2019/6/22 0022 13:43
 * @Description: 重点监测污染物控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("keyMonitorPollutant")
public class KeyMonitorPollutantController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private KeyMonitorPollutantService keyMonitorPollutantService;

    private String sysmodel = "keyMonitorPollutant";
    private String pk_id = "pk_id";
    private String listfieldtype = "list";
    /**
     * 数据中心数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 2:06
     * @Description: 获取重点监测污染物初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "/getKeyMonitorPollutantsListPage", method = RequestMethod.POST)
    public Object getKeyMonitorPollutantsListPage(HttpServletRequest request ) {
        try {
            //获取userid

            Map<String, Object> datas = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap= RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("datasource", datasource);
            paramMap.put("queryfieldtype", "query");
            String param = AuthUtil.paramDataFormat(paramMap);
            //表头数据
            Object tableTitle = publicSystemMicroService.getTableTitle(param);
            JSONObject jsonObject = JSONObject.fromObject(tableTitle);
            String titleData = jsonObject.getString("data");
            //查询条件数据
            Map<String, Object> querydata = new HashMap<>();
            Object queryCriteriaData = publicSystemMicroService.getQueryCriteriaData(param);
            JSONObject jsonObject2 = jsonObject.fromObject(queryCriteriaData);
            String queryData = jsonObject2.getString("data");
            JSONObject jsonObject3 = JSONObject.fromObject(queryData);
            String dualcontrolskey = jsonObject3.getString("dualcontrolskey");
            String querycontroldata = jsonObject3.getString("querycontroldata");
            String queryformdata = jsonObject3.getString("queryformdata");
            querydata.put("dualcontrolskey", dualcontrolskey);
            querydata.put("querycontroldata", querycontroldata);
            querydata.put("queryformdata", queryformdata);
            // 分页
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
               PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
            }
            Map<String, Object> tabledata = new HashMap<>();
            List<Map<String, Object>> dataList = keyMonitorPollutantService.getKeyMonitorPollutantsByParamMap(paramMap);
            // 保存分页信息
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            // 分页后的数据
            List<Map<String, Object>> listInfo = pageInfo.getList();
            tabledata.put("pagesize", pageInfo.getPageSize());// 每页条数
            tabledata.put("pagenum", pageInfo.getPageNum());// 当前页
            tabledata.put("total", pageInfo.getTotal());// 总条数
            tabledata.put("pages", pageInfo.getPages());// 总页数
            tabledata.put("total", dataList.size());// 总条数
            tabledata.put("primarykey", pk_id);// 主键
            tabledata.put("tablelistdata", dataList);// 数据
            tabledata.put("tabletitledata", titleData);// 表头
            //按钮数据
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject4 = jsonObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject4.getString("data");
            JSONObject jsonObject5 = JSONObject.fromObject(buttonData);
            String topoperations = jsonObject5.getString("topbuttondata");
            String listoperation = jsonObject5.getString("tablebuttondata");
            Map<String, Object> buttondatamap = new HashMap<>();//按钮
            buttondatamap.put("topbuttondata", topoperations);
            buttondatamap.put("tablebuttondata", listoperation);
            //返回数据
            datas.put("querydata", querydata);
            datas.put("tabledata", tabledata);
            datas.put("buttondata", buttondatamap);
            return AuthUtil.parseJsonKeyToLower("success", datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 2:06
     * @Description: 通过自定义参数获取重点监测污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "/getKeyMonitorPollutantsByParamMap", method = RequestMethod.POST)
    public Object getKeyMonitorPollutantsByParamMap(@RequestJson(value = "paramsjson", required = true) Object map) {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            if(paramMap.get("pagenum")!=null && paramMap.get("pagesize")!=null){
                int pagenum=Integer.parseInt(paramMap.get("pagenum").toString());
                int pagesize=Integer.parseInt(paramMap.get("pagesize").toString());
                PageHelper.startPage(pagenum, pagesize);
            }
            if (paramMap.get("pollutanttype")!=null && !"".equals(paramMap.get("pollutanttype").toString())){
                String pollutanttype=paramMap.get("pollutanttype").toString();
                String[] strs = pollutanttype.split(",");
                paramMap.put("pollutanttype",strs);
            }else{  paramMap.put("pollutanttype",null);   }
            List<Map<String, Object>> listData = keyMonitorPollutantService.getKeyMonitorPollutantsByParamMap(paramMap);
            PageInfo<Map<String, Object>> page = new PageInfo<>(listData);
            Map<String, Object> resultMap = new HashMap<>();
            if(paramMap.get("pagenum")!=null && paramMap.get("pagesize")!=null){
                resultMap.put("pageSize", page.getPageSize());
                resultMap.put("pageNum", page.getPageNum());
                resultMap.put("pages", page.getPages());
            }
            //总条数
            resultMap.put("total", page.getTotal());
            resultMap.put("tablelistdata", listData);
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 2:06
     * @Description: 获取重点监测污染物新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getKeyMonitorPollutantAddPage", method = RequestMethod.POST)
    public Object getKeyMonitorPollutantAddPage() {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            // 获取token
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 2:06
     * @Description: 新增重点监测污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addKeyMonitorPollutant", method = RequestMethod.POST)
    public Object addKeyMonitorPollutant(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            // 获取token
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 2:06
     * @Description: 通过重点监测污染物id删除重点监测污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteKeyMonitorPollutantByID", method = RequestMethod.POST)
    public Object deleteKeyMonitorPollutantByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            // 获取token
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 2:06
     * @Description: 通过重点监测污染物id查询重点监测污染物详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [airid]
     * @throws:
     */
    @RequestMapping(value = "getKeyMonitorPollutantDetailByID", method = RequestMethod.POST)
    public Object getKeyMonitorPollutantDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("pkid", id);
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> resultlist = new ArrayList<>();
            List<Map<String, Object>> dataList = keyMonitorPollutantService.getKeyMonitorPollutantsByParamMap(paramMap);
            Map<String, Object> obj =dataList.get(0);
            //拼写详情页面
            Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("fieldname", "pollutantname");
                objectMap.put("width", "50%");
                objectMap.put("showhide", true);
                objectMap.put("ordernum", 1);
                objectMap.put("label", "污染物名称");
                objectMap.put("type", "string");
                objectMap.put("value", obj.get("pollutantname"));
                resultlist.add(objectMap);
             Map<String, Object> objectMap2 = new HashMap<>();
                objectMap2.put("fieldname", "pollutantcode");
                objectMap2.put("width", "50%");
                objectMap2.put("showhide", true);
                objectMap2.put("ordernum", 1);
                objectMap2.put("label", "污染物编码");
                objectMap2.put("type", "string");
                objectMap2.put("value", obj.get("pollutantcode"));
                resultlist.add(objectMap2);
            Map<String, Object> objectMap3 = new HashMap<>();
                objectMap3.put("fieldname", "pollutanttype");
                objectMap3.put("width", "50%");
                objectMap3.put("showhide", true);
                objectMap3.put("ordernum", 1);
                objectMap3.put("label", "污染物类型");
                objectMap3.put("type", "string");
                objectMap3.put("value", obj.get("pollutanttype"));
                resultlist.add(objectMap3);
            Map<String, Object> objectMap4 = new HashMap<>();
                objectMap4.put("fieldname", "pollutantunit");
                objectMap4.put("width", "50%");
                objectMap4.put("showhide", true);
                objectMap4.put("ordernum", 1);
                objectMap4.put("label", "污染物单位");
                objectMap4.put("type", "string");
                objectMap4.put("value", obj.get("pollutantunit"));
                resultlist.add(objectMap4);
            Map<String, Object> objectMap5 = new HashMap<>();
                objectMap5.put("fieldname", "OrderIndex");
                objectMap5.put("width", "50%");
                objectMap5.put("showhide", true);
                objectMap5.put("ordernum", 1);
                objectMap5.put("label", "排序号");
                objectMap5.put("type", "string");
                objectMap5.put("value", obj.get("OrderIndex"));
                resultlist.add(objectMap5);
            Map<String, Object> objectMap6 = new HashMap<>();
                objectMap6.put("fieldname", "UpdateTime");
                objectMap6.put("width", "50%");
                objectMap6.put("showhide", true);
                objectMap6.put("ordernum", 1);
                objectMap6.put("label", "更新时间");
                objectMap6.put("type", "string");
                objectMap6.put("value", obj.get("UpdateTime"));
                resultlist.add(objectMap6);
             Map<String, Object> objectMap7 = new HashMap<>();
                objectMap7.put("fieldname", "UpdateUser");
                objectMap7.put("width", "50%");
                objectMap7.put("showhide", true);
                objectMap7.put("ordernum", 1);
                objectMap7.put("label", "更新人");
                objectMap7.put("type", "string");
                objectMap7.put("value", obj.get("UpdateUser"));
                resultlist.add(objectMap7);
            result.put("detaildata",resultlist);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }





}

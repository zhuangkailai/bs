package com.tjpu.sp.controller.environmentalprotection.parkinfo.assets;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.parkinfo.assets.AssetsService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2019/5/9 0009 14:18
 * @Description: 园区营业额控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("assets")
public class AssetsController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private AssetsService assetsService;


    private String sysmodel = "assets";
    private String pk_id = "pk_assetsid";
    private String listfieldtype = "list-base";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;
    /**
     * 存放token的key
     */


    /**
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 2:39
     * @Description: 获取营业额初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "/getAssetsListPage", method = RequestMethod.POST)
    public Object getAssetsListPage(HttpServletRequest request, HttpSession session) {
        try {
            //获取userid
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
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
     * @date: 2019/5/9 0009 下午 3:45
     * @Description: 通过自定义参数获取营业额信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @RequestMapping(value = "/getAssetByParamMap", method = RequestMethod.POST)
    public Object getAssetByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
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
     * @date: 2019/5/9 0009 下午 3:47
     * @Description: 获取营业额新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getAssetsAddPage", method = RequestMethod.POST)
    public Object getAssetsAddPage() {
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
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:51
     * @Description: 新增营业额
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addAssets", method = RequestMethod.POST)
    public Object addAssets(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            // 获取token
            JSONObject formdata = JSONObject.fromObject(paramMap.get("formdata"));
            formdata.put("updatetime", format.format(new Date()));
            formdata.put("updateuser", username);
            paramMap.put("formdata", formdata);

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
     * @date: 2019/5/9 0009 下午 3:52
     * @Description: 通过营业额id获取营业额修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getAssetsUpdatePageByID", method = RequestMethod.POST)
    public Object getAssetsUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            // 获取token

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(param);
            return resultList;
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:54
     * @Description: 修改营业额
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateAssets", method = RequestMethod.POST)
    public Object updateAssets(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            // 获取token
            JSONObject formdata = JSONObject.fromObject(paramMap.get("formdata"));
            formdata.put("updatetime", format.format(new Date()));
            formdata.put("updateuser", username);
            paramMap.put("formdata", formdata);
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
     * @date: 2019/5/9 0009 下午 3:57
     * @Description: 通过营业额id删除营业额
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteAssetsByID", method = RequestMethod.POST)
    public Object deleteAssetsByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:58
     * @Description: 通过营业额id查询营业额详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [airid]
     * @throws:
     */
    @RequestMapping(value = "getAssetsDetailByID", method = RequestMethod.POST)
    public Object getAssetsDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            // 获取token

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:45
     * @Description: 查询不同经济种类下园区营业额信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @RequestMapping(value = "/getAssetInfoGroupByAssettpye", method = RequestMethod.POST)
    public Object getAssetInfoGroupByAssettpye() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);

            // 获取token

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListData(param);
            resultList = AuthUtil.decryptData(resultList);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data2 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data2);
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject1.get("tablelistdata");

            if (listdata != null) {
                Map<Object, List<Map<String, Object>>> datas = listdata.stream().filter(m -> m != null && m.get("fk_assetstype") != null).collect(Collectors.groupingBy(m -> m.get("fk_assetstype")));
                return AuthUtil.parseJsonKeyToLower("success", datas);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/12 0012 下午 3:47
     * @Description: 通过年份获取主要经济指标及金额信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [year]
     * @throws:
     */
    @RequestMapping(value = "/getAssertsTypeInfoByYear", method = RequestMethod.POST)
    public Object getAssertsTypeInfoByYear(@RequestJson(value = "year") String year) {
        try {
            return AuthUtil.parseJsonKeyToLower("success", assetsService.getAssertsTypeInfoByYear(year));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/12 0012 下午 5:27
     * @Description: 获取逐年的净利情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getAssretsInfos", method = RequestMethod.POST)
    public Object getAssretsInfos() {
        try {
            return AuthUtil.parseJsonKeyToLower("success", assetsService.getAssretsInfos());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/16 0016 上午 10:08
     * @Description: 获取主导产业聚集度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getPrimeIndustryAssretsInfos", method = RequestMethod.POST)
    public Object getPrimeIndustryAssretsInfos() {
        try {
            List<Map<String, Object>> assretsInfos = assetsService.getPrimeIndustryAssretsInfos();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            for (Map<String, Object> assretsInfo : assretsInfos) {
                Map<String, Object> data = new HashMap<>();
                Double PrimeIndustryAsset = Double.valueOf(assretsInfo.get("PrimeIndustryAsset") == null ? "0" : assretsInfo.get("PrimeIndustryAsset").toString());
                Double Assets = Double.valueOf(assretsInfo.get("Assets") == null ? "0" : assretsInfo.get("Assets").toString());
                if (Assets == null || PrimeIndustryAsset == 0) {
                    data.put("value", 0);
                } else {
                    data.put("value", decimalFormat.format(PrimeIndustryAsset / Assets * 100));
                }
                data.put("year", assretsInfo.get("year") == null ? "" : assretsInfo.get("year").toString());
                resultList.add(data);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

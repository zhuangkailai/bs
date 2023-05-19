package com.tjpu.sp.controller.environmentalprotection.parkinfo.keycompany;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.FileController;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2019/5/9 0009 14:18
 * @Description: 园区重点企业控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("keycompany")
public class KeyCompanyController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;

    @Autowired
    private FileController fileController;

    private String sysmodel = "keycompany";
    private String pk_id = "pk_keycompanyid";
    private String listfieldtype = "list-base";
    /**
     * 数据中心数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;
    /**
     * 存放token的key
     */


    /**
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 2:39
     * @Description: 获取重点企业初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "/getKeyCompanysListPage", method = RequestMethod.POST)
    public Object getKeyCompanysListPage(HttpServletRequest request, HttpSession session) {
        try {
            //获取userid
            String sessionId = session.getId();
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
     * @date: 2019/5/9 0009 下午 3:45
     * @Description: 通过自定义参数获取重点企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @RequestMapping(value = "/getKeyCompanysByParamMap", method = RequestMethod.POST)
    public Object getKeyCompanysByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
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
     * @Description: 获取重点企业新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getKeyCompanyAddPage", method = RequestMethod.POST)
    public Object getKeyCompanyAddPage() {
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
     * @Description: 新增重点企业
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addKeyCompany", method = RequestMethod.POST)
    public Object addKeyCompany(HttpServletRequest request) throws Exception {
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
     * @author: chengzq
     * @date: 2019/5/9 0009 下午 3:52
     * @Description: 通过重点企业id获取重点企业修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getKeyCompanyUpdatePageByID", method = RequestMethod.POST)
    public Object getKeyCompanyUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @Description: 修改重点企业
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateKeyCompany", method = RequestMethod.POST)
    public Object updateKeyCompany(HttpServletRequest request,HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            JSONObject formdata=JSONObject.fromObject(paramMap.get("formdata"));
            formdata.put("updatetime",format.format(new Date()));
            formdata.put("updateuser",username);
            paramMap.put("formdata",formdata);

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
     * @Description: 通过重点企业id删除重点企业
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteKeyCompanyByID", method = RequestMethod.POST)
    public Object deleteKeyCompanyByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @Description: 通过重点行业id查询重点行业详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [airid]
     * @throws:
     */
    @RequestMapping(value = "getKeyCompanyDetailByID", method = RequestMethod.POST)
    public Object getKeyCompanyDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
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
     * @Description: 获取所有重点企业信息以及logo图片信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @RequestMapping(value = "/getKeyCompanysAndImgs", method = RequestMethod.POST)
    public Object getKeyCompanysAndImgs() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);

            // 获取token

            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListByParam(param);
            resultList = AuthUtil.decryptData(resultList);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data1 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data1);
            String tabledata = jsonObject1.getString("tabledata");
            JSONObject jsonObject2 = JSONObject.fromObject(tabledata);
            Object tablelistdata = jsonObject2.get("tablelistdata");


            List<Map<String, Object>> data = (List<Map<String, Object>>) tablelistdata;
            List<String> fk_imgfileids = data.stream().map(m -> m.get("fk_imgfileid").toString()).collect(Collectors.toList());

            JSONArray datas = JSONArray.fromObject(tablelistdata);
            for (String fk_imgfileid : fk_imgfileids) {

                Object filesInfoAndImgByParam = fileController.getFilesInfoAndImgByParam(fk_imgfileid, null, null);
                JSONObject jsonObject3 = JSONObject.fromObject(filesInfoAndImgByParam);


                JSONArray data2 = JSONArray.fromObject(jsonObject3.get("data"));

                for (Object o : data2) {
                    String filepath = ((Map) o).get("fileflag").toString();
                    for (Object data3 : datas) {
                        String fileid = ((Map) data3).get("fk_imgfileid").toString();
                        if (filepath.equals(fileid)) {
                            ((Map) data3).put("imgsrc", ((Map) o).get("imgsrc"));
                        }
                    }
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", datas);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

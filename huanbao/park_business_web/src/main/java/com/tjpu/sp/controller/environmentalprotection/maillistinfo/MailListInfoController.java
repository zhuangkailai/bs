package com.tjpu.sp.controller.environmentalprotection.maillistinfo;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.environmentalprotection.maillistinfo.MailListInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年7月15日 下午3:50:29
 * @Description:通信录信息接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("mailListInfo")
public class MailListInfoController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private MailListInfoService mailListInfoService;

    private String sysmodel = "AddressBook";
    private String listfieldtype = "list";
    private String pk_id = "pk_id";


    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description:获取通信录信息列表页面数据，包含查询控件，以及按钮权限，分页信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request，session]
     * @throws:
     */
    @RequestMapping(value = "getMailListInfosListPage", method = RequestMethod.POST)
    public Object getMailListInfosListPage(HttpServletRequest request ) {
        try {
            //获取userid

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("sysmodel", sysmodel);
            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.getListByParam(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @throws :
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description: 自定义查询条件查询通信录信息列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson，pagesize，pagenum]
     * @return:
     */
    @RequestMapping(value = "getMailListInfosByParamMap", method = RequestMethod.POST)
    public Object getMailListInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson,
                                             @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                             @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (null != paramsjson) {
                paramMap = JSONObject.fromObject(paramsjson);
            }
            paramMap.put("datasource", datasource);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("sysmodel", sysmodel);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getListData(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description: 获取通信录信息新增页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getMailListInfoAddPage", method = RequestMethod.POST)
    public Object getMailListInfoAddPage() throws Exception {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data2 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data2);
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject1.get("addcontroldata");
            for (Map<String, Object> objmap : listdata) {
                if ("contactunit".equals(objmap.get("name").toString())) {
                    objmap.put("defaultfirstoption", true);
                    objmap.put("filterable", true);
                    objmap.put("allowcreate", true);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", jsonObject1);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description: 根据主键ID和监测点类型获取通信录信息修改页面的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pk_monitorpointid]
     * @throws:
     */
    @RequestMapping(value = "getMailListInfoUpdatePageByID", method = RequestMethod.POST)
    public Object getMailListInfoUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(Param);
            JSONObject jsonObject = JSONObject.fromObject(resultList);
            Object data2 = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data2);
            List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject1.get("editcontroldata");
            for (Map<String, Object> objmap : listdata) {
                if ("contactunit".equals(objmap.get("name").toString())) {
                    objmap.put("defaultfirstoption", true);
                    objmap.put("filterable", true);
                    objmap.put("allowcreate", true);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", jsonObject1);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description: 新增通信录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addMailListInfo", method = RequestMethod.POST)
    public Object addMailListInfo(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
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
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description: 修改通信录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateMailListInfo", method = RequestMethod.POST)
    public Object updateMailListInfo(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description: 根据通信录信息主键ID删除该条记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteMailListInfoByID", method = RequestMethod.POST)
    public Object deleteMailListInfoByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description: 根据主键ID查询通信录信息详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getMailListInfoDetailByID", method = RequestMethod.POST)
    public Object getMailListInfoDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object result = publicSystemMicroService.getDetail(Param);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/15 下午6:31
     * @Description: 根据主键ID查询通信录信息详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getMailListInfoDataList", method = RequestMethod.POST)
    public Object getMailListInfoDataList(
            @RequestJson(value = "pagesize",required = false) Integer pagesize,
            @RequestJson(value = "pagenum",required = false) Integer pagenum,
            @RequestJson(value = "contactunit", required = false) String contactunit
    ){
        try {
             Map<String,Object>  resultMap = new HashMap<>();
            Map<String,Object> paramMap = new HashMap<>();
            if (StringUtils.isNotBlank(contactunit)){
                paramMap.put("contactunit",contactunit);
            }
            List<Map<String,Object>> dataList = mailListInfoService.getMailListInfoDataByParam(paramMap);
            if (dataList.size()>0){
                String unit;
                Map<String,List<Map<String,Object>>> unitAndDataList = new HashMap<>();
                List<Map<String,Object>> subDataList;
                for (Map<String,Object> dataMap:dataList){
                    unit = dataMap.get("contactunit").toString();
                    if (unitAndDataList.containsKey(unit)){
                        subDataList = unitAndDataList.get(unit);
                    }else {
                        subDataList = new ArrayList<>();
                    }
                    subDataList.add(dataMap);
                    unitAndDataList.put(unit,subDataList);
                }
                dataList.clear();
                for (String unitIndex:unitAndDataList.keySet()){
                    Map<String,Object> dataMap = new HashMap<>();
                    dataMap.put("contactunit",unitIndex);
                    dataMap.put("datalist",unitAndDataList.get(unitIndex));
                    dataList.add(dataMap);
                }
                //排序+分页
                dataList = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("contactunit").toString())).collect(Collectors.toList());
                if (pagesize != null && pagenum != null) {
                    resultMap.put("total", dataList.size());
                    dataList = MongoDataUtils.getPageData(dataList, pagenum, pagesize);
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", dataList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/9/17 上午11:10
     * @Description: 获取联系单位下拉框数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getContactUnitSelectData", method = RequestMethod.POST)
    public Object getContactUnitSelectData() throws Exception {
        try {
            List<Map<String, Object>> listdata = mailListInfoService.getContactUnitSelectData();
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("ContactUnit"));
                    objmap.put("value", map.get("ContactUnit"));
                    result.add(objmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/9/17  0017 下午 1:15
     * @Description: 根据联系单位名称和人员名称判断是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/isTableDataHaveInfoByContactUnitAndPeopleName", method = RequestMethod.POST)
    public Object isTableDataHaveInfoByContactUnitAndPeopleName(@RequestJson(value = "contactunit", required = false) String contactunit, @RequestJson(value = "peoplename", required = true) String peoplename) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (contactunit != null && !"".equals(contactunit)) {
                paramMap.put("contactunit", contactunit);
            } else {
                paramMap.put("contactunit", "");
            }
            paramMap.put("peoplename", peoplename);
            List<Map<String, Object>> value = mailListInfoService.isTableDataHaveInfoByContactUnitAndPeopleName(paramMap);
            if (value.size() == 0) {    //等于0 没有此条数据可以添加
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

package com.tjpu.sp.controller.environmentalprotection.licence;

import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.environmentalprotection.licence.LicenceService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("licence")
public class PwLicenceController {

    private final LicenceService licenceService;

    @Autowired
    public PwLicenceController(LicenceService licenceService) {
        this.licenceService = licenceService;
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/30 9:09
     * @Description: 获取过去许可证个数
     * @param:
     * @return:
     */
    @RequestMapping(value = "countOverdueLicenceNum", method = RequestMethod.GET)
    public Object countOverdueLicenceNum() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("label", "过期许可证");
        try {
            resultMap.put("num", licenceService.countOverdueLicenceNum());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", resultMap);
    }


    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 6:01
     * @Description: 获取过期许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: 污染源名称、许可证编号、有效开始日期、有效截止日期、发证日期、发证单位
     */
    @RequestMapping(value = "getOverdueLicenceData", method = RequestMethod.POST)
    public Object getOverdueLicenceData(@RequestJson(value = "pagesize", required = false) Integer pageSize,
                                        @RequestJson(value = "pagenum", required = false) Integer pageNum) {
        try {
            Map<String, Object> mapData = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pagesize", pageSize);
            paramMap.put("pagenum", pageNum);
            List<Map<String, Object>> tabletitledata = licenceService.getOverdueLicenceTableTitleData();
            PageInfo<Map<String, Object>> pageInfos = licenceService.getOverdueLicenceTableListDataByParamMap(paramMap);
            mapData.put("tabletitledata", tabletitledata);
            mapData.put("tablelistdata", pageInfos.getList());
            mapData.put("total", pageInfos.getTotal());
            mapData.put("pages", pageInfos.getPages());
            mapData.put("pagesize", pageInfos.getPageSize());
            mapData.put("pagenum", pageInfos.getPageNum());
            return AuthUtil.parseJsonKeyToLower("success", mapData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

   
   /**
    * @Description: 获取最新排污许可证信息 
    * @Param:  
    * @return:  
    * @Author: lip
    * @Date: 2022/8/15 10:40
    */ 
    @RequestMapping(value = "getLastDataListByParam", method = RequestMethod.POST)
    public Object getLastDataListByParam(@RequestJson(value="paramjson")Object paramjson) {
        try {


            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = (Map<String, Object>) paramjson;
            PageInfo<Map<String, Object>> pageInfos = licenceService.getLastDataListByParam(paramMap);
            resultMap.put("datalist", pageInfos.getList());
            resultMap.put("total", pageInfos.getTotal());
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @Description: 获取厂内外菜单信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/17 9:35
     */
    @RequestMapping(value = "getInOrOutMenuDataList", method = RequestMethod.POST)
    public Object getInOrOutMenuDataList(@RequestJson(value = "inoutmark") String inoutmark) {
        try {

            Map<String, Object> paramMap = new HashMap<>();

            if (inoutmark.equals("1")) {//内
                paramMap.put("inmark", "true");
            } else {//外
                paramMap.put("outmark", "true");
            }

            List<Map<String, Object>> resultList = licenceService.getInOrOutMenuDataListByParam(paramMap);

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 设置厂内外菜单权限
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/17 9:35
     */
    @RequestMapping(value = "setInOrOutMenuData", method = RequestMethod.POST)
    public Object setInOrOutMenuData(@RequestJson(value = "formdata") Object formdata) {
        try {

            List<Map<String, Object>> dataList = (List<Map<String, Object>>) formdata;
            if (dataList.size()>0){
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                Date nowDay = new Date();
                for (Map<String, Object> map : dataList) {
                    map.put("pkid",UUID.randomUUID().toString());
                    map.put("updateuser",username);
                    map.put("updatetime",nowDay);
                }
            }
            licenceService.setInOrOutMenuData(dataList);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取厂内外菜单信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/17 9:35
     */
    @RequestMapping(value = "getInOrOutAllMenuData", method = RequestMethod.POST)
    public Object getInOrOutAllMenuData(@RequestJson(value = "menucode") String menucode) {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("menucode", menucode);
            List<Map<String, Object>> resultList = licenceService.getInOrOutAllMenuData(paramMap);
            for (Map<String, Object> dataMap : resultList) {
                dataMap.putIfAbsent("inmark", "false");
                dataMap.putIfAbsent("outmark", "false");
                if (dataMap.get("sortcode") == null) {
                    dataMap.putIfAbsent("sortcode", -1);
                }
            }

            //排序
            resultList = resultList.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("sortcode").toString())
                    )).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取企业云台账信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/20 15:15
     */
    @RequestMapping(value = "getEntStandingInfoByParam", method = RequestMethod.POST)
    public Object getEntStandingInfoByParam(
            @RequestJson(value = "paramjson") Object paramjson) {
        try {


            Map<String, Object> resultMap = new HashMap<>();
            JSONObject jsonObject = JSONObject.fromObject(paramjson);
            jsonObject.put("year", DataFormatUtil.getDateY(new Date()));
            PageInfo<Map<String, Object>> pageInfos = licenceService.getEntStandingInfoByParam(jsonObject);
            resultMap.put("tablelistdata", pageInfos.getList());
            resultMap.put("total", pageInfos.getTotal());

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 信息公开要求
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getInfoOpenByParam", method = RequestMethod.POST)
    public Object getInfoOpenByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = licenceService.getInfoOpenByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 环境管理台账记录要求
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getStandingBookRequireByParam", method = RequestMethod.POST)
    public Object getStandingBookRequireByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = licenceService.getStandingBookRequireByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 噪声排放信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getNoiseOutputInfoByParam", method = RequestMethod.POST)
    public Object getNoiseOutputInfoByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = licenceService.getNoiseOutputInfoByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 改正规定
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getCorrectProvideByParam", method = RequestMethod.POST)
    public Object getCorrectProvideByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = licenceService.getCorrectProvideByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 文字描述内容
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/7 10:48
     */
    @RequestMapping(value = "getOtherTextRequireByParam", method = RequestMethod.POST)
    public Object getOtherTextRequireByParam(@RequestJson(value = "licenceid") String licenceid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("licenceid", licenceid);
            List<Map<String, Object>> resultList = licenceService.getOtherTextRequireByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

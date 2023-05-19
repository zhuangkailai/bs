package com.tjpu.sp.controller.environmentalprotection.useelectricfacility;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.useelectricfacility.UseElectricFacilityVO;
import com.tjpu.sp.service.environmentalprotection.useelectricfacility.UseElectricFacilityService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2020/06/18 0011 下午 1:58
 * @Description: 用电设施控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("useelectricfacility")
public class UseElectricFacilityController {

    @Autowired
    private UseElectricFacilityService useElectricFacilityService;


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 2:58
     * @Description: 通过自定义参数获取用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getUseElectricFacilityByParamMap", method = RequestMethod.POST)
    public Object getUseElectricFacilityByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> useElectricFacilityByParamMap = useElectricFacilityService.getUseElectricFacilityByParamMap(jsonObject);
            long total = useElectricFacilityByParamMap.size();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                Integer pagenum = Integer.valueOf(jsonObject.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(jsonObject.get("pagesize").toString());
                useElectricFacilityByParamMap = useElectricFacilityByParamMap.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("datalist", useElectricFacilityByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:17
     * @Description: 新增用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addUseElectricFacility", method = RequestMethod.POST)
    public Object addUseElectricFacility(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            UseElectricFacilityVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new UseElectricFacilityVO());

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            entity.setpkid(UUID.randomUUID().toString());
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            useElectricFacilityService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:19
     * @Description: 通过id获取用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getUseElectricFacilityByID", method = RequestMethod.POST)
    public Object getUseElectricFacilityByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = useElectricFacilityService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:19
     * @Description: 修改用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateUseElectricFacility", method = RequestMethod.POST)
    public Object updateUseElectricFacility(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            UseElectricFacilityVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new UseElectricFacilityVO());

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            useElectricFacilityService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:21
     * @Description: 通过id删除用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteUseElectricFacilityByID", method = RequestMethod.POST)
    public Object deleteUseElectricFacilityByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            useElectricFacilityService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/06/18 0011 下午 3:31
     * @Description: 通过id查询用电设施信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getUseElectricFacilityDetailByID", method = RequestMethod.POST)
    public Object getUseElectricFacilityDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> detailInfo = useElectricFacilityService.getUseElectricFacilityDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取企业设施统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEntFacilityCountData", method = RequestMethod.POST)
    public Object getEntFacilityCountData(
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            if (pagesize != null && pagenum != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointype", CommonTypeEnum.MonitorPointTypeEnum.ElectricFacilityEnum.getCode());
            paramMap.put("status", CommonTypeEnum.OnlineStatusEnum.ExceptionStatusEnum.getCode());
            List<Map<String, Object>> entFacilityCountData = useElectricFacilityService.getEntFacilityCountData(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(entFacilityCountData);
            long total = pageInfo.getTotal();
            resultMap.put("tablelistdata", entFacilityCountData);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取企业设施统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getFacilityTreeData", method = RequestMethod.POST)
    public Object getFacilityTreeData(
            @RequestJson(value = "pollutionid", required = false) String pollutionid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> resultList = useElectricFacilityService.getFacilityTreeDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/7 0007 上午 9:25
     * @Description: 获取所有企业下设备和监测点树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getFacilityTreesData", method = RequestMethod.POST)
    public Object getFacilityTreesData(
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "pollutionname", required = false) String pollutionname) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fkpollutionid", pollutionid);
            paramMap.put("pollutionname", pollutionname);
            List<Map<String, Object>> resultList = useElectricFacilityService.getPollutionAndFacilityInfoParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/7/8 0008 上午 11:54
     * @Description: 获取所有关联用电设施的企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getAllPollutionInfo", method = RequestMethod.POST)
    public Object getAllPollutionInfo(@RequestJson(value = "pollutionname", required = false) String pollutionname) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionname", pollutionname);
            List<Map<String, Object>> resultList = useElectricFacilityService.getAllPollutionInfo(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/8 0008 下午 1:17
     * @Description: 通过多参数获取用电监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid, fkuseelectricfacilityid]
     * @throws:
     */
    @RequestMapping(value = "getUseElectricFacilityInfoByParamMap", method = RequestMethod.POST)
    public Object getUseElectricFacilityInfoByParamMap(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                       @RequestJson(value = "fkuseelectricfacilityid", required = false) String fkuseelectricfacilityid,
                                                       @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                       @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("fkuseelectricfacilityid", fkuseelectricfacilityid);
            if (pagesize != null && pagenum != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> resultList = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(resultList);
            resultList.stream().forEach(m -> {
                m.put("FacilityType",m.get("FacilityType")==null?"":"1".equals(m.get("FacilityType").toString())?"产污环节":"治污环节");
                m.put("status",m.get("status")==null?"": CommonTypeEnum.OnlineStatusEnum.getNameByCode(m.get("status").toString()));
            });
            long total = pageInfo.getTotal();
            resultMap.put("total", total);
            resultMap.put("datalist", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

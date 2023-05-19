package com.tjpu.sp.controller.environmentalprotection.parkprofile;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.parkprofile.ParkProfileVO;
import com.tjpu.sp.service.environmentalprotection.parkprofile.ParkProfileService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;


/**
 * @author: chengzq
 * @date: 2020/11/13 0011 下午 1:58
 * @Description: 园区概况控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("parkprofile")
public class ParkProfileController {

    @Autowired
    private ParkProfileService parkprofileService;

    /**
     * @author: chengzq
     * @date: 2020/11/13 0011 下午 2:58
     * @Description: 通过自定义参数获取园区概况信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getParkProfileByParamMap", method = RequestMethod.POST)
    public Object getParkProfileByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List parkprofileByParamMap = parkprofileService.getParkProfileByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(parkprofileByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", parkprofileByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/13 0011 下午 3:17
     * @Description: 新增园区概况信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addParkProfile", method = RequestMethod.POST)
    public Object addParkProfile(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            ParkProfileVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ParkProfileVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setpkid(UUID.randomUUID().toString());
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            parkprofileService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/13 0011 下午 3:19
     * @Description: 通过id获取园区概况信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getParkProfileByID", method = RequestMethod.POST)
    public Object getParkProfileByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> result = parkprofileService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/13 0011 下午 3:19
     * @Description: 修改园区概况信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateParkProfile", method = RequestMethod.POST)
    public Object updateParkProfile(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            ParkProfileVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ParkProfileVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            parkprofileService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/13 0011 下午 3:21
     * @Description: 通过id删除园区概况信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteParkProfileByID", method = RequestMethod.POST)
    public Object deleteParkProfileByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            parkprofileService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/13 0011 下午 3:31
     * @Description: 通过id查询园区概况信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getParkProfileDetailByID", method = RequestMethod.POST)
    public Object getParkProfileDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> detailInfo = parkprofileService.getParkProfileDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}

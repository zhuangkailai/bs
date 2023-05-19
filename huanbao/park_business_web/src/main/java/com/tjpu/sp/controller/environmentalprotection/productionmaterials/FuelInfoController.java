package com.tjpu.sp.controller.environmentalprotection.productionmaterials;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.productionmaterials.FuelInfoVO;
import com.tjpu.sp.service.environmentalprotection.productionmaterials.FuelInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: xsm
 * @description: 生产物料-企业燃料信息表
 * @create: 2019-10-18 13:19
 * @version: V1.0
 */
@RestController
@RequestMapping("fuelInfo")
public class FuelInfoController {
   @Autowired
    private FuelInfoService fuelInfoService;

    /**
     * @Author: xsm
     * @Date: 2019-10-18 0018 下午 13:19
     * @Description: 自定义查询条件查询企业燃料列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getFuelInfosByParamMap", method = RequestMethod.POST)
    public Object getFuelInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist =  fuelInfoService.getFuelInfosByParamMap(jsonObject);
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
     * @date: 2019/10/18 0018 下午 9:47
     * @Description: 新增企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addFuelInfo", method = RequestMethod.POST)
    public Object addFuelInfo(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            FuelInfoVO fuelInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new FuelInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            fuelInfoVO.setUpdatetime(new Date());
            fuelInfoVO.setUpdateuser(username);
            fuelInfoVO.setPkFuelinfoid(UUID.randomUUID().toString());
            fuelInfoService.insert(fuelInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:47
     * @Description: 通过id获取企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getFuelInfoByID", method = RequestMethod.POST)
    public Object getFuelInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            FuelInfoVO rawMaterialVO = fuelInfoService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", rawMaterialVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:47
     * @Description: 修改企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateFuelInfo", method = RequestMethod.POST)
    public Object updateFuelInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            FuelInfoVO fuelInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new FuelInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            fuelInfoVO.setUpdateuser(username);
            fuelInfoVO.setUpdatetime(new Date());
            fuelInfoService.updateByPrimaryKey(fuelInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:48
     * @Description: 通过id删除企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteFuelInfoByID", method = RequestMethod.POST)
    public Object deleteFuelInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            fuelInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:48
     * @Description: 通过id获取企业燃料详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getFuelInfoDetailByID", method = RequestMethod.POST)
    public Object getFuelInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = fuelInfoService.getFuelInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

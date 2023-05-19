package com.tjpu.sp.controller.environmentalprotection.productionmaterials;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.productionmaterials.RawMaterialVO;
import com.tjpu.sp.service.environmentalprotection.productionmaterials.RawMaterialService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: xsm
 * @description: 企业主要原料及辅料信息表
 * @create: 2019-10-18 9:45
 * @version: V1.0
 */
@RestController
@RequestMapping("rawMaterial")
public class RawMaterialController {
    @Autowired
    private RawMaterialService rawMaterialService;

    /**
     * @Author: xsm
     * @Date: 2019-10-18 9:45
     * @Description: 自定义查询条件查询企业主要原料及辅料列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getRawMaterialsByParamMap", method = RequestMethod.POST)
    public Object getRawMaterialsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist =  rawMaterialService.getRawMaterialsByParamMap(jsonObject);
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
     * @date: 2019/10/18 0018 上午 9:47
     * @Description: 新增企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addRawMaterial", method = RequestMethod.POST)
    public Object addRawMaterial(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            RawMaterialVO rawMaterialVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new RawMaterialVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            rawMaterialVO.setUpdatetime(new Date());
            rawMaterialVO.setUpdateuser(username);
            rawMaterialVO.setPkRawmaterialid(UUID.randomUUID().toString());
            rawMaterialService.insert(rawMaterialVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:47
     * @Description: 通过id获取企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getRawMaterialByID", method = RequestMethod.POST)
    public Object getRawMaterialByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            RawMaterialVO rawMaterialVO = rawMaterialService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", rawMaterialVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:47
     * @Description: 修改企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateRawMaterial", method = RequestMethod.POST)
    public Object updateRawMaterial(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            RawMaterialVO rawMaterialVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new RawMaterialVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            rawMaterialVO.setUpdateuser(username);
            rawMaterialVO.setUpdatetime(new Date());
            rawMaterialService.updateByPrimaryKey(rawMaterialVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:48
     * @Description: 通过id删除企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteRawMaterialByID", method = RequestMethod.POST)
    public Object deleteRawMaterialByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            rawMaterialService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:48
     * @Description: 通过id获取企业主要原料及辅料详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getRawMaterialDetailByID", method = RequestMethod.POST)
    public Object getRawMaterialDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = rawMaterialService.getRawMaterialDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

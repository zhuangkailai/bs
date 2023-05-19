package com.tjpu.sp.controller.environmentalprotection.productionmaterials;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.productionmaterials.ProductInfoVO;
import com.tjpu.sp.service.environmentalprotection.productionmaterials.ProductInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: xsm
 * @description: 生产物料-产品表
 * @create: 2019-10-23 8:34
 * @version: V1.0
 */
@RestController
@RequestMapping("productInfo")
public class ProductInfoController {
    @Autowired
    private ProductInfoService productInfoService;

    /**
     * @Author: xsm
     * @Date: 2019-10-23 0023 上午 8:57
     * @Description: 自定义查询条件查询生产物料产品列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getProductInfosByParamMap", method = RequestMethod.POST)
    public Object getProductInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = productInfoService.getProductInfosByParamMap(jsonObject);
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
     * @date: 2019-10-23 0023 上午 8:57
     * @Description: 新增生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addProductInfo", method = RequestMethod.POST)
    public Object addProductInfo(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            ProductInfoVO productInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ProductInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            productInfoVO.setUpdatetime(new Date());
            productInfoVO.setUpdateuser(username);
            productInfoVO.setPkFuelinfoid(UUID.randomUUID().toString());
            productInfoService.insert(productInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019-10-23 0023 上午 8:57
     * @Description: 通过id获取生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getProductInfoByID", method = RequestMethod.POST)
    public Object getProductInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            ProductInfoVO dangerWasteLicenceInfoVO = productInfoService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", dangerWasteLicenceInfoVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019-10-23 0023 上午 8:57
     * @Description: 修改生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateProductInfo", method = RequestMethod.POST)
    public Object updateProductInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            ProductInfoVO productInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ProductInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            productInfoVO.setUpdateuser(username);
            productInfoVO.setUpdatetime(new Date());
            productInfoService.updateByPrimaryKey(productInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019-10-23 0023 上午 8:57
     * @Description: 通过id删除生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteProductInfoByID", method = RequestMethod.POST)
    public Object deleteProductInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            productInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019-10-23 0023 上午 8:57
     * @Description: 通过id获取生产物料产品详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getProductInfoDetailByID", method = RequestMethod.POST)
    public Object getProductInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = productInfoService.getProductInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

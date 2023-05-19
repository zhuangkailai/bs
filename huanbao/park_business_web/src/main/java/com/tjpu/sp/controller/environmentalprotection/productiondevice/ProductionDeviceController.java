package com.tjpu.sp.controller.environmentalprotection.productiondevice;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.productiondevice.ProductionDeviceVO;
import com.tjpu.sp.service.common.UserAuthSupportService;
import com.tjpu.sp.service.environmentalprotection.productiondevice.ProductionDeviceService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;


/**
 * @author: chengzq
 * @date: 2019/11/01 0011 下午 1:58
 * @Description: 生产装置设备控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("productiondevice")
public class ProductionDeviceController {

    @Autowired
    private ProductionDeviceService productionDeviceService;
    @Autowired
    private UserAuthSupportService userAuthSupportService;


    private String sysmodel = "productionEquipment";


    /**
     * @author: chengzq
     * @date: 2019/11/01 0011 下午 2:31
     * @Description: 获取生产装置设备列表初始化信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getProductionDeviceListPage", method = RequestMethod.POST)
    public Object getProductionDeviceListPage(@RequestJson(value = "paramsjson") Object paramsJson, HttpServletRequest request) throws ParseException {
        try {
            String token = request.getHeader("token");
            Map<String,Object> jsonObject = (Map)paramsJson;
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> productionDeviceByParamMap = productionDeviceService.getProductionDeviceByParamMap(jsonObject);

            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(productionDeviceByParamMap);
            long total = pageInfo.getTotal();

            //获取按钮
            Map<String, Object> userButtonAuthBySysmodelAndSessionId = userAuthSupportService.getUserButtonAuthBySysmodelAndSessionId(sysmodel, token);


            userButtonAuthBySysmodelAndSessionId.put("datalist", productionDeviceByParamMap);
            userButtonAuthBySysmodelAndSessionId.put("total", total);

            return AuthUtil.parseJsonKeyToLower("success", userButtonAuthBySysmodelAndSessionId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/01 0011 下午 2:58
     * @Description: 通过自定义参数获取生产装置设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getProductionDeviceByParamMap", method = RequestMethod.POST)
    public Object getProductionDeviceByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List productionDeviceByParamMap = productionDeviceService.getProductionDeviceByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(productionDeviceByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", productionDeviceByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/01 0011 下午 3:17
     * @Description: 新增生产装置设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addProductionDevice", method = RequestMethod.POST)
    public Object addProductionDevice(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<ProductionDeviceVO> list=new ArrayList<>();

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            ProductionDeviceVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ProductionDeviceVO());
            String parentid = UUID.randomUUID().toString();
            entity.setPkId(parentid);
            entity.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setUpdateuser(username);
            list.add(entity);

            if(jsonObject.get("child")!=null){
                for (Object child : (List) jsonObject.get("child")) {
                    ProductionDeviceVO productiondevicevo = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(child), new ProductionDeviceVO());
                    productiondevicevo.setPkId(UUID.randomUUID().toString());
                    productiondevicevo.setParentid(parentid);
                    productiondevicevo.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    productiondevicevo.setUpdateuser(username);
                    list.add(productiondevicevo);
                }
            }
            productionDeviceService.insert(list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/01 0011 下午 3:19
     * @Description: 通过id获取生产装置设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getProductionDeviceByID", method = RequestMethod.POST)
    public Object getProductionDeviceByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> entity = productionDeviceService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/01 0011 下午 3:19
     * @Description: 修改生产装置设备信息及子生产装置设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateProductionDevices", method = RequestMethod.POST)
    public Object updateProductionDevices(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            List<ProductionDeviceVO> list=new ArrayList<>();

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            ProductionDeviceVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ProductionDeviceVO());
            entity.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setUpdateuser(username);
            if(jsonObject.get("child")!=null){
                for (Object child : (List) jsonObject.get("child")) {
                    ProductionDeviceVO productiondevicevo = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(child), new ProductionDeviceVO());
                    productiondevicevo.setPkId(UUID.randomUUID().toString());
                    productiondevicevo.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    productiondevicevo.setUpdateuser(username);
                    list.add(productiondevicevo);
                }
            }
            productionDeviceService.update(entity,list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/12 0012 下午 5:06
     * @Description: 编辑单条设备
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateProductionDevice", method = RequestMethod.POST)
    public Object updateProductionDevice(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            ProductionDeviceVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ProductionDeviceVO());
            entity.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setUpdateuser(username);
            productionDeviceService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/01 0011 下午 3:21
     * @Description: 通过id删除生产装置设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteProductionDeviceByID", method = RequestMethod.POST)
    public Object deleteProductionDeviceByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            productionDeviceService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/01 0011 下午 3:31
     * @Description: 通过id查询生产装置设备信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getProductionDeviceDetailByID", method = RequestMethod.POST)
    public Object getProductionDeviceDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> detailInfo = productionDeviceService.getProductionDeviceDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @author: chengzq
     * @date: 2019/11/01 0011 下午 3:31
     * @Description: 通过id查询生产装置设备信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getProductionDeviceInfoByID", method = RequestMethod.POST)
    public Object getProductionDeviceInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> detailInfo = productionDeviceService.getProductionDeviceInfoByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

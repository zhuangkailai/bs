package com.tjpu.sp.controller.environmentalprotection.navigation;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.navigation.NavigationStandardVO;
import com.tjpu.sp.service.environmentalprotection.navigation.NavigationStandardService;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.NavigationEnum;

/**
 * @author: xsm
 * @date: 2020年8月31日 下午15:51
 * @Description:走航标准信息处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("NavigationStandard")
@Api(value = "走航标准信息处理类", tags = "走航标准信息处理类")
public class NavigationStandardController {
    @Autowired
    private NavigationStandardService NavigationStandardService;


    /**
     * @Author: xsm
     * @Date: 2020/08/31 0031 下午 1:52
     * @Description: 自定义查询条件查询走航污染物列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getNavigationStandardsByParamMap", method = RequestMethod.POST)
    public Object getNavigationStandardsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = NavigationStandardService.getNavigationStandardsByParamMap(jsonObject);
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
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 新增走航污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addNavigationStandard", method = RequestMethod.POST)
    public Object addNavigationStandard(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            NavigationStandardVO NavigationStandardVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new NavigationStandardVO());
            NavigationStandardVO.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            NavigationStandardVO.setUpdateuser(username);
            NavigationStandardVO.setPkId(UUID.randomUUID().toString());
            NavigationStandardService.insert(NavigationStandardVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 通过id获取走航污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getNavigationStandardByID", method = RequestMethod.POST)
    public Object getNavigationStandardByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            NavigationStandardVO NavigationStandardVO = NavigationStandardService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", NavigationStandardVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 修改走航污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateNavigationStandard", method = RequestMethod.POST)
    public Object updateNavigationStandard(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            NavigationStandardVO NavigationStandardVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new NavigationStandardVO());
            NavigationStandardVO.setUpdatetime(new Date());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            NavigationStandardVO.setUpdateuser(username);
            NavigationStandardService.updateByPrimaryKey(NavigationStandardVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 通过id删除走航污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteNavigationStandardByID", method = RequestMethod.POST)
    public Object deleteNavigationStandardByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            NavigationStandardService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/08/31 0031 上午 1:52
     * @Description: 通过id获取走航污染物详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getNavigationStandardDetailByID", method = RequestMethod.POST)
    public Object getNavigationStandardDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = NavigationStandardService.getNavigationStandardDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/01 0001 上午 8:36
     * @Description: 获取按污染物类型分组的走航污染物数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getNavigationStandardDataGroupByCategory", method = RequestMethod.POST)
    public Object getNavigationStandardDataGroupByCategory() throws Exception {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype",NavigationEnum.getCode());
            List<Map<String, Object>> result = NavigationStandardService.getNavigationStandardDataGroupByCategory(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/01 0001 上午 9:33
     * @Description: 获取各级别走航污染物数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getAllLevelNavigationStandardData", method = RequestMethod.POST)
    public Object getAllLevelNavigationStandardData() throws Exception {
        try {
            List<Map<String, Object>> result = NavigationStandardService.getAllLevelNavigationStandardData();
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/2/22 0022 下午 3:02
     * @Description: 验证重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_pollutantcode, standardlevel, fk_monitorpointtypecode]
     * @throws:
     */
    @RequestMapping(value = "/isHaveNavigationStandardData", method = RequestMethod.POST)
    public Object isHaveNavigationStandardData(@RequestJson(value = "fk_pollutantcode") String fk_pollutantcode,
                                               @RequestJson(value = "standardlevel") String standardlevel,
                                               @RequestJson(value = "fk_monitorpointtypecode") String fk_monitorpointtypecode,
                                               @RequestJson(value = "pollutantcategory") Integer pollutantcategory) throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("fk_pollutantcode",fk_pollutantcode);
            paramMap.put("standardlevel",standardlevel);
            paramMap.put("fk_monitorpointtypecode",fk_monitorpointtypecode);
            paramMap.put("pollutantcategory",pollutantcategory);
            Integer data=NavigationStandardService.CountStandardColorInfoByParamMap(paramMap);
            if(data>0){
                return AuthUtil.parseJsonKeyToLower("success", true);
            }else{
                return AuthUtil.parseJsonKeyToLower("success", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}

package com.tjpu.sp.controller.envhousekeepers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.EntWorkDynamicVO;
import com.tjpu.sp.service.envhousekeepers.EntWorkDynamicService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 企业工作动态
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("entWorkDynamic")
public class EntWorkDynamicController {


    @Autowired
    private EntWorkDynamicService entWorkDynamicService;

    /**
     * @Description: 添加或更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateData", method = RequestMethod.POST)
    public Object addOrUpdateData(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            EntWorkDynamicVO entWorkDynamicVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntWorkDynamicVO());
            entWorkDynamicVO.setUpdatetime(new Date());
            entWorkDynamicVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(entWorkDynamicVO.getPkId())) {//更新操作
                entWorkDynamicService.updateInfo(entWorkDynamicVO);
            } else {//添加操作
                entWorkDynamicVO.setPkId(UUID.randomUUID().toString());
                entWorkDynamicService.insertInfo(entWorkDynamicVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getEntWorkDynamicListDataByParamMap", method = RequestMethod.POST)
    public Object getEntWorkDynamicListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson){
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entWorkDynamicService.getListDataByParamMap(jsonObject);
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
     * @Description: 获取编辑回显或详情数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "getEditOrDetailsDataById", method = RequestMethod.POST)
    public Object getEditOrDetailsDataById(@RequestJson(value = "id") String id) {
        try {
            Map<String,Object> resultMap = entWorkDynamicService.getEditOrDetailsDataById(id);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @Description: 根据ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public Object deleteAppSuggestionById(
            @RequestJson(value = "id") String id
    ) {
        try {
            entWorkDynamicService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}

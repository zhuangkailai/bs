package com.tjpu.sp.controller.envhousekeepers.focusconcernentset;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.focusconcernentset.FocusConcernEntSetVO;
import com.tjpu.sp.service.envhousekeepers.focusconcernentset.FocusConcernEntSetService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.*;

/**
 * @author: xsm
 * @date: 2021/08/05 0005 下午 14:46
 * @Description: 重点关注企业设置
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("focusConcernEntSet")
public class FocusConcernEntSetController {

    @Autowired
    private FocusConcernEntSetService focusConcernEntSetService;

    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 14:46
     * @Description: 通过自定义参数查询重点关注企业设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getFocusConcernEntSetsByParamMap", method = RequestMethod.POST)
    public Object getFocusConcernEntSetsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = focusConcernEntSetService.getFocusConcernEntSetsByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", dataList);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 14:46
     * @Description: 新增重点关注企业设置
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addFocusConcernEntSet", method = RequestMethod.POST)
    public Object addFocusConcernEntSet(@RequestJson(value = "addformdata") Object addformdata
                                  ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            FocusConcernEntSetVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), FocusConcernEntSetVO.class);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setConcerntime(new Date());
            entity.setConcernuser(userid);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            focusConcernEntSetService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 14:46
     * @Description: 通过id删除重点关注企业设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteFocusConcernEntSetByID", method = RequestMethod.POST)
    public Object deleteFocusConcernEntSetByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            focusConcernEntSetService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 4:03
     * @Description: 验证重点关注企业是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsFocusConcernEntSetValidByPollutionid", method = RequestMethod.POST)
    public Object IsFocusConcernEntSetValidByPollutionid(
            @RequestJson(value = "pollutionid") String pollutionid
    ) {
        try {
            Map<String, Object> resultmap = focusConcernEntSetService.IsFocusConcernEntSetValidByPollutionid(pollutionid);
            String flag = "no";
            if (resultmap != null) {    //不等于空，表示重复 不可以添加
                flag = "yes";
            }
            return AuthUtil.parseJsonKeyToLower("success", flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

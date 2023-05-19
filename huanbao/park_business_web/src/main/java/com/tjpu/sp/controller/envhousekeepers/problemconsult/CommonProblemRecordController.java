package com.tjpu.sp.controller.envhousekeepers.problemconsult;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.envhousekeepers.problemconsult.CommonProblemRecordVO;
import com.tjpu.sp.service.envhousekeepers.problemconsult.CommonProblemRecordService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.*;


/**
 * @author: xsm
 * @date: 2021/08/24 0024 下午 16:06
 * @Description: 常见问题记录控制层
 */
@RestController
@RequestMapping("commonProblemRecord")
public class CommonProblemRecordController {

    @Autowired
    private CommonProblemRecordService commonProblemRecordService;


    /**
     * @author: xsm
     * @date: 2021/08/24 0024 下午 16:06
     * @Description: 通过自定义参数查询常见问题记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getCommonProblemRecordByParamMap", method = RequestMethod.POST)
    public Object getCommonProblemRecordByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> dataList = commonProblemRecordService.getCommonProblemRecordByParamMap(jsonObject);
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
     * @date: 2021/08/24 0024 下午 16:06
     * @Description: 新增常见问题记录记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addCommonProblemRecord", method = RequestMethod.POST)
    public Object addCommonProblemRecord(@RequestJson(value = "addformdata") Object addformdata
    ) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            CommonProblemRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), CommonProblemRecordVO.class);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setPkId(UUID.randomUUID().toString());
            entity.setRecorduser(username);
            entity.setRecordtime(new Date());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            commonProblemRecordService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/24 0024 下午 16:06
     * @Description: 通过id获取常见问题记录记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getCommonProblemRecordByID", method = RequestMethod.POST)
    public Object getCommonProblemRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> entity = commonProblemRecordService.getCommonProblemRecordDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/24 0024 下午 16:06
     * @Description: 修改常见问题记录记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateCommonProblemRecord", method = RequestMethod.POST)
    public Object updateCommonProblemRecord(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            CommonProblemRecordVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), CommonProblemRecordVO.class);
            CommonProblemRecordVO obj = commonProblemRecordService.selectByPrimaryKey(entity.getPkId());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setRecorduser(obj.getRecorduser());
            entity.setRecordtime(obj.getRecordtime());
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            commonProblemRecordService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/24 0024 下午 16:06
     * @Description: 通过id删除常见问题记录记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteCommonProblemRecordByID", method = RequestMethod.POST)
    public Object deleteCommonProblemRecordByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            commonProblemRecordService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/24 0024 下午 16:06
     * @Description: 通过id获取常见问题记录记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getCommonProblemRecordDetailByID", method = RequestMethod.POST)
    public Object getCommonProblemRecordDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = commonProblemRecordService.getCommonProblemRecordDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/24 0024 下午 16:06
     * @Description: 获取主要常见问题
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getKeyCommonProblemRecordByParam", method = RequestMethod.POST)
    public Object getKeyCommonProblemRecordByParam(@RequestJson(value = "typenum") Integer typenum,
                                                   @RequestJson(value = "pagesize") Integer pagesize) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("typenum",typenum);
            param.put("pagesize",pagesize);
            List<Map<String, Object>> dataList = commonProblemRecordService.getKeyCommonProblemRecordByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

package com.tjpu.sp.controller.environmentalprotection.dangerwaste;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.dangerwaste.DangerWasteLicenceInfoVO;
import com.tjpu.sp.service.environmentalprotection.dangerwaste.DangerWasteLicenceInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: xsm
 * @description: 危废许可证表
 * @create: 2019-10-21 17:23
 * @version: V1.0
 */
@RestController
@RequestMapping("dangerWasteLicenceInfo")
public class DangerWasteLicenceInfoController {
    @Autowired
    private DangerWasteLicenceInfoService dangerWasteLicenceInfoService;

    /**
     * @Author: xsm
     * @Date: 2019-10-18 9:45
     * @Description: 自定义查询条件查询危废许可证表列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getDangerWasteLicenceInfosByParamMap", method = RequestMethod.POST)
    public Object getDangerWasteLicenceInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist = dangerWasteLicenceInfoService.getDangerWasteLicenceInfosByParamMap(jsonObject);
         if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                List<Map<String, Object>> dataList = getPageData(datalist, Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
                resultMap.put("total", datalist.size());
                resultMap.put("datalist", dataList);
            } else {
                resultMap.put("datalist", datalist);
                resultMap.put("total", datalist.size());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:47
     * @Description: 新增危废许可证表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addDangerWasteLicenceInfo", method = RequestMethod.POST)
    public Object addDangerWasteLicenceInfo(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            DangerWasteLicenceInfoVO dangerWasteLicenceInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new DangerWasteLicenceInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            dangerWasteLicenceInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            dangerWasteLicenceInfoVO.setUpdateuser(username);
            dangerWasteLicenceInfoVO.setPkLicenceid(UUID.randomUUID().toString());
            dangerWasteLicenceInfoService.insert(dangerWasteLicenceInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:47
     * @Description: 通过id获取危废许可证表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getDangerWasteLicenceInfoByID", method = RequestMethod.POST)
    public Object getDangerWasteLicenceInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            DangerWasteLicenceInfoVO dangerWasteLicenceInfoVO = dangerWasteLicenceInfoService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", dangerWasteLicenceInfoVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:47
     * @Description: 修改危废许可证表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateDangerWasteLicenceInfo", method = RequestMethod.POST)
    public Object updateDangerWasteLicenceInfo(@RequestJson(value = "updateformdata") Object updateformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            DangerWasteLicenceInfoVO dangerWasteLicenceInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new DangerWasteLicenceInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            dangerWasteLicenceInfoVO.setUpdateuser(username);
            dangerWasteLicenceInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            dangerWasteLicenceInfoService.updateByPrimaryKey(dangerWasteLicenceInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:48
     * @Description: 通过id删除危废许可证表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteDangerWasteLicenceInfoByID", method = RequestMethod.POST)
    public Object deleteDangerWasteLicenceInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            dangerWasteLicenceInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 9:48
     * @Description: 通过id获取危废许可证表详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getDangerWasteLicenceInfoDetailByID", method = RequestMethod.POST)
    public Object getDangerWasteLicenceInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = dangerWasteLicenceInfoService.getDangerWasteLicenceInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/22 0022 上午 9:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }

}

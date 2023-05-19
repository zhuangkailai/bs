package com.tjpu.sp.controller.environmentalprotection.entevaluation;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationIndexVO;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntEvaluationIndexService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: xsm
 * @description: 企业评价指标控制层
 * @create: 2022-03-04 09:16
 * @version: V1.0
 */
@RestController
@RequestMapping("entEvaluationIndex")
public class EntEvaluationIndexController {
    @Autowired
    private EntEvaluationIndexService entEvaluationIndexService;

    /**
     * @Author: xsm
     * @Date: 2022/03/04 0004 09:18
     * @Description: 自定义查询条件查询企业评价指标控列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getEntEvaluationIndexListDataByParamMap", method = RequestMethod.POST)
    public Object getEntEvaluationIndexListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entEvaluationIndexService.getEntEvaluationIndexListDataByParamMap(jsonObject);
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
     * @date: 2022/03/04 0004 上午 9:22
     * @Description: 新增企业评价指标信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addEntEvaluationIndexInfo", method = RequestMethod.POST)
    public Object addEntEvaluationIndexInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            EntEvaluationIndexVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntEvaluationIndexVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            entity.setPkId(pkid);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            entEvaluationIndexService.addEntEvaluationIndexInfo(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 上午 09:21
     * @Description: 修改企业评价指标信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateEntEvaluationIndexInfo", method = RequestMethod.POST)
    public Object updateEntEvaluationIndexInfo(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            EntEvaluationIndexVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntEvaluationIndexVO.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            entEvaluationIndexService.updateEntEvaluationIndexInfo(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 获取企业评价指标详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getEntEvaluationIndexDetailById", method = RequestMethod.POST)
    public Object getEntEvaluationIndexDetailById(@RequestJson(value = "id") String id ) throws Exception {
        try {
            Map<String,Object> objmap = entEvaluationIndexService.getEntEvaluationIndexDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 删除企业评价指标信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/deleteEntEvaluationIndexById", method = RequestMethod.POST)
    public Object deleteEntEvaluationIndexById(@RequestJson(value = "id") String id ) throws Exception {
        try {
            entEvaluationIndexService.deleteEntEvaluationIndexById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/08 0008 11:57
     * @Description: 获取所有指标类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getAllEntEvaluationIndexType", method = RequestMethod.POST)
    public Object getAllEntEvaluationIndexType() throws Exception {
        try {
            List<Map<String, Object>> datalist = entEvaluationIndexService.getAllEntEvaluationIndexType();
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/08 0008 11:57
     * @Description: 验证指标名称是否数据重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsHaveEntEvaluationIndexCode", method = RequestMethod.POST)
    public Object IsHaveEntEvaluationIndexCode(@RequestJson(value = "hasindexname") String hasindexname) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("hasindexname",hasindexname);
            List<Map<String, Object>> datalist = entEvaluationIndexService.getEntEvaluationIndexListDataByParamMap(param);
            if(datalist.size()>0){
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            }else{
                return AuthUtil.parseJsonKeyToLower("success", "no");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: xsm
     * @Date: 2022/03/04 0004 09:18
     * @Description: 获取企业评价指标评分页面
     * @UpdateUser: xsm
     * @UpdateDate: 2022/03/14 16:24
     * @UpdateDescription:根据选择的方案id获取配置的指标信息
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getEntEvaluationIndexPageData", method = RequestMethod.POST)
    public Object getEntEvaluationIndexPageData(@RequestJson(value = "schemeid") String schemeid) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("schemeid",schemeid);
            List<Map<String, Object>> datalist = entEvaluationIndexService.getEntEvaluationIndexPageData(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

package com.tjpu.sp.controller.environmentalprotection.entevaluation;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationSchemeVO;
import com.tjpu.sp.model.environmentalprotection.entevaluation.SchemeIndexConfigVO;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntEvaluationSchemeService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: xsm
 * @description: 企业评价方案控制层
 * @create: 2022-03-14 13:05
 * @version: V1.0
 */
@RestController
@RequestMapping("entEvaluationScheme")
public class EntEvaluationSchemeController {
    @Autowired
    private EntEvaluationSchemeService entEvaluationSchemeService;

    /**
     * @Author: xsm
     * @Date: 2022/03/14 0014 13:18
     * @Description: 自定义查询条件查询企业评价方案控列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getEntEvaluationSchemeListDataByParamMap", method = RequestMethod.POST)
    public Object getEntEvaluationSchemeListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entEvaluationSchemeService.getEntEvaluationSchemeListDataByParamMap(jsonObject);
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
     * @Description: 新增企业评价方案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addEntEvaluationSchemeInfo", method = RequestMethod.POST)
    public Object addEntEvaluationSchemeInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<String> indexdata = (List<String>) jsonObject.get("indexlist");
            EntEvaluationSchemeVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntEvaluationSchemeVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            entity.setPkSchemeid(pkid);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            List<SchemeIndexConfigVO> list = new ArrayList<>();
            for (String indexid:indexdata){
                SchemeIndexConfigVO oneobj = new SchemeIndexConfigVO();
                oneobj.setPkId(UUID.randomUUID().toString());
                oneobj.setUpdatetime(new Date());
                oneobj.setUpdateuser(username);
                oneobj.setFkSchemeid(pkid);
                oneobj.setFkEvaluationindexid(indexid);
                list.add(oneobj);
            }
            //添加评价方案信息和配置方案指标
            entEvaluationSchemeService.addEntEvaluationSchemeInfo(entity,list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/14 0014 下午 13:32
     * @Description: 修改企业评价方案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateEntEvaluationSchemeInfo", method = RequestMethod.POST)
    public Object updateEntEvaluationSchemeInfo(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            List<String> indexdata = (List<String>) jsonObject.get("indexlist");
            EntEvaluationSchemeVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntEvaluationSchemeVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            List<SchemeIndexConfigVO> list = new ArrayList<>();
            for (String indexid:indexdata){
                SchemeIndexConfigVO oneobj = new SchemeIndexConfigVO();
                oneobj.setPkId(UUID.randomUUID().toString());
                oneobj.setUpdatetime(new Date());
                oneobj.setUpdateuser(username);
                oneobj.setFkSchemeid(entity.getPkSchemeid());
                oneobj.setFkEvaluationindexid(indexid);
                list.add(oneobj);
            }
            //添加企业评价信息
            entEvaluationSchemeService.updateEntEvaluationSchemeInfo(entity,list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/04 0004 09:18
     * @Description: 获取企业评价方案详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getEntEvaluationSchemeDetailById", method = RequestMethod.POST)
    public Object getEntEvaluationSchemeDetailById(@RequestJson(value = "id") String id ) throws Exception {
        try {
            Map<String,Object> objmap = entEvaluationSchemeService.getEntEvaluationSchemeDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/14 0014 13:48
     * @Description: 验证是否数据重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsHaveEntEvaluationSchemeData", method = RequestMethod.POST)
    public Object IsHaveEntEvaluationSchemeData(@RequestJson(value = "hasschemename") String hasschemename ) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("hasschemename",hasschemename);
            List<Map<String, Object>> datalist = entEvaluationSchemeService.getEntEvaluationSchemeListDataByParamMap(param);
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
     * @author: xsm
     * @date: 2022/03/14 0014 13:52
     * @Description: 删除企业评价方案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/deleteEntEvaluationSchemeById", method = RequestMethod.POST)
    public Object deleteEntEvaluationSchemeById(@RequestJson(value = "id") String id ) throws Exception {
        try {
            entEvaluationSchemeService.deleteEntEvaluationSchemeById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
}

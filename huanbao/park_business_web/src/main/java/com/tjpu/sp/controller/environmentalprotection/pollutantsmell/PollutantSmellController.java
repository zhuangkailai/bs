package com.tjpu.sp.controller.environmentalprotection.pollutantsmell;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.model.environmentalprotection.pollutantsmell.PollutantSmellVO;
import com.tjpu.sp.service.common.UserAuthSupportService;
import com.tjpu.sp.service.environmentalprotection.pollutantsmell.PollutantSmellService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2019/10/26 0011 下午 1:58
 * @Description: 污染物味道控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("pollutantsmell")
public class PollutantSmellController {

    @Autowired
    private PollutantSmellService pollutantSmellService;
    @Autowired
    private UserAuthSupportService userAuthSupportService;


    private String sysmodel = "hiddenDangerQuery";


    /**
     * @author: chengzq
     * @date: 2019/10/26 0011 下午 2:31
     * @Description: 获取污染物味道列表初始化信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getPollutantSmellListPage", method = RequestMethod.POST)
    public Object getPollutantSmellListPage(@RequestJson(value = "paramsjson") Object paramsJson, HttpServletRequest request) throws ParseException {
        try {
            String token = request.getHeader("token");
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);

            List<Map<String, Object>> pollutantSmellByParamMap = pollutantSmellService.getPollutantSmellByParamMap(jsonObject);

            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(pollutantSmellByParamMap);
            long total = pageInfo.getTotal();

            //获取按钮
            Map<String, Object> userButtonAuthBySysmodelAndSessionId = userAuthSupportService.getUserButtonAuthBySysmodelAndSessionId(sysmodel, token);

            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                Integer pagenum = Integer.valueOf(jsonObject.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(jsonObject.get("pagesize").toString());
                pollutantSmellByParamMap=pollutantSmellByParamMap.stream().peek(m -> {
                    Set<Map<String,Object>> pollutants = (Set) m.get("pollutants");
                    String collect = pollutants.stream().filter(p -> p.get("pollutantname") != null).map(p -> p.get("pollutantname").toString()).collect(Collectors.joining("、"));
                    m.put("pollutants",collect);
                }).skip((pagenum-1)*pagesize).limit(pagesize).collect(Collectors.toList());
            }else{
                pollutantSmellByParamMap=pollutantSmellByParamMap.stream().peek(m -> {
                    Set<Map<String,Object>> pollutants = (Set) m.get("pollutants");
                    String collect = pollutants.stream().filter(p -> p.get("pollutantname") != null).map(p -> p.get("pollutantname").toString()).collect(Collectors.joining("、"));
                    m.put("pollutants",collect);
                }).collect(Collectors.toList());
            }

            userButtonAuthBySysmodelAndSessionId.put("datalist", pollutantSmellByParamMap);
            userButtonAuthBySysmodelAndSessionId.put("total", total);

            return AuthUtil.parseJsonKeyToLower("success", userButtonAuthBySysmodelAndSessionId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/10/26 0011 下午 2:58
     * @Description: 通过自定义参数获取污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getPollutantSmellByParamMap", method = RequestMethod.POST)
    public Object getPollutantSmellByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();

            List<Map<String,Object>> pollutantSmellByParamMap = pollutantSmellService.getPollutantSmellByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(pollutantSmellByParamMap);
            long total = pageInfo.getTotal();

            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                Integer pagenum = Integer.valueOf(jsonObject.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(jsonObject.get("pagesize").toString());
                pollutantSmellByParamMap=pollutantSmellByParamMap.stream().peek(m -> {
                    Set<Map<String,Object>> pollutants = (Set) m.get("pollutants");
                    String collect = pollutants.stream().filter(p -> p.get("pollutantname") != null).map(p -> p.get("pollutantname").toString()).collect(Collectors.joining("、"));
                    m.put("pollutants",collect);
                }).skip((pagenum-1)*pagesize).limit(pagesize).collect(Collectors.toList());
            }else{
                pollutantSmellByParamMap=pollutantSmellByParamMap.stream().peek(m -> {
                    Set<Map<String,Object>> pollutants = (Set) m.get("pollutants");
                    String collect = pollutants.stream().filter(p -> p.get("pollutantname") != null).map(p -> p.get("pollutantname").toString()).collect(Collectors.joining("、"));
                    m.put("pollutants",collect);
                }).collect(Collectors.toList());
            }
            resultMap.put("datalist", pollutantSmellByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/10/26 0011 下午 3:17
     * @Description: 新增污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addPollutantSmell", method = RequestMethod.POST)
    public Object addPollutantSmell(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            PollutantSmellVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PollutantSmellVO());

            List<PollutantSmellVO> insertList = new ArrayList<>();

            if (jsonObject.get("pollutants") != null && JSONArray.fromObject(jsonObject.get("pollutants")).size() > 0) {
                JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("pollutants"));
                for (Object pollutantcode : jsonArray) {
                    String code = pollutantcode == null ? "" : pollutantcode.toString();
                    PollutantSmellVO pollutantSmellVO = new PollutantSmellVO();
                    pollutantSmellVO.setPkId(UUID.randomUUID().toString());
                    pollutantSmellVO.setFkPollutantcode(code);
                    pollutantSmellVO.setCode(entity.getCode());
                    pollutantSmellVO.setName(entity.getName());
                    insertList.add(pollutantSmellVO);
                }
                pollutantSmellService.insertBatch(insertList);
            } else {
                entity.setPkId(UUID.randomUUID().toString());
                pollutantSmellService.insert(entity);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/10/26 0011 下午 3:19
     * @Description: 通过code获取污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/selectBySmellCode", method = RequestMethod.POST)
    public Object selectBySmellCode(@RequestJson(value = "smellcode") String smellcode) throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("smellcode",smellcode);
            Map<String, Object> map = pollutantSmellService.selectBySmellCode(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/10/26 0011 下午 3:19
     * @Description: 修改污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updatePollutantSmell", method = RequestMethod.POST)
    public Object updatePollutantSmell(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            PollutantSmellVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new PollutantSmellVO());
            List<PollutantSmellVO> insertList = new ArrayList<>();

            if (jsonObject.get("pollutants") != null && JSONArray.fromObject(jsonObject.get("pollutants")).size() > 0) {
                JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("pollutants"));
                for (Object pollutantcode : jsonArray) {
                    String code = pollutantcode == null ? "" : pollutantcode.toString();
                    PollutantSmellVO pollutantSmellVO = new PollutantSmellVO();
                    pollutantSmellVO.setPkId(UUID.randomUUID().toString());
                    pollutantSmellVO.setFkPollutantcode(code);
                    pollutantSmellVO.setCode(entity.getCode());
                    pollutantSmellVO.setName(entity.getName());
                    insertList.add(pollutantSmellVO);
                }
                pollutantSmellService.updateBatch(insertList,jsonObject.get("smellcode")==null?"":jsonObject.get("smellcode").toString());
            } else {
                entity.setFkPollutantcode("");
                pollutantSmellService.updateByCode(entity);
            }


            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/10/26 0011 下午 3:21
     * @Description: 通过code删除污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deletePollutantSmellBySmellcode", method = RequestMethod.POST)
    public Object deletePollutantSmellBySmellcode(@RequestJson(value = "smellcode") String smellcode) throws Exception {
        try {
            pollutantSmellService.deleteByCode(smellcode);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/10/26 0011 下午 3:31
     * @Description: 通过code查询污染物味道信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getPollutantSmellDetailBySmellcode", method = RequestMethod.POST)
    public Object getPollutantSmellDetailBySmellcode(@RequestJson(value = "smellcode") String smellcode) throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("smellcode",smellcode);
            Map<String, Object> detailInfo = pollutantSmellService.getPollutantSmellDetailBySmellCode(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 上午 10:06
     * @Description:  通过自定义参数获取味道对应污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype]
     * @throws:
     */
    @RequestMapping(value = "/getPollutantByPollutantType", method = RequestMethod.POST)
    public Object getPollutantByPollutantType(@RequestJson(value = "pollutanttype",required = false) String pollutanttype,
                                              @RequestJson(value = "smellcode",required = false) String smellcode) throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("pollutanttype",pollutanttype);
            paramMap.put("smellcode",smellcode);
            List<Map<String, Object>> pollutantByPollutantType = pollutantSmellService.getPollutantByPollutantType(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", pollutantByPollutantType.stream().distinct().collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

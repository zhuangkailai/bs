package com.tjpu.sp.controller.common;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.common.UserAuthSupportService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "userAuthSupport")
public class UserAuthSupportController {

    @Autowired
    private UserAuthSupportService userAuthSupportService;

    /**
     * @author: lip
     * @date: 2019/9/9 0009 下午 7:26
     * @Description: 根据菜单标记获取按钮权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getUserButtonAuthBySysmodel", method = RequestMethod.POST)
    public Object getUserButtonAuthBySysmodel(@RequestJson(value = "sysmodel", required = false) String sysmodel, HttpServletRequest request) {
        try {
            String redisKey = request.getHeader("token");
            Map<String, Object> resultMap = userAuthSupportService.getUserButtonAuthBySysmodelAndSessionId(sysmodel, redisKey);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取用户业务操作数据权限
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/1 16:16
     */
    @RequestMapping(value = "getUserModuleDataList", method = RequestMethod.POST)
    public Object getUserModuleDataList(@RequestJson(value = "userid") String userid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userid);
            List<Map<String, Object>> dataList = userAuthSupportService.getUserModuleDataListByParam(paramMap);
            if (dataList.size() > 0) {
                Map<String, List<Map<String, Object>>> typeAndDataList = dataList.stream()
                        .filter(m -> m.get("moduletype") != null)
                        .collect(Collectors.groupingBy(m ->  m.get("moduletype")+","+m.get("orderindex")));
                dataList.clear();
                String type;
                Integer index;
                List<Map<String,Object>> children;
                for (String type_index : typeAndDataList.keySet()) {
                    Map<String,Object> dataMap = new HashMap<>();
                    type = type_index.split(",")[0];
                    if (!type_index.split(",")[1].equals("null")){
                        index = Integer.parseInt(type_index.split(",")[1]);
                    }else {
                        index = Integer.MAX_VALUE;
                    }
                    dataMap.put("moduletypecode",type);
                    dataMap.put("moduletypename",CommonTypeEnum.ModuleTypeEnum.getNameByCode(type));
                    dataMap.put("orderindex",index);
                    children = typeAndDataList.get(type_index);
                    dataMap.put("children",children);
                    dataList.add(dataMap);
                }
                //排序
                dataList = dataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 更新用户业务操作数据权限
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/1 16:16
     */
    @RequestMapping(value = "updateUserModuleData", method = RequestMethod.POST)
    public Object updateUserModuleData(@RequestJson(value = "moduleids") List<String> moduleids,
                                       @RequestJson(value = "userid") String userid
                                       ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userid);
            paramMap.put("moduleids", moduleids);
            userAuthSupportService.batchAdd(userid,moduleids);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




}

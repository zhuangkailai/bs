package com.tjpu.sp.service.impl.common;

import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.common.UserMapper;
import com.tjpu.sp.service.common.UserAuthSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author: lip
 * @date: 2019/10/15 0015 上午 9:17
 * @Description: 用户权限实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@Service
@Transactional
public class UserAuthSupportServiceImpl implements UserAuthSupportService {


    @Autowired
    private UserMapper userMapper;

    /**
     * @author: lip
     * @date: 2019/10/15 0015 上午 9:21
     * @Description: 通过菜单标记和会话ID获取用户按钮权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getUserButtonAuthBySysmodelAndSessionId(String sysmodel, String sessionId) {
        List<Map<String, Object>> tablebuttondata = new ArrayList<>();
        List<Map<String, Object>> topbuttondata = new ArrayList<>();
        Map<String, List<Map<String, Object>>> usermenuandbuttonauth = RedisTemplateUtil.getRedisCacheDataByKey("usermenuandbuttonauth", sessionId, Map.class);
        if (usermenuandbuttonauth != null) {
            List<Map<String, Object>> buttonAuth = usermenuandbuttonauth.get(sysmodel.toLowerCase());
            if (buttonAuth != null && buttonAuth.size() > 0) {
                for (Map<String, Object> button : buttonAuth) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", button.get("button_code"));
                    map.put("icon", button.get("button_img"));
                    map.put("label", button.get("button_name"));
                    map.put("type", button.get("button_style"));
                    map.put("sortcode", button.get("sortcode"));
                    if ("1".equals(button.get("button_type"))) {
                        tablebuttondata.add(map);
                    } else if ("2".equals(button.get("button_type"))) {
                        topbuttondata.add(map);
                    }
                }
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        tablebuttondata.sort(Comparator.comparing(m -> m.get("sortcode") == null ? "-1" : m.get("sortcode").toString()));
        topbuttondata.sort(Comparator.comparing(m -> m.get("sortcode") == null ? "-1" : m.get("sortcode").toString()));
        resultMap.put("tablebuttondata", tablebuttondata);
        resultMap.put("topbuttondata", topbuttondata);
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getUserModuleDataListByParam(Map<String, Object> paramMap) {
        return userMapper.getUserModuleDataListByParam(paramMap);
    }

    @Override
    public void batchAdd(String userid, List<String> moduleids) {
        userMapper.deleteModuleDataByUserid(userid);
        String username = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        List<Map<String, Object>> addList = new ArrayList<>();
        Date updateTime = new Date();
        for (String moduleId : moduleids) {
            Map<String, Object> map = new HashMap<>();
            map.put("pkId",UUID.randomUUID().toString());
            map.put("updateuser",username);
            map.put("updatetime",updateTime);
            map.put("fkUserid",userid);
            map.put("moduleid",moduleId);
            addList.add(map);
        }
        userMapper.batchAddModuleData(addList);
    }
}

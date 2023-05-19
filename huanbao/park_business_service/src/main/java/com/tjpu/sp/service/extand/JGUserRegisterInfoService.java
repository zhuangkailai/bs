package com.tjpu.sp.service.extand;


import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface JGUserRegisterInfoService {

    void updateUserRegisterByParams(Map<String, Object> map);
    /**
     *
     * @author: lip
     * @date: 2019/8/2 0002 下午 2:53
     * @Description: 自定义查询条件获取用户注册信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getUserRegisterInfoListByParam(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2020/3/12 0012 下午 2:07
     * @Description: 根据推送类型获取用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getUserInfoByPushType(String pushType);

    /**
     *
     * @author: lip
     * @date: 2020/3/12 0012 下午 2:36
     * @Description: 自定义查询条件获取微信群信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getWeChartGroupByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2020/3/26 0026 上午 9:29
     * @Description: 获取用户微信消息推送设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getUserPushSetList();

    List<String> getUserPushPhoneByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getUserPushDataByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getTextMessageListData(JSONObject paramMap);
}

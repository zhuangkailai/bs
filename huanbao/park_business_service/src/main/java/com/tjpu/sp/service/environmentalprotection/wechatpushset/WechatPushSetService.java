package com.tjpu.sp.service.environmentalprotection.wechatpushset;

import com.tjpu.sp.model.environmentalprotection.wechatpushset.WechatPushSetVO;

import java.util.List;
import java.util.Map;

public interface WechatPushSetService {

    /**
     * @author: xsm
     * @date: 202003/20 0020 下午 2:36
     * @Description: 新增微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void addWechatPushSetInfo(List<WechatPushSetVO> listobjs);

    /**
     * @author: xsm
     * @date: 202003/20 0020 下午 3:14
     * @Description: 修改微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updateWechatPushSetInfo(List<WechatPushSetVO> listobjs, String pkid);

    /**
     * @author: xsm
     * @date: 202003/20 0020 下午 3:25
     * @Description: 删除微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void deleteWechatPushSetInfoByWechatName(String wechatname);

    /**
     * @author: xsm
     * @date: 202003/20 0020 下午 3:25
     * @Description: 删除微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getWechatPushSetInfosByParamMap(Map<String,Object> paramMap);

    Map<String,Object> getWechatPushSetInfoByWechatName(String wechatname);
}

package com.tjpu.auth.controller.common;

import com.alibaba.fastjson.JSON;
import com.tjpu.pk.common.utils.AESUtil;

import java.util.Map;
import java.util.UUID;

/**
 * @author: zzc
 * @date: 2018/7/3 13:57
 * @Description:基础控制层方法
 */
public class BaseController {
    /**
     * @author: zzc
     * @date: 2018/7/3 13:58
     * @Description: 解密前台传来的参数，转为map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public Map<String, Object> getParamMap(String paramJson) throws Exception {
        String decrypt = AESUtil.Decrypt(paramJson, AESUtil.KEY_Secret);// 解密后的字符串
        return JSON.parseObject(decrypt);
    }

    /**
     * 获取UUID
     *
     * @return 返回生成的UUID
     * @author hy
     * @date 2016-5-19
     */
    public String getUUID() {
        String s = UUID.randomUUID().toString();
        // 去掉“-”符号
        // s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24)
        return s;
    }

}

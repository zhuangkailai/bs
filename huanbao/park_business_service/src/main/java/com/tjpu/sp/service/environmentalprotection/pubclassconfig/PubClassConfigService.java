package com.tjpu.sp.service.environmentalprotection.pubclassconfig;

import java.util.List;
import java.util.Map;

public interface PubClassConfigService {
    List<Map<String,Object>> getPubClassConfigTreeData();

    int isTableDataHaveInfo(Map<String, Object> paramMap);


    /**
     *
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:07
     * @Description: 拼接sql，删除公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deletePubCodeDataByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:07
     * @Description: 拼接sql，添加公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    void addPubCodeDataByParam(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:07
     * @Description: 拼接sql，修改公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    void editPubCodeDataByParam(Map<String, Object> paramMap);
}

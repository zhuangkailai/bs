package com.tjpu.sp.service.common.pubcode;

import java.util.List;
import java.util.Map;

public interface PubCodeService {
    List<Map<String, Object>> getPubCodeDataByParam(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/10/14 0014 下午 2:19
     * @Description: 通过表名称、排序字段、where条件，获取公共代码表指定的数据(有二级缓存)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getPubCodesDataByParamWithCache(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/10/14 0014 下午 2:19
     * @Description: 通过表名称、排序字段、where条件，获取公共代码表指定的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getPubCodesDataByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPubTree(String tablename, String code) throws Exception;

    int isTableDataHaveInfo(Map<String,Object> paramMap);

    List<Map<String,Object>> getTreeDataByCodeValue(String pub_code_basin, String code, String name, String parentCode, String o, String o1, String code1);



}

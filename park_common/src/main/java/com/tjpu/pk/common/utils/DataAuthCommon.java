package com.tjpu.pk.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zhangzc
 * @date: 2018/6/12 11:18
 * @Description:配置数据权限
 */
public class DataAuthCommon {


    /**
     * @author: zhangzc
     * @date: 2018/6/21 9:26
     * @Description: 构建数据权限map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Map<String, Object> getAuthDataMap(String fieldName, Object fieldValue, boolean isEdit, boolean isDelete, boolean isDetail) {
        Map<String, Object> map = new HashMap<>();
        map.put("fieldname", fieldName);
        map.put("fieldvalue", fieldValue);
        List<String> buttonauthority = new ArrayList<>();
        if (!isEdit) {
            buttonauthority.add("editButton");
        }
        if (!isDelete) {
            buttonauthority.add("deleteButton");
        }
        if (!isDetail) {
            buttonauthority.add("detailsButton");
        }
        map.put("buttonauthority", buttonauthority);
        return map;
    }


    /**
     * @author: zhangzc
     * @date: 2018/6/13 10:38
     * @Description:数据权限处理
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static void formatDataAuth(List<Map<String, Object>> listInfo, List<Map<String, Object>> modelDataMap) {
        //根据模块名称去获取对应的数据权限集合
        assert modelDataMap != null;
        for (Map<String, Object> map : listInfo) {
            for (Map<String, Object> objectMap : modelDataMap) {
                String fieldName = objectMap.get("fieldname").toString();   //字段名称，根据该字段做数据对比
                if (map.get(fieldName) != null) {   //如果列表数据中有这个字段并且有值
                    final Object fieldValueInfo = objectMap.get("fieldvalue");    //要加数据权限的某条数据
                    final Object fieldValue = map.get(fieldName);   //列表数据中的对应配置字段的值
                    if (fieldValueInfo.toString().equalsIgnoreCase(fieldValue.toString())) {
                        map.put("disablebutton", objectMap.get("buttonauthority"));
                    }
                }
            }
        }
    }
}

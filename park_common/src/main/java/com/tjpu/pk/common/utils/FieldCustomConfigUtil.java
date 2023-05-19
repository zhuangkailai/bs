package com.tjpu.pk.common.utils;



import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.tjpu.pk.common.annotation.FieldCustomConfig;

/**
 * @author: chengzq
 * @date: 2018/7/13 16:58
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class FieldCustomConfigUtil {

    /**
     * @author: chengzq
     * @date: 2018/7/13
     * @Description: 获取 @FieldCustomConfg注解的属性值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [t：实体, property：前端传入的字段]
     * @throws:
     */
    public static <T> Map getColumnValue(T t,String property) {
        Field[] fields=t.getClass().getDeclaredFields();
        Field field;
        Map<String,String> values = new HashMap<String,String>();
        for (int i = 0; i <fields.length ; i++) {
            fields[i].setAccessible(true);
        }
        for(int i = 0;i<fields.length ; i++){
            try {
                field=t.getClass().getDeclaredField(fields[i].getName());
                FieldCustomConfig annotation = field.getAnnotation(FieldCustomConfig.class);
                String name = field.getName();
                if(name.equals(property)){
                    values.put("field",annotation.field());
                    values.put("operator",annotation.operator());
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
       return values;
    }
}

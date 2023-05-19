package com.tjpu.pk.common.utils;

import java.util.*;

import com.alibaba.fastjson.serializer.SerializerFeature;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年4月10日 下午1:56:41
 * @Description:认证处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public abstract class AuthUtil {
    public final static String CAHCETOKENNAME = "TOKEN_";// token相关缓存前缀名
    public static final String KEY_ID = "keyId_";

    /**
     * @return
     * @author: lip
     * @date: 2018年4月10日 下午1:14:30
     * @Description:微服务调用，请求参数格式化
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     */
    public static String paramDataFormat(Object param) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("microparamdata", param);
        JSONObject jsonObject = JSONObject.fromObject(map);
        JSONObject filterNull = filterNull(jsonObject);
        //System.out.println(filterNull);
        return filterNull.toString();
    }


    public static JSONObject filterNull(JSONObject jsonObj) {
        Iterator<String> it = jsonObj.keys();
        Object obj = null;
        String key = null;
        while (it.hasNext()) {
            key = it.next();
            obj = jsonObj.get(key);
            if (obj instanceof JSONObject) {
                filterNull((JSONObject) obj);
            }
            if (obj instanceof JSONArray) {
                JSONArray objArr = (JSONArray) obj;
                for (int i = 0; i < objArr.size(); i++) {
                    if (objArr.get(i) instanceof JSONObject) {
                        filterNull(objArr.getJSONObject(i));
                    }
                }
            }
            if (obj == null || obj instanceof JSONNull) {
                jsonObj.put(key, "");
            }
            if (obj.equals(null)) {
                jsonObj.put(key, "");
            }
        }
        return jsonObj;
    }


    /**
     * @param flag :是否成功标记
     * @param data ：响应数据
     * @return
     * @author: lip
     * @date: 2018年4月10日 上午9:15:24
     * @Description: 响应返回指定的object
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static Object returnObject(String flag, Object data) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("flag", flag);
        map.put("data", data);
        return JSONObject.fromObject(map);
    }



    public static Object reSubmit(Object data) {
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("flag","resubmit");
        jsonObject.put("data",data);
        return jsonObject;
    }
    /**
     * @param flag
     * @param data
     * @return
     * @author: lip
     * @date: 2018年8月22日 下午2:12:29
     * @Description: 将json对象的属性key，转换成全小写
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static Object parseJsonKeyToLower(String flag, Object data) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("flag", flag);
        map.put("data", data);
        JsonConfig jsonConfig = JSONObjectUtil.getRegisterDefaultJsonConfig();
        JSONObject jsonObject = JSONObject.fromObject(map, jsonConfig);
        jsonObject = AuthUtil.transObject(jsonObject);
        return encryptData(jsonObject);
    }


    public static Object parseJsonKeyToLowerNoEncrypt(String flag, Object data) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("flag", flag);
        map.put("data", data);
        JsonConfig jsonConfig = JSONObjectUtil.getRegisterDefaultJsonConfig();
        JSONObject jsonObject = JSONObject.fromObject(map, jsonConfig);
        jsonObject = AuthUtil.transObject(jsonObject);
        return jsonObject;
    }


    public static Object encryptData(Object jsonObject) {
        if (DataFormatUtil.parseProperties("isEncryption") != null) {
            Boolean isEncryption = Boolean.parseBoolean(DataFormatUtil.parseProperties("isEncryption"));
            if (isEncryption) {
                String secret = DataFormatUtil.parseProperties("secret");
                String returnData = jsonObject.toString();
                returnData = AESUtil.Encrypt(returnData, secret);
                return returnData;
            }else {
                return jsonObject;
            }
        }
        return jsonObject;
    }

    public static String decryptData(Object jsonObject) throws Exception {
        String returnData = jsonObject.toString();
        if (DataFormatUtil.parseProperties("isEncryption") != null) {
            Boolean isEncryption = Boolean.parseBoolean(DataFormatUtil.parseProperties("isEncryption"));
            if (isEncryption) {
                String secret = DataFormatUtil.parseProperties("secret");
                returnData = AESUtil.Decrypt(returnData, secret);
            }
        }
        return returnData;
    }

    /**
     * @author: lip
     * @date: 2020/6/24 0024 上午 8:34
     * @Description: 对象转换key值转换成小写
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static JSONObject parseObjectKeyToLower(JSONObject data) {
        JSONObject jsonObject = filterNull(data);
        return AuthUtil.transObject(jsonObject);
    }


    /**
     * @param jsonObject1
     * @return
     * @author: lip
     * @date: 2018年8月22日 下午1:53:12
     * @Description: json对象转换key值转换成小写
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static JSONObject transObject(JSONObject jsonObject1) {
        JSONObject jsonObject2 = new JSONObject();
        Iterator it = jsonObject1.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object object = jsonObject1.get(key);
            if (object.getClass().toString().endsWith("JSONObject")) {
                jsonObject2.accumulate(key.toLowerCase(), AuthUtil.transObject((JSONObject) object));
            } else if (object.getClass().toString().endsWith("JSONArray")) {
                jsonObject2.accumulate(key.toLowerCase(), AuthUtil.transArray(jsonObject1.getJSONArray(key)));
            } else {
                jsonObject2.accumulate(key.toLowerCase(), object);
            }
        }
        return jsonObject2;
    }

    /**
     * @param jsonArray1
     * @return
     * @author: lip
     * @date: 2018年8月22日 下午1:53:56
     * @Description: json数组遍历转换为json对象
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static JSONArray transArray(JSONArray jsonArray1) {
        JSONArray jsonArray2 = new JSONArray();
        for (int i = 0; i < jsonArray1.toArray().length; i++) {
            Object object = jsonArray1.get(i);
            if (object.getClass().toString().endsWith("JSONObject")) {
                jsonArray2.add(AuthUtil.transObject((JSONObject) object));
            } else if (object.getClass().toString().endsWith("JSONArray")) {
                jsonArray2.add(AuthUtil.transArray((JSONArray) object));
            } else {
                jsonArray2 = jsonArray1;
                break;
            }
        }
        return jsonArray2;
    }

    /**
     * @param flag :是否成功标记
     * @param data ：响应数据
     * @return
     * @author: lip
     * @date: 2018年4月10日 上午9:15:24
     * @Description: 响应返回指定的object
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static String returnLogJson(String flag, Object data) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("flag", flag);
        map.put("errormessage", data);
        JSONObject jsonObject = JSONObject.fromObject(map);

        return jsonObject.toString();
    }

}

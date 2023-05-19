package com.tjpu.sp.common.utils;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import net.sf.json.JSONObject;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * @author: zhangzhenchao
 * @date: 2019/11/2 13:56
 * @Description: 监测提醒工具类
 */
public class AlarmRemindUtil {


    /**
     * @author: zhangzc
     * @date: 2019/7/18 13:23
     * @Description: 通过menuid获取子菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getChildrenMenusByMenuId(String menuid, List<JSONObject> userAuthMenus, List<Map<String, Object>> resultList) {
        if (userAuthMenus != null && resultList == null) {
            for (JSONObject jsonObject : userAuthMenus) {
                if (menuid.equals(jsonObject.get("menuid").toString())) {
                    resultList = (List<Map<String, Object>>) jsonObject.get("datalistchildren");
                    break;
                } else {
                    userAuthMenus = (List<JSONObject>) jsonObject.get("datalistchildren");
                    resultList = getChildrenMenusByMenuId(menuid, userAuthMenus, resultList);
                }
            }
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getSonMenusByMenuCode(String menuCode, List<JSONObject> menus, List<Map<String, Object>> result) {
        if (menus != null && result == null) {
            for (JSONObject jsonObject : menus) {
                if (menuCode.equals(jsonObject.get("menucode").toString())) {
                    result = (List<Map<String, Object>>) jsonObject.get("datalistchildren");
                    break;
                } else {
                    menus = (List<JSONObject>) jsonObject.get("datalistchildren");
                    result = getSonMenusByMenuCode(menuCode, menus, result);
                }
            }
        }
        return result;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 10:35
     * @Description: 获取时间字段名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getTimeFieldNameByRemindType(int remindType) {
        CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindType);
        Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
        String timeFieldName = "MonitorTime";
        switch (remindTypeEnum) {
            case ExceptionAlarmEnum:
                timeFieldName = "ExceptionTime";
                break;
            case WaterNoFlowEnum:
                timeFieldName = "ExceptionTime";
                break;
            case OverAlarmEnum:
                timeFieldName = "OverTime";
                break;
            case EarlyAlarmEnum:
                timeFieldName = "EarlyWarnTime";
                break;
            case ExceptionAlarmEnum2:
                timeFieldName = "FirstExceptionTime";
                break;
            case OverAlarmEnum2:
                timeFieldName = "FirstOverTime";
                break;
            case EarlyAlarmEnum2:
                timeFieldName = "FirstOverTime";
                break;
        }
        return timeFieldName;
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/19 10:35
     * @Description: 获取时间字段名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getCollectionByRemindType(int remindType) {
        CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindType);
        Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
        String collection = "";
        switch (remindTypeEnum) {
            case ExceptionAlarmEnum:
                collection = "ExceptionData";
                break;
            case WaterNoFlowEnum:
                collection = "ExceptionData";
                break;
            case OverAlarmEnum:
                collection = "OverData";
                break;
            case EarlyAlarmEnum:
                collection = "EarlyWarnData";
                break;
            case FlowChangeEnum:
                collection = "HourFlowData";
                break;
            case ConcentrationChangeEnum:
                collection = "MinuteData";
                break;
            case StorageTankDataChangeEnum:
                collection = "RealTimeData";
                break;
            case ExceptionAlarmEnum2:
                collection = "ExceptionModel";
                break;
            case OverAlarmEnum2:
                collection = "OverModel";
                break;
            case EarlyAlarmEnum2:
                collection = "OverModel";
                break;
        }
        return collection;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 10:35
     * @Description: 获取时间字段名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getUnWindowNameByRemindType(int remindType) {
        CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindType);
        Assert.notNull(remindTypeEnum, "remindTypeEnum must not be null!");
        String unWindName = null;
        switch (remindTypeEnum) {
            case ConcentrationChangeEnum:
                unWindName = "MinuteDataList";
                break;
            case FlowChangeEnum:
                unWindName = "HourFlowDataList";
                break;
            case StorageTankDataChangeEnum:
                unWindName = "RealDataList";
                break;
        }
        return unWindName;
    }


    /**
     * @author: lip
     * @date: 2019/10/25 0025 下午 5:32
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramsInfo：{key:value}
     * @return: 符合条件的{key:value}
     */
    public static Map<String, Float> findOutLiers(LinkedHashMap<String, Float> paramsInfo) {
        if (paramsInfo == null || paramsInfo.size() == 0) return null;
        Map<String, Float> params = paramsInfo.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
        Map<String, Float> resultMap = new LinkedHashMap<>();
        BigDecimal[] datas = new BigDecimal[params.size()];
        String[] names = new String[params.size()];
        Float[] values = new Float[params.size()];
        int i = -1;
        for (Map.Entry<String, Float> entry : params.entrySet()) {
            Float value = entry.getValue();
            i++;
            datas[i] = BigDecimal.valueOf(value);
            names[i] = entry.getKey();
            values[i] = value;
        }
        int len = datas.length;
        if (1 < len && len < 4) {
            if (len == 2) {
                if (datas[1].divide(datas[0], ROUND_HALF_UP, 4).floatValue() > 2) {
                    resultMap.put(names[1], values[1]);
                }
            } else {
                BigDecimal standardDeviation = getStandardDeviation(datas);
                float v = (datas[1].subtract(datas[0])).floatValue();
                if (v > standardDeviation.floatValue()) {
                    resultMap.put(names[1], values[1]);
                }
                float v1 = (datas[2].subtract(datas[1])).floatValue();
                if (v1 > standardDeviation.floatValue()) {
                    resultMap.put(names[2], values[2]);
                }
            }
        } else if (len >= 4) {
            BigDecimal q1;
            BigDecimal q3;
            BigDecimal upperLimitValue;
            int index;
            // n代表项数，因为下标是从0开始所以这里理解为：len = n+1
            if (len % 2 == 0) { // 偶数
                index = new BigDecimal(len).divide(new BigDecimal("4")).intValue();
                q1 = datas[index - 1].multiply(new BigDecimal("0.25")).add(datas[index].multiply(new BigDecimal("0.75")));
                index = new BigDecimal(3 * (len + 1)).divide(new BigDecimal("4")).intValue();
                q3 = datas[index - 1].multiply(new BigDecimal("0.75")).add(datas[index].multiply(new BigDecimal("0.25")));
            } else { // 奇数
                q1 = datas[new BigDecimal(len).multiply(new BigDecimal("0.25")).intValue()];
                q3 = datas[new BigDecimal(len).multiply(new BigDecimal("0.75")).intValue()];
            }
            BigDecimal num1 = new BigDecimal("1.5");
            upperLimitValue = q3.subtract(q1).multiply(num1).add(q3);
            for (int k = 0; k < datas.length; k++) {
                if (datas[k].compareTo(upperLimitValue) > 0) {
                    resultMap.put(names[k], values[k]);
                }
            }
        }
        return resultMap;
    }

    /**
     * @author: zzc
     * @date: 2019/9/25 18:58
     * @Description: 求出标准差
     * @param:
     * @return:
     */
    private static BigDecimal getStandardDeviation(BigDecimal[] datas) {
        BigDecimal sum = new BigDecimal(0);
        for (BigDecimal data : datas) {
            sum = sum.add(data);
        }
        BigDecimal avg = sum.divide(new BigDecimal(datas.length), ROUND_HALF_UP, 4);
        BigDecimal total = new BigDecimal(0);   //平方和
        for (BigDecimal data : datas) total = total.add((data.subtract(avg)).pow(2));
        BigDecimal divide = total.divide(new BigDecimal(datas.length), ROUND_HALF_UP, 4);
        return DataFormatUtil.sqrt(divide, 4).add(avg);
    }








}

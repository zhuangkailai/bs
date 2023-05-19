package com.tjpu.sp.common.anticontrol;

import java.util.Arrays;
import java.util.List;

/**
 * @author: xsm
 * @date: 2022/01/04 0004 下午 1:49
 * @Description: 反控命令默认值枚举类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
public class DefaultValueEnum {

    /**
     * @author: xsm
     * @date: 2022/01/05 15:27
     * @Description: 反控命令公共key枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AntiControlCommonKeyEnum {
        QNEnum("qn", "请求编码"),
        STEnum("st", "系统编码"),
        CNEnum("cn", "命令编码"),
        PWEnum("pw", "访问密码"),
        MNEnum("mn", "设备标识"),
        FLAGEnum("flag", "拆分包应答标志"),
        CPEnum("cp", "指令参数");

        AntiControlCommonKeyEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getCode() {return code; }

        public String getName() {
            return name;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/05 0005 下午 3:34
     * @Description: 获取反控公共参数key（反控队列需要）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<String> getCommonKeyList() {
        return Arrays.asList(
                AntiControlCommonKeyEnum.STEnum.getCode(),
                AntiControlCommonKeyEnum.CNEnum.getCode(),
                AntiControlCommonKeyEnum.PWEnum.getCode(),
                AntiControlCommonKeyEnum.STEnum.getCode(),
                AntiControlCommonKeyEnum.MNEnum.getCode(),
                AntiControlCommonKeyEnum.FLAGEnum.getCode());
    }

    /**
     * @author: xsm
     * @date: 2022/01/04 13:50
     * @Description: 默认值(默认方法)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum DefaultValueMethod {
        GetNewSystemTime("getNewSystemTime()", "获取当前系统时间"),
        GetSystemCoding("getSystemCoding()", "获取系统编码"),
        GetPointAccessPWD("getPointAccessPWD()", "获取访问密码");

        DefaultValueMethod(String value, String name) {
            this.value = value;
            this.name = name;
        }

        private final String value;
        private final String name;

        public String getValue() {return value; }

        public String getName() {
            return name;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/04 13:50
     * @Description: 下拉选择值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum SelectValueMethod {
        GetMinInterval("getMinInterval()", "获取分钟数据间隔"),
        GetPointPollutantData("getPointPollutantData()", "获取点位污染物"),
        GetInfoids("getInfoids()", "获取现场端信息编码");

        SelectValueMethod(String value, String name) {
            this.value = value;
            this.name = name;
        }

        private final String value;
        private final String name;

        public String getValue() {return value; }

        public String getName() {
            return name;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/04 15:50
     * @Description: 反控命令时间前端展示格式和后台接收格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum TimeFormatEnum {
        SecondTimeEnum("yyyyMMddHHmmss","yyyy-MM-dd HH:mm:ss","datetimerange"),
        MinuteTimeEnum("yyyyMMddHHmm00","yyyy-MM-dd HH:mm","datetimerange"),
        HourTimeEnum("yyyyMMddHH0000", "yyyy-MM-dd HH","datetimerange"),
        DayTimeEnum("yyyyMMdd000000", "yyyy-MM-dd","daterange");

        TimeFormatEnum(String value,String formatstr,String datetype) {
            this.value = value;
            this.formatstr = formatstr;
            this.datetype = datetype;
        }

        private final String value;
        private final String formatstr;
        private final String datetype;

        public String getValue() {return value; }

        public String getFormatstr() {
            return formatstr;
        }

        public String getDatetype() {
            return datetype;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/01/04 0004 下午 18:06
     * @Description: 反控命令系统编码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AntiControlSystemCodeEnum {
        WasteWaterSystemEnum(1, "32"),
        WasteGasSystemEnum(2, "31"),
        SmokeSystemEnum(22, "31"),
        RainSystemEnum(37, "32");
        AntiControlSystemCodeEnum(int type, String code) {
            this.type = type;
            this.code = code;

        }

        private final int type;
        private final String code;


        public int getType() {
            return type;
        }

        public String getCode() {
            return code;
        }

        public static String getCodeByType(Integer Type) {
            for (DefaultValueEnum.AntiControlSystemCodeEnum enums : DefaultValueEnum.AntiControlSystemCodeEnum.values()) {
                if (enums.getType() == Type) {
                    return enums.code;
                }
            }
            return "";
        }

    }

}

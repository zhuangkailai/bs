package com.tjpu.pk.common.utils;

/**
 * @author: zzc
 * @date: 2018/5/1610:39
 * @Description:常量信息枚举类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class ConstantsUtil {
    /* 默认密码 123456 */
    public static final String DEFAULT_USER_PWD = "yq@123456";

    /**
     * @author: zzc
     * @date: 2018/6/25 16:23
     * @Description: 默认字段值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum DefaultMethod {
        GETLOGINUSERID("getLoginUserId()", "获取当前登陆用户ID"),
        GETLOGINACCOUNT("getLoginAccount()", "获取当前用户登录账户名"),
        GETCURRENTLYDATA("getCurrentlyDate()", "获取当前时间"),
        GETUUID("getUUID()", "获取UUID"),
        getLoginUserName("getLoginUserName()", "获取当前登录用户姓名"),
        GETINITIALPASSWORD("getInitialPassword()", "默认密码"),
        getWaterOutPutCode("getWaterOutPutCode(fk_pollutionid)", "调用存储过程废水排口编码"),
        getNoiseCode("getNoiseCode(fk_pollutionid)", "调用存储过程噪声监测点排口编码"),
        getGasOutputCode("getGasOutputCode(fk_pollutionid)", "调用存储过程废气监测点排口编码");

        DefaultMethod(String value, String name) {
            this.value = value;
            this.name = name;
        }

        private final String value;
        private final String name;

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * @param
     * @author: zhangzc
     * @date: 2018/5/17 9:25
     * @Description: 字段类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @return
     */
    public enum FieldConfigType {
        LIST("list", "列表展示"),
        QUERY("query", "查询"),
        ADD("add", "增加展示"),
        DELETE("delete", "删除"),
        DETAIL("detail", "详情"),
        EDIT("edit", "修改");

        FieldConfigType(String value, String name) {
            this.value = value;
            this.name = name;
        }

        private final String value;
        private final String name;

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * @param
     * @author: zhangzc
     * @date: 2018/5/17 9:24
     * @Description: 控件类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @return
     */
    public enum ControlTypeEnum {
        text("text", "文本框"),
        number("number", "数字类型"),
        file("file", "文件类型"),
        image("image", "图片类型"),
        password("password", "密码框"),
        textarea("textarea", "文本域"),
        checkbox("checkbox", "多选框"),
        TimePicker("TimePicker", "时间选择器"),
        DatePicker("DatePicker", "日期天选择器"),
        DatePicker_oneInputTwoValue("DatePicker_oneInputTwoValue", "日期选择器一个控件两个值"),
        DatePickerMonth_oneInputTwoValue("DatePickerMonth_oneInputTwoValue", "日期选择器一个控件两个值"),
        DatePicker_month("DatePicker_month", "日期月份选择器"),
        DatePicker_year("DatePicker_year", "日期年选择器"),
        DatePicker_dates("DatePicker_dates", "多个日期选择器"),
        DateTimePicker("DateTimePicker", "日期时间选择器"),
        cascader("cascader", "级联"),
        radio("radio", "单选框"),
        //范围选择器 两个控件都是文本框的
        textScope("textScope", "两个控件都是文本框的"),
        //范围选择器 两个控件都是日期控件
        DatePickerScope("DatePickerScope", " 两个控件都是日期控件"),
        datePickerScope_Date("datePickerScope_Date", " 两个控件都是日期控件"),
        datePickerScope_Month("datePickerScope_Month", " 两个控件都是日期控件"),
        datePickerScope_Year("datePickerScope_Year", " 两个控件都是日期控件"),
        select("select", "单选下拉列表框"),
        selectInput("selectInput", "单选下拉输入列表框"),
        selectChecked("selectChecked", "多选下拉列表框"),
        treeSelect("treeselect", "单选下拉树"),
        yearQuarter("yearQuarter", "季度控件"),
        array("array", "数组"),
        treeSelectChecked("treeSelectChecked", "多选下拉树");


        ControlTypeEnum(String value, String name) {
            this.value = value;
            this.name = name;
        }

        private final String value;
        private final String name;

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public static ControlTypeEnum getControlTypeEnumByControlType(String value) {
        ControlTypeEnum[] values = ControlTypeEnum.values();
        for (ControlTypeEnum controlTypeEnum : values) {
            if (controlTypeEnum.getValue().equals(value)) {
                return controlTypeEnum;
            }
        }
        return null;
    }

}

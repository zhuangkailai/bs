package com.tjpu.sp.common.enumconfig;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;

/**
 * @author: lip
 * @date: 2019/6/18 0018 下午 7:19
 * @Description: 公共类型枚举类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
public class CommonTypeEnum {

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:52
     * @Description: 获取“排放口”类型的编码类型集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getOutPutTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.WasteWaterEnum.getCode(),
                WasteGasEnum.getCode(),
                SmokeEnum.getCode(),
                SecurityCombustibleMonitor.getCode(),
                SecurityToxicMonitor.getCode(),
                MonitorPointTypeEnum.RainEnum.getCode());
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:52
     * @Description: 六参数+AQI的编码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<String> getSixIndexList() {
        return Arrays.asList(
                AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_AQI.code,
                AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_PM25.code,
                AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_PM10.code,
                AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_CO.code,
                AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_O3.code,
                AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_NO2.code,
                AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_SO2.code
        );
    }


    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:52
     * @Description: 获取“监测点”类型的编码类型集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.meteoEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(),
                MonitorPointTypeEnum.AirEnum.getCode(),
                TZFEnum.getCode(),
                XKLWEnum.getCode(),
                MonitorPointTypeEnum.AirEnum.getCode(),
                MonitorPointTypeEnum.WaterQualityEnum.getCode(),
                MonitorPointTypeEnum.MicroStationEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(),
                MonitorPointTypeEnum.SecurityLeakageMonitor.getCode(),
                MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode(),
                MonitorPointTypeEnum.SecurityToxicMonitor.getCode(),
                MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                unOrgGasEnum.getCode());
    }

    public static List<Integer> getOtherMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(),
                TZFEnum.getCode(),
                XKLWEnum.getCode(),
                unOrgGasEnum.getCode(),
                MonitorPointTypeEnum.MicroStationEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
    }

    /**
     * @author: lip
     * @date: 2019/7/24 0024 下午 7:38
     * @Description: 获取所有监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getAllMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(),
                MonitorPointTypeEnum.AirEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                MonitorPointTypeEnum.RainEnum.getCode(),
                MonitorPointTypeEnum.WasteWaterEnum.getCode(),
                WasteGasEnum.getCode(),
                MonitorPointTypeEnum.meteoEnum.getCode(),
                MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(),
                MonitorPointTypeEnum.MicroStationEnum.getCode(),
                MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
    }

    /**
     * @author: xsm
     * @date: 2020/06/09 0009 下午 2:20
     * @Description: 获取所有环保监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getAllEnvMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(),
                MonitorPointTypeEnum.AirEnum.getCode(),
                MonitorPointTypeEnum.WaterQualityEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                MonitorPointTypeEnum.RainEnum.getCode(),
                MonitorPointTypeEnum.SmokeEnum.getCode(),
                MonitorPointTypeEnum.WasteWaterEnum.getCode(),
                MonitorPointTypeEnum.WasteGasEnum.getCode(),
                MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(),
                MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
    }

    /**
     * @author: xsm
     * @date: 2021/01/26 0026 上午 11:10
     * @Description: 获取环保点位任务监测的监测点类型（非企业关联）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getEnvPointTaskMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(),
                MonitorPointTypeEnum.MicroStationEnum.getCode());
    }

    /**
     * @author: xsm
     * @date: 2022/04/18 0018 下午 2:42
     * @Description: 获取非其它监测点表里的监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getNotOtherPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.WasteWaterEnum.getCode(),//废水
                MonitorPointTypeEnum.WasteGasEnum.getCode(),//废气
                MonitorPointTypeEnum.SmokeEnum.getCode(),//烟气
                MonitorPointTypeEnum.RainEnum.getCode(),//雨水
                MonitorPointTypeEnum.AirEnum.getCode(),//常规空气
                FactoryBoundaryStinkEnum.getCode(),//厂界恶臭
                MonitorPointTypeEnum.WaterQualityEnum.getCode());//水质
    }


    /**
     * @author: xsm
     * @date: 2019/8/12 0012 下午 6:57
     * @Description: 获取污染源关联的所有监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getPollutionMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.RainEnum.getCode(),
                MonitorPointTypeEnum.WasteWaterEnum.getCode(),
                MonitorPointTypeEnum.WasteGasEnum.getCode(),
                MonitorPointTypeEnum.SmokeEnum.getCode(),
                MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(),
                MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
    }

    /**
     * @author: xsm
     * @date: 2020/10/20 0020 下午 2:01
     * @Description: 获取风险区域监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getRiskAreaMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.SecurityLeakageMonitor.getCode(),
                SecurityCombustibleMonitor.getCode(),
                SecurityToxicMonitor.getCode()
        );
    }

    /**
     * @author: xsm
     * @date: 2020/11/05 0005 下午 2:52
     * @Description: 获取安全首页监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getSecurityHomePageMonitorPointTypes() {
        return Arrays.asList(
                MonitorPointTypeEnum.ProductionSiteEnum.getCode(),
                MonitorPointTypeEnum.StorageTankAreaEnum.getCode(),
                SecurityCombustibleMonitor.getCode(),
                SecurityToxicMonitor.getCode()
        );
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:52
     * @Description: 获取“厂界监测点”类型的编码类型集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getEntMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(),
                MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()
        );
    }

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 下午 5:26
     * @Description: 获取报警任务状态编码类型集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getAlarmTaskStatusList() {
        return Arrays.asList(
                AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode(),
                AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode(),
                AlarmTaskDisposalScheduleEnum.HandleEnum.getCode(),
                AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode()
        );
    }

    /**
     * @author: xsm
     * @date: 2019/11/12 0012 上午 8:38
     * @Description: 获取投诉任务状态编码类型集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getComplaintTaskStatusList() {
        return Arrays.asList(
                ComplaintTaskEnum.UnassignedTaskEnum.getCode(),
                ComplaintTaskEnum.UndisposedEnum.getCode(),
                ComplaintTaskEnum.HandleEnum.getCode(),
                ComplaintTaskEnum.CompletedEnum.getCode()
        );
    }

    /**
     * @author: xsm
     * @date: 2019/11/22 0022 上午 11:21
     * @Description: 异常报警主要异常类型编码集合（零值异常、连续值异常、超限异常、无流量异常）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<String> getExceptionMainTypeList() {
        return Arrays.asList(
                ExceptionTypeEnum.ZeroExceptionEnum.getCode(),
                ExceptionTypeEnum.ContinuousExceptionEnum.getCode(),
                ExceptionTypeEnum.OverExceptionEnum.getCode(),
                ExceptionTypeEnum.NoFlowExceptionEnum.getCode());
    }

    /**
     * @author: chengzq
     * @date: 2020/7/30 0030 下午 1:39
     * @Description: 排除权限监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    public static List<String> getExcludeAuthTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.StorageTankAreaEnum.getCode() + "",
                MonitorPointTypeEnum.SecurityLeakageMonitor.getCode() + "");
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 监测点类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum MonitorPointTypeEnum {

        WasteWaterEnum(1, "废水监测点"),
        RainEnum(37, "雨水监测点"),


        WasteGasEnum(2, "废气监测点"),
        SmokeEnum(22, "烟气监测点"),
        SmokeGasEnum(941, "烟气废气合并类型"),//自定义

        FactoryBoundaryStinkEnum(40, "厂界恶臭监测点"),

        StinkEnum(940, "恶臭监测点"),

        SolidWasteEnum(13, "固废"),

        FactoryBoundarySmallStationEnum(41, "厂界小型站监测点"),
        unOrganizationWasteGasEnum(38, "废气无组织监测点"),
        //永修（类似恶臭）
        unOrgGasEnum(61, "废气无组织监测点"),


        EnvironmentalStinkEnum(9, "恶臭监测点"),
        XKLWEnum(14, "细颗粒物"),
        TZFEnum(15, "碳组分"),
        EnvironmentalVocEnum(10, "VOC监测点"),
        NavigationEnum(11, "走航监测点"),
        meteoEnum(52, "气象监测点"),
        MicroStationEnum(33, "微型站"),
        EnvironmentalDustEnum(12, "扬尘测点"),


        AirEnum(5, "常规空气监测点"),
        WaterQualityEnum(6, "水质监测点"),
        GroundWaterEnum(7, "地下水监测点"),
        DBWaterEnum(16, "断面"),
        videoEnum(39, "视频监测点"),
        soilEnum(51, "土壤监测点"),


        riskEnum(50, "风险监测点"),
        ElectricFacilityEnum(53, "用电监测点"),
        SecurityLeakageMonitor(54, "安全泄露监测点"),
        SecurityCombustibleMonitor(55, "可燃易爆气体监测点"),
        SecurityToxicMonitor(56, "有毒有害气体监测点"),
        ProductionSiteEnum(57, "生产场所"),
        FingerPrintDatabaseEnum(58, "指纹库"),
        StorageTankAreaEnum(30, "企业贮罐"),
        StorageRoomAreaEnum(31, "企业仓库");

        MonitorPointTypeEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }

        public static MonitorPointTypeEnum getCodeByInt(Integer code) {
            for (MonitorPointTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    //获取指定的枚举
                    return transactType;
                }
            }
            return null;
        }

        public static int getEnumIndex(Integer code) {
            for (MonitorPointTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    //获取指定的枚举
                    return transactType.ordinal();
                }
            }
            return 0;
        }

        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(Integer Code) {
            for (MonitorPointTypeEnum enums : MonitorPointTypeEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.name;
                }
            }
            return "";
        }


    }


    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 监测点类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum MonitorPointStatusEnum {

        StartEnum(1, "启用"),

        StopEnum(0, "停用");

        MonitorPointStatusEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 监测点类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum TransferAreaTypeEnum {

        outsideEnum("1", "省外"),

        insideEnum("2", "省内");

        TransferAreaTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 移动端类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AppTypeEnum {

        HBEnum(1, "智慧环保"),
        AQEnum(2, "智慧安全"),
        YTHEnum(3, "一体化");

        AppTypeEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 水质类别枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum WaterLevelEnum {

        OneEnum(1, "Ⅰ", "Ⅰ类"),
        TwoEnum(2, "Ⅱ", "Ⅱ类"),
        ThreeEnum(3, "Ⅲ", "Ⅲ类"),
        FourEnum(4, "Ⅳ", "Ⅳ类"),
        FiveEnum(5, "Ⅴ", "Ⅴ类"),
        LFiveEnum(6, "劣Ⅴ", "劣Ⅴ类");

        WaterLevelEnum(int index, String code, String name) {
            this.index = index;
            this.code = code;
            this.name = name;
        }

        private final int index;
        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public String getCode() {
            return code;
        }

        public static WaterLevelEnum getObjectByCode(String Code) {
            for (WaterLevelEnum enums : WaterLevelEnum.values()) {
                if (enums.code.equals(Code)) {
                    return enums;
                }
            }
            return null;
        }

        public static WaterLevelEnum getObjectByName(String name) {
            for (WaterLevelEnum enums : WaterLevelEnum.values()) {
                if (enums.name.equals(name)) {
                    return enums;
                }
            }
            return null;
        }

        public static WaterLevelEnum getObjectByIndex(Integer index) {
            for (WaterLevelEnum enums : WaterLevelEnum.values()) {
                if (enums.index == index) {
                    return enums;
                }
            }
            return null;
        }

    }


    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 土壤评价等级枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum SoilEvaluateLevelEnum {

        NormalLevelEnum(0, "正常"),
        OneLevelEnum(1, "轻微"),
        TwoLevelEnum(2, "轻度"),
        ThreeLevelEnum(3, "中度"),
        FourLevelEnum(4, "重度");

        SoilEvaluateLevelEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }

        public static String getNameByCode(Integer Code) {
            for (SoilEvaluateLevelEnum enums : SoilEvaluateLevelEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.name;
                }
            }
            return "";
        }
    }


    /**
     * @Description:
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/1 16:46
     */
    public enum ModuleTypeEnum {

        ProblemEnum("1", "巡查问题"),
        AlarmEnum("2", "报警工单"),
        DevEnum("3", "运维工单"),
        TBEnum("4", "突变工单"),
        RCFPEnum("5", "日常工单");

        ModuleTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static String getNameByCode(String Code) {
            for (ModuleTypeEnum enums : ModuleTypeEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }
    }


    /**
     * @Description: 业务模块编码
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/1 16:46
     */
    public enum ModuleItemCodeEnum {

        AlarmFPEnum("alarm_fp_task", "报警工单分派"),
        DevFPEnum("dev_fk_task", "运维工单分派"),
        TBEnum("tb_fp_task", "突变工单分派"),
        RCFPEnum("rc_fp_task", "日常工单分派");
        ModuleItemCodeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
        private final String code;
        private final String name;
        public String getName() {
            return name;
        }
        public String getCode() {
            return code;
        }

    }


    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 极光消息类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum JGMessageTypeStatusEnum {
        HiddenDangerSuperviseEnum(1, "隐患督办");

        JGMessageTypeStatusEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 消息推送类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum PushTypeEnum {

        PCEnum("1", "平台端"),
        PhoneEnum("2", "手机端"),
        MessageEnum("3", "短信端"),
        WeChartEnum("4", "微信推送");

        PushTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }


    /**
     * @author: lip
     * @date: 2019/10/15 0015 上午 10:29
     * @Description: 数据库表标记枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum TableFlagEnum {

        pollutioninfo("pollutioninfo", "T_BAS_Pollution", false),//污染源
        wateroutputinfo("wateroutputinfo", "T_BAS_WaterOutputInfo", false),//废水排口
        storagetankareainfo("storagetankareainfo", "T_AQ_StorageTankAreaInfo", false),//储罐区
        storagetankinfo("storagetankinfo", "T_AQ_StorageTankInfo", false),//储罐
        gasoutputinfo("gasoutputinfo", "T_BAS_GasOutputInfo", false),//废气排口
        unormonitorpoint("unormonitorpoint", "T_BAS_UnorganizedMonitorPointInfo", false),//无组织排口
        wateroutputpollutantset("wateroutputpollutantset", "T_BAS_WaterOutPutPollutantSet", false),//废水污染物
        othermonitorpointpollutantset("othermonitorpointpollutantset", "T_BAS_OtherMonitorPointPollutantSet", false),//其他监测点污染物
        othermonitorpoint("othermonitorpoint", "T_BAS_OtherMonitorPoint", false),//其他监测点
        gasoutputpollutantset("gasoutputpollutantset", "T_BAS_GasOutPutPollutantSet", false),//废气污染物
        airstationpollutantset("airstationpollutantset", "T_BAS_AirStationPollutantSet", false),//空气站污染物
        particularpollutants("particularpollutants", "T_BAS_ParticularPollutants", false),//基础表-排放口特征污染物库
        emergencySupplies("emergencySupplies", "T_BAS_EmergencySupplies", false),//基础表-应急物资
        devicestatus("devicestatus", "T_BAS_DeviceStatus", false),//基础表-在线监测设备状态表
        outputstopproductioninfo("outputstopproductioninfo", "T_BAS_OutPutStopProductionInfo", false),//基础表-排口停产信息表
        keyMonitorPollutant("keyMonitorPollutant", "T_BAS_KeyMonitorPollutant", false),//基础表-重点监测污染物
        taskDisposeManagement("taskDisposeManagement", "T_BAS_TaskDisposeManagement", false),//基础表-任务管理
        traceSourceEventInfo("traceSourceEventInfo", "T_BAS_TraceSourceEventInfo", false),//基础表-溯源事件信息
        transportunit("transportunit", "T_BAS_TransportUnit", true),//基础表-运输单位基本信息
        majorhazardsources("majorhazardsources", "T_AQ_MajorHazardSources", false),//基础表-重大危险源
        enthazardoustechnology("enthazardoustechnology", "T_AQ_EntHazardousTechnology", false),//基础表-企业危险工艺表
        chemicalstore("chemicalstore", "T_AQ_ChemicalStore", false),//基础表-危化品库
        userinfo("userinfo", "Base_UserInfo", false),//基础表-用户信息
        storagetankset("storagetankset", "T_AQ_StorageTankSet", false),//储罐污染物设置表
        waterstationinfo("waterstationinfo", "T_BAS_WaterStationInfo", false),//水质监测点表
        entkeymonitorchemicals("entkeymonitorchemicals", "T_AQ_EntMonitorChemicals", false),//基础表-重点监控危化品
        producplace("producplace", "T_AQ_ProducPlace", false),//基础表-生产场所
        riskarea("riskarea", "T_AQ_RiskArea", false),//基础表-安全风险区域

        keytechnologyarea("keytechnologyarea", "T_AQ_KeyTechnologyArea", false),//基础表-重点危险工艺区域

        useelectricfacility("useelectricfacility", "T_BAS_UseElectricFacility", false),//基础表-用电设施信息
        useelectricfacilitymonitorpoint("useelectricfacilitymonitorpoint", "T_BAS_UseElectricFacilityMonitorPoint", false),//基础表-用电设施监测点信息
        riskareamonitorpoint("riskareamonitorpoint", "T_AQ_RiskAreaMonitorPoint", false),//基础表-安全风险区域监测点
        riskareamonitorpointpollutantset("riskareamonitorpointpollutantset", "T_AQ_RiskAreaMonitorPointPollutantSet", false),//基础表-安全风险区域监测点set
        productdevicepollutantset("productdevicepollutantset", "T_AQ_ProductDevicePollutantSet", false),//基础表-安全生产场所set
        hazardsourceproductdevice("hazardsourceproductdevice", "T_AQ_HazardSourceProductDevice", false),//基础表-危险源生产装置
        transferlist("transferlist", "T_WXFW_TransferList", false),//基础表-危废转移联单
        groundwaterpointinfo("groundwaterpointinfo", "T_BAS_GroundWaterPointInfo", false),//基础表-地下水

        SecurityLicenseType("securitylicensetype", "PUB_CODE_SecurityLicenseType", true),//码表-证书类型


        pollutantfactor("pollutantfactor", "PUB_CODE_PollutantFactor", true),//污染物码表
        alarmLevel("alarmlevel", "PUB_CODE_AlarmLevel", true),//报警等级码表
        aqalarmLevel("aqalarmlevel", "PUB_CODE_AQAlarmLevel", true),//安全报警等级码表
        registrationtype("registrationtype", "PUB_CODE_RegistrationType", true),//码表-登记注册类型
        industrytype("industrytype", "PUB_CODE_IndustryType", true),//码表-行业类型
        basin("basin", "PUB_CODE_Basin", true),//码表-流域
        region("region", "PUB_CODE_Region", true),//码表-行政区划
        envorganization("envorganization", "PUB_CODE_EnvOrganization", true),//码表-环评审批单位
        licencecondition("licencecondition", "PUB_CODE_LicenceCondition", true),//码表-许可证状态
        issueunit("issueunit", "PUB_CODE_IssueUnit", true),//码表-发证单位
        casetype("casetype", "PUB_CODE_CaseType", true),//码表-案件类型
        illegaltype("illegaltype", "PUB_CODE_IllegalType", true),//码表-违法类型
        entstate("entstate", "PUB_CODE_EntState", true),//码表-企业状态
        certificatetype("certificatetype", "PUB_CODE_CertificateType", true),//码表-证件类型
        entscale("entscale", "PUB_CODE_EntScale", true),//码表-企业规模
        enttype("enttype", "PUB_CODE_EntType", true),//码表-企业类型
        pollutionclass("pollutionclass", "PUB_CODE_PollutionClass", true),//码表-污染源类别
        keyindustrytype("keyindustrytype", "PUB_CODE_KeyIndustryType", true),//码表-重点行业类型
        draindirection("draindirection", "PUB_CODE_DrainDirection", true),//码表-排水去向
        standardlevel("standardlevel", "PUB_CODE_Standardlevel", true),//码表-标准化等级
        tasksource("tasksource", "PUB_CODE_TaskSource", true),//码表-任务来源
        risklevel("risklevel", "PUB_CODE_RiskLevel", true),//码表-风险等级
        risktype("risktype", "PUB_CODE_RiskType", true),//码表-风险类别
        hiddendangerlevel("hiddendangerlevel", "PUB_CODE_HiddenDangerLevel", true),//码表-隐患级别
        hiddendangersource("hiddendangersource", "PUB_CODE_HiddenDangerSource", true),//码表-隐患来源
        hiddendangertype("hiddendangertype", "PUB_CODE_HiddenDangerType", true),//码表-隐患类别
        rectificationlevel("rectificationlevel", "PUB_CODE_RectificationLevel", true),//码表-整改紧急程度
        securitystandardlevel("securitystandardlevel", "PUB_CODE_SecurityStandardLevel", true),//码表-安全标准化等级
        tasktype("tasktype", "PUB_CODE_TaskType", true),//码表-任务类型
        enerlvl("enerlvl", "PUB_CODE_EnerLvl", true),//码表-紧急程度
        petitionkind("petitionkind", "PUB_CODE_PetitionKind", true),//码表-举报方式
        petitiontype("petitiontype", "PUB_CODE_PetitionType", true),//码表-投诉类型
        wasteoperationmodes("wasteoperationmodes", "PUB_CODE_WasteOperationModes", true),//码表-危险废物经营方式
        disposalmeethod("disposalmeethod", "PUB_CODE_DisposalMethod", true),//码表-废物处置方式
        wastematerial("wastematerial", "PUB_CODE_WasteMaterial", true),//码表-危险废物
        materialtype("materialtype", "PUB_CODE_MaterialType", true),//码表-原辅料类型
        firerisktype("firerisktype", "PUB_CODE_FireRiskType", true),//码表-火灾危险类别
        occupathazardlevel("occupathazardlevel", "PUB_CODE_OccupatHazardLevel", true),//码表-职业危害等级
        chemicalhazardtype("chemicalhazardtype", "PUB_CODE_ChemicalHazardType", true),//码表-化学品危险性类别
        workplacelevel("workplacelevel", "PUB_CODE_WorkPlaceLevel", true),//码表-场所等级
        radionuclide("radionuclide", "PUB_CODE_Radionuclide", true),//码表-核素
        activitytype("activitytype", "PUB_CODE_ActivityType", true),//码表-活动种类
        raydevicetype("raydevicetype", "PUB_CODE_RaydeviceType", true),//码表-射线装置类别
        radiontype("radiontype", "PUB_CODE_RadionType", true),//码表-放射源类别
        transferareatype("transferareatype", "PUB_CODE_TransferAreaType", true),//码表-转移区域类型
        managementmodel("managementmodel", "PUB_CODE_ManagementModel", true),//码表-经营方式
        safemanageruletype("safemanageruletype", "PUB_CODE_SafeManageRuleType", true),//码表-经营方式
        pollutantsmell("pollutantsmell", "PUB_CODE_PollutantSmell", true),//码表-气味
        projectchecknature("projectchecknature", "PUB_CODE_ProjectCheckNature", true),//码表-项目验收性质
        politicalaffiliation("politicalaffiliation", "PUB_CODE_PoliticalAffiliation", true),//码表-政治面貌
        chemicaltype("chemicaltype", "PUB_CODE_ChemicalType", true),//码表-危化品类型
        chemicalusetype("chemicalusetype", "PUB_CODE_ChemicalUseType", true),//码表-危化品使用类型
        refractorylevel("refractorylevel", "PUB_CODE_RefractoryLevel", true),//码表-耐火等级
        airfunarealevel("airfunarealevel", "PUB_CODE_AirFunAreaLevel", true),//码表-所处环境功能区
        storagetankshape("storagetankshape", "PUB_CODE_StorageTankShape", true),//码表-贮罐形状
        storagetankform("storagetankform", "PUB_CODE_StorageTankForm", true),//码表-贮罐形式
        majorhazardlevel("majorhazardlevel", "PUB_CODE_MajorHazardLevel", true),//码表-重大危险源级别
        productiontype("productiontype", "PUB_CODE_ProductionType", true),//码表-生产活动经营种类
        placeproperty("placeproperty", "PUB_CODE_PlaceProperty", true),//码表-生产存储产所产权
        accidenttype("accidenttype", "PUB_CODE_AccidentType", true),//码表-事故类型
        accidentlevel("accidentlevel", "PUB_CODE_AccidentLevel", true),//码表-事故等级
        hazardcategory("hazardcategory", "PUB_CODE_HazardCategory", true),//码表-危险性类别
        intrusionmode("intrusionmode", "PUB_CODE_IntrusionMode", true),//码表-侵入方式
        firedangergrade("firedangergrade", "PUB_CODE_FireDangerGrade", true),//码表-火险等级
        controlleve("controlleve", "PUB_CODE_ControlLeve", true),//码表-测点控制级别
        soilpointtype("soilpointtype", "PUB_CODE_SoilPointType", true),//码表-土壤监测点位类型
        monitorpointtype("monitorpointtype", "PUB_CODE_MonitorPointType", true),//码表-监测点类型
        installform("installform", "PUB_CODE_InstallForm", true),//码表-安装形式
        stopproductiontype("stopproductiontype", "PUB_CODE_StopProductionType", true),//码表-停产类型
        devicetype("devicetype", "PUB_CODE_DeviceType", true),//码表-生产装置类型
        technologystore("technologystore", "T_AQ_TechnologyStore", true),//码表-工艺
        storetype("storetype", "PUB_CODE_StoreType", true),//码表-停产类型
        vedioalarmtype("vedioalarmtype", "PUB_CODE_VedioAlarmType", true),//码表-停产类型
        outputattribute("outputattribute", "PUB_CODE_OutPutAttribute", true),//码表-排口属性
        problemsource("problemsource", "PUB_CODE_ProblemSource", true),//码表-问题来源
        standardtype("standardtype", "PUB_CODE_StandardType", true),//码表-标准类型
        emergencyCaseType("emergencyCaseType", "PUB_CODE_EmergencyCaseType", true),//码表-案件类型
        emergencyAccidentType("emergencyAccidentType", "PUB_CODE_EmergencyAccidentType", true),//码表-事故等级
        knowledgeType("knowledgeType", "pub_code_KnowledgeType", true),//码表-知识分类
        trainType("trainType", "PUB_CODE_TrainType", true),//码表-培训类型
        inOutMaterialMode("inOutMaterialMode", "PUB_CODE_InOutMaterialMode", true),//码表-进出料方式
        materialStatus("materialStatus", "PUB_CODE_MaterialStatus", true),//码表-物质状态
        loadAndUnloadMode("loadAndUnloadMode", "PUB_CODE_LoadAndUnloadMode", true),//码表-装卸方式
        commonproblemtype("commonproblemtype", "PUB_CODE_CommonProblemType", true),//码表-常见问题类型
        evaluationlevel("evaluationlevel", "PUB_CODE_EvaluationLevel", true),//码表-评价级别
        devopsunittype("devopsunittype", "PUB_CODE_DevOpsUnitType", true),//码表-运维单位类型
        aircontrollevel("aircontrollevel", "PUB_CODE_AirControlLevel", true),//码表-空气控制级别
        waterstationcontrollevel("waterstationcontrollevel", "PUB_CODE_WaterStationControlLevel", true),//码表-水质控制级别
        qualificationtype("qualificationtype", "PUB_CODE_QualificationType", true),//码表-资质类型
        devopscontent("devopscontent", "PUB_CODE_DevOpsContent", true),//码表-运维内容（报备类型）
        entenvcredit("entenvcredit", "PUB_CODE_EntEnvCredit", true),//企业信用评价码表
        pollutionlabel("pollutionlabel", "PUB_CODE_PollutionLabel", true);//码表-企业标签


        /*emergencymaterialtype("emergencymaterialtype", "PUB_CODE_EmergencyMaterialType", true),//码表-应急物资类型
        emergencyunit("emergencyunit", "PUB_CODE_Unit", true),//码表-应急单位
        nationalitytype("nationalitytype", "PUB_CODE_NationalityType", true),//码表-民族类型
        emergencyequipmenttype("emergencyequipmenttype", "PUB_CODE_EmergencyEquipmentType", true),//码表-应急装备类型
        emergencyexperttype("emergencyexperttype", "PUB_CODE_EmergencyExpertType", true),//码表-专家类型
        emergencyexpertlevel("emergencyexpertlevel", "PUB_CODE_EmergencyExpertLevel", true),//码表-专家级别
        userdutytype("userdutytype", "PUB_CODE_UserDutyType", true),//码表-人员职责类型
        reserveplanclass("reserveplanclass", "PUB_CODE_ReservePlanClass", true),//码表-预案类别
        reserveplantype("reserveplantype", "PUB_CODE_ReservePlanType", true),//码表-预案分类
        sensitivetype("sensitivetype", "PUB_CODE_SensitiveType", true),//码表-环境要素类型
        sensitivepointtype("sensitivepointtype", "PUB_CODE_SensitivePointType", true),//码表-应急敏感点类型
        emergencyeeventtype("emergencyeeventtype", "PUB_CODE_EventType", true),//码表-应急案例类型
        educationlevel("educationlevel", "PUB_CODE_EducationLevel", true);//码表-教育级别*/


        TableFlagEnum(String tableMark, String tableName, boolean Cache) {
            this.tableMark = tableMark;
            this.tableName = tableName;
            this.Cache = Cache;
        }

        private final String tableMark;
        private final String tableName;
        private final boolean Cache;

        public String getTableMark() {
            return tableMark;
        }

        public String getTableName() {
            return tableName;
        }

        public boolean isCache() {
            return Cache;
        }

        public static String getNameByMark(String tableMark) {
            for (TableFlagEnum enums : TableFlagEnum.values()) {
                if (enums.tableMark.equals(tableMark)) {
                    return enums.tableName;
                }
            }
            return "";
        }

        public static Boolean isCached(String tableMark) {
            for (TableFlagEnum enums : TableFlagEnum.values()) {
                if (enums.tableMark.equals(tableMark)) {
                    return enums.Cache;
                }
            }
            return false;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 溯源配置属性枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum TraceSourceConfigEnum {

        MonitorPointEnum("monitorpointid", "监测点主键"),

        PollutantEnum("pollutantcode", "污染物编码");


        TraceSourceConfigEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

    }


    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 报警类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AlarmTypeEnum {
        UpperAlarmEnum("1", "上限报警"),
        LowerAlarmEnum("2", "下限报警"),
        BetweenAlarmEnum("3", "区间报警");

        AlarmTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }


        public static AlarmTypeEnum getCodeByString(String code) {
            for (AlarmTypeEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (AlarmTypeEnum enums : AlarmTypeEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 异常类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum ExceptionTypeEnum {
        ZeroExceptionEnum("1", "零值异常"),
        ContinuousExceptionEnum("2", "连续恒值"),
        OverExceptionEnum("3", "超限异常"),
        ParamExceptionEnum("4", "动态管控参数异常"),
        HandPickingExceptionEnum("5", "人工挑选异常"),
        StatusExceptionEnum("6", "动态管控状态异常"),
        NoFlowExceptionEnum("7", "无流量异常");

        ExceptionTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static ExceptionTypeEnum getCodeByString(String code) {
            for (ExceptionTypeEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (ExceptionTypeEnum enums : ExceptionTypeEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 在线状态枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum OnlineStatusEnum {
        OfflineStatusEnum("0", "离线", "OfflineStatus"),
        NormalStatusEnum("1", "正常", "NormalStatus"),
        OverStatusEnum("2", "超标", "OverStatus"),
        ExceptionStatusEnum("3", "异常", "ExceptionStatus"),
        StopStatusEnum("4", "停产", "StopStatus");//停产  属于自定义状态

        OnlineStatusEnum(String code, String name, String othername) {
            this.code = code;
            this.name = name;
            this.othername = othername;
        }

        private final String code;
        private final String name;
        private final String othername;

        public String getOthername() {
            return othername;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static OnlineStatusEnum getCodeByString(String code) {
            for (OnlineStatusEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        public static String getNameByCode(String code) {
            for (OnlineStatusEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.name;
                }
            }
            return null;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 气象污染物枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum WeatherPollutionEnum {
        WindSpeedEnum("a01007", "风速"),
        WindDirectionEnum("a01008", "风向"),
        TemperatureEnum("a01001", "气温"),
        PressureEnum("a01006", "大气压"),
        HumidityEnum("a01002", "相对湿度"),
        RainfallEnum("a02005", "降水量");

        WeatherPollutionEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static WeatherPollutionEnum getCodeByString(String code) {
            for (WeatherPollutionEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (WeatherPollutionEnum enums : WeatherPollutionEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }

        public static List<String> getAllCodes() {
            List<String> codes = new ArrayList<>();
            for (WeatherPollutionEnum enums : WeatherPollutionEnum.values()) {
                codes.add(enums.getCode());
            }
            return codes;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 气象污染物枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum GasWaterPollutionEnum {
        YCEnum("01", "烟尘"),
        SO2Enum("02", "SO2"),
        NOXEnum("03", "NOx"),
        CODEnum("011", "化学需氧量"),
        ADEnum("060", "氨氮");

        GasWaterPollutionEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static GasWaterPollutionEnum getCodeByString(String code) {
            for (GasWaterPollutionEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (GasWaterPollutionEnum enums : GasWaterPollutionEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }


    }


    public enum O3PollutionEnum {

        TemperatureEnum("a01001", "温度"),
        O3Enum("a05024", "O3"),
        HumidityEnum("a01002", "湿度"),
        NO2Enum("a21004", "NO2");

        O3PollutionEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static String getNameByCode(String code) {
            for (O3PollutionEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.name;
                }
            }
            return null;
        }

        public static List<String> getAllCodes() {
            List<String> codes = new ArrayList<>();
            for (O3PollutionEnum enums : O3PollutionEnum.values()) {
                codes.add(enums.getCode());
            }
            return codes;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/11/19 0019 上午 11:32
     * @Description: 储罐污染物code
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum storageTankCodeEnum {
        levelCode("a001"),//液位
        temperatureCode("a01012"),//温度
        pressureCode("a01006"),//压力
        storageCode("a002");//实时存储

        storageTankCodeEnum(String code) {
            this.code = code;
        }

        private final String code;

        public String getCode() {
            return code;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/11/19 0019 上午 11:32
     * @Description: 工况污染物code
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum useElectricFacilityEnum {
        electricCode("ele1001"),//电流
        voltageCode("ele1002"),//电压
        powerUsageCode("ele1003"),//用电量
        powerCode("ele1004");//功率

        useElectricFacilityEnum(String code) {
            this.code = code;
        }

        private final String code;

        public String getCode() {
            return code;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 气象污染物枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum StinkPollutionEnum {
        OUEnum("06", "OU"),
        H2SEnum("05", "硫化氢"),
        NHEnum("10", "氨"),
        VOCEnum("m005", "VOC");

        StinkPollutionEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static StinkPollutionEnum getCodeByString(String code) {
            for (StinkPollutionEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (StinkPollutionEnum enums : StinkPollutionEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 监控预警提醒类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum RemindTypeEnum {
        ConcentrationChangeEnum(1, "浓度突变"),
        FlowChangeEnum(2, "排放量突变"),
        EarlyAlarmEnum(3, "超阈值预警"),
        ExceptionAlarmEnum(4, "异常报警"),

        ZeroExceptionAlarmEnum(41, "零值异常"),
        ContinuousExceptionAlarmEnum(42, "恒值异常"),

        OverAlarmEnum(5, "超标报警"),
        FlowPermitEnum(6, "排放许可预警"),
        WaterNoFlowEnum(8, "废水无流量报警"),
        VideoOverEnum(9, "视频报警"),
        StorageTankDataChangeEnum(7, "数据突变"),       //储罐的数据突变报警

        EarlyAlarmEnum2(23, "超阈值预警"),//model表应用类型 限分级预警新菜单统计用 例：废水数据超限报警2
        ExceptionAlarmEnum2(24, "异常报警"),//model表应用类型
        OverAlarmEnum2(25, "超标报警"),//model表应用类型

        AlarmCountListEnum(26, "报警统计清单"),//2.0版本 监测管理报警统计 用到
        AlarmWorkOrderEnum(27, "报警处置工单"),//2.0版本 监测管理报警工单 用到
        DevOpsWorkOrderEnum(28, "异常处置工单"),//2.0版本 监测管理异常工单统计 用到
        TBWorkOrderEnum(29, "突变处置工单");//2.0版本 监测管理突变工单统计 用到


        RemindTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static RemindTypeEnum getObjectByCode(Integer code) {
            for (RemindTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: 监控预警提醒类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum RemindTypeCodeEnum {
        ConcentrationChangeEnum("Online_ConcentrationChange", 1),
        FlowChangeEnum("Online_FlowChange", 2),
        EarlyWarnEnum("Online_EarlyWarn", 3),
        ExceptionEnum("Online_Exception", 4),
        OverLimitEnum("Online_OverLimit", 5),
        OverStandardEnum("Online_OverStandard", 5);
//        FlowPermitEnum(6, "排放许可预警");

        RemindTypeCodeEnum(String name, Integer code) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static Integer getCodeByString(String name) {
            for (RemindTypeCodeEnum transactType : values()) {
                if (transactType.getName().equals(name)) {
                    return transactType.getCode();
                }
            }
            return null;
        }

        /**
         * @author: lip
         * @date: 2019/11/4 0004 下午 4:50
         * @Description: 通过名称获取编码
         * @updateUser:
         * @updateDate:
         * @updateDescription:
         * @param:
         * @return:
         */
        public static Integer getCodeByName(String name) {
            for (RemindTypeCodeEnum enums : RemindTypeCodeEnum.values()) {
                if (enums.getName().equals(name)) {
                    return enums.code;
                }
            }
            return null;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: mongodb数据类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum MongodbDataTypeEnum {
        RealTimeDataEnum(1, "RealTimeData", "实时数据"),
        MinuteDataEnum(2, "MinuteData", "分钟数据"),
        HourDataEnum(3, "HourData", "小时数据"),
        DayDataEnum(4, "DayData", "日数据");

        MongodbDataTypeEnum(Integer code, String name, String text) {
            this.code = code;
            this.name = name;
            this.text = text;
        }

        private final Integer code;
        private final String name;
        private final String text;

        public String getText() {
            return text;
        }

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static MongodbDataTypeEnum getCodeByString(Integer code) {
            for (MongodbDataTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }

        public static MongodbDataTypeEnum getNameByString(String name) {
            for (MongodbDataTypeEnum transactType : values()) {
                if (transactType.name.equals(name)) {
                    return transactType;
                }
            }
            return null;
        }

        public static String getTextByName(String name) {
            for (MongodbDataTypeEnum transactType : values()) {
                if (transactType.name.equals(name)) {
                    return transactType.text;
                }
            }
            return "";
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 上午 9:04
     * @Description: socket类型枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum SocketTypeEnum {
        APPAlarmTaskDisposeEnum("e60749f2-e4a4-4f31-82cf-9ee2f65bb2ab", "rwtx", "", "任务提醒"),
        APPOverAlarmTaskDisposeEnum("9028f2a4-eb52-4152-8a50-13edbe64722a", "bjrwgl", "", "报警任务提醒"),
        APPComplaintTaskDisposeEnum("463d2318-5568-479d-a754-857777c0c31b", "tsrwgl", "", "投诉任务提醒"),
        APPDailyTaskDisposeEnum("bfb61839-c06c-40a7-8435-0589a6623853", "rcrwgl", "", "日常任务提醒"),
        APPDevOpsTaskDisposeEnum("7d62e45a-86f9-47b4-b428-f3e3f180c36e", "ywrwgl", "", "运维任务提醒"),
        APPAlarmRemindDisposeEnum("c40db980-ef0d-473d-a495-0634ff110b21", "jctx", "", "监测提醒"),
        alarmDataSearchEnum("ff8f077e-6d8d-4a24-b9dc-faf7b4f885a1", "", "alarmDataSearch", "信息查询"),
        GradeAlarmEnum("2f04226a-c86f-4ed0-b121-3a5b94dc9e97", "", "GradeAlarmMethod", "分级预警"),
        //安全储罐的分级预警
        AQGradeAlarmEnum("f74ca17f-0e53-4c7d-a87f-d0002a9916ec", "EarlyWarning", "AQgradeAlarmMethod", "分级预警"),
        SafetyTaskDisposalEnum("31645f00-7520-4d22-b5c4-55e015f2aacc", "SafetyTaskDisposal", "SafetyTaskDisposalMethod", "安全任务处置"),

        EnvSupervisionEnum("14a312db-c6c3-4d37-a36c-9da6ea4b73ab", "", "EnvSupervisionMethod", "环境监管"),
        AlarmTaskManagementEnum("ba3a998d-8130-4523-ae21-7dbbba2c4264", "alarmTaskManagement", "EnvSupervisionMethod", "报警任务处置管理"),
        GeneralTaskManagementEnum("3c3081b8-0a8b-4db5-bccb-bcefe39717a8", "TaskDisposeManagement", "EnvSupervisionMethod", "日常任务管理"),
        HomePageAlarmDataEnum("", "", "AlarmDataMethod", "首页报警数据消息"),
        AQHomePageAlarmDataEnum("", "", "AQAlarmDataMethod", "安全首页报警数据消息"),
        HomePageTaskDataEnum("", "", "TaskDataMethod", "首页任务消息"),
        HomePageAlarmTaskEnum("", "", "AlarmTaskMethod", "首页报警任务消息"),
        HomePageEmissionControlEnum("", "", "EmissionControlMethod", "首页排口排放控制消息"),


        EntHomePageRemindEnum("", "", "FeedbackRemindMethod", "企业端首页反馈提醒消息"),
        EntHomePageAlarmRemindEnum("", "", "EntAlarmRemindMethod", "企业端首页报警提醒消息"),

        ManagementHomePageRemindEnum("", "", "ManagementRemindMethod", "管委会端首页提醒消息"),


        HomePageNoticeEnum("", "", "EmissionControlMethod", "首页通知消息"),
        ForecastDataEnum("", "", "ForecastDataMethod", "预报数据消息"),
        AQHiddenTaskEnum("", "", "HiddenTaskMethod", "安全隐患任务"),
        AQVideoAlarmEnum("", "", "AQVideoAlarmMethod", "安全视频报警"),
        HomePageMonitorTaskEnum("", "", "HomePageMonitorTaskMethod", "首页监测点报警任务"),
        TSTaskManagementEnum("fbf769da-efa6-471e-9f85-504da06eff1c", "complaintTaskManagement", "EnvSupervisionMethod", "投诉任务处置管理");

        SocketTypeEnum(String menuid, String menucode, String socketMethod, String des) {
            this.menuid = menuid;
            this.menucode = menucode;
            this.socketMethod = socketMethod;
            this.des = des;
        }

        private final String menuid;
        private final String menucode;
        private final String socketMethod;
        private final String des;

        public String getMenucode() {
            return menucode;
        }

        public String getMenuid() {
            return menuid;
        }

        public String getSocketMethod() {
            return socketMethod;
        }

        public String getDes() {
            return des;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/19 0019 下午 4:17
     * @Description: 任务类型枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum TaskTypeEnum {
        AlarmTaskEnum(1, "报警任务"),
        ComplaintEnum(2, "投诉任务"),
        DailyEnum(3, "日常任务"),
        TraceSourceEvent(4, "溯源事件"),
        DevOpsTaskEnum(5, "运维任务"),
        MonitorTaskEnum(6, "安全监测任务"),//生产场所、储罐、有毒有害
        SupervisoryControlEnum(7, "监控任务"),
        SecurityDevOpsTaskEnum(8, "安全运维任务"),
        ChangeAlarmTaskEnum(9, "突变任务"),
        CheckProblemExpoundEnum(10, "检查问题记录");

        TaskTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static TaskTypeEnum getCodeByInteger(Integer code) {
            for (TaskTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }
    }


    /**
     * @Description: 周边类型
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/22 11:24
     */
    public enum PeripheryTypeEnum {
        FSEnum(1, "废水排口"),
        FQEnum(2, "废气排口"),
        YSEnum(37, "雨水排口"),
        ECEnum(9, "恶臭点位"),
        VOCEnum(11, "VOC点位"),
        AIREnum(5, "空气站"),
        WZEnum(33, "微型站"),
        QXEnum(52, "气象站"),
        TSDEnum(99, "周边投诉点"),
        SZEnum(6, "水站");

        PeripheryTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static PeripheryTypeEnum getObjectByCode(Integer code) {
            for (PeripheryTypeEnum transactType : values()) {
                if (transactType.code == code) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/6 0006 上午 11:12
     * @Description: 投诉任务处置状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum ComplaintTaskEnum {
        UnassignedTaskEnum(1, "待分派"),
        UndisposedEnum(2, "待处理"),
        HandleEnum(3, "处理中"),
        CompletedEnum(4, "已完成");

        ComplaintTaskEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static ComplaintTaskEnum getCodeByInteger(Integer code) {
            for (ComplaintTaskEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: lip
     * @date: 2020/1/2 0002 上午 8:44
     * @Description: 隐患任务状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum HiddenDangerTaskEnum {
        addTaskEnum(1, "新建"),
        sendTaskEnum(2, "已下达"),
        HandleEnum(3, "进行中"),
        CompletedEnum(4, "已完成"),
        TransferEnum(5, "已转办");

        HiddenDangerTaskEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static String getNameByCode(Integer code) {
            for (HiddenDangerTaskEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType.name;
                }
            }
            return null;
        }
    }


    /**
     * @author: lip
     * @date: 2020/1/2 0002 上午 8:44
     * @Description: 隐患任务记录状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: 3个驳回主要是在显示流程时
     */
    public enum HiddenDangerTaskRecordEnum {
        addTaskEnum(1, "新建", ""),
        DisposedTaskEnum(2, "待处理", "UndisposedEnum"),
        HandleEnum(3, "处理中", "HandleEnum"),
        TransferEnum(4, "转办", "TransferEnum"),
        rectifiedEnum(5, "待整改", "rectifiedEnum"),
        ReviewEnum(6, "待复查", "ReviewEnum"),
        ExamineEnum(7, "待审核", "ExamineEnum"),
        rectifiedRejectEnum(8, "整改驳回", "rectifiedRejectEnum"),
        ReviewRejectEnum(9, "复查驳回", "ReviewRejectEnum"),
        ExamineRejectEnum(10, "审核驳回", "ExamineRejectEnum"),
        CompletedEnum(11, "已完成", "CompletedEnum");

        HiddenDangerTaskRecordEnum(Integer code, String name, String taskMark) {
            this.code = code;
            this.name = name;
            this.taskMark = taskMark;
        }

        private final Integer code;
        private final String name;
        private final String taskMark;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }


        public static HiddenDangerTaskRecordEnum getEnumByCode(Integer code) {
            for (HiddenDangerTaskRecordEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }


        public static String getNameByCode(Integer code) {
            for (HiddenDangerTaskRecordEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType.name;
                }
            }
            return null;
        }

        public static String getTaskMarkByCode(Integer code) {
            for (HiddenDangerTaskRecordEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType.taskMark;
                }
            }
            return null;
        }

    }


    /**
     * @author: chengzq
     * @date: 2019/8/22 0022 下午 4:38
     * @Description: 日常任务状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum DaliyTaskEnum {
        UnassignedTaskEnum(0, "待分派"),
        UndisposedEnum(1, "待处理"),
        HandleEnum(2, "处理中"),
        //        NeglectEnum(3, "已忽略"),
        CompletedEnum(4, "已完成");

        DaliyTaskEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static DaliyTaskEnum getCodeByInteger(Integer code) {
            for (DaliyTaskEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 10:06
     * @Description: 报警任务处置状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AlarmTaskDisposalScheduleEnum {
        UnassignedTaskEnum(0, "待分派"),
        UndisposedEnum(1, "待处理"),
        HandleEnum(2, "处理中"),
        NeglectEnum(3, "已忽略"),
        CompletedEnum(4, "已完成"),
        ConfirmEndEnum(5, "待审核");

        AlarmTaskDisposalScheduleEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static AlarmTaskDisposalScheduleEnum getCodeByInteger(Integer code) {
            for (AlarmTaskDisposalScheduleEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }

        public static String getNameByCode(Integer Code) {
            for (AlarmTaskDisposalScheduleEnum enums : AlarmTaskDisposalScheduleEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.name;
                }
            }
            return "";
        }
    }

    /**
     * @author: lip
     * @date: 2020/1/8 0008 下午 4:05
     * @Description: 菜单编码枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum MenuCodeEnum {
        AQ_MonitorTaskManageEnum("alarmDispose_app", "报警处置"),
        AQ_WatchTaskManageEnum("controlDispose_app", "监控处置"),
        AQ_MaintenTaskManageEnum("devOpsDispose_app", "运维处置"),
        AQ_HiddenDangerTaskEnum("hiddenDangerTask", "隐患排查任务");

        MenuCodeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static MenuCodeEnum getCodeByInteger(Integer code) {
            for (MenuCodeEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 10:06
     * @Description: 任务流程状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AlarmTaskStatusEnum {
        GenerateTaskEnum(0, "分派任务"),
        UntreatedEnum(1, "待处理"),
        SuperviseEnum(2, "处理中"),
        NeglectTaskEnum(3, "忽略任务"),
        FeedbackEnum(4, "反馈信息"),
        TransferEnum(5, "转办任务"),
        TransferTaskEnum(6, "转办"),//被转办任务的处置人
        CarbonCopyTaskEnum(7, "抄送"),//抄送人
        ReadCopyTaskEnum(8, "抄送已读"),//抄送已读
        ComplateTaskEnum(9, "已完成"),//已完成
        ReviewedEnum(10, "审核"),//审核人
        TaskRepulseEnum(11, "任务退回"),//抄送人打回任务
        ConfirmTaskEnum(12, "确认办结");//抄送人确认办结任务

        AlarmTaskStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static AlarmTaskStatusEnum getCodeByInteger(Integer code) {
            for (AlarmTaskStatusEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }

        public static Integer getCodeByName(String name) {
            for (AlarmTaskStatusEnum transactType : values()) {
                if (transactType.getName().equals(name)) {
                    return transactType.code;
                }
            }
            return null;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/1/25 0025 下午 4:44
     * @Description: 监测点类型报警任务枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum MonitorTaskStatusEnum {
        PendingEnum(1, "待处理"),
        ComplateEnum(2, "已完成");

        MonitorTaskStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static MonitorTaskStatusEnum getCodeByInteger(Integer code) {
            for (MonitorTaskStatusEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/09/02 0002 下午 16:48
     * @Description: 任务状态类型枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum TaskStatusTypeEnum {
        needassignEnum("needassign", "待分派"),
        needfeedbackEnum("needfeedback", "待处理"),
        feedbackingEnum("feedbacking", "处理中"),
        hascloseEnum("hasclose", "已处理");

        TaskStatusTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static TaskStatusTypeEnum getCodeByString(String code) {
            for (TaskStatusTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/9/23 0023 上午 10:29
     * @Description: 投诉事件事件状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum TraceSourceEventStatusEnum {
        PlatformTraceSourceEnum(1, "平台溯源"),
        VoyageTraceSourceEnum(2, "走航溯源"),
        ConsultationEnum(3, "溯源会商"),
        CompleteEnum(4, "完成");

        TraceSourceEventStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static TraceSourceEventStatusEnum getCodeByInteger(Integer code) {
            for (TraceSourceEventStatusEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/2 0002 上午 10:17
     * @Description: 分析报告类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AnalysisReportTypeEnum {

        DayReportEnum(1, "日报"),
        WeekReportEnum(2, "周报"),
        MonthReportEnum(3, "月报"),
        SeasonReportEnum(4, "季报"),
        HalfYearReportEnum(5, "半年报"),
        YearReportEnum(6, "年报"),
        ExpertReportEnum(7, "专报"),
        BriefReportEnum(10, "简报");

        AnalysisReportTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static AnalysisReportTypeEnum getCodeByInteger(Integer code) {
            for (AnalysisReportTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/19 9:56
     * @Description: 环境监管菜单枚举
     * @param:
     * @return:
     */
    public enum EnvSupervisionMenus {

        alarmTaskManagement("alarmTaskManagement", "报警任务处置管理"),
        complaintTaskManagement("complaintTaskManagement", "投诉任务处置管理"),
        TaskDisposeManagement("TaskDisposeManagement", "日常任务管理"),
        operateTaskManagement("operateTaskManagement", "运维任务管理"),
        alarmtaskmanagementsafety("alarmTaskManagementSafety", "安全报警任务管理"),
        operatetaskmanagementsafety("operateTaskManagementSafety", "安全运维任务管理"),
        watchtaskmanage("watchTaskManage", "监控任务管理"),
        mutationTaskManagement("mutationTaskManagement", "突变任务管理");

        EnvSupervisionMenus(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static EnvSupervisionMenus getCodeByString(String code) {
            for (EnvSupervisionMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/1/8 16:01
     * @Description: 安全任务处理菜单枚举
     * @param:
     * @return:
     */
    public enum SecurityTaskMenus {

        securityMonitorTask("alarmTaskManagementSafety", "监测任务管理", TaskTypeEnum.MonitorTaskEnum.code),
        supervisoryControlTask("watchTaskManage", "监控任务管理", TaskTypeEnum.SupervisoryControlEnum.code),
        securityOperateTask("operateTaskManagementSafety", "运维任务管理", TaskTypeEnum.SecurityDevOpsTaskEnum.code);

        SecurityTaskMenus(String code, String name, Integer taskType) {
            this.code = code;
            this.name = name;
            this.taskType = taskType;
        }

        private final String code;
        private final String name;
        private final Integer taskType;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public Integer getTaskType() {
            return taskType;
        }

        public static SecurityTaskMenus getCodeByString(String code) {
            for (SecurityTaskMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: liyc
     * @date: 2019/12/12 0012 10:59
     * @Description: 证书管理菜单枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     **/
    public enum CertificateManagementMenus {
        SafetyProductionLicense("safetyProductionLCP", "安全生产许可证", "1"),
        DangerousChemicalsBusinessLicense("dangerMaterialsManageLCP", "危化品经营许可证", "2"),
        DangerousChemicalsUseLicense("dangerMaterialsUseLCP", "危化品使用许可证", "3"),
        SafetyProductionCertificate("safetyProductionNormalizationLCE", "安全生产标准化证书", "4"),
        SafetyManagementCertificate("safetyManagerCertification", "安全管理人员资格证", "5"),
        SpecialOperationCertificate("specialOperationCertificate", "特种作业操作证", "6"),
        OccupationalHealthCertificate("occupationalHealthCertificate", "职业健康证", "7"),
        PeopleQualificationCertificateSafety("peopleQualificationCertificateSafety", "人员资质证书", "8");//该证书为自定义证书类型，合并5，6，7三种证书为一种

        CertificateManagementMenus(String code, String name, String type) {
            this.code = code;
            this.name = name;
            this.type = type;
        }

        private final String code;
        private final String name;
        private final String type;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getType() {
            return type;
        }

        public static CertificateManagementMenus getCodeByString(String code) {
            for (CertificateManagementMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }
    }


    /**
     * @author: lip
     * @date: 2020/1/7 0007 上午 9:06
     * @Description: 隐患排查模块子菜单枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum HiddenDangerTaskMenusEnum {
        EntHiddenDangerTaskEnum("hiddenDangerTask", "隐患排查任务");

        HiddenDangerTaskMenusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }


    }


    /**
     * @author: lip
     * @date: 2019/8/2 0002 上午 10:17
     * @Description: rabbitMq队列消息类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum RabbitMQMessageTypeEnum {

        AlarmTaskMessage("1", "环保_环境监管消息"),
        AlarmDataMessage("2", "环保_分级预警报警"),
        ForecastMessage("3", "预报消息"),
        RainMonitorMessage("4", "雨水点位监控消息类型"),
        PollutantMonitorMessage("5", "污染物运维消息类型"),
        SecurityHiddenTaskMessage("6", "安全_隐患任务消息"),
        StopProductionMessage("7", "企业停产消息类型"),
        SecurityTaskMessage("8", "安全_任务处置消息"),
        SecurityAlarmDataMessage("9", "安全_分级预警报警"),
        OutPutStopProductionMessage("10", "废气废水排口停产消息类型"),
        PointOffLineMessage("11", "监测点位离线消息类型");


        RabbitMQMessageTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static RabbitMQMessageTypeEnum getCodeByString(String code) {
            for (RabbitMQMessageTypeEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        /**
         * @author: lip
         * @date: 2019/9/7 0007 上午 11:03
         * @Description: 全部消息类型
         * @return:
         */
        public static List<String> allType() {
            return Arrays.asList(AlarmTaskMessage.getCode(), AlarmDataMessage.getCode());
        }

        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (RabbitMQMessageTypeEnum enums : RabbitMQMessageTypeEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 上午 10:17
     * @Description: rabbitMq队列消息类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum appMessageTypeEnum {
        //首页报警数据统计
        AlarmDataCountMessage("1", "报警数据统计"),
        //首页报警文字提醒
        AlarmDataHomeMessage("2", "在线监测报警");

        appMessageTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static appMessageTypeEnum getCodeByString(String code) {
            for (appMessageTypeEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (appMessageTypeEnum enums : appMessageTypeEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 上午 10:17
     * @Description: rabbitMq队列报警类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum RabbitMQAlarmTypeEnum {

        EarlyWarnMessage("Online_EarlyWarn", "2", "超阈"),
        OverLimitMessage("Online_OverLimit", "2", "超标"),
        OverStandardMessage("Online_OverStandard", "2", "超标"),
        ExceptionMessage("Online_Exception", "2", "异常"),
        ConcentrationChangeMessage("Online_ConcentrationChange", "2", "浓度突变"),
        FlowChangeMessage("Online_FlowChange", "2", "排放量突变"),
        AlarmTaskMessage("Online_AlarmTask", "1", "");

        RabbitMQAlarmTypeEnum(String code, String type, String name) {
            this.code = code;
            this.type = type;
            this.name = name;

        }

        private final String code;
        private final String type;
        private final String name;

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static RabbitMQAlarmTypeEnum getCodeByString(String code) {
            for (RabbitMQAlarmTypeEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }


        /**
         * 通过code取type
         *
         * @param Code
         * @return
         */
        public static String getTypeByCode(String Code) {
            for (RabbitMQAlarmTypeEnum enums : RabbitMQAlarmTypeEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.type;
                }
            }
            return "";
        }

        /**
         * 通过code取type
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (RabbitMQAlarmTypeEnum enums : RabbitMQAlarmTypeEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/2 0002 上午 10:17
     * @Description: 报警监测类型和菜单对照信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AlarmMonitorTypeMenu {

        WaterEarlyWarnMessage("1_Online_EarlyWarn", Arrays.asList("waterOverthresholdEarlyWarn", "fsjc")),
        WaterOverLimitMessage("1_Online_OverLimit", Arrays.asList("waterOverAlarm", "fsjc")),
        WaterOverStandardMessage("1_Online_OverStandard", Arrays.asList("waterOverAlarm", "fsjc")),
        WaterExceptionMessage("1_Online_Exception", Arrays.asList("waterDataAbnormalAlarm", "fsjc")),
        WaterConcentrationChangeMessage("1_Online_ConcentrationChange", Arrays.asList("wasteWaterSuddenChangeWarn", "fsjc")),
        WaterFlowChangeMessage("1_Online_FlowChange", Arrays.asList("WaterFlowChangeEarlyWarn", "fsjc")),


        smokeEarlyWarnMessage("22_Online_EarlyWarn", Arrays.asList("smokeOverthresholdEarlyWarn", "yqjc")),
        smokeOverLimitMessage("22_Online_OverLimit", Arrays.asList("smokeOverAlarm", "yqjc")),
        smokeOverStandardMessage("22_Online_OverStandard", Arrays.asList("smokeOverAlarm", "yqjc")),
        smokeExceptionMessage("22_Online_Exception", Arrays.asList("smokeDataAbnormalAlarm", "yqjc")),
        smokeConcentrationChangeMessage("22_Online_ConcentrationChange", Arrays.asList("smokeSuddenChangeWarn", "yqjc")),
        smokeFlowChangeMessage("22_Online_FlowChange", Arrays.asList("SmokeFlowChangeEarlyWarn", "yqjc")),


        gasEarlyWarnMessage("2_Online_EarlyWarn", Arrays.asList("gasOverthresholdEarlyWarn", "fqjc")),
        gasOverLimitMessage("2_Online_OverLimit", Arrays.asList("gasOverAlarm", "fqjc")),
        gasOverStandardMessage("2_Online_OverStandard", Arrays.asList("gasOverAlarm", "fqjc")),
        gasExceptionMessage("2_Online_Exception", Arrays.asList("gasDataAbnormalAlarm", "fqjc")),
        gasConcentrationChangeMessage("2_Online_ConcentrationChange", Arrays.asList("wasteGasSuddenChangeWarn", "fqjc")),
        gasFlowChangeMessage("2_Online_FlowChange", Arrays.asList("GasFlowChangeEarlyWarn", "fqjc")),


        rainEarlyWarnMessage("37_Online_EarlyWarn", Arrays.asList("rainOverthresholdEarlyWarn", "ysjc")),
        rainOverLimitMessage("37_Online_OverLimit", Arrays.asList("rainOverAlarm", "ysjc")),
        rainOverStandardMessage("37_Online_OverStandard", Arrays.asList("rainOverAlarm", "ysjc")),
        rainExceptionMessage("37_Online_Exception", Arrays.asList("rainDataAbnormalAlarm", "ysjc")),
        rainConcentrationChangeMessage("37_Online_ConcentrationChange", Arrays.asList("rainSuddenChangeWarn", "ysjc")),

        entStinkEarlyWarnMessage("40_Online_EarlyWarn", Arrays.asList("wasteEntStinkOverthresholdEarlyWarn", "ec")),
        entStinkOverLimitMessage("40_Online_OverLimit", Arrays.asList("wasteEntStinkOverAlarm", "ec")),
        entStinkOverStandardMessage("40_Online_OverStandard", Arrays.asList("wasteEntStinkOverAlarm", "ec")),
        entStinkExceptionMessage("40_Online_Exception", Arrays.asList("wasteEntStinkDataAbnormalAlarm", "ec")),
        entStinkConcentrationChangeMessage("40_Online_ConcentrationChange", Arrays.asList("factoryStinkSuddenChangeWarn", "ec")),

        entSEarlyWarnMessage("41_Online_EarlyWarn", Arrays.asList("wasteEntSmallStationOverthresholdEarlyWarn")),
        entSOverLimitMessage("41_Online_OverLimit", Arrays.asList("wasteEntSmallStationOverAlarm")),
        entSOverStandardMessage("41_Online_OverStandard", Arrays.asList("wasteEntSmallStationOverAlarm")),
        entSExceptionMessage("41_Online_Exception", Arrays.asList("wasteEntSmallStationDataAbnormalAlarm")),
        entSConcentrationChangeMessage("41_Online_ConcentrationChange", Arrays.asList("factorySmallStationSuddenChangeWarn")),


        airEarlyWarnMessage("5_Online_EarlyWarn", Arrays.asList("airOverthresholdEarlyWarn", "dqjc")),
        airOverLimitMessage("5_Online_OverLimit", Arrays.asList("airOverAlarm", "dqjc")),
        airOverStandardMessage("5_Online_OverStandard", Arrays.asList("airOverAlarm", "dqjc")),
        airExceptionMessage("5_Online_Exception", Arrays.asList("airDataAbnormalAlarm", "dqjc")),
        airConcentrationChangeMessage("5_Online_ConcentrationChange", Arrays.asList("airSuddenChangeWarn", "dqjc")),

        stinkEarlyWarnMessage("9_Online_EarlyWarn", Arrays.asList("stinkOverthresholdEarlyWarn", "ec")),
        stinkOverLimitMessage("9_Online_OverLimit", Arrays.asList("stinkOverAlarm", "ec")),
        stinkOverStandardMessage("9_Online_OverStandard", Arrays.asList("stinkOverAlarm", "ec")),
        stinkExceptionMessage("9_Online_Exception", Arrays.asList("stinkDataAbnormalAlarm", "ec")),
        stinkConcentrationChangeMessage("9_Online_ConcentrationChange", Arrays.asList("stinkSuddenChangeWarn", "ec")),

        vocEarlyWarnMessage("10_Online_EarlyWarn", Arrays.asList("vocOverthresholdEarlyWarn", "dqjc")),
        vocOverLimitMessage("10_Online_OverLimit", Arrays.asList("vocOverAlarm", "dqjc")),
        vocOverStandardMessage("10_Online_OverStandard", Arrays.asList("vocOverAlarm", "dqjc")),
        vocExceptionMessage("10_Online_Exception", Arrays.asList("vocDataAbnormalAlarm", "dqjc")),
        vocConcentrationChangeMessage("10_Online_ConcentrationChange", Arrays.asList("vocSuddenChangeWarn", "dqjc"));

        AlarmMonitorTypeMenu(String monitorAlarmType, List<String> menucodes) {
            this.monitorAlarmType = monitorAlarmType;
            this.menucodes = menucodes;

        }

        private final String monitorAlarmType;

        private final List<String> menucodes;

        public String getMonitorAlarmType() {
            return monitorAlarmType;
        }

        public List<String> getMenucodes() {
            return menucodes;
        }

        public static List<String> getMenuCodeByType(String monitorAlarmType) {
            for (AlarmMonitorTypeMenu enums : AlarmMonitorTypeMenu.values()) {
                if (enums.monitorAlarmType.equals(monitorAlarmType)) {
                    return enums.menucodes;
                }
            }
            return Arrays.asList();
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/27 0027 上午 9:53
     * @Description: 常规六参数+aqi
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum AirCommonSixIndexEnum {

        AIR_COMMON_SIX_INDEX_AQI("aqi", "AQI", ""),
        AIR_COMMON_SIX_INDEX_PM25("a34004", "PM2.5", "μg/m³"),
        AIR_COMMON_SIX_INDEX_PM10("a34002", "PM10", "μg/m³"),
        AIR_COMMON_SIX_INDEX_O3("a05024", "O3", "μg/m³"),
        AIR_COMMON_SIX_INDEX_NO2("a21004", "NO2", "μg/m³"),
        AIR_COMMON_SIX_INDEX_CO("a21005", "CO", "mg/m³"),
        AIR_COMMON_SIX_INDEX_SO2("a21026", "SO2", "μg/m³");

        AirCommonSixIndexEnum(String code, String name, String unit) {
            this.code = code;
            this.name = name;
            this.unit = unit;
        }

        private final String code;
        private final String name;
        private final String unit;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getUnit() {
            return unit;
        }

        public static AirCommonSixIndexEnum getCodeByString(String code) {
            for (AirCommonSixIndexEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }


        /**
         * 通过code取name
         *
         * @param Code
         * @return
         */
        public static String getNameByCode(String Code) {
            for (AirCommonSixIndexEnum enums : AirCommonSixIndexEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }


        public static String getCodeByName(String name) {
            for (AirCommonSixIndexEnum enums : AirCommonSixIndexEnum.values()) {
                if (enums.name.equals(name)) {
                    return enums.code;
                }
            }
            return "";
        }

        /**
         * 通过code取unit
         *
         * @param Code
         * @return
         */
        public static String getUnitByCode(String Code) {
            for (AirCommonSixIndexEnum enums : AirCommonSixIndexEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.unit;
                }
            }
            return "";
        }

        public static List<String> getSixPollutantCodes() {
            AirCommonSixIndexEnum[] values = AirCommonSixIndexEnum.values();
            List<String> sixPollutants = new ArrayList<>();
            for (AirCommonSixIndexEnum value : values) {
                if (value != AIR_COMMON_SIX_INDEX_AQI) {
                    sixPollutants.add(value.getCode());
                }
            }
            return sixPollutants;
        }

        public static List<AirCommonSixIndexEnum> getSixPollutantsEnum() {
            AirCommonSixIndexEnum[] values = AirCommonSixIndexEnum.values();
            List<AirCommonSixIndexEnum> sixPollutants = new ArrayList<>();
            for (AirCommonSixIndexEnum value : values) {
                if (value != AIR_COMMON_SIX_INDEX_AQI) {
                    sixPollutants.add(value);
                }
            }
            return sixPollutants;
        }
    }


    public static int[] getRemindsByMonitorPointType(int monitorPointType) {
        MonitorPointTypeEnum monitorPointTypeEnum = MonitorPointTypeEnum.getCodeByInt(monitorPointType);
        Assert.notNull(monitorPointTypeEnum, "monitorPointTypeEnum must not be null!");
        if (monitorPointTypeEnum == MonitorPointTypeEnum.WasteGasEnum || MonitorPointTypeEnum.WasteWaterEnum == monitorPointTypeEnum) {
            return new int[]{
                    RemindTypeEnum.ConcentrationChangeEnum.getCode(),
                    RemindTypeEnum.FlowChangeEnum.getCode(),
                    RemindTypeEnum.EarlyAlarmEnum.getCode(),
                    RemindTypeEnum.ExceptionAlarmEnum.getCode(),
                    RemindTypeEnum.OverAlarmEnum.getCode()
            };
        } else {
            return new int[]{
                    RemindTypeEnum.ConcentrationChangeEnum.getCode(),
                    RemindTypeEnum.EarlyAlarmEnum.getCode(),
                    RemindTypeEnum.ExceptionAlarmEnum.getCode(),
                    RemindTypeEnum.OverAlarmEnum.getCode()
            };
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/13 16:09
     * @Description: app菜单和监测点类型对照关系
     * @param:
     * @return:
     */
    public enum AppSysModelAndType {
        app_fqjc("fqjc", "废气监测", Arrays.asList(MonitorPointTypeEnum.WasteGasEnum.getCode(), MonitorPointTypeEnum.SmokeEnum.getCode())),
        app_fsjc("fsjc", "废水监测", Arrays.asList(MonitorPointTypeEnum.WasteWaterEnum.getCode())),
        app_ysjc("ysjc", "雨水监测", Arrays.asList(MonitorPointTypeEnum.RainEnum.getCode())),
        app_ec("ec", "恶臭", Arrays.asList(MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode())),
        app_dqjc("dqjc", "大气监测", Arrays.asList(MonitorPointTypeEnum.AirEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalVocEnum.getCode())),
        app_cjxxz("xxz", "厂界小型站", Arrays.asList(MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode())),
        app_yqqy("yqqy", "园区企业", null);

        AppSysModelAndType(String sysmodel, String sysname, List<Integer> monitorPointTypes) {
            this.sysmodel = sysmodel;
            this.sysname = sysname;
            this.monitorPointTypes = monitorPointTypes;
        }

        private final String sysmodel;
        private final String sysname;
        private final List<Integer> monitorPointTypes;

        public String getSysmodel() {
            return sysmodel;
        }

        public String getSysname() {
            return sysname;
        }

        public List<Integer> getMonitorPointTypes() {
            return monitorPointTypes;
        }

        public static List<Integer> getTypesBySysmodel(String sysmodel) {
            for (AppSysModelAndType enums : AppSysModelAndType.values()) {
                if (enums.sysmodel.equals(sysmodel)) {
                    return enums.monitorPointTypes;
                }
            }
            return null;
        }

        public static String getSysnameBySysmodel(String sysmodel) {
            for (AppSysModelAndType enums : AppSysModelAndType.values()) {
                if (enums.sysmodel.equals(sysmodel)) {
                    return enums.sysname;
                }
            }
            return null;
        }


    }


    /**
     * @author: lip
     * @date: 2019/7/13 16:09
     * @Description: app菜单和监测点类型对照关系
     * @param:
     * @return:
     */
    public enum alarmTypeAndRemindType {
        alarmData("alarmdata", Arrays.asList(RemindTypeEnum.ExceptionAlarmEnum.code,
                RemindTypeEnum.OverAlarmEnum.code)),
        earlyData("earlydata", Arrays.asList(RemindTypeEnum.ConcentrationChangeEnum.code,
                RemindTypeEnum.EarlyAlarmEnum.code, RemindTypeEnum.FlowChangeEnum.code));


        alarmTypeAndRemindType(String typecode, List<Integer> remindTypes) {
            this.typecode = typecode;
            this.remindTypes = remindTypes;
        }

        private final String typecode;

        private final List<Integer> remindTypes;

        public String getTypecode() {
            return typecode;
        }

        public List<Integer> getRemindTypes() {
            return remindTypes;
        }

        public static String getAlarmByType(Integer remindType) {
            for (alarmTypeAndRemindType enums : alarmTypeAndRemindType.values()) {
                if (enums.remindTypes.contains(remindType)) {
                    return enums.typecode;
                }
            }
            return null;
        }


    }


    /**
     * @author: zhangzc
     * @date: 2019/7/13 16:09
     * @Description: 监测预警菜单枚举
     * @param:
     * @return:
     */
    public enum MonitorAlarmMenus {
        app_fqjc("fqjc", "废气监测", WasteGasEnum.getCode(), getRemindsByMonitorPointType(WasteGasEnum.getCode()), true),
        app_fsjc("fsjc", "废水监测", MonitorPointTypeEnum.WasteWaterEnum.getCode(), getRemindsByMonitorPointType(MonitorPointTypeEnum.WasteWaterEnum.getCode()), true),
        app_ysjc("ysjc", "雨水监测", MonitorPointTypeEnum.RainEnum.getCode(), getRemindsByMonitorPointType(MonitorPointTypeEnum.RainEnum.getCode()), true),
        app_cjec("cjec", "厂界恶臭", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), getRemindsByMonitorPointType(MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()), true),
        app_cjxxz("xxz", "厂界小型站", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), getRemindsByMonitorPointType(MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()), true),
        app_dqjc("dqjc", "大气监测", MonitorPointTypeEnum.AirEnum.getCode(), getRemindsByMonitorPointType(MonitorPointTypeEnum.AirEnum.getCode()), true),
        app_ec("ec", "恶臭", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), getRemindsByMonitorPointType(MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()), true),
        app_voc("voc", "VOC", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), getRemindsByMonitorPointType(MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()), true),
        app_yqqy("yqqy", "园区企业提醒", null, null, true),
        hjzl_stink("dqjc", "环境质量恶臭", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), getRemindsByMonitorPointType(MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()), true),

        WaterFlowChangeEarlyWarn("WaterFlowChangeEarlyWarn", "废水排放量突变预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.FlowChangeEnum.getCode(), false),
        GasFlowChangeEarlyWarn("GasFlowChangeEarlyWarn", "废气排放量突变预警", WasteGasEnum.getCode(), RemindTypeEnum.FlowChangeEnum.getCode(), false),

        wasteWaterSuddenChangeWarn("wasteWaterSuddenChangeWarn", "废水浓度突变预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode(), false),
        wasteGasSuddenChangeWarn("wasteGasSuddenChangeWarn", "废气浓度突变预警", WasteGasEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode(), false),
        rainSuddenChangeWarn("rainSuddenChangeWarn", "雨水浓度突变预警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode(), false),
        factoryStinkSuddenChangeWarn("factoryStinkSuddenChangeWarn", "厂界恶臭浓度突变预警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode(), false),
        factorySmallStationSuddenChangeWarn("factorySmallStationSuddenChangeWarn", "厂界小型站浓度突变预警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode(), false),
        airSuddenChangeWarn("airSuddenChangeWarn", "空气浓度突变预警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode(), false),
        stinkSuddenChangeWarn("stinkSuddenChangeWarn", "恶臭浓度突变预警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode(), false),
        vocSuddenChangeWarn("vocSuddenChangeWarn", "VOC浓度突变预警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode(), false),


        gasOverthresholdEarlyWarn("gasOverthresholdEarlyWarn", "废气浓度超阈值预警", WasteGasEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode(), false),
        waterOverthresholdEarlyWarn("waterOverthresholdEarlyWarn", "废水浓度超阈值预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode(), false),

        waterNoFlowAbnormal("waterNoFlowAbnormal", "废水无流量报警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.WaterNoFlowEnum.getCode(), false),

        rainOverthresholdEarlyWarn("rainOverthresholdEarlyWarn", "雨水浓度超阈值预警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode(), false),
        wasteEntSmallStationOverthresholdEarlyWarn("wasteEntSmallStationOverthresholdEarlyWarn", "厂界小型站浓度超阈值预警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode(), false),
        wasteEntStinkOverthresholdEarlyWarn("wasteEntStinkOverthresholdEarlyWarn", "厂界恶臭浓度超阈值预警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode(), false),
        airOverthresholdEarlyWarn("airOverthresholdEarlyWarn", "空气浓度超阈值预警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode(), false),
        stinkOverthresholdEarlyWarn("stinkOverthresholdEarlyWarn", "恶臭浓度超阈值预警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode(), false),
        vocOverthresholdEarlyWarn("vocOverthresholdEarlyWarn", "VOC浓度超阈值预警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode(), false),


        gasDataAbnormalAlarm("gasDataAbnormalAlarm", "废气数据异常报警", WasteGasEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode(), false),
        waterDataAbnormalAlarm("waterDataAbnormalAlarm", "废水数据异常报警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode(), false),
        rainDataAbnormalAlarm("rainDataAbnormalAlarm", "雨水数据异常报警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode(), false),
        wasteEntStinkDataAbnormalAlarm("wasteEntStinkDataAbnormalAlarm", "厂界恶臭数据异常报警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode(), false),
        wasteEntSmallStationDataAbnormalAlarm("wasteEntSmallStationDataAbnormalAlarm", "厂界小型站数据异常报警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode(), false),
        airDataAbnormalAlarm("airDataAbnormalAlarm", "空气数据异常报警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode(), false),
        stinkDataAbnormalAlarm("stinkDataAbnormalAlarm", "恶臭数据异常报警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode(), false),
        vocDataAbnormalAlarm("vocDataAbnormalAlarm", "VOC数据异常报警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode(), false),


        gaspermissionEarlyWarning("gaspermissionEarlyWarning", "废气排放量许可预警", null, null, null),
        waterPermissionEarlyWarning("waterPermissionEarlyWarning", "废水排放量许可预警", null, null, null),


        gasOverAlarm("gasOverAlarm", "废气数据超限报警", WasteGasEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode(), false),
        waterOverAlarm("waterOverAlarm", "废水数据超限报警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode(), false),
        rainOverAlarm("rainOverAlarm", "雨水数据超限报警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode(), false),
        wasteEntStinkOverAlarm("wasteEntStinkOverAlarm", "厂界恶臭数据超限报警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode(), false),
        wasteEntSmallStationOverAlarm("wasteEntSmallStationOverAlarm", "厂界小型站数据超限报警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode(), false),
        airOverAlarm("airOverAlarm", "空气数据超限报警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode(), false),
        stinkOverAlarm("stinkOverAlarm", "恶臭数据超限报警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode(), false),
        vocOverAlarm("vocOverAlarm", "VOC数据超限报警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode(), false);


        MonitorAlarmMenus(String code, String name, Integer monitorPointType, Object remindTypes, Boolean isApp) {
            this.code = code;
            this.name = name;
            this.monitorPointType = monitorPointType;
            this.remindTypes = remindTypes;
            this.isApp = isApp;
        }

        private final String code;
        private final String name;
        private final Integer monitorPointType;
        private final Object remindTypes;
        private final Boolean isApp;

        public Boolean isApp() {
            return isApp;
        }


        public Object getRemindTypes() {
            return remindTypes;
        }

        public Integer getMonitorPointType() {
            return monitorPointType;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static MonitorAlarmMenus getCodeByString(String code) {
            for (MonitorAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        public static String getNameByString(String code) {
            for (MonitorAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getName();
                }
            }
            return null;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/13 16:09
     * @Description: 监测预警菜单枚举
     * @param:
     * @return:
     */
    public enum SafeAlarmMenus {
        safeSuddenChange("safeSuddenChange", "HourData", "MonitorTime"),
        safeDataOver("safeDataOver", "OverData", "OverTime"),
        safeDateException("safeDateException", "ExceptionData", "ExceptionTime");
        private final String menuCode;
        private final String collectionName;

        public String getTimeFiledName() {
            return timeFiledName;
        }

        private final String timeFiledName;

        public String getMenuCode() {
            return menuCode;
        }

        public String getCollectionName() {
            return collectionName;
        }

        SafeAlarmMenus(String menuCode, String collectionName, String timeFiledName) {
            this.menuCode = menuCode;
            this.collectionName = collectionName;
            this.timeFiledName = timeFiledName;
        }

        public static SafeAlarmMenus getMenuEnumByMenuCode(String menuCode) {
            for (SafeAlarmMenus transactType : values()) {
                if (transactType.getMenuCode().equals(menuCode)) {
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/9/5 0005 下午 4:41
     * @Description: 菜单和报警枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum AlarmMenus {

        WaterFlowChangeEarlyWarn("WaterFlowChangeEarlyWarn", "排放突变"),
        GasFlowChangeEarlyWarn("GasFlowChangeEarlyWarn", "排放突变"),

        smokeSuddenChangeWarn("smokeSuddenChangeWarn", "浓度突变"),
        wasteWaterSuddenChangeWarn("wasteWaterSuddenChangeWarn", "浓度突变"),
        wasteGasSuddenChangeWarn("wasteGasSuddenChangeWarn", "浓度突变"),
        rainSuddenChangeWarn("rainSuddenChangeWarn", "浓度突变"),
        factoryStinkSuddenChangeWarn("factoryStinkSuddenChangeWarn", "浓度突变"),
        factorySmallStationSuddenChangeWarn("factorySmallStationSuddenChangeWarn", "浓度突变"),
        airSuddenChangeWarn("airSuddenChangeWarn", "浓度突变"),
        stinkSuddenChangeWarn("stinkSuddenChangeWarn", "浓度突变"),
        vocSuddenChangeWarn("vocSuddenChangeWarn", "浓度突变"),


        smokeOverthresholdEarlyWarn("smokeOverthresholdEarlyWarn", "超阈"),
        gasOverthresholdEarlyWarn("gasOverthresholdEarlyWarn", "超阈"),
        waterOverthresholdEarlyWarn("waterOverthresholdEarlyWarn", "超阈"),
        rainOverthresholdEarlyWarn("rainOverthresholdEarlyWarn", "超阈"),
        wasteEntSmallStationOverthresholdEarlyWarn("wasteEntSmallStationOverthresholdEarlyWarn", "超阈"),
        wasteEntStinkOverthresholdEarlyWarn("wasteEntStinkOverthresholdEarlyWarn", "超阈"),
        airOverthresholdEarlyWarn("airOverthresholdEarlyWarn", "超阈"),
        stinkOverthresholdEarlyWarn("stinkOverthresholdEarlyWarn", "超阈"),
        vocOverthresholdEarlyWarn("vocOverthresholdEarlyWarn", "超阈"),


        smokeDataAbnormalAlarm("smokeDataAbnormalAlarm", "异常"),
        gasDataAbnormalAlarm("gasDataAbnormalAlarm", "异常"),
        waterDataAbnormalAlarm("waterDataAbnormalAlarm", "异常"),
        rainDataAbnormalAlarm("rainDataAbnormalAlarm", "异常"),
        wasteEntStinkDataAbnormalAlarm("wasteEntStinkDataAbnormalAlarm", "异常"),
        wasteEntSmallStationDataAbnormalAlarm("wasteEntSmallStationDataAbnormalAlarm", "异常"),
        airDataAbnormalAlarm("airDataAbnormalAlarm", "异常"),
        stinkDataAbnormalAlarm("stinkDataAbnormalAlarm", "异常"),
        vocDataAbnormalAlarm("vocDataAbnormalAlarm", "异常"),


        gaspermissionEarlyWarning("gaspermissionEarlyWarning", "排放许可"),
        waterPermissionEarlyWarning("waterPermissionEarlyWarning", "排放许可"),


        smokeOverAlarm("smokeOverAlarm", "超限"),
        gasOverAlarm("gasOverAlarm", "超限"),
        waterOverAlarm("waterOverAlarm", "超限"),
        rainOverAlarm("rainOverAlarm", "超限"),
        wasteEntStinkOverAlarm("wasteEntStinkOverAlarm", "超限"),
        wasteEntSmallStationOverAlarm("wasteEntSmallStationOverAlarm", "超限"),
        airOverAlarm("airOverAlarm", "超限"),
        stinkOverAlarm("stinkOverAlarm", "超限"),
        vocOverAlarm("vocOverAlarm", "超限");


        AlarmMenus(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;


        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static AlarmMenus getCodeByString(String code) {
            for (AlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        public static String getNameByString(String code) {
            for (AlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getName();
                }
            }
            return null;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 10:23
     * @Description: gis系统监测点类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum GisMonitorPointTypeEnum {
        GisPollutionEnum("pollution", "入驻企业"),

        GisWasteWaterEnum("1", "废水监测点类型"),

        GisWasteGasEnum("2", "废气监测点类型"),

        GisRainEnum("37", "雨水监测点类型"),

        GisAirEnum("5", "大气监测点类型"),
        GisWaterQualityEnum("6", "水质监测点类型"),

        GisEnvironmentalVocEnum("10", "环境质量VOC监测点类型"),

        GisEnvironmentalStinkEnum("9", "环境质量恶臭监测点类型"),

        GisMicroStationEnum("33", "微型站"),

        GisvideoEnum("39", "视频监测点类型"),

        GisFactoryBoundarySmallStationEnum("41", "厂界小型站监测点类型"),

        GisFactoryBoundaryStinkEnum("40", "厂界恶臭监测点类型"),

        GisMeteoEnum("52", "气象监测点类型");


        GisMonitorPointTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static GisMonitorPointTypeEnum getCodeByString(String code) {
            for (GisMonitorPointTypeEnum transactType : values()) {
                if (code.equals(transactType.getCode())) {
                    //获取指定的枚举
                    return transactType;
                }
            }
            return null;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 上午 11:19
     * @Description: 事件类型枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum EventTypeEnum {
        AlarmDataEnum((short) 1, "数据报警"),
        PetitionEnum((short) 2, "投诉信访");

        EventTypeEnum(short code, String name) {
            this.code = code;
            this.name = name;
        }

        private final short code;
        private final String name;

        public String getName() {
            return name;
        }

        public short getCode() {
            return code;
        }

        public static EventTypeEnum getCodeByString(Integer code) {
            for (EventTypeEnum transactType : values()) {
                if (code.equals(transactType.getCode())) {
                    //获取指定的枚举
                    return transactType;
                }
            }
            return null;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/29 0029 上午 11:19
     * @Description: 隐患状态枚举 1新建，2整改中，3完成
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum DangerStateTypeEnum {
        addEnum((short) 1, "新建"),
        doingEnum((short) 2, "整改中"),
        completeEnum((short) 3, "完成");

        DangerStateTypeEnum(short code, String name) {
            this.code = code;
            this.name = name;
        }

        private final short code;
        private final String name;

        public String getName() {
            return name;
        }

        public short getCode() {
            return code;
        }

    }


    /**
     * @author: lip
     * @date: 2019/11/15 0015 下午 2:27
     * @Description: app状态排序配置
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum onlineStatusOrderEnum {
        overEnum(1, "3"),
        exceptionEnum(2, "2"),
        onlineEnum(3, "1"),
        offlineEnum(4, "0");

        onlineStatusOrderEnum(Integer index, String code) {
            this.index = index;
            this.code = code;
        }

        private final Integer index;
        private final String code;

        public String getCode() {
            return code;
        }

        public Integer getIndex() {
            return index;
        }

        public static Integer getIndexByCode(String code) {
            for (onlineStatusOrderEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getIndex();
                }
            }
            return 5;
        }
    }

    /**
     * @author: lip
     * @date: 2020/4/3 0003 下午 1:15
     * @Description: pc数据一览排序
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum onlinePCStatusOrderEnum {
        overEnum(4, "2"),
        exceptionEnum(3, "3"),
        onlineEnum(1, "1"),
        offlineEnum(2, "0");

        onlinePCStatusOrderEnum(Integer index, String code) {
            this.index = index;
            this.code = code;
        }

        private final Integer index;
        private final String code;

        public String getCode() {
            return code;
        }

        public Integer getIndex() {
            return index;
        }

        public static Integer getIndexByCode(String code) {
            for (onlinePCStatusOrderEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getIndex();
                }
            }
            return 5;
        }
    }


    /**
     * @author: lip
     * @date: 2020/4/3 0003 下午 1:15
     * @Description: 获取排序key
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum StatusOrderSetEnum {
        stopEnum("stop", "4"),
        overEnum("over", "2"),
        exceptionEnum("exception", "3"),
        onlineEnum("normal", "1"),
        offlineEnum("offline", "0");

        StatusOrderSetEnum(String key, String code) {
            this.key = key;
            this.code = code;
        }

        private final String key;
        private final String code;

        public String getCode() {
            return code;
        }

        public String getKey() {
            return key;
        }

        public static String getIndexByCode(String code) {
            for (StatusOrderSetEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getKey();
                }
            }
            return "";
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/13 16:09
     * @Description: 监测预警菜单枚举
     * @param:
     * @return:
     */
    public enum PcHBAlarmMenus {
        WaterFlowChangeEarlyWarn("WaterFlowChangeEarlyWarn", "废水排放量突变预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.FlowChangeEnum.getCode()),
        GasFlowChangeEarlyWarn("GasFlowChangeEarlyWarn", "废气排放量突变预警", WasteGasEnum.getCode(), RemindTypeEnum.FlowChangeEnum.getCode()),
        SmokeFlowChangeEarlyWarn("SmokeFlowChangeEarlyWarn", "烟气排放量突变预警", SmokeEnum.getCode(), RemindTypeEnum.FlowChangeEnum.getCode()),

        wasteWaterSuddenChangeWarn("wasteWaterSuddenChangeWarn", "废水浓度突变预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        wasteGasSuddenChangeWarn("wasteGasSuddenChangeWarn", "废气浓度突变预警", WasteGasEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        smokeSuddenChangeWarn("smokeSuddenChangeWarn", "烟气浓度突变预警", SmokeEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        rainSuddenChangeWarn("rainSuddenChangeWarn", "雨水浓度突变预警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        factoryStinkSuddenChangeWarn("factoryStinkSuddenChangeWarn", "厂界恶臭浓度突变预警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        factorySmallStationSuddenChangeWarn("factorySmallStationSuddenChangeWarn", "厂界小型站浓度突变预警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        airSuddenChangeWarn("airSuddenChangeWarn", "空气浓度突变预警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        stinkSuddenChangeWarn("stinkSuddenChangeWarn", "恶臭浓度突变预警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        vocSuddenChangeWarn("vocSuddenChangeWarn", "VOC浓度突变预警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        raiseDustSuddenChangeWarn("raiseDustSuddenChangeWarn", "扬尘突变预警", MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),

        gasOverthresholdEarlyWarn("gasOverthresholdEarlyWarn", "废气浓度超阈值预警", WasteGasEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        smokeOverthresholdEarlyWarn("smokeOverthresholdEarlyWarn", "烟气浓度超阈值预警", SmokeEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        waterOverthresholdEarlyWarn("waterOverthresholdEarlyWarn", "废水浓度超阈值预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        rainOverthresholdEarlyWarn("rainOverthresholdEarlyWarn", "雨水浓度超阈值预警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        wasteEntSmallStationOverthresholdEarlyWarn("wasteEntSmallStationOverthresholdEarlyWarn", "厂界小型站浓度超阈值预警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        wasteEntStinkOverthresholdEarlyWarn("wasteEntStinkOverthresholdEarlyWarn", "厂界恶臭浓度超阈值预警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        airOverthresholdEarlyWarn("airOverthresholdEarlyWarn", "空气浓度超阈值预警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        stinkOverthresholdEarlyWarn("stinkOverthresholdEarlyWarn", "恶臭浓度超阈值预警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        vocOverthresholdEarlyWarn("vocOverthresholdEarlyWarn", "VOC浓度超阈值预警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        raiseDustOverthresholdEarlyWarn("raiseDustOverthresholdEarlyWarn", "扬尘超阈值预警", MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),


        gasDataAbnormalAlarm("gasDataAbnormalAlarm", "废气数据异常报警", WasteGasEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        smokeDataAbnormalAlarm("smokeDataAbnormalAlarm", "烟气数据异常报警", SmokeEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        waterDataAbnormalAlarm("waterDataAbnormalAlarm", "废水数据异常报警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        waterNoFlowAbnormal("waterNoFlowAbnormal", "废水无流量异常", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.WaterNoFlowEnum.getCode()),
        rainDataAbnormalAlarm("rainDataAbnormalAlarm", "雨水数据异常报警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        wasteEntStinkDataAbnormalAlarm("wasteEntStinkDataAbnormalAlarm", "厂界恶臭数据异常报警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        wasteEntSmallStationDataAbnormalAlarm("wasteEntSmallStationDataAbnormalAlarm", "厂界小型站数据异常报警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        airDataAbnormalAlarm("airDataAbnormalAlarm", "空气数据异常报警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        stinkDataAbnormalAlarm("stinkDataAbnormalAlarm", "恶臭数据异常报警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        vocDataAbnormalAlarm("vocDataAbnormalAlarm", "VOC数据异常报警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        raiseDustDataAbnormalAlarm("raiseDustDataAbnormalAlarm", "扬尘异常报警", MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),

        gasOverAlarm("gasOverAlarm", "废气数据超限报警", WasteGasEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        smokeOverAlarm("smokeOverAlarm", "烟气数据超限报警", SmokeEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        waterOverAlarm("waterOverAlarm", "废水数据超限报警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        rainOverAlarm("rainOverAlarm", "雨水数据超限报警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        wasteEntStinkOverAlarm("wasteEntStinkOverAlarm", "厂界恶臭数据超限报警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        wasteEntSmallStationOverAlarm("wasteEntSmallStationOverAlarm", "厂界小型站数据超限报警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        airOverAlarm("airOverAlarm", "空气数据超限报警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        stinkOverAlarm("stinkOverAlarm", "恶臭数据超限报警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        raiseDustOverAlarm("raiseDustOverAlarm", "扬尘超限报警", MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        vocOverAlarm("vocOverAlarm", "VOC数据超限报警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),


        microStationSuddenChangeWarn("microStationSuddenChangeWarn", "TVOC突变预警", MonitorPointTypeEnum.MicroStationEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        microStationOverthresholdEarlyWarn("microStationOverthresholdEarlyWarn", "TVOC超阈值预警", MonitorPointTypeEnum.MicroStationEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),
        microStationDataAbnormalAlarm("microStationDataAbnormalAlarm", "TVOC异常报警", MonitorPointTypeEnum.MicroStationEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        microStationOverAlarm("microStationOverAlarm", "TVOC超限报警", MonitorPointTypeEnum.MicroStationEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),


        dataMutationWarning("dataMutationWarning", "数据突变预警", MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), RemindTypeEnum.StorageTankDataChangeEnum.getCode()),
        dataDataAbnormalAlarm("dataDataAbnormalAlarm", "数据异常报警", MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        dataOverAlarm("dataOverAlarm", "数据超限报警", MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        dataOverthresholdEarlyWarn("dataOverthresholdEarlyWarn", "数据超阈值预警", MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),

        easyBumsGasAbnormalPolice("easyBumsGasAbnormalPolice", "易燃气体异常报警", SecurityCombustibleMonitor.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        easyBumsGasTransfinitePolice("easyBumsGasTransfinitePolice", "易燃气体超限报警", SecurityCombustibleMonitor.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        easyBumsGasSuperthreshold("easyBumsGasSuperthreshold", "易燃气体超阈值预警", SecurityCombustibleMonitor.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),

        harmGasAbnormalPolice("harmGasAbnormalPolice", "有毒气体异常报警", SecurityToxicMonitor.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        harmGasTransfinitePolice("harmGasTransfinitePolice", "有毒气体超限报警", SecurityToxicMonitor.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        harmGasSuperthreshold("harmGasSuperthreshold", "有毒气体超阈值预警", SecurityToxicMonitor.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),

        //新分级预警菜单
        gasOverthresholdEarlyWarn2("gasOverthresholdEarlyWarn2", "废气浓度超阈值预警", WasteGasEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        smokeOverthresholdEarlyWarn2("smokeOverthresholdEarlyWarn2", "烟气浓度超阈值预警", SmokeEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        waterOverthresholdEarlyWarn2("waterOverthresholdEarlyWarn2", "废水浓度超阈值预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        rainOverthresholdEarlyWarn2("rainOverthresholdEarlyWarn2", "雨水浓度超阈值预警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        wasteEntSmallStationOverthresholdEarlyWarn2("wasteEntSmallStationOverthresholdEarlyWarn2", "厂界小型站浓度超阈值预警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        wasteEntStinkOverthresholdEarlyWarn2("wasteEntStinkOverthresholdEarlyWarn2", "厂界恶臭浓度超阈值预警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        airOverthresholdEarlyWarn2("airOverthresholdEarlyWarn2", "空气浓度超阈值预警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        stinkOverthresholdEarlyWarn2("stinkOverthresholdEarlyWarn2", "恶臭浓度超阈值预警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        vocOverthresholdEarlyWarn2("vocOverthresholdEarlyWarn2", "VOC浓度超阈值预警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        raiseDustOverthresholdEarlyWarn2("raiseDustOverthresholdEarlyWarn2", "扬尘超阈值预警", MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),


        gasDataAbnormalAlarm2("gasDataAbnormalAlarm2", "废气数据异常报警", WasteGasEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        smokeDataAbnormalAlarm2("smokeDataAbnormalAlarm2", "烟气数据异常报警", SmokeEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        waterDataAbnormalAlarm2("waterDataAbnormalAlarm2", "废水数据异常报警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        waterNoFlowAbnormal2("waterNoFlowAbnormal2", "废水无流量异常", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.WaterNoFlowEnum.getCode()),
        rainDataAbnormalAlarm2("rainDataAbnormalAlarm2", "雨水数据异常报警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        wasteEntStinkDataAbnormalAlarm2("wasteEntStinkDataAbnormalAlarm2", "厂界恶臭数据异常报警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        wasteEntSmallStationDataAbnormalAlarm2("wasteEntSmallStationDataAbnormalAlarm2", "厂界小型站数据异常报警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        airDataAbnormalAlarm2("airDataAbnormalAlarm2", "空气数据异常报警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        stinkDataAbnormalAlarm2("stinkDataAbnormalAlarm2", "恶臭数据异常报警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        vocDataAbnormalAlarm2("vocDataAbnormalAlarm2", "VOC数据异常报警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        raiseDustDataAbnormalAlarm2("raiseDustDataAbnormalAlarm2", "扬尘异常报警", MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),

        gasOverAlarm2("gasOverAlarm2", "废气数据超限报警", WasteGasEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        smokeOverAlarm2("smokeOverAlarm2", "烟气数据超限报警", SmokeEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        waterOverAlarm2("waterOverAlarm2", "废水数据超限报警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        rainOverAlarm2("rainOverAlarm2", "雨水数据超限报警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        wasteEntStinkOverAlarm2("wasteEntStinkOverAlarm2", "厂界恶臭数据超限报警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        wasteEntSmallStationOverAlarm2("wasteEntSmallStationOverAlarm2", "厂界小型站数据超限报警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        airOverAlarm2("airOverAlarm2", "空气数据超限报警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        stinkOverAlarm2("stinkOverAlarm2", "恶臭数据超限报警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        raiseDustOverAlarm2("raiseDustOverAlarm2", "扬尘超限报警", MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        vocOverAlarm2("vocOverAlarm2", "VOC数据超限报警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),

        microStationOverthresholdEarlyWarn2("microStationOverthresholdEarlyWarn2", "TVOC超阈值预警", MonitorPointTypeEnum.MicroStationEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        microStationDataAbnormalAlarm2("microStationDataAbnormalAlarm2", "TVOC异常报警", MonitorPointTypeEnum.MicroStationEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        microStationOverAlarm2("microStationOverAlarm2", "TVOC超限报警", MonitorPointTypeEnum.MicroStationEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode());


        PcHBAlarmMenus(String code, String name, Integer monitorPointType, Integer remindType) {
            this.code = code;
            this.name = name;
            this.monitorPointType = monitorPointType;
            this.remindType = remindType;
        }

        private final String code;
        private final String name;
        private final Integer monitorPointType;
        private final Integer remindType;

        public Integer getRemindType() {
            return remindType;
        }

        public Integer getMonitorPointType() {
            return monitorPointType;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static PcHBAlarmMenus getCodeByString(String code) {
            for (PcHBAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        public static PcHBAlarmMenus getEnumByParam(Integer monitorPointType, Integer remindType) {
            PcHBAlarmMenus[] values = PcHBAlarmMenus.values();
            for (PcHBAlarmMenus value : values) {
                if (value.getRemindType().equals(remindType) && value.getMonitorPointType().equals(monitorPointType)) {
                    return value;
                }
            }
            return null;
        }

        public static String getNameByString(String code) {
            for (PcHBAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getName();
                }
            }
            return null;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/13 16:09
     * @Description: 监测预警菜单枚举
     * @param:
     * @return:
     */
    public enum PcAQAlarmMenus {

        dataMutationWarning("dataMutationWarning", "数据突变预警", MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), RemindTypeEnum.StorageTankDataChangeEnum.getCode()),
        dataDataAbnormalAlarm("dataDataAbnormalAlarm", "数据异常报警", MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        dataOverAlarm("dataOverAlarm", "数据超限报警", MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        dataOverthresholdEarlyWarn("dataOverthresholdEarlyWarn", "数据超阈值预警", MonitorPointTypeEnum.StorageTankAreaEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),

        riskAreaMutationWarning("riskAreaMutationWarning", "数据突变预警", SecurityToxicMonitor.getCode(), RemindTypeEnum.StorageTankDataChangeEnum.getCode()),
        riskAreaAbnormalPolice("riskAreaAbnormalPolice", "数据异常报警", SecurityToxicMonitor.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        riskAreaTransfinitePolice("riskAreaTransfinitePolice", "数据超限报警", SecurityToxicMonitor.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        riskAreaSuperthresholdWarning("riskAreaSuperthresholdWarning", "数据超阈值预警", SecurityToxicMonitor.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode()),

        productionDataMutationWarning("productionDataMutationWarning", "数据突变预警", MonitorPointTypeEnum.ProductionSiteEnum.getCode(), RemindTypeEnum.StorageTankDataChangeEnum.getCode()),
        productionDataDataAbnormalAlarm("productionDataDataAbnormalAlarm", "数据异常报警", MonitorPointTypeEnum.ProductionSiteEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum.getCode()),
        productionDataOverAlarm("productionDataOverAlarm", "数据超限报警", MonitorPointTypeEnum.ProductionSiteEnum.getCode(), RemindTypeEnum.OverAlarmEnum.getCode()),
        productionOverthresholdEarlyWarn("productionOverthresholdEarlyWarn", "数据超阈值预警", MonitorPointTypeEnum.ProductionSiteEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum.getCode());

        PcAQAlarmMenus(String code, String name, Integer monitorPointType, Integer remindType) {
            this.code = code;
            this.name = name;
            this.monitorPointType = monitorPointType;
            this.remindType = remindType;
        }

        private final String code;
        private final String name;
        private final Integer monitorPointType;
        private final Integer remindType;

        public Integer getRemindType() {
            return remindType;
        }

        public Integer getMonitorPointType() {
            return monitorPointType;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static PcAQAlarmMenus getCodeByString(String code) {
            for (PcAQAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        public static PcAQAlarmMenus getEnumByParam(Integer monitorPointType, Integer remindType) {
            PcAQAlarmMenus[] values = PcAQAlarmMenus.values();
            for (PcAQAlarmMenus value : values) {
                if (value.getRemindType().equals(remindType) && value.getMonitorPointType().equals(monitorPointType)) {
                    return value;
                }
            }
            return null;
        }

        public static String getNameByString(String code) {
            for (PcAQAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getName();
                }
            }
            return null;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/12/27 0027 上午 11:16
     * @Description: 风险因素
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum RiskFactorEnum {
        personEnum("1", "人"),
        machineEnum("2", "机"),
        liaoEnum("3", "物"),
        huanEnum("4", "环"),
        guanEnum("5", "管");

        RiskFactorEnum(String index, String code) {
            this.index = index;
            this.code = code;
        }

        private final String index;
        private final String code;

        public String getCode() {
            return code;
        }

        public String getIndex() {
            return index;
        }

        public static String getIndexByCode(String code) {
            for (RiskFactorEnum transactType : values()) {
                if (transactType.getIndex().equals(code)) {
                    return transactType.getCode();
                }
            }
            return "";
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/10 0010 上午 11:43
     * @Description: 水质等级枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum FunWaterQaulityCodeEnum {

        QaulityOneEnum("Ⅰ", 1),
        QaulityTWOEnum("Ⅱ", 2),
        QaulityThreeEnum("Ⅲ", 3),
        QaulityFourEnum("Ⅳ", 4),
        QaulityFiveEnum("Ⅴ", 5),
        QaulitySixEnum("劣Ⅴ", 6);


        FunWaterQaulityCodeEnum(String name, Integer code) {
            this.code = code;
            this.name = name;
        }

        private final String name;
        private final Integer code;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static Integer getCodeByName(String name) {
            for (FunWaterQaulityCodeEnum transactType : values()) {
                if (transactType.getName().equals(name)) {
                    return transactType.getCode();
                }
            }
            return -1;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/24 0024 上午 10:57
     * @Description: 运维总览菜单枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum DevOpsOverviewMenusEnum {

        WasteWaterDevOpsEnum(1, "废水运维"),
        WasteGasDevOpsEnum(2, "废气运维"),
        SmokeDevOpsEnum(22, "烟气运维"),
        RainDevOpsEnum(37, "雨水运维"),
        FactorytinkDevOpsEnum(40, "厂界恶臭运维"),
        FactorySmallStationDevOpsEnum(41, "厂界小型站运维"),
        AirDevOpsEnum(5, "大气运维"),
        EnvStinkDevOpsEnum(9, "恶臭运维"),
        EnvVocDevOpsEnum(10, "VOC运维"),
        WaterQualityDevOpsEnum(6, "水质运维");


        DevOpsOverviewMenusEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }

        public static String getNameByCode(Integer Code) {
            for (DevOpsOverviewMenusEnum enums : DevOpsOverviewMenusEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.name;
                }
            }
            return "";
        }

    }

    /**
     * @author: xsm
     * @date: 2020/2/24 0024 下午 13:28
     * @Description: 获取所有运维监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getAllDevOpsMonitorPointTypeList() {
        return Arrays.asList(
                DevOpsOverviewMenusEnum.WasteWaterDevOpsEnum.getCode(),
                DevOpsOverviewMenusEnum.WasteGasDevOpsEnum.getCode(),
                DevOpsOverviewMenusEnum.SmokeDevOpsEnum.getCode(),
                DevOpsOverviewMenusEnum.RainDevOpsEnum.getCode(),
                DevOpsOverviewMenusEnum.FactorytinkDevOpsEnum.getCode(),
                DevOpsOverviewMenusEnum.AirDevOpsEnum.getCode(),
                DevOpsOverviewMenusEnum.EnvStinkDevOpsEnum.getCode(),
                DevOpsOverviewMenusEnum.EnvVocDevOpsEnum.getCode(),
                DevOpsOverviewMenusEnum.WaterQualityDevOpsEnum.getCode());
    }

    /**
     * @author: xsm
     * @date: 2020/03/18 0018 下午 13:53
     * @Description: 首页消息提醒消息类型（小铃铛）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum HomePageMessageTypeEnum {


        StopProductionMessage("1", "停产记录类型"),
        RainDischargeMessage("2", "雨水排放记录类型"),
        PointOffLineMessage("3", "点位离线记录类型"),
        EntCheckFeedbackMessage("4", "企业自查反馈记录类型"),
        EntCheckSubmitMessage("5", "企业自查提交记录类型"),
        EntProblemConsultMessage("6", "企业问题咨询记录类型"),
        ProblemReplyMessage("7", "咨询问题回复记录类型"),
        NoticeMessage("8", "通知信息类型");


        HomePageMessageTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static HomePageMessageTypeEnum getCodeByString(String code) {
            for (HomePageMessageTypeEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }
    }


    /**
     * @Description: 企业台账报告类型
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/1 9:50
     * <p>
     * 1生产设施运行记录,2废水治理设施运行记录,3废气治理设施运行记录,4燃料用量记录,5危固废利用、处理记录)
     */
    public enum EntStandingBookTypeEnum {
        SGJCBG("11", "手工监测报告"),
        ZXBG("12", "执行报告"),
        SCSS("1", "生产设施运行记录"),
        FSZLSS("2", "废水治理设施运行记录"),
        FQZLSS("3", "废气治理设施运行记录"),
        RLYL("4", "燃料用量记录"),
        WGFCHL("5", "危固废利用、处理记录");

        EntStandingBookTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static String getNameByCode(String code) {
            for (EntStandingBookTypeEnum transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.name;
                }
            }
            return "";
        }
    }


    /**
     * @author: xsm
     * @date: 2020/3/23 0023 上午 9:01
     * @Description: 微信群信息推送配置报警类型枚举类
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum WechatPushSetAlarmTypeEnum {
        ConcentrationChangeEnum("Online_ConcentrationChange", "浓度突变"),
        FlowChangeEnum("Online_FlowChange", "排放量突变"),
        EarlyWarnEnum("Online_EarlyWarn", "超阈值"),
        ExceptionEnum("Online_Exception", "异常"),
        OverLimitEnum("Online_OverLimit", "超限"),
        OverStandardEnum("Online_OverStandard", "超标"),
        OfflineStatusEnum("Offline", "离线"),
        QHY_AlarmPushEnum("QHY_AlarmPush", "清华园报警推送"),
        ProblemReportEnum("ProblemReport", "设备问题报告");

        WechatPushSetAlarmTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static WechatPushSetAlarmTypeEnum getCodeByString(String code) {
            for (WechatPushSetAlarmTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }

        public static String getNameByCode(String Code) {
            for (WechatPushSetAlarmTypeEnum enums : WechatPushSetAlarmTypeEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.name;
                }
            }
            return "";
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/22 0022 上午 9:20
     * @Description: 两单三卡类型枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum RiskAreayTypeEnum {
        riskEnum(1, "安全风险清单"),
        riskInvestigateEnum(2, "安全危险因素排查辨识清单"),
        emergencyDisposalCardEnum(3, "应急处置卡"),
        hazardCharactCardEnum(4, "安全危害特征应知卡"),
        safeProductDutyPromiseCardEnum(5, "安全生产职责承诺卡");

        RiskAreayTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static RiskAreayTypeEnum getCodeByString(Integer code) {
            for (RiskAreayTypeEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType;
                }
            }
            return null;
        }

        public static String getNameByCode(Integer Code) {
            for (RiskAreayTypeEnum enums : RiskAreayTypeEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.name;
                }
            }
            return "";
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/23 0023 上午 10:21
     * @Description: 获取所有微信群信息推送配置报警类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<String> getAllWechatPushSetAlarmTypeList() {
        return Arrays.asList(
                WechatPushSetAlarmTypeEnum.ConcentrationChangeEnum.getCode(),
                WechatPushSetAlarmTypeEnum.FlowChangeEnum.getCode(),
                WechatPushSetAlarmTypeEnum.EarlyWarnEnum.getCode(),
                WechatPushSetAlarmTypeEnum.ExceptionEnum.getCode(),
                WechatPushSetAlarmTypeEnum.OverLimitEnum.getCode(),
                WechatPushSetAlarmTypeEnum.OverStandardEnum.getCode(),
                WechatPushSetAlarmTypeEnum.OfflineStatusEnum.getCode(),
                WechatPushSetAlarmTypeEnum.ProblemReportEnum.getCode());


    }

    /**
     * @author: xsm
     * @date: 2020/3/31 0031 上午 10:25
     * @Description: 获取平台首页地图所有监测点类型（包含储罐、风险点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getAllHomeMapMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.WasteWaterEnum.getCode(),
                MonitorPointTypeEnum.WasteGasEnum.getCode(),
                MonitorPointTypeEnum.SmokeEnum.getCode(),
                MonitorPointTypeEnum.RainEnum.getCode(),
                MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(),
                MonitorPointTypeEnum.AirEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(),
                MonitorPointTypeEnum.riskEnum.getCode(),
                MonitorPointTypeEnum.StorageTankAreaEnum.getCode(),
                MonitorPointTypeEnum.WaterQualityEnum.getCode());
    }

    /**
     * @author: xsm
     * @date: 2020/08/08 19:18
     * @Description: 推送首页消息需菜单权限控制的菜单code配置
     * @param:
     * @return:
     */
    public enum menuAuthorityControlMenusEnum {
        RainMonitorEnum("rainMonitorAndControl", "雨水排放控制");


        menuAuthorityControlMenusEnum(String menucode, String menuname) {
            this.menucode = menucode;
            this.menuname = menuname;
        }

        private final String menucode;
        private final String menuname;

        public String getCode() {
            return menucode;
        }

        public String getName() {
            return menuname;
        }

    }

    /**
     * @author: xsm
     * @date: 2020/08/12 0012 上午 11:36
     * @Description: 获取所有安全监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Integer> getAllSecurityMonitorPointTypeList() {
        return Arrays.asList(
                MonitorPointTypeEnum.StorageTankAreaEnum.getCode(),
                MonitorPointTypeEnum.ProductionSiteEnum.getCode(),
                MonitorPointTypeEnum.SecurityLeakageMonitor.getCode(),
                SecurityCombustibleMonitor.getCode(),
                SecurityToxicMonitor.getCode());
    }

    /**
     * @author: xsm
     * @date: 2020/08/08 19:18
     * @Description: 推送首页消息需菜单权限控制的菜单code配置
     * @param:
     * @return:
     */
    public enum NavigationPollutantCategoryEnum {
        NavigationStinkEnum(1, "恶臭类"),
        NavigationAlkaneEnum(2, "烷烃类");

        NavigationPollutantCategoryEnum(Integer menucode, String menuname) {
            this.menucode = menucode;
            this.menuname = menuname;
        }

        private final Integer menucode;
        private final String menuname;

        public Integer getCode() {
            return menucode;
        }

        public String getName() {
            return menuname;
        }

        public static String getNameByCode(Integer Code) {
            for (NavigationPollutantCategoryEnum enums : NavigationPollutantCategoryEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.getName();
                }
            }
            return "";
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/27 11:27
     * @Description: Voc因子组code配置
     * @param:
     * @return:
     */
    public enum VocPollutantFactorGroupEnum {
        AlkaneEnum(2, "烷烃类"),
        OlefinEnum(3, "烯烃"),
        AromaticHydrocarbonEnum(4, "芳香烃"),
        HalogenatedHydrocarbonEnum(5, "卤代烃"),
        OVOCsEnum(6, "OVOCs"),
        QTEnum(7, "炔烃"),
        otherClassEnum(10, "其它");

        VocPollutantFactorGroupEnum(Integer menucode, String menuname) {
            this.menucode = menucode;
            this.menuname = menuname;
        }

        private final Integer menucode;
        private final String menuname;

        public Integer getCode() {
            return menucode;
        }

        public String getName() {
            return menuname;
        }

        public static String getNameByCode(Integer Code) {
            for (VocPollutantFactorGroupEnum enums : VocPollutantFactorGroupEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.getName();
                }
            }
            return "";
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/12/15 0015 上午 9:18
     * @Description: 在线数据报警类型枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum SecurityAlarmTypeEnum {
        onelevelEnum(1, "一级报警"),
        twolevelEnum(2, "二级报警"),
        threelevelEnum(3, "三级报警"),
        earlywarnEnum(10, "预警"),
        exceptionEnum(11, "异常报警");

        SecurityAlarmTypeEnum(Integer menucode, String menuname) {
            this.menucode = menucode;
            this.menuname = menuname;
        }

        private final Integer menucode;
        private final String menuname;

        public Integer getCode() {
            return menucode;
        }

        public String getName() {
            return menuname;
        }

        public static String getNameByCode(Integer Code) {
            for (SecurityAlarmTypeEnum enums : SecurityAlarmTypeEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.getName();
                }
            }
            return "";
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/12/15 0015 上午 9:18
     * @Description: 在线数据报警类型枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum alarmLevelEnum {
        overEnum(-1, ""),
        onelevelEnum(1, "一级"),
        twolevelEnum(2, "二级"),
        threelevelEnum(3, "三级");

        alarmLevelEnum(Integer menucode, String menuname) {
            this.menucode = menucode;
            this.menuname = menuname;
        }

        private final Integer menucode;
        private final String menuname;

        public Integer getCode() {
            return menucode;
        }

        public String getName() {
            return menuname;
        }

        public static String getNameByCode(Integer Code) {
            for (alarmLevelEnum enums : alarmLevelEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.getName();
                }
            }
            return "";
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/12/22 0022 下午 4:19
     * @Description: 水质标准代码枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum FunWaterQaulityClassEnum {
        QaulityOneEnum("1", "Ⅰ", "Ⅰ类"),
        QaulityTWOEnum("2", "Ⅱ", "Ⅱ类"),
        QaulityThreeEnum("3", "Ⅲ", "Ⅲ类"),
        QaulityFourEnum("4", "Ⅳ", "Ⅳ类"),
        QaulityFiveEnum("5", "Ⅴ", "Ⅴ类"),
        QaulitySixEnum("6", "劣Ⅴ", "劣Ⅴ类");

        FunWaterQaulityClassEnum(String menucode, String menuname, String value) {
            this.menucode = menucode;
            this.menuname = menuname;
            this.value = value;
        }

        private final String menucode;
        private final String menuname;
        private final String value;

        public String getCode() {
            return menucode;
        }

        public String getName() {
            return menuname;
        }

        public String getValue() {
            return value;
        }

        public static String getNameByCode(String Code) {
            for (FunWaterQaulityClassEnum enums : FunWaterQaulityClassEnum.values()) {
                if (enums.getCode().equals(Code)) {
                    return enums.getName();
                }
            }
            return "";
        }

        public static String getCodeByName(String name) {
            for (FunWaterQaulityClassEnum enums : FunWaterQaulityClassEnum.values()) {
                if (enums.menuname.equals(name)) {
                    return enums.menucode;
                }
            }
            return "";
        }

        public static String getCodeByValue(String value) {
            for (FunWaterQaulityClassEnum enums : FunWaterQaulityClassEnum.values()) {
                if (enums.getValue().equals(value)) {
                    return enums.getCode();
                }
            }
            return "-1";
        }

        public static String getNameByValue(String value) {
            for (FunWaterQaulityClassEnum enums : FunWaterQaulityClassEnum.values()) {
                if (enums.getValue().equals(value)) {
                    return enums.getName();
                }
            }
            return "";
        }

        public static String getVauleByCode(String code) {
            for (FunWaterQaulityClassEnum enums : FunWaterQaulityClassEnum.values()) {
                if (enums.getCode().equals(code)) {
                    return enums.getValue();
                }
            }
            return "";
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/12/15 0015 上午 9:21
     * @Description: 视频报警类型枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum SecurityVideoAlarmTypeEnum {
        onelevelEnum(1, "明火"),
        twolevelEnum(2, "未戴安全帽");

        SecurityVideoAlarmTypeEnum(Integer menucode, String menuname) {
            this.menucode = menucode;
            this.menuname = menuname;
        }

        private final Integer menucode;
        private final String menuname;

        public Integer getCode() {
            return menucode;
        }

        public String getName() {
            return menuname;
        }

        public static String getNameByCode(Integer Code) {
            for (SecurityVideoAlarmTypeEnum enums : SecurityVideoAlarmTypeEnum.values()) {
                if (enums.getCode() == Code) {
                    return enums.getName();
                }
            }
            return "";
        }
    }


    /**
     * @author: xsm
     * @date: 2021/07/14 0014 上午 08:46
     * @Description: 自定义巡查信息检查内容地址参数key
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public enum CheckContentUrlKeyEnum {
        pollutionidkeyEnum("pollutionid", "污染源ID"),
        pollutionnameEnum("pollutionname", "污染源名称");


        CheckContentUrlKeyEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private final String code;
        private final String name;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static String getNameByCode(String code) {
            for (CheckContentUrlKeyEnum transactType : values()) {
                if (transactType.getName().equals(code)) {
                    return transactType.getCode();
                }
            }
            return "";
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/14 0014 上午 9:24
     * @Description: 检查内容地址参数key集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<String> getCheckContentUrlKeyList() {
        return Arrays.asList(
                CheckContentUrlKeyEnum.pollutionidkeyEnum.getCode(),
                CheckContentUrlKeyEnum.pollutionnameEnum.getCode()
        );
    }

    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 17:25
     * @Description: 检查问题状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum ProblemManagementStatusEnum {
        AddProblemEnum(0, "新建"),
        RectifiedEnum(1, "待整改"),
        ReviewEnum(2, "待复查"),
        CompletedEnum(3, "已完成");


        ProblemManagementStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static String getNameByCode(Integer code) {
            for (ProblemManagementStatusEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType.name;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 17:53
     * @Description: 问题处置流程记录状态枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum ProblemProcedureRecordStatusEnum {
        RectifiedEnum(1, "待整改"),
        ReviewEnum(2, "待复查"),
        RectifiedRejectEnum(3, "整改驳回"),
        CompletedEnum(4, "已完成");

        ProblemProcedureRecordStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private final Integer code;
        private final String name;

        public String getName() {
            return name;
        }

        public Integer getCode() {
            return code;
        }

        public static String getNameByCode(Integer code) {
            for (ProblemProcedureRecordStatusEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType.name;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/1/11 0011 下午 2:15
     * @Description: 监测类别枚举
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public enum MonitoringClassEnum {

        PollutionMonitorEnum(1, "污染源监测"),

        AirMonitorEnum(2, "大气监测"),

        WaterQualityMonitorEnum(3, "水质监测");

        MonitoringClassEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }

        public static String getNameByCode(Integer code) {
            for (MonitoringClassEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType.name;
                }
            }
            return null;
        }
    }

    /**
     * @Description: 排污单位记录内容类型
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/12 9:44
     */
    public enum BlowdownUnitBaseEnum {
        yuanliaoEnum(1, "主要原料用量"),
        fuliaoEnum(2, "主要辅料用量"),
        nengyuanEnum(3, "能源消耗"),
        shengchanEnum(4, "生产规模"),
        yunxingshijianEnum(5, "运行时间和生产负荷"),
        chanpinEnum(6, "主要产品产量"),
        qupaishuiEnum(7, "取排水"),
        touziEnum(8, "污染治理设施计划投资情况");

        BlowdownUnitBaseEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }

        public static String getNameByCode(Integer code) {
            for (BlowdownUnitBaseEnum transactType : values()) {
                if (transactType.getCode() == code) {
                    return transactType.name;
                }
            }
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/13 10:16
     * @Description: 监测管理菜单枚举（分级预警、报警任务）
     * @param:
     * @return:
     */
    public enum PcHBMonitorManagementMenus {
        WaterFlowChangeEarlyWarn("WaterFlowChangeEarlyWarn", "废水排放量突变预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.FlowChangeEnum.getCode()),
        GasFlowChangeEarlyWarn("GasFlowChangeEarlyWarn", "废气排放量突变预警", WasteGasEnum.getCode(), RemindTypeEnum.FlowChangeEnum.getCode()),
        SmokeFlowChangeEarlyWarn("SmokeFlowChangeEarlyWarn", "烟气排放量突变预警", SmokeEnum.getCode(), RemindTypeEnum.FlowChangeEnum.getCode()),

        wasteWaterSuddenChangeWarn("wasteWaterSuddenChangeWarn", "废水浓度突变预警", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        wasteGasSuddenChangeWarn("wasteGasSuddenChangeWarn", "废气浓度突变预警", WasteGasEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        smokeSuddenChangeWarn("smokeSuddenChangeWarn", "烟气浓度突变预警", SmokeEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        rainSuddenChangeWarn("rainSuddenChangeWarn", "雨水浓度突变预警", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        factoryStinkSuddenChangeWarn("factoryStinkSuddenChangeWarn", "厂界恶臭浓度突变预警", MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        factorySmallStationSuddenChangeWarn("factorySmallStationSuddenChangeWarn", "厂界小型站浓度突变预警", MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        airSuddenChangeWarn("airSuddenChangeWarn", "空气浓度突变预警", MonitorPointTypeEnum.AirEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        stinkSuddenChangeWarn("stinkSuddenChangeWarn", "恶臭浓度突变预警", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        vocSuddenChangeWarn("vocSuddenChangeWarn", "VOC浓度突变预警", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        raiseDustSuddenChangeWarn("raiseDustSuddenChangeWarn", "扬尘突变预警", MonitorPointTypeEnum.EnvironmentalDustEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode());


        PcHBMonitorManagementMenus(String code, String name, Integer monitorPointType, Integer remindType) {
            this.code = code;
            this.name = name;
            this.monitorPointType = monitorPointType;
            this.remindType = remindType;
        }

        private final String code;
        private final String name;
        private final Integer monitorPointType;
        private final Integer remindType;

        public Integer getRemindType() {
            return remindType;
        }

        public Integer getMonitorPointType() {
            return monitorPointType;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public static PcHBMonitorManagementMenus getCodeByString(String code) {
            for (PcHBMonitorManagementMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        public static PcHBMonitorManagementMenus getEnumByParam(Integer monitorPointType, Integer remindType) {
            PcHBMonitorManagementMenus[] values = PcHBMonitorManagementMenus.values();
            for (PcHBMonitorManagementMenus value : values) {
                if (value.getRemindType().equals(remindType) && value.getMonitorPointType().equals(monitorPointType)) {
                    return value;
                }
            }
            return null;
        }

        public static String getNameByString(String code) {
            for (PcHBMonitorManagementMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getName();
                }
            }
            return null;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/02/11 16:01
     * @Description: 监测管理报警统计菜单枚举(2.0版本使用)
     * @param:
     * @return:
     */
    public enum MonitorManagementAlarmMenus {
        //废气
        flueGasSuddenConcentrationWarning("flueGasSuddenConcentrationWarning", "废气监测管理-浓度突变预警", "flueGasMonitoringManagement", MonitorPointTypeEnum.WasteGasEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        flueGasOverstandardGradedWarning("flueGasOverstandardGradedWarning", "废气监测管理-超标分级预警", "flueGasMonitoringManagement", MonitorPointTypeEnum.WasteGasEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        flueGasEarlystandardGradedWarning("flueGasEarlystandardGradedWarning", "废气监测管理-超阈值分级预警", "flueGasMonitoringManagement", MonitorPointTypeEnum.WasteGasEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        flueGasAbnormalDataAlarm("flueGasAbnormalDataAlarm", "废气监测管理-数据异常报警", "flueGasMonitoringManagement", MonitorPointTypeEnum.WasteGasEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        flueGasAlarmStatisticsList("flueGasAlarmStatisticsList", "废气监测管理-报警统计清单", "flueGasMonitoringManagement", MonitorPointTypeEnum.WasteGasEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        flueGasAlarmDisposalWorkOrder("flueGasAlarmDisposalWorkOrder", "废气监测管理-报警处置工单", "flueGasMonitoringManagement", MonitorPointTypeEnum.WasteGasEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        flueGasDevOpsDisposalWorkOrder("flueGasDevOpsDisposalWorkOrder", "废气监测管理-异常处置工单", "flueGasMonitoringManagement", MonitorPointTypeEnum.WasteGasEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        flueGasTBDisposalWorkOrder("flueGasTBDisposalWorkOrder", "废气监测管理-突变处置工单", "flueGasMonitoringManagement", MonitorPointTypeEnum.WasteGasEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),



        //烟气
        smokeSuddenConcentrationWarning("smokeSuddenConcentrationWarning", "烟气监测管理-浓度突变预警", "smokeMonitoringManagement", MonitorPointTypeEnum.SmokeEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        smokeOverstandardGradedWarning("smokeOverstandardGradedWarning", "烟气监测管理-超标分级预警", "smokeMonitoringManagement", MonitorPointTypeEnum.SmokeEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        smokeEarlystandardGradedWarning("smokeEarlystandardGradedWarning", "烟气监测管理-超阈值分级预警", "smokeMonitoringManagement", MonitorPointTypeEnum.SmokeEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        smokeAbnormalDataAlarm("smokeAbnormalDataAlarm", "烟气监测管理-数据异常报警", "smokeMonitoringManagement", MonitorPointTypeEnum.SmokeEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        smokeAlarmStatisticsList("smokeAlarmStatisticsList", "烟气监测管理-报警统计清单", "smokeMonitoringManagement", MonitorPointTypeEnum.SmokeEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        smokeAlarmDisposalWorkOrder("smokeAlarmDisposalWorkOrder", "烟气监测管理-报警处置工单", "smokeMonitoringManagement", MonitorPointTypeEnum.SmokeEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        smokeDevOpsDisposalWorkOrder("smokeDevOpsDisposalWorkOrder", "烟气监测管理-异常处置工单", "smokeMonitoringManagement", MonitorPointTypeEnum.SmokeEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        smokeTBDisposalWorkOrder("smokeTBDisposalWorkOrder", "烟气监测管理-突变处置工单", "smokeMonitoringManagement", MonitorPointTypeEnum.SmokeEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),



        //废气烟气合并
        gasSmokeSuddenConcentrationWarning("gasSmokeSuddenConcentrationWarning", "废气监测管理-浓度突变预警", "gasSmokeMonitoringManagement", MonitorPointTypeEnum.SmokeGasEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        gasSmokeOverstandardGradedWarning("gasSmokeOverstandardGradedWarning", "废气监测管理-超标分级预警", "gasSmokeMonitoringManagement", MonitorPointTypeEnum.SmokeGasEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        gasSmokeEarlystandardGradedWarning("gasSmokeEarlystandardGradedWarning", "废气监测管理-超阈值分级预警", "gasSmokeMonitoringManagement", MonitorPointTypeEnum.SmokeGasEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        gasSmokeAbnormalDataAlarm("gasSmokeAbnormalDataAlarm", "废气监测管理-数据异常报警", "gasSmokeMonitoringManagement", MonitorPointTypeEnum.SmokeGasEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        gasSmokeAlarmStatisticsList("gasSmokeAlarmStatisticsList", "废气监测管理-报警统计清单", "gasSmokeMonitoringManagement", MonitorPointTypeEnum.SmokeGasEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        gasSmokeAlarmDisposalWorkOrder("gasSmokeAlarmDisposalWorkOrder", "废气监测管理-报警处置工单", "gasSmokeMonitoringManagement", MonitorPointTypeEnum.SmokeGasEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        gasSmokeDevOpsDisposalWorkOrder("gasSmokeDevOpsDisposalWorkOrder", "废气监测管理-异常处置工单", "gasSmokeMonitoringManagement", MonitorPointTypeEnum.SmokeGasEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        gasSmokeTBDisposalWorkOrder("gasSmokeTBDisposalWorkOrder", "废气监测管理-突变处置工单", "gasSmokeMonitoringManagement", MonitorPointTypeEnum.SmokeGasEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),


        //废水
        wasteWaterSuddenConcentrationWarning("wasteWaterSuddenConcentrationWarning", "废水监测管理-浓度突变预警", "wasteWaterMonitoringManagement", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        wasteWaterOverstandardGradedWarning("wasteWaterOverstandardGradedWarning", "废水监测管理-超标分级预警", "wasteWaterMonitoringManagement", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        wasteWaterEarlystandardGradedWarning("wasteWaterEarlystandardGradedWarning", "废水监测管理-超阈值分级预警", "wasteWaterMonitoringManagement", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        wasteWaterAbnormalDataAlarm("wasteWaterAbnormalDataAlarm", "废水监测管理-数据异常报警", "wasteWaterMonitoringManagement", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        wasteWaterAlarmStatisticsList("wasteWaterAlarmStatisticsList", "废水监测管理-报警统计清单", "wasteWaterMonitoringManagement", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        wasteWaterAlarmDisposalWorkOrder("wasteWaterAlarmDisposalWorkOrder", "废水监测管理-报警处置工单", "wasteWaterMonitoringManagement", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        wasteWaterDevOpsDisposalWorkOrder("wasteWaterDevOpsDisposalWorkOrder", "废水监测管理-异常处置工单", "wasteWaterDevOpsDisposalWorkOrder", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        wasteWaterTBDisposalWorkOrder("wasteWaterTBDisposalWorkOrder", "废水监测管理-突变处置工单", "wasteWaterTBDisposalWorkOrder", MonitorPointTypeEnum.WasteWaterEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),
        //雨水
        rainWaterSuddenConcentrationWarning("rainWaterSuddenConcentrationWarning", "雨水监测管理-浓度突变预警", "rainWaterMonitoringManagement", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        rainWaterOverstandardGradedWarning("rainWaterOverstandardGradedWarning", "雨水监测管理-超标分级预警", "rainWaterMonitoringManagement", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        rainWaterEarlystandardGradedWarning("rainWaterEarlystandardGradedWarning", "雨水监测管理-超阈值分级预警", "rainWaterMonitoringManagement", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        rainWaterAbnormalDataAlarm("rainWaterAbnormalDataAlarm", "雨水监测管理-数据异常报警", "rainWaterMonitoringManagement", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        rainWaterAlarmStatisticsList("rainWaterAlarmStatisticsList", "雨水监测管理-报警统计清单", "rainWaterMonitoringManagement", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        rainWaterAlarmDisposalWorkOrder("rainWaterAlarmDisposalWorkOrder", "雨水监测管理-报警处置工单", "rainWaterMonitoringManagement", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        rainWaterDevOpsDisposalWorkOrder("rainWaterDevOpsDisposalWorkOrder", "雨水监测管理-异常处置工单", "rainWaterMonitoringManagement", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        rainWaterTBDisposalWorkOrder("rainWaterTBDisposalWorkOrder", "雨水监测管理-突变处置工单", "rainWaterMonitoringManagement", MonitorPointTypeEnum.RainEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),
        //恶臭
        stinkSuddenConcentrationWarning("stinkSuddenConcentrationWarning", "恶臭监测管理-浓度突变预警", "stinkMonitoringManagement", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        stinkOverstandardGradedWarning("stinkOverstandardGradedWarning", "恶臭监测管理-超标分级预警", "stinkMonitoringManagement", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        stinkEarlystandardGradedWarning("stinkEarlystandardGradedWarning", "恶臭监测管理-超阈值分级预警", "stinkMonitoringManagement", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        stinkAbnormalDataAlarm("stinkAbnormalDataAlarm", "恶臭监测管理-数据异常报警", "stinkMonitoringManagement", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        stinkAlarmStatisticsList("stinkAlarmStatisticsList", "恶臭监测管理-报警统计清单", "stinkMonitoringManagement", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        stinkAlarmDisposalWorkOrder("stinkAlarmDisposalWorkOrder", "恶臭监测管理-报警处置工单", "stinkMonitoringManagement", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        stinkDevOpsDisposalWorkOrder("stinkDevOpsDisposalWorkOrder", "恶臭监测管理-异常处置工单", "stinkMonitoringManagement", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        stinkTBDisposalWorkOrder("stinkTBDisposalWorkOrder", "恶臭监测管理-突变处置工单", "stinkMonitoringManagement", MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),
        //VOC
        vocSuddenConcentrationWarning("vocSuddenConcentrationWarning", "VOC监测管理-浓度突变预警", "vocMonitoringManagement", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        vocOverstandardGradedWarning("vocOverstandardGradedWarning", "VOC废气监测管理-超标分级预警", "vocMonitoringManagement", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        vocEarlystandardGradedWarning("vocEarlystandardGradedWarning", "VOC废气监测管理-超阈值分级预警", "vocMonitoringManagement", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        vocAbnormalDataAlarm("vocAbnormalDataAlarm", "VOC废气监测管理-数据异常报警", "vocMonitoringManagement", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        vocAlarmStatisticsList("vocAlarmStatisticsList", "VOC废气监测管理-报警统计清单", "vocMonitoringManagement", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        vocAlarmDisposalWorkOrder("vocAlarmDisposalWorkOrder", "VOC废气监测管理-报警处置工单", "vocMonitoringManagement", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        vocDevOpsDisposalWorkOrder("vocDevOpsDisposalWorkOrder", "VOC废气监测管理-异常处置工单", "vocMonitoringManagement", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        vocTBDisposalWorkOrder("vocTBDisposalWorkOrder", "VOC废气监测管理-突变处置工单", "vocMonitoringManagement", MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),


        //颗粒物
        particulateSuddenConcentrationWarning("particulateSuddenConcentrationWarning", "颗粒物监测管理-浓度突变预警", "particulateMonitoringManagement", XKLWEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        particulateOverstandardGradedWarning("particulateOverstandardGradedWarning", "颗粒物监测管理-超标分级预警", "particulateMonitoringManagement", XKLWEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        particulateEarlystandardGradedWarning("particulateEarlystandardGradedWarning", "颗粒物监测管理-超阈值分级预警", "particulateMonitoringManagement", XKLWEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        particulateAbnormalDataAlarm("particulateAbnormalDataAlarm", "颗粒物监测管理-数据异常报警", "particulateMonitoringManagement", XKLWEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        particulateAlarmStatisticsList("particulateAlarmStatisticsList", "颗粒物监测管理-报警统计清单", "particulateMonitoringManagement", XKLWEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        particulateAlarmDisposalWorkOrder("particulateAlarmDisposalWorkOrder", "颗粒物监测管理-报警处置工单", "particulateMonitoringManagement", XKLWEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        klwDevOpsDisposalWorkOrder("klwDevOpsDisposalWorkOrder", "颗粒物监测管理-异常处置工单", "particulateMonitoringManagement", XKLWEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        klwTBDisposalWorkOrder("klwTBDisposalWorkOrder", "颗粒物监测管理-突变处置工单", "particulateMonitoringManagement", XKLWEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),

        //碳组分
        carbonFractionSuddenConcentrationWarning("carbonFractionSuddenConcentrationWarning", "碳组分监测管理-浓度突变预警", "particulateMonitoringManagement", TZFEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        carbonFractionOverstandardGradedWarning("carbonFractionOverstandardGradedWarning", "碳组分监测管理-超标分级预警", "particulateMonitoringManagement", TZFEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        carbonFractionEarlystandardGradedWarning("carbonFractionEarlystandardGradedWarning", "碳组分监测管理-超阈值分级预警", "particulateMonitoringManagement", TZFEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        carbonFractionAbnormalDataAlarm("carbonFractionAbnormalDataAlarm", "碳组分监测管理-数据异常报警", "particulateMonitoringManagement", TZFEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        carbonFractionAlarmStatisticsList("carbonFractionAlarmStatisticsList", "碳组分监测管理-报警统计清单", "particulateMonitoringManagement", TZFEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        carbonFractionAlarmDisposalWorkOrder("carbonFractionAlarmDisposalWorkOrder", "碳组分监测管理-报警处置工单", "particulateMonitoringManagement", TZFEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        tzfDevOpsDisposalWorkOrder("tzfDevOpsDisposalWorkOrder", "碳组分监测管理-异常处置工单", "carbonFractionMonitoringManagement", TZFEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        tzfTBDisposalWorkOrder("tzfTBDisposalWorkOrder", "碳组分监测管理-突变处置工单", "carbonFractionMonitoringManagement", TZFEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),


        //无组织废气
        wasteGasDisorganizationSuddenConcentrationWarning("wasteGasDisorganizationSuddenConcentrationWarning", "无组织废气监测管理-浓度突变预警", "wasteGasDisorganizationMonitoringManagement", unOrgGasEnum.getCode(), RemindTypeEnum.ConcentrationChangeEnum.getCode()),
        wasteGasDisorganizationOverstandardGradedWarning("wasteGasDisorganizationOverstandardGradedWarning", "无组织废气监测管理-超标分级预警", "wasteGasDisorganizationMonitoringManagement", unOrgGasEnum.getCode(), RemindTypeEnum.OverAlarmEnum2.getCode()),
        wasteGasDisorganizationEarlystandardGradedWarning("wasteGasDisorganizationEarlystandardGradedWarning", "无组织废气监测管理-超阈值分级预警", "wasteGasDisorganizationMonitoringManagement", unOrgGasEnum.getCode(), RemindTypeEnum.EarlyAlarmEnum2.getCode()),
        wasteGasDisorganizationAbnormalDataAlarm("wasteGasDisorganizationAbnormalDataAlarm", "无组织废气监测管理-数据异常报警", "wasteGasDisorganizationMonitoringManagement", unOrgGasEnum.getCode(), RemindTypeEnum.ExceptionAlarmEnum2.getCode()),
        wasteGasDisorganizationAlarmStatisticsList("wasteGasDisorganizationAlarmStatisticsList", "无组织废气监测管理-报警统计清单", "wasteGasDisorganizationMonitoringManagement", unOrgGasEnum.getCode(), RemindTypeEnum.AlarmCountListEnum.getCode()),
        wasteGasDisorganizationAlarmDisposalWorkOrder("wasteGasDisorganizationAlarmDisposalWorkOrder", "无组织废气监测管理-报警处置工单", "wasteGasDisorganizationMonitoringManagement", unOrgGasEnum.getCode(), RemindTypeEnum.AlarmWorkOrderEnum.getCode()),
        nogasDevOpsDisposalWorkOrder("nogasDevOpsDisposalWorkOrder", "无组织废气监测管理-异常处置工单", "wasteGasDisorganizationMonitoringManagement", unOrgGasEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        nogasTBDisposalWorkOrder("nogasTBDisposalWorkOrder", "无组织废气监测管理-突变处置工单", "wasteGasDisorganizationMonitoringManagement", unOrgGasEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),


        //常规空气
        airDevOpsDisposalWorkOrder("airDevOpsDisposalWorkOrder", "常规空气监测管理-异常处置工单", "airMonitoringManagement", AirEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        airTBDisposalWorkOrder("airTBDisposalWorkOrder", "常规空气监测管理-突变处置工单", "airMonitoringManagement", AirEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode()),

        //水质
        szDevOpsDisposalWorkOrder("szDevOpsDisposalWorkOrder", "水质监测管理-异常处置工单", "waterQualityMonitoringManagement", WaterQualityEnum.getCode(), RemindTypeEnum.DevOpsWorkOrderEnum.getCode()),
        szTBDisposalWorkOrder("szTBDisposalWorkOrder", "水质监测管理-突变处置工单", "waterQualityMonitoringManagement", WaterQualityEnum.getCode(), RemindTypeEnum.TBWorkOrderEnum.getCode())

        ;


        MonitorManagementAlarmMenus(String code, String name, String parentcode, Integer monitorPointType, Integer remindType) {
            this.code = code;
            this.name = name;
            this.parentcode = parentcode;
            this.monitorPointType = monitorPointType;
            this.remindType = remindType;
        }

        private final String code;
        private final String name;
        private final String parentcode;
        private final Integer monitorPointType;
        private final Integer remindType;

        public Integer getRemindType() {
            return remindType;
        }

        public Integer getMonitorPointType() {
            return monitorPointType;
        }

        public String getName() {
            return name;
        }

        public String getParentCode() {
            return parentcode;
        }

        public String getCode() {
            return code;
        }

        public static MonitorManagementAlarmMenus getCodeByString(String code) {
            for (MonitorManagementAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType;
                }
            }
            return null;
        }

        public static MonitorManagementAlarmMenus getEnumByParam(Integer monitorPointType, Integer remindType) {
            MonitorManagementAlarmMenus[] values = MonitorManagementAlarmMenus.values();
            for (MonitorManagementAlarmMenus value : values) {
                if (value.getRemindType().equals(remindType) && value.getMonitorPointType().equals(monitorPointType)) {
                    return value;
                }
            }
            return null;
        }

        public static String getNameByString(String code) {
            for (MonitorManagementAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getName();
                }
            }
            return null;
        }

        public static String getParentCodeByString(String code) {
            for (MonitorManagementAlarmMenus transactType : values()) {
                if (transactType.getCode().equals(code)) {
                    return transactType.getParentCode();
                }
            }
            return null;
        }
    }
}

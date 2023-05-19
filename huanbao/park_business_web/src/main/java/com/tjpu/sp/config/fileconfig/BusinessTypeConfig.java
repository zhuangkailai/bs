package com.tjpu.sp.config.fileconfig;


import java.util.HashMap;
import java.util.Map;

/**
 * @author: lip
 * @date: 2018/8/31 0031 下午 4:27
 * @Description:文件上传到mongodb的文件类型配，对应mongodb表前缀
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
public class BusinessTypeConfig {


    /**
     * 文件类型map
     */
    public static Map<String, String> businessTypeMap = new HashMap<String, String>();


    /**
     *
     * @author: lip
     * @date: 2018/8/31 0031 下午 4:29
     * @Description: 静态代码块，初始化code对应表前缀
     */
    static {
        //污染源
        businessTypeMap.put("1", "pollutionFile");
        //项目验收
        businessTypeMap.put("2", "projectCheckFile");
        //项目审批
        businessTypeMap.put("3", "projectApprovalFile");
        //通知公告
        businessTypeMap.put("4", "noticeFile");
        //排污许可证
        businessTypeMap.put("5", "pwxkzFile");
        //危废许可证
        businessTypeMap.put("6", "wxfwFile");
        //辐射安全许可证
        businessTypeMap.put("7", "hyfsFile");
        //监察执法
        businessTypeMap.put("8", "jczfFile");
        //行政处罚
        businessTypeMap.put("9", "xzcfFile");
        //应急预案
        businessTypeMap.put("10", "yjyaFile");
        //清洁生产
        businessTypeMap.put("11", "qjscFile");
        //应急事件
        businessTypeMap.put("12", "yjsjFile");
        //知识库
        businessTypeMap.put("13", "zskFile");
        //app版本文件
        businessTypeMap.put("14", "appVersionFile");
        //信用评价
        businessTypeMap.put("15", "xypjFile");
        //安全生产年度报告
        businessTypeMap.put("16", "aqscndbgFile");
        //安全管理制度
        businessTypeMap.put("17", "aqgzzdFile");
        //安全-特种设备
        businessTypeMap.put("18", "aqtzsbFile");
        //安全-主要建筑构造物
        businessTypeMap.put("19", "aqzyjzgzwFile");
        //安全-生产事故
        businessTypeMap.put("20", "aqscsgFile");
        //隐患
        businessTypeMap.put("21", "aqyhFile");
        //危险点
        businessTypeMap.put("22", "aqwxdFile");
        //危化品
        businessTypeMap.put("23", "aqwhpFile");
        //分析报告
        businessTypeMap.put("24", "fxbgFile");
        //土壤
        businessTypeMap.put("25", "soilFile");
        //停产信息
        businessTypeMap.put("26", "stopproductionFile");
        //隐患排查任务
        businessTypeMap.put("27", "hiddendangertaskFile");
        //视频
        businessTypeMap.put("28", "videoFile");
        //系统帮助图片说明
        businessTypeMap.put("29", "captionFile");
        //问题数据
        businessTypeMap.put("30", "probledata");
        //企业复产
        businessTypeMap.put("31", "productionFile");
        //监测点监测控制文件
        businessTypeMap.put("32", "monitorControlFile");
        //典型投诉库文件
        businessTypeMap.put("33", "typicalComplaintBaseFile");
        //应急物资文件
        businessTypeMap.put("34", "emergencySuppliesFile");
        //指标库证明文件
        businessTypeMap.put("35", "evaluationIndexFile");
        //app反馈意见附件
        businessTypeMap.put("36", "appSuggestionFile");
        //环保管家规章制度附件
        businessTypeMap.put("37", "ruleInfoFile");
        //环保管家执行报告附件
        businessTypeMap.put("38", "executeReportFile");
        //环保管家手工监测附件
        businessTypeMap.put("39", "manualReportFile");
        //检查模板配置附件
        businessTypeMap.put("50", "checkTemplateConfigFile");
        //检查问题说明附件
        businessTypeMap.put("51", "checkProblemExpoundFile");
        //企业问题整改报表附件
        businessTypeMap.put("52", "entProblemRectifyReportFile");
        //企业问题整改报表附件
        businessTypeMap.put("53", "voucherRecordFile");
        //问题整改记录附件
        businessTypeMap.put("54", "ProblemErectifiedFile");
        //问题复查记录附件
        businessTypeMap.put("55", "ProblemReviewFile");
        //企业台账报告记录附件
        businessTypeMap.put("56", "entTZFile");
        //溯源结果附件
        businessTypeMap.put("57", "SYJGFile");
        /**
         * 57 - 70 中间有部分安全类型
         * */
        //运维记录附件
        businessTypeMap.put("70", "devopsFile");
        //证明材料
        businessTypeMap.put("71", "entevaluationFile");

        //企业检查考核附近
        businessTypeMap.put("72", "entAssessFile");
    }


}

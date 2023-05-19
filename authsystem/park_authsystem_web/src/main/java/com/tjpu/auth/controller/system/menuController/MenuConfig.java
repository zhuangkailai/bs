package com.tjpu.auth.controller.system.menuController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author: lip
 * @date: 2018年8月9日 上午11:01:13
 * @Description:统计菜单记录数的相关配置
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
public class MenuConfig {
	/**key:menucode,value:tablename+fk_id **/
	public static Map<String, List<Map<String,Object>>> menuMap =  new HashMap<>();

	
	/** 初始化集合 **/
	static {
		//废水直接排口
		List<Map<String,Object>> waterDirectOutPutInfo = new ArrayList<>();
		Map<String,Object> waterOutPutMap = new HashMap<>();

		String waterDirectOutpubTableName = "T_BAS_WaterOutputInfo t1 " +
				"JOIN PUB_CODE_DrainDirection t2 ON t1.FK_DrainDirection = t2.CODE ";
		waterOutPutMap.put("tablename",waterDirectOutpubTableName);
		waterOutPutMap.put("field","t1.PK_ID");
		waterOutPutMap.put("fk_id","t1.FK_Pollutionid");
		waterOutPutMap.put("whereString","(t1.OutputType !='3' or t1.OutputType is null ) AND t2.DirectorIndirect = '1' ");
		waterDirectOutPutInfo.add(waterOutPutMap);
		menuMap.put("waterdirectOne",waterDirectOutPutInfo);




		//废水间接排口
		List<Map<String,Object>> waterInDirectOutPutInfo = new ArrayList<>();
		Map<String,Object> waterInDirectOutPutMap = new HashMap<>();
		String waterInDirectOutpubTableName = "T_BAS_WaterOutputInfo t1 " +
				"JOIN PUB_CODE_DrainDirection t2 ON t1.FK_DrainDirection = t2.CODE ";
		waterInDirectOutPutMap.put("tablename",waterInDirectOutpubTableName);
		waterInDirectOutPutMap.put("field","t1.PK_ID");
		waterInDirectOutPutMap.put("fk_id","t1.FK_Pollutionid");
		waterInDirectOutPutMap.put("whereString","(t1.OutputType !='3' or t1.OutputType is null ) AND t2.DirectorIndirect = '2' ");
		waterInDirectOutPutInfo.add(waterInDirectOutPutMap);
		menuMap.put("waterindirectOne",waterInDirectOutPutInfo);


		//雨水排口
		List<Map<String,Object>> rainOutPutInfo = new ArrayList<>();
		Map<String,Object> rainOutPutMap = new HashMap<>();
		rainOutPutMap.put("tablename","T_BAS_WaterOutputInfo");
		rainOutPutMap.put("field","PK_ID");
		rainOutPutMap.put("fk_id","FK_Pollutionid");
		rainOutPutMap.put("whereString","OutputType ='3'");
		rainOutPutInfo.add(rainOutPutMap);
		menuMap.put("rainOutPutInfo",rainOutPutInfo);


		//废气排口
		List<Map<String,Object>> gasOutPutInfo = new ArrayList<>();
		Map<String,Object> gasOutPutMap = new HashMap<>();
		gasOutPutMap.put("tablename","T_BAS_GasOutputInfo");
		gasOutPutMap.put("field","PK_ID");
		gasOutPutMap.put("fk_id","FK_Pollutionid");
		gasOutPutInfo.add(gasOutPutMap);
		menuMap.put("gasOutPutInfo",gasOutPutInfo);

		//废气无组织排口
		List<Map<String,Object>> unorganizedMonitorPoint = new ArrayList<>();
		Map<String,Object> unorganizedMonitorPointMap = new HashMap<>();
		unorganizedMonitorPointMap.put("tablename","T_BAS_UnorganizedMonitorPointInfo");
		unorganizedMonitorPointMap.put("fk_id","FK_Pollutionid");
		unorganizedMonitorPointMap.put("field","PK_ID");
		unorganizedMonitorPoint.add(unorganizedMonitorPointMap);
		menuMap.put("gasFugitiveEmissions",unorganizedMonitorPoint);


		//噪声监测点
		List<Map<String,Object>> noiseMonitorPoint = new ArrayList<>();
		Map<String,Object> noiseMonitorPointMap = new HashMap<>();
		noiseMonitorPointMap.put("tablename","T_BAS_NoiseMonitorPoint");
		noiseMonitorPointMap.put("field","PK_NoisePointID");
		noiseMonitorPointMap.put("fk_id","FK_PollutionID");
		noiseMonitorPoint.add(noiseMonitorPointMap);
		menuMap.put("noiseMonitorPoint",noiseMonitorPoint);

		//建设项目审批
		List<Map<String,Object>> projectApproval = new ArrayList<>();
		Map<String,Object> projectApprovalMap = new HashMap<>();
		projectApprovalMap.put("field","PK_ApprovalID");
		projectApprovalMap.put("tablename","T_PROJECT_Approval");
		projectApprovalMap.put("fk_id","FK_PollutionID");
		projectApproval.add(projectApprovalMap);
		menuMap.put("projectApproval",projectApproval);


		//建设项目验收
		List<Map<String,Object>> projectCheck = new ArrayList<>();
		Map<String,Object> projectCheckMap = new HashMap<>();
		projectCheckMap.put("tablename","T_PROJECT_Check");
		projectCheckMap.put("field","PK_CheckID");
		projectCheckMap.put("fk_id","FK_Pollutionid");
		projectCheck.add(projectCheckMap);
		menuMap.put("projectCheck",projectCheck);

		//排污许可证
		List<Map<String,Object>> PWXKZList = new ArrayList<>();
		Map<String,Object> PWXKZMap = new HashMap<>();
		PWXKZMap.put("tablename","T_PWXKZ_LicenceInfo");
		PWXKZMap.put("field","PK_LicenceId");
		PWXKZMap.put("fk_id","FK_PollutionId");
		PWXKZList.add(PWXKZMap);
		menuMap.put("dischargePermit",PWXKZList);

		//危废许可证
		List<Map<String,Object>> WFXKZList = new ArrayList<>();
		Map<String,Object> WFXKZMap = new HashMap<>();
		WFXKZMap.put("tablename","T_WXFW_LicenseInfo");
		WFXKZMap.put("field","PK_LicenceId");
		WFXKZMap.put("fk_id","FK_PollutionId");
		WFXKZList.add(WFXKZMap);
		menuMap.put("hazardousWasteLic",WFXKZList);

		//危废转移联单
		List<Map<String,Object>> WFXKTransferList = new ArrayList<>();
		Map<String,Object> WFXKTransferMap = new HashMap<>();
		WFXKTransferMap.put("tablename","T_WXFW_TransferList");
		WFXKTransferMap.put("field","PK_ID");
		WFXKTransferMap.put("fk_id","FK_ProductentID");
		WFXKTransferList.add(WFXKTransferMap);
		menuMap.put("transferringTable",WFXKTransferList);


		//辐射许可证
		List<Map<String,Object>> FSLicenseInfoList = new ArrayList<>();
		Map<String,Object> FSLicenseInfoMap = new HashMap<>();
		FSLicenseInfoMap.put("tablename","T_HYFS_LicenseInfo");
		FSLicenseInfoMap.put("field","PK_LicenceID");
		FSLicenseInfoMap.put("fk_id","FK_PollutionID");
		FSLicenseInfoList.add(FSLicenseInfoMap);
		menuMap.put("radiationSafetyLic",FSLicenseInfoList);


		//环保税申请表
		String HBS = "( SELECT t1.PK_DataID,t1.FK_PollutionID FROM T_HBS_TaxInfo t1,T_BAS_WaterOutputInfo t2,PUB_CODE_TaxPollutant t4"
				+" WHERE t1.FK_OutPutID = t2.PK_ID AND  t1.FK_TaxType = '101' AND ( t2.OutputType IS NOT NULL OR t2.OutputType != '3' )"
				+" AND t1.FK_TaxPollutant = t4.code AND t1.FK_TaxType = t4.FK_TaxType"
				+" UNION"
				+" SELECT t1.PK_DataID,t1.FK_PollutionID FROM T_HBS_TaxInfo t1,T_BAS_GASOutPutInfo t2,PUB_CODE_TaxPollutant t4"
				+" WHERE t1.FK_OutPutID = t2.PK_ID AND t1.FK_TaxType = '201' AND t1.FK_TaxPollutant = t4.code AND t1.FK_TaxType = t4.FK_TaxType"
				+" UNION"
				+" SELECT t1.PK_DataID,t1.FK_PollutionID FROM T_HBS_TaxInfo t1,T_BAS_NoiseMonitorPoint t2,PUB_CODE_TaxPollutant t4"
				+" WHERE t1.FK_OutPutID = t2.PK_NoisePointID AND t1.FK_TaxPollutant = t4.code AND t1.FK_TaxType = t4.FK_TaxType"
				+" UNION SELECT t1.PK_DataID,t1.FK_PollutionID FROM T_HBS_TaxInfo t1"
				+" WHERE t1.FK_TaxType IN ('401','402','403','404','405','406','407')) t ";
		List<Map<String,Object>> HBSList = new ArrayList<>();
		Map<String,Object> HBSMap = new HashMap<>();
		HBSMap.put("tablename",HBS);
		HBSMap.put("field","T.PK_DataID");
		HBSMap.put("fk_id","T.FK_PollutionID");
		HBSList.add(HBSMap);
		menuMap.put("envTaxApplication",HBSList);


		//环保税-大气污染物


		String gasHBSTableName = "(SELECT t1.PK_DataID,t1.FK_PollutionID FROM T_HBS_TaxInfo t1,T_BAS_GASOutPutInfo t2,PUB_CODE_TaxPollutant t4"+
				" WHERE t1.FK_OutPutID = t2.PK_ID AND t1.FK_TaxType = '201' AND t1.FK_TaxPollutant = t4.code AND t1.FK_TaxType = t4.FK_TaxType) t";
		List<Map<String,Object>> HBSGasList = new ArrayList<>();
		Map<String,Object> HBSGasMap = new HashMap<>();
		HBSGasMap.put("tablename",gasHBSTableName);
		HBSGasMap.put("field","t.PK_DataID");
		HBSGasMap.put("fk_id","t.FK_PollutionID");
		HBSGasList.add(HBSGasMap);
		menuMap.put("airPollutantDeclare",HBSGasList);

		//环保税-水污染物

		String waterHBSTableName = "(SELECT t1.PK_DataID,t1.FK_PollutionID FROM T_HBS_TaxInfo t1,T_BAS_WaterOutputInfo t2,PUB_CODE_TaxPollutant t4"+
				" WHERE t1.FK_OutPutID = t2.PK_ID AND  t1.FK_TaxType = '101' AND ( t2.OutputType IS NOT NULL OR t2.OutputType != '3' )"+
				" AND t1.FK_TaxPollutant = t4.code AND t1.FK_TaxType = t4.FK_TaxType) t";
		List<Map<String,Object>> HBSWaterList = new ArrayList<>();
		Map<String,Object> HBSWaterMap = new HashMap<>();
		HBSWaterMap.put("tablename",waterHBSTableName);
		HBSWaterMap.put("field","t.PK_DataID");
		HBSWaterMap.put("fk_id","t.FK_PollutionID");
		HBSWaterList.add(HBSWaterMap);
		menuMap.put("waterPollutantDeclare",HBSWaterList);



		//环保税-固废污染物


		String solidHBSTableName = "(SELECT t1.PK_DataID,t1.FK_PollutionID FROM T_HBS_TaxInfo t1"
				+" WHERE t1.FK_TaxType IN ('401','402','403','404','405','406','407')) t";

		List<Map<String,Object>> HBSSolidList = new ArrayList<>();
		Map<String,Object> HBSSolidMap = new HashMap<>();
		HBSSolidMap.put("tablename",solidHBSTableName);
		HBSSolidMap.put("field","t.PK_DataID");
		HBSSolidMap.put("fk_id","t.FK_PollutionID");
		HBSSolidList.add(HBSSolidMap);
		menuMap.put("soilPollutantDeclare",HBSSolidList);


		//环保税-噪声污染物



		String soundTableName = "(SELECT t1.PK_DataID,t1.FK_PollutionID FROM T_HBS_TaxInfo t1,T_BAS_NoiseMonitorPoint t2"
		+" WHERE t1.FK_OutPutID = t2.PK_NoisePointID and t1.FK_TaxType = '301' )t";
		List<Map<String,Object>> HBSSoundList = new ArrayList<>();
		Map<String,Object> HBSSoundMap = new HashMap<>();
		HBSSoundMap.put("tablename",soundTableName);
		HBSSoundMap.put("field","t.PK_DataID");
		HBSSoundMap.put("fk_id","t.FK_PollutionID");
		HBSSoundList.add(HBSSoundMap);
		menuMap.put("soundDeclare",HBSSoundList);

		//监察执法
		List<Map<String,Object>> lawEnforcementList = new ArrayList<>();
		Map<String,Object> lawEnforcementMap = new HashMap<>();
		lawEnforcementMap.put("tablename","T_JCZF_TaskInfo");
		lawEnforcementMap.put("field","ID");
		lawEnforcementMap.put("fk_id","FK_PollutionID");
		lawEnforcementList.add(lawEnforcementMap);
		menuMap.put("lawEnforcement",lawEnforcementList);

		//行政处罚
		List<Map<String,Object>> administrativePenaltyList = new ArrayList<>();
		Map<String,Object> administrativePenaltyMap = new HashMap<>();
		administrativePenaltyMap.put("tablename","T_HJWF_CaseInfo");
		administrativePenaltyMap.put("field","PK_CaseID");
		administrativePenaltyMap.put("fk_id","FK_PollutionID");
		administrativePenaltyList.add(administrativePenaltyMap);
		menuMap.put("administrativePenalty",administrativePenaltyList);

		//信访投诉
		List<Map<String,Object>> petitionInfoList = new ArrayList<>();
		Map<String,Object> petitionInfoMap = new HashMap<>();
		petitionInfoMap.put("tablename","T_XFTS_PetitionInfo");
		petitionInfoMap.put("field","ID");
		petitionInfoMap.put("fk_id","FK_PollutionID");
		petitionInfoList.add(petitionInfoMap);
		menuMap.put("petitionInfo",petitionInfoList);


		//应急预案
		List<Map<String,Object>> contingencyPlanList = new ArrayList<>();
		Map<String,Object> contingencyPlanMap = new HashMap<>();
		contingencyPlanMap.put("tablename","T_HJYJ_EmergencyPlan");
		contingencyPlanMap.put("field","ID");
		contingencyPlanMap.put("fk_id","FK_PollutionID");
		contingencyPlanList.add(contingencyPlanMap);
		menuMap.put("contingencyPlan",contingencyPlanList);


		//应急事件
		List<Map<String,Object>> emergentEventList = new ArrayList<>();
		Map<String,Object> emergentEventMap = new HashMap<>();
		emergentEventMap.put("tablename","T_HJYJ_EmergencyEvent");
		emergentEventMap.put("field","ID");
		emergentEventMap.put("fk_id","FK_PollutionID");
		emergentEventList.add(emergentEventMap);
		menuMap.put("emergentEvent",emergentEventList);

		//监督性监测废水
		List<Map<String,Object>> superviseWaterList = new ArrayList<>();
		Map<String,Object> superviseWaterMap = new HashMap<>();
		String waterTableName = "T_SUPERVISE_WaterData t1 " +
				"JOIN T_BAS_WaterOutputInfo t2 ON (t2.PK_ID = t1.FK_WaterOutPutID and (t2.OutputType !='3' or t2.OutputType is null)) " +
				"JOIN PUB_CODE_PollutantFactor T3 ON t1.FK_PollutantCode = T3.Code ";
		superviseWaterMap.put("tablename",waterTableName);
		superviseWaterMap.put("field","t1.FK_PollutionID,t1.FK_WaterOutputID,t1.MonitorTime");
		superviseWaterMap.put("fk_id","t1.FK_PollutionID");
		superviseWaterMap.put("groupString","t1.FK_PollutionID,t1.FK_WaterOutputID,t1.MonitorTime");
		superviseWaterList.add(superviseWaterMap);
		menuMap.put("waterMonitor",superviseWaterList);


		//监督性监测有组织废气
		List<Map<String,Object>> superviseGasList = new ArrayList<>();
		Map<String,Object> superviseGasMap = new HashMap<>();
		String gasTableName = "T_SUPERVISE_GasData t1 " +
				"JOIN T_BAS_GasOutputInfo t2 ON t1.FK_GasOutputID = t2.PK_ID " +
				"JOIN PUB_CODE_PollutantFactor T3 ON t1.FK_PollutantCode = T3.Code ";
		superviseGasMap.put("tablename",gasTableName);
		superviseGasMap.put("field","t1.FK_PollutionID,t1.FK_GasOutputID,t1.MonitorTime");
		superviseGasMap.put("fk_id","t1.FK_PollutionID");
		superviseGasMap.put("groupString","t1.FK_PollutionID,t1.FK_GasOutputID,t1.MonitorTime");
		superviseGasList.add(superviseGasMap);
		menuMap.put("gasOrganizedMonitor",superviseGasList);



		//监督性监测无组织废气
		List<Map<String,Object>> superviseUnGasList = new ArrayList<>();
		Map<String,Object> superviseUnGasMap = new HashMap<>();
		String unGasTableName = "T_SUPERVISE_GasUnorganizedData t1 " +
				"JOIN T_BAS_UnorganizedMonitorPointInfo t2 ON t1.FK_GasOutputID = t2.PK_ID " +
				"JOIN PUB_CODE_PollutantFactor T3 ON t1.FK_PollutantCode = T3.Code ";
		superviseUnGasMap.put("tablename",unGasTableName);
		superviseUnGasMap.put("field","t1.FK_PollutionID,t1.FK_GasOutputID,t1.MonitorTime");
		superviseUnGasMap.put("fk_id","t1.FK_PollutionID");
		superviseUnGasMap.put("groupString","t1.FK_PollutionID,t1.FK_GasOutputID,t1.MonitorTime");
		superviseUnGasList.add(superviseUnGasMap);
		menuMap.put("gasUnOrganizedMonitor",superviseUnGasList);

		//在线废水（小时数据）

		List<Map<String,Object>> onlineWaterList = new ArrayList<>();
		Map<String,Object> onlineWaterMap = new HashMap<>();
		String onlineWaterTableName = "T_ONLINE_WaterHourData t1 " +
				"JOIN T_BAS_WaterOutPutInfo t3 ON (t3.PK_ID = t1.FK_WaterOutPutID and (t3.OutputType !='3' or t3.OutputType is null)) "+
				"JOIN PUB_CODE_PollutantFactor T4 ON t1.FK_PollutantCode = T4.Code "
				+"and T4.PollutantType = '1' and T4.IsUsed = '1'";
		onlineWaterMap.put("tablename",onlineWaterTableName);
		onlineWaterMap.put("field","t1.FK_PollutionID,t1.FK_WaterOutPutID,t1.MonitorTime");
		onlineWaterMap.put("fk_id","t1.FK_PollutionID");
		onlineWaterMap.put("groupString","t1.FK_PollutionID,t1.FK_WaterOutPutID,t1.MonitorTime");
		onlineWaterList.add(onlineWaterMap);
		menuMap.put("waterOnlieMonitor",onlineWaterList);




		//在线废气（小时数据）

		List<Map<String,Object>> onlineGasList = new ArrayList<>();
		Map<String,Object> onlineGasMap = new HashMap<>();
		String onlineGasTableName = "T_ONLINE_GasHourData t1 " +
				"JOIN T_BAS_GASOutPutInfo t3 ON t3.PK_ID = t1.FK_GasOutPutID " +
				"JOIN PUB_CODE_PollutantFactor T4 ON t1.FK_PollutantCode = T4.Code"
				+" and T4.PollutantType = '2' and T4.IsUsed = '1' ";
		onlineGasMap.put("tablename",onlineGasTableName);
		onlineGasMap.put("field","t1.FK_PollutionID,t1.FK_GasOutPutID,t1.MonitorTime");
		onlineGasMap.put("fk_id","t1.FK_PollutionID");
		onlineGasMap.put("groupString","t1.FK_PollutionID,t1.FK_GasOutPutID,t1.MonitorTime");
		onlineGasList.add(onlineGasMap);
		menuMap.put("gasOnlineMonitor",onlineGasList);



		//信用评价
		List<Map<String,Object>> creditRatingList = new ArrayList<>();
		Map<String,Object> creditRatingMap = new HashMap<>();
		creditRatingMap.put("tablename","T_XYPJ_EnvCreditEvaluation");
		creditRatingMap.put("field","PK_DataID");
		creditRatingMap.put("fk_id","FK_PollutionID");
		creditRatingList.add(creditRatingMap);
		menuMap.put("creditRating",creditRatingList);

		//清洁生产
		List<Map<String,Object>> clearerProductionList = new ArrayList<>();
		Map<String,Object> clearerProductionMap = new HashMap<>();
		clearerProductionMap.put("tablename","T_QJSC_CleanerProductionInfo");
		clearerProductionMap.put("field","PK_DataID");
		clearerProductionMap.put("fk_id","FK_PollutionID");
		clearerProductionList.add(clearerProductionMap);
		menuMap.put("clearerProduction",clearerProductionList);


		//危险单元信息
		List<Map<String,Object>> DangerDistinguishList = new ArrayList<>();
		Map<String,Object> DangerDistinguishListMap = new HashMap<>();
		DangerDistinguishListMap.put("tablename","T_WXFW_DangerDistinguish");
		DangerDistinguishListMap.put("field","PK_ID");
		DangerDistinguishListMap.put("fk_id","FK_PollutionId");
		DangerDistinguishList.add(DangerDistinguishListMap);
		menuMap.put("HazardSourceInformation",DangerDistinguishList);

		//废水手工监测数据

		List<Map<String,Object>> handWaterList = new ArrayList<>();
		Map<String,Object> handWaterMap = new HashMap<>();
		String handWaterTableName = "T_ONESELF_WaterManualData t1 " +
				"JOIN T_BAS_WaterOutPutInfo t3 ON t3.PK_ID = t1.FK_WaterOutPutID " +
				"JOIN PUB_CODE_PollutantFactor T4 ON t1.FK_PollutantCode = T4.Code "+
				" and T4.PollutantType='1' AND T4.IsUsed = '1'";
		handWaterMap.put("tablename",handWaterTableName);
		handWaterMap.put("field","t1.PK_DataID");
		handWaterMap.put("fk_id","t1.FK_PollutionID");
		handWaterList.add(handWaterMap);
		menuMap.put("wasteWaterAutomaticTest",handWaterList);

		//废气手工监测数据
		List<Map<String,Object>> handGasList = new ArrayList<>();
		Map<String,Object> handGasMap = new HashMap<>();
		String handGasTableName = "T_ONESELF_GasManualData t1 " +
				"JOIN T_BAS_GASOutPutInfo t3 ON t3.PK_ID = t1.FK_GasOutPutID " +
				"JOIN PUB_CODE_PollutantFactor T4 ON t1.FK_PollutantCode = T4.Code "+
				" and T4.PollutantType='2' AND T4.IsUsed = '1'";
		handGasMap.put("tablename",handGasTableName);
		handGasMap.put("field","t1.PK_DataID");
		handGasMap.put("fk_id","t1.FK_PollutionID");
		handGasList.add(handGasMap);
		menuMap.put("wasteGasAutomaticTest",handGasList);


	}

}

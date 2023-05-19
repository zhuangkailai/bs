package com.tjpu.sp.controller.environmentalprotection.monitorpoint;


import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.fileconfig.BusinessTypeConfig;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.controller.common.RabbitmqMongoDBController;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirMonitorStationVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirStationPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirStationPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.AirEnum;

/**
 * 
 * @author: xsm
 * @date: 2019年5月24日 上午 9:14
 * @Description:大气监测点处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
@RestController
@RequestMapping("airMonitorStation")
public class AirMonitorStationController {

	@Autowired
	private PublicSystemMicroService publicSystemMicroService;
	@Autowired
	private AirMonitorStationService airMonitorStationService;
	@Autowired
	private DeviceStatusService deviceStatusService;
	@Autowired
	private FileInfoService fileInfoService;
	@Autowired
	private KeyMonitorPollutantService keyMonitorPollutantService;
	@Autowired
	private AirStationPollutantSetService airStationPollutantSetService;
	@Autowired
	private VideoCameraService videoCameraService;
	@Autowired
	private PollutantService pollutantService;
	@Autowired
	private RabbitmqController rabbitmqController;
	@Autowired
	private RabbitmqMongoDBController rabbitmqMongoDBController;
	@Autowired
	private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
	@Autowired
	@Qualifier("secondMongoTemplate")
	private MongoTemplate mongoTemplate;

	private String sysmodel="airMonitorStation";
	private String pk_id="pk_airid";
	private String listfieldtype="list-air";
	private String monitorpointtype= String.valueOf(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode());//空气监测点类型

	/**
	 * 数据源
	 */
	@Value("${spring.datasource.primary.name}")
	private String datasource;



	/**
	 * @author: xsm
	 * @date: 2019/05/23 0016 上午 10:20
	 * @Description: 获取大气监测点初始化列表信息
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [request, session]
	 * @return:
	 */
	@RequestMapping(value = "getAirMonitorStationsListPage", method = RequestMethod.POST)
	public Object getAirMonitorStationsListPage( HttpServletRequest request ) throws Exception {
		try {
			//获取userid

			String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
			Map<String, Object> paramMap= RequestUtil.parseRequest(request);
			paramMap.put("sysmodel", sysmodel);
			paramMap.put("userid", userId);
			paramMap.put("listfieldtype", listfieldtype);
			paramMap.put("datasource", datasource);
			String param = AuthUtil.paramDataFormat( paramMap);
			Object resultList = publicSystemMicroService.getListByParam(param);
			resultList = AuthUtil.decryptData(resultList);
			JSONObject jsonObject = JSONObject.fromObject(resultList);
			Object data2 = jsonObject.get("data");
			JSONObject jsonObject1 = JSONObject.fromObject(data2);
			Object data3 = jsonObject1.get("tabledata");
			JSONObject jsonObject2 = JSONObject.fromObject(data3);
			List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject2.get("tablelistdata");
			pollutantService.orderPollutantDataByParamMap(listdata,"pollutants",AirEnum.getCode());
			jsonObject2.put("tablelistdata",listdata);
			jsonObject1.put("tabledata",jsonObject2);
			jsonObject.put("data",jsonObject1);
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	/**
	 * @author: xsm
	 * @date: 2019/5/23 0016 上午 10:32
	 * @Description: 根据自定义参数获取大气监测点列表数据
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [paramsjson]
	 * @return:
	 */
	@RequestMapping(value = "getAirMonitorStationsByParamMap", method = RequestMethod.POST)
	public Object getAirMonitorStationsByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) throws Exception {
		try {
			JSONObject paramMap = JSONObject.fromObject(map);
			paramMap.put("listfieldtype", listfieldtype);
			paramMap.put("sysmodel", sysmodel);
			paramMap.put("datasource", datasource);
			String param = AuthUtil.paramDataFormat( paramMap);
			Object resultList = publicSystemMicroService.getListData(param);
			resultList = AuthUtil.decryptData(resultList);

			JSONObject jsonObject = JSONObject.fromObject(resultList);
			Object data2 = jsonObject.get("data");
			JSONObject jsonObject1 = JSONObject.fromObject(data2);
			List<Map<String, Object>> listdata = (List<Map<String, Object>>) jsonObject1.get("tablelistdata");
			pollutantService.orderPollutantDataByParamMap(listdata,"pollutants",AirEnum.getCode());
			jsonObject1.put("tablelistdata",listdata);
			jsonObject.put("data",jsonObject1);
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @author: xsm
	 * @date: 2019/5/24 上午9:18
	 * @Description:  获取大气监测点新增页面
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: []
	 * @throws:
	 */
	@RequestMapping(value = "getAirMonitorStationAddPage",method = RequestMethod.POST)
	public Object getAirMonitorStationAddPage() {
		try {
			//设置参数
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("sysmodel", sysmodel);
			paramMap.put("datasource", datasource);
			String param = AuthUtil.paramDataFormat( paramMap);
			Object resultList = publicSystemMicroService.getAddPageInfo(param);
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * @author: xsm
	 * @date: 2019/5/24 上午9:20
	 * @Description:  新增大气监测点信息
	 * @updateUser:xsm
	 * @updateDate:2019/7/8 上午11:02
	 * @updateDescription:添加点位时，默认添加该点位类型的主要污染物信息
	 * @param: [request]
	 * @throws:
	 */
	@RequestMapping(value = "addAirMonitorStation",method = RequestMethod.POST)
	public Object addAirMonitorStation(HttpServletRequest request) throws Exception {
		try {
			Map<String, Object> paramMap = RequestUtil.parseRequest(request);
			//设置参数
			paramMap.put("sysmodel", sysmodel);
			paramMap.put("datasource", datasource);
			String formdata=paramMap.get("formdata").toString();
			JSONObject jsondatas = JSONObject.fromObject(formdata);
			String mnnum = jsondatas.getString("dgimn");
			String param = AuthUtil.paramDataFormat( paramMap);
			Object resultList = publicSystemMicroService.doAddMethod(param);
			JSONObject jsonObject = JSONObject.fromObject(resultList);
			String flag = jsonObject.getString("flag");
			if("success".equals(flag) ) {
				if (mnnum != null && !"".equals(mnnum)) {
					//根据MN号查询状态表中是否有重复数据
					List<DeviceStatusVO> objlist = deviceStatusService.getDeviceStatusInfosByDgimn(mnnum);
					if (objlist==null||objlist.size()==0){//当不存在重复数据时
					//判断MN号是否为空，不为空则进行维护，存入到关系表中
					DeviceStatusVO obj = new DeviceStatusVO();
					obj.setPkId(UUID.randomUUID().toString());
					obj.setDgimn(mnnum);
					obj.setFkMonitorpointtypecode(monitorpointtype);
					obj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
					obj.setUpdatetime(new Date());
					deviceStatusService.insert(obj);
					}
				}
				Map<String, Object> params = new HashMap<>();
				params.put("dgimn", JSONObject.fromObject(formdata).get("dgimn")!=null?JSONObject.fromObject(formdata).get("dgimn").toString():null);
				params.put("monitorpointname", JSONObject.fromObject(formdata).get("monitorpointname").toString());
				//根据监测点名称和MN号获取新增的那条空气站点信息
				Map<String, Object> map = airMonitorStationService.selectAirStationInfoByPointNameAndDgimn(params);
				List<Map<String, Object>> list = keyMonitorPollutantService.selectByPollutanttype(CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() + "");
				List<AirStationPollutantSetVO> airlist = new ArrayList<>();
				for (Map<String, Object> objmap : list) {
					AirStationPollutantSetVO airobj = new AirStationPollutantSetVO();
					airobj.setUpdatetime(new Date());
					airobj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
					airobj.setPkDataid(UUID.randomUUID().toString());
					airobj.setFkAirmonintpointid(map.get("PK_AirID").toString());
					airobj.setFkPollutantcode(objmap.get("FK_PollutantCode").toString());
					airlist.add(airobj);
				}
				//批量添加 将该类型的重点污染物存储到污染物设置（标准）表中
				if (airlist!=null && airlist.size()>0) {
					airStationPollutantSetService.insertAirStationPollutantSets(airlist);
				}

				//发送消息到队列
				sendToMq(jsonObject.getString("data"));
			}
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	

	/**
	 * @author: xsm
	 * @date: 2019/5/24 上午9:21
	 * @Description:  根据主键ID获取大气监测点信息修改页面
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [id]
	 * @throws:
	 */
	@RequestMapping(value = "getAirMonitorStationUpdatePageByID",method = RequestMethod.POST)
	public Object getAirMonitorStationUpdatePageByID(@RequestJson(value="id",required = true)String id) throws Exception {
		try {	
			Map<String, Object> paramMap = new HashMap<String, Object>();
			//设置参数
			paramMap.put("sysmodel", sysmodel);			
			paramMap.put(pk_id, id);
			paramMap.put("datasource", datasource);
			String Param = AuthUtil.paramDataFormat(paramMap);
			Object resultList = publicSystemMicroService.goUpdatePage(Param);
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * @author: xsm
	 * @date: 2019/5/24 上午9:26
	 * @Description:  修改大气监测点信息
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [request]
	 * @throws:
	 */
	@RequestMapping(value = "updateAirMonitorStation",method = RequestMethod.POST)
	public Object updateAirMonitorStation(HttpServletRequest request) throws Exception {
		try {
			Map<String, Object> paramMap = RequestUtil.parseRequest(request);
			Map<String, Object> parammap = new HashMap<>();
			//设置参数
			paramMap.put("sysmodel", sysmodel);
			paramMap.put("datasource", datasource);
			String formdata=paramMap.get("formdata").toString();
			JSONObject jsondatas = JSONObject.fromObject(formdata);
			String pkid =jsondatas.get("pk_airid").toString();
			String newmnnum = jsondatas.getString("dgimn");//修改后的MN号
			parammap.put("pkid",pkid);
			AirMonitorStationVO airMonitorStation = airMonitorStationService.getAirMonitorStationByID(pkid);
			String Param = AuthUtil.paramDataFormat( paramMap);
			Object resultList = publicSystemMicroService.doEditMethod(Param);
			JSONObject jsonObject = JSONObject.fromObject(resultList);
			String flag = jsonObject.getString("flag");
			//将页面修改信息set进实体对象中
			DeviceStatusVO obj = new DeviceStatusVO();
			obj.setPkId(UUID.randomUUID().toString());
			obj.setDgimn(newmnnum);
			obj.setFkMonitorpointtypecode(monitorpointtype);
			obj.setUpdateuser(RedisTemplateUtil.getRedisCacheDataByToken("username", String.class));
			obj.setUpdatetime(new Date());
			//逻辑判断
			if("success".equals(flag) && airMonitorStation != null){//当修改前对象不为空
				if (airMonitorStation.getDgimn() != null && !"".equals(airMonitorStation.getDgimn())) {//判断修改前MN号不为空
					if (newmnnum!=null&&!"".equals(newmnnum)) {//当修改后的MN号也不为空时
					//比较两个MN号是否相等，判断MN号是否有修改
						if (!newmnnum.equals(airMonitorStation.getDgimn())){//当修改前和修改后的MN号不等
							//根据MN号查询状态表中是否有重复数据
							List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(airMonitorStation.getDgimn());
							if (oldobjlist!=null&&oldobjlist.size()>0) {//当存在重复数据时,删除修改前MN号的状态表数据
								obj.setStatus(oldobjlist.get(0).getStatus());
								deviceStatusService.deleteDeviceStatusByMN(airMonitorStation.getDgimn());
							}
							List<DeviceStatusVO> newobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(newmnnum);
							if (newobjlist==null||newobjlist.size()==0) {//当不存在重复数据时
								deviceStatusService.insert(obj);
							}
							//修改点位MN时  批量修改数据权限表中相关点位MN
							userMonitorPointRelationDataService.updataUserMonitorPointRelationDataByMnAndType(airMonitorStation.getDgimn(),newmnnum,monitorpointtype);

                            //更新MongoDB数据的MN号
                            Map<String, Object> mqMap = new HashMap<>();
                            mqMap.put("monitorpointtype", AirEnum.getCode());
                            mqMap.put("dgimn", newmnnum);
                            mqMap.put("oldMN", airMonitorStation.getDgimn());
                            rabbitmqMongoDBController.sendPointMNUpdateDirectQueue(JSONObject.fromObject(mqMap));
						}

					}else{
						//根据MN号查询状态表中是否有重复数据
						List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(airMonitorStation.getDgimn());
						if (oldobjlist!=null&&oldobjlist.size()>0) {//当存在重复数据时,删除修改前MN号的状态表数据
							deviceStatusService.deleteDeviceStatusByMN(airMonitorStation.getDgimn());
						}
					}
					}else{//修改前MN为空
					if (newmnnum!=null&&!"".equals(newmnnum)){//当修改后的MN号不为空时
						//根据MN号查询状态表中是否有重复数据
						List<DeviceStatusVO> oldobjlist = deviceStatusService.getDeviceStatusInfosByDgimn(newmnnum);
						if (oldobjlist==null||oldobjlist.size()==0){//当不存在重复数据时
							//判断MN号是否为空，不为空则进行维护，存入到关系表中
							deviceStatusService.insert(obj);
						}
					}
				}
				//发送消息到队列
				sendToMq(pkid);
			}
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * @author: xsm
	 * @date: 2019/5/24 上午9:30
	 * @Description: 根据大气监测点信息主键ID删除单条数据
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [id]
	 * @throws:
	 */
	@RequestMapping(value = "deleteAirMonitorStationByID",method = RequestMethod.POST)
	public Object deleteAirMonitorStationByID(@RequestJson(value="id",required = true)String id) throws Exception {
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			Map<String, Object> parammap = new HashMap<>();
			//设置参数
			paramMap.put("sysmodel", sysmodel);			
			paramMap.put(pk_id, id);
			paramMap.put("datasource", datasource);
			parammap.put("pkid",id);
			Map<String, Object> oldobj=airMonitorStationService.getAirStationDeviceStatusByID(parammap);
			AirMonitorStationVO airmonintpoint = airMonitorStationService.getAirMonitorStationByID(id);

			//获取附件表关系
			List<String> fileIds = airMonitorStationService.getfileIdsByID(parammap);
			String statuspkid = (oldobj!=null && oldobj.size()>0)?oldobj.get("PK_ID").toString():"";
			String Param = AuthUtil.paramDataFormat(paramMap);
			Object resultList = publicSystemMicroService.deleteMethod(Param);
			JSONObject jsonObject = JSONObject.fromObject(resultList);
			String flag = jsonObject.getString("flag");
			if("success".equals(flag)) {
				if (!"".equals(statuspkid)) {//删除状态表关系
					deviceStatusService.deleteByPrimaryKey(statuspkid);
				}
				//删除数据权限表相关点位的数据权限
				userMonitorPointRelationDataService.deleteUserMonitorPointRelationDataByMnAndType(airmonintpoint.getDgimn(),CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()+"");
				//删除附件表关系以及MongoDB数据
				if (fileIds != null && fileIds.size() > 0) {
					MongoDatabase useDatabase = mongoTemplate.getDb();
					String collectionType = BusinessTypeConfig.businessTypeMap.get("1");
					GridFSBucket gridFSBucket = GridFSBuckets.create(useDatabase, collectionType);
					fileInfoService.deleteFilesByParams(fileIds, gridFSBucket);
				}
				//删除点位下的所有视频摄像头信息
				parammap.clear();
				parammap.put("monitorpointid",id);
				parammap.put("monitorpointtype",CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode());
				videoCameraService.deleteVideoCameraByParamMap(parammap);

				//发送消息到队列
				sendToMq(airmonintpoint);
			}
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * @author: xsm
	 * @date: 2019/5/24 上午9:37
	 * @Description:  根据大气监测点信息主键ID获取详情信息
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [id]
	 * @throws:
	 */
	@RequestMapping(value = "getAirMonitorStationDetailByID",method = RequestMethod.POST)
	public Object getAirMonitorStationDetailByID(@RequestJson(value="id",required = true)String id) throws Exception {
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();			
			//设置参数
			paramMap.put("sysmodel", sysmodel);			
			paramMap.put(pk_id, id);
			paramMap.put("datasource", datasource);
			String Param = AuthUtil.paramDataFormat(paramMap);
			Object resultList = publicSystemMicroService.getDetail(Param);
			return resultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	/**
	 * @author: xsm
	 * @date: 2019/6/11 0011 上午9:37
	 * @Description:  根据监测点名称获取空气站点的基础信息及点位状态
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [id]
	 * @throws:
	 */
	@RequestMapping(value = "getAirStationInfoAndStateByMonitorPointName",method = RequestMethod.POST)
	public Object getAirStationInfoAndStateByMonitorPointName(@RequestJson(value="monitorpointname",required = false)String monitorpointname) throws Exception {
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			//设置参数
			paramMap.put("monitorpointname", monitorpointname);
			paramMap.put("monitorpointtype", monitorpointtype);
			List<Map<String, Object>> listdata= airMonitorStationService.getAirStationInfosByMonitorPointNameAndType(paramMap);
			if (listdata!=null&&listdata.size()>0){
				for (Map<String, Object> obj:listdata){
					if (obj.get("status")!=null){
							obj.put("status",obj.get("status"));
					}else{
						obj.put("status",0);
					}
				}
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("listdata",listdata);
			return  AuthUtil.parseJsonKeyToLower("success", result);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @author: xsm
	 * @date: 2019/6/11 0011 下午1:30
	 * @Description:  根据空气监测点id获取该监测点下监测的所有污染物
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [id]
	 * @throws:
	 */
	@RequestMapping(value = "getAirStationAllPollutantsByID",method = RequestMethod.POST)
	public Object getAirStationAllPollutantsByID(@RequestJson(value = "pkids",required = true) List<Object> pkidlist) throws Exception {
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			//设置参数
			paramMap.put("pkidlist", pkidlist);
			paramMap.put("monitorpointtype", monitorpointtype);
			List<Map<String, Object>> listdata= airMonitorStationService.getAirStationAllPollutantsByIDAndType(paramMap);
			List<Map<String, Object>> result = new ArrayList<>();
			for (Map<String, Object> map : listdata) {
				Map<String, Object> objmap = new HashMap<String, Object>();
				objmap.put("labelname", map.get("name"));
				objmap.put("value", map.get("code"));
				objmap.put("standardmaxvalue", map.get("standardmaxvalue"));
				objmap.put("standardminvalue", map.get("standardminvalue"));
				objmap.put("pollutantunit", map.get("PollutantUnit"));
				result.add(objmap);
			}
			return  AuthUtil.parseJsonKeyToLower("success", result);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	private void sendToMq(String outputid){
		AirMonitorStationVO airmonintpoint = airMonitorStationService.getAirMonitorStationByID(outputid);
		//发送消息到队列
		Map<String,Object> mqMap=new HashMap<>();
		mqMap.put("monitorpointtype",monitorpointtype);
		mqMap.put("dgimn",airmonintpoint.getDgimn());
		mqMap.put("monitorpointid",airmonintpoint.getPkAirid());
		mqMap.put("fkpollutionid","");
		rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
	}
	private void sendToMq(AirMonitorStationVO airmonintpoint){
		//发送消息到队列
		Map<String,Object> mqMap=new HashMap<>();
		mqMap.put("monitorpointtype",monitorpointtype);
		mqMap.put("dgimn",airmonintpoint.getDgimn());
		mqMap.put("monitorpointid",airmonintpoint.getPkAirid());
		mqMap.put("fkpollutionid","");
		rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
	}
}

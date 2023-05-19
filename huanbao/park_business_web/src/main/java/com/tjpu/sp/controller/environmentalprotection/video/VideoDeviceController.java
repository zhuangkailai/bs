package com.tjpu.sp.controller.environmentalprotection.video;


import cn.hutool.core.lang.Assert;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.environmentalprotection.video.VideoCameraVO;
import com.tjpu.sp.model.environmentalprotection.video.VideoDeviceVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.MonitorPointCommonService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import com.tjpu.sp.service.environmentalprotection.video.VideoDeviceService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年5月23日 下午 4:14
 * @Description:硬盘录像机处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("videoDevice")
public class VideoDeviceController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private VideoDeviceService videoDeviceService;
    @Autowired
    private VideoCameraService videoCameraService;
    @Autowired
    private PollutionService pollutionService;

    private String sysmodel = "videodevice";
    private String pk_id = "pk_vediodeviceid";
    private String camerasysmodel = "videocamera";//摄像头sysmodel
    private String camerapkid = "pk_vediocameraid";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author: xsm
     * @date: 2019/5/23 下午5:14
     * @Description: 根据监测点ID和监测点类型获取硬盘录像机和摄像头组合树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointid，monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getVideoDeviceAndVideoCameraTreeByMonitorPointIDAndType", method = RequestMethod.POST)
    public Object getVideoDeviceAndVideoCameraTreeByMonitorPointIDAndType(@RequestJson(value = "monitorpointid", required = true) String monitorpointid,
                                                                          @RequestJson(value = "monitorpointtype", required = true) String monitorpointtype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> VideoData = videoDeviceService.getVideoDeviceAndVideoCameraTree(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", VideoData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/23 下午5:18
     * @Description: 获取硬盘录像机新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getVideoDeviceAddPage", method = RequestMethod.POST)
    public Object getVideoDeviceAddPage() {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            //添加属性
            JSONObject formdata = JSONObject.fromObject(resultList);
            Map<String, Object> data = formdata.getJSONObject("data");
            List<Map<String, Object>> addcontroldata = (List<Map<String, Object>>) data.get("addcontroldata");
            for (Map<String, Object> obj : addcontroldata) {
                if ("pk_vediodeviceid".equals(obj.get("name"))) {
                    obj.put("filterable", true);
                    obj.put("allowcreate", true);
                }
                obj.put("defaultfirstoption", true);
            }
            return AuthUtil.parseJsonKeyToLower("success", data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: mmt
     * @date: 2022/9/6 下午5:18
     * @Description: 获取所有设备名字
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getAllVideoDeviceNames", method = RequestMethod.POST)
    public Object getAllVideoDeviceNames() {
        try {
            return AuthUtil.parseJsonKeyToLower("success", videoDeviceService.getAllVideoDeviceNames());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/5/24 下午3:20
     * @Description: 新增硬盘录像机和摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addVideoDeviceAndVideoCamera", method = RequestMethod.POST)
    public Object addVideoDeviceAndVideoCamera(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            Map<String, Object> deviceMap = (Map<String, Object>) paramMap.get("deviceformdata");
            Map<String, Object> cameraMap = (Map<String, Object>) paramMap.get("cameraformdata");
            String pkdeviceid = "";
            if ("add".equals(deviceMap.get("addoredit"))) {//判断硬盘录像机是添加操作还是修改操作
                String name = deviceMap.get("pk_vediodeviceid").toString();
                //添加操作
                VideoDeviceVO obj = JSONObjectUtil.JsonObjectToEntity((JSONObject) deviceMap, new VideoDeviceVO());
                pkdeviceid = UUID.randomUUID().toString().replaceAll("-", "");
                obj.setPkVediodeviceid(pkdeviceid);
                obj.setVediodevicename(name);
                videoDeviceService.saveVideoDeviceInfo(obj);
            } else if ("edit".equals(deviceMap.get("addoredit"))) {
                //修改操作
                pkdeviceid = deviceMap.get("pk_vediodeviceid").toString();
                VideoDeviceVO obj = JSONObjectUtil.JsonObjectToEntity((JSONObject) deviceMap, new VideoDeviceVO());
                obj.setPkVediodeviceid(pkdeviceid);
                videoDeviceService.editVideoDeviceInfo(obj);
            }
            cameraMap.put("fk_vediodeviceid", pkdeviceid);//将硬盘录像机ID插入摄像头中
            paramMap.clear();
            paramMap.put("formdata", cameraMap);
            paramMap.put("sysmodel", camerasysmodel);//添加摄像头信息
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: mmt
     * @date: 2022/9/6 下午3:20
     * @Description: 新增硬盘录像机和摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addVideoDeviceAndVideoCameraByParam", method = RequestMethod.POST)
    public Object addVideoDeviceAndVideoCameraByParam(@RequestJson(value = "deviceformdata") Object deviceformdata,
                                                      @RequestJson(value = "addoredit") Object addoredit,
                                                      @RequestJson(value = "cameraformdata") Object cameraformdata) throws Exception {
        try {
            String pkdeviceid = "";
            if ("add".equals(addoredit)) {//判断硬盘录像机是添加操作还是修改操作
                //添加操作
                VideoDeviceVO videoDeviceVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(deviceformdata), new VideoDeviceVO());
                pkdeviceid = UUID.randomUUID().toString().replaceAll("-", "");
                videoDeviceVO.setPkVediodeviceid(pkdeviceid);
                videoDeviceService.saveVideoDeviceInfo(videoDeviceVO);
            } else if ("edit".equals(addoredit)) {
                //修改操作
                VideoDeviceVO obj = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(deviceformdata), new VideoDeviceVO());
                pkdeviceid = obj.getPkVediodeviceid();
                Assert.notNull(pkdeviceid, "pkVediodeviceid不能为空");
                videoDeviceService.editVideoDeviceInfo(obj);
            }

            VideoCameraVO cameraVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(cameraformdata), new VideoCameraVO());
            cameraVO.setFkVediodeviceid(pkdeviceid);//将硬盘录像机ID插入摄像头中
            String pkVediocameraid = UUID.randomUUID().toString().replaceAll("-", "");
            cameraVO.setPkVediocameraid(pkVediocameraid);
            videoCameraService.saveVideoCamera(cameraVO);
            return AuthUtil.parseJsonKeyToLower("success", pkVediocameraid);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/5/24 下午3:21
     * @Description: 根据主键ID获取硬盘录像机信息修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVideoDeviceUpdatePageByID", method = RequestMethod.POST)
    public Object getVideoDeviceUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @date: 2019/5/24 下午3:24
     * @Description: 修改硬盘录像机信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateVideoDevice", method = RequestMethod.POST)
    public Object updateVideoDevice(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: mmt
     * @date: 2022/9/5 下午3:24
     * @Description: 修改硬盘录像机信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateVideoDeviceByParam", method = RequestMethod.POST)
    public Object updateVideoDeviceByParam(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            VideoDeviceVO videoDeviceVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new VideoDeviceVO());
            Assert.notNull(videoDeviceVO.getPkVediodeviceid(), "pkVediodeviceid不能为空");
            videoDeviceService.updateVideoDeviceByParam(videoDeviceVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/24 下午3:24
     * @Description: 通过硬盘录像机id删除硬盘录像机
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteVideoDeviceByParam", method = RequestMethod.POST)
    public Object deleteVideoDeviceByParam(@RequestJson(value = "videodeviceid", required = true) String id,
                                           @RequestJson(value = "monitorpointid", required = false) String monitorcode,
                                           @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype) throws Exception {
        try {
            videoDeviceService.VideoDeviceByID(id, monitorcode, monitorpointtype);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/5/24 下午3:27
     * @Description: 根据硬盘录像机信息主键ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVideoDeviceDetailByID", method = RequestMethod.POST)
    public Object getVideoDeviceDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @author: mmt
     * @date: 2022/9/6 下午3:27
     * @Description: 根据硬盘录像机信息主键ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVideoDeviceDetails", method = RequestMethod.POST)
    public Object getVideoDeviceDetails(@RequestJson(value = "id") String id) throws Exception {
        try {
            return AuthUtil.parseJsonKeyToLower("success", videoDeviceService.info(id));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/25 0025 上午 9:12
     * @Description: 获取视频和排口信息
     * @updateUser:xsm
     * @updateDate:2020/2/10 0010 上午 11:39
     * @updateDescription:
     * @updateUser:xsm
     * @updateDate:2020/2/17 0017 下午 18:26
     * @updateDescription:获取叠加视频树
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getVideoAndOutPutInfos", method = RequestMethod.POST)
    public Object getVideoAndOutPutInfos(@RequestJson(value = "isoverlay", required = false) String isoverlay) throws Exception {
        try {
            LinkedList<Map<String, Object>> videoAndOutPutInfos = videoDeviceService.getVideoAndOutPutInfos();
            /*if (isoverlay != null && "1".equals(isoverlay)) {
                LinkedList<Map<String, Object>> datalist = new LinkedList<>();
                if (videoAndOutPutInfos != null && videoAndOutPutInfos.size() > 0) {
                    for (Map<String, Object> map : videoAndOutPutInfos) {
                        if ("high".equals(map.get("branchcode"))) {
                            continue;
                        } else {
                            datalist.add(map);
                        }
                    }
                }
                return AuthUtil.parseJsonKeyToLower("success", datalist);
            } else {*/
            List<Map<String, Object>> resultList = videoAndOutPutInfos.stream().filter(m -> m != null).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", resultList);
            //}
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/25 0025 下午 5:08
     * @Description: 获取所有排口类型下的视频信息
     * @updateUser:
     * @updateDate:throw e;
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getVideoAndAllOutPutInfos", method = RequestMethod.POST)
    public Object getVideoAndAllOutPutInfos(@RequestJson(value = "name", required = false) String name) throws Exception {
        try {


            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));


            String pollutionflag = "pollution_d8240d6fbc6b";
            String highflag = "highflag_d8240d6fbc6b";
            String pointflag = "pointflag_d8240d6fbc6b";
            String solidwasteflag = "solidwaste_d8240d6fbc6b";
            /*String stenchflag = "stench_d8240d6fbc6b";
            String vocflag = "voc_d8240d6fbc6b";

            String airflag = "air_d8240d6fbc6b";
            String waterstationflag = "waterstation_d8240d6fbc6b";
            String microflag = "microflag_d8240d6fbc6b";*/


            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            int meteo = CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode();
            List<Integer> nomonitortypes = new ArrayList<>(pollutiontypes);
            nomonitortypes.add(meteo);//不包含气象点
            paramMap.put("nomonitortypes", nomonitortypes);
            List<Map<String, Object>> types = videoDeviceService.getAllEnvMonitorTypeList(paramMap);
            paramMap.put("monitorpointtypecodes", types.stream().map(item -> item.get("monitorpointtypecode")).collect(Collectors.toSet()));
            paramMap.put("pollutionflag", pollutionflag);
            paramMap.put("pointflag", pointflag);
            paramMap.put("solidwasteflag", solidwasteflag);
           /* paramMap.put("stenchflag", stenchflag);
            paramMap.put("vocflag", vocflag);

            paramMap.put("airflag", airflag);
            paramMap.put("waterstationflag", waterstationflag);
            paramMap.put("microflag", microflag);*/
            paramMap.put("categorys", categorys);
            paramMap.put("highflag", highflag);
            paramMap.put("name", name);
            LinkedList<Map<String, Object>> videoAndOutPutInfos = videoDeviceService.getVideoAndOutPutsInfos(paramMap);

            List<Map<String, Object>> pollutionvideo = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && "企业视频".equals(m.get("label").toString())).collect(Collectors.toList());
            List<Map<String, Object>> highvideo = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && "高空瞭望视频".equals(m.get("label").toString())).collect(Collectors.toList());
            /*List<Map<String, Object>> VOCvideo = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && "VOC视频".equals(m.get("label").toString())).collect(Collectors.toList());
            List<Map<String, Object>> stenchvideo = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && "恶臭视频".equals(m.get("label").toString())).collect(Collectors.toList());
            List<Map<String, Object>> airvideo = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && "空气视频".equals(m.get("label").toString())).collect(Collectors.toList());
            List<Map<String, Object>> waterstaionvideo = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && "水质视频".equals(m.get("label").toString())).collect(Collectors.toList());*/
            if (types != null && types.size() > 0) {
                for (Map<String, Object> onemap : types) {
                    List<Map<String, Object>> videolist = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && (onemap.get("monitorpointtypename") + "视频").equals(m.get("label").toString())).collect(Collectors.toList());
                    if (videolist.size() == 0) {
                        if (name != null && !onemap.get("monitorpointtypename").toString().contains(name)) {
                            continue;
                        }
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", UUID.randomUUID().toString());
                        data.put("label", onemap.get("monitorpointtypename") + "视频");
                        data.put("branchcode", onemap.get("monitorpointtypecode") + "");
                        data.put("fk_monitorpointtypecode", onemap.get("monitorpointtypecode") + "");
                        data.put("childrendata", new ArrayList<>());
                        videoAndOutPutInfos.add(data);
                    }
                }
            }

            if (pollutionvideo.size() == 0 && !(name != null && !"企业视频".contains(name))) {
                Map<String, Object> data = new HashMap<>();
                ArrayList<Object> child = new ArrayList<>();
                data.put("id", UUID.randomUUID().toString());
                data.put("label", "企业视频");
                data.put("branchcode", "pollution");
                data.put("childrendata", child);
                videoAndOutPutInfos.add(data);

            }
            if (highvideo.size() == 0 && !(name != null && !"高空瞭望视频".contains(name))) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", UUID.randomUUID().toString());
                data.put("label", "高空瞭望视频");
                data.put("branchcode", "high");
                data.put("childrendata", new ArrayList<>());
                videoAndOutPutInfos.addFirst(data);
            }

            return AuthUtil.parseJsonKeyToLower("success", videoAndOutPutInfos);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/12/16 0016 下午 1:10
     * @Description: 获取企业和高空瞭望视频
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getPollutionVideoInfos", method = RequestMethod.POST)
    public Object getPollutionVideoInfos(@RequestJson(value = "pkpollutionid", required = false) String pkpollutionid,
                                         @RequestJson(value = "customname", required = false) String customname) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("pkpollutionid", pkpollutionid);
            paramMap.put("categorys", categorys);
            paramMap.put("customname", customname);

            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            int meteo = CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode();
            List<Integer> nomonitortypes = new ArrayList<>(pollutiontypes);
            nomonitortypes.add(meteo);//不包含气象点
            paramMap.put("nomonitortypes", nomonitortypes);
            List<Map<String, Object>> types = videoDeviceService.getAllEnvMonitorTypeList(paramMap);
            paramMap.put("monitorpointtypecodes", types.stream().map(item -> item.get("monitorpointtypecode")).collect(Collectors.toSet()));
            LinkedList<Map<String, Object>> videoAndOutPutInfos = videoDeviceService.getPollutionVideoInfos(paramMap);
            List<Map<String, Object>> resultList = videoAndOutPutInfos.stream().filter(m -> m != null).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/12/16 0016 下午 4:25
     * @Description: 获取监测点类型和视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getMonitorTypeVideoInfos", method = RequestMethod.POST)
    public Object getMonitorTypeVideoInfos(@RequestJson(value = "pkpollutionid", required = false) String pkpollutionid,
                                           @RequestJson(value = "customname", required = false) String customname,
                                           @RequestJson(value = "notneedhigh", required = false) String notneedhigh) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("pkpollutionid", pkpollutionid);
            paramMap.put("categorys", categorys);
            paramMap.put("customname", customname);
            paramMap.put("notneedhigh", notneedhigh);//是否需要高空视频

            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            int meteo = CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode();
            List<Integer> nomonitortypes = new ArrayList<>(pollutiontypes);
            nomonitortypes.add(meteo);//不包含气象点
            paramMap.put("nomonitortypes", nomonitortypes);
            List<Map<String, Object>> types = videoDeviceService.getAllEnvMonitorTypeList(paramMap);
            paramMap.put("monitorpointtypecodes", types.stream().map(item -> item.get("monitorpointtypecode")).collect(Collectors.toSet()));

            LinkedList<Map<String, Object>> videoAndOutPutInfos = videoDeviceService.getMonitorTypeVideoInfos(paramMap);
            List<Map<String, Object>> resultList = videoAndOutPutInfos.stream().filter(m -> m != null).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取视频树形结构（按企业）
     * @Param: customname:企业或摄像头名称查询 ，isneedhigh：是否有高空视频（默认有）
     * @return:
     * @Author: lip
     * @Date: 2022/10/13 9:04
     */

    @RequestMapping(value = "getVideoTreeDataForEnt", method = RequestMethod.POST)
    public Object getVideoTreeDataForEnt(@RequestJson(value = "pkpollutionid", required = false) String pkpollutionid,
                                         @RequestJson(value = "customname", required = false) String customname,
                                         @RequestJson(value = "isneedhigh", required = false) String isneedhigh) throws Exception {
        try {

            List<Map<String, Object>> isUseTypeList = pollutionService.getIsUseMonitorPointTypeData();
            List<String> types = isUseTypeList.stream().filter(m -> m.get("code") != null)
                    .map(m -> m.get("code").toString()).collect(Collectors.toList());
            if (StringUtils.isBlank(isneedhigh)||"true".equals(isneedhigh)){
                types.add("high");
            }
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (types.size()>0){
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("customname",customname);
                paramMap.put("pkpollutionid",pkpollutionid);
                paramMap.put("types",types);
                resultList = videoDeviceService.getVideoTreeDataForEntByParam(paramMap);
                //排序：根据orderindex
                if (resultList.size()>0){
                    resultList = resultList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/1/18 0018 下午 3:26
     * @Description: 获取所有企业相关视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getPollutionAllVideoInfos", method = RequestMethod.POST)
    public Object getPollutionAllVideoInfos(@RequestJson(value = "pkpollutionid", required = false) String pkpollutionid) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("pkpollutionid", pkpollutionid);
            paramMap.put("categorys", categorys);

            LinkedList<Map<String, Object>> videoAndOutPutInfos = videoDeviceService.getPollutionAllVideoInfos(paramMap);
            List<Map<String, Object>> resultList = videoAndOutPutInfos.stream().filter(m -> m != null).collect(Collectors.toList());
            /*List<Map<String, Object>> highvideo = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && "高空瞭望视频".equals(m.get("label").toString())).collect(Collectors.toList());
            if (highvideo.size() == 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", UUID.randomUUID().toString());
                data.put("label", "高空瞭望视频");
                data.put("branchcode", "high");
                data.put("childrendata", new ArrayList<>());
                videoAndOutPutInfos.addFirst(data);
            }*/
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取高空瞭望视频信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/6 13:44
     */
    @RequestMapping(value = "getHighVideoInfos", method = RequestMethod.POST)
    public Object getHighVideoInfos() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();

            LinkedList<Map<String, Object>> videoAndOutPutInfos = videoDeviceService.getHighVideoInfos(paramMap);
            List<Map<String, Object>> highvideo = videoAndOutPutInfos.stream().filter(m -> m.get("label") != null && "高空瞭望视频".equals(m.get("label").toString())).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", highvideo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

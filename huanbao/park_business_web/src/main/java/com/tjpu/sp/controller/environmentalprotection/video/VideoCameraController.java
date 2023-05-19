package com.tjpu.sp.controller.environmentalprotection.video;


import cn.hutool.core.lang.Assert;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.environmentalprotection.video.VideoCameraVO;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: xsm
 * @date: 2019年5月23日 上午8:57:52
 * @Description:摄像头处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("videoCamera")
@Api(value = "摄像头处理类", tags = "摄像头处理类")
public class VideoCameraController {
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private VideoCameraService videoCameraService;

    private String sysmodel = "videocamera";
    private String pk_id = "pk_vediocameraid";

    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author: xsm
     * @date: 2019/5/24 上午11:18
     * @Description: 获取摄像头新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVideoCameraAddPage", method = RequestMethod.POST)
    public Object getVideoCameraAddPage() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/24 上午11:20
     * @Description: 新增摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addVideoCamera", method = RequestMethod.POST)
    public Object addVideoCamera(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
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
     * @author: xsm
     * @date: 2019/5/24 上午11:21
     * @Description: 根据主键ID获取摄像头信息修改页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVideoCameraUpdatePageByID", method = RequestMethod.POST)
    public Object getVideoCameraUpdatePageByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @date: 2019/5/24 上午11:24
     * @Description: 修改摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateVideoCamera", method = RequestMethod.POST)
    public Object updateVideoCamera(HttpServletRequest request) throws Exception {
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
     * @date: 2022/9/6 上午11:24
     * @Description: 修改摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateVideoCameraByParam", method = RequestMethod.POST)
    public Object updateVideoCameraByParam(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            VideoCameraVO cameraVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new VideoCameraVO());
            Assert.notNull(cameraVO.getPkVediocameraid(), "pkVediocameraid不能为空");
            videoCameraService.updateVideoCamera(cameraVO);
            return AuthUtil.parseJsonKeyToLower("success", cameraVO.getPkVediocameraid());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/24 上午11:24
     * @Description: 根据摄像头信息主键ID删除单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteVideoCameraByID", method = RequestMethod.POST)
    public Object deleteVideoCameraByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: mmt
     * @date: 2022/9/6 上午11:24
     * @Description: 根据摄像头信息主键ID删除单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteVideoCameras", method = RequestMethod.POST)
    public Object deleteVideoCameras(@RequestJson(value = "id") String id) throws Exception {
        try {
            videoCameraService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/24 上午11:27
     * @Description: 根据摄像头信息主键ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVideoCameraDetailByID", method = RequestMethod.POST)
    public Object getVideoCameraDetailByID(@RequestJson(value = "id", required = true) String id) throws Exception {
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
     * @date: 2022/9/6 上午11:27
     * @Description: 根据摄像头信息主键ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getVideoCameraDetails", method = RequestMethod.POST)
    public Object getVideoCameraDetails(@RequestJson(value = "id") String id) throws Exception {
        try {
            return AuthUtil.parseJsonKeyToLower("success", videoCameraService.info(id));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/5/27  下午 1:15
     * @Description: 根据摄像头名称和监测点ID以及硬盘ID判断该摄像头是否重复（重复验证）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/isTableDataHaveInfoByParamMap", method = RequestMethod.POST)
    public Object isTableDataHaveInfoByParamMap(@RequestJson(value = "vediocameraname", required = true) String vediocameraname,
                                                @RequestJson(value = "monitorpointid", required = false) String monitorcode,
                                                @RequestJson(value = "videodeviceid", required = false) String videodeviceid) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("vediocameraname", vediocameraname);
            paramMap.put("fkMonitorpointoroutputid", monitorcode);
            paramMap.put("videodeviceid", videodeviceid);
            List<Map<String, Object>> value = videoCameraService.isTableDataHaveInfoByParamMap(paramMap);
            //当监测点ID和硬盘录像机ID都为空时（同时添加硬盘录像机和摄像头）
            if (value.size() == 0) {    //等于0 没有此条数据可以添加
                return AuthUtil.parseJsonKeyToLower("success", "no");
            } else {    //已经有了不添加
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/29  下午 2:48
     * @Description: 获取所有高空瞭望
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getAllHighAltitudeVideoInfos", method = RequestMethod.POST)
    public Object getAllHighAltitudeVideoInfos() {
        try {
            List<Map<String, Object>> value = videoCameraService.getAllHighAltitudeVideos();
            //当监测点ID和硬盘录像机ID都为空时（同时添加硬盘录像机和摄像头）
            return AuthUtil.parseJsonKeyToLower("success", value);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/11/18  下午 6:25
     * @Description: 根据监测点ID和类型获取视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getVideoCameraInfosByMonitorPointIDAndType", method = RequestMethod.POST)
    public Object getVideoCameraInfosByMonitorPointIDAndType(
            @RequestJson(value = "monitorpointid",required = false) String monitorpointid,
            @RequestJson(value = "monitorpointtype",required = false) String monitorpointtype,
            @RequestJson(value = "pollutionid",required = false) String pollutionid) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("pollutionid", pollutionid);
            if (pollutionid!=null){
                List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
                paramMap.put("monitorpointtype", pollutiontypes);
            }
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> valuedata = videoCameraService.getVideoCameraInfosByMonitorPointIDAndType(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", valuedata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/24  下午 3:51
     * @Description: 根据企业id和排口id获取视频摄像头信息(app)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getVideoCameraInfosByPollutionIdAndOutPutId", method = RequestMethod.POST)
    public Object getVideoCameraInfosByPollutionIdAndOutPutId(
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "outputid", required = false) String outputid) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> valuedata = videoCameraService.getVideoCameraInfosByPollutionIdAndOutPutId(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", valuedata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/27 0027 13:44
     * @Description: 根据污染源id获取点位、视频组合树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     **/
    @RequestMapping(value = "getPollutionVideoTreeByPollutionid", method = RequestMethod.POST)
    public Object getPollutionVideoTreeByPollutionid(@RequestJson(value = "pollutionid", required = true) String pollutionid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fkpollutionid", pollutionid);
            Map<String, Object> resultMap = videoCameraService.getPollutionVideoTreeByPollutionid(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/20 0020 下午 2:24
     * @Description: 通过自定义参数获取企业相关视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getPollutionVideoCameraInfosByParamMap", method = RequestMethod.POST)
    public Object getPollutionVideoCameraInfosByParamMap(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                         @RequestJson(value = "outputid", required = false) String outputid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> pollutionVideoCameraInfosByParamMap = videoCameraService.getPollutionVideoCameraInfosByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", pollutionVideoCameraInfosByParamMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/9/21 0021 下午 3:09
     * @Description: 获取点位视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVideoCameraDataById", method = RequestMethod.POST)
    public Object getVideoCameraDataById(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("videoid", id);
            List<Map<String, Object>> dataList = videoCameraService.getVideoCameraInfoByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/05/08  下午 1:46
     * @Description: 获取所有在大屏展示的摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getIsShowVideoCameraInfos", method = RequestMethod.POST)
    public Object getIsShowVideoCameraInfos() {
        try {
            List<Map<String, Object>> valuedata = videoCameraService.getIsShowVideoCameraInfos();
            return AuthUtil.parseJsonKeyToLower("success", valuedata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取污水处理厂相关视频信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/14 9:31
     */
    @RequestMapping(value = "/getWaterHandleVideoList", method = RequestMethod.POST)
    public Object getWaterHandleVideoList() {
        try {
            List<String> pollutionIds = videoCameraService.getWaterHandleEntIds();
            List<Map<String,Object>> resultList = new ArrayList<>();
            if (pollutionIds.size()>0){
                Map<String,Object> paramMap = new HashMap<>();
                paramMap.put("pollutionids",pollutionIds);
                resultList = videoCameraService.getVideoListByParam(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/10/26 0026 下午 4:38
     * @Description:  获取生产场所，储罐区，仓库视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, areaids]
     * @throws:
     */
    @RequestMapping(value = "/getMajorHazardSourceVideoByParamMap", method = RequestMethod.POST)
    public Object getMajorHazardSourceVideoByParamMap(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                      @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                      @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                      @RequestJson(value = "areaids", required = false) List<String> areaids) {
        try {
            Map<String,Object> resultMap=new HashMap<>();
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("monitorpointtypes",monitorpointtypes);
            paramMap.put("areaids",areaids);
            if(pagesize!=null && pagenum!=null){
                PageHelper.startPage(pagenum,pagesize);
            }
            List<Map<String, Object>> valuedata = videoCameraService.getMajorHazardSourceVideoByParamMap(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(valuedata);
            long total = pageInfo.getTotal();
            resultMap.put("total",total);
            resultMap.put("datalist",valuedata);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/11/30 0030 下午 3:18
     * @Description: 通过视频类别获取视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [categorys]
     * @throws:
     */
    @RequestMapping(value = "/getVideoCameraCategoryInfoByParamMap", method = RequestMethod.POST)
    public Object getVideoCameraCategoryInfoByParamMap(@RequestJson(value = "categorys", required = false) Object categorys) {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("categorys",categorys);
            List<Map<String, Object>> datalist = videoCameraService.getVideoCameraCategoryInfoByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

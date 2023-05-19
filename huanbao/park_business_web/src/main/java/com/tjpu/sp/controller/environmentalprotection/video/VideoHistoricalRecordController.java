package com.tjpu.sp.controller.environmentalprotection.video;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: xsm
 * @date: 2020年2月12日 下午14:53:52
 * @Description:摄像头历史视频处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("videoHistoricalRecord")
@Api(value = "摄像头历史视频处理类", tags = "摄像头历史视频处理类")
public class VideoHistoricalRecordController {
    @Autowired
    private VideoCameraService videoCameraService;



    /**
     * @author: xsm
     * @date: 2020/02/12  下午 2:56
     * @Description: 根据自定义参数获取视频摄像头列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getVideoCameraInfosByParamMap", method = RequestMethod.POST)
    public Object getVideoCameraInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = videoCameraService.getVideoCameraInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/13  上午 8:29
     * @Description: 根据自定义参数获取某一个摄像头历史视频列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getVideoHistoricalRecordInfosByParamMap", method = RequestMethod.POST)
    public Object getVideoHistoricalRecordInfosByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = videoCameraService.getVideoHistoricalRecordInfosByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/13  上午 8:29
     * @Description: 根据历史视频主键ID和绝对路径删除文件及该条记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/deleteVideoHistoricalRecordInfoByParamMap", method = RequestMethod.POST)
    public Object deleteVideoHistoricalRecordInfoByParamMap(@RequestJson(value = "pkid") String pkid,
                                                            @RequestJson(value = "filepath") String filepath) {
        try {
            String rootpath=DataFormatUtil.parseProperties("rootpath");//获取配置的根目录
            File file = new File(rootpath+filepath);
            // 判断目录或文件是否存在
            if (file.exists()) {  // 存在就删除视频
                file.delete();
            }
            //删除记录
            videoCameraService.deleteVideoHistoricalRecordInfoByID(pkid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/3/2 0002 上午 11:30
     * @Description: 获取视频信息，如果有报警包含报警时间信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "/getvideocamerinfo", method = RequestMethod.POST)
    public Object getvideocamerinfo(@RequestJson(value = "pagesize",required = false) Integer pagesize,
                                    @RequestJson(value = "pagenum",required = false) Integer pagenum) {
        try {
            Map<String,Object> resultMap=new HashMap<>();
            if(pagesize==null && pagenum==null){
                pagesize=Integer.MAX_VALUE;
                pagenum=1;
            }
            List<Map<String, Object>> getvideocamerinfo = videoCameraService.getvideocamerinfo(new HashMap<>());
            resultMap.put("total",getvideocamerinfo.size());
            resultMap.put("datalist",getvideocamerinfo.stream().skip((pagenum-1)*pagesize).limit(pagesize).collect(Collectors.toList()));

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

package com.tjpu.sp.controller.environmentalprotection.video;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.video.VideoOverlayVO;
import com.tjpu.sp.service.environmentalprotection.video.VideoOverlayService;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author: xsm
 * @date: 2020年2月18日 上午11:34
 * @Description:视频叠加处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("videoOverlay")
@Api(value = "视频叠加处理类", tags = "视频叠加处理类")
public class VideoOverlayController {
    @Autowired
    private VideoOverlayService videoOverlayService;
    @Autowired
    private RabbitmqController rabbitmqController;


    /**
     * @author: xsm
     * @date: 2020/02/18  上午 11:47
     * @Description: 根据摄像头ID获取相关视频叠加配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getVideoOverlayInfosByVedioCameraID", method = RequestMethod.POST)
    public Object getVideoOverlayInfosByVedioCameraID(@RequestJson(value = "vediocameraid") String vediocameraid) {
        try {
            List<Map<String, Object>> datalist = videoOverlayService.getVideoOverlayInfosByVedioCameraID(vediocameraid);
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcodes",new HashSet<>());
            map.put("overlayposition","");
            if (datalist!=null&&datalist.size()>0){
                Set<String> set1= new HashSet<>();
                String str= "";
                for(Map<String, Object> obj:datalist){
                    if (obj.get("FK_PollutantCode")!=null) {
                        set1.add(obj.get("FK_PollutantCode").toString());
                    }
                    if (obj.get("OverlayPosition")!=null) {
                        str = obj.get("OverlayPosition").toString();
                    }
                }
                map.put("pollutantcodes",set1);
                map.put("overlayposition",str);
            }
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/18  下午 14:07
     * @Description: 保存视频叠加配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/addVideoOverlayInfo", method = RequestMethod.POST)
    public Object addVideoOverlayInfo(@RequestJson(value = "vediocameraid") String vediocameraid,
                                      @RequestJson(value = "overlayposition", required = false) String overlayposition,
                                      @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                      HttpServletRequest request, HttpSession session) {
        try {
            String sessionId = session.getId();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            List<VideoOverlayVO> addlist = new ArrayList<>();
            JSONObject jsonObject = new JSONObject();
            //当配置信息不为空时
            if (overlayposition!=null&&!"".equals(overlayposition)&&pollutantcodes!=null&&pollutantcodes.size()>0){
                int i =1;
                for (String code:pollutantcodes) {
                    VideoOverlayVO obj = new VideoOverlayVO();
                    obj.setPkVediooverlayid(UUID.randomUUID().toString());
                    obj.setFkVediocameraid(vediocameraid);
                    obj.setFkPollutantcode(code);
                    obj.setOrderindex(i);
                    obj.setOverlayposition(overlayposition);
                    obj.setUpdateuser(username);
                    obj.setUpdatetime(new Date());
                    addlist.add(obj);
                    i++;
                }
                //判断有无视频叠加配置信息
                List<Map<String, Object>> datalist = videoOverlayService.getVideoOverlayInfosByVedioCameraID(vediocameraid);
                boolean flag = false;
                if (datalist!=null&&datalist.size()>0){
                    flag =true;
                }
                videoOverlayService.addVideoOverlayInfo(vediocameraid,addlist);
                if (flag==false) {//属于新增 则发送消息到队列
                    jsonObject.put("operate", "add");//新增
                }else{
                    jsonObject.put("operate", "update");//修改
                }
            }else{
                if (overlayposition!=null&&!"".equals(overlayposition)){
                    //判断有无视频叠加配置信息
                    List<Map<String, Object>> datalist = videoOverlayService.getVideoOverlayInfosByVedioCameraID(vediocameraid);
                    boolean flag = false;
                    if (datalist!=null&&datalist.size()>0){
                        flag =true;
                    }
                    VideoOverlayVO obj = new VideoOverlayVO();
                    obj.setPkVediooverlayid(UUID.randomUUID().toString());
                    obj.setFkVediocameraid(vediocameraid);
                    obj.setOrderindex(1);
                    obj.setOverlayposition(overlayposition);
                    obj.setUpdateuser(username);
                    obj.setUpdatetime(new Date());
                    videoOverlayService.addNoPollutanVideoOverlayInfo(vediocameraid,obj);
                    if (flag==false) {//属于新增 则发送消息到队列
                        jsonObject.put("operate", "add");//新增
                    }else{
                        jsonObject.put("operate", "update");//修改
                    }
                } else{
                    videoOverlayService.deleteVideoOverlayInfoByVedioCameraID(vediocameraid);
                    jsonObject.put("operate", "delete");//删除
                }
            }
            //发送消息到队列
                jsonObject.put("vediocameraid", vediocameraid);
                jsonObject.put("datetime", DataFormatUtil.getDateYMDHMS(new Date()));
                rabbitmqController.sendVideoOverlayDirectQueue(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/18  下午 17:32
     * @Description: 根据监测类型获取相关因子信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getPollutantInfoByMonitorPointIdAndMonitorType", method = RequestMethod.POST)
    public Object getPollutantInfoByMonitorPointIdAndMonitorType(@RequestJson(value = "monitorpointid") String monitorpointid,
                                      @RequestJson(value = "pollutanttype") Integer pollutanttype
                                    ) {
        try {
            List<Map<String,Object>> datalist = new ArrayList<>();
            if (pollutanttype!= CommonTypeEnum.MonitorPointTypeEnum.StorageRoomAreaEnum.getCode()){
                datalist = videoOverlayService.getPollutantInfoByMonitorPointIdAndMonitorType(monitorpointid,pollutanttype);
            }
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

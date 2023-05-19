package com.tjpu.sp.controller.environmentalprotection.video;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.video.VideoRecordPlanVO;
import com.tjpu.sp.service.environmentalprotection.video.VideoRecordPlanService;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author: xsm
 * @date: 2020年2月19日 下午16:45
 * @Description:视频录制计划处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("videoRecordPlan")
@Api(value = "视频录制计划处理类", tags = "视频录制计划处理类")
public class VideoRecordPlanController {
    @Autowired
    private VideoRecordPlanService videoRecordPlanService;


    /**
     * @author: xsm
     * @date: 2020/02/19  下午 16:55
     * @Description: 根据摄像头ID获取视频录制计划信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getVideoRecordPlanInfoByVideoCameraID", method = RequestMethod.POST)
    public Object getVideoRecordPlanInfoByVideoCameraID(@RequestJson(value = "vediocameraid") String vediocameraid) {
        try {
            Map<String, Object> result = videoRecordPlanService.getVideoRecordPlanInfoByVideoCameraID(vediocameraid);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/19  下午 16:57
     * @Description: 保存视频录制计划信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/addVideoRecordPlanInfo", method = RequestMethod.POST)
    public Object addVideoRecordPlanInfo(HttpServletRequest request ) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            JSONObject jsonObject = JSONObject.fromObject(paramMap.get("addformdata"));

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            VideoRecordPlanVO obj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new VideoRecordPlanVO());
            obj.setUpdatetime(new Date());
            obj.setUpdateuser(username);
            //判断有无视频叠加配置信息
            Map<String, Object> map = videoRecordPlanService.getVideoRecordPlanInfoByVideoCameraID(obj.getFkVediocameraid());
            if (map!=null){
                obj.setPkVideorecordplanid(map.get("PK_VideoRecordPlanID").toString());
                videoRecordPlanService.updateVideoRecordPlanInfo(obj);
            }else{
                obj.setPkVideorecordplanid(UUID.randomUUID().toString());
                videoRecordPlanService.addVideoRecordPlanInfo(obj);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

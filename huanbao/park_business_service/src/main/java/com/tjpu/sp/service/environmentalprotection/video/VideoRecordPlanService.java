package com.tjpu.sp.service.environmentalprotection.video;


import com.tjpu.sp.model.environmentalprotection.video.VideoRecordPlanVO;

import java.util.Map;

public interface VideoRecordPlanService {

    /**
     * @author: xsm
     * @date: 2020/02/19  下午 17:12
     * @Description: 根据摄像头ID获取相关视频录像计划信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getVideoRecordPlanInfoByVideoCameraID(String vediocameraid);

    /**
     * @author: xsm
     * @date: 2020/02/19  下午 17:15
     * @Description: 保存视频录制计划信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void addVideoRecordPlanInfo(VideoRecordPlanVO obj);

    /**
     * @author: xsm
     * @date: 2020/02/19  下午 17:15
     * @Description: 修改视频录制计划信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void updateVideoRecordPlanInfo(VideoRecordPlanVO obj);


}

package com.tjpu.sp.service.impl.environmentalprotection.video;


import com.tjpu.sp.dao.environmentalprotection.video.VideoRecordPlanMapper;
import com.tjpu.sp.model.environmentalprotection.video.VideoRecordPlanVO;
import com.tjpu.sp.service.environmentalprotection.video.VideoRecordPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class VideoRecordPlanServiceImpl implements VideoRecordPlanService {


    @Autowired
    private VideoRecordPlanMapper videoRecordPlanMapper;


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
    @Override
    public Map<String, Object> getVideoRecordPlanInfoByVideoCameraID(String vediocameraid) {
        return videoRecordPlanMapper.getVideoRecordPlanInfoByVideoCameraID(vediocameraid);
    }

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
    @Override
    public void addVideoRecordPlanInfo(VideoRecordPlanVO obj) {
        videoRecordPlanMapper.insert(obj);
    }


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
    @Override
    public void updateVideoRecordPlanInfo(VideoRecordPlanVO obj) {
        videoRecordPlanMapper.updateByPrimaryKey(obj);
    }
}

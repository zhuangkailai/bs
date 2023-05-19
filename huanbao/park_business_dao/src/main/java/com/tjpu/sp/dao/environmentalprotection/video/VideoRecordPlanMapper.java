package com.tjpu.sp.dao.environmentalprotection.video;

import com.tjpu.sp.model.environmentalprotection.video.VideoRecordPlanVO;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface VideoRecordPlanMapper {
    int deleteByPrimaryKey(String pkVideorecordplanid);

    int insert(VideoRecordPlanVO record);

    int insertSelective(VideoRecordPlanVO record);

    VideoRecordPlanVO selectByPrimaryKey(String pkVideorecordplanid);

    int updateByPrimaryKeySelective(VideoRecordPlanVO record);

    int updateByPrimaryKey(VideoRecordPlanVO record);

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
    Map<String,Object> getVideoRecordPlanInfoByVideoCameraID(@Param("vediocameraid") String vediocameraid);
}
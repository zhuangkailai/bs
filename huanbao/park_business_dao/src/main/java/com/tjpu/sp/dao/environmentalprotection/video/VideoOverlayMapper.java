package com.tjpu.sp.dao.environmentalprotection.video;

import com.tjpu.sp.model.environmentalprotection.video.VideoOverlayVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface VideoOverlayMapper {
    int deleteByPrimaryKey(String pkVediooverlayid);

    int insert(VideoOverlayVO record);

    int insertSelective(VideoOverlayVO record);

    VideoOverlayVO selectByPrimaryKey(String pkVediooverlayid);

    int updateByPrimaryKeySelective(VideoOverlayVO record);

    int updateByPrimaryKey(VideoOverlayVO record);

    List<Map<String,Object>> getVideoOverlayInfosByVedioCameraID(@Param("vediocameraid") String vediocameraid);

    void deleteByVedioCameraID(String vediocameraid);

    void batchInsert(@Param("list")List<VideoOverlayVO> addlist);

    List<Map<String,Object>> getPollutantInfoByMonitorPointIdAndMonitorType(Map<String, Object> param);

    List<Map<String, Object>> getPollutionVideoInfoAndAlarmDataByParams(Map<String, Object> paramMap);


    List<Map<String, Object>> getSecurityVideoAlarmDataByParams(Map<String, Object> paramMap);

    List<Map<String, Object>> countSecurityVideoAlarmDataByParams(Map<String, Object> paramMap);
}
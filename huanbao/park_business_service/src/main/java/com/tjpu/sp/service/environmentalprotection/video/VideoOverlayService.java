package com.tjpu.sp.service.environmentalprotection.video;


import com.tjpu.sp.model.environmentalprotection.video.VideoOverlayVO;

import java.util.List;
import java.util.Map;

public interface VideoOverlayService {

    /**
     * @author: xsm
     * @date: 2020/02/18  下午 13:09
     * @Description: 根据摄像头ID获取相关视频叠加配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVideoOverlayInfosByVedioCameraID(String vediocameraid);

    /**
     * @author: xsm
     * @date: 2020/02/18  下午 14:22
     * @Description: 保存视频叠加配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void addVideoOverlayInfo(String vediocameraid, List<VideoOverlayVO> addlist);

    /**
     * @author: xsm
     * @date: 2020/02/18  下午 17:40
     * @Description: 根据监测类型和监测点ID获取监测点监测污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getPollutantInfoByMonitorPointIdAndMonitorType(String monitorpointid, Integer pollutanttype);

    /**
     * @author: xsm
     * @date: 2020/02/19  上午 12:05
     * @Description: 根据摄像头ID删除视频叠加配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deleteVideoOverlayInfoByVedioCameraID(String vediocameraid);

    void addNoPollutanVideoOverlayInfo(String vediocameraid, VideoOverlayVO obj);
}

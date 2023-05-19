package com.tjpu.sp.service.impl.environmentalprotection.video;


import com.tjpu.sp.dao.environmentalprotection.video.VideoOverlayMapper;
import com.tjpu.sp.model.environmentalprotection.video.VideoOverlayVO;
import com.tjpu.sp.service.environmentalprotection.video.VideoOverlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VideoOverlayServiceImpl implements VideoOverlayService {


    @Autowired
    private VideoOverlayMapper videoOverlayMapper;


    @Override
    public List<Map<String, Object>> getVideoOverlayInfosByVedioCameraID(String vediocameraid) {
        return videoOverlayMapper.getVideoOverlayInfosByVedioCameraID(vediocameraid);
    }

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
    @Override
    public void addVideoOverlayInfo(String vediocameraid, List<VideoOverlayVO> addlist) {
        videoOverlayMapper.deleteByVedioCameraID(vediocameraid);
        videoOverlayMapper.batchInsert(addlist);
    }

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
    @Override
    public List<Map<String, Object>> getPollutantInfoByMonitorPointIdAndMonitorType(String monitorpointid, Integer pollutanttype) {
        Map<String,Object> param = new HashMap<>();
        param.put("monitorpointid",monitorpointid);
        param.put("pollutanttype",pollutanttype);
        return videoOverlayMapper.getPollutantInfoByMonitorPointIdAndMonitorType(param);
    }

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
    @Override
    public void deleteVideoOverlayInfoByVedioCameraID(String vediocameraid) {
        videoOverlayMapper.deleteByVedioCameraID(vediocameraid);
    }

    @Override
    public void addNoPollutanVideoOverlayInfo(String vediocameraid, VideoOverlayVO obj) {
            videoOverlayMapper.deleteByVedioCameraID(vediocameraid);
            videoOverlayMapper.insert(obj);
    }
}

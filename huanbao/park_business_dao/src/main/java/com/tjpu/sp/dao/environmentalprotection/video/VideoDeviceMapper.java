package com.tjpu.sp.dao.environmentalprotection.video;

import com.tjpu.sp.model.environmentalprotection.video.VideoDeviceVO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
@Repository
public interface VideoDeviceMapper {
    int deleteByPrimaryKey(String pkVediodeviceid);

    int insert(VideoDeviceVO record);

    int insertSelective(VideoDeviceVO record);

    VideoDeviceVO selectByPrimaryKey(String pkVediodeviceid);

    int updateByPrimaryKeySelective(VideoDeviceVO record);

    int updateByPrimaryKey(VideoDeviceVO record);

    List<VideoDeviceVO> getAllVideoDevice();

    /**
     * @author: chengzq
     * @date: 2019/9/25 0025 上午 9:07
     * @Description: 获取视频和排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    LinkedList<Map<String, Object>> getVideoAndOutPutInfos();


    LinkedList<Map<String, Object>> getVideoAndOutPutsInfos(Map<String, Object> paramMap);

    LinkedList<Map<String, Object>> getPollutionVideoInfos(Map<String, Object> paramMap);

    LinkedList<Map<String, Object>> getMonitorTypeVideoInfos(Map<String, Object> paramMap);

    LinkedList<Map<String, Object>> getPollutionAllVideoInfos(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 下午 5:08
     * @Description: 根据自定义参数获取硬盘录像机信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> selectVideoDeviceByparamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllEnvMonitorTypeList(Map<String, Object> paramMap);

    LinkedList<Map<String, Object>> getHighVideoInfos(Map<String, Object> paramMap);

    List<HashMap<String, Object>> getAllVideoDeviceNames();

    List<Map<String, Object>> getWaterVideoDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getGasVideoDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getAirVideoDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterQVideoDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getOtherVideoDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getHighVideoDataListByParam(Map<String, Object> paramMap);
}
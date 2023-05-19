package com.tjpu.sp.service.environmentalprotection.video;


import com.tjpu.sp.model.environmentalprotection.video.VideoDeviceVO;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface VideoDeviceService {

    /**
     * @return
     * @author: xsm
     * @date: 2019年06月17日 上午 11:25
     * @Description: 获取硬盘录像机和摄像头组合树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    List<Map<String, Object>> getVideoDeviceAndVideoCameraTree(Map<String, Object> paramMap);

    /**
     * @return
     * @author: xsm
     * @date: 2019年06月17日 上午 11:27
     * @Description: 添加硬盘录像机信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    void saveVideoDeviceInfo(VideoDeviceVO obj);

    /**
     * @return
     * @author: xsm
     * @date: 2019年06月17日 下午 2:32
     * @Description: 修改硬盘录像机信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    void editVideoDeviceInfo(VideoDeviceVO obj);


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

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 下午 4:50
     * @Description: 根据硬盘录像机ID和自定义参数删除硬盘录像机
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    int VideoDeviceByID(String id, String monitorcode, String monitorpointtype);

    LinkedList<Map<String, Object>> getPollutionAllVideoInfos(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllEnvMonitorTypeList(Map<String, Object> paramMap);

    LinkedList<Map<String, Object>> getHighVideoInfos(Map<String, Object> paramMap);

    VideoDeviceVO info(String id);

    int updateVideoDeviceByParam(VideoDeviceVO videoDeviceVO);

    List<HashMap<String,Object>> getAllVideoDeviceNames();

    List<Map<String, Object>> getVideoTreeDataForEntByParam(Map<String, Object> paramMap);
}

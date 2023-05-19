package com.tjpu.sp.service.environmentalprotection.video;


import com.tjpu.sp.model.environmentalprotection.video.VideoCameraVO;

import java.util.List;
import java.util.Map;

public interface VideoCameraService {

    long countTotalByParam(Map<String, Object> paramMap);

    int saveVideoCamera(VideoCameraVO obj);

    int updateVideoCamera(VideoCameraVO obj);

    List<Map<String, Object>> isTableDataHaveInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/21 0021 下午 5:40
     * @Description: 获取所有视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllMonitorVideoInfos();

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 下午 5:40
     * @Description: gis-获取所有视频摄像头信息和在线状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getAllMonitorVideoInfo();

    /**
     * @author: xsm
     * @date: 2019/10/29  下午 2:48
     * @Description: 获取所有高空瞭望
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllHighAltitudeVideos();

    /**
     * @author: xsm
     * @date: 2019/11/18  下午 1:06
     * @Description: 根据监测点ID删除点位下的视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     * @param parammap
     */
    void deleteVideoCameraByParamMap(Map<String, Object>  parammap);
    /**
     * @author: mmt
     * @date: 2022/9/6  下午 1:06
     * @Description: 根据监测点ID删除点位下的视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     * @param id
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/11/18  下午 6:25
     * @Description: 根据监测点ID和类型获取视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVideoCameraInfosByMonitorPointIDAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/24  下午 3:51
     * @Description: 根据企业id和排口id获取视频摄像头信息(app)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVideoCameraInfosByPollutionIdAndOutPutId(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/27   下午 1:54
     * @Description: 根据企业id获取和企业相关的视频信息（安全）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getPollutionVideoTreeByPollutionid(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/1/7  0007 上午 11:23
     * @Description: 根据监测类型获取该类型所有视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVideoInfoByMonitorpointType(Integer monitorpointtype);


    /**
     * @author: chengzq
     * @date: 2020/1/17 0017 上午 11:51
     * @Description: 通过自定义参数获取视频
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getVideoCameraInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/1/17  0017 下午 2:55
     * @Description: 根据自定义参数获取相关视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVideoMonitoringAlarmDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/1/19  0019 上午 10:20
     * @Description: 根据自定义参数统计监控报警条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Long countVideoListDataNumByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/1/19  0019 上午 10:44
     * @Description: 根据自定义参数获取监控详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVedioMonitoringAlarmDetailDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/12  0012 下午 15:05
     * @Description: 根据自定义参数获取视频摄像头列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVideoCameraInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/02/13  上午 8:29
     * @Description: 根据自定义参数获取某一个摄像头历史视频列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVideoHistoricalRecordInfosByParamMap(Map<String, Object> paramMap);

    void deleteVideoHistoricalRecordInfoByID(String pkid);

    /**
     * @author: chengzq
     * @date: 2020/3/1 0001 下午 10:06
     * @Description: 通过自定义参数获取视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getvideocamerinfo(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/05/08  下午 1:46
     * @Description: 获取所有在大屏展示的摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getIsShowVideoCameraInfos();

    /**
     * @author: xsm
     * @date: 2020/06/23  下午 2:15
     * @Description: 获取所有视频信息并按监测点ID分组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getAllVideoInfoGroupByPointID();

    /**
     * @author: xsm
     * @date: 2020/08/20  下午 3:51
     * @Description: 根据企业id获取企业下安全视频摄像头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getSecurityVideoCameraInfosByPollutionId(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/10/26 0026 下午 4:34
     * @Description: 获取生产场所，储罐区，仓库视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getMajorHazardSourceVideoByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2020/11/20 0020 下午 2:23
     * @Description: 通过自定义参数获取企业相关视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutionVideoCameraInfosByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2020/11/30 0030 下午 3:15
     * @Description: 通过视频类别获取视频信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getVideoCameraCategoryInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/04/27 0027 下午 1:50
     * @Description: 统计按企业分组的视频报警数据条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> countVideoAlarmDataNumGroupByPollution(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/04/27 0027 下午 1:50
     * @Description: 统计按日期分组的视频报警数据条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> countVideoAlarmDataNumGroupByDate(Map<String, Object> paramMap);

    List<Map<String,Object>> getVideoInfoByMonitorpointTypes(List<Integer> monitorpointtypes);

    List<String> getWaterHandleEntIds();

    List<Map<String, Object>> getVideoListByParam(Map<String, Object> paramMap);

    Map<String, Object> info(String id);
}

package com.tjpu.sp.service.impl.environmentalprotection.video;


import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.video.VideoCameraMapper;
import com.tjpu.sp.dao.environmentalprotection.video.VideoDeviceMapper;
import com.tjpu.sp.model.environmentalprotection.video.VideoCameraVO;
import com.tjpu.sp.model.environmentalprotection.video.VideoDeviceVO;
import com.tjpu.sp.service.environmentalprotection.video.VideoDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt;


@Service
@Transactional
public class VideoDeviceServiceImpl implements VideoDeviceService {

    @Autowired
    private VideoDeviceMapper videoDeviceMapper;
    @Autowired
    private VideoCameraMapper videoCameraMapper;


    /**
     * @return
     * @author: xsm
     * @date: 2018年11月14日 下午4:25:01
     * @Description: 获取硬盘录像机和摄像头组合树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public List<Map<String, Object>> getVideoDeviceAndVideoCameraTree(Map<String, Object> paramMap) {
        //获取所有硬盘录像机信息
        final List<VideoDeviceVO> videoDeviceTreeData = videoDeviceMapper.getAllVideoDevice();
        final List<VideoCameraVO> videocameraTreeData = videoCameraMapper.getAllVideoCamera(paramMap);
        List<Map<String, Object>> treedata = new ArrayList<Map<String, Object>>();
        if (videoDeviceTreeData.size() > 0) {
            for (VideoDeviceVO device : videoDeviceTreeData) {
                Map<String, Object> devicemap = new LinkedHashMap<>();
                String id = device.getPkVediodeviceid();
                String name = device.getVediodevicename();
                devicemap.put("id", id);
                devicemap.put("label", name);
                devicemap.put("type", "E");
                List<Map<String, Object>> cameradata = new ArrayList<Map<String, Object>>();
                if (videocameraTreeData.size() > 0) {
                    for (VideoCameraVO camera : videocameraTreeData) {
                        String fkdeviceid = camera.getFkVediodeviceid();
                        if (id.equalsIgnoreCase(fkdeviceid)) {
                            Map<String, Object> map = new LinkedHashMap<>();
                            map.put("id", camera.getPkVediocameraid());
                            map.put("label", camera.getVediocameraname());
                            map.put("type", "V");
                            cameradata.add(map);
                        }
                    }
                }
                if (cameradata != null && cameradata.size() > 0) {
                    devicemap.put("children", cameradata);
                    treedata.add(devicemap);
                }
            }
        }
        return treedata;
    }

    /**
     * @return
     * @author: xsm
     * @date: 2019年06月17日 上午 11:27
     * @Description: 添加硬盘录像机信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public void saveVideoDeviceInfo(VideoDeviceVO obj) {
        videoDeviceMapper.insert(obj);
    }

    /**
     * @return
     * @author: xsm
     * @date: 2019年06月17日 下午 2:32
     * @Description: 修改硬盘录像机信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public void editVideoDeviceInfo(VideoDeviceVO obj) {
        VideoDeviceVO videodevice = videoDeviceMapper.selectByPrimaryKey(obj.getPkVediodeviceid());
        obj.setVediodevicename(videodevice.getVediodevicename());
        videoDeviceMapper.updateByPrimaryKey(obj);
    }

    /**
     * @author: chengzq
     * @date: 2019/9/25 0025 上午 9:12
     * @Description: 获取视频和排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public LinkedList<Map<String, Object>> getVideoAndOutPutInfos() {
        return videoDeviceMapper.getVideoAndOutPutInfos();
    }

    @Override
    public LinkedList<Map<String, Object>> getVideoAndOutPutsInfos(Map<String, Object> paramMap) {
        return videoDeviceMapper.getVideoAndOutPutsInfos(paramMap);
    }

    @Override
    public LinkedList<Map<String, Object>> getPollutionVideoInfos(Map<String, Object> paramMap) {
        return videoDeviceMapper.getPollutionVideoInfos(paramMap);
    }

    @Override
    public LinkedList<Map<String, Object>> getMonitorTypeVideoInfos(Map<String, Object> paramMap) {
        return videoDeviceMapper.getMonitorTypeVideoInfos(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 下午 4:50
     * @Description: 根据硬盘录像机ID和自定义参数删除硬盘录像机
     * @updateUser:xsm
     * @updateDate:2021/6/7 下午 2:04
     * @updateDescription:删除硬盘录像机下只关联企业的视频
     * @param: []
     * @throws:
     */
    @Override
    public int VideoDeviceByID(String id, String monitorcode, String monitorpointtype) {
        int i = 0;
        try {
            Map<String, Object> paramMap = new HashMap<>();
            //判断该硬盘录像机是否有关联点位
            if (monitorcode != null && !"".equals(monitorcode) && monitorpointtype != null && !"".equals(monitorpointtype)) {//当不关联监测点时
                //查询监测点不为空的硬盘录像机时
                paramMap.put("videodeviceid", id);
                paramMap.put("monitorcode", monitorcode);
                paramMap.put("fkMonitorpointoroutputid", monitorcode);
                paramMap.put("monitorpointtype", monitorpointtype);
                List<Map<String, Object>> listdata = videoDeviceMapper.selectVideoDeviceByparamMap(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    //只删除摄像头
                    videoCameraMapper.deleteByVideoDeviceParamMap(paramMap);
                } else {
                    //删除硬盘录像机
                    videoDeviceMapper.deleteByPrimaryKey(id);
                    //删除摄像头
                    videoCameraMapper.deleteByVideoDevice(id);
                }
            } else {
                //查询为监测点为空的硬盘录像机
                paramMap.put("videodeviceid", id);
                List<Map<String, Object>> listdata = videoDeviceMapper.selectVideoDeviceByparamMap(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    if (monitorpointtype != null && "other".equals(monitorpointtype)) {
                        //删除该硬盘录像机下 企业关联视频
                        paramMap.put("videodeviceid", id);
                        videoCameraMapper.deletePollutionVideoDeviceParamMap(paramMap);
                    } else {//非企业其它视频
                        //只删除摄像头
                        //删除摄像头
                        paramMap.put("monitorcode", monitorcode);
                        paramMap.put("monitorpointtype", monitorpointtype);
                        videoCameraMapper.deleteByVideoDeviceParamMap(paramMap);
                    }
                } else {
                    //删除硬盘录像机
                    videoDeviceMapper.deleteByPrimaryKey(id);
                    //删除摄像头
                    videoCameraMapper.deleteByVideoDevice(id);
                }


            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }


    @Override
    public LinkedList<Map<String, Object>> getPollutionAllVideoInfos(Map<String, Object> paramMap) {
        return videoDeviceMapper.getPollutionAllVideoInfos(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAllEnvMonitorTypeList(Map<String, Object> paramMap) {
        return videoDeviceMapper.getAllEnvMonitorTypeList(paramMap);
    }

    @Override
    public LinkedList<Map<String, Object>> getHighVideoInfos(Map<String, Object> paramMap) {
        return videoDeviceMapper.getHighVideoInfos(paramMap);
    }

    @Override
    public VideoDeviceVO info(String id) {
        return videoDeviceMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateVideoDeviceByParam(VideoDeviceVO videoDeviceVO) {
        return videoDeviceMapper.updateByPrimaryKeySelective(videoDeviceVO);
    }

    @Override
    public List<HashMap<String, Object>> getAllVideoDeviceNames() {
        return videoDeviceMapper.getAllVideoDeviceNames();
    }

    @Override
    public List<Map<String, Object>> getVideoTreeDataForEntByParam(Map<String, Object> paramMap) {
        List<String> types = (List<String>) paramMap.get("types");
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String type : types) {
            resultList.addAll(getTreeDataListForEnt(type, paramMap));
        }
        return resultList;
    }

    private List<Map<String, Object>> getTreeDataListForEnt(String type, Map<String, Object> paramMap) {

        List<Map<String, Object>> dataList;
        if (type.equals("high")) {
            dataList = videoDeviceMapper.getHighVideoDataListByParam(paramMap);
        } else {
            int monitorType = Integer.parseInt(type);
            switch (getCodeByInt(monitorType)) {
                case WasteWaterEnum:
                case RainEnum:
                    paramMap.put("monitorpointtype", monitorType);
                    dataList = videoDeviceMapper.getWaterVideoDataListByParam(paramMap);
                    break;
                case WasteGasEnum:
                case SmokeEnum:
                    dataList = videoDeviceMapper.getGasVideoDataListByParam(paramMap);
                    break;
                case AirEnum:
                    paramMap.put("id", monitorType);
                    paramMap.put("monitorpointtype", monitorType);
                    dataList = videoDeviceMapper.getAirVideoDataListByParam(paramMap);
                    break;
                case WaterQualityEnum:
                    paramMap.put("monitorpointtype", monitorType);
                    paramMap.put("id", monitorType);
                    dataList = videoDeviceMapper.getWaterQVideoDataListByParam(paramMap);
                    break;
                default:
                    List<Integer> otherType = CommonTypeEnum.getOtherMonitorPointTypeList();
                    if (otherType.contains(monitorType)){
                        paramMap.put("id", monitorType);
                        paramMap.put("monitorpointtype", monitorType);
                        dataList = videoDeviceMapper.getOtherVideoDataListByParam(paramMap);
                    }else {
                        dataList = new ArrayList<>();
                    }
                    break;
            }
        }
        return dataList;

    }

}

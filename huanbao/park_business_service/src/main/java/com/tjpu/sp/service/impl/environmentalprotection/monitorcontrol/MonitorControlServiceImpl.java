package com.tjpu.sp.service.impl.environmentalprotection.monitorcontrol;

import com.github.pagehelper.PageHelper;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.monitorcontrol.MonitorPointMonitorControlMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterOutputInfoMapper;
import com.tjpu.sp.model.environmentalprotection.monitorcontrol.MonitorPointMonitorControlVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;
import com.tjpu.sp.service.environmentalprotection.monitorcontrol.MonitorControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class MonitorControlServiceImpl implements MonitorControlService {

    @Autowired
    private MonitorPointMonitorControlMapper monitorPointMonitorControlMapper;

    @Autowired
    private WaterOutputInfoMapper waterOutputInfoMapper;

    /**
     * @author: lip
     * @date: 2019/11/28 0028 下午 1:22
     * @Description: 自定义查询参数获取点位监控配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointMonitorControlInfo(Map<String, Object> paramMap) {
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            PageHelper.startPage(Integer.valueOf(paramMap.get("pagenum").toString()), Integer.valueOf(paramMap.get("pagesize").toString()));
        }
        List<Map<String, Object>> dataList = monitorPointMonitorControlMapper.getMaxTimeDataByParam(paramMap);
        return dataList;
    }


    /**
     * @author: lip
     * @date: 2019/11/27 0027 下午 7:25
     * @Description: 更新实体
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateEntity(MonitorPointMonitorControlVO monitorPointMonitorControlVO) {
        monitorPointMonitorControlMapper.updateByPrimaryKey(monitorPointMonitorControlVO);

    }


    /**
     * @author: lip
     * @date: 2019/11/28 0028 下午 1:41
     * @Description: 自定义查询条件获取监测控制记录表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorControlLogDataByParamMap(Map<String, Object> paramMap) {
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            PageHelper.startPage(Integer.valueOf(paramMap.get("pagenum").toString()), Integer.valueOf(paramMap.get("pagesize").toString()));
        }
        List<Map<String, Object>> dataList = monitorPointMonitorControlMapper.getMonitorPointMonitorControlByParam(paramMap);
        return dataList;
    }



    @Override
    public MonitorPointMonitorControlVO selectByPrimaryKey(String pkid) {
        return monitorPointMonitorControlMapper.selectByPrimaryKey(pkid);
    }

    /**
     * @author: lip
     * @date: 2019/12/5 0005 上午 9:57
     * @Description: 自定义查询条件获取点位和启动停止时间数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, List<Map<String, Object>>> getMonitorPointIdAndTimesByParam(Map<String, Object> paramMap) {
        Map<String, List<Map<String, Object>>> idAndTimes = new HashMap<>();
        List<Map<String, Object>> timeList;

        String startTime = paramMap.get("starttime").toString();
        String endTime = paramMap.get("endtime").toString();

        String monitorpointid;
        String startmointortime;
        String stopmointortime;
        List<Map<String, Object>> dataList = monitorPointMonitorControlMapper.getMonitorPointIdAndTimesByParam(paramMap);
        for (Map<String, Object> map : dataList) {
            monitorpointid = map.get("monitorpointid").toString();
            startmointortime = map.get("startmointortime").toString();
            if(map.get("stopmointortime")!=null){
                stopmointortime = map.get("stopmointortime").toString();
            }else {
                stopmointortime = endTime;
            }
            if (DataFormatUtil.getDateYMDHMS(startTime).before(DataFormatUtil.getDateYMDHMS(startmointortime))
                    &&DataFormatUtil.getDateYMDHMS(endTime).after(DataFormatUtil.getDateYMDHMS(startmointortime))){
                if (DataFormatUtil.getDateYMDHMS(startmointortime).before(DataFormatUtil.getDateYMDHMS(startTime))) {
                    startmointortime = startTime;
                }
                if (DataFormatUtil.getDateYMDHMS(endTime).before(DataFormatUtil.getDateYMDHMS(stopmointortime))) {
                    stopmointortime = endTime;
                }
                if (idAndTimes.containsKey(monitorpointid)) {
                    timeList = idAndTimes.get(monitorpointid);
                } else {
                    timeList = new ArrayList<>();
                }
                Map<String, Object> time = new HashMap<>();
                time.put("starttime", startmointortime);
                time.put("endtime", stopmointortime);
                timeList.add(time);
                idAndTimes.put(monitorpointid, timeList);
            }



        }
        return idAndTimes;
    }

    /**
     *
     * @author: xsm
     * @date: 2019/12/26 0026 下午 1:45
     * @Description: 修改雨水排口点位状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateRainOutPutStatusByParam(Map<String, Object> param) {
        monitorPointMonitorControlMapper.updateRainOutPutStatusByParam(param);
    }
    /**
     * @author: lip
     * @date: 2019/11/27 0027 下午 7:25
     * @Description: 添加最新记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void insert(MonitorPointMonitorControlVO monitorPointMonitorControlVO) {
        monitorPointMonitorControlMapper.insert(monitorPointMonitorControlVO);
    }
    /**
     *
     * @author: lip
     * @date: 2020/2/26 0026 上午 10:33
     * @Description: 自定义查询条件获取最新停止监测时间数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getLastEndTimeByParamMap(Map<String, Object> paramMap) {
        return monitorPointMonitorControlMapper.getLastEndTimeByParamMap(paramMap);
    }
    /**
     *
     * @author: lip
     * @date: 2020/2/26 0026 上午 10:54
     * @Description: 删除记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void deleteEntity(String pkid) {
        monitorPointMonitorControlMapper.deleteByPrimaryKey(pkid);
    }
    /**
     *
     * @author: lip
     * @date: 2020/2/26 0026 上午 11:08
     * @Description: 根据主键获取记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getMonitorControlInfoById(String id) {
        return monitorPointMonitorControlMapper.getMonitorControlInfoById(id);
    }

    @Override
    public WaterOutputInfoVO getWaterOutputInfoVOById(String monitorpointid) {
        return waterOutputInfoMapper.selectByPrimaryKey(monitorpointid);
    }

    /**
     *
     * @author: xsm
     * @date: 2020/3/02 0002 上午 10:47
     * @Description: 获取当前未排放的雨水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getCurrentTimeMonitorControlInfoByParamMap(Map<String, Object> paramMap) {
        return monitorPointMonitorControlMapper.getCurrentTimeMonitorControlInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String,Object>> getNowRainMonitorControlInfo(Map<String, Object> parammap) {
        return monitorPointMonitorControlMapper.getNowRainMonitorControlInfoByParamMap(parammap);
    }

    @Override
    public List<Map<String, Object>> getMonitorControlHistoryLogDataByParamMap(Map<String, Object> parammap) {
        return monitorPointMonitorControlMapper.getMonitorControlHistoryLogDataByParamMap(parammap);
    }
}

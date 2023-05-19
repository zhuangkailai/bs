package com.tjpu.sp.service.environmentalprotection.monitorcontrol;


import com.tjpu.sp.model.environmentalprotection.monitorcontrol.MonitorPointMonitorControlVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;

import java.util.List;
import java.util.Map;

public interface MonitorControlService {


    /**
     * @author: chengzq
     * @date: 2019/11/27 0027 下午 7:15
     * @Description: 通过自定义参数获取雨水监测控制数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getMonitorPointMonitorControlInfo(Map<String, Object> paramMap);

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
    void updateEntity(MonitorPointMonitorControlVO monitorPointMonitorControlVO);



    List<Map<String,Object>> getMonitorControlLogDataByParamMap(Map<String, Object> jsonObject);



    MonitorPointMonitorControlVO selectByPrimaryKey(String pkid);
    /**
     *
     * @author: lip
     * @date: 2019/12/5 0005 上午 9:57
     * @Description: 自定义查询条件获取点位和启动停止时间数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    Map<String,List<Map<String,Object>>> getMonitorPointIdAndTimesByParam(Map<String, Object> paramMap);

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
    void updateRainOutPutStatusByParam(Map<String, Object> param);

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
    void insert(MonitorPointMonitorControlVO monitorPointMonitorControlVO);

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
    Map<String,Object> getLastEndTimeByParamMap(Map<String, Object> paramMap);

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
    void deleteEntity(String pkid);

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
    Map<String,Object> getMonitorControlInfoById(String id);
    /**
     *
     * @author: lip
     * @date: 2020/2/26 0026 上午 11:44
     * @Description: 获取点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    WaterOutputInfoVO getWaterOutputInfoVOById(String monitorpointid);

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
    List<Map<String,Object>> getCurrentTimeMonitorControlInfoByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: xsm
     * @date: 2020/3/18 0018 下午 3:24
     * @Description: 获取当前正在排放的雨水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getNowRainMonitorControlInfo(Map<String, Object> parammap);

    List<Map<String,Object>> getMonitorControlHistoryLogDataByParamMap(Map<String, Object> jsonObject);
}

package com.tjpu.sp.dao.environmentalprotection.monitorcontrol;

import com.tjpu.sp.model.environmentalprotection.monitorcontrol.MonitorPointMonitorControlVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface MonitorPointMonitorControlMapper {
    int insert(MonitorPointMonitorControlVO record);

    int insertSelective(MonitorPointMonitorControlVO record);

    /**
     * @author: xsm
     * @date: 2019/11/27 0027 下午 6:41
     * @Description: 根据监测点ID和监测类型获取监测控制信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getMonitorPointMonitorControlByParam(Map<String, Object> paramMap);

    void updateByPrimaryKey(MonitorPointMonitorControlVO monitorPointMonitorControlVO);



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
    List<Map<String,Object>> getMonitorPointMonitorControlInfo(Map<String,Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/11/28 0028 下午 1:41
     * @Description: 获取最新监控配置数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getMaxTimeDataByParam(Map<String, Object> paramMap);

    MonitorPointMonitorControlVO selectByPrimaryKey(String pkid);
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
    List<Map<String,Object>> getMonitorPointIdAndTimesByParam(Map<String, Object> paramMap);

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

    void deleteByPrimaryKey(String pkid);

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

    List<Map<String,Object>> getNowRainMonitorControlInfoByParamMap(Map<String, Object> parammap);

    List<Map<String,Object>> getMonitorControlHistoryLogDataByParamMap(Map<String, Object> parammap);
}
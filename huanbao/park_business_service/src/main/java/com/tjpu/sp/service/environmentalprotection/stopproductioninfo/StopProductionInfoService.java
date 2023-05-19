package com.tjpu.sp.service.environmentalprotection.stopproductioninfo;


import com.tjpu.sp.model.environmentalprotection.stopproductioninfo.MessageReadUserVO;
import com.tjpu.sp.model.environmentalprotection.stopproductioninfo.StopProductionInfoVO;

import java.util.List;
import java.util.Map;

public interface StopProductionInfoService {

    /**
     * @author: xsm
     * @date: 2019/12/18 0018 下午 6:36
     * @Description: 根据自定义参数获取停产排口列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    List<Map<String,Object>> getStopProductionInfosByParamMap(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/18 0018 下午 7:14
     * @Description: 修改点位状态为停用
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    void updatePollutionOutPutStatusByParam(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2019/12/18 0018 下午 7:14
     * @Description: 新增一条停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    void addStopProductionInfo(StopProductionInfoVO entity);

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 上午 8:37
     * @Description: 根据主键ID获取停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [stopproductionid]
     * @throws:
     */
    StopProductionInfoVO getStopProductionInfoByPkid(String stopproductionid);

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 上午 8:39
     * @Description: 修改停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [entity]
     * @throws:
     */
    void editProductionInfo(StopProductionInfoVO entity);

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 上午 9:17
     * @Description: 根据自定义参数获取停产历史信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getStopProductionHistoryInfosByParamMap(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/02/25 0025 上午 10:20
     * @Description: 根据自定义参数获取最新一条停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getLatestStopProductionInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/25 0025 下午 3:23
     * @Description: 根据id获取停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getStopProductionInfoByID(String id);

    StopProductionInfoVO selectByPrimaryKey(String id);

    void updateStopProductionInfo(StopProductionInfoVO entity);

    /**
     * @author: xsm
     * @date: 2020/3/02 0002 上午 10:30
     * @Description: 根据自定义参数获取正在停产的排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getCurrentTimeStopProductionInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/3/9 0009 下午 5:23
     * @Description: 通过自定义参数获取最新的排口停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getLastStopProductionInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/18 0018 下午 14:51
     * @Description: 通过自定义参数获取正在停产的排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getNowStopProductionInfosByParamMap(Map<String, Object> parammap);

    void addMessageReadUserInfo(MessageReadUserVO obj);


    /**
     * @author: chengzq
     * @date: 2020/9/2 0002 下午 6:45
     * @Description: 通过自定义条件获取所有停产历史数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    List<Map<String,Object>> getStopProductHistory(Map<String, Object> parammap);

    List<Map<String,Object>> getStopProductionListDataByParamMap(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2020/9/03 0003 上午 11:11
     * @Description: 批量添加停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void insertStopProductionInfos(List<StopProductionInfoVO> objs);

    /**
     * @author: xsm
     * @date: 2020/9/03 0003 上午 11:11
     * @Description: 根据停产主键ID数组获取多条停产记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getStopProductionInfoByPkids(List<String> ids);

    List<StopProductionInfoVO> selectByPrimaryKeys(List<String> pkids);

    void deleteStopProductionInfoByIDs(List<String> pkids);

    Map<String,Object> getReproductionInfoByIDs(List<String> pkids);

    List<Map<String,Object>> getHistoryStopProductionDetailByIDs(List<String> pkids);

    List<Map<String,Object>> getEntStopProductionInfosByParamMap(Map<String, Object> parammap);

    void addEntStopProductionInfo(StopProductionInfoVO obj);

    Map<String,Object> getEntStopProductionDetailByID(String id);

    void deleteEntStopProductionInfoByID(String id);

    void updateEntStopProductionInfo(StopProductionInfoVO obj);
}

package com.tjpu.sp.dao.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface OtherMonitorPointMapper {
    int deleteByPrimaryKey(String pkMonitorpointid);

    int insert(OtherMonitorPointVO record);

    int insertSelective(OtherMonitorPointVO record);

    OtherMonitorPointVO selectByPrimaryKey(String pkMonitorpointid);

    int updateByPrimaryKeySelective(OtherMonitorPointVO record);

    int updateByPrimaryKey(OtherMonitorPointVO record);

    /**
     * @author: lip
     * @date: 2018/9/11 0011 下午 3:57
     * @Description: 根据其他设备类型统计设备个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> countOtherEquipmentPointByType(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/5/29 15:31
     * @Description: 动态条件获取其他监测点信息
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOnlineOtherPointInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> isTableDataHaveInfoByParamMap(Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2019/6/11 0011 下午8:36
     * @Description: 通过监测点名称和监测点类型获取该类型监测点的基础信息及点位状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @throws:
     */
    List<Map<String, Object>> getOtherMonitorPointInfoAndStateByparamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getOtherMonitorPointAllPollutantsByIDAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/12 0012 下午 4:54
     * @Description: 通过id获取其它监测点的监测设备状态基础信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    Map<String, Object> getOtherMonitorPointDeviceStatusByID(Map<String, Object> paramMap);

    List<Map<String, Object>> getfileIdsByID(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2019/6/21 0021 下午 5:33
     * @Description: 获取所有VOC点位信息及状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllMonitorEnvironmentalVocAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2019/6/21 0021 下午 5:40
     * @Description: 获取所有恶臭点位信息及状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllMonitorEnvironmentalStinkAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2019/6/26 0026 下午 6:31
     * @Description: 根据监测点ID和监测点类型获取该监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOtherMonitorPointInfoByIDAndType(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 上午 10:57
     * @Description: 查询所有恶臭及厂界恶臭监测点信息包含状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getStenchMonitorPointInfo(Map<String, Object> paramMap);
    List<Map<String, Object>> getMicroStationInfo(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 下午 2:18
     * @Description: 通过监测点集合查询恶臭及厂界恶臭污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getStenchPollutantMonitorPointids(Map<String, Object> paramMap);
    List<Map<String, Object>> getMicroStationPollutantMonitorPointids(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 上午 10:20
     * @Description: 通过监测点Dgimn号查询空气Dgimn号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn]
     * @throws:
     */
    List<String> getAirDgimnByMonitorDgimn(String dgimn);

    /**
     * @author: xsm
     * @date: 2019/7/8 0008 下午 2:26
     * @Description: 根据监测点名称和监测点类型以及MN号获取其它监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    Map<String, Object> selectOtherMonitorPointInfoByParams(Map<String, Object> params);

    /**
     * @author: xsm
     * @date: 2019/7/13 0013 下午 1:02
     * @Description: 根据监测点类型和自定义参数获取其它监测点某类型所有监测点的MN号和污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    List<Map<String, Object>> getOtherMonitorPointDgimnAndPollutantInfosByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/19 0019 下午 3:19
     * @Description: 获取所有恶臭监测点的信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    List<Map<String, Object>> getAllStenchMonitorPointInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 下午 3:30
     * @Description: 根据MN号获取监测点信息（恶臭、voc）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getMonitorPointInfoByMns(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/7/30 15:37
     * @Description: 条件查询其他监测点企业、排口、污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOtherMonitorPollutionOutPutPollutants(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 7:46
     * @Description: 通过自定义参数获取监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorInfoByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 9:57
     * @Description: 根据监测时间获取所有恶臭、voc、厂界恶臭的MN号和关联气象的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTraceSourceMonitorPointMN(Map<String, Object> param);

    /**
     * @author: zhangzc
     * @date: 2019/9/4 15:24
     * @Description: 获取恶臭监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getStinkMonitorPoint(@Param(value = "type") int code);


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 3:58
     * @Description: 通过味道code和mn集合获取恶臭监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    List<Map<String, Object>> selectStinkInfoBySmellcodeAndMns(Map<String, Object> param);

    /**
     *
     * @author: lip
     * @date: 2019/10/30 0030 下午 2:40
     * @Description: 自定义查询条件获取空气站点和其他监测点mn号数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getAirMNAndOtherMNByParam(Map<String, Object> paramMap);


    List<Map<String,Object>> getTransportChannelMonitorPointInfos();
    /**
     * @author: zhangzhenchao
     * @date: 2019/11/4 15:00
     * @Description: 根据mn号获取各个mn号监测的污染物
     * @param:
     * @return:
     * @throws:
     */
    List<Map<String,String>> getMonitorPollutantByParam(@Param("mns") List<String> mns);

    /**
     * @author: xsm
     * @date: 2019/11/23 0023 上午 11:51
     * @Description: 根据恶臭MN号获取关联的空气Mn号（无关联则取自身，包括厂界恶臭和环境恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<String> getAirDgimnByStinkMonitorDgimn(String dgimn);

    /**
     * @author: xsm
     * @date: 2020/04/09 0009 上午 10:47
     * @Description: 获取所有气象点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAllMonitorEnvironmentalMeteoAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2020/04/09 0009 上午 10:47
     * @Description: 根据自定义参数获取溯源气象点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getTraceSourceMeteoMonitorPointMN(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2020/6/11 0011 下午 2:11
     * @Description: 获取所有微站点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllMonitorMicroStationAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2020/8/25 0025 上午 11:05
     * @Description: 根据恶臭标记类型获取相关恶臭点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getVocPollutantFactorGroupData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/11/18 0018 上午 8:51
     * @Description: 根据因子组获取该因子组下所有因子信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAllVocPollutantByParam(Map<String, Object> param);

    /**
     *
     * @author: lip
     * @date: 2020/11/30 0030 上午 9:46
     * @Description: 获取恶臭（环境恶臭、厂界恶臭点位数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getStinkPointDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getStinkPollutantSetDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getOthorPointInfoByPointType(Map<String, Object> paramMap);

    List<Map<String,Object>> getVocPollutantDataByFactorGroups(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutantStandardDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getStinkAndVocMonitorPointInfos();

    List<PollutantSetDataVO> getPollutantSetDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getTraceSourceMonitorPointInfoByParam(Map<String, Object> paramMap);

    void setTimeDataByParam(Map<String, Object> updateMap);

    List<Map<String, Object>> getAllStinkPointDataList();

    List<Map<String,Object>> getAllOnlineOtherPointInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getOtherPointInfoAndPollutantsByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllMonitorPointAndStatusInfo(Map<String, Object> param);

    List<Map<String,Object>> getOtherPointInfoAndAirMNByParamMap(Map<String, Object> paramMap);

    long countTotalByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getVocRelationDgimnByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getDataListMapByParam(JSONObject jsonObject);

    Map<String, Object> getEditOrViewDataById(String id);

    List<Map<String, Object>> getWaterOutputPointInfo(HashMap<String, Object> paramMap);
}
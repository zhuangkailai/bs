package com.tjpu.sp.dao.base.pollution;

import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface PollutionMapper {

    int deleteByPrimaryKey(String pkPollutionid);

    int insert(PollutionVO record);

    int insertSelective(PollutionVO record);

    PollutionVO selectByPrimaryKey(String pkPollutionid);

    int updateByPrimaryKeySelective(PollutionVO record);

    int updateByPrimaryKey(PollutionVO record);

    /**
     * @author: zhangzc
     * @date: 2019/5/9 16:01
     * @Description: 按行业类型统计企业分布情况
     * @param:
     * @return:
     */
    List<Map<String, Object>> getEnterpriseForIndustry();

    /**
     * @author: chengzq
     * @date: 2019/5/20 0020 上午 11:05
     * @Description: 通过企业id获取废水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getWaterOutPutInfoByPollutionid(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/20 0020 上午 11:05
     * @Description: 通过企业id获取废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getGasOutPutInfoByPollutionid(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/21 0021 下午 3:55
     * @Description: 自定义条件，查询污染源总数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    long countTotalByParam(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 10:54
     * @Description: 通过自定义参数获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [jsonObject]
     * @throws:
     */
    List<Map<String, Object>> getPollutionsInfoByParamMap(Map<String, Object> jsonObject);

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 3:30
     * @Description: 通过污染源id查询污染源及标签
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    PollutionVO getPollutionAndLabelsByPollutionid(String id);

    /**
     * @author: zhangzc
     * @date: 2019/5/27 13:21
     * @Description: 获取所有污染源名称
     * @param:
     * @return:
     */
    List<String> getPollutionNames();

    /**
     * @author: chengzq
     * @date: 2019/5/29 0029 下午 7:34
     * @Description: 通过id获取污染源详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    PollutionVO getDetailById(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/12 0012 下午 2:11
     * @Description: 自定义查询条件按污染标签类型分组统计污染源
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> countPollutionForPollutionLabelByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/13 0013 下午 1:40
     * @Description: 获取按标签类型分组的污染源标签信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutionLabelsGroupByLabelType();

    /**
     * @author: chengzq
     * @date: 2020/10/21 0021 下午 3:17
     * @Description: 通过企业id获取企业用户
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<String> getUsersByPollution(@Param("fkpollutionid") String fkpollutionid);

    /**
     * @author: chengzq
     * @date: 2020/10/21 0021 下午 3:17
     * @Description: 通过企业id获取企业用户id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<String> getUserInfoByPollution(@Param("fkpollutionid") String fkpollutionid);

    /**
     * @author: lip
     * @date: 2019/6/14 0014 下午 2:58
     * @Description: 获取企业下停产的排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getStopProductionOutPutByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/15 0015 下午 3:48
     * @Description: 通过污染源id删除视频
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    int deleteVideoCameraByPollutionId(String pollutionid);


    /**
     * @author: chengzq
     * @date: 2019/6/15 0015 下午 3:49
     * @Description: 通过污染源id删除检测设备
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    int deleteMonitorEquipmentByPollutionId(String pollutionid);


    /**
     * @author: chengzq
     * @date: 2019/6/17 0017 下午 6:08
     * @Description: 通过污染源id查询废水，废气，无组织监测点文件id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    List<String> getImgIdByPollutionid(String id);

    /**
     * @author: lip
     * @date: 2019/6/21 0021 上午 11:07
     * @Description: 自定义查询条件获取所有污染源下排口（废水直接排口、废水间接排口、雨水排口、废气有组织、废气无组织）信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllPollutionAndOutPut(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 3:41
     * @Description: 获取所有污染源名称和id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getPollutionNameAndPkid(Map<String,Object> paramMap);

    /**
     * @param paramMap
     * @author: lip
     * @date: 2019/7/12 0012 下午 4:20
     * @Description: 自定义查询条件获取用户企业报警关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getUserEntAlarmRelationListByParamMap(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/12 0012 下午 4:47
     * @Description: 根据用户ID删除用户企业报警关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deleteUserEntAlarmRelationDataByUserId(String userid);

    /**
     * @author: lip
     * @date: 2019/7/12 0012 下午 5:22
     * @Description: 批量插入关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void batchInsertUserEntRelation(List<UserMonitorPointRelationDataVO> userMonitorPointRelationDataVOS);

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 上午 9:01
     * @Description:获取所有包含排口的企业信息（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getSelectPollutionInfo(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/27 0027 上午 10:42
     * @Description: 根据监测类型和污染源id获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutionDataByIdAndType(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/31 0031 上午 10:06
     * @Description: 自定义查询条件获取持有排污许可证企业的信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPWXKPollutionDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/1 0001 上午 10:06
     * @Description: 自定义查询条件获取持有排污许可证企业的信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getSelectPollutionAndWaterOutputInfo(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 10:37
     * @Description: gis-获取所有污染源企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllPollutionInfo();

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 10:37
     * @Description:获取所有在线排口信息（废水、废气、雨水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOutputStatusAndOutputInfo();

    List<Map<String,Object>> getAllPollutionInfoByPollutionids(Map<String, Object> parammap);

    /**
     * @author: chengzq
     * @date: 2019/10/21 0021 上午 10:04
     * @Description: 通过污染源id获取安全管理机构信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkpollutionid]
     * @throws:
     */
    Map<String,Object> getSafeManageInfoByPollutionid(String pkpollutionid);

    /**
     * @author: chengzq
     * @date: 2019/10/21 0021 上午 10:18
     * @Description: 修改安全管理机构信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    int updateSafeManageInfo(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/10/23 0023 上午 9:40
     * @Description: 通过自定义参数获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    List<Map<String,Object>> getPollutionByParamsMap(Map<String, Object> parammap);

    /**
     * @author: chengzq
     * @date: 2019/11/6 0006 上午 9:09
     * @Description: 通过自定义参数获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    List<Map<String,Object>> getOutPutInfosByParamMap(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2019/11/06 0006 上午 9:01
     * @Description: 根据污染源ID获取污染源下所有排口的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getOutputInfosByPollutionID(@Param("pollutionid") String pollutionid);
    /**
    *@author: liyc
    *@date: 2019/11/11 0011 18:47
    *@Description: 档案首页  通过污染源的id获取污染源的基本信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    Map<String,Object> getPollutionBasicInfoByPollutionId(String pollutionid);

    /**
     * @author: chengzq
     * @date: 2019/11/25 0025 上午 9:33
     * @Description: 删除安全与企业相关数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    int deleteSecurityRelevantData(Map<String,Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/12/2 0002 下午 1:54
     * @Description:  获取监测点传输率、有效率、传输有效率列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getMonitorPointTransmissionEffectiveRateList(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 5:04
     * @Description: 通过污染因子获取企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutionInfoByPollutantcodes(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2020/1/7 0007 上午 10:26
     * @Description:  通过自定义参数获取企业最高
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> getMaxRiskLevelInfoByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2020/3/3 0003 上午 10:00
     * @Description: 根据企业ID获取监测点（废水，废气，雨水，烟气）点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getMonitorPointInfoByPollutionId(String pollutionId);
    /**
     *
     * @author: lip
     * @date: 2020/3/3 0003 上午 10:00
     * @Description: 根据企业ID获取监测点（废水，废气，雨水，烟气）监测污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getPollutantDataByPollutionId(String pollutionId);

    /**
     * @author: xsm
     * @date: 2020/03/24 0024 上午 10:02
     * @Description: 通过用户ID获取企业基本信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    PollutionVO getPollutionDetailByUserId(Map<String, Object> paramMap);

    void updatePollutionInfoByPrimaryKey(PollutionVO record);

    List<Map<String, Object>> getManyMonitorPointTransmissionEffectiveRateList(Map<String, Object> paramMap);

    List<Map<String,Object>> getIsUseMonitorPointTypeData();

    List<Map<String,Object>> getSafePollutionListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getHBPointDataList();

    List<Map<String,Object>> countComplateData(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutionInfoByParamMaps(Map<String, Object> paramMap);

    List<Map<String,Object>> getUserEntInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getPointListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutionSecurityPointMn(Map<String, Object> paramMap);

    List<Map<String,Object>> getSelectFactoryBoundaryPointInfo(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllIsUsedMonitorPointTypes();

    List<Map<String,Object>> countEntRateDataGroupByIndustryType();

    List<Map<String,Object>> getEntMonitorPointTypeByEntID(Map<String,Object> param);

    List<Map<String,Object>> getEntMonitorPointPollutantDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getEntPointMNDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getEnterpriseArchivesDataByPollutionID(String pollutionid);

    List<Map<String,Object>> getEntNewDynamicDataByPollutionID( Map<String,Object> param);

    List<Map<String,Object>> getEntNewStandingBookDataByPollutionID(Map<String, Object> param);

    List<Map<String,Object>> getEntPointInfoByEntIDAndTypes(Map<String, Object> param);

    long countTotalByLabel(String labelcode);

    long countTotalByClass(Map<String, Object> paramMap);

    List<Map<String, Object>> countTotalByIndustry();

    List<Map<String, Object>> countPWPollutionData();

    long countStopTotal(String nowDay);

    List<String> getTotalIdsByLabel(String labelcode);

    List<String> getTotalIdsByClass(Map<String, Object> paramMap);

    List<String> getStopIdsTotal(String nowDay);

    List<String> getTotalIdsByParam();

    List<Map<String, Object>> getWSPollutionList();

    List<Map<String,Object>> getEntMonitorPointTypeByParam(Map<String, Object> param);

    List<Map<String,Object>> countEntStandingBookDataByPollutionID(Map<String, Object> param);

    long countPWTotalByParam(HashMap<Object, Object> objectObjectHashMap);

    List<String> getAllPWIds();

    List<Map<String, Object>> countEntControlData();

    List<Map<String, Object>> countEntRegionData();

    List<Map<String, Object>> getEntQRListDataByParamMap(Map<String, Object> jsonObject);

    int updateQRDataByParam(Map<String, Object> param);

    void addQRDataByParam(Map<String, Object> param);

    List<Map<String, Object>> getEntLabelDataListById(String pollutionid);

    long getQJSCNumByPid(String pollutionid);

    List<Integer> getIsUseTypeList();
}
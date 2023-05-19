package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutPutPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface WaterOutputInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WaterOutputInfoVO record);

    int insertSelective(WaterOutputInfoVO record);

    WaterOutputInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WaterOutputInfoVO record);

    int updateByPrimaryKey(WaterOutputInfoVO record);

    /**
     * @author: lip
     * @date: 2018/9/11 0011 下午 2:56
     * @Description: 自定义查询参数，获取总的排口总数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    long countTotalByParam(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description:获取废水污染源下排放口及状态信息，并组合成污染源排口树形结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datamark：数据标记1-废水、3-雨水
     * @return:
     */
    List<Map<String, Object>> getPollutionWaterOuputsAndStatus(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 4:32
     * @Description: 通过自定义条件获取废水列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [jsonObject]
     * @throws:
     */
    List<Map<String, Object>> getWatreOutPutByParamMap(JSONObject jsonObject);


    /**
     * @author: chengzq
     * @date: 2019/5/24 0024 上午 8:49
     * @Description:通过排口id删除排口下关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    int deleteAssociateInfoByOutPutID(String id);


    /**
     * @author: lip
     * @date: 2019/5/28 0028 下午 3:44
     * @Description: 通过自定义条件获取废水记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getWaterOutPutsByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 下午 3:53
     * @Description:通过污染源id获取所有废水排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<WaterOutputInfoVO> selectOutputByPollutionid(String pollutionid);


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 下午 6:04
     * @Description: 通过排口类型获取废水排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    //todo water
    List<Map<String, Object>> getAllOutPutInfoByType(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:05
     * @Description: 获取所有已监测废水排口和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorWaterOutPutAndStatusInfo();

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:05
     * @Description: 获取所有已监测雨水排口和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorRainOutPutAndStatusInfo();

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 2:15
     * @Description: 通过名称和污染源id查询排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 4:16
     * @Description: 通过污染源id查询排口名称和主键
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> selectByPollutionid(String pollutionid);

    /**
     * @param paramMap
     * @author: zhangzc
     * @date: 2019/7/10 14:00
     * @Description: 获取废水排口相关污染物和企业信息(废水排放量突变预警涉及的企业排口污染物信息)
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutionWaterOutPutPollutants(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/12 0026 下午 3:24
     * @Description: 获取废水排口相关污染物和企业信息(废水异常数据报警)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getPollutionWaterOutPutPollutantsByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterOutputDgimnAndPollutantInfosByParam(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/7/26 18:01
     * @Description: 根据污染源id获取废水排口和污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getWaterOutPutAndPollutantsByID(String pollutionid);

    /**
     * @author: zhangzc
     * @date: 2019/7/26 18:01
     * @Description: 根据污染源id获取废雨水排口和污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getRainOutPutAndPollutantsByID(String pollutionid);


    /**
     * @author: xsm
     * @date: 2019/7/31 10:33
     * @Description: 根据自定义参数获取污染源在线废水排口的MN号信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getWaterOutPutDgimnsByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterOutPutPollutants(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 11:11
     * @Description: gis-获取所有废水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllWaterOrRainOutPutInfoByOutputType(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/11/4 0004 下午 1:27
     * @Description: 删除状态表垃圾数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    int deleteGarbageData();
    /**
     * @author: zhangzhenchao
     * @date: 2019/11/4 15:00
     * @Description: 根据mn号获取各个mn号监测的污染物
     * @param:
     * @return:
     * @throws:
     */
    List<Map<String, String>> getMonitorPollutantByParam(@Param("mns") List<String> mns);

    /**
     * @author: xsm
     * @date: 2020/06/17 0017 下午 2:27
     * @Description: 根据自定义参数获取废气废水排口信息及状态（包括停产状态）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getGasAndWaterOutPutAndStatusByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getRainOutPutAndStatusByParamMap(Map<String, Object> paramMap);

    void setTimeDataByParam(Map<String, Object> updateMap);

    List<Map<String, Object>> countWGPointData();

    List<String> getInOrOutPutMnListByParam(Map<String, Object> paramMap);
    List<Map<String,Object>> getInOrOutPutMnListByParams(Map<String, Object> paramMap);

    List<Map<String,Object>>  getEntWaterSupplyDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getPWOutPutSelectData(Map<String, Object> paramMap);

    List<String> getWaterHandleEntIds();

    Map<String,Object> getOutputInfoByMn(String dgimn);
}
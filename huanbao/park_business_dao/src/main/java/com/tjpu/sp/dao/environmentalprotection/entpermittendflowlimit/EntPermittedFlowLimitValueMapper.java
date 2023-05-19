package com.tjpu.sp.dao.environmentalprotection.entpermittendflowlimit;

import com.tjpu.sp.model.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface EntPermittedFlowLimitValueMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntPermittedFlowLimitValueVO record);

    int insertSelective(EntPermittedFlowLimitValueVO record);

    EntPermittedFlowLimitValueVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntPermittedFlowLimitValueVO record);

    int updateByPrimaryKey(EntPermittedFlowLimitValueVO record);

    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 上午 8:36
     * @Description: 通过污染源，监测点类型，排放年限查询企业许可排放限值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> selectByParams(Map<String,Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 2:17
     * @Description: 通过自定义参数获取企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<EntPermittedFlowLimitValueVO> getEntPermittedFlowLimitInfoByParamMap(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 4:19
     * @Description: 根据年份和类型获取配置有排放量许可预警值的所有企业下废气排口的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     */
    List<Map<String,Object>> getGasDgimnsByYearAndType(Map<String, Object> paramMap);

    List<Map<String,Object>> getEntPermittedFlowLimitInfoByYearAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 4:19
     * @Description: 根据年份和类型获取配置有排放量许可预警值的所有企业下废水排口的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     */
    List<Map<String,Object>> getWaterDgimnsByYearAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/08/13 0013 上午 9:24
     * @Description: 获取某企业下单个污染物某一年的许可排放情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     */
    Map<String,Object> getOnePollutantPermitFlowDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getEntWaterPermittedFlowLimitInfoByParamMap(Map<String, Object> param);

    void batchInsert(@Param("list") List<EntPermittedFlowLimitValueVO> listobj);

    void deleteEntWaterFlowInfoByIDAndCode(Map<String, Object> param);

    List<Map<String,Object>> getEntAllOutputData(Map<String, Object> param);

    List<Map<String,Object>> getEntOutPutPollutantData(Map<String, Object> param);
}
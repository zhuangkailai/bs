package com.tjpu.sp.service.environmentalprotection.entpermittendflowlimit;


import com.tjpu.sp.model.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueVO;

import java.util.List;
import java.util.Map;

public interface EntPermittedFlowLimitValueService {


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
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 2:57
     * @Description: 新增数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    int insert(EntPermittedFlowLimitValueVO record);


    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 3:27
     * @Description: 通过主键查询企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    EntPermittedFlowLimitValueVO selectByPrimaryKey(String pkId);

    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 3:33
     * @Description: 修改企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    int updateByPrimaryKey(EntPermittedFlowLimitValueVO record);


    /**
     * @author: chengzq
     * @date: 2019/6/27 0027 下午 3:34
     * @Description: 通过主键id删除企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    int deleteByPrimaryKey(String pkId);

    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 3:59
     * @Description: 根据年份和监测点类型获取企业许可排放限值信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     */
    List<Map<String,Object>> getEntPermittedFlowLimitInfoByYearAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 4:19
     * @Description: 根据年份和类型获取配置有排放量许可预警值的所有企业下排口的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     */
    List<Map<String,Object>> getAllDgimnsByYearAndType(Map<String, Object> paramMap);

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

    void addEntWaterPermittedFlowLimitInfo(List<EntPermittedFlowLimitValueVO> listobj);

    void deleteEntWaterFlowInfoByIDAndCode(Map<String, Object> param);

    void updateEntWaterPermittedFlowLimitInfo(String pollutionid, String pollutantcode, List<EntPermittedFlowLimitValueVO> listobj);

    List<Map<String,Object>> getEntWaterPermittedFlowInfoByIDAndCode(Map<String, Object> param);

    Map<String,Object> getEntWaterPermittedFlowInfoListPage(Map<String, Object> param);

    List<Map<String,Object>> IsHaveGasPollutantFlowYearValidByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getEntAllOutputData(Map<String, Object> param);

    Map<String,Object> getEntWaterDischargeTotalListData(Map<String, Object> param);

    Map<String,Object> getEntWaterDischargeTotalAnalysisData(Map<String, Object> param);
}

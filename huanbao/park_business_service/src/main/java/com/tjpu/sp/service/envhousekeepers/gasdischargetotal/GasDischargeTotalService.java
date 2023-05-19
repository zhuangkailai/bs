package com.tjpu.sp.service.envhousekeepers.gasdischargetotal;

import com.tjpu.sp.model.envhousekeepers.gasdischargetotal.GasDischargeTotalVO;

import java.util.List;
import java.util.Map;

public interface GasDischargeTotalService {
    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 通过自定义参数获取企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String, Object>> getGasDischargeTotalByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 根据主键ID删除企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    void deleteByPollutionIDAndPollutantCode(Map<String, Object> param);

    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 添加企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    void insert(List<GasDischargeTotalVO> listobj);
    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 更新企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void updateByPrimaryKey(String pollutionid, String pollutantcode, List<GasDischargeTotalVO> listobj);

    /**
     *
     * @author: xsm
     * @date: 2021/08/16 0016 下午 1:18
     * @Description: 根据污染源ID和污染物编码获取企业大气污染总排放许可量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getGasDischargeTotalsByParam(Map<String, Object> param);

    /**
     *
     * @author: xsm
     * @date: 2021/08/17 0017 上午 10:03
     * @Description: 获取企业大气污染总排放许可量初始化信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getGasDischargeTotalListPage(Map<String, Object> param);

    List<Map<String,Object>> IsHaveGasPollutantFlowYearValidByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 通过自定义参数查询企业近五年各排放污染物污染总排放许可量信息（废气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    Map<String,Object> getEntGasDischargeTotalListData(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 下午 4:38
     * @Description: 通过自定义参数查询企业近五年各排放污染物污染总排放及排放分析情况（废气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    Map<String,Object> getEntGasDischargeTotalAnalysisData(Map<String, Object> param);
}

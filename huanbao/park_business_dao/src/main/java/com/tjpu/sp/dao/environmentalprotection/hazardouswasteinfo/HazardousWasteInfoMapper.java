package com.tjpu.sp.dao.environmentalprotection.hazardouswasteinfo;

import com.tjpu.sp.model.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoVO;

import java.util.List;
import java.util.Map;

public interface HazardousWasteInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(HazardousWasteInfoVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(HazardousWasteInfoVO record);

    int updateByParams(HazardousWasteInfoVO record);


    /**
     * @author: chengzq
     * @date: 2020/09/22 0016 下午 2:37
     * @Description:  通过自定义参数获取危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getHazardousWasteInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/9/27 0027 上午 10:33
     * @Description: 通过自定义参数获取危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> countHazardousWasteDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/05/18 0018 上午 10:00
     * @Description: 通过自定义参数统计危废年生产、同比情况（生产、贮存、利用）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countHazardousWasteDataGroupYearByParamMap(Map<String, Object> paramMap);


    List<Map<String,Object>> countMainHazardousWasteTypeDataByParamMap(Map<String, Object> paramMap);


    List<Map<String,Object>> countHazardousWasteCharacteristicRatioData(Map<String, Object> paramMap);

    List<Map<String,Object>> countEntKeepStorageHazardousWasteRankData(Map<String, Object> paramMap);
}
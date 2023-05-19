package com.tjpu.sp.service.environmentalprotection.hazardouswasteinfo;


import com.tjpu.sp.model.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoVO;

import java.util.List;
import java.util.Map;

public interface HazardousWasteInfoService {

    int deleteByPrimaryKey(String pkId);

    int insert(HazardousWasteInfoVO record);

    int insertBatch(List<HazardousWasteInfoVO> record);

    int addAndUpdateBatch(List<HazardousWasteInfoVO> repeat,List<HazardousWasteInfoVO> unrepeat);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(HazardousWasteInfoVO record);

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
     * @date: 2020/09/22 0016 下午 2:37
     * @Description:  通过id获取危废信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> getHazardousWasteInfoDetailByID(String pkid);


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

    /**
     * @author: xsm
     * @date: 2022/05/18 0018 下午 16:34
     * @Description: 通过自定义参数统计危险特性占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countHazardousWasteCharacteristicRatioData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/05/19 0019 上午 8:49
     * @Description: 通过自定义参数统计企业贮存危废量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countEntKeepStorageHazardousWasteRankData(Map<String, Object> paramMap);
}

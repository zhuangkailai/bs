package com.tjpu.sp.service.environmentalprotection.licence;

import com.github.pagehelper.PageInfo;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface LicenceService {
    /**
     * @author: zhangzc
     * @date: 2019/5/30 9:10
     * @Description: 获取过期排污许可证个数
     * @param:
     * @return:
     */
    Integer countOverdueLicenceNum();

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 6:08
     * @Description: 获取排污许可证表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOverdueLicenceTableTitleData();

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 6:08
     * @Description: 获取过期排污许可证表格内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    PageInfo<Map<String, Object>> getOverdueLicenceTableListDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterOutPutDataListByParam(Map<String, Object> paramMap);


    List<Map<String, Object>> getGasFacilityDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterFacilityDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> gasOutListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> gasUnOutListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> PWOutOverListByParam(Map<String, Object> paramMap);


    List<Map<String, Object>> getFacilityExceptionDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> gasQOutListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> gasUnQOutListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterOutPutQDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> gasYOutListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> gasUnYOutListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterOutPutYDataListByParam(Map<String, Object> paramMap);

    PageInfo<Map<String, Object>> getEntStandingInfoByParam(JSONObject jsonObject);

    List<Map<String, Object>> getInOrOutMenuDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getInOrOutAllMenuData(Map<String, Object> paramMap);

    void setInOrOutMenuData(List<Map<String, Object>> dataList);

    List<Map<String, Object>> getPWLicenceListDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getInfoOpenByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getStandingBookRequireByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getNoiseOutputInfoByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getCorrectProvideByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getOtherTextRequireByParam(Map<String, Object> paramMap);

    PageInfo<Map<String, Object>> getLastDataListByParam(Map<String, Object> paramMap);
}

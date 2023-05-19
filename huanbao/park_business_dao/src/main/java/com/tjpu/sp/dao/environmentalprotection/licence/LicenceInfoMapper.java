package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.base.licence.LicenceInfoVO;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface LicenceInfoMapper {
    int deleteByPrimaryKey(String pkLicenceid);

    int insert(LicenceInfoVO record);

    int insertSelective(LicenceInfoVO record);

    LicenceInfoVO selectByPrimaryKey(String pkLicenceid);

    int updateByPrimaryKeySelective(LicenceInfoVO record);

    int updateByPrimaryKey(LicenceInfoVO record);

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
     * @date: 2019/6/17 0017 下午 6:24
     * @Description: 自定义查询条件获取排污许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPWLicenceListDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterOutPutDataListByParam(Map<String, Object> paramMap);


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

    List<Map<String, Object>> getProblemSourceDataByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getEntStandingInfoByParam(JSONObject jsonObject);

    List<Map<String, Object>> getInOrOutMenuDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getInOrOutAllMenuData(Map<String, Object> paramMap);

    void deleteInOrOutMenuData();

    void batchInsert(List<Map<String, Object>> dataList);

    List<Map<String, Object>> getInfoOpenByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getStandingBookRequireByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getNoiseOutputInfoByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getCorrectProvideByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getOtherTextRequireByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getLastDataListByParam(Map<String, Object> paramMap);
}
package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.SoilPointVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface SoilPointMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(SoilPointVO record);

    int insertSelective(SoilPointVO record);

    SoilPointVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(SoilPointVO record);

    int updateByPrimaryKey(SoilPointVO record);
    /**
    *@author: liyc
    *@date: 2019/12/17 0017 11:59
    *@Description: 通过自定义参数获取地下水监测点信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getSoilPointByParamMap(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2020/3/24 0024 上午 8:36
     * @Description: 获取所有土壤监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllSoilPointInfo();
    /**
     *
     * @author: lip
     * @date: 2020/3/24 0024 上午 9:11
     * @Description: 自定义查询条件获取土壤污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getSoilPollutantsByParam(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/3/26 0026 下午 2:39
     * @Description: 通过自定义参数获取土壤监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getSoilPointInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/05/20 0020 下午 2:29
     * @Description: 通过自定义参数获取土壤监测点位信息和其对监测污染物信息以及对应的污染物标准设置
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getSoilPointsAndPollutantStandard(Map<String, Object> paramMap);

    List<Map<String,Object>> getSoilPointsAndPollutantStandardInfo(Map<String, Object> paramMap);

    List<Map<String,Object>> getEntSoilPointByParamMap(Map<String, Object> paramMap);
}
package com.tjpu.sp.dao.environmentalprotection.devopsinfo;

import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface EntDevOpsInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntDevOpsInfoVO record);

    int insertSelective(EntDevOpsInfoVO record);

    EntDevOpsInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntDevOpsInfoVO record);

    int updateByPrimaryKey(EntDevOpsInfoVO record);

    /**
     * @author: xsm
     * @date: 2019/12/03 0003 下午 2:08
     * @Description: 根据自定义参数获取企业运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getEntDevOpsInfosByParamMap(Map<String, Object> paramMap);

    void batchInsert(@Param("list") List<EntDevOpsInfoVO> objlist);

    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 1:17
     * @Description: 通过自定义参数获取企业运维信息和排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutionAndOutputInfoByParamMap(Map<String, Object> paramMap);

    EntDevOpsInfoVO getEntDevOpsInfoVOByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getEntDevOpsDataByParamMap(Map<String, Object> paramMap);

    void deleteEntDevOpsInfoByTypeAndPollutionids(Map<String, Object> param);

    List<Map<String,Object>> countDevOpsPointNumGropuByUnitByParam(Map<String, Object> param);

    void deleteEntDevOpsInfoByUnitID(String id);

    List<Map<String,Object>> countDevOpsPointNumGropuByPersonnelByParam(Map<String, Object> param);

    List<Map<String,Object>> getEntDevOpsInfoListDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countDevOpsPersonnelNumDataByParam(Map<String, Object> param);

    Map<String,Object> getDevOpsMonitorPointDetailByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getDevOpsPersonnelDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getEntDevOpsHistoryDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countDevOpsPatrolNumData(Map<String, Object> param);

    List<Map<String,Object>> getDevOpsRecordStatisticsDataByParamMap(Map<String, Object> paramMap);
}
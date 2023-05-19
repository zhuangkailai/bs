package com.tjpu.sp.dao.environmentalprotection.output;

import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;
import feign.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface UserMonitorPointRelationDataMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(UserMonitorPointRelationDataVO record);

    int insertSelective(UserMonitorPointRelationDataVO record);

    UserMonitorPointRelationDataVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(UserMonitorPointRelationDataVO record);

    int updateByPrimaryKey(UserMonitorPointRelationDataVO record);


    /**
     * @author: chengzq
     * @date: 2020/4/20 0020 下午 12:08
     * @Description: 通过自定义参数获取dgimn
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [ParamMap]
     * @throws:
     */
    List<Map<String,Object>> getDGIMNByParamMap(Map<String,Object> ParamMap);


    List<Map<String,Object>> getUserMonitorPointRelationDataByParams(Map<String, Object> paramMap);

    void deleteByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllMonitorPointDataByParamMap(Map<String, Object> paramMap);

    void batchAdd(@Param("list") List<UserMonitorPointRelationDataVO> list1);

    void updataUserMonitorPointRelationDataByMnAndType(Map<String, Object> parammap);

    void deleteUserMonitorPointRelationDataByMnAndType(Map<String, Object> parammap);

    List<String> getUserIdByParamMap(Map<String, Object> parammap);

    List<UserMonitorPointRelationDataVO> getUserMonitorPointRelationData(Map<String, Object> parammap);
    List<String> getMonitorPointIDsByUserid(String userid);
}
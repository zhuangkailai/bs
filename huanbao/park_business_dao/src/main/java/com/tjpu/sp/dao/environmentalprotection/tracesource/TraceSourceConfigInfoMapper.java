package com.tjpu.sp.dao.environmentalprotection.tracesource;

import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceConfigInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TraceSourceConfigInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TraceSourceConfigInfoVO record);

    int insertSelective(TraceSourceConfigInfoVO record);

    TraceSourceConfigInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TraceSourceConfigInfoVO record);

    int updateByPrimaryKey(TraceSourceConfigInfoVO record);

    /**
     * @author: lip
     * @date: 2019/8/13 0013 下午 4:51
     * @Description: 自定义查询条件获取溯源配置属性信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTraceSourceConfigDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/28 0028 下午 6:35
     * @Description: 批量新增溯源配置属性信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void batchInsert(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/28 0028 下午 6:43
     * @Description: 获取所有溯源配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllTraceSourceConfigInfo(Map<String, Object> paramMap);

    void detleteTraceSourceConfigDataByAttributeCode(Map<String, Object> paramMap);
}
package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.GroundWaterVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface GroundWaterMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(GroundWaterVO record);

    int insertSelective(GroundWaterVO record);

    GroundWaterVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(GroundWaterVO record);

    int updateByPrimaryKey(GroundWaterVO record);

    /**
     * @author: liyc
     * @date: 2019/12/14 0014 13:10
     * @Description: 通过自定义参数获取地下水监测点信息列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     **/
    List<Map<String, Object>> getGroundWaterInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: liyc
     * @date: 2019/12/14 0014 14:53
     * @Description: 获取地下水监测点详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    Map<String, Object> getGroundWaterDetailById(String id);

    /**
     * @author: chengzq
     * @date: 2021/4/13 0013 上午 10:40
     * @Description: 动态条件获取地下水监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    List<Map<String, Object>> getOnlineGroundWaterInfoByParamMap(Map<String, Object> paramMap);
    String getTargetLevelByDgimn(@Param(value = "dgimn") String dgimn);
}
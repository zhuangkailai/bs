package com.tjpu.sp.dao.environmentalprotection.parkinfo.controlrecords;

import com.tjpu.sp.model.environmentalprotection.controlrecords.ControlRecordsVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ControlRecordsMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(ControlRecordsVO record);

    int insertSelective(ControlRecordsVO record);

    ControlRecordsVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(ControlRecordsVO record);

    int updateByPrimaryKey(ControlRecordsVO record);

    /**
     *
     * @author: lip
     * @date: 2020/5/9 0009 下午 4:58
     * @Description: 获取最新一条管控建议记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    Map<String,Object> getLastData();

    /**
     *
     * @author: lip
     * @date: 2020/5/11 0011 下午 3:45
     * @Description: 自定义查询条件获取管控建议记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getControlRecordsDataByParam(Map<String, Object> paramMap);

    Map<String, Object> getEditOrDetailById(String id);

    List<Map<String, Object>> getAllStinkPoint();
}
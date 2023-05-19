package com.tjpu.sp.dao.environmentalprotection.alarm;

import com.tjpu.sp.model.environmentalprotection.alarm.AlarmHasReadUserInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AlarmHasReadUserInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(AlarmHasReadUserInfoVO record);

    int insertSelective(AlarmHasReadUserInfoVO record);

    AlarmHasReadUserInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(AlarmHasReadUserInfoVO record);

    int updateByPrimaryKey(AlarmHasReadUserInfoVO record);

    /**
     * @author: lip
     * @date: 2019/7/16 0016 上午 11:29
     * @Description: 自定义查询条件获取报警已读信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getHasReadAlarmInfoByParams(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/7/18 14:52
     * @Description: 自定义条件查询已读报警信息返回所有字段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getHasReadAlarmInfosByParams(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/16 0016 下午 4:17
     * @Description: 批量插入
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void batchInsert(List<AlarmHasReadUserInfoVO> alarmHasReadUserInfoVOS);
}
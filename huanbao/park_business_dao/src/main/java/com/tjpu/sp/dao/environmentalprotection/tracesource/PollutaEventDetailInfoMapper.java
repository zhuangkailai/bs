package com.tjpu.sp.dao.environmentalprotection.tracesource;

import com.tjpu.sp.model.environmentalprotection.tracesource.PollutaEventDetailInfoVO;


public interface PollutaEventDetailInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PollutaEventDetailInfoVO record);

    int insertSelective(PollutaEventDetailInfoVO record);

    PollutaEventDetailInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PollutaEventDetailInfoVO record);

    int updateByPrimaryKey(PollutaEventDetailInfoVO record);

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 1:56
     * @Description: 通过污染事件id查询污染事件详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    PollutaEventDetailInfoVO selectByPolluteeventid(String id);

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 2:06
     * @Description: 通过污染事件id删除污染事件详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    int deleteByPolluteeventid(String id);

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 5:54
     * @Description: 通过污染事件id获取监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    String selectMonitorInfoByPolluteeventid(String id);



}
package com.tjpu.sp.dao.environmentalprotection.pollutantsmell;


import com.tjpu.sp.model.environmentalprotection.pollutantsmell.PollutantSmellVO;

import java.util.List;
import java.util.Map;

public interface PollutantSmellMapper {
    int deleteByPrimaryKey(String pkId);

    int deleteByCode(String code);

    int insert(PollutantSmellVO record);

    PollutantSmellVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(PollutantSmellVO record);

    int updateByCode(PollutantSmellVO record);


    /**
     * @author: chengzq
     * @date: 2019/10/26 0016 下午 2:37
     * @Description:  通过自定义参数获取污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutantSmellByParamMap(Map<String, Object> paramMap);



    /**
     * @author: chengzq
     * @date: 2019/10/26 0016 下午 2:37
     * @Description:  通过id获取污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> selectBySmellCode(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 上午 11:55
     * @Description: 通过污染物code获取味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutantcode]
     * @throws:
     */
    Map<String,Object> selectByPollutantcode(String fkpollutantcode);



    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 上午 10:05
     * @Description: 通过污染物类型获取污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutantByPollutantType(Map<String, Object> paramMap);


}
package com.tjpu.sp.service.environmentalprotection.pollutantsmell;



import com.tjpu.sp.model.environmentalprotection.pollutantsmell.PollutantSmellVO;

import java.util.List;
import java.util.Map;

public interface PollutantSmellService {

    int deleteByPrimaryKey(String pkId);

    int deleteByCode(String code);

    int insert(PollutantSmellVO record);

    int insertBatch(List<PollutantSmellVO> record);

    PollutantSmellVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(PollutantSmellVO record);
    int updateByCode(PollutantSmellVO record);

    int updateBatch(List<PollutantSmellVO> record,String smellcode);

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
     * @Description:  通过id获取污染物味道详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> getPollutantSmellDetailBySmellCode(Map<String, Object> paramMap);

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

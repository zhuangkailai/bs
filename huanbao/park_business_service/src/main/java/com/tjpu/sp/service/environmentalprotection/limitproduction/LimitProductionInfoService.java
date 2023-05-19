package com.tjpu.sp.service.environmentalprotection.limitproduction;


import com.tjpu.sp.model.environmentalprotection.limitproduction.LimitProductionDetailInfoVO;
import com.tjpu.sp.model.environmentalprotection.limitproduction.LimitProductionInfoVO;

import java.util.List;
import java.util.Map;

public interface LimitProductionInfoService {

    int deleteByPrimaryKey(String pkId);

    int insert(LimitProductionInfoVO record, List<LimitProductionDetailInfoVO> data);

    LimitProductionInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(LimitProductionInfoVO record,List<LimitProductionDetailInfoVO> data);

    /**
     * @author: chengzq
     * @date: 2019/6/24 0024 下午 7:12
     * @Description:  通过自定义参数获取排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<LimitProductionInfoVO> getLimitProductionInfoByParamMap(Map<String,Object> paramMap);



    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 上午 10:21
     * @Description: 通过id获取排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    LimitProductionInfoVO getLimitProductionInfoByID(String pkid);

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 上午 10:55
     * @Description: 通过id查询排口限产信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    LimitProductionInfoVO getLimitProductionInfoDetailByID(String pkid);


    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 4:48
     * @Description: 验证同一污染源，同一类型监测点在表内有没有时间重叠的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    int isHaveData(Map<String,Object> paramMap);
}

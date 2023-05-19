package com.tjpu.sp.dao.environmentalprotection.entemissioncontribution;

import com.tjpu.sp.model.environmentalprotection.entemissioncontribution.EntEmissionContributionVO;

import java.util.List;
import java.util.Map;

public interface EntEmissionContributionMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntEmissionContributionVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(EntEmissionContributionVO record);


    /**
     * @author: chengzq
     * @date: 2021/05/10 0016 下午 2:37
     * @Description:  通过自定义参数获取企业排放贡献信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getEntEmissionContributionByParamMap(Map<String, Object> paramMap);
    int countEntEmissionContributionByParamMap(Map<String, Object> paramMap);
    List<Map<String,Object>> getEntEmissionContributionInfoByParamMap(Map<String, Object> paramMap);

}
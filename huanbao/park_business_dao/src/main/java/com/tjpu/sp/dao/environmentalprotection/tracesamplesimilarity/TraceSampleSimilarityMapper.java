package com.tjpu.sp.dao.environmentalprotection.tracesamplesimilarity;

import com.tjpu.sp.model.environmentalprotection.tracesamplesimilarity.TraceSampleSimilarityVO;

import java.util.List;
import java.util.Map;

public interface TraceSampleSimilarityMapper {
    int deleteByPrimaryKey(String pkId);
    int deleteByfktracesampleid(String deleteByfktracesampleid);

    int insert(TraceSampleSimilarityVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(TraceSampleSimilarityVO record);


    /**
     * @author: chengzq
     * @date: 2020/11/11 0016 下午 2:37
     * @Description:  通过自定义参数获取溯源样品相似度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getTraceSampleSimilarityByParamMap(Map<String, Object> paramMap);

}
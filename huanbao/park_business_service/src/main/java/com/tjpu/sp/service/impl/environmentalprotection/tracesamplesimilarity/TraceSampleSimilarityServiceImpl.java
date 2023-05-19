package com.tjpu.sp.service.impl.environmentalprotection.tracesamplesimilarity;

import com.tjpu.sp.dao.environmentalprotection.tracesamplesimilarity.TraceSampleSimilarityMapper;
import com.tjpu.sp.model.environmentalprotection.tracesamplesimilarity.TraceSampleSimilarityVO;
import com.tjpu.sp.service.environmentalprotection.tracesamplesimilarity.TraceSampleSimilarityService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class TraceSampleSimilarityServiceImpl implements TraceSampleSimilarityService {

    @Autowired
    private TraceSampleSimilarityMapper traceSampleSimilarityMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return traceSampleSimilarityMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int deleteByFktracesampleid(String fktracesampleid) {
        return traceSampleSimilarityMapper.deleteByfktracesampleid(fktracesampleid);
    }

    @Override
    public int insert(TraceSampleSimilarityVO record) {
        return traceSampleSimilarityMapper.insert(record);
    }

    @Override
    public int insertBatch(List<TraceSampleSimilarityVO> record) {
        for (TraceSampleSimilarityVO traceSampleSimilarityVO : record) {
            traceSampleSimilarityMapper.insert(traceSampleSimilarityVO);
        }
        return 0;
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return traceSampleSimilarityMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(TraceSampleSimilarityVO record) {
        return traceSampleSimilarityMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2020/11/11 0016 下午 2:38
     * @Description:  通过自定义参数获取溯源样品相似度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTraceSampleSimilarityByParamMap(Map<String, Object> paramMap) {
        return traceSampleSimilarityMapper.getTraceSampleSimilarityByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/11/11 0016 下午 2:38
     * @Description: 通过id获取溯源样品相似度详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getTraceSampleSimilarityDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = traceSampleSimilarityMapper.getTraceSampleSimilarityByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }

}

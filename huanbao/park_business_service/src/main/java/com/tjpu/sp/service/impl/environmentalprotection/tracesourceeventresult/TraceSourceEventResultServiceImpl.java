package com.tjpu.sp.service.impl.environmentalprotection.tracesourceeventresult;

import com.tjpu.sp.dao.environmentalprotection.tracesourceeventresult.TraceSourceEventResultMapper;
import com.tjpu.sp.model.environmentalprotection.tracesourceeventresult.TraceSourceEventResultVO;
import com.tjpu.sp.service.environmentalprotection.tracesourceeventresult.TraceSourceEventResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class TraceSourceEventResultServiceImpl implements TraceSourceEventResultService {

    @Autowired
    private TraceSourceEventResultMapper traceSourceEventResultMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return traceSourceEventResultMapper.deleteByPrimaryKey(pkId);
    }



    @Override
    public int insert(List<TraceSourceEventResultVO> records) {
        for (TraceSourceEventResultVO record : records) {
            traceSourceEventResultMapper.insert(record);
        }
        return 0;
    }
    @Override
    public int update(List<TraceSourceEventResultVO> records) {
        String fktracesourceeventid = records.stream().findFirst().orElse(new TraceSourceEventResultVO()).getfktracesourceeventid();
        traceSourceEventResultMapper.deleteByTraceSourceEventid(fktracesourceeventid);
        for (TraceSourceEventResultVO record : records) {
            traceSourceEventResultMapper.insert(record);
        }
        return 0;
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return traceSourceEventResultMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(TraceSourceEventResultVO record) {
        return traceSourceEventResultMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0016 下午 2:38
     * @Description:  通过自定义参数获取溯源事件结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceEventResultByParamMap(Map<String, Object> paramMap) {
        return traceSourceEventResultMapper.getTraceSourceEventResultByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2021/05/10 0016 下午 2:38
     * @Description: 通过id获取溯源事件结果详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getTraceSourceEventResultDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = traceSourceEventResultMapper.getTraceSourceEventResultByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }

}

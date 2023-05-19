package com.tjpu.sp.service.impl.environmentalprotection.tracesourcesample;

import com.tjpu.sp.dao.environmentalprotection.tracesourcesample.TraceSourceSampleMapper;
import com.tjpu.sp.model.environmentalprotection.tracesourcesample.TraceSourceSampleVO;
import com.tjpu.sp.service.environmentalprotection.tracesourcesample.TraceSourceSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class TraceSourceSampleServiceImpl implements TraceSourceSampleService {

    @Autowired
    private TraceSourceSampleMapper traceSourceSampleMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return traceSourceSampleMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insert(TraceSourceSampleVO record) {
        return traceSourceSampleMapper.insert(record);
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return traceSourceSampleMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(TraceSourceSampleVO record) {
        return traceSourceSampleMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2020/10/21 0016 下午 2:38
     * @Description:  通过自定义参数获取溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceSampleByParamMap(Map<String, Object> paramMap) {
        return traceSourceSampleMapper.getTraceSourceSampleByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/10/21 0016 下午 2:38
     * @Description: 通过id获取溯源样品详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getTraceSourceSampleDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = traceSourceSampleMapper.getTraceSourceSampleByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }

}

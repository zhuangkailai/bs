package com.tjpu.sp.service.impl.environmentalprotection.online;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.online.EffectiveTransmissionMapper;
import com.tjpu.sp.model.environmentalprotection.online.EffectiveTransmissionVO;
import com.tjpu.sp.service.environmentalprotection.online.EffectiveTransmissionService;
import com.tjpu.sp.service.impl.base.output.UserMonitorPointRelationDataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EffectiveTransmissionServiceImpl implements EffectiveTransmissionService {

    @Autowired
    private EffectiveTransmissionMapper effectiveTransmissionMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;



    /**
     *
     * @author: lip
     * @date: 2019/7/31 0031 下午 1:43
     * @Description: 自定义查询条件获取有效传输数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEffectiveTransmissionByParamMap(Map<String, Object> paramMap) {
        return effectiveTransmissionMapper.getEffectiveTransmissionByParamMap(paramMap);
    }
    /**
     *
     * @author: lip
     * @date: 2019/7/31 0031 下午 2:09
     * @Description: 自定义查询条件获取最新时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public String getLastDateByParamMap(Map<String, Object> paramMap) {
        return effectiveTransmissionMapper.getLastDateByParamMap(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/1/16 0016 上午 11:51
     * @Description: 通过自定义参数获取企业，监测点，传输有效信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutionEffectiveTransmissionInfoByParamMap(Map<String, Object> paramMap) {
        return effectiveTransmissionMapper.getPollutionEffectiveTransmissionInfoByParamMap(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/1/16 0016 下午 5:23
     * @Description: 通过自定义参数获取监测点传输有效率信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEffectiveTransmissionInfoByParamMap(Map<String, Object> paramMap) {
        return effectiveTransmissionMapper.getEffectiveTransmissionInfoByParamMap(paramMap);
    }


    @Override
    public List<Map<String, Object>> getOutPutEffectiveTransmissionInfoByParamMap(Map<String, Object> paramMap) {
        return effectiveTransmissionMapper.getOutPutEffectiveTransmissionInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getMonitorEffectiveTransmissionInfoByParamMap(Map<String, Object> paramMap) {
        return effectiveTransmissionMapper.getMonitorEffectiveTransmissionInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getStinkEffectiveTransmissionByParamMap(Map<String, Object> paramMap) {
        return effectiveTransmissionMapper.getStinkEffectiveTransmissionByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getMonitorPollutantSetDataByParam(Map<String, Object> paramMap) {
        return pollutantFactorMapper.getMonitorPollutantSetDataByParam(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2021/3/30 0030 下午 2:46
     * @Description: 重新计算传输有效率并修改数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public void supplyOutPutEffectiveTransmissionByParams(Map<String, Object> paramMap, List<EffectiveTransmissionVO> datalist) {
        if(datalist.size()>0){
            //先将之前的数据删除
            effectiveTransmissionMapper.deleteByParamMap(paramMap);
        }
        List<List<EffectiveTransmissionVO>> monitorLists = UserMonitorPointRelationDataServiceImpl.getMonitorLists(datalist);
        //100条新增一次
        for (List<EffectiveTransmissionVO> monitorList : monitorLists) {
            effectiveTransmissionMapper.insertBatch(monitorList);
        }
    }

    @Override
    public List<Map<String, Object>> getGasEffectiveTransmissionByParamMap(Map<String, Object> paramMap) {
        return effectiveTransmissionMapper.getGasEffectiveTransmissionByParamMap(paramMap);
    }


}

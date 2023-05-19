package com.tjpu.sp.service.impl.environmentalprotection.pollutantvaluescope;

import com.tjpu.sp.dao.environmentalprotection.pollutantvaluescope.PollutantValueScopeMapper;
import com.tjpu.sp.model.environmentalprotection.pollutantvaluescope.PollutantValueScopeVO;
import com.tjpu.sp.service.environmentalprotection.pollutantvaluescope.PollutantValueScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class PollutantValueScopeServiceImpl implements PollutantValueScopeService {

    @Autowired
    private PollutantValueScopeMapper pollutantValueScopeMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return pollutantValueScopeMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insert(PollutantValueScopeVO record) {
        return pollutantValueScopeMapper.insert(record);
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return pollutantValueScopeMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(PollutantValueScopeVO record) {
        return pollutantValueScopeMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2020/05/19 0016 下午 2:38
     * @Description:  通过自定义参数获取污染物监测值范围信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutantValueScopeByParamMap(Map<String, Object> paramMap) {
        return pollutantValueScopeMapper.getPollutantValueScopeByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/05/19 0016 下午 2:38
     * @Description: 通过id获取污染物监测值范围详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getPollutantValueScopeDetailByID(String pkid) {
        return pollutantValueScopeMapper.selectByPrimaryKey(pkid);
    }

}

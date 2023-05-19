package com.tjpu.sp.service.impl.environmentalprotection.pollutantsmell;

import com.tjpu.sp.dao.environmentalprotection.pollutantsmell.PollutantSmellMapper;
import com.tjpu.sp.model.environmentalprotection.pollutantsmell.PollutantSmellVO;
import com.tjpu.sp.service.environmentalprotection.pollutantsmell.PollutantSmellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PollutantSmellServiceImpl implements PollutantSmellService {

    @Autowired
    private PollutantSmellMapper pollutantSmellMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return pollutantSmellMapper.deleteByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 上午 8:38
     * @Description: 通过code删除
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [code]
     * @throws:
     */
    @Override
    public int deleteByCode(String code) {
        return pollutantSmellMapper.deleteByCode(code);
    }

    @Override
    public int insert(PollutantSmellVO record) {
        return pollutantSmellMapper.insert(record);
    }

    @Override
    public int insertBatch(List<PollutantSmellVO> record) {
        for (PollutantSmellVO pollutantSmellVO : record) {
            pollutantSmellMapper.insert(pollutantSmellVO);
        }
        return 0;
    }

    @Override
    public PollutantSmellVO selectByPrimaryKey(String pkId) {
        return pollutantSmellMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(PollutantSmellVO record) {
        return pollutantSmellMapper.updateByPrimaryKey(record);
    }
    @Override
    public int updateByCode(PollutantSmellVO record) {
        return pollutantSmellMapper.updateByCode(record);
    }

    @Override
    public int updateBatch(List<PollutantSmellVO> record,String smellcode) {
        if(!record.isEmpty()){
            pollutantSmellMapper.deleteByCode(smellcode);
        }
        for (PollutantSmellVO pollutantSmellVO : record) {
            pollutantSmellMapper.insert(pollutantSmellVO);
        }
        return 0;
    }


    /**
     * @author: chengzq
     * @date: 2019/10/26 0016 下午 2:38
     * @Description:  通过自定义参数获取污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutantSmellByParamMap(Map<String, Object> paramMap) {
        return pollutantSmellMapper.getPollutantSmellByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/10/26 0016 下午 2:38
     * @Description: 通过smellcode获取污染物味道详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getPollutantSmellDetailBySmellCode(Map<String, Object> paramMap) {
        return pollutantSmellMapper.selectBySmellCode(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/10/26 0026 下午 4:37
     * @Description: 通过smellcode获取污染物味道信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [smellcode]
     * @throws:
     */
    @Override
    public Map<String, Object> selectBySmellCode(Map<String, Object> paramMap) {
        return pollutantSmellMapper.selectBySmellCode(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 上午 10:06
     * @Description:  通过污染物类型获取污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutantByPollutantType(Map<String, Object> paramMap) {
        return pollutantSmellMapper.getPollutantByPollutantType(paramMap);
    }

}

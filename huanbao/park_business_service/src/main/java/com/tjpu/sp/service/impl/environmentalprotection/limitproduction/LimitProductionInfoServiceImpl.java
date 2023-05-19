package com.tjpu.sp.service.impl.environmentalprotection.limitproduction;

import com.tjpu.sp.dao.environmentalprotection.limitproduction.LimitProductionDetailInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.limitproduction.LimitProductionInfoMapper;
import com.tjpu.sp.model.environmentalprotection.limitproduction.LimitProductionDetailInfoVO;
import com.tjpu.sp.model.environmentalprotection.limitproduction.LimitProductionInfoVO;
import com.tjpu.sp.service.environmentalprotection.limitproduction.LimitProductionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LimitProductionInfoServiceImpl implements LimitProductionInfoService {

    @Autowired
    private LimitProductionInfoMapper limitProductionInfoMapper;
    @Autowired
    private LimitProductionDetailInfoMapper limitProductionDetailInfoMapper;


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 8:59
     * @Description: 通过id删除排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkId) {
        limitProductionDetailInfoMapper.deleteByLimitProductionID(pkId);
        return limitProductionInfoMapper.deleteByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 8:59
     * @Description: 新增排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int insert(LimitProductionInfoVO record,List<LimitProductionDetailInfoVO> data) {

        for (LimitProductionDetailInfoVO datum : data) {
            limitProductionDetailInfoMapper.insert(datum);
        }

        return limitProductionInfoMapper.insert(record);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 9:00
     * @Description: 通过id查询排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public LimitProductionInfoVO selectByPrimaryKey(String pkId) {
        return limitProductionInfoMapper.selectByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 上午 9:00
     * @Description: 通过id修改排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int updateByPrimaryKey(LimitProductionInfoVO record,List<LimitProductionDetailInfoVO> data) {

        limitProductionDetailInfoMapper.deleteByLimitProductionID(record.getPkId());

        for (LimitProductionDetailInfoVO datum : data) {
            limitProductionDetailInfoMapper.insert(datum);
        }

        return limitProductionInfoMapper.updateByPrimaryKey(record);
    }




    /**
     * @author: chengzq
     * @date: 2019/6/24 0024 下午 7:21
     * @Description: 通过自定义参数获取排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<LimitProductionInfoVO> getLimitProductionInfoByParamMap(Map<String, Object> paramMap) {
        return limitProductionInfoMapper.getLimitProductionInfoByParamMap(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 上午 10:25
     * @Description: 通过id获取排口限产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public LimitProductionInfoVO getLimitProductionInfoByID(String pkid) {
        return limitProductionInfoMapper.getLimitProductionInfoByID(pkid);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 上午 10:55
     * @Description: 通过id获取排口限产信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public LimitProductionInfoVO getLimitProductionInfoDetailByID(String pkid) {
        return limitProductionInfoMapper.getLimitProductionInfoDetailByID(pkid);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 4:49
     * @Description:  验证同一污染源，同一类型监测点在表内有没有时间重叠的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public int isHaveData(Map<String, Object> paramMap) {
        return limitProductionInfoMapper.isHaveData(paramMap);
    }

}

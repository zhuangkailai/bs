package com.tjpu.sp.service.impl.environmentalprotection.entemissioncontribution;

import com.tjpu.sp.dao.environmentalprotection.entemissioncontribution.EntEmissionContributionMapper;
import com.tjpu.sp.model.environmentalprotection.entemissioncontribution.EntEmissionContributionVO;
import com.tjpu.sp.service.environmentalprotection.entemissioncontribution.EntEmissionContributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class EntEmissionContributionServiceImpl implements EntEmissionContributionService {

    @Autowired
    private EntEmissionContributionMapper entemissionContributionMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return entemissionContributionMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insert(EntEmissionContributionVO record) {
        return entemissionContributionMapper.insert(record);
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return entemissionContributionMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(EntEmissionContributionVO record) {
        return entemissionContributionMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0016 下午 2:38
     * @Description:  通过自定义参数获取企业排放贡献信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntEmissionContributionByParamMap(Map<String, Object> paramMap) {
        return entemissionContributionMapper.getEntEmissionContributionByParamMap(paramMap);
    }
    @Override
    public List<Map<String, Object>> getEntEmissionContributionInfoByParamMap(Map<String, Object> paramMap) {
        return entemissionContributionMapper.getEntEmissionContributionInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2021/05/10 0016 下午 2:38
     * @Description: 通过id获取企业排放贡献详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getEntEmissionContributionDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = entemissionContributionMapper.getEntEmissionContributionByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }

    @Override
    public int countEntEmissionContributionByParamMap(Map<String, Object> paramMap) {
        return entemissionContributionMapper.countEntEmissionContributionByParamMap(paramMap);
    }

}

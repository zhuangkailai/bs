package com.tjpu.sp.service.impl.envhousekeepers.problemconsult;


import com.tjpu.sp.dao.envhousekeepers.problemconsult.EntProblemConsultRecordMapper;
import com.tjpu.sp.model.envhousekeepers.problemconsult.EntProblemConsultRecordVO;
import com.tjpu.sp.service.envhousekeepers.problemconsult.EntProblemConsultRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Service
public class EntProblemConsultRecordServiceImpl implements EntProblemConsultRecordService {

    @Autowired
    private EntProblemConsultRecordMapper entProblemConsultRecordMapper;

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:11
     * @Description: 通过自定义参数查询企业问题咨询记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntProblemConsultRecordByParamMap(Map<String,Object> paramMap) {
        return entProblemConsultRecordMapper.getEntProblemConsultRecordByParamMap(paramMap);
    }

    @Override
    public int insert(EntProblemConsultRecordVO entity) {
        return entProblemConsultRecordMapper.insert(entity);
    }

    @Override
    public EntProblemConsultRecordVO selectByPrimaryKey(String id) {
        return entProblemConsultRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKey(EntProblemConsultRecordVO entity) {
        return entProblemConsultRecordMapper.updateByPrimaryKey(entity);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        entProblemConsultRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getEntProblemConsultRecordDetailByID(String id) {
        return entProblemConsultRecordMapper.getEntProblemConsultRecordDetailByID(id);
    }

    @Override
    public List<Map<String, Object>> getAllSearchProblemDataByParamMap(Map<String, Object> paramMap) {
        return entProblemConsultRecordMapper.getAllSearchProblemDataByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getNoReadProblemConsultRecordByParam(Map<String, Object> parammap) {
        return entProblemConsultRecordMapper.getNoReadProblemConsultRecordByParam(parammap);
    }

    @Override
    public List<Map<String, Object>> getNoReadEntProblemConsultRecordByParam(Map<String, Object> parammap) {
        return entProblemConsultRecordMapper.getNoReadEntProblemConsultRecordByParam(parammap);
    }

}

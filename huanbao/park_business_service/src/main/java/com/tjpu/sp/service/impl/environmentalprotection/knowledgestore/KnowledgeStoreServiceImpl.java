package com.tjpu.sp.service.impl.environmentalprotection.knowledgestore;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.sp.dao.environmentalprotection.knowledgestore.KnowledgeStoreInfoMapper;
import com.tjpu.sp.model.base.knowledgestore.KnowledgeStoreInfo;
import com.tjpu.sp.service.environmentalprotection.knowledgestore.KnowledgeStoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class KnowledgeStoreServiceImpl implements KnowledgeStoreService {

    private final KnowledgeStoreInfoMapper knowledgeStoreMapper;

    public KnowledgeStoreServiceImpl(KnowledgeStoreInfoMapper knowledgeStoreMapper) {
        this.knowledgeStoreMapper = knowledgeStoreMapper;
    }

    @Override
    public int deleteByPrimaryKey(String pkId) {
        return knowledgeStoreMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insertSelective(KnowledgeStoreInfo record) {
        return knowledgeStoreMapper.insertSelective(record);
    }

    @Override
    public int updateByPrimaryKeySelective(KnowledgeStoreInfo record) {
        return knowledgeStoreMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public Map<String, Object> getKnowledgeStoresByParam(Map<String, Object> paramMap, Integer pageSize, Integer pageNum) {
        Map<String, Object> result = new HashMap<>();
        if (pageSize != null && pageNum != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Map<String, Object>> knowledgeStoresByParam = knowledgeStoreMapper.getKnowledgeStoresByParam(paramMap);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(knowledgeStoresByParam);
        long total = pageInfo.getTotal();
        List<Map<String, Object>> list = pageInfo.getList();
        result.put("total", total);
        result.put("knowledgestores", list);
        return result;
    }

    /**
     * @author: zhangzc
     * @date: 2019/9/3 16:48
     * @Description: 获取知识库类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getKnowledgeStoresType() {
        return knowledgeStoreMapper.getKnowledgeStoresType();
    }

    @Override
    public List<Map<String, Object>> getKnowledgeStoresDataListByParam(Map<String, Object> paramMap) {
        return knowledgeStoreMapper.getKnowledgeStoresByParam(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/08/24 0024 下午 4:36
     * @Description:分组统计各类别知识库信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countKnowledgeStoreGroupByStoreType() {
        return knowledgeStoreMapper.countKnowledgeStoreGroupByStoreType();
    }
}

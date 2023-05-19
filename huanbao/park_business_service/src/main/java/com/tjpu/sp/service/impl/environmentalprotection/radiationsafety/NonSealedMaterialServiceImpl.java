package com.tjpu.sp.service.impl.environmentalprotection.radiationsafety;

import com.tjpu.sp.dao.environmentalprotection.radiationsafety.NonSealedMapper;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.NonSealedVO;
import com.tjpu.sp.service.environmentalprotection.radiationsafety.NonSealedMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/23 0023 9:33
 * @Description: 非密封放射性物质实现层实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class NonSealedMaterialServiceImpl implements NonSealedMaterialService {

    @Autowired
    private NonSealedMapper nonSealedMapper;
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 9:50
    *@Description: 通过自定义参数获取非密封放射性物质信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getNonSealedByParamMap(Map<String, Object> paramMap) {
        return nonSealedMapper.getNonSealedByParamMap(paramMap);
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:28
    *@Description: 通过主键id删除非密封放射性物质数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public void deleteNonSealedById(String id) {
        nonSealedMapper.deleteByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:27
    *@Description: 添加非密封放射性物质列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [nonSealedVO]
    *@throws:
    **/
    @Override
    public void addNonSealedInfo(NonSealedVO nonSealedVO) {
        nonSealedMapper.insert(nonSealedVO);
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:34
    *@Description: 非密封放射性物质编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public NonSealedVO getNonSealedById(String id) {
        return nonSealedMapper.selectByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:47
    *@Description: 编辑保存非密封放射性物质信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [nonSealedVO]
    *@throws:
    **/
    @Override
    public void updateNonSealedInfo(NonSealedVO nonSealedVO) {
        nonSealedMapper.updateByPrimaryKey(nonSealedVO);
    }
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:52
    *@Description: 获取非密封放射性物质详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public Map<String, Object> getNonSealedDetailById(String id) {
        return nonSealedMapper.getNonSealedDetailById(id);
    }
}

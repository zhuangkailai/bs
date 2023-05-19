package com.tjpu.sp.service.impl.environmentalprotection.radiationsafety;

import com.tjpu.sp.dao.environmentalprotection.radiationsafety.SourcesMapper;
import com.tjpu.sp.model.environmentalprotection.radiationsafety.SourcesVO;
import com.tjpu.sp.service.environmentalprotection.radiationsafety.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/22 0022 16:14
 * @Description: 放射源实现层实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class SourceServiceImpl implements SourceService {

    @Autowired
    private SourcesMapper sourcesMapper;
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 16:35
    *@Description: 通过自定义参数获取放射源信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getSourceListByParamMap(Map<String, Object> paramMap) {
        return sourcesMapper.getSourceListByParamMap(paramMap);
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 17:22
    *@Description: 通过主键id删除放射源单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public void deleteSourceById(String id) {
        sourcesMapper.deleteByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:03
    *@Description: 添加放射源信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [sourcesVO]
    *@throws:
    **/
    @Override
    public void addSourceInfo(SourcesVO sourcesVO) {
        sourcesMapper.insert(sourcesVO);
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:06
    *@Description: 放射源列表编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public SourcesVO getSourceInfoById(String id) {
        return sourcesMapper.selectByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:12
    *@Description: 编辑保存放射源列表数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [sourcesVO]
    *@throws:
    **/
    @Override
    public void updateSourceInfo(SourcesVO sourcesVO) {
        sourcesMapper.updateByPrimaryKey(sourcesVO);
    }
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:15
    *@Description: 获取放射源详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public Map<String, Object> getSourceDetailById(String id) {
        return sourcesMapper.getSourceDetailById(id);
    }
}

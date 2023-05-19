package com.tjpu.sp.service.impl.envhousekeepers.focusconcernentset;

import com.tjpu.sp.dao.envhousekeepers.focusconcernentset.FocusConcernEntSetMapper;
import com.tjpu.sp.model.envhousekeepers.focusconcernentset.FocusConcernEntSetVO;
import com.tjpu.sp.service.envhousekeepers.focusconcernentset.FocusConcernEntSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FocusConcernEntSetServiceImpl implements FocusConcernEntSetService {
    @Autowired
    private FocusConcernEntSetMapper focusConcernEntSetMapper;

    /**
     *@author: xsm
     *@date: 2021/08/05 0005 下午 14:46
     *@Description: 通过自定义参数获取重点关注企业设置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [jsonObject]
     *@throws:
     **/
    @Override
    public List<Map<String, Object>> getFocusConcernEntSetsByParamMap(Map<String,Object> param) {
        return focusConcernEntSetMapper.getFocusConcernEntSetsByParamMap(param);
    }

    /**
     *@author: xsm
     *@date: 2021/08/05 0005 下午 14:46
     *@Description: 新增企业重点关注企业设置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    @Override
    public void insert(FocusConcernEntSetVO entity) {
        focusConcernEntSetMapper.insert(entity);
    }


    /**
     *@author: xsm
     *@date: 2021/08/05 0005 下午 14:46
     *@Description: 根据主键ID删除重点关注企业设置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    @Override
    public void deleteByPrimaryKey(String id) {
        focusConcernEntSetMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 4:03
     * @Description: 验证重点关注企业是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> IsFocusConcernEntSetValidByPollutionid(String pollutionid) {
        return focusConcernEntSetMapper.selectByPollutionid(pollutionid);
    }

}

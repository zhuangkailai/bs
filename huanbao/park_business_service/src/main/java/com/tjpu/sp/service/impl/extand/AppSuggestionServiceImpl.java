package com.tjpu.sp.service.impl.extand;


import com.tjpu.sp.dao.extand.AppSuggestionMapper;
import com.tjpu.sp.model.extand.AppSuggestionVO;
import com.tjpu.sp.service.extand.AppSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class AppSuggestionServiceImpl implements AppSuggestionService {
    @Autowired
    private AppSuggestionMapper appSuggestionMapper;

    @Override
    public void insert(AppSuggestionVO appSuggestionVO) {
        appSuggestionMapper.insert(appSuggestionVO);
    }

    /**
     * @author: xsm
     * @date: 2020/09/24 0024 下午 15:26
     * @Description: 根据自定义参数获取app反馈意见列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAppSuggestionInfosByParamMap(Map<String, Object> jsonObject) {
        return appSuggestionMapper.getAppSuggestionInfosByParamMap(jsonObject);
    }

    /**
     * @author: xsm
     * @date: 2020/09/24 0024 下午 16:47
     * @Description: 根据主键ID获取app反馈意见详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAppSuggestionDetailById(String id) {
        return appSuggestionMapper.getAppSuggestionDetailById(id);
    }

    /**
     * @author: xsm
     * @date: 2020/09/24 0024 下午 5:01
     * @Description: 根据ID删除app反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void deleteAppSuggestionById(String id) {
        appSuggestionMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2020/09/24 0024 下午 5:11
     * @Description: 修改app反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void update(AppSuggestionVO appSuggestionVO) {
        appSuggestionMapper.updateByPrimaryKey(appSuggestionVO);
    }

    @Override
    public AppSuggestionVO getUpdatePageAppSuggestionInfoById(String id) {
        return appSuggestionMapper.selectByPrimaryKey(id);
    }

}

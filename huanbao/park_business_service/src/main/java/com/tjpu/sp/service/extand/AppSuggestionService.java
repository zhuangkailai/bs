package com.tjpu.sp.service.extand;



import com.tjpu.sp.model.extand.AppSuggestionVO;

import java.util.List;
import java.util.Map;

public interface AppSuggestionService {


    void insert(AppSuggestionVO appSuggestionVO);

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
    List<Map<String,Object>> getAppSuggestionInfosByParamMap(Map<String,Object> jsonObject);

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
    Map<String,Object> getAppSuggestionDetailById(String id);

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
    void deleteAppSuggestionById(String id);

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
    void update(AppSuggestionVO appSuggestionVO);

    AppSuggestionVO getUpdatePageAppSuggestionInfoById(String id);
}

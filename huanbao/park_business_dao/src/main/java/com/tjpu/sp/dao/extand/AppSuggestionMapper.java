package com.tjpu.sp.dao.extand;

import com.tjpu.sp.model.extand.AppSuggestionVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AppSuggestionMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(AppSuggestionVO record);

    int insertSelective(AppSuggestionVO record);

    AppSuggestionVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(AppSuggestionVO record);

    int updateByPrimaryKey(AppSuggestionVO record);

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
    List<Map<String,Object>> getAppSuggestionInfosByParamMap(Map<String, Object> jsonObject);

    /**
     * @author: xsm
     * @date: 2020/09/24 0024 下午 15:26
     * @Description: 根据id获取反馈意见详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getAppSuggestionDetailById(String pkid);
}
package com.tjpu.sp.dao.environmentalprotection.wechatpushset;

import com.tjpu.sp.model.environmentalprotection.wechatpushset.WechatPushSetVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface WechatPushSetMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WechatPushSetVO record);

    int insertSelective(WechatPushSetVO record);

    WechatPushSetVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WechatPushSetVO record);

    int updateByPrimaryKey(WechatPushSetVO record);

    void batchInsert(@Param("list")List<WechatPushSetVO> listobjs);

    void deleteByWechatName(@Param("wechatname")String wechatname);

    List<Map<String,Object>> getWechatPushSetInfosByParamMap(Map<String, Object> paramMap);
}
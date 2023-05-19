package com.tjpu.sp.dao.environmentalprotection.notice;

import com.tjpu.sp.model.environmentalprotection.notice.NoticeVO;

import java.util.List;
import java.util.Map;

public interface NoticeMapper {
    int deleteByPrimaryKey(String pkNoticeid);

    int insert(NoticeVO record);

    int insertSelective(NoticeVO record);

    NoticeVO selectByPrimaryKey(String pkNoticeid);

    int updateByPrimaryKeySelective(NoticeVO record);

    int updateByPrimaryKey(NoticeVO record);

    List<NoticeVO> getNoticeInfoByParam(Map<String, Object> paramMap);

    NoticeVO getNoticeDetailDataByNoticeId(String noticeid);

    List<Map<String,Object>> getNoReadNoticeDataByParam(Map<String, Object> parammap);
}
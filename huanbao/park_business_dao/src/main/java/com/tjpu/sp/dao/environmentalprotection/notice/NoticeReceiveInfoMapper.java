package com.tjpu.sp.dao.environmentalprotection.notice;

import com.tjpu.sp.model.environmentalprotection.notice.NoticeReceiveInfoVO;

import java.util.List;
import java.util.Map;

public interface NoticeReceiveInfoMapper {
    int deleteByPrimaryKey(String pkNoticereceiveid);

    int insert(NoticeReceiveInfoVO record);

    int insertSelective(NoticeReceiveInfoVO record);

    NoticeReceiveInfoVO selectByPrimaryKey(String pkNoticereceiveid);

    int updateByPrimaryKeySelective(NoticeReceiveInfoVO record);

    int updateByPrimaryKey(NoticeReceiveInfoVO record);

    void batchInsert(List<NoticeReceiveInfoVO> noticeReceiveInfoVOS);

    void updateIsReadByIds(Map<String, Object> paramMap);

    List<NoticeReceiveInfoVO> getReceiveNoticeInfoByParam(Map<String, Object> paramMap);

    void deleteReceiveNoticeByParam(Map<String, Object> paramMap);

    void deleteByNoticeid(String noticeid);
}
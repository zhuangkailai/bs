package com.tjpu.sp.service.environmentalprotection.notice;

import com.tjpu.sp.model.environmentalprotection.notice.NoticeVO;

import java.util.List;
import java.util.Map;


public interface NoticeService {

    int addNoticeAndOtherInfo(NoticeVO noticeVO, List<String> receiverIds);

    void updateNoticeIsRead(List<String> pknoticereceiveIds, Integer isread, String userid);

    Map<String,Object> getNoticeInfoByParam(Integer pagenum, Integer pagesize, Map<String, Object> paramMap);

    Map<String,Object> getReceiveNoticeInfoByParam(Integer pagenum, Integer pagesize, Map<String, Object> paramMap);

    void deleteReceiveNoticeByParam(Map<String, Object> paramMap);

    Map<String,Object> getNoticeDetailDataByNoticeId(String noticeid, String userId);


    NoticeVO getNoticeInfoByNoticeID(String pknoticeid);

    void updateNoticeInfo(NoticeVO notice, List<String> receiverids, String userid);

    void recallNoticeInfo(NoticeVO notice);

    void deleteNoticeInfo(String pknoticeid);

    List<Map<String,Object>> getNoReadNoticeDataByParam(Map<String, Object> parammap);
}

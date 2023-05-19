package com.tjpu.sp.service.impl.environmentalprotection.notice;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.notice.NoticeMapper;
import com.tjpu.sp.dao.environmentalprotection.notice.NoticeReceiveInfoMapper;
import com.tjpu.sp.model.environmentalprotection.notice.NoticeReceiveInfoVO;
import com.tjpu.sp.model.environmentalprotection.notice.NoticeVO;
import com.tjpu.sp.service.environmentalprotection.notice.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;



@Service
@Transactional
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private NoticeReceiveInfoMapper noticeReceiveInfoMapper;


    /**
     * @author: xsm
     * @date: 2018/10/13 0013 下午 2:16
     * @Description: 添加通知信息以及其他相关信息（发送通知）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public int addNoticeAndOtherInfo(NoticeVO noticeVO, List<String> receiverIds) {
        try {
            int j= 1;
            noticeMapper.insert(noticeVO);
            //添加接收人关联(批量)
            if (receiverIds != null && receiverIds.size() > 0) {
                List<NoticeReceiveInfoVO> noticeReceiveInfoVOS = new ArrayList<>();
                for (int i = 0; i < receiverIds.size(); i++) {
                    NoticeReceiveInfoVO noticeReceiveInfoVO = new NoticeReceiveInfoVO();
                    noticeReceiveInfoVO.setPkNoticereceiveid(UUID.randomUUID().toString());
                    noticeReceiveInfoVO.setFkNoticeid(noticeVO.getPkNoticeid());
                    noticeReceiveInfoVO.setIsread(0);
                    noticeReceiveInfoVO.setNoticereceiverid(receiverIds.get(i));
                    noticeReceiveInfoVOS.add(noticeReceiveInfoVO);
                }
                noticeReceiveInfoMapper.batchInsert(noticeReceiveInfoVOS);
            }
            return j;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }


    }

    /**
     * @author: xsm
     * @date: 2018/10/13 0013 下午 3:09
     * @Description: 更新通知的已读未读状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateNoticeIsRead(List<String> noticeids, Integer isread, String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("isRead", isread);
        paramMap.put("userid", userid);
        paramMap.put("noticeids", noticeids);
        //根据条件更新接收通知的已读未读状态
        noticeReceiveInfoMapper.updateIsReadByIds(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2018/10/13 0013 下午 5:09
     * @Description: 自定义查询条件查询通知信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getNoticeInfoByParam(Integer pagenum, Integer pagesize, Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        List<NoticeVO> noticeVOS = noticeMapper.getNoticeInfoByParam(paramMap);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (NoticeVO noticeVO : noticeVOS) {
            Map<String, Object> map = new HashMap<>();
            map.put("pknoticeid", noticeVO.getPkNoticeid());
            map.put("noticetitle", noticeVO.getNoticetitle());
            map.put("noticecontent", noticeVO.getNoticecontent());
            map.put("sendusername", noticeVO.getSendusername());
            map.put("sendtime", DataFormatUtil.getDateYMDHMS(noticeVO.getSendtime()));
            List<NoticeReceiveInfoVO> objs =noticeVO.getNoticeReceiveInfoVOS();
            String NoticeReceiverNames = "";
            if (objs!=null&&objs.size()>0){
                for (NoticeReceiveInfoVO obj:objs){
                    if (obj.getNoticereceivername()!=null) {
                        NoticeReceiverNames = NoticeReceiverNames + obj.getNoticereceivername() + "、";
                    }
                }
            }
            if (!"".equals(NoticeReceiverNames)){
                NoticeReceiverNames =NoticeReceiverNames.substring(0,NoticeReceiverNames.length()-1);
            }
            map.put("noticereceivernames", NoticeReceiverNames);
            map.put("fileid", noticeVO.getFileid());
            map.put("isRecall",noticeVO.getIsrecall());
            map.put("isRecallname",noticeVO.getIsrecall()==null?"":noticeVO.getIsrecall()==1?"已撤回":"已发送");
            dataList.add(map);
        }
        if (pagenum != null && pagesize != null) {//分页数据
            List<Map<String, Object>> datalist = getPageData(dataList, pagenum, pagesize);
            resultMap.put("total", dataList.size());
            resultMap.put("datalist", datalist);
            return resultMap;
        }else{
            resultMap.put("datalist", dataList);
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> getReceiveNoticeInfoByParam(Integer pagenum, Integer pagesize, Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        List<NoticeReceiveInfoVO> noticeReceiveInfoVOS = noticeReceiveInfoMapper.getReceiveNoticeInfoByParam(paramMap);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (NoticeReceiveInfoVO noticeReceiveInfoVO : noticeReceiveInfoVOS) {
            Map<String, Object> map = new HashMap<>();
            map.put("pkNoticeid", noticeReceiveInfoVO.getNoticeVO().getPkNoticeid());
            map.put("noticetitle", noticeReceiveInfoVO.getNoticeVO().getNoticetitle());
            map.put("isread", noticeReceiveInfoVO.getIsread());
            map.put("noticecontent", noticeReceiveInfoVO.getNoticeVO().getNoticecontent());
            map.put("sendusername", noticeReceiveInfoVO.getNoticeVO().getSendusername());
            map.put("sendtime", DataFormatUtil.getDateYMDHMS(noticeReceiveInfoVO.getNoticeVO().getSendtime()));
            map.put("fileid", noticeReceiveInfoVO.getNoticeVO().getFileid());
            dataList.add(map);
        }
        if (pagenum != null && pagesize != null) {//分页数据
            List<Map<String, Object>> datalist = getPageData(dataList, pagenum, pagesize);
            resultMap.put("total", dataList.size());
            resultMap.put("datalist", datalist);
            return resultMap;
        }else{
            resultMap.put("datalist", dataList);
            return resultMap;
        }
    }

    /**
     * @author: xsm
     * @date: 2018/10/15 0015 下午 3:03
     * @Description: 根据条件删除接收表记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void deleteReceiveNoticeByParam(Map<String, Object> paramMap) {
        //根据条件更新接收通知的已读未读状态
        noticeReceiveInfoMapper.deleteReceiveNoticeByParam(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/03/17 0017下午 3:29
     * @Description: 根据主键ID获取通知详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getNoticeDetailDataByNoticeId(String noticeid, String userId) {

        //如果当前登录人是接收人，将状态修改为已读
      /*  List<String> noticeids = new ArrayList<>();
        noticeids.add(noticeid);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("isRead", "1");
        paramMap.put("userid", userId);
        paramMap.put("noticeids", noticeids);
        //根据条件更新接收通知的已读未读状态
        noticeReceiveInfoMapper.updateIsReadByIds(paramMap);*/


        NoticeVO noticeVO = noticeMapper.getNoticeDetailDataByNoticeId(noticeid);
        Map<String, Object> map = new HashMap<>();
        map.put("pkNoticeid", noticeVO.getPkNoticeid());
        map.put("noticetitle", noticeVO.getNoticetitle());
        map.put("noticecontent", noticeVO.getNoticecontent());
        map.put("sendusername", noticeVO.getSendusername());
        map.put("sendtime", DataFormatUtil.getDateYMDHMS(noticeVO.getSendtime()));
        List<NoticeReceiveInfoVO> objs =noticeVO.getNoticeReceiveInfoVOS();
        String NoticeReceiverNames = "";
        List<Map<String,Object>> readUsers=new ArrayList<>();
        if (objs!=null&&objs.size()>0){
            for (NoticeReceiveInfoVO obj:objs){
                if (obj.getNoticereceivername() != null) {
                    NoticeReceiverNames = NoticeReceiverNames + obj.getNoticereceivername() + "、";
                    Map<String, Object> data = new HashMap<>();
                    data.put("userid", obj.getNoticereceiverid());
                    data.put("username", obj.getNoticereceivername());
                    data.put("isread", obj.getIsread());
                    readUsers.add(data);
                }
            }
        }
        if (!"".equals(NoticeReceiverNames)){
            NoticeReceiverNames =NoticeReceiverNames.substring(0,NoticeReceiverNames.length()-1);
        }
        map.put("readusers",readUsers);
        map.put("noticereceivernames", NoticeReceiverNames);
        map.put("fileid", noticeVO.getFileid());
        return map;
    }

    /**
     * @author: xsm
     * @date: 2020/03/17 0017下午 3:29
     * @Description: 根据ID获取通知信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public NoticeVO getNoticeInfoByNoticeID(String pknoticeid) {
        return noticeMapper.selectByPrimaryKey(pknoticeid);
    }

    /**
     * @author: xsm
     * @date: 2020/03/17 0017下午 3:29
     * @Description: 修改通知信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateNoticeInfo(NoticeVO notice, List<String> receiverIds, String userid) {
        String noticeid = notice.getPkNoticeid();
        //修改通告信息
        noticeMapper.updateByPrimaryKey(notice);
        //删除接收人关联
        noticeReceiveInfoMapper.deleteByNoticeid(noticeid);
        //添加接收人关联(批量)
        if (receiverIds != null && receiverIds.size() > 0) {
            List<NoticeReceiveInfoVO> noticeReceiveInfoVOS = new ArrayList<>();
            for (int i = 0; i < receiverIds.size(); i++) {
                NoticeReceiveInfoVO noticeReceiveInfoVO = new NoticeReceiveInfoVO();
                noticeReceiveInfoVO.setPkNoticereceiveid(UUID.randomUUID().toString());
                noticeReceiveInfoVO.setFkNoticeid(noticeid);
                noticeReceiveInfoVO.setIsread(0);
                noticeReceiveInfoVO.setNoticereceiverid(receiverIds.get(i));
                noticeReceiveInfoVOS.add(noticeReceiveInfoVO);
            }
            noticeReceiveInfoMapper.batchInsert(noticeReceiveInfoVOS);
        }
    }

    @Override
    public void recallNoticeInfo(NoticeVO notice) {
        //修改通告信息
        noticeMapper.updateByPrimaryKey(notice);

    }

    /**
     * @author: xsm
     * @date: 2020/03/17 0017下午 3:29
     * @Description: 删除通知信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void deleteNoticeInfo(String pknoticeid) {
        //删除通告信息
        noticeMapper.deleteByPrimaryKey(pknoticeid);
        //删除接收人关联
        noticeReceiveInfoMapper.deleteByNoticeid(pknoticeid);
    }

    @Override
    public List<Map<String, Object>> getNoReadNoticeDataByParam(Map<String, Object> parammap) {
        return noticeMapper.getNoReadNoticeDataByParam(parammap);
    }


    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 7:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }
}

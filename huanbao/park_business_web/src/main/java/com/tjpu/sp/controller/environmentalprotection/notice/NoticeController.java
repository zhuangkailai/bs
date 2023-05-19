package com.tjpu.sp.controller.environmentalprotection.notice;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.notice.NoticeVO;
import com.tjpu.sp.service.environmentalprotection.notice.NoticeService;
import io.swagger.annotations.Api;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @author: xsm
 * @date: 2020/03/16 0016 下午 14:17
 * @Description: 通知信息处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("notice")
@Api(value = "通知信息处理类", tags = "通知信息处理类")
public class NoticeController {


    @Autowired
    private NoticeService noticeService;
    @Autowired
    private RabbitmqController rabbitmqController;


    /**
     * @author: xsm
     * @date: 2020/03/16 0016 下午 15:26
     * @Description: 获取全部通知信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllNoticeInfosByParamMap", method = RequestMethod.POST)
    public Object getAllNoticeInfosByParamMap(
                                              @RequestJson(value = "startsendtime", required = false) String startsendtime,
                                              @RequestJson(value = "endsendtime", required = false) String endsendtime,
                                              @RequestJson(value = "noticetitle", required = false) String noticetitle,
                                              @RequestJson(value = "topnum", required = false) Integer topnum,
                                              @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                              @RequestJson(value = "pagesize", required = false) Integer pagesize) {
        try {

            Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("startsendtime", startsendtime);
                paramMap.put("endsendtime", endsendtime);
                paramMap.put("noticetitle", noticetitle);
                paramMap.put("topnum", topnum);
            Map<String, Object>  datamap = noticeService.getNoticeInfoByParam(pagenum, pagesize, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datamap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/16 0016 下午 14:18
     * @Description: 获取已发送通知信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getHasSendNotices", method = RequestMethod.POST)
    public Object getHasSendNotices(
                                    @RequestJson(value = "startsendtime", required = false) String startsendtime,
                                    @RequestJson(value = "endsendtime", required = false) String endsendtime,
                                    @RequestJson(value = "noticetitle", required = false) String noticetitle,
                                    @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                    @RequestJson(value = "pagesize", required = false) Integer pagesize) {
        try {

           Map<String, Object> paramMap = new HashMap<>();
            if (startsendtime != null && !"".equals(startsendtime)) {
                paramMap.put("startsendtime", startsendtime);
            }
            if (endsendtime != null && !"".equals(endsendtime)) {
                paramMap.put("endsendtime", endsendtime);
            }
            paramMap.put("noticetitle", noticetitle);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //userid = "system";
            Map<String, Object>  datamap = new HashMap<>();
            if (userid != null && !"".equals(userid)) {
                paramMap.put("senduserid", userid);
                datamap = noticeService.getNoticeInfoByParam(pagenum, pagesize, paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", datamap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/16 0016 下午 14:18
     * @Description: 获取已接收的通知
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getHasReceiveNotices", method = RequestMethod.POST)
    public Object getHasReceiveNotices(
                                       @RequestJson(value = "startsendtime", required = false) String startsendtime,
                                       @RequestJson(value = "endsendtime", required = false) String endsendtime,
                                       @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                       @RequestJson(value = "noticetitle", required = false) String  noticetitle,
                                       @RequestJson(value = "isread", required = false) String  isread,
                                       @RequestJson(value = "pagesize", required = false) Integer pagesize) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (startsendtime != null && !"".equals(startsendtime)) {
                paramMap.put("startsendtime", startsendtime);
            }
            if (endsendtime != null && !"".equals(endsendtime)) {
                paramMap.put("endsendtime", endsendtime);
            }
            paramMap.put("isread", isread);
            //收件人是当前登录人
            paramMap.put("noticetitle", noticetitle);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object>  datamap = new HashMap<>();
            if (userid != null && !"".equals(userid)) {
                paramMap.put("noticereceiverid", userid);
                datamap = noticeService.getReceiveNoticeInfoByParam(pagenum, pagesize, paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", datamap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/03/16 0016 下午 14:18
     * @Description: 根据通知主键ID获取通知详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getNoticeDetailDataByNoticeId", method = RequestMethod.POST)
    public Object getNoticeDetailDataByNoticeId( @RequestJson(value = "id", required = false) String noticeid) {
        try {
            Map<String, Object> dataMap = new HashMap<>();

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);

            dataMap = noticeService.getNoticeDetailDataByNoticeId(noticeid,userId);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/03/16 0016 下午 12:16
     * @Description: 发送通知
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "sendNotice", method = RequestMethod.POST)
    public Object sendNotice(
                             @RequestJson(value = "noticetitle", required = true) String noticetitle,
                             @RequestJson(value = "noticecontent", required = false) String noticecontent,
                             @RequestJson(value = "receiverids", required = true) List<String> receiverIds,
                             @RequestJson(value = "fkfileid", required = false) String fkfileid) {
        try {

            String userId= RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String userName = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            //添加通知信息
            Date nowDate = new Date();
            NoticeVO noticeVO = new NoticeVO();
            String pkNoticeId = UUID.randomUUID().toString();
            noticeVO.setPkNoticeid(pkNoticeId);
            noticeVO.setNoticetitle(noticetitle);
            noticeVO.setNoticecontent(noticecontent);
            noticeVO.setFkSenduserid(userId);
            noticeVO.setSendusername(userName);
            noticeVO.setSendtime(nowDate);
            noticeVO.setFileid(fkfileid);
            noticeVO.setIsrecall(0);
            int i = noticeService.addNoticeAndOtherInfo(noticeVO,receiverIds);
            //推送通知消息
            if (i > 0) {
                //推送到首页
                String messageType = CommonTypeEnum.HomePageMessageTypeEnum.NoticeMessage.getCode();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("userids", receiverIds);
                jsonobj.put("pkid", pkNoticeId);
                jsonobj.put("updatetime", noticeVO.getSendtime() != null ? DataFormatUtil.parseDateYMDHMS(noticeVO.getSendtime()) : DataFormatUtil.parseDateYMDHMS(new Date()));
                String str = "您有一条通知【"+noticeVO.getNoticetitle()+"】！";
                jsonobj.put("messagestr", str);
                jsonobj.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.NoticeMessage.getCode());
                jsonobj.put("isread", "0");
                //rabbitmqController.sendNoticeInfo(jsonobj, messageType);
            }
            return AuthUtil.parseJsonKeyToLower("success",  "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/17 0017 上午 10:10
     * @Description: 修改通告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateNoticeInfo", method = RequestMethod.POST)
    public Object updateNoticeInfo(
                                   @RequestJson(value = "pknoticeid", required = true) String pknoticeid,
                                   @RequestJson(value = "noticetitle", required = true) String noticetitle,
                                   @RequestJson(value = "noticecontent", required = false) String noticecontent,
                                   @RequestJson(value = "receiverids", required = true) List<String> receiverids,
                                   @RequestJson(value = "fileid", required = false) String fileid) {
        try {

            String userid= RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String userName = RedisTemplateUtil.getRedisCacheDataByToken("username",String.class);
            NoticeVO notice = noticeService.getNoticeInfoByNoticeID(pknoticeid);
            if (notice!=null){
                notice.setNoticetitle(noticetitle);
                notice.setNoticecontent(noticecontent);
                notice.setFkSenduserid(userid);
                notice.setSendusername(userName);
                notice.setSendtime(new Date());
                notice.setFileid(fileid);
                notice.setIsrecall(0);
                noticeService.updateNoticeInfo(notice, receiverids, userid);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/12/28 0028 上午 11:16
     * @Description: 撤回消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pknoticeid, noticetitle, noticecontent, receiverids, fkfileid]
     * @throws:
     */
    @RequestMapping(value = "recallNoticeInfo", method = RequestMethod.POST)
    public Object recallNoticeInfo(@RequestJson(value = "pknoticeid", required = true) String pknoticeid) {
        try {
            NoticeVO notice = noticeService.getNoticeInfoByNoticeID(pknoticeid);
            if (notice!=null){
                notice.setIsrecall(1);
                noticeService.recallNoticeInfo(notice);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/17 0017 上午 10:53
     * @Description: 删除通告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "deleteNoticeInfo", method = RequestMethod.POST)
    public Object deleteNoticeInfo(
                                   @RequestJson(value = "id", required = true) String pknoticeid
                                   ) {
        try {
            noticeService.deleteNoticeInfo(pknoticeid);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/16 0016 下午 12:16
     * @Description: 更新通知的已读未读（支持批量）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateNoticeIsRead", method = RequestMethod.POST)
    public Object updateNoticeIsRead(
                                     @RequestJson(value = "noticeids", required = true) List<String> noticeids,
                                     @RequestJson(value = "isread", required = false) Integer isread) {
        try {

            String userid= RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            noticeService.updateNoticeIsRead(noticeids, isread, userid);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/03/16 0016 下午 14:18
     * @Description: 删除已收到的通知（收件人删除自己已收到的通知，支持批量）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "deleteReceiveNotices", method = RequestMethod.POST)
    public Object deleteReceiveNotices(  @RequestJson(value = "noticeIds", required = true) List<String> noticeIds) {
        try {

            //删除通知记录

            String userid= RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("noticeIds", noticeIds);
            paramMap.put("userid", userid);
            noticeService.deleteReceiveNoticeByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

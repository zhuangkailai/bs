package com.tjpu.sp.service.impl.extand;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.extand.JGMessageRecordInfoMapper;
import com.tjpu.sp.dao.extand.JGUserRegisterInfoMapper;
import com.tjpu.sp.model.extand.JGMessageRecordInfoVO;
import com.tjpu.sp.model.extand.JGUserRegisterInfoVO;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.extand.JGMessageRecordInfoService;
import com.tjpu.sp.service.extand.JGUserRegisterInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class JGMessageRecordInfoServiceImpl implements JGMessageRecordInfoService {

    @Autowired
    private JGMessageRecordInfoMapper jgMessageRecordInfoMapper;
    @Autowired
    private JGUserRegisterInfoMapper jgUserRegisterInfoMapper;

    @Autowired
    private AuthSystemMicroService authSystemMicroService;
    /**
     * @author: lip
     * @date: 2020/8/20 0020 上午 10:24
     * @Description: 发生消息并添加消息记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void sendMessageAndAddData(JGMessageRecordInfoVO jgMessageRecordInfoVO,String pollutionid) {
        try {
            //获取企业关联用户
            List<String> regIds = jgUserRegisterInfoMapper.getEntRegIdList(pollutionid);
            if (regIds.size()>0){
                String messageContent = jgMessageRecordInfoVO.getMessagecontent();
                JSONObject contentJson = new JSONObject();
                contentJson.put("reminddata",messageContent);
                int messageType = jgMessageRecordInfoVO.getMessagetype();
                List<Map<String,Object>> messageDataList = new ArrayList<>();
                for (String regId:regIds){
                    Map<String,Object> messageMap = new HashMap<>();
                    messageMap.put("regid",regId);
                    messageMap.put("messagetype",jgMessageRecordInfoVO.getMessagetype());
                    messageMap.put("reminddata",contentJson);
                    messageMap.put("messagetitle",messageContent);
                    messageDataList.add(messageMap);
                }
                JSONObject JGJson = new JSONObject();
                JGJson.put("contenttype", messageType+"");
                JGJson.put("messagetype", messageType+"");
                JGJson.put("messagetypename","隐患整改消息");
                JGJson.put("messageanduserdata", messageDataList);
                authSystemMicroService.sendMessageAndTitleToAppClient(JGJson);
                jgMessageRecordInfoMapper.insert(jgMessageRecordInfoVO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

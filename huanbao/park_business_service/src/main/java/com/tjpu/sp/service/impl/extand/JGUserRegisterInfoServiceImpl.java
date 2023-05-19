package com.tjpu.sp.service.impl.extand;

import com.tjpu.sp.dao.extand.JGUserRegisterInfoMapper;
import com.tjpu.sp.dao.extand.TextMessageMapper;
import com.tjpu.sp.model.extand.JGUserRegisterInfoVO;
import com.tjpu.sp.service.extand.JGUserRegisterInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class JGUserRegisterInfoServiceImpl implements JGUserRegisterInfoService {

    @Autowired
    private JGUserRegisterInfoMapper jgUserRegisterInfoMapper;
    @Autowired
    private TextMessageMapper textMessageMapper;
    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 2:36
     * @Description: 更新用户注册信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void updateUserRegisterByParams(Map<String, Object> map) {

        if (map.get("userid") != null) {
            String userid = map.get("userid").toString();
            //根据用户ID删除记录
            jgUserRegisterInfoMapper.deleteByUserId(userid);
            //添加记录
            JGUserRegisterInfoVO jgUserRegisterInfoVO = new JGUserRegisterInfoVO();
            jgUserRegisterInfoVO.setPkId(UUID.randomUUID().toString());
            jgUserRegisterInfoVO.setAppkey(map.get("appkey").toString());
            jgUserRegisterInfoVO.setRegid(map.get("regid").toString());
            jgUserRegisterInfoVO.setFkUserid(userid);
            jgUserRegisterInfoVO.setPackagename(map.get("packagename").toString());
            jgUserRegisterInfoVO.setDeviceid(map.get("deviceid").toString());
            jgUserRegisterInfoVO.setUsercode(map.get("usercode")!=null?map.get("usercode").toString():null);
            jgUserRegisterInfoVO.setApptype(map.get("apptype")!=null?Integer.parseInt(map.get("apptype").toString()):null);
            jgUserRegisterInfoVO.setUpdatetime(new Date());
            jgUserRegisterInfoVO.setUpdateuser(map.get("username")!=null?map.get("username").toString():null);
            jgUserRegisterInfoMapper.insert(jgUserRegisterInfoVO);
        }


    }
    /**
     *
     * @author: lip
     * @date: 2019/8/2 0002 下午 2:54
     * @Description: 自定义查询条件获取用户注册信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @Override
    public List<Map<String, Object>> getUserRegisterInfoListByParam(Map<String, Object> paramMap) {
        return jgUserRegisterInfoMapper.getUserRegisterInfoListByParam(paramMap);
    }
    /**
     *
     * @author: lip
     * @date: 2020/3/12 0012 下午 2:07
     * @Description: 获取微信端用户的备注名称信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getUserInfoByPushType(String pushType) {
        return jgUserRegisterInfoMapper.getUserInfoByPushType(pushType);
    }
    /**
     *
     * @author: lip
     * @date: 2020/3/12 0012 下午 2:36
     * @Description: 自定义查询条件获取微信群信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getWeChartGroupByParam(Map<String, Object> paramMap) {
        return jgUserRegisterInfoMapper.getWeChartGroupByParam(paramMap);
    }

    /**
     *
     * @author: lip
     * @date: 2020/3/26 0026 上午 9:29
     * @Description: 获取用户微信消息推送设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getUserPushSetList() {
        return jgUserRegisterInfoMapper.getUserPushSetList();
    }

    @Override
    public List<String> getUserPushPhoneByParam(Map<String, Object> paramMap) {
        return jgUserRegisterInfoMapper.getUserPushPhoneByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getUserPushDataByParam(Map<String, Object> paramMap) {
        return jgUserRegisterInfoMapper.getUserPushDataByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTextMessageListData(JSONObject paramMap) {
        return textMessageMapper.getTextMessageListData(paramMap);
    }
}

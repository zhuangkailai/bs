package com.tjpu.sp.dao.extand;

import com.tjpu.sp.model.extand.JGUserRegisterInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface JGUserRegisterInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(JGUserRegisterInfoVO record);

    int insertSelective(JGUserRegisterInfoVO record);

    JGUserRegisterInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(JGUserRegisterInfoVO record);

    int updateByPrimaryKey(JGUserRegisterInfoVO record);

    void deleteByUserId(String userid);
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
    List<Map<String,Object>> getUserRegisterInfoListByParam(Map<String, Object> paramMap);

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
    List<Map<String,Object>> getUserInfoByPushType(String pushType);
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
    List<Map<String,Object>> getWeChartGroupByParam(Map<String, Object> paramMap);
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
    List<Map<String,Object>> getUserPushSetList();

    List<String> getEntRegIdList(String pollutionid);

    List<String> getUserPushPhoneByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getUserPushDataByParam(Map<String, Object> paramMap);
}
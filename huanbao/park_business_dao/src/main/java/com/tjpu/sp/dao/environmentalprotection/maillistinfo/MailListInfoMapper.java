package com.tjpu.sp.dao.environmentalprotection.maillistinfo;

import com.tjpu.sp.model.environmentalprotection.maillistinfo.MailListInfoVO;

import java.util.List;
import java.util.Map;

public interface MailListInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(MailListInfoVO record);

    int insertSelective(MailListInfoVO record);

    MailListInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(MailListInfoVO record);

    int updateByPrimaryKey(MailListInfoVO record);

    /**
     * @author: xsm
     * @date: 2019/9/17 0017 上午 11:44
     *
     * @Description: 获取所有联系单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getContactUnitSelectData();

    /**
     * @author: xsm
     * @date: 2019/9/17  0017 下午 1:15
     * @Description: 根据联系单位名称和人员名称判断是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> isTableDataHaveInfoByContactUnitAndPeopleName(Map<String, Object> paramMap);

    List<Map<String,Object>> getMailListInfoDataByParam(Map<String, Object> paramMap);
}
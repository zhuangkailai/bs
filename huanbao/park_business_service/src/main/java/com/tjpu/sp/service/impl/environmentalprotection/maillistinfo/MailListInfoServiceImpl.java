package com.tjpu.sp.service.impl.environmentalprotection.maillistinfo;

import com.tjpu.sp.dao.environmentalprotection.maillistinfo.MailListInfoMapper;
import com.tjpu.sp.service.environmentalprotection.maillistinfo.MailListInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MailListInfoServiceImpl implements MailListInfoService {
    @Autowired
    private MailListInfoMapper mailListInfoMapper;

    /**
     * @author: xsm
     * @date: 2019/9/17 0017 上午 11:44
     * @Description: 获取所有联系单位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getContactUnitSelectData() {
        return mailListInfoMapper.getContactUnitSelectData();
    }

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
    @Override
    public List<Map<String, Object>> isTableDataHaveInfoByContactUnitAndPeopleName(Map<String, Object> paramMap) {
        return mailListInfoMapper.isTableDataHaveInfoByContactUnitAndPeopleName(paramMap);
    }

    @Override
    public List<Map<String, Object>> getMailListInfoDataByParam(Map<String, Object> paramMap) {
        return mailListInfoMapper.getMailListInfoDataByParam(paramMap);
    }
}

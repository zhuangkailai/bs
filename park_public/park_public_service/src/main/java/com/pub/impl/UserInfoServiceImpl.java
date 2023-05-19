
package com.pub.impl;


import com.pub.dao.UserInfoMapper;
import com.pub.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public List<Map<String, Object>> getSystemAccessTokenByParam(HashMap<String, Object> hashMap) {
        return userInfoMapper.getSystemAccessTokenByParam(hashMap);
    }

}

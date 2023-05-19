package com.pub.dao;

import com.pub.model.UserInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface UserInfoMapper {

    void deleteUserAuthForAddMenu(@Param("menuids") List<String> menuids);

    UserInfoVO selectByPrimaryKey(@Param("userid") String userId);


    List<Map<String,Object>> getSystemAccessTokenByParam(HashMap<String, Object> hashMap);
}
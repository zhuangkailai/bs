package com.pub.dao;

import com.pub.model.ButtonVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ButtonMapper {

    List<ButtonVO> getButtonsByMenuIdAndUserId(@Param("menuid") String menuID, @Param("userid") String userID);

    List<ButtonVO> getButtonNameByButtonid(Map<String, Object> params);


}
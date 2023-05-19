package com.tjpu.auth.dao.system;

import com.tjpu.auth.model.system.ButtonVO;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface ButtonMapper {
    int deleteByPrimaryKey(String buttonId);

    int insert(ButtonVO record);

    int insertSelective(ButtonVO record);

    ButtonVO selectByPrimaryKey(String buttonId);

    int updateByPrimaryKeySelective(ButtonVO record);

    int updateByPrimaryKey(ButtonVO record);

    /**
     * @author: zhangzc
     * @date: 2018/5/24 8:48
     * @Description: 根据菜单id和用户id获取用户在此菜单上的按钮权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<ButtonVO> getButtonsByMenuIdAndUserId(@Param("menuid") String menuID, @Param("userid") String userID);

    /**
     * @author: xsm
     * @date: 2018/7/16 11:05
     * @Description: 获取用户权限的按钮名称集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
	List<ButtonVO> getButtonNameByButtonid(Map<String, Object> params);

	

}
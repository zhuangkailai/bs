package com.tjpu.auth.service.system;

import com.tjpu.auth.model.system.UserInfoVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年4月2日 上午10:54:18
 * @Description:用户信息操作接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public interface UserInfoService {
	
		
    /**
     * @param hashMap
     * @return
     * @author: lip
     * @date: 2018年4月2日 上午10:56:28
     * @Description:定义查询条件查询用户实体信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    UserInfoVO getUserInfoByParam(HashMap<String, Object> hashMap);

//    /**
//     * @author: zhangzc
//     * @date: 2018/5/24 15:39
//     * @Description: 根据用户id和系统id获取用户在该系统的菜单权限
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    Map<String, Object> getMenusByUserIdAndAppId(String userid,String appid);

    /**
     * @author: zhangzc
     * @param operateLogVO 
     * @date: 2018/6/8 17:58
     * @Description: 重置用户密码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void resetUserPassword(String userId) throws Exception;

    /**
     * @author: zhangzc
     * @param operateLogVO 
     * @date: 2018/6/8 17:58
     * @Description: 修改用户密码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void editUserPassword(String userId, String userPwd);

    /**
     * @author: zhangzc
     * @date: 2018/6/20 18:11
     * @Description: 判断用户密码是否正确
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:userId 用户id  userPwd用户密码
     * @return:
     */
    Boolean judgeUserPassword(String userId, String userPwd);


    /**
     * @author: zzc
     * @date: 2018/7/3 10:21
     * @Description: 动态条件获取用户信息
     * @updateUser: lip
     * @updateDate:2018-10-15
     * @updateDescription:参数修改为map
     * @param:userInfoMap
     * @return:
     */
    List<UserInfoVO> getUserInfoVOsByParam(Map<String,Object> paramMap);

    /**
     * 
     * @author: lip
     * @date: 2018年7月16日 上午9:33:39
     * @Description: 自定义参数更新语句
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param userInfo
     * @return
     */
	int updateByPrimaryKeySelective(UserInfoVO userInfo);

	/**      
	 * @author: xsm
	 * @date: 2018年8月1日 下午2:23:16
	 * @Description: 获取行政区划tree数据
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @return    
	 */
	List<Map<String, Object>> getRegionTreeData(String regionparentcode);

	/**      
	 * @author: xsm
	 * @date: 2018年8月3日 下午2:28:48
	 * @Description: 根据用户ID获取监管用户的数据权限	 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param userId
	 * @return    
	 */
	List<String> getRegionListByUserId(String userId);

	/**      
	 * @author: xsm
	 * @date: 2018年8月3日 下午3:53:12
	 * @Description: 根据用户ID获取企业用户的数据权限
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param userId
	 * @return    
	 */
	List<Map<String, Object>> getDataPermissionsByUserId(String userId);

	/**      
	 * @author: xsm
	 * @date: 2018年8月6日 上午11:20:30
	 * @Description: 根据行政区划父级编码查询其所有子级编码
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param regionCode：行政区划编码
	 * @return    
	 */
	List<String> getChildRegionCodeByParentRegionCode(String regionCode);

	/**      
	 * @author: xsm
	 * @date: 2018年8月7日 上午11:23:57
	 * @Description: 根据用户ID获取该用户的用户信息
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param userId:用户ID
	 * @return    
	 */
	UserInfoVO getUserInfoByUserId(String userId);

	/**      
	 * @author: xsm
	 * @date: 2018年8月7日 下午1:06:14
	 * @Description:根据污染源ID去数据中心库中查询ID对应的污染源信息 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param pollutionList:用户关联的相关企业ID的List
	 * @return    
	 */
	List<Map<String, Object>> getPollutionListByPollutionIdList(
			Map<String, Object> params);

	/**      
	 * @author: xsm			
	 * @date: 2018年8月9日 下午2:40:33
	 * @Description: 根据用户ID获取被重置密码的用户信息
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param resetUserId：被重置了密码的用户的ID
	 * @return    
	 */
	UserInfoVO selectUserByUserId(String resetUserId);

	/**      
	 * @author: xsm
	 * @date: 2018年8月14日 下午2:08:28
	 * @Description:根据自定义参数获取企业用户数据权限中污染源企业列表数据 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param paramMap:自定义参数
	 * @return    
	 */
	List<Map<String, Object>> getDataPermissionsListByParams(
			Map<String, Object> paramMap);

	
    /**
       *
       * @author: xsm
       * @date: 2018年8月30日 下午14:35:36
       * @Description:根据用户ID获取企业用户数据权限（企业）
       * @updateUser:
       * @updateDate:
       * @updateDescription:
       * @param hashMap
       * @return
       */
    List<String>  getDataPermissionsByUserID(String userId);





	/**
	 *
	 * @author: lip
	 * @date: 2018/9/27 0027 上午 10:24
	 * @Description: 根据用户ID,获取用的系统菜单权限
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param:
	 * @return:
	 */
	List<Map<String, Object>> getSystemRightByUserId(String userId);


	/**
	 * 
	 * @author: lip
	 * @date: 2018/10/20 0020 上午 11:36
	 * @Description: 根据用户ID,获取关联企业信息ID
	 * @updateUser: 
	 * @updateDate: 
	 * @updateDescription: 
	 * @param: 
	 * @return: 
	*/
    List<Map<String,Object>> getEntUserByUserId(String userId);


    /**
     * 
     * @author: lip
     * @date: 2018/10/25 0025 下午 2:02
     * @Description: 自定义查询条件，获取系统访问令牌数据
     * @updateUser: 
     * @updateDate: 
     * @updateDescription: 
     * @param: 
     * @return: 
    */
	List<Map<String,Object>> getSystemAccessTokenByParam(HashMap<String, Object> hashMap);

	/**
	 * @author: zhangzc
	 * @date: 2019/5/15 15:12
	 * @Description: 获取用户在菜单上的按钮权限
	 * @param:
	 * @return:
	 */
    Map<String,Object> getUserButtonAuthInMenu(String menuId, String userId);

    /**
     * @author: zzc
     * @date: 2019/10/14 10:19
     * @Description: 获取用户在所有菜单上的按钮权限
     * @param:
     * @return:
     */
    Map<String, List<Map<String, Object>>> getUserMenusButtonAuth(String userId);

    /**
     *
     * @author: lip
     * @date: 2020/4/20 0020 上午 10:36
     * @Description: 根据用户id获取用户关联的mn号权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<String> getUserDgimnListByUserId(String userId);

    List<String> getUserPollutionIdListByUserId(String userId);

    List<Map<String,Object>> getAllUserInfo();

	List<Map<String,Object>> getUserDepartmentTree();

    List<Map<String,Object>> getUserRoleListByUserId(String userId);
}

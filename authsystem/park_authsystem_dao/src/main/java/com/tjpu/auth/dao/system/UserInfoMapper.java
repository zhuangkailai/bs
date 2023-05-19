package com.tjpu.auth.dao.system;

import com.tjpu.auth.model.system.UserInfoVO;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface UserInfoMapper {
    int deleteByPrimaryKey(@Param("userid") String userId);

    int insert(UserInfoVO record);

    int insertSelective(UserInfoVO record);

    UserInfoVO selectByPrimaryKey(@Param("userid") String userId);

    int updateByPrimaryKeySelective(UserInfoVO record);

    int updateByPrimaryKey(UserInfoVO record);

    /**
     * @author: lip
     * @date: 2018年4月2日 上午10:56:28
     * @Description:定义查询条件查询用户实体信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param hashMap
     * @return:
     */
    UserInfoVO getUserInfoByParam(Map<String, Object> hashMap);

    /**
     * @author: zhangzc
     * @date: 2018/6/8 18:00
     * @Description:重置用户密码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void resetUserPassword(Map<String, String> map);
    /**
     * @author: zzc
     * @date: 2018/7/3 10:22
     * @Description: 动态条件获取用户实体信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:userInfoMap
     * @return:
     */
    List<UserInfoVO> getUserInfoVOsByParam(Map<String,Object> paramMap);

	/**      
	 * @author: xsm
	 * @date: 2018年8月1日 下午2:46:59
	 * @Description: 获取行政区划tree数据
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @return    
	 */
	List<Map<String, Object>> getRegionTreeData();

	/**      
	 * @author: xsm
	 * @date: 2018年8月3日 下午2:48:51
	 * @Description:根据用户ID获取监管用户的数据权限 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param userId:用户ID
	 * @return    
	 */
	List<String> getRegionListByUserId(String userid);

	/**      
	 * @author: xsm
	 * @date: 2018年8月3日 下午3:57:10
	 * @Description: 根据用户ID获取企业用户的数据权限
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param userId:用户ID
	 * @return    
	 */
	List<Map<String, Object>> getDataPermissionsByUserId(String userid);

	/**      
	 * @author: xsm
	 * @date: 2018年8月7日 下午1:09:03
	 * @Description: 根据污染源ID去数据中心库中查询ID对应的污染源信息 
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
	 * @date: 2018年8月14日 下午2:16:35
	 * @Description:根据自定义参数获取企业用户数据权限中污染源企业列表数据 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param paramMap
	 * @return    
	 */
	List<Map<String, Object>> getDataPermissionsListByParams(
			Map<String, Object> paramMap);

	
    /**
       *
       * @author: xsm
       * @date: 2018年8月30日 下午14:32:36
       * @Description:根据用户ID获取企业用户数据权限（企业）
       * @updateUser:
       * @updateDate:
       * @updateDescription:
       * @param hashMap
       * @return
       */      
	List<String> getPollutionIdListByUserId(String userId);
	/**
	 *
	 * @author: lip
	 * @date: 2018/10/20 0020 下午 1:24
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
	 * @author: chengzq
	 * @date: 2019/1/9 0009 下午 3:47
	 * @Description: 获取所有用户
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: []
	 * @throws:
	 */
    List<Map<String,Object>> getAllUser();

    /**
     * @author: chengzq
     * @date: 2019/1/9 0009 下午 3:52
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAuthByUseridAndMenuid(@Param("menuid") String menuid,@Param("userid")String userid);

	void deleteByUserAndMenu(Map<String,Object>  paramMap);


	/**
	 * @author: chengzq
	 * @date: 2019/1/8 0008 上午 11:33
	 * @Description: 查询所有用户父菜单下拥有权限的子菜单
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [paramMap]
	 * @throws:
	 */
	List<Map<String,Object>> getAllAuthByParentId(@Param("parentid") String parentid,@Param("userid") String userid);


	/**
	 * @author: chengzq
	 * @date: 2019/1/8 0008 上午 11:33
	 * @Description:  根据菜单id查询是否有权限
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [paramMap]
	 * @throws:
	 */
	List<Map<String,Object>> getAuthByMenuId(@Param("menuid") String menuid,@Param("userid")String userid);


	void insertUserRight(Map<String,Object> paramMap);

	/**
	  * @author: zhangzc
	  * @date: 2019/1/10 11:03
	  * @Description: 添加菜单时删除用户权限
	  * @updateUser:
	  * @updateDate:
	  * @updateDescription:
	  * @param:
	  * @return:
	  */
    void deleteUserAuthForAddMenu(@Param("menuids") List<String> menuids);

	/**
	 * @author: zzc
	 * @date: 2019/10/14 10:19
	 * @Description: 获取用户在所有菜单上的按钮权限
	 * @param:
	 * @return:
	 */
	List<Map<String,Object>> getUserMenusButtonAuth(@Param("userid") String userId);

	/**
	 *
	 * @author: lip
	 * @date: 2020/4/20 0020 上午 10:38
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

    List<Map<String,Object>> getDepartments();

    List<Map<String,Object>> getUserRoleListByUserId(String userId);
}
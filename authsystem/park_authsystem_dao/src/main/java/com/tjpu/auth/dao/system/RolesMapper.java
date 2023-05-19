package com.tjpu.auth.dao.system;

import com.tjpu.auth.model.system.RolesVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RolesMapper {
    int deleteByPrimaryKey(String rolesId);

    int insert(RolesVO record);

    int insertSelective(RolesVO record);

    RolesVO selectByPrimaryKey(String rolesId);

    /**
     * @author: zhangzc
     * @date: 2018/6/21 13:50
     * @Description: 获取顶级角色实体信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    RolesVO getTopRoleVO();

    int updateByPrimaryKeySelective(RolesVO record);

    int updateByPrimaryKey(RolesVO record);

    /**
     * @author: zhangzc
     * @date: 2018/6/21 13:50
     * @Description: 获取角色树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<RolesVO> getRoleTreeData();

    /**
     * @author: zzc
     * @date: 2018/7/17 11:41
     * @Description: 删除顶级角色权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deleteTopRoleAuth(@Param("topRoleId") String topRoleId);

    /**
     * @author: zzc
     * @date: 2018/7/17 11:41
     * @Description: 重置顶级角色权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void resetTopRoleAuth(@Param("topRoleId") String topRoleId);


    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 12:45
     * @Description: 根据角色id集合获取角色拥有的功能权限
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:roleids
     * @Return:
     */
    List<Map<String, Object>> getRoleAuthByRoleIDs(@Param("roleids") List<String> roleids);

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 12:45
     * @Description: 根据角色id获取角色拥有的功能权限
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:roleids
     * @Return:
     */
    List<Map<String, Object>> getRoleAuthByRoleID(@Param("roleid") String id);

    /**
     * @author: chengzq
     * @date: 2019/1/8 0008 上午 11:33
     * @Description: 根据菜单id查询是否有权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAuthByMenuId(@Param("menuid") String menuid, @Param("rolesid") String rolesid);

    /**
     * @author: chengzq
     * @date: 2019/1/8 0008 上午 11:33
     * @Description: 查询父菜单下拥有权限的子菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getAllAuthByParentId(@Param("parentid") String parentid,@Param("rolesid") String rolesid);


    void deleteByRoleAndMenu(Map<String, Object> paramMap);


    void insertRolesRight(Map<String, Object> paramMap);


    List<Map<String, Object>> getAllRole();

    /**
     * @author: zhangzc
     * @date: 2019/1/10 10:59
     * @Description: 添加菜单时删除角色权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deleteRoleAuthForAddMenu(@Param("menuids") List<String> menuids);

    /**
      * @author: zhangzc
      * @date: 2019/1/10 14:31
      * @Description: 角色权限改变时获取子角色ID
      * @updateUser:
      * @updateDate:
      * @updateDescription:
      * @param:
      * @return:
      */
    List<String> getSonRoleIDsForChangeParentRoleAuth(@Param("roleid") String roleid);
}
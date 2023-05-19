package com.tjpu.auth.dao.system;

import com.tjpu.auth.model.system.SysMenuVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SysMenuMapper {
    int deleteByPrimaryKey(@Param("menuid") String menuId);

    int insert(SysMenuVO record);

    int insertSelective(SysMenuVO record);

    SysMenuVO selectByPrimaryKey(@Param("menuid") String menuId);

    int updateByPrimaryKeySelective(SysMenuVO record);

    int updateByPrimaryKey(SysMenuVO record);

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 10:03
     * @Description: 获取所有的权限菜单资源
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<SysMenuVO> getMenuList();

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 10:03
     * @Description: 获取菜单资源不包括menu_type = 0 的菜单，此类菜单与标签页类似，不展示为菜单，但权限又需要配置其按钮权限，所以进行筛选
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<SysMenuVO> getMenusByMenuType();


//    /**
//     * @author: zhangzc
//     * @date: 2018/5/24 8:49
//     * @Description: 根据用户ID和系统Id获取用户在此系统的菜单权限
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    List<SysMenuVO> getMenusByUserIdAndAppId(@Param("userid") String userid, @Param("appid") String appid);

    /**
     * @author: ZhangZhenChao
     * @date: 2018/7/25 15:47
     * @Description: 根据菜单Code获取菜单实体
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: menuCode 菜单code
     * @return:
     */
    SysMenuVO getMenuVOByMenuCode(@Param("menucode") String menuCode);

    /**
     * @author: xsm
     * @date: 2018/7/15 18:33
     * @Description: 获取用户权限的菜单名称集合。
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: params 获取用户权限的菜单名称集合的查询条件
     * @return:
     */
    List<SysMenuVO> getMenuNameByMenuid(Map<String, Object> params1);

    /**
     * 根据系统ID获取系统拥有的所有菜单信息
     *
     * @param appid
     * @return
     */
    List<SysMenuVO> getMenusByAppID(@Param("appid") String appid);
    /**
     *
     * @author: lip
     * @date: 2018/9/26 0026 下午 8:57
     * @Description: 获取root菜单：系统名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<SysMenuVO> getRootMenuByCode(String rootCode);

    /**
     * 
     * @author: lip
     * @date: 2018/9/27 0027 上午 9:07
     * @Description: 获取用户权限关联的菜单
     * @updateUser: 
     * @updateDate: 
     * @updateDescription: 
     * @param: 
     * @return: 
    */
    List<SysMenuVO> getMenuIdByUserId(String userId);

     /**
       * @Author: zhangzc
       * @Date: 2018/9/27 14:49
       * @Description: 获取所有的菜单信息包含按钮
       * @UpdateUser:
       * @UpdateDate:
       * @UpdateDescription:
       * @Param:
       * @Return:
       */
    List<SysMenuVO> getMenuAndButtonList();

    List<SysMenuVO> getSonNodesByParentID(@Param("parentid") String parentId);

    void batchUpdateMenuSortCode(@Param("menudatamap") Map<String, Object> meunsortdata,@Param("menuids") List<String> menuids);

    /**
     * @author: chengzq
     * @date: 2019/1/8 0008 下午 1:05
     * @Description:  查询所有子菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parentid]
     * @throws:
     */
    List<String> getMenuIdsByParentId(@Param("parentid") String parentid);

    /**
     * @author: chengzq
     * @date: 2019/1/8 0008 下午 3:54
     * @Description: 获取所有菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getAllMenu();

    /**
     *
     * @author: lip
     * @date: 2018/9/26 0026 下午 6:54
     * @Description: 自定义表，自定义条件，查询表记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getTableDataByParam(Map<String, Object> map);

    List<String> getUserAppsByUserId(@Param("userid") String userid);
}
package com.tjpu.auth.service.system;

import com.tjpu.auth.model.system.SysMenuVO;

import java.util.List;
import java.util.Map;

/**
 * @Author: zzc
 * @Date: 2018/6/27 14:14
 * @Description:菜单sevice层接口
 */
public interface SysMenuService {
    /**
     * @author: zzc
     * @date: 2018/6/27 14:23
     * @Description: 获取各个系统的菜单信息    系统结构维护中的菜单树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAppMenus();

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
    SysMenuVO getMenuVOByMenuCode(String menuCode);


    List<SysMenuVO> getMenuList();

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 11:17
     * @Description: 获取菜单和按钮树形结构数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String, Object>> getMenuAndButtonTreeData();

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 13:01
     * @Description: 获取菜单树形结构数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String, Object>> getMenuTreeData();

    SysMenuVO selectByPrimaryKey(String menuid);

    void updateByPrimaryKeySelective(SysMenuVO sysMenuVO,Map<String,Object> paramMap);

    List<SysMenuVO> getSonNodesByParentID(String parentId);

    void moveMenu(SysMenuVO sysMenuVO, List<SysMenuVO> menuVOS,Map<String,Object> paramMap);

    List<Map<String,Object>> getTableDataByParam(Map<String, Object> map);


    List<String> getUserAppsByUserId(String userid);
}

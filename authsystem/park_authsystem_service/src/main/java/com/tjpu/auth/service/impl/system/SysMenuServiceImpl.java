package com.tjpu.auth.service.impl.system;

import com.tjpu.auth.dao.system.RolesMapper;
import com.tjpu.auth.dao.system.SysAppMapper;
import com.tjpu.auth.dao.system.SysMenuMapper;
import com.tjpu.auth.dao.system.UserInfoMapper;
import com.tjpu.auth.model.system.ButtonVO;
import com.tjpu.auth.model.system.SysMenuVO;
import com.tjpu.auth.service.system.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Author: zzc
 * @Date: 2018/6/27 14:14
 * @Description:菜单service层实现类
 */
@Service
@Transactional
public class SysMenuServiceImpl implements SysMenuService {

    private final SysAppMapper sysAppMapper;
    private final SysMenuMapper sysMenuMapper;
    private final RolesMapper rolesMapper;
    private final UserInfoMapper userInfoMapper;

    @Autowired
    public SysMenuServiceImpl(SysAppMapper sysAppMapper, SysMenuMapper sysMenuMapper,RolesMapper rolesMapper,UserInfoMapper userInfoMapper) {
        this.sysAppMapper = sysAppMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.rolesMapper = rolesMapper;
        this.userInfoMapper=userInfoMapper;
    }

    @Override
    public List<Map<String, Object>> getAppMenus() {
        List<Map<String, Object>> result = new ArrayList<>();
//        List<SysAppVO> sysAppVOs = sysAppMapper.getAppMenus();
//        for (SysAppVO sysAppVO : sysAppVOs) {
//            Map<String, Object> appMap = new LinkedHashMap<>();
//            appMap.put("labelName", sysAppVO.getAppName());
//            appMap.put("id", sysAppVO.getAppId());
//            appMap.put("appID", sysAppVO.getAppId());
//            appMap.put("flag", "app"); // 系统标示
//            List<Map<String, Object>> menuList = formatParentMenuData(sysAppVO.getSysMenuVOs(), sysAppVO.getAppId(), sysAppVO.getAppId());
//            if (menuList.size() > 0) {
//                appMap.put("children", menuList);
//            }
//            result.add(appMap);
//        }
        return result;
    }


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
    @Override
    public SysMenuVO getMenuVOByMenuCode(String menuCode) {
        return sysMenuMapper.getMenuVOByMenuCode(menuCode);
    }

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 11:13
     * @Description: 获取所有的菜单数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<SysMenuVO> getMenuList() {
        return sysMenuMapper.getMenuList();
    }

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
    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMenuAndButtonTreeData() {
        try {
            List<Map<String, Object>> resultInfo = new ArrayList<>();
            List<SysMenuVO> menuList = sysMenuMapper.getMenuAndButtonList();
            if (menuList.size() > 0) {
                for (SysMenuVO sysMenuVO : menuList) {
                    if ("root".equals(sysMenuVO.getParentId())) {
                        Map<String, Object> appMap = new LinkedHashMap<>();
                        appMap.put("labelName", sysMenuVO.getMenuName()); // 系统名称
                        appMap.put("id", sysMenuVO.getMenuId()); // 系统ID
                        appMap.put("flag", "app"); // 系统标示
                        appMap.put("parentid", sysMenuVO.getParentId());
                        if (menuList.size() > 0) {
                            final List<Map<String, Object>> menuListInfo = formatParentData(menuList, sysMenuVO.getMenuId());
                            if (menuListInfo.size() > 0) {
                                appMap.put("children", menuListInfo);
                            }
                        }
                        resultInfo.add(appMap);
                    }
                }
            }
            return resultInfo;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

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
    @Override
    public List<Map<String, Object>> getMenuTreeData() {
        try {
            List<Map<String, Object>> resultInfo = new ArrayList<>();
            List<SysMenuVO> menuList = sysMenuMapper.getMenuList();
            if (menuList.size() > 0) {
                for (SysMenuVO sysMenuVO : menuList) {
                    if ("root".equals(sysMenuVO.getParentId())) {
                        Map<String, Object> appMap = new LinkedHashMap<>();
                        appMap.put("labelName", sysMenuVO.getMenuName()); // 系统名称
                        appMap.put("labelCode", sysMenuVO.getMenuCode()); // 菜单code
                        appMap.put("menuimg", sysMenuVO.getMenuImg()); // 菜单图标
                        appMap.put("navigateurl", sysMenuVO.getNavigateUrl()); // 菜单URL
                        appMap.put("parentid", sysMenuVO.getParentId());
                        appMap.put("id", sysMenuVO.getMenuId()); // 系统ID
                        appMap.put("AllowDelete", sysMenuVO.getAllowDelete() == null || sysMenuVO.getAllowDelete() == 1);
                        appMap.put("AllowEdit", sysMenuVO.getAllowEdit() == null || sysMenuVO.getAllowEdit() == 1);
                        appMap.put("flag", "app"); // 系统标示
                        appMap.put("appID", sysMenuVO.getMenuId());
                        if (menuList.size() > 0) {
                            final List<Map<String, Object>> menuListInfo = formatParentMenuData(menuList, sysMenuVO.getMenuId(), sysMenuVO.getMenuId());
                            if (menuListInfo.size() > 0) {
                                appMap.put("children", menuListInfo);
                            }
                        }
                        resultInfo.add(appMap);
                    }
                }
            }
            return resultInfo;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public SysMenuVO selectByPrimaryKey(String menuid) {
        return sysMenuMapper.selectByPrimaryKey(menuid);
    }

    private List<Map<String, Object>> formatParentMenuData(List<SysMenuVO> menuList, String parentId, String appID) {
        List<Map<String, Object>> sysMenuList = new ArrayList<>();
        for (SysMenuVO sysMenuVO : menuList) {
            if (parentId.equals(sysMenuVO.getParentId())) {
                Map<String, Object> menuMap = new LinkedHashMap<>();
                menuMap.put("id", sysMenuVO.getMenuId());
                menuMap.put("parentid", sysMenuVO.getParentId());
                menuMap.put("labelName", sysMenuVO.getMenuName());
                menuMap.put("labelCode", sysMenuVO.getMenuCode());
                menuMap.put("menuimg", sysMenuVO.getMenuImg());
                menuMap.put("navigateurl", sysMenuVO.getNavigateUrl());
                menuMap.put("flag", "menu");
                menuMap.put("appID", appID);
                menuMap.put("AllowDelete", sysMenuVO.getAllowDelete() == null || sysMenuVO.getAllowDelete() == 1);
                menuMap.put("AllowEdit", sysMenuVO.getAllowEdit() == null || sysMenuVO.getAllowEdit() == 1);
                final List<Map<String, Object>> menuMapInfo = formatParentMenuData(menuList, sysMenuVO.getMenuId(), appID);
                if (menuMapInfo.size() > 0) {
                    menuMap.put("children", menuMapInfo);
                }
                sysMenuList.add(menuMap);
            }
        }
        return sysMenuList;
    }

    private List<Map<String, Object>> formatParentData(List<SysMenuVO> sysMenuVOs, String parentId) {
        List<Map<String, Object>> sysMenuList = new ArrayList<>();
        for (SysMenuVO sysMenuVO : sysMenuVOs) {
            if (parentId.equals(sysMenuVO.getParentId())) {
                Map<String, Object> menuMap = new LinkedHashMap<>();
                menuMap.put("labelName", sysMenuVO.getMenuName());
                menuMap.put("id", sysMenuVO.getMenuId());
                menuMap.put("parentid", sysMenuVO.getParentId());
                menuMap.put("flag", "menu");
                final List<Map<String, Object>> menuMapInfo = formatParentData(sysMenuVOs, sysMenuVO.getMenuId());
                final List<ButtonVO> buttonVOs = sysMenuVO.getButtonVOs();
                List<Map<String, Object>> buttonList = new ArrayList<>();
                if (buttonVOs.size() > 0) {
                    for (ButtonVO buttonVO : buttonVOs) {
                        Map<String, Object> buInfo = new LinkedHashMap<>();
                        buInfo.put("flag", "button");   //按钮标示
                        buInfo.put("id", sysMenuVO.getMenuId() + "_" + buttonVO.getButtonId()); //按钮id
                        buInfo.put("labelName", buttonVO.getButtonName());  //按钮名称
                        buttonList.add(buInfo);
                    }
                }
                if (menuMapInfo.size() > 0) {
                    buttonList.addAll(menuMapInfo);
                }
                if (buttonList.size() > 0) {
                    menuMap.put("children", buttonList);
                }
                sysMenuList.add(menuMap);
            }
        }
        return sysMenuList;
    }

    @Override
    public void updateByPrimaryKeySelective(SysMenuVO sysMenuVO,Map<String,Object> paramMap) {
        sysMenuMapper.updateByPrimaryKeySelective(sysMenuVO);
        changeRoleAuth(paramMap);
        changeUserAuth(paramMap);
    }

    @Override
    public List<SysMenuVO> getSonNodesByParentID(String parentId) {
        return sysMenuMapper.getSonNodesByParentID(parentId);
    }

    @Override
    public void moveMenu(SysMenuVO sysMenuVO, List<SysMenuVO> menuVOS,Map<String,Object> paramMap) {
        Map<String, Object> meunsortdata = new HashMap<>();
        List<String> menuids = new ArrayList<>();
        for (int i = 0; i < menuVOS.size(); i++) {
            meunsortdata.put(menuVOS.get(i).getMenuId(), i);
            menuids.add(menuVOS.get(i).getMenuId());
        }
        if (sysMenuVO != null) {
            sysMenuMapper.updateByPrimaryKeySelective(sysMenuVO);
        }
        sysMenuMapper.batchUpdateMenuSortCode(meunsortdata, menuids);
        changeRoleAuth(paramMap);
        changeUserAuth(paramMap);

    }

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
    @Override
    public List<Map<String, Object>> getTableDataByParam(Map<String, Object> map) {
        return sysMenuMapper.getTableDataByParam(map);
    }

    @Override
    public List<String> getUserAppsByUserId(String userid) {
        return sysMenuMapper.getUserAppsByUserId(userid);
    }


    /**
     * @author: chengzq
     * @date: 2019/1/9 0009 上午 11:13
     * @Description: 移动菜单修改角色权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    private void changeRoleAuth(Map<String,Object> paramMap){
        String sourceParentId="";
        String moveMenuId="";
        String nowParentId="";
        if(paramMap.get("movemenuid") != null){
            moveMenuId=paramMap.get("movemenuid").toString();
        }
        if(paramMap.get("sourceparentid") != null){
            sourceParentId=paramMap.get("sourceparentid").toString();
        }
        if(paramMap.get("nowparentid") != null){
            nowParentId=paramMap.get("nowparentid").toString();
        }
        //统计菜单权限不需要修改
        if(sourceParentId.equals(nowParentId)){
            return ;
        }
        if(!"".equals(moveMenuId)){
            List<Map<String, Object>> allRole = rolesMapper.getAllRole();
            List<Map<String, Object>> allMenu = sysMenuMapper.getAllMenu();
            //查询父菜单下所有子菜单
            List<String> menuIdsByParentId = sysMenuMapper.getMenuIdsByParentId(sourceParentId);
            for (Map<String, Object> stringObjectMap : allRole) {
                //<1>现父菜单权限修改
                //查询移动菜单是否有权限
                List<Map<String,Object>> authByMenuId = rolesMapper.getAuthByMenuId(moveMenuId,stringObjectMap.get("Roles_ID").toString());
                //移动菜单没有权限 父菜单权限需要改变
                if(authByMenuId.size()==0){
                    //删除原父菜单权限 递归向上删除父菜单权限
                    List<String> parentIds=new ArrayList<>();
                    parentIds.add(nowParentId);
                    getAllParentids(allMenu, nowParentId,parentIds);
                    Map<String,Object> param=new HashMap<>();
                    param.put("rolesid",stringObjectMap.get("Roles_ID").toString());
                    param.put("menuid",parentIds);
                    rolesMapper.deleteByRoleAndMenu(param);
                }

                //<2>原父菜单权限修改
                //查询父菜单下拥有权限的子菜单
                List<Map<String, Object>> allAuthByParentId = rolesMapper.getAllAuthByParentId(sourceParentId,stringObjectMap.get("Roles_ID").toString());
                //现在原父菜单下的子菜单都是有权限的
                if(menuIdsByParentId.size()>0 && menuIdsByParentId.size()==allAuthByParentId.size()){
                    Map<String,Object> param=new HashMap<>();
                    param.put("rolesid",stringObjectMap.get("Roles_ID").toString());
                    param.put("menuid",sourceParentId);
                    //查询原父菜单有没有权限
                    List<Map<String,Object>> authByMenuId1 = rolesMapper.getAuthByMenuId(sourceParentId,stringObjectMap.get("Roles_ID").toString());
                    //没有权限原父菜单新增权限
                    if(authByMenuId1.size()==0){
                        rolesMapper.insertRolesRight(param);
                    }
                }
            }
        }
    }



    /**
     * @author: chengzq
     * @date: 2019/1/9 0009 上午 11:13
     * @Description: 移动菜单修改用户权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    private void changeUserAuth(Map<String,Object> paramMap){
        String sourceParentId="";
        String moveMenuId="";
        String nowParentId="";
        if(paramMap.get("movemenuid") != null){
            moveMenuId=paramMap.get("movemenuid").toString();
        }
        if(paramMap.get("sourceparentid") != null){
            sourceParentId=paramMap.get("sourceparentid").toString();
        }
        if(paramMap.get("nowparentid") != null){
            nowParentId=paramMap.get("nowparentid").toString();
        }
        //统计菜单权限不需要修改
        if(sourceParentId.equals(nowParentId)){
            return ;
        }
        if(!"".equals(moveMenuId)){

            List<Map<String, Object>> allUser = userInfoMapper.getAllUser();
            List<Map<String, Object>> allMenu = sysMenuMapper.getAllMenu();
            //查询父菜单下所有子菜单
            List<String> menuIdsByParentId = sysMenuMapper.getMenuIdsByParentId(sourceParentId);
            for (Map<String, Object> stringObjectMap : allUser) {
                //<1>现父菜单权限修改
                List<Map<String, Object>> authByMenuId = userInfoMapper.getAuthByUseridAndMenuid(moveMenuId, stringObjectMap.get("User_ID").toString());
                //移动菜单没有权限 父菜单权限需要改变
                if(authByMenuId.size()==0){
                    //删除原父菜单权限 递归向上删除父菜单权限

                    List<String> parentIds=new ArrayList<>();
                    parentIds.add(nowParentId);
                    getAllParentids(allMenu, nowParentId,parentIds);
                    Map<String,Object> param=new HashMap<>();
                    param.put("userid",stringObjectMap.get("User_ID").toString());
                    param.put("menuid",parentIds);
                    userInfoMapper.deleteByUserAndMenu(param);
                }

                //<2>原父菜单权限修改
                //查询父菜单下拥有权限的子菜单
                List<Map<String, Object>> allAuthByParentId = userInfoMapper.getAllAuthByParentId(sourceParentId,stringObjectMap.get("User_ID").toString());
                //现在原父菜单下的子菜单都是有权限的
                if(menuIdsByParentId.size()>0 && menuIdsByParentId.size()==allAuthByParentId.size()){
                    Map<String,Object> param=new HashMap<>();
                    param.put("userid",stringObjectMap.get("User_ID").toString());
                    param.put("menuid",sourceParentId);
                    //查询原父菜单有没有权限
                    List<Map<String,Object>> authByMenuId1 = userInfoMapper.getAuthByMenuId(sourceParentId,stringObjectMap.get("User_ID").toString());
                    //没有权限原父菜单新增权限
                    if(authByMenuId1.size()==0){
                        userInfoMapper.insertUserRight(param);
                    }
                }
            }
        }
    }




    /**
     * @author: chengzq
     * @date: 2019/1/9 0009 上午 11:12
     * @Description: 获取所有父菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramList, menuid, parentIds]
     * @throws:
     */
    private void getAllParentids(List<Map<String,Object>> paramList,String menuid,List<String> parentIds){
        for (Map<String, Object> stringObjectMap : paramList) {
            if(stringObjectMap.get("Menu_Id")!=null && stringObjectMap.get("Menu_Id").toString().equals(menuid)){
                parentIds.add(stringObjectMap.get("ParentId").toString());
                String parentid=stringObjectMap.get("ParentId").toString();
                getAllParentids(paramList,parentid,parentIds);
            }
        }
    }
}

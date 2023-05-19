
package com.tjpu.auth.service.impl.system;


import com.tjpu.auth.dao.system.ButtonMapper;
import com.tjpu.auth.dao.system.SysMenuMapper;
import com.tjpu.auth.dao.system.UserInfoMapper;
import com.tjpu.auth.model.system.ButtonVO;
import com.tjpu.auth.model.system.SysMenuVO;
import com.tjpu.auth.model.system.UserInfoVO;
import com.tjpu.auth.service.common.CommonServiceSupport;
import com.tjpu.auth.service.system.UserInfoService;
import com.tjpu.pk.common.utils.ConstantsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private ButtonMapper buttonMapper;


    @Autowired
    private SysMenuMapper sysMenuMapper;
//    @Autowired
//    private SysAppMapper sysAppMapper;

    /**
     * @author: zzc
     * @date: 2018/7/3 15:40
     * @Description: 动态条件获取用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<UserInfoVO> getUserInfoVOsByParam(Map<String, Object> paramMap) {
        return userInfoMapper.getUserInfoVOsByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2018年4月2日 上午10:56:28
     * @Description:定义查询条件查询用户实体信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: hashMap
     * @return:
     */
    @Override
    public UserInfoVO getUserInfoByParam(HashMap<String, Object> hashMap) {
        return userInfoMapper.getUserInfoByParam(hashMap);
    }

//    /**
//     * @author: zhangzc
//     * @date: 2018/5/23 13:30
//     * @Description: 获取用户左侧菜单权限
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    @Override
//    public Map<String, Object> getMenusByUserIdAndAppId(String userid, String appid) {
//        Map<String, Object> data = new LinkedHashMap<>(); // 系统信息map
//        // 系统信息
//        SysAppVO appVO = sysAppMapper.selectByPrimaryKey(appid);
//        //根据系统ID查询系统拥有的菜单
//        List<SysMenuVO> allMenuVos = sysMenuMapper.getMenusByAppID(appid);
//        //获取用户拥有的菜单权限
//        List<SysMenuVO> SysMenuVOs = sysMenuMapper.getMenusByUserIdAndAppId(userid, appid);
//        List<Map<String, Object>> dataList = getUserMenuAuth(allMenuVos, SysMenuVOs, appVO);
//        data.put("icon", appVO.getAppImg());
//        data.put("appName", appVO.getAppName());
//        data.put("dataList", dataList);
//        return data;
//    }

//    private List<Map<String, Object>> getUserMenuAuth(List<SysMenuVO> allMenuVos, List<SysMenuVO> sysMenuVOs, SysAppVO appVO) {
//        List<SysMenuVO> menus = new ArrayList<>();
//        for (SysMenuVO menuVO : sysMenuVOs) {
//            String parentId = menuVO.getParentId();
//            String menuId = menuVO.getMenuId();
//            //倒着递归找到其上的所有父级菜单
//            getMenuParent(allMenuVos, parentId, menus, menuId);
//        }
//        List<String> menuIDs = new ArrayList<>();
//        for (SysMenuVO menu : menus) {
//            menuIDs.add(menu.getMenuId());
//        }
//        allMenuVos.removeIf(menuVo -> !menuIDs.contains(menuVo.getMenuId()));
//        return formatParentMenuData(allMenuVos, appVO.getAppId(),appVO.getAppId());
//    }

    private void getMenuParent(List<SysMenuVO> allMenuVos, String parentID, List<SysMenuVO> menus, String menuId) {
        for (SysMenuVO menuVo : allMenuVos) {
            String menuIdInfo = menuVo.getMenuId();
            if (menuIdInfo.equals(parentID)) {
                if (!menus.contains(menuVo)) {
                    menus.add(menuVo);
                    getMenuParent(allMenuVos, menuVo.getParentId(), menus, menuIdInfo);
                }
            } else if (menuIdInfo.equals(menuId)) {
                if (!menus.contains(menuVo)) {
                    menus.add(menuVo);
                }
            }
        }
    }

    /**
     * @param
     * @return
     * @author: zhangzc
     * @date: 2018/4/23 13:42
     * @Description: 父子菜单重组
     * @updateUser:lip
     * @updateDate:2018-07-31
     * @updateDescription:添加navigateUrl：前端路由地址，pageMark：页面标记，用于区分是否公共页面
     */
    private List<Map<String, Object>> formatParentMenuData(List<SysMenuVO> sysMenus, String parentId, String appid) {
        List<Map<String, Object>> sysMenuList = new ArrayList<>();
        for (SysMenuVO sysMenuVO : sysMenus) {
            if (parentId.equals(sysMenuVO.getParentId())) {
                Map<String, Object> parentMap = new LinkedHashMap<>();
                parentMap.put("icon", sysMenuVO.getMenuImg() != null ? sysMenuVO.getMenuImg().toString() : "");
                parentMap.put("menucode", sysMenuVO.getMenuCode() != null ? sysMenuVO.getMenuCode() : "");
                parentMap.put("menutype", sysMenuVO.getMenuType() != null ? sysMenuVO.getMenuType() : "");
                //parentMap.put("appid", appid);
                parentMap.put("parentid", sysMenuVO.getParentId());
                parentMap.put("menutype", sysMenuVO.getMenuType()!=null?sysMenuVO.getMenuType():"");
                parentMap.put("target", sysMenuVO.getTarget() != null ? sysMenuVO.getTarget() : "");
                parentMap.put("menuId", sysMenuVO.getMenuId() != null ? sysMenuVO.getMenuId().toString() : "");
                parentMap.put("menuName", sysMenuVO.getMenuName() != null ? sysMenuVO.getMenuName().toString() : "");
                parentMap.put("menuTitle", sysMenuVO.getMenuTitle() != null ? sysMenuVO.getMenuTitle().toString() : "");
                parentMap.put("sysmodel", sysMenuVO.getMenuCode() != null ? sysMenuVO.getMenuCode().toString() : ""); // 菜单所对应的模块
                parentMap.put("navigateUrl", sysMenuVO.getNavigateUrl() != null ? sysMenuVO.getNavigateUrl().toString() : ""); // 菜单url
                parentMap.put("pageMark", getPageMark(parentMap.get("navigateUrl") != null ? parentMap.get("navigateUrl").toString() : "")); // 页面标记
                parentMap.put("dataListChildren", formatParentMenuData(sysMenus, sysMenuVO.getMenuId(), appid));
                sysMenuList.add(parentMap);
            }
        }
        return sysMenuList;
    }


    /**
     * @author: zhangzc
     * @date: 2018/6/8 17:59
     * @Description: 重置用户密码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void resetUserPassword(String userId) {
        Map<String, String> map = new HashMap<>();
        map.put("userid", userId);
        map.put("user_pwd", CommonServiceSupport.getDefaultFieldMethodValue(ConstantsUtil.DefaultMethod.GETINITIALPASSWORD.getValue()));
        userInfoMapper.resetUserPassword(map);
        // 重置用户密码日志
    }

    /**
     * @author: zhangzc
     * @date: 2018/6/20 11:05
     * @Description: 修改用户密码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void editUserPassword(String userId, String userPwd) {
        Map<String, String> map = new HashMap<>();
        map.put("userid", userId);
        map.put("user_pwd", userPwd);
        userInfoMapper.resetUserPassword(map);
    }

    /**
     * @author: zhangzc
     * @date: 2018/6/20 18:11
     * @Description: 判断用户密码是否正确
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [userId, userPwd]
     * @return: java.lang.Boolean
     */
    @Override
    public Boolean judgeUserPassword(String userId, String userPwd) {
        // 根据用户id获取用户对象
        final UserInfoVO userInfoVO = userInfoMapper.selectByPrimaryKey(userId);
        if (userInfoVO.getUserPwd().equals(userPwd)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * @param navigateUrl
     * @return
     * @author: lip
     * @date: 2018年8月1日 上午9:41:26
     * @Description: 获取前端路由路径
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    private String getUrl(String navigateUrl) {
        String url = "";

        if (navigateUrl != null && navigateUrl.indexOf("?") > -1) {
            url = navigateUrl.split("[?]")[0];
        } else {
            url = navigateUrl;
        }
        return url;
    }


    /**
     * @param navigateUrl ：路由地址
     * @return
     * @author: lip
     * @date: 2018年7月31日 上午8:42:48
     * @Description: 从路由地址中分离出页面标记
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    private String getPageMark(String navigateUrl) {

        String pageMark = "";
        if (navigateUrl != null) {
            String url = navigateUrl.toString();
            if (url.indexOf("/") > -1) {
                int length = url.split("/").length;
                pageMark = url.split("/")[length - 1];
            }
        }

        return pageMark;
    }

    /**
     * @author: lip
     * @date: 2018年7月16日 上午9:33:39
     * @Description: 自定义参数更新语句
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: userInfo
     * @return:
     */
    @Override
    public int updateByPrimaryKeySelective(UserInfoVO userInfo) {
        return userInfoMapper.updateByPrimaryKeySelective(userInfo);
    }

    /**
     * @author: xsm
     * @date: 2018/8/1 14：25
     * @Description:获取行政区划tree的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getRegionTreeData(String regionparentcode) {
        // TODO Auto-generated method stub
        //切换数据源查询数据中心库中行政区划码表中所有数据
        List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> regionList = userInfoMapper.getRegionTreeData();
        //Map<String, Object> resultMap = new HashMap<String, Object>();
        for (int i = 0; i < regionList.size(); i++) {
            Map<String, Object> resultMap = regionList.get(i);
            if (regionparentcode.equals(resultMap.get("ParentCode"))) {// 父节点
                Map<String, Object> parentMap = new HashMap<String, Object>();
                parentMap.put("id", resultMap.get("Code"));
                parentMap.put("label", resultMap.get("Name"));
                List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
                String parentCode = resultMap.get("Code").toString();
                childList = getChildrenRegionList(regionList, parentCode);
                parentMap.put("children", childList);
                listData.add(parentMap);
            }
        }
        return listData;
    }

    /**
     * @param regionList：查询出来的所有行政区划节点数据
     * @param parentCode：父节点编码
     * @return
     * @author: xsm
     * @date: 2018年8月1日 下午3:47:33
     * @Description:判断行政区划节点是否存在子节点，并返回
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    private List<Map<String, Object>> getChildrenRegionList(
            List<Map<String, Object>> regionList, String parentCode) {
        // TODO Auto-generated method stub
        List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < regionList.size(); i++) {
            if (regionList.get(i).get("ParentCode") != null && regionList.get(i).get("ParentCode").equals(parentCode)) {
                Map<String, Object> chlidMap = new HashMap<String, Object>();
                chlidMap.put("id", regionList.get(i).get("Code"));
                chlidMap.put("label", regionList.get(i).get("Name"));
                List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
                childList = getChildrenRegionList(regionList, regionList.get(i).get("Code").toString());
                chlidMap.put("children", childList);
                listData.add(chlidMap);
            }
        }
        return listData;
    }

    /**
     * @author: xsm
     * @date: 2018年8月3日 下午2:47:33
     * @Description:根据用户ID获取监管用户的数据权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param： userId:用户ID
     * @return：
     */
    @Override
    public List<String> getRegionListByUserId(String userid) {
        // TODO Auto-generated method stub
        return userInfoMapper.getRegionListByUserId(userid);
    }

    /**
     * @param userId:用户ID
     * @return
     * @author: xsm
     * @date: 2018年8月3日 下午3:54:36
     * @Description: 根据用户ID获取企业用户的数据权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public List<Map<String, Object>> getDataPermissionsByUserId(String userId) {
        // TODO Auto-generated method stub
        return userInfoMapper.getDataPermissionsByUserId(userId);
    }

    /**
     * @author: xsm
     * @date: 2018年8月6日 下午13:04:36
     * @Description: 根据行政区划编码获取其子节点行政区划编码
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: userId:用户ID
     * @return:
     */
    @Override
    public List<String> getChildRegionCodeByParentRegionCode(String regionCode) {
        // TODO Auto-generated method stub
        String parentCode = regionCode;
        //查询所有行政区划数据
        List<Map<String, Object>> regionList = userInfoMapper.getRegionTreeData();
        List<String> regionCodeList = new ArrayList<String>();
        regionCodeList.add(regionCode);
        regionCodeList = getChildrenRegionCodeList(regionList, parentCode, regionCodeList);
        return regionCodeList;
    }

    /**
     * @author: xsm
     * @date: 2018年8月1日 下午3:47:33
     * @Description:判断行政区划节点是否存在子节点，并返回
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: regionList:查询出来的所有行政区划节点数据
     * @param: parentCode:父节点编码
     * @param: regionCodeList:存储行政区划编码的list集合
     * @return:
     */
    private List<String> getChildrenRegionCodeList(
            List<Map<String, Object>> regionList, String parentCode, List<String> regionCodeList) {
        // TODO Auto-generated method stub
        for (int i = 0; i < regionList.size(); i++) {
            if (regionList.get(i).get("ParentCode") != null && regionList.get(i).get("ParentCode").equals(parentCode)) {
                regionCodeList.add(regionList.get(i).get("Code").toString());
                regionCodeList = getChildrenRegionCodeList(regionList, regionList.get(i).get("Code").toString(), regionCodeList);
            }
        }
        return regionCodeList;
    }

    /**
     * @author: xsm
     * @date: 2018年8月7日 上午11:25:36
     * @Description: 根据用户ID获取该用户的用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: userId:用户ID
     * @return:
     */
    @Override
    public UserInfoVO getUserInfoByUserId(String userId) {
        // TODO Auto-generated method stub
        return userInfoMapper.selectByPrimaryKey(userId);
    }

    /**
     * @return
     * @author: xsm
     * @date: 2018年8月7日 下午1:08:16
     * @Description:根据污染源ID去数据中心库中查询ID对应的污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param：pollutionList:用户关联的相关企业ID的List
     */
    @Override
    public List<Map<String, Object>> getPollutionListByPollutionIdList(
            Map<String, Object> params) {
        // TODO Auto-generated method stub
        return userInfoMapper.getPollutionListByPollutionIdList(params);
    }

    /**
     * @param resetUserId：被重置了密码的用户的ID
     * @return
     * @author: xsm
     * @date: 2018年8月9日 下午2:43:33
     * @Description: 根据用户ID获取被重置密码的用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public UserInfoVO selectUserByUserId(String resetUserId) {
        // TODO Auto-generated method stub
        return userInfoMapper.selectByPrimaryKey(resetUserId);
    }

    /**
     * @param paramMap:自定义参数
     * @return
     * @author: xsm
     * @date: 2018年8月14日 下午2:08:28
     * @Description:根据自定义参数获取企业用户数据权限中污染源企业列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public List<Map<String, Object>> getDataPermissionsListByParams(
            Map<String, Object> paramMap) {
        // TODO Auto-generated method stub
        return userInfoMapper.getDataPermissionsListByParams(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2018年8月30日 下午2:00:29
     * @Description:根据用户ID获取用户数据权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param： paramMap:自定义参数
     * @return：
     */
    @Override
    public List<String> getDataPermissionsByUserID(String userId) {
        // TODO Auto-generated method stub
        List<String> list = new ArrayList<>();
        // 根据用户ID查询用户信息
        UserInfoVO user = userInfoMapper.selectByPrimaryKey(userId);
        // 获取用户类型
        String userType = user.getUserType();
        // 判断用户类型，"0":表示监管用户；"1":表示企业用户。
        if ("0".equals(userType)) {
            // 根据用户ID去获取用户行政区划关系表中该用户关联的行政区划Code(一对多)
            list = userInfoMapper.getRegionListByUserId(userId);
        } else if ("1".equals(userType)) {
            list = userInfoMapper.getPollutionIdListByUserId(userId);
        }
        return list;
    }

    /**
     * @author: lip
     * @date: 2018/9/27 0027 上午 10:24
     * @Description: 根据用户ID, 获取用的系统菜单权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getSystemRightByUserId(String userId) {
        List<Map<String, Object>> appList = new ArrayList<>();
        //获取拥有的所有系统权限
        List<SysMenuVO> allMenuVos = sysMenuMapper.getMenuList();
        try {
            List<SysMenuVO> userAllSysmenu = new ArrayList<>();
            List<SysMenuVO> userSysmenu = sysMenuMapper.getMenuIdByUserId(userId);
            userAllSysmenu = getUserMenusAuth(allMenuVos, userSysmenu);
            for (SysMenuVO sysMenuVO : userAllSysmenu) {
                if ("root".equals(sysMenuVO.getParentId())) {
                    Map<String, Object> appMap = new HashMap<>();
                    String appid = sysMenuVO.getMenuId();
                    appMap.put("menuid", appid);
                    appMap.put("menuname", sysMenuVO.getMenuName());
                    appMap.put("menutype", sysMenuVO.getMenuType()!=null?sysMenuVO.getMenuType():"");
                    appMap.put("menucode", sysMenuVO.getMenuCode()!=null?sysMenuVO.getMenuCode():"");
                    appMap.put("icon", sysMenuVO.getMenuImg() != null ? sysMenuVO.getMenuImg() : "");
                    appMap.put("target", sysMenuVO.getTarget()!= null ? sysMenuVO.getTarget() : "");
                    appMap.put("parentid", sysMenuVO.getParentId() != null ? sysMenuVO.getParentId() : "");
                    appMap.put("navigateurl", sysMenuVO.getNavigateUrl() != null ? sysMenuVO.getNavigateUrl() : "");
                    appMap.put("datalistchildren", formatParentMenuData(userAllSysmenu, sysMenuVO.getMenuId(), appid));
                    appList.add(appMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return appList;
    }

    /**
     * @author: lip
     * @date: 2018/10/20 0020 下午 1:24
     * @Description: 根据用户ID, 获取关联企业信息ID
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEntUserByUserId(String userId) {
        return userInfoMapper.getEntUserByUserId(userId);
    }

    /**
     * @author: lip
     * @date: 2018/10/25 0025 下午 2:02
     * @Description: 自定义查询条件，获取系统访问令牌数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getSystemAccessTokenByParam(HashMap<String, Object> hashMap) {
        return userInfoMapper.getSystemAccessTokenByParam(hashMap);
    }

    /**
     * @author: zhangzc
     * @date: 2018/6/21 11:29
     * @Description: 获取用户在菜单上拥有的按钮权限信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getUserButtonAuthInMenu(String menuID, String userID) {
        Map<String, Object> resultMap = new HashMap<>(3);
        /* 根据菜单id和用户id获取用户在此菜单上的按钮权限 */
        List<ButtonVO> buttons = buttonMapper.getButtonsByMenuIdAndUserId(menuID, userID);
        if (buttons.size() > 0) {
            //列表外的按钮
            List<Map<String, Object>> topOperations = new ArrayList<>();
            //列 表内的按钮
            List<Map<String, Object>> listOperation = new ArrayList<>();
            for (ButtonVO button : buttons) {
                Map<String, Object> buttonMap = new HashMap<>(6);
                // 图标按钮样式（点击事件名称）
                buttonMap.put("name", button.getButtonCode());
                // 名称
                buttonMap.put("label", button.getButtonName());
                // 图标
                buttonMap.put("icon", button.getButtonImg());
                // 按钮样式
                buttonMap.put("type", button.getButtonStyle());
                //列表外的按钮
                if ("2".equals(button.getButtonType())) {
                    topOperations.add(buttonMap);
                }
                //列表内的按钮
                if ("1".equals(button.getButtonType())) {
                    listOperation.add(buttonMap);
                }
            }
            resultMap.put("topbuttondata", topOperations);
            resultMap.put("tablebuttondata", listOperation);
        }
        return resultMap;
    }

    /**
     * @author: zzc
     * @date: 2019/10/14 10:19
     * @Description: 获取用户在所有菜单上的按钮权限
     * @param:
     * @return:
     */
    @Override
    public Map<String, List<Map<String, Object>>> getUserMenusButtonAuth(String userId) {
        List<Map<String, Object>> list = userInfoMapper.getUserMenusButtonAuth(userId);
        //根据菜单ID分组
        return list.stream().collect(Collectors.groupingBy(m -> m.get("Menu_Code").toString()));
    }
    /**
     *
     * @author: lip
     * @date: 2020/4/20 0020 上午 10:37
     * @Description: 根据用户id获取用户关联的mn号权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @Override
    public List<String> getUserDgimnListByUserId(String userId) {
        return userInfoMapper.getUserDgimnListByUserId(userId);
    }

    @Override
    public List<String> getUserPollutionIdListByUserId(String userId) {
        return userInfoMapper.getUserPollutionIdListByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getAllUserInfo() {
        return userInfoMapper.getAllUserInfo();
    }

    @Override
    public List<Map<String, Object>> getUserDepartmentTree() {
        //获取用户和部门信息
        List<Map<String, Object>> Departments = userInfoMapper.getDepartments();
        List<Map<String, Object>> organizationTree = new ArrayList<>();
        if (Departments.size() > 0) {
            for (Map<String, Object> map : Departments) {
                if ((map.get("parentId") == null || map.get("parentId").equals("")) && map.get("flag").equals("1")) {
                    String organizationId = map.get("id").toString();
                    Map<String, Object> topOrganization = new HashMap<>();
                    topOrganization.put("name", map.get("name"));   //部门名称
                    topOrganization.put("flag", map.get("flag"));   //部门名称
                    List<Map<String, Object>> maps = formatParentData(Departments, organizationId);    //递归获取下面子节点
                    if (maps.size() > 0) {
                        organizationTree.addAll(maps);
                    }
                }
            }
        }
        return organizationTree;
    }

    @Override
    public List<Map<String, Object>> getUserRoleListByUserId(String userId) {
        return userInfoMapper.getUserRoleListByUserId(userId);
    }


    private List<Map<String, Object>> formatParentData(List<Map<String, Object>> organizationVO, String parentId) {
        List<Map<String, Object>> organizationTree = new ArrayList<>();
        for (Map<String, Object> vo : organizationVO) {
            if (vo.get("parentId") != null && vo.get("parentId").equals(parentId)) {
                String organizationId = vo.get("id").toString();
                Map<String, Object> parentMap = new HashMap<>();
                parentMap.put("name", vo.get("name"));
                parentMap.put("flag", vo.get("flag"));
                parentMap.put("id", vo.get("id"));
                if (vo.get("flag").equals("2")) {
                    parentMap.put("phone", vo.get("phone"));
                }
                final List<Map<String, Object>> menuMapInfo = formatParentData(organizationVO, organizationId);
                if (menuMapInfo.size() > 0) {
                    parentMap.put("children", menuMapInfo);
                }
                organizationTree.add(parentMap);
            }

        }
        return organizationTree;
    }

    private List<SysMenuVO> getUserMenusAuth(List<SysMenuVO> allMenuVos, List<SysMenuVO> sysMenuVOs) {
        List<SysMenuVO> menus = new ArrayList<>();
        for (SysMenuVO menuVO : sysMenuVOs) {
            String parentId = menuVO.getParentId();
            String menuId = menuVO.getMenuId();
            //倒着递归找到其上的所有父级菜单
            getMenuParent(allMenuVos, parentId, menus, menuId);
        }
        List<String> menuIDs = new ArrayList<>();
        for (SysMenuVO menu : menus) {
            menuIDs.add(menu.getMenuId());
        }
        allMenuVos.removeIf(menuVo -> !menuIDs.contains(menuVo.getMenuId()));
        return allMenuVos;
    }

}

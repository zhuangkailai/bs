package com.tjpu.auth.controller.system.menuController;

import com.tjpu.auth.common.utils.RedisTemplateUtil;
import com.tjpu.auth.model.system.SysMenuVO;
import com.tjpu.auth.service.system.SysMenuService;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import io.swagger.annotations.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @version V1.0
 * @author: zzc
 * @date: 2018/6/2 19:04
 * @Description: 菜单控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@Api(value = "菜单操作Api", tags = {"菜单操作接口"})
@RequestMapping("menuController")
public class SysMenuController {
    private final SysMenuService sysMenuService;

    @Autowired
    public SysMenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }


    /**
     * @author: zzc
     * @date: 2018/6/28 9:27
     * @Description: 获取菜单树形结构数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "获取菜单树形结构数据", notes = "获取菜单树形结构数据")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @GetMapping(value = "getMenuTreeData")
    public Object getMenuTreeData() {
        try {
            List<Map<String, Object>> menuTreeData = sysMenuService.getMenuTreeData();
            String userid= RedisTemplateUtil.getRedisCacheDataByToken("userid",String.class);
            List<String> userApps = sysMenuService.getUserAppsByUserId(userid);
            List<Map<String,Object>> dataList = new ArrayList<>();

            if (userApps.size()>0){
                for (Map<String, Object> dataMap:menuTreeData){
                    if (userApps.contains(dataMap.get("id"))){
                        dataList.add(dataMap);
                    }
                }
            }else {
                dataList = menuTreeData;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("roleTreeData", dataList);
            result.put("pk", "menu_id");
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @ApiOperation(value = "获取菜单和按钮树形结构数据", notes = "获取菜单和按钮树形结构数据")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @GetMapping(value = "getMenuAndButtonTreeData")
    public Object getMenuAndButtonTreeData() {
        try {
            List<Map<String, Object>> resultInfo = sysMenuService.getMenuAndButtonTreeData();
            return AuthUtil.parseJsonKeyToLower("success", resultInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Author: zhangzc
     * @Date: 2019/1/3 11:21
     * @Description: 菜单拖拽
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param: flag 移动方式 movemenuid操作菜单ID targetmenuid目标菜单ID
     * @Return:
     */
    @PostMapping(value = "moveMenu")
    public Object moveMenu(@RequestJson(value = "flag") String flag,
                           @RequestJson(value = "movemenuid") String moveMenuId,
                           @RequestJson(value = "sourceparentid") String sourceparentid,
                           @RequestJson(value = "nowparentid") String nowparentid,
                           @RequestJson(value = "targetmenuid") String targetMenuId) {
        try {
            SysMenuVO moveMenu = sysMenuService.selectByPrimaryKey(moveMenuId);
            SysMenuVO targetMenu = sysMenuService.selectByPrimaryKey(targetMenuId);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("movemenuid", moveMenuId);
            paramMap.put("sourceparentid", sourceparentid);
            paramMap.put("nowparentid", nowparentid);
            List<SysMenuVO> sonlist;
            if (flag.equals("inner")) {
                paramMap.put("nowparentid", targetMenuId);
                moveMenu.setParentId(targetMenuId);
                sonlist = sysMenuService.getSonNodesByParentID(targetMenuId);
                if (sonlist.size() == 0) {
                    moveMenu.setSortCode(0);
                } else {
                    moveMenu.setSortCode(sonlist.size() + 1);
                }
                sysMenuService.updateByPrimaryKeySelective(moveMenu, paramMap);
            } else if (flag.equals("before") || flag.equals("after")) {
                if (!moveMenu.getParentId().equals(targetMenu.getParentId())) {
                    moveMenu.setParentId(targetMenu.getParentId());
                    sonlist = sysMenuService.getSonNodesByParentID(targetMenu.getParentId());
                    int index = 0;
                    for (int i = 0; i < sonlist.size(); i++) {
                        if (sonlist.get(i).getMenuId().equals(targetMenuId)) {
                            index = i;
                            break;
                        }
                    }
                    if (flag.equals("before")) {
                        sonlist.add(index, moveMenu);
                    } else {
                        sonlist.add(index + 1, moveMenu);
                    }
                    sysMenuService.moveMenu(moveMenu, sonlist, paramMap);
                } else {
                    sonlist = sysMenuService.getSonNodesByParentID(targetMenu.getParentId());
                    sonlist.removeIf(menuVO -> menuVO.getMenuId().equals(moveMenuId));
                    int index = 0;
                    for (int i = 0; i < sonlist.size(); i++) {
                        if (sonlist.get(i).getMenuId().equals(targetMenuId)) {
                            index = i;
                            break;
                        }
                    }
                    if (flag.equals("before")) {
                        sonlist.add(index, moveMenu);
                    } else {
                        sonlist.add(index + 1, moveMenu);
                    }
                    sysMenuService.moveMenu(null, sonlist, paramMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/5/16 0016 上午 9:21
     * @Description: 根据菜单ID, 获取第一级菜单信息（不包含级联菜单以及当前菜单信息）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLoginUserFirstMenuByMenuId", method = RequestMethod.POST)
    public Object getLoginUserFirstMenuByMenuId(@RequestJson(value = "menuid") String menuid) {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            //递归遍历，找出菜单id=menuid的菜单，返回datalistchildren
            List<Map<String, Object>> dataList = null;
            List<Map<String, Object>> menuChildren = getChildrenByMenuMark(menuid, "menuid", objectList, dataList);
            if (menuChildren != null) {
                for (Map<String, Object> menu : menuChildren) {
                    menu.replace("datalistchildren", new ArrayList<>());
                }
            }
            dataMap.put("datalist", menuChildren);

            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/16 0016 上午 9:21
     * @Description: 根据菜单编码, 获取第一级菜单信息（不包含级联菜单以及当前菜单信息）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLoginUserFirstMenuByMenuCode", method = RequestMethod.POST)
    public Object getLoginUserFirstMenuByMenuCode(@RequestJson(value = "menucode") String menuCode) {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);

            List<Map<String, Object>> dataList = null;
            List<Map<String, Object>> menuChildren = getChildrenByMenuMark(menuCode, "menucode", objectList, dataList);

            dataMap.put("datalist", menuChildren);
            JSONObject menuData = getMenuDataByMenuCode(menuCode, objectList);
            if (menuData.containsKey("datalistchildren")){
                menuData.remove("datalistchildren");
            }
            dataMap.put("menudata", menuData);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private JSONObject getMenuDataByMenuCode(String menuCode, List<JSONObject> objectList) {
        JSONObject jsonObjectRe = new JSONObject();
        if (objectList != null ) {
            for (JSONObject jsonObject : objectList) {
                if (jsonObject.get("menucode") != null&&jsonObjectRe.size()==0) {
                    if (menuCode.equals(jsonObject.get("menucode").toString())) {
                        jsonObjectRe = jsonObject;
                        break;
                    } else {
                        objectList = (List<JSONObject>) jsonObject.get("datalistchildren");
                        jsonObjectRe = getMenuDataByMenuCode(menuCode, objectList);
                    }
                }
            }
        }
        return jsonObjectRe;
    }


    /**
     * @author: lip
     * @date: 2019/5/16 0016 上午 9:21
     * @Description: 根据菜单编码, 获取子菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLoginUserMenuDataByMenuCode", method = RequestMethod.POST)
    public Object getLoginUserMenuDataByMenuCode(@RequestJson(value = "menucode") String menuCode) {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);

            List<Map<String, Object>> dataList = null;
            List<Map<String, Object>> menuChildren = getChildrenByMenuMark(menuCode, "menucode", objectList, dataList);
            dataMap.put("datalist", menuChildren);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/16 0016 上午 9:21
     * @Description: 获取移动App全部菜单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAppAllMenuInfo", method = RequestMethod.POST)
    public Object getAppAllMenuInfo() {
        try {
            Map<String, Object> appMenu = new LinkedHashMap<>();
            List<Map<String, Object>> menuTreeData = sysMenuService.getMenuTreeData();

            String appId = "app10";

            for (Map<String, Object> map : menuTreeData) {
                if (appId.equals(map.get("id"))) {
                    List<JSONObject> userAuth = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
                    Set<String> menuIds = getMenuIds(userAuth, appId);
                    String menuId = map.get("id").toString();
                    appMenu.put("isright", menuIds.contains(menuId));
                    appMenu.put("menuid", map.get("id"));
                    appMenu.put("menuname", map.get("labelName"));
                    appMenu.put("menucode", map.get("labelCode"));
                    appMenu.put("menuimg", map.get("menuimg"));
                    List<Map<String, Object>> children = getAppChildren(map.get("children"), menuIds);
                    appMenu.put("children", children);
                    break;
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", appMenu);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private Set<String> getMenuIds(List<JSONObject> objectList, String appId) {
        Set<String> menuIds = new HashSet<>();
        if (objectList.size() > 0) {
            String menuId;
            for (JSONObject jsonObject : objectList) {
                if (appId.equals(jsonObject.get("menuid"))) {
                    menuId = jsonObject.getString("menuid");
                    menuIds.add(menuId);
                    if (jsonObject.get("datalistchildren") != null) {
                        menuIds.addAll(getChildrenMenuIds(jsonObject.get("datalistchildren")));
                    }
                    break;
                }
            }
        }
        return menuIds;
    }

    private Set<String> getChildrenMenuIds(Object children) {
        Set<String> menuIds = new HashSet<>();
        if (children != null && !"".equals(children)) {
            List<Map<String, Object>> subTemp = (List<Map<String, Object>>) children;
            for (Map<String, Object> map : subTemp) {
                menuIds.add(map.get("menuid").toString());
                if (map.get("datalistchildren") != null) {
                    menuIds.addAll(getChildrenMenuIds(map.get("datalistchildren")));
                }

            }
        }
        return menuIds;
    }

    /**
     * @author: lip
     * @date: 2019/5/16 0016 上午 9:21
     * @Description: 根据菜单ID获取移动App子菜单菜单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLoginUserAppChildrenMenuByMenuId", method = RequestMethod.POST)
    public Object getLoginUserAppMenuByMenuId(@RequestJson("menuid") String menuid) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            for (JSONObject jsonObject : objectList) {
                if ("app10".equals(jsonObject.get("menuid"))) {
                    if (jsonObject.get("datalistchildren") != null) {
                        List<Map<String, Object>> chlidren = getChildrenByMenuMark(menuid, "menuid", objectList, null);
                        if (chlidren != null && chlidren.size() > 0) {
                            for (Map<String, Object> map : chlidren) {
                                Map<String, Object> appMenu = new HashMap<>();
                                appMenu.put("menuid", map.get("menuid"));
                                appMenu.put("menuname", map.get("menuname"));
                                appMenu.put("menucode", map.get("menucode"));
                                appMenu.put("menuimg", map.get("icon"));
                                appMenu.put("navigateurl", map.get("navigateurl"));
                                dataList.add(appMenu);
                            }
                        }
                    }
                    break;
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/7/25 0025 下午 3:23
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getAppChildren(Object children, Set<String> menuIds) {
        if (children != null && !"".equals(children)) {
            List<Map<String, Object>> sub = new ArrayList<>();
            List<Map<String, Object>> subTemp = (List<Map<String, Object>>) children;
            for (Map<String, Object> map : subTemp) {
                Map<String, Object> appMenu = new LinkedHashMap<>();
                appMenu.put("menuid", map.get("id"));
                appMenu.put("isright", menuIds.contains(map.get("id")));
                appMenu.put("menuname", map.get("labelName"));
                appMenu.put("menucode", map.get("labelCode"));
                appMenu.put("menuimg", map.get("menuimg"));
                appMenu.put("navigateurl", map.get("navigateurl"));
                appMenu.put("children", getAppChildren(map.get("children"), menuIds));
                sub.add(appMenu);
            }
            return sub;
        } else {
            return null;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/16 0016 上午 9:21
     * @Description: 根据菜单ID, 获取级联菜单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: menuid 菜单ID
     * @return:
     */
    @RequestMapping(value = "getLoginUserCascadeMenuByMenuId", method = RequestMethod.POST)
    public Object getLoginUserCascadeMenuByMenuId(@RequestJson(value = "menuid") String menuid) {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            //递归遍历，找出菜单id=menuid的菜单，返回datalistchildren
            List<Map<String, Object>> dataList = null;
            List<Map<String, Object>> menuChildren = getChildrenByMenuMark(menuid, "menuid", objectList, dataList);
            //递归子菜单，menuType=0时置空当前菜单的子菜单
            if (menuChildren!=null){
                setMenuChildren(menuChildren);
            }
            dataMap.put("datalist", menuChildren);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //递归子菜单，menuType=0时置空当前菜单的子菜单
    private void setMenuChildren(List<Map<String, Object>> menuChildren) {
        for (Map<String, Object> map : menuChildren) {
            if (map.get("menutype") != null && "0".equals(map.get("menutype").toString())) {
                map.replace("datalistchildren", new ArrayList<>());
            } else {
                List<Map<String, Object>> mapChlidren = (List<Map<String, Object>>) map.get("datalistchildren");
                setMenuChildren(mapChlidren);
                map.put("datalistchildren", mapChlidren);
            }
        }


    }


    /**
     * @author: lip
     * @date: 2019/5/16 0016 上午 9:29
     * @Description: 私有方法：根据菜单id获取子菜单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    private List<Map<String, Object>> getChildrenByMenuMark(String menuMarkValue, String menuMarkKey,
                                                            List<JSONObject> objectList,
                                                            List<Map<String, Object>> dataList) {
        if (objectList != null && dataList == null) {
            for (JSONObject jsonObject : objectList) {
                if (jsonObject.get(menuMarkKey) != null) {
                    if (menuMarkValue.equals(jsonObject.get(menuMarkKey).toString())) {
                        dataList = (List<Map<String, Object>>) jsonObject.get("datalistchildren");
                        break;
                    } else {
                        objectList = (List<JSONObject>) jsonObject.get("datalistchildren");
                        dataList = getChildrenByMenuMark(menuMarkValue, menuMarkKey, objectList, dataList);
                    }
                }
            }
        }
        return dataList;
    }


    /**
     * @author: lip
     * @date: 2018/9/26 0026 下午 7:17
     * @Description: 根据系统id获取当前登陆用户的系统信息以及一级菜单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "根据系统id获取系统信息以及一级菜单信息", notes = "根据系统id获取系统信息以及一级菜单信息")
    @ApiImplicitParam(name = "appid", value = "系统主键ID（必传）", defaultValue = "", required = true, dataType = "String")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getLoginUserFirstMenuByAppId", method = RequestMethod.POST)
    public Object getLoginUserFirstMenuByAppId(@RequestJson(value = "appid") String appid) {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            if (objectList != null) {
                for (JSONObject jsonObject : objectList) {
                    if (appid.equals(jsonObject.get("appid").toString())) {
                        dataMap.put("appname", jsonObject.get("appname"));
                        List<Map<String, Object>> menuChildren = (List<Map<String, Object>>) jsonObject.get("appmenus");
                        dataMap.put("datalist", menuChildren);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2018/9/26 0026 下午 7:18
     * @Description: 根据菜单ID和系统ID获取当前登陆用户级联菜单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "根据菜单ID和系统ID获取当前登陆用户级联菜单信息", notes = "根据菜单ID和系统ID获取当前登陆用户级联菜单信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "menuid", value = "菜单主键ID", defaultValue = "", required = true, dataType = "String"),
            @ApiImplicitParam(name = "appid", value = "系统主键ID", defaultValue = "", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pollutionid", value = "污染源主键ID", defaultValue = "", required = false, dataType = "String")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @RequestMapping(value = "getLoginUserCascadeMenuByMenuIdAndAppId", method = RequestMethod.POST)
    public Object getLoginUserCascadeMenuByMenuIdAndAppId(
            @RequestJson(value = "menuid") String menuid,
            @RequestJson(value = "appid") String appid,
            @RequestJson(value = "pollutionid", required = false) String pollutionid
    ) {
        try {
            List<Map<String, Object>> dataListSub = new ArrayList<>();
            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
            if (objectList != null) {
                for (JSONObject jsonObject : objectList) {
                    if (appid.equals(jsonObject.get("appid"))) {
                        //系统下的菜单集合
                        List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonObject.get("appmenus");
                        for (Map<String, Object> map : dataList) {
                            if (map.get("menuid") != null && map.get("menuid").equals(menuid)) {
                                dataListSub = (List<Map<String, Object>>) map.get("datalistchildren");
                                break;
                            } else {
                                dataListSub = getDataListSubInfo(map, menuid);
                                if (dataListSub != null) {
                                    break;
                                }
                            }
                        }
                        if (dataListSub == null) {
                            dataListSub = new ArrayList<>();
                        }
                        setCascadeMenu(dataListSub, pollutionid);
                        for (Map<String, Object> objectMap : dataListSub) {
                            if (objectMap.get("menutype") != null && objectMap.get("menutype").equals(2)) {
                                objectMap.replace("datalistchildren", new ArrayList<>());
                            }
                        }
                        break;
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataListSub);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getDataListSubInfo(Map<String, Object> mapInfo, String menuid) {
        List<Map<String, Object>> datalistchildren = (List<Map<String, Object>>) mapInfo.get("datalistchildren");
        for (Map<String, Object> map : datalistchildren) {
            if (map.get("menuid") != null && map.get("menuid").equals(menuid)) {
                return (List<Map<String, Object>>) map.get("datalistchildren");
            } else {
                getDataListSubInfo(map, menuid);
            }
        }
        return null;
    }


    /**
     * @author: lip
     * @date: 2018/9/26 0026 下午 4:26
     * @Description: 递归菜单，当前菜单信息以及所有子菜单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setCascadeMenu(List<Map<String, Object>> menuChildren, String pollutionid) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Map<String, Object> map : menuChildren) {
            map.put("menucount", getMenuCountByPollutionId(map.get("menucode"), pollutionid));
            List<Map<String, Object>> chlidrenList = (List<Map<String, Object>>) map.get("datalistchildren");
            setCascadeMenu(chlidrenList, pollutionid);
        }
    }

    /**
     * @author: lip
     * @date: 2018/9/26 0026 下午 6:29
     * @Description: 根据污染源ID获取关联表记录数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Object getMenuCountByPollutionId(Object menucode, String pollutionid) {
        if (pollutionid != null) {
            if (menucode.equals("airPollutantDeclare")) {
                System.out.println();
            }
            List<Map<String, Object>> menuConfig = MenuConfig.menuMap.get(menucode);
            if (menuConfig != null) {
                long num = 0l;
                for (Map<String, Object> map : menuConfig) {
                    map.put("fk_value", pollutionid);
                    List<Map<String, Object>> dataList = sysMenuService.getTableDataByParam(map);
                    num += dataList.size();
                }
                return num;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


}

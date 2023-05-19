package com.tjpu.auth.controller.system.role;

import com.tjpu.auth.model.codeTable.CommonSelectTableConfigVO;
import com.tjpu.auth.service.codeTable.CommonSelectTableConfigService;
import com.tjpu.auth.service.system.RolesService;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @author: zhangzc
 * @date: 2018/6/2 19:04
 * @Description: 角色控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@Api(value = "角色操作Api", tags = {"角色操作接口"})
@RequestMapping("roleController")
public class RolesController {
    private final RolesService rolesService;
    private final CommonSelectTableConfigService commonSelectTableConfigService;

    @Autowired
    public RolesController(RolesService rolesService, CommonSelectTableConfigService commonSelectTableConfigService) {
        this.rolesService = rolesService;
        this.commonSelectTableConfigService = commonSelectTableConfigService;
    }

    /**
     * @author: zhangzc
     * @date: 2018/6/21 13:50
     * @Description: 获取角色树形结构以及角色表的主键字段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "获取角色树形结构以及角色表的主键字段", notes = "获取角色树形结构以及角色表的主键字段")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @GetMapping(value = "getRoleTreeData")
    public Object getRoleTreeData() {
        try {
            List<Map<String, Object>> roleTreeData = rolesService.getRoleTreeData();
            final CommonSelectTableConfigVO commonSelectTableConfigVO = commonSelectTableConfigService.getTableConfigByName("Base_Roles");
            final String pkFieldName = commonSelectTableConfigVO.getKeyFieldName();
            Map<String, Object> result = new HashMap<>();
            result.put("roleTreeData", roleTreeData);
            result.put("pk", pkFieldName.toLowerCase());
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 12:14
     * @Description: 根据角色ID获取角色权限
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */

    /**
     * @author: lip
     * @date: 2018/9/27 0027 上午 10:31
     * @Description: 根据角色id集合获取角色拥有的功能权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @ApiOperation(value = "根据用户ID,获取用户系统菜单权限", notes = "根据用户ID,获取用户系统菜单权限")
    @ApiImplicitParam(name = "roles_id", value = "用户ID", defaultValue = "", required = true, dataType = "List<String>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")
    })
    @RequestMapping(value = "getRoleAuthByRoleIDs", method = RequestMethod.POST)
    public Object getRoleAuthByRoleIDs(@RequestJson(value = "roles_id") List<String> roleids) {
        try {
            if (roleids.size() > 0) {
                List<String> authData = rolesService.getRoleAuthByRoleIDs(roleids);
                return AuthUtil.parseJsonKeyToLower("success", authData);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

package com.tjpu.auth.controller.system.organization;

import com.tjpu.auth.service.system.OrganizationService;
import com.tjpu.pk.common.utils.AuthUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: xsm
 * @date: 2018/7/6 14:09
 * @Description: 部门控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("organizationController")
@Api(value = "部门信息处理类", tags = "部门信息处理类")
public class OrganizationController {
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * @return
     * @author: xsm
     * @date: 2018/7/6 14:09
     * @Description:获取部门树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @ApiOperation(value = "获取部门Tree数据", notes = "获取部门Tree数据")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "{\"flag\":\"success\",\"state\":\"success\",\"data\":\"具体json格式\"}"),
            @ApiResponse(code = 500, message = "{\"flag\":\"fail\",\"state\":\"exception\",\"errorMessage\":\"具体异常信息\"}")})
    @GetMapping(value = "getOrganizationTreeData")
    public Object getOrganizationTreeData() {
        try {
            List<Map<String, Object>> organizationTreeData = organizationService.getOrganizationTreeData();
            Map<String, Object> result = new HashMap<>();
            result.put("organizationtreedata", organizationTreeData);
            result.put("pk", "organization_id");
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
           throw e;
        }
    }

}

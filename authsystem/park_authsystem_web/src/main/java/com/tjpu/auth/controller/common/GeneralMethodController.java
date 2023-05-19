package com.tjpu.auth.controller.common;


import com.tjpu.auth.common.utils.RedisTemplateUtil;
import com.tjpu.auth.model.codeTable.CommonSelectTableConfigVO;
import com.tjpu.auth.service.codeTable.CommonSelectTableConfigService;
import com.tjpu.auth.service.micro.AuthSystemMicroService;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("generalMethod")
public class GeneralMethodController {

    private final AuthSystemMicroService authSystemMicroService;


    @Autowired
    public GeneralMethodController(AuthSystemMicroService authSystemMicroService,
                                   CommonSelectTableConfigService commonSelectTableConfigService) {
        this.authSystemMicroService = authSystemMicroService;

        this.commonSelectTableConfigService = commonSelectTableConfigService;
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:19
     * @Description: 初始化列表页面（列表数据、分页信息、查询控件、按钮权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getListByParam", method = RequestMethod.POST)
    public Object getListByParam(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (StringUtils.isNotBlank(userid)) {
                paramMap.put("userid", userid);
                String param = AuthUtil.paramDataFormat(paramMap);
                return authSystemMicroService.getListByParam(param);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:21
     * @Description: 列表表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTableTitle", method = RequestMethod.POST)
    public Object getTableTitle(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.getTableTitle(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private final CommonSelectTableConfigService commonSelectTableConfigService;

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:21
     * @Description: 导出excel
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportExcel", method = RequestMethod.POST)
    public void exportExcel(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String sysModel = paramMap.get("sysmodel").toString();
            CommonSelectTableConfigVO tableVO = commonSelectTableConfigService.getTableConfigVOBySysModel(sysModel);
            if (tableVO != null) {
                String tableComments = tableVO.getTableComments();
                String fileNameInfo = tableComments.replace("-", "") + new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
                String fileName = paramMap.get("excelfilename") != null ? paramMap.get("excelfilename").toString() : fileNameInfo;
                paramMap.put("excelfilename", fileName);
                String param = AuthUtil.paramDataFormat(paramMap);
                byte[] workbook = authSystemMicroService.getHSSFWorkbook(param);
                ExcelUtil.downLoadExcel(fileName, response, request, workbook);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:21
     * @Description: 获取列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getListData", method = RequestMethod.POST)
    public Object getListData(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.getListData(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:22
     * @Description: 获取添加页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAddPageInfo", method = RequestMethod.POST)
    public Object getAddPageInfo(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.getAddPageInfo(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:22
     * @Description: 添加方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "doAddMethod", method = RequestMethod.POST)
    public Object doAddMethod(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.doAddMethod(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:22
     * @Description: 获取修改页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "goUpdatePage", method = RequestMethod.POST)
    public Object goUpdateMethod(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.goUpdatePage(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:22
     * @Description: 修改方法
     * @updateUser: lip
     * @updateDate: 2019/07/12
     * @updateDescription: 添加功能：用户编辑时，设置企业报警关联设置信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "doEditMethod", method = RequestMethod.POST)
    public Object doEditMethod(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.doEditMethod(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:23
     * @Description: 获取详情页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getDetail", method = RequestMethod.POST)
    public Object getDetail(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.getDetail(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:23
     * @Description: 删除方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "deleteMethod")
    public Object deleteMethod(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.deleteMethod(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:24
     * @Description: 判断数据是否存在
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping("isTableDataHaveInfo")
    public Object isTableDataHaveInfo(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.isTableDataHaveInfo(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:24
     * @Description: 获取用户在菜单上的按钮权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping("getUserButtonAuthInMenu")
    public Object getUserButtonAuthInMenu(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (StringUtils.isNotBlank(userid)) {
                paramMap.put("userid", userid);

                String param = AuthUtil.paramDataFormat(paramMap);
                return authSystemMicroService.getUserButtonAuthInMenu(param);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/2/12 10:23
     * @Description: 获取查询条件数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getQueryCriteriaData", method = RequestMethod.POST)
    public Object getQueryCriteriaData(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String param = AuthUtil.paramDataFormat(paramMap);
            return authSystemMicroService.getQueryCriteriaData(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

package com.tjpu.sp.controller.emergency;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author: xsm
 * @date: 2019/6/14 0014 上午 10:21
 * @Description: 应急人员控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
*/
@RestController
@RequestMapping("emergencystaff")
public class EmergencySuppliesController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;

    private String sysmodel="emergencystaff";
    private String pk_id="pk_id";
    private String listfieldtype="list-emergencystaff";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: xsm
     * @date: 2019/06/14 0014 上午 10:20
     * @Description: 获取应急人员初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request, session]
     * @return:
     */
    @RequestMapping(value = "getEmergencyStaffsListPage", method = RequestMethod.POST)
    public Object getEmergencyStaffsListPage(HttpServletRequest request, HttpSession session) {
        try {
            //获取userid
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap= RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat( paramMap);
            Object resultList = publicSystemMicroService.getListByParam(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/14 0014 上午 10:32
     * @Description: 根据自定义参数获取应急人员列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @return:
     */
    @RequestMapping(value = "getEmergencyStaffsByParamMap", method = RequestMethod.POST)
    public Object getEmergencyStaffsByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat( paramMap);
            Object resultList = publicSystemMicroService.getListData(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/6/14 0014 上午10:36
     * @Description:  获取应急人员新增页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getEmergencyStaffAddPage",method = RequestMethod.POST)
    public Object getEmergencyStaffAddPage() {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat( paramMap);
            Object resultList = publicSystemMicroService.getAddPageInfo(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date:  2019/6/14 0014 上午10:41
     * @Description:  新增应急人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addEmergencyStaff",method = RequestMethod.POST)
    public Object addEmergencyStaff(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String param = AuthUtil.paramDataFormat( paramMap);
            Object resultList = publicSystemMicroService.doAddMethod(param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date:  2019/6/14 0014 上午10:42
     * @Description:  根据主键ID获取应急人员信息修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getEmergencyStaffUpdatePageByID",method = RequestMethod.POST)
    public Object getEmergencyStaffUpdatePageByID(@RequestJson(value="id",required = true)String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.goUpdatePage(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date:  2019/6/14 0014 上午10:44
     * @Description:  修改应急人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateEmergencyStaff",method = RequestMethod.POST)
    public Object updateEmergencyStaff(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat( paramMap);
            Object resultList = publicSystemMicroService.doEditMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date:  2019/6/14 0014 上午10:49
     * @Description: 根据应急人员信息主键ID删除单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteEmergencyStaffByID",method = RequestMethod.POST)
    public Object deleteEmergencyStaffByID(@RequestJson(value="id",required = true)String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.deleteMethod(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date:  2019/6/14 0014 上午10:47
     * @Description:  根据应急人员主键ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getEmergencyStaffDetailByID",method = RequestMethod.POST)
    public Object getEmergencyStaffDetailByID(@RequestJson(value="id",required = true)String id) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("sysmodel", sysmodel);
            paramMap.put(pk_id, id);
            paramMap.put("datasource", datasource);
            String Param = AuthUtil.paramDataFormat(paramMap);
            Object resultList = publicSystemMicroService.getDetail(Param);
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

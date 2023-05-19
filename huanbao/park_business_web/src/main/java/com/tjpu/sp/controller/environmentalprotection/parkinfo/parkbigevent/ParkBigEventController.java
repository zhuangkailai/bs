package com.tjpu.sp.controller.environmentalprotection.parkinfo.parkbigevent;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.parkinfo.parkbigevent.ParkBigEventService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("parkBigEvent")
public class ParkBigEventController {


    private static final String sysModel = "parkBigEvent";

    private final PublicSystemMicroService publicSystemMicroService;

    private final ParkBigEventService parkBigEventService;

    @Autowired
    public ParkBigEventController(PublicSystemMicroService publicSystemMicroService, ParkBigEventService parkBigEventService) {
        this.publicSystemMicroService = publicSystemMicroService;

        this.parkBigEventService = parkBigEventService;
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:43
     * @Description: 获取大事件信息按时间倒序
     * @param:
     * @return:
     */
    @RequestMapping(value = "getDescBigEventsInTime", method = RequestMethod.GET)
    public Object getDescBigEventsInTime() {
        try {
            List<Map<String, Object>> bigEvents = parkBigEventService.getDescBigEventsInTime();
            return AuthUtil.parseJsonKeyToLower("success", bigEvents);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 14:59
     * @Description: 获取大事件列表页面信息（包含分页、表头、查询控件、权限、列表信息）
     * @param:
     * @return:
     */
    @RequestMapping(value = "getBigEventsListPage", method = RequestMethod.POST)
    public Object getBigEventsListPage(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String userID = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userID);
            paramMap.put("sysmodel", sysModel);

            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.getListByParam(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:25
     * @Description: 动态条件分页查询大事件列表信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getBigEventsByParamMap", method = RequestMethod.POST)
    public Object getBigEventsByParamMap(
            @RequestJson(value = "paramsjson", required = false) Object paramsjson,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            JSONObject paramMap = new JSONObject();
            if (paramsjson != null) {
                paramMap = JSONObject.fromObject(paramsjson);
            }
            paramMap.put("sysmodel", sysModel);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);

            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.getListData(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:26
     * @Description: 获取大事件添加页面信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkIntroduceAddPage", method = RequestMethod.POST)
    public Object getParkIntroduceAddPage() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);

            String param = AuthUtil.paramDataFormat( paramMap);
            return publicSystemMicroService.getAddPageInfo(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:28
     * @Description:添加大事件信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "addBigEvent", method = RequestMethod.POST)
    public Object addBigEvent(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysModel);

            String Param = AuthUtil.paramDataFormat( paramMap);
            return publicSystemMicroService.doAddMethod(Param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:29
     * @Description: 根据大事件信息ID获取大事件修改页面信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getBigEventUpdatePageByID", method = RequestMethod.POST)
    public Object getBigEventUpdatePageByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            paramMap.put("pk_bigeventid", id);

            String param = AuthUtil.paramDataFormat( paramMap);
            return publicSystemMicroService.goUpdatePage(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:30
     * @Description: 修改大事件信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateBigEvent", method = RequestMethod.POST)
    public Object updateBigEvent(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysModel);

            String Param = AuthUtil.paramDataFormat( paramMap);
            return publicSystemMicroService.doEditMethod(Param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:31
     * @Description: 根据大事件信息ID获取大事件详情信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getBigEventDetailByID", method = RequestMethod.POST)
    public Object getBigEventDetailByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            paramMap.put("pk_bigeventid", id);

            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.getDetail(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:30
     * @Description: 根据大事件信息ID删除大事件信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "deleteBigEventByID", method = RequestMethod.POST)
    public Object deleteBigEventByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            paramMap.put("pk_bigeventid", id);

            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.deleteMethod(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

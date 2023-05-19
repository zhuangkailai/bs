package com.tjpu.sp.controller.environmentalprotection.parkinfo.parkintroduce;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.FileController;
import com.tjpu.sp.model.base.parkintroduce.ParkIntroduceVO;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.parkinfo.parkintroduce.ParkIntroduceService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author: zhangzc
 * @date: 2019/5/9 14:14
 * @Description: 园区介绍信息类
 * @param:
 * @return:
 */
@RestController
@RequestMapping("parkIntroduce")
public class ParkIntroduceController {


    private static final String sysModel = "parkIntroduce";

    private final ParkIntroduceService parkIntroduceService;
    private final PublicSystemMicroService publicSystemMicroService;


    private final FileController fileController;

    @Autowired
    public ParkIntroduceController(PublicSystemMicroService publicSystemMicroService, ParkIntroduceService parkIntroduceService, FileController fileController) {
        this.publicSystemMicroService = publicSystemMicroService;
        this.parkIntroduceService = parkIntroduceService;

        this.fileController = fileController;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 14:17
     * @Description: 获取最新一条园区介绍信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLastParkIntroduceInfo", method = RequestMethod.GET)
    public Object getLastParkIntroduceInfo() throws Exception {
        try {
            Map<String, Object> map = parkIntroduceService.getLastParkIntroduceInfo();
            if (map != null&& map.get("FilePath") != null) {
                String FilePath = map.get("FilePath").toString();
                List<String> fileid = new ArrayList<>();
                fileid.add(FilePath);
                Object data = fileController.getFilesInfosByParams(fileid);
                data = AuthUtil.decryptData(data);
                JSONObject jsonObject3 = JSONObject.fromObject(data);
                JSONArray data2 = JSONArray.fromObject(jsonObject3.get("data"));    //imgsrc
                if (data2.size() > 0) {
                    JSONObject o = JSONObject.fromObject(data2.get(0));
                    map.put("imgsrc", o.get("imgsrc"));
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/9 14:59
     * @Description: 获取园区介绍列表页面信息（包含分页、表头、查询控件、权限、列表信息）
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkIntroduceListPage", method = RequestMethod.POST)
    public Object getParkIntroduceListPage(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            String userID = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
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
     * @Description: 动态条件分页查询园区介绍列表信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkIntroducesByParamMap", method = RequestMethod.POST)
    public Object getParkIntroducesByParamMap(
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
     * @Description: 获取园区介绍添加页面信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkIntroduceAddPage", method = RequestMethod.POST)
    public Object getParkIntroduceAddPage() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);

            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.getAddPageInfo(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:28
     * @Description:添加园区介绍信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "addParkIntroduce", method = RequestMethod.POST)
    public Object addParkIntroduce(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysModel);

            String Param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.doAddMethod(Param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:29
     * @Description: 根据园区介绍信息ID获取园区介绍修改页面信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkIntroduceUpdatePageByID", method = RequestMethod.POST)
    public Object getParkIntroduceUpdatePageByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            paramMap.put("id", id);

            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.goUpdatePage(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:30
     * @Description: 修改园区介绍信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateParkIntroduce", method = RequestMethod.POST)
    public Object updateParkIntroduce(HttpServletRequest request) {
        try {
            Map<String, Object> map = parkIntroduceService.getLastParkIntroduceInfo();
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysModel);
            String Param = AuthUtil.paramDataFormat(paramMap);
            if(map!=null && map.size()>0){
                return publicSystemMicroService.doEditMethod(Param);
            }else{
                Map<String, Object> data = paramMap.get("formdata") == null ? new HashMap<>() : (Map<String, Object>) paramMap.get("formdata");
                ParkIntroduceVO parkIntroduceVO = new ParkIntroduceVO();
                parkIntroduceVO.setPkParkintroduceid(UUID.randomUUID().toString());
                parkIntroduceVO.setMainindustry(data.get("mainIndustry")==null?"":data.get("mainIndustry").toString());
                parkIntroduceVO.setParkintroduce(data.get("parkintroduce")==null?"":data.get("parkintroduce").toString());
                parkIntroduceVO.setFkParkmapfileid(data.get("fk_parkmapfileid")==null?"":data.get("fk_parkmapfileid").toString());
                return AuthUtil.parseJsonKeyToLower("success",parkIntroduceService.insert(parkIntroduceVO));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:31
     * @Description: 根据园区介绍信息ID获取园区介绍信息详情
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTaskInfoDetailByID", method = RequestMethod.POST)
    public Object getTaskInfoDetailByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            paramMap.put("id", id);

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
     * @Description: 根据园区介绍信息ID删除园区介绍信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "deleteParkIntroduceByID", method = RequestMethod.POST)
    public Object deleteParkIntroduceByID(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysModel);
            paramMap.put("id", id);

            String param = AuthUtil.paramDataFormat(paramMap);
            return publicSystemMicroService.deleteMethod(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

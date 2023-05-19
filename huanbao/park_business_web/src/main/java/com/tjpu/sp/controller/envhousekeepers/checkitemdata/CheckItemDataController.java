package com.tjpu.sp.controller.envhousekeepers.checkitemdata;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.service.common.user.UserService;
import com.tjpu.sp.service.envhousekeepers.checkentinfo.CheckEntInfoService;
import com.tjpu.sp.service.envhousekeepers.checkitemdata.CheckItemDataService;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 *
 * @author: xsm
 * @date: 2021/06/29 0029 下午 13:12
 * @Description: 检查企业信息控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
*/
@RestController
@RequestMapping("checkItemData")
public class CheckItemDataController {

    @Autowired
    private CheckItemDataService checkItemDataService;
    @Autowired
    private CheckProblemExpoundService checkProblemExpoundService;
    @Autowired
    private UserService userService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private CheckEntInfoService checkEntInfoService;

    /**
     * @author: xsm
     * @date: 2021/06/29 0029 下午 15:37
     * @Description: 根据污染源ID、检查日期、检查类型获取检查项目数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllCheckItemDataByParam", method = RequestMethod.POST)
    public Object getAllCheckItemDataByParam(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                             @RequestJson(value = "checktime", required = false) String checktime,
                                             @RequestJson(value = "dataflag", required = false) String dataflag,
                                             @RequestJson(value = "checktypecode", required = false) String checktypecode
    ) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            Map<String, Object> result = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("checktime",checktime);
            param.put("checktypecode",checktypecode);
            param.put("dataflag",dataflag);
            Map<String, Object> checkentinfo = checkItemDataService.getOneCheckEntInfoByParam(param);
            List<Map<String, Object>> datalist = checkItemDataService.getAllCheckItemDataByParam(param);
            if (datalist!=null&&datalist.size()>0&&"pollution".equals(dataflag)){
                //若是企业端 则重新赋予排序号
                int i =1;
                for (Map<String, Object> map:datalist){
                    map.put("OrderIndex",i);
                    i++;
                }
            }
            result.put("checkentdata",checkentinfo);
            result.put("checkitemdata",datalist);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: xsm
     * @date: 2021/07/07 0007 上午 10:11
     * @Description: 根据污染源ID、检查日期更新该企业该日期所有检查报告的问题状态(提交)
     * @updateUser:xsm
     * @updateDate:2021/08/31 下午3:33
     * @updateDescription:提交时更新反馈信息表修改状态为可修改,且修改已读状态为未读
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateAllCheckProblemExpoundStatusByParam", method = RequestMethod.POST)
    public Object updateAllCheckProblemExpoundStatusByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                            @RequestJson(value = "checktypecode") String checktypecode,
                                                            @RequestJson(value = "checktime") String checktime,
                                                            @RequestJson(value = "entcheckinfo", required = false) Object entcheckinfo
    ) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("status",1);
            param.put("pollutionid",pollutionid);
            param.put("checktypecode",checktypecode);
            param.put("checktime",checktime);
            int i = checkItemDataService.updateAllCheckProblemExpoundStatusByParam(param);
            checkItemDataService.updateEntCheckFeedbackData(param);
            //提交检查表时 向管委会端推送消息(企业端)
            JSONObject jsonObject = JSONObject.fromObject(entcheckinfo);
            if (i > 0 && entcheckinfo!=null) {
                //推送到管委会端首页
                String messageType = CommonTypeEnum.HomePageMessageTypeEnum.EntCheckSubmitMessage.getCode();
                //根据菜单Code 获取拥有该菜单code权限的用户
              /*  List<String> userids = new ArrayList<>();
                if (jsonObject.get("menucode")!=null&&!"".equals(jsonObject.get("menucode").toString())) {
                    userids = userService.getAllUserIdsByMenuCode(jsonObject.get("menucode").toString());
                }*/
                Map<String, Object> onemap  = checkEntInfoService.getOneCheckEntDataByParam(param);
                JSONObject jsonobj = new JSONObject();
                //jsonobj.put("userids", userids);
                if (onemap!=null){
                    jsonobj.put("isupdate", onemap.get("isupdate"));
                }else{
                    jsonobj.put("isupdate", 0);
                }
                jsonobj.put("pkid", jsonObject.get("pkid"));
                jsonobj.put("pollutionname", jsonObject.get("pollutionname"));
                jsonobj.put("fkpollutionid", jsonObject.get("pollutionid"));
                jsonobj.put("checktime", checktime);
                jsonobj.put("updatetime", DataFormatUtil.getDateYMDHMS(new Date()));

                String str = "";
                if (jsonObject.get("pollutionname")!= null) {//标题不为空
                    str = jsonObject.get("pollutionname") +"提交了一份问题检查表！";
                } else {
                    str = "有一份企业提交的问题检查表！";
                }
                jsonobj.put("messagestr", str);
                jsonobj.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckSubmitMessage.getCode());
                jsonobj.put("isread", "0");
                rabbitmqController.sendMessageToManagementSide(jsonobj, messageType);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/06/29 0029 下午 15:37
     * @Description: 根据污染源ID、检查日期、检查类型获取检查项目数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllCheckItemDataByParamForApp", method = RequestMethod.POST)
    public Object getAllCheckItemDataByParamForApp(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                   @RequestJson(value = "checktime", required = false) String checktime,
                                                   @RequestJson(value = "dataflag", required = false) String dataflag,
                                                   @RequestJson(value = "checktypecode", required = false) String checktypecode
    ) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            Map<String, Object> result = new HashMap<>();
            param.put("pollutionid",pollutionid);
            param.put("checktime",checktime);
            param.put("checktypecode",checktypecode);
            param.put("dataflag",dataflag);
            Map<String, Object> checkentinfo = checkItemDataService.getOneCheckEntInfoByParam(param);
            List<Map<String, Object>> datalist = checkItemDataService.getAllCheckItemDataByParam(param);
            List<Map<String, Object>> onelist = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            if (datalist!=null&&datalist.size()>0){
                for (Map<String, Object> map:datalist){
                    if (map.get("CheckCategory")!=null&&!ids.contains(map.get("CheckCategory").toString())){
                        Map<String,Object> twomap = new HashMap<>();
                        twomap.put("checkcategory",map.get("CheckCategory"));
                        twomap.put("checkcategoryname",map.get("CheckCategoryName"));
                        List<Map<String, Object>> twolist = new ArrayList<>();
                        for (Map<String, Object> map2:datalist){
                            if (map2.get("CheckCategory")!=null&&map.get("CheckCategory").toString().equals(map2.get("CheckCategory").toString())){
                                twolist.add(map2);
                            }
                        }
                        twomap.put("childlist",twolist);
                        onelist.add(twomap);
                        ids.add(map.get("CheckCategory").toString());
                    }
                }
            }
            result.put("checkentdata",checkentinfo);
            result.put("checkitemdata",onelist);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @author: mmt
     * @date: 2021/08/04 0004 下午 14:12
     * @Description: 自定义参数获取多个问题记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getManyCheckProblemExpoundDataByParamMap", method = RequestMethod.POST)
    public Object getManyCheckProblemExpoundDataByParamMap(@RequestJson(value = "pollutionid") String pollutionid,
                                                           @RequestJson(value = "problemids") List<String> problemids,
                                                           @RequestJson(value = "checktypecode") String checktypecode) throws Exception {
        try {
            Map<String, Object> paramtMap = new HashMap<>();
            paramtMap.put("problemids",problemids);
            paramtMap.put("pollutionid",pollutionid);
            paramtMap.put("checktypecode",checktypecode);
            List<Map<String, Object>> result = checkItemDataService.getManyCheckProblemExpoundDataByParamMap(paramtMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

package com.tjpu.sp.controller.environmentalprotection.wechatpushset;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.wechatpushset.WechatPushSetVO;
import com.tjpu.sp.service.environmentalprotection.wechatpushset.WechatPushSetService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("wechatPushSet")
public class WechatPushSetController {
    @Autowired
    private WechatPushSetService wechatPushSetService;


    /**
     * @author: xsm
     * @date: 2020/03/20 0020 上午 11:36
     * @Description: 根据自定义参数获取微信群信息推送配置列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getWechatPushSetInfosByParamMap", method = RequestMethod.POST)
    public Object getWechatPushSetInfosByParamMap(@RequestJson(value = "paramsjson", required = true) Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist= wechatPushSetService.getWechatPushSetInfosByParamMap(jsonObject);
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                List<Map<String, Object>> dataList = getPageData(datalist, Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
                resultMap.put("total", datalist.size());
                resultMap.put("datalist", dataList);
            } else {
                resultMap.put("datalist", datalist);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 202003/20 0020 下午 1:21
     * @Description: 新增微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addWechatPushSetInfo", method = RequestMethod.POST)
    public Object addWechatPushSetInfo(@RequestJson(value = "addformdata") Object addformdata ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<String> alarmtypes = jsonObject.getJSONArray("alarmtypes");
            String wechatname = jsonObject.getString("wechatname");
            String remark = jsonObject.getString("remark");

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            List<WechatPushSetVO>  listobjs = new ArrayList<>();
            if (alarmtypes!=null&&alarmtypes.size()>0){
                for(String alarmtype:alarmtypes){
                    WechatPushSetVO obj =new WechatPushSetVO();
                    obj.setPkId( UUID.randomUUID().toString());
                    obj.setAlarmtype(alarmtype);
                    obj.setWechatname(wechatname);
                    obj.setRemark(remark);
                    obj.setUpdateuser(username);
                    obj.setUpdatetime(new Date());
                    listobjs.add(obj);
                }
            }
            wechatPushSetService.addWechatPushSetInfo(listobjs);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/20 0020 下午 2:55
     * @Description: 修改微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateWechatPushSetInfo", method = RequestMethod.POST)
    public Object updateWechatPushSetInfo(@RequestJson(value = "updateformdata") Object updateformdata
                                           ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            List<String> alarmtypes = jsonObject.getJSONArray("alarmtypes");
            String pkid = jsonObject.getString("pkid");
            String wechatname = jsonObject.getString("wechatname");
            String remark = jsonObject.getString("remark");

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            List<WechatPushSetVO>  listobjs = new ArrayList<>();
            if (alarmtypes!=null&&alarmtypes.size()>0){
                for(String alarmtype:alarmtypes){
                    WechatPushSetVO obj =new WechatPushSetVO();
                    obj.setPkId( UUID.randomUUID().toString());
                    obj.setAlarmtype(alarmtype);
                    obj.setWechatname(wechatname);
                    obj.setRemark(remark);
                    obj.setUpdateuser(username);
                    obj.setUpdatetime(new Date());
                    listobjs.add(obj);
                }
            }
            wechatPushSetService.updateWechatPushSetInfo(listobjs,pkid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/20 0020 下午 3:20
     * @Description: 删除微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "deleteWechatPushSetInfo", method = RequestMethod.POST)
    public Object deleteWechatPushSetInfo(@RequestJson(value = "wechatname") String wechatname
                                         ) throws Exception {
        try {
            wechatPushSetService.deleteWechatPushSetInfoByWechatName(wechatname);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/23 0023 上午 9:50
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2020/03/23 0023 上午 10:18
     * @Description: 获取微信群信息推送配置报警类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getWechatPushSetAlarmTypes", method = RequestMethod.POST)
    public Object getWechatPushSetAlarmTypes(
    ) throws Exception {
        try {
            List<String> list = CommonTypeEnum.getAllWechatPushSetAlarmTypeList();
            List<Map<String,Object>> result =new ArrayList<>();
            for (String str:list){
                Map<String,Object> map = new HashMap<>();
                map.put("id",str);
                map.put("labelname",CommonTypeEnum.WechatPushSetAlarmTypeEnum.getNameByCode(str)+"");
                result.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/03/23 0023 上午 10:45
     * @Description: 根据微信群名获取微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getWechatPushSetInfoByWechatName", method = RequestMethod.POST)
    public Object getWechatPushSetInfoByWechatName(@RequestJson(value = "wechatname") String wechatname
    ) throws Exception {
        try {
            Map<String,Object> result =wechatPushSetService.getWechatPushSetInfoByWechatName(wechatname);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

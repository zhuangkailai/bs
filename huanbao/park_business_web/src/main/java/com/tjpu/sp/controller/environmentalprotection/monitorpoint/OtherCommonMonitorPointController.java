package com.tjpu.sp.controller.environmentalprotection.monitorpoint;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @Description: 其他监测点通用接口
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/8/1 8:59
 */

@RestController
@RequestMapping("otherCommonMonitorPoint")
public class OtherCommonMonitorPointController {
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private RabbitmqController rabbitmqController;


    /**
     * @Description: 自定义查询条件获取列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/1 9:12
     */
    @RequestMapping(value = "getDataListByParam", method = RequestMethod.POST)
    public Object getDataListByParam(@RequestJson(value = "paramjson") Object paramjson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramjson);
            Map<String, Object> resultMap = otherMonitorPointService.getDataListMapByParam(jsonObject);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 导出列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/1 9:12
     */
    @RequestMapping(value = "exportDataListByParam", method = RequestMethod.POST)
    public void exportDataListByParam(
            @RequestJson(value = "paramjson") Object paramjson,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramjson);
            Map<String, Object> resultMap = otherMonitorPointService.getDataListMapByParam(jsonObject);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) resultMap.get("datalist");
            int orderIndex = 0;
            for (Map<String, Object> dataMap : dataList) {
                orderIndex++;
                dataMap.put("ordernum", orderIndex);
            }
            List<Integer> types = jsonObject.getJSONArray("monitorpointtypes");
            List<String> headers;
            List<String> headersField;
            if (types.contains(CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode())) {
                headers = Arrays.asList("序号", "监测点名称", "数采仪MN号", "监测点类别", "监测污染物", "中心经度", "中心纬度", "排序");
                headersField = Arrays.asList("ordernum", "monitorpointname", "dgimn", "monitorpointcategoryname", "pollutants", "longitude", "latitude", "orderindex");
            } else {
                headers = Arrays.asList("序号", "监测点名称", "数采仪MN号", "监测点类别", "中心经度", "中心纬度", "排序");
                headersField = Arrays.asList("ordernum", "monitorpointname", "dgimn", "pollutants", "longitude", "latitude", "orderindex");
            }
            //设置文件名称
            String fileName = "监测点信息_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, dataList, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取企业检查信息列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 10:51
     */
    @RequestMapping(value = "addOrUpdateData", method = RequestMethod.POST)
    public Object addOrUpdateData(
            @RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            OtherMonitorPointVO otherMonitorPointVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new OtherMonitorPointVO());
            Date nowDay = new Date();
            otherMonitorPointVO.setUpdatetime(nowDay);
            otherMonitorPointVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(otherMonitorPointVO.getPkMonitorpointid())) {//更新操作
                otherMonitorPointService.updateInfo(otherMonitorPointVO);
            } else {//添加操作
                otherMonitorPointVO.setPkMonitorpointid(UUID.randomUUID().toString());
                otherMonitorPointService.insertInfo(otherMonitorPointVO);
            }
            sendToMq(otherMonitorPointVO);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 信息删除
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 12:03
     */
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public Object deleteById(
            @RequestJson(value = "id") String id) throws Exception {
        try {
            otherMonitorPointService.deleteById(id);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 编辑或详情数据回显
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 12:03
     */
    @RequestMapping(value = "getEditOrViewDataById", method = RequestMethod.POST)
    public Object getEditOrViewDataById(
            @RequestJson(value = "id") String id){
        try {
            Map<String, Object> resultMap = otherMonitorPointService.getEditOrViewDataById(id);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void sendToMq(OtherMonitorPointVO otherMonitorPointVO) {
        //发送消息到队列
        Map<String, Object> mqMap = new HashMap<>();
        mqMap.put("monitorpointtype", otherMonitorPointVO.getFkMonitorpointtypecode());
        mqMap.put("dgimn", otherMonitorPointVO.getDgimn());
        mqMap.put("monitorpointid", otherMonitorPointVO.getPkMonitorpointid());
        mqMap.put("fkpollutionid", "");
        rabbitmqController.sendPointUpdateDirectQueue(JSONObject.fromObject(mqMap));
    }
}

package com.tjpu.sp.controller.environmentalprotection.pointofflinerecord;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.pointofflinerecord.PointOffLineRecordVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.environmentalprotection.pointofflinerecord.PointOffLineRecordService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: xsm
 * @description: 点位离线记录
 * @create: 2021-05-13 10:04
 * @version: V1.0
 */
@RestController
@RequestMapping("pointOffLineRecord")
public class PointOffLineRecordController {
    @Autowired
    private PointOffLineRecordService pointOffLineRecordService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private PollutionService pollutionService;

    /**
     * @Author: xsm
     * @Date: 2021/05/13 0013 10:16
     * @Description: 自定义查询条件查询点位离线记录列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getPointOffLineRecordsByParamMap", method = RequestMethod.POST)
    public Object getPointOffLineRecordsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = pointOffLineRecordService.getPointOffLineRecordsByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/13 0013 上午 11:50
     * @Description: 修改离线点位记录为已读状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/updatePointOffLineRecordReadStatus", method = RequestMethod.POST)
    public Object updatePointOffLineRecordReadStatus(@RequestJson(value = "id") String id ) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            PointOffLineRecordVO obj = pointOffLineRecordService.getPointOffLineRecordInfoById(id);
            if (obj!=null){
                obj.setIsread((short) 1);
                obj.setReaduser(username);
                obj.setReadtime(new Date());
                obj.setUpdatetime(new Date());
            }
            pointOffLineRecordService.update(obj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/13 0013 上午 11:50
     * @Description: 获取离线点位记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getPointOffLineRecordDetailById", method = RequestMethod.POST)
    public Object getPointOffLineRecordDetailById(@RequestJson(value = "id") String id ) throws Exception {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("id",id);
            Map<String,Object> objmap = pointOffLineRecordService.getPointOffLineRecordDetailById(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/05/13 0013 下午 12:01
     * @Description: 保存点位离线信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "addPointOffLineRecordInfo", method = RequestMethod.POST)
    public void addPointOffLineRecordInfo(@RequestJson(value = "paramsjson") Object paramsjson) {
        try {
            Map<String, Object> paramMap = JSONObject.fromObject(paramsjson);
            //获取离线消息内的数据
            String MonitorPointTypeCode = paramMap.get("MonitorPointTypeCode") != null ? paramMap.get("MonitorPointTypeCode").toString() : "";
            String MN = paramMap.get("MN") != null ? paramMap.get("MN").toString() : "";
            String DateTime = paramMap.get("DateTime") != null ? paramMap.get("DateTime").toString() : "";
            PointOffLineRecordVO obj = new PointOffLineRecordVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setDgimn(MN);
            obj.setFkMonitorpointtypecode(MonitorPointTypeCode);
            obj.setIsread((short)0);
            obj.setOfflinetime(DataFormatUtil.getDateYMDHMS(DateTime));
            obj.setUpdatetime(new Date());
            int i = pointOffLineRecordService.insert(obj);
            if (i>0){//添加成功 推送离线消息到首页
                String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.PointOffLineMessage.getCode();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("pk_id", obj.getPkId());
                jsonobj.put("monitorpointname", paramMap.get("OutPutName"));
                jsonobj.put("pollutionname", paramMap.get("PollutionName"));
                jsonobj.put("PollutionID", paramMap.get("PollutionID"));
                jsonobj.put("MonitorPointId", paramMap.get("MonitorPointId"));
                jsonobj.put("updatetime", DateTime);
                jsonobj.put("MN", MN);
                String pollutionname = paramMap.get("PollutionName") != null ? paramMap.get("PollutionName").toString() : "";
                String monitorpointname = paramMap.get("OutPutName") != null ? paramMap.get("OutPutName").toString() : "";
                if (!"".equals(pollutionname)){
                    jsonobj.put("messagestr", pollutionname+"_"+monitorpointname + "离线");
                }else{
                    jsonobj.put("messagestr", monitorpointname + "离线");
                }
                jsonobj.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode());
                jsonobj.put("isread", "0");
                //推送到首页
                rabbitmqController.sendEmissionControlInfo(jsonobj, messageType);
                //推送到管委会端首页
                rabbitmqController.sendMessageToManagementSide(jsonobj, CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode());
                //推到企业端首页
                //根据企业ID获取企业关联的企业用户ID
                List<String> userids = pollutionService.getUserInfoByPollution(paramMap.get("PollutionID").toString());
                if (userids.size()>0) {
                    jsonobj.put("userids", userids);
                    jsonobj.put("messagestr", monitorpointname + "离线");
                    rabbitmqController.sendEntCheckFeedbackInfo(jsonobj, CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode());
                }
            }
            //return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}

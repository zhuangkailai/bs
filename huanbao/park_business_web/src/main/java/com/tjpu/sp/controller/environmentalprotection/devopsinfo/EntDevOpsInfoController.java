package com.tjpu.sp.controller.environmentalprotection.devopsinfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevicePersonnelRecordVO;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.EntDevOpsInfoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("entDevOpsInfo")
public class EntDevOpsInfoController {
    @Autowired
    private EntDevOpsInfoService entDevOpsInfoService;

    /**
     * @author: xsm
     * @date: 2019/12/03 0003 下午 2:08
     * @Description: 根据自定义参数获取企业运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEntDevOpsInfosByParamMap",method = RequestMethod.POST)
    public Object getEntDevOpsInfosByParamMap(@RequestJson(value="paramsjson",required = true)Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entDevOpsInfoService.getEntDevOpsInfosByParamMap(jsonObject);
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
     * @date: 2019/12/03 0003 下午 3:50
     * @Description: 批量新增企业运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "addEntDevOpsInfos",method = RequestMethod.POST)
    public Object addEntDevOpsInfos(@RequestJson(value="addformdata",required = true)Object addformdata ) throws Exception {
        try {
           Map<String,Object> addmap =(Map<String,Object>) JSONObject.fromObject(addformdata);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",String.class);
            List<EntDevOpsInfoVO> objlist = new ArrayList<>();
            List<Integer> pollutiontypes = new ArrayList<>(CommonTypeEnum.getPollutionMonitorPointTypeList());
            if (addmap.get("ids")!=null&&addmap.get("monitorpointtype")!=null) {
                List<String> ids = (List<String>) addmap.get("ids");
                String monitorpointtype = addmap.get("monitorpointtype").toString();
                boolean ispollutionflag = false;
                for (Integer type : pollutiontypes) {//是否企业关联监测类型
                    if (monitorpointtype.equals(String.valueOf(type))) {
                        ispollutionflag = true;
                        break;
                    }
                }
                if (ids.size()>0){
                    for (String id:ids){
                        EntDevOpsInfoVO obj = new EntDevOpsInfoVO();
                        obj.setPkId(UUID.randomUUID().toString());
                        if (ispollutionflag == true){
                            obj.setFkPollutionid(id);
                        }else{
                            obj.setFkMonitorpointid(id);
                        }
                        obj.setFkMonitorpointtypecode(monitorpointtype);
                        obj.setDevopsunit(addmap.get("devopsunit")!=null?addmap.get("devopsunit").toString():"");
                        obj.setDevopspeople(addmap.get("devopspeople")!=null?addmap.get("devopspeople").toString():"");
                        obj.setUpdatetime(new Date());
                        obj.setUpdateuser(username);
                        objlist.add(obj);
                    }
                }
                entDevOpsInfoService.addEntDevOpsInfos(objlist,monitorpointtype,ids);
            }
            return AuthUtil.parseJsonKeyToLower("success",null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 1:26
     * @Description:  通过自定义参数获取运维企业和排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getEntDevPollutionAndOutputInfoByParamMap",method = RequestMethod.POST)
    public Object getEntDevPollutionAndOutputInfoByParamMap(@RequestJson(value="monitorpointtypes",required = false)Object monitorpointtypes,
                                                            @RequestJson(value="pollutionid",required = false)String pollutionid) throws Exception {
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("monitorpointtypes",monitorpointtypes);
            paramMap.put("pollutionid",pollutionid);
            List<Map<String, Object>> pollutionAndOutputInfoByParamMap = entDevOpsInfoService.getPollutionAndOutputInfoByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success",pollutionAndOutputInfoByParamMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/03 0003 下午 2:08
     * @Description: 根据自定义参数获取运维单位下运维的点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEntDevOpsInfoListDataByParamMap",method = RequestMethod.POST)
    public Object getEntDevOpsInfoListDataByParamMap(@RequestJson(value="paramsjson",required = true)Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = entDevOpsInfoService.getEntDevOpsInfoListDataByParamMap(jsonObject);
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
     * @date: 2022/04/02 0002 08:56
     * @Description: 新增运维点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "addDevOpsMonitorPointInfo", method = RequestMethod.POST)
    public Object addDevOpsMonitorPointInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            List<String> personnelids = jsonObject.getJSONArray("personnelids");
            EntDevOpsInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntDevOpsInfoVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            entity.setPkId(pkid);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            List<DevicePersonnelRecordVO> list = new ArrayList<>();
            for (String id:personnelids){
                DevicePersonnelRecordVO oneobj = new DevicePersonnelRecordVO();
                oneobj.setPkId(UUID.randomUUID().toString());
                oneobj.setUpdatetime(new Date());
                oneobj.setUpdateuser(username);
                oneobj.setFkEntdevopsid(pkid);
                oneobj.setFkPersonnelid(id);
                list.add(oneobj);
            }
            //添加运维人员信息
            entDevOpsInfoService.addDevOpsMonitorPointInfo(entity,list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 08:56
     * @Description: 修改运维监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateDevOpsMonitorPointInfo", method = RequestMethod.POST)
    public Object updateDevOpsMonitorPointInfo(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            List<String> personnelids = jsonObject.getJSONArray("personnelids");
            EntDevOpsInfoVO entity = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), EntDevOpsInfoVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setUpdatetime(new Date());
            entity.setUpdateuser(username);
            List<DevicePersonnelRecordVO> list = new ArrayList<>();
            for (String id : personnelids) {
                DevicePersonnelRecordVO oneobj = new DevicePersonnelRecordVO();
                oneobj.setPkId(UUID.randomUUID().toString());
                oneobj.setUpdatetime(new Date());
                oneobj.setUpdateuser(username);
                oneobj.setFkEntdevopsid(entity.getPkId());
                oneobj.setFkPersonnelid(id);
                list.add(oneobj);
            }
            //添加运维人员信息
            entDevOpsInfoService.updateDevOpsMonitorPointInfo(entity, list);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维人员回显信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getDevOpsPersonnelInfoByParam", method = RequestMethod.POST)
    public Object getDevOpsPersonnelInfoByParam(@RequestJson(value = "unitid") String unitid,
                                                @RequestJson(value = "personnelid") String personnelid) throws Exception {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("unitid",unitid);
            paramMap.put("personnelid",personnelid);
            //Map<String,Object> objmap = devOpsPersonnelService.getDevOpsPersonnelInfoByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 获取运维人员详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getDevOpsMonitorPointDetailByParam", method = RequestMethod.POST)
    public Object getDevOpsMonitorPointDetailByParam(@RequestJson(value = "devopsunit") String devopsunit,
                                                 @RequestJson(value = "entdevopsid") String entdevopsid) throws Exception {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("devopsunitid",devopsunit);
            paramMap.put("entdevopsid",entdevopsid);
            Map<String,Object> objmap = entDevOpsInfoService.getDevOpsMonitorPointDetailByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", objmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 删除运维人员信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "deleteDevOpsMonitorPointByID", method = RequestMethod.POST)
    public Object deleteDevOpsMonitorPointByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            entDevOpsInfoService.deleteDevOpsMonitorPointByID(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/01 0001 08:56
     * @Description: 验证运维单位名称是否数据重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/IsHaveDevOpsMonitorPointInfo", method = RequestMethod.POST)
    public Object IsHaveDevOpsMonitorPointInfo(@RequestJson(value = "devopsunit") String devopsunit,
                                               @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
                                               @RequestJson(value = "monitorpointid") String monitorpointid) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("devopsunit",devopsunit);
            param.put("monitorpointtypecode",monitorpointtypecode);
            param.put("monitorpointid",monitorpointid);
            List<Map<String, Object>> datalist = entDevOpsInfoService.getEntDevOpsInfoListDataByParamMap(param);
            if(datalist.size()>0){
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            }else{
                return AuthUtil.parseJsonKeyToLower("success", "no");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/02 0002 下午 15:25
     * @Description: 根据监测类型和监测点ID获取该监测点的历史运维记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getEntDevOpsHistoryDataByParamMap",method = RequestMethod.POST)
    public Object getEntDevOpsHistoryDataByParamMap(@RequestJson(value="monitorpointtypecode",required = true)Integer monitorpointtypecode,
                                                    @RequestJson(value="monitorpointid",required = true)String monitorpointid,
                                                    @RequestJson(value="pagenum",required = false)Integer pagenum,
                                                    @RequestJson(value="pagesize",required = false)Integer pagesize) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypecode",monitorpointtypecode);
            paramMap.put("monitorpointid",monitorpointid);
            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> datalist = entDevOpsInfoService.getEntDevOpsHistoryDataByParamMap(paramMap);
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
     * @date: 2022/04/11 0011 18:03
     * @Description: 修改运维计划信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "updateDevOpsPlanInfo", method = RequestMethod.POST)
    public Object updateDevOpsPlanInfo(@RequestJson(value = "updateformdata") Object updateformdata
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            JSONArray jsonArray = jsonObject.getJSONArray("pointinfos");
            JSONObject obj;
            List<String> personnelids;
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid;
            List<String> oldpkids = new ArrayList<>();
            List<EntDevOpsInfoVO> onelist = new ArrayList<>();
            List<DevicePersonnelRecordVO> twolist = new ArrayList<>();
            for (int i = 0;i<jsonArray.size();i++){
                obj = (JSONObject) jsonArray.get(i);
                if (obj.get("pkid")!=null&&!"".equals(obj.get("pkid").toString())){
                    oldpkids.add(obj.get("pkid").toString());
                }
                personnelids = obj.getJSONArray("personnelids");
                EntDevOpsInfoVO entity = JSONObjectUtil.parseStringToJavaObject(obj.toString(), EntDevOpsInfoVO.class);
                pkid = UUID.randomUUID().toString();
                entity.setPkId(pkid);
                entity.setUpdatetime(new Date());
                entity.setUpdateuser(username);
                onelist.add(entity);
                for (String id : personnelids) {
                    DevicePersonnelRecordVO oneobj = new DevicePersonnelRecordVO();
                    oneobj.setPkId(UUID.randomUUID().toString());
                    oneobj.setUpdatetime(new Date());
                    oneobj.setUpdateuser(username);
                    oneobj.setFkEntdevopsid(entity.getPkId());
                    oneobj.setFkPersonnelid(id);
                    twolist.add(oneobj);
                }
            }
            //添加运维人员信息
            entDevOpsInfoService.updateDevOpsPlanInfo(onelist, twolist,oldpkids);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

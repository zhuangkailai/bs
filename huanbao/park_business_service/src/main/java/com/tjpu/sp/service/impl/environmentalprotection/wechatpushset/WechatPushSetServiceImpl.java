package com.tjpu.sp.service.impl.environmentalprotection.wechatpushset;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.wechatpushset.WechatPushSetMapper;
import com.tjpu.sp.model.environmentalprotection.wechatpushset.WechatPushSetVO;
import com.tjpu.sp.service.environmentalprotection.wechatpushset.WechatPushSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WechatPushSetServiceImpl implements WechatPushSetService {

    @Autowired
    private WechatPushSetMapper wechatPushSetMapper;

    /**
     * @author: xsm
     * @date: 202003/20 0020 下午 2:36
     * @Description: 新增微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void addWechatPushSetInfo(List<WechatPushSetVO> listobjs) {
        //批量新增微信群信息推送配置信息
        if (listobjs!=null&&listobjs.size()>0) {
            wechatPushSetMapper.batchInsert(listobjs);
        }
    }

    /**
     * @author: xsm
     * @date: 202003/20 0020 下午 3:14
     * @Description: 修改微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updateWechatPushSetInfo(List<WechatPushSetVO> listobjs, String pkid) {
        WechatPushSetVO obj = wechatPushSetMapper.selectByPrimaryKey(pkid);
        //根据微信群名删除数据
        wechatPushSetMapper.deleteByWechatName(obj.getWechatname());
        //批量新增微信群信息推送配置信息
        if (listobjs!=null&&listobjs.size()>0) {
            wechatPushSetMapper.batchInsert(listobjs);
        }
    }

    /**
     * @author: xsm
     * @date: 202003/20 0020 下午 3:25
     * @Description: 删除微信群信息推送配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deleteWechatPushSetInfoByWechatName(String wechatname) {
        //根据微信群名删除数据
        wechatPushSetMapper.deleteByWechatName(wechatname);
    }

    /**
     * @author: xsm
     * @date: 2020/03/20 0020 上午 11:36
     * @Description: 根据自定义参数获取微信群信息推送配置列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWechatPushSetInfosByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultlist= new ArrayList<>();
        List<Map<String, Object>> listdata = wechatPushSetMapper.getWechatPushSetInfosByParamMap(paramMap);
        if (listdata!=null&&listdata.size()>0){
            Set<String> set = new HashSet();
            for(Map<String,Object> objmap:listdata){
                boolean flag = set.contains(objmap.get("WechatName").toString());
                if (flag == false) {//没有重复
                    set.add(objmap.get("WechatName").toString());
                    Set<String> alarmtypes =new HashSet();
                    String pkid = objmap.get("PK_ID").toString();
                    String wechatname = objmap.get("WechatName").toString();
                    String Remark = objmap.get("Remark")!=null?objmap.get("Remark").toString():"";
                    for(Map<String,Object> map:listdata){
                        if ((objmap.get("WechatName").toString()).equals(map.get("WechatName").toString())){
                            String type = map.get("AlarmType").toString();
                            String typecode =CommonTypeEnum.WechatPushSetAlarmTypeEnum.getNameByCode(type)+"";
                            alarmtypes.add(typecode);
                        }
                    }
                    String alarmtype = "";
                    if(alarmtypes.size()>0){
                        for (String str :alarmtypes){
                            alarmtype =alarmtype+str+"、";
                        }
                        if (!"".equals(alarmtype)) {
                            alarmtype = alarmtype.substring(0, alarmtype.length() - 1);
                        }
                    }
                    Map<String,Object> resultmap = new HashMap<>();
                    resultmap.put("pkid",pkid);
                    resultmap.put("wechatname",wechatname);
                    resultmap.put("alarmtype",alarmtype);
                    resultmap.put("remark",Remark);
                    resultlist.add(resultmap);
                }else{
                    continue;
                }
            }
        }
        return resultlist;
    }

    @Override
    public Map<String, Object> getWechatPushSetInfoByWechatName(String wechatname) {
        Map<String, Object> resultmap = new HashMap<>();
        Map<String,Object> paramMap =new HashMap<>();
        paramMap.put("wechatname",wechatname);
        List<Map<String, Object>> listdata = wechatPushSetMapper.getWechatPushSetInfosByParamMap(paramMap);
        if (listdata!=null&&listdata.size()>0) {
            String Remark = "";
            Set<String> alarmtypes = new HashSet();
            for (Map<String, Object> map : listdata) {
                Remark = map.get("Remark")!=null?map.get("Remark").toString():"";
                if (wechatname.equals(map.get("WechatName").toString())) {
                    String type = map.get("AlarmType").toString();
                    alarmtypes.add(type);
                    }
            }
            resultmap.put("wechatname", wechatname);
            resultmap.put("alarmtypes", alarmtypes);
            resultmap.put("remark", Remark);
        }
        return resultmap;
    }
}

package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoMapper;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoVO;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OutPutUnorganizedService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class OutPutUnorganizedServiceImpl implements OutPutUnorganizedService {

    @Autowired
    private UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;

    @Autowired
    private PollutionMapper pollutionMapper;

    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;

    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return unorganizedMonitorPointInfoMapper.countTotalByParam(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/12 0012 下午 3:42
     * @Description: 删除
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkId) {
        return unorganizedMonitorPointInfoMapper.deleteByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/12 0012 下午 3:42
     * @Description: 通过id查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    @Override
    public UnorganizedMonitorPointInfoVO selectByPrimaryKey(String pkId) {
        return unorganizedMonitorPointInfoMapper.selectByPrimaryKey(pkId);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/12 0012 下午 3:43
     * @Description: 修改
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int updateByPrimaryKey(UnorganizedMonitorPointInfoVO record) {
        return unorganizedMonitorPointInfoMapper.updateByPrimaryKey(record);
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 下午 2:18
     * @Description: 自定义查询条件获取污染源下在线无组织监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnlineUnorganizedMonitorPointInfoByParamMap(Map<String, Object> paramMap) {
        return unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/6/19 0019 下午 5:11
     * @Description: 获取污染源下无组织监测点树形结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutionUnorganizedMonitorPointInfoByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = unorganizedMonitorPointInfoMapper.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
        if (dataList.size() > 0) {
            List<Map<String, Object>> dataListTemp = new ArrayList<>();
            Set<String> tempSet = new HashSet<>();
            for (Map<String, Object> map : dataList) {
                if (!tempSet.contains(map.get("pk_pollutionid").toString())) {
                    tempSet.add(map.get("pk_pollutionid").toString());
                    Map<String, Object> mapTemp = new HashMap<>();
                    mapTemp.put("shortername", map.get("shortername"));
                    mapTemp.put("pk_pollutionid", map.get("pk_pollutionid"));
                    List<Map<String, Object>> outputdata = new ArrayList<>();
                    Map<String, Object> outputMap = new HashMap<>();
                    outputMap.put("pk_id", map.get("pk_id"));
                    if (paramMap.get("orderfield")!=null&&"status".equals(paramMap.get("orderfield").toString())){
                        String statusstr = map.get("onlinestatus")!=null?map.get("onlinestatus").toString():"";
                        String namebycode = "";
                        if (!"".equals(statusstr)) {
                            namebycode = CommonTypeEnum.OnlineStatusEnum.getNameByCode(statusstr);
                        }
                        if (!"".equals(namebycode)) {
                            outputMap.put("monitorpointname", namebycode + "_" + map.get("monitorpointname"));
                        }
                    }else{
                        outputMap.put("monitorpointname", map.get("monitorpointname"));
                    }
                    outputMap.put("outputname", map.get("outputname"));
                    outputMap.put("dgimn", map.get("dgimn"));
                    outputMap.put("monitorpointtype", map.get("monitorpointtype"));
                    outputMap.put("onlinestatus", map.get("onlinestatus"));
                    outputMap.put("Longitude", map.get("Longitude"));
                    outputMap.put("Latitude", map.get("Latitude"));
                    outputMap.put("status", map.get("status"));
                    outputdata.add(outputMap);
                    mapTemp.put("outputdata", outputdata);
                    dataListTemp.add(mapTemp);
                } else {
                    for (Map<String, Object> mapTemp : dataListTemp) {
                        if (mapTemp.get("pk_pollutionid").equals(map.get("pk_pollutionid"))) {
                            List<Map<String, Object>> outputdata = (List<Map<String, Object>>) mapTemp.get("outputdata");
                            Map<String, Object> outputMap = new HashMap<>();
                            outputMap.put("pk_id", map.get("pk_id"));
                            if (paramMap.get("orderfield")!=null&&"status".equals(paramMap.get("orderfield").toString())){
                                String statusstr = map.get("onlinestatus")!=null?map.get("onlinestatus").toString():"";
                                String namebycode = "";
                                if (!"".equals(statusstr)) {
                                    namebycode = CommonTypeEnum.OnlineStatusEnum.getNameByCode(statusstr);
                                }
                                if (!"".equals(namebycode)) {
                                    outputMap.put("monitorpointname", namebycode + "_" + map.get("monitorpointname"));
                                }
                            }else{
                                outputMap.put("monitorpointname", map.get("monitorpointname"));
                            }
                            outputMap.put("outputname", map.get("outputname"));
                            outputMap.put("dgimn", map.get("dgimn"));
                            outputMap.put("Longitude", map.get("Longitude"));
                            outputMap.put("Latitude", map.get("Latitude"));
                            outputMap.put("monitorpointtype", map.get("monitorpointtype"));
                            outputMap.put("onlinestatus", map.get("onlinestatus"));
                            outputMap.put("status", map.get("status"));
                            outputdata.add(outputMap);
                            mapTemp.put("outputdata", outputdata);
                            break;
                        }
                    }
                }

            }
            dataList = dataListTemp;
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 2:29
     * @Description: 组装污染源、排口、监测污染物、特征污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> setUNGasOutPutAndPollutantDetail(List<Map<String, Object>> detailDataTemp, String id) {
        List<Map<String, Object>> detailData = new ArrayList<>();
        String pollutionid = "";
        for (Map<String, Object> map : detailDataTemp) {
            if ("fk_pollutionid".equals(map.get("fieldname").toString().toLowerCase())) {
                pollutionid = (String) map.get("value");
            }
        }
        //添加污染源信息
        detailData.add(pollutionMap(pollutionid));
        int num = 1;
        List<String> noInList = Arrays.asList("updatetime", "updateuser", "remark");
        //添加排口信息
        for (Map<String, Object> map : detailDataTemp) {
            if (!noInList.contains(map.get("fieldname").toString().toLowerCase())) {
                num = num + 1;
                if (num == 4) {
                    map.put("width", "50%");
                }
                map.put("ordernum", num);
                detailData.add(map);
            }
        }
        //添加监测污染物信息
        num = num + 1;
        detailData.add(getMonitorPollutant(pollutionid, id, num));
        return detailData;
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 2:52
     * @Description: 获取监测污染物信息详情格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getMonitorPollutant(String pollutionid, String id, int num) {
        Map<String, Object> paramMap = new HashMap<>();
        String monitorpollutants = "";
        paramMap.put("pollutionid", pollutionid);
        paramMap.put("outputid", id);
        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.unOrganizationWasteGasEnum.getCode());
        List<Map<String, Object>> monitorPollutant = pollutantFactorMapper.getPollutantSetInfoByParamMap(paramMap);
        if (monitorPollutant.size() > 0) {
            for (Map<String, Object> pollutant : monitorPollutant) {
                monitorpollutants += pollutant.get("pollutantname") + "、";
            }
            if (StringUtils.isNotBlank(monitorpollutants)) {
                monitorpollutants = monitorpollutants.substring(0, monitorpollutants.length() - 1);
            }
        } else {
            monitorpollutants = "";
        }
        paramMap.clear();
        paramMap.put("fieldname", "monitorpollutants");
        paramMap.put("controltype", "");
        paramMap.put("width", "50%");
        paramMap.put("showhide", true);
        paramMap.put("ordernum", num);
        paramMap.put("label", "监测污染物");
        paramMap.put("type", "string");
        paramMap.put("value", monitorpollutants);
        return paramMap;

    }

    private Map<String, Object> pollutionMap(String pollutionid) {
        PollutionVO pollutionVO = pollutionMapper.selectByPrimaryKey(pollutionid);
        Map<String, Object> map = new HashMap<>();
        map.put("fieldname", "pollutionname");
        map.put("controltype", "");
        map.put("width", "100%");
        map.put("showhide", true);
        map.put("ordernum", 1);
        map.put("label", "企业名称");
        map.put("type", "string");
        map.put("value", pollutionVO.getPollutionname());
        return map;
    }


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:38
     * @Description:获取所有已监测厂界小型站和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorUnMINIAndStatusInfo() {
        return unorganizedMonitorPointInfoMapper.getAllMonitorUnMINIAndStatusInfo();
    }

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:36
     * @Description: 获取所有已监测厂界恶臭和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorUnstenchAndStatusInfo() {
        return unorganizedMonitorPointInfoMapper.getAllMonitorUnstenchAndStatusInfo();
    }


    /**
     * @author: chengzq
     * @date: 2019/12/12 0012 上午 10:55
     * @Description: 获取所有已监测厂界扬尘和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorUnDustAndStatusInfo() {
        return unorganizedMonitorPointInfoMapper.getAllMonitorUnDustAndStatusInfo();
    }

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 3:09
     * @Description: 通过监测点名称，污染源id查询监测点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    @Override
    public Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> params) {
        return unorganizedMonitorPointInfoMapper.selectByPollutionidAndOutputName(params);
    }

    @Override
    public List<Map<String, Object>> getEntBoundaryAllPollutantsByIDAndType(Map<String, Object> paramMap) {
        return unorganizedMonitorPointInfoMapper.getEntBoundaryAllPollutantsByIDAndType(paramMap);
    }

    @Override
    public List<Map<String, Object>> getOutPutUnorganizedInfoByIDAndType(Map<String, Object> paramMap) {
        return unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap);
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/30 15:23
     * @Description: 获取无组织排口相关的企业、排口、污染物信息（厂界小型站，厂界恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getUnorganizedPollutionOutPutPollutants(Map<String, Object> paramMap) {
        return unorganizedMonitorPointInfoMapper.getUnorganizedPollutionOutPutPollutants(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 下午 5:02
     * @Description: gis-根据监测点类型获取所有厂界恶臭或厂界小型站的点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getAllUnorganizedInfoByType(Map<String, Object> paramMap, Map<String, Object> pollutionMap) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> alldata = unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap);
        int onlinenum = 0;
        int offlinenum = 0;
        if (alldata != null && alldata.size() > 0) {
            for (Map<String, Object> map : alldata) {
                int status = 0;
                if (map.get("OnlineStatus") != null && !"".equals(map.get("OnlineStatus").toString())) {//当状态不为空
                    if ("1".equals(map.get("OnlineStatus").toString())) {//有在线排口，有一个在线排口，则该企业为在线企业
                        status = 1;
                    } else {
                        if (status < Integer.parseInt(map.get("OnlineStatus").toString())) {
                            status = Integer.parseInt(map.get("OnlineStatus").toString());
                        }
                    }
                }
                if (status == 0) {//离线
                    offlinenum += 1;
                } else if (status == 1) {//在线
                    onlinenum += 1;
                }
                map.put("OnlineStatus", status);
            }
        }
        result.put("total", (alldata != null && alldata.size() > 0) ? alldata.size() : 0);
        result.put("pollutiontotal", pollutionMap.get("total"));
        result.put("gaspollution", pollutionMap.get("gaspollution"));
        result.put("onlinepollution", pollutionMap.get("onlinepollution"));
        result.put("onlinenum", onlinenum);
        result.put("offlinenum", offlinenum);
        result.put("listdata", alldata);
        return result;
    }


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 3:43
     * @Description: 通过味道code和mn号集合查询厂界恶臭信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> selectFactStenchInfoBySmellCodeAndMns(Map<String, Object> paramMap) {
        return unorganizedMonitorPointInfoMapper.selectFactStenchInfoBySmellCodeAndMns(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/11/4 0004 下午 1:36
     * @Description: 删除状态表中垃圾数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public int deleteGarbageData() {
        return unorganizedMonitorPointInfoMapper.deleteGarbageData();
    }

}

package com.tjpu.sp.service.impl.environmentalprotection.stopproductioninfo;

import com.tjpu.sp.dao.environmentalprotection.stopproductioninfo.MessageReadUserMapper;
import com.tjpu.sp.dao.environmentalprotection.stopproductioninfo.StopProductionInfoMapper;
import com.tjpu.sp.model.environmentalprotection.stopproductioninfo.MessageReadUserVO;
import com.tjpu.sp.model.environmentalprotection.stopproductioninfo.StopProductionInfoVO;
import com.tjpu.sp.service.environmentalprotection.stopproductioninfo.StopProductionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class StopProductionInfoServiceImpl implements StopProductionInfoService {

    @Autowired
    private StopProductionInfoMapper stopProductionInfoMapper;
    @Autowired
    private MessageReadUserMapper messageReadUserMapper;

    /**
     * @author: xsm
     * @date: 2019/12/18 0018 下午 6:36
     * @Description: 根据自定义参数获取停产排口列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStopProductionInfosByParamMap(Map<String,Object> paramMap) {
        return stopProductionInfoMapper.getStopProductionInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/12/18 0018 下午 7:14
     * @Description: 修改点位状态为停用
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    @Override
    public void updatePollutionOutPutStatusByParam(Map<String, Object> param) {
        stopProductionInfoMapper.updatePollutionOutPutStatusByParam(param);
    }

    /**
     * @author: xsm
     * @date: 2019/12/18 0018 下午 7:14
     * @Description: 新增一条停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [entity]
     * @throws:
     */
    @Override
    public void addStopProductionInfo(StopProductionInfoVO entity) {
        stopProductionInfoMapper.insert(entity);
    }

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 上午 8:37
     * @Description: 根据主键ID获取停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [stopproductionid]
     * @throws:
     */
    @Override
    public StopProductionInfoVO getStopProductionInfoByPkid(String stopproductionid) {
        return stopProductionInfoMapper.selectByPrimaryKey(stopproductionid);
    }

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 上午 8:39
     * @Description: 修改停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [entity]
     * @throws:
     */
    @Override
    public void editProductionInfo(StopProductionInfoVO entity) {
        stopProductionInfoMapper.updateByPrimaryKey(entity);
    }

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 上午 9:17
     * @Description: 根据自定义参数获取停产历史信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStopProductionHistoryInfosByParamMap(Map<String, Object> paramMap) {
        return stopProductionInfoMapper.getStopProductionHistoryInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/02/25 0025 上午 10:20
     * @Description: 根据自定义参数获取最新一条停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getLatestStopProductionInfoByParamMap(Map<String, Object> paramMap) {
        return stopProductionInfoMapper.getLatestStopProductionInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/2/25 0025 下午 3:23
     * @Description: 根据id获取停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getStopProductionInfoByID(String id) {
        return stopProductionInfoMapper.getStopProductionInfoByID(id);
    }

    @Override
    public StopProductionInfoVO selectByPrimaryKey(String id) {
        return stopProductionInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateStopProductionInfo(StopProductionInfoVO entity) {
        stopProductionInfoMapper.updateByPrimaryKey(entity);
    }

    /**
     * @author: xsm
     * @date: 2020/3/02 0002 上午 10:30
     * @Description: 根据自定义参数获取正在停产的排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getCurrentTimeStopProductionInfoByParamMap(Map<String, Object> paramMap) {
        return stopProductionInfoMapper.getCurrentTimeStopProductionInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/3/9 0009 下午 5:24
     * @Description: 通过自定义参数获取最新的排口停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getLastStopProductionInfoByParamMap(Map<String, Object> paramMap) {
        return stopProductionInfoMapper.getLastStopProductionInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/3/18 0018 下午 14:51
     * @Description: 通过自定义参数获取正在停产的排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getNowStopProductionInfosByParamMap(Map<String, Object> parammap) {
        return stopProductionInfoMapper.getNowStopProductionInfosByParamMap(parammap);
    }

    @Override
    public void addMessageReadUserInfo(MessageReadUserVO obj) {
        messageReadUserMapper.insert(obj);
    }

    @Override
    public List<Map<String, Object>> getStopProductHistory(Map<String, Object> parammap) {
        return stopProductionInfoMapper.getStopProductHistory(parammap);
    }

    @Override
    public List<Map<String, Object>> getStopProductionListDataByParamMap(Map<String, Object> parammap) {
        return stopProductionInfoMapper.getStopProductionListDataByParamMap(parammap);
    }

    /**
     * @author: xsm
     * @date: 2020/9/03 0003 上午 11:11
     * @Description: 批量添加停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void insertStopProductionInfos(List<StopProductionInfoVO> objs) {
        if (objs.size() > 0) {
            //批量添加
            stopProductionInfoMapper.batchAdd(objs);
        }
    }

    /**
     * @author: xsm
     * @date: 2020/9/03 0003 上午 11:11
     * @Description: 根据停产主键ID数组获取多条停产记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStopProductionInfoByPkids(List<String> ids) {
        return stopProductionInfoMapper.getStopProductionInfoByPkids(ids);
    }

    @Override
    public List<StopProductionInfoVO> selectByPrimaryKeys(List<String> pkids) {
        return stopProductionInfoMapper.selectByPrimaryKeys(pkids);
    }

    @Override
    public void deleteStopProductionInfoByIDs(List<String> pkids) {
        if (pkids.size()>0) {
            for (String id:pkids) {
                stopProductionInfoMapper.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public  Map<String,Object> getReproductionInfoByIDs(List<String> pkids) {
        List<Map<String,Object>> listdata = stopProductionInfoMapper.getStopProductionInfoByPkids(pkids);
        //判断停产 开始时间 结束时间  复产时间 停产类型一致 则合并详情信息
        Map<String,Object> result = new HashMap<>();
        if (listdata!=null&&listdata.size()>0) {
            result = listdata.get(0);
            String outputname ="";
            for (Map<String, Object> map : listdata) {
                outputname = outputname + map.get("outputname").toString()+"、";
            }
            if (!"".equals(outputname)){
                outputname = outputname.substring(0, outputname.length() - 1);
            }
            result.put("outputname",outputname);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getHistoryStopProductionDetailByIDs(List<String> pkids) {
        List<Map<String,Object>>  result = new ArrayList<>();
        List<Map<String,Object>> grouplistdata = stopProductionInfoMapper.getStopProductionGroupDataByPkids(pkids);
        List<Map<String,Object>> listdata = stopProductionInfoMapper.getStopProductionInfoByPkids(pkids);
        //判断停产 开始时间 结束时间  复产时间 停产类型一致 则合并详情信息
        if (grouplistdata!=null&&grouplistdata.size()>0){
            for (Map<String,Object> map:grouplistdata){
                Map<String,Object> obj = new HashMap<>();
                String outputname ="";
                boolean flag1 = true;
                boolean flag2 = true;
                boolean flag3 = true;
                boolean flag4 = true;
                boolean flag5 = true;
                boolean flag6 = true;
                boolean flag7 = true;
                boolean flag8 = true;
                for (Map<String,Object> twomap:listdata){
                    if (map.get("StartTime")!=null&&twomap.get("StartTime")!=null){
                        if (!(map.get("StartTime").toString()).equals(twomap.get("StartTime").toString())){
                            flag1 = false;
                        }
                    }else{
                        if (map.get("StartTime")==null&&twomap.get("StartTime")==null){
                            continue;
                        }else{
                            flag1 = false;
                        }
                    }
                    if (map.get("EndTime")!=null&&twomap.get("EndTime")!=null){
                        if (!(map.get("EndTime").toString()).equals(twomap.get("EndTime").toString())){
                            flag2 = false;
                        }
                    }else{
                        if (map.get("EndTime")==null&&twomap.get("EndTime")==null){
                            continue;
                        }else{
                            flag2 = false;
                        }
                    }
                    if (map.get("StopProductionRemark")!=null&&twomap.get("StopProductionRemark")!=null){
                        if (!(map.get("StopProductionRemark").toString()).equals(twomap.get("StopProductionRemark").toString())){
                            flag3 = false;
                        }
                    }else{
                        if (map.get("StopProductionRemark")==null&&twomap.get("StopProductionRemark")==null){
                            continue;
                        }else{
                            flag3 = false;
                        }
                    }
                    if (map.get("FK_FileID")!=null&&twomap.get("FK_FileID")!=null){
                        if (!(map.get("FK_FileID").toString()).equals(twomap.get("FK_FileID").toString())){
                            flag4 = false;
                        }
                    }else{
                        if (map.get("FK_FileID")==null&&twomap.get("FK_FileID")==null){
                            continue;
                        }else{
                            flag4 = false;
                        }
                    }
                    if (map.get("FK_StopProductionType")!=null&&twomap.get("FK_StopProductionType")!=null){
                        if (!(map.get("FK_StopProductionType").toString()).equals(twomap.get("FK_StopProductionType").toString())){
                            flag5 = false;
                        }
                    }else{
                        if (map.get("FK_StopProductionType")==null&&twomap.get("FK_StopProductionType")==null){
                            continue;
                        }else{
                            flag5 = false;
                        }
                    }
                    if (map.get("RecoveryProductionTime")!=null&&twomap.get("RecoveryProductionTime")!=null){
                        if (!(map.get("RecoveryProductionTime").toString()).equals(twomap.get("RecoveryProductionTime").toString())){
                            flag6 = false;
                        }
                    }else{
                        if (map.get("RecoveryProductionTime")==null&&twomap.get("RecoveryProductionTime")==null){
                            continue;
                        }else{
                            flag6 = false;
                        }
                    }
                    if (map.get("FK_RecoveryProductionFileID")!=null&&twomap.get("FK_RecoveryProductionFileID")!=null){
                        if (!(map.get("FK_RecoveryProductionFileID").toString()).equals(twomap.get("FK_RecoveryProductionFileID").toString())){
                            flag7 = false;
                        }
                    }else{
                        if (map.get("FK_RecoveryProductionFileID")==null&&twomap.get("FK_RecoveryProductionFileID")==null){
                            continue;
                        }else{
                            flag7 = false;
                        }
                    }
                    if (map.get("RecoveryProductionReason")!=null&&twomap.get("RecoveryProductionReason")!=null){
                        if (!(map.get("RecoveryProductionReason").toString()).equals(twomap.get("RecoveryProductionReason").toString())){
                            flag5 = false;
                        }
                    }else{
                        if (map.get("RecoveryProductionReason")==null&&twomap.get("RecoveryProductionReason")==null){
                            continue;
                        }else{
                            flag8 = false;
                        }
                    }
                    if (flag1==true&&flag2==true&&flag3==true&&flag4==true&&flag5==true&&flag6==true&&flag7==true&&flag8==true){
                        if (obj==null){
                            obj = twomap;
                        }
                        outputname = outputname + twomap.get("outputname").toString()+"、";
                    }
                }

                if (!"".equals(outputname)){
                    outputname = outputname.substring(0, outputname.length() - 1);
                }
                obj.put("outputname",outputname);
                result.add(obj);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/05/23 0023 上午 9:19
     * @Description: 根据自定义参数获取停产企业列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntStopProductionInfosByParamMap(Map<String, Object> parammap) {
        return stopProductionInfoMapper.getEntStopProductionInfosByParamMap(parammap);
    }

    @Override
    public void addEntStopProductionInfo(StopProductionInfoVO obj) {
        stopProductionInfoMapper.insert(obj);
    }

    @Override
    public Map<String, Object> getEntStopProductionDetailByID(String id) {
        return stopProductionInfoMapper.getEntStopProductionDetailByID(id);
    }

    @Override
    public void deleteEntStopProductionInfoByID(String id) {
        stopProductionInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void updateEntStopProductionInfo(StopProductionInfoVO obj) {
        stopProductionInfoMapper.updateByPrimaryKey(obj);
    }
}

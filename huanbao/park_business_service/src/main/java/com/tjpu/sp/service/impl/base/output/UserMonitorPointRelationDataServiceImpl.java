package com.tjpu.sp.service.impl.base.output;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.environmentalprotection.output.UserMonitorPointRelationDataMapper;
import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserMonitorPointRelationDataServiceImpl implements UserMonitorPointRelationDataService {

    @Autowired
    private UserMonitorPointRelationDataMapper userMonitorPointRelationDataMapper;



    /**
     * @author: chengzq
     * @date: 2020/4/20 0020 下午 12:13
     * @Description: 从查询出的监测点里筛选拥有权限的监测点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void ExcludeNoAuthDGIMNByParamMap(Collection<String> mns) {

        Map<String,Object> ParamMap=new HashMap<>();
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        ParamMap.put("categorys", categorys);
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
        ParamMap.put("userid", userid);
        List<Map<String, Object>> dgimnByParamMap = userMonitorPointRelationDataMapper.getDGIMNByParamMap(ParamMap);
        List<String> authMN = dgimnByParamMap.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());

        mns.removeIf(m->!authMN.contains(m));
    }

    @Override
    public List<Map<String, Object>> getDGIMNByParamMap(Map<String,Object> paramMap) {
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        paramMap.put("categorys", categorys);
        return userMonitorPointRelationDataMapper.getDGIMNByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/9/03 0003 下午 15:20
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updataUserMonitorPointRelationDataByMnAndType(String oldmn, String newmn, String monitorpointtype) {
        Map<String, Object> parammap = new HashMap<>();
        parammap.put("oldmn",oldmn);
        parammap.put("newmn",newmn);
        parammap.put("monitorpointtype",monitorpointtype);
        userMonitorPointRelationDataMapper.updataUserMonitorPointRelationDataByMnAndType(parammap);
    }

    /**
     * @author: xsm
     * @date: 2020/9/03 0003 下午 15:20
     * @Description:根据MN和类型批量删除该MN权限数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void deleteUserMonitorPointRelationDataByMnAndType(String dgimn, String monitorpointtype) {
        Map<String, Object> parammap = new HashMap<>();
        parammap.put("oldmn",dgimn);
        parammap.put("monitorpointtype",monitorpointtype);
        userMonitorPointRelationDataMapper.deleteUserMonitorPointRelationDataByMnAndType(parammap);
    }


    /**
     * @author: chengzq
     * @date: 2020/9/19 0019 下午 2:35
     * @Description: 通过自定义条件获取用户id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    @Override
    public List<String> getUserIdByParamMap(Map<String, Object> parammap) {
        return userMonitorPointRelationDataMapper.getUserIdByParamMap(parammap);
    }

    @Override
    public void addUserMonitorPointRelation(Map<String, Object> deletemap, List<UserMonitorPointRelationDataVO> list) {
        List<Map<String,Object>> monitiorpints=deletemap.get("monitiorpints")==null?new ArrayList<>():(List<Map<String,Object>>)deletemap.get("monitiorpints");
        deletemap.put("monitiorpints",getMonitorLists(monitiorpints));
        userMonitorPointRelationDataMapper.deleteByParamMap(deletemap);
        List<List<UserMonitorPointRelationDataVO>> monitorLists = getMonitorLists(list);
        for (List<UserMonitorPointRelationDataVO> monitorList : monitorLists) {
            userMonitorPointRelationDataMapper.batchAdd(monitorList);
        }

    }

    @Override
    public void deleteByParamMap(Map<String, Object> deletemap) {
        List<Map<String,Object>> monitiorpints=deletemap.get("monitiorpints")==null?new ArrayList<>():(List<Map<String,Object>>)deletemap.get("monitiorpints");
        deletemap.put("monitiorpints",getMonitorLists(monitiorpints));
        userMonitorPointRelationDataMapper.deleteByParamMap(deletemap);
    }

    @Override
    public List<String> getMonitorPointIDsByUserid(String userid) {
        return userMonitorPointRelationDataMapper.getMonitorPointIDsByUserid(userid);
    }

    //每100条删一次
    public synchronized static <T> List<List<T>> getMonitorLists(List<T> list){
        List<List<T>> result=new ArrayList<>();
        int listSize=list.size();
        int toIndex=100;
        for(int i = 0;i<list.size();i+=100){
            if(i+100>listSize){        //作用为toIndex最后没有100条数据则剩余几条newList中就装几条
                toIndex=listSize-i;
            }
            List newList = list.subList(i,i+toIndex);
            result.add(newList);
        }
        return result;
    }

}

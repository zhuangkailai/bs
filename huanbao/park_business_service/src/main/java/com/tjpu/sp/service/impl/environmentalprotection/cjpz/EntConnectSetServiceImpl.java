package com.tjpu.sp.service.impl.environmentalprotection.cjpz;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.cjpz.EntConnectSetMapper;
import com.tjpu.sp.dao.environmentalprotection.cjpz.PointAddressSetMapper;
import com.tjpu.sp.model.environmentalprotection.cjpz.EntConnectSetVO;
import com.tjpu.sp.model.environmentalprotection.cjpz.PointAddressSetVO;
import com.tjpu.sp.service.environmentalprotection.cjpz.EntConnectSetService;
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
public class EntConnectSetServiceImpl implements EntConnectSetService {
    @Autowired
    private EntConnectSetMapper entConnectSetMapper;
    @Autowired
    private PointAddressSetMapper pointAddressSetMapper;


    /**
     * @author: xsm
     * @date: 2021/01/12 0012 下午 13:58
     * @Description:根据自定义参数获取企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getEntConnectSetsByParamMap(Map<String, Object> paramMap) {
        return entConnectSetMapper.getEntConnectSetsByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/01/12 0012 下午 13:58
     * @Description:新增企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void insert(EntConnectSetVO entConnectSetVO) {
        entConnectSetMapper.insert(entConnectSetVO);
    }

    /**
     * @author: xsm
     * @date: 2021/01/12 0012 下午 13:58
     * @Description:修改企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateByPrimaryKey(EntConnectSetVO entConnectSetVO) {
        entConnectSetMapper.updateByPrimaryKey(entConnectSetVO);
    }
    
    /**
     * @author: xsm
     * @date: 2021/01/12 0012 下午 13:58
     * @Description:根据主键ID删除企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        entConnectSetMapper.deleteByPrimaryKey(id);
        pointAddressSetMapper.deleteByEntConnectSetID(id);
    }
    

    /**
     * @author: xsm
     * @date: 2021/01/12 0012 下午 13:58
     * @Description:根据主键ID获取企业连接设置详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getEntConnectSetDetailByID(String id) {
        return entConnectSetMapper.getEntConnectSetDetailByID(id);
    }

    /**
     * @author: xsm
     * @date: 2021/01/12 0021 下午 15:27
     * @Description: 添加实体和关联表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void insertEntityAndSetData(EntConnectSetVO entConnectSetVO, List<PointAddressSetVO> paramList) {
        entConnectSetMapper.insert(entConnectSetVO);//添加储罐信息
        if (paramList != null && paramList.size() > 0) {
            pointAddressSetMapper.batchInsert(paramList);//添加污染物信息到set表中
        }
    }

    @Override
    public long getEntConnectSetNumByParamMap(Map<String, Object> paramMap) {
        return entConnectSetMapper.getEntConnectSetNumByParamMap(paramMap);
    }

    @Override
    public void updateEntityAndSetData(EntConnectSetVO entConnectSetVO, List<PointAddressSetVO> paramList) {
        entConnectSetMapper.updateByPrimaryKey(entConnectSetVO);
        pointAddressSetMapper.deleteByEntConnectSetID(entConnectSetVO.getPkId());
        if (paramList != null && paramList.size() > 0) {
            pointAddressSetMapper.batchInsert(paramList);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/01/12 0012 下午 13:58
     * @Description:根据id获取企业连接设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public EntConnectSetVO selectByPrimaryKey(String id) {
        return entConnectSetMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getSecurityPointTreeData(Map<String, Object> param) {
        Integer type = Integer.parseInt(param.get("monitorpointtype").toString());
        List<Map<String, Object>> outPuts = new ArrayList<>();
        List<Map<String, Object>> Tree = new ArrayList<>();
        if (outPuts.size() > 0) {
            if (type == CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode() ||  type == CommonTypeEnum.MonitorPointTypeEnum.ProductionSiteEnum.getCode()){
                Map<String, List<Map<String, Object>>> listMap = outPuts.stream().collect(Collectors.groupingBy(m -> m.get("parentid").toString()));
                for(Map.Entry<String, List<Map<String, Object>>> entry:listMap.entrySet()){
                    List<Map<String, Object>> listone = entry.getValue();
                    Map<String, Object> parentmap = new HashMap<>();
                    parentmap.put("id",entry.getKey());
                    parentmap.put("label",listone.get(0).get("parentname"));
                    parentmap.put("flag",1);

                    List<Map<String, Object>> childrendata = getChildrenData(listone,parentmap.get("label"));
                    parentmap.put("children",childrendata);
                    Tree.add(parentmap);
                }
            }else{
                Map<String, List<Map<String, Object>>> listMap = outPuts.stream().collect(Collectors.groupingBy(m -> m.get("parentname").toString()));
                for(Map.Entry<String, List<Map<String, Object>>> entry:listMap.entrySet()){
                    List<Map<String, Object>> listone = entry.getValue();
                    Map<String, Object> parentmap = new HashMap<>();
                    parentmap.put("id",entry.getKey());
                    parentmap.put("label",entry.getKey());
                    parentmap.put("flag",1);
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (Map<String, Object> vo : listone) {
                        Map<String, Object> map  = new HashMap<>();
                        map.put("parentname",entry.getKey());
                        map.put("id",vo.get("DGIMN"));
                        map.put("label",vo.get("outputname"));
                        map.put("monitorpointid",vo.get("outputid"));
                        map.put("monitorpointtype",vo.get("FK_MonitorPointTypeCode"));
                        list.add(map);
                    }
                    parentmap.put("children",list);
                    Tree.add(parentmap);
                }
            }
        }
        return Tree;
    }

    private List<Map<String, Object>> getChildrenData(List<Map<String, Object>> listone,Object obj) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> vo : listone) {
            Map<String, Object> map  = new HashMap<>();
            map.put("parentname",obj);
            map.put("id",vo.get("DGIMN"));
            map.put("label",vo.get("monitorpointname"));
            map.put("monitorpointid",vo.get("outputid")!=null?vo.get("outputid"):vo.get("monitorpointid"));
            map.put("monitorpointtype",vo.get("monitorpointtype"));
            list.add(map);
        }
        return list;
    }

    /**
     *@author: xsm
     *@date: 2021/01/13 0013 9:32
     *@Description: 通过企业采集配置ID获取该点位采集配置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [storageid]
     *@throws:
     **/
    @Override
    public List<Map<String, Object>> getPointAddressSetsByEntConnectSetID(Map<String, Object> paramMap) {
        return pointAddressSetMapper.getPointAddressSetsByEntConnectSetID(paramMap);
    }
}

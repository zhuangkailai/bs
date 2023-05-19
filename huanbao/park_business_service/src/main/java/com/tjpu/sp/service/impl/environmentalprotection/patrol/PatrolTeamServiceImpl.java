package com.tjpu.sp.service.impl.environmentalprotection.patrol;


import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.environmentalprotection.patrol.PatrolTeamEntOrPointMapper;
import com.tjpu.sp.dao.environmentalprotection.patrol.PatrolTeamMapper;
import com.tjpu.sp.dao.environmentalprotection.patrol.PatrolTeamUserMapper;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamEntOrPointVO;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamUserVO;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamVO;
import com.tjpu.sp.service.environmentalprotection.patrol.PatrolTeamService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.*;


@Service
@Transactional
public class PatrolTeamServiceImpl implements PatrolTeamService {
    @Autowired
    private PatrolTeamMapper patrolTeamMapper;

    @Autowired
    private PatrolTeamEntOrPointMapper patrolTeamEntOrPointMapper;
    @Autowired
    private PatrolTeamUserMapper patrolTeamUserMapper;


    @Override
    public void updateData(PatrolTeamVO obj) {
        //更新组信息
        patrolTeamMapper.updateByPrimaryKey(obj);
        //更新人员信息
        patrolTeamUserMapper.deleteByFkId(obj.getPkId());
        addUser(obj);
        //更新企业点位信息
        patrolTeamEntOrPointMapper.deleteByFkId(obj.getPkId());
        addEntOrPoint(obj);
    }

    private void addEntOrPoint(PatrolTeamVO obj) {
        List<PatrolTeamEntOrPointVO> patrolTeamEntOrPointVOS = obj.getPatrolTeamEntOrPointVOS();
        if (patrolTeamEntOrPointVOS!=null&&patrolTeamEntOrPointVOS.size()>0){
            for (PatrolTeamEntOrPointVO patrolTeamEntOrPointVO : patrolTeamEntOrPointVOS) {
                patrolTeamEntOrPointVO.setFkTeamid(obj.getPkId());
                patrolTeamEntOrPointMapper.insert(patrolTeamEntOrPointVO);
            }
        }

    }

    private void addUser(PatrolTeamVO obj) {
        List<PatrolTeamUserVO> patrolTeamUserVOS = obj.getPatrolTeamUserVOS();
        if (patrolTeamUserVOS!=null&&patrolTeamUserVOS.size()>0){
            for (PatrolTeamUserVO patrolTeamUserVO : patrolTeamUserVOS) {
                patrolTeamUserVO.setFkTeamid(obj.getPkId());
                patrolTeamUserMapper.insert(patrolTeamUserVO);
            }
        }

    }

    @Override
    public void insertData(PatrolTeamVO obj) {
        //添加组信息
        patrolTeamMapper.insert(obj);
        //添加人员信息
        addUser(obj);
        //添加企业或点位信息
        addEntOrPoint(obj);
        //更新数据权限

    }

    @Override
    public void deleteByPrimaryKey(String id) {
        patrolTeamMapper.deleteByPrimaryKey(id);
        patrolTeamUserMapper.deleteByFkId(id);
        patrolTeamEntOrPointMapper.deleteByFkId(id);
    }

    @Override
    public Map<String, Object> getDetailOrEditById(String id) {
        return patrolTeamMapper.getDetailOrEditById(id);
    }

    @Override
    public List<Map<String, Object>> getUserIdsById(String id) {
        return patrolTeamUserMapper.getUserIdsById(id);
    }

    @Override
    public List<Map<String, Object>> getEntOrPointDataListById(String id) {
        return patrolTeamEntOrPointMapper.getEntOrPointDataListById(id);
    }

    @Override
    public List<Map<String, Object>> getDataListByParamMap(JSONObject jsonObject) {
        List<Map<String, Object>> dataList = patrolTeamMapper.getDataListByParamMap(jsonObject);
        List<Map<String, Object>> userDataList = patrolTeamUserMapper.getUserDataList();
        setUserData(dataList, userDataList);
        List<Map<String, Object>> entDataList = patrolTeamEntOrPointMapper.getEntOrPointDataList();
        setEntOrPointData(dataList, entDataList);
        return dataList;
    }

    @Override
    public Map<String, Object> getDataMapByParam(Map<String, Object> paramMap) {
        return patrolTeamMapper.getDataMapByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPointDataListByParamMap(JSONObject jsonObject) {

        List<Map<String, Object>> dataList = patrolTeamEntOrPointMapper.getPointDataListByParamMap(jsonObject);
        List<Map<String, Object>> userDataList = patrolTeamUserMapper.getUserDataList();
        setUserData(dataList, userDataList);
        return dataList;
    }

    @Override
    public void updatePointData(PatrolTeamEntOrPointVO obj) {
        patrolTeamEntOrPointMapper.updateByPrimaryKey(obj);
    }

    @Override
    public void insertPointData(PatrolTeamEntOrPointVO obj) {
        patrolTeamEntOrPointMapper.insert(obj);
    }

    @Override
    public void deletePointDataById(String id) {
        patrolTeamEntOrPointMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getEntDataListByParamMap(JSONObject jsonObject) {
        List<Map<String, Object>> dataList = patrolTeamEntOrPointMapper.getEntDataListByParamMap(jsonObject);
        List<Map<String, Object>> userDataList = patrolTeamUserMapper.getUserDataList();
        setUserData(dataList, userDataList);
        return dataList;
    }

    @Override
    public List<Map<String, Object>> getTeamDataList() {
        return patrolTeamMapper.getTeamDataList();
    }

    @Override
    public List<Map<String, Object>> getUserDataListById(String id) {
        return patrolTeamUserMapper.getUserDataListById(id);
    }

    @Override
    public List<Map<String, Object>> getOverReviewerUserTreeData(Map<String, Object> param) {
        return patrolTeamMapper.getOverReviewerUserTreeData(param);
    }


    private void setEntOrPointData(List<Map<String, Object>> dataList, List<Map<String, Object>> entDataList) {
        if (entDataList.size() > 0) {
            Comparator<String> comparator = new Comparator<String>() {
                public int compare(String o1, String o2) {
                    Collator collator = Collator.getInstance();
                    return collator.getCollationKey(o1).compareTo(
                            collator.getCollationKey(o2));
                }
            };

            Map<String, List<String>> idAndEnts = new HashMap<>();
            Map<String, Map<String, List<String>>> idAndTypePoints = new HashMap<>();
            Map<String, List<String>> typeAndPoints;

            List<String> ents;
            List<String> points;
            String entId;
            String pointName;
            String monitorpointtypename;
            for (Map<String, Object> dataMap : entDataList) {
                entId = dataMap.get("fk_teamid").toString();
                if (dataMap.get("pollutionname") != null) {
                    if (idAndEnts.containsKey(entId)) {
                        ents = idAndEnts.get(entId);
                    } else {
                        ents = new ArrayList<>();
                    }
                    ents.add(dataMap.get("pollutionname").toString());
                    idAndEnts.put(entId, ents);
                } else {
                    if (idAndTypePoints.containsKey(entId)) {
                        typeAndPoints = idAndTypePoints.get(entId);
                    } else {
                        typeAndPoints = new HashMap<>();
                    }
                    monitorpointtypename = dataMap.get("monitorpointtypename") + "";
                    pointName = dataMap.get("monitorpointname").toString();
                    if (typeAndPoints.containsKey(monitorpointtypename)) {
                        points = typeAndPoints.get(monitorpointtypename);
                    } else {
                        points = new ArrayList<>();
                    }
                    points.add(pointName);
                    typeAndPoints.put(monitorpointtypename, points);
                    idAndTypePoints.put(entId, typeAndPoints);
                }
            }

            for (Map<String, Object> dataMap : dataList) {
                String entpointnames = "";
                entId = dataMap.get("pk_id").toString();
                //企业信息
                if (idAndEnts.containsKey(entId)) {
                    ents = idAndEnts.get(entId);
                    Collections.sort(ents,comparator);
                    entpointnames += "企业:" + DataFormatUtil.FormatListToString(ents, "、")+"；";
                }
                if (idAndTypePoints.containsKey(entId)) {
                    typeAndPoints = idAndTypePoints.get(entId);
                    typeAndPoints = DataFormatUtil.sortByKey(typeAndPoints, false);
                    for (String type : typeAndPoints.keySet()) {
                        points = typeAndPoints.get(type);
                        Collections.sort(points,comparator);
                        entpointnames +=type + ":" + DataFormatUtil.FormatListToString(points, "、")+"；";
                    }
                }
                dataMap.put("entpointnames", entpointnames);
            }
        }
    }
    private void setUserData(List<Map<String, Object>> dataList, List<Map<String, Object>> userDataList) {

        if (userDataList.size() > 0) {


            Comparator<String> comparator = new Comparator<String>() {
                public int compare(String o1, String o2) {
                    Collator collator = Collator.getInstance();
                    return collator.getCollationKey(o1).compareTo(
                            collator.getCollationKey(o2));
                }
            };

            Map<String, List<String>> idAndTypes = new HashMap<>();
            List<String> types;
            String entId;
            for (Map<String, Object> dataMap : userDataList) {
                entId = dataMap.get("fk_teamid").toString();
                if (idAndTypes.containsKey(entId)) {
                    types = idAndTypes.get(entId);
                } else {
                    types = new ArrayList<>();
                }
                types.add(dataMap.get("user_name").toString());
                idAndTypes.put(entId, types);
            }
            for (Map<String, Object> dataMap : dataList) {
                if (dataMap.get("pk_id")!=null){
                    entId = dataMap.get("pk_id").toString();
                    if (idAndTypes.containsKey(entId)) {
                        types = idAndTypes.get(entId);
                    } else {
                        types = new ArrayList<>();
                    }
                    Collections.sort(types,comparator);
                    dataMap.put("usernames", DataFormatUtil.FormatListToString(types, "、"));
                }

            }
        }
    }
}

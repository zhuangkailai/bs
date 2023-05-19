package com.pub.impl;

import com.pub.common.utils.RedisTemplateUtil;
import com.pub.dao.*;
import com.pub.model.*;
import com.pub.service.OperateLogService;
import com.tjpu.pk.common.utils.DataFormatUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class OperateLogServiceImpl implements OperateLogService {
    @Autowired
    private OperateLogMapper operateLogMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private CommonSelectFieldConfigMapper commonSelectFieldConfigMapper;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private ButtonMapper buttonMapper;

    @Override
    public int insert(OperateLogVO operateLogVO) {
        return operateLogMapper.insert(operateLogVO);
    }


    public void saveUserOperationLog(String operateType,
                                     Map<String, Object> paramMap, Map<String, Object> compareDataMap,
                                     Map<String, Object> oldMapData, Map<String, Object> newMapData) {

        String logDescription = "";

        String logType = "";

        String userName = "";
        switch (operateType) {
            case "login":
                logDescription = "登录了系统。";
                logType = "loginlog";
                break;
            case "exit":
                logDescription = "退出了系统。";
                logType = "loginlog";
                break;
            case "editpwd":
                logDescription = "修改了密码。";
                logType = "handlelog";
                break;
            case "resetpwd":
                if (paramMap.get("resetuserid") != null) {

                    String resetUserId = paramMap.get("resetuserid").toString();

                    UserInfoVO user = userInfoMapper.selectByPrimaryKey(resetUserId);

                    String userAccount = user.getUserAccount();

                    logDescription = "重置了帐号为【" + userAccount + "】的用户的帐号密码。";
                    logType = "handlelog";
                    break;
                }
            case "add":
                if (paramMap.get("tablename") != null) {

                    String tableName = paramMap.get("tablename").toString();

                    logDescription = doOperateLogVO(operateType, tableName, oldMapData, newMapData);
                    logType = "handlelog";
                    break;
                }
            case "delete":
                if (paramMap.get("tablename") != null) {

                    String tableName = paramMap.get("tablename").toString();

                    logDescription = doOperateLogVO(operateType, tableName, oldMapData, newMapData);
                    logType = "handlelog";
                    break;
                }
            case "edit":
                if (paramMap.get("tablename") != null) {

                    String tableName = paramMap.get("tablename").toString();

                    logDescription = doOperateLogVO(operateType, tableName, oldMapData, newMapData);
                    if (!"Base_userInfo".equals(tableName) && !"".equals(logDescription)) {
                        Map<String, Object> oldMaps = new HashMap<String, Object>();
                        Map<String, Object> newMaps = new HashMap<String, Object>();
                        if (compareDataMap != null && compareDataMap.size() > 0) {
                            for (Map.Entry<String, Object> entry : compareDataMap.entrySet()) {
                                String key = entry.getKey();
                                oldMaps.put(key, oldMapData.get(key));
                                newMaps.put(key, newMapData.get(key));
                            }
                        }

                        String lastDescription = fomateEditLog(tableName, oldMaps, newMaps);
                        if (!"".equals(lastDescription)) {
                            logDescription += lastDescription;
                        } else {
                            logDescription = "";
                        }
                        logType = "handlelog";
                        break;
                    }
                }

        }
        OperateLogVO operateLogVO = new OperateLogVO();

        String uuid = CommonServiceSupport.getUUID();

        if (!"exit".equals(operateType)) {
            userName = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
        } else {
            if (paramMap.get("username") != null) {
                userName = paramMap.get("username").toString();
            }
        }
        if (!"".equals(userName) && userName != null && !"".equals(logDescription)) {

            operateLogVO.setBaseOperateId(uuid);
            operateLogVO.setBaseOperateType(operateType);
            operateLogVO.setBaseOperateContent(logDescription);
            operateLogVO.setBaseOperatePerson(userName);
            operateLogVO.setBaseOperateDatetime(DataFormatUtil.getDate());
            operateLogVO.setBaseLogType(logType);
            if (!"".equals(logDescription)) {
                operateLogMapper.insert(operateLogVO);
            }
        }
    }


    public String doOperateLogVO(String methodType, String tableName,
                                 Map<String, Object> oldMapData, Map<String, Object> newMapData) {

        String firstDescription = "";

        String middleDescription = "";

        String lastDescription = "";

        if ("add".equals(methodType)) {
            firstDescription = "添加";
        } else if ("edit".equals(methodType)) {
            firstDescription = "修改";
        } else if ("delete".equals(methodType)) {
            firstDescription = "删除";
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("configtype", "add");
        map.put("tablename", tableName);
        map.put("logfieldflag", 1);

        List<CommonSelectFieldConfigVO> list = new ArrayList<CommonSelectFieldConfigVO>();
        list = commonSelectFieldConfigMapper.getOperateLogFieldFlag(map);

        if (oldMapData != null && newMapData != null) {

            if (list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    CommonSelectFieldConfigVO obj = list.get(j);
                    String FieldComments = obj.getFieldComments();
                    String FieldName = obj.getFieldName();
                    if (FieldComments != null && FieldName != null) {
                        if (oldMapData.get(FieldName.toLowerCase()) != null) {
                            String value = oldMapData.get(FieldName.toLowerCase()).toString();
                            if (j == 0) {
                                middleDescription = FieldComments + "为【" + value;
                            } else {
                                middleDescription = middleDescription + "(" + value + ")";
                            }
                        }
                    }
                }
            }

            if (!"".equals(middleDescription)) {
                middleDescription = middleDescription + "】的记录\n";
            }
        } else if (oldMapData == null && newMapData != null) {

            if (list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    CommonSelectFieldConfigVO obj = list.get(j);
                    String FieldComments = obj.getFieldComments();
                    String FieldName = obj.getFieldName();
                    if (FieldComments != null && FieldName != null) {
                        if (newMapData.get(FieldName.toLowerCase()) != null) {
                            String value = newMapData.get(FieldName.toLowerCase()).toString();
                            if (j == 0) {
                                middleDescription = FieldComments + "为【" + value;
                            } else {
                                middleDescription = middleDescription + "(" + value + ")";
                            }
                        }
                    }
                }
            }
            if (!"".equals(middleDescription)) {
                middleDescription = middleDescription + "】的记录。";
            }
        } else if (oldMapData != null && newMapData == null) {
            if (list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    CommonSelectFieldConfigVO obj = list.get(j);
                    String FieldComments = obj.getFieldComments();
                    String FieldName = obj.getFieldName();
                    if (FieldComments != null && FieldName != null) {
                        if (oldMapData.get(FieldName.toLowerCase()) != null) {
                            String value = oldMapData.get(FieldName.toLowerCase()).toString();
                            if (j == 0) {
                                middleDescription = FieldComments + "为【" + value;
                            } else {
                                middleDescription = middleDescription + "(" + value + ")";
                            }
                        }
                    }
                }
            }
            if (!"".equals(middleDescription)) {
                middleDescription = middleDescription + "】的记录。";
            }
        }


        if (!"".equals(middleDescription)) {

            lastDescription = firstDescription + middleDescription;

        } else {
            lastDescription = "";
        }
        return lastDescription;
    }


    public String getUserDataPermissionsEditLog(Map<String, Object> oldMapData, Map<String, Object> newMapData) {
        String addDescription = "";
        String deleteDescription = "";
        String resultDescription = "";
        List<String> oldRegionList = null;
        List<String> newRegionList = null;
        List<String> oldPollutionList = null;
        List<String> newPollutionList = null;

        if (oldMapData.get("fk_region") != null && !"".equals(oldMapData.get("fk_region"))) {
            oldRegionList = Arrays.asList(oldMapData.get("fk_region").toString());
            if (oldRegionList.size() > 0) {

            } else {
                oldRegionList = null;
            }
        }

        if (newMapData.get("fk_region") != null && !"".equals(newMapData.get("fk_region"))) {
            newRegionList = Arrays.asList(newMapData.get("fk_region").toString());
            if (newRegionList.size() > 0) {

            } else {
                newRegionList = null;
            }
        }

        if (oldMapData.get("fk_pollutionid") != null && !"".equals(oldMapData.get("fk_pollutionid"))) {
            oldPollutionList = Arrays.asList(oldMapData.get("fk_pollutionid").toString());
            if (oldPollutionList.size() > 0) {

            } else {
                oldPollutionList = null;
            }
        }

        if (newMapData.get("fk_pollutionid") != null && !"".equals(newMapData.get("fk_pollutionid"))) {
            newPollutionList = Arrays.asList(newMapData.get("fk_pollutionid").toString());
            if (newPollutionList.size() > 0) {

            } else {
                newPollutionList = null;
            }
        }

        if ((oldRegionList != null && newRegionList != null) && (oldPollutionList != null && newPollutionList != null)) {
            List<Map<String, String>> regionOldList = new ArrayList<Map<String, String>>();
            List<Map<String, String>> regionNewList = new ArrayList<Map<String, String>>();
            List<Map<String, String>> pollutionOldlist = new ArrayList<Map<String, String>>();
            List<Map<String, String>> pollutionNewlist = new ArrayList<Map<String, String>>();
            Map<String, Object> paramsOldRegion = new HashMap<String, Object>();
            Map<String, Object> paramsNewRegion = new HashMap<String, Object>();
            Map<String, Object> paramsOldPollution = new HashMap<String, Object>();
            Map<String, Object> paramsNewPollution = new HashMap<String, Object>();

            if (oldPollutionList != null && newPollutionList != null) {
                for (int m = oldPollutionList.size() - 1; m >= 0; m--) {
                    String valueKeyOne = oldPollutionList.get(m).toString();
                    for (int n = 0; n < newPollutionList.size(); n++) {
                        if (newPollutionList.get(n) != null) {
                            String valueKeyTwo = newPollutionList.get(n).toString();
                            if (valueKeyOne.equals(valueKeyTwo)) {
                                oldPollutionList.remove(m);
                                newPollutionList.remove(n);
                                break;
                            }
                        }
                    }
                }
            }

            if (oldRegionList.size() > 0) {
                paramsOldPollution.put("pollutionidlist", oldPollutionList);
                paramsOldRegion.put("regioncodelist", oldRegionList);
            }
            if (newRegionList.size() > 0) {
                paramsNewPollution.put("pollutionidlist", newPollutionList);
                paramsNewRegion.put("regioncodelist", newRegionList);
            }

            if (oldRegionList != null && oldRegionList.size() > 0) {
                regionOldList = commonSelectFieldConfigMapper.getRegionListByparams(paramsOldRegion);
            }
            if (oldPollutionList != null && oldPollutionList.size() > 0) {
                pollutionOldlist = commonSelectFieldConfigMapper.getPollutionListByparams(paramsOldPollution);
            }

            if (newRegionList != null && newRegionList.size() > 0) {
                regionNewList = commonSelectFieldConfigMapper.getRegionListByparams(paramsNewRegion);
            }
            if (newPollutionList != null && newPollutionList.size() > 0) {
                pollutionNewlist = commonSelectFieldConfigMapper.getPollutionListByparams(paramsNewPollution);
            }
            String oldRegionName = "";
            String NewRegionName = "";

            if (regionOldList.size() > 0) {
                Map<String, String> regionOldMap = regionOldList.get(0);
                oldRegionName = regionOldMap.get("name");
            }
            if (regionNewList.size() > 0) {
                Map<String, String> regionNewMap = regionNewList.get(0);
                NewRegionName = regionNewMap.get("name");
            }

            if (pollutionOldlist.size() > 0) {

                if (oldRegionName.equals(NewRegionName)) {
                    deleteDescription = " 删除数据权限:行政区划【" + oldRegionName
                            + "】下企业";
                } else {
                    deleteDescription = " 删除数据权限:行政区划【" + oldRegionName
                            + "】及企业";
                }
                for (int m = 0; m < pollutionOldlist.size(); m++) {
                    Map<String, String> maps = pollutionOldlist.get(m);
                    deleteDescription = deleteDescription + "【" + maps.get("pollutionname") + "】、";
                }
                if (!"".equals(deleteDescription)) {
                    deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
                }
            }
            if (pollutionNewlist.size() > 0) {

                if (oldRegionName.equals(NewRegionName)) {
                    addDescription = " 新增数据权限:行政区划【" + NewRegionName + "】下企业";
                } else {
                    addDescription = " 新增数据权限:行政区划【" + NewRegionName + "】及企业";
                }
                for (int m = 0; m < pollutionNewlist.size(); m++) {
                    Map<String, String> maps = pollutionNewlist.get(m);
                    addDescription = addDescription + "【" + maps.get("pollutionname") + "】、";
                }
                if (!"".equals(addDescription)) {
                    addDescription = addDescription.substring(0, addDescription.length() - 1);
                }
            }

        } else if ((oldRegionList != null && newRegionList != null) && oldPollutionList == null && newPollutionList == null) {
            List<Map<String, String>> oldlist = new ArrayList<Map<String, String>>();
            List<Map<String, String>> newlist = new ArrayList<Map<String, String>>();
            Map<String, Object> paramsOld = new HashMap<String, Object>();
            Map<String, Object> paramsNew = new HashMap<String, Object>();

            if (oldRegionList != null && newRegionList != null) {
                for (int m = oldRegionList.size() - 1; m >= 0; m--) {
                    if (oldRegionList.get(m) != null) {
                        String valueKeyOne = oldRegionList.get(m).toString();
                        for (int n = 0; n < newRegionList.size(); n++) {
                            if (newRegionList.get(n) != null) {
                                String valueKeyTwo = newRegionList.get(n).toString();
                                if (valueKeyOne.equals(valueKeyTwo)) {
                                    oldRegionList.remove(m);
                                    newRegionList.remove(n);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            paramsOld.put("regioncodelist", oldRegionList);
            paramsNew.put("regioncodelist", newRegionList);

            if (oldRegionList != null && oldRegionList.size() > 0) {
                oldlist = commonSelectFieldConfigMapper.getRegionListByparams(paramsOld);
            }

            if (newRegionList != null && newRegionList.size() > 0) {
                newlist = commonSelectFieldConfigMapper.getRegionListByparams(paramsNew);
            }

            if (oldlist.size() > 0) {
                deleteDescription = " 删除数据权限:行政区划";
                for (int m = 0; m < oldlist.size(); m++) {
                    Map<String, String> maps = oldlist.get(m);
                    deleteDescription = deleteDescription + "【" + maps.get("name") + "】、";
                }
                if (!"".equals(deleteDescription)) {
                    deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
                }
            }
            if (newlist.size() > 0) {
                addDescription = " 新增数据权限:行政区划";
                for (int m = 0; m < newlist.size(); m++) {
                    Map<String, String> maps = newlist.get(m);
                    addDescription = addDescription + "【" + maps.get("name") + "】、";
                }
                if (!"".equals(addDescription)) {
                    addDescription = addDescription.substring(0, addDescription.length() - 1);
                }
            }
        } else {
            List<Map<String, String>> regionOldList = new ArrayList<Map<String, String>>();
            List<Map<String, String>> regionNewList = new ArrayList<Map<String, String>>();
            List<Map<String, String>> pollutionOldlist = new ArrayList<Map<String, String>>();
            List<Map<String, String>> pollutionNewlist = new ArrayList<Map<String, String>>();
            Map<String, Object> paramsOldRegion = new HashMap<String, Object>();
            Map<String, Object> paramsNewRegion = new HashMap<String, Object>();
            Map<String, Object> paramsOldPollution = new HashMap<String, Object>();
            Map<String, Object> paramsNewPollution = new HashMap<String, Object>();

            if (oldRegionList != null && oldRegionList.size() > 0) {
                paramsOldRegion.put("regioncodelist", oldRegionList);
            }
            if (oldPollutionList != null && oldPollutionList.size() > 0) {
                paramsOldPollution.put("pollutionidlist", oldPollutionList);
            }
            if (newRegionList != null && newRegionList.size() > 0) {

                paramsNewRegion.put("regioncodelist", newRegionList);
            }
            if (newPollutionList != null && newPollutionList.size() > 0) {
                paramsNewPollution.put("pollutionidlist", newPollutionList);
            }
            String oldRegionName = "";
            String NewRegionName = "";
            if (oldRegionList != null && oldRegionList.size() > 0) {
                regionOldList = commonSelectFieldConfigMapper.getRegionListByparams(paramsOldRegion);
            }
            if (oldPollutionList != null && oldPollutionList.size() > 0) {
                pollutionOldlist = commonSelectFieldConfigMapper.getPollutionListByparams(paramsOldPollution);
                if (regionOldList.size() > 0) {
                    Map<String, String> regionOldMap = regionOldList.get(0);
                    oldRegionName = regionOldMap.get("name");
                }
            }
            if (newRegionList != null && newRegionList.size() > 0) {
                regionNewList = commonSelectFieldConfigMapper.getRegionListByparams(paramsNewRegion);
            }
            if (newPollutionList != null && newPollutionList.size() > 0) {
                pollutionNewlist = commonSelectFieldConfigMapper.getPollutionListByparams(paramsNewPollution);
                if (regionNewList.size() > 0) {
                    Map<String, String> regionNewMap = regionNewList.get(0);
                    NewRegionName = regionNewMap.get("name");
                }
            }

            if (pollutionOldlist.size() > 0) {
                deleteDescription = " 删除数据权限:行政区划【" + oldRegionName + "】及企业";
                for (int m = 0; m < pollutionOldlist.size(); m++) {
                    Map<String, String> maps = pollutionOldlist.get(m);
                    deleteDescription = deleteDescription + "【" + maps.get("pollutionname") + "】、";
                }
                deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
            } else {
                if (regionOldList.size() > 0) {
                    deleteDescription = " 删除数据权限:行政区划";
                    for (int i = 0; i < regionOldList.size(); i++) {
                        Map<String, String> regionOldMap = regionOldList.get(i);
                        oldRegionName = regionOldMap.get("name").toString();
                        deleteDescription += "【" + oldRegionName + "】、";
                    }
                    deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
                } else {

                }

            }

            if (pollutionNewlist.size() > 0) {
                addDescription = " 新增数据权限:行政区划【" + NewRegionName + "】及企业";
                for (int m = 0; m < pollutionNewlist.size(); m++) {
                    Map<String, String> maps = pollutionNewlist.get(m);
                    addDescription = addDescription + "【" + maps.get("pollutionname") + "】、";
                }
                addDescription = addDescription.substring(0, addDescription.length() - 1);
            } else {
                if (regionNewList.size() > 0) {
                    addDescription = " 新增数据权限:行政区划";
                    for (int i = 0; i < regionNewList.size(); i++) {
                        Map<String, String> regionNewMap = regionNewList.get(i);
                        NewRegionName = regionNewMap.get("name");
                        addDescription += "【" + NewRegionName + "】、";
                    }
                    addDescription = addDescription.substring(0, addDescription.length() - 1);
                } else {

                }
            }

        }
        resultDescription = addDescription + deleteDescription;

        return resultDescription;
    }


    @SuppressWarnings("unused")
    public String fomateEditLog(String tableName, Map<String, Object> oldMapData, Map<String, Object> newMapData) {
        String logDescription = "";
        String addDescription = "";
        String deleteDescription = "";
        String middleDescription = "";
        String lastDescription = "";
        String addAuthority = "";
        String deleteAuthority = "";
        String radioDescription = "";

        for (String key : oldMapData.keySet()) {
            if ((oldMapData.get(key) != null && newMapData.get(key) != null) && oldMapData.get(key).toString()
                    .equals(newMapData.get(key).toString())) {

            } else {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("tablename", tableName);
                map.put("configtype", "edit");
                map.put("fieldname", key);
                List<CommonSelectFieldConfigVO> datas = new ArrayList<CommonSelectFieldConfigVO>();

                datas = commonSelectFieldConfigMapper.getCommonSelectFieldConfigByMap(map);

                String keyComments = datas.get(0).getFieldComments();

                if (datas.size() > 0 && (datas.get(0).getCustomOptions() == null || "".equals(datas.get(0).getCustomOptions()))) {
                    CommonSelectFieldConfigVO com = datas.get(0);

                    String RelationalTable = com.getRelationalTable();

                    String FKKeyField = com.getFkKeyField();

                    String FKNameField = com.getFkNameField();

                    String MiddleTable = com.getMiddleTable();

                    String LeftId = com.getLeftId();

                    String RightId = com.getRightId();

                    String MenuId = "";

                    String ButtonId = "";

                    if (RightId != null) {
                        String[] Right = RightId.split(",");
                        if (Right.length > 1) {
                            if ("menu_id".equals(Right[0].toLowerCase())) {
                                MenuId = Right[0];
                            }
                            if ("button_id".equals(Right[1].toLowerCase())) {
                                ButtonId = Right[1];
                            }
                        }
                    }
                    if (StringUtils.isBlank(RelationalTable) && StringUtils.isBlank(MiddleTable)) {

                        Object oldvalue = oldMapData.get(key);
                        Object newvalue = newMapData.get(key);
                        logDescription = logDescription + "\n" + keyComments + "【" + oldvalue + "】修改为【" + newvalue + "】,";
                    } else {

                        List<String> oldDataList = new ArrayList<>();

                        List<String> newDataList = new ArrayList<>();

                        if (oldMapData.get(key) != null) {
                            if (oldMapData.get(key) instanceof List) {
                                oldDataList = (List<String>) oldMapData.get(key);
                            } else {
                                String name = oldMapData.get(key).toString();
                                oldDataList.add(name);
                            }
                        }
                        if (newMapData.get(key) != null) {
                            if (newMapData.get(key) instanceof List) {
                                newDataList = (List<String>) newMapData.get(key);
                            } else {
                                String name = newMapData.get(key).toString();
                                newDataList.add(name);
                            }
                        }

                        if (oldDataList != null && newDataList != null) {
                            for (int m = oldDataList.size() - 1; m >= 0; m--) {
                                if (oldDataList.get(m) != null) {
                                    String valueKeyOne = oldDataList.get(m).toString();
                                    for (int n = 0; n < newDataList.size(); n++) {
                                        if (newDataList.get(n) != null) {
                                            String valueKeyTwo = newDataList.get(n).toString();
                                            if (valueKeyOne.equals(valueKeyTwo)) {
                                                oldDataList.remove(m);
                                                newDataList.remove(n);
                                                break;
                                            }
                                        }
                                    }

                                }
                            }
                        }
                        if ((StringUtils.isNotBlank(RelationalTable) && StringUtils.isBlank(MiddleTable)) || (StringUtils.isNotBlank(RelationalTable) && StringUtils.isNotBlank(MiddleTable))) {

                            Map<String, Object> params1 = new HashMap<String, Object>();

                            Map<String, Object> params2 = new HashMap<String, Object>();
                            List<Map<String, String>> oldlist = new ArrayList<Map<String, String>>();
                            List<Map<String, String>> newlist = new ArrayList<Map<String, String>>();

                            params1.put("tablename", RelationalTable);
                            params2.put("tablename", RelationalTable);

                            params1.put("fkkeyfield", FKKeyField);
                            params2.put("fkkeyfield", FKKeyField);

                            params1.put("fknamefield", FKNameField);
                            params2.put("fknamefield", FKNameField);

                            params1.put("fieldlist", oldDataList);
                            params2.put("fieldlist", newDataList);

                            if (oldDataList != null && oldDataList.size() > 0) {
                                oldlist = commonSelectFieldConfigMapper.getRelationTableFieldNameList(params1);
                            }

                            if (newDataList != null && newDataList.size() > 0) {
                                newlist = commonSelectFieldConfigMapper.getRelationTableFieldNameList(params2);
                            }
                            if (StringUtils.isNotBlank(RelationalTable) && StringUtils.isBlank(MiddleTable)) {
                                String strstr = "";
                                if (oldlist.size() > 0) {
                                    Map<String, String> maps = oldlist.get(0);
                                    String name = maps.get(FKNameField);
                                    strstr = "修改" + keyComments + "【" + name + "】";
                                } else {
                                    strstr = "修改" + keyComments + "【】";
                                }
                                if (newlist.size() > 0) {
                                    Map<String, String> maps = newlist.get(0);
                                    String name = maps.get(FKNameField);
                                    strstr += "为【" + name + "】";
                                } else {
                                    strstr += "为【】";
                                }
                                middleDescription += strstr;
                            } else {

                                if (oldlist.size() > 0) {
                                    deleteDescription = "删除" + keyComments + ":";
                                    for (int m = 0; m < oldlist.size(); m++) {
                                        Map<String, String> maps = oldlist.get(m);
                                        String name = maps.get(FKNameField);
                                        deleteDescription += "【" + name + "】、";
                                    }
                                    if (!"".equals(deleteDescription)) {
                                        deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
                                    }
                                }
                                if (newlist.size() > 0) {
                                    addDescription = "新增" + keyComments + ":";
                                    for (int n = 0; n < newlist.size(); n++) {
                                        Map<String, String> maps = newlist.get(n);
                                        String name = maps.get(FKNameField);
                                        addDescription += "【" + name + "】、";
                                    }
                                    if (!"".equals(addDescription)) {
                                        addDescription = addDescription.substring(0, addDescription.length() - 1);
                                    }
                                }
                                middleDescription += addDescription + deleteDescription;
                            }

                        } else if (StringUtils.isBlank(RelationalTable) && StringUtils.isBlank(MiddleTable) && LeftId != null && RightId != null && !"".equals(MenuId) && !"".equals(ButtonId)) {
                            String[] oldData = null;
                            String[] newData = null;

                            List<String> menuIdList = new ArrayList<String>();

                            List<String> buttonList = new ArrayList<String>();

                            List<SysMenuVO> menuNamelist = new ArrayList<SysMenuVO>();

                            List<ButtonVO> buttonNameList = new ArrayList<ButtonVO>();

                            Map<String, Object> munuParams = new HashMap<String, Object>();

                            Map<String, Object> buttonParams = new HashMap<String, Object>();

                            List<String> oldMenuKeyList = new ArrayList<String>();

                            List<String> newMenuKeyList = new ArrayList<String>();
                            Map<String, Object> oldMenuMap = new HashMap<String, Object>();
                            Map<String, Object> newMenuMap = new HashMap<String, Object>();
                            if (oldDataList != null && oldDataList.size() > 0) {
                                for (int m = 0; m < oldDataList.size(); m++) {
                                    String valueKeys = oldDataList.get(m).toString();
                                    oldData = valueKeys.split("_");
                                    String oldMenuKey = oldData[0];
                                    if (oldData.length > 1) {
                                        menuIdList.add(oldData[0]);
                                        buttonList.add(oldData[1]);
                                    } else {
                                        menuIdList.add(valueKeys);
                                    }
                                    if (oldMenuKeyList.size() > 0) {
                                        boolean flag = true;
                                        for (int n = 0; n < oldMenuKeyList.size(); n++) {
                                            String menuKey = oldMenuKeyList.get(n);
                                            if (oldMenuKey.equals(menuKey)) {
                                                flag = false;
                                            }
                                        }
                                        if (flag == true) {
                                            oldMenuKeyList.add(oldMenuKey);
                                            String[] oldDataLists = null;
                                            String buttonValue = "";
                                            for (int i = 0; i < oldDataList.size(); i++) {
                                                if (oldDataList.get(i) != null) {
                                                    String valuekey = oldDataList.get(i).toString();
                                                    oldDataLists = valuekey.split("_");
                                                    String menuKey = oldDataLists[0];
                                                    if (oldMenuKey.equals(menuKey)) {
                                                        buttonValue += valuekey + "、";
                                                    }
                                                }
                                            }
                                            if (!"".equals(buttonValue)) {
                                                buttonValue = buttonValue.substring(0, buttonValue.length() - 1);
                                                oldMenuMap.put(oldMenuKey, buttonValue);
                                            }
                                        }

                                    } else {
                                        oldMenuKeyList.add(oldMenuKey);
                                        String[] oldDataLists = null;
                                        String buttonValue = "";
                                        for (int i = 0; i < oldDataList.size(); i++) {
                                            if (oldDataList.get(i) != null) {
                                                String valuekey = oldDataList.get(i).toString();
                                                oldDataLists = valuekey.split("_");
                                                String menuKey = oldDataLists[0];
                                                if (oldMenuKey.equals(menuKey)) {
                                                    buttonValue += valuekey + "、";
                                                }
                                            }
                                        }
                                        if (!"".equals(buttonValue)) {
                                            buttonValue = buttonValue.substring(0, buttonValue.length() - 1);
                                            oldMenuMap.put(oldMenuKey, buttonValue);
                                        }
                                    }
                                }
                            }

                            if (newDataList != null && newDataList.size() > 0) {
                                for (int m = 0; m < newDataList.size(); m++) {
                                    String valueKeys = "";
                                    if (newDataList.get(m) != null) {
                                        valueKeys = newDataList.get(m).toString();
                                    }
                                    newData = valueKeys.split("_");
                                    String newMenuKey = newData[0];
                                    if (newData.length > 1) {
                                        menuIdList.add(newData[0]);
                                        buttonList.add(newData[1]);
                                    } else {
                                        menuIdList.add(valueKeys);
                                    }
                                    if (newMenuKeyList.size() > 0) {
                                        boolean flag = true;
                                        for (int n = 0; n < newMenuKeyList.size(); n++) {
                                            String menuKey = newMenuKeyList.get(n);
                                            if (newMenuKey.equals(menuKey)) {
                                                flag = false;
                                            }
                                        }
                                        if (flag == true) {
                                            newMenuKeyList.add(newMenuKey);
                                            String[] newDataLists = null;
                                            String buttonValue = "";
                                            for (int i = 0; i < newDataList.size(); i++) {
                                                String valueKey = newDataList.get(i).toString();
                                                newDataLists = valueKey.split("_");
                                                String menuKey = newDataLists[0];
                                                if (newMenuKey.equals(menuKey)) {
                                                    buttonValue += valueKey + "、";
                                                }
                                            }
                                            if (!"".equals(buttonValue)) {
                                                buttonValue = buttonValue.substring(0, buttonValue.length() - 1);
                                                newMenuMap.put(newMenuKey, buttonValue);
                                            }
                                        }

                                    } else {
                                        newMenuKeyList.add(newMenuKey);
                                        String[] newDataLists = null;
                                        String buttonValue = "";
                                        for (int i = 0; i < newDataList.size(); i++) {
                                            String valueKey = "";
                                            if (newDataList.get(i) != null) {
                                                valueKey = newDataList.get(i).toString();
                                            }
                                            newDataLists = valueKey.split("_");
                                            String menuKey = newDataLists[0];
                                            if (newMenuKey.equals(menuKey)) {
                                                buttonValue += valueKey + "、";
                                            }
                                        }
                                        if (!"".equals(buttonValue)) {
                                            buttonValue = buttonValue.substring(0, buttonValue.length() - 1);
                                            newMenuMap.put(newMenuKey, buttonValue);
                                        }
                                    }
                                }
                            }

                            HashSet<String> H1 = new HashSet<String>(menuIdList);
                            menuIdList.clear();
                            menuIdList.addAll(H1);

                            HashSet<String> H2 = new HashSet<String>(buttonList);
                            buttonList.clear();
                            buttonList.addAll(H2);

                            if (menuIdList.size() > 0 && menuIdList != null) {
                                munuParams.put("menuidlist", menuIdList);
                                menuNamelist = sysMenuMapper.getMenuNameByMenuid(munuParams);
                            }

                            if (buttonList.size() > 0 && buttonList != null) {
                                buttonParams.put("buttonidlist", buttonList);
                                buttonNameList = buttonMapper.getButtonNameByButtonid(buttonParams);
                            }

                            if (oldMenuMap.size() > 0) {
                                for (Object value : oldMenuMap.values()) {
                                    String[] keyValue = ((String) value).split("、");
                                    String menuString = "";
                                    String buttonString = "";
                                    for (int n = 0; n < keyValue.length; n++) {
                                        String buttonId = keyValue[n];
                                        String[] buttonIdStr = buttonId.split("_");
                                        if (n == 0) {
                                            if (buttonIdStr.length > 1) {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单下";
                                                        break;
                                                    }
                                                }
                                                for (int j = 0; j < buttonNameList.size(); j++) {
                                                    if (buttonNameList.get(j).getButtonId().equals(buttonIdStr[1])) {
                                                        buttonString = "【" + buttonNameList.get(j).getButtonName() + "】、";
                                                        break;
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单及";
                                                        break;
                                                    }
                                                }
                                            }
                                        } else {
                                            if (buttonIdStr.length > 1) {
                                                for (int j = 0; j < buttonNameList.size(); j++) {
                                                    if (buttonNameList.get(j).getButtonId().equals(buttonIdStr[1])) {
                                                        buttonString = buttonString + "【" + buttonNameList.get(j).getButtonName() + "】、";
                                                        break;
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单及";
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!"".equals(buttonString)) {
                                        buttonString = buttonString.substring(0, buttonString.length() - 1);
                                        deleteAuthority += menuString + buttonString + "按钮;";
                                    } else {
                                        if (!"".equals(menuString)) {
                                            menuString = menuString.substring(0, menuString.length() - 1);
                                            deleteAuthority += menuString + ";";
                                        }

                                    }
                                }
                            }
                            if (!"".equals(deleteAuthority)) {
                                deleteAuthority = "删除功能权限" + ":" + deleteAuthority;
                            }

                            if (newMenuMap.size() > 0) {
                                for (Object value : newMenuMap.values()) {
                                    String[] keyValue = ((String) value).split("、");
                                    String menuString = "";
                                    String buttonString = "";
                                    for (int n = 0; n < keyValue.length; n++) {
                                        String buttonId = keyValue[n];
                                        String[] buttonIdStr = buttonId.split("_");
                                        if (n == 0) {
                                            if (buttonIdStr.length > 1) {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单下";
                                                    }
                                                }
                                                for (int j = 0; j < buttonNameList.size(); j++) {
                                                    if (buttonNameList.get(j).getButtonId().equals(buttonIdStr[1])) {
                                                        buttonString = "【" + buttonNameList.get(j).getButtonName() + "】、";
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单及";
                                                    }
                                                }
                                            }
                                        } else {
                                            if (buttonIdStr.length > 1) {
                                                for (int j = 0; j < buttonNameList.size(); j++) {
                                                    if (buttonNameList.get(j).getButtonId().equals(buttonIdStr[1])) {
                                                        buttonString = buttonString + "【" + buttonNameList.get(j).getButtonName() + "】、";
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单及";
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!"".equals(buttonString)) {
                                        buttonString = buttonString.substring(0, buttonString.length() - 1);
                                        addAuthority += menuString + buttonString + "按钮;";
                                    } else {
                                        if (!"".equals(menuString)) {
                                            menuString = menuString.substring(0, menuString.length() - 1);
                                            addAuthority += menuString + ";";
                                        }
                                    }
                                }
                            }
                            if (!"".equals(addAuthority)) {
                                addAuthority = "新增功能权限" + ":" + addAuthority;
                            }

                        }

                    }
                } else if (datas.size() > 0 && datas.get(0).getCustomOptions() != null && !"".equals(datas.get(0).getCustomOptions())) {
                    String customOptions = datas.get(0).getCustomOptions();
                    if (customOptions.length() > 0) {
                        customOptions = customOptions.substring(1);
                    }
                    @SuppressWarnings("rawtypes")
                    Map jsonObject = JSONObject.fromObject(customOptions);

                    String oldValue = "";
                    String newValue = "";
                    if (oldMapData.get(key) == null) {
                        oldValue = "";
                    } else {
                        oldValue = oldMapData.get(key).toString();
                    }
                    if (newMapData.get(key) == null) {
                        newValue = "";
                    } else {
                        newValue = newMapData.get(key).toString();
                    }
                    String valueOne = "";
                    String valueTwo = "";
                    if (jsonObject.size() > 0) {
                        if (!"".equals(oldValue) && jsonObject.get(oldValue) != null) {
                            valueOne = jsonObject.get(oldValue).toString();
                        } else if (!"".equals(oldValue) && jsonObject.get("value") != null) {
                            @SuppressWarnings("rawtypes")
                            Map valueMap = JSONObject.fromObject(jsonObject.get("value").toString());
                            if (valueMap.get(oldValue) != null) {
                                valueOne = valueMap.get(oldValue).toString();
                            }
                        }
                        if (!"".equals(newValue) && jsonObject.get(newValue) != null) {
                            valueTwo = jsonObject.get(newValue).toString();
                        } else if (!"".equals(newValue) && jsonObject.get("value") != null) {
                            @SuppressWarnings("rawtypes")
                            Map valueMap = JSONObject.fromObject(jsonObject.get("value").toString());
                            if (valueMap.get(newValue) != null) {
                                valueTwo = valueMap.get(newValue).toString();
                            }

                        }
                    }
                    if (!valueOne.equals(valueTwo)) {

                        radioDescription = radioDescription + "\n" + keyComments + "【" + valueOne + "】修改为【" + valueTwo + "】,";
                        radioDescription = radioDescription.substring(0, radioDescription.length() - 1);
                    }

                }
            }
        }
        if (!"".equals(logDescription)) {
            logDescription = logDescription.substring(0, logDescription.length() - 1);
        }

        lastDescription = logDescription + "\t" + radioDescription + "\t" + middleDescription + "\t" + addAuthority + "\t" + "" + deleteAuthority;
        return lastDescription;
    }

}

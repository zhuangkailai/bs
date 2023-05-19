package com.tjpu.sp.service.impl.envhousekeepers.checkitemdata;

import com.alibaba.fastjson.JSON;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.envhousekeepers.checkcontentdescription.CheckContentDescriptionMapper;
import com.tjpu.sp.dao.envhousekeepers.checkentinfo.CheckEntInfoMapper;
import com.tjpu.sp.dao.envhousekeepers.checkentinfo.EntCheckFeedbackRecordMapper;
import com.tjpu.sp.dao.envhousekeepers.checkitemdata.CheckItemDataMapper;
import com.tjpu.sp.dao.envhousekeepers.checkproblemexpound.CheckProblemExpoundMapper;
import com.tjpu.sp.dao.envhousekeepers.dataconnection.DataConnectionMapper;
import com.tjpu.sp.dao.environmentalprotection.stopproductioninfo.MessageReadUserMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TaskFlowRecordInfoMapper;
import com.tjpu.sp.model.envhousekeepers.checkentinfo.EntCheckFeedbackRecordVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.service.envhousekeepers.checkitemdata.CheckItemDataService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CheckItemDataServiceImpl implements CheckItemDataService {
    @Autowired
    private CheckItemDataMapper checkItemDataMapper;
    @Autowired
    private CheckEntInfoMapper checkEntInfoMapper;
    @Autowired
    private CheckProblemExpoundMapper checkProblemExpoundMapper;
    @Autowired
    private DataConnectionMapper dataConnectionMapper;
    @Autowired
    private CheckContentDescriptionMapper checkContentDescriptionMapper;
    @Autowired
    private TaskFlowRecordInfoMapper taskFlowRecordInfoMapper;
    @Autowired
    private EntCheckFeedbackRecordMapper entCheckFeedbackRecordMapper;
    @Autowired
    private MessageReadUserMapper messageReadUserMapper;


    /**
     * @author: xsm
     * @date: 2021/06/29 0029 下午 15:37
     * @Description: 根据污染源ID、检查日期、检查类型获取检查项目数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllCheckItemDataByParam(Map<String, Object> param) {
        //获取所有检查项数据
        List<Map<String, Object>> datalist = new ArrayList<>();
        if (param.get("dataflag") != null && "pollution".equals(param.get("dataflag").toString())) {
            datalist = checkItemDataMapper.getAllCheckItemDataByParam(param);
            if (datalist == null || datalist.size() == 0) {
                param.remove("dataflag");
                datalist = checkItemDataMapper.getAllCheckItemDataByParam(param);
            }
        } else {
            datalist = checkItemDataMapper.getAllCheckItemDataByParam(param);
        }
        //根据查询条件获取检查项目记录信息的备注说明和附件信息
        List<Map<String, Object>> filedata = checkItemDataMapper.getRemarkAndFileDataByParam(param);
        //根据查询条件获取检查内容跳转路径数据
        List<Map<String, Object>> urldata = dataConnectionMapper.getCheckContentConnectionDataByParam(param);
        //根据查询条件获取检查内容说明和附件信息
        List<Map<String, Object>> contentdata = checkContentDescriptionMapper.getCheckContentDescriptionFileDataByParam(param);
        //获取上一次检查报告问题
        List<Map<String, Object>> problemlist = checkProblemExpoundMapper.getLastReportProblemByParam(param);
        //根据参数获取企业信息
        Map<String, Object> pollutionmap = checkProblemExpoundMapper.getCheckPollutionInfoByParam(param);
        Map<String, List<Map<String, Object>>> filemap = new HashMap<>();
        Map<String, List<Map<String, Object>>> contentmap = new HashMap<>();
        Map<String, List<Map<String, Object>>> problemmap = new HashMap<>();
        if (filedata != null) {
            filemap = filedata.stream().collect(Collectors.groupingBy(m -> m.get("checkitemdataid").toString()));
        }
        if (contentdata != null) {
            contentmap = contentdata.stream().collect(Collectors.groupingBy(m -> m.get("checkitemdataid").toString()));
        }
        if (problemlist != null) {//按配置项ID分组
            problemmap = problemlist.stream().collect(Collectors.groupingBy(m -> m.get("configid").toString()));
        }
        Map<String, List<Map<String, Object>>> uslmap = new HashMap<>();
        if (urldata != null) {
            //组装地址信息
            if (pollutionmap != null) {
                Object json = AuthUtil.parseJsonKeyToLower("success", pollutionmap);
                Map<String, Object> onemap = JSONObject.fromObject(JSON.toJSONString(json));
                SetCheckContentUrlPath(urldata, onemap);
            }
            uslmap = urldata.stream().collect(Collectors.groupingBy(m -> m.get("configid").toString()));
        }
        List<Map<String, Object>> onelist = new ArrayList<>();
        List<Map<String, Object>> twolist = new ArrayList<>();
        List<Map<String, Object>> threelist = new ArrayList<>();
        for (Map<String, Object> map : datalist) {
            String checkitemid = map.get("checkitemid") != null ? map.get("checkitemid").toString() : "";
            onelist = filemap.get(checkitemid);
            Set<String> fileids = new HashSet<>();
            Set<String> confileids = new HashSet<>();
            List<String> problemids = new ArrayList<>();
            //检查项纪录说明
            if (onelist != null && onelist.size() > 0) {
                List<Map<String, Object>> resultlist = new ArrayList<>();
                for (Map<String, Object> onemap : onelist) {
                    if (onemap.get("FK_FileID") != null && !"".equals(onemap.get("FK_FileID").toString())) {
                        if (!fileids.contains(onemap.get("FK_FileID").toString())) {
                            Map<String, Object> resultmap = new HashMap<>();
                            resultmap.put("FK_FileID", onemap.get("FK_FileID").toString());
                            resultmap.put("Remark", onemap.get("Remark"));
                            List<Map<String, Object>> filelist = new ArrayList<>();
                            for (Map<String, Object> twomap : onelist) {
                                if (twomap.get("FK_FileID") != null && onemap.get("FK_FileID").toString().equals(twomap.get("FK_FileID").toString())) {
                                    Map<String, Object> onefilemap = new HashMap<>();
                                    onefilemap.put("filepath", twomap.get("FilePath"));
                                    onefilemap.put("filename", twomap.get("filename"));
                                    if (twomap.get("FilePath") != null && twomap.get("filename") != null) {
                                        filelist.add(onefilemap);
                                    }
                                }
                            }
                            resultmap.put("filelist", filelist);
                            resultlist.add(resultmap);
                            fileids.add(onemap.get("FK_FileID").toString());
                        } else {
                            continue;
                        }
                    } else {
                        Map<String, Object> resultmap = new HashMap<>();
                        resultmap.put("FK_FileID", "");
                        resultmap.put("Remark", onemap.get("Remark"));
                        resultmap.put("filelist", new ArrayList<>());
                        resultlist.add(resultmap);
                    }
                }
                map.put("remarkdata", resultlist);
            } else {
                map.put("remarkdata", new ArrayList<>());
            }
            //检查内容说明
            twolist = contentmap.get(checkitemid);
            //检查项纪录说明
            if (twolist != null && twolist.size() > 0) {
                List<Map<String, Object>> resultlist = new ArrayList<>();
                for (Map<String, Object> onemap : twolist) {
                    if (onemap.get("FK_FileID") != null && !"".equals(onemap.get("FK_FileID").toString())) {
                        if (!confileids.contains(onemap.get("FK_FileID").toString())) {
                            Map<String, Object> resultmap = new HashMap<>();
                            resultmap.put("FK_FileID", onemap.get("FK_FileID").toString());
                            resultmap.put("Remark", onemap.get("Remark"));
                            List<Map<String, Object>> filelist = new ArrayList<>();
                            for (Map<String, Object> twomap : twolist) {
                                if (twomap.get("FK_FileID") != null && onemap.get("FK_FileID").toString().equals(twomap.get("FK_FileID").toString())) {
                                    Map<String, Object> onefilemap = new HashMap<>();
                                    onefilemap.put("filepath", twomap.get("FilePath"));
                                    onefilemap.put("filename", twomap.get("filename"));
                                    if (twomap.get("FilePath") != null && twomap.get("filename") != null) {
                                        filelist.add(onefilemap);
                                    }
                                }
                            }
                            resultmap.put("filelist", filelist);
                            resultlist.add(resultmap);
                            confileids.add(onemap.get("FK_FileID").toString());
                        } else {
                            continue;
                        }
                    } else {
                        Map<String, Object> resultmap = new HashMap<>();
                        resultmap.put("FK_FileID", "");
                        resultmap.put("Remark", onemap.get("Remark"));
                        resultmap.put("filelist", new ArrayList<>());
                        resultlist.add(resultmap);
                    }
                }
                map.put("contentdata", resultlist);
            } else {
                map.put("contentdata", new ArrayList<>());
            }
            //检查内容地址组装
            String configid = map.get("PK_ID") != null ? map.get("PK_ID").toString() : "";
            if (uslmap.get(configid) != null) {
                map.put("urldata", uslmap.get(configid));
            } else {
                map.put("urldata", new ArrayList<>());
            }
            //判断检查项是否有上次报告检查问题
            if (problemmap.get(configid) != null) {
                threelist = problemmap.get(configid);
                for (Map<String, Object> problem : threelist) {
                    if (problem.get("problemid") != null) {
                        problemids.add(problem.get("problemid").toString());
                    }
                }
                map.put("islastproblem", 1);
            } else {
                map.put("islastproblem", 0);
            }
            map.put("problemids", problemids);
        }
        return datalist;
    }


    /**
     * @author: mmt
     * @date: 2022/08/18
     * @Description: 自定义参数获取多个问题记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getManyCheckProblemExpoundDataByParamMap(Map<String, Object> param) {
        //获取所有检查项数据
        List<Map<String, Object>> datalist = new ArrayList<>();
        datalist = checkItemDataMapper.getManyCheckProblemExpoundDataByParamMap(param);
        //根据查询条件获取检查项目记录信息的备注说明和附件信息
        List<Map<String, Object>> filedata = checkItemDataMapper.getRemarkAndFileDataByParam(param);
        //根据查询条件获取检查内容跳转路径数据
        List<Map<String, Object>> urldata = dataConnectionMapper.getCheckContentConnectionDataByParam(param);
        //根据查询条件获取检查内容说明和附件信息
        List<Map<String, Object>> contentdata = checkContentDescriptionMapper.getCheckContentDescriptionFileDataByParam(param);
        //根据参数获取企业信息
        Map<String, Object> pollutionmap = checkProblemExpoundMapper.getCheckPollutionInfoByParam(param);
        Map<String, List<Map<String, Object>>> filemap = new HashMap<>();
        Map<String, List<Map<String, Object>>> contentmap = new HashMap<>();
        if (filedata != null) {
            filemap = filedata.stream().collect(Collectors.groupingBy(m -> m.get("checkitemdataid").toString()));
        }
        if (contentdata != null) {
            contentmap = contentdata.stream().collect(Collectors.groupingBy(m -> m.get("checkitemdataid").toString()));
        }
        Map<String, List<Map<String, Object>>> uslmap = new HashMap<>();
        if (urldata != null) {
            //组装地址信息
            if (pollutionmap != null) {
                Object json = AuthUtil.parseJsonKeyToLower("success", pollutionmap);
                Map<String, Object> onemap = JSONObject.fromObject(JSON.toJSONString(json));
                SetCheckContentUrlPath(urldata, onemap);
            }
            uslmap = urldata.stream().collect(Collectors.groupingBy(m -> m.get("configid").toString()));
        }
        List<Map<String, Object>> onelist = new ArrayList<>();
        List<Map<String, Object>> twolist = new ArrayList<>();
        for (Map<String, Object> map : datalist) {
            String checkitemid = map.get("checkitemid") != null ? map.get("checkitemid").toString() : "";
            onelist = filemap.get(checkitemid);
            Set<String> fileids = new HashSet<>();
            Set<String> confileids = new HashSet<>();
            //检查项纪录说明
            if (onelist != null && onelist.size() > 0) {
                List<Map<String, Object>> resultlist = new ArrayList<>();
                for (Map<String, Object> onemap : onelist) {
                    if (onemap.get("FK_FileID") != null && !"".equals(onemap.get("FK_FileID").toString())) {
                        if (!fileids.contains(onemap.get("FK_FileID").toString())) {
                            Map<String, Object> resultmap = new HashMap<>();
                            resultmap.put("FK_FileID", onemap.get("FK_FileID").toString());
                            resultmap.put("Remark", onemap.get("Remark"));
                            List<Map<String, Object>> filelist = new ArrayList<>();
                            for (Map<String, Object> twomap : onelist) {
                                if (twomap.get("FK_FileID") != null && onemap.get("FK_FileID").toString().equals(twomap.get("FK_FileID").toString())) {
                                    Map<String, Object> onefilemap = new HashMap<>();
                                    onefilemap.put("filepath", twomap.get("FilePath"));
                                    onefilemap.put("filename", twomap.get("filename"));
                                    if (twomap.get("FilePath") != null && twomap.get("filename") != null) {
                                        filelist.add(onefilemap);
                                    }
                                }
                            }
                            resultmap.put("filelist", filelist);
                            resultlist.add(resultmap);
                            fileids.add(onemap.get("FK_FileID").toString());
                        } else {
                            continue;
                        }
                    } else {
                        Map<String, Object> resultmap = new HashMap<>();
                        resultmap.put("FK_FileID", "");
                        resultmap.put("Remark", onemap.get("Remark"));
                        resultmap.put("filelist", new ArrayList<>());
                        resultlist.add(resultmap);
                    }
                }
                map.put("remarkdata", resultlist);
            } else {
                map.put("remarkdata", new ArrayList<>());
            }
            //检查内容说明
            twolist = contentmap.get(checkitemid);
            //检查项纪录说明
            if (twolist != null && twolist.size() > 0) {
                List<Map<String, Object>> resultlist = new ArrayList<>();
                for (Map<String, Object> onemap : twolist) {
                    if (onemap.get("FK_FileID") != null && !"".equals(onemap.get("FK_FileID").toString())) {
                        if (!confileids.contains(onemap.get("FK_FileID").toString())) {
                            Map<String, Object> resultmap = new HashMap<>();
                            resultmap.put("FK_FileID", onemap.get("FK_FileID").toString());
                            resultmap.put("Remark", onemap.get("Remark"));
                            List<Map<String, Object>> filelist = new ArrayList<>();
                            for (Map<String, Object> twomap : twolist) {
                                if (twomap.get("FK_FileID") != null && onemap.get("FK_FileID").toString().equals(twomap.get("FK_FileID").toString())) {
                                    Map<String, Object> onefilemap = new HashMap<>();
                                    onefilemap.put("filepath", twomap.get("FilePath"));
                                    onefilemap.put("filename", twomap.get("filename"));
                                    if (twomap.get("FilePath") != null && twomap.get("filename") != null) {
                                        filelist.add(onefilemap);
                                    }
                                }
                            }
                            resultmap.put("filelist", filelist);
                            resultlist.add(resultmap);
                            confileids.add(onemap.get("FK_FileID").toString());
                        } else {
                            continue;
                        }
                    } else {
                        Map<String, Object> resultmap = new HashMap<>();
                        resultmap.put("FK_FileID", "");
                        resultmap.put("Remark", onemap.get("Remark"));
                        resultmap.put("filelist", new ArrayList<>());
                        resultlist.add(resultmap);
                    }
                }
                map.put("contentdata", resultlist);
            } else {
                map.put("contentdata", new ArrayList<>());
            }
            //检查内容地址组装
            String configid = map.get("PK_ID") != null ? map.get("PK_ID").toString() : "";
            if (uslmap.get(configid) != null) {
                map.put("urldata", uslmap.get(configid));
            } else {
                map.put("urldata", new ArrayList<>());
            }
        }
        return datalist;
    }

    private void SetCheckContentUrlPath(List<Map<String, Object>> usldata, Map<String, Object> objmap) {
        Map<String, Object> onemap = (Map<String, Object>) objmap.get("data");
        List<String> keys = CommonTypeEnum.getCheckContentUrlKeyList();
        for (Map<String, Object> map : usldata) {
            String url = map.get("Url") != null ? map.get("Url").toString() : "";
            if (!"".equals(url)) {
                for (String key : keys) {
                    if (onemap.get(key) != null) {
                        url = url.replaceAll("\\{" + key + "\\}", onemap.get(key).toString());
                    }
                }
            }
            map.put("Url", url);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/01 0001 下午 17:22
     * @Description: 根据污染源ID、检查日期、检查类型获取检查企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getOneCheckEntInfoByParam(Map<String, Object> param) {
        return checkEntInfoMapper.getOneCheckEntInfoByParam(param);
    }

    /**
     * @author: xsm
     * @date: 2021/07/07 0007 上午 10:11
     * @Description: 根据污染源ID、检查日期更新该企业该日期所有检查报告的问题状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public int updateAllCheckProblemExpoundStatusByParam(Map<String, Object> param) {
        try {
            int i = 1;
            checkProblemExpoundMapper.updateAllCheckProblemExpoundStatusByParam(param);
            checkEntInfoMapper.updatecheckEntInfoStatusByParam(param);
            List<Map<String, Object>> listdata = checkProblemExpoundMapper.getOneCheckProbleReportDataByParamMap(param);
            if (listdata != null && listdata.size() > 0) {
                List<TaskFlowRecordInfoVO> objlist = new ArrayList<>();
                Date thedate = new Date();
                for (Map<String, Object> map : listdata) {
                    String problemid = map.get("pkid") != null ? map.get("pkid").toString() : "";
                    if (!"".equals(problemid)) {
                        TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
                        obj.setPkId(UUID.randomUUID().toString());//主键ID
                        obj.setFkTaskid(problemid);//任务ID
                        obj.setCurrenttaskstatus(CommonTypeEnum.ProblemProcedureRecordStatusEnum.RectifiedEnum.getName().toString());//任务状态
                        obj.setFkTasktype(CommonTypeEnum.TaskTypeEnum.CheckProblemExpoundEnum.getCode().toString());//任务类型
                        obj.setTaskhandletime(thedate);//被分派该任务的时间
                        objlist.add(obj);
                    }
                }
                if (objlist.size() > 0) {
                    taskFlowRecordInfoMapper.batchInsert(objlist);
                }
            }
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> SetCheckTemplateConfigUrlPath(List<Map<String, Object>> listdata, Map<String, Object> param) {
        //根据查询条件获取检查内容跳转路径数据
        List<Map<String, Object>> urldata = dataConnectionMapper.getCheckContentConnectionDataByParam(param);
        Map<String, List<Map<String, Object>>> uslmap = new HashMap<>();
        if (urldata != null) {
            //组装地址信息
            uslmap = urldata.stream().collect(Collectors.groupingBy(m -> m.get("configid").toString()));
        }
        for (Map<String, Object> map : listdata) {
            //检查内容地址组装
            String configid = map.get("PK_ID") != null ? map.get("PK_ID").toString() : "";
            if (uslmap.get(configid) != null) {
                map.put("urldata", uslmap.get(configid));
            } else {
                map.put("urldata", new ArrayList<>());
            }
        }
        return listdata;
    }

    @Override
    public void updateEntCheckFeedbackData(Map<String, Object> param) {
        EntCheckFeedbackRecordVO obj = entCheckFeedbackRecordMapper.selectByPollutionidAndCheckTime(param);
        if (obj != null) {
            obj.setIsupdate((short) 1);
            obj.setUpdatetime(new Date());
            entCheckFeedbackRecordMapper.updateByPrimaryKey(obj);
            //更新已读数据为未读
            messageReadUserMapper.deleteByRecordID(obj.getPkId());
        }
    }

}

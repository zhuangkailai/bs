package com.tjpu.sp.controller.envhousekeepers.checkproblemexpound;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.FileController;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.CheckProblemExpoundVO;
import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.RectifiedAndReviewRecordVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jsoup.select.Collector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xsm
 * @date: 2021/07/07 0007 上午 11:00
 * @Description:检查问题说明信息
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("checkProblemExpound")
public class CheckProblemExpoundController {

    @Autowired
    private CheckProblemExpoundService checkProblemExpoundService;
    @Autowired
    private FileController fileController;

    private final String all_problem = "all_problem";

    /**
     * @author: xsm
     * @date: 2021/07/07 0007 上午 11:15
     * @Description: 通过自定义参数获取检查问题说明信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getCheckProblemExpoundsByParamMap", method = RequestMethod.POST)
    public Object getCheckProblemExpoundsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {


            Map<String, Object> jsonObject = (Map) paramsJson;


            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<String> rightList = getRightList(userid);
            if (jsonObject.get("isshowall") == null) {
                if (!rightList.contains(all_problem)) {
                    jsonObject.put("userid", userid);
                }
            }
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> listdata = checkProblemExpoundService.getCheckProblemExpoundsByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(listdata);
            long total = pageInfo.getTotal();
            List<String> ids = listdata.stream().filter(m -> m.get("FK_FileID") != null).map(m -> m.get("FK_FileID").toString()).distinct().collect(Collectors.toList());
            Map<String, Object> param = new HashMap<>();
            param.put("fileflags", ids);
            Map<String, List<Map<String, Object>>> filedatas = checkProblemExpoundService.getFileDataByFileFlags(param);
            Date termTime;
            Date nowDay = DataFormatUtil.getDateYMD(DataFormatUtil.getDateYMD(new Date()));
            boolean isOverTime;
            for (Map<String, Object> map : listdata) {
                if (map.get("FK_FileID") != null && filedatas != null && filedatas.get(map.get("FK_FileID").toString()) != null) {
                    map.put("filedata", filedatas.get(map.get("FK_FileID").toString()));
                } else {
                    map.put("filedata", new ArrayList<>());
                }
                isOverTime = false;
                //判断是否过期
                if (map.get("RectificationTermTime") != null) {
                    termTime = DataFormatUtil.getDateYMD(map.get("RectificationTermTime").toString());
                    if (termTime.before(nowDay)) {
                        isOverTime = true;
                    }
                }
                map.put("isOverTime", isOverTime);
            }
            resultMap.put("datalist", listdata);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<String> getRightList(String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("moduletype", CommonTypeEnum.ModuleTypeEnum.ProblemEnum.getCode());
        List<String> rightList = checkProblemExpoundService.getUserModuleByParam(paramMap);
        return rightList;
    }

    /**
     * @author: xsm
     * @date: 2021/07/07 0007 下午 3:55
     * @Description: 通过id获取问题记录详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getCheckProblemExpoundDetailByID", method = RequestMethod.POST)
    public Object getCheckProblemExpoundDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = checkProblemExpoundService.getCheckProblemExpoundDetailByID(id);
            //获取最新的整改和复查记录记录
            Map<String, Object> paramtMap = new HashMap<>();
            paramtMap.put("checkproblemexpoundid", id);
            //整改
            paramtMap.put("managementtype", 1);
            Map<String, Object> zg_map = checkProblemExpoundService.getLastRectifiedAndReviewRecordByParamMap(paramtMap);
            //复查
            paramtMap.put("managementtype", 2);
            Map<String, Object> fc_map = checkProblemExpoundService.getLastRectifiedAndReviewRecordByParamMap(paramtMap);
            result.put("zgdata", zg_map);
            result.put("fcdata", fc_map);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/08 0008 上午 8:45
     * @Description: 通过id获取问题记录流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getCheckProblemExpoundProcedureByID", method = RequestMethod.POST)
    public Object getCheckProblemExpoundProcedureByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            List<Map<String, Object>> result = checkProblemExpoundService.getCheckProblemExpoundProcedureByID(id);
            resultmap.put("proceduredata", result);
            List<Map<String, Object>> cz_list = checkProblemExpoundService.countEntProblemRectifyReportNumByID(id);
            Boolean zg_ishistory = false;
            Boolean fc_ishistory = false;
            if (cz_list != null && cz_list.size() > 0) {
                for (Map<String, Object> map : cz_list) {
                    if (map.get("ManagementType") != null) {
                        if ("1".equals(map.get("ManagementType").toString())) {
                            if (map.get("num") != null && Integer.valueOf(map.get("num").toString()) > 1) {
                                zg_ishistory = true;
                            }
                        }
                        if ("2".equals(map.get("ManagementType").toString())) {
                            if (map.get("num") != null && Integer.valueOf(map.get("num").toString()) > 1) {
                                fc_ishistory = true;
                            }
                        }
                    }
                }
            }
            resultmap.put("zg_ishistory", zg_ishistory);
            resultmap.put("fc_ishistory", fc_ishistory);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 13:56
     * @Description: 通过企业ID和检查日期导出检查问题报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/exportCheckProblemExpoundReport", method = RequestMethod.POST)
    public void exportCheckProblemExpoundReport(@RequestJson(value = "pollutionid") String pollutionid,
                                                @RequestJson(value = "checktime") String checktime,
                                                @RequestJson(value = "checktypecode") String checktypecode,
                                                @RequestJson(value = "pollutionname") String pollutionname,
                                                HttpServletRequest request,
                                                HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("pollutionid", pollutionid);
            parammap.put("checktime", checktime);
            //parammap.put("checktypecode",checktypecode);
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> listdata = checkProblemExpoundService.getOneCheckProbleReportDataByParamMap(parammap);
            String titlename = "";
            List<Map<String, Object>> resultlist = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                List<String> fileids = listdata.stream().filter(m -> m.get("FK_FileID") != null).map(m -> m.get("FK_FileID").toString()).collect(Collectors.toList());
                JSONArray data2 = new JSONArray();
                if (fileids != null && fileids.size() > 0) {
                    parammap.clear();
                    parammap.put("fileflags", fileids);
                    List<Map<String, Object>> filedatas = checkProblemExpoundService.getCheckProblemFileDataByFileFlags(parammap);
                    if (filedatas != null) {
                        List<String> imgfileids = new ArrayList<>();
                        for (Map<String, Object> filemap : filedatas) {
                            imgfileids.add(filemap.get("filepath").toString());
                        }
                        if (imgfileids.size() > 0) {
                            Object data = fileController.getFilesInfosByParams(imgfileids);
                            data = AuthUtil.decryptData(data);
                            JSONObject jsonObject3 = JSONObject.fromObject(data);
                            data2 = JSONArray.fromObject(jsonObject3.get("data"));
                        }
                    }
                }

               /* for (Map<String, Object> twomap:listdata){
                    //将图片数据set进去
                    String fileid = twomap.get("FK_FileID")!=null?twomap.get("FK_FileID").toString():"";
                    List<Map<String,Object>> imgdata = new ArrayList<>();
                    if (data2.size() > 0) {
                        for (int i=0;i< data2.size();i++){
                            JSONObject o = JSONObject.fromObject(data2.get(i));
                            if (o.get("fileflag")!=null&&fileid.equals(o.get("fileflag"))){
                                Map<String,Object> onemap = new HashMap<>();
                                onemap.put("imgsrc",o.get("base64"));
                                onemap.put("fileid",o.get("fileflag"));
                                onemap.put("fileextname",o.get("fileextname")!=null?(o.get("fileextname").toString()).toLowerCase():"");
                                onemap.put("imageindex",i);
                                imgdata.add(onemap);
                            }
                        }
                    }
                    twomap.put("imagedata",imgdata);
                    if (twomap.get("textcontent")!=null){
                        String textcontent = twomap.get("textcontent").toString();
                        String regex = "<(?!p|\\/p)[^>]+>";
                        textcontent = textcontent.replaceAll(regex, "");//替换所有非<p></p>标签为“”
                        textcontent = textcontent.replaceAll("<p>", "");//替换所有<p>标签为“”
                        textcontent = textcontent.replaceAll("</p>", "<w:br/>");//替换所有<p>标签为 <w:br/>
                        twomap.put("textcontent",textcontent);
                    }

                }*/
                Map<String, List<Map<String, Object>>> categorydata = listdata.stream().collect(Collectors.groupingBy(m -> m.get("inspectypename").toString()));
                int j = 1;
                for (Map.Entry<String, List<Map<String, Object>>> entry : categorydata.entrySet()) {
                    Map<String, Object> themap = new HashMap<>();
                    themap.put("checkcategory", entry.getKey());
                    List<Map<String, Object>> onelist = entry.getValue();
                    int m = 1;
                    for (Map<String, Object> twomap : onelist) {
                        //将图片数据set进去
                        String fileid = twomap.get("FK_FileID") != null ? twomap.get("FK_FileID").toString() : "";
                        List<Map<String, Object>> imgdata = new ArrayList<>();
                        if (data2.size() > 0) {
                            for (int i = 0; i < data2.size(); i++) {
                                JSONObject o = JSONObject.fromObject(data2.get(i));
                                if (o.get("fileflag") != null && fileid.equals(o.get("fileflag"))) {
                                    Map<String, Object> onemap = new HashMap<>();
                                    onemap.put("imgsrc", o.get("base64"));
                                    onemap.put("fileid", o.get("fileflag"));
                                    onemap.put("fileextname", o.get("fileextname") != null ? (o.get("fileextname").toString()).toLowerCase() : "");
                                    onemap.put("imageindex", i);
                                    imgdata.add(onemap);
                                }
                            }
                        }
                        if (imgdata.size() > 0) {
                            twomap.put("imagedata", imgdata);
                        }
                        if (twomap.get("textcontent") != null) {
                            String textcontent = twomap.get("textcontent").toString();
                            String regex = "<(?!p|\\/p)[^>]+>";
                            textcontent = textcontent.replaceAll(regex, "");//替换所有非<p></p>标签为“”
                            textcontent = textcontent.replaceAll("<p>", "");//替换所有<p>标签为“”
                            textcontent = textcontent.replaceAll("</p>", "<w:br/>");//替换所有<p>标签为 <w:br/>
                            twomap.put("textcontent", textcontent);
                        }
                        twomap.put("problemindex", m);
                        m += 1;
                    }
                    themap.put("problemlist", onelist);
                    themap.put("categoryindex", j);
                    resultlist.add(themap);
                    j++;
                }

                titlename = listdata.get(0).get("TableTitle").toString();
            }
            result.put("pollutionname", pollutionname);
            result.put("problemdata", resultlist);
            //文件名称
            String fileName = titlename + "检查问题记录" + new Date().getTime() + ".doc";
            byte[] fileBytes = FreeMarkerWordUtil.createWord(result, "templates/检查问题模板.ftl");
            ExcelUtil.downLoadFile(fileName, response, request, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/31 0031 13:30
     * @Description: 添加检查问题信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     **/
    @RequestMapping(value = "addCheckProblemExpound", method = RequestMethod.POST)
    public Object addCheckProblemExpound(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            CheckProblemExpoundVO oneobj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new CheckProblemExpoundVO());
            oneobj.setPkId(UUID.randomUUID().toString());
            oneobj.setUpdatetime(new Date());
            oneobj.setUpdateuser(username);
            oneobj.setEnteredby(userid);
            checkProblemExpoundService.addCheckProblemExpound(oneobj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/31 0031 13:30
     * @Description: 修改检查问题信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     **/
    @RequestMapping(value = "updateCheckProblemExpound", method = RequestMethod.POST)
    public Object updateCheckProblemExpound(@RequestJson(value = "updateformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            CheckProblemExpoundVO oneobj = JSONObjectUtil.JsonObjectToEntity(jsonObject, new CheckProblemExpoundVO());
            oneobj.setUpdatetime(new Date());
            oneobj.setUpdateuser(username);
            checkProblemExpoundService.updateCheckProblemExpound(oneobj);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/31 0031 13:43
     * @Description: 通过主键id删除问题单条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     **/
    @RequestMapping(value = "deleteCheckProblemExpoundById", method = RequestMethod.POST)
    public Object deleteCheckProblemExpoundById(@RequestJson(value = "id", required = true) String id) {
        try {
            checkProblemExpoundService.deleteCheckProblemExpoundById(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/03 0003 下午 4:12
     * @Description: 通过id获取问题记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getCheckProblemExpoundDataByID", method = RequestMethod.POST)
    public Object getCheckProblemExpoundDataByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = checkProblemExpoundService.getCheckProblemExpoundDataByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/03 0003 下午 17:09
     * @Description: 根据主键ID更新该问题状态(提交)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateOneCheckProblemExpoundStatusByParam", method = RequestMethod.POST)
    public Object updateOneCheckProblemExpoundStatusByParam(@RequestJson(value = "id") String id) throws Exception {
        try {
            CheckProblemExpoundVO obj = checkProblemExpoundService.selectCheckProblemExpoundByID(id);
            if (obj != null) {
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                obj.setStatus((short) 1);
                obj.setUpdatetime(new Date());
                obj.setUpdateuser(username);
                checkProblemExpoundService.updateCheckProblemExpound(obj);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/04 0004 下午 14:12
     * @Description: 自定义参数获取多个问题记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getManyCheckProblemExpoundDataByParamMap", method = RequestMethod.POST)
    public Object getManyCheckProblemExpoundDataByParamMap(@RequestJson(value = "problemids") List<String> problemids) throws Exception {
        try {
            Map<String, Object> paramtMap = new HashMap<>();
            paramtMap.put("problemids", problemids);
            List<Map<String, Object>> result = checkProblemExpoundService.getManyCheckProblemExpoundDataByParamMap(paramtMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/04 0004 下午 16:21
     * @Description: 根据问题id和处置类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getHistoryDisposalDataByParamMap", method = RequestMethod.POST)
    public Object getHistoryDisposalDataByParamMap(@RequestJson(value = "problemid") String problemid,
                                                   @RequestJson(value = "managementtype") Integer managementtype) throws Exception {
        try {
            Map<String, Object> paramtMap = new HashMap<>();
            paramtMap.put("problemid", problemid);
            paramtMap.put("managementtype", managementtype);
            List<Map<String, Object>> result = checkProblemExpoundService.getHistoryDisposalDataByParamMap(paramtMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 17:13
     * @Description: 复查并更新问题状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/updateProblemExpoundDataForReview", method = RequestMethod.POST)
    public Object updateProblemExpoundDataForReview(@RequestJson(value = "formdata") Object formdata, HttpSession session) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(formdata);
            if (jsonObject.get("ispass") != null && jsonObject.get("problemid") != null) {
                //判断 复查是否通过还是驳回
                Boolean ispass = jsonObject.getBoolean("ispass");
                String problemid = jsonObject.getString("problemid");//问题主键
                //问题状态
                int status;
                if (ispass) {//通过
                    status = CommonTypeEnum.ProblemManagementStatusEnum.CompletedEnum.getCode();
                } else {//驳回
                    status = CommonTypeEnum.ProblemManagementStatusEnum.RectifiedEnum.getCode();
                }
                //更新问题状态
                CheckProblemExpoundVO obj = checkProblemExpoundService.selectCheckProblemExpoundByID(problemid);
                obj.setStatus((short) status);
                //问题处置情况
                RectifiedAndReviewRecordVO objone = JSONObjectUtil.JsonObjectToEntity(jsonObject, new RectifiedAndReviewRecordVO());
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                objone.setUpdatetime(new Date());
                objone.setUpdateuser(username);
                objone.setFkCheckproblemexpoundid(problemid);
                objone.setPkId(UUID.randomUUID().toString());
                //流程信息
                List<TaskFlowRecordInfoVO> objlist = new ArrayList<>();
                Calendar calendar = Calendar.getInstance();
                TaskFlowRecordInfoVO objtwo = new TaskFlowRecordInfoVO();
                //String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                objtwo.setPkId(UUID.randomUUID().toString());//主键ID
                objtwo.setFkTaskid(problemid);//任务ID
                //objtwo.setFkTaskhandleuserid();//被分派该任务的处置人ID
                if (ispass) {//通过
                    objtwo.setCurrenttaskstatus(CommonTypeEnum.ProblemProcedureRecordStatusEnum.CompletedEnum.getName().toString());//任务状态
                } else {
                    objtwo.setCurrenttaskstatus(CommonTypeEnum.ProblemProcedureRecordStatusEnum.RectifiedRejectEnum.getName().toString());//任务状态
                }
                objtwo.setFkTasktype(CommonTypeEnum.TaskTypeEnum.CheckProblemExpoundEnum.getCode().toString());//任务类型
                objtwo.setTaskhandletime(calendar.getTime());//被分派该任务的时间
                objlist.add(objtwo);
                if (ispass == false) {//未通过 驳回后 再整改 多存一条 二次待整改流程记录
                    TaskFlowRecordInfoVO objthree = new TaskFlowRecordInfoVO();
                    //String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                    objthree.setPkId(UUID.randomUUID().toString());//主键ID
                    objthree.setFkTaskid(problemid);//任务ID
                    //objtwo.setFkTaskhandleuserid();//被分派该任务的处置人ID
                    objthree.setCurrenttaskstatus(CommonTypeEnum.ProblemProcedureRecordStatusEnum.RectifiedEnum.getName().toString());//任务状态
                    objthree.setFkTasktype(CommonTypeEnum.TaskTypeEnum.CheckProblemExpoundEnum.getCode().toString());//任务类型
                    //在当前时间的基础上添加一秒
                    calendar.add(Calendar.SECOND, 1);
                    objthree.setTaskhandletime(calendar.getTime());//被分派该任务的时间
                    objlist.add(objthree);
                }
                checkProblemExpoundService.updateProblemExpoundDataForReview(obj, objone, objlist);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 18:22
     * @Description: 整改并更新问题状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/updateProblemExpoundDataForRectified", method = RequestMethod.POST)
    public Object updateProblemExpoundDataForRectified(@RequestJson(value = "formdata") Object formdata, HttpSession session) throws Exception {
        try {

            JSONObject jsonObject = JSONObject.fromObject(formdata);
            if (jsonObject.get("problemid") != null) {
                String problemid = jsonObject.getString("problemid");//问题主键
                //问题状态
                int status = CommonTypeEnum.ProblemManagementStatusEnum.ReviewEnum.getCode();
                //更新问题状态
                CheckProblemExpoundVO obj = checkProblemExpoundService.selectCheckProblemExpoundByID(problemid);
                obj.setStatus((short) status);
                //问题处置情况
                RectifiedAndReviewRecordVO objone = JSONObjectUtil.JsonObjectToEntity(jsonObject, new RectifiedAndReviewRecordVO());
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                objone.setUpdatetime(new Date());
                objone.setUpdateuser(username);
                objone.setFkCheckproblemexpoundid(problemid);
                objone.setPkId(UUID.randomUUID().toString());
                //流程信息
                List<TaskFlowRecordInfoVO> objlist = new ArrayList<>();
                TaskFlowRecordInfoVO objtwo = new TaskFlowRecordInfoVO();
                //String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                objtwo.setPkId(UUID.randomUUID().toString());//主键ID
                objtwo.setFkTaskid(problemid);//任务ID
                //objtwo.setFkTaskhandleuserid();//被分派该任务的处置人ID
                objtwo.setCurrenttaskstatus(CommonTypeEnum.ProblemProcedureRecordStatusEnum.ReviewEnum.getName().toString());//任务状态
                objtwo.setFkTasktype(CommonTypeEnum.TaskTypeEnum.CheckProblemExpoundEnum.getCode().toString());//任务类型
                objtwo.setTaskhandletime(new Date());//被分派该任务的时间
                objlist.add(objtwo);
                checkProblemExpoundService.updateProblemExpoundDataForReview(obj, objone, objlist);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/01 0001 下午 16:35
     * @Description: 根据企业ID和检查类别获取问题信息(当前年)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getCurrentYearEntCheckProblemDataByParamMap", method = RequestMethod.POST)
    public Object getCurrentYearEntCheckProblemDataByParamMap(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                              @RequestJson(value = "fkcheckcategorydataid", required = false) String fkcheckcategorydataid,
                                                              @RequestJson(value = "checkcategoryname", required = false) String checkcategoryname,
                                                              @RequestJson(value = "statuslist", required = false) List<Integer> statuslist,
                                                              @RequestJson(value = "titletype", required = false) Integer titletype,
                                                              @RequestJson(value = "starttime", required = false) String starttime,
                                                              @RequestJson(value = "endtime", required = false) String endtime,
                                                              @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                              @RequestJson(value = "pagenum", required = false) Integer pagenum) throws Exception {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            Map<String, Object> param = new HashMap<>();
            param.put("pollutionid", pollutionid);
            param.put("fkcheckcategorydataid", fkcheckcategorydataid);
            param.put("titletype", titletype);
            param.put("checkcategoryname", checkcategoryname);
            if (statuslist != null && statuslist.size() > 0) {
                param.put("statuslist", statuslist);
            }
            Date nowTime = new Date();
            if (starttime != null && endtime != null) {
                param.put("starttime", starttime);
                param.put("endtime", endtime);
            } else {
                String year = DataFormatUtil.getDateY(nowTime);//当前年
                param.put("yeardate", year);
            }
            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> datalist = checkProblemExpoundService.getCurrentYearEntCheckProblemDataByParamMap(param);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultmap.put("datalist", datalist);
            resultmap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 监督检查问题分类统计
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/18 9:00
     */
    @RequestMapping(value = "/countProblemForType", method = RequestMethod.POST)
    public Object countProblemForType(
            @RequestJson(value = "year") String year,
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "checkcategoryids", required = false) List<String> checkcategoryids

    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("year", year);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("checkcategoryids", checkcategoryids);
            List<Map<String, Object>> resultList = checkProblemExpoundService.countProblemForType(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 按照月份监督检查问题整改统计
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/18 9:00
     */
    @RequestMapping(value = "/countProblemForMonth", method = RequestMethod.POST)
    public Object countProblemForMonth(@RequestJson(value = "year") String year,
                                       @RequestJson(value = "checkcategoryids", required = false) List<String> checkcategoryids
    ) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("year", year);
            paramMap.put("checkcategoryids", checkcategoryids);
            List<Map<String, Object>> dataList = checkProblemExpoundService.getMonthProblemByParam(paramMap);
            Map<String, Map<String, Object>> monthAndMap = new HashMap<>();

            if (dataList.size() > 0) {
                Map<String, List<Map<String, Object>>> monthAndList = dataList.stream().collect(Collectors.groupingBy(m -> m.get("checktime").toString()));

                long totalNum = 0;
                long completeNum = 0;
                for (String month : monthAndList.keySet()) {
                    dataList = monthAndList.get(month);
                    totalNum = dataList.size();
                    completeNum = dataList.stream().filter(m -> m.get("status") != null && Integer.parseInt(m.get("status").toString()) > 1).count();
                    Map<String, Object> map = new HashMap<>();
                    map.put("totalNum", totalNum);
                    map.put("completeNum", completeNum);
                    monthAndMap.put(month, map);
                }
            }

            List<String> monthList = DataFormatUtil.getMonthByYear(year);
            for (String month : monthList) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("month", month);
                if (monthAndMap.get(month) != null) {
                    dataMap.putAll(monthAndMap.get(month));
                } else {
                    dataMap.put("totalNum", 0);
                    dataMap.put("completeNum", 0);
                }
                resultList.add(dataMap);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取企业检查问题统计数据（问题数、整改完成数、及时整改数）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/11/03 9:00
     */
    @RequestMapping(value = "/getEntCheckProblemCountByParam", method = RequestMethod.POST)
    public Object getEntCheckProblemCountByParam(@RequestJson(value = "paramjson") Object paramjson) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = (Map<String, Object>) paramjson;
            List<Map<String, Object>> dataList = checkProblemExpoundService.getDataListByParamMap(paramMap);
            if (dataList.size() > 0) {
                Map<String, List<Map<String, Object>>> id_nameAndDataList = dataList.stream()
                        .collect(Collectors.groupingBy(m -> m.get("pk_pollutionid") + "," + m.get("pollutionname") + "," + m.get("shortername")));
                int problemNum;
                int totalNum = dataList.size();
                for (String id_name : id_nameAndDataList.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("pollutionid", id_name.split(",")[0]);
                    resultMap.put("pollutionname", id_name.split(",")[1]);
                    resultMap.put("shortername", id_name.split(",")[2]);
                    Map<String, Object> subMap = getDataMap(id_nameAndDataList.get(id_name));
                    resultMap.putAll(subMap);
                    problemNum = Integer.parseInt(subMap.get("problemnum").toString());
                    resultMap.put("problemrate",DataFormatUtil.SaveOneAndSubZero(problemNum*100D/totalNum));
                    resultList.add(resultMap);
                }
                //排序
                resultList = resultList.stream().sorted(
                        Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("problemnum").toString())).reversed()).collect(Collectors.toList());

            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @Description: 获取检查问题类型统计数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/11/03 9:00 DataFormatUtil.FormatDateOneToOther(m.get("checktime").toString(),"yyyy-MM-dd","yyyy-MM")
     */
    @RequestMapping(value = "/getCheckProblemTypeCountByParam", method = RequestMethod.POST)
    public Object getCheckProblemTypeCountByParam(@RequestJson(value = "paramjson") Object paramjson) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = (Map<String, Object>) paramjson;
            List<Map<String, Object>> dataList = checkProblemExpoundService.getDataListByParamMap(paramMap);
            if (dataList.size() > 0) {
                Map<String, List<Map<String, Object>>> id_nameAndDataList = dataList.stream()
                        .collect(Collectors.groupingBy(m -> m.get("checkcategoryid") + "," + m.get("checkcategoryname")));
                int problemNum;
                int totalNum = dataList.size();
                for (String id_name : id_nameAndDataList.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("checkcategoryid", id_name.split(",")[0]);
                    resultMap.put("checkcategoryname", id_name.split(",")[1]);
                    Map<String, Object> subMap = getDataMap(id_nameAndDataList.get(id_name));
                    resultMap.putAll(subMap);
                    problemNum = Integer.parseInt(subMap.get("problemnum").toString());
                    resultMap.put("problemrate",DataFormatUtil.SaveOneAndSubZero(problemNum*100D/totalNum));
                    resultList.add(resultMap);
                }
                //排序
                resultList = resultList.stream().sorted(
                        Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("problemnum").toString())).reversed()).collect(Collectors.toList());

            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @Description: 获取检查问题类型统计数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/11/03 9:00
     */
    @RequestMapping(value = "/getCheckProblemTimeCountByParam", method = RequestMethod.POST)
    public Object getCheckProblemTimeCountByParam(@RequestJson(value = "paramjson") Object paramjson) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = (Map<String, Object>) paramjson;
            List<Map<String, Object>> dataList = checkProblemExpoundService.getDataListByParamMap(paramMap);
            if (dataList.size() > 0) {
                Map<String, List<Map<String, Object>>> timeAndDataList = dataList.stream()
                        .collect(Collectors.groupingBy(m ->
                                DataFormatUtil.FormatDateOneToOther(m.get("checktime").toString(),"yyyy-MM-dd","yyyy-MM")
                        ));
                for (String time : timeAndDataList.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("time", time);
                    Map<String, Object> subMap = getDataMap(timeAndDataList.get(time));
                    resultMap.putAll(subMap);
                    resultList.add(resultMap);
                }
                //排序
                resultList = resultList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("time").toString())).collect(Collectors.toList());

            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private Map<String, Object> getDataMap(List<Map<String, Object>> dataList) {
        Map<String, Object> resultMap = new HashMap<>();
        int problemnum = dataList.size();
        int jszgnum = 0;
        String jszgrate = "";
        int zgwcnum = 0;
        String zgwcrate = "";
        String typenum = "";
        Date rectificationtermtime;
        Date managementtime;
        String checkcategoryname;
        Map<String,Integer> typeAndNum = new LinkedHashMap<>();
        for (Map<String, Object> dataMap : dataList) {
            if (dataMap.get("status")!=null&&Integer.parseInt(dataMap.get("status").toString())==
                    CommonTypeEnum.ProblemManagementStatusEnum.CompletedEnum.getCode()){
                zgwcnum++;
                if (dataMap.get("rectificationtermtime")!=null&&dataMap.get("managementtime")!=null){
                    rectificationtermtime = DataFormatUtil.getDateYMD(dataMap.get("rectificationtermtime").toString());
                    managementtime = DataFormatUtil.getDateYMD(dataMap.get("managementtime").toString());
                    if (rectificationtermtime.getTime()>=managementtime.getTime()){
                        jszgnum++;
                    }
                }
            }
            if (dataMap.get("checkcategoryname")!=null){
                checkcategoryname = dataMap.get("checkcategoryname").toString();
                typeAndNum.put(checkcategoryname,typeAndNum.get(checkcategoryname)!=null?typeAndNum.get(checkcategoryname)+1:1);
            }
        }
        resultMap.put("problemnum", problemnum);
        resultMap.put("jszgnum", jszgnum);
        jszgrate = DataFormatUtil.SaveTwoAndSubZero(100d*jszgnum/problemnum);
        resultMap.put("jszgrate",jszgrate );
        resultMap.put("zgwcnum", zgwcnum);
        zgwcrate = DataFormatUtil.SaveTwoAndSubZero(100d*zgwcnum/problemnum);
        resultMap.put("zgwcrate", zgwcrate);
        for (String type:typeAndNum.keySet()){
            typenum +=type+"("+typeAndNum.get(type)+")、";
        }
        if (StringUtils.isNotBlank(typenum)){
            typenum = typenum.substring(0,typenum.length()-1);
        }
        resultMap.put("typenum", typenum);
        return resultMap;


    }


}

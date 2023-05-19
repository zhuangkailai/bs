package com.tjpu.sp.controller.common.knowledge;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.common.knowledge.ScienceKnowledgeVO;
import com.tjpu.sp.model.common.knowledge.TrainInfoVO;
import com.tjpu.sp.model.common.knowledge.TrainUserInfoVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.common.knowledge.ScienceKnowledgeService;
import com.tjpu.sp.service.common.knowledge.TrainInfoService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 培训控制层
 * @Param
 * @return:
 * @Author: lip
 * @Date: 2021/7/6 11:54
 */
@RestController
@RequestMapping("trainInfo")
public class TrainInfoController {


    @Autowired
    private TrainInfoService trainInfoService;

    @Autowired
    private FileInfoService fileInfoService;


    /**
     * @Description: 添加或更新信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addOrUpdateData", method = RequestMethod.POST)
    public Object addOrUpdateData(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            TrainInfoVO trainInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TrainInfoVO());
            trainInfoVO.setUpdatetime(new Date());
            trainInfoVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(trainInfoVO.getPkId())) {//更新操作
                trainInfoService.updateInfo(trainInfoVO);
            } else {//添加操作
                trainInfoVO.setPkId(UUID.randomUUID().toString());
                trainInfoService.insertInfo(trainInfoVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 开始学习
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 11:56
     */
    @RequestMapping(value = "addStudy", method = RequestMethod.POST)
    public Object addStudy(@RequestJson(value = "formdata") Object formdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            TrainUserInfoVO trainUserInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TrainUserInfoVO());
            List<Map<String,Object>> dataList = trainInfoService.getStudyUserListById(trainUserInfoVO.getFkTrainid());
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            boolean isAdd = true;
            if (dataList.size()>0){
                for (Map<String,Object> dataMap:dataList){
                    if (dataMap.get("user_id").equals(userid)){
                        isAdd = false;
                        break;
                    }
                }
            }
            if (isAdd){
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                trainUserInfoVO.setUpdatetime(new Date());
                trainUserInfoVO.setStudytime(new Date());
                trainUserInfoVO.setFkUserid(userid);
                trainUserInfoVO.setUpdateuser(username);
                trainUserInfoVO.setPkId(UUID.randomUUID().toString());
                trainInfoService.insertUserInfo(trainUserInfoVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getListDataByParamMap", method = RequestMethod.POST)
    public Object getListDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            Map<String, Object> jsonObject = (Map) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List<Map<String, Object>> datalist = trainInfoService.getListDataByParamMap(jsonObject);
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
     * @Description: 根据ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public Object deleteById(
            @RequestJson(value = "id") String id) {
        try {
            trainInfoService.deleteInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取编辑回显数据或详情数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:01
     */
    @RequestMapping(value = "getEditOrDetailsDataById", method = RequestMethod.POST)
    public Object getEditOrDetailsDataById(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> resultMap = trainInfoService.getEditDataById(id);
            if (resultMap.get("fkfileid") != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.clear();
                jsonObject.put("fileflags", Arrays.asList(resultMap.get("fkfileid")));
                jsonObject.put("businesstype", "13");
                List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfosByParam(jsonObject);
                if (fileInfoVOS.size() > 0) {
                    List<Map<String, Object>> fileList = new ArrayList<>();
                    for (FileInfoVO fileInfoVO : fileInfoVOS) {
                        Map<String, Object> fileMap = new HashMap<>();
                        fileMap.put("fileid", fileInfoVO.getFilepath());
                        fileMap.put("filename", fileInfoVO.getOriginalfilename());
                        fileList.add(fileMap);
                    }
                    resultMap.put("fileDataList", fileList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取已学习人员列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/6 12:03
     */
    @RequestMapping(value = "getStudyUserListById", method = RequestMethod.POST)
    public Object getStudyUserListById(@RequestJson(value = "id") String id,
                                       @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                       @RequestJson(value = "pagesize", required = false) Integer pagesize
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            //获取已学习人员
            List<Map<String, Object>> datalist = trainInfoService.getStudyUserListById(id);
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


}

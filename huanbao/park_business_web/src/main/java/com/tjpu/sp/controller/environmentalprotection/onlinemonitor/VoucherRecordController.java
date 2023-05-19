package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.environmentalprotection.online.VoucherRecordVO;
import com.tjpu.sp.model.extand.AppSuggestionVO;
import com.tjpu.sp.service.common.FileInfoService;
import com.tjpu.sp.service.environmentalprotection.online.VoucherRecordService;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Description: 修约凭证记录
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/8/4 9:46
 */
@RequestMapping("voucherRecord")
@RestController
public class VoucherRecordController {

    private final VoucherRecordService voucherRecordService;
    private final FileInfoService fileInfoService;

    public VoucherRecordController(VoucherRecordService voucherRecordService, FileInfoService fileInfoService) {
        this.voucherRecordService = voucherRecordService;
        this.fileInfoService = fileInfoService;
    }



    /**
     * @Description:  获取凭证记录列表信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/8/4 9:48
     */
    @RequestMapping(value = "getVoucherRecordListDataByParam", method = RequestMethod.POST)
    public Object getVoucherRecordListDataByParam(
            @RequestJson(value = "pointid") String pointid,
            @RequestJson(value = "starttime",required = false) String starttime,
            @RequestJson(value = "endtime",required = false) String endtime,
            @RequestJson(value = "pagesize",required = false) Integer pagesize,
            @RequestJson(value = "pagenum",required = false) Integer pagenum) {
        try {
            // 分页
            Map<String,Object> resultMap = new HashMap<>();
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("starttime",starttime);
            paramMap.put("endtime",endtime);
            paramMap.put("pointid",pointid);
            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String,Object>> dataList = voucherRecordService.getVoucherRecordListDataByParam(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(dataList);
            //附件信息
            if (dataList.size() > 0) {
                List<String> fileIds = new ArrayList<>();
                String fileId;
                for (Map<String, Object> dataMap : dataList) {
                    if (dataMap.get("fk_fileid") != null) {
                        fileIds.add(dataMap.get("fk_fileid").toString());
                    }
                }
                if (fileIds.size() > 0) {
                    paramMap.clear();
                    paramMap.put("fileflags", fileIds);
                    paramMap.put("businesstype", "53");
                    List<FileInfoVO> fileInfoVOS = fileInfoService.getFilesInfosByParam(paramMap);
                    if (fileInfoVOS.size() > 0) {
                        Map<String, List<Map<String, Object>>> idAndFileList = new HashMap<>();
                        List<Map<String, Object>> fileList;
                        for (FileInfoVO fileInfoVO : fileInfoVOS) {
                            fileId = fileInfoVO.getFileflag();
                            if (idAndFileList.containsKey(fileId)) {
                                fileList = idAndFileList.get(fileId);
                            } else {
                                fileList = new ArrayList<>();
                            }
                            Map<String, Object> fileMap = new HashMap<>();
                            fileMap.put("fileid", fileInfoVO.getFilepath());
                            fileMap.put("filename", fileInfoVO.getOriginalfilename());
                            fileList.add(fileMap);
                            idAndFileList.put(fileId, fileList);
                        }
                        for (Map<String, Object> dataMap : dataList) {
                            if (dataMap.get("fk_fileid") != null) {
                                fileId = dataMap.get("fk_fileid").toString();
                                dataMap.put("fileDataList", idAndFileList.get(fileId));
                            }
                        }
                    }
                }
            }
            long total = pageInfo.getTotal();
            resultMap.put("datalist", dataList);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


   /**
    * @Description: 添加凭证记录
    * @Param:
    * @return:
    * @Author: lip
    * @Date: 2021/8/4 10:12
    */
    @RequestMapping(value = "addVoucherRecord", method = RequestMethod.POST)
    public Object addVoucherRecord(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            VoucherRecordVO voucherRecordVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new VoucherRecordVO());
            voucherRecordVO.setUpdatetime(new Date());
            voucherRecordVO.setPkId(UUID.randomUUID().toString());
            voucherRecordVO.setUpdateuser(username);
            voucherRecordService.insert(voucherRecordVO);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 删除凭证记录
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/8/4 10:12
     */
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public Object deleteById(@RequestJson(value = "id") String id) throws Exception {
        try {
            voucherRecordService.deleteById(id);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
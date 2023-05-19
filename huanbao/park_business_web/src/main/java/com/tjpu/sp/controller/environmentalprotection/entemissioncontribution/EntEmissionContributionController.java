package com.tjpu.sp.controller.environmentalprotection.entemissioncontribution;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.entemissioncontribution.EntEmissionContributionVO;
import com.tjpu.sp.service.environmentalprotection.entemissioncontribution.EntEmissionContributionService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2021/05/10 0011 下午 1:58
 * @Description: 企业排放贡献控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("entemissioncontribution")
public class EntEmissionContributionController {

    @Autowired
    private EntEmissionContributionService entemissionContributionService;

    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 2:58
     * @Description: 通过自定义参数获取企业排放贡献信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getEntEmissionContributionByParamMap", method = RequestMethod.POST)
    public Object getEntEmissionContributionByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<Map<String,Object>> entemissionContributionByParamMap = entemissionContributionService.getEntEmissionContributionByParamMap(jsonObject);
            entemissionContributionByParamMap.stream().filter(m->m.get("contributionratio")!=null).forEach(m->{
                Double contributionratio = Double.valueOf(m.get("contributionratio").toString());
                m.put("contributionratio",decimalFormat.format(contributionratio)+"%");
            });
            long total = entemissionContributionService.countEntEmissionContributionByParamMap(jsonObject);
            resultMap.put("datalist", entemissionContributionByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:17
     * @Description: 新增企业排放贡献信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addEntEmissionContribution", method = RequestMethod.POST)
    public Object addEntEmissionContribution(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            EntEmissionContributionVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntEmissionContributionVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setpkid(UUID.randomUUID().toString());
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            entemissionContributionService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:19
     * @Description: 通过id获取企业排放贡献信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntEmissionContributionByID", method = RequestMethod.POST)
    public Object getEntEmissionContributionByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> result = entemissionContributionService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:19
     * @Description: 修改企业排放贡献信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateEntEmissionContribution", method = RequestMethod.POST)
    public Object updateEntEmissionContribution(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            EntEmissionContributionVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new EntEmissionContributionVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            entemissionContributionService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:21
     * @Description: 通过id删除企业排放贡献信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteEntEmissionContributionByID", method = RequestMethod.POST)
    public Object deleteEntEmissionContributionByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            entemissionContributionService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/05/10 0011 下午 3:31
     * @Description: 通过id查询企业排放贡献信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getEntEmissionContributionDetailByID", method = RequestMethod.POST)
    public Object getEntEmissionContributionDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            Map<String,Object> detailInfo = entemissionContributionService.getEntEmissionContributionDetailByID(id);
            Double contributionratio = Double.valueOf(detailInfo.get("contributionratio").toString());
            detailInfo.put("contributionratio",decimalFormat.format(contributionratio)+"%");
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/5/13 0013 上午 11:17
     * @Description: 导出企业排放贡献信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson, request, response]
     * @throws:
     */
    @RequestMapping(value = "/ExportEntEmissionContributionByParamMap", method = RequestMethod.POST)
    public void ExportEntEmissionContributionByParamMap(@RequestJson(value = "paramsjson") Object paramsJson,HttpServletRequest request,HttpServletResponse response) throws Exception {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            List<Map<String,Object>> entemissionContributionByParamMap = entemissionContributionService.getEntEmissionContributionByParamMap(jsonObject);

            List<String> headers = new ArrayList<>();
            List<String> headersField = new ArrayList<>();
            headers.add("企业名称");
            headers.add("排放污染物");
            headers.add("贡献比例");
            headersField.add("pollutionname");
            headersField.add("pollutantname");
            headersField.add("contributionratio");

            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, entemissionContributionByParamMap, "");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("企业排放清单", response, request, bytesForWorkBook);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: chengzq
     * @date: 2021/5/10 0010 下午 3:37
     * @Description: 计算溯源事件结果并返回
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getTraceSourceEventContributionByParamMap", method = RequestMethod.POST)
    public Object getTraceSourceEventContributionByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            DecimalFormat decimalFormat = new DecimalFormat("0.###");
            List<String> pollutantcodes = jsonObject.get("fkpollutantcodes") == null ? new ArrayList<>() : JSONArray.fromObject(jsonObject.get("fkpollutantcodes"));
            List<Map<String,Object>> entemissionContributionByParamMap = entemissionContributionService.getEntEmissionContributionInfoByParamMap(jsonObject);

            for (Map<String, Object> paramMap : entemissionContributionByParamMap) {
                List<String> collect = pollutantcodes.stream().collect(Collectors.toList());
                List<String> FK_PollutantCodes = paramMap.get("fkpollutantcodes") == null ? new ArrayList<>() : Arrays.asList(paramMap.get("fkpollutantcodes").toString().split(","));
                collect.retainAll(FK_PollutantCodes);
                if(pollutantcodes.size()>0 && collect.size()==0){
                    paramMap.put("contributionratio",0d);
                }
            }
            Double sum = entemissionContributionByParamMap.stream().filter(m -> m.get("contributionratio") != null).map(m -> m.get("contributionratio").toString()).collect(Collectors.summingDouble(m -> Double.valueOf(m)));
            if(sum>0){
                for (Map<String, Object> paramMap : entemissionContributionByParamMap) {
                    if(paramMap.get("contributionratio")!=null){
                        String contributionratio = decimalFormat.format(Double.valueOf(paramMap.get("contributionratio").toString()) / sum * 100);
                        paramMap.put("contributionratio",contributionratio);
                    }
                }
            }
            List<Map<String, Object>> collect = entemissionContributionByParamMap.stream().filter(m -> m.get("contributionratio") != null).sorted(Comparator.comparing(m -> Double.valueOf(((Map<String,Object>)m).get("contributionratio").toString())).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}

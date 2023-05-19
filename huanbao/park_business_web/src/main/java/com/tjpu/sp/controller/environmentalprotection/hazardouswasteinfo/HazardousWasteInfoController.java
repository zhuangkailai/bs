package com.tjpu.sp.controller.environmentalprotection.hazardouswasteinfo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: chengzq
 * @date: 2020/09/22 0011 下午 1:58
 * @Description: 危废信息控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("hazardouswasteinfo")
public class HazardousWasteInfoController {

    @Autowired
    private HazardousWasteInfoService hazardousWasteInfoService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private PubCodeService pubCodeService;

    /**
     * @author: chengzq
     * @date: 2020/09/22 0011 下午 2:58
     * @Description: 通过自定义参数获取危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "/getHazardousWasteInfoByParamMap", method = RequestMethod.POST)
    public Object getHazardousWasteInfoByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws ParseException {
        try {
            Map<String,Object> jsonObject = (Map)paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List hazardousWasteInfoByParamMap = hazardousWasteInfoService.getHazardousWasteInfoByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(hazardousWasteInfoByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", hazardousWasteInfoByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/09/22 0011 下午 3:17
     * @Description: 新增危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addHazardousWasteInfo", method = RequestMethod.POST)
    public Object addHazardousWasteInfo(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);

            HazardousWasteInfoVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new HazardousWasteInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setpkid(UUID.randomUUID().toString());
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            hazardousWasteInfoService.insert(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/09/22 0011 下午 3:19
     * @Description: 通过id获取危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getHazardousWasteInfoByID", method = RequestMethod.POST)
    public Object getHazardousWasteInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> result = hazardousWasteInfoService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/09/22 0011 下午 3:19
     * @Description: 修改危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateHazardousWasteInfo", method = RequestMethod.POST)
    public Object updateHazardousWasteInfo(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);

            HazardousWasteInfoVO entity = JSONObjectUtil.JsonObjectToEntity(jsonObject, new HazardousWasteInfoVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            entity.setupdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            entity.setupdateuser(username);

            hazardousWasteInfoService.updateByPrimaryKey(entity);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/09/22 0011 下午 3:21
     * @Description: 通过id删除危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteHazardousWasteInfoByID", method = RequestMethod.POST)
    public Object deleteHazardousWasteInfoByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            hazardousWasteInfoService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/09/22 0011 下午 3:31
     * @Description: 通过id查询危废信息信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getHazardousWasteInfoDetailByID", method = RequestMethod.POST)
    public Object getHazardousWasteInfoDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String,Object> detailInfo = hazardousWasteInfoService.getHazardousWasteInfoDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", detailInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: chengzq
     * @date: 2020/9/25 0025 下午 2:28
     * @Description: 上传固废模板信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [file, request, response]
     * @throws:
     */
    @RequestMapping(value = "/ImportHazardousWasteByExcel", method = RequestMethod.POST)
    public Object ImportHazardousWasteByExcel(@RequestParam(value = "file") MultipartFile file) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            Boolean[] flag={true};
            Map<String, Object> paramMap = new HashMap<>();
            List<HazardousWasteInfoVO> addlist=new ArrayList<>();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            List<Map<String, Object>> pollutionNameAndPkid = pollutionService.getPollutionNameAndPkid(paramMap);
            Map<String, String> pollutionMap = pollutionNameAndPkid.stream().filter(m -> m.get("pollutionid") != null && m.get("pollutionname") != null).collect(Collectors.toMap(m -> m.get("pollutionname").toString()
                    , m -> m.get("pollutionid").toString(), (a, b) -> a));
            paramMap.put("tablename","PUB_CODE_WasteMaterial");
            paramMap.put("fields",Arrays.asList("name","code"));
            List<Map<String, Object>> pubCodesDataByParam = pubCodeService.getPubCodesDataByParam(paramMap);
            Map<String, String> wasteMap = pubCodesDataByParam.stream().filter(m -> m.get("name") != null && m.get("code") != null).collect(Collectors.toMap(m -> m.get("code").toString()+ m.get("name").toString()
                    , m -> m.get("code").toString(), (a, b) -> a));

            Workbook book = getWorkbook(inputStream,originalFilename);


            //样式对象设置样式
            CellStyle cellStyle = book.createCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);


            Sheet sheetAt = book.getSheetAt(7);


            if(!"产废单位月汇总(表7)".equals(sheetAt.getSheetName())){
                Map<String,Object> data=new HashMap<>();
                data.put("message","导入失败，请确认‘产废单位月汇总(表7)’位于第八sheet页");
                data.put("filename",originalFilename);
                return AuthUtil.parseJsonKeyToLower("success",data);
            }

            int lastRowNum = sheetAt.getLastRowNum();

            //从第8个sheet页中获取日期，每次只能导入一个月的数据。首先移除第一行表头-------------------
            Sheet sheetAt1 = book.getSheetAt(8);
            Row row1 = sheetAt1.getRow(0);
            List<String> dates=new ArrayList<>();
            for (int i = 1; i < sheetAt1.getLastRowNum(); i++) {
                Row cells = sheetAt1.getRow(i);
                if(!row1.equals(cells)){
                    Cell cell = cells.getCell(0);
                    int cellType = cell.getCellType();
                    Date dateCellValue = cell.getDateCellValue();
                    if(cellType!=Cell.CELL_TYPE_NUMERIC){
                        Map<String,Object> data=new HashMap<>();
                        data.put("message","导入失败，请在'产废单位月汇总(表8)'页中输入正确的日期");
                        data.put("filename",originalFilename);
                        return AuthUtil.parseJsonKeyToLower("success",data);
                    }
                    dates.add(DataFormatUtil.getDateYM(dateCellValue));
                }

            }
            List<String> collect = dates.stream().distinct().collect(Collectors.toList());
            if(collect.size()!=1){
                Map<String,Object> data=new HashMap<>();
                data.put("message","导入失败，请导入单月数据");
                data.put("filename",originalFilename);
                return AuthUtil.parseJsonKeyToLower("success",data);
            }
            String Month = collect.get(0);
            //获取日期结束---------------------------



            for (int i = 1; i < lastRowNum; i++) {
                //第i行
                Row row = sheetAt.getRow(i);
                Boolean[] isStore={true};
                HazardousWasteInfoVO hazardousWasteInfoVO = AssembleDataWithComment(sheetAt, row, username, pollutionMap, wasteMap, cellStyle, originalFilename, Month, isStore);
                //如果有错误跳过这一行
                if(!isStore[0]){
                    flag[0]=false;
                    continue;
                }/*else{
                    //如果没有错误移除改行
                    sheetAt.removeRow(row);
                    //往上移动一行
                    sheetAt.shiftRows(i,lastRowNum+1,-1);
                    lastRowNum--;
                    i--;
                }*/
                addlist.add(hazardousWasteInfoVO);
            }



            String uuid = UUID.randomUUID().toString().toLowerCase();
            //数据有问题（没有企业，没有固废类型，数字类型验证）
            if(!flag[0]){
                System.out.println(uuid);
                book.write(os);
                RedisTemplateUtil.putCacheWithExpireTime(uuid,os.toByteArray(), RedisTemplateUtil.CAHCEONEHOUR/2);
                Map<String,Object> data=new HashMap<>();
                data.put("message","数据有问题是否下载错误数据模板");
                data.put("filename",originalFilename);
                data.put("fileid",uuid);
                return AuthUtil.parseJsonKeyToLower("success",data);
            }
            //验证数据重复
            else if(isRepeat(sheetAt, Month,originalFilename,cellStyle).size()>0){
                System.out.println(uuid);
                book.write(os);
                RedisTemplateUtil.putCacheWithExpireTime(uuid,os.toByteArray(), RedisTemplateUtil.CAHCEONEHOUR/2);
                Map<String,Object> data=new HashMap<>();
                data.put("message","导入数据包含重复数据是否下载重复数据模板");
                data.put("filename",originalFilename);
                data.put("fileid",uuid);
                return AuthUtil.parseJsonKeyToLower("success",data);
            }
            //数据没有问题
            else{
                hazardousWasteInfoService.insertBatch(addlist);
                Map<String,Object> data=new HashMap<>();
                data.put("message","导入成功");
                data.put("filename",originalFilename);
                return AuthUtil.parseJsonKeyToLower("success",data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            os.close();
        }
    }



    /**
     * @author: chengzq
     * @date: 2020/9/25 0025 下午 1:38
     * @Description: 点击下载问题模板
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [file, request, response]
     * @throws:
     */
    @RequestMapping(value = "/exportExcelByParams", method = RequestMethod.POST)
    public void exportExcelByParams(@RequestJson(value = "fileid") String fileid,@RequestJson(value = "filename") String filename,
                                             HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            byte[] cache = RedisTemplateUtil.getCache(fileid, byte[].class);
            if(cache!=null){
                ExcelUtil.downLoadExcel(filename, response, request,cache,"");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/9/25 0025 下午 2:41
     * @Description: 选择覆盖后对固废信息进行覆盖操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fileid, filename, request, response]
     * @throws:
     */
    @RequestMapping(value = "/addAndUpdateHazardousWaste", method = RequestMethod.POST)
    public Object addAndUpdateHazardousWaste(@RequestJson(value = "fileid") String fileid,@RequestJson(value = "filename") String filename) throws Exception {
        ByteArrayInputStream arrayInputStream=null;
        try {
            byte[] cache = RedisTemplateUtil.getCache(fileid, byte[].class);
            if(cache==null){
                Map<String,Object> data=new HashMap<>();
                data.put("message","文件已过期");
                data.put("filename",filename);
                return AuthUtil.parseJsonKeyToLower("success",data);
            }

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Map<String,Object> paramMap = new HashMap<>();
//            Boolean[] flag={true};
            List<HazardousWasteInfoVO> repeatdata=new ArrayList<>();//重复数据
            List<HazardousWasteInfoVO> unrepeatdata=new ArrayList<>();//不重复数据

            List<Map<String, Object>> pollutionNameAndPkid = pollutionService.getPollutionNameAndPkid(paramMap);
            Map<String, String> pollutionMap = pollutionNameAndPkid.stream().filter(m -> m.get("pollutionid") != null && m.get("pollutionname") != null).collect(Collectors.toMap(m -> m.get("pollutionname").toString()
                    , m -> m.get("pollutionid").toString(), (a, b) -> a));
            paramMap.put("tablename","PUB_CODE_WasteMaterial");
            paramMap.put("fields",Arrays.asList("name","code"));
            List<Map<String, Object>> pubCodesDataByParam = pubCodeService.getPubCodesDataByParam(paramMap);
            Map<String, String> wasteMap = pubCodesDataByParam.stream().filter(m -> m.get("name") != null && m.get("code") != null).collect(Collectors.toMap(m -> m.get("code").toString()+ m.get("name").toString()
                    , m -> m.get("code").toString(), (a, b) -> a));




            arrayInputStream = new ByteArrayInputStream(cache);
            Workbook book = getWorkbook(arrayInputStream, filename);
            Sheet sheetAt = book.getSheetAt(7);

            /*//样式对象设置样式
            CellStyle cellStyle = book.createCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);*/
            //获取最后一行，当数据没有问题时将该条数据删除并往上移动一行
            int lastRowNum = sheetAt.getLastRowNum();

            //从第8个sheet页中获取日期，每次只能导入一个月的数据。首先移除第一行表头-------------------
            Sheet sheetAt1 = book.getSheetAt(8);
            Row row1 = sheetAt1.getRow(0);
            List<String> dates=new ArrayList<>();
            for (int i = 1; i < sheetAt1.getLastRowNum(); i++) {
                Row cells = sheetAt1.getRow(i);
                if(!row1.equals(cells)){
                    Cell cell = cells.getCell(0);
//                    int cellType = cell.getCellType();
                    Date dateCellValue = cell.getDateCellValue();
                    /*if(cellType!=Cell.CELL_TYPE_NUMERIC){
                        return AuthUtil.parseJsonKeyToLower("success","导入失败，请在'产废单位月汇总(表8)'页中输入正确的日期");
                    }*/
                    dates.add(DataFormatUtil.getDateYM(dateCellValue));
                }

            }
            List<String> collect = dates.stream().distinct().collect(Collectors.toList());
            /*if(collect.size()!=1){
                return AuthUtil.parseJsonKeyToLower("success","导入失败，请导入单月数据");
            }*/
            String Month = collect.get(0);
            //获取日期结束---------------------------

            paramMap.put("monthdate",Month);
            List<Map<String, Object>> hazardousWasteInfoByParamMap = hazardousWasteInfoService.getHazardousWasteInfoByParamMap(paramMap);
            Map<String, String> collect1 = hazardousWasteInfoByParamMap.stream().filter(m -> m.get("PollutionName") != null && m.get("FKWasteMaterialName") != null)
                    .collect(Collectors.toMap(m -> m.get("PollutionName").toString()
                            , m -> m.get("FKWasteMaterialName").toString(), (a, b) -> a));

            for (int i = 1; i < lastRowNum; i++) {
                Row row = sheetAt.getRow(i);
//                Boolean[] isStore={true};
                String pollutionname = getRowStringValue(row, 0);
                String wastematerialtype = getRowStringValue(row, 4);
                String s = collect1.get(pollutionname);

                HazardousWasteInfoVO hazardousWasteInfoVO = AssembleDataWithComment(sheetAt, row, username, pollutionMap, wasteMap,null, filename, Month,null);

                /*if(!isStore[0]){
                    flag[0]=false;
                    continue;
                }*/
                //数据重复
                if(StringUtils.isNotBlank(s) && s.equals(wastematerialtype)){
                    repeatdata.add(hazardousWasteInfoVO);
                }else{
                    unrepeatdata.add(hazardousWasteInfoVO);
                }
            }

            /*String uuid = UUID.randomUUID().toString().toLowerCase();
            //数据有问题（没有企业，没有固废类型，数字类型验证）
            if(!flag[0]){
                System.out.println(uuid);
                book.write(os);
                RedisTemplateUtil.putCacheWithExpireTime(uuid,os.toByteArray(), RedisTemplateUtil.CAHCEONEHOUR/2);
                Map<String,Object> data=new HashMap<>();
                data.put("message","数据有问题是否下载错误数据模板");
                data.put("filename",filename);
                return AuthUtil.parseJsonKeyToLower("success",data);
            }*/

            return  AuthUtil.parseJsonKeyToLower("success",hazardousWasteInfoService.addAndUpdateBatch(repeatdata,unrepeatdata));

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            arrayInputStream.close();
        }
    }



    /**
     * @author: chengzq
     * @date: 2020/9/24 0024 下午 6:19
     * @Description: 组装数据有错误设置批注
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sheetAt, lastRowNum, username, pollutionMap, wasteMap, cellStyle, originalFilename, Month, flag, addlist]
     * @throws:
     */
    private HazardousWasteInfoVO AssembleDataWithComment(Sheet sheetAt,Row row,String username,Map<String,String> pollutionMap,Map<String,String> wasteMap,CellStyle cellStyle,String originalFilename,String Month,Boolean[] isStore){
        Date date = new Date();
        HazardousWasteInfoVO hazardousWasteInfoVO = new HazardousWasteInfoVO();
        hazardousWasteInfoVO.setupdatetime(DataFormatUtil.getDateYMDHMS(date));
        hazardousWasteInfoVO.setupdateuser(username);
        hazardousWasteInfoVO.setpkid(UUID.randomUUID().toString());


        String pollutionid = pollutionMap.get(getRowStringValue(row, 0));
        String wastecode = wasteMap.get(getRowStringValue(row, 4));
        if(StringUtils.isBlank(pollutionid) || StringUtils.isBlank(wastecode)){
            //如果企业不存在添加批注
            if(StringUtils.isBlank(pollutionid)){
                row.getCell(0).setCellStyle(cellStyle);
                if(originalFilename.endsWith(".xlsx")){
                    setXSSFComment(sheetAt,row.getRowNum(),0,"该企业不存在");
                }else{
                    setHSSFComment(sheetAt,row.getRowNum(),0,"该企业不存在");
                }
            }
            if(StringUtils.isBlank(wastecode)){
                row.getCell(4).setCellStyle(cellStyle);
                if(originalFilename.endsWith(".xlsx")){
                    setXSSFComment(sheetAt,row.getRowNum(),4,"该危险类别不存在");
                }else{
                    setHSSFComment(sheetAt,row.getRowNum(),4,"该危险类别不存在");
                }
            }
            isStore[0]=false;
        }
        hazardousWasteInfoVO.setfkpollutionid(pollutionMap.get(getRowStringValue(row,0)));//企业名称
        hazardousWasteInfoVO.setfkwastematerialtype(wasteMap.get(getRowStringValue(row,4)));//危废类别
        hazardousWasteInfoVO.setmonthdate(Month);//月份
        hazardousWasteInfoVO.setlastmonthlegacyquantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,5,originalFilename));//上一月遗留量
        hazardousWasteInfoVO.setplannedproductionquantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,6,originalFilename));//计划产生量
        hazardousWasteInfoVO.setproductionquantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,7,originalFilename));//产生量
        hazardousWasteInfoVO.setstockadjustquantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,8,originalFilename));//库存调整量
        hazardousWasteInfoVO.setdelegateutilizationquantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,9,originalFilename));//委外处置利用量
        hazardousWasteInfoVO.setoutprovincetransferlist(getRowDoubleValue(isStore,cellStyle,sheetAt,row,10,originalFilename));//联单转出省外
        hazardousWasteInfoVO.setprovincetransferlist(getRowDoubleValue(isStore,cellStyle,sheetAt,row,11,originalFilename));//联单省内转出
        hazardousWasteInfoVO.setselfusequantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,12,originalFilename));//自行利用处置量
        hazardousWasteInfoVO.setamongusequantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,13,originalFilename));//其中利用
        hazardousWasteInfoVO.setamongmanagequantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,14,originalFilename));//其中处置
        hazardousWasteInfoVO.setsecondaryquantity(getRowDoubleValue(isStore,cellStyle,sheetAt,row,15,originalFilename));//次生量
        hazardousWasteInfoVO.setendingstocks(getRowDoubleValue(isStore,cellStyle,sheetAt,row,16,originalFilename));//期末库存
        hazardousWasteInfoVO.setoveryearstocks(getRowDoubleValue(isStore,cellStyle,sheetAt,row,17,originalFilename));//超一年库存

        return hazardousWasteInfoVO;
    }




    /**
     * @author: chengzq
     * @date: 2020/9/24 0024 上午 11:24
     * @Description:获取重复数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sheet, Month]
     * @throws:
     */
    private List<Row> isRepeat(Sheet sheet,String Month,String originalFilename,CellStyle cellStyle){
        Map<String, Object> paramMap = new HashMap<>();
        List<Row> repeat=new ArrayList<>();//重复数据集合
        List<String> pollutionnamelist=new ArrayList<>();
        List<String> wastematerialtypelist=new ArrayList<>();

        paramMap.put("monthdate",Month);
        List<Map<String, Object>> hazardousWasteInfoByParamMap = hazardousWasteInfoService.getHazardousWasteInfoByParamMap(paramMap);
        Map<String, String> collect = hazardousWasteInfoByParamMap.stream().filter(m -> m.get("PollutionName") != null && m.get("FKWasteMaterialName") != null)
                                    .collect(Collectors.toMap(m -> m.get("PollutionName").toString()
                , m -> m.get("FKWasteMaterialName").toString(), (a, b) -> a));

        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i < lastRowNum; i++) {
            Row row = sheet.getRow(i);
            String pollutionname = getRowStringValue(row, 0);
            String wastematerialtype = getRowStringValue(row, 4);
            String s = collect.get(pollutionname);

            //数据重复
            if(StringUtils.isNotBlank(s) && s.equals(wastematerialtype)  || (pollutionnamelist.contains(pollutionname) && wastematerialtypelist.contains(wastematerialtype))){
                repeat.add(row);
            }
            pollutionnamelist.add(pollutionname);
            wastematerialtypelist.add(wastematerialtype);
        }



        //如果有重复设置批注
        if(repeat.size()>0){
            for (int i = 1; i < lastRowNum; i++) {
                Row row = sheet.getRow(i);
                String pollutionname = getRowStringValue(row, 0);
                String wastematerialtype = getRowStringValue(row, 4);
                for (Row cells : repeat) {
                    String pollutionnameR = getRowStringValue(cells, 0);
                    String wastematerialtypeR = getRowStringValue(cells, 4);
                    //企业和危废类型都相同设置批注或者在excel自身中就重复
                    if((pollutionnameR.equals(pollutionname) && wastematerialtypeR.equals(wastematerialtype))){
                        if(originalFilename.endsWith(".xlsx")){
                            row.getCell(0).setCellStyle(cellStyle);
                            setXSSFComment(sheet,row.getRowNum(),0,"该企业在"+Month+"已存在"+wastematerialtype+"的记录");
                        }else{
                            row.getCell(0).setCellStyle(cellStyle);
                            setHSSFComment(sheet,row.getRowNum(),0,"该企业在"+Month+"已存在"+wastematerialtype+"的记录");
                        }
                    }
                }
            }
        }

        return repeat;

    }






    public static Workbook getWorkbook(InputStream inputStream,String originalFilename) throws IOException {
        if(originalFilename.endsWith(".xlsx")){
            return new XSSFWorkbook(inputStream);
        }else{
            return new HSSFWorkbook(inputStream);
        }
    }


    public static Double getRowDoubleValue(Boolean[] isStore, CellStyle cellStyle, Sheet sheet, Row row, Integer index, String originalFilename){
        short lastCellNum = row.getLastCellNum();
        if(lastCellNum>=index && row.getCell(index)!=null){
            int cellTypeBlank = Cell.CELL_TYPE_BLANK;
            int cellTypeNumeric = Cell.CELL_TYPE_NUMERIC;
            int cellType = row.getCell(index).getCellType();
            if(cellType!=cellTypeBlank){//不是空类型
                if(cellType==cellTypeNumeric){
                    return row.getCell(index).getNumericCellValue();
                }else{
                    //如果不是数字类型设置样式并且添加批注
                    //设置样式
                    row.getCell(index).setCellStyle(cellStyle);
                    isStore[0]=false;
                    if(originalFilename.endsWith(".xlsx")){
                        setXSSFComment(sheet,row.getRowNum(),index,"请输入数字类型");
                    }else{
                        setHSSFComment(sheet,row.getRowNum(),index,"请输入数字类型");
                    }
                }
            }

        }
        return null;
    }

    public static String getRowStringValue(Row row, Integer index){
        short lastCellNum = row.getLastCellNum();
        if(lastCellNum>=index && row.getCell(index)!=null){
            int cellTypeBlank = Cell.CELL_TYPE_BLANK;
            int cellType = row.getCell(index).getCellType();
            if(cellType!=cellTypeBlank){
                return row.getCell(index).getStringCellValue();
            }
        }
        return null;
    }

    public static String getRowDateValue(Row row, Integer index){
        short lastCellNum = row.getLastCellNum();
        if(lastCellNum>=index && row.getCell(index)!=null){
            int cellTypeBlank = Cell.CELL_TYPE_BLANK;
            int cellType = row.getCell(index).getCellType();
            if(cellType!=cellTypeBlank){
                return DataFormatUtil.getDateYMDHMS(row.getCell(index).getDateCellValue());
            }
        }
        return null;
    }


    /**
     * @author: chengzq
     * @date: 2020/9/25 0025 下午 2:26
     * @Description: 设置批注excel高版本
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sheet, rowindex, cellindex, text]
     * @throws:
     */
    public static void setXSSFComment(Sheet sheet, Integer rowindex, Integer cellindex,String text){
        // 创建绘图对象
        Drawing drawingPatriarch = sheet.createDrawingPatriarch();
        // 创建单元格对象,批注插入到4行,1列,B5单元格
        Cell cell = sheet.getRow(rowindex).getCell(cellindex);

        // 获取批注对象
        // (int dx1, int dy1, int dx2, int dy2, short col1, int row1, short
        // col2, int row2)
        // 前四个参数是坐标点,后四个参数是编辑和显示批注时的大小.
        XSSFComment comment = (XSSFComment) drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0,0, (short) 3, 3, (short) 5, 6));
        // 输入批注信息
        comment.setString(new XSSFRichTextString(text));
        cell.removeCellComment();
        cell.setCellComment(comment);
    }


    /**
     * @author: chengzq
     * @date: 2020/9/25 0025 下午 2:27
     * @Description: 设置批注excel2003
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sheet, rowindex, cellindex, text]
     * @throws:
     */
    public static void setHSSFComment(Sheet sheet, Integer rowindex, Integer cellindex,String text){
        // 创建绘图对象
        Drawing drawingPatriarch = sheet.createDrawingPatriarch();
        // 创建单元格对象,批注插入到4行,1列,B5单元格
        Cell cell = sheet.getRow(rowindex).getCell(cellindex);

        // 获取批注对象
        // (int dx1, int dy1, int dx2, int dy2, short col1, int row1, short
        // col2, int row2)
        // 前四个参数是坐标点,后四个参数是编辑和显示批注时的大小.
        HSSFComment comment = (HSSFComment) drawingPatriarch.createCellComment(new HSSFClientAnchor(0, 0, 0,0, (short) 3, 3, (short) 5, 6));
        // 输入批注信息
        comment.setString(new HSSFRichTextString(text));
        cell.removeCellComment();
        cell.setCellComment(comment);
    }




}

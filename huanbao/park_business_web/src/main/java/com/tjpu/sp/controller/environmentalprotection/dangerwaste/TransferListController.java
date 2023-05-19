package com.tjpu.sp.controller.environmentalprotection.dangerwaste;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.environmentalprotection.dangerwaste.TransferListVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.dangerwaste.TransferListService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.controller.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoController.*;

/**
 * @author: xsm
 * @description: 转移联单表
 * @create: 2019-10-21 19:25
 * @version: V1.0
 */
@RestController
@RequestMapping("transferList")
public class TransferListController {
    @Autowired
    private TransferListService transferListService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private PubCodeService pubCodeService;

    /**
     * @Author: xsm
     * @Date: 2019-10-21 0021 下午 19:26
     * @Description: 自定义查询条件查询转移联单表列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getTransferListsByParamMap", method = RequestMethod.POST)
    public Object getTransferListsByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist = transferListService.getTransferListsByParamMap(jsonObject);
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                List<Map<String, Object>> dataList = getPageData(datalist, Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
                resultMap.put("total", datalist.size());
                resultMap.put("datalist", dataList);
            } else {
                resultMap.put("datalist", datalist);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/10/27 0027 下午 3:29
     * @Description: 通过自定义条件查询转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @RequestMapping(value = "getTransferListInfoByParamMap", method = RequestMethod.POST)
    public Object getTransferListInfoByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist = transferListService.getTransferListInfoByParamMap(jsonObject);
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                List<Map<String, Object>> dataList = getPageData(datalist, Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
                resultMap.put("total", datalist.size());
                resultMap.put("datalist", dataList);
            } else {
                resultMap.put("datalist", datalist);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019-10-21 0021 下午 19:26
     * @Description: 新增转移联单表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata]
     * @throws:
     */
    @RequestMapping(value = "/addTransferList", method = RequestMethod.POST)
    public Object addTransferList(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            TransferListVO transferListVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TransferListVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            transferListVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            transferListVO.setUpdateuser(username);
            transferListVO.setPkid(UUID.randomUUID().toString());
            transferListService.insert(transferListVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019-10-21 0021 下午 19:26
     * @Description: 通过id获取转移联单表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTransferListByID", method = RequestMethod.POST)
    public Object getTransferListByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            TransferListVO transferListVO = transferListService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", transferListVO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019-10-21 0021 下午 19:26
     * @Description: 修改转移联单表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata]
     * @throws:
     */
    @RequestMapping(value = "/updateTransferList", method = RequestMethod.POST)
    public Object updateTransferList(@RequestJson(value = "updateformdata") Object updateformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            TransferListVO transferListVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new TransferListVO());
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            transferListVO.setUpdateuser(username);
            transferListVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            transferListService.updateByPrimaryKey(transferListVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019-10-21 0021 下午 19:26
     * @Description: 通过id删除转移联单表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteTransferListByID", method = RequestMethod.POST)
    public Object deleteTransferListByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            transferListService.deleteByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019-10-21 0021 下午 19:26
     * @Description: 通过id获取转移联单表详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTransferListDetailByID", method = RequestMethod.POST)
    public Object getTransferListDetailByID(@RequestJson(value = "id") String id) throws Exception {
        try {
            Map<String, Object> result = transferListService.getTransferListDetailByID(id);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/22 0022 上午 9:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }


    /**
     * @author: chengzq
     * @date: 2020/9/25 0025 下午 2:28
     * @Description: 上传转移联单模板信息
     * @updateUser:
     * @updateDate:
     * @updateDescription: transferareatype 1省外2省内
     * @param: [file, request, response]
     * @throws:
     */
    @RequestMapping(value = "/ImportTransferListByExcel", method = RequestMethod.POST)
    public Object ImportTransferListByExcel(@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "transferareatype") Integer transferareatype) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            Boolean[] flag = {true};
            Map<String, Object> paramMap = new HashMap<>();
            List<TransferListVO> addlist = new ArrayList<>();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            List<Map<String, Object>> pollutionNameAndPkid = pollutionService.getPollutionNameAndPkid(paramMap);
            Map<String, String> pollutionMap = pollutionNameAndPkid.stream().filter(m -> m.get("pollutionid") != null && m.get("pollutionname") != null).collect(Collectors.toMap(m -> m.get("pollutionname").toString()
                    , m -> m.get("pollutionid").toString(), (a, b) -> a));
            paramMap.put("tablename", "PUB_CODE_WasteMaterial");
            paramMap.put("fields", Arrays.asList("name", "code"));
            List<Map<String, Object>> pubCodesDataByParam = pubCodeService.getPubCodesDataByParam(paramMap);
            Map<String, String> wasteMap = pubCodesDataByParam.stream().filter(m -> m.get("name") != null && m.get("code") != null).collect(Collectors.toMap(m -> m.get("code").toString() + m.get("name").toString()
                    , m -> m.get("code").toString(), (a, b) -> a));

            Workbook book = getWorkbook(inputStream, originalFilename);


            //样式对象设置样式
            CellStyle cellStyle = book.createCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);


            Sheet sheetAt = book.getSheetAt(0);
            //获取最后一行，当数据没有问题时将该条数据删除并往上移动一行
            int lastRowNum = sheetAt.getLastRowNum();

            for (int i = 2; i <= lastRowNum; i++) {
                //第i行
                Row row = sheetAt.getRow(i);
                Boolean[] isStore = {true};
                TransferListVO transferListVO;
                if (transferareatype == 1) {//省外
                    transferListVO = AssembleDataWithCommentOutside(sheetAt, row, username, pollutionMap, wasteMap, cellStyle, originalFilename, isStore);
                } else {
                    transferListVO = AssembleDataWithCommentInside(sheetAt, row, username, pollutionMap, wasteMap, cellStyle, originalFilename, isStore);
                }
                //如果有错误跳过这一行
                if (!isStore[0]) {
                    flag[0] = false;
                    continue;
                }
                addlist.add(transferListVO);
            }


            String uuid = UUID.randomUUID().toString().toLowerCase();
            //数据有问题（没有企业，没有固废类型，数字类型验证）
            if (!flag[0]) {
                System.out.println(uuid);
                book.write(os);
                RedisTemplateUtil.putCacheWithExpireTime(uuid, os.toByteArray(), RedisTemplateUtil.CAHCEONEHOUR / 2);
                Map<String, Object> data = new HashMap<>();
                data.put("message", "数据有问题是否下载错误数据模板");
                data.put("filename", originalFilename);
                data.put("fileid", uuid);
                return AuthUtil.parseJsonKeyToLower("success", data);
            }
            //验证数据重复
            else if (isRepeat(sheetAt, originalFilename, cellStyle).size() > 0) {
                System.out.println(uuid);
                book.write(os);
                RedisTemplateUtil.putCacheWithExpireTime(uuid, os.toByteArray(), RedisTemplateUtil.CAHCEONEHOUR / 2);
                Map<String, Object> data = new HashMap<>();
                data.put("message", "导入数据包含重复数据是否下载重复数据模板");
                data.put("filename", originalFilename);
                data.put("fileid", uuid);
                return AuthUtil.parseJsonKeyToLower("success", data);
            }
            //数据没有问题
            else {
                transferListService.insertBatch(addlist);
                Map<String, Object> data = new HashMap<>();
                data.put("message", "导入成功");
                data.put("filename", originalFilename);
                return AuthUtil.parseJsonKeyToLower("success", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            os.close();
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/9/25 0025 下午 2:41
     * @Description: 选择覆盖后对转移联单信息进行覆盖操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fileid, filename, request, response]
     * @throws:
     */
    @RequestMapping(value = "/addAndUpdateTransferList", method = RequestMethod.POST)
    public Object addAndUpdateTransferList(@RequestJson(value = "fileid") String fileid,
                                           @RequestJson(value = "filename") String filename,
                                           @RequestJson(value = "transferareatype") Integer transferareatype) throws Exception {
        ByteArrayInputStream arrayInputStream = null;
        try {
            byte[] cache = RedisTemplateUtil.getCache(fileid, byte[].class);
            if (cache == null) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", "文件已过期");
                data.put("filename", filename);
                return AuthUtil.parseJsonKeyToLower("success", data);
            }
            arrayInputStream = new ByteArrayInputStream(cache);
            Workbook book = getWorkbook(arrayInputStream, filename);

            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            List<TransferListVO> repeatdata = new ArrayList<>();//重复数据
            List<TransferListVO> unrepeatdata = new ArrayList<>();//不重复数据

            List<Map<String, Object>> pollutionNameAndPkid = pollutionService.getPollutionNameAndPkid(paramMap);
            Map<String, String> pollutionMap = pollutionNameAndPkid.stream().filter(m -> m.get("pollutionid") != null && m.get("pollutionname") != null).collect(Collectors.toMap(m -> m.get("pollutionname").toString()
                    , m -> m.get("pollutionid").toString(), (a, b) -> a));
            paramMap.put("tablename", "PUB_CODE_WasteMaterial");
            paramMap.put("fields", Arrays.asList("name", "code"));
            List<Map<String, Object>> pubCodesDataByParam = pubCodeService.getPubCodesDataByParam(paramMap);
            Map<String, String> wasteMap = pubCodesDataByParam.stream().filter(m -> m.get("name") != null && m.get("code") != null).collect(Collectors.toMap(m -> m.get("code").toString() + m.get("name").toString()
                    , m -> m.get("code").toString(), (a, b) -> a));


            Sheet sheetAt = book.getSheetAt(0);
            int lastRowNum = sheetAt.getLastRowNum();
            List<String> numbers = new ArrayList<>();
            for (int i = 1; i < sheetAt.getLastRowNum(); i++) {
                Row cells = sheetAt.getRow(i);
                Cell cell = cells.getCell(0);
                int cellType = cell.getCellType();
                if (cellType != Cell.CELL_TYPE_STRING) {
                    return AuthUtil.parseJsonKeyToLower("success", "导入失败，正确格式的单号");
                }
                String dateCellValue = cell.getStringCellValue();
                numbers.add(dateCellValue);
            }


            paramMap.put("transferlistnums", numbers);
            List<String> transferlistnums = transferListService.getTransferlistnumByParams(paramMap);


            for (int i = 2; i < lastRowNum; i++) {
                Row row = sheetAt.getRow(i);
                String transferlistnum = getRowStringValue(row, 0);
                TransferListVO hazardousWasteInfoVO;
                if (transferareatype == 1) {//省外
                    hazardousWasteInfoVO = AssembleDataWithCommentOutside(sheetAt, row, username, pollutionMap, wasteMap, null, filename, null);

                } else {
                    hazardousWasteInfoVO = AssembleDataWithCommentInside(sheetAt, row, username, pollutionMap, wasteMap, null, filename, null);
                }
                //数据重复
                if (transferlistnums.contains(transferlistnum)) {
                    repeatdata.add(hazardousWasteInfoVO);
                } else {
                    unrepeatdata.add(hazardousWasteInfoVO);
                }
            }


            return AuthUtil.parseJsonKeyToLower("success", transferListService.addAndUpdateBatch(unrepeatdata, repeatdata));

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            arrayInputStream.close();
        }
    }


    /**
     * @Description: 转移联单统计（转移联单数、转移量、省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/8 10:43
     */
    @RequestMapping(value = "/getTransferCountData", method = RequestMethod.POST)
    public Object getTransferCountData(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> dataList = transferListService.getTransferListInfoByParamMap(paramMap);
            Map<String,Object> resultMap = new HashMap<>();
            if (dataList.size()>0){
                resultMap.put("listnum",dataList.size());
                Double transfernum = 0d;
                Double subNum;
                Map<String,Double> typeAndNum = new HashMap<>();
                String type;
                for (Map<String,Object> dataMap:dataList){
                    if (dataMap.get("TransferQuantity")!=null){
                        subNum = Double.parseDouble(dataMap.get("TransferQuantity").toString());
                        transfernum+=subNum;
                        if (dataMap.get("FK_TransferAreaTypeCode")!=null){
                            type =dataMap.get("FK_TransferAreaTypeCode").toString();
                            typeAndNum.put(type,typeAndNum.get(type)!=null?typeAndNum.get(type)+subNum:subNum);
                        }
                    }
                }
                resultMap.put("transfernum",DataFormatUtil.SaveOneAndSubZero(transfernum));
                Double insidenum  = typeAndNum.get(CommonTypeEnum.TransferAreaTypeEnum.insideEnum.getCode());
                if (insidenum!=null){
                    resultMap.put("insidenum",DataFormatUtil.SaveOneAndSubZero(insidenum));
                }else {
                    resultMap.put("insidenum",0);
                }
                Double outsidenum  = typeAndNum.get(CommonTypeEnum.TransferAreaTypeEnum.outsideEnum.getCode());
                if (outsidenum!=null){
                    resultMap.put("outsidenum",DataFormatUtil.SaveOneAndSubZero(outsidenum));
                }else {
                    resultMap.put("outsidenum",0);
                }
            }else {
                resultMap.put("listnum",0);
                resultMap.put("transfernum",0);
                resultMap.put("insidenum",0);
                resultMap.put("outsidenum",0);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

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
    private List<Row> isRepeat(Sheet sheet, String originalFilename, CellStyle cellStyle) {
        Map<String, Object> paramMap = new HashMap<>();
        List<Row> repeat = new ArrayList<>();//重复数据集合


        int lastRowNum = sheet.getLastRowNum();

        List<String> transferlistnumself = new ArrayList<>();//收集excel中所有单号
        for (int i = 2; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            String transferlistnum = getRowStringValue(row, 0);
            if (StringUtils.isNotBlank(transferlistnum)) {
                transferlistnumself.add(transferlistnum);
            }
        }
        paramMap.put("transferlistnums", transferlistnumself);
        //查找库中存在的单号
        List<String> transferlistnums = transferListService.getTransferlistnumByParams(paramMap);


        transferlistnumself.clear();
        for (int i = 2; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            String transferlistnum = getRowStringValue(row, 0);
            //数据重复或者在excel自身中就重复
            if (transferlistnums.contains(transferlistnum) || transferlistnumself.contains(transferlistnum)) {
                repeat.add(row);
            }
            transferlistnumself.add(transferlistnum);
        }

        //如果有重复设置批注
        if (repeat.size() > 0) {
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                String transferlistnum = getRowStringValue(row, 0);
                for (Row cells : repeat) {
                    String transferlistnumR = getRowStringValue(cells, 0);

                    //相同设置批注
                    if (transferlistnumR.equals(transferlistnum)) {
                        if (originalFilename.endsWith(".xlsx")) {
                            row.getCell(0).setCellStyle(cellStyle);
                            setXSSFComment(sheet, row.getRowNum(), 0, "该联单在excel中重复或已存在");
                        } else {
                            row.getCell(0).setCellStyle(cellStyle);
                            setHSSFComment(sheet, row.getRowNum(), 0, "该联单在excel中重复或已存在");
                        }
                    }
                }
            }
        }

        return repeat;

    }


    /**
     * @author: chengzq
     * @date: 2020/9/24 0024 下午 6:19
     * @Description: 组装数据有错误设置批注（省内联单）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sheetAt, lastRowNum, username, pollutionMap, wasteMap, cellStyle, originalFilename, Month, flag, addlist]
     * @throws:
     */
    private TransferListVO AssembleDataWithCommentInside(Sheet sheetAt, Row row, String username, Map<String, String> pollutionMap, Map<String, String> wasteMap, CellStyle cellStyle, String originalFilename, Boolean[] isStore) {
        Date date = new Date();
        TransferListVO transferListVO = new TransferListVO();
        transferListVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(date));
        transferListVO.setUpdateuser(username);
        transferListVO.setPkid(UUID.randomUUID().toString());


        String pollutionid = pollutionMap.get(getRowStringValue(row, 2));
        String wastecode = wasteMap.get(getRowStringValue(row, 7));
        if (StringUtils.isBlank(pollutionid) || StringUtils.isBlank(wastecode)) {
            //如果企业不存在添加批注
            if (StringUtils.isBlank(pollutionid)) {
                row.getCell(2).setCellStyle(cellStyle);
                if (originalFilename.endsWith(".xlsx")) {
                    setXSSFComment(sheetAt, row.getRowNum(), 2, "该企业不存在");
                } else {
                    setHSSFComment(sheetAt, row.getRowNum(), 2, "该企业不存在");
                }
            }
            if (StringUtils.isBlank(wastecode)) {
                row.getCell(7).setCellStyle(cellStyle);
                if (originalFilename.endsWith(".xlsx")) {
                    setXSSFComment(sheetAt, row.getRowNum(), 7, "该危险类别不存在");
                } else {
                    setHSSFComment(sheetAt, row.getRowNum(), 7, "该危险类别不存在");
                }
            }
            isStore[0] = false;
        }
        transferListVO.setTransferlistnum(getRowStringValue(row, 0));//联单编号
        transferListVO.setFkProductentid(pollutionMap.get(getRowStringValue(row, 2)));//转出企业
        transferListVO.setReceiveentname(getRowStringValue(row, 4));//经营企业
        transferListVO.setTransportstartdate(getRowDateValue(row, 5));//出厂日期
        transferListVO.setTransportenddate(getRowDateValue(row, 6));//接收日期
        transferListVO.setFkWastematerialcode(wasteMap.get(getRowStringValue(row, 7)));//危废名称
        transferListVO.setMaterialdetailname(getRowStringValue(row, 8));//废物名称
        transferListVO.setMaterialstate(getRowStringValue(row, 11));//主要成分物理状态
//        transferListVO.setOuttransportpurpose(getRowStringValue(row,12));//经营方式
        transferListVO.setTransferquantity(getRowDoubleValue(isStore, cellStyle, sheetAt, row, 13, originalFilename));//转移量
        transferListVO.setReceivevolume(getRowDoubleValue(isStore, cellStyle, sheetAt, row, 14, originalFilename));//实际接收量
        transferListVO.setPackingway(getRowStringValue(row, 15));//包装形态
        transferListVO.setQuantityunit(getRowStringValue(row, 16));//单位
        transferListVO.setTransferentname(getRowStringValue(row, 17));//运输公司
        transferListVO.setFirsttransporttoolnum(getRowStringValue(row, 18));//运输车牌
        transferListVO.setFirsttransportmidaddr(getRowStringValue(row, 19));//途径地市
        transferListVO.setFirsttransportendaddr(getRowStringValue(row, 20));//经营设施地址
        transferListVO.setState(wasteMap.get(getRowStringValue(row, 21)));//状态
        transferListVO.setFkTransferareatypecode("2");//省内

        return transferListVO;
    }


    /**
     * @author: chengzq
     * @date: 2020/9/24 0024 下午 6:19
     * @Description: 组装数据有错误设置批注（省外联单）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [sheetAt, lastRowNum, username, pollutionMap, wasteMap, cellStyle, originalFilename, Month, flag, addlist]
     * @throws:
     */
    private TransferListVO AssembleDataWithCommentOutside(Sheet sheetAt, Row row, String username, Map<String, String> pollutionMap, Map<String, String> wasteMap, CellStyle cellStyle, String originalFilename, Boolean[] isStore) {
        Date date = new Date();
        TransferListVO transferListVO = new TransferListVO();
        transferListVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(date));
        transferListVO.setUpdateuser(username);
        transferListVO.setPkid(UUID.randomUUID().toString());


        String pollutionid = pollutionMap.get(getRowStringValue(row, 2));
        String wastecode = wasteMap.get(getRowStringValue(row, 7));
        if (StringUtils.isBlank(pollutionid) || StringUtils.isBlank(wastecode)) {
            //如果企业不存在添加批注
            if (StringUtils.isBlank(pollutionid)) {
                row.getCell(3).setCellStyle(cellStyle);
                if (originalFilename.endsWith(".xlsx")) {
                    setXSSFComment(sheetAt, row.getRowNum(), 3, "该企业不存在");
                } else {
                    setHSSFComment(sheetAt, row.getRowNum(), 3, "该企业不存在");
                }
            }
            if (StringUtils.isBlank(wastecode)) {
                row.getCell(6).setCellStyle(cellStyle);
                if (originalFilename.endsWith(".xlsx")) {
                    setXSSFComment(sheetAt, row.getRowNum(), 6, "该危险类别不存在");
                } else {
                    setHSSFComment(sheetAt, row.getRowNum(), 6, "该危险类别不存在");
                }
            }
            isStore[0] = false;
        }
        transferListVO.setTransferlistnum(getRowStringValue(row, 0));//联单编号
        transferListVO.setFkProductentid(pollutionMap.get(getRowStringValue(row, 3)));//转出企业
        transferListVO.setReceiveentname(getRowStringValue(row, 5));//经营企业
        transferListVO.setFkWastematerialcode(wasteMap.get(getRowStringValue(row, 6)));//危废名称
        transferListVO.setMaterialstate(getRowStringValue(row, 9));//主要成分物理状态
//        transferListVO.setOuttransportpurpose(getRowStringValue(row,10));//经营方式
        transferListVO.setTransferquantity(getRowDoubleValue(isStore, cellStyle, sheetAt, row, 11, originalFilename));//转移量
        transferListVO.setQuantityunit(getRowStringValue(row, 12));//单位
        transferListVO.setTransferentname(getRowStringValue(row, 13));//运输公司
        transferListVO.setFirsttransporttoolnum(getRowStringValue(row, 14));//运输车牌
        transferListVO.setFirsttransportmidaddr(getRowStringValue(row, 15));//途径地市
        transferListVO.setFkTransferareatypecode("1");//省外

        return transferListVO;
    }

    /**
     * @Description: 转移联单年统计（转移联单数、省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/05/19 10:44
     */
    @RequestMapping(value = "/countTransferYearNumData", method = RequestMethod.POST)
    public Object countTransferYearNumData(@RequestJson(value = "starttime") String starttime,
                                            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("startyear", starttime);
            paramMap.put("endyear", endtime);
            List<Map<String, Object>> dataList = transferListService.getTransferListInfoByParamMap(paramMap);
            Map<String,Object> resultMap = new HashMap<>();
            if (dataList.size()>0){
                resultMap.put("listnum",dataList.size());
                Double subNum;
                Map<String,Double> typeAndNum = new HashMap<>();
                String type;
                for (Map<String,Object> dataMap:dataList){
                    if (dataMap.get("TransferQuantity")!=null){
                        subNum = Double.parseDouble(dataMap.get("TransferQuantity").toString());
                        if (dataMap.get("FK_TransferAreaTypeCode")!=null){
                            type =dataMap.get("FK_TransferAreaTypeCode").toString();
                            typeAndNum.put(type,typeAndNum.get(type)!=null?typeAndNum.get(type)+subNum:subNum);
                        }
                    }
                }
                Double insidenum  = typeAndNum.get(CommonTypeEnum.TransferAreaTypeEnum.insideEnum.getCode());
                if (insidenum!=null){
                    resultMap.put("insidenum",DataFormatUtil.SaveOneAndSubZero(insidenum));
                }else {
                    resultMap.put("insidenum",0);
                }
                Double outsidenum  = typeAndNum.get(CommonTypeEnum.TransferAreaTypeEnum.outsideEnum.getCode());
                if (outsidenum!=null){
                    resultMap.put("outsidenum",DataFormatUtil.SaveOneAndSubZero(outsidenum));
                }else {
                    resultMap.put("outsidenum",0);
                }
            }else {
                resultMap.put("listnum",0);
                resultMap.put("insidenum",0);
                resultMap.put("outsidenum",0);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @Description：按年分组统计危废转移数量（省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/05/19 10:44
     */
    @RequestMapping(value = "/countTransferNumDataGroupByYear", method = RequestMethod.POST)
    public Object countTransferNumDataGroupByYear(@RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String,Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String,Object>> datalist= transferListService.countTransferNumDataGroupByYear(paramMap);
            if (datalist != null && datalist.size() > 0){
                //分组
                Map<String, List<Map<String, Object>>> collect = datalist.stream().filter(m -> m.get("transferareatypecode") != null).collect(Collectors.groupingBy(m -> m.get("transferareatypecode").toString()));
                for (Map.Entry<String, List<Map<String, Object>>> entry : collect.entrySet()) {
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("transferareatypecode", entry.getKey());
                    if ("1".equals(entry.getKey())) {
                        onemap.put("transferareatypename", "跨省转出");
                    }else if("2".equals(entry.getKey())){
                        onemap.put("transferareatypename", "省内转移");
                    }
                    onemap.put("valuelist",entry.getValue());
                    if (onemap.get("transferareatypename")!=null){
                        result.add(onemap);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description：按月分组统计某年危废转移数量（省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/05/19 10:44
     */
    @RequestMapping(value = "/countTransferNumDataGroupByMonth", method = RequestMethod.POST)
    public Object countTransferNumDataGroupByMonth(@RequestJson(value = "yeartime") String yeartime ) {

        try {
            List<Map<String,Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monthstarttime", yeartime+"-01");
            paramMap.put("monthendtime", yeartime+"-12");
            List<Map<String,Object>> datalist= transferListService.countTransferNumDataGroupByYear(paramMap);
            if (datalist != null && datalist.size() > 0){
                String yearmonth = "";
                for (int i=1;i<=12;i++){
                    Map<String,Object> map = new HashMap<>();
                    yearmonth = yeartime+"-"+(i<10?"0"+i:i+"");
                    map.put("monitortime",yearmonth);
                    map.put("province_in","");
                    map.put("province_out","");
                    for (Map<String,Object> onemap:datalist){
                        if(onemap.get("outputcategorycode")!=null && onemap.get("monthtime")!=null && yearmonth.equals(onemap.get("monthtime").toString())){
                            if("1".equals(onemap.get("transferareatypecode").toString())){
                                map.put("province_out",onemap.get("num")!=null?onemap.get("num").toString():"");
                            }
                            if("2".equals(onemap.get("transferareatypecode").toString())){
                                map.put("province_in",onemap.get("num")!=null?onemap.get("num").toString():"");
                            }
                        }
                    }
                result.add(map);
                }
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description：统计按危废父级种类分组的危废占比情况（省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/05/19 10:44
     */
    @RequestMapping(value = "/countTransferNumDataGroupByParentCode", method = RequestMethod.POST)
    public Object countTransferNumDataGroupByParentCode(@RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "endtime") String endtime,
                                                        @RequestJson(value = "transferareatypecode") Integer transferareatypecode) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime",starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("transferareatypecode", transferareatypecode);
            List<Map<String,Object>> datalist= transferListService.countTransferNumDataGroupByParentCode(paramMap);
            List<Map<String,Object>> onelist = new ArrayList<>();
            Double total = 0d;
            if (datalist!=null && datalist.size()>0){
                for (Map<String,Object> map:datalist){
                    if (map.get("num")!=null && !"".equals(map.get("num").toString())){
                        total += Double.valueOf(map.get("num").toString());
                        onelist.add(map);
                    }
                }
                if (total>0) {
                    for (Map<String, Object> map : onelist) {
                        map.put("proportion", DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(map.get("num").toString()) *100 /total));
                    }
                }
            }
            paramMap.clear();
            paramMap.put("total",total);
            paramMap.put("valuedata",onelist);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, paramMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }
}

package com.tjpu.pk.common.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: zhangzhenchao
 * @description: excel工具类
 * @create: 2018-10-19 10:42
 * @version: V1.0
 */
public class ExcelUtil {

    /**
     * this method is for workbook
     *
     * @param sheetName    表格sheet名
     * @param headers      表格属性列名数组
     * @param headersField 表格属性列名数组所对应的Map的Key值的集合
     * @param excelData    需要显示的数据集合,集合中一定要放置符合Map风格的类的对象。此方法支持的
     *                     javabean属性的数据类型有基本数据类型及String,Date,byte[](图片数据)
     * @param pattern      如果有时间数据，设定输出格式。
     */
    public static HSSFWorkbook exportExcel(String sheetName, List<String> headers, List<String> headersField, List<Map<String, Object>> excelData,
                                           String pattern) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        int index = 0;
        if (StringUtils.isBlank(sheetName)) {
            sheetName = "sheet_" + new Date().getTime();
        }
        HSSFSheet sheet = workbook.createSheet(sheetName);
        HSSFRow row = sheet.createRow(0);
        HSSFCellStyle columnTopStyle = getColumnTopStyle(workbook);
        for (int i = 0; i < headers.size(); i++) {
            HSSFCell cell = row.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers.get(i));
            cell.setCellValue(text);
            cell.setCellStyle(columnTopStyle);
        }

        HSSFCellStyle blackStyle = getStyleByMark(workbook, HSSFColor.BLACK.index);
        HSSFCellStyle redStyle = getStyleByMark(workbook, HSSFColor.RED.index);
        HSSFCellStyle ORANGEStyle = getStyleByMark(workbook, HSSFColor.LIGHT_ORANGE.index);

        HSSFCellStyle style = getStyleByMark(workbook, HSSFColor.BLACK.index);

        short colorValue;
        String keyString;
        String key;
        for (Map<String, Object> anExcelData : excelData) {
            index++;
            row = sheet.createRow(index);
            int m = 0;
            for (short n = 0; n < headersField.size(); n++) {
                if (n == 0) m = 0;
                HSSFCell cell = row.createCell(m);
                m++;
                Object value = null;

                Set<String> codes = new HashSet<>();

                for (Map.Entry<String, Object> entry : anExcelData.entrySet()) {
                    keyString = entry.getKey();
                    if (keyString.indexOf("#") > 0) {
                        key = keyString.split("#")[0];
                    } else {
                        key = keyString;
                    }

                    if (key != null && !codes.contains(key) && key.equalsIgnoreCase(headersField.get(n))) {
                        if (keyString.indexOf("#") > 0) {
                            codes.add(key);
                            colorValue = getColorValue(keyString);
                        } else {
                            colorValue = HSSFColor.BLACK.index;
                        }
                        if (colorValue == HSSFColor.LIGHT_ORANGE.index) {
                            style = ORANGEStyle;
                        } else if (colorValue == HSSFColor.RED.index) {
                            style = redStyle;
                        } else {
                            style = blackStyle;
                        }
                        value = entry.getValue();
                    }
                }
                // 判断值的类型后进行强制类型转换
                if (value instanceof Date) {
                    Date date = (Date) value;
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    String   textValue = sdf.format(date);
                    cell.setCellValue(textValue);
                } else if (value instanceof Double){
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else if (value instanceof Integer){
                    cell.setCellValue(Integer.parseInt(value.toString()));
                }else{// 其它数据类型都当作字符串处理
                    if (value!=null){
                        String valueString = value.toString();
                        if (isNumeric(valueString)){
                            cell.setCellValue(Double.parseDouble(valueString));
                        }else {
                            cell.setCellValue(valueString);
                        }
                    }
                }
                cell.setCellStyle(style);
            }
        }
        for (int k = 0; k < headers.size(); k++) {
            sheet.autoSizeColumn(k);
        }
        setSizeColumn(sheet, headers.size());
        return workbook;
    }


    public static boolean isNumeric(String str){
        Pattern Zpattern = Pattern.compile("[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");
        Pattern Fpattern = Pattern.compile("-?[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");



        Matcher isZNum = Zpattern.matcher(str);
        Matcher isFNum = Fpattern.matcher(str);
        boolean isNumber = false;
        if( isZNum.matches() ||isFNum.matches()){
            isNumber = true;
        }
        return isNumber;
    }

    private static short getColorValue(String key) {
        String[] keys = key.split("#");
        short colorValue = HSSFColor.BLACK.index;
        if (keys.length >= 2) {//有超标标记
            String isOver = keys[1];
            if (!"-1".equals(isOver)) {
                colorValue = HSSFColor.RED.index;
            }
            if ("true".equals(isOver)) {
                colorValue = HSSFColor.RED.index;
            }
            if ("-1".equals(isOver) || "false".equals(isOver)) {
                colorValue = HSSFColor.BLACK.index;
            }
        }
        if (keys.length >= 6) {//有flag标记
            String isException = keys[5];
            if (!"正常".equals(isException)&&!"null".equals(isException)) {
                colorValue = HSSFColor.LIGHT_ORANGE.index;
            }
        }else if (keys.length >= 3) {//有异常标记
            String isException = keys[2];
            if (!"0".equals(isException)) {
                colorValue = HSSFColor.LIGHT_ORANGE.index;
            }
        }

        return colorValue;
    }

    /**
     * this method is for workbook
     *
     * @param sheetName 表格sheet名
     * @param headers   表格属性列名数组
     * @param excelData 需要显示的数据集合,集合中一定要放置符合Map风格的类的对象。此方法支持的
     *                  javabean属性的数据类型有基本数据类型及String,Date,byte[](图片数据)
     * @param pattern   如果有时间数据，设定输出格式。
     */
    public static HSSFWorkbook exportManyHeaderExcel(String sheetName, List<Map<String, Object>> headers, List<Map<String, Object>> excelData,
                                                     String pattern) {

        HSSFWorkbook workbook = new HSSFWorkbook();

        if (StringUtils.isBlank(sheetName)) {
            sheetName = "sheet_" + new Date().getTime();
        }
        HSSFSheet sheet = workbook.createSheet(sheetName);

        HSSFCellStyle columnTopStyle = getColumnTopStyle(workbook);
        List<String> headerCodeList = new ArrayList<>();
        //占行数
        int rownum = 0;
        int rowmaxnum = 0;
        //占列数据
        int columnnum;
        //单元格索引
        int cellindex = 0;
        //表头名称
        String headername;
        //表头编码
        String headercode;
        //行索引
        int rowIndex = 0;
        //行对象
        HSSFRow row = sheet.createRow(rowIndex);
        //子表头数据
        List<Map<String, Object>> chlidHeader;
        for (Map<String, Object> map : headers) {
            headername = map.get("headername").toString();
            headercode = map.get("headercode").toString();

            row = sheet.getRow(rowIndex);
            HSSFCell cell = row.createCell(cellindex);
            HSSFRichTextString text = new HSSFRichTextString(headername);
            cell.setCellValue(text);
            cell.setCellStyle(columnTopStyle);
            //占1列
            columnnum = Integer.parseInt(map.get("columnnum").toString());
            //占2行
            rownum = Integer.parseInt(map.get("rownum").toString());

            if (rownum >= rowmaxnum) {
                rowmaxnum = rownum;
            }

            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, rownum - 1, cellindex, cellindex + (columnnum - 1));
            sheet.addMergedRegion(cellRangeAddress);
            chlidHeader = (List<Map<String, Object>>) map.get("chlidheader");
            if (chlidHeader != null && chlidHeader.size() > 0) {
                rowIndex = rowIndex + rownum;
                row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
                setChlidHeader(sheet, chlidHeader, headerCodeList, columnTopStyle, row, cellindex, rowIndex, workbook);
                rowIndex = 0;
            } else {
                headerCodeList.add(headercode);
            }
            cellindex = cellindex + columnnum;
            RegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook); // 下边框

            RegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook); // 左边框

            RegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook); // 有边框

            RegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook); // 上边框

        }
        rowIndex = rowmaxnum;


        HSSFCellStyle blackStyle = getStyleByMark(workbook, HSSFColor.BLACK.index);
        HSSFCellStyle redStyle = getStyleByMark(workbook, HSSFColor.RED.index);
        HSSFCellStyle ORANGEStyle = getStyleByMark(workbook, HSSFColor.LIGHT_ORANGE.index);

        HSSFCellStyle style = getStyleByMark(workbook, HSSFColor.BLACK.index);

        short colorValue;
        String keyString;
        String key;
        for (Map<String, Object> anExcelData : excelData) {

            row = sheet.createRow(rowIndex);
            int m = 0;
            for (short n = 0; n < headerCodeList.size(); n++) {
                if (n == 0) m = 0;
                HSSFCell cell = row.createCell(m);
                m++;
                Object value = null;
                Set<String> codes = new HashSet<>();
                for (Map.Entry<String, Object> entry : anExcelData.entrySet()) {
                    keyString = entry.getKey();
                    if (keyString.indexOf("#") > 0) {
                        key = keyString.split("#")[0];
                    } else {
                        key = keyString;
                    }
                    if (key != null && !codes.contains(key) && key.equalsIgnoreCase(headerCodeList.get(n))) {
                        if (keyString.indexOf("#") > 0
                                &&!keyString.contains("_sc")
                                &&!keyString.contains("_nd")&&!keyString.contains("_pfl")) {
                            codes.add(key);
                            colorValue = getColorValue(keyString);
                        } else {
                            colorValue = HSSFColor.BLACK.index;
                        }
                        if (colorValue == HSSFColor.LIGHT_ORANGE.index) {
                            style = ORANGEStyle;
                        } else if (colorValue == HSSFColor.RED.index) {
                            style = redStyle;
                        } else {
                            style = blackStyle;
                        }
                        value = entry.getValue();

                    }
                }
                if (value instanceof Date) {
                    Date date = (Date) value;
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    String   textValue = sdf.format(date);
                    cell.setCellValue(textValue);
                } else if (value instanceof Double){
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else if (value instanceof Integer){
                    cell.setCellValue(Integer.parseInt(value.toString()));
                }else{// 其它数据类型都当作字符串处理
                    if (value!=null){
                        String valueString = value.toString();
                        if (isNumeric(valueString)){
                            cell.setCellValue(Double.parseDouble(valueString));
                        }else {
                            cell.setCellValue(valueString);
                        }
                    }
                }
                cell.setCellStyle(style);
            }
            rowIndex++;
        }
        for (
                int k = 0; k < headers.size(); k++) {
            sheet.autoSizeColumn(k);
        }

        setSizeColumn(sheet, headers.size());
        return workbook;
    }

    private static void setChlidHeader(HSSFSheet sheet, List<Map<String, Object>> chlidHeader, List<String> headerCodeList,
                                       HSSFCellStyle columnTopStyle, HSSFRow row, int cellindex, int rowIndex, HSSFWorkbook workbook) {
        //占行数1
        int rownum;
        //占列数1
        int columnnum;
        //表头名称
        String headername;
        //表头编码
        String headercode;

        int tempNum = rowIndex;
        for (Map<String, Object> map : chlidHeader) {
            columnnum = Integer.parseInt(map.get("columnnum").toString());
            rownum = Integer.parseInt(map.get("rownum").toString());
            headername = map.get("headername").toString();
            headercode = map.get("headercode").toString();
            HSSFCell cell = row.getCell(cellindex);
            if (cell == null) {
                cell = row.createCell(cellindex);
            }
            HSSFRichTextString text = new HSSFRichTextString(headername);
            cell.setCellValue(text);
            cell.setCellStyle(columnTopStyle);
            CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex, rowIndex + rownum - 1, cellindex, cellindex + (columnnum - 1));
            sheet.addMergedRegion(cellRangeAddress);
            chlidHeader = (List<Map<String, Object>>) map.get("chlidheader");
            if (chlidHeader != null && chlidHeader.size() > 0) {
                rowIndex = rowIndex + rownum;
                row = sheet.getRow(rowIndex);
                setChlidHeader(sheet, chlidHeader, headerCodeList, columnTopStyle, row, cellindex, rowIndex, workbook);
                row = sheet.getRow(rowIndex);
                rowIndex = tempNum;
            } else {
                headerCodeList.add(headercode);
            }
            cellindex = cellindex + columnnum;
        }
    }


    /**
     * 中文自适应
     *
     * @param sheet
     * @param size
     */
    private static void setSizeColumn(HSSFSheet sheet, int size) {
        for (int columnNum = 0; columnNum < size; columnNum++) {
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                HSSFRow currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }
                if (currentRow.getCell(columnNum) != null) {
                    HSSFCell cell = currentRow.getCell(columnNum);
                    if (cell.getCellType() == cell.CELL_TYPE_STRING) {
                        int length = cell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        } else {
                            if (columnWidth > 254) {//超过最大宽度
                                columnWidth = 254;
                            }
                        }
                    }
                }
            }
            if(columnWidth<255){
                sheet.setColumnWidth(columnNum, columnWidth * 256);
            }else{
                sheet.setColumnWidth(columnNum,6000);
            }
           // sheet.setColumnWidth(columnNum, columnWidth * 256);
        }
    }


    /**
     * 表头样式
     *
     * @param workbook
     * @return
     */
    private static HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {
        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 11);
        //字体加粗
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("微软雅黑");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        return style;

    }

    /**
     * 内容样式
     *
     * @param workbook
     * @return
     */
    private static HSSFCellStyle getStyle(HSSFWorkbook workbook) {
        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        //font.setFontHeightInPoints((short)10);
        //字体加粗
        //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("微软雅黑");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(true);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        return style;
    }

    /**
     * 内容样式
     *
     * @param workbook
     * @return
     */
    private static HSSFCellStyle getStyleByMark(HSSFWorkbook workbook, short color) {
        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        //font.setFontHeightInPoints((short)10);
        //字体加粗
        //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("微软雅黑");
        font.setColor(color);
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(true);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        return style;
    }

    /**
     * 将workbook 转为 字节流
     *
     * @param workbook
     * @return
     * @throws IOException
     */
    public static synchronized byte[] getBytesForWorkBook(HSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.write(os);
        os.close();
        return os.toByteArray();
    }

    /**
     * 下载
     *
     * @param fileName
     * @param response
     * @param request
     * @param workbook
     * @throws IOException
     */
    public static void downLoadExcel(String fileName, HttpServletResponse response, HttpServletRequest request, byte[] workbook) throws IOException {
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        response.setContentType("application/octet- stream");
        response.setCharacterEncoding("utf-8");
        String codedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        codedFileName = codedFileName.replaceAll("\\+", "%20");
        if (agent.contains("firefox")) {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1") + ".xls");
        } else if (agent.contains("IE")) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "No-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        }
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("isStream", "true");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(workbook);
        outputStream.flush();
        outputStream.close();
    }


    public static void downLoadExcel(String fileName, HttpServletResponse response, HttpServletRequest request, byte[] workbook, String extName) throws IOException {
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        response.setContentType("application/octet- stream");
        response.setCharacterEncoding("utf-8");
        String codedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        codedFileName = codedFileName.replaceAll("\\+", "%20");
        if (agent.contains("firefox")) {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1") + extName);
        } else if (agent.contains("IE")) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "No-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + extName);
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + extName);
        }
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("isStream", "true");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(workbook);
        outputStream.flush();
        outputStream.close();
    }


    /**
     * 下载
     *
     * @param fileName
     * @param response
     * @param request
     * @param fileBytes
     * @throws IOException
     */
    public static void downLoadFile(String fileName, HttpServletResponse response, HttpServletRequest request, byte[] fileBytes) throws IOException {
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        response.setContentType("application/octet- stream");
        response.setCharacterEncoding("utf-8");
        String codedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        codedFileName = codedFileName.replaceAll("\\+", "%20");
        if (agent.contains("firefox")) {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1"));
        } else if (agent.contains("IE")) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "No-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName);
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName);
        }
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("isStream", "true");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(fileBytes);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * @author: lip
     * @date: 2019/5/30 0030 上午 10:44
     * @Description: 导出excel文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: fileName：导出文件名称（可以为空，默认“导出文件_时间戳”，例如导出文件_201905301047.xls）
     * @param: response：响应(非空)
     * @param: request：请求（非空）
     * @param: sheetName：sheet名称（可以为空，默认“sheet__时间戳”，例如sheet__201905301047）
     * @param: headers：表头中文名称（非空）
     * @param: headersField：表头编码名称（非空）
     * @param: excelData：表格数据（非空，表头编码+表数据）
     * @param: pattern：数据格式（可以为空）
     * @return:
     */
    public static void exportExcelFile(String fileName,
                                       HttpServletResponse response,
                                       HttpServletRequest request,
                                       String sheetName,
                                       List<String> headers,
                                       List<String> headersField,
                                       List<Map<String, Object>> excelData,
                                       String pattern
    ) throws IOException {
        //设置Excel
        HSSFWorkbook workbook = ExcelUtil.exportExcel(sheetName, headers, headersField, excelData, pattern);
        //转换字节流
        byte[] workbookByte = ExcelUtil.getBytesForWorkBook(workbook);
        //设置响应头，下载文件
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        response.setContentType("application/octet- stream");
        response.setCharacterEncoding("utf-8");
        if (StringUtils.isBlank(fileName)) {
            fileName = "导出文件_" + new Date().getTime();
        }
        String codedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        codedFileName = codedFileName.replaceAll("\\+", "%20");
        if (agent.contains("firefox")) {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1") + ".xls");
        } else if (agent.contains("IE")) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "No-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        }
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("isStream", "true");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(workbookByte);
        outputStream.flush();
        outputStream.close();
    }


    /**
     * @author: lip
     * @date: 2019/5/30 0030 上午 10:44
     * @Description: 导出excel文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: fileName：导出文件名称（可以为空，默认“导出文件_时间戳”，例如导出文件_201905301047.xls）
     * @param: response：响应(非空)
     * @param: request：请求（非空）
     * @param: sheetName：sheet名称（可以为空，默认“sheet__时间戳”，例如sheet__201905301047）
     * @param: headers：表头中文名称（非空）
     * @param: headersField：表头编码名称（非空）
     * @param: excelData：表格数据（非空，表头编码+表数据）
     * @param: pattern：数据格式（可以为空）
     * @return:
     */
    public static void exportManyHeaderExcelFile(String fileName,
                                                 HttpServletResponse response,
                                                 HttpServletRequest request,
                                                 String sheetName,
                                                 List<Map<String, Object>> headers,
                                                 List<Map<String, Object>> excelData,
                                                 String pattern
    ) throws IOException {
        //设置Excel
        HSSFWorkbook workbook = ExcelUtil.exportManyHeaderExcel(sheetName, headers, excelData, pattern);
        //转换字节流
        byte[] workbookByte = ExcelUtil.getBytesForWorkBook(workbook);
        //设置响应头，下载文件
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        response.setContentType("application/octet- stream");
        response.setCharacterEncoding("utf-8");
        if (StringUtils.isBlank(fileName)) {
            fileName = "导出文件_" + new Date().getTime();
        }
        String codedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        codedFileName = codedFileName.replaceAll("\\+", "%20");
        if (agent.contains("firefox")) {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1") + ".xls");
        } else if (agent.contains("IE")) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "No-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        }
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("isStream", "true");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(workbookByte);
        outputStream.flush();
        outputStream.close();
    }


    /**
     * @author: lip
     * @date: 2019/5/30 0030 上午 11:20
     * @Description: 设置导出文件数据格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<String> setExportTableDataByKey(List<Map<String, Object>> tabletitledata, String key) {
        List<String> strings = new ArrayList<>();
        for (Map<String, Object> map : tabletitledata) {
            strings.add(map.get(key).toString());
        }
        return strings;

    }

    /**
     * @author: lip
     * @date: 2019/5/30 0030 上午 11:20
     * @Description: 设置导出文件数据格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyExportHeaderDataByKey(List<Map<String, Object>> tabletitledata) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> children;

        for (Map<String, Object> titleMap : tabletitledata) {
            Map<String, Object> dataMap = new HashMap<>();


            List<Map<String, Object>> childrenList = new ArrayList<>();
            if (titleMap.get("children") != null) {
                children = (List<Map<String, Object>>) titleMap.get("children");
                for (Map<String, Object> chlid : children) {
                    Map<String, Object> chlidMap = new HashMap<>();
                    chlidMap.put("headername", chlid.get("label"));
                    chlidMap.put("headercode", chlid.get("prop"));
                    chlidMap.put("rownum", "1");
                    chlidMap.put("columnnum", "1");
                    childrenList.add(chlidMap);
                }
                dataMap.put("columnnum", children.size());
                dataMap.put("rownum", "1");
            } else {
                dataMap.put("columnnum", "1");
                dataMap.put("rownum", "2");
            }
            dataMap.put("headername", titleMap.get("label"));
            dataMap.put("headercode", titleMap.get("prop"));
            dataMap.put("chlidheader", childrenList);
            dataList.add(dataMap);
        }
        return dataList;

    }

    /**
     * @author: lip
     * @date: 2019/5/30 0030 上午 11:20
     * @Description: 设置导出文件数据格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyExportValueDataByKey(List<Map<String, Object>> tableListdata) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        Object value;
        for (Map<String, Object> map : tableListdata) {
            Map<String, Object> dataMap = new HashMap<>();
            for (String key : map.keySet()) {
                value = map.get(key);
                dataMap.put(key, value);
                if (key.indexOf("#") >= 0) {
                    key = key.split("#")[0];
                }
                dataMap.put(key, value);
            }
            dataList.add(dataMap);
        }
        return dataList;

    }


    /**
     * @author: xsm
     * @date: 2019/12/10 0010 上午 10:37
     * @Description: 导出excel文件(各类型数据报表调用)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: fileName：导出文件名称（可以为空，默认“导出文件_时间戳”，例如导出文件_201905301047.xls）
     * @param: response：响应(非空)
     * @param: request：请求（非空）
     * @param: sheetName：sheet名称（可以为空，默认“sheet__时间戳”，例如sheet__201905301047）
     * @param: headers：表头中文名称（非空）
     * @param: headersField：表头编码名称（非空）
     * @param: excelData：表格数据（非空，表头编码+表数据）
     * @param: pattern：数据格式（可以为空）
     * @param: titlename：表格标题名称
     * @return:
     */
    public static void exportManyHeaderExcelDataReportFile(String fileName,
                                                           HttpServletResponse response,
                                                           HttpServletRequest request,
                                                           String sheetName,
                                                           List<Map<String, Object>> headers,
                                                           List<Map<String, Object>> excelData,
                                                           String pattern,
                                                           String titlename
    ) throws IOException {
        //设置Excel
        HSSFWorkbook workbook = ExcelUtil.exportDataReportExcel(sheetName, headers, excelData, pattern, titlename);
        //转换字节流
        byte[] workbookByte = ExcelUtil.getBytesForWorkBook(workbook);
        //设置响应头，下载文件
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        response.setContentType("application/octet- stream");
        response.setCharacterEncoding("utf-8");
        if (StringUtils.isBlank(fileName)) {
            fileName = "导出文件_" + new Date().getTime();
        }
        String codedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        codedFileName = codedFileName.replaceAll("\\+", "%20");
        if (agent.contains("firefox")) {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1") + ".xls");
        } else if (agent.contains("IE")) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "No-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        }
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("isStream", "true");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(workbookByte);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * this method is for workbook
     *
     * @param sheetName 表格sheet名
     * @param headers   表格属性列名数组
     * @param excelData 需要显示的数据集合,集合中一定要放置符合Map风格的类的对象。此方法支持的
     *                  javabean属性的数据类型有基本数据类型及String,Date,byte[](图片数据)
     * @param pattern   如果有时间数据，设定输出格式。
     * @param titlename excel表中标题名称。
     */
    public static HSSFWorkbook exportDataReportExcel(String sheetName, List<Map<String, Object>> headers, List<Map<String, Object>> excelData,
                                                     String pattern, String titlename) {

        HSSFWorkbook workbook = new HSSFWorkbook();

        if (StringUtils.isBlank(sheetName)) {
            sheetName = "sheet_" + new Date().getTime();
        }
        HSSFSheet sheet = workbook.createSheet(sheetName);

        HSSFCellStyle columnTopStyle = getColumnTopStyle(workbook);
        List<String> headerCodeList = new ArrayList<>();
        //占行数
        int rownum = 0;
        int rowmaxnum = 0;
        //占列数据
        int columnnum;
        //单元格索引
        int cellindex = 0;
        //总单元格索引
        int totalcellindex = 0;
        //表头名称
        String headername;
        //表头编码
        String headercode;
        //行索引
        int rowIndex = 1;
        //行对象
        HSSFRow row = sheet.createRow(rowIndex);
        //子表头数据
        List<Map<String, Object>> chlidHeader;
        for (Map<String, Object> map : headers) {
            headername = map.get("headername").toString();
            headercode = map.get("headercode").toString();

            row = sheet.getRow(rowIndex);
            HSSFCell cell = row.createCell(cellindex);
            HSSFRichTextString text = new HSSFRichTextString(headername);
            cell.setCellValue(text);
            cell.setCellStyle(columnTopStyle);
            //占1列
            columnnum = Integer.parseInt(map.get("columnnum").toString());
            //占2行
            rownum = Integer.parseInt(map.get("rownum").toString()) + 1;
            if (rownum >= rowmaxnum) {
                rowmaxnum = rownum;
            }

            CellRangeAddress cellRangeAddress = new CellRangeAddress(1, rownum - 1, cellindex, cellindex + (columnnum - 1));
            sheet.addMergedRegion(cellRangeAddress);
            chlidHeader = (List<Map<String, Object>>) map.get("chlidheader");
            if (chlidHeader != null && chlidHeader.size() > 0) {
                rowIndex = rowIndex + rownum - 1;
                row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
                setChlidHeader(sheet, chlidHeader, headerCodeList, columnTopStyle, row, cellindex, rowIndex, workbook);
                rowIndex = 1;
            } else {
                headerCodeList.add(headercode);
            }
            cellindex = cellindex + columnnum;
            if (totalcellindex < cellindex) {
                totalcellindex = cellindex;
            }
            RegionUtil.setBorderBottom(1, cellRangeAddress, sheet, workbook); // 下边框

            RegionUtil.setBorderLeft(1, cellRangeAddress, sheet, workbook); // 左边框

            RegionUtil.setBorderRight(1, cellRangeAddress, sheet, workbook); // 有边框

            RegionUtil.setBorderTop(1, cellRangeAddress, sheet, workbook); // 上边框

        }
        rowIndex = rowmaxnum;

        HSSFCellStyle blackStyle = getStyleByMark(workbook, HSSFColor.BLACK.index);
        HSSFCellStyle redStyle = getStyleByMark(workbook, HSSFColor.RED.index);
        HSSFCellStyle ORANGEStyle = getStyleByMark(workbook, HSSFColor.LIGHT_ORANGE.index);

        HSSFCellStyle style = getStyleByMark(workbook, HSSFColor.BLACK.index);
        short colorValue;
        String keyString;
        String key;
        for (Map<String, Object> anExcelData : excelData) {

            row = sheet.createRow(rowIndex);
            int m = 0;
            for (short n = 0; n < headerCodeList.size(); n++) {
                if (n == 0) m = 0;
                HSSFCell cell = row.createCell(m);
                m++;
                Object value = null;
                Set<String> codes = new HashSet<>();
                for (Map.Entry<String, Object> entry : anExcelData.entrySet()) {
                    keyString = entry.getKey();
                    if (keyString.indexOf("#") > 0) {
                        key = keyString.split("#")[0];
                    } else {
                        key = keyString;
                    }
                    if (key != null && !codes.contains(key) && key.equalsIgnoreCase(headerCodeList.get(n))) {
                        if (keyString.indexOf("#") > 0) {
                            codes.add(key);
                            colorValue = getColorValue(keyString);
                        } else {
                            colorValue = HSSFColor.BLACK.index;
                        }
                        if (colorValue == HSSFColor.LIGHT_ORANGE.index) {
                            style = ORANGEStyle;
                        } else if (colorValue == HSSFColor.RED.index) {
                            style = redStyle;
                        } else {
                            style = blackStyle;
                        }
                        value = entry.getValue();

                    }
                }
                // 判断值的类型后进行强制类型转换
                String textValue;
                if (value instanceof Date) {
                    Date date = (Date) value;
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    textValue = sdf.format(date);
                    if (textValue != null) {
                        cell.setCellValue(textValue);
                    }
                } else if(value instanceof Double){
                    if (value != null&&!"".equals(value.toString())) {
                        cell.setCellValue(Double.valueOf(value.toString()));
                    }
                }else {
                    // 其它数据类型都当作字符串处理
                    textValue = value == null ? null : value.toString();
                    if (textValue != null) {
                        cell.setCellValue(textValue);
                    }
                }

                cell.setCellStyle(style);
            }
            rowIndex++;
        }
        for (int k = 0; k < headers.size(); k++) {
            sheet.autoSizeColumn(k);
        }
        setSizeColumn(sheet, totalcellindex);
        //设置标题到第一行
        HSSFCellStyle titleStyle = getColumnTitleStyle(workbook);        //标题样式
        row = sheet.createRow(0);
        // 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
        HSSFCell cell0 = null;
        cell0 = row.createCell(0);
        // 合并单元格CellRangeAddress构造参数依次表示起始行，截至行，起始列， 截至列
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalcellindex - 1));
        // 设置单元格内容
        cell0.setCellValue(titlename);
        cell0.setCellStyle(titleStyle);
        return workbook;
    }

    /**
     * 表头样式
     *
     * @param workbook
     * @return
     */
    private static HSSFCellStyle getColumnTitleStyle(HSSFWorkbook workbook) {
        // 设置字体
        HSSFFont font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 16);
        //字体加粗
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("微软雅黑");
        //设置样式;
        HSSFCellStyle style = workbook.createCellStyle();
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        return style;
    }

    public static HSSFWorkbook exportExcelManySheet(HSSFWorkbook workbook, int sheetindex, String sheetName, List<String> headers, List<String> headersField, List<Map<String, Object>> excelData,
                                                    String pattern) {
        int index = 0;
        if (StringUtils.isBlank(sheetName)) {
            sheetName = "sheet_" + new Date().getTime();
        }
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(sheetindex, sheetName);

        HSSFRow row = sheet.createRow(0);
        HSSFCellStyle columnTopStyle = getColumnTopStyle(workbook);
        for (int i = 0; i < headers.size(); i++) {
            HSSFCell cell = row.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers.get(i));
            cell.setCellValue(text);
            cell.setCellStyle(columnTopStyle);
        }
        HSSFCellStyle style = getStyle(workbook);
        for (Map<String, Object> anExcelData : excelData) {
            index++;
            row = sheet.createRow(index);
            int m = 0;
            for (short n = 0; n < headersField.size(); n++) {
                if (n == 0) m = 0;
                HSSFCell cell = row.createCell(m);
                m++;
                Object value = null;
                String key = null;

                for (Map.Entry<String, Object> entry : anExcelData.entrySet()) {

                    key = entry.getKey();
                    if (key.indexOf("#") > 0) {
                        key = key.split("#")[0];
                    }
                    if (key != null && key.equalsIgnoreCase(headersField.get(n))) {
                        value = entry.getValue();
                    }
                }
                // 判断值的类型后进行强制类型转换
                if (value instanceof Date) {
                    Date date = (Date) value;
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    String   textValue = sdf.format(date);
                    cell.setCellValue(textValue);
                } else if (value instanceof Double){
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }else if (value instanceof Integer){
                    cell.setCellValue(Integer.parseInt(value.toString()));
                }else{// 其它数据类型都当作字符串处理
                    if (value!=null){
                        String valueString = value.toString();
                        if (isNumeric(valueString)){
                            cell.setCellValue(Double.parseDouble(valueString));
                        }else {
                            cell.setCellValue(valueString);
                        }
                    }
                }
                cell.setCellStyle(style);
            }
        }
        for (int k = 0; k < headers.size(); k++) {
            sheet.autoSizeColumn(k);
        }
        setSizeColumn(sheet, headers.size());
        return workbook;
    }


    /**
     * @author: xsm
     * @date: 2021/08/23 0023 下午 1:37
     * @Description: 导出多级表头（支持三级及以上）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: fileName：导出文件名称（可以为空，默认“导出文件_时间戳”，例如导出文件_201905301047.xls）
     * @param: response：响应(非空)
     * @param: request：请求（非空）
     * @param: sheetName：sheet名称（可以为空，默认“sheet__时间戳”，例如sheet__201905301047）
     * @param: headers：表头中文名称（非空）
     * @param: headersstartend：表头名称起始位置（非空）
     * @param: titlekeys：表头要展示内容对应key值，按展示顺序排列（非空）
     * @param: excelData：内容数据（非空）
     * @param: pattern：数据格式（可以为空）
     * @param: titlename：表格标题名称
     * @return:
     */
    public static void exportManyTitleHeaderExcelData(String fileName,
                                                           HttpServletResponse response,
                                                           HttpServletRequest request,
                                                           String sheetName,
                                                           String[][] headers,
                                                           String[][] headersstartend,
                                                           List<String> titlekeys,
                                                           List<Map<String, Object>> excelData,
                                                           String pattern,
                                                           String titlename
    ) throws IOException {
        //设置Excel
        HSSFWorkbook workbook = ExcelUtil.exportManyTitleHeaderExcel(sheetName, headers,headersstartend,titlekeys, excelData, pattern, titlename);
        //转换字节流
        byte[] workbookByte = ExcelUtil.getBytesForWorkBook(workbook);
        //设置响应头，下载文件
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        response.setContentType("application/octet- stream");
        response.setCharacterEncoding("utf-8");
        if (StringUtils.isBlank(fileName)) {
            fileName = "导出文件_" + new Date().getTime();
        }
        String codedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        codedFileName = codedFileName.replaceAll("\\+", "%20");
        if (agent.contains("firefox")) {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1") + ".xls");
        } else if (agent.contains("IE")) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "No-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        }
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("isStream", "true");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(workbookByte);
        outputStream.flush();
        outputStream.close();
    }



    /**
     * @author: xsm
     * @date: 2021/8/21 0021 下午 1:19
     * @Description: 生成多级表格
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static HSSFWorkbook exportManyTitleHeaderExcel(String sheetName, String[][] headers, String[][] headersstartend, List<String> titlekeys, List<Map<String, Object>> excelData, String pattern, String titlename) {
        // 声明一个工作簿
        HSSFWorkbook wb = new HSSFWorkbook();
        // 生成一个表格
        if (StringUtils.isBlank(sheetName)) {
            sheetName = "sheet_" + new Date().getTime();
        }
        //例台账危废记录
        /*List<String> keys = Arrays.asList("RecordDate","GeneratedNum","SCPersonCharge","ReceiptNum","InventoryBalance",
            "ZCPersonCharge","ReceivingUnit","OutsourceHandleNum","TransferNumber","TransferTime","WWPersonCharge",
            "SelfDisposalNum","ZXPersonCharge");
    String[][] excelHeader = {{"日  期", "产生情况","", "贮存情况","","", "处置利用情况","","","","","",""},
            { "","数量(吨)","责任人签名", "入库情况", "库存余量","责任人签名","委外处置/利用情况","","","","","自行处置情况",""},
            {"","","","数量(吨)","数量(吨)","","接收单位","数量(吨)","联单编号","转移时间","责任人签名","数量(吨)","责任人签名"}};
    String[][] headnum = {{"1,3,0,0", "1,1,1,2", "1,1,3,5", "1,1,6,12"},
            {"2,3,1,1", "2,3,2,2","2,2,3,3", "2,2,4,4","2,3,5,5",  "2,2,6,10", "2,2,11,12"},
            {"3,3,3,3","3,3,4,4","3,3,6,6","3,3,7,7","3,3,8,8","3,3,9,9","3,3,10,10","3,3,11,11","3,3,12,12"}};
    */

        HSSFSheet sheet = wb.createSheet(sheetName);
        // 生成一种样式
        HSSFCellStyle columnTopStyle = getColumnTopStyle(wb);
        HSSFCellStyle style2 = getStyleByMark(wb, HSSFColor.BLACK.index);
        int startrownmu =0;//起始行数
        if (!StringUtils.isBlank(titlename)) {
            //当表头名称不为空  起始行从第二行开始
            startrownmu =1;
        }
        for (int i = 0; i < headers.length; i++) {
            HSSFRow row = sheet.createRow(i+startrownmu);
            //row.setHeight((short)700);
            //String[] cellstrlist = headnum[i];
            for(int j=0; j<headers[i].length; j++){
               /* String[] temp = cellstrlist[j].split(",");
                Integer startcol = Integer.parseInt(temp[2]);*/
                HSSFCell cell = row.createCell(j);
                cell.setCellStyle(columnTopStyle);
                cell.setCellValue(headers[i][j]);
                int length = cell.getStringCellValue().getBytes().length;
                if (length>0) {
                    sheet.setColumnWidth(j, length * 256);
                }
            }
        }

        // 动态合并单元格
        for (int i = 0; i < headersstartend.length; i++) {
            String[] childherd = headersstartend[i];
            for (int j =0;j<childherd.length;j++){
                sheet.autoSizeColumn(j);
                //sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 17 / 10);
                String[] temp = childherd[j].split(",");
                Integer startrow = Integer.parseInt(temp[0]);
                Integer overrow = Integer.parseInt(temp[1]);
                Integer startcol = Integer.parseInt(temp[2]);
                Integer overcol = Integer.parseInt(temp[3]);
                sheet.addMergedRegion(new CellRangeAddress(startrow, overrow, startcol, overcol));
            }
            //sheet.autoSizeColumn(i, true);
        }
        // 数据行
        for (int i = 0; i < excelData.size(); i++) {
            HSSFRow row = sheet.createRow(i + startrownmu+headers.length);
            Map<String, Object> obj =excelData.get(i);
            for(int j=0;j< titlekeys.size();j++){
                HSSFCell cell = row.createCell(j);
                Object value = obj.get(titlekeys.get(j));
                // 判断值的类型后进行强制类型转换
                String textValue;
               if (value instanceof Date) {
                    Date date = (Date) value;
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    textValue = sdf.format(date);
                    if (textValue != null) {
                        cell.setCellValue(textValue);
                    }
                } else if(value instanceof Double){
                    if (value != null&&!"".equals(value.toString())) {
                        cell.setCellValue(Double.valueOf(value.toString()));
                    }
                }else {
                    // 其它数据类型都当作字符串处理
                    textValue = value == null ? null : value.toString();
                    if (textValue != null) {
                        cell.setCellValue(textValue);
                    }
                }
                cell.setCellStyle(style2);
            }

        }
        if (!StringUtils.isBlank(titlename)) {//设置标题
            //设置标题到第一行
            HSSFCellStyle titleStyle = getColumnTitleStyle(wb);        //标题样式
            HSSFRow row = sheet.createRow(0);
            // 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
            HSSFCell cell0 = null;
            cell0 = row.createCell(0);
            // 合并单元格CellRangeAddress构造参数依次表示起始行，截至行，起始列， 截至列
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, titlekeys.size() - 1));
            // 设置单元格内容
            cell0.setCellValue(titlename);
            cell0.setCellStyle(titleStyle);
        }
        return wb;
    }


}

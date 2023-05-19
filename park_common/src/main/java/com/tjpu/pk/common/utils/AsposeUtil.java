package com.tjpu.pk.common.utils;

import com.aspose.cells.*;

import com.aspose.pdf.devices.PngDevice;

import com.aspose.slides.Presentation;
import com.aspose.words.Document;

import com.aspose.words.ImageSaveOptions;

import com.aspose.words.SaveFormat;



import org.springframework.core.io.ClassPathResource;



import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author: lip
 * @date: 2018/9/5 0005 下午 5:14
 * @Description: 使用Aspose组件处理文件
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
public class AsposeUtil {


    private static List<String> wordList = new ArrayList<>();
    private static List<String> excelList = new ArrayList<>();
    private static List<String> pdfList = new ArrayList<>();
    private static List<String> imgList = new ArrayList<>();
    private static List<String> pptList = new ArrayList<>();


    static {
        //wordList.add("TXT");
        wordList.add("DOC");
        wordList.add("DOCX");


        excelList.add("XLSX");
        excelList.add("XLS");

        pdfList.add("PDF");

        imgList.add("BMP");
        imgList.add("JPG");
        imgList.add("JPEG");
        imgList.add("PNG");
        imgList.add("GIF");

        pptList.add("PPT");
        pptList.add("PPTX");
    }


    public static String parseFileToBase64(InputStream inputStream, int pageNum, String ext) throws Exception {
        // String png_base64 = "";
        List<BufferedImage> bufferedImages = new ArrayList<>();
        BufferedImage image = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//io流
        if (wordList.contains(ext.toUpperCase())) {
            bufferedImages = wordToImg(inputStream, pageNum);
            if (bufferedImages.size() > 0) {

                image = mergeImage(false, bufferedImages);
                ImageIO.write(image, "PNG", baos);//写入流中
            }
        } else if (excelList.contains(ext.toUpperCase())) {
            bufferedImages = ExcelToImg(inputStream, pageNum);
            if (bufferedImages.size() > 0) {
                image = mergeImage(false, bufferedImages);
                ImageIO.write(image, "PNG", baos);//写入流中
            }
        } else if (pdfList.contains(ext.toUpperCase())) {
            bufferedImages = pdfToImg(inputStream, pageNum);
            if (bufferedImages.size() > 0) {
                image = mergeImage(false, bufferedImages);
                ImageIO.write(image, "PNG", baos);//写入流中
            }
        } else if (imgList.contains(ext.toUpperCase())) {
            baos = parse(inputStream);
        } else if (pptList.contains(ext.toUpperCase())) {
            bufferedImages = pptToImg(inputStream, pageNum);
            if (bufferedImages.size() > 0) {
                image = mergeImage(false, bufferedImages);
                ImageIO.write(image, "PNG", baos);//写入流中
            }
        }
        byte[] bytes = baos.toByteArray();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return base64;

    }


    private static ByteArrayOutputStream thumbnailImage(InputStream in1, String fileName, int wide, int high) throws IOException {
        InputStream inThumb = null;

        String types = Arrays.toString(ImageIO.getReaderFormatNames()).replace("]", ",");
        String suffix = null;
        if (fileName.indexOf(".") > -1) {
            suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        }// 类型和图片后缀全部小写，然后判断后缀是否合法
        if (suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase() + ",") < 0) {
            return null;
        }
        Image img = ImageIO.read(in1);
        BufferedImage bi = new BufferedImage(wide, high, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        g.drawImage(img, 0, 0, wide, high, Color.LIGHT_GRAY, null);
        g.dispose();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut;
        imOut = ImageIO.createImageOutputStream(bs);
        ImageIO.write(bi, "jpg", imOut);
        inThumb = new ByteArrayInputStream(bs.toByteArray());
        return bs;
    }


    /**
     * @author: lip
     * @date: 2018/9/5 0005 下午 5:15
     * @Description: 验证aspose.word组件是否授权：无授权的文件有水印标记
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static boolean isWordLicense() {
        boolean result = false;
        try {

            ClassPathResource resource = new ClassPathResource("static/wordLicense.xml");
            InputStream inputStream = resource.getInputStream();

            com.aspose.words.License license = new com.aspose.words.License();
            license.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @author: lip
     * @date: 2018/9/5 0005 下午 5:15
     * @Description: 验证aspose.pdf组件是否授权：无授权的文件有水印标记
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static boolean isPdfLicense() {
        boolean result = false;
        try {

            ClassPathResource resource = new ClassPathResource("static/pdfLicense.xml");
            InputStream inputStream = resource.getInputStream();

            com.aspose.pdf.License license = new com.aspose.pdf.License();
            license.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * @author: lip
     * @date: 2018/9/5 0005 下午 5:15
     * @Description: 验证aspose.slides组件是否授权：无授权的文件有水印标记
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static boolean isPptLicense() {
        boolean result = false;
        try {

            ClassPathResource resource = new ClassPathResource("static/pptLicense.xml");
            InputStream inputStream = resource.getInputStream();


            com.aspose.slides.License license = new com.aspose.slides.License();
            license.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @author: lip
     * @date: 2018/9/5 0005 下午 5:15
     * @Description: 验证aspose.excel组件是否授权：无授权的文件有水印标记
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static boolean isExcelLicense() {
        boolean result = false;
        try {
            ClassPathResource resource = new ClassPathResource("/static/excelLicense.xml");
            InputStream inputStream = resource.getInputStream();
            com.aspose.cells.License license = new com.aspose.cells.License();
            license.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * @author: lip
     * @date: 2018/9/5 0005 下午 5:21
     * @Description: excel文件转换图片
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static List<BufferedImage> ExcelToImg(InputStream inputStream, int pageNum) throws Exception {
        if (!isExcelLicense()) {
            return null;
        }
        try {
            List<BufferedImage> imageList = new ArrayList<>();
            Workbook book = new Workbook(inputStream);
            ImageOrPrintOptions imgOptions = new ImageOrPrintOptions();
            imgOptions.setAllColumnsInOnePagePerSheet(true);
            imgOptions.setQuality(100);
            //设置图片类型
            imgOptions.setImageFormat(ImageFormat.getPng());
            Iterator<Worksheet> sheets = book.getWorksheets().iterator();
            int pageCount = 0;
            boolean isBreak = false;
            while (sheets.hasNext()) {
                Worksheet sheet = sheets.next();

                SheetRender sheetRender = new SheetRender(sheet, imgOptions);
                if (sheetRender != null) {
                    pageCount = sheetRender.getPageCount() + pageCount;

                    if (sheetRender.getPageCount() != 0) {
                        if (pageCount > pageNum) {//累计总数达到限制
                            pageCount = pageNum;
                            isBreak = true;
                        }
                        for (int j = 0; j < pageCount; j++) {
                            OutputStream output = new ByteArrayOutputStream();
                            //图片文件存入流中
                            sheetRender.toImage(j, output);
                            ImageInputStream imageInputStream = javax.imageio.ImageIO.createImageInputStream(parse(output));
                            BufferedImage bufferedImage = javax.imageio.ImageIO.read(imageInputStream);
                            if(bufferedImage!=null){
                                imageList.add(bufferedImage);
                            }
                        }
                        if (isBreak) {
                            break;
                        }
                    }
                }


            }
            return imageList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018/9/5 0005 下午 5:21
     * @Description: word和txt文件转换图片
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static List<BufferedImage> wordToImg(InputStream inputStream, int pageNum) throws Exception {
        if (!isWordLicense()) {
            return null;
        }
        try {
            long old = System.currentTimeMillis();
            Document doc = new Document(inputStream);
            ImageSaveOptions options = new ImageSaveOptions(SaveFormat.PNG);

            options.setPrettyFormat(true);
            options.setUseAntiAliasing(true);
            options.setUseHighQualityRendering(true);
            int pageCount = doc.getPageCount();
            if (pageCount > pageNum) {//生成前pageCount张
                pageCount = pageNum;
            }
            List<BufferedImage> imageList = new ArrayList<>();
            for (int i = 0; i < pageCount; i++) {
                OutputStream output = new ByteArrayOutputStream();
                options.setPageIndex(i);
                doc.save(output, options);

                BufferedImage bufferedImage = javax.imageio.ImageIO.read(parse(output));
                if(bufferedImage!=null){
                    imageList.add(bufferedImage);
                }
            }
            return imageList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2018/9/5 0005 下午 5:21
     * @Description: ppt文件转换图片
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static List<BufferedImage> pptToImg(InputStream inputStream, int pageNum) throws Exception {

        if (!isPptLicense()) {
            return null;
        }
        try {
            Presentation presentation = new Presentation(inputStream);

            OutputStream output = new ByteArrayOutputStream();

            presentation.save(output, com.aspose.slides.SaveFormat.Pdf);

            inputStream = parse(output);
            List<BufferedImage> imageList = pdfToImg(inputStream, pageNum);
            return imageList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2018/9/5 0005 下午 5:21
     * @Description: pdf文件转换图片
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static List<BufferedImage> pdfToImg(InputStream inputStream, int pageNum) throws Exception {

        if (!isPdfLicense()) {
            return null;
        }
        try {
            com.aspose.pdf.Document doc = new com.aspose.pdf.Document(inputStream);
            PngDevice device = new PngDevice();


            int pageCount = doc.getPages().size();
            if (pageCount > pageNum) {//生成前pageCount张
                pageCount = pageNum;
            }
            List<BufferedImage> imageList = new ArrayList<>();
            for (int i = 1; i <= pageCount; i++) {
                BufferedImage bufferedImage = device.processToBufferedImage(doc.getPages().get_Item(i));
                if(bufferedImage!=null){
                    imageList.add(bufferedImage);
                }
            }
            return imageList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 合并任数量的图片成一张图片
     *
     * @param isHorizontal true代表水平合并，fasle代表垂直合并
     * @param imgs         待合并的图片数组
     * @return
     * @throws IOException
     */
    public static BufferedImage mergeImage(boolean isHorizontal, List<BufferedImage> imgs) throws IOException {
        // 生成新图片
        BufferedImage destImage = null;
        // 计算新图片的长和高
        int allw = 0, allh = 0, allwMax = 0, allhMax = 0;
        // 获取总长、总宽、最长、最宽
        for (int i = 0; i < imgs.size(); i++) {
            BufferedImage img = imgs.get(i);
            allw += img.getWidth();

            if (imgs.size() != i + 1) {
                allh += img.getHeight() + 5;
            } else {
                allh += img.getHeight();
            }


            if (img.getWidth() > allwMax) {
                allwMax = img.getWidth();
            }
            if (img.getHeight() > allhMax) {
                allhMax = img.getHeight();
            }
        }


        // 创建新图片
        if (isHorizontal) {
            destImage = new BufferedImage(allw, allhMax, BufferedImage.TYPE_INT_RGB);
        } else {
            destImage = new BufferedImage(allwMax, allh, BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g2 = (Graphics2D) destImage.getGraphics();
        g2.setBackground(Color.LIGHT_GRAY);
        g2.clearRect(0, 0, allw, allh);
        g2.setPaint(Color.RED);

        // 合并所有子图片到新图片
        int wx = 0, wy = 0;
        for (int i = 0; i < imgs.size(); i++) {
            BufferedImage img = imgs.get(i);
            int w1 = img.getWidth();
            int h1 = img.getHeight();
            // 从图片中读取RGB
            int[] ImageArrayOne = new int[w1 * h1];
            ImageArrayOne = img.getRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 逐行扫描图像中各个像素的RGB到数组中
            if (isHorizontal) { // 水平方向合并
                destImage.setRGB(wx, 0, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            } else { // 垂直方向合并
                destImage.setRGB(0, wy, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            }
            wx += w1;
            wy += h1 + 5;
        }


        return destImage;
    }


    //outputStream转inputStream
    public static ByteArrayInputStream parse(OutputStream out) throws Exception {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos = (ByteArrayOutputStream) out;
        ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());


        return swapStream;
    }


    //inputStream转outputStream
    public static ByteArrayOutputStream parse(InputStream in) throws Exception {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            swapStream.write(buffer, 0, bytesRead);
        }
        return swapStream;
    }

}

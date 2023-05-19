package com.tjpu.pk.common.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Locale;
import java.util.Map;
/**
 *
 * @author: lip
 * @date: 2019/8/26 0026 下午 5:19
 * @Description: freemark工具类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
*/
public class FreeMarkerWordUtil {


    public static final String  defaultEncoding = "UTF-8";
    /**
     *
     * @author: lip
     * @date: 2019/8/26 0026 下午 5:19
     * @Description: 根据模板文件导出文件流数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    public static byte [] createWord(Map dataMap, String templateName) throws IOException, TemplateException {


        ByteArrayOutputStream out = null;
        OutputStreamWriter outWriter = null;
        byte [] bytes = null;
        try {
            //创建配置实例
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            //设置编码
            configuration.setDefaultEncoding(defaultEncoding);
            configuration.setEncoding(Locale.CHINA, defaultEncoding);
            configuration.setOutputEncoding(defaultEncoding);

            //ftl模板文件
            configuration.setClassForTemplateLoading(FreeMarkerWordUtil.class, "/");
            //获取模板
            Template template = configuration.getTemplate(templateName);
            out = new ByteArrayOutputStream();
            outWriter = new OutputStreamWriter(out, defaultEncoding);
            //生成文件
            template.process(dataMap, outWriter);
            bytes = out.toByteArray();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            if (out!=null){
                out.close();
            }
            if (outWriter!=null){
                outWriter.close();
            }
        }
    }

}

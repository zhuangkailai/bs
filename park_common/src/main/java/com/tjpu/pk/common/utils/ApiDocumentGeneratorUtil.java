package com.tjpu.pk.common.utils;
import io.swagger.annotations.*;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author: chengzq
 * @date: 2018/8/7  08:51
 * @Description:  //api文档生成工具类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class ApiDocumentGeneratorUtil {



    /**
     * @author: chengzq
     * @date: 2018/8/10  上午 11:10
     * @Description: 生成文档静态页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [springContextHolderUtil]
     * @throws:
     */
    public static <T> void apiDocumentGenerat(T springContextHolderUtil,String filePath,String fileName) throws Exception {

        try {
            ApplicationContext applicationContext = getApplicationContext(springContextHolderUtil);


            makeHtml(filePath,fileName,applicationContext);

         }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: chengzq
     * @date: 2018/8/7  上午 9:18
     * @Description: 拼接html静态文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [filePath,fileName, html]
     * @throws:
     */
    private static void makeHtml(String filePath,String fileName,ApplicationContext applicationContext){
        try{
            String[] beanNamesForAnnotation = applicationContext.getBeanNamesForAnnotation(Api.class);
            StringBuffer table;   //拼接的表格
            int titleFlagOne = 1;   //标记1级标题
            StringBuffer url=new StringBuffer();  //请求URL
            StringBuffer type=new StringBuffer(); //请求方式
            StringBuffer html = new StringBuffer("<!DOCTYPE html> \n" +
                    "<html> \n" +
                    "<head> \n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=\"utf-8\" /> \n" +
                    "<title>"+fileName.substring(0,fileName.lastIndexOf("."))+"</title> \n" +
                    "<style> \n" +
                    "body{ align:center}"+
                    " table {\n" +
                    "        border-collapse:collapse;\n" +
                    "        border:1px solid black;\n" +
                    "    }"+
                    "    td {\n" +
                    "        border:1px solid black;\n" +
                    "    }"+
                    "</style> \n" +
                    "</head> \n" +
                    "<body> \n");
            for (String beanName : beanNamesForAnnotation) {
                int titleFlagTwo = 1; //标记2级标题
                Object bean = applicationContext.getBean(beanName);
                //类上面注解
                String[] value = bean.getClass().getAnnotation(RequestMapping.class).value();
                Api api = bean.getClass().getAnnotation(Api.class);
                table = new StringBuffer("<h2>" + titleFlagOne + "." + api.value() + "</h2>");

                Method[] methods = bean.getClass().getMethods();
                for (Method method : methods) {
                    url.setLength(0);
                    type.setLength(0);

                    //方法上面注解
                    ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
                    ApiImplicitParams apiImplicitParams = method.getAnnotation(ApiImplicitParams.class);
                    ApiResponses apiResponses = method.getAnnotation(ApiResponses.class);
                    ApiImplicitParam apiImplicitParam = method.getAnnotation(ApiImplicitParam.class);


                    Annotation[] annotations = method.getAnnotations();
                    //设置url和type
                    if (annotations.length > 0) {
                        for (Annotation annotation : annotations) {
                            String simpleName = annotation.annotationType().getSimpleName();
                            if ("RequestMapping".equals(simpleName)) {

                                RequestMapping requestMapping = (RequestMapping) annotation;
                                url.append(requestMapping.value()[0]);
                                if (requestMapping.method().length > 0) {
                                    type.append(requestMapping.method()[0].name());
                                }
                            }
                            if ("PostMapping".equals(simpleName)) {
                                PostMapping postMapping = (PostMapping) annotation;
                                url.append(postMapping.value()[0]);
                                type.append("POST");
                            }
                            if ("GetMapping".equals(simpleName)) {
                                GetMapping getMapping = (GetMapping) annotation;
                                url.append(getMapping.value()[0]);
                                type.append("GET");
                            }

                        }
                    }

                    //拼接文档静态页面
                    if (apiOperation != null) {

                        table.append("<h3>" + titleFlagOne + "." + titleFlagTwo + "" + apiOperation.value() + "</h3>");
                        //标题 接口说明
                        table.append("<table style='width:100%;'><h4>" + titleFlagOne + "." + titleFlagTwo + ".1 接口说明</h4><tr><td style=\"background-color:#2381e3;\">接口说明</td><td>"
                                + apiOperation.notes() + "</td></tr>"
                                + "<tr><td style=\"background-color:#2381e3;\">接口协议</td><td>HTTP协议</td></tr>"
                                + "<tr><td style=\"background-color:#2381e3;\">请求URL</td><td>http://地址:端口/项目名称/"
                                + value[0] + "/" + url.toString().replace("/", "") + "</td></tr>"
                                + "<tr><td style=\"background-color:#2381e3;\">请求参数</td><td>详见接口請求参数说明</td></tr>"
                                + "<tr><td style=\"background-color:#2381e3;\">请求方式</td><td>" + type.toString() + "</td></tr>"
                                + "<tr><td style=\"background-color:#2381e3;\">返回数据</td><td>详见返回数据说明</td></tr>"
                                + "</table>");

                        //参数
                        table.append("<table style='width:100%;'><h4>" + titleFlagOne + "." + titleFlagTwo + ".2 接口请求参数说明</h4><tr style=\"background-color:#2381e3;\">"
                                + "<td>参数</td><td>类型</td><td>默认值</td><td>必填</td><td>说明</td></tr>");
                        //多个参数
                        if (apiImplicitParams != null) {
                            ApiImplicitParam[] apiImplicitParamArr = apiImplicitParams.value();

                            for (ApiImplicitParam param : apiImplicitParamArr) {
                                table.append("<tr><td>" + param.name() + "</td><td>" + param.dataType() + "</td><td style=\"text-align:center\">" + ("".equals(param.defaultValue()) ? "无" : param.defaultValue()) +
                                        "</td><td>" + param.required() + "</td><td>" + param.value() + "</td>");
                            }
                        }
                        //一个参数
                        else if (apiImplicitParam != null) {
                            table.append("<tr><td>" + apiImplicitParam.name() + "</td><td>" + apiImplicitParam.dataType() + "</td><td style=\"text-align:center\">" + ("".equals(apiImplicitParam.defaultValue()) ? "无" : apiImplicitParam.defaultValue())
                                    + "</td><td>" + apiImplicitParam.required() + "</td><td>" + apiImplicitParam.value() + "</td>");
                        } else {
                            table.append("<tr><td style=\"text-align:center\" colspan=5>没有参数</td></tr>");
                        }
                        table.append("</table>");

                        //返回数据
                        table.append("<table style='width:100%;'><h4>" + titleFlagOne + "." + titleFlagTwo + ".3 接口返回数据说明</h4><tr style=\"background-color:#2381e3;\">"
                                + "<td>状态码</td><td>返回数据</td></tr>");
                        if (apiResponses != null) {
                            ApiResponse[] apiResponse = apiResponses.value();
                            for (ApiResponse param : apiResponse) {
                                table.append("<tr><td>" + param.code() + "</td><td>" + param.message() + "</td></tr>");
                            }
                        }
                        table.append("</table>");

                        html.append(table);
                        table.setLength(0);
                        titleFlagTwo++;

                    }
                }
                titleFlagOne++;

                html.append("</body> \n" + "</html> ");

                //打开文件
                PrintStream printStream = new PrintStream(new FileOutputStream(filePath + "/" + fileName));
                printStream.println(html.toString());
                printStream.close();
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

    }

    /**
     * @author: chengzq
     * @date: 2018/8/7  上午 10:28
     * @Description: 获取applicationcontext
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private static <T> ApplicationContext getApplicationContext(T springContextHolderUtil) throws Exception {
        ApplicationContext applicationContext = null;
        Class<?> aClass = springContextHolderUtil.getClass();
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for(Method method :declaredMethods){
            String methodName = method.getName();
            if(methodName.equals("getApplicationContext")){
                applicationContext =(ApplicationContext) method.invoke(springContextHolderUtil);
                return applicationContext;
            }
        }
        return applicationContext;
    }




}

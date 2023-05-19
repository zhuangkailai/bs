package com.tjpu.pk.common.utils;

import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class RequestUtil {

    /**
     * 解析request的输入流
     *
     * @param request
     * @return 请求的json字符串
     */
    public static synchronized String getRequestBody(HttpServletRequest request) {
        String str = null;
        try {

            System.out.println();
            str = IOUtils.toString(request.getInputStream(), "utf-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 解析request的json数据
     *
     * @param request
     * @return Map
     */
    public static synchronized Map<String, Object> parseRequest(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String req = getRequestPayload(request);
            if (DataFormatUtil.parseProperties("isEncryption") != null) {
                Boolean isEncryption = Boolean.parseBoolean(DataFormatUtil.parseProperties("isEncryption"));
                if (isEncryption) {
                    if (request.getHeader("encryption") == null) {//如果有此参数，为微服务调用，不解密
                        String secret = DataFormatUtil.parseProperties("secret");
                        req = AESUtil.Decrypt(req, secret);
                    }

                }
            }
            if (StringUtils.isNotBlank(req)) {
                JSONObject jsonObject = JSONObject.fromObject(req);
                for (Iterator iter = jsonObject.keys(); iter.hasNext(); ) {
                    String key = (String) iter.next();
                    map.put(key, jsonObject.get(key));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return map;
    }

    /**
     * @param mapString
     * @return
     * @author: lip
     * @date: 2018年7月11日 下午3:31:59
     * @Description: 将字符串类型的json转换成map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static Map<String, Object> parseStringToMap(String mapString) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (StringUtils.isNotBlank(mapString)) {
                JSONObject jsonObject = JSONObject.fromObject(mapString);
                for (Iterator iter = jsonObject.keys(); iter.hasNext(); ) {
                    String key = (String) iter.next();
                    map.put(key, jsonObject.get(key));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return map;
    }


    private static String getRequestPayload(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = req.getReader()) {
            char[] buff = new char[1024];
            int len;
            while ((len = reader.read(buff)) != -1) {
                sb.append(buff, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ipAddress = inet.getHostAddress();
            }

        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) { //"***.***.***.***".length() = 15
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    private final static String[] appAgent = {"Android", "iPhone", "iPod", "iPad", "Windows Phone", "MQQBrowser"};

    public static String checkAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        String agent = "其他";
        for (String item : appAgent) {
            if (ua.contains(item)) {
                agent = "手机浏览";
                break;
            }
        }
        if(ua.contains("okhttp")){
            agent = "app";
        }
        if (ua.contains("Windows NT") || ua.contains("Macintosh")) {
            agent = "pc";
        }
        return agent;
    }

    /**
     * @author: lip
     * @date: 2020/4/17 0017 下午 5:00
     * @Description: 发送post请求
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    public static void sendPost(String url, Object param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * 发送post请求
     *
     * @param url        路径
     * @param jsonObject 参数(json类型)
     * @param encoding   编码格式
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static String sendPostForJson(String url, JSONObject jsonObject, String encoding) throws ParseException, IOException {
        String body = "";
        //创建httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);
        //装填参数
        StringEntity s = new StringEntity(jsonObject.toString(), "utf-8");
        s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        //设置参数到请求对象中
        httpPost.setEntity(s);
        System.out.println("请求地址：" + url);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = client.execute(httpPost);
        //获取结果实体
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            //按指定编码转换结果实体为String类型
            body = EntityUtils.toString(entity, encoding);
        }
        EntityUtils.consume(entity);
        //释放链接
        response.close();
        return body;
    }


}

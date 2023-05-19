package com.tjpu.sp.common.echarts;

import java.io.*;
import java.util.*;

/**
 * @author: chengzq
 * @date: 2019/8/14 0014 11:00
 * @Description:
 * @updateUser: lip
 * @updateDate: 2019/08/21
 * @updateDescription: 文件路径修改
 */
public class EchartsUtil {
    private static final String JSpath = "f:\\phantomjs-2.1.1-windows\\echarts-convert\\echarts-convert1.js";
    private static final String PhantomjsPath = "F:\\phantomjs-2.1.1-windows\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe ";
    private static final String fileName = "test-" + UUID.randomUUID().toString().substring(0, 8) + ".png";
    private static final String path = "D:\\temp\\Echart\\";

    public static String generateEChart(String options) throws IOException {
        String dataPath = writeFile(options);
        try {
            String filePath = path + fileName;
            File file = new File(path);     //文件路径（路径+文件名）
            if (!file.exists()) {   //文件不存在则创建文件，先创建目录
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            String cmd = PhantomjsPath + JSpath + " -infile " + dataPath + " -outfile " + path + fileName;
            Process process = Runtime.getRuntime().exec(cmd);

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
            Runtime.getRuntime().exec("cmd.exe /c del " + path + "*.json /q");
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String writeFile(String options) {
        String dataPath = path + UUID.randomUUID().toString().substring(0, 8) + ".json";
        try {
            File writename = new File(dataPath); // 相对路径，如果没有则要建立一个新的output.txt文件
            if (!writename.exists()) {   //文件不存在则创建文件，先创建目录
                File dir = new File(writename.getParent());
                dir.mkdirs();
                writename.createNewFile(); // 创建新文件
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            out.write(options); // \r\n即为换行
            out.flush(); // 把缓存区内容压入文件
            out.close(); // 最后记得关闭文件
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataPath;
    }
}

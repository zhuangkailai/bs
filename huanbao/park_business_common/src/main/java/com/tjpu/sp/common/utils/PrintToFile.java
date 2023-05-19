package com.tjpu.sp.common.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 写入日志
 * filePath 日志文件的路径
 * code 要写入日志文件的内容
 */
public class PrintToFile {
    /**
     * 写入日志 filePath 日志文件的路径 code 要写入日志文件的内容
     */
    public static  boolean print(String filePath, String code) {
        try {
            File tofile = new File(filePath);
            FileWriter fw = new FileWriter(tofile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(now()+":"+code);
            pw.close();
            bw.close();
            fw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 当前时间，格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间的标准形式字符串
     */
    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public static void main(String[] args){
        String location = System.getProperty("user.dir") + "/data/tmp/test.txt";
        print(location,"123456");
    }
}

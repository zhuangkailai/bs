package com.tjpu.pk.common.utils;

import cn.hutool.http.HttpUtil;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jexl3.*;
import org.apache.commons.lang.StringUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年3月19日 下午5:32:24
 * @Description:对数据进行处理的工具类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class DataFormatUtil {

    /**
     * @param preDate   前一段时间点
     * @param currDate  后一段时间点
     * @param minuteNum 相差的分钟数
     * @return
     * @author: lip
     * @date: 2018年4月2日 下午5:26:20
     * @Description:判断两个时间是否超过多少分钟
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static boolean isNeedUpdate(Date preDate, Date currDate, int minuteNum) {
        long diff = currDate.getTime() - preDate.getTime();
        long minutes = diff / (1000 * 60);
        if (minutes >= minuteNum) {// 最后一次操作系统的时间超过20分钟更新Cache，避免操作时Cache过期。
            return true;
        } else {
            return false;
        }
    }


    public static String generateRandomArray(int num) {
        String passWord = "";
        String chars = "0123456789";
        char[] rands = new char[num];
        for (int i = 0; i < num; i++) {
            int rand = (int) (Math.random() * 10);
            rands[i] = chars.charAt(rand);
        }
        for (int i = 0; i < rands.length; i++) {
            passWord += rands[i];
        }
        return passWord;
    }


    /**
     * @param preDate   前一段时间点
     * @param currDate  后一段时间点
     * @param minuteNum 相差的分钟数
     * @return
     * @author: lip
     * @date: 2018年4月2日 下午5:26:20
     * @Description:判断两个时间是否超过多少分钟
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static boolean isLessEqualUpdate(Date preDate, Date currDate, int minuteNum) {
        long diff = currDate.getTime() - preDate.getTime();
        long minutes = diff / (1000 * 60);
        if (minutes <= minuteNum) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/23 0023 下午 5:26
     * @Description: 给指定时间添加小时
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date addHourDate(Date cur, int Hour) {
        Calendar c = Calendar.getInstance();
        c.setTime(cur);   //设置时间
        c.add(Calendar.HOUR, Hour); //日期分钟加1,Calendar.DATE(天),Calendar.HOUR(小时)
        Date date = c.getTime(); //结果
        return date;
    }


    /**
     * @author: lip
     * @date: 2019/7/22 0022 下午 3:54
     * @Description: 格林时间字符串转换成普通时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date zoneToLocalTime(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.US);
        Date pictureCapturedTime = sdf.parse(dateString);
        return pictureCapturedTime;
    }

    /**
     * @param path
     * @author: lip
     * @date: 2018年3月19日 下午5:34:17
     * @Description: 删除某目录下的所有文件（根据路径）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static void deleteFile(String path) {
        if (DataFormatUtil.nil(path))
            return;
        File f = new File(path);
        String[] list = f.list();
        if (null != list && list.length > 0) {
            for (int i = 0; i < list.length; i++) {
                File file = new File(path + "/" + list[i]);
                file.delete();
            }

        }
    }


    /**
     * @param path
     * @return
     * @author: lip
     * @date: 2018年3月19日 下午5:34:29
     * @Description: 判断是否为目录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static boolean isDirectory(String path) {
        if (DataFormatUtil.nil(path))
            return false;
        File f = new File(path);
        String[] list = f.list();
        if (null != list && list.length > 0) {
            return true;
        }
        return false;
    }

    /**
     * @param str
     * @return
     * @author: lip
     * @date: 2018年3月19日 下午5:35:15
     * @Description: 特殊字符替换
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static String replaceSpecialStr(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.replaceAll("&", "&amp;");
            str = str.replaceAll("<", "&lt;");
            str = str.replaceAll(">", "&gt;");
            str = str.replaceAll("\"", "&quot;");
            str = str.replaceAll("'", "&#146;");
            str = str.replaceAll("\r\n", "<br />");
            return str.trim();
        }

    }

    /**
     * @author: xsm
     * @date: 2018/7/18 11:26
     * @Description: 获取并返回一个当前时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getDate() {
        Date nowTime = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(nowTime);
        Date Time = null;
        try {
            Time = df.parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return nowTime;
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:45
     * @Description: 获取指定日期的前num天的日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getPreDate(Date nowDate, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDate);
        calendar.add(Calendar.DATE, -num);
        nowDate = calendar.getTime();
        return nowDate;
    }

    /**
     * @author: xsm
     * @date: 2019/9/20 0020 上午 10:08
     * @Description: 获取指定日期的后num天的日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getPostponeDate(Date nowDate, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDate);
        calendar.add(Calendar.DATE, num);
        nowDate = calendar.getTime();
        return nowDate;
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:45
     * @Description: 获取指定日期的前num月的日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getPreMonthDate(Date nowDate, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDate);
        calendar.add(Calendar.MONTH, -num);
        nowDate = calendar.getTime();
        return nowDate;
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:45
     * @Description: 获取指定日期的小时值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static int getSomeDateHour(Date nowDate) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(nowDate);
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:45
     * @Description: 获取指定日期的分钟值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static int getSomeDateMinute(Date nowDate) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(nowDate);
        int minute = rightNow.get(Calendar.MINUTE);
        return minute;
    }


    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:45
     * @Description: 获取指定日期的分钟字符串
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getSomeDateMinuteString(Date nowDate) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(nowDate);
        int minute = rightNow.get(Calendar.MINUTE);
        String minuteString;
        if (minute < 10) {
            minuteString = "0" + minute;
        } else {
            minuteString = minute + "";
        }
        return minuteString;
    }


    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:45
     * @Description: 根据开始时间，结束时间，间隔分钟数，获取一段时间所属次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: timeFormat:GL-格林格式时间
     * @return:
     */
    public static Map<String, Integer> getIntervalTimeNumList(Date startDate, Date endDate, int interval,
                                                              List<String> ymdhms, String timeFormat, Map<String, Integer> timeIntervalAndSort) throws ParseException {
        List<Date> intervalTimeList = DataFormatUtil.getIntervalTimeList(startDate, endDate, interval);
        Map<String, Integer> timeAndNum = new LinkedHashMap<>();
        Date thisDate;

        String timeIntervalKey;
        for (String timeKey : ymdhms) {
            if (timeFormat != null && timeFormat.equals("GL")) {
                thisDate = DataFormatUtil.zoneToLocalTime(timeKey);
            } else {
                thisDate = DataFormatUtil.getDateYMDHMS(timeKey);
            }
            for (int i = 0; i < intervalTimeList.size() - 1; i++) {
                startDate = intervalTimeList.get(i);
                endDate = intervalTimeList.get(i + 1);
                if (DataFormatUtil.belongCalendar(thisDate, startDate, endDate)) {
                    timeIntervalKey = DataFormatUtil.getSomeDateHour(startDate) + ":" + DataFormatUtil.getSomeDateMinuteString(startDate)
                            + "~" + DataFormatUtil.getSomeDateHour(endDate) + ":" + DataFormatUtil.getSomeDateMinuteString(endDate);
                    if (timeAndNum.containsKey(timeIntervalKey)) {
                        timeAndNum.put(timeIntervalKey, timeAndNum.get(timeIntervalKey) + 1);
                    } else {
                        timeAndNum.put(timeIntervalKey, 1);
                    }
                }
            }
        }
        Map<String, Integer> timeAndNumTemp = new LinkedHashMap<>();
        String startPoint = "";
        String endPoint = "";
        String startTime;
        String endTime;
        Integer num = 0;
        int i = 0;
        for (String key : timeAndNum.keySet()) {
            startTime = key.split("~")[0];
            endTime = key.split("~")[1];
            if (endPoint.equals(startTime)) {
                String tempKey = startPoint + "~" + endPoint;
                num = timeAndNum.get(key) + timeAndNumTemp.get(tempKey);
                endPoint = endTime;
                timeAndNumTemp.remove(tempKey);
            } else {
                startPoint = startTime;
                endPoint = endTime;
                num = timeAndNum.get(key);
            }
            timeIntervalKey = startPoint + "~" + endPoint;
            timeAndNumTemp.put(timeIntervalKey, num);
            if (timeIntervalAndSort != null) {
                timeIntervalAndSort.put(timeIntervalKey, i++);
            }
        }
        //排序
        Map<String, Integer> result = new LinkedHashMap<>();


        timeAndNumTemp.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
        return result;
    }

    /**
     * @param s
     * @return 空串或null为true否则为false
     * @author: lip
     * @date: 2018年3月19日 下午5:39:30
     * @Description: 判断是否为空串或null
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static boolean nil(String s) {
        if (s == null || "".equals(s.trim()) || s.length() <= 0) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为空数组或null
     *
     * @param s
     * @return 空数组或null为true否则为false
     */
    public static boolean nil(String s[]) {
        if (s == null || s.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为空串
     *
     * @param s
     * @return 空串再返回null否则返回原串
     */
    public static String eq(String s) {
        if ("".equals(s) || s == "" || null == s)
            return null;
        else
            return s.trim();
    }

    // ///////////////编码类//////////////////////

    /**
     * 把字符串src(原编码方式first)转成新的编码方式（second）
     */
    public static String Encoding(String src, String first, String second) {
        if (nil(src) || nil(first) || nil(second))
            return "";
        String s = null;
        try {
            s = new String(src.getBytes(first), second);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 把字符串转成GBK编码方式
     *
     * @param src
     * @return
     */
    public static String toGBK(String src) {
        if (nil(src))
            return "";
        String s = null;
        try {
            s = new String(src.getBytes("ISO-8859-1"), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 把字符串转成GB2312编码方式
     *
     * @param src
     * @return
     */
    public static String toGB2312(String src) {
        if (nil(src))
            return "";
        String s = null;
        try {
            s = new String(src.getBytes("ISO-8859-1"), "gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * mao中key转为小写
     *
     * @param map
     * @return
     */
    public static Map<String, Object> toLowerCaseMap(Map map) {
        Map resultMap = new HashMap<>();
        if (map == null || map.isEmpty()) {
            return resultMap;
        }
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            String newKey = key.toLowerCase();
            resultMap.put(newKey, map.get(key));
        }
        return resultMap;
    }

    /**
     * 去掉空格
     *
     * @param src
     * @return
     */
    public static String trim(String src) {
        if (nil(src)) {
            return src;

        } else {
            return src.trim();
        }
    }

    /**
     * 把字符串转成UTF8编码方式
     *
     * @param src
     * @return
     */
    public static String toUTF8(String src) {
        if (nil(src))
            return "";
        String s = null;
        try {
            s = new String(src.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    // ///////////////类型转化///////////////////////

    /**
     * 把原字符串按spliter分隔成数组
     */
    public static String[] splittoArray(String s, String spliter) {
        if (DataFormatUtil.nil(s))
            return null;
        return s.split(spliter);
    }

    /**
     * @return
     * @author: lip
     * @date: 2018年3月19日 下午5:42:18
     * @Description: Set集合转成list集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static <T> List<T> setToList(Set<T> set) {
        Iterator<T> i = set.iterator();
        List<T> list = new ArrayList<T>();
        while (i.hasNext()) {
            list.add(i.next());
        }
        return list;
    }

    /**
     * 把字符数组转化成字符串
     *
     * @param s
     * @param spliter
     * @return
     */
    public static String split(String[] s, String spliter) {
        if (DataFormatUtil.nil(s))
            return "";
        if (s.length == 1)
            return s[0];
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length; i++) {
            sb.append(s[i]).append(spliter);
        }
        sb.deleteCharAt(sb.lastIndexOf(spliter));
        return sb.toString();
    }

    /**
     * 把字符串的真与假返回Boolean
     *
     * @param flag
     * @return
     */
    public static boolean parseBoolean(String flag) {
        if (nil(flag))
            return false;
        else if (flag.equals("true") || flag.equals("1") || flag.equals("真") || flag.equals("yes"))
            return true;
        else if (flag.equals("false") || flag.equals("0") || flag.equals("假") || flag.equals("no"))
            return false;
        return false;
    }

    /**
     * 把长整形转成整形
     *
     * @param s
     * @return
     */
    public static int LongToInt(Long s) {
        if (null == s)
            return 0;
        else
            return s.intValue();
    }

    /**
     * 把字符串转成整形
     *
     * @param flag
     * @return
     */
    public static int parseInt(String flag) {
        if (nil(flag))
            return 0;
        else if (flag.equals("true"))
            return 1;
        else if (flag.equals("false"))
            return 0;
        return Integer.parseInt(flag);
    }

    /**
     * 把字符转成长整形
     *
     * @param flag
     * @return
     */
    public static long parseLong(String flag) {
        if (nil(flag))
            return 0;
        return Long.parseLong(flag);
    }

    /**
     * 把长整形转成字符型
     *
     * @param flag
     * @return
     */
    public static String parseLongToString(Long flag) {
        if (null != flag)
            return flag.toString();
        return "";
    }

    /**
     * 把字符转成浮点型
     *
     * @param flag
     * @return
     */
    public static Double parseDouble(String flag) {
        if (nil(flag))
            return 0.0;
        return Double.parseDouble(flag);
    }

    // ///////////////////时间类///////////////////////////

    /**
     * 获得两时间段 之间的分钟数
     *
     * @param d1
     * @param d2
     * @return
     * @throws java.text.ParseException
     */
    public static long getDateMinutes(String d1, String d2) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = df.parse(d1);
            Date date2 = df.parse(d2);
            if (date2.before(date1)) {
                Date tempdate = date1;
                date1 = date2;
                date2 = tempdate;
            }
            long rvalue = 0L;

            rvalue = date2.getTime() / 60 / 1000 - date1.getTime() / 60 / 1000;
            return rvalue;
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * 获得两时间段 之间的分钟数
     *
     * @return
     * @throws java.text.ParseException
     */
    public static long getDateMinutes(Date date1, Date date2) {
        try {
            if (date2.before(date1)) {
                Date tempdate = date1;
                date1 = date2;
                date2 = tempdate;
            }
            long rvalue = 0L;
            rvalue = date2.getTime() / 60 / 1000 - date1.getTime() / 60 / 1000;
            if (rvalue == 59) {
                rvalue = rvalue + 1;
            }
            return rvalue;
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * 获得两时间段之间的小时
     *
     * @return
     * @throws java.text.ParseException
     */
    public static long getDateHours(Date date1, Date date2) {
        try {
            if (date2.before(date1)) {
                Date tempdate = date1;
                date1 = date2;
                date2 = tempdate;
            }
            long rvalue = 0L;
            rvalue = date2.getTime() / 60 / 60 / 1000 - date1.getTime() / 60 / 60 / 1000;
            return rvalue;
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * 把字符串yyyy-MM-dd HH:mm:ss 换成日期形
     *
     * @param string
     * @return Date
     */
    public static Date parseDate(String string) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return (Date) formatter.parse(string);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 把字符串yyyy 换成日期形
     *
     * @param string
     * @return Date
     */
    public static Date parseDateY(String string) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy");
            return (Date) formatter.parse(string);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 把Date转化成yyyy-MM-dd HH:mm:ss 换成日期形
     *
     * @return Date
     */
    public static Date parseDate(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String s = sdf.format(date);
            return (Date) sdf.parse(s);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 日期转换成yyyy-mm-dd HH:mm:ss
     *
     * @return Date
     */
    public static Date parseDateYMDHMS(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String s = sdf.format(date);
            return (Date) sdf.parse(s);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 日期转换成yyyy-mm-dd
     *
     * @return Date
     */
    public static Date parseDateYMD(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String s = sdf.format(date);
            return (Date) sdf.parse(s);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    /**
     * 把字符串yyyy-MM-dd换成日期型
     *
     * @param date
     * @return
     */
    public static Date parseDateYMD(String date) {
        if (date == null || date.length() == 0) {
            return null;
        }
        try {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return (java.util.Date) sdf.parse(date);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Date parseDateHM(String date) {
        if (date == null || date.length() == 0) {
            return null;
        }
        try {
            DateFormat sdf = new SimpleDateFormat("HH:mm");
            return (java.util.Date) sdf.parse(date);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 把字符串yyyy-MM-dd换成日期型
     *
     * @param date
     * @return
     */
    public static Calendar parseCalendarHM(String date) {
        if (date == null || date.length() == 0) {
            return null;
        }
        try {
            DateFormat sdf = new SimpleDateFormat("HH:mm");
            Date dateCale = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateCale);
            return calendar;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 把字符串yyyy-MM换成日期型
     *
     * @param date
     * @return
     */
    public static Date parseDateYM(String date) {
        if (date == null || date.length() == 0) {
            return null;
        }
        try {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM");
            return (java.util.Date) sdf.parse(date);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 把字符串yyyy-MM-dd HH:mm换成日期型
     *
     * @param date
     * @return
     */
    public static Date parseDateYMDHM(String date) {
        if (date == null || date.length() == 0) {
            return null;
        }
        try {
            DateFormat sdf = null;
            if (date.indexOf(":") > 0) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd");
            }
            return (java.util.Date) sdf.parse(date);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @return 将当前时间格式化为yyyy-MM-dd HH:mm:ss
     * @author fengbo
     */
    public static String getStandardTime() {

        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = new Date();

        String date = format.format(dt);
        return date;
    }

    /**
     * 把日期转成yyyy-MM-dd HH:mm:ss的字符串
     *
     * @param dates
     * @return
     */
    public static String getDate(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = df.format(dates);
        return s;

    }

    public static String getDatetoHour(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH");
        String s = df.format(dates);
        return s;

    }


    /**
     * 把日期转成yyyy的字符串
     *
     * @param dates
     * @return
     */
    public static String getDateY(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy");
        String s = df.format(dates);
        return s;

    }

    /**
     * 时间格式：yyyy 转换成时间格式
     *
     * @param date
     * @return
     */
    public static Date getDateY(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            Date d = (java.util.Date) sdf.parse(date);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }

    }


    /**
     * @param nowDate
     * @return
     * @throws Exception
     * @author: lip
     * @date: 2018年6月6日 上午10:29:45
     * @Description: 获取某个月的所有日期yyyy-MM-dd
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static List<String> getDayListOfMonth(Date nowDate) {
        List<String> result = new ArrayList<String>();
        try {

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTime(nowDate);

            calendar.add(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            String firstday = format.format(calendar.getTime());
            // 获取前月的最后一天

            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            String lastday = format.format(calendar.getTime());
            result = DataFormatUtil.getMonthBetweenForDay(firstday, lastday);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 时间格式：yyyy-MM 转换成时间格式
     *
     * @param date
     * @return
     */
    public static Date getDateYM(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date d = (java.util.Date) sdf.parse(date);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }

    }

    /**
     * 时间格式：yyyy-MM-dd hh 转换成时间格式
     *
     * @param date
     * @return
     */
    public static Date getDateYMDH(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
            Date d = (java.util.Date) sdf.parse(date);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }

    }

    /**
     * 时间格式：yyyy-MM-dd hh 转换成时间格式
     *
     * @param date
     * @return
     */
    public static Date getDateYMDHM(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date d = (java.util.Date) sdf.parse(date);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 把日期转成yyyy-MM-dd HH:mm:ss的字符串
     *
     * @param dates
     * @return
     */
    public static String getDateYMDHMS(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = df.format(dates);
        return s;

    }

    /**
     * 把日期转成yyyy-mm-dd hh24:mi:ss的字符串
     *
     * @param dates
     * @return
     */
    public static String getDateYMDHMS2(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-mm-dd hh24:mi:ss");
        String s = df.format(dates);
        return s;

    }

    /**
     * 把日期转成yyyy-MM-dd HH:mm:ss的字符串
     *
     * @param dates
     * @return
     */
    public static String getDateYMDHMS1(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
        String s = df.format(dates);
        return s;

    }

    /**
     * 把日期转成yyyy-MM-dd HH:mm:ss的字符串
     *
     * @param dates
     * @return
     */
    public static String getCompactDateYMDHMS(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        String s = df.format(dates);
        return s;

    }

    /**
     * 把日期转成MMdd的字符串
     *
     * @param dates
     * @return
     */
    public static String getDateMMDD(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("MMdd");
        String s = df.format(dates);
        return s;

    }

    /**
     * 把日期型换成yyyy-MM-dd字符串
     *
     * @param d
     * @return
     */
    public static String getDateYMD(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把日期型换成yyyy-MM-dd H字符串
     *
     * @param d
     * @return
     */
    public static String getDateYMDoneH(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd H");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把日期型换成yyyy-MM-dd HH mm字符串
     *
     * @param d
     * @return
     */
    public static String getDateYMDHM(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 把日期型换成HH mm字符串
     *
     * @param d
     * @return
     */
    public static String getDateHM(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("HH:mm");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 把日期型换成HH字符串
     *
     * @param d
     * @return
     */
    public static String getDateH(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("HH");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把日期型换成MM-dd字符串
     *
     * @param d
     * @return
     */
    public static String getDateMD(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("MM-dd");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把日期型换成yyyy 年 MM 月dd 日字符串
     *
     * @param d
     * @return
     */
    public static String getDateYMD1(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy年MM月dd日");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把日期型换成yyyy年MM月dd日字符串
     *
     * @param d
     * @return
     */
    public static String getDateYMD3(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy年MM月dd日");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把日期型换成yyyy年MM月dd日字符串
     *
     * @param d
     * @return
     */
    public static String getDateYMD2(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy. MM.dd");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把日期型换成yyyy-MM字符串
     *
     * @param d
     * @return
     */
    public static String getDateYM(Date d) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM");
            String s = dt.format(d);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取日期的小时数字
     *
     * @param date
     * @return
     */
    public static Integer getDateHourNum(Date date) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("H");
            String hour = dt.format(date);
            return Integer.parseInt(hour);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取日期的分钟数字
     *
     * @param date
     * @return
     */
    public static Integer getDateMinuteNum(Date date) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("m");
            String hour = dt.format(date);
            return Integer.parseInt(hour);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取日期的日数字
     *
     * @param date
     * @return
     */
    public static Integer getDateDayNum(Date date) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("d");
            String day = dt.format(date);
            return Integer.parseInt(day);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 转换日期类型的字符串格式
     *
     * @param data   :要转换的数据(String)
     * @param oneF   :原来的格式
     * @param otherF :之后的格式
     * @return
     */
    public static String FormatDateOneToOther(String data, String oneF, String otherF) {
        try {
            DateFormat sdf = new SimpleDateFormat(oneF);
            Date date = sdf.parse(data);
            SimpleDateFormat dt = new SimpleDateFormat(otherF);
            String s = dt.format(date);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 将一个字符串从一个格式转换为另一个格式
     *
     * @param date       要转换的数据
     * @param timeFormat 转换后的格式
     * @return
     */
    public static String formatDateToOtherFormat(Date date, String timeFormat) {
        try {
            if (date != null) {
                SimpleDateFormat dt = new SimpleDateFormat(timeFormat);
                return dt.format(date);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 把日期型换成HH:mm:ss字符串
     *
     * @param data
     * @return
     */
    public static String getDateHMS(Date data) {
        if (null == data) {
            return "";
        }
        java.text.SimpleDateFormat formathhmmss = new java.text.SimpleDateFormat("HH:mm:ss");

        return formathhmmss.format(data);
    }


    /**
     * 把日期型换成mm:ss字符串
     *
     * @param data
     * @return
     */
    public static String getDateMS(Date data) {
        if (null == data) {
            return "";
        }
        java.text.SimpleDateFormat formathhmmss = new java.text.SimpleDateFormat("mm:ss");

        return formathhmmss.format(data);
    }

    /**
     * 把日期型换成mm字符串
     *
     * @param data
     * @return
     */
    public static String getDateM(Date data) {
        if (null == data) {
            return "";
        }
        java.text.SimpleDateFormat formathhmmss = new java.text.SimpleDateFormat("mm");

        return formathhmmss.format(data);
    }

    /**
     * 获取两个日期的天数
     *
     * @param smdate
     * @param bdate
     * @return
     * @throws ParseException
     * @author zhangcd
     * @date 2016-4-13
     */
    public static int getDaysHMS(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 得到今天0点的时间
     *
     * @return
     */
    public static String getNow() {
        Date now = new Date();
        String nows = getDateYMD(now);
        nows = nows + " 00:00:00";
        return nows;

    }

    /**
     * 从时间格式获取秒数
     *
     * @param date
     * @return 1970-1-1到date的秒数
     */
    public static Long getDateLong(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // sdf.setTimeZone(TimeZone.getTimeZone("GMT+04"));
            Date d = (java.util.Date) sdf.parse(date);
            return d.getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0l;
        }

    }

    /**
     * 时间格式：yyyy-MM-dd 转换成时间格式
     *
     * @param date
     * @return
     */
    public static Date getDateYMD(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = (java.util.Date) sdf.parse(date);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }

    }


    /**
     * 时间格式：yyyy-MM-dd HH:mm:ss转换成时间格式
     *
     * @param date
     * @return
     */
    public static Date getDateYMDHMS(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = sdf.parse(date);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }

    }


    /**
     * 获取固定间隔时刻集合
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  时间间隔(单位：分钟)
     * @return
     */
    public static List<String> getIntervalTimeStringList(Date startTime, Date endTime, int interval) {

        List<String> list = new ArrayList<>();
        while (startTime.getTime() <= endTime.getTime()) {
            list.add(DataFormatUtil.getDateYMDHMS(startTime));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            calendar.add(Calendar.MINUTE, interval);
            if (calendar.getTime().getTime() > endTime.getTime()) {
                if (!startTime.equals(endTime)) {
                    list.add(DataFormatUtil.getDateYMDHMS(endTime));
                }
                startTime = calendar.getTime();
            } else {
                startTime = calendar.getTime();
            }

        }
        return list;
    }


    /**
     * 获取固定间隔时刻集合
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  时间间隔(单位：分钟)
     * @return
     */
    public static List<Date> getIntervalTimeList(Date startTime, Date endTime, int interval) {

        List<Date> list = new ArrayList<>();
        while (startTime.getTime() <= endTime.getTime()) {
            list.add(startTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            calendar.add(Calendar.MINUTE, interval);
            if (calendar.getTime().getTime() > endTime.getTime()) {
                if (!startTime.equals(endTime)) {
                    list.add(endTime);
                }
                startTime = calendar.getTime();
            } else {
                startTime = calendar.getTime();
            }

        }
        return list;
    }


    /**
     * 判断某一时间属于哪个季度 1 第一季度 2 第二季度 3 第三季度 4 第四季度
     *
     * @param date
     * @return
     */
    public static int getSeason(Date date) {

        int season = 0;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int month = c.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                season = 1;
                break;
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                season = 2;
                break;
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                season = 3;
                break;
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                season = 4;
                break;
            default:
                break;
        }
        return season;
    }

    /**
     * @param minDate 最小时间 2015-01
     * @param maxDate 最大时间 2015-10
     * @return 日期集合 格式为 年-月
     * @throws Exception
     */
    public static List<String> getMonthBetween(String minDate, String maxDate) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");// 格式化为年月

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

            max.setTime(sdf.parse(maxDate));
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.MONTH, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * @param minDate 最小时间 2001
     * @param maxDate 最大时间 2020
     * @return 日期集合 格式为 年
     * @throws Exception
     */
    public static List<String> getYearBetween(String minDate, String maxDate) throws Exception {
        List<String> yearList = new ArrayList<>();
        int startIndex = Integer.parseInt(minDate);
        int endIndex = Integer.parseInt(maxDate);
        for (int i = startIndex; i <= endIndex; i++) {
            yearList.add(i + "");
        }
        return yearList;
    }


    /**
     * @author: lip
     * @date: 2019/6/5 0005 上午 10:30
     * @Description: 将mm格式的月度字符串转换成“一月”
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getLabelByDate(String ym) {
        String label = "";
        switch (ym) {
            case "01":
                label = "一月";
                break;
            case "02":
                label = "二月";
                break;
            case "03":
                label = "三月";
                break;
            case "04":
                label = "四月";
                break;
            case "05":
                label = "五月";
                break;
            case "06":
                label = "六月";
                break;
            case "07":
                label = "七月";
                break;
            case "08":
                label = "八月";
                break;
            case "09":
                label = "九月";
                break;
            case "10":
                label = "十月";
                break;
            case "11":
                label = "十一月";
                break;
            case "12":
                label = "十二月";
                break;
            default:
                break;
        }
        return label;
    }


    /**
     * @author: lip
     * @date: 2019/6/4 0004 下午 2:43
     * @Description: 获取某年月（yyyy-MM）最后一天
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: yyyy-MM-dd
     */
    public static String getLastDayOfMonth(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);  //年
        int month = Integer.parseInt(yearMonth.split("-")[1]); //月
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DATE);
        // 设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }


    /**
     * @author: lip
     * @date: 2019/6/4 0004 下午 2:43
     * @Description: 获取某年月（yyyy-MM）第一天
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: yyyy-MM-dd
     */
    public static String getFirstDayOfMonth(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);  //年
        int month = Integer.parseInt(yearMonth.split("-")[1]); //月
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }


    /**
     * @param minDate 最小时间 2015-01-01
     * @param maxDate 最大时间 2015-10 -01
     * @return 日期集合 格式为 年-月 - 日
     * @throws Exception
     */
    public static List<String> getYMDBetween(String minDate, String maxDate) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 格式化为年月

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            max.setTime(sdf.parse(maxDate));
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Description:
     *
     * @param minDate 小时时间的区间集合
     * @param maxDate
     * @return
     * @throws Exception date :2018年1月9日 author : lwc
     */
    public static List<String> getYMDHBetween(String minDate, String maxDate) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");// 格式化为年月

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            max.setTime(sdf.parse(maxDate));
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.HOUR_OF_DAY, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Description:
     *
     * @param minDate 分钟时间的区间集合
     * @param maxDate
     * @return
     * @throws Exception date :2018年1月9日 author : lwc
     */
    public static List<String> getYMDHMBetween(String minDate, String maxDate) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");// 格式化为年月

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            max.setTime(sdf.parse(maxDate));
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.MINUTE, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Description:
     *
     * @param minDate 分钟时间的区间集合
     * @param maxDate
     * @return
     * @throws Exception date :2018年1月9日 author : lwc
     */
    public static List<String> getYMDHM2Between(String minDate, String maxDate) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");// 格式化为年月

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            max.setTime(sdf.parse(maxDate));
            Calendar curr = min;
            while (curr.before(max)) {
                curr.add(Calendar.MINUTE, 1);
                result.add(sdf.format(curr.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }



    /**
     * 得到某个时间的前N个月
     *
     * @return
     */
    public static Date getBeforeMonthByNum(int num, Date nowDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDay);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - num);
        return calendar.getTime();
    }

    /**
     * 得到某个时间的前N个月
     *
     * @return
     */
    public static Date getBeforeYearByNum(int num, Date nowDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDay);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - num);
        return calendar.getTime();
    }


    /**
     * 得到某个时间的前N天
     *
     * @param someTimeString:yyyy-MM-dd
     * @return
     */
    public static String getBeforeByDayTime(int day, String someTimeString) {

        Date someTime = DataFormatUtil.getDateYMD(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - day);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 获取日同比日期
     *
     * @param dayString:yyyy-MM-dd
     * @return
     */
    public static String getDayTBDate(String dayString) {
        Date someTime = DataFormatUtil.getDateYMD(dayString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 获取小时同比日期
     *
     * @param hourString:yyyy-MM-dd HH
     * @return
     */
    public static String getHourTBDate(String hourString) {
        Date someTime = DataFormatUtil.getDateYMDH(hourString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 获取分钟同比日期
     *

     * @return
     */
    public static String getMinuteTBDate(String minuteString) {
        Date someTime = DataFormatUtil.getDateYMDHM(minuteString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 获取小时年同比日期
     *
     * @param hourString:yyyy-MM-dd HH
     * @return
     */
    public static String getHourYearTBDate(String hourString, int year) {
        Date someTime = DataFormatUtil.getDateYMDH(hourString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - year);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 获取日年同比日期
     *
     * @param dayString:yyyy-MM-dd
     * @return
     */
    public static String getDayYearTBDate(String dayString, int year) {
        Date someTime = DataFormatUtil.getDateYMD(dayString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - year);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 获取日年同比日期
     *
     * @param dayString:yyyy-MM
     * @return
     */
    public static String getMonthYearTBDate(String dayString, int year) {
        Date someTime = DataFormatUtil.getDateYM(dayString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - year);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 获取月同比数据
     *
     * @param dayString:yyyy-MM-dd
     * @return
     */
    public static String getMonthTBDate(String dayString) {
        Date someTime = DataFormatUtil.getDateYMD(dayString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 获取月同比数据
     *
     * @param dayString:yyyy-MM-dd
     * @return
     */
    public static String getMonthTBYearDate(String dayString) {
        Date someTime = DataFormatUtil.getDateYM(dayString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 得到某个时间的前N个月
     *
     * @param someTimeString:yyyy-MM-dd
     * @return
     */
    public static String getBeforeByMonthTime(int day, String someTimeString) {
        Date someTime = DataFormatUtil.getDateYMD(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - day);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 得到某个时间的前N个月
     *
     * @param someTimeString:yyyy-MM
     * @return
     */
    public static String getBeforeMonthTime(int day, String someTimeString) {
        Date someTime = DataFormatUtil.getDateYM(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - day);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 得到某个时间的前N个年
     *
     * @param someTimeString:yyyy-MM-dd
     * @return
     */
    public static String getBeforeByYearTime(int day, String someTimeString) {
        Date someTime = DataFormatUtil.getDateYMD(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - day);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 得到某个时间的前N个年
     *
     * @param someTimeString:yyyy-MM-dd
     * @return
     */
    public static String getBeforeYear(int day, String someTimeString) {
        Date someTime = DataFormatUtil.getDateY(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - day);
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 得到某个时间的前N小时
     *
     * @param someTimeString:yyyy-MM-dd HH
     * @return
     */
    public static String getBeforeByHourTime(int hour, String someTimeString) {

        Date someTime = DataFormatUtil.getDateYMDH(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - hour);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 得到某个时间的前N小时
     *
     * @param someTime
     * @return
     */
    public static String getBeforeByHourTime(int hour, Date someTime) {
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - hour);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 得到某个时间的前N分钟
     *
     * @param someTimeString:yyyy-MM-dd HH:mm
     * @return
     */
    public static String getBeforeByMinuteTime(int minute, String someTimeString) {

        Date someTime = DataFormatUtil.getDateYMDHM(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - minute);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }


    /**
     * 得到某个时间的前N分钟
     *
     * @param someTimeString:yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getBeforeBySecondTime(int second, String someTimeString) {

        Date someTime = DataFormatUtil.getDateYMDHM(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) - second);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }


    /**
     * 获取一周的时间 Description:
     *
     * @param minDate
     * @param maxDate
     * @return
     * @throws Exception date :2018年1月9日 author : lwc
     */
    public static List<String> getWeekBetween(String minDate, String maxDate) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 格式化为年月日

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            max.setTime(sdf.parse(maxDate));
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.DAY_OF_WEEK, 1);
            }
            // if(curr.equals(max)){
            // result.add(sdf.format(curr.getTime()));
            // curr.add(Calendar.DAY_OF_WEEK, 1);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 一个月内的时间集合 Description:
     *
     * @param minDate
     * @param maxDate
     * @return
     * @throws Exception date :2018年1月9日 author : lwc
     */
    public static List<String> getMonthBetweenForDay(String minDate, String maxDate) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 格式化为年月日

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            max.setTime(sdf.parse(maxDate));
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.DAY_OF_MONTH, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * @param minDate 最小时间 2015
     * @param maxDate 最大时间 2019
     * @return 日期集合 格式为 年
     * @throws Exception
     */
    public static List<String> getYBetween(String minDate, String maxDate) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");// 格式化为年月

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            max.setTime(sdf.parse(maxDate));
            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.YEAR, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 从时间格式获取秒数
     *
     * @param date
     * @return 1970-1-1到date的秒数
     */
    public static Long getDateLong(Date date) {
        try {
            // SimpleDateFormat sdf = new
            // SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // Date d = new Date(sdf.format(date));
            return date.getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return 0l;
        }

    }

    // /////////////////sql///////////////////////

    /**
     * 分页Oracle分页
     *
     * @param sql
     * @param pagefirst
     * @param page_size
     * @return
     */
    public static String getLimitSql(String sql, int pagefirst, int page_size) {
        // sql = sql + " limit " + pagefirst + "," + page_size;
        sql = "select * from (select a1.*,rownum rn from (" + sql + " ) a1 where rownum<=" + (page_size + pagefirst) + ") a2 where rn>=" + (pagefirst + 1);// 支持oracle
        // System.out.println(sql);
        return sql;
    }

    public static String getLimitMySql(String sql, int pagefirst, int page_size) {
        sql = sql + " limit " + pagefirst + " , " + page_size;
        return sql;
    }

    /**
     * 连结sql
     *
     * @param tableName
     * @param other
     * @param datePro
     * @return
     */
    public static List getProperty(String tableName, Map<String, String> other, List datePro) {
        StringBuffer str = new StringBuffer("from " + tableName + " as names where 1=1");
        List lis = new ArrayList();

        if (other != null) {
            Boolean bk = new Boolean("true");
            for (Map.Entry<String, String> m : other.entrySet()) {

                if (m.getValue() != null) {
                    if (m.getValue().equals("true") || m.getValue().equals("false")) {

                        str.append(" and names." + m.getKey() + "=?");

                        lis.add(new Boolean(m.getValue()));

                    } else {

                        str.append(" and names." + m.getKey() + "  like ?");
                        String s = "%" + m.getValue() + "%";

                        lis.add(s);
                    }
                }
            }
        }

        if (datePro != null) {

            if (datePro.get(1) != null && datePro.get(2) != null) {
                str.append(" and names." + datePro.get(0) + "  BETWEEN  ? and ?");
                lis.add(parseDate(datePro.get(1).toString()));
                // System.out.println("------------"+datePro.get(2).toString()+"
                // 23:59:59");
                // System.out.println("---------------"+parseDates(datePro.get(2).toString()+"
                // 23:59:59"));
                lis.add(parseDate(datePro.get(2).toString() + " 23:59:59"));
            }

        }
        str.append(" order by names.id desc");// 按ID倒序排列
        List proList = new ArrayList();
        proList.add(str.toString());
        // System.out.println(str.toString());
        if (lis != null || !lis.isEmpty()) {
            proList.add(lis.toArray());

        } else {

            proList.add(null);
        }
        return proList;
    }

    /**
     * 去重复
     *
     * @param list
     * @return
     */
    public static List<String> toRepeat(List<String> list) {
        int len = list.size();
        for (int i = 0; i < len - 1; i++) {
            String str = list.get(i);
            if (str != null) {
                for (int j = i + 1; j < len; j++) {
                    if (list.get(j) == null || str.equals(list.get(j))) {
                        list.remove(j);
                        j--;
                        len--;
                    }
                }
            }

        }
        return list;
    }

    /**
     * @param key 解析路径关键字
     * @author lip
     * @date 2017-09-05
     * @des 用于解析config.properties文件获取上传下载路径以及其他配置
     */

    public static String parseProperties(String key) {

        String filePath = "";
        if (key != null && !"".equals(key)) {
            try {
                // 生成输入流
                InputStreamReader ins = new InputStreamReader(DataFormatUtil.class.getResourceAsStream("/config/sysconfig.properties"), "UTF-8");
                // 生成properties对象
                Properties p = new Properties();
                p.load(ins);
                // 输出properties文件的内容
                filePath = p.getProperty(key);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return filePath;
    }

    /**
     * 读取json文件，返回json串
     *
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            Reader reader = new InputStreamReader(DataFormatUtil.class.getResourceAsStream("/json/" + fileName), "UTF-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param key 解析路径关键字
     * @author lip
     * @date 2016-08-09
     * @des 用于解析JDBC.properties数据库的信息
     */

    public static String parsePropertiesJDBC(String key) {

        String filePath = "";
        if (key != null && !"".equals(key)) {
            try {
                // 生成输入流
                InputStream ins = DataFormatUtil.class.getResourceAsStream("/jdbc.properties");
                // 生成properties对象
                Properties p = new Properties();
                p.load(ins);
                // 输出properties文件的内容
                filePath = p.getProperty(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    /**
     * 返回byte的数据大小对应的文本
     *
     * @param size
     * @return
     */
    public static String getFileSize(long size) {

        // 1KB,1MB,1GB,1TB
        long oneKB = 1024;
        long oneMB = 1024 * oneKB;
        long oneGB = 1024 * oneMB;
        long oneTB = 1024 * oneGB;
        DecimalFormat formater = new DecimalFormat("####.00");
        if (size < oneKB) {
            return size + "bytes";
        } else if (size < oneMB) {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + "KB";
        } else if (size < oneGB) {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + "MB";
        } else if (size < oneTB) {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + "GB";
        } else {
            return "size: error";
        }
    }

    /**
     * @return String
     * @des 四舍五入
     * @author lip
     * @data 2016-12-6
     */
    public static String formatDouble(String format, Double doubleNum) {
        String StringNum = "";
        if (doubleNum != null) {
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            StringNum = df.format(doubleNum);
        }
        return StringNum;
    }

    /**
     * 把字符串yyyy-MM-dd HH:mm:ss 换成日期形
     *
     * @param string
     * @return Date
     */
    public static Date parseDateYMDH(String string) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH");
            return (Date) formatter.parse(string);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @author: lip
     * @date: 2019/10/29 0029 下午 2:59
     * @Description: 根据时间类型转换时间格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date parseDateByFormat(String time, String format) {
        try {
            DateFormat formatter = new SimpleDateFormat(format);
            return (Date) formatter.parse(time);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @author: lip
     * @date: 2019/10/29 0029 下午 2:59
     * @Description: 根据时间类型转换时间格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String parseDateToStringByFormat(Date time, String format) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat(format);
            String s = dt.format(time);
            return s;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    /**
     * @return String
     * @des 四舍五入 保留两位小数
     * @author lip
     * @data 2016-12-6
     */
    public static String formatDoubleSaveTwo(Double doubleNum) {

        String StringNum = "";
        if (doubleNum != null) {
            String format = "######0.00";
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            StringNum = df.format(doubleNum);
        }
        return StringNum;
    }

    public static String formatDoubleSaveThreeS(Double doubleNum) {

        String StringNum = "";
        if (doubleNum != null) {
            String format = "######0.000";
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            StringNum = df.format(doubleNum);
        }
        return StringNum;
    }

    /**
     * @return String
     * @des 四舍五入 保留三为小数
     * @author lip
     * @data 2016-12-6
     */
    public static Double formatDoubleSaveThree(Double doubleNum) {

        Double doubleTemp = null;
        if (doubleNum != null) {
            String format = "######0.000";
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            doubleTemp = Double.parseDouble(df.format(doubleNum));
        }
        return doubleTemp;
    }



    public static Double formatDoubleByFormat(String format,Double doubleNum) {

        Double doubleTemp = null;
        if (doubleNum != null) {
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            doubleTemp = Double.parseDouble(df.format(doubleNum));
        }
        return doubleTemp;
    }
    /**
     * @return String
     * @des 四舍五入 保留二位小数
     * @author lip
     * @data 2016-12-6
     */
    public static Double formatDoubleSaveTwoDouble(Double doubleNum) {

        Double doubleTemp = null;
        if (doubleNum != null) {
            String format = "######0.00";
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            doubleTemp = Double.parseDouble(df.format(doubleNum));
        }
        return doubleTemp;
    }

    /**
     * @return String
     * @des 四舍五入 保留N位小数
     * @author mmt
     * @data 2022-11-11
     */
    public static String formatDoubleSaveN(Integer n,Double doubleNum) {

        String StringNum = "";
        if (doubleNum != null) {
            String format = "######0";
            if (n != null && n > 0) {
                format+=".";
                for (Integer i = 0; i < n; i++) {
                    format += "0";
                }
            }
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            StringNum = df.format(doubleNum);
        }
        return StringNum;
    }
    /**
     * @return String
     * @des 四舍五入 保留一位小数
     * @author lip
     * @data 2016-12-6
     */
    public static String formatDoubleSaveOne(Double doubleNum) {

        String StringNum = "";
        if (doubleNum != null) {
            String format = "######0.0";
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            StringNum = df.format(doubleNum);
        }
        return StringNum;
    }

    /**
     * @return String
     * @des 四舍五入 保留整数
     * @author lip
     * @data 2016-12-6
     */
    public static String formatDoubleSaveNo(Double doubleNum) {

        String StringNum = "";
        if (doubleNum != null) {
            String format = "######0";
            DecimalFormat df = new DecimalFormat(format);
            df.setRoundingMode(RoundingMode.HALF_UP);
            StringNum = df.format(doubleNum);
        }
        return StringNum;
    }

    /**
     * @return String
     * @des 将分钟转换成小时分钟 （xx小时yy分钟）
     * @author xsm
     * @data 2021-05-20
     */
    public static String countHourMinuteTime(int tatalnum) {
        String str = "";
        if (tatalnum < 60) {
            str = tatalnum + "分钟";
        } else if (tatalnum == 60) {
            str = "1小时";
        } else {
            int onenum = tatalnum / 60;
            str = onenum + "小时" + ((tatalnum - onenum * 60) > 0 ? (tatalnum - onenum * 60) + "分钟" : "");
        }
        return str;
    }

    /**
     * 使用java正则表达式去掉多余的.与0
     *
     * @param s
     * @return
     * @author lip
     */
    public static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");// 去掉多余的0
            s = s.replaceAll("[.]$", "");// 如最后一位是.则去掉
        }

        return s;
    }

    /**
     * 保留二位小数，去掉.0
     *
     * @return
     * @author lip
     */
    public static String SaveTwoAndSubZero(Double d) {
        return DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(d));
    }

    public static String SaveThreeAndSubZero(Double d) {
        return DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveThreeS(d));
    }

    /**
     * 保留一位小数，去掉.0
     *
     * @return
     * @author lip
     */
    public static String SaveOneAndSubZero(Double d) {
        return DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveOne(d));
    }


    /**
     * 获取十六进制的颜色代码.例如 "#6E36B4" , For HTML ,
     *
     * @return String
     */
    public static String getRandColorCode() {
        String r, g, b;
        Random random = new Random();
        r = Integer.toHexString(random.nextInt(256)).toUpperCase();
        g = Integer.toHexString(random.nextInt(256)).toUpperCase();
        b = Integer.toHexString(random.nextInt(256)).toUpperCase();

        r = r.length() == 1 ? "0" + r : r;
        g = g.length() == 1 ? "0" + g : g;
        b = b.length() == 1 ? "0" + b : b;

        return r + g + b;
    }

    // 获取请求协议头+服务器ip
    public static String getServerHttpIpPort(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort();
    }

    // 比较两个两组字符的增减
    public static String getFieldAddOrDel(String oldStrs, String newStrs) {
        String editLog = "";
        String addStr = "";
        String deleteStr = "";
        String[] names1 = oldStrs.split(",");
        String[] names2 = newStrs.split(",");
        Set<String> namasSet1 = new HashSet<String>();// 老的字符串
        Set<String> namasSet2 = new HashSet<String>();// 新的字符串
        for (int i = 0; i < names1.length; i++) {
            namasSet1.add(names1[i]);
        }
        for (int j = 0; j < names2.length; j++) {
            namasSet2.add(names2[j]);
        }

        for (int i = 0; i < names1.length; i++) {
            if (!"".equals(names1[i]) && !namasSet2.contains(names1[i])) {// 修改后不包含的子串
                deleteStr += names1[i] + "，";
            }
        }

        for (int j = 0; j < names2.length; j++) {
            if (!"".equals(names2[j]) && !namasSet1.contains(names2[j])) {// 原来字符中没有的子串
                addStr += names2[j] + "，";
            }
        }
        if (!"".equals(addStr)) {
            addStr = addStr.substring(0, addStr.length() - 1);
            addStr = "新增：" + addStr + "<br/>";
        }
        if (!"".equals(deleteStr)) {
            deleteStr = deleteStr.substring(0, deleteStr.length() - 1);
            deleteStr = "删除：" + deleteStr + "<br/>";
        }
        editLog = addStr + deleteStr;

        return editLog;
    }

    // 比较两个两组字符的增减
    public static String getFieldUpdateString(String oldStrs, String newStrs) {
        String editLog = "";
        String addStr = "";
        String deleteStr = "";
        String nullstrdel = "";
        String nullstradd = "";
        String[] names1 = oldStrs.split(",");
        String[] names2 = newStrs.split(",");
        Set<String> namasSet1 = new HashSet<String>();// 老的字符串
        Set<String> namasSet2 = new HashSet<String>();// 新的字符串
        for (int i = 0; i < names1.length; i++) {
            namasSet1.add(names1[i]);
        }
        for (int j = 0; j < names2.length; j++) {
            namasSet2.add(names2[j]);
        }

        for (int i = 0; i < names1.length; i++) {
            if (!"".equals(names1[i]) && !namasSet2.contains(names1[i])) {// 修改后不包含的子串
                deleteStr += names1[i] + "<br/>";
            } else {
                nullstrdel += names1[i] + "<br/>";
            }
        }

        for (int j = 0; j < names2.length; j++) {
            if (!"".equals(names2[j]) && !namasSet1.contains(names2[j])) {// 原来字符中没有的子串
                addStr += names2[j] + "<br/>";
            } else {
                nullstradd += names2[j] + "<br/>";
            }
        }
        // addStr = addStr.substring(0,addStr.length()-1);
        addStr = "修改为：<br/>" + addStr;
        deleteStr = deleteStr.substring(0, deleteStr.length() - 1);
        deleteStr = "原纪录：" + deleteStr + "<br/>";
        editLog = deleteStr + addStr;

        return editLog;
    }

    public static final float DEFAULT_DPI = 105;

    public static void pdfToImage(String pdfPath) {
        try {
            if (pdfPath == null || "".equals(pdfPath) || !pdfPath.endsWith(".pdf"))
                return;
            // 图像合并使用参数
            int width = 1280; // 总宽度
            int[] singleImgRGB; // 保存一张图片中的RGB数据
            int shiftHeight = 0;
            BufferedImage imageResult = null;// 保存每张图片的像素值
            // 利用PdfBox生成图像
            /*
             * PDDocument pdDocument = PDDocument.load(new File(pdfPath));
             * PDFRenderer renderer = new PDFRenderer(pdDocument);
             */
            // 循环每个页码
            /*
             * for(int i=0,len=pdDocument.getNumberOfPages(); i<len; i++){
             * BufferedImage image=renderer.renderImageWithDPI(i,
             * DEFAULT_DPI,ImageType.RGB); int imageHeight=image.getHeight();
             * int imageWidth=image.getWidth(); if(i==0){//计算高度和偏移量
             * width=imageWidth;//使用第一张图片宽度; //保存每页图片的像素值 imageResult= new
             * BufferedImage(width, imageHeight*len,
             * BufferedImage.TYPE_INT_RGB); }else{ shiftHeight += imageHeight;
             * // 计算偏移高度 } imageResult = resize(image, 1000, 1000); //
             * singleImgRGB= image.getRGB(0, 0, width, imageHeight, null, 0,
             * width); // imageResult.setRGB(0, shiftHeight, width, imageHeight,
             * singleImgRGB, 0, width); // 写入流中 } pdDocument.close(); File
             * outFile = new File(pdfPath.replace(".pdf", ".jpg"));
             * ImageIO.write(imageResult, "jpg", outFile);// 写图片
             */
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static BufferedImage resize(BufferedImage source, int targetW, int targetH) {
        int type = source.getType();
        BufferedImage target = null;
        double sx = (double) targetW / source.getWidth();
        double sy = (double) targetH / source.getHeight();
        if (sx > sy) {
            sx = sy;
            targetW = (int) (sx * source.getWidth());
        } else {
            sy = sx;
            targetH = (int) (sy * source.getHeight());
        }
        if (type == BufferedImage.TYPE_CUSTOM) {
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetW, targetH);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else {
            target = new BufferedImage(targetW, targetH, type);
        }
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return target;
    }

    /**
     * 把日期型yyyy-mm-dd hh24换成字符串
     *
     * @return String
     */
    public static String getDateYMDH(Date date) {
        if (date == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH");
        String s = df.format(date);
        return s;
    }


    /**
     * 把日期转成yyyy-MM-dd HH:mm:ss的字符串
     *
     * @param dates
     * @return
     */
    public static String getDateYMDHMSM(Date dates) {
        if (dates == null)
            return null;
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyyMMddHHmmssms");
        String s = df.format(dates);
        return s;

    }


    /**
     * 根据AQI指数获得空气质量等级
     *
     * @param AQI
     * @return String
     */
    public static String getQualityByAQI(Integer AQI) {
        String quality = "";
        if (AQI != null) {
            if (AQI >= 0 && AQI <= 50) {
                quality = "优";
            }
            if (AQI > 50 && AQI <= 100) {
                quality = "良好";
            }
            if (AQI > 100 && AQI <= 150) {
                quality = "轻度污染";
            }
            if (AQI > 150 && AQI <= 200) {
                quality = "中度污染";
            }
            if (AQI > 200 && AQI <= 300) {
                quality = "重度污染";
            }
            if (AQI > 300) {
                quality = "严重污染";
            }
        }
        return quality;
    }

    /**
     * 根据AQI指数获得空气质量等级
     *
     * @param AQI
     * @return String
     */
    public static String getQualityCodeByAQI(Integer AQI) {
        String quality = "";
        if (AQI != null) {
            if (AQI >= 0 && AQI <= 50) {
                quality = "you";
            }
            if (AQI > 50 && AQI <= 100) {
                quality = "lianghao";
            }
            if (AQI > 100 && AQI <= 150) {
                quality = "qingdu";
            }
            if (AQI > 150 && AQI <= 200) {
                quality = "zhongdu";
            }
            if (AQI > 200 && AQI <= 300) {
                quality = "zhongdu1";
            }
            if (AQI > 300) {
                quality = "yanzhong";
            }
        }
        return quality;
    }

    /**
     * 根据AQI指数获得空气质量等级
     *
     * @param AQI
     * @return String
     */
    public static String getQualityAirLevelByAQI(Integer AQI) {
        String quality = "";
        if (AQI != null) {
            if (AQI >= 0 && AQI <= 50) {
                quality = "一级";
            }
            if (AQI > 50 && AQI <= 100) {
                quality = "二级";
            }
            if (AQI > 100 && AQI <= 150) {
                quality = "三级";
            }
            if (AQI > 150 && AQI <= 200) {
                quality = "四级";
            }
            if (AQI > 200 && AQI <= 300) {
                quality = "五级";
            }
            if (AQI > 300) {
                quality = "六级";
            }
        }
        return quality;
    }


    /**
     * 根据所传的正则表达式和字符串返回匹配的结果
     *
     * @param regexstr strcode
     * @return String
     */
    public static String getRegexstr(String regexstr, String strcode) {
        String result = "";
        Pattern pattern = Pattern.compile(regexstr);
        Matcher matcher = pattern.matcher(strcode);

        if (matcher.find()) {
            result = matcher.group(0);
        }
        return result;
    }

    /**
     *      * 判断时间是否在时间段内([))
     *      * 
     *      * @param nowTime
     *      * @param beginTime
     *      * @param endTime
     *      * @return
     *     
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        if (date.after(begin) && date.before(end)) {
            return true;
        } else if (nowTime.compareTo(beginTime) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 比较两个日期之间的大小
     *
     * @param minDate
     * @param maxDate
     * @return 前者大于后者返回true 反之false
     */
    public static boolean compareDate(Date minDate, Date maxDate) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(minDate);
        c2.setTime(maxDate);

        int result = c1.compareTo(c2);
        if (result >= 0)
            return true;
        else
            return false;
    }


    public static final String[] directName =
            new String[]{"北", "东北偏北", "东北", "东北偏东", "东", "东南偏东", "东南", "东南偏南", "南",
                    "西南偏南", "西南", "西南偏西", "西", "西北偏西", "西北", "西北偏北"};
    public static final String[] directCode =
            new String[]{"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S",
                    "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 5:21
     * @Description: 风向转换
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: key：code/name，degrees：度数
     * @return:
     */
    public static String windDirectionSwitch(Double degrees, String key) {
        int index = 0;
        if (348.75 <= degrees && degrees <= 360) {
            index = 0;
        } else if (0 <= degrees && degrees <= 11.25) {
            index = 0;
        } else if (11.25 < degrees && degrees <= 33.75) {
            index = 1;
        } else if (33.75 < degrees && degrees <= 56.25) {
            index = 2;
        } else if (56.25 < degrees && degrees <= 78.75) {
            index = 3;
        } else if (78.75 < degrees && degrees <= 101.25) {
            index = 4;
        } else if (101.25 < degrees && degrees <= 123.75) {
            index = 5;
        } else if (123.75 < degrees && degrees <= 146.25) {
            index = 6;
        } else if (146.25 < degrees && degrees <= 168.75) {
            index = 7;
        } else if (168.75 < degrees && degrees <= 191.25) {
            index = 8;
        } else if (191.25 < degrees && degrees <= 213.75) {
            index = 9;
        } else if (213.75 < degrees && degrees <= 236.25) {
            index = 10;
        } else if (236.25 < degrees && degrees <= 258.75) {
            index = 11;
        } else if (258.75 < degrees && degrees <= 281.25) {
            index = 12;
        } else if (281.25 < degrees && degrees <= 303.75) {
            index = 13;
        } else if (303.75 < degrees && degrees <= 326.25) {
            index = 14;
        } else if (326.25 < degrees && degrees < 348.75) {
            index = 15;
        } else {
            System.out.println("未定义");
        }
        if ("code".equals(key)) {
            return directCode[index];
        } else if ("name".equals(key)) {
            return directName[index];
        } else {
            return null;
        }
    }

    /**
     * @author: lip
     * @date: 2020/4/2 0002 下午 3:09
     * @Description: 根据编码获取风值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Double windDirectionSwitch(String code) {
        Double value = null;
        switch (code) {
            case "N":
                value = 360d;
                break;
            case "NNE":
                value = 11.25d;
                break;
            case "NE":
                value = 33.75d;
                break;
            case "ENE":
                value = 56.25d;
                break;
            case "E":
                value = 78.75d;
                break;
            case "ESE":
                value = 101.25d;
                break;
            case "SE":
                value = 123.75d;
                break;
            case "SSE":
                value = 146.25d;
                break;
            case "S":
                value = 168.75d;
                break;
            case "SSW":
                value = 191.25d;
                break;
            case "SW":
                value = 213.75d;
                break;
            case "WSW":
                value = 236.25d;
                break;
            case "W":
                value = 258.75d;
                break;
            case "WNW":
                value = 281.25d;
                break;
            case "NW":
                value = 303.75d;
                break;
            case "NNW":
                value = 326.25;
                break;
        }
        return value;

    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 5:56
     * @Description: 风速转换
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static final String[] speedName =
            new String[]{"静风", "一级风", "二级风", "三级风", "四级风", "大于四级风"};
    public static final String[] speedCode =
            new String[]{"quietwind", "onewind", "twowind", "threewind", "fourwind", "upperfourwind"};
    public static final String[] speedValue =
            new String[]{"0-0.2m/s", "0.3-1.5m/s", "1.6-3.3m/s", "3.4-5.4m/s", "5.5-7.9m/s", ">7.9m/s"};

    public static String windSpeedSwitch(Double degrees, String key) {
        int index = 0;
        if (0 <= degrees && degrees <= 0.2) {
            index = 0;
        } else if (0.3 <= degrees && degrees <= 1.5) {
            index = 1;
        } else if (1.6 <= degrees && degrees <= 3.3) {
            index = 2;
        } else if (3.4 <= degrees && degrees <= 5.4) {
            index = 3;
        } else if (5.5 <= degrees && degrees <= 7.9) {
            index = 4;
        } else if (degrees > 7.9) {
            index = 5;
        }
        if ("value".equals(key)) {
            return speedValue[index];
        } else if ("name".equals(key)) {
            return speedName[index];
        } else if ("code".equals(key)) {
            return speedCode[index];
        } else {
            return null;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 7:12
     * @Description: 获取数组的平均值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getListAvgValue(List<Double> doubles) {
        String avg = "";
        if (doubles != null && doubles.size() > 0) {
            Double total = 0d;
            for (Double d : doubles) {
                total += d;
            }
            avg = DataFormatUtil.formatDoubleSaveOne(total / doubles.size());
        }
        return avg;
    }

    
    
    /**
     * @Description: 获取数组平均值，并保留指定小数位置
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/10/28 9:26
     */ 
    public static String getListAvgValueFormatData(List<Double> doubles,String format) {
        String avg = "";
        if (doubles != null && doubles.size() > 0) {
            Double total = 0d;
            for (Double d : doubles) {
                total += d;
            }
            avg = DataFormatUtil.formatDouble(format,total / doubles.size());
        }
        return avg;
    }
    
    
    public static Double getListAvgDValue(List<Double> doubles) {
        Double avg = 0d;
        if (doubles != null && doubles.size() > 0) {
            Double total = 0d;
            for (Double d : doubles) {
                total += d;
            }
            avg = (total / doubles.size());
        }
        return avg;
    }

    public static Double getListSumDValue(List<Double> doubles) {
        Double total = 0d;
        if (doubles != null && doubles.size() > 0) {
            for (Double d : doubles) {
                total += d;
            }
        }
        return total;
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 7:12
     * @Description: 获取数组的平均值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getListAvgValueSaveTwo(List<Double> doubles) {
        String avg = "";
        if (doubles != null && doubles.size() > 0) {
            Double total = 0d;
            for (Double d : doubles) {
                total += d;
            }
            avg = DataFormatUtil.formatDoubleSaveTwo(total / doubles.size());
        }
        return avg;
    }

    /**
     * @author: lip
     * @date: 2019/7/5 0005 上午 11:48
     * @Description: 获取两组数的相关性系数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Double getRelationPercent(List<Double> xData, List<Double> yData) {
        Double relationpercent = null;
        if (xData.size() == yData.size() && xData.size() > 0) {
            int m = xData.size();
            //分子
            Double molecule1 = 0d;
            Double xsum = 0d;
            Double ysum = 0d;
            Double x2sum = 0d;
            Double y2sum = 0d;
            for (int i = 0; i < m; i++) {
                molecule1 += xData.get(i) * yData.get(i);
                xsum += xData.get(i);
                ysum += yData.get(i);
                x2sum += Math.pow(xData.get(i), 2);
                y2sum += Math.pow(yData.get(i), 2);
            }
            Double molecule2 = m * (xsum / m) * (ysum / m);
            Double molecule = molecule1 - molecule2;
            //分母
            Double denominator1 = x2sum - m * Math.pow(xsum / m, 2);
            Double denominator2 = y2sum - m * Math.pow(ysum / m, 2);
            Double denominatorSum = denominator1 * denominator2;
            Boolean isF = false;

            if (denominatorSum < 0) {
                denominatorSum = -denominatorSum;
                isF = true;
            }
            Double denominator = Math.sqrt(denominatorSum);
            if (isF) {
                denominator = -denominator;
            }
            if (molecule == 0 || denominator == 0) {
                relationpercent = 0d;
            } else {
                relationpercent = molecule / denominator;
                relationpercent = DataFormatUtil.formatDoubleSaveThree(relationpercent);
            }
        }
        return relationpercent;
    }


    /**
     * @author: chengzq
     * @date: 2020/11/20 0020 上午 11:32
     * @Description: 余弦相似度算法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [va, vb]
     * @throws:
     */
    public static Double cosineSimilarity(List<Double> va, List<Double> vb) {
        double simVal = 0;
        if (va.size() == vb.size() && va.size() > 0) {
            int size = va.size();
            double num = 0;
            double den = 1;
            double powa_sum = 0;
            double powb_sum = 0;
            for (int i = 0; i < size; i++) {
                double a = Double.parseDouble(va.get(i).toString());
                double b = Double.parseDouble(vb.get(i).toString());

                num = num + a * b;
                powa_sum = powa_sum + Math.pow(a, 2);
                powb_sum = powb_sum + Math.pow(b, 2);
            }
            double sqrta = Math.sqrt(powa_sum);
            double sqrtb = Math.sqrt(powb_sum);
            den = sqrta * sqrtb;
            if (den == 0) {
                return 0d;
            }
            simVal = num / den;
        }

        return simVal;
    }


    /**
     * @author: chengzq
     * @date: 2020/11/20 0020 上午 11:33
     * @Description: 向量相似度算法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [va, vb]
     * @throws:
     */
    public static Double vectorSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() == vector2.size() && vector1.size() > 0) {
            double sum = 0.0;
            Double v1Len = 0.0;
            Double v2Len = 0.0;
            for (int i = 0; i < vector1.size(); i++) {
                sum += vector1.get(i) * vector2.get(i);
                v1Len += vector1.get(i) * vector1.get(i);
            }
            for (int k = 0; k < vector2.size(); k++) {
                v2Len += vector2.get(k) * vector2.get(k);
            }

            if ((Math.sqrt(v1Len) * Math.sqrt(v2Len)) == 0) {
                return 0d;
            }
            return sum / (Math.sqrt(v1Len) * Math.sqrt(v2Len));
        }
        return null;
    }

    /**
     * @author: lip
     * @date: 2019/7/5 0005 上午 11:48
     * @Description: 获取两组数的相关性系数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
   /* public static Double getRelationPercent(List<Double> xData, List<Double> yData) {
        Double relationpercent = null;

        if (xData.size() == yData.size() && xData.size() > 0) {
            double x = 0, y = 0, z = 0;
            for (int i = 0; i < xData.size(); i++) {
                x += xData.get(i) * xData.get(i);
                y += yData.get(i) * yData.get(i);
                z += xData.get(i) * yData.get(i);
            }
            x = Math.sqrt(x);
            y = Math.sqrt(y);
            relationpercent = z / (x * y);
            relationpercent = DataFormatUtil.formatDoubleSaveThree(relationpercent);
        }
        return relationpercent;
    }*/


    /**
     * @author: lip
     * @date: 2019/7/5 0005 下午 3:28
     * @Description: 获取线性回归方程斜率值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Double getRelationSlope(List<Double> xData, List<Double> yData) {
        Double slope = null;
        if (xData.size() == yData.size() && xData.size() > 0) {
            int m = xData.size();
            //分子
            Double molecule1 = 0d;
            Double xsum = 0d;
            Double ysum = 0d;
            Double x2sum = 0d;

            for (int i = 0; i < m; i++) {
                molecule1 += xData.get(i) * yData.get(i);
                xsum += xData.get(i);
                ysum += yData.get(i);
                x2sum += Math.pow(xData.get(i), 2);

            }
            Double molecule2 = xsum * ysum / m;
            Double molecule = molecule1 - molecule2;
            //分母
            Double denominator = x2sum - Math.pow(xsum, 2) / m;
            if (molecule == 0 || denominator == 0) {
                slope = 0d;
            } else {
                slope = molecule / denominator;
                slope = DataFormatUtil.formatDoubleSaveThree(slope);
            }
        }
        return slope;
    }

    /**
     * @author: lip
     * @date: 2019/7/5 0005 下午 3:28
     * @Description: 获取线性回归方程常量值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Double getRelationConstant(List<Double> xData, List<Double> yData, Double slope) {
        Double constant = null;
        if (xData.size() == yData.size() && xData.size() > 0) {
            int m = xData.size();
            Double xsum = 0d;
            Double ysum = 0d;
            for (int i = 0; i < m; i++) {
                xsum += xData.get(i);
                ysum += yData.get(i);
            }
            constant = ysum / m - slope * xsum / m;
            constant = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo(constant));
        }
        return constant;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/24 11:49
     * @Description: 时间字符串格式化为 yyyy-MM-dd
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Object formatTimeToYMDByString(Object time) {
        if (time != null && time.toString().length() >= 10) {
            return time.toString().substring(0, 10);
        }
        return time;
    }

    public static Object formatTimeToYByString(Object time) {
        if (time != null && time.toString().length() >= 4) {
            return time.toString().substring(0, 4);
        }
        return time;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/31 17:35
     * @Description: 所有日期转换为小时
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Object formatTimeToYMDHByString(Object time) {
        if (time != null && time.toString().length() >= 13) {
            return time.toString().substring(0, 13);
        } else if (time != null && time.toString().length() == 10) {
            return time.toString() + " 00";
        }
        return time;
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/24 13:28
     * @Description: 获取时间在该月哪一周 返回格式 2019年7月1周
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Object getTimeOfYMWByString(Object time) {
        if (time != null) {
            Object s = formatTimeToYMDByString(time);
            LocalDate date = LocalDate.parse(s.toString());
            int dayOfMonth = date.getDayOfMonth();
            int week;
            if (dayOfMonth % 7 > 0) { //有余数
                week = dayOfMonth / 7 + 1;
            } else {
                week = dayOfMonth / 7;
            }
            int year = date.getYear();
            int month = date.getMonth().getValue();
            return year + "年" + month + "月第" + week + "周";
        }
        return null;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/24 13:42
     * @Description: 获取时间在该月哪一月 返回格式 2019年7月1周
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Object getTimeOfYMByString(String time) {
        if (time != null) {
            Object s = formatTimeToYMDByString(time);
            LocalDate date = LocalDate.parse(s.toString());
            int year = date.getYear();
            int month = date.getMonth().getValue();
            return year + "年" + month + "月";
        }
        return null;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/27 19:46
     * @Description: 格式化CST时间
     * @param:
     * @return:
     */
    public static String formatCST(String cst) throws ParseException {
        //获取监测时间
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date d = sdf.parse(cst);
        String formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
        return formatDate;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/30 14:03
     * @Description: 获取距当前时间过去24小时时间数组   2019-05-29 13:00:00
     * @param:
     * @return:
     */
    public static List<String> getBefore24Hours() {
        //现在时间
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime minus = localDateTime.minus(1, ChronoUnit.DAYS);
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
        List<String> list = new ArrayList<>();
        String time = dtf2.format(minus);
        list.add(time);
        while (!minus.equals(localDateTime)) {
            minus = minus.plusHours(1);
            time = dtf2.format(minus);
            list.add(time);
        }
        return list;
    }

    /**
     * 获取某年第一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getCurrYearFirst(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    /**
     * 获取某年最后一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getCurrYearLast(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }

    /**
     * 获取某年某月第一天日期
     *
     * @param yearMonth 年月 yyyy-MM
     * @return String yyyy-MM-dd
     */
    public static String getYearMothFirst(String yearMonth) {
        return yearMonth + "-01";
    }

    /**
     * 获取某年某月最后一天日期
     *
     * @param yearMonth 年月 yyyy-MM
     * @return String yyyy-MM-dd
     */
    public static String getYearMothLast(String yearMonth) {
        return YearMonth.parse(yearMonth).atEndOfMonth().toString();
    }

    /**
     * @author: zzc
     * @date: 2019/9/24 15:20
     * @Description: 获取每月天数
     * @param:
     * @return:
     */
    public static int getYearMotDays(String yearMonth) {
        return YearMonth.parse("2019-08").lengthOfMonth();
    }


    public static Boolean deleteFile(File file) {
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()) {
            System.out.println("文件删除失败,请检查文件是否存在以及文件路径是否正确");
            return false;
        }
        //获取目录下子文件
        File[] files = file.listFiles();
        //遍历该目录下的文件对象
        for (File f : files) {
            //判断子目录是否存在子目录,如果是文件则删除
            if (f.isDirectory()) {
                //递归删除目录下的文件
                deleteFile(f);
            } else {
                //文件删除
                f.delete();
                //打印文件名
                System.out.println("文件名：" + f.getName());
            }
        }
        //文件夹删除
        file.delete();
        System.out.println("目录名：" + file.getName());
        return true;
    }


    /**
     * 获取某年第一天日期
     *
     * @param year 年 yyyy
     * @return String yyyy-MM-dd
     */
    public static String getYearFirst(Object year) {
        return year + "-01-01";
    }

    /**
     * 获取某年最后一天日期
     *
     * @param year 年月 yyyy
     * @return String yyyy-MM-dd
     */
    public static String getYearLast(Object year) {
        return year + "-12-31";
    }

    public static String groupTimesByIntervalTime(List<String> timesInfo, int m) {
        if (timesInfo.size() == 0) {
            return "";
        }
        List<String> times = timesInfo.stream().distinct().collect(Collectors.toList());
        if (timesInfo.size() == 1) {
            return timesInfo.get(0);
        }
        Collections.sort(times);
        LocalTime index = LocalTime.parse(times.get(0));
        Map<String, String> map = new LinkedHashMap<>();
        for (String time : times) {
            LocalTime firstTime = LocalTime.parse(time);
            if (index.plusMinutes(m).isBefore(firstTime)) {    //相差大于30分钟
                map.put(firstTime.toString(), firstTime.toString());
                index = firstTime;
            } else {
                map.put(index.toString(), firstTime.toString());
            }
        }
        StringBuilder stringBuffer = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String entryKey = entry.getKey();
            String value = entry.getValue();
            if (entryKey.equals(value)) {
                stringBuffer.append(entryKey).append("、");
            } else {
                stringBuffer.append(entryKey).append("~").append(value).append("、");
            }
        }
        if (stringBuffer.length() > 0) {
            stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());
        }
        return stringBuffer.toString();
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/13 15:36
     * @Description: 两个时间通过间隔天数分隔开
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, String>> separateTime(String startDate, String endDate, long intervalDays) {
        List<Map<String, String>> timeMapArray = new ArrayList<>();
        LocalDate startTime = LocalDate.parse(startDate);
        LocalDate endTime = LocalDate.parse(endDate);
        long until = startTime.until(endTime, ChronoUnit.DAYS);
        if (until < intervalDays) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("starttime", startDate);
            map.put("endtime", endDate);
            timeMapArray.add(map);
            return timeMapArray;
        }
        long index = until / intervalDays;
        for (int i = 0; i < index; i++) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("starttime", startTime.plusDays(i * intervalDays).toString());
            map.put("endtime", startTime.plusDays((i + 1) * intervalDays - 1).toString());
            timeMapArray.add(map);
        }
        long remainder = until % intervalDays;
        if (remainder <= 0) {
            return timeMapArray;
        }
        Map<String, String> map = new LinkedHashMap<>();
        map.put("starttime", endTime.plusDays(1 - remainder).toString());
        map.put("endtime", endTime.toString());
        timeMapArray.add(map);
        return timeMapArray;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/13 15:36
     * @Description: 两个时间通过小时分割
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: yyyy-MM-dd hh
     */
    public static List<String> separateTimeForHour(String startTime, String endTime, long hours) {
        List<String> times = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TemporalAccessor parse = formatter.parse(startTime);
        TemporalAccessor parse1 = formatter.parse(endTime);
        LocalDateTime startTime1 = LocalDateTime.from(parse);
        LocalDateTime endTime1 = LocalDateTime.from(parse1);
        while (startTime1.isBefore(endTime1)) {
            String s = formatter.format(startTime1);
            String substring = s.substring(0, 13);
            times.add(substring);
            startTime1 = startTime1.plusHours(hours);
        }
        return times;
    }

    /**
     * @author: lip
     * @date: 2019/8/20 0020 上午 9:10
     * @Description: 获取某年+某月+某周的开始日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getFirstDayOfWeek(int year, int month, int week) {
        week = week - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, 1);
        Calendar cal = (Calendar) calendar.clone();
        cal.add(Calendar.DATE, week * 7);
        return getFirstDayOfWeek(cal.getTime());
    }

    private static Calendar getCalendarFormYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.YEAR, year);
        return cal;
    }


    public static String getEndDayOfWeekNo(int year, int weekNo) {
        Calendar cal = getCalendarFormYear(year);
        cal.set(Calendar.WEEK_OF_YEAR, weekNo);
        cal.add(Calendar.DAY_OF_WEEK, 6);
        String ymd = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" +
                cal.get(Calendar.DAY_OF_MONTH);
        return DataFormatUtil.FormatDateOneToOther(ymd, "yyyy-M-d", "yyyy-MM-dd");

    }

    //获取某一年的某一周的周一日期
    public static String getStartDayOfWeekNo(int year, int weekNo) {
        Calendar cal = getCalendarFormYear(year);
        cal.set(Calendar.WEEK_OF_YEAR, weekNo);

        String ymd = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" +
                cal.get(Calendar.DAY_OF_MONTH);

        return DataFormatUtil.FormatDateOneToOther(ymd, "yyyy-M-d", "yyyy-MM-dd");

    }


    /**
     * @author: lip
     * @date: 2019/8/20 0020 上午 9:10
     * @Description: 获取某年+某月+某周的结束日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getLastDayOfWeek(int year, int month, int week) {
        week = week - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, 1);
        Calendar cal = (Calendar) calendar.clone();
        cal.add(Calendar.DATE, week * 7);

        return getLastDayOfWeek(cal.getTime());
    }

    /**
     * @author: lip
     * @date: 2019/8/20 0020 上午 9:10
     * @Description: 获取某日期所在周的开始日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getFirstDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK,
                calendar.getFirstDayOfWeek()); // Sunday
        return calendar.getTime();
    }


    /**
     * 罗马数字转阿拉伯数字
     *
     * @param romanStr
     * @return
     */
    public static int formatLevelToNum(String romanStr) {
        int num;
        switch (romanStr) {
            case "Ⅰ":
                num = 1;
                break;
            case "Ⅱ":
                num = 2;
                break;
            case "Ⅲ":
                num = 3;
                break;
            case "Ⅳ":
                num = 4;
                break;
            case "Ⅴ":
                num = 5;
                break;
            case "劣Ⅴ":
                num = 6;
                break;
            default:
                num = 0;
                break;
        }
        return num;
    }

    /**
     * @author: lip
     * @date: 2019/8/20 0020 上午 9:10
     * @Description: 获取某日期所在周的结束日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getLastDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK,
                calendar.getFirstDayOfWeek() + 6); // Saturday
        return calendar.getTime();
    }

    public static Date[] getQueryTimeToQueryVoByTimeType(String timeType, String startTime, String endTime) {
        Assert.notNull(timeType, "timeType must not be null!");
        Assert.notNull(startTime, "startTime must not be null!");
        Assert.notNull(endTime, "endTime must not be null!");
        Date starttime = null;
        Date endtime = null;
        switch (timeType) {
            case "hour":
                starttime = DataFormatUtil.parseDate(startTime + ":00:00");
                endtime = DataFormatUtil.parseDate(endTime + ":59:59");
                break;
            case "day":
                starttime = DataFormatUtil.parseDate(startTime + " 00:00:00");
                endtime = DataFormatUtil.parseDate(endTime + " 23:59:59");
                break;
            case "week":
            case "month":
                String yearMothFirst = DataFormatUtil.getYearMothFirst(startTime);
                String yearMothFirst1 = DataFormatUtil.getYearMothLast(endTime);
                starttime = DataFormatUtil.parseDate(yearMothFirst + " 00:00:00");
                endtime = DataFormatUtil.parseDate(yearMothFirst1 + " 23:59:59");
                break;
            case "year":
                starttime = DataFormatUtil.parseDate(DataFormatUtil.getYearFirst(startTime) + " 00:00:00");
                endtime = DataFormatUtil.parseDate(DataFormatUtil.getYearLast(endTime) + " 23:59:59");
                break;
        }
        return new Date[]{starttime, endtime};
    }

    public static String getTimeStyleByTimeTypeForMongdb(String timeType) {
        switch (timeType) {
            case "hour":
                return "%Y-%m-%d %H";
            case "day":
                return "%Y-%m-%d";
            case "week":
                return "%Y-%m-%d";
            case "month":
                return "%Y-%m";
            case "year":
                return "%Y";
        }
        return "";
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/21 16:35
     * @Description: 统计时间段间隔
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Map<String, Integer> formatIntervalTimeNumList(Date startDate, Date endDate, int interval,
                                                                 Map<String, Integer> data) {
        List<Date> intervalTimeList = DataFormatUtil.getIntervalTimeList(startDate, endDate, interval);
        Map<String, Integer> timeAndNum = new LinkedHashMap<>();
        Date thisDate;
        String timeIntervalKey;
        //data排序
        Map<String, Integer> dataInfo = new LinkedHashMap<>();
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> dataInfo.put(x.getKey(), x.getValue()));
        for (Map.Entry<String, Integer> entry : dataInfo.entrySet()) {
            String time = entry.getKey();
            int num = entry.getValue();
            thisDate = DataFormatUtil.getDateYMDHMS(time);
            for (int i = 0; i < intervalTimeList.size() - 1; i++) {
                startDate = intervalTimeList.get(i);
                endDate = intervalTimeList.get(i + 1);
                if (DataFormatUtil.belongCalendar(thisDate, startDate, endDate)) {
                    timeIntervalKey = DataFormatUtil.getSomeDateHour(startDate) + ":" + DataFormatUtil.getSomeDateMinuteString(startDate)
                            + "~" + DataFormatUtil.getSomeDateHour(endDate) + ":" + DataFormatUtil.getSomeDateMinuteString(endDate);
                    if (timeAndNum.containsKey(timeIntervalKey)) {
                        timeAndNum.put(timeIntervalKey, timeAndNum.get(timeIntervalKey) + num);
                    } else {
                        timeAndNum.put(timeIntervalKey, num);
                    }
                }
            }
        }
        Map<String, Integer> timeAndNumTemp = new LinkedHashMap<>();
        String startPoint = "";
        String endPoint = "";
        String startTime;
        String endTime;
        Integer num;
        for (String key : timeAndNum.keySet()) {
            startTime = key.split("~")[0];
            endTime = key.split("~")[1];
            if (endPoint.equals(startTime)) {
                String tempKey = startPoint + "~" + endPoint;
                num = timeAndNum.get(key) + timeAndNumTemp.get(tempKey);
                endPoint = endTime;
                timeAndNumTemp.remove(tempKey);
            } else {
                startPoint = startTime;
                endPoint = endTime;
                num = timeAndNum.get(key);
            }
            timeIntervalKey = startPoint + "~" + endPoint;
            timeAndNumTemp.put(timeIntervalKey, num);
        }
        //排序
        Map<String, Integer> result = new LinkedHashMap<>();
        timeAndNumTemp.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/09/04 12:35
     * @Description: 根据两个点位的经纬度获取，一点相对于另一点的方位角
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [lat_a：a点位纬度，lng_a：a点位经度,lat_b：b点位纬度，lng_b：b点位经度,]
     * @return:
     */
    public static double getAngle1(double lat_a, double lng_a, double lat_b, double lng_b) {
        lat_a = lat_a * Math.PI / 180;
        lng_a = lng_a * Math.PI / 180;
        lat_b = lat_b * Math.PI / 180;
        lng_b = lng_b * Math.PI / 180;
        //求B点相对于A点的方位角，因此这里是lng_b-lng_a
        double y = Math.sin(lng_b - lng_a) * Math.cos(lat_b);
        double x = Math.cos(lat_a) * Math.sin(lat_b) - Math.sin(lat_a) * Math.cos(lat_b) * Math.cos(lng_b - lng_a);
        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        if (brng < 0)
            brng = brng + 360;
        return brng;
    }

    /**
     * @author: lip
     * @date: 2019/9/23 0023 下午 3:22
     * @Description: 判断对象是否为null 或 ""
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static boolean isObjectNull(Object object) {
        if (object == null || "".equals(object)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @author: lip
     * @date: 2019/9/23 0023 下午 4:15
     * @Description: 转换list 为 string ,用分割符分割
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String FormatListToString(List<String> listData, String split) {
        String result = "";
        if (listData != null && listData.size() > 0) {
            for (String item : listData) {
                result += item + split;
            }
            if (!"".equals(result)) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    /**
     * 根据map的value排序
     *
     * @param map    待排序的map
     * @param isDesc 是否降序，true：降序，false：升序
     * @return 排序好的map
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, boolean isDesc) {
        Map<K, V> result = Maps.newLinkedHashMap();
        if (isDesc) {
            map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByValue().reversed())
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        } else {
            map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByValue())
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        }
        return result;
    }


    /**
     * 根据map的key排序
     *
     * @param map    待排序的map
     * @param isDesc 是否降序，true：降序，false：升序
     * @return 排序好的map
     * @author
     */
    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map, boolean isDesc) {
        Map<K, V> result = Maps.newLinkedHashMap();
        if (isDesc) {
            map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByKey().reversed())
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        } else {
            map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByKey())
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        }
        return result;
    }


    public static String getFirstKey(Map<String, Integer> map) {
        String key = null;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            key = entry.getKey();
            break;
        }
        return key;
    }

    /**
     * @author: lip
     * @date: 2019/9/24 0024 上午 10:28
     * @Description: 合并连续数字
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: numList:
     * @return:
     */
    public static String mergeContinueNum(List<Integer> numList, int index, String split, String numAfter) {

        Collections.sort(numList);

        int end = index;
        if (numList.size() == index) {//结束条件，遍历完数组
            return "";
        } else {
            for (int i = index; i < numList.size(); i++) {
                if (i < numList.size() - 1) {
                    if (numList.get(i) + 1 == numList.get(i + 1)) {
                        end = i;
                    } else {
                        if (i > index)
                            end = end + 1;
                        break;
                    }
                } else {
                    if (end == numList.size() - 2) {
                        end = numList.size() - 1;
                        break;
                    }
                }
            }
            if (index == end)//相等说明不连续
                return numList.get(index) + numAfter +
                        split + mergeContinueNum(numList, end + 1, split, numAfter);
            else//连续
                return numList.get(index) + numAfter + "-" + numList.get(end) + numAfter +
                        split + mergeContinueNum(numList, end + 1, split, numAfter);

        }

    }


    /**
     * @author: lip
     * @date: 2019/9/24 0024 上午 10:28
     * @Description: 合并连续数字
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: dateList:[yyyy-MM-dd HH:mm:ss]
     * @return:
     */
    public static String mergeContinueDate(List<String> dateList, int interval, String beforeDataFormat, String split, String afterDataFormat) {

        List<Date> dates = new ArrayList<>();
        String ymd = getDateYMD(new Date());
        String hourMinute;
        String ymdhm;
        Date date;
        for (String time : dateList) {
            hourMinute = FormatDateOneToOther(time, beforeDataFormat, afterDataFormat);
            ymdhm = ymd + " " + hourMinute;
            date = parseDateByFormat(ymdhm, beforeDataFormat);
            if (!dates.contains(date)) {
                dates.add(date);
            }
        }
        Collections.sort(dates);
        int startIndex = 0;
        String timeListString = getMergeDateContinueData(dates, startIndex, interval, split, afterDataFormat);
        if (StringUtils.isNotBlank(timeListString)) {
            timeListString = timeListString.substring(0, timeListString.length() - 1);
        }
        return timeListString;

    }

    /**
     * @author: lip
     * @date: 2019/9/24 0024 上午 10:28
     * @Description: 合并连续数字
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: dateList:[yyyy-MM-dd HH:mm:ss]
     * @return:
     */
    public static String mergeContinueDayDate(List<String> dateList, int interval, String beforeDataFormat, String split, String afterDataFormat) {

        List<Date> dates = new ArrayList<>();
        Date date;
        for (String time : dateList) {
            date = DataFormatUtil.getDateYMD(time);
            if (!dates.contains(date)) {
                dates.add(date);
            }
        }
        Collections.sort(dates);
        int startIndex = 0;
        String timeListString = getMergeDateContinueData(dates, startIndex, interval, split, afterDataFormat);
        if (StringUtils.isNotBlank(timeListString)) {
            timeListString = timeListString.substring(0, timeListString.length() - 1);
        }
        return timeListString;

    }


    private static String getMergeDateContinueData(List<Date> dates, int startIndex, int interval, String split, String afterDataFormat) {
        int endIndex = startIndex;
        if (dates.size() == startIndex) {//结束条件，遍历完数组
            return "";
        } else {
            for (int i = startIndex; i < dates.size(); i++) {
                if (i < dates.size() - 1) {
                    if (isLessEqualUpdate(dates.get(i), dates.get(i + 1), interval)) {
                        endIndex = i;
                    } else {
                        if (i > startIndex)
                            endIndex = endIndex + 1;
                        break;
                    }
                } else {
                    if (endIndex == dates.size() - 2) {
                        endIndex = dates.size() - 1;
                        break;
                    }
                }
            }
            if (startIndex == endIndex)//相等说明不连续
                return parseDateToStringByFormat(dates.get(startIndex), afterDataFormat)
                        + split + getMergeDateContinueData(dates, endIndex + 1, interval, split, afterDataFormat);
            else {
                return parseDateToStringByFormat(dates.get(startIndex), afterDataFormat) + "-" + parseDateToStringByFormat(dates.get(endIndex), afterDataFormat)
                        + split + getMergeDateContinueData(dates, endIndex + 1, interval, split, afterDataFormat);
            }

        }

    }

    /**
     * 标准差σ=sqrt(s^2)
     * 结果精度：scale
     * 牛顿迭代法求大数开方
     *
     * @param value
     * @param scale
     * @return
     */
    public static BigDecimal sqrt(BigDecimal value, int scale) {
        BigDecimal num2 = BigDecimal.valueOf(2);
        int precision = 100;
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal deviation = value;
        int cnt = 0;
        while (cnt < precision) {
            deviation = (deviation.add(value.divide(deviation, mc))).divide(num2, mc);
            cnt++;
        }
        deviation = deviation.setScale(scale, BigDecimal.ROUND_HALF_UP);
        return deviation;
    }

    /**
     * @author: lip
     * @date: 2020/3/6 0006 下午 4:16
     * @Description: 获取标准差
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static double standardDeviation(List<Double> doubles) {
        int m = doubles.size();
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += doubles.get(i);
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (doubles.get(i) - dAve) * (doubles.get(i) - dAve);
        }
        return Math.sqrt(dVar / m);
    }


    /**
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     * @author sunran   判断当前时间是否在时间区间内
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     * @author sunran   判断当前时间是否在时间区间内(不包含等于)
     */
    public static boolean isEffectiveDateForNeq(Date nowTime, Date startTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @author: lip
     * @date: 2020/5/11 0011 下午 1:38
     * @Description: 取整分钟日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String formatIntMinuteTime(String time) {
        String minuteString = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm:ss", "m");
        int minuteInt = Integer.parseInt(minuteString);
        Double temp = (Math.floor(minuteInt / 10));
        minuteInt = temp.intValue();
        return DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:" + minuteInt + "0:00");
    }

    /**
     * @author: xsm
     * @date: 2019/8/20 0020 下午 1:08
     * @Description: 比较两个时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public static boolean compare(String time1, String time2) throws ParseException {
        //如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //将字符串形式的时间转化为Date类型的时间
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        //Date类的一个方法，如果a早于b返回true，否则返回false
        if (a.before(b))
            return true;
        else
            return false;
    }


    public static int HourToSecond(Double hour) {
        int second = (int) (hour * 60 * 60);
        return second;
    }

    public static String secondToText(int second) {
        String text = "";
        int m = second / 60;
        if (m > 1) {//满1分钟
            Double h = m / 60d;
            if (h > 1) {//满1小时
                String HM = h.toString();
                int hInt = Integer.parseInt(HM.split("\\.")[0]);
                Double MD = Double.parseDouble("0." + HM.split("\\.")[1]);
                int mInt = (int) (MD * 60);
                text = hInt + "小时" + mInt + "分钟";
            } else {
                text = m + "分钟";
            }
        } else {
            text = m + "分钟";
        }
        return text;
    }

    //获取年的第几周
    public static int getWeekOfYear(Date dataTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);//设置星期一为一周开始的第一天
        calendar.setMinimalDaysInFirstWeek(4);//可以不用设置
        calendar.setTimeInMillis(dataTime.getTime());//获得当前的时间戳
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);//获得当前日期属于今年的第几周
        return weekOfYear;
    }

    public static boolean convertToBoolean(String jexlExp, Map<String, Object> map) {
        // 创建或检索引擎
        JexlEngine jexlEngine = new JexlBuilder().create();
        // 创建一个表达式
        JexlExpression jexlExpression = jexlEngine.createExpression(jexlExp);
        // 创建上下文并添加数据
        JexlContext jexlContext = new MapContext();
        for (String key : map.keySet()) {
            jexlContext.set(key, map.get(key));
        }
        // 得到结果
        Object result = jexlExpression.evaluate(jexlContext);
        return (boolean) result;
    }


    public static List<Map<String, Object>> buildTree(List<Map<String, Object>> dataList,
                                                      Object parentValue,
                                                      String parentKey,
                                                      String thisKey) {
        List<Map<String, Object>> topTree = new ArrayList<>();
        Map<String, Object> childTree;
        for (Map<String, Object> dataMap : dataList) {
            if (Objects.equals(dataMap.get(parentKey), parentValue)) {
                childTree = findChild(dataMap, dataList, parentKey, thisKey);
                topTree.add(childTree);
            }
        }
        return topTree;
    }

    private static Map<String, Object> findChild(Map<String, Object> node,
                                                 List<Map<String, Object>> dataList,
                                                 String parentKey,
                                                 String thisKey) {
        for (Map<String, Object> dataMap : dataList) {
            if (Objects.equals(dataMap.get(parentKey), node.get(thisKey))) {
                if (node.get("children") == null) {
                    node.put("children", new ArrayList<>());
                }
                List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");

                children.add(findChild(dataMap, dataList, parentKey, thisKey));
            }
        }
        return node;
    }

    public static double getMeterByDegree(double latitudeS, double longitudeS, double latitudeT, double longitudeT) {
        GlobalCoordinates source = new GlobalCoordinates(latitudeS, longitudeS);
        GlobalCoordinates target = new GlobalCoordinates(latitudeT, longitudeT);
        double meter = getDistanceMeter(source, target, Ellipsoid.WGS84);
        return meter;

    }

    /**
     * @Description: 返回（米）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/10/20 14:11
     */
    public static double getDistanceMeter(GlobalCoordinates gpsFrom, GlobalCoordinates gpsTo, Ellipsoid ellipsoid) {

        //创建GeodeticCalculator，调用计算方法，传入坐标系、经纬度用于计算距离
        GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(ellipsoid, gpsFrom, gpsTo);
        return geoCurve.getEllipsoidalDistance();
    }


    public static String getWindName(double lat1, double lon1, double lat2, double lon2) {
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);
        Double bear = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        return windDirectionSwitch(bear, "name");

    }


    public static String downloadFileByUrl(String fileUrl, String filePath) {
        long l = 0L;
        String staticAndMksDir = null;
        if (fileUrl != null) {
            //下载时文件名称
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("."));
            try {
                String dataStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String uuidName = UUID.randomUUID().toString();
                filePath = "resources/images/" + dataStr + "/" + uuidName + fileName;
                staticAndMksDir = Paths.get(ResourceUtils.getURL("classpath:").getPath(), "resources", "images", dataStr).toString();
                HttpUtil.downloadFile(fileUrl, staticAndMksDir + File.separator + uuidName + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }

        return filePath;
    }

    /**
     * 使用NIO下载文件， 需要 jdk 1.4+
     *
     * @param url
     * @param saveDir
     * @param fileName
     */
    public static void downloadByNIO(String url, String saveDir, String fileName) {
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        FileChannel foutc = null;
        try {
            rbc = Channels.newChannel(new URL(url).openStream());
            File file = new File(saveDir, fileName);
            file.getParentFile().mkdirs();
            fos = new FileOutputStream(file);
            foutc = fos.getChannel();
            foutc.transferFrom(rbc, 0, Long.MAX_VALUE);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (rbc != null) {
                try {
                    rbc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (foutc != null) {
                try {
                    foutc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void downloadByApacheCommonIO(String url, String saveDir, String fileName) {
        try {
            FileUtils.copyURLToFile(new URL(url), new File(saveDir, fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用NIO下载文件， 需要 jdk 1.7+
     *
     * @param url
     * @param saveDir
     * @param fileName
     */
    public static void downloadByNIO2(String url, String saveDir, String fileName) {
        try (InputStream ins = new URL(url).openStream()) {
            Path target = Paths.get(saveDir, fileName);
            Files.createDirectories(target.getParent());
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void saveAsFileWriter(String content) {
        FileWriter fwriter = null;
        try {
            String filePath = "D:\\videoFile\\";
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            fwriter = new FileWriter(filePath + DataFormatUtil.getDateYMDHMS1(new Date()) + ".txt", true);
            fwriter.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void sortStringList(List<String> list) {

        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String o1, String o2) {
                Collator collator = Collator.getInstance();
                return collator.getCollationKey(o1).compareTo(
                        collator.getCollationKey(o2));
            }
        };
        Collections.sort(list);
    }

    public static String getHTBDate(String time, String timeF, String htb) {
        String returnString = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(timeF);
            Date date = (java.util.Date) sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            if ("TB".equals(htb)) {
                calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
            } else {
                if ("yyyy-MM-dd".equals(timeF)){//日数据
                    calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
                }else {//小时数据
                    calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                }
            }
            SimpleDateFormat df = new SimpleDateFormat(timeF);
            returnString = df.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnString;

    }

    public static double addDouble(double number1 , double number2) {
        BigDecimal bigDecimal1 = new BigDecimal(String.valueOf(number1));
        BigDecimal bigDecimal2 = new BigDecimal(String.valueOf(number2));
        return bigDecimal1.add(bigDecimal2).doubleValue();
    }

    public static List<String> getMonthByYear(String year) throws Exception {
        String startTime = DataFormatUtil.getYearFirst(year);
        Date nowDay = new Date();
        String endTime =  DataFormatUtil.getDateY(nowDay).equals(year)?DataFormatUtil.getDateYM(nowDay):DataFormatUtil.getYearLast(year);
        List<String> monthList = DataFormatUtil.getMonthBetween(startTime,endTime);
        return monthList;


    }

    /**
     * @author: mmt
     * @date: 2022/8/19 0017 下午 1:45
     * @Description: 获取指定日期的前num年的日期
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static Date getPreYearDate(Date nowDate, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDate);
        calendar.add(Calendar.YEAR, -num);
        nowDate = calendar.getTime();
        return nowDate;
    }

    /**
     * 得到某个时间的前N小时
     *
     * @param someTimeString:yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getBeforeByHourTimeHMS(int hour, String someTimeString) {

        Date someTime = DataFormatUtil.getDateYMDH(someTimeString);
        String returnstr = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(someTime);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - hour);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    public static boolean dataIsMany(long totalCount) {
       String maxDataSet =  parseProperties("max.data.set");
       long defaultCount = 10000;
       if (StringUtils.isNotBlank(maxDataSet)){
           defaultCount = Long.parseLong(maxDataSet);
       }
       if (totalCount>defaultCount){
           return true;
       }else {
           return false;
       }

    }
    public static boolean exportDataIsMany(long totalCount) {
        String maxDataSet =  parseProperties("max.export.data.set");
        long defaultCount = 10000;
        if (StringUtils.isNotBlank(maxDataSet)){
            defaultCount = Long.parseLong(maxDataSet);
        }
        if (totalCount>defaultCount){
            return true;
        }else {
            return false;
        }

    }

}
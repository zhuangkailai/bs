package com.tjpu.sp.common.utils;

import com.google.common.collect.Maps;

import com.tjpu.pk.common.utils.DataFormatUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class FormatUtils {

    /**
     * @author: chengzq
     * @date: 2019/7/11 0011 上午 11:45
     * @Description: 将一个集合中的所有数字根据是否连续分成多个集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    public static List<List<Integer>> groupIntegerList(List<Integer> list) {
        List<List<Integer>> datas = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        Integer temp = null;
        for (int i = 0; i < list.size(); i++) {
            Integer integer = list.get(i);
            if (temp != null && integer - temp > 1) {
                datas.add(data);
                data = new ArrayList<>();
            }
            if (list.size() - 1 == i) {
                datas.add(data);
            }

            data.add(integer);
            temp = integer;
        }
        return datas;
    }

    /**
     * @author: chengzq
     * @date: 2019/7/11 0011 上午 11:55
     * @Description: 转换字符串
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    public static String getLine(List<List<Integer>> list) {
        String line = "";
        for (List<Integer> integers : list) {
            if (integers.size() > 0) {
                List<String> data = new ArrayList<>();
                Integer integer = integers.get(0);
                Integer integer1 = integers.get(integers.size() - 1);
                data.add(integer + "时");
                data.add(integer1 + "时");
                String collect = data.stream().distinct().collect(Collectors.joining("-"));
                line += collect + "、";
            }
        }
        return line;
    }

    /**
     * @author: lip
     * @date: 2019/9/29 0029 下午 6:55
     * @Description: 配置复合表头导出格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static List<Map<String, Object>> setManyHeaderExportData(List<Map<String, Object>> tableTitle) {
        int maxRowNum = getMaxRowNum(tableTitle);
        List<Map<String, Object>> children;
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Map<String, Object> title : tableTitle) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("headername", title.get("label"));
            dataMap.put("headercode", title.get("prop"));
            dataMap.put("rownum", maxRowNum);
            if (title.get("children") != null) {
                dataMap.put("rownum", 1);
                children = (List<Map<String, Object>>) title.get("children");
                List<Map<String, Object>> childrenList = new ArrayList<>();
                dataMap.put("columnnum", children.size());
                for (Map<String, Object> chlid : children) {
                    Map<String, Object> chlidMap = new HashMap<>();
                    chlidMap.put("headername", chlid.get("label"));
                    chlidMap.put("headercode", chlid.get("prop"));
                    chlidMap.put("rownum", 1);
                    chlidMap.put("columnnum", "1");
                    chlidMap.put("chlidheader", new ArrayList<>());
                    childrenList.add(chlidMap);
                }
                dataMap.put("chlidheader", childrenList);
            } else {
                dataMap.put("columnnum", "1");
                dataMap.put("chlidheader", new ArrayList<>());
            }
            dataList.add(dataMap);
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/9/29 0029 下午 7:01
     * @Description: 遍历表头获取子集最大数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static int getMaxRowNum(List<Map<String, Object>> tableTitle) {
        int num = 1;
        List<Map<String, Object>> children;
        for (Map<String, Object> title : tableTitle) {
            if (title.get("children") != null) {
                children = (List<Map<String, Object>>) title.get("children");
                if (children.size() > num) {
                    num = getMaxRowNum(children) + 1;
                }
            }
        }
        return num;
    }

    /**
     * @author: chengzq
     * @date: 2020/2/20 0020 上午 10:17
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [cst, pattern]
     * @throws:
     */
    public static String formatCSTString(String cst, String pattern) {
        try {
            //获取监测时间
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            Date d = sdf.parse(cst);
            String formatDate = new SimpleDateFormat(pattern).format(d);
            return formatDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatDate(String cst, String pattern) {
        try {
            //获取监测时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = sdf.parse(cst);
            String formatDate = new SimpleDateFormat(pattern).format(d);
            return formatDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据map的key排序
     *
     * @param map    待排序的map
     * @param isDesc 是否降序，true：降序，false：升序
     * @return 排序好的map
     * @author zero 2019/04/08
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

    public static void main(String[] args) throws IOException {
        File f = new File("e://out.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        PrintStream printStream = new PrintStream(fileOutputStream);
        System.setOut(printStream);
        System.out.println("默认输出到了文件 out.txt");


    }

    //字符串写出到文本
    public static void writeTxt(Object paramsjson) throws Exception {
        FileWriter fw = null;
        String rootpath = "/usr/tjpufile/logs/business_logs/";
        String fileName = DataFormatUtil.getDateYMD(new Date()) + "alarmdata.txt";
        File file = new File(rootpath + fileName);
        String content = paramsjson.toString()+",";
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file, true);
            BufferedWriter out = new BufferedWriter(fw);
            // FileOutputStream fos = new FileOutputStream(f);
            // OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/3/16 0016 下午 3:12
     * @Description: 在源日期上加amount秒
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [date, amount]
     * @throws:
     */
    public static Date getDateYMDHMSPlus(String date,int amount) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = sdf.parse(date);
            Calendar instance = Calendar.getInstance();
            instance.setTime(d);
            instance.add(Calendar.SECOND, amount);
            return instance.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    /**
     * 获取字符串拼音的第一个字母
     * @param chinese
     * @return
     */
    public static String ToFirstChar (String chinese){
        String pinyinStr = "";
        char[] newChar = chinese.toCharArray();  //转为单个字符
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    if (PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)!=null
                            &&PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0]!=null) {
                        pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0].charAt(0);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr;
    }

    /**
     * 汉字转为拼音
     * @param chinese
     * @return
     */
    public static String ToPinyin (String chinese){
        String pinyinStr = "";
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr;
    }
}

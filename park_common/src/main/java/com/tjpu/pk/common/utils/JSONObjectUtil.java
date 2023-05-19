package com.tjpu.pk.common.utils;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.DefaultValueProcessor;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: chengzq
 * @date: 2018/11/14 0014 16:20
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class JSONObjectUtil {

    /**
     * @author: chengzq
     * @date: 2018/11/14 0014 下午 4:21
     * @Description: 将JSONObject对象转换成实体对象，忽略属性大小写
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    public static <T>  T JsonObjectToEntity(JSONObject jsonObject,T obj) throws Exception {
        JsonConfig config = new JsonConfig();
        config.setRootClass(obj.getClass());
        config.setJavaPropertyFilter(new PropertyFilter() {
            @Override
            public boolean apply(Object o, String s, Object o1) {
                Class<?> aClass = o.getClass();
                Field[] declaredFields = aClass.getDeclaredFields();
                BeanWrapper bw = new BeanWrapperImpl(o);
                for (Field declaredField : declaredFields) {
                    String name = declaredField.getName();
                    if (StringUtils.equals(name.toLowerCase(),s.toLowerCase())){
                        try {
                            Type declaredFieldType = declaredField.getGenericType();
                            //实体属性类型名称
                            String typeName3 = declaredFieldType.getTypeName();
                            if(!typeName3.equals("java.util.Date") && !"null".equals(o1.toString())){
                                bw.setPropertyValue(name, o1);
                            }
                            if(typeName3.equals("java.util.Date") && o1!=null && !"null".equals(o1.toString()) && !"".equals(o1.toString())){
                                declaredField.setAccessible(true);
                                if(o1.toString().startsWith("{") && o1.toString().endsWith("}")){
                                    JSONObject jsonObject1 = JSONObject.fromObject(o1);
                                    declaredField.set(o,JSONObject.toBean(jsonObject1, Date.class));
                                }else{
                                    int length = o1.toString().length();
                                    String format = getFormat(length);
                                    SimpleDateFormat format1 = new SimpleDateFormat(format);
                                    Date date = format1.parse(o1.toString());
                                    declaredField.set(o,date);
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            }
        });
        return (T) JSONObject.toBean(jsonObject, config);
    }


    /**
     * @author: chengzq
     * @date: 2018/12/26 0026 上午 10:00
     * @Description: 解决JSONObject.fromObject数字为null时被转换为0
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    public static JsonConfig getRegisterDefaultJsonConfig(){
        JsonConfig config = new JsonConfig();
        config.registerDefaultValueProcessor(Double.class, new DefaultValueProcessor() {
            @Override
            public Object getDefaultValue(Class aClass) {
                return null;
            }
        });

        config.registerDefaultValueProcessor(Float.class, new DefaultValueProcessor() {
            @Override
            public Object getDefaultValue(Class aClass) {
                return null;
            }
        });

        config.registerDefaultValueProcessor(Integer.class, new DefaultValueProcessor() {
            @Override
            public Object getDefaultValue(Class aClass) {
                return null;
            }
        });
        config.registerDefaultValueProcessor(Short.class, new DefaultValueProcessor() {
            @Override
            public Object getDefaultValue(Class aClass) {
                return null;
            }
        });

        return config;
    }

    public static String getFormat(int len){
        if(len==4){
            return "yyyy";
        }else if(len==7){
            return "yyyy-MM";
        }else if(len==10){
            return "yyyy-MM-dd";
        }else if (len==13){
            return "yyyy-MM-dd HH";
        }else if(len==16){
            return "yyyy-MM-dd HH:mm";
        }else if(len==19){
            return "yyyy-MM-dd HH:mm:ss";
        }
        return "yyyy-MM-dd HH:mm:ss";
    }


    public static String[] getTimeBetween(String time){
        String [] s = new String[2];
        if(time.length()==4){
            s[0]=time+"-01-01 00:00:00";
            s[1]=DataFormatUtil.getLastDayOfMonth(time+"-12")+" 23:59:59";
        }else if(time.length()==7){
            s[0]=time+"-01 00:00:00";
            s[1]=DataFormatUtil.getLastDayOfMonth(time)+" 23:59:59";
        }else if(time.length()==10){
            s[0]=time+" 00:00:00";
            s[1]=time+" 23:59:59";
        }else{
            s[0]=time;
            s[1]=time;
        }
        return s;
    }

    public static String getStartTime(String starttime){
        if(starttime.length()==4){
            starttime=starttime+"-01-01 00:00:00";
        }else if(starttime.length()==7){
            starttime=starttime+"-01 00:00:00";
        }else if(starttime.length()==10){
            starttime=starttime+" 00:00:00";
        }else if(starttime.length()==13){
            starttime=starttime+":00:00";
        }else if(starttime.length()==16){
            starttime=starttime+":00";
        }
        return starttime;
    }

    public static String getEndTime(String endtime){

        if(endtime.length()==4){
            endtime=DataFormatUtil.getLastDayOfMonth(endtime+"-12")+" 23:59:59";
        }else if(endtime.length()==7){
            endtime=DataFormatUtil.getLastDayOfMonth(endtime)+" 23:59:59";
        }else if(endtime.length()==10){
            endtime=endtime+" 23:59:59";
        }else if(endtime.length()==13){
            endtime=endtime+":59:59";
        }else if(endtime.length()==16){
            endtime=endtime+":59";
        }
        return endtime;
    }


    /**
     * @author: chengzq
     * @date: 2021/4/2 0002 下午 2:48
     * @Description: 获取两个日期间所有日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    public static List<String> getAllDays(String starttime,String endtime){
        List<String> timelist=new LinkedList<>();
        timelist.add(starttime);
        if(starttime.equals(endtime)){
            return timelist;
        }
        Calendar instance = Calendar.getInstance();
        for(;;){
            Date dateYMD = DataFormatUtil.getDateYMD(starttime);
            instance.setTime(dateYMD);
            instance.add(Calendar.DAY_OF_MONTH,1);
            Date time = instance.getTime();
            String dateYMD1 = DataFormatUtil.getDateYMD(time);
            if (endtime.equals(dateYMD1)){
                timelist.add(dateYMD1);
                return timelist;
            }
            timelist.add(dateYMD1);
            starttime=dateYMD1;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/4/9 0009 上午 11:46
     * @Description: 获取截至现在当天小时时间点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [daytime]
     * @throws:
     */
    public static List<String> getHourTimePoints(String daytime,SimpleDateFormat format){
        String starttime=JSONObjectUtil.getStartTime(daytime);
        Date now = new Date();
        Calendar instance = Calendar.getInstance();
        List<String> timepoints=new ArrayList<>();
        if(daytime.substring(0,10).equals(DataFormatUtil.getDateYMD(now))){
            //当天
            instance.setTime(DataFormatUtil.getDateYMDHMS(starttime));
            timepoints.add(format.format(instance.getTime()));
            while (!DataFormatUtil.getDateYMDH(instance.getTime()).equals(DataFormatUtil.getDateYMDH(now)))
            {
                instance.add(Calendar.HOUR,1);
                timepoints.add(format.format(instance.getTime()));
            }
            timepoints.remove(timepoints.size()-1);
        }else{
            String endTime=JSONObjectUtil.getEndTime(daytime);
            instance.setTime(DataFormatUtil.getDateYMDHMS(starttime));
            timepoints.add(format.format(instance.getTime()));
            while (!DataFormatUtil.getDateYMDH(instance.getTime()).equals(endTime.substring(0,13)))
            {
                instance.add(Calendar.HOUR,1);
                timepoints.add(format.format(instance.getTime()));
            }
        }
        return timepoints;
    }


    /**
     * @author: chengzq
     * @date: 2021/4/9 0009 上午 11:46
     * @Description: 获取截至现在当天小时时间点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [daytime]
     * @throws:
     */
    public static List<String> get24HourTimePoints(String starttime,String endtime,SimpleDateFormat format){
        Calendar instance = Calendar.getInstance();
        List<String> timepoints=new LinkedList<>();
        instance.setTime(DataFormatUtil.getDateYMDHMS(starttime));
        timepoints.add(format.format(instance.getTime()));
        while(!DataFormatUtil.getDateYMDH(instance.getTime()).equals(endtime.substring(0,13))){

            instance.add(Calendar.HOUR,1);
            timepoints.add(format.format(instance.getTime()));

        }
        timepoints.remove(timepoints.size()-1);

        return timepoints;
    }

    /**
     *
     * @author: lip
     * @date: 2019/10/22 0022 下午 2:45
     * @Description: 将json字符串转换成实体
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    public static <T> T parseStringToJavaObject(String jsonString, Class<T> clazz) {
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(jsonString);
        return com.alibaba.fastjson.JSONObject.toJavaObject(jsonObject,clazz);
    }


    /**
     * @author: chengzq
     * @date: 2021/1/19 0019 下午 4:31
     * @Description: 获取所有日时间点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    public static List<String> getAllTimePoint(String starttime,String endTime){

        String format = getFormat(starttime.length());
        SimpleDateFormat format1 = new SimpleDateFormat(format);

        List<String> timepoints=new ArrayList<>();
        timepoints.add(starttime);

        int timetype=0;
        if(starttime.length()==13){
            timetype = Calendar.HOUR;
        }else if(starttime.length()==10){
            timetype = Calendar.DAY_OF_MONTH;
        }else if(starttime.length()==7){
            timetype = Calendar.MONTH;
        }

        long time = DataFormatUtil.getDateYMDHMS(getStartTime(endTime)).getTime();
        Calendar instance = Calendar.getInstance();
        instance.setTime(DataFormatUtil.getDateYMDHMS(getStartTime(starttime)));
        while (true){
            instance.add(timetype,1);
            Date time1 = instance.getTime();
            long time2 = time1.getTime();
            if(time<time2){
                return timepoints;
            }
            timepoints.add(format1.format(time1));
        }
    }

}
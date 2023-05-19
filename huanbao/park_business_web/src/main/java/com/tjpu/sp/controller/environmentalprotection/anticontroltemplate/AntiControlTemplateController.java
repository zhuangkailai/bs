package com.tjpu.sp.controller.environmentalprotection.anticontroltemplate;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.anticontrol.DefaultValueEnum;
import com.tjpu.sp.config.rabbitmq.RabbitMqConfig;
import com.tjpu.sp.model.environmentalprotection.anticontroltemplate.AntiControlTemplateConfigVO;
import com.tjpu.sp.service.environmentalprotection.anticontroltemplate.AntiControlTemplateService;
import com.tjpu.sp.service.impl.common.rabbitmq.RabbitSender;
import net.sf.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tjpu.sp.common.anticontrol.DefaultValueEnum.AntiControlCommonKeyEnum.*;

/**
 * @author: xsm
 * @date:2021/12/28 0028 16:36
 * @Description: 设备反控控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@RestController
@RequestMapping("antiControlTemplate")
public class AntiControlTemplateController {
    @Autowired
    private AntiControlTemplateService antiControlTemplateService;
    @Autowired
    private RabbitSender rabbitSender;

    /**
     * @author: xsm
     * @date: 2021/12/28 0028 16:37
     * @Description: 通过自定义参数获取设备反控信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     **/
    @RequestMapping(value = "/getAntiControlTemplateDataByParamMap", method = RequestMethod.POST)
    public Object getAntiControlTemplateDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsJson);
            Map<String, Object> resultMap = new HashMap<>();
            if (jsonObject.get("pagenum") != null && jsonObject.get("pagesize") != null) {
                PageHelper.startPage(Integer.valueOf(jsonObject.get("pagenum").toString()), Integer.valueOf(jsonObject.get("pagesize").toString()));
            }
            List cleanerInfoByParamMap = antiControlTemplateService.getAntiControlTemplateDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(cleanerInfoByParamMap);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", cleanerInfoByParamMap);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/28 0029 8:30
     * @Description: 通过自定义参数获取设备反控模板的配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     **/
    @RequestMapping(value = "/getAntiControlConfigDataByParamMap", method = RequestMethod.POST)
    public Object getAntiControlConfigDataByParamMap(@RequestJson(value = "templateid") String templateid,
                                                     @RequestJson(value = "mn", required = false) String mn,
                                                     @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype) throws Exception {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            AntiControlTemplateConfigVO antiControlTemplateConfigVO = antiControlTemplateService.selectByPrimaryKey(templateid);
            //根据模板ID获取所有配置字段
            List<Map<String, Object>> fields = antiControlTemplateService.getAntiControlFieldDataByTemplateid(templateid);
            if (fields != null && fields.size() > 0) {
                String template = antiControlTemplateConfigVO.getTemplateformat();
                List<String> parameters = new ArrayList<>();
                List<String> mr_parameters = new ArrayList<>();//系统默认
                String regex = "\\{(.*?)}";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(template);
                String value;
                String fieldkey;
                while (matcher.find()) {
                    value = matcher.group(0);
                    value = value.substring(1, value.length() - 1);//去掉大括号
                    for (Map<String, Object> map : fields) {
                        fieldkey = map.get("fieldname").toString();
                        if (value.equals(fieldkey) && map.get("fieldvaluesource") != null) {
                            if (!"1".equals(map.get("fieldvaluesource").toString())) {
                                parameters.add(value);
                            } else {
                                mr_parameters.add(value);
                            }
                        }
                    }
                }
                if (parameters.size() > 0) {
                    resultmap = getAntiControlConfigAddPageData(parameters, fields, mn, monitorpointtype);
                } else {//无需要手动输入及勾选的参数  则直接发送
                    sendAntiControlDirectCommand(antiControlTemplateConfigVO, fields, mn, monitorpointtype);
                    return AuthUtil.parseJsonKeyToLower("success", null);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 直接发送
     * */
    private void sendAntiControlDirectCommand(AntiControlTemplateConfigVO antiControlTemplateConfigVO, List<Map<String, Object>> fields, String mn, Integer monitorpointtype) {
        try {
            JSONObject onejson = new JSONObject();
            String value;
            String template = antiControlTemplateConfigVO.getTemplateformat();
            List<String> list = Arrays.asList(PWEnum.getCode(),STEnum.getCode(),MNEnum.getCode());
            //获取CN 和 Flag 的值
            String cn = "";
            String flag = "";
            String cp_str = "";
            if (template!=null && template.length()>0){
                //将公共参数 和指令参数分隔开
                String[] commonstr = template.split(";CP=");
                //获取模板中CN 、Flag默认值
                String[] strs = commonstr[0].split(";");
                String cn_substr;
                String flag_substr;
                for (String str:strs){
                    cn_substr = str.substring(0,3);
                    flag_substr = str.substring(0,5);
                    if ("CN=".equals(cn_substr)){
                        cn = str.substring(3,str.length());
                    }
                    if ("Flag=".equals(flag_substr)){
                        flag = str.substring(5,str.length());
                    }
                }
                //获取指令参数的 所有参数  且拼接
                //去掉首位的 && 符号
                cp_str = commonstr[1].replace("&&","");
                if (!"".equals(cp_str)){
                    String regex = "\\{(.*?)}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(cp_str);
                    String fieldkey;
                    while (matcher.find()) {
                        fieldkey = matcher.group(0);
                        fieldkey = fieldkey.substring(1, fieldkey.length() - 1);//去掉大括号
                        for (Map<String, Object> map : fields) {
                            if (fieldkey.equals(map.get("fieldname").toString())){
                                value = getDefaultValue(map, mn, monitorpointtype);
                                cp_str = cp_str.replace("{" + fieldkey + "}", value + "");
                                break;
                            }
                        }
                    }
                }
            }
            onejson.put("cn", cn);
            onejson.put("flag", flag);
            for (String key :list){
                if (key.equals(MNEnum.getCode())) {
                    onejson.put("mn", mn);
                } else {
                    for (Map<String, Object> map : fields) {
                        if (key.equals(map.get("fieldname")+"") && map.get("defaultvalue") != null) {
                            value = getDefaultValue(map, mn, monitorpointtype);
                            onejson.put(map.get("fieldname").toString(), value);
                            break;
                        }
                    }
                }
            }
           /* for (String fieldkey : mr_parameters) {
                template = template.replace("{" + fieldkey + "}", filed_valuemap.get(fieldkey) + "");
            }*/
            //放到消息队列中
            onejson.put("cp",cp_str);
            onejson.put("command", antiControlTemplateConfigVO.getAntcontrolcommand());
            sendAntiControlMessageToRabbit(onejson);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 组装反控命令参数设置页面
     */
    private Map<String, Object> getAntiControlConfigAddPageData(List<String> parameters, List<Map<String, Object>> fields, String mn, Integer monitorpointtype) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> listdata = new ArrayList<>();
            List<Map<String, Object>> dualcontrolskey = new ArrayList<>();
            Map<String, Object> addformdata = new HashMap<>();
            String fieldkey;
            String fieldvaluesource;
            boolean istimesflag = false;//是否存在  开始 结束时间标记
            for (String str : parameters) {
                for (Map<String, Object> onemap : fields) {
                    fieldkey = onemap.get("fieldname").toString();
                    fieldvaluesource = onemap.get("fieldvaluesource").toString();
                    if (str.equals(fieldkey)) {
                        if ("2".equals(fieldvaluesource)) {//手动输入
                            Map<String, Object> map = new HashMap<>();
                            //key 相同 且不是系统默认值
                            //组装表单
                            map.put("name", str);
                            map.put("remarks", onemap.get("remarks")+"：");
                            map.put("label", onemap.get("fieldcomments")+"：");
                            map.put("showhide", true);
                            map.put("width", "100%");
                            //不为空验证
                            map.put("validtriggers", "blur");
                            map.put("validmessage", onemap.get("fieldcomments") + "不能为空!");
                            map.put("validrules", "isNonEmpty");
                            if (onemap.get("fielddatatype") != null) {
                                if ("datetime".equals(onemap.get("fielddatatype").toString())) {
                                    map.put("type", "DatePicker");
                                    map.put("datetype", "datetime");
                                    map.put("editable", false);
                                    map.put("placeholder", "请选选择" + onemap.get("fieldcomments") + "!");
                                    if (onemap.get("controlvalueformat") != null) {
                                        if ("begintime".equals(str) || "endtime".equals(str)) {
                                            //请求时间
                                            if ((onemap.get("controlvalueformat").toString()).equals(DefaultValueEnum.TimeFormatEnum.SecondTimeEnum.getValue())){
                                                map.put("datetype", DefaultValueEnum.TimeFormatEnum.SecondTimeEnum.getDatetype());
                                                map.put("format", DefaultValueEnum.TimeFormatEnum.SecondTimeEnum.getFormatstr());
                                                map.put("popperclass","sbfk_second");
                                            }else if ((onemap.get("controlvalueformat").toString()).equals(DefaultValueEnum.TimeFormatEnum.MinuteTimeEnum.getValue())){
                                                map.put("datetype", DefaultValueEnum.TimeFormatEnum.MinuteTimeEnum.getDatetype());
                                                map.put("format", DefaultValueEnum.TimeFormatEnum.MinuteTimeEnum.getFormatstr());
                                                map.put("popperclass","sbfk_minute");
                                            }else if((onemap.get("controlvalueformat").toString()).equals(DefaultValueEnum.TimeFormatEnum.HourTimeEnum.getValue())){
                                                map.put("datetype", DefaultValueEnum.TimeFormatEnum.HourTimeEnum.getDatetype());
                                                map.put("format", DefaultValueEnum.TimeFormatEnum.HourTimeEnum.getFormatstr());
                                                map.put("popperclass", "sbfk_hour");
                                            }else if((onemap.get("controlvalueformat").toString()).equals(DefaultValueEnum.TimeFormatEnum.DayTimeEnum.getValue())){
                                                map.put("datetype", DefaultValueEnum.TimeFormatEnum.DayTimeEnum.getDatetype());
                                                map.put("format", DefaultValueEnum.TimeFormatEnum.DayTimeEnum.getFormatstr());
                                                map.put("popperclass", "sbfk_day");
                                            }else{
                                                map.put("format", "yyyy-MM-dd HH:mm:ss");
                                            }
                                            map.remove("placeholder");
                                            map.put("rangeseparator", "至");
                                            map.put("startplaceholder", "开始日期");
                                            map.put("endplaceholder", "结束日期");
                                        }else if("cstarttime".equals(str)){
                                            map.put("type", "TimePicker");
                                            map.put("datetype", "datetime");
                                            map.put("placeholder", "请选选择" + onemap.get("fieldcomments") + "!");
                                        }else {
                                            map.put("format", onemap.get("controlvalueformat").toString());
                                        }
                                        map.put("valueformat", onemap.get("controlvalueformat").toString());
                                    }
                                } else {//其它都按字符串 文本框处理
                                    map.put("placeholder", "请输入" + onemap.get("fieldcomments") + "!");
                                    map.put("type", "text");
                                }
                            }
                            //最大最小值
                            if (onemap.get("minlimit") != null || onemap.get("maxlimit") != null) {
                                List<String> values = new ArrayList<>();
                                if (onemap.get("minlimit") != null && !"".equals(onemap.get("minlimit").toString())) {
                                    if ("numeric".equals(onemap.get("fielddatatype").toString())) {
                                        values.add(onemap.get("minlimit") != null ? onemap.get("minlimit").toString() : null);
                                    } else if ("float".equals(onemap.get("fielddatatype").toString())) {
                                        values.add(onemap.get("minlimit") != null ? onemap.get("minlimit").toString() : null);
                                    } else {
                                        map.put("minlength", Integer.valueOf(onemap.get("minlimit").toString()));
                                    }
                                }
                                if (onemap.get("maxlimit") != null && !"".equals(onemap.get("maxlimit").toString())) {
                                    if ("numeric".equals(onemap.get("fielddatatype").toString())) {
                                        values.add(onemap.get("maxlimit") != null ? onemap.get("maxlimit").toString() : null);
                                    } else if ("float".equals(onemap.get("fielddatatype").toString())) {
                                        values.add(onemap.get("maxlimit") != null ? onemap.get("maxlimit").toString() : null);
                                    } else {
                                        map.put("maxlength", Integer.valueOf(onemap.get("maxlimit").toString()));
                                    }
                                }
                                map.put("validtriggers", "blur");
                                map.put("validmessage", onemap.get("fieldcomments") + "不能为空!");
                                map.put("validrules", "isNonEmpty");
                                if (map.get("validtriggers") != null) {
                                    map.put("validtriggers", map.get("validtriggers") + "$+$blur");
                                }
                                if (map.get("validmessage") != null) {
                                    if ("numeric".equals(onemap.get("fielddatatype").toString())||"float".equals(onemap.get("fielddatatype").toString())) {
                                        if (onemap.get("maxlimit") != null && !"".equals(onemap.get("maxlimit").toString())) {
                                            map.put("validmessage", map.get("validmessage") + "$+$请输入范围在" + onemap.get("minlimit") + "至" + onemap.get("maxlimit") + "之间的数!");
                                        } else {
                                            map.put("validmessage", map.get("validmessage") + "$+$" + onemap.get("fieldcomments") + "输入不正确!");
                                        }
                                    }else{
                                        map.put("validmessage", map.get("validmessage") + "$+$" + onemap.get("fieldcomments") + "输入不正确!");
                                    }
                                }
                                if (map.get("validrules") != null) {
                                    values.add("number");
                                    if ("numeric".equals(onemap.get("fielddatatype").toString())) {
                                        map.put("validrules", map.get("validrules") + "$+$" + values.toString()+ "$+$ifDecimalNegative" );
                                    } else if ("float".equals(onemap.get("fielddatatype").toString())) {
                                        map.put("validrules", map.get("validrules") + "$+$" + values.toString()+ "$+$ifHaveValueMustNum");
                                    }
                                }
                            }
                            if ("begintime".equals(str) || "endtime".equals(str)) {
                                if (istimesflag == false) {
                                    map.put("name", "monitortimes");
                                    map.put("label", "请求时间"+"：");
                                    map.put("validmessage",  "请求时间不能为空!");
                                    listdata.add(map);
                                    addformdata.put("monitortimes", "");
                                    istimesflag = true;
                                }
                            }else if("cstarttime".equals(str)) {
                                addformdata.put(str, onemap.get("defaultvalue"));
                                listdata.add(map);
                            }else {
                                addformdata.put(str, "");
                                listdata.add(map);
                            }

                        } else if ("3".equals(fieldvaluesource)) {//根据系统范围值 勾选
                            addformdata.put(str, "");
                            Map<String, Object> map2 = new HashMap<>();
                            //组装表单
                            map2.put("name", str);
                            map2.put("label", onemap.get("fieldcomments")+"：");
                            List<Map<String, Object>> childs = new ArrayList<>();
                            if (onemap.get("defaultvalue") != null) {
                                String methodName = onemap.get("defaultvalue").toString();
                                if (methodName.equals(DefaultValueEnum.SelectValueMethod.GetMinInterval.getValue())) {
                                    //获取分钟数据间隔
                                    childs = getMinInterval();
                                } else if (methodName.equals(DefaultValueEnum.SelectValueMethod.GetPointPollutantData.getValue())) {
                                    //获取点位污染物
                                    childs = getPointPollutantData(mn, monitorpointtype);
                                }else if(methodName.equals(DefaultValueEnum.SelectValueMethod.GetInfoids.getValue())) {
                                    //获取现场端信息编码
                                    childs = getInfoids();
                                }

                            }
                            map2.put("type", "radio");
                            map2.put("showhide", true);
                            map2.put("width", "100%");
                            map2.put("radiochildren", childs);
                            map2.put("validtriggers", "blur");
                            map2.put("validmessage", onemap.get("fieldcomments") + "不能为空!");
                            map2.put("validrules", "isNonEmpty");
                            listdata.add(map2);
                        }
                    }
                }
            }
            result.put("addcontroldata", listdata);
            result.put("dualcontrolskey", dualcontrolskey);
            result.put("addformdata", addformdata);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/28 0029 8:30
     * @Description: 通过自定义参数获取设备反控模板的配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     **/
    @RequestMapping(value = "/sendAntiControlConfigDataByParamMap", method = RequestMethod.POST)
    public Object sendAntiControlConfigDataByParamMap(@RequestJson(value = "addformdata") Object addformdata,
                                                      @RequestJson(value = "templateid") String templateid,
                                                      @RequestJson(value = "mn") String mn,
                                                      @RequestJson(value = "monitorpointtype") Integer monitorpointtype) throws Exception {
        try {
            JSONObject onejson = new JSONObject();
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            Map<String, Object> resultmap = new HashMap<>();
            AntiControlTemplateConfigVO antiControlTemplateConfigVO = antiControlTemplateService.selectByPrimaryKey(templateid);
            String template = antiControlTemplateConfigVO.getTemplateformat();
            //根据模板ID 和字段来源 获取所有系统默认字段配置字段
            Map<String, Object> param = new HashMap<>();
            param.put("templateid", templateid);
            param.put("fieldvaluesource", 1);//查出该模板 所有系统默认字段
            List<Map<String, Object>> fields = antiControlTemplateService.getAntiControlFieldDataByParam(param);
            List<String> list = Arrays.asList(PWEnum.getCode(),STEnum.getCode(),MNEnum.getCode());
            //获取CN 和 Flag 的值
            String cn = "";
            String flag = "";
            String cp_str = "";
            String value;
            if (template!=null && template.length()>0){
                if (jsonObject.get("monitortimes") != null) {
                    String monitortimes = jsonObject.getString("monitortimes");
                    String[] times = monitortimes.split(",");
                    jsonObject.put("begintime", times[0]);
                    jsonObject.put("endtime", times[1]);
                }
                //将公共参数 和指令参数分隔开
                String[] commonstr = template.split(";CP=");
                //获取模板中CN 、Flag默认值
                String[] strs = commonstr[0].split(";");
                String cn_substr;
                String flag_substr;
                for (String str:strs){
                    cn_substr = str.substring(0,3);
                    flag_substr = str.substring(0,5);
                    if ("CN=".equals(cn_substr)){
                        cn = str.substring(3,str.length());
                    }
                    if ("Flag=".equals(flag_substr)){
                        flag = str.substring(5,str.length());
                    }
                }
                //获取指令参数的 所有参数  且拼接
                //去掉首位的 && 符号
                cp_str = commonstr[1].replace("&&","");
                if (!"".equals(cp_str)){
                    String regex = "\\{(.*?)}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(cp_str);
                    String fieldkey;
                    while (matcher.find()) {
                        fieldkey = matcher.group(0);
                        fieldkey = fieldkey.substring(1, fieldkey.length() - 1);//去掉大括号
                        //将 CP中的参数赋值
                        for (Map<String, Object> map : fields) {
                            //取默认值
                            if (fieldkey.equals(map.get("fieldname").toString())){
                                value = getDefaultValue(map, mn, monitorpointtype);
                                cp_str = cp_str.replace("{" + fieldkey + "}", value + "");
                                break;
                            }
                        }
                        //取页面传参值
                        cp_str = cp_str.replace("{" + fieldkey + "}", jsonObject.get(fieldkey) + "");
                    }
                }
            }
            onejson.put("cn", cn);
            onejson.put("flag", flag);
            onejson.put("cp",cp_str);
            for (String key :list){
                if (key.equals(MNEnum.getCode())) {
                    onejson.put("mn", mn);
                } else {
                    for (Map<String, Object> map : fields) {
                        if (key.equals(map.get("fieldname")+"") && map.get("defaultvalue") != null) {
                            value = getDefaultValue(map, mn, monitorpointtype);
                            onejson.put(map.get("fieldname").toString(), value);
                            break;
                        }
                    }
                }
            }
            onejson.put("command", antiControlTemplateConfigVO.getAntcontrolcommand());
            sendAntiControlMessageToRabbit(onejson);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/04 0004 下午 6:31
     * @Description: 发送消息到反控命令队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendAntiControlMessageToRabbit(JSONObject jsonObject) {
        MessageProperties properties = new MessageProperties();
        Message message = null;
        try {
            message = new Message(jsonObject.toString().getBytes("UTF-8"), properties);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        rabbitSender.sendMessage(RabbitMqConfig.ANTI_CONTROL_DIRECT_EXCHANGE, RabbitMqConfig.ANTI_CONTROL_DIRECT_KEY, message);
    }

    /**
     * 获取默认值
     */
    private String getDefaultValue(Map<String, Object> map, String mn, Integer monitorpointtype) {
        try {
            String value = "";
            String methodName = map.get("defaultvalue")!=null?map.get("defaultvalue").toString():"";
            String controlvalueformat = map.get("controlvalueformat") != null ? map.get("controlvalueformat").toString() : "yyyyMMddHHmmsszzz";
            if (methodName.equals(DefaultValueEnum.DefaultValueMethod.GetNewSystemTime.getValue())) {
                //获取当前系统时间
                SimpleDateFormat df = new SimpleDateFormat(controlvalueformat);
                value = df.format(new Date());
            } else if (methodName.equals(DefaultValueEnum.DefaultValueMethod.GetSystemCoding.getValue())) {
                //获取系统编码
                value = getGetSystemCoding(monitorpointtype);
            } else if (methodName.equals(DefaultValueEnum.DefaultValueMethod.GetPointAccessPWD.getValue())) {
                value = getPointAccessPWD(mn, monitorpointtype);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取分钟数据间隔
     */
    private List<Map<String, Object>> getMinInterval() {
        List<Map<String, Object>> minInterval = new ArrayList<>();
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30);
        for (Integer i : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("labelname", i);
            map.put("value", i);
            minInterval.add(map);
        }
        return minInterval;
    }

    /**
     * 获取系统编码
     */
    private String getGetSystemCoding(Integer monitorpointtype) {
        return DefaultValueEnum.AntiControlSystemCodeEnum.getCodeByType(monitorpointtype);
    }

    /**
     * 获取点位污染物
     */
    private List<Map<String, Object>> getPointPollutantData(String mn, Integer monitorpointtype) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> param = new HashMap<>();
            param.put("mn", mn);
            param.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> pollutants = antiControlTemplateService.getPointPollutantDataByParamMap(param);
            for (Map<String, Object> pollutant : pollutants) {
                if (pollutant.get("Code") != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("labelname", pollutant.get("pollutantname"));
                    map.put("value", pollutant.get("Code"));
                    result.add(map);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取某个设备的访问密码
     */
    private String getPointAccessPWD(String mn, Integer monitorpointtype) {
        try {
            String pwd = "123456";
            Map<String, Object> param = new HashMap<>();
            param.put("mn", mn);
            param.put("monitorpointtype", monitorpointtype);
            Map<String, Object> pointmap = antiControlTemplateService.getOnePointAccessPasswordByParamMap(param);
            if (pointmap != null && pointmap.get("accesspassword") != null && !"".equals(pointmap.get("accesspassword").toString())) {
                pwd = pointmap.get("accesspassword").toString();
            }
            return pwd;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //获取现场端信息编码
    private List<Map<String,Object>> getInfoids() {
        List<Map<String, Object>> infoids = antiControlTemplateService.getInformationEncoding();
        return infoids;
    }
}

package com.tjpu.auth.service.common;

import com.alibaba.fastjson.JSONObject;
import com.tjpu.auth.common.utils.RedisTemplateUtil;
import com.tjpu.auth.dao.GeneralMethod.FunctionMapper;
import com.tjpu.auth.model.codeTable.CommonSelectFieldConfigVO;

import com.tjpu.pk.common.utils.AESUtil;
import com.tjpu.pk.common.utils.ConstantsUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: zzc
 * @Date: 2018/6/25 10:34
 * @Description:service公共方法支持
 */
@Component
public class CommonServiceSupport {

    @Autowired
    private FunctionMapper functionMapper;
    private static FunctionMapper staticFunctionMapper;

    @PostConstruct
    public void init() {
        staticFunctionMapper = functionMapper;
    }


    private static String defaultDataSource;
    //private static String datacenterDataSource;

    /**
     * 默认数据源
     */
    @Value("${spring.datasource.primary.name}")
    public void setDefaultDataSource(String defaultDataSource) {
        CommonServiceSupport.defaultDataSource = defaultDataSource;
    }

    /**
     * 数据中心数据源
     */
    /*@Value("${spring.datasource.slave.datacenter.name}")
    public void setDatacenterDataSource(String datacenterDataSource) {
        CommonServiceSupport.datacenterDataSource = datacenterDataSource;
    }*/

    /**
     * @author: zzc
     * @date: 2018/6/13 16:24
     * @Description: 查询时排序字段处理 拼装排序字段
     * @updateUser:
     * @updateDate:
     * @updateDescription 处理页面操作排序字段
     * @return:
     * @param: paramMap             包含页面传来的排序条件字段，
     * @param: orderFields          配置的页面排序字段信息实体集合，
     * @param: defaultOrderByFields 配置的默认排序字段实体集合，
     * @param: tableName            表名称
     */
    public static String doPageHelpOrderByField(Map<String, Object> paramMap, List<CommonSelectFieldConfigVO> orderFields, List<CommonSelectFieldConfigVO> defaultOrderByFields, String tableName) {
        StringBuffer orderSql = new StringBuffer();
        //1、先处理默认排序字段
        Map<String, String> map = new LinkedHashMap<>();
        if (!defaultOrderByFields.isEmpty()) {
            for (CommonSelectFieldConfigVO field : defaultOrderByFields) {
                String defaultOrderType = field.getDefaultOrderType();  //排序规则 asc 或者desc
                String fieldName = tableName + "." + field.getFieldName();  //字段名称
                if (StringUtils.isNotBlank(field.getRelationalTable())) {    //关联表排序
                    tableName = field.getRelationalTable();
                    fieldName = tableName + "." + field.getFkNameField();
                }
                map.put(fieldName, defaultOrderType);
            }
        }
        //2、页面传来的排序条件处理
        if (paramMap.get("sortdata") != null && StringUtils.isNotBlank(paramMap.get("sortdata").toString())) {
            final Map<String, Object> sortData = JSONObject.parseObject(paramMap.get("sortdata").toString());
            for (CommonSelectFieldConfigVO field : orderFields) {
                String orderByFieldName = field.getFieldName().toLowerCase();
                String fieldName = tableName + "." + field.getFieldName().toLowerCase();
                if (sortData.get(orderByFieldName) != null) {
                    map.clear();
                    final String orderValue = sortData.get(orderByFieldName).toString();   //排序规则
                    if (StringUtils.isNotBlank(field.getRelationalTable())) {    //关联表排序
                        tableName = field.getRelationalTable();
                        fieldName = tableName + "." + field.getFkNameField().toLowerCase();
                    }
                    if (StringUtils.isNotBlank(orderValue)) {
                        String orderType;
                        switch (orderValue) {
                            case "ascending":
                                orderType = " asc";
                                break;
                            case "descending":
                                orderType = " desc";
                                break;
                            default:
                                orderType = "";
                                break;
                        }
                        map.put(fieldName, orderType);
                    }
                }
            }
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            orderSql.append(entry.getKey()).append(" ").append(entry.getValue()).append(",");
        }
        if (orderSql.length() > 0) {
            orderSql = orderSql.deleteCharAt(orderSql.lastIndexOf(","));
        }
        return orderSql.toString();
    }


    /**
     * @author: zzc
     * @date: 2018/6/21 14:40
     * @Description: 列表表头信息(包含排序字段)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:listFields 列表字段  orderByFields排序字段
     * @return:
     */
    public static List<Map<String, Object>> getTableTitle(List<CommonSelectFieldConfigVO> listFields) {
        List<Map<String, Object>> tableTitleData = new ArrayList<>(); // tableTitle
        if (listFields.size() > 0) {
            for (CommonSelectFieldConfigVO fieldVO : listFields) {
                Map<String, Object> tableTitleMap = new HashMap<>();
                tableTitleMap.put("dataname", (StringUtils.isNotBlank(fieldVO.getOtherFieldName()) ? fieldVO.getOtherFieldName() : fieldVO.getFieldName()).toLowerCase());
                tableTitleMap.put("titlename", fieldVO.getFieldComments()); // title显示名称
                tableTitleMap.put("width", fieldVO.getControlWidth()); // 宽度
                tableTitleMap.put("height", fieldVO.getControlHeight()); // 高度
                tableTitleMap.put("fixed", fieldVO.getListFixed()); // 列表滚动条固定列
                //字段是否展示 null和1的时候页面上展示 0的时候false页面上不展示
                tableTitleMap.put("showhide", fieldVO.getShowPage() == null || fieldVO.getShowPage() == 1);
                if (StringUtils.isNotBlank(fieldVO.getControlTextAlign())) {
                    tableTitleMap.put("headeralign", fieldVO.getControlTextAlign()); // 文本内容样式
                } else {
                    tableTitleMap.put("headeralign", "center"); // 文本内容样式
                }
                //用户排序
                if (fieldVO.getCustomOrderBy() != null && fieldVO.getCustomOrderBy() == 1) {
                    tableTitleMap.put("sortable", "custom");
                } else {
                    tableTitleMap.put("sortable", false);
                }
                tableTitleData.add(tableTitleMap);
            }
        }
        return tableTitleData;
    }


    /**
     * @author: zzc
     * @date: 2018/6/25 17:15
     * @Description: 默认字段函数获取的值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getDefaultFieldMethodValue(String methodName) {
        if (methodName.equals(ConstantsUtil.DefaultMethod.GETCURRENTLYDATA.getValue())) {   //当前时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return df.format(new Date());
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.GETUUID.getValue())) {   //uuid
            return UUID.randomUUID().toString();
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.GETLOGINUSERID.getValue())) {    //登录用户id
            return RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.GETLOGINACCOUNT.getValue())) {   //登录账户名
            return RedisTemplateUtil.getRedisCacheDataByToken("useraccount", String.class);
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.GETINITIALPASSWORD.getValue())) {  //初始密码
            return AESUtil.Encrypt(ConstantsUtil.DEFAULT_USER_PWD, AESUtil.KEY_Secret);
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.getLoginUserName.getValue())) {  //获取当前登录用户姓名
            return RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
        }
        return null;
    }


    /**
     * @author: xsm
     * @date: 2018/7/17 14:23
     * @Description: 获得一个UUID
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static String getUUID() {
        //生成uuid
        String uuid = UUID.randomUUID().toString();

        return uuid;
    }


    /**
     * @Author: zhangzc
     * @Date: 2018/12/19 16:55
     * @Description: 添加是主表默认添加字段存储过程的调用
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    public static void doStoredProcedureFieldsForAddMethod(List<CommonSelectFieldConfigVO> mainDefaultFields, List<String> fieldList, List<Object> values, Map<String, Object> formData) {
        for (CommonSelectFieldConfigVO field : mainDefaultFields) {
            final String value = getDefaultFieldMethodValueByAddMethod(field.getControlDefault(), formData);
            if (StringUtils.isNotBlank(value)) {
                fieldList.add(field.getFieldName());
                values.add(value);
            }
        }
    }

    /**
     * @Author: zhangzc
     * @Date: 2018/12/19 17:00
     * @Description: 根据存储过程获取值
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    private static String getDefaultFieldMethodValueByAddMethod(String methodName, Map map) {
        if (methodName.equals(ConstantsUtil.DefaultMethod.getWaterOutPutCode.getValue())) {  //调用存储过程获取废水排口编号
            int i = methodName.indexOf("(");
            int i1 = methodName.indexOf(")");
            String substring = methodName.substring(i + 1, i1);
            String s = map.get(substring).toString();
            String waterOutPutCode = getWaterOutPutCode(s);
            return waterOutPutCode;
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.getNoiseCode.getValue())) {  //调用存储过程获取噪声监测点编号
            int i = methodName.indexOf("(");
            int i1 = methodName.indexOf(")");
            String substring = methodName.substring(i + 1, i1);
            String s = map.get(substring).toString();
            String noiseCode = getNoiseCode(s);
            return noiseCode;
        }
        return "";
    }

    /**
     * @author: lip
     * @date: 2018/11/1 0001 上午 11:38
     * @Description: 根据污染源ID获取废水排口编号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private static String getNoiseCode(String pollutionid) {
        try {
            String outputcode;
            outputcode = "";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutioId", pollutionid);
            //System.out.println(datacenterDataSource);
            //DynamicDataSourceContextHolderUtil.setDataSourceType(datacenterDataSource);
            staticFunctionMapper.getNoiseCode(paramMap);
            //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            if (paramMap.get("monitorpointcode") != null) {
                outputcode = paramMap.get("monitorpointcode").toString();
            }
            return outputcode;
        } catch (Exception e) {
            //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Author: zhangzc
     * @Date: 2018/12/19 16:12
     * @Description: 根据污染源ID获取噪声监测点编号
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    private static String getWaterOutPutCode(String pollutionid) {
        try {
            String outputcode;
            outputcode = "";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutioId", pollutionid);
            //System.out.println(datacenterDataSource);
            //DynamicDataSourceContextHolderUtil.setDataSourceType(datacenterDataSource);
            staticFunctionMapper.getWaterOutputCode(paramMap);
            //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            if (paramMap.get("WaterOutputCode") != null) {
                outputcode = paramMap.get("WaterOutputCode").toString();
            }
            return outputcode;
        } catch (Exception e) {
            //DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }
}

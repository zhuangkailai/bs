package com.pub.impl;

import com.alibaba.fastjson.JSONObject;
import com.pub.common.utils.RedisTemplateUtil;
import com.tjpu.pk.common.datasource.DynamicDataSourceContextHolderUtil;
import com.tjpu.pk.common.utils.AESUtil;
import com.tjpu.pk.common.utils.ConstantsUtil;
import com.pub.dao.FunctionMapper;
import com.pub.model.CommonSelectFieldConfigVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;


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
    private static String slaveDataSource;


    @Value("${spring.datasource.primary.name}")
    public void setDefaultDataSource(String defaultDataSource) {
        CommonServiceSupport.defaultDataSource = defaultDataSource;
    }


    @Value("${spring.datasource.slave.smartpark.name}")
    public void setSlaveDataSource(String slaveDataSource) {
        CommonServiceSupport.slaveDataSource = slaveDataSource;
    }


    public static String doPageHelpOrderByField(Map<String, Object> paramMap, List<CommonSelectFieldConfigVO> orderFields, List<CommonSelectFieldConfigVO> defaultOrderByFields, String tableName) {
        StringBuffer orderSql = new StringBuffer();

        Map<String, String> map = new LinkedHashMap<>();
        if (!defaultOrderByFields.isEmpty()) {
            for (CommonSelectFieldConfigVO field : defaultOrderByFields) {
                String defaultOrderType = field.getDefaultOrderType();
                String fieldName = tableName + "." + field.getFieldName();
                if (StringUtils.isNotBlank(field.getRelationalTable())) {
                    fieldName = field.getRelationalTable() + "." + field.getFkNameField();
                }
                if (StringUtils.isNotBlank(field.getQueryFieldSql())){
                    fieldName = field.getQueryFieldSql();
                }
                map.put(fieldName, defaultOrderType);
            }
        }

        if (paramMap.get("sortdata") != null && StringUtils.isNotBlank(paramMap.get("sortdata").toString())) {
            final Map<String, Object> sortData = JSONObject.parseObject(paramMap.get("sortdata").toString());
            for (CommonSelectFieldConfigVO field : orderFields) {
                String orderByFieldName = field.getFieldName().toLowerCase();
                String fieldName = tableName + "." + field.getFieldName().toLowerCase();
                if (sortData.get(orderByFieldName) != null) {
                    map.clear();
                    final String orderValue = sortData.get(orderByFieldName).toString();
                    if (StringUtils.isNotBlank(field.getRelationalTable())) {
                        fieldName = field.getRelationalTable() + "." + field.getFkNameField().toLowerCase();
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



    public static List<Map<String, Object>> getTableTitle(List<CommonSelectFieldConfigVO> listFields) {
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        if (listFields.size() > 0) {
            for (CommonSelectFieldConfigVO fieldVO : listFields) {
                Map<String, Object> tableTitleMap = new HashMap<>();
                if (StringUtils.isNotBlank(fieldVO.getControlType()) && fieldVO.getControlType().equals(ConstantsUtil.ControlTypeEnum.image.getValue())){
                    tableTitleMap.put("type",fieldVO.getControlType());
                }
                tableTitleMap.put("prop", (StringUtils.isNotBlank(fieldVO.getOtherFieldName()) ? fieldVO.getOtherFieldName() : fieldVO.getFieldName()).toLowerCase());
                tableTitleMap.put("label", fieldVO.getFieldComments());
                tableTitleMap.put("width", fieldVO.getControlWidth());
                tableTitleMap.put("height", fieldVO.getControlHeight());
                tableTitleMap.put("fixed", fieldVO.getListFixed());

                tableTitleMap.put("showhide", fieldVO.getShowPage() == null || fieldVO.getShowPage() == 1);
                if (StringUtils.isNotBlank(fieldVO.getControlTextAlign())) {
                    tableTitleMap.put("headeralign", fieldVO.getControlTextAlign());
                    tableTitleMap.put("align", fieldVO.getControlTextAlign());
                } else {
                    tableTitleMap.put("headeralign", "center");
                    tableTitleMap.put("align", "center");
                }

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



    public static String getDefaultFieldMethodValue(String methodName) {
        if (methodName.equals(ConstantsUtil.DefaultMethod.GETCURRENTLYDATA.getValue())) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return df.format(new Date());
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.GETUUID.getValue())) {
            return UUID.randomUUID().toString();
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.GETLOGINUSERID.getValue())) {
            return RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.GETLOGINACCOUNT.getValue())) {
            return RedisTemplateUtil.getRedisCacheDataByToken("useraccount", String.class);
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.GETINITIALPASSWORD.getValue())) {
            return AESUtil.Encrypt(ConstantsUtil.DEFAULT_USER_PWD, AESUtil.KEY_Secret);
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.getLoginUserName.getValue())) {
            return RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
        }
        return null;
    }



    public static String getUUID() {

        String uuid = UUID.randomUUID().toString();

        return uuid;
    }



    public static void doStoredProcedureFieldsForAddMethod(List<CommonSelectFieldConfigVO> mainDefaultFields, List<String> fieldList, List<Object> values, Map<String, Object> formData) {
        for (CommonSelectFieldConfigVO field : mainDefaultFields) {
            final String value = getDefaultFieldMethodValueByAddMethod(field.getControlDefault(), formData);
            if (StringUtils.isNotBlank(value)) {
                fieldList.add(field.getFieldName());
                values.add(value);
            }
        }
    }


    private static String getDefaultFieldMethodValueByAddMethod(String methodName, Map map) {
        if (methodName.equals(ConstantsUtil.DefaultMethod.getWaterOutPutCode.getValue())) {
            int i = methodName.indexOf("(");
            int i1 = methodName.indexOf(")");
            String substring = methodName.substring(i + 1, i1);
            String s = map.get(substring).toString();
            String waterOutPutCode = getWaterOutPutCode(s);
            return waterOutPutCode;
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.getNoiseCode.getValue())) {
            int i = methodName.indexOf("(");
            int i1 = methodName.indexOf(")");
            String substring = methodName.substring(i + 1, i1);
            String s = map.get(substring).toString();
            String noiseCode = getNoiseCode(s);
            return noiseCode;
        } else if (methodName.equals(ConstantsUtil.DefaultMethod.getGasOutputCode.getValue())) {
            int i = methodName.indexOf("(");
            int i1 = methodName.indexOf(")");
            String substring = methodName.substring(i + 1, i1);
            String s = map.get(substring).toString();
            String gasOutPutCode = getGasOutPutCode(s);
            return gasOutPutCode;
        }
        return "";
    }


    private static String getNoiseCode(String pollutionid) {
        try {
            String outputcode;
            outputcode = "";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutioId", pollutionid);
            System.out.println(slaveDataSource);
            DynamicDataSourceContextHolderUtil.setDataSourceType(slaveDataSource);
            staticFunctionMapper.getNoiseCode(paramMap);
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            if (paramMap.get("monitorpointcode") != null) {
                outputcode = paramMap.get("monitorpointcode").toString();
            }
            return outputcode;
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }


    private static String getWaterOutPutCode(String pollutionid) {
        try {
            String outputcode;
            outputcode = "";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutioId", pollutionid);

            DynamicDataSourceContextHolderUtil.setDataSourceType(slaveDataSource);
            staticFunctionMapper.getWaterOutputCode(paramMap);
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            if (paramMap.get("WaterOutputCode") != null) {
                outputcode = paramMap.get("WaterOutputCode").toString();
            }
            return outputcode;
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }



    private static String getGasOutPutCode(String pollutionid) {
        try {
            String outputcode;
            outputcode = "";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutioId", pollutionid);
            System.out.println(slaveDataSource);
            DynamicDataSourceContextHolderUtil.setDataSourceType(slaveDataSource);
            staticFunctionMapper.getGasOutputCode(paramMap);
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            if (paramMap.get("GasOutputCode") != null) {
                outputcode = paramMap.get("GasOutputCode").toString();
            }
            return outputcode;
        } catch (Exception e) {
            DynamicDataSourceContextHolderUtil.setDataSourceType(defaultDataSource);
            e.printStackTrace();
            throw e;
        }
    }
}

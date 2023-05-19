package com.tjpu.sp.service.impl.environmentalprotection.pubclassconfig;


import com.tjpu.pk.common.datasource.DynamicDataSourceContextHolderUtil;
import com.tjpu.sp.dao.common.pubcode.PubCodeMapper;
import com.tjpu.sp.service.environmentalprotection.pubclassconfig.PubClassConfigService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class PubClassConfigServiceImpl implements PubClassConfigService {

    @Autowired
    private PubCodeMapper pubCodeMapper;


    public List<Map<String, Object>> getPubClassConfigTreeData() {
        //获取所有数据
        List<Map<String, Object>> data = pubCodeMapper.getPubClassConfigTreeData();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> map : data) {
            if (("".equals(map.get("ParentCode")) || map.get("ParentCode") == null) && map.get("DataType") != null && !"".equals(map.get("DataType")) && "0".equals(map.get("DataType").toString())) {
                Map<String, Object> map1 = new HashMap<String, Object>();
                map1.put("id", map.get("Code"));
                map1.put("label", map.get("Name"));
                List<Map<String, Object>> childList = getPubChildrenListInfo(data, map.get("Code"));
                if (childList.size() > 0) {
                    map1.put("children", childList);
                }
                result.add(map1);
            }
        }
        return result;
    }


    private List<Map<String, Object>> getPubChildrenListInfo(List<Map<String, Object>> data, Object parentCode) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> map : data) {
            if (parentCode.equals(map.get("ParentCode")) && map.get("DataType") != null && !"".equals(map.get("DataType")) && "1".equals(map.get("DataType").toString())) {
                Map<String, Object> map1 = new HashMap<String, Object>();
                map1.put("id", map.get("Code"));
                map1.put("label", map.get("Name"));
                List<Map<String, Object>> childList = getPubChildrenListInfo(data, map.get("Code"));
                if (childList.size() > 0) {
                    map1.put("children", childList);
                }
                result.add(map1);
            }
        }
        return result;

    }

    /**
     * @author: xsm
     * @date: 2019/6/17 0017 下午 5:36
     * @Description: 根据sysmodel获取表名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public int isTableDataHaveInfo(Map<String, Object> paramMap) {
        String tablename = pubCodeMapper.getPubCodeTableNameBySysmodel(paramMap);
        int i = 0;
        if (tablename != null) {
            paramMap.put("tableName", tablename);
            i = pubCodeMapper.isTableDataHaveInfo(paramMap);
        }
        return i;
    }

    /**
     * @author: lip
     * @date: 2019/10/15 0015 下午 5:14
     * @Description: 自定义条件删除公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void deletePubCodeDataByParam(Map<String, Object> paramMap) {
        String tablename = pubCodeMapper.getPubCodeTableNameBySysmodel(paramMap);
        if (StringUtils.isNotBlank(tablename)) {
            paramMap.put("tablename", tablename);
            pubCodeMapper.deletePubCodeDataByParam(paramMap);
        }

    }

    /**
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:07
     * @Description: 拼接sql，添加公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addPubCodeDataByParam(Map<String, Object> paramMap) {

        Map<String, Object> tableMap = pubCodeMapper.getPubCodeTableConfigBySysmodel(paramMap);
        if (tableMap.size() > 0) {
            String tableName = tableMap.get("tablename").toString();
            String keyFieldName = tableMap.get("keyfieldname").toString();
            //判断指定表是否有自增字段，是：返回1，否：返回0
            int isHave = pubCodeMapper.getTableHasIdentity("'" + tableName + "'");
            //1,判断表是否自增，是：{sql中不插入主键ID},否：{判断是否为字符串，是：使用uuid，否：获取最大值+1}
            if (isHave == 0) {
                String pkValue;
                if (tableMap.get("keyfieldisnumber") != null && "1".equals(tableMap.get("keyfieldisnumber").toString())) {
                    Map<String, Object> paramMapTemp = new HashMap<>();
                    paramMapTemp.put("tablename", tableName);
                    paramMapTemp.put("pkid", keyFieldName);
                    int pkId = pubCodeMapper.getMaxNumByTableName(paramMapTemp);
                    pkValue = (pkId + 1) + "";
                } else {
                    pkValue = UUID.randomUUID().toString();
                }
                List<String> fieldList = (List<String>) paramMap.get("fieldList");
                fieldList.add(keyFieldName);
                paramMap.put("fieldList", fieldList);

                List<String> values = (List<String>) paramMap.get("values");
                values.add(pkValue);
                paramMap.put("values", values);
            }
            paramMap.put("tablename", tableName);
            pubCodeMapper.addPubCodeDataByParam(paramMap);
        }

    }

    /**
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:07
     * @Description: 拼接sql，添加公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void editPubCodeDataByParam(Map<String, Object> paramMap) {
        Map<String, Object> tableMap = pubCodeMapper.getPubCodeTableConfigBySysmodel(paramMap);
        if (tableMap.size() > 0) {
            String tableName = tableMap.get("tablename").toString();
            String keyFieldName = tableMap.get("keyfieldname").toString().toLowerCase();
            Map<String, Object> editMap = new HashMap<>();
            Object pkValue = null;
            for (String key : paramMap.keySet()) {
                if (!keyFieldName.equals(key) && !key.equals("sysmodel")) {
                    editMap.put(key, paramMap.get(key));
                } else if (keyFieldName.equals(key)) {
                    pkValue = paramMap.get(key);
                }
            }
            paramMap.clear();
            paramMap.put("editMap", editMap);
            paramMap.put("key", keyFieldName);
            paramMap.put("value", pkValue);
            paramMap.put("tablename", tableName);
            pubCodeMapper.editPubCodeDataByParam(paramMap);
        }
    }
}
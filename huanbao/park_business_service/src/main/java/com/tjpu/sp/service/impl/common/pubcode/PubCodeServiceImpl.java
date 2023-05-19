package com.tjpu.sp.service.impl.common.pubcode;


import com.tjpu.sp.dao.common.pubcode.PubCodeMapper;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PubCodeServiceImpl implements PubCodeService {

    @Autowired
    private PubCodeMapper pubCodeMapper;



    /**
     * @author: lip
     * @date: 2018/9/13 0013 下午 5:44
     * @Description: 通过表名称、排序字段、where条件，获取公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPubCodeDataByParam(Map<String, Object> paramMap) {
        return pubCodeMapper.getPubCodeDataByParam(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/10/14 0014 下午 2:20
     * @Description: 通过表名称、排序字段、where条件，获取公共代码表指定的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPubCodesDataByParam(Map<String, Object> paramMap) {
        return pubCodeMapper.getPubCodesDataByParam(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/10/15 0015 下午 3:53
     * @Description:  通过表名称、排序字段、where条件，获取公共代码表指定的数据(有二级缓存)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPubCodesDataByParamWithCache(Map<String, Object> paramMap) {
        return pubCodeMapper.getPubCodesDataByParamWithCache(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2018/11/14 0014 上午 10:01
     * @Description: 获取码表树数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    public List<Map<String, Object>> getPubTree(String tablename,String wherestring) throws Exception {
        List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("wherestring", wherestring);
        paramMap.put("tablename", tablename);
        List<Map<String, Object>> pubCodeDataByParam = pubCodeMapper.getPubCodeDataByParam(paramMap);

        List<String> Codes = pubCodeDataByParam.stream().map(m -> m.get("Code").toString()).collect(Collectors.toList());

        List<String> parentCodeList = pubCodeDataByParam.stream().filter(m -> m.get("Code") != null && m.get("ParentCode") != null &&
                !Codes.contains(m.get("ParentCode").toString())).map(m -> m.get("Code").toString()).collect(Collectors.toList());
        List<String> collect2 = pubCodeDataByParam.stream().filter(m -> m.get("Code") != null && m.get("ParentCode") == null)
                .map(m -> m.get("Code").toString()).collect(Collectors.toList());

        parentCodeList.addAll(collect2);


        for (String code : parentCodeList) {
            for (Map<String, Object> stringObjectMap : pubCodeDataByParam) {
                if (code.equals(stringObjectMap.get("Code").toString())) {
                    Map<String, Object> parentMap = new HashMap<String, Object>();
                    parentMap.put("id", stringObjectMap.get("Code"));
                    parentMap.put("label", stringObjectMap.get("Name"));
                    List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
                    String parentCode = stringObjectMap.get("Code").toString();
                    childList = getChildrenList(pubCodeDataByParam, parentCode);
                    if (childList.size() > 0) {
                        parentMap.put("children", childList);
                    }
                    listData.add(parentMap);
                }
            }
        }


        return listData;
    }

    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 下午 4:42
     * @Description: 验证传入数据是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public int isTableDataHaveInfo(Map<String, Object> paramMap) {
        return pubCodeMapper.isTableDataHaveInfo(paramMap);
    }



    /**
     * @Author: zhangzc
     * @Date: 2018/12/20 14:31
     * @Description: 添加时懒加载树形结构数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param: tableName 表名称
     * @Param: codeFieldName  表中code字段名称
     * @Param: viewFieldName  表中name字段名称
     * @Param: orderFieldName 排序字段名称
     * @Param: parentFieldName 父级字段名称
     * @Param: whereSql 查询条件Sql
     * @Param: codeValue 节点值
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getTreeDataByCodeValue(String tableName,
                                                            String codeFieldName,
                                                            String viewFieldName,
                                                            String parentFieldName,
                                                            String orderFieldName,
                                                            String whereSql,
                                                            String codeValue) {
        //获取数据
        List<Map<String, Object>> data = pubCodeMapper.getTreeDataByCodeValue(tableName, codeFieldName, viewFieldName, parentFieldName, orderFieldName, whereSql);
        List<Map<String, Object>> result = new ArrayList<>();
        if (codeValue == null) {
            codeValue = "";
        }
        for (Map<String, Object> map : data) {
            if (map.get(parentFieldName).equals(codeValue)) {
                Map<String, Object> map1 = new HashMap<>();
                Object code = map.get(codeFieldName);
                map1.put("id", map.get(codeFieldName));
                map1.put("label", map.get(viewFieldName));
                boolean flag = false;
                for (Map<String, Object> datum : data) {
                    if (datum.get(parentFieldName).equals(code)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    map1.put("children", null);
                }
                result.add(map1);
            }
        }
        return result;
    }




    private List<Map<String, Object>> getChildrenList(List<Map<String, Object>> List, String parentCode) throws Exception {
        List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
        for (Map map : List) {
            if (map.get("ParentCode") != null && map.get("ParentCode").toString().equals(parentCode)) {
                Map<String, Object> chlidMap = new HashMap<String, Object>();
                chlidMap.put("id", map.get("Code"));
                chlidMap.put("label", map.get("Name"));
                List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
                childList = getChildrenList(List, map.get("Code").toString());
                if (childList.size() > 0) {
                    chlidMap.put("children", childList);
                }
                listData.add(chlidMap);
            }
        }
        return listData;
    }

}
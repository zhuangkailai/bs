package com.tjpu.sp.dao.environmentalprotection.radiationsafety;

import com.tjpu.sp.model.environmentalprotection.radiationsafety.NonSealedVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface NonSealedMapper {
    int deleteByPrimaryKey(String pkNonsealedmaterialid);

    int insert(NonSealedVO record);

    int insertSelective(NonSealedVO record);

    NonSealedVO selectByPrimaryKey(String pkNonsealedmaterialid);

    int updateByPrimaryKeySelective(NonSealedVO record);

    int updateByPrimaryKey(NonSealedVO record);
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 9:52
    *@Description: 通过自定义参数获取非密封放射性物质信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getNonSealedByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/23 0023 10:52
    *@Description: 获取非密封放射性物质详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getNonSealedDetailById(String id);
}
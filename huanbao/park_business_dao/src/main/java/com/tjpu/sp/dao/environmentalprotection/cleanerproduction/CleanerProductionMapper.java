package com.tjpu.sp.dao.environmentalprotection.cleanerproduction;

import com.tjpu.sp.model.environmentalprotection.cleanerproduction.CleanerProductionVO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface CleanerProductionMapper {
    int deleteByPrimaryKey(String pkCleanerproductid);

    int insert(CleanerProductionVO record);

    int insertSelective(CleanerProductionVO record);

    CleanerProductionVO selectByPrimaryKey(String pkCleanerproductid);

    int updateByPrimaryKeySelective(CleanerProductionVO record);

    int updateByPrimaryKey(CleanerProductionVO record);
    /**
    *@author: liyc
    *@date: 2019/10/18 0018 16:15
    *@Description: 通过自定义参数获取清洁生产信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getCleanerInfoByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/21 0021 9:55
    *@Description: 获取清洁生产的详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getCleanerDetailById(String id);
}
package com.tjpu.sp.dao.environmentalprotection.productionmaterials;

import com.tjpu.sp.model.environmentalprotection.productionmaterials.ProductInfoVO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
@Repository
public interface ProductInfoMapper {
    int deleteByPrimaryKey(String pkFuelinfoid);

    int insert(ProductInfoVO record);

    int insertSelective(ProductInfoVO record);

    ProductInfoVO selectByPrimaryKey(String pkFuelinfoid);

    int updateByPrimaryKeySelective(ProductInfoVO record);

    int updateByPrimaryKey(ProductInfoVO record);

    List<Map<String,Object>> getProductInfosByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getProductInfoDetailByID(String pkid);

    List<Map<String,Object>> getProductInfoAndPollutionInfoByParamMap(Map<String, Object> paramMap);
}
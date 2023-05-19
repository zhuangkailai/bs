package com.tjpu.auth.dao.GeneralMethod;

import org.springframework.stereotype.Repository;

import java.util.Map;
@Repository
public interface FunctionMapper {
    void getPollutionCode(Map<String, Object> params);
    void getWaterOutputCode(Map<String, Object> paramMap);
    void getNoiseCode(Map<String, Object> paramMap);

}
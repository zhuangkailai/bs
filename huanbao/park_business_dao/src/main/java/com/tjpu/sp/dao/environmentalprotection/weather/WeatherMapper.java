package com.tjpu.sp.dao.environmentalprotection.weather;

import com.tjpu.sp.model.environmentalprotection.weather.WeatherVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface WeatherMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WeatherVO record);

    int insertSelective(WeatherVO record);

    WeatherVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WeatherVO record);

    int updateByPrimaryKey(WeatherVO record);


    /**
     *
     * @author: lip
     * @date: 2019/8/14 0014 下午 3:24
     * @Description: 自定义查询条件获取日气象数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getDayWeatherByParamMap(Map<String, Object> paramMap);
}
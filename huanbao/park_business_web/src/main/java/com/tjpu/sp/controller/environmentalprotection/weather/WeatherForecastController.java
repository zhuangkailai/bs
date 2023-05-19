package com.tjpu.sp.controller.environmentalprotection.weather;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.weather.WeatherService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("weather")
public class WeatherForecastController {

    private final WeatherService weatherService;

    public WeatherForecastController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/15 10:44
     * @Description: 获取园区目前天气预报以及后四天天气预报（当天为小时数据，后四天为日数据）
     * @updateUser:xsm
     * @updateDate:2019/6/21 9:27
     * @updateDescription: 获取从目前天气预报日期开始的后四天的天气预报
     * @param:
     * @return:
     */
    @RequestMapping(value = "getCurrentAndAfterFourDaysWeathers", method = RequestMethod.GET)
    public Object getCurrentAndAfterFourDaysWeathers() {
        try {
            //获取当天的根据小时时间持续更新和后四天的预报
            Map<String,Object> resultMap = weatherService.getTodayLastHourAndAfterFourDayWeathers();
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     *
     * @author: lip
     * @date: 2019/8/14 0014 下午 3:17
     * @Description: 获取当前天气象数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @RequestMapping(value = "getCurrentDaysWeathers", method = RequestMethod.GET)
    public Object getCurrentDaysWeathers() {
        Map<String,Object> resultMap = new HashMap<>();


        String nowDay = DataFormatUtil.getDateYMD(new Date());

        nowDay = "2019-05-18";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("weatherdate",nowDay);
        List<Map<String, Object>>  dayWeather = weatherService.getDayWeatherByParamMap(paramMap);
        if (dayWeather.size()>0){
            resultMap = dayWeather.get(0);
        }
        return AuthUtil.parseJsonKeyToLower("success", resultMap);
    }

    /**
     * @author: xsm
     * @date: 2020/5/19 0019 上午 10:17
     * @Description: 根据日期获取该日期所有小时气候数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getHoursWeathersByDayTime", method = RequestMethod.POST)
    public Object getHoursWeathersByDayTime(@RequestJson(value = "daytime") String daytime ) throws Exception {
        try {
            List<Map<String,Object>> result = weatherService.getHoursWeathersByDayTime(daytime);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/5/19 0019 上午 10:17
     * @Description: 获取小时天气情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getHourWeathersByTime", method = RequestMethod.POST)
    public Object getHourWeathersByTime(@RequestJson(value = "monitortime") String monitortime ) throws Exception {
        try {
            Map<String, Object> result = weatherService.getHourWeathersByTime(monitortime);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @author: chengzq
     * @date: 2020/11/20 0020 下午 4:34
     * @Description: 获取当天最新天气信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getTodayLastDayWeather", method = RequestMethod.POST)
    public Object getTodayLastDayWeather() throws Exception {
        try {
            Map<String,Object> result = weatherService.getTodayLastDayWeather();
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

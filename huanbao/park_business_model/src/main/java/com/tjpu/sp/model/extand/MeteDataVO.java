package com.tjpu.sp.model.extand;

import com.opencsv.bean.CsvBindByName;

public class MeteDataVO {

    @CsvBindByName(column = "StaID")
    private String StaID;

    @CsvBindByName(column = "Time")
    private String Time;

    @CsvBindByName(column = "wind direction")
    private String windDirection;

    @CsvBindByName(column = "wind speed")
    private String windSpeed;


    @CsvBindByName(column = "temperature")
    private String temperature;


    @CsvBindByName(column = "rain")
    private String rain;

    public String getStaID() {
        return StaID;
    }

    public void setStaID(String staID) {
        StaID = staID;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getRain() {
        return rain;
    }

    public void setRain(String rain) {
        this.rain = rain;
    }
}

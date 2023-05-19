package com.tjpu.sp.model.environmentalprotection.weather;

import java.util.Date;

public class WeatherVO {
    private String pkId;

    private String fkRegioncode;

    private Date weatherdate;

    private Date weatherhour;

    private String weatherphenomenon;

    private String temperature;

    private String winddirection;

    private String windpower;

    private String aqi;

    private String humidity;

    private String precipitation;

    private String weekdate;

    private String hightemperature;

    private String lowtemperature;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkRegioncode() {
        return fkRegioncode;
    }

    public void setFkRegioncode(String fkRegioncode) {
        this.fkRegioncode = fkRegioncode == null ? null : fkRegioncode.trim();
    }

    public Date getWeatherdate() {
        return weatherdate;
    }

    public void setWeatherdate(Date weatherdate) {
        this.weatherdate = weatherdate;
    }

    public Date getWeatherhour() {
        return weatherhour;
    }

    public void setWeatherhour(Date weatherhour) {
        this.weatherhour = weatherhour;
    }

    public String getWeatherphenomenon() {
        return weatherphenomenon;
    }

    public void setWeatherphenomenon(String weatherphenomenon) {
        this.weatherphenomenon = weatherphenomenon == null ? null : weatherphenomenon.trim();
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature == null ? null : temperature.trim();
    }

    public String getWinddirection() {
        return winddirection;
    }

    public void setWinddirection(String winddirection) {
        this.winddirection = winddirection == null ? null : winddirection.trim();
    }

    public String getWindpower() {
        return windpower;
    }

    public void setWindpower(String windpower) {
        this.windpower = windpower == null ? null : windpower.trim();
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi == null ? null : aqi.trim();
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity == null ? null : humidity.trim();
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(String precipitation) {
        this.precipitation = precipitation == null ? null : precipitation.trim();
    }

    public String getWeekdate() {
        return weekdate;
    }

    public void setWeekdate(String weekdate) {
        this.weekdate = weekdate == null ? null : weekdate.trim();
    }

    public String getHightemperature() {
        return hightemperature;
    }

    public void setHightemperature(String hightemperature) {
        this.hightemperature = hightemperature == null ? null : hightemperature.trim();
    }

    public String getLowtemperature() {
        return lowtemperature;
    }

    public void setLowtemperature(String lowtemperature) {
        this.lowtemperature = lowtemperature == null ? null : lowtemperature.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatedate) {
        this.updatetime = updatedate;
    }
}
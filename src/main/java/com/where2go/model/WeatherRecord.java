package com.where2go.model;

import java.util.Objects;

/**
 * WeatherRecord represents a single hourly weather observation.
 * Supports:
 * - weatherCode (Tomorrow.io code)
 * - weatherType (converted string: sunny, cloudy, rain, etc.)
 */
public class WeatherRecord {

    private String city;
    private String date; // YYYY-MM-DD
    private int hour;

    private double temperature;
    private double humidity;
    private double windSpeed;
    private double precipitation;
    private double uvIndex;
    private double cloudCover;
    private double visibility;
    private double pressure;

    private int weatherCode; 
    private String weatherType;

    public WeatherRecord() {
    }

    public WeatherRecord(
            String city,
            String date,
            int hour,
            double temperature,
            double humidity,
            double windSpeed,
            double precipitation,
            double uvIndex,
            double cloudCover,
            double visibility,
            double pressure,
            int weatherCode,
            String weatherType) {
        this.city = city;
        this.date = date;
        this.hour = hour;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.precipitation = precipitation;
        this.uvIndex = uvIndex;
        this.cloudCover = cloudCover;
        this.visibility = visibility;
        this.pressure = pressure;
        this.weatherCode = weatherCode;
        this.weatherType = weatherType;
    }

    public String getCity() {
        return city;
    }

    public String getDate() {
        return date;
    }

    public int getHour() {
        return hour;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public double getCloudCover() {
        return cloudCover;
    }

    public double getVisibility() {
        return visibility;
    }

    public double getPressure() {
        return pressure;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public String getWeatherType() {
        return weatherType;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }

    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }

    public void setCloudCover(double cloudCover) {
        this.cloudCover = cloudCover;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    public void setWeatherType(String weatherType) {
        this.weatherType = weatherType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WeatherRecord))
            return false;
        WeatherRecord that = (WeatherRecord) o;
        return hour == that.hour &&
                Objects.equals(city, that.city) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, date, hour);
    }
}

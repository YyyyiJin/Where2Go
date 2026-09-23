package com.where2go.util;

import com.where2go.model.WeatherRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeatherParser {

    private static final ZoneId SEATTLE_ZONE = ZoneId.of("America/Los_Angeles");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    public static List<WeatherRecord> parseWeather(String city, JSONObject json) {
        List<WeatherRecord> list = new ArrayList<>();

        JSONArray hours = json.getJSONObject("timelines").getJSONArray("hourly");

        ZonedDateTime nowLocal = ZonedDateTime.now(SEATTLE_ZONE)
                .withMinute(0).withSecond(0).withNano(0);

        LocalDate today = LocalDate.now(SEATTLE_ZONE);
        LocalDate maxDate = today.plusDays(3);   // t+3

        for (int i = 0; i < hours.length(); i++) {
            JSONObject h = hours.getJSONObject(i);
            JSONObject v = h.getJSONObject("values");

            String timeStr = h.getString("time");
            ZonedDateTime utcTime = ZonedDateTime.parse(timeStr, ISO_FORMAT);
            ZonedDateTime localTime = utcTime.withZoneSameInstant(SEATTLE_ZONE);

            String date = localTime.toLocalDate().toString();
            int hour = localTime.getHour();

            LocalDate parsed = LocalDate.parse(date);
            if (parsed.isBefore(today) || parsed.isAfter(maxDate)) {
                continue; 
            }

            int weatherCode = v.optInt("weatherCode", 0);
            String weatherType = convertWeatherCode(weatherCode);

            double precipitationProb = v.optDouble("precipitationProbability", 0);

            WeatherRecord record = new WeatherRecord(
                    city,
                    date,
                    hour,
                    v.optDouble("temperature", 0),
                    v.optDouble("humidity", 0),
                    v.optDouble("windSpeed", 0),
                    precipitationProb,
                    v.optDouble("uvIndex", 0),
                    v.optDouble("cloudCover", 0),
                    v.optDouble("visibility", 0),
                    v.optDouble("pressureSeaLevel", 0),
                    weatherCode,
                    weatherType
            );

            list.add(record);
        }

        return list;
    }

    private static String convertWeatherCode(int code) {
        return switch (code) {
            case 1000 -> "sunny";
            case 1100 -> "mostly_sunny";
            case 1101 -> "partly_cloudy";
            case 1102 -> "mostly_cloudy";
            case 1001 -> "cloudy";

            case 2000 -> "fog";
            case 2100 -> "light_fog";

            case 3000 -> "light_wind";
            case 3001 -> "wind";
            case 3002 -> "strong_wind";

            case 4000 -> "drizzle";
            case 4001 -> "rain";
            case 4200 -> "light_rain";
            case 4201 -> "heavy_rain";

            case 5000 -> "snow";
            case 5001 -> "flurries";
            case 5100 -> "light_snow";
            case 5101 -> "heavy_snow";

            case 6000 -> "freezing_drizzle";
            case 6001 -> "freezing_rain";
            case 6200 -> "light_freezing_rain";
            case 6201 -> "heavy_freezing_rain";

            case 7000 -> "ice_pellets";
            case 7101 -> "heavy_ice_pellets";
            case 7102 -> "light_ice_pellets";

            case 8000 -> "thunderstorm";

            default -> "unknown";
        };
    }
}

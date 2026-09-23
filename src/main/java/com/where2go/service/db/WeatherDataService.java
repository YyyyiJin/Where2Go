package com.where2go.service.db;

import com.where2go.model.WeatherRecord;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class WeatherDataService {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/data/app.db";

    public WeatherDataService() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = """
                CREATE TABLE IF NOT EXISTS weather (
                    city TEXT NOT NULL,
                    date TEXT NOT NULL,
                    hour INTEGER NOT NULL,
                    temperature REAL,
                    humidity REAL,
                    windSpeed REAL,
                    precipitation REAL,
                    uvIndex REAL,
                    cloudCover REAL,
                    visibility REAL,
                    pressure REAL,
                    weatherCode INTEGER,
                    weatherType TEXT,
                    PRIMARY KEY (city, date, hour)
                );
            """;

            conn.createStatement().execute(sql);
            System.out.println("📂 [Database] Weather table initialized.");

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize weather.db: " + e.getMessage());
        }
    }

    public void saveAll(List<WeatherRecord> records) {
        String sql = """
            INSERT OR REPLACE INTO weather (
                city, date, hour,
                temperature, humidity, windSpeed, precipitation,
                uvIndex, cloudCover, visibility, pressure,
                weatherCode, weatherType
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (WeatherRecord r : records) {
                ps.setString(1, r.getCity());
                ps.setString(2, r.getDate());
                ps.setInt(3, r.getHour());
                ps.setDouble(4, r.getTemperature());
                ps.setDouble(5, r.getHumidity());
                ps.setDouble(6, r.getWindSpeed());
                ps.setDouble(7, r.getPrecipitation());
                ps.setDouble(8, r.getUvIndex());
                ps.setDouble(9, r.getCloudCover());
                ps.setDouble(10, r.getVisibility());
                ps.setDouble(11, r.getPressure());
                ps.setInt(12, r.getWeatherCode());
                ps.setString(13, r.getWeatherType());
                ps.addBatch();
            }

            ps.executeBatch();
            System.out.println("💾 Weather data saved: " + records.size());

        } catch (Exception e) {
            System.err.println("❌ Failed to save weather: " + e.getMessage());
        }
    }

    public List<WeatherRecord> load(String city, String date, List<Integer> hours) {
        List<WeatherRecord> result = new ArrayList<>();
        if (hours == null || hours.isEmpty()) return result;

        String hourPlaceholders = String.join(",", hours.stream().map(h -> "?").toList());

        String sql = """
            SELECT * FROM weather
            WHERE city = ? AND date = ? AND hour IN (%s)
            ORDER BY hour ASC;
        """.formatted(hourPlaceholders);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);
            ps.setString(2, date);

            int idx = 3;
            for (int h : hours) ps.setInt(idx++, h);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new WeatherRecord(
                        rs.getString("city"),
                        rs.getString("date"),
                        rs.getInt("hour"),
                        rs.getDouble("temperature"),
                        rs.getDouble("humidity"),
                        rs.getDouble("windSpeed"),
                        rs.getDouble("precipitation"),
                        rs.getDouble("uvIndex"),
                        rs.getDouble("cloudCover"),
                        rs.getDouble("visibility"),
                        rs.getDouble("pressure"),
                        rs.getInt("weatherCode"),
                        rs.getString("weatherType")
                ));
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to load weather: " + e.getMessage());
        }

        return result;
    }

    public List<String> getAvailableDates() {
        List<String> list = new ArrayList<>();

        String sql = "SELECT DISTINCT date FROM weather ORDER BY date ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("date"));
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to fetch dates: " + e.getMessage());
        }

        return list;
    }
}

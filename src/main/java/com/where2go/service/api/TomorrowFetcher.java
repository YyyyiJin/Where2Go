package com.where2go.service.api;

import com.where2go.model.WeatherRecord;
import com.where2go.service.db.WeatherDataService;
import com.where2go.util.WeatherParser;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * TomorrowFetcher
 * --------------------------
 * Responsibilities:
 *  - Call the Tomorrow.io Weather API
 *  - Parse the API response into WeatherRecord objects
 *  - Provide both synchronous (fetchAndSave) and asynchronous (fetchAsync) data-saving methods
 *  - Does NOT decide when updates should occur; update scheduling is handled elsewhere
 */
@Service
public class TomorrowFetcher {

    private final WeatherDataService weatherDataService;

    /**
     * API KEY set in application.properties
     *   tomorrow.api.key=xxxx
     */
    @Value("${tomorrow.api.key}")
    private String apiKey;

    private static boolean initialized = false;

    public TomorrowFetcher(WeatherDataService weatherDataService) {
        this.weatherDataService = weatherDataService;
    }

    @PostConstruct
    public void init() {
        if (initialized) {
            System.out.println("[TomorrowFetcher] Initialization skipped (already initialized).");
            return;
        }
        initialized = true;
        System.out.println("[TomorrowFetcher] Service initialized. Ready to fetch on demand.");
    }

    /**
     * Synchronous fetch + save
     */
    public void fetchAndSave(String city) {
        System.out.println("[TomorrowFetcher] 🔄 Fetching data for: " + city);

        List<WeatherRecord> records = fetch(city);

        if (!records.isEmpty()) {
            weatherDataService.saveAll(records);  // ✅ FIXED
            System.out.println("[TomorrowFetcher] ✅ Saved " + records.size() + " records for " + city);
        } else {
            System.err.println("[TomorrowFetcher] ❌ No records fetched for " + city);
        }
    }

    /**
     * Asynchronous fetch (background thread)
     */
    public void fetchAsync(String city) {
        System.out.println("[TomorrowFetcher] 🚀 Starting async fetch for: " + city);

        new Thread(() -> {
            try {
                fetchAndSave(city);
            } catch (Exception e) {
                System.err.println("[TomorrowFetcher] ❌ Async fetch failed for " + city);
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Call Tomorrow.io API and return WeatherRecord list
     */
    public List<WeatherRecord> fetch(String city) {

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[TomorrowFetcher] ❌ API KEY missing! Please configure tomorrow.api.key");
            return List.of();
        }

        try {
            String encodedCity = city.replace(" ", "%20");
            String apiUrl =
                    "https://api.tomorrow.io/v4/weather/forecast?location="
                    + encodedCity + "&apikey=" + apiKey;

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("[TomorrowFetcher] ❌ API error for " + city + " (code: " + code + ")");
                printErrorStream(conn);
                return List.of();
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            JSONObject json = new JSONObject(sb.toString());

            if (!json.has("timelines")
                    || !json.getJSONObject("timelines").has("hourly")) {
                System.err.println("[TomorrowFetcher] ❌ Unexpected JSON structure for " + city);
                return List.of();
            }

            return WeatherParser.parseWeather(city, json);

        } catch (Exception e) {
            System.err.println("[TomorrowFetcher] ❌ Exception when fetching " + city);
            e.printStackTrace();
            return List.of();
        }
    }

    /** Print API error details */
    private void printErrorStream(HttpURLConnection conn) {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.err.println("[TomorrowFetcher]   " + line);
            }
        } catch (Exception ignored) {}
    }
}

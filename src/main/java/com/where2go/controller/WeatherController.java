package com.where2go.controller;

import com.where2go.config.CityRegistry;
import com.where2go.model.WeatherRecord;
import com.where2go.service.api.TomorrowFetcher;
import com.where2go.service.db.WeatherDataService;   // ← 改这里
import com.where2go.service.recommend.RecommendationEngine;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * WeatherController exposes REST endpoints:
 *  - /cities       → configured cities
 *  - /dates        → available dates in DB
 *  - /metrics      → compute metrics + ML recommendation
 *  - /recommend    → top recommendation only
 *  - /hourly       → detailed hourly data for charts
 *  - /refresh-all  → force refresh weather data for all cities
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final RecommendationEngine recommendationEngine;
    private final WeatherDataService weatherDb;        // ← 改这里
    private final CityRegistry cityRegistry;
    private final TomorrowFetcher fetcher;

    public WeatherController(RecommendationEngine recommendationEngine,
                             WeatherDataService weatherDb,             // ← 改这里
                             CityRegistry cityRegistry,
                             TomorrowFetcher fetcher) {
        this.recommendationEngine = recommendationEngine;
        this.weatherDb = weatherDb;                         // ← 改这里
        this.cityRegistry = cityRegistry;
        this.fetcher = fetcher;
    }

    @GetMapping("/cities")
    public List<String> getConfiguredCities() {
        return cityRegistry.getAllCities();
    }

    @GetMapping("/dates")
    public List<String> getAvailableDates() {
        List<String> dbDates = weatherDb.getAvailableDates();   // ← 改这里
        if (dbDates.isEmpty()) {
            return Collections.singletonList(LocalDate.now().toString());
        }
        return dbDates;
    }

    @GetMapping("/metrics")
    public List<Map<String, Object>> getMetrics(
            @RequestParam String cities,
            @RequestParam String date,
            @RequestParam String hours,
            @RequestParam String origin
    ) {
        System.out.println("➡️ [Controller] Request: cities=" + cities +
                " | date=" + date +
                " | hours=" + hours +
                " | origin=" + origin);

        List<String> cityList = parseCities(cities);
        List<Integer> hourList = parseHours(hours);

        List<Map<String, Object>> result =
                recommendationEngine.recommendCities(cityList, date, hourList, origin);

        System.out.println("⬅️ [Controller] Returning " + result.size() + " records.");

        return result;
    }

    @GetMapping("/recommend")
    public Map<String, Object> getRecommendation(
            @RequestParam String cities,
            @RequestParam String date,
            @RequestParam String hours,
            @RequestParam String origin
    ) {
        List<Map<String, Object>> all = getMetrics(cities, date, hours, origin);
        if (all.isEmpty()) {
            return Map.of("message", "No recommendation available");
        }
        return all.get(0);
    }

    @GetMapping("/hourly")
    public List<Map<String, Object>> getHourlyData(
            @RequestParam String cities,
            @RequestParam String date,
            @RequestParam String hours
    ) {
        List<String> cityList = parseCities(cities);
        List<Integer> hourList = parseHours(hours);

        List<Map<String, Object>> result = new ArrayList<>();

        for (String city : cityList) {
            List<WeatherRecord> records = weatherDb.load(city, date, hourList); // ← 改这里
            for (WeatherRecord r : records) {
                Map<String, Object> row = new HashMap<>();
                row.put("city", city);
                row.put("date", r.getDate());
                row.put("hour", r.getHour());
                row.put("hourLabel", String.format("%02d:00", r.getHour()));
                row.put("temperature", r.getTemperature());

                double rain = r.getPrecipitation();
                if (rain <= 1.0) rain *= 100.0;
                row.put("rainProbability", rain);

                row.put("windSpeed", r.getWindSpeed());
                row.put("weatherType", r.getWeatherType());

                result.add(row);
            }
        }

        result.sort(Comparator
                .comparing((Map<String, Object> m) -> (Integer) m.get("hour"))
                .thenComparing(m -> (String) m.get("city")));

        return result;
    }

    @GetMapping("/refresh-all")
    public Map<String, Object> refreshAll() {

        List<String> cities = cityRegistry.getAllCities();
        int success = 0;

        for (String city : cities) {
            try {
                System.out.println("🔄 [Refresh] Fetching city: " + city);
                fetcher.fetchAndSave(city);
                success++;
            } catch (Exception e) {
                System.err.println("❌ Refresh failed for " + city + ": " + e.getMessage());
            }
        }

        return Map.of(
                "message", "Weather data refreshed for " + success + " cities.",
                "count", success
        );
    }

    private List<String> parseCities(String cities) {
        if (cities == null || cities.isBlank()) return Collections.emptyList();

        List<String> list = new ArrayList<>();
        for (String c : cities.split(",")) {
            String t = c.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list;
    }

    private List<Integer> parseHours(String hours) {
        if (hours == null || hours.isBlank()) return Collections.emptyList();

        List<Integer> list = new ArrayList<>();
        for (String h : hours.split(",")) {
            String t = h.trim();
            if (t.isEmpty()) continue;

            String onlyHour = t.contains(":") ? t.substring(0, t.indexOf(':')) : t;
            try {
                list.add(Integer.parseInt(onlyHour));
            } catch (NumberFormatException e) {
                System.err.println("[Controller] Invalid hour: " + t);
            }
        }
        return list;
    }
}

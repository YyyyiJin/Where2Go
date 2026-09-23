package com.where2go.service.metrics;

import com.where2go.config.CityRegistry;
import com.where2go.model.WeatherRecord;
import com.where2go.service.api.TomorrowFetcher;
import com.where2go.service.db.WeatherDataService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MetricsCalculator computes numerical indicators for each city:
 *  - Average temperature
 *  - Average rain probability (%)
 *  - Average wind speed
 *  - Latitude & longitude (from CityRegistry)
 *
 *  Workflow:
 *   1. Load weather records from DB
 *   2. If missing, fetch from API and reload
 *   3. Compute metrics
 *   4. Return values for UI + RecommendationEngine
 */
@Service
public class MetricsCalculator {

    private final WeatherDataService weatherDataService;
    private final TomorrowFetcher fetcher;
    private final CityRegistry cityRegistry;

    public MetricsCalculator(WeatherDataService weatherDataService,
                             TomorrowFetcher fetcher,
                             CityRegistry cityRegistry) {

        this.weatherDataService = weatherDataService;
        this.fetcher = fetcher;
        this.cityRegistry = cityRegistry;
    }

    /**
     * Computes metrics for multiple cities.
     */
    public List<Map<String, Object>> computeMetrics(
            List<String> cities,
            String date,
            List<Integer> hours
    ) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (cities == null || cities.isEmpty()) return results;

        for (String city : cities) {
            results.add(computeForSingleCity(city, date, hours));
        }

        return results;
    }

    /**
     * Computes metrics for one city:
     *  1. Load weather from DB
     *  2. If empty → API fetch → reload
     *  3. Compute averages
     *  4. Add lat/lon
     */
    private Map<String, Object> computeForSingleCity(
            String city,
            String date,
            List<Integer> hours
    ) {

        Map<String, Object> result = new HashMap<>();
        result.put("city", city);

        // 1) Load from Weather DB
        List<WeatherRecord> records = weatherDataService.load(city, date, hours);

        // 2) If empty → fetch + reload
        if (records.isEmpty()) {
            System.out.println("⚠️ [Metrics] No DB data for " + city + " on " + date + " → fetching from API...");

            try {
                fetcher.fetchAndSave(city);
                records = weatherDataService.load(city, date, hours);
            } catch (Exception e) {
                System.err.println("❌ API fetch failed for " + city + ": " + e.getMessage());
            }
        }

        // 3) If still empty → return default
        if (records.isEmpty()) {
            result.put("count", 0);
            result.put("avgTemperature", 0.0);
            result.put("avgRainProbability", 0.0);
            result.put("avgWindSpeed", 0.0);
            result.put("message", "No data");
            return result;
        }

        // 4) Temperature
        double avgTemp = records.stream()
                .mapToDouble(WeatherRecord::getTemperature)
                .average().orElse(0);

        // 5) Rain probability (0–1 or 0–100 → converted to %)
        double avgRain = records.stream()
                .mapToDouble(WeatherRecord::getPrecipitation)
                .average().orElse(0);

        if (avgRain <= 1.0) avgRain = avgRain * 100.0;

        // 6) Wind
        double avgWind = records.stream()
                .mapToDouble(WeatherRecord::getWindSpeed)
                .average().orElse(0);

        // 7) Coordinates
        double[] coords = cityRegistry.getCoords(city);
        result.put("latitude", coords[0]);
        result.put("longitude", coords[1]);

        // 8) Fill final metrics
        result.put("count", records.size());
        result.put("avgTemperature", avgTemp);
        result.put("avgRainProbability", avgRain);
        result.put("avgWindSpeed", avgWind);

        return result;
    }
}

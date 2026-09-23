package com.where2go.service.geo;

import com.where2go.config.CityRegistry;
import com.where2go.service.db.DistanceDataService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class DistanceService {

    private final CityRegistry cityRegistry;
    private final DistanceDataService db;

    // Sentinel value for failed distance API or missing data
    private static final double BAD_DISTANCE_DB = 999.0;

    @Value("${ors.api.key}")
    private String ORS_API_KEY;

    @Value("${graphhopper.api.key}")
    private String GH_API_KEY;

    private long lastORSCall = 0;
    private static final long ORS_INTERVAL_MS = 3500;

    public DistanceService(CityRegistry cityRegistry,
                           DistanceDataService db) {
        this.cityRegistry = cityRegistry;
        this.db = db;
    }

    /**
     * Determine whether a DB value should be recalculated.
     */
    public boolean needsRecalc(Double dist) {
        return dist == null || dist == BAD_DISTANCE_DB || dist < 0 || dist > 200;
    }

    /**
     * Main API: returns distance between cities A and B (km).
     */
    public double distanceKm(String a, String b) {

        if (a.equals(b)) return 0;

        // 1) Check DB cache
        Double cached = db.loadDistance(a, b);
        if (cached != null) return cached;

        // 2) ORS API call
        Double km = callORSWithRateLimit(a, b);

        // 3) Fallback GraphHopper if ORS fails
        if (km == null) {
            System.err.println("⚠️ ORS failed; trying GraphHopper...");
            km = callGraphHopper(a, b);
        }

        // 4) Both failed → store sentinel
        if (km == null) km = BAD_DISTANCE_DB;

        // 5) Save result to DB
        db.saveDistance(a, b, km);

        return km;
    }

    // -----------------------------------------------------------
    // ORS with rate limiting
    // -----------------------------------------------------------

    private Double callORSWithRateLimit(String cityA, String cityB) {
        long now = System.currentTimeMillis();
        long diff = now - lastORSCall;

        if (diff < ORS_INTERVAL_MS) {
            try {
                Thread.sleep(ORS_INTERVAL_MS - diff);
            } catch (InterruptedException ignored) {}
        }

        lastORSCall = System.currentTimeMillis();
        return callORS(cityA, cityB);
    }

    private Double callORS(String cityA, String cityB) {

        double[] c1 = cityRegistry.getCoordinates(cityA);
        double[] c2 = cityRegistry.getCoordinates(cityB);

        if (c1 == null || c2 == null) return null;

        try {
            String url = String.format(
                    "https://api.openrouteservice.org/v2/directions/driving-car?api_key=%s&start=%f,%f&end=%f,%f",
                    ORS_API_KEY,
                    c1[1], c1[0],
                    c2[1], c2[0]
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("❌ ORS API failed: HTTP " + code);
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            JSONObject json = new JSONObject(sb.toString());

            double meters = json
                    .getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("properties")
                    .getJSONArray("segments")
                    .getJSONObject(0)
                    .getDouble("distance");

            return meters / 1000.0;

        } catch (Exception e) {
            System.err.println("❌ ORS error: " + e.getMessage());
            return null;
        }
    }

    // -----------------------------------------------------------
    // GraphHopper Fallback
    // -----------------------------------------------------------

    private Double callGraphHopper(String cityA, String cityB) {

        double[] c1 = cityRegistry.getCoordinates(cityA);
        double[] c2 = cityRegistry.getCoordinates(cityB);

        if (c1 == null || c2 == null) return null;

        try {
            String url = String.format(
                    "https://graphhopper.com/api/1/route?point=%f,%f&point=%f,%f&vehicle=car&locale=en&key=%s",
                    c1[0], c1[1],
                    c2[0], c2[1],
                    GH_API_KEY
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("❌ GraphHopper failed: HTTP " + code);
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            JSONObject json = new JSONObject(sb.toString());

            if (!json.has("paths")) return null;

            double meters = json
                    .getJSONArray("paths")
                    .getJSONObject(0)
                    .getDouble("distance");

            return meters / 1000.0;

        } catch (Exception e) {
            System.err.println("❌ GraphHopper error: " + e.getMessage());
            return null;
        }
    }
}

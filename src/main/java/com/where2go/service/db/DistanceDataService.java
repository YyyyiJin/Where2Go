package com.where2go.service.db;

import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class DistanceDataService {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/data/app.db";

    // Sentinel bad value
    private static final double BAD_DISTANCE_DB = 999.0;

    public DistanceDataService() {
        initTable();
    }

    private void initTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = """
                CREATE TABLE IF NOT EXISTS driving_distance (
                    cityA TEXT NOT NULL,
                    cityB TEXT NOT NULL,
                    distanceKm REAL,
                    PRIMARY KEY (cityA, cityB)
                );
            """;

            conn.createStatement().execute(sql);
            System.out.println("📦 [DB] driving_distance table ready");

        } catch (Exception e) {
            System.err.println("❌ Failed to init driving_distance: " + e.getMessage());
        }
    }

    // Local validation — no dependency on DistanceService
    private boolean needsRecalc(Double dist) {
        return dist == null || dist == BAD_DISTANCE_DB || dist < 0 || dist > 200;
    }

    public Double loadDistance(String a, String b) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = """
                SELECT distanceKm FROM driving_distance
                WHERE cityA = ? AND cityB = ?;
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, a);
            ps.setString(2, b);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Double dist = rs.getDouble(1);

                if (needsRecalc(dist)) {
                    System.out.println("⚠️ Bad distance (" + dist + ") for " + a + " -> " + b);
                    return null;
                }

                return dist;
            }

        } catch (Exception e) {
            System.err.println("❌ DB loadDistance failed: " + e.getMessage());
        }

        return null;
    }


    public void saveDistance(String a, String b, double km) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = """
                INSERT OR REPLACE INTO driving_distance (cityA, cityB, distanceKm)
                VALUES (?, ?, ?);
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, a);
            ps.setString(2, b);
            ps.setDouble(3, km);
            ps.executeUpdate();

        } catch (Exception e) {
            System.err.println("❌ DB saveDistance failed: " + e.getMessage());
        }
    }
}

package com.where2go.config;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class CityRegistry {

    private final Map<String, double[]> coords = new LinkedHashMap<>();

    public CityRegistry() {

        // 🌆 Greater Seattle Core
        coords.put("Seattle", new double[]{47.6062, -122.3321});
        coords.put("Bellevue", new double[]{47.6101, -122.2015});
        coords.put("Redmond", new double[]{47.67399, -122.12151});
        coords.put("Kirkland", new double[]{47.6769, -122.2060});
        coords.put("Issaquah", new double[]{47.5301, -122.0326});
        coords.put("Sammamish", new double[]{47.6163, -122.0356});
        coords.put("Mercer Island", new double[]{47.5707, -122.2220});

        // 🏙️ North of Seattle
        coords.put("Shoreline", new double[]{47.7557, -122.3415});
        coords.put("Edmonds", new double[]{47.8107, -122.3773});
        coords.put("Lynnwood", new double[]{47.8209, -122.3151});
        coords.put("Mukilteo", new double[]{47.9474, -122.3030});
        coords.put("Everett", new double[]{47.9780, -122.2021});
        coords.put("Mill Creek", new double[]{47.8601, -122.2043});
        coords.put("Bothell", new double[]{47.7601, -122.2054});
        coords.put("Woodinville", new double[]{47.7543, -122.1635});

        // 🛣️ South of Seattle
        coords.put("Renton", new double[]{47.4829, -122.2171});
        coords.put("Kent", new double[]{47.3809, -122.2348});
        coords.put("Auburn", new double[]{47.3073, -122.2285});
        coords.put("Federal Way", new double[]{47.3133, -122.3126});
        coords.put("Des Moines", new double[]{47.4017, -122.3243});
        coords.put("Burien", new double[]{47.4704, -122.3468});
    }

    public List<String> getAllCities() {
        return new ArrayList<>(coords.keySet());
    }

    public double[] getCoords(String city) {
        return coords.getOrDefault(city, new double[]{0, 0});
    }

    public double[] getCoordinates(String city) {
        return getCoords(city);
    }
}

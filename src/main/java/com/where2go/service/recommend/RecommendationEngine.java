package com.where2go.service.recommend;

import com.where2go.config.CityRegistry;
import com.where2go.config.RecommendationWeights;
import com.where2go.service.geo.DistanceService;
import com.where2go.service.metrics.MetricsCalculator;
import org.springframework.stereotype.Service;
import weka.classifiers.trees.J48;
import weka.core.*;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class RecommendationEngine {

    private final MetricsCalculator calc;
    private final DistanceService distanceService;
    private final CityRegistry cityRegistry;
    private final RecommendationWeights weights;
    private final ScoreFunctions scoreFunctions;

    private J48 classifier;
    private Instances modelStructure;

    public RecommendationEngine(
            MetricsCalculator calc,
            DistanceService distanceService,
            CityRegistry cityRegistry,
            RecommendationWeights weights,
            ScoreFunctions scoreFunctions) {

        this.calc = calc;
        this.distanceService = distanceService;
        this.cityRegistry = cityRegistry;
        this.weights = weights;
        this.scoreFunctions = scoreFunctions;
    }

    // ========== ML initiate ==========
    @PostConstruct
    public void initModel() {
        try {
            ArrayList<Attribute> attrs = new ArrayList<>();
            attrs.add(new Attribute("temperature"));
            attrs.add(new Attribute("rainProbability"));

            ArrayList<String> labels = new ArrayList<>();
            labels.add("Recommended");
            labels.add("NotRecommended");
            attrs.add(new Attribute("label", labels));

            Instances training = new Instances("WeatherRec", attrs, 10);
            training.setClassIndex(2);

            // Good samples
            addTraining(training, 20, 0, "Recommended");
            addTraining(training, 22, 10, "Recommended");
            addTraining(training, 25, 5, "Recommended");
            addTraining(training, 18, 0, "Recommended");

            // Bad samples
            addTraining(training, 5, 20, "NotRecommended");
            addTraining(training, 10, 90, "NotRecommended");
            addTraining(training, 15, 80, "NotRecommended");
            addTraining(training, 35, 0, "NotRecommended");
            addTraining(training, 20, 60, "NotRecommended");

            classifier = new J48();
            classifier.buildClassifier(training);

            modelStructure = new Instances(training, 0);

            System.out.println("=== ML Model Initialized ===");
        } catch (Exception e) {
            System.err.println("❌ ML Init Error: " + e.getMessage());
        }
    }

    private void addTraining(Instances data, double t, double r, String label) {
        DenseInstance inst = new DenseInstance(3);
        inst.setDataset(data);
        inst.setValue(0, t);
        inst.setValue(1, r);
        inst.setValue(2, label);
        data.add(inst);
    }

    private double mlScore(double t, double r) {
        if (classifier == null)
            return 0.0;

        try {
            DenseInstance inst = new DenseInstance(3);
            inst.setDataset(modelStructure);
            inst.setValue(0, t);
            inst.setValue(1, r);

            double idx = classifier.classifyInstance(inst);
            String label = modelStructure.classAttribute().value((int) idx);
            return label.equals("Recommended") ? 1.0 : 0.0;

        } catch (Exception e) {
            System.err.println("❌ ML scoring error: " + e.getMessage());
            return 0.0;
        }
    }

    // ========== core logic ==========
    public List<Map<String, Object>> recommendCities(
            List<String> cities,
            String date,
            List<Integer> hours,
            String originCity) {

        List<Map<String, Object>> list = calc.computeMetrics(cities, date, hours);

        for (Map<String, Object> m : list) {
            String city = (String) m.get("city");

            double temp = ((Number) m.getOrDefault("avgTemperature", 0)).doubleValue();
            double rain = ((Number) m.getOrDefault("avgRainProbability", 0)).doubleValue();
            double wind = ((Number) m.getOrDefault("avgWindSpeed", 0)).doubleValue();


            double distKm = distanceService.distanceKm(originCity, city);
            double distMiles = distKm * 0.621371;

            // --- Score using external scoreFunctions ---
            double rScore = scoreFunctions.rainScore(rain);
            double wScore = scoreFunctions.windScore(wind);
            double tScore = scoreFunctions.temperatureScore(temp);
            double dScore = scoreFunctions.distanceScore(distKm);

            double finalScore = weights.rainWeight() * rScore +
                    weights.windWeight() * wScore +
                    weights.temperatureWeight() * tScore +
                    weights.distanceWeight() * dScore;

            m.put("rainScore", rScore);
            m.put("windScore", wScore);
            m.put("tempScore", tScore);
            m.put("distanceKm", distKm);
            m.put("distanceMiles", distMiles);
            m.put("finalScore", finalScore);

            double[] coords = cityRegistry.getCoords(city);
            m.put("latitude", coords[0]);
            m.put("longitude", coords[1]);
        }

        list.sort((a, b) -> Double.compare(
                (double) b.get("finalScore"),
                (double) a.get("finalScore")));

        return list;
    }
}

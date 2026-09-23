package com.where2go.config;

import org.springframework.stereotype.Component;

@Component
public class RecommendationWeights {

    public double rainWeight() {
        return 0.50;
    }

    public double windWeight() {
        return 0.15;
    }

    public double temperatureWeight() {
        return 0.20;
    }

    public double distanceWeight() {
        return 0.15;
    }
}

package com.where2go.service.recommend;

import org.springframework.stereotype.Component;

@Component
public class ScoreFunctions {
    // public double calculateFeelsLike(double tempCelsius, double windMph) {

    //     double windKmh = windMph * 1.60934;

    //     if (windKmh < 5) {
    //         return tempCelsius;
    //     }

    //     return 13.12 +
    //             0.6215 * tempCelsius -
    //             11.37 * Math.pow(windKmh, 0.16) +
    //             0.3965 * tempCelsius * Math.pow(windKmh, 0.16);
    // }

    public double rainScore(double rain) {
        if (rain <= 10)
            return 1.0;
        if (rain <= 30)
            return 0.8;
        if (rain <= 50)
            return 0.5;
        if (rain <= 70)
            return 0.3;
        return 0.1;
    }

    public double windScore(double wind) {
        if (wind <= 5)
            return 1.0;
        if (wind <= 10)
            return 0.8;
        return 0.5;
    }

    // the temperature is setted for winter
    public double temperatureScore(double temp) {
        if (temp >= 10 && temp <= 30)
            return 1.0;
        if (temp >= 8 && temp < 10)
            return 0.7;
        if (temp >= 5 && temp < 8)
            return 0.5;
        if (temp >= 2 && temp < 5)
            return 0.3;
        return 0.1;
    }

    public double distanceScore(double km) {
        if (km <= 10)
            return 1.0;
        if (km <= 20)
            return 0.8;
        if (km <= 30)
            return 0.5;
        if (km <= 40)
            return 0.3;
        return 0.1;
    }
}

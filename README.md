# Where2Go

Weather-aware destination recommendation application built with Java, Spring Boot, SQLite, and the Tomorrow.io API.

## Summary

Where2Go helps users choose where to go based on hourly weather conditions and travel distance.

Instead of manually comparing forecasts across multiple locations, the application retrieves weather data for Seattle-area cities, stores the results locally, calculates weather and distance metrics, and ranks destinations using a configurable recommendation engine.

- Backend: Java 17 + Spring Boot
- API: REST
- Database: SQLite
- External API: Tomorrow.io Weather API
- Frontend: HTML + CSS + JavaScript
- Recommendation: Weighted scoring engine

## Key Features

- Fetch hourly weather forecasts for 20+ Seattle-area cities
- Compare precipitation, temperature, and wind conditions across destinations
- Rank destinations using weather conditions and driving distance
- Store and query weather data using SQLite
- Support configurable recommendation weights
- Refresh weather data asynchronously
- Provide hourly weather metrics through REST APIs
- Interactive frontend for selecting date, hour, origin, and destinations

## Architecture

The application follows a layered architecture that separates API integration, data access, metrics calculation, recommendation logic, and presentation.

```text
                 ┌─────────────────────┐
                 │      Frontend       │
                 │ HTML / CSS / JS     │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │   REST Controller   │
                 │ WeatherController   │
                 └──────────┬──────────┘
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
      Weather Service   Metrics Layer   Recommendation
             │          Calculator         Engine
             │                              │
       ┌─────┴─────┐                        │
       ▼           ▼                        │
 Tomorrow.io     SQLite              Weighted Scoring
 Weather API    Database
```

### Main Components

- `WeatherController` — exposes REST endpoints for weather data, metrics, and recommendations
- `TomorrowFetcher` — retrieves forecast data from Tomorrow.io
- `WeatherDataService` — manages persisted weather records
- `DistanceDataService` — manages stored driving-distance data
- `DistanceService` — handles geographic distance-related logic
- `MetricsCalculator` — calculates weather metrics used by the application
- `RecommendationEngine` — ranks destinations based on multiple factors
- `ScoreFunctions` — contains individual scoring functions
- `RecommendationWeights` — centralizes configurable scoring weights
- `CityRegistry` — maintains supported cities and geographic coordinates
- `WeatherParser` — parses weather API responses into application data

## Recommendation Engine

Destinations are ranked using a weighted scoring model:

```text
Recommendation Score =
    Rain Score        × 0.50
  + Temperature Score × 0.20
  + Wind Score        × 0.15
  + Distance Score    × 0.15
```

The current configuration prioritizes precipitation while still considering temperature, wind, and travel distance.

Keeping the weights separate from the scoring logic makes the recommendation strategy configurable without changing the core engine.

## REST Endpoints

Base path:

```text
/api/weather
```

### Supported Cities

```http
GET /api/weather/cities
```

Returns the locations currently supported by the application.

### Available Dates

```http
GET /api/weather/dates
```

Returns dates with available weather data.

### Weather Metrics

```http
GET /api/weather/metrics
```

Calculates weather metrics based on selected cities, date, hours, and origin.

### Destination Recommendations

```http
GET /api/weather/recommend
```

Returns destinations ranked by the recommendation engine.

### Hourly Weather

```http
GET /api/weather/hourly
```

Returns weather data for a selected hour.

### Refresh Weather Data

```http
POST /api/weather/refresh-all
```

Refreshes weather information for supported locations.

## Supported Locations

Where2Go currently focuses on the Seattle metropolitan area, including:

Seattle, Bellevue, Redmond, Kirkland, Issaquah, Sammamish, Mercer Island, Shoreline, Edmonds, Lynnwood, Mukilteo, Everett, Mill Creek, Bothell, Woodinville, Renton, Kent, Auburn, Federal Way, Des Moines, and Burien.

## Repository Layout

```text
src/main/java/com/where2go/
├── config/
│   ├── CityRegistry.java
│   └── RecommendationWeights.java
├── controller/
│   └── WeatherController.java
├── model/
│   └── WeatherRecord.java
├── service/
│   ├── api/
│   │   └── TomorrowFetcher.java
│   ├── db/
│   │   ├── DistanceDataService.java
│   │   └── WeatherDataService.java
│   ├── geo/
│   │   └── DistanceService.java
│   ├── metrics/
│   │   └── MetricsCalculator.java
│   └── recommend/
│       ├── RecommendationEngine.java
│       └── ScoreFunctions.java
└── util/
    └── WeatherParser.java

src/main/resources/static/
├── index.html
├── styles.css
└── js/
```

## Requirements

- Java 17+
- Maven
- Tomorrow.io API key

## Quick Start

### 1. Clone the repository

```bash
git clone git@github.com:YyyyyJin/Where2Go.git
cd Where2Go
```

### 2. Configure the weather API

Create a Tomorrow.io API key and configure it locally.

API keys and other credentials should not be committed to the repository.

### 3. Run the application

```bash
mvn spring-boot:run
```

After the application starts, open:

```text
http://localhost:8080
```

## Engineering Considerations

- External weather retrieval is separated from recommendation logic, allowing the weather provider to evolve independently from the scoring engine.
- Recommendation weights are centralized in configuration instead of being scattered throughout business logic.
- Weather data is persisted locally to avoid unnecessary external API requests and support repeated analysis.
- Invalid or unexpected distance values are validated before being used by the recommendation engine.
- API, database, metrics, and recommendation responsibilities are separated into dedicated service components.
- API credentials are excluded from version control.

## Future Improvements

- User profiles with personalized recommendation preferences
- Activity-aware recommendations such as parks, beaches, hiking, and indoor activities
- Dynamic recommendation weights based on user preferences
- Real-time travel-time integration
- Caching for external API requests
- Automated unit and integration tests
- Dockerized deployment
- CI/CD pipeline
- Cloud deployment
- AI agent for context-aware destination and activity recommendations
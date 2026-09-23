// app.js
import { CitySelector } from "./city-selector.js";
import { HourSelector } from "./hour-selector.js";
import { DateSelector } from "./date-selector.js";
import { DataTable } from "./data-table.js";
import { WeatherMap } from "./weather-map.js";
import { WeatherCharts } from "./weather-charts.js";
import { Utils } from "./utils.js";

class AppController {

  static async init() {
    console.log("🎉 App initializing...");

    const cities = await fetch("/api/weather/cities").then(r => r.json());

    const originSelect = Utils.qs(".origin-picker");
    originSelect.innerHTML = "";
    cities.forEach(city => {
      const opt = Utils.create("option", city);
      opt.value = city;
      originSelect.appendChild(opt);
    });
    originSelect.value = cities[0];
    this.originSelect = originSelect;

    this.citySelector = new CitySelector("city-picker", cities);
    this.hourSelector = new HourSelector("hour-picker");
    this.dateSelector = new DateSelector("date-picker");

    this.table = new DataTable("data-table");
    this.map = new WeatherMap("map");
    this.charts = new WeatherCharts(".weather-type-grid");

    this.citySelector.render();
    this.hourSelector.render();
    this.dateSelector.loadDates();

    Utils.qs(".start-btn").onclick = () => this.loadData();

    const refreshBtn = Utils.qs(".refresh-btn");
    if (refreshBtn) {
      refreshBtn.onclick = () => this.refreshWeather();
    }
  }

  static async refreshWeather() {
    console.log("🔄 Refreshing all weather data...");

    const res = await fetch("/api/weather/refresh-all").then(r => r.json());
    alert(res.message || "Weather data refreshed!");

    await this.dateSelector.loadDates();
  }

  static async loadData() {
    const cities = this.citySelector.getSelected();
    const hours = this.hourSelector.getSelected();
    const date = this.dateSelector.getValue();
    const origin = this.originSelect.value;

    if (cities.length === 0) {
      alert("Please select at least one city!");
      return;
    }
    if (hours.length === 0) {
      alert("Please select at least one hour!");
      return;
    }

    const citiesParam = cities.join(",");
    const hoursParam = hours.join(",");

    const metricsUrl =
      `/api/weather/metrics?cities=${citiesParam}&date=${date}&hours=${hoursParam}&origin=${origin}`;

    console.log("📡 Request /metrics:", metricsUrl);
    const metrics = await fetch(metricsUrl).then(r => r.json());
    console.log("📦 Response /metrics:", metrics);

    if (!metrics || metrics.length === 0) {
      Utils.qs(".recommend").innerText =
        "🌈 Top Recommended City: (No Data)";
      this.table.update([]);
      this.map.update([], origin);
      this.charts.update([]);
      return;
    }

    this.table.update(metrics);
    this.map.update(metrics, origin);

    const best = metrics[0];
    Utils.qs(".recommend").innerText =
      `🌈 Top Recommended City: ${best.city}`;

    const hourlyUrl =
      `/api/weather/hourly?cities=${citiesParam}&date=${date}&hours=${hoursParam}`;
    console.log("📡 Request /hourly:", hourlyUrl);
    const hourlyData = await fetch(hourlyUrl).then(r => r.json());
    console.log("📦 Response /hourly:", hourlyData);

    const scoreMap = {};
    metrics.forEach(m => {
      scoreMap[m.city] = m.finalScore ?? 0;
    });

    hourlyData.forEach(h => {
      h.finalScore = scoreMap[h.city] ?? 0;
    });

    console.log("🔁 hourlyData + finalScore merged:", hourlyData);

    this.charts.update(hourlyData);
  }
}

window.addEventListener("load", () => AppController.init());

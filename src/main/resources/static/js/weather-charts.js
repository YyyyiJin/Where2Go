export class WeatherCharts {

  constructor(typeGridSelector) {
    this.typeGridEl = document.querySelector(typeGridSelector);
  }

  update(hourlyData) {
    if (!hourlyData || hourlyData.length === 0) {
      this.renderTypeGrid([]);
      return;
    }
    this.renderTypeGrid(hourlyData);
  }

  renderTypeGrid(data) {
    if (!this.typeGridEl) return;

    this.typeGridEl.innerHTML = "";

    if (!data.length) {
      this.typeGridEl.textContent = "No hourly data.";
      return;
    }

    const cities = [...new Set(data.map(d => d.city))];
    const cityScores = {};

    data.forEach(d => {
      cityScores[d.city] = d.finalScore ?? 0;
    });

    cities.sort((a, b) => (cityScores[b] - cityScores[a]));

    const hours = [...new Set(data.map(d => d.hourLabel))].sort();

    const table = document.createElement("table");
    table.className = "weather-type-table";

    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    const emptyTh = document.createElement("th");
    emptyTh.textContent = "City / Hour";
    headerRow.appendChild(emptyTh);

    hours.forEach(h => {
      const th = document.createElement("th");
      th.textContent = h;
      headerRow.appendChild(th);
    });

    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");

    cities.forEach(city => {
      const tr = document.createElement("tr");

      const cityTd = document.createElement("td");
      cityTd.textContent = city;
      tr.appendChild(cityTd);

      hours.forEach(h => {
        const td = document.createElement("td");

        const rec = data.find(d =>
          d.city === city && d.hourLabel === h
        );

        if (rec) {
          const { emoji, label } = this.weatherEmoji(rec.weatherType);
          td.textContent = emoji;
          td.title = label;
        } else {
          td.textContent = "-";
        }

        tr.appendChild(td);
      });

      tbody.appendChild(tr);
    });

    table.appendChild(tbody);

    this.typeGridEl.appendChild(table);
  }

  weatherEmoji(type) {
    const t = (type || "").toLowerCase();

    if (t.includes("sun")) return { emoji: "☀️", label: "Sunny" };
    if (t.includes("cloud")) return { emoji: "☁️", label: "Cloudy" };
    if (t.includes("drizzle")) return { emoji: "🌦️", label: "Drizzle" };
    if (t.includes("rain")) return { emoji: "🌧️", label: "Rain" };
    if (t.includes("snow")) return { emoji: "❄️", label: "Snow" };
    if (t.includes("fog")) return { emoji: "🌫️", label: "Fog" };
    if (t.includes("ice")) return { emoji: "🧊", label: "Ice" };
    if (t.includes("thunder")) return { emoji: "⛈️", label: "Thunderstorm" };
    if (t.includes("wind")) return { emoji: "🌬️", label: "Windy" };

    return { emoji: "❔", label: type || "Unknown" };
  }

  renderRainChart() {
  }
}

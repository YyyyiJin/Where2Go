// weather-map.js
export class WeatherMap {
  constructor(mapClassName) {
    const mapElement = document.querySelector(`.${mapClassName}`);
    this.map = L.map(mapElement).setView([47.60, -122.33], 9);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 18
    }).addTo(this.map);

    this.markers = {};
  }

  update(metrics, origin) {
    Object.values(this.markers).forEach(m => this.map.removeLayer(m));
    this.markers = {};

    if (!metrics || metrics.length === 0) return;

    const bestCity = metrics[0]?.city;

    metrics.forEach(entry => {
      const lat = entry.latitude;
      const lon = entry.longitude;
      if (lat == null || lon == null) return;

      const isOrigin = entry.city === origin;
      const isBest = entry.city === bestCity;

      let className = "city-label";  
      if (isOrigin && isBest) className = "double-label";
      else if (isOrigin) className = "origin-label";
      else if (isBest) className = "best-label";

      let prefix = "";
      if (isOrigin) prefix += "📍 ";
      if (isBest) prefix += "⭐ ";

      const labelText = `${prefix}${entry.city}`;

      const labelIcon = L.divIcon({
        className: className,
        html: `<div class="city-text">${labelText}</div>`,
        iconSize: [120, 24],
        iconAnchor: [60, 12]
      });

      const marker = L.marker([lat, lon], { icon: labelIcon }).addTo(this.map);

      const popupHtml = `
        <b>${entry.city}</b><br>
        ⭐ Score: ${(entry.finalScore * 100).toFixed(0)}%<br>
        🌡 Temp: ${entry.avgTemperature?.toFixed(1)}°C<br>
        🌧 Rain: ${entry.avgRainProbability?.toFixed(0)}%<br>
        💨 Wind: ${entry.avgWindSpeed?.toFixed(1) ?? "-"} mph<br>
        📏 Distance: ${entry.distanceMiles?.toFixed(1)} miles
      `;

      marker.bindPopup(popupHtml);
      this.markers[entry.city] = marker;
    });

    const coords = metrics
      .filter(m => m.latitude != null && m.longitude != null)
      .map(m => [m.latitude, m.longitude]);

    if (coords.length) {
      this.map.fitBounds(coords, { padding: [50, 50] });
    }
  }
}

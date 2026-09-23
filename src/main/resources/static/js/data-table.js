export class DataTable {
  constructor(tableId) {
    this.table = document.querySelector(`.${tableId} tbody`);
  }

  update(metrics) {
    this.table.innerHTML = "";

    metrics.forEach(entry => {
      const tr = document.createElement("tr");

      tr.innerHTML = `
        <td>${entry.city}</td>
        <td>${entry.avgTemperature?.toFixed(1) ?? "-"}</td>
        <td>${entry.avgRainProbability?.toFixed(0) ?? "-"}</td>
        <td>${entry.avgWindSpeed?.toFixed(1) ?? "-"}</td>
        <td>${entry.distanceMiles?.toFixed(1) ?? "-"}</td>
        <td>${(entry.finalScore * 100).toFixed(0)}%</td>
      `;

      this.table.appendChild(tr);
    });
  }
}
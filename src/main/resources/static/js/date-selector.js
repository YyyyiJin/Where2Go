import { Utils } from "./utils.js";

export class DateSelector {

  constructor(containerId) {
    this.select = Utils.qs(`.${containerId}`);
  }

  async loadDates() {
    const dates = await fetch("/api/weather/dates").then(r => r.json());

    console.log("📅 Loaded DB dates:", dates);

    this.select.innerHTML = "";

    if (!dates || dates.length === 0) {
      const opt = Utils.create("option", "No Data");
      opt.value = "";
      this.select.appendChild(opt);
      return;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const allowedDates = [];
    for (let i = 0; i < 4; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() + i);
      allowedDates.push(d.toISOString().substring(0, 10));
    }

    console.log("📅 Allowed date range:", allowedDates);

    const finalDates = dates.filter(d => allowedDates.includes(d));

    console.log("📅 Final selectable dates:", finalDates);

    if (finalDates.length === 0) {
      const opt = Utils.create("option", "No Valid Date");
      opt.value = "";
      this.select.appendChild(opt);
      return;
    }

    finalDates.sort();

    finalDates.forEach(d => {
      const opt = Utils.create("option", d);
      opt.value = d;
      this.select.appendChild(opt);
    });

    this.select.value = finalDates[0];
  }

  getValue() {
    return this.select.value;
  }
}

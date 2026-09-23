import { Utils } from "./utils.js";

export class OriginSelector {
  constructor(selectId, cities) {
    this.select = Utils.qs(`#${selectId}`);
    this.cities = cities;
  }

  render() {
    this.select.innerHTML = "";
    this.cities.forEach(city => {
      const opt = Utils.create("option", city);
      opt.value = city;
      this.select.appendChild(opt);
    });
  }

  getValue() {
    return this.select.value;
  }
}

import { Utils } from "./utils.js";

export class HourSelector {

  constructor(containerId) {
    this.listEl = Utils.qs(`.${containerId}`);       
    this.dropdown = this.listEl.parentElement;  
    this.selectedEl = this.dropdown.querySelector(".dropdown-selected");

    this.hours = Array.from({ length: 12 }, (_, i) =>
      `${String(8 + i).padStart(2, "0")}:00`
    );
  }

  render() {
    if (!this.listEl) return;

    this.listEl.innerHTML = "";

    const controls = Utils.create("div", "", "dropdown-item");
    controls.style.display = "flex";
    controls.style.justifyContent = "space-between";
    controls.style.fontWeight = "600";
    controls.innerHTML = `
      <span class="select-all" style="cursor:pointer;color:#1a73e8;">Select All</span>
      <span class="clear-all" style="cursor:pointer;color:#e53935;">Clear</span>
    `;
    this.listEl.appendChild(controls);

    this.hours.forEach(h => {
      const item = Utils.create("div", "", "dropdown-item");
      item.innerHTML = `
        <input type="checkbox" value="${h}">
        <label>${h}</label>
      `;
      this.listEl.appendChild(item);
    });

    this.selectedEl.onclick = () => {
      this.dropdown.classList.toggle("open");
    };

    document.addEventListener("click", (e) => {
      if (!this.dropdown.contains(e.target)) {
        this.dropdown.classList.remove("open");
      }
    });

    this.listEl.addEventListener("change", () => this.updateLabel());

    controls.querySelector(".select-all").onclick = (e) => {
      e.stopPropagation();
      this.setAll(true);
    };

    controls.querySelector(".clear-all").onclick = (e) => {
      e.stopPropagation();
      this.setAll(false);
    };

    this.setAll(true);
  }

  setAll(checked) {
    this.listEl.querySelectorAll("input[type=checkbox]")
      .forEach(cb => cb.checked = checked);
    this.updateLabel();
  }

  updateLabel() {
    const selected = this.getSelected();
    this.selectedEl.textContent =
      selected.length ? selected.join(", ") : "Select Hours";
  }

  getSelected() {
    return [...this.listEl.querySelectorAll("input[type=checkbox]:checked")]
      .map(cb => cb.value);
  }
}

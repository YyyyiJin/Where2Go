export class Utils {
  static qs(sel) {
    return document.querySelector(sel);
  }

  static create(tag, text = "") {
    const el = document.createElement(tag);
    if (text) el.textContent = text;
    return el;
  }
}

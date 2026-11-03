(function () {
  "use strict";

  const CART_KEY = "partpulseDemoCart";

  async function request(path, options) {
    const response = await fetch(path, {
      ...options,
      headers: {
        Accept: "application/json",
        ...(options && options.body ? { "Content-Type": "application/json" } : {}),
        ...(options && options.headers ? options.headers : {})
      }
    });

    let data;
    try {
      data = await response.json();
    } catch (error) {
      if (!response.ok) {
        throw new Error("Request failed (" + response.status + ")");
      }
      throw new Error("The server returned an invalid JSON response.");
    }

    if (!response.ok) {
      throw new Error(data && data.error
        ? data.error
        : "Request failed (" + response.status + ")");
    }
    return data;
  }

  function readDemoCart() {
    try {
      const cart = JSON.parse(localStorage.getItem(CART_KEY) || "[]");
      return Array.isArray(cart) ? cart : [];
    } catch (error) {
      console.error("Unable to read the browser demo cart:", error);
      return [];
    }
  }

  function writeDemoCart(cart) {
    if (!Array.isArray(cart)) {
      throw new TypeError("Cart must be an array.");
    }
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
    window.dispatchEvent(new CustomEvent("partpulse:cartchange", {
      detail: { count: cart.reduce((total, item) => total + Number(item.quantity || 0), 0) }
    }));
  }

  function addDemoCartItem(item) {
    if (!item || !item.partId || !Number.isInteger(Number(item.quantity)) ||
        Number(item.quantity) < 1) {
      throw new TypeError("A product and positive quantity are required.");
    }

    const cart = readDemoCart();
    const existing = cart.find(entry => String(entry.partId) === String(item.partId));
    if (existing) {
      existing.quantity = Number(existing.quantity || 0) + Number(item.quantity);
    } else {
      cart.push({
        partId: String(item.partId),
        name: String(item.name || "Spare part"),
        model: String(item.model || ""),
        quantity: Number(item.quantity),
        unitPrice: Number.isFinite(Number(item.unitPrice)) ? Number(item.unitPrice) : null
      });
    }
    writeDemoCart(cart);
    return cart;
  }

  window.API = Object.freeze({
    get(path) {
      return request(path);
    },
    post(path, data) {
      return request(path, {
        method: "POST",
        body: JSON.stringify(data)
      });
    },
    delete(path) {
      return request(path, { method: "DELETE" });
    }
  });

  window.PartPulseDemo = Object.freeze({
    cartKey: CART_KEY,
    readCart: readDemoCart,
    writeCart: writeDemoCart,
    addCartItem: addDemoCartItem
  });

  function addDemoNotice() {
    const header = document.querySelector("header");
    if (!header || document.getElementById("partpulse-demo-notice")) return;

    const style = document.createElement("style");
    style.textContent = [
      "#partpulse-demo-notice{padding:9px 16px;background:#fff7d6;color:#713f12;border-bottom:1px solid #f5d77b;font:600 12px/1.5 Inter,'Segoe UI',Arial,sans-serif;text-align:center}",
      "#partpulse-demo-notice strong{font-weight:900}",
      ".partpulse-cart-link{white-space:nowrap}"
    ].join("");
    document.head.appendChild(style);

    const notice = document.createElement("div");
    notice.id = "partpulse-demo-notice";
    notice.setAttribute("role", "status");
    notice.innerHTML = "<strong>DEMO MODE:</strong> Catalogue, stock, prices, and orders shown here are illustrative; no database changes, real orders, or payments are made.";
    header.insertAdjacentElement("afterend", notice);

    const navigation = document.querySelector("header nav");
    if (navigation && !navigation.querySelector('a[href="cart.html"]')) {
      const link = document.createElement("a");
      link.href = "cart.html";
      link.className = "partpulse-cart-link";
      link.textContent = "Cart";
      navigation.appendChild(link);
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", addDemoNotice, { once: true });
  } else {
    addDemoNotice();
  }
})();

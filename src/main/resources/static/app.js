const state = { username: "", password: "", products: [] };
const byId = (id) => document.getElementById(id);

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
}

function authHeader() {
  return "Basic " + btoa(state.username + ":" + state.password);
}

async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      Authorization: authHeader(),
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.headers || {})
    }
  });
  if (!response.ok) {
    const error = new Error(response.status === 404 ? "Not found or access denied." :
      response.status === 403 ? "Your account does not have permission for this action." :
      response.status === 401 ? "Invalid username or password." :
      "Request failed (" + response.status + ").");
    error.status = response.status;
    throw error;
  }
  if (response.status === 204) return null;
  return response.json();
}

function setConnected(connected) {
  byId("session-status").textContent = connected ? "CONNECTED" : "SIGNED OUT";
  byId("session-status").classList.toggle("connected", connected);
  byId("metrics").hidden = !connected;
  byId("workspace").hidden = !connected;
  byId("login-button").textContent = connected ? "Disconnect" : "Connect →";
}

function renderProducts(products) {
  state.products = products;
  byId("product-count").textContent = products.length;
  byId("products").innerHTML = products.map((product) => `
    <div class="product-row">
      <div class="product-icon">▦</div>
      <div class="product-info"><strong>${escapeHtml(product.name)}</strong><small>SKU-${String(product.id).padStart(4, "0")}</small></div>
      <div class="product-price"><strong>₹${(product.priceInPaise / 100).toFixed(2)}</strong><small>${product.stock} in stock</small></div>
    </div>`).join("") || '<p class="muted">No products found.</p>';
}

async function loadProducts() {
  const products = await api("/api/products");
  renderProducts(products);
}

byId("login-button").addEventListener("click", async () => {
  if (state.username) {
    state.username = "";
    state.password = "";
    byId("password").value = "";
    byId("login-error").textContent = "";
    byId("products").innerHTML = '<p class="muted">Sign in to load inventory.</p>';
    byId("order-result").innerHTML = '<span class="muted">No order selected.</span>';
    byId("admin-form").hidden = true;
    setConnected(false);
    return;
  }
  state.username = byId("username").value.trim();
  state.password = byId("password").value;
  byId("login-error").textContent = "";
  try {
    await loadProducts();
    byId("admin-form").hidden = state.username !== "lab-admin";
    setConnected(true);
  } catch (error) {
    state.username = "";
    state.password = "";
    byId("login-error").textContent = error.message;
  }
});

byId("refresh-products").addEventListener("click", async () => {
  try { await loadProducts(); } catch (error) { byId("login-error").textContent = error.message; }
});

byId("lookup-order").addEventListener("submit", async (event) => {
  event.preventDefault();
  const id = encodeURIComponent(byId("order-id").value);
  try {
    const order = await api("/api/orders/" + id);
    byId("order-result").innerHTML = `
      <div class="order-success"><span class="check">✓</span><strong>Access granted</strong></div>
      <dl><dt>Order</dt><dd>#${escapeHtml(order.id)}</dd><dt>Owner</dt><dd>${escapeHtml(order.owner)}</dd><dt>Status</dt><dd>${escapeHtml(order.status)}</dd></dl>`;
  } catch (error) {
    byId("order-result").innerHTML = `<div class="order-denied"><strong>Access denied / not found</strong><p>${escapeHtml(error.message)}</p></div>`;
  }
});

byId("create-product").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const payload = {
    name: String(form.get("name")).trim(),
    priceInPaise: Number(form.get("priceInPaise")),
    stock: Number(form.get("stock"))
  };
  byId("product-error").textContent = "";
  try {
    await api("/api/products", { method: "POST", body: JSON.stringify(payload) });
    event.currentTarget.reset();
    await loadProducts();
  } catch (error) {
    byId("product-error").textContent = error.message;
  }
});

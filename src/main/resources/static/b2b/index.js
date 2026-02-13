// B2B partner page client script
let stripePubKey = null;
let stripe = null;

async function loadConfig() {
  const res = await fetch('/api/payment/config');
  const cfg = await res.json();
  stripePubKey = cfg.publishableKey;
  if (stripePubKey) stripe = Stripe(stripePubKey);
}

async function loadServices() {
  const res = await fetch('/api/external/services');
  if (!res.ok) throw new Error('Failed to load services: ' + res.status);
  const services = await res.json();
  const container = document.getElementById('services');
  container.innerHTML = '';
  services.forEach(s => {
    const col = document.createElement('div');
    col.className = 'col-md-4';
    col.innerHTML = '<div class="card p-3">' +
      '<h5>' + (s.service_name || '') + '</h5>' +
      '<p>' + (s.description || '') + '</p>' +
      '<div><strong>Price: $' + (s.price != null ? Number(s.price).toFixed(2) : '0.00') + '</strong></div>' +
      '<div class="form-check mt-2">' +
      '<input class="form-check-input" type="checkbox" value="' + (s.id || '') + '" id="svc-' + (s.id || '') + '">' +
      '<label class="form-check-label">Select for checkout</label>' +
      '</div>' +
      '</div>';
    container.appendChild(col);
  });
}

async function createSession() {
  const checked = Array.from(document.querySelectorAll('#services input[type=checkbox]:checked')).map(cb => parseInt(cb.value));
  if (checked.length === 0) { alert('Please select at least one service'); return; }

  const emailInput = document.getElementById('customerEmail');
  const email = emailInput ? (emailInput.value || '') : '';

  const resp = await fetch('/api/payment/create-session', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ serviceIds: checked, customerEmail: email })
  });
  const data = await resp.json();
  if (data.id) {
    if (!stripe) {
      alert('Stripe not initialized. Publishable key missing.');
      return;
    }
    const { error } = await stripe.redirectToCheckout({ sessionId: data.id });
    if (error) alert(error.message);
  } else {
    alert('Failed to create session');
  }
}

document.addEventListener('DOMContentLoaded', function(){
  const btn = document.getElementById('checkoutBtn');
  if (btn) btn.addEventListener('click', createSession);
  loadConfig().then(loadServices).catch(err => {
    const msg = document.createElement('div');
    msg.className = 'alert alert-danger';
    msg.textContent = 'Failed to load B2B services: ' + err.message;
    const container = document.querySelector('.container');
    if (container) container.prepend(msg);
  });
});

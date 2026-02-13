<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="container mt-4">
    <h2 class="text-primary">Partner API — B2B Service Browser</h2>
    <p class="text-muted">
        This page shows service categories, cleaning services and caregivers (via B2B REST endpoints).
    </p>

    <div class="mb-3">
      <label for="customerEmail" class="form-label">Customer Email (optional for checkout)</label>
      <input id="customerEmail" class="form-control" placeholder="client@example.com">
    </div>

    <div id="services" class="row gy-3"></div>

    <button id="checkoutBtn" class="btn btn-primary mt-3">Create Checkout Session</button>

    <!-- Caregiver Dashboard -->
    <hr/>
    <h3>Caregiver Dashboard</h3>
    <div class="row">
        <div class="col-md-6">
            <div class="card mb-3">
                <div class="card-header">Caregivers</div>
                <div class="card-body">
                    <ul id="caregiverList" class="list-group"></ul>
                </div>
            </div>
        </div>

        <div class="col-md-6">
            <div class="card mb-3">
                <div class="card-header">Active Bookings</div>
                <div class="card-body">
                    <table class="table table-sm" id="bookingTable">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Customer</th>
                                <th>Caregiver</th>
                                <th>Status</th>
                                <th>Checked-In</th>
                                <th>Checked-Out</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody></tbody>
                    </table>
                </div>
            </div>

            <div class="card">
                <div class="card-header">Live Activity</div>
                <div class="card-body" style="max-height:240px;overflow:auto;" id="activityLog"></div>
            </div>
        </div>
    </div>

</div>

<script>
// Existing partner-b2b script area (kept minimal) - production code may be in /static/b2b/index.js
async function fetchServices(){
    // placeholder — existing site scripts may replace this
}

// Caregiver dashboard code
async function fetchCaregivers(){
    try{
        const res = await fetch('${pageContext.request.contextPath}/b2b/api/caregivers');
        const list = await res.json();
        const ul = document.getElementById('caregiverList');
        ul.innerHTML = '';
        list.forEach(c => {
            const li = document.createElement('li');
            li.className = 'list-group-item d-flex justify-content-between align-items-center';
            const badgeClass = (c.availabilityStatus==='AVAILABLE') ? 'success' : 'secondary';
            li.innerHTML = `<div><strong>${c.name}</strong><br/><small>${c.qualifications||''}</small></div><span class="badge bg-${badgeClass}">${c.availabilityStatus||'UNKNOWN'}</span>`;
            ul.appendChild(li);
        });
    }catch(e){ console.warn(e); }
}

async function fetchBookings(){
    try{
        const res = await fetch('${pageContext.request.contextPath}/b2b/api/bookings');
        const list = await res.json();
        const tbody = document.querySelector('#bookingTable tbody');
        tbody.innerHTML = '';
        list.forEach(b => {
            const tr = document.createElement('tr');
            const status = b.availabilityStatus || 'PENDING';
            const caregiverName = b.name || (b.caregiver && b.caregiver.name) || '';
            const checkedIn = b.checkedInAt ? new Date(b.checkedInAt).toLocaleString() : '';
            const checkedOut = b.checkedOutAt ? new Date(b.checkedOutAt).toLocaleString() : '';
            tr.innerHTML = `<td>${b.id}</td><td>${b.customerName}</td><td>${caregiverName}</td><td class="booking-status">${status}</td>
                <td>${checkedIn}</td>
                <td>${checkedOut}</td>
                <td>
                    <button class="btn btn-sm btn-success me-1" data-id="${b.id}" data-action="checkin">Check-in</button>
                    <button class="btn btn-sm btn-secondary" data-id="${b.id}" data-action="checkout">Check-out</button>
                </td>`;
            tbody.appendChild(tr);
        });
    }catch(e){ console.warn(e); }
}

document.addEventListener('click', async function(e){
    const btn = e.target.closest('button[data-action]');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    const action = btn.getAttribute('data-action');
    try{
        const res = await fetch('${pageContext.request.contextPath}/b2b/api/' + action, { method: 'POST', headers: {'Content-Type':'application/x-www-form-urlencoded'}, body: 'booking_id=' + encodeURIComponent(id)});
        const data = await res.json();
        appendLog(`Action ${action} on booking ${id}: ${JSON.stringify(data)}`);
        await fetchBookings();
        await fetchCaregivers();
    }catch(err){ appendLog('Error: '+err.message) }
});

function appendLog(text){
    const el = document.getElementById('activityLog');
    const p = document.createElement('div');
    p.textContent = (new Date()).toLocaleTimeString() + ' - ' + text;
    el.prepend(p);
}

// SSE for live updates
(function(){
    const url = '${pageContext.request.contextPath}/b2b/stream';
    const evt = new EventSource(url);
    evt.addEventListener('status-update', function(e){
        try{
            const d = JSON.parse(e.data);
            appendLog('Status update: ' + (d.caregiver_name || '') + ' -> ' + (d.status||'') );
            document.querySelectorAll('#bookingTable tbody tr').forEach(tr => {
                if (tr.children[0].textContent === String(d.booking_id)) {
                    tr.querySelector('.booking-status').textContent = d.status;
                }
            });
        }catch(err){ console.error(err) }
    });
    evt.onopen = ()=> console.log('SSE connected');
    evt.onerror = ()=> console.log('SSE error');
})();

// initial load
fetchCaregivers();
fetchBookings();
setInterval(fetchBookings, 30000);
setInterval(fetchCaregivers, 30000);
</script>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
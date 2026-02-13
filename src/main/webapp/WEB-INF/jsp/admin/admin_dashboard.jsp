<%@ page contentType="text/html; charset=UTF-8" %>
<jsp:include page="../header.jsp" />

<%
String adminUser = (String) session.getAttribute("admin_username");
if (adminUser == null) {
    response.sendRedirect(request.getContextPath() + "/admin/admin_login.jsp");
    return;
}
%>

<h2 class="mb-3 text-primary">Admin Dashboard</h2>
<p class="text-muted">Welcome, <strong><%= adminUser %></strong>. Manage services, clients, and feedback.</p>

<div class="row g-3">
    <div class="col-md-3">
        <div class="card h-100 shadow-sm border-0">
            <div class="card-body">
                <h5 class="card-title text-primary">Manage Services</h5>
                <p class="card-text text-muted">Create, update, and delete care services.</p>
                <a href="<%= request.getContextPath() %>/admin/manage-services"
                   class="btn btn-outline-primary btn-sm">Go to Services</a>
            </div>
        </div>
    </div>

    <div class="col-md-3">
        <div class="card h-100 shadow-sm border-0">
            <div class="card-body">
                <h5 class="card-title text-primary">Manage Clients</h5>
                <p class="card-text text-muted">View and manage registered client information.</p>
                <a href="<%= request.getContextPath() %>/admin/manage-clients"
                   class="btn btn-outline-primary btn-sm">Go to Clients</a>
            </div>
        </div>
    </div>

    <div class="col-md-3">
        <div class="card h-100 shadow-sm border-0">
            <div class="card-body">
                <h5 class="card-title text-primary">View Feedback</h5>
                <p class="card-text text-muted">Review feedback submitted by clients and families.</p>
                <a href="<%= request.getContextPath() %>/admin/feedback"
                   class="btn btn-outline-primary btn-sm">View Feedback</a>
            </div>
        </div>
    </div>

    <div class="col-md-3">
        <div class="card h-100 shadow-sm border-0">
            <div class="card-body">
                <h5 class="card-title text-primary">Billing & Reports</h5>
                <p class="card-text text-muted">View bookings, payments, and client billing reports.</p>
                <a href="<%= request.getContextPath() %>/admin/manage-billings"
                   class="btn btn-outline-primary btn-sm">Manage Billings</a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../footer.jsp" />
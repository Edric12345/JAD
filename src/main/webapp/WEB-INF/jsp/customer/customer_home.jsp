<%@ page contentType="text/html; charset=UTF-8" %>
<jsp:include page="../header.jsp" />

<%
Integer cid = (Integer) session.getAttribute("customer_id");
String cname = (String) session.getAttribute("customer_name");

if (cid == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
}
%>

<h2 class="mb-3 text-primary">My Account</h2>
<p class="text-muted">Welcome back, <strong><%= cname %></strong>.</p>

<div class="row g-3">
    <div class="col-md-4">
        <div class="card h-100 shadow-sm border-0">
            <div class="card-body">
                <h5 class="card-title text-primary">Manage Profile</h5>
                <p class="card-text text-muted">Update your personal details and contact information.</p>
                <a href="<%= request.getContextPath() %>/customer/manage_profile.jsp"
                   class="btn btn-outline-primary btn-sm">Edit Profile</a>
            </div>
        </div>
    </div>

    <div class="col-md-4">
        <div class="card h-100 shadow-sm border-0">
            <div class="card-body">
                <h5 class="card-title text-primary">Browse Services</h5>
                <p class="card-text text-muted">View care categories and add services to your booking.</p>
                <a href="<%= request.getContextPath() %>/public/service_category.jsp"
                   class="btn btn-outline-primary btn-sm">View Services</a>
            </div>
        </div>
    </div>

    <div class="col-md-4">
        <div class="card h-100 shadow-sm border-0">
            <div class="card-body">
                <h5 class="card-title text-primary">Feedback</h5>
                <p class="card-text text-muted">Tell us how we are doing and what can be improved.</p>
                <a href="<%= request.getContextPath() %>/public/feedback.jsp"
                   class="btn btn-outline-primary btn-sm">Give Feedback</a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../footer.jsp" />

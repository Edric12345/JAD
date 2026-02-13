<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<h2 class="text-primary mb-3">Book This Service</h2>
<div class="card shadow-sm border-0 mb-4">
    <div class="card-body">
        <h4 class="card-title text-primary">${service.service_name}</h4>
        <p class="text-muted">${service.description}</p>
        <p class="fw-bold">Price: $${service.price}</p>
        <p class="card-note">We will confirm staff availability after you submit — please choose a preferred date (optional).</p>
    </div>
</div>

<c:if test="${not empty msg}">
    <div class="alert alert-success" data-sfx="success-on-load">${msg}</div>
    <% if (session.getAttribute("msg") != null) { session.removeAttribute("msg"); } %>
</c:if>
<c:if test="${not empty error}">
    <div class="alert alert-danger" data-sfx="error-on-load">${error}</div>
    <% if (session.getAttribute("error") != null) { session.removeAttribute("error"); } %>
</c:if>

<form method="post" action="<%= request.getContextPath() %>/customer/book-service/${service.id}" class="card p-4 shadow-sm bg-white mb-3">
    <div class="mb-3">
        <label class="form-label">Preferred Date (optional)</label>
        <input type="date" name="date" class="form-control" min="<%= java.time.LocalDate.now().toString() %>">
    </div>
    <button class="btn btn-primary btn-fullwidth">Confirm Booking</button>
</form>

<!-- Add to Cart as a POST button (same size as confirm) -->
<form method="post" action="<%= request.getContextPath() %>/customer/add-to-cart/${service.id}" class="mb-3">
    <button type="submit" class="btn btn-outline-primary btn-fullwidth">Add to Cart</button>
</form>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
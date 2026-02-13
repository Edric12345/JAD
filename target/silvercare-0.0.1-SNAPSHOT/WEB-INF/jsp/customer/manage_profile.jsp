<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-4">Manage My Profile</h2>

<%-- Display success message from Flash Attributes --%>
<c:if test="${not empty msg}">
    <div class="alert alert-success">${msg}</div>
</c:if>

<form method="post" action="/customer/update-profile" class="card p-4 shadow-sm bg-white">
    <div class="mb-3">
        <label class="form-label">Full Name</label>
        <input type="text" name="name" class="form-control"
               value="${customer.name}" required>
    </div>
    <div class="mb-3">
        <label class="form-label">Email</label>
        <input type="email" name="email" class="form-control"
               value="${customer.email}" required>
    </div>
    <div class="mb-3">
        <label class="form-label">Phone</label>
        <input type="text" name="phone" class="form-control"
               value="${customer.phone}">
    </div>
    <div class="mb-3">
        <label class="form-label">Address</label>
        <textarea name="address" class="form-control" rows="3">${customer.address}</textarea>
    </div>
    
    <%-- Hidden password field to ensure it isn't lost during the update --%>
    <input type="hidden" name="password" value="${customer.password}">

    <button class="btn btn-primary">Save Changes</button>
    <a href="/customer/customer-home" class="btn btn-outline-secondary">Cancel</a>
</form>

<jsp:include page="../footer.jsp" />


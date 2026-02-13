<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-3">Edit Client</h2>

<form method="post" action="${pageContext.request.contextPath}/admin/update-client/${client.id}" class="card p-4 shadow-sm bg-white">
    <div class="mb-3">
        <label class="form-label">Full Name</label>
        <input type="text" name="client_name" class="form-control" value="${client.name}" required>
    </div>
    <div class="mb-3">
        <label class="form-label">Email</label>
        <input type="email" name="email" class="form-control" value="${client.email}" required>
    </div>
    <div class="mb-3">
        <label class="form-label">Phone</label>
        <input type="text" name="phone" class="form-control" value="${client.phone}">
    </div>
    <div class="mb-3">
        <label class="form-label">Address</label>
        <textarea name="address" class="form-control" rows="2">${client.address}</textarea>
    </div>

    <h5>Health & Emergency</h5>
    <div class="mb-3">
        <label class="form-label">Care Preferences</label>
        <textarea name="carePreferences" class="form-control" rows="3">${client.carePreferences}</textarea>
    </div>
    <div class="mb-3">
        <label class="form-label">Medical Notes</label>
        <textarea name="medicalNotes" class="form-control" rows="3">${client.medicalNotes}</textarea>
    </div>
    <div class="mb-3">
        <label class="form-label">Emergency Contact</label>
        <input type="text" name="emergencyContact" class="form-control" value="${client.emergencyContact}">
    </div>

    <button class="btn btn-primary">Save</button>
    <a href="${pageContext.request.contextPath}/admin/manage-clients" class="btn btn-outline-secondary">Cancel</a>
</form>

<jsp:include page="../footer.jsp" />
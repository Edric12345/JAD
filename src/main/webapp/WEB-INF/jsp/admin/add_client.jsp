<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-4">Add Client</h2>

<form method="post" action="${pageContext.request.contextPath}/admin/add-client" class="card p-4 shadow-sm bg-white">
    <div class="row">
        <div class="col-md-6">
            <div class="mb-3">
                <label class="form-label">Name</label>
                <input type="text" name="name" class="form-control" required />
            </div>
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" name="email" class="form-control" required />
            </div>
            <div class="mb-3">
                <label class="form-label">Phone</label>
                <input type="text" name="phone" class="form-control" />
            </div>
            <div class="mb-3">
                <label class="form-label">Address</label>
                <textarea name="address" class="form-control" rows="2"></textarea>
            </div>
        </div>
        <div class="col-md-6">
            <div class="mb-3">
                <label class="form-label">Care Preferences</label>
                <textarea name="carePreferences" class="form-control" rows="3"></textarea>
            </div>
            <div class="mb-3">
                <label class="form-label">Medical Notes</label>
                <textarea name="medicalNotes" class="form-control" rows="3"></textarea>
            </div>
            <div class="mb-3">
                <label class="form-label">Emergency Contact</label>
                <input type="text" name="emergencyContact" class="form-control" />
            </div>
        </div>
    </div>
    <div class="mt-3">
        <button class="btn btn-primary">Create Client</button>
        <a href="${pageContext.request.contextPath}/admin/manage-clients" class="btn btn-outline-secondary">Cancel</a>
    </div>
</form>

<jsp:include page="../footer.jsp" />
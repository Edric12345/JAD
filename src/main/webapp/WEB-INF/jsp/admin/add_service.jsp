<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-3">Add New Service</h2>

<c:if test="${not empty msg}">
    <div class="alert alert-success">${msg}</div>
</c:if>

<form method="post" action="/admin/add-service" class="card p-4 shadow-sm bg-white" enctype="multipart/form-data">
    <div class="mb-3">
        <label class="form-label">Service Name</label>
        <input type="text" name="service_name" class="form-control" required>
    </div>

    <div class="mb-3">
        <label class="form-label">Description</label>
        <textarea name="description" class="form-control" rows="3" required></textarea>
    </div>

    <div class="mb-3">
        <label class="form-label">Price ($)</label>
        <input type="number" name="price" class="form-control" min="0" step="0.01" required>
    </div>
    
    <div class="mb-3">
        <label class="form-label">Image Upload</label>
        <input type="file" name="imageFile" accept="image/*" class="form-control">
        <div class="form-text">Uploaded images will be saved to the server and the image path will be set automatically.</div>
    </div>

    <div class="mb-3">
        <label class="form-label">Availability (slots)</label>
        <input type="number" name="availability" class="form-control" min="0" placeholder="e.g., 5">
    </div>

    <div class="mb-3">
        <label class="form-label">Category</label>
        <select name="category_id" class="form-select" required>
            <c:forEach items="${categories}" var="cat">
                <option value="${cat.id}">${cat.category_name}</option>
            </c:forEach>
        </select>
    </div>

    <button type="submit" class="btn btn-primary">Add Service</button>
    <a href="/admin/manage-services" class="btn btn-outline-secondary">Cancel</a>
</form>

<jsp:include page="../footer.jsp" />
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-3">Add New Service</h2>

<c:if test="${not empty msg}">
    <div class="alert alert-success">${msg}</div>
</c:if>

<form method="post" action="/admin/add-service" class="card p-4 shadow-sm bg-white">
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
        <label class="form-label">Image File Name (from /images/)</label>
        <input type="text" name="image_path" class="form-control" placeholder="/images/homecare.png">
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
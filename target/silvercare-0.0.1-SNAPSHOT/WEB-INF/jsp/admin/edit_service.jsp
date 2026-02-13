<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-3">Edit Service</h2>

<%-- Action points to the POST method in AdminController --%>
<form method="post" action="/admin/update-service/${service.id}" class="card p-4 shadow-sm bg-white">

    <div class="mb-3">
        <label class="form-label">Service Name</label>
        <input type="text" name="service_name" class="form-control"
               value="${service.service_name}" required>
    </div>

    <div class="mb-3">
        <label class="form-label">Description</label>
        <textarea name="description" class="form-control" rows="3" required>${service.description}</textarea>
    </div>

    <div class="mb-3">
        <label class="form-label">Price ($)</label>
        <input type="number" name="price" class="form-control"
               step="0.01" value="${service.price}" required>
    </div>
    
    <div class="mb-3">
        <label class="form-label">Image File Name</label>
        <input type="text" name="image_path" class="form-control" value="${service.image_path}">
    </div>

    <div class="mb-3">
        <label class="form-label">Category</label>
        <select name="category_id" class="form-select">
            <c:forEach items="${categories}" var="cat">
                <option value="${cat.id}" ${cat.id == service.category_id ? 'selected' : ''}>
                    ${cat.category_name}
                </option>
            </c:forEach>
        </select>
    </div>

    <button type="submit" class="btn btn-primary">Save Changes</button>
    <a href="/admin/manage-services" class="btn btn-outline-secondary">Cancel</a>
</form>

<jsp:include page="../footer.jsp" />

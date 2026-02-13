<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %> <%-- Add this tag library --%>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-4">Manage Services</h2>

<%-- Check for success/error messages sent via RedirectAttributes --%>
<c:if test="${not empty msg}">
    <div class="alert alert-success">${msg}</div>
</c:if>

<div class="mb-3">
    <a href="/admin/add-service" class="btn btn-primary">+ Add New Service</a>
</div>

<table class="table table-bordered table-striped shadow-sm bg-white">
    <thead class="table-primary">
        <tr>
            <th>Service Name</th>
            <th>Description</th>
            <th>Price ($)</th>
            <th style="width: 180px;">Actions</th>
        </tr>
    </thead>
    <tbody>
        <%-- Use c:forEach to loop through the list sent from Controller --%>
        <c:forEach items="${services}" var="s">
            <tr>
                <td>${s.service_name}</td>
                <td>${s.description}</td>
                <td>$${s.price}</td>
                <td>
                    <a class="btn btn-sm btn-outline-primary"
                       href="/admin/edit-service/${s.id}">
                        Edit
                    </a>
                    <a class="btn btn-sm btn-outline-danger"
                       href="/admin/delete-service/${s.id}"
                       onclick="return confirm('Are you sure you want to delete this service?');">
                        Delete
                    </a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="../footer.jsp" />
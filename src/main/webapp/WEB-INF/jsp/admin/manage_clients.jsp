<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-4">Manage Clients</h2>

<c:if test="${not empty msg}">
    <div class="alert alert-success">${msg}</div>
</c:if>
<c:if test="${not empty error}">
    <div class="alert alert-danger">${error}</div>
</c:if>

<div class="mb-3">
    <form class="row g-2" method="get" action="${pageContext.request.contextPath}/admin/manage-clients">
        <div class="col-auto">
            <input type="text" name="area" value="${areaQuery}" placeholder="Area (partial)" class="form-control form-control-sm" />
        </div>
        <div class="col-auto">
            <input type="text" name="careNeeds" value="${needsQuery}" placeholder="Care needs (keyword)" class="form-control form-control-sm" />
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-outline-secondary btn-sm">Filter</button>
            <a href="${pageContext.request.contextPath}/admin/manage-clients" class="btn btn-outline-secondary btn-sm">Clear</a>
        </div>
    </form>
</div>

<table class="table table-bordered table-striped shadow-sm bg-white">
    <thead class="table-primary">
        <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Area</th>
            <th>Care Needs</th>
            <th style="width:160px;">Actions</th>
        </tr>
    </thead>
    <tbody>
        <%-- Use c:forEach to loop through the 'clients' list from the controller --%>
        <c:forEach items="${clients}" var="client">
            <tr>
                <td>${client.name}</td>
                <td>${client.email}</td>
                <td>${client.phone}</td>
                <td>${client.address}</td>
                <td>${client.carePreferences}</td>
                <td>
                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/edit-client/${client.id}">Edit</a>
                    <a class="btn btn-sm btn-outline-danger" href="${pageContext.request.contextPath}/admin/delete-client/${client.id}" onclick="return confirm('Delete client?');">Delete</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="../footer.jsp" />
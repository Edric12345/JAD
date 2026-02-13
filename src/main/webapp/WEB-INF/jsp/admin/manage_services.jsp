<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-4">Manage Services</h2>

<!-- Flash messages (request-scoped or session-scoped) -->
<c:if test="${not empty requestScope.msg}">
    <div class="alert alert-success" data-sfx="success-on-load">${requestScope.msg}</div>
</c:if>
<c:if test="${not empty requestScope.error}">
    <div class="alert alert-danger" data-sfx="error-on-load">${requestScope.error}</div>
</c:if>

<c:if test="${not empty sessionScope.msg and empty requestScope.msg}">
    <div class="alert alert-success" data-sfx="success-on-load">${sessionScope.msg}</div>
    <c:remove var="msg" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.error and empty requestScope.error}">
    <div class="alert alert-danger" data-sfx="error-on-load">${sessionScope.error}</div>
    <c:remove var="error" scope="session" />
</c:if>

<c:if test="${not empty topRated}">
    <div class="mb-3">
        <h5>Top Rated Services</h5>
        <div class="list-group list-group-horizontal overflow-auto">
            <c:forEach var="row" items="${topRated}">
                <c:set var="svc" value="${row[0]}" />
                <c:set var="avg" value="${row[1]}" />
                <a class="list-group-item list-group-item-action" href="${pageContext.request.contextPath}/admin/edit-service/${svc.id}">
                    <strong>${svc.service_name}</strong>
                    <span class="text-muted"> - Avg: <fmt:formatNumber value="${avg}" minFractionDigits="2" maxFractionDigits="2"/></span>
                </a>
            </c:forEach>
        </div>
    </div>
</c:if>

<c:if test="${not empty lowRated}">
    <div class="mb-3">
        <h5>Lowest Rated Services</h5>
        <div class="list-group list-group-horizontal overflow-auto">
            <c:forEach var="row" items="${lowRated}">
                <c:set var="svc" value="${row[0]}" />
                <c:set var="avg" value="${row[1]}" />
                <a class="list-group-item list-group-item-action" href="${pageContext.request.contextPath}/admin/edit-service/${svc.id}">
                    <strong>${svc.service_name}</strong>
                    <span class="text-muted"> - Avg: <fmt:formatNumber value="${avg}" minFractionDigits="2" maxFractionDigits="2"/></span>
                </a>
            </c:forEach>
        </div>
    </div>
</c:if>

<div class="d-flex justify-content-between mb-3">
    <a href="${pageContext.request.contextPath}/admin/add-service" class="btn btn-primary">+ Add New Service</a>
    <div class="d-flex">
        <form class="d-flex me-3" method="get" action="${pageContext.request.contextPath}/admin/manage-services/search">
            <input type="text" name="q" value="${searchQuery}" placeholder="Search services..." class="form-control form-control-sm me-2" />
            <button class="btn btn-outline-secondary btn-sm">Search</button>
        </form>
        <form class="d-flex" method="get" action="${pageContext.request.contextPath}/admin/manage-services">
            <select name="filter" class="form-select form-select-sm me-2">
                <option value="" ${selectedFilter == null ? 'selected' : ''}>All Services</option>
                <option value="topRated" ${selectedFilter == 'topRated' ? 'selected' : ''}>Top Rated</option>
                <option value="lowRated" ${selectedFilter == 'lowRated' ? 'selected' : ''}>Lowest Rated</option>
                <option value="highDemand" ${selectedFilter == 'highDemand' ? 'selected' : ''}>High Demand</option>
            </select>
            <button class="btn btn-outline-primary btn-sm">Apply</button>
        </form>
    </div>
</div>

<table class="table table-bordered table-striped shadow-sm bg-white">
    <caption class="text-muted">Use the filters to view metrics like ratings or bookings.</caption>
    <thead class="table-primary">
        <tr>
            <th>Service Name</th>
            <th>Description</th>
            <th>Price ($)</th>
            <th>${metricLabel != null && metricLabel != '' ? metricLabel : 'Metric'}</th>
            <th style="width: 180px;">Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${services}" var="s">
            <tr>
                <td>${s.service_name}</td>
                <td>${s.description}</td>
                <td>$<fmt:formatNumber value="${s.price}" minFractionDigits="2"/></td>
                <td>
                    <c:choose>
                        <c:when test="${selectedFilter == 'topRated' || selectedFilter == 'lowRated'}">
                            <c:choose>
                                <c:when test="${ratingsMap[s.id] != null}">
                                    <fmt:formatNumber value="${ratingsMap[s.id]}" minFractionDigits="2" maxFractionDigits="2" />
                                </c:when>
                                <c:otherwise>N/A</c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:when test="${selectedFilter == 'highDemand'}">
                            <c:out value="${demandMap[s.id] != null ? demandMap[s.id] : 0}" />
                        </c:when>
                        <c:otherwise>
                            <c:choose>
                                <c:when test="${s.availability != null}">
                                    <c:out value="Slots: " />
                                    <c:out value="${s.availability}" />
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/edit-service/${s.id}">Edit</a>
                    <a class="btn btn-sm btn-outline-danger" href="${pageContext.request.contextPath}/admin/delete-service/${s.id}" onclick="return confirm('Are you sure you want to delete this service?');">Delete</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="../footer.jsp" />
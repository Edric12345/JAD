<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="container mt-4">
  <h2 class="text-primary">Service Reports</h2>

  <div class="row mb-3">
    <div class="col-md-6">
      <form class="d-flex" method="get" action="${pageContext.request.contextPath}/admin/reports/clients-by-service">
        <select name="serviceId" class="form-select me-2">
          <option value="">-- Select service to see clients --</option>
          <c:forEach items="${services}" var="s">
            <option value="${s.id}">${s.service_name}</option>
          </c:forEach>
        </select>
        <button class="btn btn-outline-secondary">Show Clients</button>
      </form>
    </div>
  </div>

  <div class="row">
    <div class="col-md-6">
      <h5>Top Rated Services</h5>
      <ul class="list-group mb-3">
        <c:forEach items="${topRated}" var="row">
          <c:set var="svc" value="${row[0]}" />
          <c:set var="avg" value="${row[1]}" />
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <div>
              <strong>${svc.service_name}</strong>
              <div class="text-muted small">${svc.description}</div>
            </div>
            <span class="badge bg-success">Avg: <fmt:formatNumber value="${avg}" minFractionDigits="2"/></span>
          </li>
        </c:forEach>
      </ul>

      <h5>Lowest Rated Services</h5>
      <ul class="list-group mb-3">
        <c:forEach items="${lowRated}" var="row">
          <c:set var="svc" value="${row[0]}" />
          <c:set var="avg" value="${row[1]}" />
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <div>
              <strong>${svc.service_name}</strong>
              <div class="text-muted small">${svc.description}</div>
            </div>
            <span class="badge bg-danger">Avg: <fmt:formatNumber value="${avg}" minFractionDigits="2"/></span>
          </li>
        </c:forEach>
      </ul>
    </div>

    <div class="col-md-6">
      <h5>High Demand Services (by bookings)</h5>
      <ul class="list-group mb-3">
        <c:forEach items="${highDemand}" var="row">
          <c:set var="svc" value="${row[0]}" />
          <c:set var="count" value="${row[1]}" />
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <div>
              <strong>${svc.service_name}</strong>
              <div class="text-muted small">${svc.description}</div>
            </div>
            <span class="badge bg-primary">Bookings: ${count}</span>
          </li>
        </c:forEach>
      </ul>

      <h5>Low Availability</h5>
      <ul class="list-group mb-3">
        <c:forEach items="${lowAvail}" var="s">
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <div>
              <strong>${s.service_name}</strong>
              <div class="text-muted small">${s.description}</div>
            </div>
            <span class="badge bg-warning">Slots: ${s.availability}</span>
          </li>
        </c:forEach>
      </ul>
    </div>
  </div>

</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
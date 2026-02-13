<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="container mt-4">
  <h2 class="text-primary">Sales / Billing Reports</h2>

  <form method="get" action="/admin/reports/sales" class="row g-2 mb-3">
    <div class="col-auto">
      <input type="date" name="from" value="${from}" class="form-control">
    </div>
    <div class="col-auto">
      <input type="date" name="to" value="${to}" class="form-control">
    </div>
    <div class="col-auto">
      <button class="btn btn-primary">Filter</button>
    </div>
  </form>

  <div class="row">
    <div class="col-md-8">
      <h5>Payments</h5>
      <table class="table table-sm table-striped">
        <thead>
          <tr><th>ID</th><th>Booking</th><th>Customer</th><th>Amount</th><th>Paid At</th></tr>
        </thead>
        <tbody>
          <c:forEach items="${payments}" var="p">
            <tr>
              <td>${p.id}</td>
              <td>${p.bookingId}</td>
              <td>${p.customer != null ? p.customer.name : 'N/A'}</td>
              <td>$<fmt:formatNumber value="${p.totalAmount != null ? p.totalAmount : p.amount}" minFractionDigits="2"/></td>
              <td>${p.paidAt}</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
    <div class="col-md-4">
      <h5>Top Clients by Spend</h5>
      <ul class="list-group">
        <c:forEach items="${topClients}" var="r">
          <c:set var="c" value="${r[0]}" />
          <c:set var="sum" value="${r[1]}" />
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <div>${c.name}</div>
            <div>$<fmt:formatNumber value="${sum}" minFractionDigits="2"/></div>
          </li>
        </c:forEach>
      </ul>
    </div>
  </div>

</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
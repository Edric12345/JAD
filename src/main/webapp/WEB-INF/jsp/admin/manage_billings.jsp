<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../header.jsp" />

<div class="container mt-4">
    <h2 class="text-primary">Billing & Reports</h2>

    <div class="card mb-3 p-3">
        <form class="row g-2 align-items-end" method="get" action="${pageContext.request.contextPath}/admin/manage-billings">
            <div class="col-auto">
                <label class="form-label">From (YYYY-MM-DD)</label>
                <input type="date" name="from" value="${from}" class="form-control" />
            </div>
            <div class="col-auto">
                <label class="form-label">To (YYYY-MM-DD)</label>
                <input type="date" name="to" value="${to}" class="form-control" />
            </div>
            <div class="col-auto">
                <label class="form-label">Month (YYYY-MM)</label>
                <input type="month" name="month" value="${month}" class="form-control" />
            </div>
            <div class="col-auto">
                <label class="form-label">Service</label>
                <select name="serviceId" class="form-select">
                    <option value="">--any--</option>
                    <c:forEach items="${services}" var="s">
                        <option value="${s.id}">
                            <c:if test="${serviceId == s.id}"> 
                                <c:out value="${s.service_name}" />
                                <c:out value=""/> <!-- preserve content -->
                            </c:if>
                            <c:if test="${serviceId != s.id}">
                                <c:out value="${s.service_name}" />
                            </c:if>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-auto">
                <button class="btn btn-primary">Run</button>
            </div>
        </form>
    </div>

    <div class="row">
        <div class="col-md-8">
            <h5>Bookings</h5>
            <table class="table table-sm table-striped">
                <thead>
                    <tr><th>ID</th><th>Customer</th><th>Date</th><th>Status</th><th>Value</th></tr>
                </thead>
                <tbody>
                    <c:forEach items="${bookings}" var="b">
                        <tr>
                            <td>${b.id}</td>
                            <td>${b.customer.name}</td>
                            <td>${b.formattedBookingDate}</td>
                            <td>${b.status}</td>
                            <td>
                                <c:set var="sum" value="0" />
                                <c:forEach items="${b.details}" var="d">
                                    <c:set var="sum" value="${sum + d.subtotal}" />
                                </c:forEach>
                                $<fmt:formatNumber value="${sum}" minFractionDigits="2" />
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <div class="col-md-4">
            <h5>Top Clients by Spend</h5>
            <ul class="list-group mb-3">
                <c:forEach items="${topClients}" var="row">
                    <c:set var="cst" value="${row[0]}" />
                    <c:set var="amt" value="${row[1]}" />
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${cst.name} <div class="text-muted small">${cst.email}</div></div>
                        <div>$<fmt:formatNumber value="${amt}" minFractionDigits="2" /></div>
                    </li>
                </c:forEach>
            </ul>

            <h5>Payments in Period</h5>
            <ul class="list-group small">
                <c:forEach items="${payments}" var="p">
                    <li class="list-group-item">
                        ${p.customer.name} - $<fmt:formatNumber value="${p.totalAmount}" minFractionDigits="2" /> <div class="text-muted small">${p.paidAt}</div>
                    </li>
                </c:forEach>
            </ul>

            <h5 class="mt-3">Clients who booked selected service</h5>
            <ul class="list-group small">
                <c:forEach items="${clientsByService}" var="cl">
                    <li class="list-group-item">${cl.name} - ${cl.email} - ${cl.phone}</li>
                </c:forEach>
            </ul>
        </div>
    </div>

</div>

<jsp:include page="../footer.jsp" />
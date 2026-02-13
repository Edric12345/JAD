<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../header.jsp" />

<div class="container mt-5">
    <h2 class="text-primary">My Bookings</h2>

    <div class="card mb-3 p-3">
        <form method="get" action="" class="row g-2 align-items-end">
            <div class="col-auto">
                <label class="form-label">Status</label>
                <select name="status" class="form-select">
                    <option value="" <c:if test="${empty filter_status}">selected</c:if>>All</option>
                    <option value="PENDING" <c:if test="${filter_status == 'PENDING'}">selected</c:if>>Pending</option>
                    <option value="PAID" <c:if test="${filter_status == 'PAID'}">selected</c:if>>Paid</option>
                    <option value="CANCELLED" <c:if test="${filter_status == 'CANCELLED'}">selected</c:if>>Cancelled</option>
                </select>
            </div>
            <div class="col-auto">
                <label class="form-label">From</label>
                <input type="date" name="from" class="form-control" value="${filter_from}" min="${pageContext.request.contextPath}">
            </div>
            <div class="col-auto">
                <label class="form-label">To</label>
                <input type="date" name="to" class="form-control" value="${filter_to}">
            </div>
            <div class="col-auto">
                <button class="btn btn-primary">Filter</button>
                <a href="${pageContext.request.contextPath}/customer/my-bookings" class="btn btn-outline-secondary">Reset</a>
            </div>
        </form>
    </div>

    <c:if test="${empty bookings}">
        <div class="alert alert-info mt-3">You have no bookings yet. Browse services to add bookings.</div>
        <a href="${pageContext.request.contextPath}/customer/booking" class="btn btn-outline-primary">Browse Services</a>
    </c:if>

    <c:if test="${not empty bookings}">
        <table class="table table-hover mt-3">
            <thead>
                <tr>
                    <th>Booking #</th>
                    <th>Date</th>
                    <th>Services</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="b" items="${bookings}">
                    <tr>
                        <td>${b.id}</td>
                        <td>${b.formattedBookingDate}</td>
                        <td>
                            <ul class="list-unstyled mb-0">
                                <c:forEach var="d" items="${b.details}">
                                    <li>
                                        <strong>${d.service.service_name}</strong>
                                        - $<c:out value="${d.priceAtBooking}"/>
                                    </li>
                                </c:forEach>
                            </ul>
                        </td>
                        <td>${b.status}</td>
                        <td>
                            <c:choose>
                                <c:when test="${b.status == 'PENDING'}">
                                    <a href="${pageContext.request.contextPath}/customer/cancel-booking/${b.id}" class="btn btn-danger btn-sm" onclick="return confirm('Cancel booking #${b.id}?');">Cancel</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">No actions</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:if>

</div>

<jsp:include page="../footer.jsp" />
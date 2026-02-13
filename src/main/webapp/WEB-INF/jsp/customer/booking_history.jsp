<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../header.jsp" />

<div class="container mt-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="text-primary">Booking History</h2>
    <a href="${pageContext.request.contextPath}/customer/profile" class="btn btn-outline-secondary">Edit Profile</a>
  </div>

  <c:if test="${not empty msg}">
    <div class="alert alert-success">${msg}</div>
  </c:if>

  <c:choose>
    <c:when test="${not empty bookings}">
      <div class="list-group">
        <c:forEach items="${bookings}" var="b">
          <div class="list-group-item list-group-item-action mb-3 shadow-sm">
            <div class="d-flex w-100 justify-content-between">
              <div>
                <h5 class="mb-1">Booking #${b.id}</h5>
                <small class="text-muted">Created: ${b.formattedBookingDate}</small>
              </div>
              <div class="text-end">
                <c:choose>
                  <c:when test="${b.status == 'PAID'}">
                    <span class="badge bg-success">PAID</span>
                  </c:when>
                  <c:when test="${b.status == 'PENDING'}">
                    <span class="badge bg-warning text-dark">PENDING</span>
                  </c:when>
                  <c:when test="${b.status == 'CANCELLED'}">
                    <span class="badge bg-secondary">CANCELLED</span>
                  </c:when>
                  <c:otherwise>
                    <span class="badge bg-info text-dark">${b.status}</span>
                  </c:otherwise>
                </c:choose>
                <div class="mt-2">Booking ID: <strong>${b.id}</strong></div>
              </div>
            </div>

            <hr/>

            <div class="row">
              <div class="col-md-8">
                <h6>Services / Items</h6>
                <ul class="list-group mb-2">
                  <c:forEach items="${b.details}" var="d">
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                      <div>
                        <strong>${d.service.service_name}</strong>
                        <div class="text-muted small">${d.service.description}</div>
                      </div>
                      <div class="text-end">
                        <div>$${d.priceAtBooking}</div>
                        <div class="text-muted small">Qty: ${d.quantity != null ? d.quantity : 1}</div>
                      </div>
                    </li>
                  </c:forEach>
                </ul>

                <c:if test="${not empty b.notes}">
                  <div class="mt-2"><strong>Notes:</strong> ${b.notes}</div>
                </c:if>

              </div>

              <div class="col-md-4">
                <h6>Summary</h6>
                <table class="table table-borderless small">
                  <tr>
                    <td>Subtotal</td>
                    <td class="text-end">
                      <c:set var="subtotal" value="0" />
                      <c:forEach items="${b.details}" var="dd">
                        <c:set var="subtotal" value="${subtotal + dd.subtotal}" />
                      </c:forEach>
                      $${subtotal}
                    </td>
                  </tr>
                  <tr>
                    <td>GST (9%)</td>
                    <td class="text-end">$<fmt:formatNumber value="${(subtotal * 0.09)}" type="number" minFractionDigits="2"/></td>
                  </tr>
                  <tr class="fw-bold">
                    <td>Total</td>
                    <td class="text-end">$<fmt:formatNumber value="${(subtotal + (subtotal * 0.09))}" type="number" minFractionDigits="2"/></td>
                  </tr>
                </table>

                <div class="mt-3">
                  <c:if test="${b.status == 'PENDING'}">
                    <form action="${pageContext.request.contextPath}/customer/booking/pay/${b.id}" method="post">
                      <button type="submit" class="btn btn-success w-100 mb-2">Pay Now</button>
                    </form>
                    <a href="${pageContext.request.contextPath}/customer/cancel-booking/${b.id}" class="btn btn-outline-danger w-100" onclick="return confirm('Cancel booking #${b.id}?');">Cancel Booking</a>
                  </c:if>

                  <c:if test="${b.status == 'PAID'}">
                    <a href="${pageContext.request.contextPath}/customer/receipt/${b.id}" class="btn btn-outline-primary w-100">View Receipt</a>
                  </c:if>
                </div>

              </div>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:when>
    <c:otherwise>
      <div class="alert alert-info">You have no past bookings.</div>
    </c:otherwise>
  </c:choose>

</div>

<jsp:include page="../footer.jsp" />
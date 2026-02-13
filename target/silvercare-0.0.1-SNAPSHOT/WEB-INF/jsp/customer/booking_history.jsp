<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-4">My Bookings</h2>

<%-- Check if msg was passed from the cancel-booking redirect --%>
<c:if test="${not empty msg}">
    <div class="alert alert-success">${msg}</div>
</c:if>

<c:choose>
    <c:when test="${not empty bookings}">
        <c:forEach items="${bookings}" var="b">
            <div class="card mb-4 shadow-sm border-0">
                <div class="card-body">
                    <h5 class="text-primary">Booking #${b.id}</h5>
                    <p class="text-muted">Date: ${b.booking_date}</p>
                    <p>Status: <strong>${b.status}</strong></p>

                    <hr>
                    <h6>Services:</h6>

                    <%-- Calculate total on the fly or via a model method --%>
                    <c:set var="totalPrice" value="0" />
                    
                    <c:forEach items="${b.details}" var="detail">
                        <p>
                            <strong>${detail.service.service_name}</strong><br>
                            Qty: ${detail.quantity}<br>
                            Subtotal: $${detail.subtotal}
                        </p>
                        <c:set var="totalPrice" value="${totalPrice + detail.subtotal}" />
                    </c:forEach>

                    <p class="fw-bold">Total Amount: $${totalPrice}</p>

                    <c:if test="${b.status == 'PENDING'}">
                        <a href="/customer/cancel-booking/${b.id}"
                           class="btn btn-danger btn-sm"
                           onclick="return confirm('Confirm cancellation?');">
                           Cancel Booking
                        </a>
                    </c:if>
                </div>
            </div>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <div class="alert alert-info">
            You have no bookings yet.
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="../footer.jsp" />

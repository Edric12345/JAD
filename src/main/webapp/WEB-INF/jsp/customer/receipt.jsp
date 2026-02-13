<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="container mt-4">
    <h2 class="text-primary">Receipt</h2>
    <p class="small text-muted">Payment Ref: ${payment.transactionRef}</p>

    <div class="card mt-3">
        <div class="card-body">
            <h5>Booking Details</h5>
            <p>Booking ID: ${booking.id}</p>
            <p>Customer: ${booking.customer.name}</p>
            <p>Total: $<fmt:formatNumber value="${payment.totalAmount}" minFractionDigits="2" /></p>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
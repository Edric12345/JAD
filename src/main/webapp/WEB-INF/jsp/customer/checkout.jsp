<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../header.jsp" />

<div class="container mt-5">
    <h2 class="text-primary">Booking Summary</h2>
    <table class="table mt-4">
        <thead>
            <tr>
                <th>Service</th>
                <th>Price</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${selectedServices}" var="s">
                <tr>
                    <td>${s.service_name}</td>
                    <td>$<fmt:formatNumber value="${s.price}" minFractionDigits="2"/></td>
                </tr>
            </c:forEach>
        </tbody>
        <tfoot>
            <tr>
                <td><strong>Subtotal</strong></td>
                <td>$<fmt:formatNumber value="${subtotal}" minFractionDigits="2"/></td>
            </tr>
            <tr>
                <td><strong>GST (9%)</strong></td>
                <td>$<fmt:formatNumber value="${gstAmount}" minFractionDigits="2"/></td>
            </tr>
            <tr class="table-primary">
                <td><strong>Total Amount</strong></td>
                <td><strong>$<fmt:formatNumber value="${total}" minFractionDigits="2"/></strong></td>
            </tr>
        </tfoot>
    </table>

    <div class="d-grid gap-2">
        <!-- Prepaid payment option -->
        <form action="<%= request.getContextPath() %>/customer/checkout/pay" method="post">
            <input type="hidden" name="paymentMethod" value="online" />
            <button type="submit" class="btn btn-success btn-lg">Pay Now (Prepaid)</button>
        </form>

        <!-- Existing confirm-only option (creates pending booking) -->
        <form action="<%= request.getContextPath() %>/customer/confirm-booking" method="post">
            <button type="submit" class="btn btn-outline-primary btn-lg">Confirm and Book (Pay Later)</button>
        </form>
    </div>

</div>

<jsp:include page="../footer.jsp" />
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-4">Client Feedback</h2>

<table class="table table-bordered table-striped shadow-sm bg-white">
    <thead class="table-primary">
        <tr>
            <th>Customer</th>
            <th>Service</th>
            <th>Rating</th>
            <th>Comments</th>
            <th>Submitted At</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${feedbacks}" var="f">
        <tr>
            <td>${f.customer.name}</td>
            <td>${f.service.service_name}</td>
            <td>${f.rating}/5</td>
            <td>${f.comments}</td>
            <td>${f.created_at}</td>
        </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="../footer.jsp" />
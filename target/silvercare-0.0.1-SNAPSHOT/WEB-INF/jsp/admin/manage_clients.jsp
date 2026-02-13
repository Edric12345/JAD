<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %> <%-- Add this tag --%>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-4">Manage Clients</h2>

<table class="table table-bordered table-striped shadow-sm bg-white">
    <thead class="table-primary">
        <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
        </tr>
    </thead>
    <tbody>
        <%-- Use c:forEach to loop through the 'clients' list from the controller --%>
        <c:forEach items="${clients}" var="client">
            <tr>
                <td>${client.name}</td>
                <td>${client.email}</td>
                <td>${client.phone}</td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="../footer.jsp" />

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />
<div class="container mt-5">
    <h2 class="text-danger">Something went wrong</h2>
    <p class="text-muted">We're sorry, but an unexpected error occurred. Please try again or contact support.</p>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-warning">${errorMessage}</div>
    </c:if>
    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return Home</a>
</div>
<jsp:include page="/WEB-INF/jsp/footer.jsp" />
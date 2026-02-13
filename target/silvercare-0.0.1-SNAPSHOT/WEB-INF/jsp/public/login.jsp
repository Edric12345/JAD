<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Note the "../" to go up one level to find the header if it's in the base jsp folder --%>
<jsp:include page="../header.jsp" />

<div class="row justify-content-center mt-5 mb-5">
    <div class="col-md-6 col-lg-4">
        <div class="card shadow-sm border-0 p-3">
            <div class="card-body">
                <h3 class="card-title text-center mb-3 text-primary">Customer Login</h3>
                
                <%-- Matches 'error' flash attribute from Controller --%>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>
                
                <%-- Success message (e.g., after registration) --%>
                <c:if test="${not empty msg}">
                    <div class="alert alert-success">${msg}</div>
                </c:if>

                <form method="post" action="/login">
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" name="email" class="form-control"
                               value="${savedEmail}" placeholder="name@example.com" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input type="password" name="password" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">Login</button>
                </form>
                
                <p class="mt-3 text-center">
                    New here? <a href="/register" class="text-decoration-none">Register as a client</a>
                </p>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../footer.jsp" />


<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card shadow-sm border-0">
            <div class="card-body">
                <h3 class="text-center text-primary mb-4">Admin Login</h3>
                
                <%-- Use Spring's model attribute 'message' --%>
                <c:if test="${not empty message}">
                    <div class="alert alert-danger">${message}</div>
                </c:if>

                <form method="post" action="/admin/login">
                    <div class="mb-3">
                        <label class="form-label">Admin Username</label>
                        <input type="text" class="form-control" name="username" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input type="password" class="form-control" name="password" required>
                    </div>
                    <button class="btn btn-primary w-100">Login</button>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../footer.jsp" />


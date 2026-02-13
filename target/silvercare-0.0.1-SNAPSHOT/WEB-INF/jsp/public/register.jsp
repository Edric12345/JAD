<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<div class="row justify-content-center mt-5 mb-5">
    <div class="col-md-6 col-lg-5">
        <div class="card shadow-sm border-0 p-3">
            <div class="card-body">
                <h3 class="card-title text-center mb-3 text-primary">Client Registration</h3>
                
                <c:if test="${not empty error}">
                    <div class="alert alert-danger">${error}</div>
                </c:if>

                <form method="post" action="/register">
                    <div class="mb-3">
                        <label class="form-label">Full Name</label>
                        <input type="text" name="name" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" name="email" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input type="password" name="password" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Phone</label>
                        <input type="text" name="phone" class="form-control" placeholder="Optional">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Address</label>
                        <textarea name="address" class="form-control" rows="3" placeholder="Optional"></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">Register</button>
                </form>
                
                <p class="mt-3 text-center">
                    Already have an account? <a href="/login" class="text-decoration-none">Log in here</a>
                </p>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../footer.jsp" />
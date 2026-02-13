<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<style>
    /* ... (Keep your existing CSS styles here) ... */
</style>

<h2 class="mb-4 text-primary fw-bold">Care Service Categories</h2>

<div class="p-5 mb-5 hero-box shadow-sm">
    <div class="row align-items-center">
        <div class="col-md-6">
            <h1 class="display-6 fw-bold text-primary">Our Care Services</h1>
            <p class="fs-5 text-muted mt-3">
                At SilverCare, we provide compassionate and professional support designed 
                to improve the wellbeing of seniors and families.
            </p>
        </div>
        <div class="col-md-6 text-center">
            <img src="${pageContext.request.contextPath}/images/servicecat.png" class="hero-img shadow-sm">
        </div>
    </div>
</div>

<div class="row g-4 mb-5">
    <c:forEach items="${categories}" var="cat">
        <div class="col-md-4">
            <div class="card shadow-sm border-0 category-card h-100 p-3">
                <div class="category-icon">
                    <i class="fa-solid fa-hand-holding-heart"></i>
                </div>
                <h5 class="fw-bold text-primary">${cat.category_name}</h5>
                <p class="text-muted">${cat.description}</p>
                
                <%-- Updated link to use the controller route --%>
                <a href="/public/services/${cat.id}" class="btn btn-primary w-100 mt-auto">
                    View Services
                </a>
            </div>
        </div>
    </c:forEach>
</div>

<jsp:include page="../footer.jsp" />


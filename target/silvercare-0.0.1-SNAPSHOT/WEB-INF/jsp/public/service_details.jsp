<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-2">${category.category_name}</h2>
<p class="text-muted mb-4">${category.description}</p>

<div class="row g-3">
    <c:forEach items="${services}" var="s">
        <div class="col-md-4">
            <div class="card h-100 shadow-sm border-0">
                <%-- Handle Image Fallback --%>
                <c:set var="imgSrc" value="${not empty s.image_path ? s.image_path : 'images/default.png'}" />
                <img src="${pageContext.request.contextPath}/${imgSrc}" class="service-img mb-2">

                <div class="card-body">
                    <h5 class="card-title text-primary">${s.service_name}</h5>
                    <p class="text-muted mb-2">${s.description}</p>
                    <p class="fw-bold mb-0">$${s.price}</p>

                    <c:choose>
                        <c:when test="${not empty sessionScope.customer_id}">
                            <%-- Logged in view: Link to the booking controller route --%>
                            <a href="/customer/book-service/${s.id}" class="btn btn-primary btn-sm mt-2">
                                Book Appointment
                            </a>
                        </c:when>
                        <c:otherwise>
                            <%-- Guest view --%>
                            <a href="/login" class="btn btn-outline-secondary btn-sm mt-2">
                                Login to Book
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<jsp:include page="../footer.jsp" />

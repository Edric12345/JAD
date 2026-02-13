<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="container mt-5">
    <c:if test="${not empty promotion}">
        <div class="card shadow-sm">
            <div class="row g-0">
                <div class="col-md-4">
                    <c:if test="${not empty promotion.image_path}">
                        <img src="${promotion.image_path}" class="img-fluid rounded-start" alt="${promotion.title}" />
                    </c:if>
                </div>
                <div class="col-md-8">
                    <div class="card-body">
                        <h3 class="card-title">${promotion.title}</h3>
                        <p class="text-muted small">
                            <c:if test="${not empty startsAt}">Starts: ${startsAt}</c:if>
                            <c:if test="${not empty endsAt}"> <span>• Ends: ${endsAt}</span></c:if>
                        </p>
                        <p class="card-text">${promotion.summary}</p>
                        <div class="mt-3">${promotion.body}</div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
        <div class="card shadow-sm border-0">
            <div class="card-body">
                <h3 class="card-title text-primary mb-3">Share Your Feedback</h3>

                <c:if test="${not empty msgFb}">
                    <div class="alert alert-success">${msgFb}</div>
                </c:if>

                <form method="post" action="/customer/feedback">
                    <div class="mb-3">
                        <label class="form-label">Service</label>
                        <select name="service_id" class="form-select" required>
                            <c:forEach items="${services}" var="s">
                                <option value="${s.id}">${s.service_name}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Rating (1–5)</label>
                        <select name="rating" class="form-select">
                            <option value="5">5 - Excellent</option>
                            <option value="4">4 - Good</option>
                            <option value="3">3 - Average</option>
                            <option value="2">2 - Poor</option>
                            <option value="1">1 - Very Poor</option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Comments</label>
                        <textarea name="comments" class="form-control" rows="4" required></textarea>
                    </div>

                    <button type="submit" class="btn btn-primary w-100">Submit Feedback</button>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../footer.jsp" />

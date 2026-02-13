<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
        <div class="card shadow-sm border-0 mb-4">
            <div class="card-body">
                <h3 class="card-title text-primary mb-3">Share Your Feedback</h3>

                <c:if test="${not empty msgFb}">
                    <div class="alert alert-success" data-sfx="success-on-load">${msgFb}</div>
                    <% if (session.getAttribute("msgFb") != null) { session.removeAttribute("msgFb"); } %>
                </c:if>
                <c:if test="${not empty errorFb}">
                    <div class="alert alert-danger" data-sfx="error-on-load">${errorFb}</div>
                    <% if (session.getAttribute("errorFb") != null) { session.removeAttribute("errorFb"); } %>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/customer/feedback">
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

                    <button type="submit" class="btn btn-primary btn-fullwidth">Submit Feedback</button>
                </form>
            </div>
        </div>

        <div class="card shadow-sm border-0">
            <div class="card-body">
                <h4 class="card-title">Recent Feedback</h4>
                <c:if test="${empty feedbacks}">
                    <p class="text-muted">No feedback yet.</p>
                </c:if>
                <c:forEach items="${feedbacks}" var="f">
                    <div class="d-flex mb-3">
                        <div style="width:80px; flex-shrink:0;">
                            <c:choose>
                                <c:when test="${not empty f.service.image_path}">
                                    <img src="${f.service.image_path}" alt="${f.service.service_name}" class="img-fluid rounded" />
                                </c:when>
                                <c:otherwise>
                                    <div class="bg-light rounded" style="width:80px;height:60px;display:flex;align-items:center;justify-content:center;color:#888">No Image</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="flex-grow-1 ms-3">
                            <div class="d-flex justify-content-between">
                                <div>
                                    <strong>${f.service.service_name}</strong>
                                    <div class="text-muted small">by 
                                        <c:choose>
                                            <c:when test="${f.customer != null}">
                                                <c:out value="${f.customer.name}" />
                                            </c:when>
                                            <c:otherwise>Guest</c:otherwise>
                                        </c:choose>
                                        • ${f.created_at}
                                    </div>
                                </div>
                                <div class="text-end">
                                    <div class="badge bg-primary">${f.rating}/5</div>
                                </div>
                            </div>
                            <p class="mt-2 mb-1">${f.comments}</p>

                            <c:if test="${currentCustomerId != null && f.customer != null && f.customer.id == currentCustomerId}">
                                <!-- Edit and delete forms only visible to the author -->
                                <div class="d-flex gap-2">
                                    <button class="btn btn-sm btn-outline-secondary" type="button" data-bs-toggle="collapse" data-bs-target="#editFb${f.id}">Edit</button>
                                    <form method="post" action="${pageContext.request.contextPath}/customer/feedback/delete" onsubmit="return confirm('Delete this feedback?');">
                                        <input type="hidden" name="feedback_id" value="${f.id}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                                    </form>
                                </div>

                                <div class="collapse mt-2" id="editFb${f.id}">
                                    <form method="post" action="${pageContext.request.contextPath}/customer/feedback/edit">
                                        <input type="hidden" name="feedback_id" value="${f.id}" />
                                        <div class="row g-2 align-items-center">
                                            <div class="col-auto">
                                                <select name="rating" class="form-select form-select-sm">
                                                    <option value="5" <c:if test="${f.rating == 5}">selected</c:if>>5</option>
                                                    <option value="4" <c:if test="${f.rating == 4}">selected</c:if>>4</option>
                                                    <option value="3" <c:if test="${f.rating == 3}">selected</c:if>>3</option>
                                                    <option value="2" <c:if test="${f.rating == 2}">selected</c:if>>2</option>
                                                    <option value="1" <c:if test="${f.rating == 1}">selected</c:if>>1</option>
                                                </select>
                                            </div>
                                            <div class="col">
                                                <input type="text" name="comments" value="${f.comments}" class="form-control form-control-sm" />
                                            </div>
                                            <div class="col-auto">
                                                <button type="submit" class="btn btn-sm btn-primary">Save</button>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </c:if>
                        </div>
                    </div>
                    <hr />
                </c:forEach>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
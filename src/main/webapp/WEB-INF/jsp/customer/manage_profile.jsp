<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="container mt-5">
    <h2>Manage Profile</h2>

    <!-- Profile summary table -->
    <div class="card mb-4 shadow-sm">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-bordered table-hover mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Area</th>
                            <th>Care Needs</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>${customer.name}</td>
                            <td>${customer.email}</td>
                            <td>${customer.phone}</td>
                            <td>${customer.address}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty customer.carePreferences}">${customer.carePreferences}</c:when>
                                    <c:when test="${not empty customer.medicalNotes}">${customer.medicalNotes}</c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <!-- Edit scrolls down to the form; other actions can be added later -->
                                <a class="btn btn-sm btn-outline-primary me-2" href="#editForm">Edit</a>
                                <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/customer/booking-history">My Bookings</a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Editable form (kept from original) -->
    <a id="editForm"></a>
    <form method="post" action="${pageContext.request.contextPath}/customer/update-profile" class="mt-3">
        <div class="mb-3">
            <label class="form-label">Name</label>
            <input class="form-control" name="name" value="${customer.name}">
        </div>
        <div class="mb-3">
            <label class="form-label">Phone</label>
            <input class="form-control" name="phone" value="${customer.phone}">
        </div>
        <div class="mb-3">
            <label class="form-label">Area / Address</label>
            <input class="form-control" name="address" value="${customer.address}">
        </div>
        <div class="mb-3">
            <label class="form-label">Care Preferences</label>
            <textarea class="form-control" name="carePreferences" rows="2">${customer.carePreferences}</textarea>
        </div>
        <div class="mb-3">
            <label class="form-label">Medical Notes</label>
            <textarea class="form-control" name="medicalNotes" rows="3">${customer.medicalNotes}</textarea>
        </div>
        <button class="btn btn-primary">Update</button>
    </form>
</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../header.jsp" />

<h2 class="text-primary mb-3">Book This Service</h2>
<div class="card shadow-sm border-0 mb-4">
    <div class="card-body">
        <h4 class="card-title text-primary">${service.service_name}</h4>
        <p class="text-muted">${service.description}</p>
        <p class="fw-bold">Price: $${service.price}</p>
    </div>
</div>

<form method="post" action="/customer/book-service/${service.id}" class="card p-4 shadow-sm bg-white">
    <div class="mb-3">
        <label class="form-label">Select Date</label>
        <input type="date" name="date" class="form-control" required>
    </div>
    <button class="btn btn-primary">Confirm Booking</button>
</form>
<a href="adding_to_cart.jsp?id=${service.id}">Add to Cart</a>

<jsp:include page="../footer.jsp" />


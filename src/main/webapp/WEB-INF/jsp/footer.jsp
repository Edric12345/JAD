</div> <!-- closes container from header -->
<%
Boolean cartEmpty = (Boolean) request.getAttribute("cartEmpty");
%>

<% if (cartEmpty != null && cartEmpty) { %>
<script>
    alert("🛒 Your cart is empty. Please add a service before checking out.");
</script>
<% } %>

<footer class="mt-auto bg-primary text-white text-center py-3">
    <p class="mb-0">&copy; 2025 SilverCare. All Rights Reserved.</p>

</footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>


</body>
</html>
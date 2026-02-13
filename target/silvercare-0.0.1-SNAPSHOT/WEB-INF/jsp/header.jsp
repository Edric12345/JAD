<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>SilverCare</title>

    <!-- BOOTSTRAP 5 CSS -->
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">

    <!-- Font Awesome Icons -->
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

    <style>
        .nav-link.active {
            font-weight: bold;
            border-bottom: 2px solid #fff;
        }
        .navbar-brand {
            font-size: 1.4rem;
            letter-spacing: 0.5px;
        }
        .dropdown-menu a:hover {
            background-color: #e3f2fd;
        }
        body {
            padding-top: 70px; /* For sticky navbar */
        }
    </style>
</head>

<body class="bg-light d-flex flex-column min-vh-100">


<!-- NAVBAR (STICKY) -->
<nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm fixed-top">
    <div class="container">

        <!-- LOGO -->
        <a class="navbar-brand fw-bold" href="<%= request.getContextPath() %>/index.jsp">
            <i class="fa-solid fa-hand-holding-heart me-2"></i> SilverCare
        </a>

        <!-- Toggle -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- MENU -->
        <div class="collapse navbar-collapse" id="navMenu">

            <!-- LEFT NAV -->
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <li class="nav-item">
                    <a class="nav-link <%= request.getRequestURI().contains("index.jsp") ? "active" : "" %>"
                       href="<%= request.getContextPath() %>/index.jsp">
                        <i class="fa-solid fa-house me-1"></i> Home
                    </a>
                </li>

                <!-- SERVICES DROPDOWN -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle <%= request.getRequestURI().contains("service") ? "active" : "" %>"
                       href="#" id="servicesDropdown" role="button" data-bs-toggle="dropdown">
                        <i class="fa-solid fa-list me-1"></i> Services
                    </a>
                    <ul class="dropdown-menu">
                        <li>
                            <a class="dropdown-item"
                               href="<%= request.getContextPath() %>/public/service_category.jsp">
                                Browse Categories
                            </a>
                        </li>
                    </ul>
                </li>

                <li class="nav-item">
                    <a class="nav-link <%= request.getRequestURI().contains("feedback") ? "active" : "" %>"
                       href="<%= request.getContextPath() %>/public/feedback.jsp">
                        <i class="fa-solid fa-comment-dots me-1"></i> Feedback
                    </a>
                </li>
            </ul>

            <!-- RIGHT NAV (USER SIDE) -->
            <ul class="navbar-nav ms-auto">

                <%
                    Integer cid = (Integer) session.getAttribute("customer_id");
                    String cname = (String) session.getAttribute("customer_name");
                    String adminUser = (String) session.getAttribute("admin_username");
                %>

                <!-- CUSTOMER LOGGED IN -->
                <% if (cid != null) { %>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle text-white"
                           href="#" role="button" data-bs-toggle="dropdown">
                            <i class="fa-solid fa-user me-1"></i> <%= cname %>
                        </a>

                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item"
                                   href="<%= request.getContextPath() %>/customer/customer_home.jsp">
                                My Account
                            </a></li>
                            <li><a class="dropdown-item"
                                   href="<%= request.getContextPath() %>/customer/booking_history.jsp">
                                Booking History
                            </a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item text-danger"
                                   href="<%= request.getContextPath() %>/logout.jsp">
                                Logout
                            </a></li>
                        </ul>
                    </li>

                <!-- ADMIN LOGGED IN -->
                <% } else if (adminUser != null) { %>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle text-warning fw-bold"
                           href="#" role="button" data-bs-toggle="dropdown">
                            <i class="fa-solid fa-user-shield me-1"></i> Admin: <%= adminUser %>
                        </a>

                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item"
                                   href="<%= request.getContextPath() %>/admin/admin_dashboard.jsp">
                                Dashboard
                            </a></li>
                            <li><a class="dropdown-item"
                                   href="<%= request.getContextPath() %>/admin/manage_services.jsp">
                                Manage Services
                            </a></li>
                            <li><a class="dropdown-item"
                                   href="<%= request.getContextPath() %>/admin/manage_clients.jsp">
                                Manage Clients
                            </a></li>

                            <li><hr class="dropdown-divider"></li>

                            <li><a class="dropdown-item text-danger"
                                   href="<%= request.getContextPath() %>/logout.jsp">
                                Logout
                            </a></li>
                        </ul>
                    </li>

                <!-- NO ONE LOGGED IN -->
                <% } else { %>

                    <li class="nav-item">
                        <a class="nav-link" href="<%= request.getContextPath() %>/login.jsp">
                            <i class="fa-solid fa-right-to-bracket me-1"></i> Login
                        </a>
                    </li>

                    <li class="nav-item">
                        <a class="nav-link" href="<%= request.getContextPath() %>/register.jsp">
                            <i class="fa-solid fa-user-plus me-1"></i> Register
                        </a>
                    </li>

                    <li class="nav-item">
                        <a class="nav-link text-warning fw-bold"
                           href="<%= request.getContextPath() %>/admin/admin_login.jsp">
                            <i class="fa-solid fa-user-shield me-1"></i> Admin Login
                        </a>
                    </li>

                <% } %>

            </ul>
        </div>
    </div>
</nav>

<!-- PAGE CONTENT WRAPPER -->
<div class="container py-4">


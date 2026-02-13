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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

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
        /* Utilities used by booking/manage pages */
        .btn-fullwidth { width: 100%; }
        .card-note { color: #555; font-size: 0.95rem; }
    </style>
</head>

<body class="bg-light d-flex flex-column min-vh-100">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Compute the request URI once as a JSTL variable -->
<c:set var="_uri" value="${pageContext.request.requestURI}" />

<!-- Compute the application root with a trailing slash safely (handles empty context path) -->
<c:choose>
    <c:when test="${empty pageContext.request.contextPath}">
        <c:set var="ctxRoot" value="/" />
    </c:when>
    <c:otherwise>
        <c:set var="ctxRoot" value="${pageContext.request.contextPath}/" />
    </c:otherwise>
</c:choose>

<!-- NAVBAR (STICKY) -->
<nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm fixed-top">
    <div class="container">

        <!-- LOGO -->
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">
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
                <c:choose>
                    <c:when test="${not empty sessionScope.admin_username}">
                        <li class="nav-item">
                            <a class="nav-link ${fn:contains(_uri, '/admin/manage-services') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/manage-services">
                                <i class="fa-solid fa-list me-1"></i> Manage Services
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link ${fn:contains(_uri, '/admin/manage-clients') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/manage-clients">
                                <i class="fa-solid fa-users me-1"></i> Manage Clients
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link ${fn:contains(_uri, '/admin/feedback') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/feedback">
                                <i class="fa-solid fa-comment-dots me-1"></i> View Feedback
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link ${fn:contains(_uri, '/admin/manage-billings') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/manage-billings">
                                <i class="fa-solid fa-file-invoice-dollar me-1"></i> Billing &amp; Reports
                            </a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link ${_uri eq ctxRoot ? 'active' : ''}"
                               href="${ctxRoot}">
                                <i class="fa-solid fa-house me-1"></i> Home
                            </a>
                        </li>

                        <!-- SERVICES DROPDOWN -->
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle ${fn:contains(_uri, 'service') ? 'active' : ''}"
                               href="#" id="servicesDropdown" role="button" data-bs-toggle="dropdown">
                                <i class="fa-solid fa-list me-1"></i> Services
                            </a>
                            <ul class="dropdown-menu">
                                <li>
                                   <a class="dropdown-item" href="${pageContext.request.contextPath}/customer/services/categories">
                                            Browse Categories
                                      </a>
                                </li>
                                <li>
                                   <a class="dropdown-item" href="${pageContext.request.contextPath}/b2b/index.jsp">
                                            Partner API (B2B)
                                      </a>
                                </li>
                            </ul>
                        </li>

                        <li class="nav-item">
                            <a class="nav-link ${fn:contains(_uri, 'feedback') ? 'active' : ''}"
                               href="${pageContext.request.contextPath}/customer/feedback">
                                <i class="fa-solid fa-comment-dots me-1"></i> Feedback
                            </a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>

            <!-- RIGHT NAV (USER SIDE) -->
            <ul class="navbar-nav ms-auto">

                <!-- CART (visible to all, links to checkout or login) -->
                <li class="nav-item me-2">
                    <c:choose>
                        <c:when test="${not empty sessionScope.customer_id}">
                            <a class="nav-link position-relative cart-link" href="${pageContext.request.contextPath}/customer/checkout" data-cart-count="${fn:length(sessionScope.cart)}">
                                <i class="fa-solid fa-cart-shopping"></i>
                                <span class="badge bg-danger rounded-pill position-absolute" style="top:0;right:-8px;font-size:0.7rem;"> ${fn:length(sessionScope.cart)} </span>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="nav-link position-relative" href="${pageContext.request.contextPath}/customer/login">
                                <i class="fa-solid fa-cart-shopping"></i>
                                <span class="badge bg-danger rounded-pill position-absolute" style="top:0;right:-8px;font-size:0.7rem;"> 0 </span>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </li>

                <!-- CUSTOMER LOGGED IN -->
                <c:choose>
                    <c:when test="${not empty sessionScope.customer_id}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-white" href="#" role="button" data-bs-toggle="dropdown">
                                <i class="fa-solid fa-user me-1"></i> ${sessionScope.customer_name}
                            </a>

                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/profile">My Account</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/booking-history">Booking History</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/my-bookings">My Bookings</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/customer/logout">Logout</a></li>
                            </ul>
                        </li>
                    </c:when>
                    <c:when test="${not empty sessionScope.admin_username}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-warning fw-bold" href="#" role="button" data-bs-toggle="dropdown">
                                <i class="fa-solid fa-user-shield me-1"></i> Admin: ${sessionScope.admin_username}
                            </a>

                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/manage-services">Manage Services</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/manage-clients">Manage Clients</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/feedback">View Feedback</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/manage-billings">Billing &amp; Reports</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/admin/logout">Logout</a></li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/customer/login"><i class="fa-solid fa-right-to-bracket me-1"></i> Login</a></li>
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/customer/register"><i class="fa-solid fa-user-plus me-1"></i> Register</a></li>
                        <li class="nav-item"><a class="nav-link text-warning fw-bold" href="${pageContext.request.contextPath}/admin/login"><i class="fa-solid fa-user-shield me-1"></i> Admin Login</a></li>
                    </c:otherwise>
                </c:choose>

            </ul>
        </div>
    </div>
</nav>

<!-- Promotion banner placeholder -->
<div id="promotion-banner-placeholder" class="container mt-2"></div>

<!-- PAGE CONTENT WRAPPER -->
<div class="container py-4">

<script>
// Tiny WebAudio SFX engine: click + success tones
(function(){
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return; // no audio support
    const ctx = new AudioContext();

    function playTone(freq, type='sine', dur=0.08, vol=0.12){
        const o = ctx.createOscillator();
        const g = ctx.createGain();
        o.type = type; o.frequency.value = freq;
        g.gain.value = vol;
        o.connect(g); g.connect(ctx.destination);
        o.start();
        g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + dur);
        setTimeout(()=>{ try{ o.stop(); }catch(e){} }, dur*1000 + 20);
    }

    window.sfx = {
        click: function(){ try{ playTone(880,'sine',0.05,0.07); }catch(e){} },
        success: function(){ try{ playTone(660,'triangle',0.09,0.12); setTimeout(()=>playTone(880,'sine',0.06,0.09),90); }catch(e){} },
        error: function(){ try{ playTone(220,'sawtooth',0.12,0.14); }catch(e){} }
    };

    // Play click on interactive elements
    document.addEventListener('pointerdown', function(e){
        const t = e.target.closest('button,a,input[type=submit]');
        if (!t) return;
        // avoid playing on text inputs etc.
        if (t.matches('a') && t.getAttribute('href') && t.getAttribute('href').startsWith('mailto:')) return;
        try{ sfx.click(); }catch(e){}
    }, {passive:true});
})();
</script>

<!-- flash-sfx: if a page shows a success flash message, call sfx.success() -->
<script>
document.addEventListener('DOMContentLoaded', function(){
    try{
        if (typeof window.sfx === 'object'){
            // If any element with data-sfx="success-on-load" exists, play success
            if (document.querySelector('[data-sfx="success-on-load"]')) window.sfx.success();
            if (document.querySelector('[data-sfx="error-on-load"]')) window.sfx.error();
        }
    }catch(e){}
});
</script>

<!-- Fetch active header promotions and render the top banner -->
<script>
(function(){
    const target = 'header';
    fetch(window.location.origin + '/api/promotions/active?target=' + encodeURIComponent(target))
        .then(r => r.ok ? r.json() : Promise.reject(r))
        .then(list => {
            if (!Array.isArray(list) || list.length === 0) return;
            const p = list[0]; // pick highest priority
            const placeholder = document.getElementById('promotion-banner-placeholder');
            if (!placeholder) return;

            // Build the inner HTML without using ES template sequences so JSP EL doesn't try to parse them.
            var imgHtml = p.image_path ? '<img src="' + p.image_path + '" style="height:56px;object-fit:cover;margin-right:12px;border-radius:6px;" />' : '';
            var titleHtml = '<div class="fw-bold">' + (p.title || '') + '</div>';
            var summaryHtml = '<div class="small">' + (p.summary || '') + '</div>';
            var linkHtml = '<a class="btn btn-sm btn-outline-light" href="' + (window.location.origin + '/promotions/' + (p.id || '')) + '">Learn more</a>';

            var html = '' +
                '<div class="alert alert-info rounded-3 d-flex align-items-center" role="alert">' +
                imgHtml +
                '<div>' + titleHtml + summaryHtml + '</div>' +
                '<div class="ms-auto">' + linkHtml + '</div>' +
                '</div>';

            placeholder.innerHTML = html;
        }).catch(()=>{});
})();
</script>

package com.silvercare.config;

import com.silvercare.servlet.AdminServlet;
import com.silvercare.servlet.CustomerServlet;
import com.silvercare.servlet.PublicServlet;
import com.silvercare.servlet.PartnerBookingServlet;
import jakarta.servlet.ServletContext;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletRegistrar {

    // Force-register the traditional servlets so /customer/* and related paths are handled by the
    // existing HttpServlet implementations. This avoids relying on @WebServlet scanning which
    // can sometimes be disabled or not picked up in certain Spring Boot setups.

    @Bean
    public ServletRegistrationBean<CustomerServlet> customerServlet(ServletContext sc) {
        ServletRegistrationBean<CustomerServlet> bean = new ServletRegistrationBean<>(new CustomerServlet(), "/customer/*");
        bean.setName("CustomerServletBean");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<AdminServlet> adminServlet(ServletContext sc) {
        ServletRegistrationBean<AdminServlet> bean = new ServletRegistrationBean<>(new AdminServlet(), "/admin/*");
        bean.setName("AdminServletBean");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<PublicServlet> publicServlet(ServletContext sc) {
        ServletRegistrationBean<PublicServlet> bean = new ServletRegistrationBean<>(new PublicServlet(), "/services/*", "/welcome");
        bean.setName("PublicServletBean");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<PartnerBookingServlet> partnerServlet(ServletContext sc) {
        ServletRegistrationBean<PartnerBookingServlet> bean = new ServletRegistrationBean<>(new PartnerBookingServlet(), "/partner/bookings");
        bean.setName("PartnerBookingServletBean");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
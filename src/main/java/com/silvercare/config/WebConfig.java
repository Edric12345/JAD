package com.silvercare.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply to customer and admin areas, exclude public assets and login/register paths
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/customer/**", "/admin/**")
                .excludePathPatterns("/customer/login", "/customer/register", "/admin/login", "/static/**", "/resources/**", "/api/**");
    }
}

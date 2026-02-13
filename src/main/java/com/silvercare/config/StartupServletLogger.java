package com.silvercare.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StartupServletLogger implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StartupServletLogger.class);

    private final ServletContext servletContext;

    public StartupServletLogger(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Map<String, ? extends ServletRegistration> regs = servletContext.getServletRegistrations();
        logger.info("--- Registered Servlets at startup ({} entries) ---", regs.size());
        regs.forEach((name, reg) -> {
            logger.info("Servlet: {} -> class={}, mappings={}", name, reg.getClassName(), reg.getMappings());
        });
        logger.info("--- End Registered Servlets ---");
    }
}

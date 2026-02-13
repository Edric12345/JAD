package com.silvercare.restController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DevServletInspector {

    @Autowired
    private ServletContext servletContext;

    @GetMapping("/internal/servlets")
    public Map<String, Object> listServlets() {
        var regs = servletContext.getServletRegistrations();
        return regs.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Map.of(
                        "class", e.getValue().getClassName(),
                        "mappings", e.getValue().getMappings()
                )
        ));
    }

    @GetMapping("/internal/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
